package hotelbooking.reservations.internal;

import hotelbooking.reservations.DateRange;
import hotelbooking.reservations.Guest;
import hotelbooking.reservations.Reservation;
import hotelbooking.reservations.ReservationStatus;
import hotelbooking.reservations.ReservationStore;
import hotelbooking.rooms.Room;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// One row of the reservations table in db/init.sql (Spring Boot maps guestName to guest_name).
@Entity
@Table(name = "reservations")
class ReservationRow {
    @Id String id;
    String guestName;
    String guestEmail;
    String room;
    LocalDate checkIn;
    LocalDate checkOut;
    String status;
}

// Keeps reservations in PostgreSQL. Main creates one per request (a unit of work): it remembers
// the reservations it hands out, and saveChanges writes their changes.
public class PostgresReservationStore implements ReservationStore {
    private final EntityManager db;
    private final List<Room> rooms;
    private final Map<ReservationRow, Reservation> loaded = new LinkedHashMap<>();

    public PostgresReservationStore(EntityManager db, List<Room> rooms) {
        this.db = db;
        this.rooms = rooms;
    }

    @Override
    public int count() {
        return db.createQuery("SELECT count(r) FROM ReservationRow r", Long.class).getSingleResult().intValue();
    }

    @Override
    public boolean hasOverlap(Room room, DateRange stay) {
        return countConfirmed(room, stay) > 0;
    }

    @Override
    public void save(Reservation reservation) {
        ReservationRow row = new ReservationRow();
        row.id = reservation.id();
        row.guestName = reservation.guest().name();
        row.guestEmail = reservation.guest().email();
        row.room = reservation.room().number();
        row.checkIn = reservation.stay().checkIn();
        row.checkOut = reservation.stay().checkOut();
        row.status = statusName(reservation.status());
        db.persist(row);
        db.flush(); // INSERT now: a second R101 is refused here
    }

    // Returns null when no reservation has that id.
    @Override
    public Reservation find(String id) {
        ReservationRow row = db.find(ReservationRow.class, id);
        if (row == null) return null;
        Room room = rooms.stream().filter(candidate -> candidate.number().equals(row.room)).findFirst().orElseThrow();
        Reservation reservation = new Reservation(row.id, new Guest(row.guestName, row.guestEmail), room, new DateRange(row.checkIn, row.checkOut));
        if (row.status.equals("Cancelled")) reservation.cancel();
        loaded.put(row, reservation);
        return reservation;
    }

    // Confirmed reservations of this room that share at least one night with the stay.
    @Override
    public int countConfirmed(Room room, DateRange stay) {
        return db.createQuery("""
                SELECT count(r) FROM ReservationRow r WHERE r.room = :room AND r.status = 'Confirmed'
                AND r.checkIn < :checkOut AND :checkIn < r.checkOut""", Long.class)
            .setParameter("room", room.number())
            .setParameter("checkIn", stay.checkIn())
            .setParameter("checkOut", stay.checkOut())
            .getSingleResult().intValue();
    }

    // The end of the request: copy each loaded reservation's status to its row; JPA writes what changed.
    public void saveChanges() {
        loaded.forEach((row, reservation) -> row.status = statusName(reservation.status()));
        db.flush();
    }

    // The status as the reservations table writes it: Confirmed or Cancelled.
    private static String statusName(ReservationStatus status) {
        return status == ReservationStatus.CONFIRMED ? "Confirmed" : "Cancelled";
    }
}
