package hotelbooking.rooms.internal;

import hotelbooking.rooms.Room;
import hotelbooking.rooms.RoomCatalog;
import jakarta.persistence.EntityManager;

import java.util.List;

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
