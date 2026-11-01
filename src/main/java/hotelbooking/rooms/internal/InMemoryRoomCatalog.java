package hotelbooking.rooms.internal;

import java.util.List;
import hotelbooking.rooms.Room;
import hotelbooking.rooms.RoomCatalog;

// Keeps the hotel's rooms in a list. Only Main creates it.
public class InMemoryRoomCatalog implements RoomCatalog {
    private final List<Room> rooms;

    public InMemoryRoomCatalog(List<Room> rooms) {
        this.rooms = List.copyOf(rooms);
    }

    @Override
    public List<Room> all() {
        return rooms;
    }
}
