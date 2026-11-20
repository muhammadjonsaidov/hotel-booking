// Sends 1,000 searches to the running app, one after another, and prints how long they took
// and what they cost: the difference of GET /stats before and after.
// java tools/Measure.java                         -> 12-14 Nov
// java tools/Measure.java 2026-12-31 2027-01-02   -> other dates
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Measure {
    static final int SEARCHES = 1000;
    static final String[] NAMES = {"searches", "databaseQueries", "cacheHits", "cacheMisses"};
    // One client for every request: it keeps its connection to the app open.
    static final HttpClient CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    static String get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:5080" + path)).build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            System.err.println("GET " + path + " answered " + response.statusCode() + ": " + response.body());
            System.exit(1);
        }
        return response.body();
    }

    // The four numbers of GET /stats, such as "searches":12, found with a small pattern instead of a JSON library.
    static long[] counts(String json) {
        long[] values = new long[NAMES.length];
        for (int i = 0; i < NAMES.length; i++) {
            Matcher number = Pattern.compile("\"" + NAMES[i] + "\":(\\d+)").matcher(json);
            if (!number.find()) {
                System.err.println("GET /stats has no " + NAMES[i] + ": " + json);
                System.exit(1);
            }
            values[i] = Long.parseLong(number.group(1));
        }
        return values;
    }

    public static void main(String[] args) throws Exception {
        String checkIn = args.length >= 2 ? args[0] : "2026-11-12";
        String checkOut = args.length >= 2 ? args[1] : "2026-11-14";

        long[] before = counts(get("/stats"));
        double[] times = new double[SEARCHES];
        long started = System.nanoTime();
        for (int i = 0; i < SEARCHES; i++) {
            long start = System.nanoTime();
            get("/rooms/availability?checkIn=" + checkIn + "&checkOut=" + checkOut);
            times[i] = (System.nanoTime() - start) / 1_000_000.0;
        }
        double total = (System.nanoTime() - started) / 1_000_000_000.0;
        long[] after = counts(get("/stats"));

        Arrays.sort(times);
        double p95 = times[(int) Math.ceil(0.95 * SEARCHES) - 1]; // 950 of the 1,000 searches were this fast or faster
        StringBuilder difference = new StringBuilder();
        for (int i = 0; i < NAMES.length; i++) {
            difference.append(i == 0 ? "" : ",").append('"').append(NAMES[i]).append("\":").append(after[i] - before[i]);
        }
        // Locale.ROOT: always 2.0, never 2,0, whatever language the laptop is set to.
        System.out.println(SEARCHES + " searches for " + checkIn + " to " + checkOut);
        System.out.println(String.format(Locale.ROOT, "Average: %.1f ms", Arrays.stream(times).sum() / SEARCHES));
        System.out.println(String.format(Locale.ROOT, "p95: %.1f ms", p95));
        System.out.println(String.format(Locale.ROOT, "Total: %.1f s", total));
        System.out.println("GET /stats difference: {" + difference + "}");
    }
}
