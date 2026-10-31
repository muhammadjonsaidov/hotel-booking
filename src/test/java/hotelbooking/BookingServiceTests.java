package hotelbooking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

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
    record Fixture(BookingService service, ReservationStore store, EmailSender email) {}

    static Fixture newService() {
        ReservationStore store = new ReservationStore();
        EmailSender email = new EmailSender();
        return new Fixture(new BookingService(List.of(R7, R8), store, email), store, email);
    }

    @Test
    void maya_gets_R7_when_it_is_free() {
        Fixture fixture = newService();
        assertEquals("Accepted R101", fixture.service().reserve(MAYA, R7, nov(12, 14)).toString());
    }

    @Test
    void omar_is_refused_when_a_night_is_shared() {
        Fixture fixture = newService();
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertEquals("Unavailable", fixture.service().reserve(OMAR, R7, nov(13, 15)).toString());
        assertEquals(1, fixture.store().count());
    }

    @Test
    void nora_can_arrive_on_the_day_maya_leaves() {
        Fixture fixture = newService();
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertEquals("Accepted R102", fixture.service().reserve(NORA, R7, nov(14, 16)).toString());
    }

    @Test
    void check_out_must_be_after_check_in() {
        Fixture fixture = newService();
        assertEquals("InvalidDates", fixture.service().reserve(OMAR, R8, nov(15, 15)).toString());
        assertEquals(0, fixture.store().count());
    }

    @Test
    void cancelling_frees_the_room_again() {
        Fixture fixture = newService();
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertTrue(fixture.service().cancel("R101"));
        assertTrue(fixture.service().findFreeRooms(nov(12, 14)).contains(R7));
    }

    @Test
    void one_confirmation_per_accepted_booking() {
        Fixture fixture = newService();
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        fixture.service().reserve(OMAR, R7, nov(13, 15));
        fixture.service().reserve(NORA, R7, nov(14, 16));
        assertEquals(2, fixture.email().sent());
    }
}
