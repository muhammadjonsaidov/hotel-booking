package hotelbooking.reservations.internal;

import hotelbooking.reservations.DateRange;
import hotelbooking.reservations.Guest;
import hotelbooking.reservations.Reservation;
import hotelbooking.reservations.ReservationStatus;
import hotelbooking.reservations.OverlapRejectedException;
import hotelbooking.reservations.ReservationStore;
import hotelbooking.rooms.Room;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.EntityManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    // The name of the exclusion constraint in db/init.sql, and PostgreSQL's SQLSTATE for "a row
    // conflicted with an exclusion constraint". The store recognises exactly this one violation.
    private static final String NO_OVERLAP_CONSTRAINT = "reservations_no_overlap";
    private static final String EXCLUSION_VIOLATION = "23P01";

    // Set when the constraint refused a save. Main.java reads it and rolls the request back.
    private boolean refused;

    // True when this request's transaction has been aborted by the booking rule, and so must be
    // rolled back instead of committed.
    public boolean wasRefused() {
        return refused;
    }

    // nextval never hands the same number to two callers, and it does not wait for, or roll back
    // with, the transaction that asked for it. That is exactly what an id needs, and it is what
    // stops two requests in this language both being given R101.
    @Override
    public String nextReservationId() {
        Number next = (Number) db.createNativeQuery("SELECT nextval('reservation_ids')").getSingleResult();
        return "R" + next.longValue();
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
        try {
            db.persist(row);
            db.flush(); // INSERT now: the constraint answers here, not at the end of the request
        } catch (PersistenceException failure) {
            if (!isOverlapRefusal(failure)) {
                throw failure; // a duplicate id, or anything else: a real fault, not a double booking
            }
            // Two things have to happen here, and both are specific to running inside one
            // transaction. First, take the refused row back out of the persistence context, so
            // Hibernate does not try to write it again when it next flushes. Second, remember that
            // the booking was refused: PostgreSQL has marked this transaction as aborted and will
            // refuse every further statement in it with SQLSTATE 25P02, so the unit of work must
            // roll back rather than commit. A refused booking saves nothing, so that is exactly
            // the right ending.
            db.detach(row);
            refused = true;
            throw new OverlapRejectedException(
                    reservation.room().number() + " already has a confirmed reservation for these nights.");
        }
    }

    // Hibernate wraps the driver's error, so the cause chain is what has to be read.
    private static boolean isOverlapRefusal(PersistenceException failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof java.sql.SQLException sql
                    && EXCLUSION_VIOLATION.equals(sql.getSQLState())
                    && String.valueOf(sql.getMessage()).contains(NO_OVERLAP_CONSTRAINT)) {
                return true;
            }
        }
        return false;
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
