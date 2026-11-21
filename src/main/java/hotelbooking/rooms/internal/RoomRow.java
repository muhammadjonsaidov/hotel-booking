package hotelbooking.rooms.internal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// One row of the rooms table in db/init.sql.
@Entity
@Table(name = "rooms")
class RoomRow {
    @Id
    String number;
    String type;
}
