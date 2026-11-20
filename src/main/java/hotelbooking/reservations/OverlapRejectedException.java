package hotelbooking.reservations;

// Thrown by ReservationStore.save when writing a reservation would give the room two Confirmed
// reservations covering the same night.
//
// Every store promises this. How a store keeps the promise is its own business: the list store
// checks inside a lock, and PostgreSQL (Step 7) refuses the INSERT with an exclusion constraint.
// BookingService turns it into Unavailable, the same answer the pre-check gives, so a guest never
// sees a storage error.
public class OverlapRejectedException extends RuntimeException {
    public OverlapRejectedException(String message) {
        super(message);
    }
}
