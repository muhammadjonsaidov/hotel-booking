package hotelbooking.rooms;

import java.util.List;

// Rooms' public contract: which rooms the hotel sells. From P07 they come from a database.
public interface RoomCatalog {
    public List<Room> all();
}
