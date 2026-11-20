package hotelbooking;

import hotelbooking.api.AvailabilityCache;
import hotelbooking.api.CountingReservationStore;
import hotelbooking.api.HotelApi;
import hotelbooking.api.Stats;

import hotelbooking.reservations.BookingService;
import hotelbooking.reservations.BookingResult;
import hotelbooking.reservations.DateRange;
import hotelbooking.reservations.Guest;
import hotelbooking.reservations.internal.InMemoryReservationStore;
import hotelbooking.reservations.internal.PostgresReservationStore;

import hotelbooking.notifications.internal.EmailNotificationSender;
import hotelbooking.notifications.internal.QueuedNotificationSender;

import hotelbooking.rooms.Room;
import hotelbooking.rooms.RoomCatalog;
import hotelbooking.rooms.internal.InMemoryRoomCatalog;
import hotelbooking.rooms.internal.PostgresRoomCatalog;

import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.support.TransactionTemplate;

// ./gradlew bootRun             -> the API on http://localhost:5080, with PostgreSQL (first: docker compose up -d for PostgreSQL and Redis)
//                                  HOTEL_PORT=5081 starts a second instance; HOTEL_SLOW_STEP=1 pauses each booking 1 s between check and save
//                                  HOTEL_EMAIL_SECONDS=1 makes each confirmation email take 1 s, like a real mail server
// ./gradlew run --args="--demo" -> the P06 demo, with the data in lists
// ./gradlew run --args="--race" -> two guests book the same room at the same moment

// Spring Boot starts the web server and connects to PostgreSQL (application.properties);
// hotelApi below is the wiring that hands it the endpoints.
@SpringBootConfiguration
@EnableAutoConfiguration
public class Main {
    private static final Room R7 = new Room("R7", "Standard");
    private static final Room R8 = new Room("R8", "Deluxe");
    private static final Guest MAYA = new Guest("Maya", "maya@example.com");
    private static final Guest OMAR = new Guest("Omar", "omar@example.com");
    private static final Guest NORA = new Guest("Nora", "nora@example.com");

    // The wiring chooses the internal implementations and connects their public contracts.
    private static final RoomCatalog CATALOG = new InMemoryRoomCatalog(List.of(R7, R8));

    public static void main(String[] args) throws InterruptedException {
        if (List.of(args).contains("--race")) runRace();
        else if (List.of(args).contains("--demo")) runDemo();
        else SpringApplication.run(Main.class, args);
    }

