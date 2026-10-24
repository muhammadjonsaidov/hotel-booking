package uz.pdp.hotelbooking;

public class Reservation {
    final String id;
    final Guest guest;
    final Room room;
    final DateRange stay;
    private ReservationStatus status = ReservationStatus.Confirmed;

    public Reservation(String id, Guest guest, Room room, DateRange stay) {
        this.id = id;
        this.guest = guest;
        this.room = room;
        this.stay = stay;
    }

    public ReservationStatus status() {
        return status;
    }

    // A reservation blocks a request when it is still Confirmed for the same room and shares a night.
    public boolean blocks(Room room, DateRange stay) {
        return status == ReservationStatus.Confirmed && this.room.equals(room) && this.stay.overlaps(stay);
    }

    public boolean cancel() {
        if (status == ReservationStatus.Cancelled) return false;
        status = ReservationStatus.Cancelled;
        return true;
    }
}
