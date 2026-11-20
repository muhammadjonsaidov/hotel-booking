package hotelbooking.api;

import hotelbooking.notifications.NotificationCounts;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

// The four counters of GET /stats. Spring runs requests on several threads at once,
// so each counter is an AtomicLong: every incrementAndGet is exact.
public class Stats {
    final AtomicLong searches = new AtomicLong();
    final AtomicLong databaseQueries = new AtomicLong();
    final AtomicLong cacheHits = new AtomicLong();
    final AtomicLong cacheMisses = new AtomicLong();

    // The body of GET /stats; JSON keeps the order of the record's fields.
    record Counts(long searches, long databaseQueries, long cacheHits, long cacheMisses) {}

    Counts counts() {
        return new Counts(searches.get(), databaseQueries.get(), cacheHits.get(), cacheMisses.get());
    }

    // The body of GET /stats when the app is wired with a notification queue (P10): the four search
    // counters, then what the queue has accepted, refused, finished and still holds.
    Map<String, Object> counts(NotificationCounts queue) {
        Map<String, Object> answer = new LinkedHashMap<>();
        answer.put("searches", searches.get());
        answer.put("databaseQueries", databaseQueries.get());
        answer.put("cacheHits", cacheHits.get());
        answer.put("cacheMisses", cacheMisses.get());
        answer.put("notificationsAccepted", queue.accepted());
        answer.put("notificationsRejected", queue.rejected());
        answer.put("notificationsCompleted", queue.completed());
        answer.put("notificationsQueued", queue.queued());
        return answer;
    }
}
