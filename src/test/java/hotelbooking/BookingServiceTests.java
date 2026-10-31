package hotelbooking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

// A test sender that records reservation ids or fails on purpose.
class FakeNotificationSender implements NotificationSender {
    private final boolean fail;
    final List<String> sent = new ArrayList<>();

    FakeNotificationSender(boolean fail) {
        this.fail = fail;
    }

    @Override
    public void sendConfirmation(Reservation reservation) {
        if (fail) throw new IllegalStateException("The mail server is down.");
        sent.add(reservation.id());
    }
}

class BookingServiceTests {
    static final Room R7 = new Room("R7", "Standard");
    static final Room R8 = new Room("R8", "Deluxe");
    static final Guest MAYA = new Guest("Maya", "maya@example.com");
    static final Guest OMAR = new Guest("Omar", "omar@example.com");
    static final Guest NORA = new Guest("Nora", "nora@example.com");

    static DateRange nov(int checkIn, int checkOut) {
        return new DateRange(LocalDate.of(2026, 11, checkIn), LocalDate.of(2026, 11, checkOut));
    }

    // The only place that builds the service, so construction changes stay in one place.
    record Fixture(BookingService service, InMemoryReservationStore store, FakeNotificationSender sender) {}

    static Fixture newService(boolean failingSender) {
        InMemoryReservationStore store = new InMemoryReservationStore();
        FakeNotificationSender sender = new FakeNotificationSender(failingSender);
        return new Fixture(new BookingService(List.of(R7, R8), store, sender), store, sender);
    }

    @Test
    void maya_gets_R7_when_it_is_free() {
        Fixture fixture = newService(false);
        assertEquals("Accepted R101", fixture.service().reserve(MAYA, R7, nov(12, 14)).toString());
    }

    @Test
    void omar_is_refused_when_a_night_is_shared() {
        Fixture fixture = newService(false);
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertEquals("Unavailable", fixture.service().reserve(OMAR, R7, nov(13, 15)).toString());
        assertEquals(1, fixture.store().count());
    }

    @Test
    void nora_can_arrive_on_the_day_maya_leaves() {
        Fixture fixture = newService(false);
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertEquals("Accepted R102", fixture.service().reserve(NORA, R7, nov(14, 16)).toString());
    }

    @Test
    void check_out_must_be_after_check_in() {
        Fixture fixture = newService(false);
        assertEquals("InvalidDates", fixture.service().reserve(OMAR, R8, nov(15, 15)).toString());
        assertEquals(0, fixture.store().count());
    }

    @Test
    void cancelling_frees_the_room_again() {
        Fixture fixture = newService(false);
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertTrue(fixture.service().cancel("R101"));
        assertTrue(fixture.service().findFreeRooms(nov(12, 14)).contains(R7));
    }

    @Test
    void one_confirmation_per_accepted_booking() {
        Fixture fixture = newService(false);
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        fixture.service().reserve(OMAR, R7, nov(13, 15));
        fixture.service().reserve(NORA, R7, nov(14, 16));
        assertEquals(List.of("R101", "R102"), fixture.sender().sent);
    }

    @Test
    void a_failed_confirmation_keeps_the_booking() {
        Fixture fixture = newService(true);
        BookingResult result = fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertEquals("Accepted R101", result.toString());
        assertTrue(result.notificationFailed());
        assertEquals(1, fixture.store().count());
    }

    @Test
    void sms_confirmations_need_no_change_to_BookingService() {
        SmsNotificationSender sms = new SmsNotificationSender();
        BookingService service = new BookingService(List.of(R7, R8), new InMemoryReservationStore(), sms);
        service.reserve(MAYA, R7, nov(12, 14));
        assertEquals(1, sms.sent());
    }
}
