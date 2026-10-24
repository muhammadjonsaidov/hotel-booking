package hotelbooking;

import java.util.ArrayList;
import java.util.List;

// Keeps reservations in a list.
public class ReservationStore {
    private final List<Reservation> rows = new ArrayList<>();

    public int count() {
        return rows.size();
    }

    public boolean hasOverlap(Room room, DateRange stay) {
        return rows.stream().anyMatch(row -> row.blocks(room, stay));
    }

    public void save(Reservation reservation) {
        rows.add(reservation);
    }

    public Reservation find(String id) {
        return rows.stream().filter(row -> row.id.equals(id)).findFirst().orElse(null);
    }

    public int countConfirmed(Room room, DateRange stay) {
        return (int) rows.stream().filter(row -> row.blocks(room, stay)).count();
    }
}
