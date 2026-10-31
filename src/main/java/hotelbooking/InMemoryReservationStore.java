package hotelbooking;

import java.util.ArrayList;
import java.util.List;

// Keeps reservations in a list.
public class InMemoryReservationStore implements ReservationStore {
    private final List<Reservation> rows = new ArrayList<>();

    @Override
    public int count() {
        return rows.size();
    }

    @Override
    public boolean hasOverlap(Room room, DateRange stay) {
        return rows.stream().anyMatch(row -> row.blocks(room, stay));
    }

    @Override
    public void save(Reservation reservation) {
        rows.add(reservation);
    }

    // Returns null when no reservation has that id.
    @Override
    public Reservation find(String id) {
        return rows.stream().filter(row -> row.id().equals(id)).findFirst().orElse(null);
    }

    @Override
    public int countConfirmed(Room room, DateRange stay) {
        return (int) rows.stream().filter(row -> row.blocks(room, stay)).count();
    }
}
