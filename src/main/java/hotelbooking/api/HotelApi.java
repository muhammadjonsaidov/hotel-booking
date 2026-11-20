package hotelbooking.api;

import hotelbooking.reservations.BookingResult;
import hotelbooking.reservations.BookingService;
import hotelbooking.reservations.DateRange;
import hotelbooking.reservations.Guest;
import hotelbooking.rooms.Room;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back,
// and GET /stats with what the searches cost. It lives outside the four modules and uses only
// their public contracts; Main decides which store the endpoints use, through the unit of work.
@RestController
public class HotelApi {
    // Runs one request's work with a BookingService, then saves whatever changed.
    @FunctionalInterface
    public interface UnitOfWork {
        ResponseEntity<?> run(Function<BookingService, ResponseEntity<?>> work);
    }

    static final List<String> FIELDS = List.of("guestName", "guestEmail", "room", "checkIn", "checkOut");
    static final String BODY_DETAIL = "The body must be JSON with guestName, guestEmail, room, checkIn and checkOut.";
    static final String DATE_DETAIL = "checkIn and checkOut must be dates such as 2026-11-12.";

    record RoomJson(String number, String type) {}
    record Availability(String checkIn, String checkOut, List<RoomJson> rooms, String next) {}
    record Booked(String id, String status, boolean notificationFailed) {}
    // Problem details (RFC 9457): every error has the same three keys.
    record Problem(String title, int status, String detail) {}

    private final List<Room> rooms;
    private final UnitOfWork unitOfWork;
    private final Stats stats;

    public HotelApi(List<Room> rooms, UnitOfWork unitOfWork, Stats stats) {
        this.rooms = rooms;
        this.unitOfWork = unitOfWork;
        this.stats = stats;
    }

    static ResponseEntity<?> problem(int status, String title, String detail) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(new Problem(title, status, detail));
    }

    // A date written exactly like 2026-11-12, otherwise null.
    static LocalDate parseDate(Object text) {
        if (!(text instanceof String date) || !date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) return null;
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException notADate) {
            return null;
        }
    }

    // Turns a booking answer into an HTTP status code and a JSON body (L04).
    static ResponseEntity<?> toHttp(BookingResult result) {
        return switch (result.outcome()) {
            case "Accepted" -> ResponseEntity.status(201).body(new Booked(result.reservationId(), "Confirmed", result.notificationFailed()));
            case "Unavailable" -> problem(409, "Unavailable", "The room is booked for at least one night of this stay.");
            default -> problem(400, "InvalidDates", "checkOut must be after checkIn.");
        };
    }

    @GetMapping(value = "/openapi.yaml", produces = "application/yaml")
    public String contract() throws IOException {
        return Files.readString(Path.of("docs/openapi.yaml"));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> readStats() {
        return ResponseEntity.ok(stats.counts());
    }

    @GetMapping("/rooms/availability")
    public ResponseEntity<?> findFreeRooms(@RequestParam(required = false) String checkIn, @RequestParam(required = false) String checkOut,
                                           @RequestParam(required = false) String limit, @RequestParam(defaultValue = "") String after) {
        LocalDate start = parseDate(checkIn);
        LocalDate end = parseDate(checkOut);
        if (start == null || end == null) return problem(400, "InvalidDates", DATE_DETAIL);
        DateRange stay = new DateRange(start, end);
        if (!stay.isValid()) return problem(400, "InvalidDates", "checkOut must be after checkIn.");
        String size = limit == null ? "20" : limit;
        if (!size.matches("[0-9]{1,3}") || Integer.parseInt(size) < 1 || Integer.parseInt(size) > 100) {
            return problem(400, "InvalidRequest", "limit must be a whole number from 1 to 100.");
        }

        stats.searches.incrementAndGet();
        return unitOfWork.run(service -> {
            // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
            List<Room> rest = service.findFreeRooms(stay).stream().filter(room -> room.number().compareTo(after) > 0)
                .sorted(Comparator.comparing(Room::number)).toList();
            List<Room> page = rest.stream().limit(Integer.parseInt(size)).toList();
            String next = rest.size() > page.size() ? page.get(page.size() - 1).number() : null;
            return ResponseEntity.ok(new Availability(checkIn, checkOut, page.stream().map(room -> new RoomJson(room.number(), room.type())).toList(), next));
        });
    }

    @PostMapping("/reservations")
    public ResponseEntity<?> reserve(@RequestBody(required = false) Map<String, Object> body) {
        if (body == null || !FIELDS.stream().allMatch(field -> body.get(field) instanceof String text && !text.isEmpty())) {
            return problem(400, "InvalidRequest", BODY_DETAIL);
        }
        LocalDate start = parseDate(body.get("checkIn"));
        LocalDate end = parseDate(body.get("checkOut"));
        if (start == null || end == null) return problem(400, "InvalidDates", DATE_DETAIL);
        Room room = rooms.stream().filter(candidate -> candidate.number().equals(body.get("room"))).findFirst().orElse(null);
        if (room == null) return problem(400, "UnknownRoom", "There is no room " + body.get("room") + ".");

        Guest guest = new Guest((String) body.get("guestName"), (String) body.get("guestEmail"));
        return unitOfWork.run(service -> toHttp(service.reserve(guest, room, new DateRange(start, end))));
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<?> cancel(@PathVariable String id) {
        return unitOfWork.run(service -> service.cancel(id)
            ? ResponseEntity.noContent().build()
            : problem(404, "NotFound", "There is no confirmed reservation " + id + "."));
    }

    // A body that is not JSON at all.
    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<?> notJson() {
        return problem(400, "InvalidRequest", BODY_DETAIL);
    }
}
