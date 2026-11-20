package hotelbooking.reservations;

import hotelbooking.rooms.Room;

public interface ReservationStore {
    public int count();

    public boolean hasOverlap(Room room, DateRange stay);

    // The id for the next reservation.
    //
    // The store owns this, because only the store can hand out an id that no other request can
    // also be given. Counting the rows and adding one cannot: two requests that count before either
    // has saved both get R101, and the second INSERT then fails on the primary key instead of on
    // the booking rule. That is exactly what happens here, because Spring wraps each request in one
    // transaction, so an uncommitted row is invisible to the other request's count.
    // PostgreSQL answers from a sequence; the list store counts, which is safe there because it
    // holds a lock.
    public String nextReservationId();

    // Writes the reservation.
    //
    // Throws OverlapRejectedException if writing it would give the room two Confirmed reservations
    // covering the same night. The pre-check in reserve usually catches that first; this promise is
    // what makes the rule true when two requests check at the same moment.
    public void save(Reservation reservation);

    // Returns null when no reservation has that id.
    public Reservation find(String id);

    public int countConfirmed(Room room, DateRange stay);
}
