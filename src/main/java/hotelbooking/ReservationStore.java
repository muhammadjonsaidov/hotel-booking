package hotelbooking;

public interface ReservationStore {
    int count();

    boolean hasOverlap(Room room, DateRange stay);

    void save(Reservation reservation);

    // Returns null when no reservation has that id.
    Reservation find(String id);

    int countConfirmed(Room room, DateRange stay);
}
