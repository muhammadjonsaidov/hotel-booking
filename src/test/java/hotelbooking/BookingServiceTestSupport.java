package hotelbooking;

import java.time.LocalDate;
import java.util.List;

public final class BookingServiceTestSupport {
    private BookingServiceTestSupport() {}

    public static final Room R7 = new Room("R7", "Standard");
    public static final Room R8 = new Room("R8", "Deluxe");
    public static final Guest MAYA = new Guest("Maya", "maya@example.com");
    public static final Guest OMAR = new Guest("Omar", "omar@example.com");
    public static final Guest NORA = new Guest("Nora", "nora@example.com");

    public static DateRange nov(int checkIn, int checkOut) {
        return new DateRange(LocalDate.of(2026, 11, checkIn), LocalDate.of(2026, 11, checkOut));
    }

    // The only place that builds the service, so construction changes stay in one place.
    public record Fixture(BookingService service, InMemoryReservationStore store, FakeNotificationSender sender) {}

    public static Fixture newService(boolean failingSender) {
        InMemoryReservationStore store = new InMemoryReservationStore();
        FakeNotificationSender sender = new FakeNotificationSender(failingSender);
        return new Fixture(new BookingService(List.of(R7, R8), store, sender), store, sender);
    }
}
