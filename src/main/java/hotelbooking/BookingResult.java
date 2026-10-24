package hotelbooking;

public record BookingResult(String outcome, String reservationId) {
    public BookingResult(String outcome) {
        this(outcome, null);
    }

    @Override
    public String toString() {
        return reservationId == null ? outcome : outcome + " " + reservationId;
    }
}
