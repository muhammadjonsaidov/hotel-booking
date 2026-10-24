package uz.pdp.hotelbooking;

import java.util.List;

public class BookingService {
    private final List<Room> rooms;
    private final ReservationStore store;
    private final EmailSender email;
    private final int slowStepMs;

    public BookingService(List<Room> rooms, ReservationStore store, EmailSender email) {
        this(rooms, store, email, 0);
    }

    public BookingService(List<Room> rooms, ReservationStore store, EmailSender email, int slowStepMs) {
        this.rooms = rooms;
        this.store = store;
        this.email = email;
        this.slowStepMs = slowStepMs;
    }

    public List<Room> findFreeRooms(DateRange stay) {
        return rooms.stream().filter(room -> !store.hasOverlap(room, stay)).toList();
    }

    public BookingResult reserve(Guest guest, Room room, DateRange stay) {
        if (!stay.isValid()) return new BookingResult("InvalidDates");
        if (store.hasOverlap(room, stay)) return new BookingResult("Unavailable");

        try {
            Thread.sleep(slowStepMs); // 0 normally; the race demo makes this step slow
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Booking interrupted", e);
        }

        var reservation = new Reservation("R" + (101 + store.count()), guest, room, stay);
        store.save(reservation);
        email.sendConfirmation(reservation);
        return new BookingResult("Accepted", reservation.id);
    }

    public boolean cancel(String reservationId) {
        var reservation = store.find(reservationId);
        return reservation != null && reservation.cancel();
    }
}
