package hotelbooking.reservations.internal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

// One row of the reservations table in db/init.sql (Spring Boot maps guestName to guest_name).
@Entity
@Table(name = "reservations")
class ReservationRow {
    @Id
    String id;
    String guestName;
    String guestEmail;
    String room;
    LocalDate checkIn;
    LocalDate checkOut;
    String status;
}
