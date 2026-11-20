package hotelbooking.api;

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
}
