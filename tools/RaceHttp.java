// Sends two POST /reservations for the same room and the same nights at the same moment, and prints
// what each one got back and how many confirmed reservations the room ended up with.
//
//   java tools/RaceHttp.java                          both requests to http://localhost:5080
//   java tools/RaceHttp.java http://localhost:5081    Omar's request to the second instance
//
// Start the app with HOTEL_SLOW_STEP=1 first. Without it the gap between the check and the save is
// too small to land in reliably, and the race shows up only now and then.
//
// Maya's request goes first and Omar's 500 ms later, so the two reach the gap together and leave it
// in a known order. That is what makes the output the same on every run; it is not what makes the
// bug possible. The bug is possible because checking and saving are two separate statements.
//
// A warm-up request is sent before the two bookings. The very first request to a freshly started
// app pays for connection pools, servlet initialisation and JIT, which can cost more than the
// head start Maya is given - and then Omar wins instead and the output comes out the other way
// round. The warm-up cannot reach the booking path itself, though - GET /stats never inserts - so
// Maya's request is still the first to compile the insert, which measured as much as 140 ms more
// than Omar's. Half a second of head start covers that and leaves both requests inside the gap.
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class RaceHttp {
    static final String MAYA = """
            {"guestName":"Maya","guestEmail":"maya@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}""";
    static final String OMAR = """
            {"guestName":"Omar","guestEmail":"omar@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}""";

    public static void main(String[] args) throws Exception {
        String mayaBase = "http://localhost:5080";
        String omarBase = args.length > 0 ? args[0] : mayaBase;
        HttpClient client = HttpClient.newHttpClient();

        // The warm-up: see the note at the top. Spring Boot's first request is the slowest of all.
        client.send(HttpRequest.newBuilder(URI.create(mayaBase + "/stats")).build(),
                HttpResponse.BodyHandlers.ofString());

        System.out.println("Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.");
        if (!omarBase.equals(mayaBase)) System.out.println("Maya -> " + mayaBase + "   Omar -> " + omarBase);
        System.out.println();

        // Both requests are started before either is awaited: that is what "at the same moment" means.
        CompletableFuture<String> maya = book(client, mayaBase, MAYA);
        Thread.sleep(500); // Omar clicks a moment after Maya
        CompletableFuture<String> omar = book(client, omarBase, OMAR);

        String mayaAnswer = maya.join();
        String omarAnswer = omar.join();
        System.out.println("Maya -> " + mayaAnswer);
        System.out.println("Omar -> " + omarAnswer);
        System.out.println();

        long accepted = java.util.stream.Stream.of(mayaAnswer, omarAnswer).filter(a -> a.startsWith("201")).count();
        System.out.println("Confirmed reservations for R7 on 12-14 Nov: " + accepted);
        System.out.println(accepted > 1
                ? "DOUBLE BOOKED: the room has two confirmed reservations for the same nights."
                : "CORRECT: one booking was accepted and one was refused.");
    }

    static CompletableFuture<String> book(HttpClient client, String base, String body) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/reservations"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> reason(response.statusCode()) + response.body());
    }

    static String reason(int status) {
        return switch (status) {
            case 201 -> "201 Created   ";
            case 409 -> "409 Conflict  ";
            case 400 -> "400 Bad Request  ";
            default -> status + "  ";
        };
    }
}
