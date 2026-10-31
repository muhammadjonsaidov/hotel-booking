package hotelbooking;

import java.time.LocalDate;
import java.util.List;

final class BookingServiceTestSupport {
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
}
