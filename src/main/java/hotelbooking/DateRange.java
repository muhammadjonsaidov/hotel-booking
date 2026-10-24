package hotelbooking;

import java.time.LocalDate;

// Check-in is included; check-out is excluded.
public record DateRange(LocalDate checkIn, LocalDate checkOut) {
    public boolean isValid() {
        return checkOut.isAfter(checkIn);
    }

    // Two stays overlap when they share at least one night.
    public boolean overlaps(DateRange other) {
        return checkIn.isBefore(other.checkOut) && other.checkIn.isBefore(checkOut);
    }
}
