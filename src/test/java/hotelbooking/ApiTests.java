package hotelbooking;

// The four API tests: HTTP in, HTTP out, with the reservations in a list.
// Run them with the other tests from the hotel-booking folder:  ./gradlew test
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import hotelbooking.api.AvailabilityCache;
import hotelbooking.api.CountingReservationStore;
import hotelbooking.api.HotelApi;
import hotelbooking.api.Stats;
import hotelbooking.notifications.internal.EmailNotificationSender;
import hotelbooking.reservations.BookingService;
import hotelbooking.reservations.internal.InMemoryReservationStore;
import hotelbooking.rooms.Room;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ApiTests {
    static final List<Room> ROOMS = List.of(new Room("R7", "Standard"), new Room("R8", "Deluxe"));
    static final String SEARCH = "/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14";
    static final String MAYA = """
        {"guestName":"Maya","guestEmail":"maya@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}""";
    static final String OMAR = """
        {"guestName":"Omar","guestEmail":"omar@example.com","room":"R7","checkIn":"2026-11-13","checkOut":"2026-11-15"}""";

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
}
