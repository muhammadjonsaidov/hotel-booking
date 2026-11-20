package hotelbooking;

// The four API tests: HTTP in, HTTP out, with the reservations in a list.
// Run them with the other tests from the hotel-booking folder:  ./gradlew test
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import hotelbooking.api.AvailabilityCache;
import hotelbooking.api.CountingReservationStore;
import hotelbooking.api.HotelApi;
import hotelbooking.api.Stats;
import hotelbooking.notifications.Confirmation;
import hotelbooking.notifications.NotificationCounts;
import hotelbooking.notifications.internal.EmailNotificationSender;
import hotelbooking.notifications.internal.QueuedNotificationSender;
import hotelbooking.reservations.BookingResult;
import hotelbooking.reservations.BookingService;
import hotelbooking.reservations.DateRange;
import hotelbooking.reservations.Guest;
import hotelbooking.reservations.OverlapRejectedException;
import hotelbooking.reservations.Reservation;
import hotelbooking.reservations.internal.InMemoryReservationStore;
import hotelbooking.rooms.Room;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ApiTests {
    static final List<Room> ROOMS = List.of(new Room("R7", "Standard"), new Room("R8", "Deluxe"));
    static final DateRange NOV_12_14 = new DateRange(LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 14));
    static final String SEARCH = "/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14";
    static final String MAYA = """
        {"guestName":"Maya","guestEmail":"maya@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}""";
    static final String OMAR = """
        {"guestName":"Omar","guestEmail":"omar@example.com","room":"R7","checkIn":"2026-11-13","checkOut":"2026-11-15"}""";
    static final String BOTH_FREE = """
        {"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}""";
    static final String ONLY_R8 = """
        {"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R8","type":"Deluxe"}],"next":null}""";

    // Redis's three commands on a map. The tests run in a second, so the 30 s TTL never ends.
    static class InMemoryKeyValueStore implements AvailabilityCache.KeyValueStore {
        private final Map<String, String> values = new HashMap<>();

        @Override
        public String get(String key) {
            return values.get(key);
        }

        @Override
        public void set(String key, String value, int ttlSeconds) {
            values.put(key, value);
        }

        @Override
        public void increment(String key) {
            values.put(key, String.valueOf(Long.parseLong(values.getOrDefault(key, "0")) + 1));
        }
    }

    // The web app with the in-memory store and cache; MockMvc sends requests to it without a network.
    static MockMvc newApp() {
        Stats stats = new Stats();
        BookingService service = new BookingService(ROOMS, new CountingReservationStore(new InMemoryReservationStore(), stats), new EmailNotificationSender());
        // A list in memory has nothing to save at the end of a request.
        return MockMvcBuilders.standaloneSetup(new HotelApi(ROOMS, work -> work.apply(service), stats, new AvailabilityCache(new InMemoryKeyValueStore()))).build();
    }

    @Test
    void searchAnswers200WithTheFreeRooms() throws Exception {
        newApp().perform(get(SEARCH))
            .andExpect(status().isOk())
            .andExpect(content().string("""
                {"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}"""));
    }

    @Test
    void bookingAnswers201Then409ForASharedNight() throws Exception {
        MockMvc app = newApp();
        app.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content(MAYA))
            .andExpect(status().isCreated())
            .andExpect(content().string("""
                {"id":"R101","status":"Confirmed","notificationFailed":false}"""));
        app.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content(OMAR))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Unavailable"));
    }

    @Test
    void wrongDatesAnswer400WithProblemDetails() throws Exception {
        newApp().perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content(MAYA.replace("\"checkOut\":\"2026-11-14\"", "\"checkOut\":\"2026-11-12\"")))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(content().string("""
                {"title":"InvalidDates","status":400,"detail":"checkOut must be after checkIn."}"""));
    }

    @Test
    void cancelAnswers204Then404() throws Exception {
        MockMvc app = newApp();
        app.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content(MAYA));
        app.perform(delete("/reservations/R101")).andExpect(status().isNoContent());
        app.perform(delete("/reservations/R101"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("NotFound"));
    }

    @Test
    void theSecondSearchIsAnsweredByTheCache() throws Exception {
        MockMvc app = newApp();
        String first = app.perform(get(SEARCH)).andReturn().getResponse().getContentAsString();
        String second = app.perform(get(SEARCH)).andReturn().getResponse().getContentAsString();
        assertEquals(first, second);
        app.perform(get("/stats")).andExpect(content().string("""
            {"searches":2,"databaseQueries":2,"cacheHits":1,"cacheMisses":1}"""));
    }

    @Test
    void aBookingAndACancelMakeTheNextSearchFresh() throws Exception {
        MockMvc app = newApp();
        app.perform(get(SEARCH)).andExpect(content().string(BOTH_FREE));
        app.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content(MAYA)).andExpect(status().isCreated());
        app.perform(get(SEARCH)).andExpect(content().string(ONLY_R8));
        app.perform(delete("/reservations/R101")).andExpect(status().isNoContent());
        app.perform(get(SEARCH)).andExpect(content().string(BOTH_FREE));
    }

    // ---------------------------------------------------------------- P09
    @Test
    void two_bookings_at_the_same_moment_give_the_room_one_reservation() throws Exception {
        // Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
        InMemoryReservationStore rows = new InMemoryReservationStore();
        Stats stats = new Stats();
        BookingService service = new BookingService(ROOMS, new CountingReservationStore(rows, stats),
                new EmailNotificationSender(), 300);

        // Two threads, both inside the gap between the check and the save.
        var maya = CompletableFuture.supplyAsync(
                () -> service.reserve(new Guest("Maya", "maya@example.com"), ROOMS.get(0), NOV_12_14));
        var omar = CompletableFuture.supplyAsync(
                () -> service.reserve(new Guest("Omar", "omar@example.com"), ROOMS.get(0), NOV_12_14));

        var outcomes = Stream.of(maya.join(), omar.join())
                .map(BookingResult::outcome).sorted().toList();
        assertEquals(List.of("Accepted", "Unavailable"), outcomes);
        assertEquals(1, rows.countConfirmed(ROOMS.get(0), NOV_12_14));
    }

    @Test
    void the_store_refuses_an_overlapping_save_even_without_the_pre_check() {
        // The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
        // Nothing calls hasOverlap here: save itself must refuse.
        InMemoryReservationStore store = new InMemoryReservationStore();
        store.save(new Reservation("R101", new Guest("Maya", "maya@example.com"), ROOMS.get(0), NOV_12_14));

        Reservation overlapping = new Reservation("R102", new Guest("Omar", "omar@example.com"), ROOMS.get(0),
                new DateRange(LocalDate.of(2026, 11, 13), LocalDate.of(2026, 11, 15)));
        assertThrows(OverlapRejectedException.class, () -> store.save(overlapping));
        assertEquals(1, store.countConfirmed(ROOMS.get(0), NOV_12_14));
    }

    // ---------------------------------------------------------------- P10
    // One confirmation for the queue tests; only the queue's behaviour matters, not the contents.
    static Confirmation aConfirmation(int number) {
        return new Confirmation("R" + (101 + number), "guest" + number + "@example.com", "R7",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 2));
    }

    @Test
    void a_booking_does_not_wait_for_a_slow_confirmation() {
        // The whole point of the queue: a one-second email must not cost the guest a second.
        EmailNotificationSender slow = new EmailNotificationSender(1);
        QueuedNotificationSender notifications = new QueuedNotificationSender(slow, 10, 1);

        long started = System.nanoTime();
        notifications.sendConfirmation(aConfirmation(1));
        long handedOverMs = (System.nanoTime() - started) / 1_000_000;

        assertTrue(handedOverMs < 100, "handing the email to the queue must be immediate");
        assertEquals(0, slow.sent(), "and the email has not been sent yet");
        assertEquals(1, notifications.counts().accepted());
        notifications.close(false);
    }

    @Test
    void a_burst_larger_than_the_bound_is_refused_and_counted() {
        // Thirty confirmations into a queue of ten. The exact split depends on whether the worker
        // got a turn during the burst, so what is asserted is what the bound promises rather than
        // a number.
        QueuedNotificationSender notifications =
                new QueuedNotificationSender(new EmailNotificationSender(1), 10, 1);

        for (int number = 0; number < 30; number++) {
            notifications.sendConfirmation(aConfirmation(number));
        }

        NotificationCounts counts = notifications.counts();
        assertEquals(30, counts.accepted() + counts.rejected());
        assertTrue(counts.queued() <= 10, "the queue never grows past its bound");
        assertTrue(counts.rejected() > 0, "and the bound really did refuse some");
        // Not an equality: a worker may be part-way through sending one job, so it is neither
        // queued nor completed yet. At most one per worker can be in flight.
        long inFlight = counts.accepted() - counts.completed() - counts.queued();
        assertTrue(inFlight >= 0 && inFlight <= 1, "at most one job per worker can be in flight");
        notifications.close(false);
    }

    @Test
    void a_drained_shutdown_finishes_everything_it_accepted() {
        EmailNotificationSender slow = new EmailNotificationSender(0.02);
        QueuedNotificationSender notifications = new QueuedNotificationSender(slow, 10, 1);
        for (int number = 0; number < 10; number++) {
            notifications.sendConfirmation(aConfirmation(number));
        }

        notifications.close(true);

        NotificationCounts counts = notifications.counts();
        assertEquals(10, counts.accepted());
        assertEquals(10, counts.completed());
        assertEquals(0, counts.queued());
        assertEquals(10, slow.sent());
    }
}
