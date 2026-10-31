package hotelbooking;

public interface ReservationStore {
    public int count();

    public boolean hasOverlap(Room room, DateRange stay);

    public void save(Reservation reservation);

    // Returns null when no reservation has that id.
    public Reservation find(String id);

    public int countConfirmed(Room room, DateRange stay);
}