    // The wiring for the web app: rooms and reservations live in PostgreSQL.
    @Bean
    HotelApi hotelApi(EntityManager db, TransactionTemplate transactions, StringRedisTemplate redis) {
        List<Room> rooms = new PostgresRoomCatalog(db).all();
        // P10: the mail server is slow, and the booking no longer waits for it.
        // HOTEL_EMAIL_SECONDS makes the sender as slow as a real one; the queue is what keeps that
        // out of the request. HOTEL_QUEUE_CAPACITY and HOTEL_WORKERS size the queue and its workers.
        EmailNotificationSender email = new EmailNotificationSender(
                Double.parseDouble(System.getenv().getOrDefault("HOTEL_EMAIL_SECONDS", "0")));
        QueuedNotificationSender notifications = new QueuedNotificationSender(email,
                Integer.parseInt(System.getenv().getOrDefault("HOTEL_QUEUE_CAPACITY", "10")),
                Integer.parseInt(System.getenv().getOrDefault("HOTEL_WORKERS", "1")));
        // P10 Step 6: drain the queue before the worker stops, so a polite shutdown (Ctrl+C, a normal
        // deploy) owes nobody an email. Nothing here helps with kill -9: see docs/queue-trace.md.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            var before = notifications.counts();
            System.out.println("Stopping the app with " + before.accepted() + " confirmations accepted and "
                    + before.completed() + " sent so far...");
            System.out.println("Draining the notification queue: " + before.queued() + " to go");
            notifications.close(true);
            var after = notifications.counts();
            System.out.println("Notification worker stopped.");
            System.out.println("accepted=" + after.accepted() + "  completed=" + after.completed() + "  queued=" + after.queued());
            System.out.println(after.accepted() == after.completed() ? "Nobody was owed an email." : "Some guests are still owed an email.");
        }, "notifications-drain"));
        Stats stats = new Stats();
        try (RedisConnection connection = redis.getRequiredConnectionFactory().getConnection()) {
            connection.ping(); // stop here if Redis is not running
        }
        AvailabilityCache cache = new AvailabilityCache(new AvailabilityCache.RedisKeyValueStore(redis));
        // P09 Step 1 widens the gap between the check and the save, so the race happens on every run.
        int slowStepMs = (int) (Double.parseDouble(System.getenv().getOrDefault("HOTEL_SLOW_STEP", "0")) * 1000);
        // One request: a fresh store in its own transaction, counted for GET /stats; save what changed at the end.
        HotelApi.UnitOfWork unitOfWork = work -> transactions.execute(transaction -> {
            PostgresReservationStore store = new PostgresReservationStore(db, rooms);
            var result = work.apply(new BookingService(rooms, new CountingReservationStore(store, stats), notifications, slowStepMs));
            if (store.wasRefused()) {
                // The booking rule refused a save, so PostgreSQL has aborted this transaction and
                // no further statement in it can run. Nothing should be written anyway: roll back
                // and let the endpoint answer 409.
                transaction.setRollbackOnly();
                return result;
            }
            store.saveChanges();
            return result;
        });
        return new HotelApi(rooms, unitOfWork, stats, cache, notifications);
    }

    private static void runDemo() {
        InMemoryReservationStore store = new InMemoryReservationStore();
        EmailNotificationSender email = new EmailNotificationSender();
        BookingService service = new BookingService(CATALOG.all(), store, email);

        System.out.println("Free rooms 12-14 Nov: " + names(service.findFreeRooms(nov(12, 14))));
        System.out.println("Maya books R7 12-14 Nov: " + service.reserve(MAYA, R7, nov(12, 14)));
        System.out.println("Omar books R7 13-15 Nov: " + service.reserve(OMAR, R7, nov(13, 15)));
        System.out.println("Nora books R7 14-16 Nov: " + service.reserve(NORA, R7, nov(14, 16)));
        System.out.println("Omar books R8 15-15 Nov: " + service.reserve(OMAR, R8, nov(15, 15)));
        System.out.println("Free rooms 12-14 Nov: " + names(service.findFreeRooms(nov(12, 14))));
        System.out.println("Maya cancels R101: " + boolText(service.cancel("R101")));
        System.out.println("Free rooms 12-14 Nov: " + names(service.findFreeRooms(nov(12, 14))));
        System.out.println("Confirmation emails sent: " + email.sent());
    }

    private static void runRace() throws InterruptedException {
        InMemoryReservationStore store = new InMemoryReservationStore();
        // Each request now spends half a second between checking and saving.
        BookingService service = new BookingService(CATALOG.all(), store, new EmailNotificationSender(), 500);
        DateRange stay = nov(12, 14);
        BookingResult[] results = new BookingResult[2];

        System.out.println("Maya and Omar both book R7 for 12-14 Nov at the same moment...");
        Thread mayaRequest = new Thread(() -> results[0] = service.reserve(MAYA, R7, stay));
        Thread omarRequest = new Thread(() -> results[1] = service.reserve(OMAR, R7, stay));
        mayaRequest.start();
        Thread.sleep(100); // Omar clicks a moment after Maya
        omarRequest.start();
        mayaRequest.join();
        omarRequest.join();

        System.out.println("Maya: " + results[0]);
        System.out.println("Omar: " + results[1]);
        int confirmed = store.countConfirmed(R7, stay);
        System.out.println("Confirmed reservations for R7 on 12-14 Nov: " + confirmed);
        System.out.println(confirmed > 1
                ? "Double booking! Both requests checked before either one saved."
                : "Only one guest has R7.");
    }

    private static DateRange nov(int checkIn, int checkOut) {
        return new DateRange(LocalDate.of(2026, 11, checkIn), LocalDate.of(2026, 11, checkOut));
    }

    private static String names(List<Room> rooms) {
        return rooms.stream().map(Room::number).collect(Collectors.joining(", "));
    }

    private static String boolText(boolean value) {
        return value ? "True" : "False";
    }
}
