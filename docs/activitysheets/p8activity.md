# P08 · Activity sheet

## 1. P08 · Measure the search, then make it fast

P07 put the hotel app on HTTP with PostgreSQL. Its estimate said the search would be the busiest part. Today you measure it, cache it in Redis, measure again, and find the one place where a cache may be wrong.





### Where we are

Your hotel-booking folder has the P07 project: the app on http://localhost:5080 with three endpoints, PostgreSQL in Docker, 15 green checks, docs/capacity.md, docs/openapi.yaml and docs/api-check.md.

**Why measure the search before making it faster** In short: Without a number from before the change, nobody can say how much the change helped, or whether it helped at all. P07’s estimate said searches would be the bottleneck. An estimate is a prediction; today the running app gives the real numbers.

1. Step 1: 1,000 searches without a cache cost 2,000 database queries. You write the times down.
2. Step 4: the same 1,000 searches with the cache cost 2 queries. You write the new times next to the old ones.
3. The difference between the two rows is the gain. Without the first row there is nothing to compare with.

### Why that is a problem now

| What the hotel needs | What the P07 app does |
| --- | --- |
| 200 guests search within one minute, p95 ≤ 1 second (S1) | Every search asks PostgreSQL once per room: 133 questions a second at the peak with 40 rooms (docs/capacity.md). |
| At least 20% less energy per 1,000 searches (S6) | The same answer is computed again and again, although it changes only when someone books or cancels. |
| A number, not a guess | Nobody has measured the search yet: “To measure later” in docs/capacity.md is still open. |

### Today’s plan

| Step | What you do | Where it goes |
| --- | --- | --- |
| 1 · Measure without a cache | Add GET /stats and the measurement tool; 1,000 searches for 12–14 Nov; write down the average, p95 and what GET /stats counted | the Api file, the wiring, tools/measure, docs/cache-experiment.md row “No cache” |
| 2 · Start Redis | Add the cache service to docker-compose.yml; redis-cli ping answers PONG | docker-compose.yml |
| 3 · Cache-aside for the search | Key availability:v{version}:{checkIn}:{checkOut}, TTL 30 seconds; Redis first, PostgreSQL on a miss | the AvailabilityCache file in the Api folder |
| 4 · Measure with the cache | Empty the cache, then the same 1,000 searches: 2 database queries instead of 2,000; compare the times | docs/cache-experiment.md, row “Cache-aside, TTL 30 s” |
| 5 · See the stale result | Search, book R7, search again: R7 is still listed; Omar asks for R7: 409, because booking reads PostgreSQL | docs/cache-experiment.md, “Stale result” |
| 6 · Invalidate on every change | A successful POST or DELETE raises availability:version; two new tests | the Api file and the API tests |
| 7 · Hot key and the next bottleneck | Everyone searches 31 Dec–2 Jan: one key; the work moves to POST /reservations and PostgreSQL | docs/cache-experiment.md |
| 8 · ADR-002 | PostgreSQL is the source of truth; TTL 30 s plus invalidation; booking never reads the cache | docs/adr-002-cache-availability.md |

### How you work today

Work in pairs on one laptop, in the hotel-booking folder from P07, in the same language as before. Swap who types at every new step. Every code slide has a tab for each of the six languages.

You need Docker Desktop running (PostgreSQL and now Redis run inside it), your editor, two terminals opened in the hotel-booking folder (one runs the app, one runs the measurement tool and curl.exe) and your browser. Internet is needed once, in Steps 2 and 3, to download Redis and your language’s Redis client.

If you missed P07: download the P07 runnable code in your language from the P07 lesson page. The “Runnable code” link on this page has the finished P08 project in all six languages.

### Completed example: your folder at the end of P08

### hotel-booking folder

#### C#

```text
hotel-booking/   end of P08
├── db/
│   └── init.sql
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md   NEW: the cache policy
│   ├── api-check.md
│   ├── cache-experiment.md   NEW: the measurements and the stale result
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   CHANGED: + GET /stats
│   ├── quality-scenarios.md
│   └── requirements.md
├── HotelBooking/
│   ├── Api/
│   │   ├── AvailabilityCache.cs   NEW
│   │   ├── HotelApi.cs   CHANGED: GET /stats, the cache, invalidation
│   │   └── Stats.cs   NEW
│   ├── Notifications/
│   │   ├── Internal/
│   │   │   ├── EmailNotificationSender.cs
│   │   │   └── SmsNotificationSender.cs
│   │   ├── Confirmation.cs
│   │   └── INotificationSender.cs
│   ├── Payments/
│   │   ├── Internal/
│   │   │   └── SimulatedPaymentService.cs
│   │   ├── IPaymentService.cs
│   │   └── PaymentStatus.cs
│   ├── Reservations/
│   │   ├── Internal/
│   │   │   ├── InMemoryReservationStore.cs
│   │   │   └── PostgresReservationStore.cs
│   │   ├── BookingResult.cs
│   │   ├── BookingService.cs
│   │   ├── DateRange.cs
│   │   ├── Guest.cs
│   │   ├── IReservationStore.cs
│   │   ├── Reservation.cs
│   │   └── ReservationStatus.cs
│   ├── Rooms/
│   │   ├── Internal/
│   │   │   ├── InMemoryRoomCatalog.cs
│   │   │   └── PostgresRoomCatalog.cs
│   │   ├── IRoomCatalog.cs
│   │   └── Room.cs
│   ├── HotelBooking.csproj   CHANGED: + StackExchange.Redis
│   └── Program.cs   CHANGED: the wiring connects to Redis and counts the queries
├── HotelBooking.Tests/
│   ├── ApiTests.cs   CHANGED: + 2 cache tests
│   ├── ArchitectureTests.cs
│   ├── BookingServiceTests.cs
│   └── HotelBooking.Tests.csproj
├── tools/
│   └── measure.cs   NEW
└── docker-compose.yml   CHANGED: + the Redis cache
```

#### Python

```text
hotel-booking/   end of P08
├── db/
│   └── init.sql
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md   NEW: the cache policy
│   ├── api-check.md
│   ├── cache-experiment.md   NEW: the measurements and the stale result
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   CHANGED: + GET /stats
│   ├── quality-scenarios.md
│   └── requirements.md
├── hotel_booking/
│   ├── notifications/
│   │   ├── __init__.py
│   │   └── internal.py
│   ├── payments/
│   │   ├── __init__.py
│   │   └── internal.py
│   ├── reservations/
│   │   ├── __init__.py
│   │   ├── booking.py
│   │   ├── domain.py
│   │   └── internal.py
│   ├── rooms/
│   │   ├── __init__.py
│   │   ├── catalog.py
│   │   └── internal.py
│   ├── __init__.py
│   ├── api_cache.py   NEW
│   └── api.py   CHANGED: GET /stats, the cache, invalidation
├── tools/
│   └── measure.py   NEW
├── docker-compose.yml   CHANGED: + the Redis cache
├── main.py   CHANGED: the wiring connects to Redis and counts the queries
├── pyproject.toml
├── requirements.txt   CHANGED: + redis
├── test_api.py   CHANGED: + 2 cache tests
└── test_booking_service.py
```

#### Java

```text
hotel-booking/   end of P08
├── db/
│   └── init.sql
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md   NEW: the cache policy
│   ├── api-check.md
│   ├── cache-experiment.md   NEW: the measurements and the stale result
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   CHANGED: + GET /stats
│   ├── quality-scenarios.md
│   └── requirements.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── hotelbooking/
│   │   │       ├── api/
│   │   │       │   ├── AvailabilityCache.java   NEW
│   │   │       │   ├── CountingReservationStore.java   NEW
│   │   │       │   ├── HotelApi.java   CHANGED: GET /stats, the cache, invalidation
│   │   │       │   └── Stats.java   NEW
│   │   │       ├── notifications/
│   │   │       │   ├── internal/
│   │   │       │   │   ├── EmailNotificationSender.java
│   │   │       │   │   └── SmsNotificationSender.java
│   │   │       │   ├── Confirmation.java
│   │   │       │   └── NotificationSender.java
│   │   │       ├── payments/
│   │   │       │   ├── internal/
│   │   │       │   │   └── SimulatedPaymentService.java
│   │   │       │   ├── PaymentService.java
│   │   │       │   └── PaymentStatus.java
│   │   │       ├── reservations/
│   │   │       │   ├── internal/
│   │   │       │   │   ├── InMemoryReservationStore.java
│   │   │       │   │   └── PostgresReservationStore.java
│   │   │       │   ├── BookingResult.java
│   │   │       │   ├── BookingService.java
│   │   │       │   ├── DateRange.java
│   │   │       │   ├── Guest.java
│   │   │       │   ├── Reservation.java
│   │   │       │   ├── ReservationStatus.java
│   │   │       │   └── ReservationStore.java
│   │   │       ├── rooms/
│   │   │       │   ├── internal/
│   │   │       │   │   ├── InMemoryRoomCatalog.java
│   │   │       │   │   └── PostgresRoomCatalog.java
│   │   │       │   ├── Room.java
│   │   │       │   └── RoomCatalog.java
│   │   │       └── Main.java   CHANGED: the wiring connects to Redis and counts the queries
│   │   └── resources/
│   │       └── application.properties   CHANGED: + the Redis port
│   └── test/
│       └── java/
│           └── hotelbooking/
│               ├── ApiTests.java   CHANGED: + 2 cache tests
│               ├── ArchitectureTests.java
│               └── BookingServiceTests.java
├── tools/
│   └── Measure.java   NEW
├── docker-compose.yml   CHANGED: + the Redis cache
└── pom.xml   CHANGED: + spring-boot-starter-data-redis
```

#### Go

```text
hotel-booking/   end of P08
├── api/
│   ├── api_test.go   CHANGED: + 2 cache tests
│   ├── api.go   CHANGED: GET /stats, the cache, invalidation
│   ├── cache.go   NEW
│   └── stats.go   NEW
├── db/
│   └── init.sql
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md   NEW: the cache policy
│   ├── api-check.md
│   ├── cache-experiment.md   NEW: the measurements and the stale result
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   CHANGED: + GET /stats
│   ├── quality-scenarios.md
│   └── requirements.md
├── notifications/
│   ├── internal.go
│   └── notifications.go
├── payments/
│   ├── internal.go
│   └── payments.go
├── reservations/
│   ├── booking_test.go
│   ├── booking.go
│   ├── domain.go
│   └── internal.go
├── rooms/
│   ├── internal.go
│   └── rooms.go
├── tools/
│   └── measure/
│       └── main.go   NEW
├── architecture_test.go
├── docker-compose.yml   CHANGED: + the Redis cache
├── go.mod   CHANGED: + go-redis
├── go.sum   CHANGED: written by go get
└── main.go   CHANGED: the wiring connects to Redis and counts the queries
```

#### JavaScript

```text
hotel-booking/   end of P08
├── db/
│   └── init.sql
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md   NEW: the cache policy
│   ├── api-check.md
│   ├── cache-experiment.md   NEW: the measurements and the stale result
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   CHANGED: + GET /stats
│   ├── quality-scenarios.md
│   └── requirements.md
├── src/
│   ├── notifications/
│   │   ├── internal/
│   │   │   ├── email.js
│   │   │   └── sms.js
│   │   └── index.js
│   ├── payments/
│   │   ├── internal/
│   │   │   └── simulated.js
│   │   └── index.js
│   ├── reservations/
│   │   ├── internal/
│   │   │   ├── postgres-store.js
│   │   │   └── store.js
│   │   ├── booking.js
│   │   ├── domain.js
│   │   └── index.js
│   ├── rooms/
│   │   ├── internal/
│   │   │   ├── catalog.js
│   │   │   └── postgres-catalog.js
│   │   └── index.js
│   ├── api-cache.js   NEW
│   └── api.js   CHANGED: GET /stats, the cache, invalidation
├── test/
│   ├── api.test.js   CHANGED: + 2 cache tests
│   ├── architecture.test.js
│   └── booking.test.js
├── tools/
│   └── measure.js   NEW
├── docker-compose.yml   CHANGED: + the Redis cache
├── main.js   CHANGED: the wiring connects to Redis and counts the queries
├── package-lock.json   CHANGED: written by npm install
└── package.json   CHANGED: + redis
```

#### Dart

```text
hotel-booking/   end of P08
├── bin/
│   └── main.dart   CHANGED: the wiring connects to Redis and counts the queries
├── db/
│   └── init.sql
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md   NEW: the cache policy
│   ├── api-check.md
│   ├── cache-experiment.md   NEW: the measurements and the stale result
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   CHANGED: + GET /stats
│   ├── quality-scenarios.md
│   └── requirements.md
├── lib/
│   ├── notifications/
│   │   ├── src/
│   │   │   ├── email.dart
│   │   │   └── sms.dart
│   │   └── notifications.dart
│   ├── payments/
│   │   ├── src/
│   │   │   └── simulated.dart
│   │   └── payments.dart
│   ├── reservations/
│   │   ├── src/
│   │   │   ├── booking.dart
│   │   │   ├── domain.dart
│   │   │   ├── postgres_store.dart
│   │   │   └── store.dart
│   │   └── reservations.dart
│   ├── rooms/
│   │   ├── src/
│   │   │   ├── catalog.dart
│   │   │   └── postgres_catalog.dart
│   │   └── rooms.dart
│   ├── api_cache.dart   NEW
│   └── api.dart   CHANGED: GET /stats, the cache, invalidation
├── test/
│   ├── api_test.dart   CHANGED: + 2 cache tests
│   ├── architecture_test.dart
│   └── booking_service_test.dart
├── tool/
│   └── measure.dart   NEW
├── analysis_options.yaml
├── docker-compose.yml   CHANGED: + the Redis cache
└── pubspec.yaml   CHANGED: + redis
```

### Your turn · open your project

1. Start Docker Desktop and wait until it says “Engine running”.
2. Open your hotel-booking folder in your editor, and open two terminals in the same folder.
3. In the first terminal run docker compose up -d, then your P07 checks: 15 green (Python: 12 tests, then “Contracts: 3 kept”).
4. Open docs/design-notes.md and add a new heading at the bottom: # P08 design notes
5. Open docs/capacity.md and find “To measure later”: today answers both lines.

**When you finish, you have:** Docker Desktop running, your P07 project open with green checks, and a “P08 design notes” heading.
### Explanation

BookingService does not change today, and booking never reads the cache. The new code sits in the Api folder (the counters, the cache) and in the wiring (Redis and the counting store).



---

## 2. Step 1 · Measure the search without a cache

Before changing anything, count what a search costs. The app gets four counters and GET /stats; a small tool sends 1,000 searches and prints the times and the counts.





### What Step 1 adds

| What | Where | What it does |
| --- | --- | --- |
| Stats | Next to the Api file | Four counters: searches, databaseQueries, cacheHits, cacheMisses |
| CountingReservationStore | Next to the Api file | Adds 1 to databaseQueries for every question the store sends to PostgreSQL |
| GET /stats | The Api file and docs/openapi.yaml | Answers the four counters as JSON |
| The wiring | Your wiring file | Puts the counting store around the PostgreSQL store in the unit of work |
| The measurement tool | tools/ (Dart: tool/) | 1,000 searches, one after another; prints the average, p95, total and what GET /stats counted |

**GET /stats: four counters inside the app** In short: The app counts every search, every question to PostgreSQL, and every cache hit and miss, and GET /stats shows the four numbers. Several requests can run at the same moment. In C#, Java, Go and Python two threads adding 1 at the same moment could lose one of the additions, so the counters use Interlocked, AtomicLong, atomic.Int64 or a lock. Node.js and Dart run one request’s code at a time, so a plain ++ is exact.

1. The app starts: {"searches":0,"databaseQueries":0,"cacheHits":0,"cacheMisses":0}.
2. One search without a cache: searches 1, databaseQueries 2 (one question per room: R7, R8).
3. The counters only grow. They start at 0 again when the app restarts.

C#:

```text
// HotelBooking/Api/Stats.cs
using HotelBooking.Reservations;
using HotelBooking.Rooms;

namespace HotelBooking.Api;

// The four counters of GET /stats. ASP.NET Core runs requests on several threads at once,
// so Interlocked makes every +1 exact.
public class Stats
{
    private long _searches, _databaseQueries, _cacheHits, _cacheMisses;

    public void AddSearch() => Interlocked.Increment(ref _searches);
    public void AddDatabaseQuery() => Interlocked.Increment(ref _databaseQueries);
    public void AddCacheHit() => Interlocked.Increment(ref _cacheHits);
    public void AddCacheMiss() => Interlocked.Increment(ref _cacheMisses);

    public object Answer() => new
    {
        searches = Interlocked.Read(ref _searches), databaseQueries = Interlocked.Read(ref _databaseQueries),
        cacheHits = Interlocked.Read(ref _cacheHits), cacheMisses = Interlocked.Read(ref _cacheMisses),
    };
}

// Passes every call on to the real store and counts it: each call is one question to the database.
public class CountingReservationStore(IReservationStore store, Stats stats) : IReservationStore
{
    public int Count
    {
        get
        {
            stats.AddDatabaseQuery();
            return store.Count;
        }
    }

    public bool HasOverlap(Room room, DateRange stay)
    {
        stats.AddDatabaseQuery();
        return store.HasOverlap(room, stay);
    }

    public void Save(Reservation reservation)
    {
        stats.AddDatabaseQuery();
        store.Save(reservation);
    }

    public Reservation? Find(string id)
    {
        stats.AddDatabaseQuery();
        return store.Find(id);
    }

    public int CountConfirmed(Room room, DateRange stay)
    {
        stats.AddDatabaseQuery();
        return store.CountConfirmed(room, stay);
    }
}
```

Python:

```text
class Stats:
    """The four counters of GET /stats. FastAPI runs requests on several threads at once,
    so a lock makes every += 1 exact."""

    def __init__(self) -> None:
        self._lock = threading.Lock()
        self._counts = {"searches": 0, "databaseQueries": 0, "cacheHits": 0, "cacheMisses": 0}

    def add(self, name: str) -> None:
        with self._lock:
            self._counts[name] += 1

    def as_dict(self) -> dict[str, int]:
        with self._lock:
            return dict(self._counts)

class CountingReservationStore:
    """Passes every call on to the real store and counts it: each call is one question to the database."""

    def __init__(self, store: ReservationStore, stats: Stats) -> None:
        self._store = store
        self._stats = stats

    def count(self) -> int:
        self._stats.add("databaseQueries")
        return self._store.count()

    def has_overlap(self, room: Room, stay: DateRange) -> bool:
        self._stats.add("databaseQueries")
        return self._store.has_overlap(room, stay)

    def save(self, reservation: Reservation) -> None:
        self._stats.add("databaseQueries")
        self._store.save(reservation)

    def find(self, reservation_id: str) -> Reservation | None:
        self._stats.add("databaseQueries")
        return self._store.find(reservation_id)

    def count_confirmed(self, room: Room, stay: DateRange) -> int:
        self._stats.add("databaseQueries")
        return self._store.count_confirmed(room, stay)
```

Java:

```text
// src/main/java/hotelbooking/api/Stats.java
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

// src/main/java/hotelbooking/api/CountingReservationStore.java
package hotelbooking.api;

import hotelbooking.reservations.DateRange;
import hotelbooking.reservations.Reservation;
import hotelbooking.reservations.ReservationStore;
import hotelbooking.rooms.Room;

// Passes every call on to the real store and counts it: each call is one question to the database.
public class CountingReservationStore implements ReservationStore {
    private final ReservationStore store;
    private final Stats stats;

    public CountingReservationStore(ReservationStore store, Stats stats) {
        this.store = store;
        this.stats = stats;
    }

    @Override
    public int count() {
        stats.databaseQueries.incrementAndGet();
        return store.count();
    }

    @Override
    public boolean hasOverlap(Room room, DateRange stay) {
        stats.databaseQueries.incrementAndGet();
        return store.hasOverlap(room, stay);
    }

    @Override
    public void save(Reservation reservation) {
        stats.databaseQueries.incrementAndGet();
        store.save(reservation);
    }

    @Override
    public Reservation find(String id) {
        stats.databaseQueries.incrementAndGet();
        return store.find(id);
    }

    @Override
    public int countConfirmed(Room room, DateRange stay) {
        stats.databaseQueries.incrementAndGet();
        return store.countConfirmed(room, stay);
    }
}
```

Go:

```text
// api/stats.go
package api

import (
	"sync/atomic"

	"hotelbooking/reservations"
	"hotelbooking/rooms"
)

// Stats holds the four counters of GET /stats. Requests run at the same time on several
// goroutines, so each counter is atomic: Add(1) is exact even then.
type Stats struct {
	Searches        atomic.Int64
	DatabaseQueries atomic.Int64
	CacheHits       atomic.Int64
	CacheMisses     atomic.Int64
}

type statsJSON struct {
	Searches        int64 `json:"searches"`
	DatabaseQueries int64 `json:"databaseQueries"`
	CacheHits       int64 `json:"cacheHits"`
	CacheMisses     int64 `json:"cacheMisses"`
}

func (stats *Stats) answer() statsJSON {
	return statsJSON{stats.Searches.Load(), stats.DatabaseQueries.Load(), stats.CacheHits.Load(), stats.CacheMisses.Load()}
}

// CountingReservationStore passes every call on to the real store and counts it: each call is
// one question to the database.
type CountingReservationStore struct {
	store reservations.ReservationStore
	stats *Stats
}

func NewCountingReservationStore(store reservations.ReservationStore, stats *Stats) *CountingReservationStore {
	return &CountingReservationStore{store: store, stats: stats}
}

func (counting *CountingReservationStore) Count() int {
	counting.stats.DatabaseQueries.Add(1)
	return counting.store.Count()
}

func (counting *CountingReservationStore) HasOverlap(room rooms.Room, stay reservations.DateRange) bool {
	counting.stats.DatabaseQueries.Add(1)
	return counting.store.HasOverlap(room, stay)
}

func (counting *CountingReservationStore) Save(reservation *reservations.Reservation) {
	counting.stats.DatabaseQueries.Add(1)
	counting.store.Save(reservation)
}

func (counting *CountingReservationStore) Find(id string) *reservations.Reservation {
	counting.stats.DatabaseQueries.Add(1)
	return counting.store.Find(id)
}

func (counting *CountingReservationStore) CountConfirmed(room rooms.Room, stay reservations.DateRange) int {
	counting.stats.DatabaseQueries.Add(1)
	return counting.store.CountConfirmed(room, stay)
}
```

JavaScript:

```text
// The four counters of GET /stats. Node.js runs one request's code at a time, so ++ is exact.
export class Stats {
  searches = 0;
  databaseQueries = 0;
  cacheHits = 0;
  cacheMisses = 0;
}

// Passes every call on to the real store and counts it: each call is one question to the database.
export class CountingReservationStore {
  #store;
  #stats;

  constructor(store, stats) {
    this.#store = store;
    this.#stats = stats;
  }

  get count() {
    this.#stats.databaseQueries++;
    return this.#store.count;
  }

  hasOverlap(room, stay) {
    this.#stats.databaseQueries++;
    return this.#store.hasOverlap(room, stay);
  }

  save(reservation) {
    this.#stats.databaseQueries++;
    return this.#store.save(reservation);
  }

  find(id) {
    this.#stats.databaseQueries++;
    return this.#store.find(id);
  }

  countConfirmed(room, stay) {
    this.#stats.databaseQueries++;
    return this.#store.countConfirmed(room, stay);
  }
}
```

Dart:

```text
// The four counters of GET /stats. A plain ++ is exact: Dart runs every request on one isolate.
class Stats {
  int searches = 0;
  int databaseQueries = 0;
  int cacheHits = 0;
  int cacheMisses = 0;

  Map<String, int> toJson() => {
        'searches': searches,
        'databaseQueries': databaseQueries,
        'cacheHits': cacheHits,
        'cacheMisses': cacheMisses,
      };
}

// Passes every call on to the real store and counts it: each call is one question to the database.
class CountingReservationStore implements ReservationStore {
  CountingReservationStore(this._store, this._stats);

  final ReservationStore _store;
  final Stats _stats;

  @override
  FutureOr<int> get count {
    _stats.databaseQueries++;
    return _store.count;
  }

  @override
  FutureOr<bool> hasOverlap(Room room, DateRange stay) {
    _stats.databaseQueries++;
    return _store.hasOverlap(room, stay);
  }

  @override
  FutureOr<void> save(Reservation reservation) {
    _stats.databaseQueries++;
    return _store.save(reservation);
  }

  @override
  FutureOr<Reservation?> find(String id) {
    _stats.databaseQueries++;
    return _store.find(id);
  }

  @override
  FutureOr<int> countConfirmed(Room room, DateRange stay) {
    _stats.databaseQueries++;
    return _store.countConfirmed(room, stay);
  }
}
```

**How the app counts real database questions** In short: A small store that passes every call on to the real store and adds 1 each time: BookingService does not notice and does not change. This is the decorator idea from P04: the counting store keeps the same promise as the store inside it. The wiring puts it around the PostgreSQL store in the unit of work; the API tests put it around the list store.

1. A search calls HasOverlap(R7) and HasOverlap(R8) on the counting store.
2. Each call adds 1 to databaseQueries and then asks the PostgreSQL store, which sends one SELECT.
3. A booking calls HasOverlap, Count and Save: 3 more. SaveChanges at the end of a cancel is not one of the store’s promised calls, so it is not counted.

C#:

```text
void RunApi()
{
    // The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
    var builder = WebApplication.CreateBuilder(args);
    var connection = $"Host=localhost;Port={Environment.GetEnvironmentVariable("HOTEL_DB_PORT") ?? "5432"};Database=hotel;Username=hotel;Password=hotel";
    builder.Services.AddDbContextFactory<RoomsDb>(options => options.UseNpgsql(connection));
    builder.Services.AddDbContextFactory<ReservationsDb>(options => options.UseNpgsql(connection));
    var app = builder.Build();

    var rooms = new PostgresRoomCatalog(app.Services.GetRequiredService<IDbContextFactory<RoomsDb>>()).All();
    var databases = app.Services.GetRequiredService<IDbContextFactory<ReservationsDb>>();
    var email = new EmailNotificationSender();
    var stats = new Stats();

    // One request: a fresh store over its own DbContext, counted for GET /stats; save what changed at the end.
    IResult UnitOfWork(Func<BookingService, IResult> work)
    {
        using var db = databases.CreateDbContext();
        var store = new PostgresReservationStore(db, rooms);
        var result = work(new BookingService(rooms, new CountingReservationStore(store, stats), email));
        store.SaveChanges();
        return result;
    }

    HotelApi.Map(app, rooms, UnitOfWork, stats);
    app.Run("http://localhost:5080");
}
```

Python:

```text
def run_api() -> None:
    # The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
    engine = create_engine(f"postgresql+psycopg://hotel:hotel@localhost:{os.getenv('HOTEL_DB_PORT', '5432')}/hotel")
    rooms = PostgresRoomCatalog(engine).all()
    email = EmailNotificationSender()
    stats = Stats()

    @contextmanager
    def unit_of_work():
        # One request: a fresh store over its own session, counted for GET /stats; save what changed at the end.
        with Session(engine) as session:
            store = PostgresReservationStore(session, rooms)
            yield BookingService(rooms, CountingReservationStore(store, stats), email)
            store.save_changes()

    uvicorn.run(create_app(rooms, unit_of_work, stats), host="localhost", port=5080)
```

Java:

```text
// The wiring for the web app.
@Bean
HotelApi hotelApi(EntityManager db, TransactionTemplate transactions) {
    List<Room> rooms = new PostgresRoomCatalog(db).all();
    EmailNotificationSender email = new EmailNotificationSender();
    Stats stats = new Stats();
    // One request: a fresh store in its own transaction, counted for GET /stats; save what changed at the end.
    HotelApi.UnitOfWork unitOfWork = work -> transactions.execute(transaction -> {
        PostgresReservationStore store = new PostgresReservationStore(db, rooms);
        var result = work.apply(new BookingService(rooms, new CountingReservationStore(store, stats), email));
        store.saveChanges();
        return result;
    });
    return new HotelApi(rooms, unitOfWork, stats);
}
```

Go:

```text
func runAPI() {
	// The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
	ctx := context.Background()
	pool, err := pgxpool.New(ctx, "postgres://hotel:hotel@localhost:"+cmp.Or(os.Getenv("HOTEL_DB_PORT"), "5432")+"/hotel")
	if err != nil {
		log.Fatal(err)
	}
	defer pool.Close()
	hotelRooms := rooms.NewPostgresRoomCatalog(pool).All()
	email := &notifications.EmailNotificationSender{}
	stats := &api.Stats{}

	// One request: a fresh store, counted for GET /stats; save what changed at the end.
	unitOfWork := func(work func(service *reservations.BookingService)) error {
		store := reservations.NewPostgresReservationStore(ctx, pool, hotelRooms)
		work(reservations.NewBookingService(hotelRooms, api.NewCountingReservationStore(store, stats), email, 0))
		return store.SaveChanges()
	}

	fmt.Println("Listening on http://localhost:5080")
	log.Fatal(http.ListenAndServe("localhost:5080", api.New(hotelRooms, unitOfWork, stats)))
}
```

JavaScript:

```text
async function runApi() {
  // The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
  const pool = new pg.Pool({ host: 'localhost', port: Number(process.env.HOTEL_DB_PORT ?? 5432), database: 'hotel', user: 'hotel', password: 'hotel' });
  const rooms = await new PostgresRoomCatalog(pool).all();
  const email = new EmailNotificationSender();
  const stats = new Stats();

  // One request: a fresh store, counted for GET /stats; save what changed at the end.
  const unitOfWork = async work => {
    const store = new PostgresReservationStore(pool, rooms);
    const result = await work(new BookingService(rooms, new CountingReservationStore(store, stats), email));
    await store.saveChanges();
    return result;
  };

  createApi(rooms, unitOfWork, stats).listen(5080, 'localhost', () => console.log('Listening on http://localhost:5080'));
}
```

Dart:

```text
Future<void> runApi() async {
  // The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
  final port = int.parse(Platform.environment['HOTEL_DB_PORT'] ?? '5432');
  final pool = Pool.withEndpoints([
    Endpoint(host: 'localhost', port: port, database: 'hotel', username: 'hotel', password: 'hotel')
  ], settings: PoolSettings(sslMode: SslMode.disable));
  final rooms = await PostgresRoomCatalog(pool).all();
  final email = EmailNotificationSender();
  final stats = Stats();

  // One request: a fresh store, counted for GET /stats; save what changed at the end.
  Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async {
    final store = PostgresReservationStore(pool, rooms);
    final result = await work(BookingService(rooms, CountingReservationStore(store, stats), email));
    await store.saveChanges();
    return result;
  }

  await serve(createApi(rooms, unitOfWork, stats), 'localhost', 5080);
  print('Listening on http://localhost:5080');
}
```

**What the measurement tool does, line by line** In short: It reads GET /stats, sends 1,000 searches one after another and times each one, reads GET /stats again, and prints the times and the difference. It uses only your language’s standard library and one HTTP connection for all 1,000 requests, so the times show the app, not the opening of new connections.

1. Before: GET /stats says searches 3 (your own curl.exe tests).
2. 1,000 searches for 12–14 Nov, each timed in milliseconds.
3. After: GET /stats says searches 1,003. The tool prints the difference: 1,000.
4. The times are sorted: the average, the 950th (p95) and the total.

C#:

```text
// tools/measure.cs
// Sends 1,000 searches to the running app, one after another, and prints how long they took
// and what they cost: the difference of GET /stats before and after.
//
// dotnet run tools/measure.cs                         -> 12-14 Nov
// dotnet run tools/measure.cs 2026-12-31 2027-01-02   -> other dates
using System.Diagnostics;
using System.Globalization;
using System.Text.Json;

const int searches = 1000;
string[] names = ["searches", "databaseQueries", "cacheHits", "cacheMisses"];
var (checkIn, checkOut) = args.Length >= 2 ? (args[0], args[1]) : ("2026-11-12", "2026-11-14");
var culture = CultureInfo.InvariantCulture;

// One client: it keeps the connection open and reuses it for every request.
using var client = new HttpClient { BaseAddress = new Uri("http://localhost:5080") };

async Task<string> Get(string path)
{
    var response = await client.GetAsync(path);
    var body = await response.Content.ReadAsStringAsync();
    if ((int)response.StatusCode != 200)
    {
        Console.Error.WriteLine($"GET {path} answered {(int)response.StatusCode}: {body}");
        Environment.Exit(1);
    }
    return body;
}

async Task<JsonElement> Stats() => JsonDocument.Parse(await Get("/stats")).RootElement;

var before = await Stats();
var times = new List<double>();
var started = Stopwatch.StartNew();
for (var i = 0; i < searches; i++)
{
    var start = Stopwatch.GetTimestamp();
    await Get($"/rooms/availability?checkIn={checkIn}&checkOut={checkOut}");
    times.Add(Stopwatch.GetElapsedTime(start).TotalMilliseconds);
}
var total = started.Elapsed.TotalSeconds;
var after = await Stats();

times.Sort();
var p95 = times[(int)Math.Ceiling(0.95 * searches) - 1]; // 950 of the 1,000 searches were this fast or faster
var difference = string.Join(",", names.Select(name => $"\"{name}\":{after.GetProperty(name).GetInt64() - before.GetProperty(name).GetInt64()}"));
Console.WriteLine($"{searches} searches for {checkIn} to {checkOut}");
Console.WriteLine($"Average: {times.Average().ToString("F1", culture)} ms");
Console.WriteLine($"p95: {p95.ToString("F1", culture)} ms");
Console.WriteLine($"Total: {total.ToString("F1", culture)} s");
Console.WriteLine($"GET /stats difference: {{{difference}}}");
```

Python:

```text
# tools/measure.py
"""Sends 1,000 searches to the running app, one after another, and prints how long they took
and what they cost: the difference of GET /stats before and after.

python tools/measure.py                         -> 12-14 Nov
python tools/measure.py 2026-12-31 2027-01-02   -> other dates
"""
import http.client
import json
import math
import sys
import time

SEARCHES = 1000
NAMES = ("searches", "databaseQueries", "cacheHits", "cacheMisses")

check_in, check_out = sys.argv[1:3] if len(sys.argv) >= 3 else ("2026-11-12", "2026-11-14")
connection = http.client.HTTPConnection("localhost", 5080)  # one connection for every request


def get(path: str) -> str:
    connection.request("GET", path)
    response = connection.getresponse()
    body = response.read().decode("utf-8")
    if response.status != 200:
        sys.exit(f"GET {path} answered {response.status}: {body}")
    return body


before = json.loads(get("/stats"))
times = []
started = time.perf_counter()
for _ in range(SEARCHES):
    start = time.perf_counter()
    get(f"/rooms/availability?checkIn={check_in}&checkOut={check_out}")
    times.append((time.perf_counter() - start) * 1000)
total = time.perf_counter() - started
after = json.loads(get("/stats"))

times.sort()
p95 = times[math.ceil(0.95 * SEARCHES) - 1]  # 950 of the 1,000 searches were this fast or faster
difference = ",".join(f'"{name}":{after[name] - before[name]}' for name in NAMES)
print(f"{SEARCHES} searches for {check_in} to {check_out}")
print(f"Average: {sum(times) / SEARCHES:.1f} ms")
print(f"p95: {p95:.1f} ms")
print(f"Total: {total:.1f} s")
print(f"GET /stats difference: {{{difference}}}")
```

Java:

```text
// tools/Measure.java
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
```

Go:

```text
// tools/measure/main.go
// Sends 1,000 searches to the running app, one after another, and prints how long they took
// and what they cost: the difference of GET /stats before and after.
//
// go run ./tools/measure                         -> 12-14 Nov
// go run ./tools/measure 2026-12-31 2027-01-02   -> other dates
package main

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"math"
	"net/http"
	"os"
	"slices"
	"strings"
	"time"
)

const searches = 1000

var names = []string{"searches", "databaseQueries", "cacheHits", "cacheMisses"}

// One client: it keeps the connection open and reuses it for every request.
var client = &http.Client{}

func get(path string) []byte {
	response, err := client.Get("http://localhost:5080" + path)
	if err != nil {
		log.Fatal(err)
	}
	defer response.Body.Close()
	body, _ := io.ReadAll(response.Body) // read it all, so the connection can be reused
	if response.StatusCode != http.StatusOK {
		log.Fatalf("GET %s answered %d: %s", path, response.StatusCode, body)
	}
	return body
}

func stats() map[string]int64 {
	var counters map[string]int64
	json.Unmarshal(get("/stats"), &counters)
	return counters
}

func main() {
	checkIn, checkOut := "2026-11-12", "2026-11-14"
	if len(os.Args) >= 3 {
		checkIn, checkOut = os.Args[1], os.Args[2]
	}

	before := stats()
	times := make([]float64, 0, searches)
	started := time.Now()
	for range searches {
		start := time.Now()
		get("/rooms/availability?checkIn=" + checkIn + "&checkOut=" + checkOut)
		times = append(times, float64(time.Since(start).Microseconds())/1000)
	}
	total := time.Since(started).Seconds()
	after := stats()

	slices.Sort(times)
	p95 := times[int(math.Ceil(0.95*searches))-1] // 950 of the 1,000 searches were this fast or faster
	sum := 0.0
	for _, t := range times {
		sum += t
	}
	difference := []string{}
	for _, name := range names {
		difference = append(difference, fmt.Sprintf("%q:%d", name, after[name]-before[name]))
	}
	fmt.Printf("%d searches for %s to %s\n", searches, checkIn, checkOut)
	fmt.Printf("Average: %.1f ms\n", sum/searches)
	fmt.Printf("p95: %.1f ms\n", p95)
	fmt.Printf("Total: %.1f s\n", total)
	fmt.Printf("GET /stats difference: {%s}\n", strings.Join(difference, ","))
}
```

JavaScript:

```text
// tools/measure.js
// Sends 1,000 searches to the running app, one after another, and prints how long they took
// and what they cost: the difference of GET /stats before and after.
//
// node tools/measure.js                         -> 12-14 Nov
// node tools/measure.js 2026-12-31 2027-01-02   -> other dates
import http from 'node:http';

const searches = 1000;
const names = ['searches', 'databaseQueries', 'cacheHits', 'cacheMisses'];
const [checkIn = '2026-11-12', checkOut = '2026-11-14'] = process.argv.slice(2);

// One connection, kept open and reused for every request.
const agent = new http.Agent({ keepAlive: true, maxSockets: 1 });
const get = path => new Promise((resolve, reject) => {
  http.get({ host: 'localhost', port: 5080, path, agent }, response => {
    let body = '';
    response.setEncoding('utf8');
    response.on('data', chunk => { body += chunk; });
    response.on('end', () => {
      if (response.statusCode !== 200) {
        console.error(`GET ${path} answered ${response.statusCode}: ${body}`);
        process.exit(1);
      }
      resolve(body);
    });
  }).on('error', reject);
});

const before = JSON.parse(await get('/stats'));
const times = [];
const started = performance.now();
for (let i = 0; i < searches; i++) {
  const start = performance.now();
  await get(`/rooms/availability?checkIn=${checkIn}&checkOut=${checkOut}`);
  times.push(performance.now() - start);
}
const total = (performance.now() - started) / 1000;
const after = JSON.parse(await get('/stats'));

times.sort((a, b) => a - b);
const p95 = times[Math.ceil(0.95 * searches) - 1]; // 950 of the 1,000 searches were this fast or faster
const average = times.reduce((sum, time) => sum + time, 0) / searches;
const difference = names.map(name => `"${name}":${after[name] - before[name]}`).join(',');
console.log(`${searches} searches for ${checkIn} to ${checkOut}`);
console.log(`Average: ${average.toFixed(1)} ms`);
console.log(`p95: ${p95.toFixed(1)} ms`);
console.log(`Total: ${total.toFixed(1)} s`);
console.log(`GET /stats difference: {${difference}}`);
agent.destroy();
```

Dart:

```text
// tool/measure.dart
// Sends 1,000 searches to the running app, one after another, and prints how long they took
// and what they cost: the difference of GET /stats before and after.
//
// dart run tool/measure.dart                         -> 12-14 Nov
// dart run tool/measure.dart 2026-12-31 2027-01-02   -> other dates
import 'dart:convert';
import 'dart:io';

const searches = 1000;
const names = ['searches', 'databaseQueries', 'cacheHits', 'cacheMisses'];

final client = HttpClient(); // one client, so every request goes over the same open connection

Future<String> get(String path) async {
  final request = await client.getUrl(Uri.parse('http://localhost:5080$path'));
  final response = await request.close();
  final body = await response.transform(utf8.decoder).join();
  if (response.statusCode != 200) {
    stderr.writeln('GET $path answered ${response.statusCode}: $body');
    exit(1);
  }
  return body;
}

Future<void> main(List<String> args) async {
  final checkIn = args.length >= 2 ? args[0] : '2026-11-12';
  final checkOut = args.length >= 2 ? args[1] : '2026-11-14';

  final before = jsonDecode(await get('/stats')) as Map<String, dynamic>;
  final times = <double>[];
  final total = Stopwatch()..start();
  for (var i = 0; i < searches; i++) {
    final watch = Stopwatch()..start();
    await get('/rooms/availability?checkIn=$checkIn&checkOut=$checkOut');
    times.add(watch.elapsedMicroseconds / 1000);
  }
  final seconds = total.elapsedMicroseconds / 1000000;
  final after = jsonDecode(await get('/stats')) as Map<String, dynamic>;
  client.close();

  times.sort();
  // 950 of the 1,000 searches were this fast or faster.
  final p95 = times[(0.95 * searches).ceil() - 1];
  final average = times.reduce((a, b) => a + b) / searches;
  final difference =
      names.map((name) => '"$name":${(after[name] as int) - (before[name] as int)}').join(',');
  print('$searches searches for $checkIn to $checkOut');
  print('Average: ${average.toStringAsFixed(1)} ms');
  print('p95: ${p95.toStringAsFixed(1)} ms');
  print('Total: ${seconds.toStringAsFixed(1)} s');
  print('GET /stats difference: {$difference}');
}
```

### Where Stats and the counting store live

| Language | File |
| --- | --- |
| C# | HotelBooking/Api/Stats.cs (new) |
| Python | hotel_booking/api.py |
| Java | api/Stats.java and api/CountingReservationStore.java (new) |
| Go | api/stats.go (new) |
| JavaScript | src/api.js |
| Dart | lib/api.dart |

### Completed example 1.1 · the contract gains GET /stats

Add the operation under paths: and the schema under components: schemas:. The version becomes 1.1.0: a new operation breaks no client.

### docs/openapi.yaml

```text
# docs/openapi.yaml: the new operation (under paths:) and its schema (under components: schemas:)
  /stats:
    get:
      operationId: getStats
      summary: What the searches cost since the app started
      description: Four counters for measuring the search. They start at 0 when the app starts and count this running app only (each instance has its own). databaseQueries counts every question the reservation store sends to PostgreSQL.
      responses:
        "200":
          description: The four counters.
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Stats" }
              example: { searches: 1000, databaseQueries: 2000, cacheHits: 0, cacheMisses: 0 }

    Stats:
      type: object
      required: [searches, databaseQueries, cacheHits, cacheMisses]
      properties:
        searches: { type: integer, minimum: 0, description: Answers of GET /rooms/availability that reached the search. }
        databaseQueries: { type: integer, minimum: 0, description: Questions the reservation store sent to PostgreSQL. }
        cacheHits: { type: integer, minimum: 0, description: Searches answered from a copy in Redis. }
        cacheMisses: { type: integer, minimum: 0, description: Searches that found no copy and asked PostgreSQL. }
```

### Completed example 1.2 · Stats and the counting store

**GET /stats: four counters inside the app** In short: The app counts every search, every question to PostgreSQL, and every cache hit and miss, and GET /stats shows the four numbers. Several requests can run at the same moment. In C#, Java, Go and Python two threads adding 1 at the same moment could lose one of the additions, so the counters use Interlocked, AtomicLong, atomic.Int64 or a lock. Node.js and Dart run one request’s code at a time, so a plain ++ is exact.

1. The app starts: {"searches":0,"databaseQueries":0,"cacheHits":0,"cacheMisses":0}.
2. One search without a cache: searches 1, databaseQueries 2 (one question per room: R7, R8).
3. The counters only grow. They start at 0 again when the app restarts.

C#:

```text
// HotelBooking/Api/Stats.cs
using HotelBooking.Reservations;
using HotelBooking.Rooms;

namespace HotelBooking.Api;

// The four counters of GET /stats. ASP.NET Core runs requests on several threads at once,
// so Interlocked makes every +1 exact.
public class Stats
{
    private long _searches, _databaseQueries, _cacheHits, _cacheMisses;

    public void AddSearch() => Interlocked.Increment(ref _searches);
    public void AddDatabaseQuery() => Interlocked.Increment(ref _databaseQueries);
    public void AddCacheHit() => Interlocked.Increment(ref _cacheHits);
    public void AddCacheMiss() => Interlocked.Increment(ref _cacheMisses);

    public object Answer() => new
    {
        searches = Interlocked.Read(ref _searches), databaseQueries = Interlocked.Read(ref _databaseQueries),
        cacheHits = Interlocked.Read(ref _cacheHits), cacheMisses = Interlocked.Read(ref _cacheMisses),
    };
}

// Passes every call on to the real store and counts it: each call is one question to the database.
public class CountingReservationStore(IReservationStore store, Stats stats) : IReservationStore
{
    public int Count
    {
        get
        {
            stats.AddDatabaseQuery();
            return store.Count;
        }
    }

    public bool HasOverlap(Room room, DateRange stay)
    {
        stats.AddDatabaseQuery();
        return store.HasOverlap(room, stay);
    }

    public void Save(Reservation reservation)
    {
        stats.AddDatabaseQuery();
        store.Save(reservation);
    }

    public Reservation? Find(string id)
    {
        stats.AddDatabaseQuery();
        return store.Find(id);
    }

    public int CountConfirmed(Room room, DateRange stay)
    {
        stats.AddDatabaseQuery();
        return store.CountConfirmed(room, stay);
    }
}
```

Python:

```text
class Stats:
    """The four counters of GET /stats. FastAPI runs requests on several threads at once,
    so a lock makes every += 1 exact."""

    def __init__(self) -> None:
        self._lock = threading.Lock()
        self._counts = {"searches": 0, "databaseQueries": 0, "cacheHits": 0, "cacheMisses": 0}

    def add(self, name: str) -> None:
        with self._lock:
            self._counts[name] += 1

    def as_dict(self) -> dict[str, int]:
        with self._lock:
            return dict(self._counts)

class CountingReservationStore:
    """Passes every call on to the real store and counts it: each call is one question to the database."""

    def __init__(self, store: ReservationStore, stats: Stats) -> None:
        self._store = store
        self._stats = stats

    def count(self) -> int:
        self._stats.add("databaseQueries")
        return self._store.count()

    def has_overlap(self, room: Room, stay: DateRange) -> bool:
        self._stats.add("databaseQueries")
        return self._store.has_overlap(room, stay)

    def save(self, reservation: Reservation) -> None:
        self._stats.add("databaseQueries")
        self._store.save(reservation)

    def find(self, reservation_id: str) -> Reservation | None:
        self._stats.add("databaseQueries")
        return self._store.find(reservation_id)

    def count_confirmed(self, room: Room, stay: DateRange) -> int:
        self._stats.add("databaseQueries")
        return self._store.count_confirmed(room, stay)
```

Java:

```text
// src/main/java/hotelbooking/api/Stats.java
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

// src/main/java/hotelbooking/api/CountingReservationStore.java
package hotelbooking.api;

import hotelbooking.reservations.DateRange;
import hotelbooking.reservations.Reservation;
import hotelbooking.reservations.ReservationStore;
import hotelbooking.rooms.Room;

// Passes every call on to the real store and counts it: each call is one question to the database.
public class CountingReservationStore implements ReservationStore {
    private final ReservationStore store;
    private final Stats stats;

    public CountingReservationStore(ReservationStore store, Stats stats) {
        this.store = store;
        this.stats = stats;
    }

    @Override
    public int count() {
        stats.databaseQueries.incrementAndGet();
        return store.count();
    }

    @Override
    public boolean hasOverlap(Room room, DateRange stay) {
        stats.databaseQueries.incrementAndGet();
        return store.hasOverlap(room, stay);
    }

    @Override
    public void save(Reservation reservation) {
        stats.databaseQueries.incrementAndGet();
        store.save(reservation);
    }

    @Override
    public Reservation find(String id) {
        stats.databaseQueries.incrementAndGet();
        return store.find(id);
    }

    @Override
    public int countConfirmed(Room room, DateRange stay) {
        stats.databaseQueries.incrementAndGet();
        return store.countConfirmed(room, stay);
    }
}
```

Go:

```text
// api/stats.go
package api

import (
	"sync/atomic"

	"hotelbooking/reservations"
	"hotelbooking/rooms"
)

// Stats holds the four counters of GET /stats. Requests run at the same time on several
// goroutines, so each counter is atomic: Add(1) is exact even then.
type Stats struct {
	Searches        atomic.Int64
	DatabaseQueries atomic.Int64
	CacheHits       atomic.Int64
	CacheMisses     atomic.Int64
}

type statsJSON struct {
	Searches        int64 `json:"searches"`
	DatabaseQueries int64 `json:"databaseQueries"`
	CacheHits       int64 `json:"cacheHits"`
	CacheMisses     int64 `json:"cacheMisses"`
}

func (stats *Stats) answer() statsJSON {
	return statsJSON{stats.Searches.Load(), stats.DatabaseQueries.Load(), stats.CacheHits.Load(), stats.CacheMisses.Load()}
}

// CountingReservationStore passes every call on to the real store and counts it: each call is
// one question to the database.
type CountingReservationStore struct {
	store reservations.ReservationStore
	stats *Stats
}

func NewCountingReservationStore(store reservations.ReservationStore, stats *Stats) *CountingReservationStore {
	return &CountingReservationStore{store: store, stats: stats}
}

func (counting *CountingReservationStore) Count() int {
	counting.stats.DatabaseQueries.Add(1)
	return counting.store.Count()
}

func (counting *CountingReservationStore) HasOverlap(room rooms.Room, stay reservations.DateRange) bool {
	counting.stats.DatabaseQueries.Add(1)
	return counting.store.HasOverlap(room, stay)
}

func (counting *CountingReservationStore) Save(reservation *reservations.Reservation) {
	counting.stats.DatabaseQueries.Add(1)
	counting.store.Save(reservation)
}

func (counting *CountingReservationStore) Find(id string) *reservations.Reservation {
	counting.stats.DatabaseQueries.Add(1)
	return counting.store.Find(id)
}

func (counting *CountingReservationStore) CountConfirmed(room rooms.Room, stay reservations.DateRange) int {
	counting.stats.DatabaseQueries.Add(1)
	return counting.store.CountConfirmed(room, stay)
}
```

JavaScript:

```text
// The four counters of GET /stats. Node.js runs one request's code at a time, so ++ is exact.
export class Stats {
  searches = 0;
  databaseQueries = 0;
  cacheHits = 0;
  cacheMisses = 0;
}

// Passes every call on to the real store and counts it: each call is one question to the database.
export class CountingReservationStore {
  #store;
  #stats;

  constructor(store, stats) {
    this.#store = store;
    this.#stats = stats;
  }

  get count() {
    this.#stats.databaseQueries++;
    return this.#store.count;
  }

  hasOverlap(room, stay) {
    this.#stats.databaseQueries++;
    return this.#store.hasOverlap(room, stay);
  }

  save(reservation) {
    this.#stats.databaseQueries++;
    return this.#store.save(reservation);
  }

  find(id) {
    this.#stats.databaseQueries++;
    return this.#store.find(id);
  }

  countConfirmed(room, stay) {
    this.#stats.databaseQueries++;
    return this.#store.countConfirmed(room, stay);
  }
}
```

Dart:

```text
// The four counters of GET /stats. A plain ++ is exact: Dart runs every request on one isolate.
class Stats {
  int searches = 0;
  int databaseQueries = 0;
  int cacheHits = 0;
  int cacheMisses = 0;

  Map<String, int> toJson() => {
        'searches': searches,
        'databaseQueries': databaseQueries,
        'cacheHits': cacheHits,
        'cacheMisses': cacheMisses,
      };
}

// Passes every call on to the real store and counts it: each call is one question to the database.
class CountingReservationStore implements ReservationStore {
  CountingReservationStore(this._store, this._stats);

  final ReservationStore _store;
  final Stats _stats;

  @override
  FutureOr<int> get count {
    _stats.databaseQueries++;
    return _store.count;
  }

  @override
  FutureOr<bool> hasOverlap(Room room, DateRange stay) {
    _stats.databaseQueries++;
    return _store.hasOverlap(room, stay);
  }

  @override
  FutureOr<void> save(Reservation reservation) {
    _stats.databaseQueries++;
    return _store.save(reservation);
  }

  @override
  FutureOr<Reservation?> find(String id) {
    _stats.databaseQueries++;
    return _store.find(id);
  }

  @override
  FutureOr<int> countConfirmed(Room room, DateRange stay) {
    _stats.databaseQueries++;
    return _store.countConfirmed(room, stay);
  }
}
```

### Stats and CountingReservationStore

#### C#

```csharp
// HotelBooking/Api/Stats.cs
using HotelBooking.Reservations;
using HotelBooking.Rooms;

namespace HotelBooking.Api;

// The four counters of GET /stats. ASP.NET Core runs requests on several threads at once,
// so Interlocked makes every +1 exact.
public class Stats
{
    private long _searches, _databaseQueries, _cacheHits, _cacheMisses;

    public void AddSearch() => Interlocked.Increment(ref _searches);
    public void AddDatabaseQuery() => Interlocked.Increment(ref _databaseQueries);
    public void AddCacheHit() => Interlocked.Increment(ref _cacheHits);
    public void AddCacheMiss() => Interlocked.Increment(ref _cacheMisses);

    public object Answer() => new
    {
        searches = Interlocked.Read(ref _searches), databaseQueries = Interlocked.Read(ref _databaseQueries),
        cacheHits = Interlocked.Read(ref _cacheHits), cacheMisses = Interlocked.Read(ref _cacheMisses),
    };
}

// Passes every call on to the real store and counts it: each call is one question to the database.
public class CountingReservationStore(IReservationStore store, Stats stats) : IReservationStore
{
    public int Count
    {
        get
        {
            stats.AddDatabaseQuery();
            return store.Count;
        }
    }

    public bool HasOverlap(Room room, DateRange stay)
    {
        stats.AddDatabaseQuery();
        return store.HasOverlap(room, stay);
    }

    public void Save(Reservation reservation)
    {
        stats.AddDatabaseQuery();
        store.Save(reservation);
    }

    public Reservation? Find(string id)
    {
        stats.AddDatabaseQuery();
        return store.Find(id);
    }

    public int CountConfirmed(Room room, DateRange stay)
    {
        stats.AddDatabaseQuery();
        return store.CountConfirmed(room, stay);
    }
}
```

#### Python

```python
class Stats:
    """The four counters of GET /stats. FastAPI runs requests on several threads at once,
    so a lock makes every += 1 exact."""

    def __init__(self) -> None:
        self._lock = threading.Lock()
        self._counts = {"searches": 0, "databaseQueries": 0, "cacheHits": 0, "cacheMisses": 0}

    def add(self, name: str) -> None:
        with self._lock:
            self._counts[name] += 1

    def as_dict(self) -> dict[str, int]:
        with self._lock:
            return dict(self._counts)

class CountingReservationStore:
    """Passes every call on to the real store and counts it: each call is one question to the database."""

    def __init__(self, store: ReservationStore, stats: Stats) -> None:
        self._store = store
        self._stats = stats

    def count(self) -> int:
        self._stats.add("databaseQueries")
        return self._store.count()

    def has_overlap(self, room: Room, stay: DateRange) -> bool:
        self._stats.add("databaseQueries")
        return self._store.has_overlap(room, stay)

    def save(self, reservation: Reservation) -> None:
        self._stats.add("databaseQueries")
        self._store.save(reservation)

    def find(self, reservation_id: str) -> Reservation | None:
        self._stats.add("databaseQueries")
        return self._store.find(reservation_id)

    def count_confirmed(self, room: Room, stay: DateRange) -> int:
        self._stats.add("databaseQueries")
        return self._store.count_confirmed(room, stay)
```

#### Java

```java
// src/main/java/hotelbooking/api/Stats.java
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

// src/main/java/hotelbooking/api/CountingReservationStore.java
package hotelbooking.api;

import hotelbooking.reservations.DateRange;
import hotelbooking.reservations.Reservation;
import hotelbooking.reservations.ReservationStore;
import hotelbooking.rooms.Room;

// Passes every call on to the real store and counts it: each call is one question to the database.
public class CountingReservationStore implements ReservationStore {
    private final ReservationStore store;
    private final Stats stats;

    public CountingReservationStore(ReservationStore store, Stats stats) {
        this.store = store;
        this.stats = stats;
    }

    @Override
    public int count() {
        stats.databaseQueries.incrementAndGet();
        return store.count();
    }

    @Override
    public boolean hasOverlap(Room room, DateRange stay) {
        stats.databaseQueries.incrementAndGet();
        return store.hasOverlap(room, stay);
    }

    @Override
    public void save(Reservation reservation) {
        stats.databaseQueries.incrementAndGet();
        store.save(reservation);
    }

    @Override
    public Reservation find(String id) {
        stats.databaseQueries.incrementAndGet();
        return store.find(id);
    }

    @Override
    public int countConfirmed(Room room, DateRange stay) {
        stats.databaseQueries.incrementAndGet();
        return store.countConfirmed(room, stay);
    }
}
```

#### Go

```go
// api/stats.go
package api

import (
	"sync/atomic"

	"hotelbooking/reservations"
	"hotelbooking/rooms"
)

// Stats holds the four counters of GET /stats. Requests run at the same time on several
// goroutines, so each counter is atomic: Add(1) is exact even then.
type Stats struct {
	Searches        atomic.Int64
	DatabaseQueries atomic.Int64
	CacheHits       atomic.Int64
	CacheMisses     atomic.Int64
}

type statsJSON struct {
	Searches        int64 `json:"searches"`
	DatabaseQueries int64 `json:"databaseQueries"`
	CacheHits       int64 `json:"cacheHits"`
	CacheMisses     int64 `json:"cacheMisses"`
}

func (stats *Stats) answer() statsJSON {
	return statsJSON{stats.Searches.Load(), stats.DatabaseQueries.Load(), stats.CacheHits.Load(), stats.CacheMisses.Load()}
}

// CountingReservationStore passes every call on to the real store and counts it: each call is
// one question to the database.
type CountingReservationStore struct {
	store reservations.ReservationStore
	stats *Stats
}

func NewCountingReservationStore(store reservations.ReservationStore, stats *Stats) *CountingReservationStore {
	return &CountingReservationStore{store: store, stats: stats}
}

func (counting *CountingReservationStore) Count() int {
	counting.stats.DatabaseQueries.Add(1)
	return counting.store.Count()
}

func (counting *CountingReservationStore) HasOverlap(room rooms.Room, stay reservations.DateRange) bool {
	counting.stats.DatabaseQueries.Add(1)
	return counting.store.HasOverlap(room, stay)
}

func (counting *CountingReservationStore) Save(reservation *reservations.Reservation) {
	counting.stats.DatabaseQueries.Add(1)
	counting.store.Save(reservation)
}

func (counting *CountingReservationStore) Find(id string) *reservations.Reservation {
	counting.stats.DatabaseQueries.Add(1)
	return counting.store.Find(id)
}

func (counting *CountingReservationStore) CountConfirmed(room rooms.Room, stay reservations.DateRange) int {
	counting.stats.DatabaseQueries.Add(1)
	return counting.store.CountConfirmed(room, stay)
}
```

#### JavaScript

```javascript
// The four counters of GET /stats. Node.js runs one request's code at a time, so ++ is exact.
export class Stats {
  searches = 0;
  databaseQueries = 0;
  cacheHits = 0;
  cacheMisses = 0;
}

// Passes every call on to the real store and counts it: each call is one question to the database.
export class CountingReservationStore {
  #store;
  #stats;

  constructor(store, stats) {
    this.#store = store;
    this.#stats = stats;
  }

  get count() {
    this.#stats.databaseQueries++;
    return this.#store.count;
  }

  hasOverlap(room, stay) {
    this.#stats.databaseQueries++;
    return this.#store.hasOverlap(room, stay);
  }

  save(reservation) {
    this.#stats.databaseQueries++;
    return this.#store.save(reservation);
  }

  find(id) {
    this.#stats.databaseQueries++;
    return this.#store.find(id);
  }

  countConfirmed(room, stay) {
    this.#stats.databaseQueries++;
    return this.#store.countConfirmed(room, stay);
  }
}
```

#### Dart

```dart
// The four counters of GET /stats. A plain ++ is exact: Dart runs every request on one isolate.
class Stats {
  int searches = 0;
  int databaseQueries = 0;
  int cacheHits = 0;
  int cacheMisses = 0;

  Map<String, int> toJson() => {
        'searches': searches,
        'databaseQueries': databaseQueries,
        'cacheHits': cacheHits,
        'cacheMisses': cacheMisses,
      };
}

// Passes every call on to the real store and counts it: each call is one question to the database.
class CountingReservationStore implements ReservationStore {
  CountingReservationStore(this._store, this._stats);

  final ReservationStore _store;
  final Stats _stats;

  @override
  FutureOr<int> get count {
    _stats.databaseQueries++;
    return _store.count;
  }

  @override
  FutureOr<bool> hasOverlap(Room room, DateRange stay) {
    _stats.databaseQueries++;
    return _store.hasOverlap(room, stay);
  }

  @override
  FutureOr<void> save(Reservation reservation) {
    _stats.databaseQueries++;
    return _store.save(reservation);
  }

  @override
  FutureOr<Reservation?> find(String id) {
    _stats.databaseQueries++;
    return _store.find(id);
  }

  @override
  FutureOr<int> countConfirmed(Room room, DateRange stay) {
    _stats.databaseQueries++;
    return _store.countConfirmed(room, stay);
  }
}
```

### Completed example 1.3 · GET /stats, and the search counts itself

The search endpoint gets one new line, right after its checks.

### GET /stats and the search after Step 1

#### C#

```csharp
app.MapGet("/stats", () => Results.Json(stats.Answer()));

app.MapGet("/rooms/availability", (string? checkIn, string? checkOut, string? limit, string? after) =>
{
    if (ParseDate(checkIn) is not { } start || ParseDate(checkOut) is not { } end) return Problem(400, "InvalidDates", DateDetail);
    var stay = new DateRange(start, end);
    if (!stay.IsValid()) return Problem(400, "InvalidDates", "checkOut must be after checkIn.");
    limit ??= "20";
    if (!Regex.IsMatch(limit, @"^[0-9]{1,3}\z") || int.Parse(limit) is < 1 or > 100)
        return Problem(400, "InvalidRequest", "limit must be a whole number from 1 to 100.");

    stats.AddSearch();
    return unitOfWork(service =>
    {
        // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
        var rest = service.FindFreeRooms(stay).Where(room => string.CompareOrdinal(room.Number, after ?? "") > 0)
            .OrderBy(room => room.Number, StringComparer.Ordinal).ToList();
        var page = rest.Take(int.Parse(limit)).ToList();
        return Results.Json(new
        {
            checkIn, checkOut,
            rooms = page.Select(room => new { number = room.Number, type = room.Type }),
            next = rest.Count > page.Count ? page[^1].Number : null,
        });
    });
});
```

#### Python

```python
@app.get("/stats")
def read_stats() -> JSONResponse:
    return JSONResponse(stats.as_dict())

@app.get("/rooms/availability")
def find_free_rooms(checkIn: str = "", checkOut: str = "", limit: str = "20", after: str = "") -> JSONResponse:
    check_in, check_out = parse_date(checkIn), parse_date(checkOut)
    if check_in is None or check_out is None:
        return problem(400, "InvalidDates", DATE_DETAIL)
    stay = DateRange(check_in, check_out)
    if not stay.is_valid():
        return problem(400, "InvalidDates", "checkOut must be after checkIn.")
    if not re.fullmatch(r"[0-9]{1,3}", limit) or not 1 <= int(limit) <= 100:
        return problem(400, "InvalidRequest", "limit must be a whole number from 1 to 100.")

    stats.add("searches")
    with unit_of_work() as service:
        free = service.find_free_rooms(stay)
    # Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
    rest = sorted((room for room in free if room.number > after), key=lambda room: room.number)
    page = rest[:int(limit)]
    return JSONResponse({
        "checkIn": checkIn, "checkOut": checkOut,
        "rooms": [{"number": room.number, "type": room.type} for room in page],
        "next": page[-1].number if len(rest) > len(page) else None,
    })
```

#### Java

```java
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
```

#### Go

```go
mux.HandleFunc("GET /stats", func(w http.ResponseWriter, r *http.Request) {
	write(w, http.StatusOK, "application/json", stats.answer())
})

mux.HandleFunc("GET /rooms/availability", func(w http.ResponseWriter, r *http.Request) {
	query := r.URL.Query()
	checkIn, okIn := parseDate(query.Get("checkIn"))
	checkOut, okOut := parseDate(query.Get("checkOut"))
	if !okIn || !okOut {
		problem(w, http.StatusBadRequest, "InvalidDates", dateDetail)
		return
	}
	stay := reservations.DateRange{CheckIn: checkIn, CheckOut: checkOut}
	if !stay.IsValid() {
		problem(w, http.StatusBadRequest, "InvalidDates", "checkOut must be after checkIn.")
		return
	}
	limitText := query.Get("limit")
	if !query.Has("limit") {
		limitText = "20"
	}
	limit, _ := strconv.Atoi(limitText)
	if !regexp.MustCompile(`^[0-9]{1,3}$`).MatchString(limitText) || limit < 1 || limit > 100 {
		problem(w, http.StatusBadRequest, "InvalidRequest", "limit must be a whole number from 1 to 100.")
		return
	}

	stats.Searches.Add(1)
	var free []rooms.Room
	if err := unitOfWork(func(service *reservations.BookingService) { free = service.FindFreeRooms(stay) }); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	// Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
	after := query.Get("after")
	rest := []rooms.Room{}
	for _, room := range free {
		if room.Number > after {
			rest = append(rest, room)
		}
	}
	slices.SortFunc(rest, func(a, b rooms.Room) int { return strings.Compare(a.Number, b.Number) })
	answer := availabilityJSON{CheckIn: query.Get("checkIn"), CheckOut: query.Get("checkOut"), Rooms: []roomJSON{}}
	for _, room := range rest[:min(limit, len(rest))] {
		answer.Rooms = append(answer.Rooms, roomJSON{Number: room.Number, Type: room.Type})
	}
	if len(rest) > limit {
		answer.Next = &answer.Rooms[limit-1].Number
	}
	write(w, http.StatusOK, "application/json", answer)
})
```

#### JavaScript

```javascript
app.get('/stats', (request, response) => {
  const { searches, databaseQueries, cacheHits, cacheMisses } = stats;
  response.json({ searches, databaseQueries, cacheHits, cacheMisses });
});

app.get('/rooms/availability', async (request, response) => {
  const { checkIn, checkOut, limit = '20', after = '' } = request.query;
  const start = parseDate(checkIn);
  const end = parseDate(checkOut);
  if (!start || !end) return problem(response, 400, 'InvalidDates', dateDetail);
  const stay = new DateRange(start, end);
  if (!stay.isValid()) return problem(response, 400, 'InvalidDates', 'checkOut must be after checkIn.');
  if (typeof limit !== 'string' || !/^[0-9]{1,3}$/.test(limit) || Number(limit) < 1 || Number(limit) > 100) {
    return problem(response, 400, 'InvalidRequest', 'limit must be a whole number from 1 to 100.');
  }

  stats.searches++;
  const free = await unitOfWork(service => service.findFreeRooms(stay));
  // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
  const rest = free.filter(room => room.number > after).sort((a, b) => (a.number < b.number ? -1 : 1));
  const page = rest.slice(0, Number(limit));
  response.json({
    checkIn, checkOut,
    rooms: page.map(room => ({ number: room.number, type: room.type })),
    next: rest.length > page.length ? page.at(-1).number : null,
  });
});
```

#### Dart

```dart
router.get('/stats', (Request request) => json(200, stats.toJson()));

router.get('/rooms/availability', (Request request) async {
  final query = request.url.queryParameters;
  final checkIn = parseDate(query['checkIn']);
  final checkOut = parseDate(query['checkOut']);
  if (checkIn == null || checkOut == null) return problem(400, 'InvalidDates', dateDetail);
  final stay = DateRange(checkIn, checkOut);
  if (!stay.isValid()) return problem(400, 'InvalidDates', 'checkOut must be after checkIn.');
  final limitText = query['limit'] ?? '20';
  final limit = int.tryParse(limitText) ?? 0;
  if (!RegExp(r'^[0-9]{1,3}$').hasMatch(limitText) || limit < 1 || limit > 100) {
    return problem(400, 'InvalidRequest', 'limit must be a whole number from 1 to 100.');
  }

  stats.searches++;
  final free = await unitOfWork((service) => service.findFreeRooms(stay));
  // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
  final after = query['after'] ?? '';
  final rest = free.where((room) => room.number.compareTo(after) > 0).toList()
    ..sort((a, b) => a.number.compareTo(b.number));
  final page = rest.take(limit).toList();
  return json(200, {
    'checkIn': query['checkIn'],
    'checkOut': query['checkOut'],
    'rooms': [
      for (final room in page) {'number': room.number, 'type': room.type}
    ],
    'next': rest.length > page.length ? page.last.number : null,
  });
});
```

### Completed example 1.4 · the wiring counts the queries

**How the app counts real database questions** In short: A small store that passes every call on to the real store and adds 1 each time: BookingService does not notice and does not change. This is the decorator idea from P04: the counting store keeps the same promise as the store inside it. The wiring puts it around the PostgreSQL store in the unit of work; the API tests put it around the list store.

1. A search calls HasOverlap(R7) and HasOverlap(R8) on the counting store.
2. Each call adds 1 to databaseQueries and then asks the PostgreSQL store, which sends one SELECT.
3. A booking calls HasOverlap, Count and Save: 3 more. SaveChanges at the end of a cancel is not one of the store’s promised calls, so it is not counted.

C#:

```text
void RunApi()
{
    // The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
    var builder = WebApplication.CreateBuilder(args);
    var connection = $"Host=localhost;Port={Environment.GetEnvironmentVariable("HOTEL_DB_PORT") ?? "5432"};Database=hotel;Username=hotel;Password=hotel";
    builder.Services.AddDbContextFactory<RoomsDb>(options => options.UseNpgsql(connection));
    builder.Services.AddDbContextFactory<ReservationsDb>(options => options.UseNpgsql(connection));
    var app = builder.Build();

    var rooms = new PostgresRoomCatalog(app.Services.GetRequiredService<IDbContextFactory<RoomsDb>>()).All();
    var databases = app.Services.GetRequiredService<IDbContextFactory<ReservationsDb>>();
    var email = new EmailNotificationSender();
    var stats = new Stats();

    // One request: a fresh store over its own DbContext, counted for GET /stats; save what changed at the end.
    IResult UnitOfWork(Func<BookingService, IResult> work)
    {
        using var db = databases.CreateDbContext();
        var store = new PostgresReservationStore(db, rooms);
        var result = work(new BookingService(rooms, new CountingReservationStore(store, stats), email));
        store.SaveChanges();
        return result;
    }

    HotelApi.Map(app, rooms, UnitOfWork, stats);
    app.Run("http://localhost:5080");
}
```

Python:

```text
def run_api() -> None:
    # The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
    engine = create_engine(f"postgresql+psycopg://hotel:hotel@localhost:{os.getenv('HOTEL_DB_PORT', '5432')}/hotel")
    rooms = PostgresRoomCatalog(engine).all()
    email = EmailNotificationSender()
    stats = Stats()

    @contextmanager
    def unit_of_work():
        # One request: a fresh store over its own session, counted for GET /stats; save what changed at the end.
        with Session(engine) as session:
            store = PostgresReservationStore(session, rooms)
            yield BookingService(rooms, CountingReservationStore(store, stats), email)
            store.save_changes()

    uvicorn.run(create_app(rooms, unit_of_work, stats), host="localhost", port=5080)
```

Java:

```text
// The wiring for the web app.
@Bean
HotelApi hotelApi(EntityManager db, TransactionTemplate transactions) {
    List<Room> rooms = new PostgresRoomCatalog(db).all();
    EmailNotificationSender email = new EmailNotificationSender();
    Stats stats = new Stats();
    // One request: a fresh store in its own transaction, counted for GET /stats; save what changed at the end.
    HotelApi.UnitOfWork unitOfWork = work -> transactions.execute(transaction -> {
        PostgresReservationStore store = new PostgresReservationStore(db, rooms);
        var result = work.apply(new BookingService(rooms, new CountingReservationStore(store, stats), email));
        store.saveChanges();
        return result;
    });
    return new HotelApi(rooms, unitOfWork, stats);
}
```

Go:

```text
func runAPI() {
	// The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
	ctx := context.Background()
	pool, err := pgxpool.New(ctx, "postgres://hotel:hotel@localhost:"+cmp.Or(os.Getenv("HOTEL_DB_PORT"), "5432")+"/hotel")
	if err != nil {
		log.Fatal(err)
	}
	defer pool.Close()
	hotelRooms := rooms.NewPostgresRoomCatalog(pool).All()
	email := &notifications.EmailNotificationSender{}
	stats := &api.Stats{}

	// One request: a fresh store, counted for GET /stats; save what changed at the end.
	unitOfWork := func(work func(service *reservations.BookingService)) error {
		store := reservations.NewPostgresReservationStore(ctx, pool, hotelRooms)
		work(reservations.NewBookingService(hotelRooms, api.NewCountingReservationStore(store, stats), email, 0))
		return store.SaveChanges()
	}

	fmt.Println("Listening on http://localhost:5080")
	log.Fatal(http.ListenAndServe("localhost:5080", api.New(hotelRooms, unitOfWork, stats)))
}
```

JavaScript:

```text
async function runApi() {
  // The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
  const pool = new pg.Pool({ host: 'localhost', port: Number(process.env.HOTEL_DB_PORT ?? 5432), database: 'hotel', user: 'hotel', password: 'hotel' });
  const rooms = await new PostgresRoomCatalog(pool).all();
  const email = new EmailNotificationSender();
  const stats = new Stats();

  // One request: a fresh store, counted for GET /stats; save what changed at the end.
  const unitOfWork = async work => {
    const store = new PostgresReservationStore(pool, rooms);
    const result = await work(new BookingService(rooms, new CountingReservationStore(store, stats), email));
    await store.saveChanges();
    return result;
  };

  createApi(rooms, unitOfWork, stats).listen(5080, 'localhost', () => console.log('Listening on http://localhost:5080'));
}
```

Dart:

```text
Future<void> runApi() async {
  // The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
  final port = int.parse(Platform.environment['HOTEL_DB_PORT'] ?? '5432');
  final pool = Pool.withEndpoints([
    Endpoint(host: 'localhost', port: port, database: 'hotel', username: 'hotel', password: 'hotel')
  ], settings: PoolSettings(sslMode: SslMode.disable));
  final rooms = await PostgresRoomCatalog(pool).all();
  final email = EmailNotificationSender();
  final stats = Stats();

  // One request: a fresh store, counted for GET /stats; save what changed at the end.
  Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async {
    final store = PostgresReservationStore(pool, rooms);
    final result = await work(BookingService(rooms, CountingReservationStore(store, stats), email));
    await store.saveChanges();
    return result;
  }

  await serve(createApi(rooms, unitOfWork, stats), 'localhost', 5080);
  print('Listening on http://localhost:5080');
}
```

### The web app in the wiring, after Step 1

#### C#

```csharp
void RunApi()
{
    // The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
    var builder = WebApplication.CreateBuilder(args);
    var connection = $"Host=localhost;Port={Environment.GetEnvironmentVariable("HOTEL_DB_PORT") ?? "5432"};Database=hotel;Username=hotel;Password=hotel";
    builder.Services.AddDbContextFactory<RoomsDb>(options => options.UseNpgsql(connection));
    builder.Services.AddDbContextFactory<ReservationsDb>(options => options.UseNpgsql(connection));
    var app = builder.Build();

    var rooms = new PostgresRoomCatalog(app.Services.GetRequiredService<IDbContextFactory<RoomsDb>>()).All();
    var databases = app.Services.GetRequiredService<IDbContextFactory<ReservationsDb>>();
    var email = new EmailNotificationSender();
    var stats = new Stats();

    // One request: a fresh store over its own DbContext, counted for GET /stats; save what changed at the end.
    IResult UnitOfWork(Func<BookingService, IResult> work)
    {
        using var db = databases.CreateDbContext();
        var store = new PostgresReservationStore(db, rooms);
        var result = work(new BookingService(rooms, new CountingReservationStore(store, stats), email));
        store.SaveChanges();
        return result;
    }

    HotelApi.Map(app, rooms, UnitOfWork, stats);
    app.Run("http://localhost:5080");
}
```

#### Python

```python
def run_api() -> None:
    # The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
    engine = create_engine(f"postgresql+psycopg://hotel:hotel@localhost:{os.getenv('HOTEL_DB_PORT', '5432')}/hotel")
    rooms = PostgresRoomCatalog(engine).all()
    email = EmailNotificationSender()
    stats = Stats()

    @contextmanager
    def unit_of_work():
        # One request: a fresh store over its own session, counted for GET /stats; save what changed at the end.
        with Session(engine) as session:
            store = PostgresReservationStore(session, rooms)
            yield BookingService(rooms, CountingReservationStore(store, stats), email)
            store.save_changes()

    uvicorn.run(create_app(rooms, unit_of_work, stats), host="localhost", port=5080)
```

#### Java

```java
// The wiring for the web app.
@Bean
HotelApi hotelApi(EntityManager db, TransactionTemplate transactions) {
    List<Room> rooms = new PostgresRoomCatalog(db).all();
    EmailNotificationSender email = new EmailNotificationSender();
    Stats stats = new Stats();
    // One request: a fresh store in its own transaction, counted for GET /stats; save what changed at the end.
    HotelApi.UnitOfWork unitOfWork = work -> transactions.execute(transaction -> {
        PostgresReservationStore store = new PostgresReservationStore(db, rooms);
        var result = work.apply(new BookingService(rooms, new CountingReservationStore(store, stats), email));
        store.saveChanges();
        return result;
    });
    return new HotelApi(rooms, unitOfWork, stats);
}
```

#### Go

```go
func runAPI() {
	// The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
	ctx := context.Background()
	pool, err := pgxpool.New(ctx, "postgres://hotel:hotel@localhost:"+cmp.Or(os.Getenv("HOTEL_DB_PORT"), "5432")+"/hotel")
	if err != nil {
		log.Fatal(err)
	}
	defer pool.Close()
	hotelRooms := rooms.NewPostgresRoomCatalog(pool).All()
	email := &notifications.EmailNotificationSender{}
	stats := &api.Stats{}

	// One request: a fresh store, counted for GET /stats; save what changed at the end.
	unitOfWork := func(work func(service *reservations.BookingService)) error {
		store := reservations.NewPostgresReservationStore(ctx, pool, hotelRooms)
		work(reservations.NewBookingService(hotelRooms, api.NewCountingReservationStore(store, stats), email, 0))
		return store.SaveChanges()
	}

	fmt.Println("Listening on http://localhost:5080")
	log.Fatal(http.ListenAndServe("localhost:5080", api.New(hotelRooms, unitOfWork, stats)))
}
```

#### JavaScript

```javascript
async function runApi() {
  // The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
  const pool = new pg.Pool({ host: 'localhost', port: Number(process.env.HOTEL_DB_PORT ?? 5432), database: 'hotel', user: 'hotel', password: 'hotel' });
  const rooms = await new PostgresRoomCatalog(pool).all();
  const email = new EmailNotificationSender();
  const stats = new Stats();

  // One request: a fresh store, counted for GET /stats; save what changed at the end.
  const unitOfWork = async work => {
    const store = new PostgresReservationStore(pool, rooms);
    const result = await work(new BookingService(rooms, new CountingReservationStore(store, stats), email));
    await store.saveChanges();
    return result;
  };

  createApi(rooms, unitOfWork, stats).listen(5080, 'localhost', () => console.log('Listening on http://localhost:5080'));
}
```

#### Dart

```dart
Future<void> runApi() async {
  // The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
  final port = int.parse(Platform.environment['HOTEL_DB_PORT'] ?? '5432');
  final pool = Pool.withEndpoints([
    Endpoint(host: 'localhost', port: port, database: 'hotel', username: 'hotel', password: 'hotel')
  ], settings: PoolSettings(sslMode: SslMode.disable));
  final rooms = await PostgresRoomCatalog(pool).all();
  final email = EmailNotificationSender();
  final stats = Stats();

  // One request: a fresh store, counted for GET /stats; save what changed at the end.
  Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async {
    final store = PostgresReservationStore(pool, rooms);
    final result = await work(BookingService(rooms, CountingReservationStore(store, stats), email));
    await store.saveChanges();
    return result;
  }

  await serve(createApi(rooms, unitOfWork, stats), 'localhost', 5080);
  print('Listening on http://localhost:5080');
}
```

### Completed example 1.5 · the API tests count too

Only the helper that builds the app changes; the four tests stay as they are.

### The test helper, after Step 1

#### C#

```csharp
// The web app with the in-memory store, on ASP.NET Core's test server: the client sends requests without a network.
private static async Task<HttpClient> NewClient()
{
    var stats = new Stats();
    var service = new BookingService(Rooms, new CountingReservationStore(new InMemoryReservationStore(), stats), new EmailNotificationSender());
    var builder = WebApplication.CreateBuilder();
    builder.WebHost.UseTestServer();
    var app = builder.Build();
    HotelApi.Map(app, Rooms, work => work(service), stats); // a list in memory has nothing to save at the end of a request
    await app.StartAsync();
    return app.GetTestClient();
}
```

#### Python

```python
def new_client() -> TestClient:
    """The web app with the in-memory store; FastAPI's TestClient sends the requests without a network."""
    stats = Stats()
    service = BookingService([R7, R8], CountingReservationStore(InMemoryReservationStore(), stats), EmailNotificationSender())

    @contextmanager
    def unit_of_work():
        yield service  # a list in memory has nothing to save at the end of a request

    return TestClient(create_app([R7, R8], unit_of_work, stats))
```

#### Java

```java
// The web app with the in-memory store; MockMvc sends requests to it without a network.
static MockMvc newApp() {
    Stats stats = new Stats();
    BookingService service = new BookingService(ROOMS, new CountingReservationStore(new InMemoryReservationStore(), stats), new EmailNotificationSender());
    // A list in memory has nothing to save at the end of a request.
    return MockMvcBuilders.standaloneSetup(new HotelApi(ROOMS, work -> work.apply(service), stats)).build();
}
```

#### Go

```go
// newApp is the web app with the in-memory store; httptest sends requests to it without a network.
func newApp() http.Handler {
	hotelRooms := []rooms.Room{{Number: "R7", Type: "Standard"}, {Number: "R8", Type: "Deluxe"}}
	stats := &Stats{}
	store := NewCountingReservationStore(&reservations.InMemoryReservationStore{}, stats)
	service := reservations.NewBookingService(hotelRooms, store, &notifications.EmailNotificationSender{}, 0)
	return New(hotelRooms, func(work func(*reservations.BookingService)) error {
		work(service)
		return nil // a list in memory has nothing to save at the end of a request
	}, stats)
}
```

#### JavaScript

```javascript
// The web app with the in-memory store, on a free port of this laptop. send() makes one request.
async function startApp(t) {
  const stats = new Stats();
  const service = new BookingService(rooms, new CountingReservationStore(new InMemoryReservationStore(), stats), new EmailNotificationSender());
  // A list in memory has nothing to save at the end of a request.
  const server = createApi(rooms, work => work(service), stats).listen(0, 'localhost');
  await once(server, 'listening');
  t.after(() => server.close());
  return (method, path, body) => fetch(`http://localhost:${server.address().port}${path}`, {
    method, body: body && JSON.stringify(body), headers: { 'Content-Type': 'application/json', Connection: 'close' },
  });
}
```

#### Dart

```dart
// The web app with the in-memory store. A shelf app is a function from Request to Response, so the
// tests call it directly, without a network.
Handler newApp() {
  final stats = Stats();
  final store = CountingReservationStore(InMemoryReservationStore(), stats);
  final service = BookingService(rooms, store, EmailNotificationSender());
  // A list in memory has nothing to save at the end of a request.
  Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async => work(service);
  return createApi(rooms, unitOfWork, stats);
}
```

### Completed example 1.6 · the whole Api file after Step 1

Compare yours with it line by line.

### The Api file after Step 1

#### C#

```csharp
// HotelBooking/Api/HotelApi.cs
using System.Globalization;
using System.Text.Json;
using System.Text.RegularExpressions;
using HotelBooking.Reservations;
using HotelBooking.Rooms;

namespace HotelBooking.Api;

// Runs one request's work with a BookingService, then saves whatever changed.
public delegate IResult UnitOfWork(Func<BookingService, IResult> work);

// The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back, and GET /stats
// with what the searches cost. It lives outside the four modules and uses only their public contracts;
// Program.cs decides which store the endpoints use, through the unit of work.
public static class HotelApi
{
    private static readonly string[] Fields = ["guestName", "guestEmail", "room", "checkIn", "checkOut"];
    private const string BodyDetail = "The body must be JSON with guestName, guestEmail, room, checkIn and checkOut.";
    private const string DateDetail = "checkIn and checkOut must be dates such as 2026-11-12.";

    // Problem details (RFC 9457): every error has the same three keys.
    private static IResult Problem(int status, string title, string detail) =>
        Results.Json(new { title, status, detail }, contentType: "application/problem+json", statusCode: status);

    // A date written exactly like 2026-11-12, otherwise null.
    private static DateOnly? ParseDate(string? text) =>
        DateOnly.TryParseExact(text, "yyyy-MM-dd", CultureInfo.InvariantCulture, DateTimeStyles.None, out var day) ? day : null;

    // Turns a booking answer into an HTTP status code and a JSON body (L04).
    private static IResult ToHttp(BookingResult result) => result.Outcome switch
    {
        "Accepted" => Results.Json(new { id = result.ReservationId, status = "Confirmed", notificationFailed = result.NotificationFailed }, statusCode: 201),
        "Unavailable" => Problem(409, "Unavailable", "The room is booked for at least one night of this stay."),
        _ => Problem(400, "InvalidDates", "checkOut must be after checkIn."),
    };

    public static void Map(WebApplication app, IReadOnlyList<Room> rooms, UnitOfWork unitOfWork, Stats stats)
    {
        app.MapGet("/openapi.yaml", () => Results.Text(File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "openapi.yaml")), "application/yaml"));

        app.MapGet("/stats", () => Results.Json(stats.Answer()));

        app.MapGet("/rooms/availability", (string? checkIn, string? checkOut, string? limit, string? after) =>
        {
            if (ParseDate(checkIn) is not { } start || ParseDate(checkOut) is not { } end) return Problem(400, "InvalidDates", DateDetail);
            var stay = new DateRange(start, end);
            if (!stay.IsValid()) return Problem(400, "InvalidDates", "checkOut must be after checkIn.");
            limit ??= "20";
            if (!Regex.IsMatch(limit, @"^[0-9]{1,3}\z") || int.Parse(limit) is < 1 or > 100)
                return Problem(400, "InvalidRequest", "limit must be a whole number from 1 to 100.");

            stats.AddSearch();
            return unitOfWork(service =>
            {
                // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
                var rest = service.FindFreeRooms(stay).Where(room => string.CompareOrdinal(room.Number, after ?? "") > 0)
                    .OrderBy(room => room.Number, StringComparer.Ordinal).ToList();
                var page = rest.Take(int.Parse(limit)).ToList();
                return Results.Json(new
                {
                    checkIn, checkOut,
                    rooms = page.Select(room => new { number = room.Number, type = room.Type }),
                    next = rest.Count > page.Count ? page[^1].Number : null,
                });
            });
        });

        app.MapPost("/reservations", async (HttpRequest request) =>
        {
            if (!request.HasJsonContentType()) return Problem(400, "InvalidRequest", BodyDetail);
            JsonElement body;
            try
            {
                body = (await JsonDocument.ParseAsync(request.Body)).RootElement;
            }
            catch (JsonException)
            {
                return Problem(400, "InvalidRequest", BodyDetail);
            }
            string? Text(string field) => body.ValueKind == JsonValueKind.Object && body.TryGetProperty(field, out var value)
                && value.ValueKind == JsonValueKind.String && value.GetString() is { Length: > 0 } text ? text : null;
            if (Fields.Any(field => Text(field) is null)) return Problem(400, "InvalidRequest", BodyDetail);
            if (ParseDate(Text("checkIn")) is not { } start || ParseDate(Text("checkOut")) is not { } end) return Problem(400, "InvalidDates", DateDetail);
            if (rooms.FirstOrDefault(room => room.Number == Text("room")) is not { } room) return Problem(400, "UnknownRoom", $"There is no room {Text("room")}.");

            var guest = new Guest(Text("guestName")!, Text("guestEmail")!);
            return unitOfWork(service => ToHttp(service.Reserve(guest, room, new DateRange(start, end))));
        });

        app.MapDelete("/reservations/{id}", (string id) => unitOfWork(service => service.Cancel(id)
            ? Results.NoContent()
            : Problem(404, "NotFound", $"There is no confirmed reservation {id}.")));
    }
}
```

#### Python

```python
# hotel_booking/api.py
"""The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back,
and GET /stats with what the searches cost.

It lives outside the four modules and uses only their public contracts. main.py decides
which store the endpoints use, through unit_of_work.
"""
from __future__ import annotations

import re
import threading
from contextlib import AbstractContextManager
from datetime import date
from pathlib import Path
from typing import Any, Callable

from fastapi import Body, FastAPI
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse, Response

from hotel_booking.reservations import BookingResult, BookingService, DateRange, Guest, Reservation, ReservationStore
from hotel_booking.rooms import Room

# One request's work: "with unit_of_work() as service:" gives the endpoint a BookingService,
# and whatever changed is saved when the with-block ends.
UnitOfWork = Callable[[], AbstractContextManager[BookingService]]

FIELDS = ("guestName", "guestEmail", "room", "checkIn", "checkOut")
BODY_DETAIL = "The body must be JSON with guestName, guestEmail, room, checkIn and checkOut."
DATE_DETAIL = "checkIn and checkOut must be dates such as 2026-11-12."


class Stats:
    """The four counters of GET /stats. FastAPI runs requests on several threads at once,
    so a lock makes every += 1 exact."""

    def __init__(self) -> None:
        self._lock = threading.Lock()
        self._counts = {"searches": 0, "databaseQueries": 0, "cacheHits": 0, "cacheMisses": 0}

    def add(self, name: str) -> None:
        with self._lock:
            self._counts[name] += 1

    def as_dict(self) -> dict[str, int]:
        with self._lock:
            return dict(self._counts)


class CountingReservationStore:
    """Passes every call on to the real store and counts it: each call is one question to the database."""

    def __init__(self, store: ReservationStore, stats: Stats) -> None:
        self._store = store
        self._stats = stats

    def count(self) -> int:
        self._stats.add("databaseQueries")
        return self._store.count()

    def has_overlap(self, room: Room, stay: DateRange) -> bool:
        self._stats.add("databaseQueries")
        return self._store.has_overlap(room, stay)

    def save(self, reservation: Reservation) -> None:
        self._stats.add("databaseQueries")
        self._store.save(reservation)

    def find(self, reservation_id: str) -> Reservation | None:
        self._stats.add("databaseQueries")
        return self._store.find(reservation_id)

    def count_confirmed(self, room: Room, stay: DateRange) -> int:
        self._stats.add("databaseQueries")
        return self._store.count_confirmed(room, stay)


def problem(status: int, title: str, detail: str) -> JSONResponse:
    """Problem details (RFC 9457): every error has the same three keys."""
    return JSONResponse({"title": title, "status": status, "detail": detail}, status,
                        media_type="application/problem+json")


def parse_date(text: Any) -> date | None:
    """A date written exactly like 2026-11-12, otherwise None."""
    if not isinstance(text, str) or not re.fullmatch(r"[0-9]{4}-[0-9]{2}-[0-9]{2}", text):
        return None
    try:
        return date.fromisoformat(text)
    except ValueError:
        return None


def to_http(result: BookingResult) -> JSONResponse:
    """Turns a booking answer into an HTTP status code and a JSON body (L04)."""
    if result.outcome == "Accepted":
        return JSONResponse({"id": result.reservation_id, "status": "Confirmed",
                             "notificationFailed": result.notification_failed}, 201)
    if result.outcome == "Unavailable":
        return problem(409, "Unavailable", "The room is booked for at least one night of this stay.")
    return problem(400, "InvalidDates", "checkOut must be after checkIn.")


def create_app(rooms: list[Room], unit_of_work: UnitOfWork, stats: Stats) -> FastAPI:
    # docs/openapi.yaml is the contract, so FastAPI must not publish one of its own.
    app = FastAPI(openapi_url=None, docs_url=None, redoc_url=None)

    @app.exception_handler(RequestValidationError)
    def not_json(request, error) -> JSONResponse:
        return problem(400, "InvalidRequest", BODY_DETAIL)

    @app.get("/openapi.yaml")
    def contract() -> Response:
        return Response(Path("docs/openapi.yaml").read_text(encoding="utf-8"), media_type="application/yaml")

    @app.get("/stats")
    def read_stats() -> JSONResponse:
        return JSONResponse(stats.as_dict())

    @app.get("/rooms/availability")
    def find_free_rooms(checkIn: str = "", checkOut: str = "", limit: str = "20", after: str = "") -> JSONResponse:
        check_in, check_out = parse_date(checkIn), parse_date(checkOut)
        if check_in is None or check_out is None:
            return problem(400, "InvalidDates", DATE_DETAIL)
        stay = DateRange(check_in, check_out)
        if not stay.is_valid():
            return problem(400, "InvalidDates", "checkOut must be after checkIn.")
        if not re.fullmatch(r"[0-9]{1,3}", limit) or not 1 <= int(limit) <= 100:
            return problem(400, "InvalidRequest", "limit must be a whole number from 1 to 100.")

        stats.add("searches")
        with unit_of_work() as service:
            free = service.find_free_rooms(stay)
        # Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
        rest = sorted((room for room in free if room.number > after), key=lambda room: room.number)
        page = rest[:int(limit)]
        return JSONResponse({
            "checkIn": checkIn, "checkOut": checkOut,
            "rooms": [{"number": room.number, "type": room.type} for room in page],
            "next": page[-1].number if len(rest) > len(page) else None,
        })

    @app.post("/reservations")
    def reserve(body: Any = Body(None)) -> JSONResponse:
        if not isinstance(body, dict) or not all(isinstance(body.get(field), str) and body[field] for field in FIELDS):
            return problem(400, "InvalidRequest", BODY_DETAIL)
        check_in, check_out = parse_date(body["checkIn"]), parse_date(body["checkOut"])
        if check_in is None or check_out is None:
            return problem(400, "InvalidDates", DATE_DETAIL)
        room = next((room for room in rooms if room.number == body["room"]), None)
        if room is None:
            return problem(400, "UnknownRoom", f"There is no room {body['room']}.")

        with unit_of_work() as service:
            result = service.reserve(Guest(body["guestName"], body["guestEmail"]), room, DateRange(check_in, check_out))
        return to_http(result)

    @app.delete("/reservations/{reservation_id}")
    def cancel(reservation_id: str) -> Response:
        with unit_of_work() as service:
            cancelled = service.cancel(reservation_id)
        if not cancelled:
            return problem(404, "NotFound", f"There is no confirmed reservation {reservation_id}.")
        return Response(status_code=204)

    return app
```

#### Java

```java
// src/main/java/hotelbooking/api/HotelApi.java
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
```

#### Go

```go
// api/api.go
// Package api is the HTTP API: three endpoints that turn HTTP requests into BookingService calls
// and back, and GET /stats with what the searches cost. It lives outside the four modules and uses
// only their public contracts; main.go decides which store the endpoints use, through the unit of work.
package api

import (
	"encoding/json"
	"fmt"
	"net/http"
	"os"
	"regexp"
	"slices"
	"strconv"
	"strings"
	"time"

	"hotelbooking/reservations"
	"hotelbooking/rooms"
)

// UnitOfWork runs one request's work with a BookingService and then saves whatever changed.
type UnitOfWork func(work func(service *reservations.BookingService)) error

const bodyDetail = "The body must be JSON with guestName, guestEmail, room, checkIn and checkOut."
const dateDetail = "checkIn and checkOut must be dates such as 2026-11-12."

var fields = []string{"guestName", "guestEmail", "room", "checkIn", "checkOut"}

type roomJSON struct {
	Number string `json:"number"`
	Type   string `json:"type"`
}

type availabilityJSON struct {
	CheckIn  string     `json:"checkIn"`
	CheckOut string     `json:"checkOut"`
	Rooms    []roomJSON `json:"rooms"`
	Next     *string    `json:"next"`
}

type reservationJSON struct {
	ID                 string `json:"id"`
	Status             string `json:"status"`
	NotificationFailed bool   `json:"notificationFailed"`
}

// Problem details (RFC 9457): every error has the same three keys.
type problemJSON struct {
	Title  string `json:"title"`
	Status int    `json:"status"`
	Detail string `json:"detail"`
}

func write(w http.ResponseWriter, status int, contentType string, value any) {
	body, _ := json.Marshal(value)
	w.Header().Set("Content-Type", contentType)
	w.WriteHeader(status)
	w.Write(body)
}

func problem(w http.ResponseWriter, status int, title, detail string) {
	write(w, status, "application/problem+json", problemJSON{Title: title, Status: status, Detail: detail})
}

// A date written exactly like 2026-11-12.
func parseDate(text string) (time.Time, bool) {
	day, err := time.Parse("2006-01-02", text)
	return day, err == nil
}

// toHttp turns a booking answer into an HTTP status code and a JSON body (L04).
func toHttp(w http.ResponseWriter, result reservations.BookingResult) {
	switch result.Outcome {
	case "Accepted":
		write(w, http.StatusCreated, "application/json", reservationJSON{ID: result.ReservationID, Status: "Confirmed", NotificationFailed: result.NotificationFailed})
	case "Unavailable":
		problem(w, http.StatusConflict, "Unavailable", "The room is booked for at least one night of this stay.")
	default:
		problem(w, http.StatusBadRequest, "InvalidDates", "checkOut must be after checkIn.")
	}
}

// New returns the web app: the three endpoints, GET /stats and the contract.
func New(hotelRooms []rooms.Room, unitOfWork UnitOfWork, stats *Stats) http.Handler {
	mux := http.NewServeMux()

	mux.HandleFunc("GET /openapi.yaml", func(w http.ResponseWriter, r *http.Request) {
		contract, err := os.ReadFile("docs/openapi.yaml")
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		w.Header().Set("Content-Type", "application/yaml")
		w.Write(contract)
	})

	mux.HandleFunc("GET /stats", func(w http.ResponseWriter, r *http.Request) {
		write(w, http.StatusOK, "application/json", stats.answer())
	})

	mux.HandleFunc("GET /rooms/availability", func(w http.ResponseWriter, r *http.Request) {
		query := r.URL.Query()
		checkIn, okIn := parseDate(query.Get("checkIn"))
		checkOut, okOut := parseDate(query.Get("checkOut"))
		if !okIn || !okOut {
			problem(w, http.StatusBadRequest, "InvalidDates", dateDetail)
			return
		}
		stay := reservations.DateRange{CheckIn: checkIn, CheckOut: checkOut}
		if !stay.IsValid() {
			problem(w, http.StatusBadRequest, "InvalidDates", "checkOut must be after checkIn.")
			return
		}
		limitText := query.Get("limit")
		if !query.Has("limit") {
			limitText = "20"
		}
		limit, _ := strconv.Atoi(limitText)
		if !regexp.MustCompile(`^[0-9]{1,3}$`).MatchString(limitText) || limit < 1 || limit > 100 {
			problem(w, http.StatusBadRequest, "InvalidRequest", "limit must be a whole number from 1 to 100.")
			return
		}

		stats.Searches.Add(1)
		var free []rooms.Room
		if err := unitOfWork(func(service *reservations.BookingService) { free = service.FindFreeRooms(stay) }); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		// Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
		after := query.Get("after")
		rest := []rooms.Room{}
		for _, room := range free {
			if room.Number > after {
				rest = append(rest, room)
			}
		}
		slices.SortFunc(rest, func(a, b rooms.Room) int { return strings.Compare(a.Number, b.Number) })
		answer := availabilityJSON{CheckIn: query.Get("checkIn"), CheckOut: query.Get("checkOut"), Rooms: []roomJSON{}}
		for _, room := range rest[:min(limit, len(rest))] {
			answer.Rooms = append(answer.Rooms, roomJSON{Number: room.Number, Type: room.Type})
		}
		if len(rest) > limit {
			answer.Next = &answer.Rooms[limit-1].Number
		}
		write(w, http.StatusOK, "application/json", answer)
	})

	mux.HandleFunc("POST /reservations", func(w http.ResponseWriter, r *http.Request) {
		var body map[string]any
		if strings.HasPrefix(r.Header.Get("Content-Type"), "application/json") {
			json.NewDecoder(r.Body).Decode(&body)
		}
		text := map[string]string{}
		for _, field := range fields {
			value, ok := body[field].(string)
			if !ok || value == "" {
				problem(w, http.StatusBadRequest, "InvalidRequest", bodyDetail)
				return
			}
			text[field] = value
		}
		checkIn, okIn := parseDate(text["checkIn"])
		checkOut, okOut := parseDate(text["checkOut"])
		if !okIn || !okOut {
			problem(w, http.StatusBadRequest, "InvalidDates", dateDetail)
			return
		}
		index := slices.IndexFunc(hotelRooms, func(room rooms.Room) bool { return room.Number == text["room"] })
		if index < 0 {
			problem(w, http.StatusBadRequest, "UnknownRoom", fmt.Sprintf("There is no room %s.", text["room"]))
			return
		}

		var result reservations.BookingResult
		guest := reservations.Guest{Name: text["guestName"], Email: text["guestEmail"]}
		stay := reservations.DateRange{CheckIn: checkIn, CheckOut: checkOut}
		if err := unitOfWork(func(service *reservations.BookingService) { result = service.Reserve(guest, hotelRooms[index], stay) }); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		toHttp(w, result)
	})

	mux.HandleFunc("DELETE /reservations/{id}", func(w http.ResponseWriter, r *http.Request) {
		id := r.PathValue("id")
		var cancelled bool
		if err := unitOfWork(func(service *reservations.BookingService) { cancelled = service.Cancel(id) }); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		if !cancelled {
			problem(w, http.StatusNotFound, "NotFound", fmt.Sprintf("There is no confirmed reservation %s.", id))
			return
		}
		w.WriteHeader(http.StatusNoContent)
	})

	return mux
}
```

#### JavaScript

```javascript
// src/api.js
// The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back,
// and GET /stats with what the searches cost.
// It lives outside the four modules and uses only their public contracts. main.js decides which
// store the endpoints use, through unitOfWork:
//
//   unitOfWork(work)   runs one request's work with a BookingService, then saves what changed
import fs from 'node:fs';
import express from 'express';
import { DateRange, Guest } from './reservations/index.js';

const fields = ['guestName', 'guestEmail', 'room', 'checkIn', 'checkOut'];
const bodyDetail = 'The body must be JSON with guestName, guestEmail, room, checkIn and checkOut.';
const dateDetail = 'checkIn and checkOut must be dates such as 2026-11-12.';

// The four counters of GET /stats. Node.js runs one request's code at a time, so ++ is exact.
export class Stats {
  searches = 0;
  databaseQueries = 0;
  cacheHits = 0;
  cacheMisses = 0;
}

// Passes every call on to the real store and counts it: each call is one question to the database.
export class CountingReservationStore {
  #store;
  #stats;

  constructor(store, stats) {
    this.#store = store;
    this.#stats = stats;
  }

  get count() {
    this.#stats.databaseQueries++;
    return this.#store.count;
  }

  hasOverlap(room, stay) {
    this.#stats.databaseQueries++;
    return this.#store.hasOverlap(room, stay);
  }

  save(reservation) {
    this.#stats.databaseQueries++;
    return this.#store.save(reservation);
  }

  find(id) {
    this.#stats.databaseQueries++;
    return this.#store.find(id);
  }

  countConfirmed(room, stay) {
    this.#stats.databaseQueries++;
    return this.#store.countConfirmed(room, stay);
  }
}

// Problem details (RFC 9457): every error has the same three keys.
const problem = (response, status, title, detail) =>
  response.status(status).type('application/problem+json').send(JSON.stringify({ title, status, detail }));

// A date written exactly like 2026-11-12, otherwise null.
function parseDate(text) {
  if (typeof text !== 'string' || !/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/.test(text)) return null;
  const day = new Date(`${text}T00:00:00Z`);
  return !Number.isNaN(day.getTime()) && day.toISOString().startsWith(text) ? day : null;
}

// Turns a booking answer into an HTTP status code and a JSON body (L04).
function toHttp(response, result) {
  if (result.outcome === 'Accepted') {
    return response.status(201).json({ id: result.reservationId, status: 'Confirmed', notificationFailed: result.notificationFailed });
  }
  if (result.outcome === 'Unavailable') return problem(response, 409, 'Unavailable', 'The room is booked for at least one night of this stay.');
  return problem(response, 400, 'InvalidDates', 'checkOut must be after checkIn.');
}

export function createApi(rooms, unitOfWork, stats) {
  const app = express();
  app.use(express.json());

  app.get('/openapi.yaml', (request, response) => {
    response.type('application/yaml').send(fs.readFileSync('docs/openapi.yaml', 'utf8'));
  });

  app.get('/stats', (request, response) => {
    const { searches, databaseQueries, cacheHits, cacheMisses } = stats;
    response.json({ searches, databaseQueries, cacheHits, cacheMisses });
  });

  app.get('/rooms/availability', async (request, response) => {
    const { checkIn, checkOut, limit = '20', after = '' } = request.query;
    const start = parseDate(checkIn);
    const end = parseDate(checkOut);
    if (!start || !end) return problem(response, 400, 'InvalidDates', dateDetail);
    const stay = new DateRange(start, end);
    if (!stay.isValid()) return problem(response, 400, 'InvalidDates', 'checkOut must be after checkIn.');
    if (typeof limit !== 'string' || !/^[0-9]{1,3}$/.test(limit) || Number(limit) < 1 || Number(limit) > 100) {
      return problem(response, 400, 'InvalidRequest', 'limit must be a whole number from 1 to 100.');
    }

    stats.searches++;
    const free = await unitOfWork(service => service.findFreeRooms(stay));
    // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
    const rest = free.filter(room => room.number > after).sort((a, b) => (a.number < b.number ? -1 : 1));
    const page = rest.slice(0, Number(limit));
    response.json({
      checkIn, checkOut,
      rooms: page.map(room => ({ number: room.number, type: room.type })),
      next: rest.length > page.length ? page.at(-1).number : null,
    });
  });

  app.post('/reservations', async (request, response) => {
    const body = request.body;
    if (typeof body !== 'object' || body === null || !fields.every(field => typeof body[field] === 'string' && body[field] !== '')) {
      return problem(response, 400, 'InvalidRequest', bodyDetail);
    }
    const start = parseDate(body.checkIn);
    const end = parseDate(body.checkOut);
    if (!start || !end) return problem(response, 400, 'InvalidDates', dateDetail);
    const room = rooms.find(candidate => candidate.number === body.room);
    if (!room) return problem(response, 400, 'UnknownRoom', `There is no room ${body.room}.`);

    const result = await unitOfWork(service =>
      service.reserve(new Guest(body.guestName, body.guestEmail), room, new DateRange(start, end)));
    toHttp(response, result);
  });

  app.delete('/reservations/:id', async (request, response) => {
    const cancelled = await unitOfWork(service => service.cancel(request.params.id));
    if (!cancelled) return problem(response, 404, 'NotFound', `There is no confirmed reservation ${request.params.id}.`);
    response.status(204).end();
  });

  // A body that is not JSON at all.
  app.use((error, request, response, next) => {
    if (error.type === 'entity.parse.failed') return problem(response, 400, 'InvalidRequest', bodyDetail);
    next(error);
  });

  return app;
}
```

#### Dart

```dart
// lib/api.dart
/// The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back,
/// and GET /stats with what the searches cost.
/// It lives outside the four modules and uses only their public contracts. bin/main.dart decides
/// which store the endpoints use, through the unit of work.
library;

import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';

import 'reservations/reservations.dart';
import 'rooms/rooms.dart';

/// Runs one request's work with a BookingService, then saves whatever changed.
typedef UnitOfWork = Future<T> Function<T>(FutureOr<T> Function(BookingService service) work);

const fields = ['guestName', 'guestEmail', 'room', 'checkIn', 'checkOut'];
const bodyDetail = 'The body must be JSON with guestName, guestEmail, room, checkIn and checkOut.';
const dateDetail = 'checkIn and checkOut must be dates such as 2026-11-12.';

// The four counters of GET /stats. A plain ++ is exact: Dart runs every request on one isolate.
class Stats {
  int searches = 0;
  int databaseQueries = 0;
  int cacheHits = 0;
  int cacheMisses = 0;

  Map<String, int> toJson() => {
        'searches': searches,
        'databaseQueries': databaseQueries,
        'cacheHits': cacheHits,
        'cacheMisses': cacheMisses,
      };
}

// Passes every call on to the real store and counts it: each call is one question to the database.
class CountingReservationStore implements ReservationStore {
  CountingReservationStore(this._store, this._stats);

  final ReservationStore _store;
  final Stats _stats;

  @override
  FutureOr<int> get count {
    _stats.databaseQueries++;
    return _store.count;
  }

  @override
  FutureOr<bool> hasOverlap(Room room, DateRange stay) {
    _stats.databaseQueries++;
    return _store.hasOverlap(room, stay);
  }

  @override
  FutureOr<void> save(Reservation reservation) {
    _stats.databaseQueries++;
    return _store.save(reservation);
  }

  @override
  FutureOr<Reservation?> find(String id) {
    _stats.databaseQueries++;
    return _store.find(id);
  }

  @override
  FutureOr<int> countConfirmed(Room room, DateRange stay) {
    _stats.databaseQueries++;
    return _store.countConfirmed(room, stay);
  }
}

Response json(int status, Object? body) =>
    Response(status, body: jsonEncode(body), headers: {'content-type': 'application/json'});

// Problem details (RFC 9457): every error has the same three keys.
Response problem(int status, String title, String detail) => Response(status,
    body: jsonEncode({'title': title, 'status': status, 'detail': detail}),
    headers: {'content-type': 'application/problem+json'});

// A date written exactly like 2026-11-12, otherwise null.
DateTime? parseDate(Object? text) {
  if (text is! String || !RegExp(r'^[0-9]{4}-[0-9]{2}-[0-9]{2}$').hasMatch(text)) return null;
  final day = DateTime.tryParse('${text}T00:00:00Z');
  return day != null && day.toIso8601String().startsWith(text) ? day : null;
}

// Turns a booking answer into an HTTP status code and a JSON body (L04).
Response toHttp(BookingResult result) {
  if (result.outcome == 'Accepted') {
    return json(201, {
      'id': result.reservationId,
      'status': 'Confirmed',
      'notificationFailed': result.notificationFailed
    });
  }
  if (result.outcome == 'Unavailable') {
    return problem(409, 'Unavailable', 'The room is booked for at least one night of this stay.');
  }
  return problem(400, 'InvalidDates', 'checkOut must be after checkIn.');
}

Handler createApi(List<Room> rooms, UnitOfWork unitOfWork, Stats stats) {
  final router = Router();

  router.get('/openapi.yaml', (Request request) {
    return Response.ok(File('docs/openapi.yaml').readAsStringSync(),
        headers: {'content-type': 'application/yaml'});
  });

  router.get('/stats', (Request request) => json(200, stats.toJson()));

  router.get('/rooms/availability', (Request request) async {
    final query = request.url.queryParameters;
    final checkIn = parseDate(query['checkIn']);
    final checkOut = parseDate(query['checkOut']);
    if (checkIn == null || checkOut == null) return problem(400, 'InvalidDates', dateDetail);
    final stay = DateRange(checkIn, checkOut);
    if (!stay.isValid()) return problem(400, 'InvalidDates', 'checkOut must be after checkIn.');
    final limitText = query['limit'] ?? '20';
    final limit = int.tryParse(limitText) ?? 0;
    if (!RegExp(r'^[0-9]{1,3}$').hasMatch(limitText) || limit < 1 || limit > 100) {
      return problem(400, 'InvalidRequest', 'limit must be a whole number from 1 to 100.');
    }

    stats.searches++;
    final free = await unitOfWork((service) => service.findFreeRooms(stay));
    // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
    final after = query['after'] ?? '';
    final rest = free.where((room) => room.number.compareTo(after) > 0).toList()
      ..sort((a, b) => a.number.compareTo(b.number));
    final page = rest.take(limit).toList();
    return json(200, {
      'checkIn': query['checkIn'],
      'checkOut': query['checkOut'],
      'rooms': [
        for (final room in page) {'number': room.number, 'type': room.type}
      ],
      'next': rest.length > page.length ? page.last.number : null,
    });
  });

  router.post('/reservations', (Request request) async {
    if (request.mimeType != 'application/json') return problem(400, 'InvalidRequest', bodyDetail);
    Object? json;
    try {
      json = jsonDecode(await request.readAsString());
    } on FormatException {
      return problem(400, 'InvalidRequest', bodyDetail);
    }
    final body = json is Map ? json : const {};
    if (!fields.every((field) => body[field] is String && body[field] != '')) {
      return problem(400, 'InvalidRequest', bodyDetail);
    }
    final checkIn = parseDate(body['checkIn']);
    final checkOut = parseDate(body['checkOut']);
    if (checkIn == null || checkOut == null) return problem(400, 'InvalidDates', dateDetail);
    final room = rooms.where((room) => room.number == body['room']).firstOrNull;
    if (room == null) return problem(400, 'UnknownRoom', 'There is no room ${body['room']}.');

    final guest = Guest(body['guestName'] as String, body['guestEmail'] as String);
    final result =
        await unitOfWork((service) => service.reserve(guest, room, DateRange(checkIn, checkOut)));
    return toHttp(result);
  });

  router.delete('/reservations/<id>', (Request request, String id) async {
    final cancelled = await unitOfWork((service) => service.cancel(id));
    if (!cancelled) return problem(404, 'NotFound', 'There is no confirmed reservation $id.');
    return Response(204);
  });

  return router.call;
}
```

### Completed example 1.7 · the measurement tool

**What the measurement tool does, line by line** In short: It reads GET /stats, sends 1,000 searches one after another and times each one, reads GET /stats again, and prints the times and the difference. It uses only your language’s standard library and one HTTP connection for all 1,000 requests, so the times show the app, not the opening of new connections.

1. Before: GET /stats says searches 3 (your own curl.exe tests).
2. 1,000 searches for 12–14 Nov, each timed in milliseconds.
3. After: GET /stats says searches 1,003. The tool prints the difference: 1,000.
4. The times are sorted: the average, the 950th (p95) and the total.

C#:

```text
// tools/measure.cs
// Sends 1,000 searches to the running app, one after another, and prints how long they took
// and what they cost: the difference of GET /stats before and after.
//
// dotnet run tools/measure.cs                         -> 12-14 Nov
// dotnet run tools/measure.cs 2026-12-31 2027-01-02   -> other dates
using System.Diagnostics;
using System.Globalization;
using System.Text.Json;

const int searches = 1000;
string[] names = ["searches", "databaseQueries", "cacheHits", "cacheMisses"];
var (checkIn, checkOut) = args.Length >= 2 ? (args[0], args[1]) : ("2026-11-12", "2026-11-14");
var culture = CultureInfo.InvariantCulture;

// One client: it keeps the connection open and reuses it for every request.
using var client = new HttpClient { BaseAddress = new Uri("http://localhost:5080") };

async Task<string> Get(string path)
{
    var response = await client.GetAsync(path);
    var body = await response.Content.ReadAsStringAsync();
    if ((int)response.StatusCode != 200)
    {
        Console.Error.WriteLine($"GET {path} answered {(int)response.StatusCode}: {body}");
        Environment.Exit(1);
    }
    return body;
}

async Task<JsonElement> Stats() => JsonDocument.Parse(await Get("/stats")).RootElement;

var before = await Stats();
var times = new List<double>();
var started = Stopwatch.StartNew();
for (var i = 0; i < searches; i++)
{
    var start = Stopwatch.GetTimestamp();
    await Get($"/rooms/availability?checkIn={checkIn}&checkOut={checkOut}");
    times.Add(Stopwatch.GetElapsedTime(start).TotalMilliseconds);
}
var total = started.Elapsed.TotalSeconds;
var after = await Stats();

times.Sort();
var p95 = times[(int)Math.Ceiling(0.95 * searches) - 1]; // 950 of the 1,000 searches were this fast or faster
var difference = string.Join(",", names.Select(name => $"\"{name}\":{after.GetProperty(name).GetInt64() - before.GetProperty(name).GetInt64()}"));
Console.WriteLine($"{searches} searches for {checkIn} to {checkOut}");
Console.WriteLine($"Average: {times.Average().ToString("F1", culture)} ms");
Console.WriteLine($"p95: {p95.ToString("F1", culture)} ms");
Console.WriteLine($"Total: {total.ToString("F1", culture)} s");
Console.WriteLine($"GET /stats difference: {{{difference}}}");
```

Python:

```text
# tools/measure.py
"""Sends 1,000 searches to the running app, one after another, and prints how long they took
and what they cost: the difference of GET /stats before and after.

python tools/measure.py                         -> 12-14 Nov
python tools/measure.py 2026-12-31 2027-01-02   -> other dates
"""
import http.client
import json
import math
import sys
import time

SEARCHES = 1000
NAMES = ("searches", "databaseQueries", "cacheHits", "cacheMisses")

check_in, check_out = sys.argv[1:3] if len(sys.argv) >= 3 else ("2026-11-12", "2026-11-14")
connection = http.client.HTTPConnection("localhost", 5080)  # one connection for every request


def get(path: str) -> str:
    connection.request("GET", path)
    response = connection.getresponse()
    body = response.read().decode("utf-8")
    if response.status != 200:
        sys.exit(f"GET {path} answered {response.status}: {body}")
    return body


before = json.loads(get("/stats"))
times = []
started = time.perf_counter()
for _ in range(SEARCHES):
    start = time.perf_counter()
    get(f"/rooms/availability?checkIn={check_in}&checkOut={check_out}")
    times.append((time.perf_counter() - start) * 1000)
total = time.perf_counter() - started
after = json.loads(get("/stats"))

times.sort()
p95 = times[math.ceil(0.95 * SEARCHES) - 1]  # 950 of the 1,000 searches were this fast or faster
difference = ",".join(f'"{name}":{after[name] - before[name]}' for name in NAMES)
print(f"{SEARCHES} searches for {check_in} to {check_out}")
print(f"Average: {sum(times) / SEARCHES:.1f} ms")
print(f"p95: {p95:.1f} ms")
print(f"Total: {total:.1f} s")
print(f"GET /stats difference: {{{difference}}}")
```

Java:

```text
// tools/Measure.java
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
```

Go:

```text
// tools/measure/main.go
// Sends 1,000 searches to the running app, one after another, and prints how long they took
// and what they cost: the difference of GET /stats before and after.
//
// go run ./tools/measure                         -> 12-14 Nov
// go run ./tools/measure 2026-12-31 2027-01-02   -> other dates
package main

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"math"
	"net/http"
	"os"
	"slices"
	"strings"
	"time"
)

const searches = 1000

var names = []string{"searches", "databaseQueries", "cacheHits", "cacheMisses"}

// One client: it keeps the connection open and reuses it for every request.
var client = &http.Client{}

func get(path string) []byte {
	response, err := client.Get("http://localhost:5080" + path)
	if err != nil {
		log.Fatal(err)
	}
	defer response.Body.Close()
	body, _ := io.ReadAll(response.Body) // read it all, so the connection can be reused
	if response.StatusCode != http.StatusOK {
		log.Fatalf("GET %s answered %d: %s", path, response.StatusCode, body)
	}
	return body
}

func stats() map[string]int64 {
	var counters map[string]int64
	json.Unmarshal(get("/stats"), &counters)
	return counters
}

func main() {
	checkIn, checkOut := "2026-11-12", "2026-11-14"
	if len(os.Args) >= 3 {
		checkIn, checkOut = os.Args[1], os.Args[2]
	}

	before := stats()
	times := make([]float64, 0, searches)
	started := time.Now()
	for range searches {
		start := time.Now()
		get("/rooms/availability?checkIn=" + checkIn + "&checkOut=" + checkOut)
		times = append(times, float64(time.Since(start).Microseconds())/1000)
	}
	total := time.Since(started).Seconds()
	after := stats()

	slices.Sort(times)
	p95 := times[int(math.Ceil(0.95*searches))-1] // 950 of the 1,000 searches were this fast or faster
	sum := 0.0
	for _, t := range times {
		sum += t
	}
	difference := []string{}
	for _, name := range names {
		difference = append(difference, fmt.Sprintf("%q:%d", name, after[name]-before[name]))
	}
	fmt.Printf("%d searches for %s to %s\n", searches, checkIn, checkOut)
	fmt.Printf("Average: %.1f ms\n", sum/searches)
	fmt.Printf("p95: %.1f ms\n", p95)
	fmt.Printf("Total: %.1f s\n", total)
	fmt.Printf("GET /stats difference: {%s}\n", strings.Join(difference, ","))
}
```

JavaScript:

```text
// tools/measure.js
// Sends 1,000 searches to the running app, one after another, and prints how long they took
// and what they cost: the difference of GET /stats before and after.
//
// node tools/measure.js                         -> 12-14 Nov
// node tools/measure.js 2026-12-31 2027-01-02   -> other dates
import http from 'node:http';

const searches = 1000;
const names = ['searches', 'databaseQueries', 'cacheHits', 'cacheMisses'];
const [checkIn = '2026-11-12', checkOut = '2026-11-14'] = process.argv.slice(2);

// One connection, kept open and reused for every request.
const agent = new http.Agent({ keepAlive: true, maxSockets: 1 });
const get = path => new Promise((resolve, reject) => {
  http.get({ host: 'localhost', port: 5080, path, agent }, response => {
    let body = '';
    response.setEncoding('utf8');
    response.on('data', chunk => { body += chunk; });
    response.on('end', () => {
      if (response.statusCode !== 200) {
        console.error(`GET ${path} answered ${response.statusCode}: ${body}`);
        process.exit(1);
      }
      resolve(body);
    });
  }).on('error', reject);
});

const before = JSON.parse(await get('/stats'));
const times = [];
const started = performance.now();
for (let i = 0; i < searches; i++) {
  const start = performance.now();
  await get(`/rooms/availability?checkIn=${checkIn}&checkOut=${checkOut}`);
  times.push(performance.now() - start);
}
const total = (performance.now() - started) / 1000;
const after = JSON.parse(await get('/stats'));

times.sort((a, b) => a - b);
const p95 = times[Math.ceil(0.95 * searches) - 1]; // 950 of the 1,000 searches were this fast or faster
const average = times.reduce((sum, time) => sum + time, 0) / searches;
const difference = names.map(name => `"${name}":${after[name] - before[name]}`).join(',');
console.log(`${searches} searches for ${checkIn} to ${checkOut}`);
console.log(`Average: ${average.toFixed(1)} ms`);
console.log(`p95: ${p95.toFixed(1)} ms`);
console.log(`Total: ${total.toFixed(1)} s`);
console.log(`GET /stats difference: {${difference}}`);
agent.destroy();
```

Dart:

```text
// tool/measure.dart
// Sends 1,000 searches to the running app, one after another, and prints how long they took
// and what they cost: the difference of GET /stats before and after.
//
// dart run tool/measure.dart                         -> 12-14 Nov
// dart run tool/measure.dart 2026-12-31 2027-01-02   -> other dates
import 'dart:convert';
import 'dart:io';

const searches = 1000;
const names = ['searches', 'databaseQueries', 'cacheHits', 'cacheMisses'];

final client = HttpClient(); // one client, so every request goes over the same open connection

Future<String> get(String path) async {
  final request = await client.getUrl(Uri.parse('http://localhost:5080$path'));
  final response = await request.close();
  final body = await response.transform(utf8.decoder).join();
  if (response.statusCode != 200) {
    stderr.writeln('GET $path answered ${response.statusCode}: $body');
    exit(1);
  }
  return body;
}

Future<void> main(List<String> args) async {
  final checkIn = args.length >= 2 ? args[0] : '2026-11-12';
  final checkOut = args.length >= 2 ? args[1] : '2026-11-14';

  final before = jsonDecode(await get('/stats')) as Map<String, dynamic>;
  final times = <double>[];
  final total = Stopwatch()..start();
  for (var i = 0; i < searches; i++) {
    final watch = Stopwatch()..start();
    await get('/rooms/availability?checkIn=$checkIn&checkOut=$checkOut');
    times.add(watch.elapsedMicroseconds / 1000);
  }
  final seconds = total.elapsedMicroseconds / 1000000;
  final after = jsonDecode(await get('/stats')) as Map<String, dynamic>;
  client.close();

  times.sort();
  // 950 of the 1,000 searches were this fast or faster.
  final p95 = times[(0.95 * searches).ceil() - 1];
  final average = times.reduce((a, b) => a + b) / searches;
  final difference =
      names.map((name) => '"$name":${(after[name] as int) - (before[name] as int)}').join(',');
  print('$searches searches for $checkIn to $checkOut');
  print('Average: ${average.toStringAsFixed(1)} ms');
  print('p95: ${p95.toStringAsFixed(1)} ms');
  print('Total: ${seconds.toStringAsFixed(1)} s');
  print('GET /stats difference: {$difference}');
}
```

### The measurement tool

#### C#

```csharp
// tools/measure.cs
// Sends 1,000 searches to the running app, one after another, and prints how long they took
// and what they cost: the difference of GET /stats before and after.
//
// dotnet run tools/measure.cs                         -> 12-14 Nov
// dotnet run tools/measure.cs 2026-12-31 2027-01-02   -> other dates
using System.Diagnostics;
using System.Globalization;
using System.Text.Json;

const int searches = 1000;
string[] names = ["searches", "databaseQueries", "cacheHits", "cacheMisses"];
var (checkIn, checkOut) = args.Length >= 2 ? (args[0], args[1]) : ("2026-11-12", "2026-11-14");
var culture = CultureInfo.InvariantCulture;

// One client: it keeps the connection open and reuses it for every request.
using var client = new HttpClient { BaseAddress = new Uri("http://localhost:5080") };

async Task<string> Get(string path)
{
    var response = await client.GetAsync(path);
    var body = await response.Content.ReadAsStringAsync();
    if ((int)response.StatusCode != 200)
    {
        Console.Error.WriteLine($"GET {path} answered {(int)response.StatusCode}: {body}");
        Environment.Exit(1);
    }
    return body;
}

async Task<JsonElement> Stats() => JsonDocument.Parse(await Get("/stats")).RootElement;

var before = await Stats();
var times = new List<double>();
var started = Stopwatch.StartNew();
for (var i = 0; i < searches; i++)
{
    var start = Stopwatch.GetTimestamp();
    await Get($"/rooms/availability?checkIn={checkIn}&checkOut={checkOut}");
    times.Add(Stopwatch.GetElapsedTime(start).TotalMilliseconds);
}
var total = started.Elapsed.TotalSeconds;
var after = await Stats();

times.Sort();
var p95 = times[(int)Math.Ceiling(0.95 * searches) - 1]; // 950 of the 1,000 searches were this fast or faster
var difference = string.Join(",", names.Select(name => $"\"{name}\":{after.GetProperty(name).GetInt64() - before.GetProperty(name).GetInt64()}"));
Console.WriteLine($"{searches} searches for {checkIn} to {checkOut}");
Console.WriteLine($"Average: {times.Average().ToString("F1", culture)} ms");
Console.WriteLine($"p95: {p95.ToString("F1", culture)} ms");
Console.WriteLine($"Total: {total.ToString("F1", culture)} s");
Console.WriteLine($"GET /stats difference: {{{difference}}}");
```

#### Python

```python
# tools/measure.py
"""Sends 1,000 searches to the running app, one after another, and prints how long they took
and what they cost: the difference of GET /stats before and after.

python tools/measure.py                         -> 12-14 Nov
python tools/measure.py 2026-12-31 2027-01-02   -> other dates
"""
import http.client
import json
import math
import sys
import time

SEARCHES = 1000
NAMES = ("searches", "databaseQueries", "cacheHits", "cacheMisses")

check_in, check_out = sys.argv[1:3] if len(sys.argv) >= 3 else ("2026-11-12", "2026-11-14")
connection = http.client.HTTPConnection("localhost", 5080)  # one connection for every request


def get(path: str) -> str:
    connection.request("GET", path)
    response = connection.getresponse()
    body = response.read().decode("utf-8")
    if response.status != 200:
        sys.exit(f"GET {path} answered {response.status}: {body}")
    return body


before = json.loads(get("/stats"))
times = []
started = time.perf_counter()
for _ in range(SEARCHES):
    start = time.perf_counter()
    get(f"/rooms/availability?checkIn={check_in}&checkOut={check_out}")
    times.append((time.perf_counter() - start) * 1000)
total = time.perf_counter() - started
after = json.loads(get("/stats"))

times.sort()
p95 = times[math.ceil(0.95 * SEARCHES) - 1]  # 950 of the 1,000 searches were this fast or faster
difference = ",".join(f'"{name}":{after[name] - before[name]}' for name in NAMES)
print(f"{SEARCHES} searches for {check_in} to {check_out}")
print(f"Average: {sum(times) / SEARCHES:.1f} ms")
print(f"p95: {p95:.1f} ms")
print(f"Total: {total:.1f} s")
print(f"GET /stats difference: {{{difference}}}")
```

#### Java

```java
// tools/Measure.java
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
```

#### Go

```go
// tools/measure/main.go
// Sends 1,000 searches to the running app, one after another, and prints how long they took
// and what they cost: the difference of GET /stats before and after.
//
// go run ./tools/measure                         -> 12-14 Nov
// go run ./tools/measure 2026-12-31 2027-01-02   -> other dates
package main

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"math"
	"net/http"
	"os"
	"slices"
	"strings"
	"time"
)

const searches = 1000

var names = []string{"searches", "databaseQueries", "cacheHits", "cacheMisses"}

// One client: it keeps the connection open and reuses it for every request.
var client = &http.Client{}

func get(path string) []byte {
	response, err := client.Get("http://localhost:5080" + path)
	if err != nil {
		log.Fatal(err)
	}
	defer response.Body.Close()
	body, _ := io.ReadAll(response.Body) // read it all, so the connection can be reused
	if response.StatusCode != http.StatusOK {
		log.Fatalf("GET %s answered %d: %s", path, response.StatusCode, body)
	}
	return body
}

func stats() map[string]int64 {
	var counters map[string]int64
	json.Unmarshal(get("/stats"), &counters)
	return counters
}

func main() {
	checkIn, checkOut := "2026-11-12", "2026-11-14"
	if len(os.Args) >= 3 {
		checkIn, checkOut = os.Args[1], os.Args[2]
	}

	before := stats()
	times := make([]float64, 0, searches)
	started := time.Now()
	for range searches {
		start := time.Now()
		get("/rooms/availability?checkIn=" + checkIn + "&checkOut=" + checkOut)
		times = append(times, float64(time.Since(start).Microseconds())/1000)
	}
	total := time.Since(started).Seconds()
	after := stats()

	slices.Sort(times)
	p95 := times[int(math.Ceil(0.95*searches))-1] // 950 of the 1,000 searches were this fast or faster
	sum := 0.0
	for _, t := range times {
		sum += t
	}
	difference := []string{}
	for _, name := range names {
		difference = append(difference, fmt.Sprintf("%q:%d", name, after[name]-before[name]))
	}
	fmt.Printf("%d searches for %s to %s\n", searches, checkIn, checkOut)
	fmt.Printf("Average: %.1f ms\n", sum/searches)
	fmt.Printf("p95: %.1f ms\n", p95)
	fmt.Printf("Total: %.1f s\n", total)
	fmt.Printf("GET /stats difference: {%s}\n", strings.Join(difference, ","))
}
```

#### JavaScript

```javascript
// tools/measure.js
// Sends 1,000 searches to the running app, one after another, and prints how long they took
// and what they cost: the difference of GET /stats before and after.
//
// node tools/measure.js                         -> 12-14 Nov
// node tools/measure.js 2026-12-31 2027-01-02   -> other dates
import http from 'node:http';

const searches = 1000;
const names = ['searches', 'databaseQueries', 'cacheHits', 'cacheMisses'];
const [checkIn = '2026-11-12', checkOut = '2026-11-14'] = process.argv.slice(2);

// One connection, kept open and reused for every request.
const agent = new http.Agent({ keepAlive: true, maxSockets: 1 });
const get = path => new Promise((resolve, reject) => {
  http.get({ host: 'localhost', port: 5080, path, agent }, response => {
    let body = '';
    response.setEncoding('utf8');
    response.on('data', chunk => { body += chunk; });
    response.on('end', () => {
      if (response.statusCode !== 200) {
        console.error(`GET ${path} answered ${response.statusCode}: ${body}`);
        process.exit(1);
      }
      resolve(body);
    });
  }).on('error', reject);
});

const before = JSON.parse(await get('/stats'));
const times = [];
const started = performance.now();
for (let i = 0; i < searches; i++) {
  const start = performance.now();
  await get(`/rooms/availability?checkIn=${checkIn}&checkOut=${checkOut}`);
  times.push(performance.now() - start);
}
const total = (performance.now() - started) / 1000;
const after = JSON.parse(await get('/stats'));

times.sort((a, b) => a - b);
const p95 = times[Math.ceil(0.95 * searches) - 1]; // 950 of the 1,000 searches were this fast or faster
const average = times.reduce((sum, time) => sum + time, 0) / searches;
const difference = names.map(name => `"${name}":${after[name] - before[name]}`).join(',');
console.log(`${searches} searches for ${checkIn} to ${checkOut}`);
console.log(`Average: ${average.toFixed(1)} ms`);
console.log(`p95: ${p95.toFixed(1)} ms`);
console.log(`Total: ${total.toFixed(1)} s`);
console.log(`GET /stats difference: {${difference}}`);
agent.destroy();
```

#### Dart

```dart
// tool/measure.dart
// Sends 1,000 searches to the running app, one after another, and prints how long they took
// and what they cost: the difference of GET /stats before and after.
//
// dart run tool/measure.dart                         -> 12-14 Nov
// dart run tool/measure.dart 2026-12-31 2027-01-02   -> other dates
import 'dart:convert';
import 'dart:io';

const searches = 1000;
const names = ['searches', 'databaseQueries', 'cacheHits', 'cacheMisses'];

final client = HttpClient(); // one client, so every request goes over the same open connection

Future<String> get(String path) async {
  final request = await client.getUrl(Uri.parse('http://localhost:5080$path'));
  final response = await request.close();
  final body = await response.transform(utf8.decoder).join();
  if (response.statusCode != 200) {
    stderr.writeln('GET $path answered ${response.statusCode}: $body');
    exit(1);
  }
  return body;
}

Future<void> main(List<String> args) async {
  final checkIn = args.length >= 2 ? args[0] : '2026-11-12';
  final checkOut = args.length >= 2 ? args[1] : '2026-11-14';

  final before = jsonDecode(await get('/stats')) as Map<String, dynamic>;
  final times = <double>[];
  final total = Stopwatch()..start();
  for (var i = 0; i < searches; i++) {
    final watch = Stopwatch()..start();
    await get('/rooms/availability?checkIn=$checkIn&checkOut=$checkOut');
    times.add(watch.elapsedMicroseconds / 1000);
  }
  final seconds = total.elapsedMicroseconds / 1000000;
  final after = jsonDecode(await get('/stats')) as Map<String, dynamic>;
  client.close();

  times.sort();
  // 950 of the 1,000 searches were this fast or faster.
  final p95 = times[(0.95 * searches).ceil() - 1];
  final average = times.reduce((a, b) => a + b) / searches;
  final difference =
      names.map((name) => '"$name":${(after[name] as int) - (before[name] as int)}').join(',');
  print('$searches searches for $checkIn to $checkOut');
  print('Average: ${average.toStringAsFixed(1)} ms');
  print('p95: ${p95.toStringAsFixed(1)} ms');
  print('Total: ${seconds.toStringAsFixed(1)} s');
  print('GET /stats difference: {$difference}');
}
```

### Run it

Terminal 1 runs the app, terminal 2 the tool.

### Two terminals

#### C#

```text
# terminal 1
dotnet run --project HotelBooking
# terminal 2
dotnet run tools/measure.cs
```

#### Python

```text
# terminal 1
python main.py
# terminal 2
python tools/measure.py
```

#### Java

```text
# terminal 1
mvn -q spring-boot:run
# terminal 2
java tools/Measure.java
```

#### Go

```text
# terminal 1
go run .
# terminal 2
go run ./tools/measure
```

#### JavaScript

```text
# terminal 1
node main.js
# terminal 2
node tools/measure.js
```

#### Dart

```text
# terminal 1
dart run bin/main.dart
# terminal 2
dart run tool/measure.dart
```

### What you will see

The last line is the same in every language. The times are your laptop’s own; these are from one laptop.

**p95: the time 95 of 100 searches beat** In short: Sort the 1,000 times; p95 is the 950th. Only 50 searches were slower.

1. The fastest search took 1.2 ms, the slowest 14 ms.
2. After sorting, the 950th time is 2.5 ms: that is p95.
3. The average hides the slow ones; p95 shows what an unlucky guest waits.
4. S1 asks for p95 ≤ 1 second at 200 searches a minute.

### Terminal 2

```text
1000 searches for 2026-11-12 to 2026-11-14
Average: 2.0 ms
p95: 2.5 ms
Total: 2.0 s
GET /stats difference: {"searches":1000,"databaseQueries":2000,"cacheHits":0,"cacheMisses":0}
```

### Completed example: docs/cache-experiment.md after Step 1

### docs/cache-experiment.md

```text
# Cache experiment: the free-room search

PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.

## Measurements

1,000 searches for 12–14 Nov, one after another, with the measurement tool.
Times from one laptop; yours will differ.

| Run | Searches | Database queries | Cache hits | Average | p95 |
|---|---|---|---|---|---|
| No cache | 1,000 | 2,000 | 0 | 2.0 ms | 2.5 ms |
```

### Your turn · measure the search

1. Add the /stats operation and the Stats schema to docs/openapi.yaml (1.1) and change version to 1.1.0.
2. Add Stats and CountingReservationStore (1.2) in the file your tab shows.
3. Add GET /stats and the searches line to the search endpoint (1.3); change the wiring (1.4) and the test helper (1.5).
4. Run your checks: still 15 green.
5. Create the measurement tool (1.7). Start the app in terminal 1, run the tool in terminal 2.
6. Create docs/cache-experiment.md like the completed example, with your own times in the row “No cache”.

**When you finish, you have:** GET /stats, the measurement tool, 15 green checks, and the row “No cache” in docs/cache-experiment.md.
### Explanation

2,000 database queries for 1,000 searches: one per room per search, exactly as P07’s estimate said. That is the number the cache must bring down.



---

## 3. Step 2 · Start Redis

Redis joins PostgreSQL in docker-compose.yml. One command starts it; redis-cli checks that it answers.





### The new service, line by line

| Line | What it does |
| --- | --- |
| cache: | The service name: docker compose exec cache … runs a command inside it |
| image: redis:8 | The official Redis 8 image; Docker downloads it the first time |
| ports: "6379:6379" | Your laptop’s port 6379 leads to Redis in the container |
| No volume | Redis keeps only copies of search answers. If they are lost, the next search asks PostgreSQL again. |

**Redis in docker-compose.yml** In short: Redis is a program that keeps keys and values in memory; the new service cache runs the official redis:8 image next to PostgreSQL. Redis has no volume here on purpose: it keeps only copies of search answers. If they are lost, the next search asks PostgreSQL again.

1. docker compose up -d downloads redis:8 the first time and starts the service cache.
2. docker compose exec cache redis-cli ping runs Redis’s own client inside the container: it answers PONG.
3. Your app connects to localhost:6379.

```text
  cache:
    image: redis:8
    ports:
      - "6379:6379"                  # P08: copies of search answers, nothing that must survive

docker compose exec cache redis-cli ping
PONG
```

**If port 6379 is already taken on your laptop** In short: Publish Redis on another port in docker-compose.yml and tell the app that port with HOTEL_REDIS_PORT, as you did for PostgreSQL. docker compose up then says “port is already allocated” for the service cache.

1. In docker-compose.yml change "6379:6379" to "16379:6379" and run docker compose up -d again.
2. Before starting the app, set HOTEL_REDIS_PORT=16379 in the same terminal.
3. The wiring reads HOTEL_REDIS_PORT; without it, the app uses 6379.

```text
PowerShell:   $env:HOTEL_REDIS_PORT = "16379"
cmd:          set HOTEL_REDIS_PORT=16379
Git Bash:     export HOTEL_REDIS_PORT=16379
```

### Completed example: docker-compose.yml

### docker-compose.yml

```text
# The hotel booking app's database and cache. Start: docker compose up -d   Stop: docker compose down
services:
  db:
    image: postgres:18
    environment:
      POSTGRES_DB: hotel
      POSTGRES_USER: hotel
      POSTGRES_PASSWORD: hotel       # for your laptop only
    ports:
      - "5432:5432"
    volumes:
      - ./db/init.sql:/docker-entrypoint-initdb.d/init.sql:ro   # runs once, on the first start
      - hotel-data:/var/lib/postgresql                          # the data survives a restart

  cache:
    image: redis:8
    ports:
      - "6379:6379"                  # P08: copies of search answers, nothing that must survive

volumes:
  hotel-data:

```

### Check it

### Terminal 2

```text
docker compose up -d
docker compose exec cache redis-cli ping
PONG
```

### Your turn · start Redis

1. Add the cache service to docker-compose.yml, above volumes:, exactly as in the completed example.
2. Change the first comment line to “The hotel booking app’s database and cache.”
3. Run docker compose up -d. The first time it downloads Redis.
4. Run docker compose ps: db and cache are both “Up”.
5. Run docker compose exec cache redis-cli ping: it answers PONG.

**When you finish, you have:** Redis 8 running next to PostgreSQL, and PONG in your terminal.
### Explanation

PostgreSQL keeps its data in a volume; Redis does not need one. That difference is the whole idea of a cache: the database holds the truth, the cache holds copies.



---

## 4. Step 3 · Cache-aside for the search

The search endpoint looks in Redis first. On a miss it asks PostgreSQL and keeps a copy for 30 seconds; on a hit PostgreSQL is not asked at all.





### The cache in four decisions

| Decision | Value | Why |
| --- | --- | --- |
| Key | availability:v{version}:{checkIn}:{checkOut} | One copy per stay; the version lets Step 6 make all copies old at once |
| Value | The free room numbers in text order, such as "R7,R8" | Small; the endpoint builds the rooms from the room list it already has |
| TTL | 30 seconds (SET … EX 30) | If a copy is ever wrong, it is wrong for at most 30 seconds |
| What is cached | Only the free-room search | Booking and cancelling always read PostgreSQL: the source of truth |

**The cache file: three Redis commands and the key** In short: The cache needs only GET, SET with a time to live, and INCR; AvailabilityCache builds the key and decides the TTL. The three commands sit behind a small contract (KeyValueStore). The app gives it the Redis client; the tests give it a dictionary. So the tests run the real key and version code without Redis.

1. key("2026-11-12", "2026-11-14"): GET availability:version answers nothing yet, so the version is 0.
2. The key is availability:v0:2026-11-12:2026-11-14.
3. set(key, "R7,R8") sends SET availability:v0:2026-11-12:2026-11-14 "R7,R8" EX 30.
4. invalidate() sends INCR availability:version: the version becomes 1.

C#:

```text
// HotelBooking/Api/AvailabilityCache.cs
using StackExchange.Redis;

namespace HotelBooking.Api;

// The three Redis commands the cache uses: GET, SET with a time to live, and INCR.
public interface IKeyValueStore
{
    string? Get(string key);
    void Set(string key, string value, TimeSpan ttl);
    void Increment(string key);
}

// The three commands, sent to Redis.
public class RedisKeyValueStore(IDatabase redis) : IKeyValueStore
{
    public string? Get(string key) => (string?)redis.StringGet(key);
    public void Set(string key, string value, TimeSpan ttl) => redis.StringSet(key, value, ttl);
    public void Increment(string key) => redis.StringIncrement(key);
}

// The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.
// PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds:
// the free room numbers for one stay, such as "R7,R8". Booking and cancelling never read the cache.
public class AvailabilityCache(IKeyValueStore store)
{
    public static readonly TimeSpan Ttl = TimeSpan.FromSeconds(30);
    public const string VersionKey = "availability:version";

    // The version is part of the key, so raising it makes every older copy unreachable.
    public string Key(string checkIn, string checkOut) => $"availability:v{store.Get(VersionKey) ?? "0"}:{checkIn}:{checkOut}";

    public string? Get(string key) => store.Get(key);

    public void Set(string key, string roomNumbers) => store.Set(key, roomNumbers, Ttl);

    // A booking or a cancellation changed what is free: every cached search is now out of date.
    public void Invalidate() => store.Increment(VersionKey);
}
```

Python:

```text
# hotel_booking/api_cache.py
"""The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.

PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.
Booking and cancelling never read the cache.
"""
from __future__ import annotations

from typing import Protocol

from redis import Redis

TTL_SECONDS = 30
VERSION_KEY = "availability:version"


class KeyValueStore(Protocol):
    """The three Redis commands the cache uses: GET, SET with a time to live, and INCR."""

    def get(self, key: str) -> str | None: ...
    def set(self, key: str, value: str, ttl_seconds: int) -> None: ...
    def increment(self, key: str) -> None: ...


class RedisKeyValueStore:
    """The three commands, sent to Redis."""

    def __init__(self, redis: Redis) -> None:
        self._redis = redis

    def get(self, key: str) -> str | None:
        return self._redis.get(key)

    def set(self, key: str, value: str, ttl_seconds: int) -> None:
        self._redis.set(key, value, ex=ttl_seconds)

    def increment(self, key: str) -> None:
        self._redis.incr(key)


class AvailabilityCache:
    """Copies of search answers: the free room numbers for one stay, such as "R7,R8"."""

    def __init__(self, store: KeyValueStore) -> None:
        self._store = store

    def key(self, check_in: str, check_out: str) -> str:
        # The version is part of the key, so raising it makes every older copy unreachable.
        version = self._store.get(VERSION_KEY) or "0"
        return f"availability:v{version}:{check_in}:{check_out}"

    def get(self, key: str) -> str | None:
        return self._store.get(key)

    def set(self, key: str, room_numbers: str) -> None:
        self._store.set(key, room_numbers, TTL_SECONDS)

    def invalidate(self) -> None:
        """A booking or a cancellation changed what is free: every cached search is now out of date."""
        self._store.increment(VERSION_KEY)
```

Java:

```text
// src/main/java/hotelbooking/api/AvailabilityCache.java
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
```

Go:

```text
// api/cache.go
// The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.
// PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.
// Booking and cancelling never read the cache.
package api

import (
	"context"
	"errors"
	"time"

	"github.com/redis/go-redis/v9"
)

const ttl = 30 * time.Second
const versionKey = "availability:version"

// KeyValueStore is the three Redis commands the cache uses: GET, SET with a time to live, and INCR.
type KeyValueStore interface {
	Get(key string) (value string, found bool, err error)
	Set(key, value string, ttl time.Duration) error
	Increment(key string) error
}

// RedisKeyValueStore sends the three commands to Redis.
type RedisKeyValueStore struct {
	ctx    context.Context
	client *redis.Client
}

func NewRedisKeyValueStore(ctx context.Context, client *redis.Client) *RedisKeyValueStore {
	return &RedisKeyValueStore{ctx: ctx, client: client}
}

func (store *RedisKeyValueStore) Get(key string) (string, bool, error) {
	value, err := store.client.Get(store.ctx, key).Result()
	if errors.Is(err, redis.Nil) {
		return "", false, nil // no copy under this key
	}
	return value, err == nil, err
}

func (store *RedisKeyValueStore) Set(key, value string, ttl time.Duration) error {
	return store.client.Set(store.ctx, key, value, ttl).Err()
}

func (store *RedisKeyValueStore) Increment(key string) error {
	return store.client.Incr(store.ctx, key).Err()
}

// AvailabilityCache keeps copies of search answers: the free room numbers for one stay, such as "R7,R8".
type AvailabilityCache struct {
	store KeyValueStore
}

func NewAvailabilityCache(store KeyValueStore) *AvailabilityCache {
	return &AvailabilityCache{store: store}
}

// Key is the key for one stay. The version is part of the key, so raising it makes every older copy unreachable.
func (cache *AvailabilityCache) Key(checkIn, checkOut string) (string, error) {
	version, found, err := cache.store.Get(versionKey)
	if !found {
		version = "0"
	}
	return "availability:v" + version + ":" + checkIn + ":" + checkOut, err
}

func (cache *AvailabilityCache) Get(key string) (string, bool, error) {
	return cache.store.Get(key)
}

func (cache *AvailabilityCache) Set(key, roomNumbers string) error {
	return cache.store.Set(key, roomNumbers, ttl)
}

// Invalidate is called after a booking or a cancellation changed what is free: every cached search is now out of date.
func (cache *AvailabilityCache) Invalidate() error {
	return cache.store.Increment(versionKey)
}
```

JavaScript:

```text
// src/api-cache.js
// The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.
// PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.
// Booking and cancelling never read the cache.
//
// The cache needs only three Redis commands. Every key-value store it uses keeps this contract:
//
//   get(key)                     the value, or null when there is none (may answer later)
//   set(key, value, ttlSeconds)  keep the value for ttlSeconds
//   increment(key)               add 1 to the number stored under key
export const ttlSeconds = 30;
export const versionKey = 'availability:version';

// The three commands, sent to Redis through node-redis.
export class RedisKeyValueStore {
  #redis;

  constructor(redis) {
    this.#redis = redis;
  }

  get(key) {
    return this.#redis.get(key);
  }

  async set(key, value, seconds) {
    await this.#redis.set(key, value, { expiration: { type: 'EX', value: seconds } });
  }

  async increment(key) {
    await this.#redis.incr(key);
  }
}

// Copies of search answers: the free room numbers for one stay, such as "R7,R8".
export class AvailabilityCache {
  #store;

  constructor(store) {
    this.#store = store;
  }

  async key(checkIn, checkOut) {
    // The version is part of the key, so raising it makes every older copy unreachable.
    const version = (await this.#store.get(versionKey)) ?? '0';
    return `availability:v${version}:${checkIn}:${checkOut}`;
  }

  get(key) {
    return this.#store.get(key);
  }

  async set(key, roomNumbers) {
    await this.#store.set(key, roomNumbers, ttlSeconds);
  }

  // A booking or a cancellation changed what is free: every cached search is now out of date.
  async invalidate() {
    await this.#store.increment(versionKey);
  }
}
```

Dart:

```text
// lib/api_cache.dart
/// The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.
///
/// PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.
/// Booking and cancelling never read the cache.
library;

import 'package:redis/redis.dart' show Command;

const ttlSeconds = 30;
const versionKey = 'availability:version';

// The three Redis commands the cache uses: GET, SET with a time to live, and INCR.
abstract interface class KeyValueStore {
  // Returns null when the key is missing.
  Future<String?> get(String key);

  Future<void> set(String key, String value, int ttlSeconds);

  Future<void> increment(String key);
}

// The three commands, sent to Redis over one open connection.
class RedisKeyValueStore implements KeyValueStore {
  RedisKeyValueStore(this._redis);

  final Command _redis;

  // Redis answers GET with the text, or with null when the key is missing.
  @override
  Future<String?> get(String key) async => await _redis.send_object(['GET', key]) as String?;

  @override
  Future<void> set(String key, String value, int ttlSeconds) =>
      _redis.send_object(['SET', key, value, 'EX', '$ttlSeconds']);

  @override
  Future<void> increment(String key) => _redis.send_object(['INCR', key]);
}

// Copies of search answers: the free room numbers for one stay, such as "R7,R8".
class AvailabilityCache {
  AvailabilityCache(this._store);

  final KeyValueStore _store;

  Future<String> key(String checkIn, String checkOut) async {
    // The version is part of the key, so raising it makes every older copy unreachable.
    final version = await _store.get(versionKey) ?? '0';
    return 'availability:v$version:$checkIn:$checkOut';
  }

  Future<String?> get(String key) => _store.get(key);

  Future<void> set(String key, String roomNumbers) => _store.set(key, roomNumbers, ttlSeconds);

  // A booking or a cancellation changed what is free: every cached search is now out of date.
  Future<void> invalidate() => _store.increment(versionKey);
}
```

**Cache-aside in the search endpoint** In short: Look in Redis first; on a hit answer from the copy; on a miss ask PostgreSQL, keep a copy, and answer. The key is built once, before PostgreSQL is asked. Only the free-room search is cached; the paging (limit and after) is done on the copy, so one copy serves every page.

1. Miss: Redis has no copy. The unit of work runs FindFreeRooms (2 questions to PostgreSQL), and the app stores "R7,R8" for 30 s.
2. Hit: Redis answers "R7,R8". The app builds the rooms from the room list it already has; no unit of work, no database connection.
3. Both answers are the same JSON, byte for byte.

C#:

```text
app.MapGet("/rooms/availability", (string? checkIn, string? checkOut, string? limit, string? after) =>
{
    if (ParseDate(checkIn) is not { } start || ParseDate(checkOut) is not { } end) return Problem(400, "InvalidDates", DateDetail);
    var stay = new DateRange(start, end);
    if (!stay.IsValid()) return Problem(400, "InvalidDates", "checkOut must be after checkIn.");
    limit ??= "20";
    if (!Regex.IsMatch(limit, @"^[0-9]{1,3}\z") || int.Parse(limit) is < 1 or > 100)
        return Problem(400, "InvalidRequest", "limit must be a whole number from 1 to 100.");

    stats.AddSearch();
    // Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
    var key = cache.Key(checkIn!, checkOut!);
    if (cache.Get(key) is { } cached)
    {
        stats.AddCacheHit();
        var numbers = cached.Split(',');
        return Page(rooms.Where(room => numbers.Contains(room.Number)));
    }
    stats.AddCacheMiss();
    return unitOfWork(service =>
    {
        var free = service.FindFreeRooms(stay);
        cache.Set(key, string.Join(",", free.Select(room => room.Number).Order(StringComparer.Ordinal)));
        return Page(free);
    });

    IResult Page(IEnumerable<Room> free)
    {
        // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
        var rest = free.Where(room => string.CompareOrdinal(room.Number, after ?? "") > 0)
            .OrderBy(room => room.Number, StringComparer.Ordinal).ToList();
        var page = rest.Take(int.Parse(limit)).ToList();
        return Results.Json(new
        {
            checkIn, checkOut,
            rooms = page.Select(room => new { number = room.Number, type = room.Type }),
            next = rest.Count > page.Count ? page[^1].Number : null,
        });
    }
});
```

Python:

```text
@app.get("/rooms/availability")
def find_free_rooms(checkIn: str = "", checkOut: str = "", limit: str = "20", after: str = "") -> JSONResponse:
    check_in, check_out = parse_date(checkIn), parse_date(checkOut)
    if check_in is None or check_out is None:
        return problem(400, "InvalidDates", DATE_DETAIL)
    stay = DateRange(check_in, check_out)
    if not stay.is_valid():
        return problem(400, "InvalidDates", "checkOut must be after checkIn.")
    if not re.fullmatch(r"[0-9]{1,3}", limit) or not 1 <= int(limit) <= 100:
        return problem(400, "InvalidRequest", "limit must be a whole number from 1 to 100.")

    stats.add("searches")
    # Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
    key = cache.key(checkIn, checkOut)
    cached = cache.get(key)
    if cached is not None:
        stats.add("cacheHits")
        numbers = cached.split(",")
        free = [room for room in rooms if room.number in numbers]
    else:
        stats.add("cacheMisses")
        with unit_of_work() as service:
            free = service.find_free_rooms(stay)
        cache.set(key, ",".join(sorted(room.number for room in free)))
    # Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
    rest = sorted((room for room in free if room.number > after), key=lambda room: room.number)
    page = rest[:int(limit)]
    return JSONResponse({
        "checkIn": checkIn, "checkOut": checkOut,
        "rooms": [{"number": room.number, "type": room.type} for room in page],
        "next": page[-1].number if len(rest) > len(page) else None,
    })
```

Java:

```text
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
    // Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
    String key = cache.key(checkIn, checkOut);
    String cached = cache.get(key);
    if (cached != null) {
        stats.cacheHits.incrementAndGet();
        List<String> numbers = List.of(cached.split(","));
        return toPage(rooms.stream().filter(room -> numbers.contains(room.number())).toList(), checkIn, checkOut, size, after);
    }
    stats.cacheMisses.incrementAndGet();
    return unitOfWork.run(service -> {
        List<Room> free = service.findFreeRooms(stay);
        cache.set(key, free.stream().map(Room::number).sorted().collect(Collectors.joining(",")));
        return toPage(free, checkIn, checkOut, size, after);
    });
}

// Cursor pagination (L04): the free rooms after the last one the client has seen, in text order.
static ResponseEntity<?> toPage(List<Room> free, String checkIn, String checkOut, String size, String after) {
    List<Room> rest = free.stream().filter(room -> room.number().compareTo(after) > 0)
        .sorted(Comparator.comparing(Room::number)).toList();
    List<Room> page = rest.stream().limit(Integer.parseInt(size)).toList();
    String next = rest.size() > page.size() ? page.get(page.size() - 1).number() : null;
    return ResponseEntity.ok(new Availability(checkIn, checkOut, page.stream().map(room -> new RoomJson(room.number(), room.type())).toList(), next));
}
```

Go:

```text
mux.HandleFunc("GET /rooms/availability", func(w http.ResponseWriter, r *http.Request) {
	query := r.URL.Query()
	checkIn, okIn := parseDate(query.Get("checkIn"))
	checkOut, okOut := parseDate(query.Get("checkOut"))
	if !okIn || !okOut {
		problem(w, http.StatusBadRequest, "InvalidDates", dateDetail)
		return
	}
	stay := reservations.DateRange{CheckIn: checkIn, CheckOut: checkOut}
	if !stay.IsValid() {
		problem(w, http.StatusBadRequest, "InvalidDates", "checkOut must be after checkIn.")
		return
	}
	limitText := query.Get("limit")
	if !query.Has("limit") {
		limitText = "20"
	}
	limit, _ := strconv.Atoi(limitText)
	if !regexp.MustCompile(`^[0-9]{1,3}$`).MatchString(limitText) || limit < 1 || limit > 100 {
		problem(w, http.StatusBadRequest, "InvalidRequest", "limit must be a whole number from 1 to 100.")
		return
	}

	stats.Searches.Add(1)
	// Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
	key, err := cache.Key(query.Get("checkIn"), query.Get("checkOut"))
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	cached, found, err := cache.Get(key)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	var free []rooms.Room
	if found {
		stats.CacheHits.Add(1)
		numbers := strings.Split(cached, ",")
		for _, room := range hotelRooms {
			if slices.Contains(numbers, room.Number) {
				free = append(free, room)
			}
		}
	} else {
		stats.CacheMisses.Add(1)
		if err := unitOfWork(func(service *reservations.BookingService) { free = service.FindFreeRooms(stay) }); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		numbers := []string{}
		for _, room := range free {
			numbers = append(numbers, room.Number)
		}
		slices.Sort(numbers)
		if err := cache.Set(key, strings.Join(numbers, ",")); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
	}
	// Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
	after := query.Get("after")
	rest := []rooms.Room{}
	for _, room := range free {
		if room.Number > after {
			rest = append(rest, room)
		}
	}
	slices.SortFunc(rest, func(a, b rooms.Room) int { return strings.Compare(a.Number, b.Number) })
	answer := availabilityJSON{CheckIn: query.Get("checkIn"), CheckOut: query.Get("checkOut"), Rooms: []roomJSON{}}
	for _, room := range rest[:min(limit, len(rest))] {
		answer.Rooms = append(answer.Rooms, roomJSON{Number: room.Number, Type: room.Type})
	}
	if len(rest) > limit {
		answer.Next = &answer.Rooms[limit-1].Number
	}
	write(w, http.StatusOK, "application/json", answer)
})
```

JavaScript:

```text
app.get('/rooms/availability', async (request, response) => {
  const { checkIn, checkOut, limit = '20', after = '' } = request.query;
  const start = parseDate(checkIn);
  const end = parseDate(checkOut);
  if (!start || !end) return problem(response, 400, 'InvalidDates', dateDetail);
  const stay = new DateRange(start, end);
  if (!stay.isValid()) return problem(response, 400, 'InvalidDates', 'checkOut must be after checkIn.');
  if (typeof limit !== 'string' || !/^[0-9]{1,3}$/.test(limit) || Number(limit) < 1 || Number(limit) > 100) {
    return problem(response, 400, 'InvalidRequest', 'limit must be a whole number from 1 to 100.');
  }

  stats.searches++;
  // Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
  const key = await cache.key(checkIn, checkOut);
  const cached = await cache.get(key);
  let free;
  if (cached !== null) {
    stats.cacheHits++;
    const numbers = cached.split(',');
    free = rooms.filter(room => numbers.includes(room.number));
  } else {
    stats.cacheMisses++;
    free = await unitOfWork(service => service.findFreeRooms(stay));
    await cache.set(key, free.map(room => room.number).sort().join(','));
  }
  // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
  const rest = free.filter(room => room.number > after).sort((a, b) => (a.number < b.number ? -1 : 1));
  const page = rest.slice(0, Number(limit));
  response.json({
    checkIn, checkOut,
    rooms: page.map(room => ({ number: room.number, type: room.type })),
    next: rest.length > page.length ? page.at(-1).number : null,
  });
});
```

Dart:

```text
router.get('/rooms/availability', (Request request) async {
  final query = request.url.queryParameters;
  final checkIn = parseDate(query['checkIn']);
  final checkOut = parseDate(query['checkOut']);
  if (checkIn == null || checkOut == null) return problem(400, 'InvalidDates', dateDetail);
  final stay = DateRange(checkIn, checkOut);
  if (!stay.isValid()) return problem(400, 'InvalidDates', 'checkOut must be after checkIn.');
  final limitText = query['limit'] ?? '20';
  final limit = int.tryParse(limitText) ?? 0;
  if (!RegExp(r'^[0-9]{1,3}$').hasMatch(limitText) || limit < 1 || limit > 100) {
    return problem(400, 'InvalidRequest', 'limit must be a whole number from 1 to 100.');
  }

  stats.searches++;
  // Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
  final key = await cache.key(query['checkIn']!, query['checkOut']!);
  final cached = await cache.get(key);
  final List<Room> free;
  if (cached != null) {
    stats.cacheHits++;
    final numbers = cached.split(',');
    free = rooms.where((room) => numbers.contains(room.number)).toList();
  } else {
    stats.cacheMisses++;
    free = await unitOfWork((service) => service.findFreeRooms(stay));
    await cache.set(key, (free.map((room) => room.number).toList()..sort()).join(','));
  }
  // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
  final after = query['after'] ?? '';
  final rest = free.where((room) => room.number.compareTo(after) > 0).toList()
    ..sort((a, b) => a.number.compareTo(b.number));
  final page = rest.take(limit).toList();
  return json(200, {
    'checkIn': query['checkIn'],
    'checkOut': query['checkOut'],
    'rooms': [
      for (final room in page) {'number': room.number, 'type': room.type}
    ],
    'next': rest.length > page.length ? page.last.number : null,
  });
});
```

### Your language’s Redis client

| Language | Redis client | Project change |
| --- | --- | --- |
| C# | StackExchange.Redis | HotelBooking.csproj gets StackExchange.Redis |
| Python | redis-py | requirements.txt gets redis |
| Java | Spring Data Redis | pom.xml gets spring-boot-starter-data-redis; application.properties gets the Redis port |
| Go | go-redis | go.mod gets go-redis (go get) |
| JavaScript | node-redis | package.json gets redis (npm install) |
| Dart | the redis package | pubspec.yaml gets redis (dart pub add) |

### Install (once, internet needed)

### Install

#### C#

```text
dotnet add HotelBooking package StackExchange.Redis --version 3.4.20
```

#### Python

```text
.venv\Scripts\activate          # macOS/Linux: source .venv/bin/activate
python -m pip install redis==8.1.0
```

#### Java

```text
# pom.xml: add spring-boot-starter-data-redis (the version comes from the Spring Boot parent)
mvn -q test                      # downloads it once (internet needed)
```

#### Go

```text
go get github.com/redis/go-redis/v9@v9.23.0
```

#### JavaScript

```text
npm install redis@6.3.0
```

#### Dart

```text
dart pub add redis:4.1.0
```

### Completed example 3.1 · the project file

### Project file

#### C#

```csharp
<!-- HotelBooking/HotelBooking.csproj -->
<Project Sdk="Microsoft.NET.Sdk.Web"><PropertyGroup><OutputType>Exe</OutputType><TargetFramework>net10.0</TargetFramework><ImplicitUsings>enable</ImplicitUsings><Nullable>enable</Nullable></PropertyGroup><ItemGroup>
  <PackageReference Include="Npgsql.EntityFrameworkCore.PostgreSQL" Version="10.0.3" />
  <PackageReference Include="StackExchange.Redis" Version="3.4.20" />
  <!-- The API serves the contract: the build copies docs/openapi.yaml next to the program. -->
  <None Include="..\docs\openapi.yaml" Link="openapi.yaml" CopyToOutputDirectory="PreserveNewest" />
</ItemGroup></Project>
```

#### Python

```python
# requirements.txt
# Everything the Python project needs: import-linter from P06, the five packages P07 adds and redis from P08.
# Install them all at once:  python -m pip install -r requirements.txt
fastapi==0.162.0
uvicorn==0.54.0
sqlalchemy==2.1.6
psycopg[binary]==3.3.6
httpx==0.28.1
redis==8.1.0
import-linter
```

#### Java

```java
<!-- pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <!-- Spring Boot chooses matching versions for the web, database (P07), Redis (P08) and test libraries. -->
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.2</version>
  </parent>

  <groupId>com.example</groupId>
  <artifactId>hotel-booking</artifactId>
  <version>1.0</version>

  <properties>
    <java.version>17</java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-webmvc-test</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>com.tngtech.archunit</groupId>
      <artifactId>archunit-junit5</artifactId>
      <version>1.4.0</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

#### Go

```go
// go.mod
module hotelbooking

go 1.26.0

require (
	github.com/jackc/pgx/v5 v5.11.0
	github.com/redis/go-redis/v9 v9.23.0
)

require (
	github.com/cespare/xxhash/v2 v2.3.0 // indirect
	github.com/jackc/pgpassfile v1.0.0 // indirect
	github.com/jackc/pgservicefile v0.0.0-20240606120523-5a60cdf6a761 // indirect
	github.com/jackc/puddle/v2 v2.2.2 // indirect
	go.uber.org/atomic v1.12.0 // indirect
	golang.org/x/sync v0.17.0 // indirect
	golang.org/x/sys v0.48.0 // indirect
	golang.org/x/text v0.29.0 // indirect
)
```

#### JavaScript

```javascript
// package.json
{
  "name": "hotel-booking",
  "private": true,
  "type": "module",
  "dependencies": {
    "express": "^5.3.0",
    "pg": "^8.23.1",
    "redis": "^6.3.0"
  }
}
```

#### Dart

```dart
# pubspec.yaml
name: hotel_booking
description: P08 · Hotel booking - the P07 API with a Redis cache for the free-room search.
environment:
  sdk: ^3.0.0

dev_dependencies:
  test: ^1.25.0
dependencies:
  postgres: 3.5.20
  redis: 4.1.0
  shelf: 1.4.3
  shelf_router: 1.1.5
```

### Java only: src/main/resources/application.properties

Spring Boot reads the Redis host and port from this file.

### src/main/resources/application.properties

```text
# src/main/resources/application.properties
# The web app on http://localhost:5080, with PostgreSQL and Redis from docker-compose.yml
# (set HOTEL_DB_PORT or HOTEL_REDIS_PORT only if port 5432 or 6379 is taken on your laptop).
server.address=localhost
server.port=5080
spring.datasource.url=jdbc:postgresql://localhost:${HOTEL_DB_PORT:5432}/hotel
spring.datasource.username=hotel
spring.datasource.password=hotel
spring.data.redis.host=localhost
spring.data.redis.port=${HOTEL_REDIS_PORT:6379}
# db/init.sql creates the tables; Hibernate only reads and writes rows.
spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=false
spring.main.banner-mode=off
```

### Completed example 3.2 · the cache file

**The cache file: three Redis commands and the key** In short: The cache needs only GET, SET with a time to live, and INCR; AvailabilityCache builds the key and decides the TTL. The three commands sit behind a small contract (KeyValueStore). The app gives it the Redis client; the tests give it a dictionary. So the tests run the real key and version code without Redis.

1. key("2026-11-12", "2026-11-14"): GET availability:version answers nothing yet, so the version is 0.
2. The key is availability:v0:2026-11-12:2026-11-14.
3. set(key, "R7,R8") sends SET availability:v0:2026-11-12:2026-11-14 "R7,R8" EX 30.
4. invalidate() sends INCR availability:version: the version becomes 1.

C#:

```text
// HotelBooking/Api/AvailabilityCache.cs
using StackExchange.Redis;

namespace HotelBooking.Api;

// The three Redis commands the cache uses: GET, SET with a time to live, and INCR.
public interface IKeyValueStore
{
    string? Get(string key);
    void Set(string key, string value, TimeSpan ttl);
    void Increment(string key);
}

// The three commands, sent to Redis.
public class RedisKeyValueStore(IDatabase redis) : IKeyValueStore
{
    public string? Get(string key) => (string?)redis.StringGet(key);
    public void Set(string key, string value, TimeSpan ttl) => redis.StringSet(key, value, ttl);
    public void Increment(string key) => redis.StringIncrement(key);
}

// The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.
// PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds:
// the free room numbers for one stay, such as "R7,R8". Booking and cancelling never read the cache.
public class AvailabilityCache(IKeyValueStore store)
{
    public static readonly TimeSpan Ttl = TimeSpan.FromSeconds(30);
    public const string VersionKey = "availability:version";

    // The version is part of the key, so raising it makes every older copy unreachable.
    public string Key(string checkIn, string checkOut) => $"availability:v{store.Get(VersionKey) ?? "0"}:{checkIn}:{checkOut}";

    public string? Get(string key) => store.Get(key);

    public void Set(string key, string roomNumbers) => store.Set(key, roomNumbers, Ttl);

    // A booking or a cancellation changed what is free: every cached search is now out of date.
    public void Invalidate() => store.Increment(VersionKey);
}
```

Python:

```text
# hotel_booking/api_cache.py
"""The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.

PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.
Booking and cancelling never read the cache.
"""
from __future__ import annotations

from typing import Protocol

from redis import Redis

TTL_SECONDS = 30
VERSION_KEY = "availability:version"


class KeyValueStore(Protocol):
    """The three Redis commands the cache uses: GET, SET with a time to live, and INCR."""

    def get(self, key: str) -> str | None: ...
    def set(self, key: str, value: str, ttl_seconds: int) -> None: ...
    def increment(self, key: str) -> None: ...


class RedisKeyValueStore:
    """The three commands, sent to Redis."""

    def __init__(self, redis: Redis) -> None:
        self._redis = redis

    def get(self, key: str) -> str | None:
        return self._redis.get(key)

    def set(self, key: str, value: str, ttl_seconds: int) -> None:
        self._redis.set(key, value, ex=ttl_seconds)

    def increment(self, key: str) -> None:
        self._redis.incr(key)


class AvailabilityCache:
    """Copies of search answers: the free room numbers for one stay, such as "R7,R8"."""

    def __init__(self, store: KeyValueStore) -> None:
        self._store = store

    def key(self, check_in: str, check_out: str) -> str:
        # The version is part of the key, so raising it makes every older copy unreachable.
        version = self._store.get(VERSION_KEY) or "0"
        return f"availability:v{version}:{check_in}:{check_out}"

    def get(self, key: str) -> str | None:
        return self._store.get(key)

    def set(self, key: str, room_numbers: str) -> None:
        self._store.set(key, room_numbers, TTL_SECONDS)

    def invalidate(self) -> None:
        """A booking or a cancellation changed what is free: every cached search is now out of date."""
        self._store.increment(VERSION_KEY)
```

Java:

```text
// src/main/java/hotelbooking/api/AvailabilityCache.java
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
```

Go:

```text
// api/cache.go
// The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.
// PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.
// Booking and cancelling never read the cache.
package api

import (
	"context"
	"errors"
	"time"

	"github.com/redis/go-redis/v9"
)

const ttl = 30 * time.Second
const versionKey = "availability:version"

// KeyValueStore is the three Redis commands the cache uses: GET, SET with a time to live, and INCR.
type KeyValueStore interface {
	Get(key string) (value string, found bool, err error)
	Set(key, value string, ttl time.Duration) error
	Increment(key string) error
}

// RedisKeyValueStore sends the three commands to Redis.
type RedisKeyValueStore struct {
	ctx    context.Context
	client *redis.Client
}

func NewRedisKeyValueStore(ctx context.Context, client *redis.Client) *RedisKeyValueStore {
	return &RedisKeyValueStore{ctx: ctx, client: client}
}

func (store *RedisKeyValueStore) Get(key string) (string, bool, error) {
	value, err := store.client.Get(store.ctx, key).Result()
	if errors.Is(err, redis.Nil) {
		return "", false, nil // no copy under this key
	}
	return value, err == nil, err
}

func (store *RedisKeyValueStore) Set(key, value string, ttl time.Duration) error {
	return store.client.Set(store.ctx, key, value, ttl).Err()
}

func (store *RedisKeyValueStore) Increment(key string) error {
	return store.client.Incr(store.ctx, key).Err()
}

// AvailabilityCache keeps copies of search answers: the free room numbers for one stay, such as "R7,R8".
type AvailabilityCache struct {
	store KeyValueStore
}

func NewAvailabilityCache(store KeyValueStore) *AvailabilityCache {
	return &AvailabilityCache{store: store}
}

// Key is the key for one stay. The version is part of the key, so raising it makes every older copy unreachable.
func (cache *AvailabilityCache) Key(checkIn, checkOut string) (string, error) {
	version, found, err := cache.store.Get(versionKey)
	if !found {
		version = "0"
	}
	return "availability:v" + version + ":" + checkIn + ":" + checkOut, err
}

func (cache *AvailabilityCache) Get(key string) (string, bool, error) {
	return cache.store.Get(key)
}

func (cache *AvailabilityCache) Set(key, roomNumbers string) error {
	return cache.store.Set(key, roomNumbers, ttl)
}

// Invalidate is called after a booking or a cancellation changed what is free: every cached search is now out of date.
func (cache *AvailabilityCache) Invalidate() error {
	return cache.store.Increment(versionKey)
}
```

JavaScript:

```text
// src/api-cache.js
// The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.
// PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.
// Booking and cancelling never read the cache.
//
// The cache needs only three Redis commands. Every key-value store it uses keeps this contract:
//
//   get(key)                     the value, or null when there is none (may answer later)
//   set(key, value, ttlSeconds)  keep the value for ttlSeconds
//   increment(key)               add 1 to the number stored under key
export const ttlSeconds = 30;
export const versionKey = 'availability:version';

// The three commands, sent to Redis through node-redis.
export class RedisKeyValueStore {
  #redis;

  constructor(redis) {
    this.#redis = redis;
  }

  get(key) {
    return this.#redis.get(key);
  }

  async set(key, value, seconds) {
    await this.#redis.set(key, value, { expiration: { type: 'EX', value: seconds } });
  }

  async increment(key) {
    await this.#redis.incr(key);
  }
}

// Copies of search answers: the free room numbers for one stay, such as "R7,R8".
export class AvailabilityCache {
  #store;

  constructor(store) {
    this.#store = store;
  }

  async key(checkIn, checkOut) {
    // The version is part of the key, so raising it makes every older copy unreachable.
    const version = (await this.#store.get(versionKey)) ?? '0';
    return `availability:v${version}:${checkIn}:${checkOut}`;
  }

  get(key) {
    return this.#store.get(key);
  }

  async set(key, roomNumbers) {
    await this.#store.set(key, roomNumbers, ttlSeconds);
  }

  // A booking or a cancellation changed what is free: every cached search is now out of date.
  async invalidate() {
    await this.#store.increment(versionKey);
  }
}
```

Dart:

```text
// lib/api_cache.dart
/// The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.
///
/// PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.
/// Booking and cancelling never read the cache.
library;

import 'package:redis/redis.dart' show Command;

const ttlSeconds = 30;
const versionKey = 'availability:version';

// The three Redis commands the cache uses: GET, SET with a time to live, and INCR.
abstract interface class KeyValueStore {
  // Returns null when the key is missing.
  Future<String?> get(String key);

  Future<void> set(String key, String value, int ttlSeconds);

  Future<void> increment(String key);
}

// The three commands, sent to Redis over one open connection.
class RedisKeyValueStore implements KeyValueStore {
  RedisKeyValueStore(this._redis);

  final Command _redis;

  // Redis answers GET with the text, or with null when the key is missing.
  @override
  Future<String?> get(String key) async => await _redis.send_object(['GET', key]) as String?;

  @override
  Future<void> set(String key, String value, int ttlSeconds) =>
      _redis.send_object(['SET', key, value, 'EX', '$ttlSeconds']);

  @override
  Future<void> increment(String key) => _redis.send_object(['INCR', key]);
}

// Copies of search answers: the free room numbers for one stay, such as "R7,R8".
class AvailabilityCache {
  AvailabilityCache(this._store);

  final KeyValueStore _store;

  Future<String> key(String checkIn, String checkOut) async {
    // The version is part of the key, so raising it makes every older copy unreachable.
    final version = await _store.get(versionKey) ?? '0';
    return 'availability:v$version:$checkIn:$checkOut';
  }

  Future<String?> get(String key) => _store.get(key);

  Future<void> set(String key, String roomNumbers) => _store.set(key, roomNumbers, ttlSeconds);

  // A booking or a cancellation changed what is free: every cached search is now out of date.
  Future<void> invalidate() => _store.increment(versionKey);
}
```

### AvailabilityCache

#### C#

```csharp
// HotelBooking/Api/AvailabilityCache.cs
using StackExchange.Redis;

namespace HotelBooking.Api;

// The three Redis commands the cache uses: GET, SET with a time to live, and INCR.
public interface IKeyValueStore
{
    string? Get(string key);
    void Set(string key, string value, TimeSpan ttl);
    void Increment(string key);
}

// The three commands, sent to Redis.
public class RedisKeyValueStore(IDatabase redis) : IKeyValueStore
{
    public string? Get(string key) => (string?)redis.StringGet(key);
    public void Set(string key, string value, TimeSpan ttl) => redis.StringSet(key, value, ttl);
    public void Increment(string key) => redis.StringIncrement(key);
}

// The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.
// PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds:
// the free room numbers for one stay, such as "R7,R8". Booking and cancelling never read the cache.
public class AvailabilityCache(IKeyValueStore store)
{
    public static readonly TimeSpan Ttl = TimeSpan.FromSeconds(30);
    public const string VersionKey = "availability:version";

    // The version is part of the key, so raising it makes every older copy unreachable.
    public string Key(string checkIn, string checkOut) => $"availability:v{store.Get(VersionKey) ?? "0"}:{checkIn}:{checkOut}";

    public string? Get(string key) => store.Get(key);

    public void Set(string key, string roomNumbers) => store.Set(key, roomNumbers, Ttl);

    // A booking or a cancellation changed what is free: every cached search is now out of date.
    public void Invalidate() => store.Increment(VersionKey);
}
```

#### Python

```python
# hotel_booking/api_cache.py
"""The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.

PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.
Booking and cancelling never read the cache.
"""
from __future__ import annotations

from typing import Protocol

from redis import Redis

TTL_SECONDS = 30
VERSION_KEY = "availability:version"


class KeyValueStore(Protocol):
    """The three Redis commands the cache uses: GET, SET with a time to live, and INCR."""

    def get(self, key: str) -> str | None: ...
    def set(self, key: str, value: str, ttl_seconds: int) -> None: ...
    def increment(self, key: str) -> None: ...


class RedisKeyValueStore:
    """The three commands, sent to Redis."""

    def __init__(self, redis: Redis) -> None:
        self._redis = redis

    def get(self, key: str) -> str | None:
        return self._redis.get(key)

    def set(self, key: str, value: str, ttl_seconds: int) -> None:
        self._redis.set(key, value, ex=ttl_seconds)

    def increment(self, key: str) -> None:
        self._redis.incr(key)


class AvailabilityCache:
    """Copies of search answers: the free room numbers for one stay, such as "R7,R8"."""

    def __init__(self, store: KeyValueStore) -> None:
        self._store = store

    def key(self, check_in: str, check_out: str) -> str:
        # The version is part of the key, so raising it makes every older copy unreachable.
        version = self._store.get(VERSION_KEY) or "0"
        return f"availability:v{version}:{check_in}:{check_out}"

    def get(self, key: str) -> str | None:
        return self._store.get(key)

    def set(self, key: str, room_numbers: str) -> None:
        self._store.set(key, room_numbers, TTL_SECONDS)

    def invalidate(self) -> None:
        """A booking or a cancellation changed what is free: every cached search is now out of date."""
        self._store.increment(VERSION_KEY)
```

#### Java

```java
// src/main/java/hotelbooking/api/AvailabilityCache.java
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
```

#### Go

```go
// api/cache.go
// The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.
// PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.
// Booking and cancelling never read the cache.
package api

import (
	"context"
	"errors"
	"time"

	"github.com/redis/go-redis/v9"
)

const ttl = 30 * time.Second
const versionKey = "availability:version"

// KeyValueStore is the three Redis commands the cache uses: GET, SET with a time to live, and INCR.
type KeyValueStore interface {
	Get(key string) (value string, found bool, err error)
	Set(key, value string, ttl time.Duration) error
	Increment(key string) error
}

// RedisKeyValueStore sends the three commands to Redis.
type RedisKeyValueStore struct {
	ctx    context.Context
	client *redis.Client
}

func NewRedisKeyValueStore(ctx context.Context, client *redis.Client) *RedisKeyValueStore {
	return &RedisKeyValueStore{ctx: ctx, client: client}
}

func (store *RedisKeyValueStore) Get(key string) (string, bool, error) {
	value, err := store.client.Get(store.ctx, key).Result()
	if errors.Is(err, redis.Nil) {
		return "", false, nil // no copy under this key
	}
	return value, err == nil, err
}

func (store *RedisKeyValueStore) Set(key, value string, ttl time.Duration) error {
	return store.client.Set(store.ctx, key, value, ttl).Err()
}

func (store *RedisKeyValueStore) Increment(key string) error {
	return store.client.Incr(store.ctx, key).Err()
}

// AvailabilityCache keeps copies of search answers: the free room numbers for one stay, such as "R7,R8".
type AvailabilityCache struct {
	store KeyValueStore
}

func NewAvailabilityCache(store KeyValueStore) *AvailabilityCache {
	return &AvailabilityCache{store: store}
}

// Key is the key for one stay. The version is part of the key, so raising it makes every older copy unreachable.
func (cache *AvailabilityCache) Key(checkIn, checkOut string) (string, error) {
	version, found, err := cache.store.Get(versionKey)
	if !found {
		version = "0"
	}
	return "availability:v" + version + ":" + checkIn + ":" + checkOut, err
}

func (cache *AvailabilityCache) Get(key string) (string, bool, error) {
	return cache.store.Get(key)
}

func (cache *AvailabilityCache) Set(key, roomNumbers string) error {
	return cache.store.Set(key, roomNumbers, ttl)
}

// Invalidate is called after a booking or a cancellation changed what is free: every cached search is now out of date.
func (cache *AvailabilityCache) Invalidate() error {
	return cache.store.Increment(versionKey)
}
```

#### JavaScript

```javascript
// src/api-cache.js
// The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.
// PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.
// Booking and cancelling never read the cache.
//
// The cache needs only three Redis commands. Every key-value store it uses keeps this contract:
//
//   get(key)                     the value, or null when there is none (may answer later)
//   set(key, value, ttlSeconds)  keep the value for ttlSeconds
//   increment(key)               add 1 to the number stored under key
export const ttlSeconds = 30;
export const versionKey = 'availability:version';

// The three commands, sent to Redis through node-redis.
export class RedisKeyValueStore {
  #redis;

  constructor(redis) {
    this.#redis = redis;
  }

  get(key) {
    return this.#redis.get(key);
  }

  async set(key, value, seconds) {
    await this.#redis.set(key, value, { expiration: { type: 'EX', value: seconds } });
  }

  async increment(key) {
    await this.#redis.incr(key);
  }
}

// Copies of search answers: the free room numbers for one stay, such as "R7,R8".
export class AvailabilityCache {
  #store;

  constructor(store) {
    this.#store = store;
  }

  async key(checkIn, checkOut) {
    // The version is part of the key, so raising it makes every older copy unreachable.
    const version = (await this.#store.get(versionKey)) ?? '0';
    return `availability:v${version}:${checkIn}:${checkOut}`;
  }

  get(key) {
    return this.#store.get(key);
  }

  async set(key, roomNumbers) {
    await this.#store.set(key, roomNumbers, ttlSeconds);
  }

  // A booking or a cancellation changed what is free: every cached search is now out of date.
  async invalidate() {
    await this.#store.increment(versionKey);
  }
}
```

#### Dart

```dart
// lib/api_cache.dart
/// The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.
///
/// PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.
/// Booking and cancelling never read the cache.
library;

import 'package:redis/redis.dart' show Command;

const ttlSeconds = 30;
const versionKey = 'availability:version';

// The three Redis commands the cache uses: GET, SET with a time to live, and INCR.
abstract interface class KeyValueStore {
  // Returns null when the key is missing.
  Future<String?> get(String key);

  Future<void> set(String key, String value, int ttlSeconds);

  Future<void> increment(String key);
}

// The three commands, sent to Redis over one open connection.
class RedisKeyValueStore implements KeyValueStore {
  RedisKeyValueStore(this._redis);

  final Command _redis;

  // Redis answers GET with the text, or with null when the key is missing.
  @override
  Future<String?> get(String key) async => await _redis.send_object(['GET', key]) as String?;

  @override
  Future<void> set(String key, String value, int ttlSeconds) =>
      _redis.send_object(['SET', key, value, 'EX', '$ttlSeconds']);

  @override
  Future<void> increment(String key) => _redis.send_object(['INCR', key]);
}

// Copies of search answers: the free room numbers for one stay, such as "R7,R8".
class AvailabilityCache {
  AvailabilityCache(this._store);

  final KeyValueStore _store;

  Future<String> key(String checkIn, String checkOut) async {
    // The version is part of the key, so raising it makes every older copy unreachable.
    final version = await _store.get(versionKey) ?? '0';
    return 'availability:v$version:$checkIn:$checkOut';
  }

  Future<String?> get(String key) => _store.get(key);

  Future<void> set(String key, String roomNumbers) => _store.set(key, roomNumbers, ttlSeconds);

  // A booking or a cancellation changed what is free: every cached search is now out of date.
  Future<void> invalidate() => _store.increment(versionKey);
}
```

### Completed example 3.3 · the search endpoint with cache-aside

**Cache-aside in the search endpoint** In short: Look in Redis first; on a hit answer from the copy; on a miss ask PostgreSQL, keep a copy, and answer. The key is built once, before PostgreSQL is asked. Only the free-room search is cached; the paging (limit and after) is done on the copy, so one copy serves every page.

1. Miss: Redis has no copy. The unit of work runs FindFreeRooms (2 questions to PostgreSQL), and the app stores "R7,R8" for 30 s.
2. Hit: Redis answers "R7,R8". The app builds the rooms from the room list it already has; no unit of work, no database connection.
3. Both answers are the same JSON, byte for byte.

C#:

```text
app.MapGet("/rooms/availability", (string? checkIn, string? checkOut, string? limit, string? after) =>
{
    if (ParseDate(checkIn) is not { } start || ParseDate(checkOut) is not { } end) return Problem(400, "InvalidDates", DateDetail);
    var stay = new DateRange(start, end);
    if (!stay.IsValid()) return Problem(400, "InvalidDates", "checkOut must be after checkIn.");
    limit ??= "20";
    if (!Regex.IsMatch(limit, @"^[0-9]{1,3}\z") || int.Parse(limit) is < 1 or > 100)
        return Problem(400, "InvalidRequest", "limit must be a whole number from 1 to 100.");

    stats.AddSearch();
    // Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
    var key = cache.Key(checkIn!, checkOut!);
    if (cache.Get(key) is { } cached)
    {
        stats.AddCacheHit();
        var numbers = cached.Split(',');
        return Page(rooms.Where(room => numbers.Contains(room.Number)));
    }
    stats.AddCacheMiss();
    return unitOfWork(service =>
    {
        var free = service.FindFreeRooms(stay);
        cache.Set(key, string.Join(",", free.Select(room => room.Number).Order(StringComparer.Ordinal)));
        return Page(free);
    });

    IResult Page(IEnumerable<Room> free)
    {
        // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
        var rest = free.Where(room => string.CompareOrdinal(room.Number, after ?? "") > 0)
            .OrderBy(room => room.Number, StringComparer.Ordinal).ToList();
        var page = rest.Take(int.Parse(limit)).ToList();
        return Results.Json(new
        {
            checkIn, checkOut,
            rooms = page.Select(room => new { number = room.Number, type = room.Type }),
            next = rest.Count > page.Count ? page[^1].Number : null,
        });
    }
});
```

Python:

```text
@app.get("/rooms/availability")
def find_free_rooms(checkIn: str = "", checkOut: str = "", limit: str = "20", after: str = "") -> JSONResponse:
    check_in, check_out = parse_date(checkIn), parse_date(checkOut)
    if check_in is None or check_out is None:
        return problem(400, "InvalidDates", DATE_DETAIL)
    stay = DateRange(check_in, check_out)
    if not stay.is_valid():
        return problem(400, "InvalidDates", "checkOut must be after checkIn.")
    if not re.fullmatch(r"[0-9]{1,3}", limit) or not 1 <= int(limit) <= 100:
        return problem(400, "InvalidRequest", "limit must be a whole number from 1 to 100.")

    stats.add("searches")
    # Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
    key = cache.key(checkIn, checkOut)
    cached = cache.get(key)
    if cached is not None:
        stats.add("cacheHits")
        numbers = cached.split(",")
        free = [room for room in rooms if room.number in numbers]
    else:
        stats.add("cacheMisses")
        with unit_of_work() as service:
            free = service.find_free_rooms(stay)
        cache.set(key, ",".join(sorted(room.number for room in free)))
    # Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
    rest = sorted((room for room in free if room.number > after), key=lambda room: room.number)
    page = rest[:int(limit)]
    return JSONResponse({
        "checkIn": checkIn, "checkOut": checkOut,
        "rooms": [{"number": room.number, "type": room.type} for room in page],
        "next": page[-1].number if len(rest) > len(page) else None,
    })
```

Java:

```text
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
    // Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
    String key = cache.key(checkIn, checkOut);
    String cached = cache.get(key);
    if (cached != null) {
        stats.cacheHits.incrementAndGet();
        List<String> numbers = List.of(cached.split(","));
        return toPage(rooms.stream().filter(room -> numbers.contains(room.number())).toList(), checkIn, checkOut, size, after);
    }
    stats.cacheMisses.incrementAndGet();
    return unitOfWork.run(service -> {
        List<Room> free = service.findFreeRooms(stay);
        cache.set(key, free.stream().map(Room::number).sorted().collect(Collectors.joining(",")));
        return toPage(free, checkIn, checkOut, size, after);
    });
}

// Cursor pagination (L04): the free rooms after the last one the client has seen, in text order.
static ResponseEntity<?> toPage(List<Room> free, String checkIn, String checkOut, String size, String after) {
    List<Room> rest = free.stream().filter(room -> room.number().compareTo(after) > 0)
        .sorted(Comparator.comparing(Room::number)).toList();
    List<Room> page = rest.stream().limit(Integer.parseInt(size)).toList();
    String next = rest.size() > page.size() ? page.get(page.size() - 1).number() : null;
    return ResponseEntity.ok(new Availability(checkIn, checkOut, page.stream().map(room -> new RoomJson(room.number(), room.type())).toList(), next));
}
```

Go:

```text
mux.HandleFunc("GET /rooms/availability", func(w http.ResponseWriter, r *http.Request) {
	query := r.URL.Query()
	checkIn, okIn := parseDate(query.Get("checkIn"))
	checkOut, okOut := parseDate(query.Get("checkOut"))
	if !okIn || !okOut {
		problem(w, http.StatusBadRequest, "InvalidDates", dateDetail)
		return
	}
	stay := reservations.DateRange{CheckIn: checkIn, CheckOut: checkOut}
	if !stay.IsValid() {
		problem(w, http.StatusBadRequest, "InvalidDates", "checkOut must be after checkIn.")
		return
	}
	limitText := query.Get("limit")
	if !query.Has("limit") {
		limitText = "20"
	}
	limit, _ := strconv.Atoi(limitText)
	if !regexp.MustCompile(`^[0-9]{1,3}$`).MatchString(limitText) || limit < 1 || limit > 100 {
		problem(w, http.StatusBadRequest, "InvalidRequest", "limit must be a whole number from 1 to 100.")
		return
	}

	stats.Searches.Add(1)
	// Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
	key, err := cache.Key(query.Get("checkIn"), query.Get("checkOut"))
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	cached, found, err := cache.Get(key)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	var free []rooms.Room
	if found {
		stats.CacheHits.Add(1)
		numbers := strings.Split(cached, ",")
		for _, room := range hotelRooms {
			if slices.Contains(numbers, room.Number) {
				free = append(free, room)
			}
		}
	} else {
		stats.CacheMisses.Add(1)
		if err := unitOfWork(func(service *reservations.BookingService) { free = service.FindFreeRooms(stay) }); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		numbers := []string{}
		for _, room := range free {
			numbers = append(numbers, room.Number)
		}
		slices.Sort(numbers)
		if err := cache.Set(key, strings.Join(numbers, ",")); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
	}
	// Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
	after := query.Get("after")
	rest := []rooms.Room{}
	for _, room := range free {
		if room.Number > after {
			rest = append(rest, room)
		}
	}
	slices.SortFunc(rest, func(a, b rooms.Room) int { return strings.Compare(a.Number, b.Number) })
	answer := availabilityJSON{CheckIn: query.Get("checkIn"), CheckOut: query.Get("checkOut"), Rooms: []roomJSON{}}
	for _, room := range rest[:min(limit, len(rest))] {
		answer.Rooms = append(answer.Rooms, roomJSON{Number: room.Number, Type: room.Type})
	}
	if len(rest) > limit {
		answer.Next = &answer.Rooms[limit-1].Number
	}
	write(w, http.StatusOK, "application/json", answer)
})
```

JavaScript:

```text
app.get('/rooms/availability', async (request, response) => {
  const { checkIn, checkOut, limit = '20', after = '' } = request.query;
  const start = parseDate(checkIn);
  const end = parseDate(checkOut);
  if (!start || !end) return problem(response, 400, 'InvalidDates', dateDetail);
  const stay = new DateRange(start, end);
  if (!stay.isValid()) return problem(response, 400, 'InvalidDates', 'checkOut must be after checkIn.');
  if (typeof limit !== 'string' || !/^[0-9]{1,3}$/.test(limit) || Number(limit) < 1 || Number(limit) > 100) {
    return problem(response, 400, 'InvalidRequest', 'limit must be a whole number from 1 to 100.');
  }

  stats.searches++;
  // Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
  const key = await cache.key(checkIn, checkOut);
  const cached = await cache.get(key);
  let free;
  if (cached !== null) {
    stats.cacheHits++;
    const numbers = cached.split(',');
    free = rooms.filter(room => numbers.includes(room.number));
  } else {
    stats.cacheMisses++;
    free = await unitOfWork(service => service.findFreeRooms(stay));
    await cache.set(key, free.map(room => room.number).sort().join(','));
  }
  // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
  const rest = free.filter(room => room.number > after).sort((a, b) => (a.number < b.number ? -1 : 1));
  const page = rest.slice(0, Number(limit));
  response.json({
    checkIn, checkOut,
    rooms: page.map(room => ({ number: room.number, type: room.type })),
    next: rest.length > page.length ? page.at(-1).number : null,
  });
});
```

Dart:

```text
router.get('/rooms/availability', (Request request) async {
  final query = request.url.queryParameters;
  final checkIn = parseDate(query['checkIn']);
  final checkOut = parseDate(query['checkOut']);
  if (checkIn == null || checkOut == null) return problem(400, 'InvalidDates', dateDetail);
  final stay = DateRange(checkIn, checkOut);
  if (!stay.isValid()) return problem(400, 'InvalidDates', 'checkOut must be after checkIn.');
  final limitText = query['limit'] ?? '20';
  final limit = int.tryParse(limitText) ?? 0;
  if (!RegExp(r'^[0-9]{1,3}$').hasMatch(limitText) || limit < 1 || limit > 100) {
    return problem(400, 'InvalidRequest', 'limit must be a whole number from 1 to 100.');
  }

  stats.searches++;
  // Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
  final key = await cache.key(query['checkIn']!, query['checkOut']!);
  final cached = await cache.get(key);
  final List<Room> free;
  if (cached != null) {
    stats.cacheHits++;
    final numbers = cached.split(',');
    free = rooms.where((room) => numbers.contains(room.number)).toList();
  } else {
    stats.cacheMisses++;
    free = await unitOfWork((service) => service.findFreeRooms(stay));
    await cache.set(key, (free.map((room) => room.number).toList()..sort()).join(','));
  }
  // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
  final after = query['after'] ?? '';
  final rest = free.where((room) => room.number.compareTo(after) > 0).toList()
    ..sort((a, b) => a.number.compareTo(b.number));
  final page = rest.take(limit).toList();
  return json(200, {
    'checkIn': query['checkIn'],
    'checkOut': query['checkOut'],
    'rooms': [
      for (final room in page) {'number': room.number, 'type': room.type}
    ],
    'next': rest.length > page.length ? page.last.number : null,
  });
});
```

### The search endpoint

#### C#

```csharp
app.MapGet("/rooms/availability", (string? checkIn, string? checkOut, string? limit, string? after) =>
{
    if (ParseDate(checkIn) is not { } start || ParseDate(checkOut) is not { } end) return Problem(400, "InvalidDates", DateDetail);
    var stay = new DateRange(start, end);
    if (!stay.IsValid()) return Problem(400, "InvalidDates", "checkOut must be after checkIn.");
    limit ??= "20";
    if (!Regex.IsMatch(limit, @"^[0-9]{1,3}\z") || int.Parse(limit) is < 1 or > 100)
        return Problem(400, "InvalidRequest", "limit must be a whole number from 1 to 100.");

    stats.AddSearch();
    // Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
    var key = cache.Key(checkIn!, checkOut!);
    if (cache.Get(key) is { } cached)
    {
        stats.AddCacheHit();
        var numbers = cached.Split(',');
        return Page(rooms.Where(room => numbers.Contains(room.Number)));
    }
    stats.AddCacheMiss();
    return unitOfWork(service =>
    {
        var free = service.FindFreeRooms(stay);
        cache.Set(key, string.Join(",", free.Select(room => room.Number).Order(StringComparer.Ordinal)));
        return Page(free);
    });

    IResult Page(IEnumerable<Room> free)
    {
        // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
        var rest = free.Where(room => string.CompareOrdinal(room.Number, after ?? "") > 0)
            .OrderBy(room => room.Number, StringComparer.Ordinal).ToList();
        var page = rest.Take(int.Parse(limit)).ToList();
        return Results.Json(new
        {
            checkIn, checkOut,
            rooms = page.Select(room => new { number = room.Number, type = room.Type }),
            next = rest.Count > page.Count ? page[^1].Number : null,
        });
    }
});
```

#### Python

```python
@app.get("/rooms/availability")
def find_free_rooms(checkIn: str = "", checkOut: str = "", limit: str = "20", after: str = "") -> JSONResponse:
    check_in, check_out = parse_date(checkIn), parse_date(checkOut)
    if check_in is None or check_out is None:
        return problem(400, "InvalidDates", DATE_DETAIL)
    stay = DateRange(check_in, check_out)
    if not stay.is_valid():
        return problem(400, "InvalidDates", "checkOut must be after checkIn.")
    if not re.fullmatch(r"[0-9]{1,3}", limit) or not 1 <= int(limit) <= 100:
        return problem(400, "InvalidRequest", "limit must be a whole number from 1 to 100.")

    stats.add("searches")
    # Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
    key = cache.key(checkIn, checkOut)
    cached = cache.get(key)
    if cached is not None:
        stats.add("cacheHits")
        numbers = cached.split(",")
        free = [room for room in rooms if room.number in numbers]
    else:
        stats.add("cacheMisses")
        with unit_of_work() as service:
            free = service.find_free_rooms(stay)
        cache.set(key, ",".join(sorted(room.number for room in free)))
    # Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
    rest = sorted((room for room in free if room.number > after), key=lambda room: room.number)
    page = rest[:int(limit)]
    return JSONResponse({
        "checkIn": checkIn, "checkOut": checkOut,
        "rooms": [{"number": room.number, "type": room.type} for room in page],
        "next": page[-1].number if len(rest) > len(page) else None,
    })
```

#### Java

```java
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
    // Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
    String key = cache.key(checkIn, checkOut);
    String cached = cache.get(key);
    if (cached != null) {
        stats.cacheHits.incrementAndGet();
        List<String> numbers = List.of(cached.split(","));
        return toPage(rooms.stream().filter(room -> numbers.contains(room.number())).toList(), checkIn, checkOut, size, after);
    }
    stats.cacheMisses.incrementAndGet();
    return unitOfWork.run(service -> {
        List<Room> free = service.findFreeRooms(stay);
        cache.set(key, free.stream().map(Room::number).sorted().collect(Collectors.joining(",")));
        return toPage(free, checkIn, checkOut, size, after);
    });
}

// Cursor pagination (L04): the free rooms after the last one the client has seen, in text order.
static ResponseEntity<?> toPage(List<Room> free, String checkIn, String checkOut, String size, String after) {
    List<Room> rest = free.stream().filter(room -> room.number().compareTo(after) > 0)
        .sorted(Comparator.comparing(Room::number)).toList();
    List<Room> page = rest.stream().limit(Integer.parseInt(size)).toList();
    String next = rest.size() > page.size() ? page.get(page.size() - 1).number() : null;
    return ResponseEntity.ok(new Availability(checkIn, checkOut, page.stream().map(room -> new RoomJson(room.number(), room.type())).toList(), next));
}
```

#### Go

```go
mux.HandleFunc("GET /rooms/availability", func(w http.ResponseWriter, r *http.Request) {
	query := r.URL.Query()
	checkIn, okIn := parseDate(query.Get("checkIn"))
	checkOut, okOut := parseDate(query.Get("checkOut"))
	if !okIn || !okOut {
		problem(w, http.StatusBadRequest, "InvalidDates", dateDetail)
		return
	}
	stay := reservations.DateRange{CheckIn: checkIn, CheckOut: checkOut}
	if !stay.IsValid() {
		problem(w, http.StatusBadRequest, "InvalidDates", "checkOut must be after checkIn.")
		return
	}
	limitText := query.Get("limit")
	if !query.Has("limit") {
		limitText = "20"
	}
	limit, _ := strconv.Atoi(limitText)
	if !regexp.MustCompile(`^[0-9]{1,3}$`).MatchString(limitText) || limit < 1 || limit > 100 {
		problem(w, http.StatusBadRequest, "InvalidRequest", "limit must be a whole number from 1 to 100.")
		return
	}

	stats.Searches.Add(1)
	// Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
	key, err := cache.Key(query.Get("checkIn"), query.Get("checkOut"))
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	cached, found, err := cache.Get(key)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	var free []rooms.Room
	if found {
		stats.CacheHits.Add(1)
		numbers := strings.Split(cached, ",")
		for _, room := range hotelRooms {
			if slices.Contains(numbers, room.Number) {
				free = append(free, room)
			}
		}
	} else {
		stats.CacheMisses.Add(1)
		if err := unitOfWork(func(service *reservations.BookingService) { free = service.FindFreeRooms(stay) }); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		numbers := []string{}
		for _, room := range free {
			numbers = append(numbers, room.Number)
		}
		slices.Sort(numbers)
		if err := cache.Set(key, strings.Join(numbers, ",")); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
	}
	// Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
	after := query.Get("after")
	rest := []rooms.Room{}
	for _, room := range free {
		if room.Number > after {
			rest = append(rest, room)
		}
	}
	slices.SortFunc(rest, func(a, b rooms.Room) int { return strings.Compare(a.Number, b.Number) })
	answer := availabilityJSON{CheckIn: query.Get("checkIn"), CheckOut: query.Get("checkOut"), Rooms: []roomJSON{}}
	for _, room := range rest[:min(limit, len(rest))] {
		answer.Rooms = append(answer.Rooms, roomJSON{Number: room.Number, Type: room.Type})
	}
	if len(rest) > limit {
		answer.Next = &answer.Rooms[limit-1].Number
	}
	write(w, http.StatusOK, "application/json", answer)
})
```

#### JavaScript

```javascript
app.get('/rooms/availability', async (request, response) => {
  const { checkIn, checkOut, limit = '20', after = '' } = request.query;
  const start = parseDate(checkIn);
  const end = parseDate(checkOut);
  if (!start || !end) return problem(response, 400, 'InvalidDates', dateDetail);
  const stay = new DateRange(start, end);
  if (!stay.isValid()) return problem(response, 400, 'InvalidDates', 'checkOut must be after checkIn.');
  if (typeof limit !== 'string' || !/^[0-9]{1,3}$/.test(limit) || Number(limit) < 1 || Number(limit) > 100) {
    return problem(response, 400, 'InvalidRequest', 'limit must be a whole number from 1 to 100.');
  }

  stats.searches++;
  // Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
  const key = await cache.key(checkIn, checkOut);
  const cached = await cache.get(key);
  let free;
  if (cached !== null) {
    stats.cacheHits++;
    const numbers = cached.split(',');
    free = rooms.filter(room => numbers.includes(room.number));
  } else {
    stats.cacheMisses++;
    free = await unitOfWork(service => service.findFreeRooms(stay));
    await cache.set(key, free.map(room => room.number).sort().join(','));
  }
  // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
  const rest = free.filter(room => room.number > after).sort((a, b) => (a.number < b.number ? -1 : 1));
  const page = rest.slice(0, Number(limit));
  response.json({
    checkIn, checkOut,
    rooms: page.map(room => ({ number: room.number, type: room.type })),
    next: rest.length > page.length ? page.at(-1).number : null,
  });
});
```

#### Dart

```dart
router.get('/rooms/availability', (Request request) async {
  final query = request.url.queryParameters;
  final checkIn = parseDate(query['checkIn']);
  final checkOut = parseDate(query['checkOut']);
  if (checkIn == null || checkOut == null) return problem(400, 'InvalidDates', dateDetail);
  final stay = DateRange(checkIn, checkOut);
  if (!stay.isValid()) return problem(400, 'InvalidDates', 'checkOut must be after checkIn.');
  final limitText = query['limit'] ?? '20';
  final limit = int.tryParse(limitText) ?? 0;
  if (!RegExp(r'^[0-9]{1,3}$').hasMatch(limitText) || limit < 1 || limit > 100) {
    return problem(400, 'InvalidRequest', 'limit must be a whole number from 1 to 100.');
  }

  stats.searches++;
  // Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
  final key = await cache.key(query['checkIn']!, query['checkOut']!);
  final cached = await cache.get(key);
  final List<Room> free;
  if (cached != null) {
    stats.cacheHits++;
    final numbers = cached.split(',');
    free = rooms.where((room) => numbers.contains(room.number)).toList();
  } else {
    stats.cacheMisses++;
    free = await unitOfWork((service) => service.findFreeRooms(stay));
    await cache.set(key, (free.map((room) => room.number).toList()..sort()).join(','));
  }
  // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
  final after = query['after'] ?? '';
  final rest = free.where((room) => room.number.compareTo(after) > 0).toList()
    ..sort((a, b) => a.number.compareTo(b.number));
  final page = rest.take(limit).toList();
  return json(200, {
    'checkIn': query['checkIn'],
    'checkOut': query['checkOut'],
    'rooms': [
      for (final room in page) {'number': room.number, 'type': room.type}
    ],
    'next': rest.length > page.length ? page.last.number : null,
  });
});
```

### Completed example 3.4 · the wiring connects to Redis

The Api function gets one more parameter, cache, after stats.

### The web app in the wiring

#### C#

```csharp
void RunApi()
{
    // The wiring for the web app: PostgreSQL and Redis from docker-compose.yml
    // (HOTEL_DB_PORT and HOTEL_REDIS_PORT only if 5432 or 6379 is taken).
    var builder = WebApplication.CreateBuilder(args);
    var connection = $"Host=localhost;Port={Environment.GetEnvironmentVariable("HOTEL_DB_PORT") ?? "5432"};Database=hotel;Username=hotel;Password=hotel";
    builder.Services.AddDbContextFactory<RoomsDb>(options => options.UseNpgsql(connection));
    builder.Services.AddDbContextFactory<ReservationsDb>(options => options.UseNpgsql(connection));
    var app = builder.Build();

    var rooms = new PostgresRoomCatalog(app.Services.GetRequiredService<IDbContextFactory<RoomsDb>>()).All();
    var databases = app.Services.GetRequiredService<IDbContextFactory<ReservationsDb>>();
    var email = new EmailNotificationSender();
    var stats = new Stats();
    // Connect stops here if Redis is not running.
    var redis = ConnectionMultiplexer.Connect($"localhost:{Environment.GetEnvironmentVariable("HOTEL_REDIS_PORT") ?? "6379"}");
    var cache = new AvailabilityCache(new RedisKeyValueStore(redis.GetDatabase()));

    // One request: a fresh store over its own DbContext, counted for GET /stats; save what changed at the end.
    IResult UnitOfWork(Func<BookingService, IResult> work)
    {
        using var db = databases.CreateDbContext();
        var store = new PostgresReservationStore(db, rooms);
        var result = work(new BookingService(rooms, new CountingReservationStore(store, stats), email));
        store.SaveChanges();
        return result;
    }

    HotelApi.Map(app, rooms, UnitOfWork, stats, cache);
    app.Run("http://localhost:5080");
}
```

#### Python

```python
def run_api() -> None:
    # The wiring for the web app: PostgreSQL and Redis from docker-compose.yml
    # (HOTEL_DB_PORT and HOTEL_REDIS_PORT only if 5432 or 6379 is taken).
    engine = create_engine(f"postgresql+psycopg://hotel:hotel@localhost:{os.getenv('HOTEL_DB_PORT', '5432')}/hotel")
    rooms = PostgresRoomCatalog(engine).all()
    email = EmailNotificationSender()
    stats = Stats()
    redis = Redis(host="localhost", port=int(os.getenv("HOTEL_REDIS_PORT", "6379")), decode_responses=True)
    redis.ping()  # stop here if Redis is not running
    cache = AvailabilityCache(RedisKeyValueStore(redis))

    @contextmanager
    def unit_of_work():
        # One request: a fresh store over its own session, counted for GET /stats; save what changed at the end.
        with Session(engine) as session:
            store = PostgresReservationStore(session, rooms)
            yield BookingService(rooms, CountingReservationStore(store, stats), email)
            store.save_changes()

    uvicorn.run(create_app(rooms, unit_of_work, stats, cache), host="localhost", port=5080)
```

#### Java

```java
// The wiring for the web app.
@Bean
HotelApi hotelApi(EntityManager db, TransactionTemplate transactions, StringRedisTemplate redis) {
    List<Room> rooms = new PostgresRoomCatalog(db).all();
    EmailNotificationSender email = new EmailNotificationSender();
    Stats stats = new Stats();
    try (RedisConnection connection = redis.getRequiredConnectionFactory().getConnection()) {
        connection.ping(); // stop here if Redis is not running
    }
    AvailabilityCache cache = new AvailabilityCache(new AvailabilityCache.RedisKeyValueStore(redis));
    // One request: a fresh store in its own transaction, counted for GET /stats; save what changed at the end.
    HotelApi.UnitOfWork unitOfWork = work -> transactions.execute(transaction -> {
        PostgresReservationStore store = new PostgresReservationStore(db, rooms);
        var result = work.apply(new BookingService(rooms, new CountingReservationStore(store, stats), email));
        store.saveChanges();
        return result;
    });
    return new HotelApi(rooms, unitOfWork, stats, cache);
}
```

#### Go

```go
func runAPI() {
	// The wiring for the web app: PostgreSQL and Redis from docker-compose.yml
	// (HOTEL_DB_PORT and HOTEL_REDIS_PORT only if 5432 or 6379 is taken).
	ctx := context.Background()
	pool, err := pgxpool.New(ctx, "postgres://hotel:hotel@localhost:"+cmp.Or(os.Getenv("HOTEL_DB_PORT"), "5432")+"/hotel")
	if err != nil {
		log.Fatal(err)
	}
	defer pool.Close()
	hotelRooms := rooms.NewPostgresRoomCatalog(pool).All()
	email := &notifications.EmailNotificationSender{}
	stats := &api.Stats{}
	client := redis.NewClient(&redis.Options{Addr: "localhost:" + cmp.Or(os.Getenv("HOTEL_REDIS_PORT"), "6379")})
	if err := client.Ping(ctx).Err(); err != nil {
		log.Fatal(err) // stop here if Redis is not running
	}
	cache := api.NewAvailabilityCache(api.NewRedisKeyValueStore(ctx, client))

	// One request: a fresh store, counted for GET /stats; save what changed at the end.
	unitOfWork := func(work func(service *reservations.BookingService)) error {
		store := reservations.NewPostgresReservationStore(ctx, pool, hotelRooms)
		work(reservations.NewBookingService(hotelRooms, api.NewCountingReservationStore(store, stats), email, 0))
		return store.SaveChanges()
	}

	fmt.Println("Listening on http://localhost:5080")
	log.Fatal(http.ListenAndServe("localhost:5080", api.New(hotelRooms, unitOfWork, stats, cache)))
}
```

#### JavaScript

```javascript
async function runApi() {
  // The wiring for the web app: PostgreSQL and Redis from docker-compose.yml
  // (HOTEL_DB_PORT and HOTEL_REDIS_PORT only if 5432 or 6379 is taken).
  const pool = new pg.Pool({ host: 'localhost', port: Number(process.env.HOTEL_DB_PORT ?? 5432), database: 'hotel', user: 'hotel', password: 'hotel' });
  const rooms = await new PostgresRoomCatalog(pool).all();
  const email = new EmailNotificationSender();
  const stats = new Stats();
  const redis = await createClient({ socket: { host: 'localhost', port: Number(process.env.HOTEL_REDIS_PORT ?? 6379) } })
    .on('error', error => console.error(error)) // node-redis ends the program on an error nobody listens to
    .connect();
  const cache = new AvailabilityCache(new RedisKeyValueStore(redis));

  // One request: a fresh store, counted for GET /stats; save what changed at the end.
  const unitOfWork = async work => {
    const store = new PostgresReservationStore(pool, rooms);
    const result = await work(new BookingService(rooms, new CountingReservationStore(store, stats), email));
    await store.saveChanges();
    return result;
  };

  createApi(rooms, unitOfWork, stats, cache).listen(5080, 'localhost', () => console.log('Listening on http://localhost:5080'));
}
```

#### Dart

```dart
Future<void> runApi() async {
  // The wiring for the web app: PostgreSQL and Redis from docker-compose.yml
  // (HOTEL_DB_PORT and HOTEL_REDIS_PORT only if 5432 or 6379 is taken).
  final port = int.parse(Platform.environment['HOTEL_DB_PORT'] ?? '5432');
  final pool = Pool.withEndpoints([
    Endpoint(host: 'localhost', port: port, database: 'hotel', username: 'hotel', password: 'hotel')
  ], settings: PoolSettings(sslMode: SslMode.disable));
  final rooms = await PostgresRoomCatalog(pool).all();
  final email = EmailNotificationSender();
  final stats = Stats();
  final redisPort = int.parse(Platform.environment['HOTEL_REDIS_PORT'] ?? '6379');
  // One connection for the whole app; connecting stops here if Redis is not running.
  final redis = await RedisConnection().connect('localhost', redisPort);
  final cache = AvailabilityCache(RedisKeyValueStore(redis));

  // One request: a fresh store, counted for GET /stats; save what changed at the end.
  Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async {
    final store = PostgresReservationStore(pool, rooms);
    final result = await work(BookingService(rooms, CountingReservationStore(store, stats), email));
    await store.saveChanges();
    return result;
  }

  await serve(createApi(rooms, unitOfWork, stats, cache), 'localhost', 5080);
  print('Listening on http://localhost:5080');
}
```

### Completed example 3.5 · the API tests get a cache in a dictionary

The tests must not need Redis: the helper gives the cache a dictionary that keeps the three commands’ promise.

**The cache tests use a dictionary instead of Redis** In short: The tests give AvailabilityCache a dictionary that keeps the three commands’ promise, so they run in a second without Redis. It ignores the TTL: a test takes less than a second, far below 30.

1. get looks the key up in the dictionary.
2. set stores the value; increment adds 1 to the number under the key.
3. Everything else in the test is the real code: the endpoints, the key, the version.

C#:

```text
// Redis's three commands on a dictionary. The tests run in a second, so the 30 s TTL never ends.
private class InMemoryKeyValueStore : IKeyValueStore
{
    private readonly Dictionary<string, string> _values = [];
    public string? Get(string key) => _values.GetValueOrDefault(key);
    public void Set(string key, string value, TimeSpan ttl) => _values[key] = value;
    public void Increment(string key) => _values[key] = (int.Parse(_values.GetValueOrDefault(key, "0")) + 1).ToString();
}
```

Python:

```text
class InMemoryKeyValueStore:
    """Redis's three commands on a dictionary. The tests run in a second, so the 30 s TTL never ends."""

    def __init__(self) -> None:
        self._values: dict[str, str] = {}

    def get(self, key: str) -> str | None:
        return self._values.get(key)

    def set(self, key: str, value: str, ttl_seconds: int) -> None:
        self._values[key] = value

    def increment(self, key: str) -> None:
        self._values[key] = str(int(self._values.get(key, "0")) + 1)
```

Java:

```text
// Redis's three commands on a map. The tests run in a second, so the 30 s TTL never ends.
static class InMemoryKeyValueStore implements AvailabilityCache.KeyValueStore {
    private final Map<String, String> values = new HashMap<>();

    @Override
    public String get(String key) {
        return values.get(key);
    }

    @Override
    public void set(String key, String value, int ttlSeconds) {
        values.put(key, value);
    }

    @Override
    public void increment(String key) {
        values.put(key, String.valueOf(Long.parseLong(values.getOrDefault(key, "0")) + 1));
    }
}
```

Go:

```text
// inMemoryKeyValueStore is Redis's three commands on a map. The tests run in a second, so the 30 s TTL never ends.
type inMemoryKeyValueStore map[string]string

func (store inMemoryKeyValueStore) Get(key string) (string, bool, error) {
	value, found := store[key]
	return value, found, nil
}

func (store inMemoryKeyValueStore) Set(key, value string, ttl time.Duration) error {
	store[key] = value
	return nil
}

func (store inMemoryKeyValueStore) Increment(key string) error {
	number, _ := strconv.Atoi(store[key])
	store[key] = strconv.Itoa(number + 1)
	return nil
}
```

JavaScript:

```text
// Redis's three commands on a Map. The tests run in a second, so the 30 s TTL never ends.
class InMemoryKeyValueStore {
  #values = new Map();

  get(key) {
    return this.#values.get(key) ?? null;
  }

  set(key, value) {
    this.#values.set(key, value);
  }

  increment(key) {
    this.#values.set(key, String(Number(this.#values.get(key) ?? '0') + 1));
  }
}
```

Dart:

```text
// Redis's three commands on a map. The tests run in a second, so the 30 s TTL never ends.
class InMemoryKeyValueStore implements KeyValueStore {
  final Map<String, String> _values = {};

  @override
  Future<String?> get(String key) async => _values[key];

  @override
  Future<void> set(String key, String value, int ttlSeconds) async {
    _values[key] = value;
  }

  @override
  Future<void> increment(String key) async {
    _values[key] = '${int.parse(_values[key] ?? '0') + 1}';
  }
}
```

### The test helper and the dictionary

#### C#

```csharp
// Redis's three commands on a dictionary. The tests run in a second, so the 30 s TTL never ends.
private class InMemoryKeyValueStore : IKeyValueStore
{
    private readonly Dictionary<string, string> _values = [];
    public string? Get(string key) => _values.GetValueOrDefault(key);
    public void Set(string key, string value, TimeSpan ttl) => _values[key] = value;
    public void Increment(string key) => _values[key] = (int.Parse(_values.GetValueOrDefault(key, "0")) + 1).ToString();
}

// The web app with the in-memory store and cache, on ASP.NET Core's test server: the client sends requests without a network.
private static async Task<HttpClient> NewClient()
{
    var stats = new Stats();
    var service = new BookingService(Rooms, new CountingReservationStore(new InMemoryReservationStore(), stats), new EmailNotificationSender());
    var builder = WebApplication.CreateBuilder();
    builder.WebHost.UseTestServer();
    var app = builder.Build();
    HotelApi.Map(app, Rooms, work => work(service), stats, new AvailabilityCache(new InMemoryKeyValueStore())); // a list in memory has nothing to save at the end of a request
    await app.StartAsync();
    return app.GetTestClient();
}
```

#### Python

```python
class InMemoryKeyValueStore:
    """Redis's three commands on a dictionary. The tests run in a second, so the 30 s TTL never ends."""

    def __init__(self) -> None:
        self._values: dict[str, str] = {}

    def get(self, key: str) -> str | None:
        return self._values.get(key)

    def set(self, key: str, value: str, ttl_seconds: int) -> None:
        self._values[key] = value

    def increment(self, key: str) -> None:
        self._values[key] = str(int(self._values.get(key, "0")) + 1)

def new_client() -> TestClient:
    """The web app with the in-memory store and cache; FastAPI's TestClient sends the requests without a network."""
    stats = Stats()
    service = BookingService([R7, R8], CountingReservationStore(InMemoryReservationStore(), stats), EmailNotificationSender())

    @contextmanager
    def unit_of_work():
        yield service  # a list in memory has nothing to save at the end of a request

    return TestClient(create_app([R7, R8], unit_of_work, stats, AvailabilityCache(InMemoryKeyValueStore())))
```

#### Java

```java
// Redis's three commands on a map. The tests run in a second, so the 30 s TTL never ends.
static class InMemoryKeyValueStore implements AvailabilityCache.KeyValueStore {
    private final Map<String, String> values = new HashMap<>();

    @Override
    public String get(String key) {
        return values.get(key);
    }

    @Override
    public void set(String key, String value, int ttlSeconds) {
        values.put(key, value);
    }

    @Override
    public void increment(String key) {
        values.put(key, String.valueOf(Long.parseLong(values.getOrDefault(key, "0")) + 1));
    }
}

// The web app with the in-memory store and cache; MockMvc sends requests to it without a network.
static MockMvc newApp() {
    Stats stats = new Stats();
    BookingService service = new BookingService(ROOMS, new CountingReservationStore(new InMemoryReservationStore(), stats), new EmailNotificationSender());
    // A list in memory has nothing to save at the end of a request.
    return MockMvcBuilders.standaloneSetup(new HotelApi(ROOMS, work -> work.apply(service), stats, new AvailabilityCache(new InMemoryKeyValueStore()))).build();
}
```

#### Go

```go
// inMemoryKeyValueStore is Redis's three commands on a map. The tests run in a second, so the 30 s TTL never ends.
type inMemoryKeyValueStore map[string]string

func (store inMemoryKeyValueStore) Get(key string) (string, bool, error) {
	value, found := store[key]
	return value, found, nil
}

func (store inMemoryKeyValueStore) Set(key, value string, ttl time.Duration) error {
	store[key] = value
	return nil
}

func (store inMemoryKeyValueStore) Increment(key string) error {
	number, _ := strconv.Atoi(store[key])
	store[key] = strconv.Itoa(number + 1)
	return nil
}

// newApp is the web app with the in-memory store and cache; httptest sends requests to it without a network.
func newApp() http.Handler {
	hotelRooms := []rooms.Room{{Number: "R7", Type: "Standard"}, {Number: "R8", Type: "Deluxe"}}
	stats := &Stats{}
	store := NewCountingReservationStore(&reservations.InMemoryReservationStore{}, stats)
	service := reservations.NewBookingService(hotelRooms, store, &notifications.EmailNotificationSender{}, 0)
	return New(hotelRooms, func(work func(*reservations.BookingService)) error {
		work(service)
		return nil // a list in memory has nothing to save at the end of a request
	}, stats, NewAvailabilityCache(inMemoryKeyValueStore{}))
}
```

#### JavaScript

```javascript
// Redis's three commands on a Map. The tests run in a second, so the 30 s TTL never ends.
class InMemoryKeyValueStore {
  #values = new Map();

  get(key) {
    return this.#values.get(key) ?? null;
  }

  set(key, value) {
    this.#values.set(key, value);
  }

  increment(key) {
    this.#values.set(key, String(Number(this.#values.get(key) ?? '0') + 1));
  }
}

// The web app with the in-memory store and cache, on a free port of this laptop. send() makes one request.
async function startApp(t) {
  const stats = new Stats();
  const service = new BookingService(rooms, new CountingReservationStore(new InMemoryReservationStore(), stats), new EmailNotificationSender());
  const cache = new AvailabilityCache(new InMemoryKeyValueStore());
  // A list in memory has nothing to save at the end of a request.
  const server = createApi(rooms, work => work(service), stats, cache).listen(0, 'localhost');
  await once(server, 'listening');
  t.after(() => server.close());
  return (method, path, body) => fetch(`http://localhost:${server.address().port}${path}`, {
    method, body: body && JSON.stringify(body), headers: { 'Content-Type': 'application/json', Connection: 'close' },
  });
}
```

#### Dart

```dart
// Redis's three commands on a map. The tests run in a second, so the 30 s TTL never ends.
class InMemoryKeyValueStore implements KeyValueStore {
  final Map<String, String> _values = {};

  @override
  Future<String?> get(String key) async => _values[key];

  @override
  Future<void> set(String key, String value, int ttlSeconds) async {
    _values[key] = value;
  }

  @override
  Future<void> increment(String key) async {
    _values[key] = '${int.parse(_values[key] ?? '0') + 1}';
  }
}

// The web app with the in-memory store and cache. A shelf app is a function from Request to
// Response, so the tests call it directly, without a network.
Handler newApp() {
  final stats = Stats();
  final store = CountingReservationStore(InMemoryReservationStore(), stats);
  final service = BookingService(rooms, store, EmailNotificationSender());
  // A list in memory has nothing to save at the end of a request.
  Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async => work(service);
  return createApi(rooms, unitOfWork, stats, AvailabilityCache(InMemoryKeyValueStore()));
}
```

### Completed example: one search through the cache

### Diagram: One search through the cache: a miss, then a hit

GET /rooms/availability for 12–14 Nov on your laptop. The endpoint builds the key and looks in Redis. On a miss the unit of work runs FindFreeRooms, which asks PostgreSQL once per room, and the endpoint keeps the answer for 30 seconds. The next search is a hit and stops at Redis. A booking raises the version.

Diagram as text:

The measurement tool (or curl.exe) sends GET /rooms/availability for 12–14 November to http://localhost:5080. The search endpoint adds 1 to searches, reads availability:version from Redis and builds the key availability:v0:2026-11-12:2026-11-14, then asks Redis for that key. A miss: there is no copy. The endpoint adds 1 to cacheMisses and runs FindFreeRooms through the unit of work. FindFreeRooms asks the store about R7 and about R8. The counting store adds 1 to databaseQueries for each and passes the question on; the PostgreSQL store sends one SELECT each. The endpoint keeps the answer: SET availability:v0:2026-11-12:2026-11-14 "R7,R8" EX 30. The next search within 30 seconds is a hit: arrows 1 and 2 only. The endpoint adds 1 to cacheHits and builds the rooms from the copy; PostgreSQL is not asked. After a successful booking or cancel, the endpoint sends INCR availability:version, so the next search uses the key …:v1:… and misses.

**Step by step:** Follow the numbers: 1 to 6 are one search that misses. A hit uses only 1 and 2. Arrow 7 belongs to POST and DELETE. The left column is the app; the right column is where data lives.

1. Yellow box “Measurement tool or curl.exe” and arrow 1: the request. The measurement tool sends 1,000 of them, one after another, over one connection.

   C#:
   
   ```text
   // tools/measure.cs
   // Sends 1,000 searches to the running app, one after another, and prints how long they took
   // and what they cost: the difference of GET /stats before and after.
   //
   // dotnet run tools/measure.cs                         -> 12-14 Nov
   // dotnet run tools/measure.cs 2026-12-31 2027-01-02   -> other dates
   using System.Diagnostics;
   using System.Globalization;
   using System.Text.Json;
   
   const int searches = 1000;
   string[] names = ["searches", "databaseQueries", "cacheHits", "cacheMisses"];
   var (checkIn, checkOut) = args.Length >= 2 ? (args[0], args[1]) : ("2026-11-12", "2026-11-14");
   var culture = CultureInfo.InvariantCulture;
   
   // One client: it keeps the connection open and reuses it for every request.
   using var client = new HttpClient { BaseAddress = new Uri("http://localhost:5080") };
   
   async Task<string> Get(string path)
   {
       var response = await client.GetAsync(path);
       var body = await response.Content.ReadAsStringAsync();
       if ((int)response.StatusCode != 200)
       {
           Console.Error.WriteLine($"GET {path} answered {(int)response.StatusCode}: {body}");
           Environment.Exit(1);
       }
       return body;
   }
   
   async Task<JsonElement> Stats() => JsonDocument.Parse(await Get("/stats")).RootElement;
   
   var before = await Stats();
   var times = new List<double>();
   var started = Stopwatch.StartNew();
   for (var i = 0; i < searches; i++)
   {
       var start = Stopwatch.GetTimestamp();
       await Get($"/rooms/availability?checkIn={checkIn}&checkOut={checkOut}");
       times.Add(Stopwatch.GetElapsedTime(start).TotalMilliseconds);
   }
   var total = started.Elapsed.TotalSeconds;
   var after = await Stats();
   
   times.Sort();
   var p95 = times[(int)Math.Ceiling(0.95 * searches) - 1]; // 950 of the 1,000 searches were this fast or faster
   var difference = string.Join(",", names.Select(name => $"\"{name}\":{after.GetProperty(name).GetInt64() - before.GetProperty(name).GetInt64()}"));
   Console.WriteLine($"{searches} searches for {checkIn} to {checkOut}");
   Console.WriteLine($"Average: {times.Average().ToString("F1", culture)} ms");
   Console.WriteLine($"p95: {p95.ToString("F1", culture)} ms");
   Console.WriteLine($"Total: {total.ToString("F1", culture)} s");
   Console.WriteLine($"GET /stats difference: {{{difference}}}");
   ```
   
   Python:
   
   ```text
   # tools/measure.py
   """Sends 1,000 searches to the running app, one after another, and prints how long they took
   and what they cost: the difference of GET /stats before and after.
   
   python tools/measure.py                         -> 12-14 Nov
   python tools/measure.py 2026-12-31 2027-01-02   -> other dates
   """
   import http.client
   import json
   import math
   import sys
   import time
   
   SEARCHES = 1000
   NAMES = ("searches", "databaseQueries", "cacheHits", "cacheMisses")
   
   check_in, check_out = sys.argv[1:3] if len(sys.argv) >= 3 else ("2026-11-12", "2026-11-14")
   connection = http.client.HTTPConnection("localhost", 5080)  # one connection for every request
   
   
   def get(path: str) -> str:
       connection.request("GET", path)
       response = connection.getresponse()
       body = response.read().decode("utf-8")
       if response.status != 200:
           sys.exit(f"GET {path} answered {response.status}: {body}")
       return body
   
   
   before = json.loads(get("/stats"))
   times = []
   started = time.perf_counter()
   for _ in range(SEARCHES):
       start = time.perf_counter()
       get(f"/rooms/availability?checkIn={check_in}&checkOut={check_out}")
       times.append((time.perf_counter() - start) * 1000)
   total = time.perf_counter() - started
   after = json.loads(get("/stats"))
   
   times.sort()
   p95 = times[math.ceil(0.95 * SEARCHES) - 1]  # 950 of the 1,000 searches were this fast or faster
   difference = ",".join(f'"{name}":{after[name] - before[name]}' for name in NAMES)
   print(f"{SEARCHES} searches for {check_in} to {check_out}")
   print(f"Average: {sum(times) / SEARCHES:.1f} ms")
   print(f"p95: {p95:.1f} ms")
   print(f"Total: {total:.1f} s")
   print(f"GET /stats difference: {{{difference}}}")
   ```
   
   Java:
   
   ```text
   // tools/Measure.java
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
   ```
   
   Go:
   
   ```text
   // tools/measure/main.go
   // Sends 1,000 searches to the running app, one after another, and prints how long they took
   // and what they cost: the difference of GET /stats before and after.
   //
   // go run ./tools/measure                         -> 12-14 Nov
   // go run ./tools/measure 2026-12-31 2027-01-02   -> other dates
   package main
   
   import (
   	"encoding/json"
   	"fmt"
   	"io"
   	"log"
   	"math"
   	"net/http"
   	"os"
   	"slices"
   	"strings"
   	"time"
   )
   
   const searches = 1000
   
   var names = []string{"searches", "databaseQueries", "cacheHits", "cacheMisses"}
   
   // One client: it keeps the connection open and reuses it for every request.
   var client = &http.Client{}
   
   func get(path string) []byte {
   	response, err := client.Get("http://localhost:5080" + path)
   	if err != nil {
   		log.Fatal(err)
   	}
   	defer response.Body.Close()
   	body, _ := io.ReadAll(response.Body) // read it all, so the connection can be reused
   	if response.StatusCode != http.StatusOK {
   		log.Fatalf("GET %s answered %d: %s", path, response.StatusCode, body)
   	}
   	return body
   }
   
   func stats() map[string]int64 {
   	var counters map[string]int64
   	json.Unmarshal(get("/stats"), &counters)
   	return counters
   }
   
   func main() {
   	checkIn, checkOut := "2026-11-12", "2026-11-14"
   	if len(os.Args) >= 3 {
   		checkIn, checkOut = os.Args[1], os.Args[2]
   	}
   
   	before := stats()
   	times := make([]float64, 0, searches)
   	started := time.Now()
   	for range searches {
   		start := time.Now()
   		get("/rooms/availability?checkIn=" + checkIn + "&checkOut=" + checkOut)
   		times = append(times, float64(time.Since(start).Microseconds())/1000)
   	}
   	total := time.Since(started).Seconds()
   	after := stats()
   
   	slices.Sort(times)
   	p95 := times[int(math.Ceil(0.95*searches))-1] // 950 of the 1,000 searches were this fast or faster
   	sum := 0.0
   	for _, t := range times {
   		sum += t
   	}
   	difference := []string{}
   	for _, name := range names {
   		difference = append(difference, fmt.Sprintf("%q:%d", name, after[name]-before[name]))
   	}
   	fmt.Printf("%d searches for %s to %s\n", searches, checkIn, checkOut)
   	fmt.Printf("Average: %.1f ms\n", sum/searches)
   	fmt.Printf("p95: %.1f ms\n", p95)
   	fmt.Printf("Total: %.1f s\n", total)
   	fmt.Printf("GET /stats difference: {%s}\n", strings.Join(difference, ","))
   }
   ```
   
   JavaScript:
   
   ```text
   // tools/measure.js
   // Sends 1,000 searches to the running app, one after another, and prints how long they took
   // and what they cost: the difference of GET /stats before and after.
   //
   // node tools/measure.js                         -> 12-14 Nov
   // node tools/measure.js 2026-12-31 2027-01-02   -> other dates
   import http from 'node:http';
   
   const searches = 1000;
   const names = ['searches', 'databaseQueries', 'cacheHits', 'cacheMisses'];
   const [checkIn = '2026-11-12', checkOut = '2026-11-14'] = process.argv.slice(2);
   
   // One connection, kept open and reused for every request.
   const agent = new http.Agent({ keepAlive: true, maxSockets: 1 });
   const get = path => new Promise((resolve, reject) => {
     http.get({ host: 'localhost', port: 5080, path, agent }, response => {
       let body = '';
       response.setEncoding('utf8');
       response.on('data', chunk => { body += chunk; });
       response.on('end', () => {
         if (response.statusCode !== 200) {
           console.error(`GET ${path} answered ${response.statusCode}: ${body}`);
           process.exit(1);
         }
         resolve(body);
       });
     }).on('error', reject);
   });
   
   const before = JSON.parse(await get('/stats'));
   const times = [];
   const started = performance.now();
   for (let i = 0; i < searches; i++) {
     const start = performance.now();
     await get(`/rooms/availability?checkIn=${checkIn}&checkOut=${checkOut}`);
     times.push(performance.now() - start);
   }
   const total = (performance.now() - started) / 1000;
   const after = JSON.parse(await get('/stats'));
   
   times.sort((a, b) => a - b);
   const p95 = times[Math.ceil(0.95 * searches) - 1]; // 950 of the 1,000 searches were this fast or faster
   const average = times.reduce((sum, time) => sum + time, 0) / searches;
   const difference = names.map(name => `"${name}":${after[name] - before[name]}`).join(',');
   console.log(`${searches} searches for ${checkIn} to ${checkOut}`);
   console.log(`Average: ${average.toFixed(1)} ms`);
   console.log(`p95: ${p95.toFixed(1)} ms`);
   console.log(`Total: ${total.toFixed(1)} s`);
   console.log(`GET /stats difference: {${difference}}`);
   agent.destroy();
   ```
   
   Dart:
   
   ```text
   // tool/measure.dart
   // Sends 1,000 searches to the running app, one after another, and prints how long they took
   // and what they cost: the difference of GET /stats before and after.
   //
   // dart run tool/measure.dart                         -> 12-14 Nov
   // dart run tool/measure.dart 2026-12-31 2027-01-02   -> other dates
   import 'dart:convert';
   import 'dart:io';
   
   const searches = 1000;
   const names = ['searches', 'databaseQueries', 'cacheHits', 'cacheMisses'];
   
   final client = HttpClient(); // one client, so every request goes over the same open connection
   
   Future<String> get(String path) async {
     final request = await client.getUrl(Uri.parse('http://localhost:5080$path'));
     final response = await request.close();
     final body = await response.transform(utf8.decoder).join();
     if (response.statusCode != 200) {
       stderr.writeln('GET $path answered ${response.statusCode}: $body');
       exit(1);
     }
     return body;
   }
   
   Future<void> main(List<String> args) async {
     final checkIn = args.length >= 2 ? args[0] : '2026-11-12';
     final checkOut = args.length >= 2 ? args[1] : '2026-11-14';
   
     final before = jsonDecode(await get('/stats')) as Map<String, dynamic>;
     final times = <double>[];
     final total = Stopwatch()..start();
     for (var i = 0; i < searches; i++) {
       final watch = Stopwatch()..start();
       await get('/rooms/availability?checkIn=$checkIn&checkOut=$checkOut');
       times.add(watch.elapsedMicroseconds / 1000);
     }
     final seconds = total.elapsedMicroseconds / 1000000;
     final after = jsonDecode(await get('/stats')) as Map<String, dynamic>;
     client.close();
   
     times.sort();
     // 950 of the 1,000 searches were this fast or faster.
     final p95 = times[(0.95 * searches).ceil() - 1];
     final average = times.reduce((a, b) => a + b) / searches;
     final difference =
         names.map((name) => '"$name":${(after[name] as int) - (before[name] as int)}').join(',');
     print('$searches searches for $checkIn to $checkOut');
     print('Average: ${average.toStringAsFixed(1)} ms');
     print('p95: ${p95.toStringAsFixed(1)} ms');
     print('Total: ${seconds.toStringAsFixed(1)} s');
     print('GET /stats difference: {$difference}');
   }
   ```
2. White box “Api: the search endpoint”: it checks the dates and limit exactly as in P07, then adds 1 to searches. Here is the whole endpoint:

   C#:
   
   ```text
   app.MapGet("/rooms/availability", (string? checkIn, string? checkOut, string? limit, string? after) =>
   {
       if (ParseDate(checkIn) is not { } start || ParseDate(checkOut) is not { } end) return Problem(400, "InvalidDates", DateDetail);
       var stay = new DateRange(start, end);
       if (!stay.IsValid()) return Problem(400, "InvalidDates", "checkOut must be after checkIn.");
       limit ??= "20";
       if (!Regex.IsMatch(limit, @"^[0-9]{1,3}\z") || int.Parse(limit) is < 1 or > 100)
           return Problem(400, "InvalidRequest", "limit must be a whole number from 1 to 100.");
   
       stats.AddSearch();
       // Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
       var key = cache.Key(checkIn!, checkOut!);
       if (cache.Get(key) is { } cached)
       {
           stats.AddCacheHit();
           var numbers = cached.Split(',');
           return Page(rooms.Where(room => numbers.Contains(room.Number)));
       }
       stats.AddCacheMiss();
       return unitOfWork(service =>
       {
           var free = service.FindFreeRooms(stay);
           cache.Set(key, string.Join(",", free.Select(room => room.Number).Order(StringComparer.Ordinal)));
           return Page(free);
       });
   
       IResult Page(IEnumerable<Room> free)
       {
           // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
           var rest = free.Where(room => string.CompareOrdinal(room.Number, after ?? "") > 0)
               .OrderBy(room => room.Number, StringComparer.Ordinal).ToList();
           var page = rest.Take(int.Parse(limit)).ToList();
           return Results.Json(new
           {
               checkIn, checkOut,
               rooms = page.Select(room => new { number = room.Number, type = room.Type }),
               next = rest.Count > page.Count ? page[^1].Number : null,
           });
       }
   });
   ```
   
   Python:
   
   ```text
   @app.get("/rooms/availability")
   def find_free_rooms(checkIn: str = "", checkOut: str = "", limit: str = "20", after: str = "") -> JSONResponse:
       check_in, check_out = parse_date(checkIn), parse_date(checkOut)
       if check_in is None or check_out is None:
           return problem(400, "InvalidDates", DATE_DETAIL)
       stay = DateRange(check_in, check_out)
       if not stay.is_valid():
           return problem(400, "InvalidDates", "checkOut must be after checkIn.")
       if not re.fullmatch(r"[0-9]{1,3}", limit) or not 1 <= int(limit) <= 100:
           return problem(400, "InvalidRequest", "limit must be a whole number from 1 to 100.")
   
       stats.add("searches")
       # Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
       key = cache.key(checkIn, checkOut)
       cached = cache.get(key)
       if cached is not None:
           stats.add("cacheHits")
           numbers = cached.split(",")
           free = [room for room in rooms if room.number in numbers]
       else:
           stats.add("cacheMisses")
           with unit_of_work() as service:
               free = service.find_free_rooms(stay)
           cache.set(key, ",".join(sorted(room.number for room in free)))
       # Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
       rest = sorted((room for room in free if room.number > after), key=lambda room: room.number)
       page = rest[:int(limit)]
       return JSONResponse({
           "checkIn": checkIn, "checkOut": checkOut,
           "rooms": [{"number": room.number, "type": room.type} for room in page],
           "next": page[-1].number if len(rest) > len(page) else None,
       })
   ```
   
   Java:
   
   ```text
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
       // Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
       String key = cache.key(checkIn, checkOut);
       String cached = cache.get(key);
       if (cached != null) {
           stats.cacheHits.incrementAndGet();
           List<String> numbers = List.of(cached.split(","));
           return toPage(rooms.stream().filter(room -> numbers.contains(room.number())).toList(), checkIn, checkOut, size, after);
       }
       stats.cacheMisses.incrementAndGet();
       return unitOfWork.run(service -> {
           List<Room> free = service.findFreeRooms(stay);
           cache.set(key, free.stream().map(Room::number).sorted().collect(Collectors.joining(",")));
           return toPage(free, checkIn, checkOut, size, after);
       });
   }
   
   // Cursor pagination (L04): the free rooms after the last one the client has seen, in text order.
   static ResponseEntity<?> toPage(List<Room> free, String checkIn, String checkOut, String size, String after) {
       List<Room> rest = free.stream().filter(room -> room.number().compareTo(after) > 0)
           .sorted(Comparator.comparing(Room::number)).toList();
       List<Room> page = rest.stream().limit(Integer.parseInt(size)).toList();
       String next = rest.size() > page.size() ? page.get(page.size() - 1).number() : null;
       return ResponseEntity.ok(new Availability(checkIn, checkOut, page.stream().map(room -> new RoomJson(room.number(), room.type())).toList(), next));
   }
   ```
   
   Go:
   
   ```text
   mux.HandleFunc("GET /rooms/availability", func(w http.ResponseWriter, r *http.Request) {
   	query := r.URL.Query()
   	checkIn, okIn := parseDate(query.Get("checkIn"))
   	checkOut, okOut := parseDate(query.Get("checkOut"))
   	if !okIn || !okOut {
   		problem(w, http.StatusBadRequest, "InvalidDates", dateDetail)
   		return
   	}
   	stay := reservations.DateRange{CheckIn: checkIn, CheckOut: checkOut}
   	if !stay.IsValid() {
   		problem(w, http.StatusBadRequest, "InvalidDates", "checkOut must be after checkIn.")
   		return
   	}
   	limitText := query.Get("limit")
   	if !query.Has("limit") {
   		limitText = "20"
   	}
   	limit, _ := strconv.Atoi(limitText)
   	if !regexp.MustCompile(`^[0-9]{1,3}$`).MatchString(limitText) || limit < 1 || limit > 100 {
   		problem(w, http.StatusBadRequest, "InvalidRequest", "limit must be a whole number from 1 to 100.")
   		return
   	}
   
   	stats.Searches.Add(1)
   	// Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
   	key, err := cache.Key(query.Get("checkIn"), query.Get("checkOut"))
   	if err != nil {
   		http.Error(w, err.Error(), http.StatusInternalServerError)
   		return
   	}
   	cached, found, err := cache.Get(key)
   	if err != nil {
   		http.Error(w, err.Error(), http.StatusInternalServerError)
   		return
   	}
   	var free []rooms.Room
   	if found {
   		stats.CacheHits.Add(1)
   		numbers := strings.Split(cached, ",")
   		for _, room := range hotelRooms {
   			if slices.Contains(numbers, room.Number) {
   				free = append(free, room)
   			}
   		}
   	} else {
   		stats.CacheMisses.Add(1)
   		if err := unitOfWork(func(service *reservations.BookingService) { free = service.FindFreeRooms(stay) }); err != nil {
   			http.Error(w, err.Error(), http.StatusInternalServerError)
   			return
   		}
   		numbers := []string{}
   		for _, room := range free {
   			numbers = append(numbers, room.Number)
   		}
   		slices.Sort(numbers)
   		if err := cache.Set(key, strings.Join(numbers, ",")); err != nil {
   			http.Error(w, err.Error(), http.StatusInternalServerError)
   			return
   		}
   	}
   	// Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
   	after := query.Get("after")
   	rest := []rooms.Room{}
   	for _, room := range free {
   		if room.Number > after {
   			rest = append(rest, room)
   		}
   	}
   	slices.SortFunc(rest, func(a, b rooms.Room) int { return strings.Compare(a.Number, b.Number) })
   	answer := availabilityJSON{CheckIn: query.Get("checkIn"), CheckOut: query.Get("checkOut"), Rooms: []roomJSON{}}
   	for _, room := range rest[:min(limit, len(rest))] {
   		answer.Rooms = append(answer.Rooms, roomJSON{Number: room.Number, Type: room.Type})
   	}
   	if len(rest) > limit {
   		answer.Next = &answer.Rooms[limit-1].Number
   	}
   	write(w, http.StatusOK, "application/json", answer)
   })
   ```
   
   JavaScript:
   
   ```text
   app.get('/rooms/availability', async (request, response) => {
     const { checkIn, checkOut, limit = '20', after = '' } = request.query;
     const start = parseDate(checkIn);
     const end = parseDate(checkOut);
     if (!start || !end) return problem(response, 400, 'InvalidDates', dateDetail);
     const stay = new DateRange(start, end);
     if (!stay.isValid()) return problem(response, 400, 'InvalidDates', 'checkOut must be after checkIn.');
     if (typeof limit !== 'string' || !/^[0-9]{1,3}$/.test(limit) || Number(limit) < 1 || Number(limit) > 100) {
       return problem(response, 400, 'InvalidRequest', 'limit must be a whole number from 1 to 100.');
     }
   
     stats.searches++;
     // Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
     const key = await cache.key(checkIn, checkOut);
     const cached = await cache.get(key);
     let free;
     if (cached !== null) {
       stats.cacheHits++;
       const numbers = cached.split(',');
       free = rooms.filter(room => numbers.includes(room.number));
     } else {
       stats.cacheMisses++;
       free = await unitOfWork(service => service.findFreeRooms(stay));
       await cache.set(key, free.map(room => room.number).sort().join(','));
     }
     // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
     const rest = free.filter(room => room.number > after).sort((a, b) => (a.number < b.number ? -1 : 1));
     const page = rest.slice(0, Number(limit));
     response.json({
       checkIn, checkOut,
       rooms: page.map(room => ({ number: room.number, type: room.type })),
       next: rest.length > page.length ? page.at(-1).number : null,
     });
   });
   ```
   
   Dart:
   
   ```text
   router.get('/rooms/availability', (Request request) async {
     final query = request.url.queryParameters;
     final checkIn = parseDate(query['checkIn']);
     final checkOut = parseDate(query['checkOut']);
     if (checkIn == null || checkOut == null) return problem(400, 'InvalidDates', dateDetail);
     final stay = DateRange(checkIn, checkOut);
     if (!stay.isValid()) return problem(400, 'InvalidDates', 'checkOut must be after checkIn.');
     final limitText = query['limit'] ?? '20';
     final limit = int.tryParse(limitText) ?? 0;
     if (!RegExp(r'^[0-9]{1,3}$').hasMatch(limitText) || limit < 1 || limit > 100) {
       return problem(400, 'InvalidRequest', 'limit must be a whole number from 1 to 100.');
     }
   
     stats.searches++;
     // Cache-aside (L04): Redis first; only on a miss ask PostgreSQL, then keep a copy.
     final key = await cache.key(query['checkIn']!, query['checkOut']!);
     final cached = await cache.get(key);
     final List<Room> free;
     if (cached != null) {
       stats.cacheHits++;
       final numbers = cached.split(',');
       free = rooms.where((room) => numbers.contains(room.number)).toList();
     } else {
       stats.cacheMisses++;
       free = await unitOfWork((service) => service.findFreeRooms(stay));
       await cache.set(key, (free.map((room) => room.number).toList()..sort()).join(','));
     }
     // Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
     final after = query['after'] ?? '';
     final rest = free.where((room) => room.number.compareTo(after) > 0).toList()
       ..sort((a, b) => a.number.compareTo(b.number));
     final page = rest.take(limit).toList();
     return json(200, {
       'checkIn': query['checkIn'],
       'checkOut': query['checkOut'],
       'rooms': [
         for (final room in page) {'number': room.number, 'type': room.type}
       ],
       'next': rest.length > page.length ? page.last.number : null,
     });
   });
   ```
3. Arrow 2: two Redis commands. GET availability:version gives the version (nothing yet means 0); the key is availability:v0:2026-11-12:2026-11-14. Then GET of that key. The key is built once, before PostgreSQL is asked.

   C#:
   
   ```text
   // HotelBooking/Api/AvailabilityCache.cs
   using StackExchange.Redis;
   
   namespace HotelBooking.Api;
   
   // The three Redis commands the cache uses: GET, SET with a time to live, and INCR.
   public interface IKeyValueStore
   {
       string? Get(string key);
       void Set(string key, string value, TimeSpan ttl);
       void Increment(string key);
   }
   
   // The three commands, sent to Redis.
   public class RedisKeyValueStore(IDatabase redis) : IKeyValueStore
   {
       public string? Get(string key) => (string?)redis.StringGet(key);
       public void Set(string key, string value, TimeSpan ttl) => redis.StringSet(key, value, ttl);
       public void Increment(string key) => redis.StringIncrement(key);
   }
   
   // The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.
   // PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds:
   // the free room numbers for one stay, such as "R7,R8". Booking and cancelling never read the cache.
   public class AvailabilityCache(IKeyValueStore store)
   {
       public static readonly TimeSpan Ttl = TimeSpan.FromSeconds(30);
       public const string VersionKey = "availability:version";
   
       // The version is part of the key, so raising it makes every older copy unreachable.
       public string Key(string checkIn, string checkOut) => $"availability:v{store.Get(VersionKey) ?? "0"}:{checkIn}:{checkOut}";
   
       public string? Get(string key) => store.Get(key);
   
       public void Set(string key, string roomNumbers) => store.Set(key, roomNumbers, Ttl);
   
       // A booking or a cancellation changed what is free: every cached search is now out of date.
       public void Invalidate() => store.Increment(VersionKey);
   }
   ```
   
   Python:
   
   ```text
   # hotel_booking/api_cache.py
   """The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.
   
   PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.
   Booking and cancelling never read the cache.
   """
   from __future__ import annotations
   
   from typing import Protocol
   
   from redis import Redis
   
   TTL_SECONDS = 30
   VERSION_KEY = "availability:version"
   
   
   class KeyValueStore(Protocol):
       """The three Redis commands the cache uses: GET, SET with a time to live, and INCR."""
   
       def get(self, key: str) -> str | None: ...
       def set(self, key: str, value: str, ttl_seconds: int) -> None: ...
       def increment(self, key: str) -> None: ...
   
   
   class RedisKeyValueStore:
       """The three commands, sent to Redis."""
   
       def __init__(self, redis: Redis) -> None:
           self._redis = redis
   
       def get(self, key: str) -> str | None:
           return self._redis.get(key)
   
       def set(self, key: str, value: str, ttl_seconds: int) -> None:
           self._redis.set(key, value, ex=ttl_seconds)
   
       def increment(self, key: str) -> None:
           self._redis.incr(key)
   
   
   class AvailabilityCache:
       """Copies of search answers: the free room numbers for one stay, such as "R7,R8"."""
   
       def __init__(self, store: KeyValueStore) -> None:
           self._store = store
   
       def key(self, check_in: str, check_out: str) -> str:
           # The version is part of the key, so raising it makes every older copy unreachable.
           version = self._store.get(VERSION_KEY) or "0"
           return f"availability:v{version}:{check_in}:{check_out}"
   
       def get(self, key: str) -> str | None:
           return self._store.get(key)
   
       def set(self, key: str, room_numbers: str) -> None:
           self._store.set(key, room_numbers, TTL_SECONDS)
   
       def invalidate(self) -> None:
           """A booking or a cancellation changed what is free: every cached search is now out of date."""
           self._store.increment(VERSION_KEY)
   ```
   
   Java:
   
   ```text
   // src/main/java/hotelbooking/api/AvailabilityCache.java
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
   ```
   
   Go:
   
   ```text
   // api/cache.go
   // The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.
   // PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.
   // Booking and cancelling never read the cache.
   package api
   
   import (
   	"context"
   	"errors"
   	"time"
   
   	"github.com/redis/go-redis/v9"
   )
   
   const ttl = 30 * time.Second
   const versionKey = "availability:version"
   
   // KeyValueStore is the three Redis commands the cache uses: GET, SET with a time to live, and INCR.
   type KeyValueStore interface {
   	Get(key string) (value string, found bool, err error)
   	Set(key, value string, ttl time.Duration) error
   	Increment(key string) error
   }
   
   // RedisKeyValueStore sends the three commands to Redis.
   type RedisKeyValueStore struct {
   	ctx    context.Context
   	client *redis.Client
   }
   
   func NewRedisKeyValueStore(ctx context.Context, client *redis.Client) *RedisKeyValueStore {
   	return &RedisKeyValueStore{ctx: ctx, client: client}
   }
   
   func (store *RedisKeyValueStore) Get(key string) (string, bool, error) {
   	value, err := store.client.Get(store.ctx, key).Result()
   	if errors.Is(err, redis.Nil) {
   		return "", false, nil // no copy under this key
   	}
   	return value, err == nil, err
   }
   
   func (store *RedisKeyValueStore) Set(key, value string, ttl time.Duration) error {
   	return store.client.Set(store.ctx, key, value, ttl).Err()
   }
   
   func (store *RedisKeyValueStore) Increment(key string) error {
   	return store.client.Incr(store.ctx, key).Err()
   }
   
   // AvailabilityCache keeps copies of search answers: the free room numbers for one stay, such as "R7,R8".
   type AvailabilityCache struct {
   	store KeyValueStore
   }
   
   func NewAvailabilityCache(store KeyValueStore) *AvailabilityCache {
   	return &AvailabilityCache{store: store}
   }
   
   // Key is the key for one stay. The version is part of the key, so raising it makes every older copy unreachable.
   func (cache *AvailabilityCache) Key(checkIn, checkOut string) (string, error) {
   	version, found, err := cache.store.Get(versionKey)
   	if !found {
   		version = "0"
   	}
   	return "availability:v" + version + ":" + checkIn + ":" + checkOut, err
   }
   
   func (cache *AvailabilityCache) Get(key string) (string, bool, error) {
   	return cache.store.Get(key)
   }
   
   func (cache *AvailabilityCache) Set(key, roomNumbers string) error {
   	return cache.store.Set(key, roomNumbers, ttl)
   }
   
   // Invalidate is called after a booking or a cancellation changed what is free: every cached search is now out of date.
   func (cache *AvailabilityCache) Invalidate() error {
   	return cache.store.Increment(versionKey)
   }
   ```
   
   JavaScript:
   
   ```text
   // src/api-cache.js
   // The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.
   // PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.
   // Booking and cancelling never read the cache.
   //
   // The cache needs only three Redis commands. Every key-value store it uses keeps this contract:
   //
   //   get(key)                     the value, or null when there is none (may answer later)
   //   set(key, value, ttlSeconds)  keep the value for ttlSeconds
   //   increment(key)               add 1 to the number stored under key
   export const ttlSeconds = 30;
   export const versionKey = 'availability:version';
   
   // The three commands, sent to Redis through node-redis.
   export class RedisKeyValueStore {
     #redis;
   
     constructor(redis) {
       this.#redis = redis;
     }
   
     get(key) {
       return this.#redis.get(key);
     }
   
     async set(key, value, seconds) {
       await this.#redis.set(key, value, { expiration: { type: 'EX', value: seconds } });
     }
   
     async increment(key) {
       await this.#redis.incr(key);
     }
   }
   
   // Copies of search answers: the free room numbers for one stay, such as "R7,R8".
   export class AvailabilityCache {
     #store;
   
     constructor(store) {
       this.#store = store;
     }
   
     async key(checkIn, checkOut) {
       // The version is part of the key, so raising it makes every older copy unreachable.
       const version = (await this.#store.get(versionKey)) ?? '0';
       return `availability:v${version}:${checkIn}:${checkOut}`;
     }
   
     get(key) {
       return this.#store.get(key);
     }
   
     async set(key, roomNumbers) {
       await this.#store.set(key, roomNumbers, ttlSeconds);
     }
   
     // A booking or a cancellation changed what is free: every cached search is now out of date.
     async invalidate() {
       await this.#store.increment(versionKey);
     }
   }
   ```
   
   Dart:
   
   ```text
   // lib/api_cache.dart
   /// The cache for the free-room search (P08): cache-aside in Redis, with a TTL and a version.
   ///
   /// PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.
   /// Booking and cancelling never read the cache.
   library;
   
   import 'package:redis/redis.dart' show Command;
   
   const ttlSeconds = 30;
   const versionKey = 'availability:version';
   
   // The three Redis commands the cache uses: GET, SET with a time to live, and INCR.
   abstract interface class KeyValueStore {
     // Returns null when the key is missing.
     Future<String?> get(String key);
   
     Future<void> set(String key, String value, int ttlSeconds);
   
     Future<void> increment(String key);
   }
   
   // The three commands, sent to Redis over one open connection.
   class RedisKeyValueStore implements KeyValueStore {
     RedisKeyValueStore(this._redis);
   
     final Command _redis;
   
     // Redis answers GET with the text, or with null when the key is missing.
     @override
     Future<String?> get(String key) async => await _redis.send_object(['GET', key]) as String?;
   
     @override
     Future<void> set(String key, String value, int ttlSeconds) =>
         _redis.send_object(['SET', key, value, 'EX', '$ttlSeconds']);
   
     @override
     Future<void> increment(String key) => _redis.send_object(['INCR', key]);
   }
   
   // Copies of search answers: the free room numbers for one stay, such as "R7,R8".
   class AvailabilityCache {
     AvailabilityCache(this._store);
   
     final KeyValueStore _store;
   
     Future<String> key(String checkIn, String checkOut) async {
       // The version is part of the key, so raising it makes every older copy unreachable.
       final version = await _store.get(versionKey) ?? '0';
       return 'availability:v$version:$checkIn:$checkOut';
     }
   
     Future<String?> get(String key) => _store.get(key);
   
     Future<void> set(String key, String roomNumbers) => _store.set(key, roomNumbers, ttlSeconds);
   
     // A booking or a cancellation changed what is free: every cached search is now out of date.
     Future<void> invalidate() => _store.increment(versionKey);
   }
   ```
4. Orange dashed box “Redis”: copies only. Each copy lives 30 seconds (the TTL), then Redis deletes it. FLUSHALL deletes all of them and nothing is lost: the next searches just miss.
5. Arrow 3, only on a miss: the endpoint adds 1 to cacheMisses and runs FindFreeRooms through the unit of work, exactly as in P07.
6. Green box “BookingService.FindFreeRooms” and arrow 4: the P06 rule, unchanged. It asks the store one question per room: R7, then R8.
7. White box “CountingReservationStore”: the wiring put it around the PostgreSQL store, so every question adds 1 to databaseQueries before it is passed on.

   C#:
   
   ```text
   void RunApi()
   {
       // The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
       var builder = WebApplication.CreateBuilder(args);
       var connection = $"Host=localhost;Port={Environment.GetEnvironmentVariable("HOTEL_DB_PORT") ?? "5432"};Database=hotel;Username=hotel;Password=hotel";
       builder.Services.AddDbContextFactory<RoomsDb>(options => options.UseNpgsql(connection));
       builder.Services.AddDbContextFactory<ReservationsDb>(options => options.UseNpgsql(connection));
       var app = builder.Build();
   
       var rooms = new PostgresRoomCatalog(app.Services.GetRequiredService<IDbContextFactory<RoomsDb>>()).All();
       var databases = app.Services.GetRequiredService<IDbContextFactory<ReservationsDb>>();
       var email = new EmailNotificationSender();
       var stats = new Stats();
   
       // One request: a fresh store over its own DbContext, counted for GET /stats; save what changed at the end.
       IResult UnitOfWork(Func<BookingService, IResult> work)
       {
           using var db = databases.CreateDbContext();
           var store = new PostgresReservationStore(db, rooms);
           var result = work(new BookingService(rooms, new CountingReservationStore(store, stats), email));
           store.SaveChanges();
           return result;
       }
   
       HotelApi.Map(app, rooms, UnitOfWork, stats);
       app.Run("http://localhost:5080");
   }
   ```
   
   Python:
   
   ```text
   def run_api() -> None:
       # The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
       engine = create_engine(f"postgresql+psycopg://hotel:hotel@localhost:{os.getenv('HOTEL_DB_PORT', '5432')}/hotel")
       rooms = PostgresRoomCatalog(engine).all()
       email = EmailNotificationSender()
       stats = Stats()
   
       @contextmanager
       def unit_of_work():
           # One request: a fresh store over its own session, counted for GET /stats; save what changed at the end.
           with Session(engine) as session:
               store = PostgresReservationStore(session, rooms)
               yield BookingService(rooms, CountingReservationStore(store, stats), email)
               store.save_changes()
   
       uvicorn.run(create_app(rooms, unit_of_work, stats), host="localhost", port=5080)
   ```
   
   Java:
   
   ```text
   // The wiring for the web app.
   @Bean
   HotelApi hotelApi(EntityManager db, TransactionTemplate transactions) {
       List<Room> rooms = new PostgresRoomCatalog(db).all();
       EmailNotificationSender email = new EmailNotificationSender();
       Stats stats = new Stats();
       // One request: a fresh store in its own transaction, counted for GET /stats; save what changed at the end.
       HotelApi.UnitOfWork unitOfWork = work -> transactions.execute(transaction -> {
           PostgresReservationStore store = new PostgresReservationStore(db, rooms);
           var result = work.apply(new BookingService(rooms, new CountingReservationStore(store, stats), email));
           store.saveChanges();
           return result;
       });
       return new HotelApi(rooms, unitOfWork, stats);
   }
   ```
   
   Go:
   
   ```text
   func runAPI() {
   	// The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
   	ctx := context.Background()
   	pool, err := pgxpool.New(ctx, "postgres://hotel:hotel@localhost:"+cmp.Or(os.Getenv("HOTEL_DB_PORT"), "5432")+"/hotel")
   	if err != nil {
   		log.Fatal(err)
   	}
   	defer pool.Close()
   	hotelRooms := rooms.NewPostgresRoomCatalog(pool).All()
   	email := &notifications.EmailNotificationSender{}
   	stats := &api.Stats{}
   
   	// One request: a fresh store, counted for GET /stats; save what changed at the end.
   	unitOfWork := func(work func(service *reservations.BookingService)) error {
   		store := reservations.NewPostgresReservationStore(ctx, pool, hotelRooms)
   		work(reservations.NewBookingService(hotelRooms, api.NewCountingReservationStore(store, stats), email, 0))
   		return store.SaveChanges()
   	}
   
   	fmt.Println("Listening on http://localhost:5080")
   	log.Fatal(http.ListenAndServe("localhost:5080", api.New(hotelRooms, unitOfWork, stats)))
   }
   ```
   
   JavaScript:
   
   ```text
   async function runApi() {
     // The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
     const pool = new pg.Pool({ host: 'localhost', port: Number(process.env.HOTEL_DB_PORT ?? 5432), database: 'hotel', user: 'hotel', password: 'hotel' });
     const rooms = await new PostgresRoomCatalog(pool).all();
     const email = new EmailNotificationSender();
     const stats = new Stats();
   
     // One request: a fresh store, counted for GET /stats; save what changed at the end.
     const unitOfWork = async work => {
       const store = new PostgresReservationStore(pool, rooms);
       const result = await work(new BookingService(rooms, new CountingReservationStore(store, stats), email));
       await store.saveChanges();
       return result;
     };
   
     createApi(rooms, unitOfWork, stats).listen(5080, 'localhost', () => console.log('Listening on http://localhost:5080'));
   }
   ```
   
   Dart:
   
   ```text
   Future<void> runApi() async {
     // The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
     final port = int.parse(Platform.environment['HOTEL_DB_PORT'] ?? '5432');
     final pool = Pool.withEndpoints([
       Endpoint(host: 'localhost', port: port, database: 'hotel', username: 'hotel', password: 'hotel')
     ], settings: PoolSettings(sslMode: SslMode.disable));
     final rooms = await PostgresRoomCatalog(pool).all();
     final email = EmailNotificationSender();
     final stats = Stats();
   
     // One request: a fresh store, counted for GET /stats; save what changed at the end.
     Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async {
       final store = PostgresReservationStore(pool, rooms);
       final result = await work(BookingService(rooms, CountingReservationStore(store, stats), email));
       await store.saveChanges();
       return result;
     }
   
     await serve(createApi(rooms, unitOfWork, stats), 'localhost', 5080);
     print('Listening on http://localhost:5080');
   }
   ```
8. Arrow 5 and the grey box “PostgreSQL”: one SELECT per question. A miss costs 2 database queries with our two rooms; with 40 rooms it would cost 40.
9. Arrow 6: back in the endpoint, the answer is kept for 30 seconds as the room numbers in text order, "R7,R8". Then the endpoint pages the rooms and answers 200.
10. The next search for the same dates is a hit: arrows 1 and 2, then the answer. No unit of work, no database connection. 1,000 searches from an empty cache therefore give:

    ```text
    {"searches":1000,"databaseQueries":2,"cacheHits":999,"cacheMisses":1}
    ```
11. Arrow 7, after POST or DELETE has saved its change in PostgreSQL (201 or 204): INCR availability:version. Every search now builds a key with v1, so the old v0 copy is never read again and simply expires.

    C#:
    
    ```text
    app.MapPost("/reservations", async (HttpRequest request) =>
    {
        if (!request.HasJsonContentType()) return Problem(400, "InvalidRequest", BodyDetail);
        JsonElement body;
        try
        {
            body = (await JsonDocument.ParseAsync(request.Body)).RootElement;
        }
        catch (JsonException)
        {
            return Problem(400, "InvalidRequest", BodyDetail);
        }
        string? Text(string field) => body.ValueKind == JsonValueKind.Object && body.TryGetProperty(field, out var value)
            && value.ValueKind == JsonValueKind.String && value.GetString() is { Length: > 0 } text ? text : null;
        if (Fields.Any(field => Text(field) is null)) return Problem(400, "InvalidRequest", BodyDetail);
        if (ParseDate(Text("checkIn")) is not { } start || ParseDate(Text("checkOut")) is not { } end) return Problem(400, "InvalidDates", DateDetail);
        if (rooms.FirstOrDefault(room => room.Number == Text("room")) is not { } room) return Problem(400, "UnknownRoom", $"There is no room {Text("room")}.");
    
        var guest = new Guest(Text("guestName")!, Text("guestEmail")!);
        var answer = unitOfWork(service => ToHttp(service.Reserve(guest, room, new DateRange(start, end))));
        if (answer is IStatusCodeHttpResult { StatusCode: 201 }) cache.Invalidate(); // after the unit of work: the booking is saved in PostgreSQL
        return answer;
    });
    ```
    
    Python:
    
    ```text
    @app.post("/reservations")
    def reserve(body: Any = Body(None)) -> JSONResponse:
        if not isinstance(body, dict) or not all(isinstance(body.get(field), str) and body[field] for field in FIELDS):
            return problem(400, "InvalidRequest", BODY_DETAIL)
        check_in, check_out = parse_date(body["checkIn"]), parse_date(body["checkOut"])
        if check_in is None or check_out is None:
            return problem(400, "InvalidDates", DATE_DETAIL)
        room = next((room for room in rooms if room.number == body["room"]), None)
        if room is None:
            return problem(400, "UnknownRoom", f"There is no room {body['room']}.")
    
        with unit_of_work() as service:
            result = service.reserve(Guest(body["guestName"], body["guestEmail"]), room, DateRange(check_in, check_out))
        if result.outcome == "Accepted":
            cache.invalidate()  # after the unit of work: the booking is saved in PostgreSQL
        return to_http(result)
    ```
    
    Java:
    
    ```text
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
        ResponseEntity<?> answer = unitOfWork.run(service -> toHttp(service.reserve(guest, room, new DateRange(start, end))));
        if (answer.getStatusCode().value() == 201) cache.invalidate(); // after the unit of work: the booking is saved in PostgreSQL
        return answer;
    }
    ```
    
    Go:
    
    ```text
    mux.HandleFunc("POST /reservations", func(w http.ResponseWriter, r *http.Request) {
    	var body map[string]any
    	if strings.HasPrefix(r.Header.Get("Content-Type"), "application/json") {
    		json.NewDecoder(r.Body).Decode(&body)
    	}
    	text := map[string]string{}
    	for _, field := range fields {
    		value, ok := body[field].(string)
    		if !ok || value == "" {
    			problem(w, http.StatusBadRequest, "InvalidRequest", bodyDetail)
    			return
    		}
    		text[field] = value
    	}
    	checkIn, okIn := parseDate(text["checkIn"])
    	checkOut, okOut := parseDate(text["checkOut"])
    	if !okIn || !okOut {
    		problem(w, http.StatusBadRequest, "InvalidDates", dateDetail)
    		return
    	}
    	index := slices.IndexFunc(hotelRooms, func(room rooms.Room) bool { return room.Number == text["room"] })
    	if index < 0 {
    		problem(w, http.StatusBadRequest, "UnknownRoom", fmt.Sprintf("There is no room %s.", text["room"]))
    		return
    	}
    
    	var result reservations.BookingResult
    	guest := reservations.Guest{Name: text["guestName"], Email: text["guestEmail"]}
    	stay := reservations.DateRange{CheckIn: checkIn, CheckOut: checkOut}
    	if err := unitOfWork(func(service *reservations.BookingService) { result = service.Reserve(guest, hotelRooms[index], stay) }); err != nil {
    		http.Error(w, err.Error(), http.StatusInternalServerError)
    		return
    	}
    	// After the unit of work: the booking is saved in PostgreSQL.
    	if result.Outcome == "Accepted" {
    		if err := cache.Invalidate(); err != nil {
    			http.Error(w, err.Error(), http.StatusInternalServerError)
    			return
    		}
    	}
    	toHttp(w, result)
    })
    ```
    
    JavaScript:
    
    ```text
    app.post('/reservations', async (request, response) => {
      const body = request.body;
      if (typeof body !== 'object' || body === null || !fields.every(field => typeof body[field] === 'string' && body[field] !== '')) {
        return problem(response, 400, 'InvalidRequest', bodyDetail);
      }
      const start = parseDate(body.checkIn);
      const end = parseDate(body.checkOut);
      if (!start || !end) return problem(response, 400, 'InvalidDates', dateDetail);
      const room = rooms.find(candidate => candidate.number === body.room);
      if (!room) return problem(response, 400, 'UnknownRoom', `There is no room ${body.room}.`);
    
      const result = await unitOfWork(service =>
        service.reserve(new Guest(body.guestName, body.guestEmail), room, new DateRange(start, end)));
      if (result.outcome === 'Accepted') await cache.invalidate(); // after the unit of work: the booking is saved in PostgreSQL
      toHttp(response, result);
    });
    ```
    
    Dart:
    
    ```text
    router.post('/reservations', (Request request) async {
      if (request.mimeType != 'application/json') return problem(400, 'InvalidRequest', bodyDetail);
      Object? json;
      try {
        json = jsonDecode(await request.readAsString());
      } on FormatException {
        return problem(400, 'InvalidRequest', bodyDetail);
      }
      final body = json is Map ? json : const {};
      if (!fields.every((field) => body[field] is String && body[field] != '')) {
        return problem(400, 'InvalidRequest', bodyDetail);
      }
      final checkIn = parseDate(body['checkIn']);
      final checkOut = parseDate(body['checkOut']);
      if (checkIn == null || checkOut == null) return problem(400, 'InvalidDates', dateDetail);
      final room = rooms.where((room) => room.number == body['room']).firstOrNull;
      if (room == null) return problem(400, 'UnknownRoom', 'There is no room ${body['room']}.');
    
      final guest = Guest(body['guestName'] as String, body['guestEmail'] as String);
      final result =
          await unitOfWork((service) => service.reserve(guest, room, DateRange(checkIn, checkOut)));
      if (result.outcome == 'Accepted') {
        await cache.invalidate(); // after the unit of work: the booking is saved in PostgreSQL
      }
      return toHttp(result);
    });
    ```

**Legend:**
- **Yellow box.** A person or a program outside our code. We use it; we do not build it. In this diagram: The measurement tool or curl.exe in your terminal, standing in for many guests’ browsers.
- **White box.** Our code that is not the booking rule: it passes requests on, counts or stores data. In this diagram: The search endpoint in the Api folder and the counting store the wiring puts around the PostgreSQL store.
- **Green box.** The code that owns the hotel’s booking rule. In this diagram: BookingService.FindFreeRooms, the same code as in P06 and P07.
- **Orange dashed box.** A cache: copies of answers that may be thrown away at any time. The dashed line says “only a copy”. In this diagram: Redis in Docker, with one copy per version and stay, each for 30 seconds.
- **Grey box.** Stored data that is the source of truth: if it says R7 is booked, it is. In this diagram: PostgreSQL in Docker, with the reservations table.
- **Solid line with a filled arrowhead.** A request at run time: the part at the start asks the part at the arrowhead for something. In this diagram: An HTTP request, a method call, a Redis command or an SQL statement.
- **Number at the start of a line’s label.** The order of the requests: 1 happens first, then 2, and so on. In this diagram: 1 to 6 are one search that misses; a hit is 1 and 2; 7 happens after a booking or a cancel.

### Your turn · add the cache

Restart the app after each change (Ctrl+C, then your run command).

1. Run the install line for your language once; Java: also add the two Redis lines to application.properties.
2. Create the cache file (3.2) in the place your tab shows.
3. Change the search endpoint (3.3): the key, Redis first, PostgreSQL on a miss, keep a copy.
4. Add cache as the last parameter of your Api function, and connect to Redis in the wiring (3.4).
5. Add the dictionary and change the test helper (3.5). Run your checks: still 15 green.
6. Start the app, search 12–14 Nov twice with curl.exe, then run docker compose exec cache redis-cli KEYS "availability:*": one key.
7. Open “Explain this diagram step by step” above and find each numbered arrow in your own code.

**When you finish, you have:** a search that reads Redis first and PostgreSQL only on a miss, 15 green checks, and one key in Redis.
### Explanation

A hit never opens a unit of work: it needs no database connection at all. That is why the cache wraps only the FindFreeRooms call and not the whole endpoint.



---

## 5. Step 4 · Measure with the cache

Empty the cache, then run exactly the same 1,000 searches. The counts show what the cache saved; the times show what a guest feels.





### What to expect

| Number | No cache | Cache-aside, TTL 30 s | Why |
| --- | --- | --- | --- |
| Database queries | 2,000 | 2 | Only the first search misses: 2 rooms, 2 questions |
| Cache hits | 0 | 999 | The other 999 searches come within 30 seconds |
| Cache misses | 0 | 1 | The very first search finds nothing |
| Average and p95 | your numbers | your numbers | A hit asks Redis instead of PostgreSQL |

**Why exactly 2 queries, 999 hits and 1 miss** In short: The first search finds no copy and asks PostgreSQL once per room; the other 999 come within 30 seconds and use the copy.

1. Search 1: a miss. FindFreeRooms asks about R7 and R8: 2 queries. The copy is stored for 30 s.
2. Searches 2 to 1,000 take about one or two seconds in total: all hits, 0 queries.
3. If your run took longer than 30 seconds, the copy expired once and you see 2 misses and 4 queries: that is the TTL working.

```text
No cache:              {"searches":1000,"databaseQueries":2000,"cacheHits":0,"cacheMisses":0}
Cache-aside, TTL 30 s: {"searches":1000,"databaseQueries":2,"cacheHits":999,"cacheMisses":1}
```

### Empty the cache first

**FLUSHALL: start from an empty cache** In short: FLUSHALL deletes every key in Redis, so the next search is certainly a miss. Use it only on your laptop’s cache. PostgreSQL is not touched: the reservations stay.

1. You searched 12–14 Nov 10 seconds ago: the copy is still there, and a measurement would show 0 misses.
2. docker compose exec cache redis-cli FLUSHALL answers OK.
3. Now the first of the 1,000 searches is a miss and the other 999 are hits.

```text
docker compose exec cache redis-cli FLUSHALL
OK
docker compose exec cache redis-cli KEYS "availability:*"
(empty array)
```

### Terminal 2

```text
docker compose exec cache redis-cli FLUSHALL
OK
```

### Run the measurement again

### Terminal 2

#### C#

```text
dotnet run tools/measure.cs
```

#### Python

```text
python tools/measure.py
```

#### Java

```text
java tools/Measure.java
```

#### Go

```text
go run ./tools/measure
```

#### JavaScript

```text
node tools/measure.js
```

#### Dart

```text
dart run tool/measure.dart
```

### What you will see

The last line is the same in every language. The times are from one laptop.

### Terminal 2

```text
1000 searches for 2026-11-12 to 2026-11-14
Average: 1.0 ms
p95: 1.3 ms
Total: 1.0 s
GET /stats difference: {"searches":1000,"databaseQueries":2,"cacheHits":999,"cacheMisses":1}
```

### Completed example: the measurements in docs/cache-experiment.md

### docs/cache-experiment.md (Measurements)

```text
# Cache experiment: the free-room search

PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.

## Measurements

1,000 searches for 12–14 Nov, one after another, with the measurement tool.
Times from one laptop; yours will differ.

| Run | Searches | Database queries | Cache hits | Average | p95 |
|---|---|---|---|---|---|
| No cache | 1,000 | 2,000 | 0 | 2.0 ms | 2.5 ms |
| Cache-aside, TTL 30 s | 1,000 | 2 | 999 | 1.0 ms | 1.3 ms |

S1 asks for p95 ≤ 1 second: both runs meet it here, with two rooms and one user.
The cache removes 1,998 of 2,000 database queries. With 40 rooms it would remove
39,960 of 40,000.
```

### Your turn · measure with the cache

1. Make sure the app from Step 3 is running in terminal 1.
2. In terminal 2 run the FLUSHALL line: OK.
3. Run the measurement tool: databaseQueries 2, cacheHits 999, cacheMisses 1.
4. Add the row “Cache-aside, TTL 30 s” to docs/cache-experiment.md with your times.
5. Below the table write two lines: whether both runs meet S1 (p95 ≤ 1 s), and how many queries the cache removed.

**When you finish, you have:** both rows of measurements in docs/cache-experiment.md and two lines that compare them.
### Explanation

With two rooms and one user both runs are fast; the counts are the real result: 1,998 of 2,000 database queries removed. With 40 rooms that would be 39,960 of 40,000.



---

## 6. Step 5 · See the stale result

A cache shows copies. Book R7 just after a search, search again within 30 seconds, and the copy still says R7 is free. Then try to book R7 again.





### The four requests, line by line

| Request | Answer | What happened |
| --- | --- | --- |
| Search 12–14 Nov | R7 and R8 | A miss: the copy "R7,R8" is kept for 30 s |
| Maya books R7 for 12–14 Nov | 201 R101 | Saved in PostgreSQL; the copy is not touched |
| Search 12–14 Nov again | R7 and R8 | A hit: STALE, R7 is booked |
| Omar asks for R7 for 13–15 Nov | 409 Unavailable | BookingService asks PostgreSQL, not the cache: the rule holds |

**A stale result, and why the booking rule still holds** In short: A stale result is an old copy that no longer matches PostgreSQL; it can mislead a search, but it never decides a booking. POST /reservations does not read the cache at all: BookingService asks PostgreSQL whether R7 is taken.

1. Search: a miss, the copy "R7,R8" is stored for 30 s.
2. Maya books R7: 201. PostgreSQL now has her reservation; the copy still says R7 is free.
3. Search again: a hit, R7 and R8. That is stale.
4. Omar books R7: BookingService asks PostgreSQL, finds Maya, and answers 409. Nobody is double-booked.

```text
GET /rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14
200 {"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}

POST /reservations   {"guestName":"Maya","guestEmail":"maya@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}
201 {"id":"R101","status":"Confirmed","notificationFailed":false}

GET /rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14
200 {"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}

POST /reservations   {"guestName":"Omar","guestEmail":"omar@example.com","room":"R7","checkIn":"2026-11-13","checkOut":"2026-11-15"}
409 {"title":"Unavailable","status":409,"detail":"The room is booked for at least one night of this stay."}
```

### Start from an empty database and an empty cache

Your P07 restart check left Maya’s R102 in PostgreSQL. Stop the app (Ctrl+C) first, then:

### Terminal 2

```text
docker compose down -v
docker compose up -d
```

### The four requests

Start the app again in terminal 1, then send these one by one, within 30 seconds of the first.

### Terminal 2

```text
curl.exe -s -w "\n%{http_code}\n" "http://localhost:5080/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14"
curl.exe -s -w "\n%{http_code}\n" -X POST -H "Content-Type: application/json" --data "@docs/requests/maya-r7.json" http://localhost:5080/reservations
curl.exe -s -w "\n%{http_code}\n" "http://localhost:5080/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14"
curl.exe -s -w "\n%{http_code}\n" -X POST -H "Content-Type: application/json" --data "@docs/requests/omar-r7.json" http://localhost:5080/reservations
```

### The four answers, in every language

### Status and body of each answer

```text
GET /rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14
200 {"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}

POST /reservations   {"guestName":"Maya","guestEmail":"maya@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}
201 {"id":"R101","status":"Confirmed","notificationFailed":false}

GET /rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14
200 {"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}

POST /reservations   {"guestName":"Omar","guestEmail":"omar@example.com","room":"R7","checkIn":"2026-11-13","checkOut":"2026-11-15"}
409 {"title":"Unavailable","status":409,"detail":"The room is booked for at least one night of this stay."}
```

### Completed example: “Stale result” in docs/cache-experiment.md

### docs/cache-experiment.md (Stale result)

```text
## Stale result (Step 5, before invalidation)

1. Empty cache. Search 12–14 Nov: R7 and R8 (a miss: the answer is cached for 30 s).
2. Maya books R7 for 12–14 Nov: 201 R101.
3. Search again within 30 s: R7 and R8. STALE: R7 is booked.
4. Omar asks for R7 for 13–15 Nov: 409 Unavailable. Booking reads PostgreSQL,
   not the cache, so the booking rule still holds.
```

### Your turn · watch the stale result

1. Stop the app. Run docker compose down -v, then docker compose up -d. Start the app.
2. Send the four requests one by one, within 30 seconds of the first search.
3. Check that the third answer still lists R7, and the fourth is 409 Unavailable.
4. Add the section “Stale result” to docs/cache-experiment.md: the four requests and what each answer means.
5. In docs/design-notes.md add ## Cache rule with one line: booking never reads the cache.

**When you finish, you have:** the stale result seen with your own app, written down in docs/cache-experiment.md, and the cache rule in design-notes.md.
### Explanation

This is L04’s stale-cache risk on your own laptop: speed may be a little behind, the booking rule may not. Omar sees R7 in the list, but PostgreSQL decides and he gets 409, not a double booking.



---

## 7. Step 6 · Invalidate on every change

After a successful booking or cancel, raise availability:version. Every search then builds a new key, misses, and asks PostgreSQL: the stale copy is never read again.





### When the version goes up

| Request | Answer | Version |
| --- | --- | --- |
| POST /reservations | 201 Created | INCR availability:version, after the unit of work has saved it |
| POST /reservations | 409, 400 | Unchanged: nothing was saved |
| DELETE /reservations/{id} | 204 No Content | INCR availability:version, after the unit of work has saved it |
| DELETE /reservations/{id} | 404 | Unchanged |
| GET /rooms/availability | 200 | Unchanged: a search only reads |

**Invalidation with a version number** In short: After a successful booking or cancel the app raises availability:version, so every search builds a new key and finds no old copy. Deleting exactly the keys a booking affects is hard: every stay that overlaps 12–14 Nov would have to go. Raising one number makes them all unreachable at once; Redis deletes the old copies when their 30 seconds are over. The version is raised after the unit of work, when the booking is already saved in PostgreSQL. Raised earlier, a search in between could store the old answer under the new version.

1. Version 0: the search stored availability:v0:2026-11-12:2026-11-14 = "R7,R8".
2. Maya books R7: 201, then INCR availability:version: the version is 1.
3. The next search builds availability:v1:2026-11-12:2026-11-14: a miss, PostgreSQL says R8 only.
4. A booking that answers 409 changes nothing, so it does not raise the version.

C#:

```text
app.MapPost("/reservations", async (HttpRequest request) =>
{
    if (!request.HasJsonContentType()) return Problem(400, "InvalidRequest", BodyDetail);
    JsonElement body;
    try
    {
        body = (await JsonDocument.ParseAsync(request.Body)).RootElement;
    }
    catch (JsonException)
    {
        return Problem(400, "InvalidRequest", BodyDetail);
    }
    string? Text(string field) => body.ValueKind == JsonValueKind.Object && body.TryGetProperty(field, out var value)
        && value.ValueKind == JsonValueKind.String && value.GetString() is { Length: > 0 } text ? text : null;
    if (Fields.Any(field => Text(field) is null)) return Problem(400, "InvalidRequest", BodyDetail);
    if (ParseDate(Text("checkIn")) is not { } start || ParseDate(Text("checkOut")) is not { } end) return Problem(400, "InvalidDates", DateDetail);
    if (rooms.FirstOrDefault(room => room.Number == Text("room")) is not { } room) return Problem(400, "UnknownRoom", $"There is no room {Text("room")}.");

    var guest = new Guest(Text("guestName")!, Text("guestEmail")!);
    var answer = unitOfWork(service => ToHttp(service.Reserve(guest, room, new DateRange(start, end))));
    if (answer is IStatusCodeHttpResult { StatusCode: 201 }) cache.Invalidate(); // after the unit of work: the booking is saved in PostgreSQL
    return answer;
});

app.MapDelete("/reservations/{id}", (string id) =>
{
    var answer = unitOfWork(service => service.Cancel(id)
        ? Results.NoContent()
        : Problem(404, "NotFound", $"There is no confirmed reservation {id}."));
    if (answer is IStatusCodeHttpResult { StatusCode: 204 }) cache.Invalidate(); // after the unit of work: the cancel is saved in PostgreSQL
    return answer;
});
```

Python:

```text
@app.post("/reservations")
def reserve(body: Any = Body(None)) -> JSONResponse:
    if not isinstance(body, dict) or not all(isinstance(body.get(field), str) and body[field] for field in FIELDS):
        return problem(400, "InvalidRequest", BODY_DETAIL)
    check_in, check_out = parse_date(body["checkIn"]), parse_date(body["checkOut"])
    if check_in is None or check_out is None:
        return problem(400, "InvalidDates", DATE_DETAIL)
    room = next((room for room in rooms if room.number == body["room"]), None)
    if room is None:
        return problem(400, "UnknownRoom", f"There is no room {body['room']}.")

    with unit_of_work() as service:
        result = service.reserve(Guest(body["guestName"], body["guestEmail"]), room, DateRange(check_in, check_out))
    if result.outcome == "Accepted":
        cache.invalidate()  # after the unit of work: the booking is saved in PostgreSQL
    return to_http(result)

@app.delete("/reservations/{reservation_id}")
def cancel(reservation_id: str) -> Response:
    with unit_of_work() as service:
        cancelled = service.cancel(reservation_id)
    if not cancelled:
        return problem(404, "NotFound", f"There is no confirmed reservation {reservation_id}.")
    cache.invalidate()  # after the unit of work: the cancel is saved in PostgreSQL
    return Response(status_code=204)
```

Java:

```text
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
    ResponseEntity<?> answer = unitOfWork.run(service -> toHttp(service.reserve(guest, room, new DateRange(start, end))));
    if (answer.getStatusCode().value() == 201) cache.invalidate(); // after the unit of work: the booking is saved in PostgreSQL
    return answer;
}

@DeleteMapping("/reservations/{id}")
public ResponseEntity<?> cancel(@PathVariable String id) {
    ResponseEntity<?> answer = unitOfWork.run(service -> service.cancel(id)
        ? ResponseEntity.noContent().build()
        : problem(404, "NotFound", "There is no confirmed reservation " + id + "."));
    if (answer.getStatusCode().value() == 204) cache.invalidate(); // after the unit of work: the cancel is saved in PostgreSQL
    return answer;
}
```

Go:

```text
mux.HandleFunc("POST /reservations", func(w http.ResponseWriter, r *http.Request) {
	var body map[string]any
	if strings.HasPrefix(r.Header.Get("Content-Type"), "application/json") {
		json.NewDecoder(r.Body).Decode(&body)
	}
	text := map[string]string{}
	for _, field := range fields {
		value, ok := body[field].(string)
		if !ok || value == "" {
			problem(w, http.StatusBadRequest, "InvalidRequest", bodyDetail)
			return
		}
		text[field] = value
	}
	checkIn, okIn := parseDate(text["checkIn"])
	checkOut, okOut := parseDate(text["checkOut"])
	if !okIn || !okOut {
		problem(w, http.StatusBadRequest, "InvalidDates", dateDetail)
		return
	}
	index := slices.IndexFunc(hotelRooms, func(room rooms.Room) bool { return room.Number == text["room"] })
	if index < 0 {
		problem(w, http.StatusBadRequest, "UnknownRoom", fmt.Sprintf("There is no room %s.", text["room"]))
		return
	}

	var result reservations.BookingResult
	guest := reservations.Guest{Name: text["guestName"], Email: text["guestEmail"]}
	stay := reservations.DateRange{CheckIn: checkIn, CheckOut: checkOut}
	if err := unitOfWork(func(service *reservations.BookingService) { result = service.Reserve(guest, hotelRooms[index], stay) }); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	// After the unit of work: the booking is saved in PostgreSQL.
	if result.Outcome == "Accepted" {
		if err := cache.Invalidate(); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
	}
	toHttp(w, result)
})

mux.HandleFunc("DELETE /reservations/{id}", func(w http.ResponseWriter, r *http.Request) {
	id := r.PathValue("id")
	var cancelled bool
	if err := unitOfWork(func(service *reservations.BookingService) { cancelled = service.Cancel(id) }); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	if !cancelled {
		problem(w, http.StatusNotFound, "NotFound", fmt.Sprintf("There is no confirmed reservation %s.", id))
		return
	}
	// After the unit of work: the cancel is saved in PostgreSQL.
	if err := cache.Invalidate(); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	w.WriteHeader(http.StatusNoContent)
})
```

JavaScript:

```text
app.post('/reservations', async (request, response) => {
  const body = request.body;
  if (typeof body !== 'object' || body === null || !fields.every(field => typeof body[field] === 'string' && body[field] !== '')) {
    return problem(response, 400, 'InvalidRequest', bodyDetail);
  }
  const start = parseDate(body.checkIn);
  const end = parseDate(body.checkOut);
  if (!start || !end) return problem(response, 400, 'InvalidDates', dateDetail);
  const room = rooms.find(candidate => candidate.number === body.room);
  if (!room) return problem(response, 400, 'UnknownRoom', `There is no room ${body.room}.`);

  const result = await unitOfWork(service =>
    service.reserve(new Guest(body.guestName, body.guestEmail), room, new DateRange(start, end)));
  if (result.outcome === 'Accepted') await cache.invalidate(); // after the unit of work: the booking is saved in PostgreSQL
  toHttp(response, result);
});

app.delete('/reservations/:id', async (request, response) => {
  const cancelled = await unitOfWork(service => service.cancel(request.params.id));
  if (!cancelled) return problem(response, 404, 'NotFound', `There is no confirmed reservation ${request.params.id}.`);
  await cache.invalidate(); // after the unit of work: the cancel is saved in PostgreSQL
  response.status(204).end();
});
```

Dart:

```text
router.post('/reservations', (Request request) async {
  if (request.mimeType != 'application/json') return problem(400, 'InvalidRequest', bodyDetail);
  Object? json;
  try {
    json = jsonDecode(await request.readAsString());
  } on FormatException {
    return problem(400, 'InvalidRequest', bodyDetail);
  }
  final body = json is Map ? json : const {};
  if (!fields.every((field) => body[field] is String && body[field] != '')) {
    return problem(400, 'InvalidRequest', bodyDetail);
  }
  final checkIn = parseDate(body['checkIn']);
  final checkOut = parseDate(body['checkOut']);
  if (checkIn == null || checkOut == null) return problem(400, 'InvalidDates', dateDetail);
  final room = rooms.where((room) => room.number == body['room']).firstOrNull;
  if (room == null) return problem(400, 'UnknownRoom', 'There is no room ${body['room']}.');

  final guest = Guest(body['guestName'] as String, body['guestEmail'] as String);
  final result =
      await unitOfWork((service) => service.reserve(guest, room, DateRange(checkIn, checkOut)));
  if (result.outcome == 'Accepted') {
    await cache.invalidate(); // after the unit of work: the booking is saved in PostgreSQL
  }
  return toHttp(result);
});

router.delete('/reservations/<id>', (Request request, String id) async {
  final cancelled = await unitOfWork((service) => service.cancel(id));
  if (!cancelled) return problem(404, 'NotFound', 'There is no confirmed reservation $id.');
  await cache.invalidate(); // after the unit of work: the cancel is saved in PostgreSQL
  return Response(204);
});
```

### Completed example 6.1 · POST and DELETE raise the version

**Invalidation with a version number** In short: After a successful booking or cancel the app raises availability:version, so every search builds a new key and finds no old copy. Deleting exactly the keys a booking affects is hard: every stay that overlaps 12–14 Nov would have to go. Raising one number makes them all unreachable at once; Redis deletes the old copies when their 30 seconds are over. The version is raised after the unit of work, when the booking is already saved in PostgreSQL. Raised earlier, a search in between could store the old answer under the new version.

1. Version 0: the search stored availability:v0:2026-11-12:2026-11-14 = "R7,R8".
2. Maya books R7: 201, then INCR availability:version: the version is 1.
3. The next search builds availability:v1:2026-11-12:2026-11-14: a miss, PostgreSQL says R8 only.
4. A booking that answers 409 changes nothing, so it does not raise the version.

C#:

```text
app.MapPost("/reservations", async (HttpRequest request) =>
{
    if (!request.HasJsonContentType()) return Problem(400, "InvalidRequest", BodyDetail);
    JsonElement body;
    try
    {
        body = (await JsonDocument.ParseAsync(request.Body)).RootElement;
    }
    catch (JsonException)
    {
        return Problem(400, "InvalidRequest", BodyDetail);
    }
    string? Text(string field) => body.ValueKind == JsonValueKind.Object && body.TryGetProperty(field, out var value)
        && value.ValueKind == JsonValueKind.String && value.GetString() is { Length: > 0 } text ? text : null;
    if (Fields.Any(field => Text(field) is null)) return Problem(400, "InvalidRequest", BodyDetail);
    if (ParseDate(Text("checkIn")) is not { } start || ParseDate(Text("checkOut")) is not { } end) return Problem(400, "InvalidDates", DateDetail);
    if (rooms.FirstOrDefault(room => room.Number == Text("room")) is not { } room) return Problem(400, "UnknownRoom", $"There is no room {Text("room")}.");

    var guest = new Guest(Text("guestName")!, Text("guestEmail")!);
    var answer = unitOfWork(service => ToHttp(service.Reserve(guest, room, new DateRange(start, end))));
    if (answer is IStatusCodeHttpResult { StatusCode: 201 }) cache.Invalidate(); // after the unit of work: the booking is saved in PostgreSQL
    return answer;
});

app.MapDelete("/reservations/{id}", (string id) =>
{
    var answer = unitOfWork(service => service.Cancel(id)
        ? Results.NoContent()
        : Problem(404, "NotFound", $"There is no confirmed reservation {id}."));
    if (answer is IStatusCodeHttpResult { StatusCode: 204 }) cache.Invalidate(); // after the unit of work: the cancel is saved in PostgreSQL
    return answer;
});
```

Python:

```text
@app.post("/reservations")
def reserve(body: Any = Body(None)) -> JSONResponse:
    if not isinstance(body, dict) or not all(isinstance(body.get(field), str) and body[field] for field in FIELDS):
        return problem(400, "InvalidRequest", BODY_DETAIL)
    check_in, check_out = parse_date(body["checkIn"]), parse_date(body["checkOut"])
    if check_in is None or check_out is None:
        return problem(400, "InvalidDates", DATE_DETAIL)
    room = next((room for room in rooms if room.number == body["room"]), None)
    if room is None:
        return problem(400, "UnknownRoom", f"There is no room {body['room']}.")

    with unit_of_work() as service:
        result = service.reserve(Guest(body["guestName"], body["guestEmail"]), room, DateRange(check_in, check_out))
    if result.outcome == "Accepted":
        cache.invalidate()  # after the unit of work: the booking is saved in PostgreSQL
    return to_http(result)

@app.delete("/reservations/{reservation_id}")
def cancel(reservation_id: str) -> Response:
    with unit_of_work() as service:
        cancelled = service.cancel(reservation_id)
    if not cancelled:
        return problem(404, "NotFound", f"There is no confirmed reservation {reservation_id}.")
    cache.invalidate()  # after the unit of work: the cancel is saved in PostgreSQL
    return Response(status_code=204)
```

Java:

```text
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
    ResponseEntity<?> answer = unitOfWork.run(service -> toHttp(service.reserve(guest, room, new DateRange(start, end))));
    if (answer.getStatusCode().value() == 201) cache.invalidate(); // after the unit of work: the booking is saved in PostgreSQL
    return answer;
}

@DeleteMapping("/reservations/{id}")
public ResponseEntity<?> cancel(@PathVariable String id) {
    ResponseEntity<?> answer = unitOfWork.run(service -> service.cancel(id)
        ? ResponseEntity.noContent().build()
        : problem(404, "NotFound", "There is no confirmed reservation " + id + "."));
    if (answer.getStatusCode().value() == 204) cache.invalidate(); // after the unit of work: the cancel is saved in PostgreSQL
    return answer;
}
```

Go:

```text
mux.HandleFunc("POST /reservations", func(w http.ResponseWriter, r *http.Request) {
	var body map[string]any
	if strings.HasPrefix(r.Header.Get("Content-Type"), "application/json") {
		json.NewDecoder(r.Body).Decode(&body)
	}
	text := map[string]string{}
	for _, field := range fields {
		value, ok := body[field].(string)
		if !ok || value == "" {
			problem(w, http.StatusBadRequest, "InvalidRequest", bodyDetail)
			return
		}
		text[field] = value
	}
	checkIn, okIn := parseDate(text["checkIn"])
	checkOut, okOut := parseDate(text["checkOut"])
	if !okIn || !okOut {
		problem(w, http.StatusBadRequest, "InvalidDates", dateDetail)
		return
	}
	index := slices.IndexFunc(hotelRooms, func(room rooms.Room) bool { return room.Number == text["room"] })
	if index < 0 {
		problem(w, http.StatusBadRequest, "UnknownRoom", fmt.Sprintf("There is no room %s.", text["room"]))
		return
	}

	var result reservations.BookingResult
	guest := reservations.Guest{Name: text["guestName"], Email: text["guestEmail"]}
	stay := reservations.DateRange{CheckIn: checkIn, CheckOut: checkOut}
	if err := unitOfWork(func(service *reservations.BookingService) { result = service.Reserve(guest, hotelRooms[index], stay) }); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	// After the unit of work: the booking is saved in PostgreSQL.
	if result.Outcome == "Accepted" {
		if err := cache.Invalidate(); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
	}
	toHttp(w, result)
})

mux.HandleFunc("DELETE /reservations/{id}", func(w http.ResponseWriter, r *http.Request) {
	id := r.PathValue("id")
	var cancelled bool
	if err := unitOfWork(func(service *reservations.BookingService) { cancelled = service.Cancel(id) }); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	if !cancelled {
		problem(w, http.StatusNotFound, "NotFound", fmt.Sprintf("There is no confirmed reservation %s.", id))
		return
	}
	// After the unit of work: the cancel is saved in PostgreSQL.
	if err := cache.Invalidate(); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	w.WriteHeader(http.StatusNoContent)
})
```

JavaScript:

```text
app.post('/reservations', async (request, response) => {
  const body = request.body;
  if (typeof body !== 'object' || body === null || !fields.every(field => typeof body[field] === 'string' && body[field] !== '')) {
    return problem(response, 400, 'InvalidRequest', bodyDetail);
  }
  const start = parseDate(body.checkIn);
  const end = parseDate(body.checkOut);
  if (!start || !end) return problem(response, 400, 'InvalidDates', dateDetail);
  const room = rooms.find(candidate => candidate.number === body.room);
  if (!room) return problem(response, 400, 'UnknownRoom', `There is no room ${body.room}.`);

  const result = await unitOfWork(service =>
    service.reserve(new Guest(body.guestName, body.guestEmail), room, new DateRange(start, end)));
  if (result.outcome === 'Accepted') await cache.invalidate(); // after the unit of work: the booking is saved in PostgreSQL
  toHttp(response, result);
});

app.delete('/reservations/:id', async (request, response) => {
  const cancelled = await unitOfWork(service => service.cancel(request.params.id));
  if (!cancelled) return problem(response, 404, 'NotFound', `There is no confirmed reservation ${request.params.id}.`);
  await cache.invalidate(); // after the unit of work: the cancel is saved in PostgreSQL
  response.status(204).end();
});
```

Dart:

```text
router.post('/reservations', (Request request) async {
  if (request.mimeType != 'application/json') return problem(400, 'InvalidRequest', bodyDetail);
  Object? json;
  try {
    json = jsonDecode(await request.readAsString());
  } on FormatException {
    return problem(400, 'InvalidRequest', bodyDetail);
  }
  final body = json is Map ? json : const {};
  if (!fields.every((field) => body[field] is String && body[field] != '')) {
    return problem(400, 'InvalidRequest', bodyDetail);
  }
  final checkIn = parseDate(body['checkIn']);
  final checkOut = parseDate(body['checkOut']);
  if (checkIn == null || checkOut == null) return problem(400, 'InvalidDates', dateDetail);
  final room = rooms.where((room) => room.number == body['room']).firstOrNull;
  if (room == null) return problem(400, 'UnknownRoom', 'There is no room ${body['room']}.');

  final guest = Guest(body['guestName'] as String, body['guestEmail'] as String);
  final result =
      await unitOfWork((service) => service.reserve(guest, room, DateRange(checkIn, checkOut)));
  if (result.outcome == 'Accepted') {
    await cache.invalidate(); // after the unit of work: the booking is saved in PostgreSQL
  }
  return toHttp(result);
});

router.delete('/reservations/<id>', (Request request, String id) async {
  final cancelled = await unitOfWork((service) => service.cancel(id));
  if (!cancelled) return problem(404, 'NotFound', 'There is no confirmed reservation $id.');
  await cache.invalidate(); // after the unit of work: the cancel is saved in PostgreSQL
  return Response(204);
});
```

### The booking and cancel endpoints

#### C#

```csharp
app.MapPost("/reservations", async (HttpRequest request) =>
{
    if (!request.HasJsonContentType()) return Problem(400, "InvalidRequest", BodyDetail);
    JsonElement body;
    try
    {
        body = (await JsonDocument.ParseAsync(request.Body)).RootElement;
    }
    catch (JsonException)
    {
        return Problem(400, "InvalidRequest", BodyDetail);
    }
    string? Text(string field) => body.ValueKind == JsonValueKind.Object && body.TryGetProperty(field, out var value)
        && value.ValueKind == JsonValueKind.String && value.GetString() is { Length: > 0 } text ? text : null;
    if (Fields.Any(field => Text(field) is null)) return Problem(400, "InvalidRequest", BodyDetail);
    if (ParseDate(Text("checkIn")) is not { } start || ParseDate(Text("checkOut")) is not { } end) return Problem(400, "InvalidDates", DateDetail);
    if (rooms.FirstOrDefault(room => room.Number == Text("room")) is not { } room) return Problem(400, "UnknownRoom", $"There is no room {Text("room")}.");

    var guest = new Guest(Text("guestName")!, Text("guestEmail")!);
    var answer = unitOfWork(service => ToHttp(service.Reserve(guest, room, new DateRange(start, end))));
    if (answer is IStatusCodeHttpResult { StatusCode: 201 }) cache.Invalidate(); // after the unit of work: the booking is saved in PostgreSQL
    return answer;
});

app.MapDelete("/reservations/{id}", (string id) =>
{
    var answer = unitOfWork(service => service.Cancel(id)
        ? Results.NoContent()
        : Problem(404, "NotFound", $"There is no confirmed reservation {id}."));
    if (answer is IStatusCodeHttpResult { StatusCode: 204 }) cache.Invalidate(); // after the unit of work: the cancel is saved in PostgreSQL
    return answer;
});
```

#### Python

```python
@app.post("/reservations")
def reserve(body: Any = Body(None)) -> JSONResponse:
    if not isinstance(body, dict) or not all(isinstance(body.get(field), str) and body[field] for field in FIELDS):
        return problem(400, "InvalidRequest", BODY_DETAIL)
    check_in, check_out = parse_date(body["checkIn"]), parse_date(body["checkOut"])
    if check_in is None or check_out is None:
        return problem(400, "InvalidDates", DATE_DETAIL)
    room = next((room for room in rooms if room.number == body["room"]), None)
    if room is None:
        return problem(400, "UnknownRoom", f"There is no room {body['room']}.")

    with unit_of_work() as service:
        result = service.reserve(Guest(body["guestName"], body["guestEmail"]), room, DateRange(check_in, check_out))
    if result.outcome == "Accepted":
        cache.invalidate()  # after the unit of work: the booking is saved in PostgreSQL
    return to_http(result)

@app.delete("/reservations/{reservation_id}")
def cancel(reservation_id: str) -> Response:
    with unit_of_work() as service:
        cancelled = service.cancel(reservation_id)
    if not cancelled:
        return problem(404, "NotFound", f"There is no confirmed reservation {reservation_id}.")
    cache.invalidate()  # after the unit of work: the cancel is saved in PostgreSQL
    return Response(status_code=204)
```

#### Java

```java
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
    ResponseEntity<?> answer = unitOfWork.run(service -> toHttp(service.reserve(guest, room, new DateRange(start, end))));
    if (answer.getStatusCode().value() == 201) cache.invalidate(); // after the unit of work: the booking is saved in PostgreSQL
    return answer;
}

@DeleteMapping("/reservations/{id}")
public ResponseEntity<?> cancel(@PathVariable String id) {
    ResponseEntity<?> answer = unitOfWork.run(service -> service.cancel(id)
        ? ResponseEntity.noContent().build()
        : problem(404, "NotFound", "There is no confirmed reservation " + id + "."));
    if (answer.getStatusCode().value() == 204) cache.invalidate(); // after the unit of work: the cancel is saved in PostgreSQL
    return answer;
}
```

#### Go

```go
mux.HandleFunc("POST /reservations", func(w http.ResponseWriter, r *http.Request) {
	var body map[string]any
	if strings.HasPrefix(r.Header.Get("Content-Type"), "application/json") {
		json.NewDecoder(r.Body).Decode(&body)
	}
	text := map[string]string{}
	for _, field := range fields {
		value, ok := body[field].(string)
		if !ok || value == "" {
			problem(w, http.StatusBadRequest, "InvalidRequest", bodyDetail)
			return
		}
		text[field] = value
	}
	checkIn, okIn := parseDate(text["checkIn"])
	checkOut, okOut := parseDate(text["checkOut"])
	if !okIn || !okOut {
		problem(w, http.StatusBadRequest, "InvalidDates", dateDetail)
		return
	}
	index := slices.IndexFunc(hotelRooms, func(room rooms.Room) bool { return room.Number == text["room"] })
	if index < 0 {
		problem(w, http.StatusBadRequest, "UnknownRoom", fmt.Sprintf("There is no room %s.", text["room"]))
		return
	}

	var result reservations.BookingResult
	guest := reservations.Guest{Name: text["guestName"], Email: text["guestEmail"]}
	stay := reservations.DateRange{CheckIn: checkIn, CheckOut: checkOut}
	if err := unitOfWork(func(service *reservations.BookingService) { result = service.Reserve(guest, hotelRooms[index], stay) }); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	// After the unit of work: the booking is saved in PostgreSQL.
	if result.Outcome == "Accepted" {
		if err := cache.Invalidate(); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
	}
	toHttp(w, result)
})

mux.HandleFunc("DELETE /reservations/{id}", func(w http.ResponseWriter, r *http.Request) {
	id := r.PathValue("id")
	var cancelled bool
	if err := unitOfWork(func(service *reservations.BookingService) { cancelled = service.Cancel(id) }); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	if !cancelled {
		problem(w, http.StatusNotFound, "NotFound", fmt.Sprintf("There is no confirmed reservation %s.", id))
		return
	}
	// After the unit of work: the cancel is saved in PostgreSQL.
	if err := cache.Invalidate(); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	w.WriteHeader(http.StatusNoContent)
})
```

#### JavaScript

```javascript
app.post('/reservations', async (request, response) => {
  const body = request.body;
  if (typeof body !== 'object' || body === null || !fields.every(field => typeof body[field] === 'string' && body[field] !== '')) {
    return problem(response, 400, 'InvalidRequest', bodyDetail);
  }
  const start = parseDate(body.checkIn);
  const end = parseDate(body.checkOut);
  if (!start || !end) return problem(response, 400, 'InvalidDates', dateDetail);
  const room = rooms.find(candidate => candidate.number === body.room);
  if (!room) return problem(response, 400, 'UnknownRoom', `There is no room ${body.room}.`);

  const result = await unitOfWork(service =>
    service.reserve(new Guest(body.guestName, body.guestEmail), room, new DateRange(start, end)));
  if (result.outcome === 'Accepted') await cache.invalidate(); // after the unit of work: the booking is saved in PostgreSQL
  toHttp(response, result);
});

app.delete('/reservations/:id', async (request, response) => {
  const cancelled = await unitOfWork(service => service.cancel(request.params.id));
  if (!cancelled) return problem(response, 404, 'NotFound', `There is no confirmed reservation ${request.params.id}.`);
  await cache.invalidate(); // after the unit of work: the cancel is saved in PostgreSQL
  response.status(204).end();
});
```

#### Dart

```dart
router.post('/reservations', (Request request) async {
  if (request.mimeType != 'application/json') return problem(400, 'InvalidRequest', bodyDetail);
  Object? json;
  try {
    json = jsonDecode(await request.readAsString());
  } on FormatException {
    return problem(400, 'InvalidRequest', bodyDetail);
  }
  final body = json is Map ? json : const {};
  if (!fields.every((field) => body[field] is String && body[field] != '')) {
    return problem(400, 'InvalidRequest', bodyDetail);
  }
  final checkIn = parseDate(body['checkIn']);
  final checkOut = parseDate(body['checkOut']);
  if (checkIn == null || checkOut == null) return problem(400, 'InvalidDates', dateDetail);
  final room = rooms.where((room) => room.number == body['room']).firstOrNull;
  if (room == null) return problem(400, 'UnknownRoom', 'There is no room ${body['room']}.');

  final guest = Guest(body['guestName'] as String, body['guestEmail'] as String);
  final result =
      await unitOfWork((service) => service.reserve(guest, room, DateRange(checkIn, checkOut)));
  if (result.outcome == 'Accepted') {
    await cache.invalidate(); // after the unit of work: the booking is saved in PostgreSQL
  }
  return toHttp(result);
});

router.delete('/reservations/<id>', (Request request, String id) async {
  final cancelled = await unitOfWork((service) => service.cancel(id));
  if (!cancelled) return problem(404, 'NotFound', 'There is no confirmed reservation $id.');
  await cache.invalidate(); // after the unit of work: the cancel is saved in PostgreSQL
  return Response(204);
});
```

### Completed example 6.2 · two cache tests

First the two expected answers, next to the other constants at the top of the test file; then the two tests at the end of it.

**The two cache tests** In short: One proves that a second search is answered by the cache; the other proves that a booking and a cancel make the next search fresh.

1. Test 1: search twice; the answers are equal; GET /stats says 2 searches, 2 database queries, 1 hit and 1 miss.
2. Test 2: search (R7, R8), book R7 (201), search (R8 only), cancel R101 (204), search (R7, R8).
3. Remove the invalidate line and test 2 fails at its second search: the stale R7 is back.

C#:

```text
[Fact]
public async Task The_second_search_is_answered_by_the_cache()
{
    var client = await NewClient();
    var first = await client.GetStringAsync(Search);
    Assert.Equal(first, await client.GetStringAsync(Search));
    Assert.Equal("""{"searches":2,"databaseQueries":2,"cacheHits":1,"cacheMisses":1}""", await client.GetStringAsync("/stats"));
}

[Fact]
public async Task A_booking_and_a_cancel_make_the_next_search_fresh()
{
    var client = await NewClient();
    Assert.Equal(BothFree, await client.GetStringAsync(Search));
    Assert.Equal(HttpStatusCode.Created, (await client.PostAsJsonAsync("/reservations", Maya)).StatusCode);
    Assert.Equal(OnlyR8, await client.GetStringAsync(Search));
    Assert.Equal(HttpStatusCode.NoContent, (await client.DeleteAsync("/reservations/R101")).StatusCode);
    Assert.Equal(BothFree, await client.GetStringAsync(Search));
}
```

Python:

```text
def test_the_second_search_is_answered_by_the_cache(self):
    client = new_client()
    first = client.get(SEARCH)
    second = client.get(SEARCH)
    self.assertEqual(first.text, second.text)
    self.assertEqual('{"searches":2,"databaseQueries":2,"cacheHits":1,"cacheMisses":1}', client.get("/stats").text)

def test_a_booking_and_a_cancel_make_the_next_search_fresh(self):
    client = new_client()
    self.assertEqual(BOTH_FREE, client.get(SEARCH).text)
    self.assertEqual(201, client.post("/reservations", json=MAYA).status_code)
    self.assertEqual(ONLY_R8, client.get(SEARCH).text)
    self.assertEqual(204, client.delete("/reservations/R101").status_code)
    self.assertEqual(BOTH_FREE, client.get(SEARCH).text)
```

Java:

```text
@Test
void theSecondSearchIsAnsweredByTheCache() throws Exception {
    MockMvc app = newApp();
    String first = app.perform(get(SEARCH)).andReturn().getResponse().getContentAsString();
    String second = app.perform(get(SEARCH)).andReturn().getResponse().getContentAsString();
    assertEquals(first, second);
    app.perform(get("/stats")).andExpect(content().string("""
        {"searches":2,"databaseQueries":2,"cacheHits":1,"cacheMisses":1}"""));
}

@Test
void aBookingAndACancelMakeTheNextSearchFresh() throws Exception {
    MockMvc app = newApp();
    app.perform(get(SEARCH)).andExpect(content().string(BOTH_FREE));
    app.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content(MAYA)).andExpect(status().isCreated());
    app.perform(get(SEARCH)).andExpect(content().string(ONLY_R8));
    app.perform(delete("/reservations/R101")).andExpect(status().isNoContent());
    app.perform(get(SEARCH)).andExpect(content().string(BOTH_FREE));
}
```

Go:

```text
func TestTheSecondSearchIsAnsweredByTheCache(t *testing.T) {
	app := newApp()
	first := send(app, "GET", search, "").Body.String()
	expect(t, send(app, "GET", search, ""), 200, first)
	expect(t, send(app, "GET", "/stats", ""), 200, `{"searches":2,"databaseQueries":2,"cacheHits":1,"cacheMisses":1}`)
}

func TestABookingAndACancelMakeTheNextSearchFresh(t *testing.T) {
	app := newApp()
	expect(t, send(app, "GET", search, ""), 200, bothFree)
	expect(t, send(app, "POST", "/reservations", maya), 201, `"id":"R101"`)
	expect(t, send(app, "GET", search, ""), 200, onlyR8)
	expect(t, send(app, "DELETE", "/reservations/R101", ""), 204, "")
	expect(t, send(app, "GET", search, ""), 200, bothFree)
}
```

JavaScript:

```text
test('the second search is answered by the cache', async t => {
  const send = await startApp(t);
  const first = await (await send('GET', search)).text();
  assert.equal(await (await send('GET', search)).text(), first);
  assert.equal(await (await send('GET', '/stats')).text(), '{"searches":2,"databaseQueries":2,"cacheHits":1,"cacheMisses":1}');
});

test('a booking and a cancel make the next search fresh', async t => {
  const send = await startApp(t);
  assert.equal(await (await send('GET', search)).text(), bothFree);
  assert.equal((await send('POST', '/reservations', maya)).status, 201);
  assert.equal(await (await send('GET', search)).text(), onlyR8);
  assert.equal((await send('DELETE', '/reservations/R101')).status, 204);
  assert.equal(await (await send('GET', search)).text(), bothFree);
});
```

Dart:

```text
test('the second search is answered by the cache', () async {
  final app = newApp();
  final first = await send(app, 'GET', search);
  final second = await send(app, 'GET', search);
  expect(await second.readAsString(), await first.readAsString());
  expect(await (await send(app, 'GET', '/stats')).readAsString(),
      '{"searches":2,"databaseQueries":2,"cacheHits":1,"cacheMisses":1}');
});

test('a booking and a cancel make the next search fresh', () async {
  final app = newApp();
  expect(await (await send(app, 'GET', search)).readAsString(), bothFree);
  expect((await send(app, 'POST', '/reservations', maya)).statusCode, 201);
  expect(await (await send(app, 'GET', search)).readAsString(), onlyR8);
  expect((await send(app, 'DELETE', '/reservations/R101')).statusCode, 204);
  expect(await (await send(app, 'GET', search)).readAsString(), bothFree);
});
```

### The two expected answers and the two new API tests

#### C#

```csharp
private const string BothFree = """{"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}""";
private const string OnlyR8 = """{"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R8","type":"Deluxe"}],"next":null}""";

[Fact]
public async Task The_second_search_is_answered_by_the_cache()
{
    var client = await NewClient();
    var first = await client.GetStringAsync(Search);
    Assert.Equal(first, await client.GetStringAsync(Search));
    Assert.Equal("""{"searches":2,"databaseQueries":2,"cacheHits":1,"cacheMisses":1}""", await client.GetStringAsync("/stats"));
}

[Fact]
public async Task A_booking_and_a_cancel_make_the_next_search_fresh()
{
    var client = await NewClient();
    Assert.Equal(BothFree, await client.GetStringAsync(Search));
    Assert.Equal(HttpStatusCode.Created, (await client.PostAsJsonAsync("/reservations", Maya)).StatusCode);
    Assert.Equal(OnlyR8, await client.GetStringAsync(Search));
    Assert.Equal(HttpStatusCode.NoContent, (await client.DeleteAsync("/reservations/R101")).StatusCode);
    Assert.Equal(BothFree, await client.GetStringAsync(Search));
}
```

#### Python

```python
BOTH_FREE = ('{"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},'
             '{"number":"R8","type":"Deluxe"}],"next":null}')
ONLY_R8 = '{"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R8","type":"Deluxe"}],"next":null}'

def test_the_second_search_is_answered_by_the_cache(self):
    client = new_client()
    first = client.get(SEARCH)
    second = client.get(SEARCH)
    self.assertEqual(first.text, second.text)
    self.assertEqual('{"searches":2,"databaseQueries":2,"cacheHits":1,"cacheMisses":1}', client.get("/stats").text)

def test_a_booking_and_a_cancel_make_the_next_search_fresh(self):
    client = new_client()
    self.assertEqual(BOTH_FREE, client.get(SEARCH).text)
    self.assertEqual(201, client.post("/reservations", json=MAYA).status_code)
    self.assertEqual(ONLY_R8, client.get(SEARCH).text)
    self.assertEqual(204, client.delete("/reservations/R101").status_code)
    self.assertEqual(BOTH_FREE, client.get(SEARCH).text)
```

#### Java

```java
static final String BOTH_FREE = """
    {"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}""";
static final String ONLY_R8 = """
    {"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R8","type":"Deluxe"}],"next":null}""";

@Test
void theSecondSearchIsAnsweredByTheCache() throws Exception {
    MockMvc app = newApp();
    String first = app.perform(get(SEARCH)).andReturn().getResponse().getContentAsString();
    String second = app.perform(get(SEARCH)).andReturn().getResponse().getContentAsString();
    assertEquals(first, second);
    app.perform(get("/stats")).andExpect(content().string("""
        {"searches":2,"databaseQueries":2,"cacheHits":1,"cacheMisses":1}"""));
}

@Test
void aBookingAndACancelMakeTheNextSearchFresh() throws Exception {
    MockMvc app = newApp();
    app.perform(get(SEARCH)).andExpect(content().string(BOTH_FREE));
    app.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content(MAYA)).andExpect(status().isCreated());
    app.perform(get(SEARCH)).andExpect(content().string(ONLY_R8));
    app.perform(delete("/reservations/R101")).andExpect(status().isNoContent());
    app.perform(get(SEARCH)).andExpect(content().string(BOTH_FREE));
}
```

#### Go

```go
const bothFree = `{"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}`
const onlyR8 = `{"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R8","type":"Deluxe"}],"next":null}`

func TestTheSecondSearchIsAnsweredByTheCache(t *testing.T) {
	app := newApp()
	first := send(app, "GET", search, "").Body.String()
	expect(t, send(app, "GET", search, ""), 200, first)
	expect(t, send(app, "GET", "/stats", ""), 200, `{"searches":2,"databaseQueries":2,"cacheHits":1,"cacheMisses":1}`)
}

func TestABookingAndACancelMakeTheNextSearchFresh(t *testing.T) {
	app := newApp()
	expect(t, send(app, "GET", search, ""), 200, bothFree)
	expect(t, send(app, "POST", "/reservations", maya), 201, `"id":"R101"`)
	expect(t, send(app, "GET", search, ""), 200, onlyR8)
	expect(t, send(app, "DELETE", "/reservations/R101", ""), 204, "")
	expect(t, send(app, "GET", search, ""), 200, bothFree)
}
```

#### JavaScript

```javascript
const bothFree = '{"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}';
const onlyR8 = '{"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R8","type":"Deluxe"}],"next":null}';

test('the second search is answered by the cache', async t => {
  const send = await startApp(t);
  const first = await (await send('GET', search)).text();
  assert.equal(await (await send('GET', search)).text(), first);
  assert.equal(await (await send('GET', '/stats')).text(), '{"searches":2,"databaseQueries":2,"cacheHits":1,"cacheMisses":1}');
});

test('a booking and a cancel make the next search fresh', async t => {
  const send = await startApp(t);
  assert.equal(await (await send('GET', search)).text(), bothFree);
  assert.equal((await send('POST', '/reservations', maya)).status, 201);
  assert.equal(await (await send('GET', search)).text(), onlyR8);
  assert.equal((await send('DELETE', '/reservations/R101')).status, 204);
  assert.equal(await (await send('GET', search)).text(), bothFree);
});
```

#### Dart

```dart
const bothFree =
    '{"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}';
const onlyR8 =
    '{"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R8","type":"Deluxe"}],"next":null}';

test('the second search is answered by the cache', () async {
  final app = newApp();
  final first = await send(app, 'GET', search);
  final second = await send(app, 'GET', search);
  expect(await second.readAsString(), await first.readAsString());
  expect(await (await send(app, 'GET', '/stats')).readAsString(),
      '{"searches":2,"databaseQueries":2,"cacheHits":1,"cacheMisses":1}');
});

test('a booking and a cancel make the next search fresh', () async {
  final app = newApp();
  expect(await (await send(app, 'GET', search)).readAsString(), bothFree);
  expect((await send(app, 'POST', '/reservations', maya)).statusCode, 201);
  expect(await (await send(app, 'GET', search)).readAsString(), onlyR8);
  expect((await send(app, 'DELETE', '/reservations/R101')).statusCode, 204);
  expect(await (await send(app, 'GET', search)).readAsString(), bothFree);
});
```

### Green at the end of Step 6

### Green: 8 behaviour + 3 architecture + 6 API

#### C#

```text
Passed!  - Failed: 0, Passed: 17, Skipped: 0, Total: 17
```

#### Python

```text
python -m unittest
Ran 14 tests
OK

lint-imports
Contracts: 3 kept, 0 broken.
```

#### Java

```text
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
```

#### Go

```text
go test ./...
ok      hotelbooking                 (3 architecture tests)
ok      hotelbooking/api             (6 API tests)
ok      hotelbooking/reservations    (8 booking tests)
```

#### JavaScript

```text
node --test
ℹ tests 17
ℹ pass 17
ℹ fail 0
```

#### Dart

```text
dart test
+17: All tests passed!
```

### The four requests of Step 5 again

**The four requests of Step 5 again, after Step 6** In short: The same requests now give a fresh third answer: the booking raised the version, so the search asked PostgreSQL.

1. Compare the third answer with Step 5: R8 only.

```text
GET /rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14
200 {"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}

POST /reservations   {"guestName":"Maya","guestEmail":"maya@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}
201 {"id":"R101","status":"Confirmed","notificationFailed":false}

GET /rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14
200 {"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R8","type":"Deluxe"}],"next":null}

POST /reservations   {"guestName":"Omar","guestEmail":"omar@example.com","room":"R7","checkIn":"2026-11-13","checkOut":"2026-11-15"}
409 {"title":"Unavailable","status":409,"detail":"The room is booked for at least one night of this stay."}
```

### Status and body of each answer

```text
GET /rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14
200 {"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}

POST /reservations   {"guestName":"Maya","guestEmail":"maya@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}
201 {"id":"R101","status":"Confirmed","notificationFailed":false}

GET /rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14
200 {"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R8","type":"Deluxe"}],"next":null}

POST /reservations   {"guestName":"Omar","guestEmail":"omar@example.com","room":"R7","checkIn":"2026-11-13","checkOut":"2026-11-15"}
409 {"title":"Unavailable","status":409,"detail":"The room is booked for at least one night of this stay."}
```

### Your turn · invalidate

1. Add the invalidate line to POST (only for an accepted booking) and to DELETE (only for 204), after the unit of work (6.1).
2. Add the two expected answers and the two cache tests (6.2). Run your checks: 17 green (Python: 14 tests, then “Contracts: 3 kept”).
3. Remove the invalidate line from POST, run the tests and watch the second new test fail; put it back.
4. Stop the app, docker compose down -v and up -d, start the app, and send the four requests of Step 5 again: the third answer is R8 only.
5. Add the section “After invalidation” to docs/cache-experiment.md.

**When you finish, you have:** invalidation after every change, 17 green checks, and a fresh third answer.
### Explanation

The cache now has both protections from L04: invalidation makes the search right at once after a change, and the TTL limits any copy that is ever missed to 30 seconds.



---

## 8. Step 7 · Hot key and the next bottleneck

The cache solved the search. Look at what happens on the busiest days, and at what now limits the app.





### New Year, with the cache

| What happens | What it means |
| --- | --- |
| Everyone searches 31 Dec–2 Jan | One key, availability:v{version}:2026-12-31:2027-01-02, answers almost every search: a hot key |
| Every New Year booking raises the version | The copy is thrown away after each booking; the next search asks PostgreSQL |
| Searches cost Redis, not PostgreSQL | What is left for PostgreSQL is mostly POST /reservations: 3 questions per booking |
| Two bookings for R7 at the same moment | Both can still pass the check (the P03 race): P09 fixes it in PostgreSQL |

**A hot key: one cache entry everyone wants** In short: A hot key is one key that gets most of the requests, so the Redis server holding it does most of the work.

1. In December most guests search 31 Dec–2 Jan: one key answers almost every search.
2. Even when Redis runs on several servers, one key lives on one of them.
3. Every New Year booking raises the version, so that copy is thrown away after every booking and the next search goes to PostgreSQL.
4. Fixes for later: a short copy inside each app instance, or several copies of the key.

C#:

```text
dotnet run tools/measure.cs 2026-12-31 2027-01-02
docker compose exec cache redis-cli KEYS "availability:*"
```

Python:

```text
python tools/measure.py 2026-12-31 2027-01-02
docker compose exec cache redis-cli KEYS "availability:*"
```

Java:

```text
java tools/Measure.java 2026-12-31 2027-01-02
docker compose exec cache redis-cli KEYS "availability:*"
```

Go:

```text
go run ./tools/measure 2026-12-31 2027-01-02
docker compose exec cache redis-cli KEYS "availability:*"
```

JavaScript:

```text
node tools/measure.js 2026-12-31 2027-01-02
docker compose exec cache redis-cli KEYS "availability:*"
```

Dart:

```text
dart run tool/measure.dart 2026-12-31 2027-01-02
docker compose exec cache redis-cli KEYS "availability:*"
```

**The bottleneck moves** In short: The bottleneck is the part that limits the whole system; when you remove it, the next-slowest part becomes the new one.

1. Before the cache: 1,000 searches send 2,000 questions to PostgreSQL. Searches are the bottleneck.
2. After the cache: the same searches send 2. Searches cost Redis, not PostgreSQL.
3. What is left for PostgreSQL is mostly POST /reservations: 3 questions per booking, and they must be exact.
4. Two bookings for R7 at the same moment can still both pass the check: P09 fixes that in PostgreSQL.

### Try it: measure the New Year search

### Terminal 2

#### C#

```text
dotnet run tools/measure.cs 2026-12-31 2027-01-02
docker compose exec cache redis-cli KEYS "availability:*"
```

#### Python

```text
python tools/measure.py 2026-12-31 2027-01-02
docker compose exec cache redis-cli KEYS "availability:*"
```

#### Java

```text
java tools/Measure.java 2026-12-31 2027-01-02
docker compose exec cache redis-cli KEYS "availability:*"
```

#### Go

```text
go run ./tools/measure 2026-12-31 2027-01-02
docker compose exec cache redis-cli KEYS "availability:*"
```

#### JavaScript

```text
node tools/measure.js 2026-12-31 2027-01-02
docker compose exec cache redis-cli KEYS "availability:*"
```

#### Dart

```text
dart run tool/measure.dart 2026-12-31 2027-01-02
docker compose exec cache redis-cli KEYS "availability:*"
```

### Completed example: “Hot key and the next bottleneck” in docs/cache-experiment.md

### docs/cache-experiment.md (Hot key)

```text
## Hot key and the next bottleneck (Step 7)

- New Year: everyone searches 31 Dec–2 Jan, so one key answers almost every search:
  availability:v{version}:2026-12-31:2027-01-02.
- Every booking raises the version, so in a busy sale each booking throws that copy
  away and the next search asks PostgreSQL again.
- Searches now cost Redis, not PostgreSQL. What is left for PostgreSQL is
  POST /reservations: 3 questions per booking, and they must be exact.
  That is the next bottleneck: P09.
```

### Your turn · find the next bottleneck

1. Run the measurement tool with 2026-12-31 2027-01-02: 999 hits again.
2. Run the KEYS line: one key for each stay you searched, all with the same version, plus availability:version itself.
3. Book one New Year stay with curl.exe (change the dates in a copy of maya-r7.json), search 31 Dec–2 Jan once with curl.exe, and run KEYS again: a new key with the next version next to the old one, which disappears when its 30 seconds are over.
4. Add the section “Hot key and the next bottleneck” to docs/cache-experiment.md in your own words.

**When you finish, you have:** the hot key and the next bottleneck explained in docs/cache-experiment.md.
### Explanation

This is the rhythm of system design from L04: estimate, find the bottleneck, remove it, measure, and look for the next one. P08 does one full turn.



---

## 9. Step 8 · Write ADR-002

Write the cache decision down, with its reasons and costs, next to ADR-001.





### What ADR-002 records

| Section | What it says |
| --- | --- |
| Context | 2,000 queries for 1,000 searches; the booking rule must never depend on a copy |
| Decision | Cache-aside for the search only; TTL 30 s; a version raised by every booking and cancel; POST and DELETE never read the cache |
| Alternatives considered | No cache, TTL only, invalidation only |
| Consequences | 2 queries instead of 2,000; a busy sale empties the cache often; Redis is one more program, and while it is down searches get no answer |
| Revisit when | Bookings limit the app (P09), or the hit rate under load (P23) is much lower |

**ADR-002: why the decision is written down** In short: An architecture decision record says what was decided, why, what else was considered, what it costs, and when to look at it again. ADR-001 (P06) recorded the modular monolith. ADR-002 records the cache, so whoever changes it later knows that booking must never read it.

1. Context: 2,000 queries for 1,000 searches; the booking rule must never depend on an old copy.
2. Decision: cache-aside for the search only, TTL 30 s, a version raised by every booking and cancel.
3. Consequences: 2 queries instead of 2,000; a busy sale empties the cache often; Redis is one more program.

**What happens if Redis stops** In short: The app starts only when Redis answers. While Redis is down, searches get no answer, and a booking or cancel is saved in PostgreSQL but answers with an error, because the version cannot be raised. How long a search waits before it fails depends on the language’s Redis client. A fallback (ask PostgreSQL when Redis does not answer) is what a production app adds; ADR-002 names this as a cost of the decision.

1. docker compose stop cache: the next search fails instead of answering 200.
2. Maya books R7: PostgreSQL saves R101, but the answer is an error. If she tries again she gets 409, because R7 is hers.
3. Start Redis again (docker compose start cache) and restart the app: everything works again.

### Completed example: ADR-002

### docs/adr-002-cache-availability.md

```text
# ADR-002: Cache the free-room search in Redis

Status: Accepted, 7 November 2026

## Context
Every search asks PostgreSQL once per room: 2,000 queries for 1,000 searches with
two rooms, and 133 a second at the estimated peak with 40 rooms (docs/capacity.md).
A search answer changes only when someone books or cancels. The booking rule (S2)
must never depend on an old copy.

## Decision
Cache-aside in Redis for GET /rooms/availability only. The key is
availability:v{version}:{checkIn}:{checkOut}, the value is the free room numbers,
the TTL is 30 seconds. A successful booking or cancel raises availability:version.
POST and DELETE never read the cache: PostgreSQL is the source of truth.

## Alternatives considered
- No cache: always fresh and simplest, but every search costs one query per room.
- TTL only: simple, but a search can show a booked room for up to 30 seconds.
- Invalidation only: fresh after every change, but one missed invalidation keeps
  a wrong copy until Redis restarts.

## Consequences
+ 1,000 searches for the same stay cost 2 database queries instead of 2,000
  (docs/cache-experiment.md).
+ If an invalidation is ever missed, the copy is wrong for at most 30 seconds.
- Every booking empties every cached search: in a busy sale the hit rate falls.
- Redis is one more program to run. While it is down, searches get no answer,
  and a booking or cancel is saved but answers with an error, because the version
  cannot be raised. There is no fallback to PostgreSQL yet.
- Two misses at the same moment both ask PostgreSQL.

## Revisit when
Bookings, not searches, limit the app (P09), or the hit rate measured under
load (P23) is much lower than in docs/cache-experiment.md.

```

### Your turn · write ADR-002

1. Create docs/adr-002-cache-availability.md.
2. Copy the headings of the completed example.
3. Write each section in your own words, with your own numbers from docs/cache-experiment.md.

**When you finish, you have:** docs/adr-002-cache-availability.md next to ADR-001.
### Explanation

ADR-001 recorded why the app is a modular monolith; ADR-002 records why the search is cached and why booking is not. A later change to the cache starts by reading it.



---

## 10. Check your project and see what comes next

Run everything once more, check your folder against the list, and see the problem P09 solves.





### Run everything

### Commands

#### C#

```text
docker compose up -d
dotnet test HotelBooking.Tests
dotnet run --project HotelBooking -- --demo
dotnet run --project HotelBooking -- --race
dotnet run --project HotelBooking            # the API on http://localhost:5080
dotnet run tools/measure.cs            # in a second terminal
```

#### Python

```text
docker compose up -d
python -m unittest -v
lint-imports
python main.py --demo
python main.py --race
python main.py            # the API on http://localhost:5080
python tools/measure.py            # in a second terminal
```

#### Java

```text
docker compose up -d
mvn -q test
mvn -q package
java -jar target/hotel-booking-1.0.jar --demo
java -jar target/hotel-booking-1.0.jar --race
mvn -q spring-boot:run            # the API on http://localhost:5080
java tools/Measure.java            # in a second terminal
```

#### Go

```text
docker compose up -d
go test -v ./...
go run . --demo
go run . --race
go run .            # the API on http://localhost:5080
go run ./tools/measure            # in a second terminal
```

#### JavaScript

```text
docker compose up -d
node --test
node main.js --demo
node main.js --race
node main.js            # the API on http://localhost:5080
node tools/measure.js            # in a second terminal
```

#### Dart

```text
docker compose up -d
dart test
dart run bin/main.dart --demo
dart run bin/main.dart --race
dart run bin/main.dart            # the API on http://localhost:5080
dart run tool/measure.dart            # in a second terminal
```

### The race still double-books (P09 fixes it)

### Expected race output

```text
Maya and Omar both book R7 for 12-14 Nov at the same moment...
Maya: Accepted R101
Omar: Accepted R102
Confirmed reservations for R7 on 12-14 Nov: 2
Double booking! Both requests checked before either one saved.
```

### Your P08 checklist

| File or page | It is finished when |
| --- | --- |
| docs/openapi.yaml | Version 1.1.0 with GET /stats and the Stats schema. |
| docker-compose.yml | db and cache start with docker compose up -d; redis-cli ping answers PONG. |
| The Api folder | Stats, the counting store, the cache file; the search reads Redis first; POST and DELETE raise the version. |
| The measurement tool | Prints the average, p95, total and the GET /stats difference. |
| Checks | 17 green: 8 behaviour + 3 architecture + 6 API tests (Python: 14 tests and 3 contracts). |
| docs/cache-experiment.md | Two rows of measurements, the stale result, after invalidation, the hot key. |
| docs/adr-002-cache-availability.md | Context, decision, alternatives, consequences, revisit when. |
| docs/design-notes.md | P08 sections: Cache rule, After. |

### Where the hotel app goes next

| Lesson | What changes in the hotel app |
| --- | --- |
| L05 | Transactions, locks and isolation levels: why two bookings at the same moment can both pass the check. |
| P09 | You reproduce the double booking with concurrent HTTP calls and fix it in PostgreSQL: a lock, optimistic concurrency, or an exclusion constraint. |
| P10 | The confirmation email moves out of the request into a background worker. |

### The open question P08 leaves for P09

Searches no longer load the database; bookings now decide its limit. And two bookings for R7 that arrive at exactly the same moment can still both pass the check: the race from P03 is still there.

### Completed example: the finished P08 folder

### hotel-booking folder

#### C#

```text
hotel-booking/   end of P08
├── db/
│   └── init.sql
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md   NEW: the cache policy
│   ├── api-check.md
│   ├── cache-experiment.md   NEW: the measurements and the stale result
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   CHANGED: + GET /stats
│   ├── quality-scenarios.md
│   └── requirements.md
├── HotelBooking/
│   ├── Api/
│   │   ├── AvailabilityCache.cs   NEW
│   │   ├── HotelApi.cs   CHANGED: GET /stats, the cache, invalidation
│   │   └── Stats.cs   NEW
│   ├── Notifications/
│   │   ├── Internal/
│   │   │   ├── EmailNotificationSender.cs
│   │   │   └── SmsNotificationSender.cs
│   │   ├── Confirmation.cs
│   │   └── INotificationSender.cs
│   ├── Payments/
│   │   ├── Internal/
│   │   │   └── SimulatedPaymentService.cs
│   │   ├── IPaymentService.cs
│   │   └── PaymentStatus.cs
│   ├── Reservations/
│   │   ├── Internal/
│   │   │   ├── InMemoryReservationStore.cs
│   │   │   └── PostgresReservationStore.cs
│   │   ├── BookingResult.cs
│   │   ├── BookingService.cs
│   │   ├── DateRange.cs
│   │   ├── Guest.cs
│   │   ├── IReservationStore.cs
│   │   ├── Reservation.cs
│   │   └── ReservationStatus.cs
│   ├── Rooms/
│   │   ├── Internal/
│   │   │   ├── InMemoryRoomCatalog.cs
│   │   │   └── PostgresRoomCatalog.cs
│   │   ├── IRoomCatalog.cs
│   │   └── Room.cs
│   ├── HotelBooking.csproj   CHANGED: + StackExchange.Redis
│   └── Program.cs   CHANGED: the wiring connects to Redis and counts the queries
├── HotelBooking.Tests/
│   ├── ApiTests.cs   CHANGED: + 2 cache tests
│   ├── ArchitectureTests.cs
│   ├── BookingServiceTests.cs
│   └── HotelBooking.Tests.csproj
├── tools/
│   └── measure.cs   NEW
└── docker-compose.yml   CHANGED: + the Redis cache
```

#### Python

```text
hotel-booking/   end of P08
├── db/
│   └── init.sql
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md   NEW: the cache policy
│   ├── api-check.md
│   ├── cache-experiment.md   NEW: the measurements and the stale result
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   CHANGED: + GET /stats
│   ├── quality-scenarios.md
│   └── requirements.md
├── hotel_booking/
│   ├── notifications/
│   │   ├── __init__.py
│   │   └── internal.py
│   ├── payments/
│   │   ├── __init__.py
│   │   └── internal.py
│   ├── reservations/
│   │   ├── __init__.py
│   │   ├── booking.py
│   │   ├── domain.py
│   │   └── internal.py
│   ├── rooms/
│   │   ├── __init__.py
│   │   ├── catalog.py
│   │   └── internal.py
│   ├── __init__.py
│   ├── api_cache.py   NEW
│   └── api.py   CHANGED: GET /stats, the cache, invalidation
├── tools/
│   └── measure.py   NEW
├── docker-compose.yml   CHANGED: + the Redis cache
├── main.py   CHANGED: the wiring connects to Redis and counts the queries
├── pyproject.toml
├── requirements.txt   CHANGED: + redis
├── test_api.py   CHANGED: + 2 cache tests
└── test_booking_service.py
```

#### Java

```text
hotel-booking/   end of P08
├── db/
│   └── init.sql
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md   NEW: the cache policy
│   ├── api-check.md
│   ├── cache-experiment.md   NEW: the measurements and the stale result
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   CHANGED: + GET /stats
│   ├── quality-scenarios.md
│   └── requirements.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── hotelbooking/
│   │   │       ├── api/
│   │   │       │   ├── AvailabilityCache.java   NEW
│   │   │       │   ├── CountingReservationStore.java   NEW
│   │   │       │   ├── HotelApi.java   CHANGED: GET /stats, the cache, invalidation
│   │   │       │   └── Stats.java   NEW
│   │   │       ├── notifications/
│   │   │       │   ├── internal/
│   │   │       │   │   ├── EmailNotificationSender.java
│   │   │       │   │   └── SmsNotificationSender.java
│   │   │       │   ├── Confirmation.java
│   │   │       │   └── NotificationSender.java
│   │   │       ├── payments/
│   │   │       │   ├── internal/
│   │   │       │   │   └── SimulatedPaymentService.java
│   │   │       │   ├── PaymentService.java
│   │   │       │   └── PaymentStatus.java
│   │   │       ├── reservations/
│   │   │       │   ├── internal/
│   │   │       │   │   ├── InMemoryReservationStore.java
│   │   │       │   │   └── PostgresReservationStore.java
│   │   │       │   ├── BookingResult.java
│   │   │       │   ├── BookingService.java
│   │   │       │   ├── DateRange.java
│   │   │       │   ├── Guest.java
│   │   │       │   ├── Reservation.java
│   │   │       │   ├── ReservationStatus.java
│   │   │       │   └── ReservationStore.java
│   │   │       ├── rooms/
│   │   │       │   ├── internal/
│   │   │       │   │   ├── InMemoryRoomCatalog.java
│   │   │       │   │   └── PostgresRoomCatalog.java
│   │   │       │   ├── Room.java
│   │   │       │   └── RoomCatalog.java
│   │   │       └── Main.java   CHANGED: the wiring connects to Redis and counts the queries
│   │   └── resources/
│   │       └── application.properties   CHANGED: + the Redis port
│   └── test/
│       └── java/
│           └── hotelbooking/
│               ├── ApiTests.java   CHANGED: + 2 cache tests
│               ├── ArchitectureTests.java
│               └── BookingServiceTests.java
├── tools/
│   └── Measure.java   NEW
├── docker-compose.yml   CHANGED: + the Redis cache
└── pom.xml   CHANGED: + spring-boot-starter-data-redis
```

#### Go

```text
hotel-booking/   end of P08
├── api/
│   ├── api_test.go   CHANGED: + 2 cache tests
│   ├── api.go   CHANGED: GET /stats, the cache, invalidation
│   ├── cache.go   NEW
│   └── stats.go   NEW
├── db/
│   └── init.sql
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md   NEW: the cache policy
│   ├── api-check.md
│   ├── cache-experiment.md   NEW: the measurements and the stale result
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   CHANGED: + GET /stats
│   ├── quality-scenarios.md
│   └── requirements.md
├── notifications/
│   ├── internal.go
│   └── notifications.go
├── payments/
│   ├── internal.go
│   └── payments.go
├── reservations/
│   ├── booking_test.go
│   ├── booking.go
│   ├── domain.go
│   └── internal.go
├── rooms/
│   ├── internal.go
│   └── rooms.go
├── tools/
│   └── measure/
│       └── main.go   NEW
├── architecture_test.go
├── docker-compose.yml   CHANGED: + the Redis cache
├── go.mod   CHANGED: + go-redis
├── go.sum   CHANGED: written by go get
└── main.go   CHANGED: the wiring connects to Redis and counts the queries
```

#### JavaScript

```text
hotel-booking/   end of P08
├── db/
│   └── init.sql
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md   NEW: the cache policy
│   ├── api-check.md
│   ├── cache-experiment.md   NEW: the measurements and the stale result
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   CHANGED: + GET /stats
│   ├── quality-scenarios.md
│   └── requirements.md
├── src/
│   ├── notifications/
│   │   ├── internal/
│   │   │   ├── email.js
│   │   │   └── sms.js
│   │   └── index.js
│   ├── payments/
│   │   ├── internal/
│   │   │   └── simulated.js
│   │   └── index.js
│   ├── reservations/
│   │   ├── internal/
│   │   │   ├── postgres-store.js
│   │   │   └── store.js
│   │   ├── booking.js
│   │   ├── domain.js
│   │   └── index.js
│   ├── rooms/
│   │   ├── internal/
│   │   │   ├── catalog.js
│   │   │   └── postgres-catalog.js
│   │   └── index.js
│   ├── api-cache.js   NEW
│   └── api.js   CHANGED: GET /stats, the cache, invalidation
├── test/
│   ├── api.test.js   CHANGED: + 2 cache tests
│   ├── architecture.test.js
│   └── booking.test.js
├── tools/
│   └── measure.js   NEW
├── docker-compose.yml   CHANGED: + the Redis cache
├── main.js   CHANGED: the wiring connects to Redis and counts the queries
├── package-lock.json   CHANGED: written by npm install
└── package.json   CHANGED: + redis
```

#### Dart

```text
hotel-booking/   end of P08
├── bin/
│   └── main.dart   CHANGED: the wiring connects to Redis and counts the queries
├── db/
│   └── init.sql
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md   NEW: the cache policy
│   ├── api-check.md
│   ├── cache-experiment.md   NEW: the measurements and the stale result
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   CHANGED: + GET /stats
│   ├── quality-scenarios.md
│   └── requirements.md
├── lib/
│   ├── notifications/
│   │   ├── src/
│   │   │   ├── email.dart
│   │   │   └── sms.dart
│   │   └── notifications.dart
│   ├── payments/
│   │   ├── src/
│   │   │   └── simulated.dart
│   │   └── payments.dart
│   ├── reservations/
│   │   ├── src/
│   │   │   ├── booking.dart
│   │   │   ├── domain.dart
│   │   │   ├── postgres_store.dart
│   │   │   └── store.dart
│   │   └── reservations.dart
│   ├── rooms/
│   │   ├── src/
│   │   │   ├── catalog.dart
│   │   │   └── postgres_catalog.dart
│   │   └── rooms.dart
│   ├── api_cache.dart   NEW
│   └── api.dart   CHANGED: GET /stats, the cache, invalidation
├── test/
│   ├── api_test.dart   CHANGED: + 2 cache tests
│   ├── architecture_test.dart
│   └── booking_service_test.dart
├── tool/
│   └── measure.dart   NEW
├── analysis_options.yaml
├── docker-compose.yml   CHANGED: + the Redis cache
└── pubspec.yaml   CHANGED: + redis
```

### Your turn · check and hand over

1. Run the commands above: 17 green, the demo, the race, the API, then the measurement tool.
2. Go through the checklist and fix anything that is missing.
3. In docs/design-notes.md add ## After with three lines: the checks, the two measurements, the stale result and its fix.
4. Stop the app, then docker compose down (without -v: P09 starts from this data).
5. Copy the hotel-booking folder to your partner’s laptop, so both of you start P09 from it.

**When you finish, you have:** the complete P08 folder on both laptops: the measurements, the cache, its tests, the stale result and ADR-002.
### Explanation

The app now measures itself, answers searches from Redis, and keeps every booking decision in PostgreSQL. The next limit is the booking itself: that is L05 and P09.
