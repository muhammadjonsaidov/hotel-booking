package hotelbooking.api;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;

// The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.
// PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.
// Booking and cancelling never read the cache.
public class AvailabilityCache {
    public static final int TTL_SECONDS = 30;
    public static final String VERSION_KEY = "availability:version";

    // The three Redis commands the cache uses: GET, SET with a time to live, and INCR.
    public interface KeyValueStore {
        // Returns null when Redis has no value for the key.
        String get(String key);

        void set(String key, String value, int ttlSeconds);

        void increment(String key);
    }

    // The three commands, sent to Redis.
    public static class RedisKeyValueStore implements KeyValueStore {
        private final StringRedisTemplate redis;

        public RedisKeyValueStore(StringRedisTemplate redis) {
            this.redis = redis;
        }

        @Override
        public String get(String key) {
            return redis.opsForValue().get(key);
        }

        @Override
        public void set(String key, String value, int ttlSeconds) {
            redis.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
        }

        @Override
        public void increment(String key) {
            redis.opsForValue().increment(key);
        }
    }

    // The cache keeps copies of search answers: the free room numbers for one stay, such as "R7,R8".
    private final KeyValueStore store;

    public AvailabilityCache(KeyValueStore store) {
        this.store = store;
    }

    public String key(String checkIn, String checkOut) {
        // The version is part of the key, so raising it makes every older copy unreachable.
        String version = store.get(VERSION_KEY);
        return "availability:v" + (version == null ? "0" : version) + ":" + checkIn + ":" + checkOut;
    }

    public String get(String key) {
        return store.get(key);
    }

    public void set(String key, String roomNumbers) {
        store.set(key, roomNumbers, TTL_SECONDS);
    }

    // A booking or a cancellation changed what is free: every cached search is now out of date.
    public void invalidate() {
        store.increment(VERSION_KEY);
    }
}
