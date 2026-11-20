package hotelbooking.rooms.internal;

import hotelbooking.rooms.Room;
import hotelbooking.rooms.RoomCatalog;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;

// One row of the rooms table in db/init.sql.
@Entity
@Table(name = "rooms")
class RoomRow {
    @Id String number;
    String type;
}

// Reads the hotel's rooms from the rooms table. Only Main creates it.
public class PostgresRoomCatalog implements RoomCatalog {
    private final EntityManager db;

    public PostgresRoomCatalog(EntityManager db) {
        this.db = db;
    }

    @Override
    public List<Room> all() {
        return db.createQuery("SELECT r FROM RoomRow r", RoomRow.class).getResultList().stream()
            .map(row -> new Room(row.number, row.type))
            .toList();
    }
}
