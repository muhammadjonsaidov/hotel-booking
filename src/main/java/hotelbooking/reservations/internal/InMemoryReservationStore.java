package hotelbooking.reservations.internal;

import hotelbooking.reservations.Reservation;
import hotelbooking.reservations.DateRange;
import hotelbooking.reservations.OverlapRejectedException;
import hotelbooking.reservations.ReservationStore;

import hotelbooking.rooms.Room;

import java.util.ArrayList;
import java.util.List;

// Keeps reservations in a list. save checks and appends while holding one lock, so two threads of
// this process can never both save an overlapping reservation (P09 Step 3). The lock knows nothing
// about a second copy of the app.
public class InMemoryReservationStore implements ReservationStore {
    private final List<Reservation> rows = new ArrayList<>();
    private final Object gate = new Object();

    @Override
    public int count() {
        synchronized (gate) {
            return rows.size();
        }
    }

    @Override
    public boolean hasOverlap(Room room, DateRange stay) {
        synchronized (gate) {
            return rows.stream().anyMatch(row -> row.blocks(room, stay));
        }
    }

    // Safe here for the same reason save is: it holds the same lock.
    @Override
    public String nextReservationId() {
        synchronized (gate) {
            return "R" + (101 + rows.size());
        }
    }

    @Override
    public void save(Reservation reservation) {
        synchronized (gate) {
            if (rows.stream().anyMatch(row -> row.blocks(reservation.room(), reservation.stay()))) {
                throw new OverlapRejectedException(
                        reservation.room().number() + " already has a confirmed reservation for these nights.");
            }
            rows.add(reservation);
        }
    }

    // Returns null when no reservation has that id.
    @Override
    public Reservation find(String id) {
        synchronized (gate) {
            return rows.stream().filter(row -> row.id().equals(id)).findFirst().orElse(null);
        }
    }

    @Override
    public int countConfirmed(Room room, DateRange stay) {
        synchronized (gate) {
            return (int) rows.stream().filter(row -> row.blocks(room, stay)).count();
        }
    }
}
