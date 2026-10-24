package uz.pdp.hotelbooking;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// ./gradlew run                 -> the normal demo
// ./gradlew run --args="--race" -> two guests book the same room at the same moment
public class Main {
    private static final Room R7 = new Room("R7", "Standard");
    private static final Room R8 = new Room("R8", "Deluxe");
    private static final Guest MAYA = new Guest("Maya", "maya@example.com");
    private static final Guest OMAR = new Guest("Omar", "omar@example.com");
    private static final Guest NORA = new Guest("Nora", "nora@example.com");

    public static void main(String[] args) throws InterruptedException {
        if (Arrays.asList(args).contains("--race")) runRace(); else runDemo();
    }

    private static void runDemo() {
        var store = new ReservationStore();
        var email = new EmailSender();
        var service = new BookingService(List.of(R7, R8), store, email);

        System.out.println("Free rooms 12-14 Nov: " + names(service.findFreeRooms(nov(12, 14))));
        System.out.println("Maya books R7 12-14 Nov: " + service.reserve(MAYA, R7, nov(12, 14)));
        System.out.println("Omar books R7 13-15 Nov: " + service.reserve(OMAR, R7, nov(13, 15)));
        System.out.println("Nora books R7 14-16 Nov: " + service.reserve(NORA, R7, nov(14, 16)));
        System.out.println("Omar books R8 15-15 Nov: " + service.reserve(OMAR, R8, nov(15, 15)));
        System.out.println("Free rooms 12-14 Nov: " + names(service.findFreeRooms(nov(12, 14))));
        System.out.println("Maya cancels R101: " + (service.cancel("R101") ? "True" : "False"));
        System.out.println("Free rooms 12-14 Nov: " + names(service.findFreeRooms(nov(12, 14))));
        System.out.println("Confirmation emails sent: " + email.sent());
    }

    private static void runRace() throws InterruptedException {
        var store = new ReservationStore();
        // Each request now spends half a second between checking and saving.
        var service = new BookingService(List.of(R7, R8), store, new EmailSender(), 500);
        var stay = nov(12, 14);
        var results = new BookingResult[2];

        System.out.println("Maya and Omar both book R7 for 12-14 Nov at the same moment...");
        var mayaRequest = new Thread(() -> results[0] = service.reserve(MAYA, R7, stay));
        var omarRequest = new Thread(() -> results[1] = service.reserve(OMAR, R7, stay));
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
}
