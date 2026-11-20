package hotelbooking.api;

import hotelbooking.reservations.BookingService;
import hotelbooking.rooms.Room;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back. It lives
// outside the four modules and uses only their public contracts; Main decides which store the
// endpoints use, through the unit of work.
@RestController
public class HotelApi {
    // Runs one request's work with a BookingService, then saves whatever changed.
    @FunctionalInterface
    public interface UnitOfWork {
        ResponseEntity<?> run(Function<BookingService, ResponseEntity<?>> work);
    }

    private final List<Room> rooms;
    private final UnitOfWork unitOfWork;

    public HotelApi(List<Room> rooms, UnitOfWork unitOfWork) {
        this.rooms = rooms;
        this.unitOfWork = unitOfWork;
    }

    @GetMapping(value = "/openapi.yaml", produces = "application/yaml")
    public String contract() throws IOException {
        return Files.readString(Path.of("docs/openapi.yaml"));
    }
}
