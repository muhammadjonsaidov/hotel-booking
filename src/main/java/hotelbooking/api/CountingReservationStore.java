package hotelbooking.api;

import hotelbooking.reservations.DateRange;
import hotelbooking.reservations.Reservation;
import hotelbooking.reservations.ReservationStore;
import hotelbooking.rooms.Room;

// Passes every call on to the real store and counts it: each call is one question to the database.
public class CountingReservationStore implements ReservationStore {
    private final ReservationStore store;
    private final Stats stats;

    public CountingReservationStore(ReservationStore store, Stats stats) {
        this.store = store;
        this.stats = stats;
    }

    @Override
    public int count() {
        stats.databaseQueries.incrementAndGet();
        return store.count();
    }

    @Override
    public boolean hasOverlap(Room room, DateRange stay) {
        stats.databaseQueries.incrementAndGet();
        return store.hasOverlap(room, stay);
    }

    @Override
    public void save(Reservation reservation) {
        stats.databaseQueries.incrementAndGet();
        store.save(reservation);
    }

    @Override
    public Reservation find(String id) {
        stats.databaseQueries.incrementAndGet();
        return store.find(id);
    }

    @Override
    public int countConfirmed(Room room, DateRange stay) {
        stats.databaseQueries.incrementAndGet();
        return store.countConfirmed(room, stay);
    }
}
