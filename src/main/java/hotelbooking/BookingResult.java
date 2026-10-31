package hotelbooking;

public record BookingResult(String outcome, String reservationId, boolean notificationFailed) {
    public BookingResult(String outcome) {
        this(outcome, null, false);
    }

    public BookingResult(String outcome, String reservationId) {
        this(outcome, reservationId, false);
    }

    @Override
    public String toString() {
        return reservationId == null ? outcome : outcome + " " + reservationId;
    }
}
