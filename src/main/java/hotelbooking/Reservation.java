package hotelbooking;

public class Reservation {
    private final String id;
    private final Guest guest;
    private final Room room;
    private final DateRange stay;
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    public Reservation(String id, Guest guest, Room room, DateRange stay) {
        this.id = id;
        this.guest = guest;
        this.room = room;
        this.stay = stay;
    }

    public String id() {
        return id;
    }

    public ReservationStatus status() {
        return status;
    }

    // A reservation blocks a request when it is still Confirmed for the same room and shares a night.
    public boolean blocks(Room room, DateRange stay) {
        return status == ReservationStatus.CONFIRMED && this.room.equals(room) && this.stay.overlaps(stay);
    }

    public boolean cancel() {
        if (status == ReservationStatus.CANCELLED) return false;
        status = ReservationStatus.CANCELLED;
        return true;
    }
}
