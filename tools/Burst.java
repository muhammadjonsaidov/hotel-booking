// Sends a burst of bookings as fast as it can, then reads GET /stats.
//
// Every booking is for a different room and night - R7 and R8, one night each, 1 to 15 December - so
// all 30 are legal under P09's exclusion constraint and nothing is refused by the booking rule.
// Whatever is refused here was refused by the notification queue, which is the thing being measured.
//
// Start the app with HOTEL_EMAIL_SECONDS=1 first, so the confirmation sender is as slow as a real
// mail server.
//
//   java tools/Burst.java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Burst {
    static final String BASE = "http://localhost:5080";
    static final int BURST = 30;

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        System.out.println("Sending " + BURST + " bookings, one after another...");
        System.out.println();

        int accepted = 0;
        int rejected = 0;
        long slowestNanos = 0;
        long started = System.nanoTime();
        for (int index = 0; index < BURST; index++) {
            int night = 1 + index / 2;
            String room = index % 2 == 0 ? "R7" : "R8";
            String body = """
                    {"guestName":"Guest%1$d%2$s","guestEmail":"guest%1$d%2$s@example.com","room":"%2$s","checkIn":"2026-12-%1$02d","checkOut":"2026-12-%3$02d"}"""
                    .formatted(night, room, night + 1);
            HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + "/reservations"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            long one = System.nanoTime();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            slowestNanos = Math.max(slowestNanos, System.nanoTime() - one);
            if (response.statusCode() == 201) accepted++; else rejected++;
        }
        double totalSeconds = (System.nanoTime() - started) / 1_000_000_000.0;

        System.out.println("Accepted: " + accepted);
        System.out.println("Rejected: " + rejected);
        System.out.printf("Slowest booking: %d ms%n", slowestNanos / 1_000_000);
        System.out.printf("Total: %.1f s%n", totalSeconds);
        System.out.println();
        System.out.println("GET /stats");
        HttpRequest stats = HttpRequest.newBuilder(URI.create(BASE + "/stats")).build();
        System.out.println(client.send(stats, HttpResponse.BodyHandlers.ofString()).body());
    }
}
