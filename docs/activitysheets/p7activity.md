# P07 · Activity sheet

## 1. P07 · The app gets an address and a database

In L04 you estimated the load and followed a request to the database. Today the P06 app becomes a web app on http://localhost:5080, and its reservations move into PostgreSQL.





### Where we are

Your hotel-booking folder has the P06 code: four modules, 8 behaviour tests and 3 border checks, a demo and a race. docs/l04-notes.md has your three answers from L04.

**Why the app needs a server and a database now** In short: A console program runs on one laptop and forgets everything when it stops; guests need an address to send requests to and bookings that are still there tomorrow. L04 showed the three problems: no network entry, data lost on restart, and two copies that do not share bookings. Today fixes the first two; scaling out comes later.

1. Today: you run the demo, Maya books R7, the program ends, and R7 is free again.
2. After Step 5: the app listens on http://localhost:5080, so curl.exe (later a browser) can send it requests.
3. After Step 8: Maya’s booking is a row in PostgreSQL. Stop the app, start it again: R7 is still booked.

### Why that is not enough now

| What the hotel needs | What the P06 app does |
| --- | --- |
| 200 guests search within one minute (S1) | Only the person at the laptop can use it: there is no address to send a search to. |
| Maya’s booking is still there tomorrow | The reservations are a list in memory: stopping the program deletes them. |
| Two copies of the app agree about R7 (L04) | Each copy has its own list, so both can accept R7. |

### Today’s plan

| Step | What you do | Where it goes |
| --- | --- | --- |
| 1 · Capacity estimate | Write today’s assumptions and calculations for your app: requests, storage, bandwidth, and what to measure later | docs/capacity.md |
| 2 · Start PostgreSQL | Add docker-compose.yml and db/init.sql; docker compose up -d; check that rooms R7 and R8 are there | docker-compose.yml, db/init.sql |
| 3 · The API contract | Describe the three operations, their answers and their errors | docs/openapi.yaml |
| 4 · From console to web app | Install your framework; the app listens on http://localhost:5080 and serves /openapi.yaml; --demo and --race still print the P06 lines | your project file and the wiring |
| 5 · The three endpoints | Turn every booking answer into an HTTP status and body; limit and after for the search; still the in-memory store; check them with curl.exe | the Api folder, outside the four modules; docs/requests/ |
| 6 · API tests | Four tests through your framework’s test client: search 200, book 201 then 409, wrong dates 400, cancel 204 then 404 | the API test file |
| 7 · PostgreSQL stores | A reservation store and a room catalog that read and write the init.sql tables; the booking rule does not change (JavaScript and Dart: BookingService awaits the store) | the Rooms and Reservations internal code |
| 8 · Plug it in and prove it | The wiring picks the PostgreSQL stores; run the seven requests; restart the app and Maya’s booking is still there | the wiring, docs/api-check.md |

### How you work today

Work in pairs on one laptop, in the hotel-booking folder from P06, in the same language as before. Swap who types at every new step. Every code slide has a tab for each of the six languages.

You need Docker Desktop running (PostgreSQL 18 runs inside it), your editor, a terminal opened in the hotel-booking folder and your browser. curl.exe is built into Windows 10 and 11. Internet is needed once, in Steps 2 and 4, to download PostgreSQL and your language’s packages.

If you missed P06: download the P06 runnable code in your language from the P06 lesson page. The “Runnable code” link on this page has the finished P07 project in all six languages.

### Completed example: your folder at the end of P07

### hotel-booking folder

#### C#

```text
hotel-booking/   end of P07
├── db/
│   └── init.sql   NEW: the rooms and reservations tables
├── docs/
│   ├── requests/   NEW: the request bodies curl sends
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── api-check.md   NEW: seven requests and their answers
│   ├── capacity.md   NEW: the estimate
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   NEW: the API contract
│   ├── quality-scenarios.md
│   └── requirements.md
├── HotelBooking/
│   ├── Api/
│   │   └── HotelApi.cs   NEW
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
│   │   │   └── PostgresReservationStore.cs   NEW
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
│   │   │   └── PostgresRoomCatalog.cs   NEW
│   │   ├── IRoomCatalog.cs
│   │   └── Room.cs
│   ├── HotelBooking.csproj   CHANGED: Web SDK + EF Core
│   └── Program.cs   CHANGED: web app; the wiring picks the PostgreSQL stores
├── HotelBooking.Tests/
│   ├── ApiTests.cs   NEW
│   ├── ArchitectureTests.cs
│   ├── BookingServiceTests.cs
│   └── HotelBooking.Tests.csproj   CHANGED: + the ASP.NET Core test server
└── docker-compose.yml   NEW: PostgreSQL in Docker
```

#### Python

```text
hotel-booking/   end of P07
├── db/
│   └── init.sql   NEW: the rooms and reservations tables
├── docs/
│   ├── requests/   NEW: the request bodies curl sends
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── api-check.md   NEW: seven requests and their answers
│   ├── capacity.md   NEW: the estimate
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   NEW: the API contract
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
│   │   └── internal.py   CHANGED: + PostgresReservationStore
│   ├── rooms/
│   │   ├── __init__.py
│   │   ├── catalog.py
│   │   └── internal.py   CHANGED: + PostgresRoomCatalog
│   ├── __init__.py
│   └── api.py   NEW
├── docker-compose.yml   NEW: PostgreSQL in Docker
├── main.py   CHANGED: web app; the wiring picks the PostgreSQL stores
├── pyproject.toml
├── requirements.txt   NEW
├── test_api.py   NEW
└── test_booking_service.py
```

#### Java

```text
hotel-booking/   end of P07
├── db/
│   └── init.sql   NEW: the rooms and reservations tables
├── docs/
│   ├── requests/   NEW: the request bodies curl sends
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── api-check.md   NEW: seven requests and their answers
│   ├── capacity.md   NEW: the estimate
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   NEW: the API contract
│   ├── quality-scenarios.md
│   └── requirements.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── hotelbooking/
│   │   │       ├── api/
│   │   │       │   └── HotelApi.java   NEW
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
│   │   │       │   │   └── PostgresReservationStore.java   NEW
│   │   │       │   ├── BookingResult.java
│   │   │       │   ├── BookingService.java
│   │   │       │   ├── DateRange.java
│   │   │       │   ├── Guest.java
│   │   │       │   ├── Reservation.java   CHANGED: + room() and stay(), for the store
│   │   │       │   ├── ReservationStatus.java
│   │   │       │   └── ReservationStore.java
│   │   │       ├── rooms/
│   │   │       │   ├── internal/
│   │   │       │   │   ├── InMemoryRoomCatalog.java
│   │   │       │   │   └── PostgresRoomCatalog.java   NEW
│   │   │       │   ├── Room.java
│   │   │       │   └── RoomCatalog.java
│   │   │       └── Main.java   CHANGED: web app; the wiring picks the PostgreSQL stores
│   │   └── resources/
│   │       └── application.properties   NEW
│   └── test/
│       └── java/
│           └── hotelbooking/
│               ├── ApiTests.java   NEW
│               ├── ArchitectureTests.java
│               └── BookingServiceTests.java
├── docker-compose.yml   NEW: PostgreSQL in Docker
└── pom.xml   CHANGED: Spring Boot parent and starters
```

#### Go

```text
hotel-booking/   end of P07
├── api/
│   ├── api_test.go   NEW
│   └── api.go   NEW
├── db/
│   └── init.sql   NEW: the rooms and reservations tables
├── docs/
│   ├── requests/   NEW: the request bodies curl sends
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── api-check.md   NEW: seven requests and their answers
│   ├── capacity.md   NEW: the estimate
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   NEW: the API contract
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
│   └── internal.go   CHANGED: + PostgresReservationStore
├── rooms/
│   ├── internal.go   CHANGED: + PostgresRoomCatalog
│   └── rooms.go
├── architecture_test.go
├── docker-compose.yml   NEW: PostgreSQL in Docker
├── go.mod   CHANGED: + pgx
├── go.sum   NEW
└── main.go   CHANGED: web app; the wiring picks the PostgreSQL stores
```

#### JavaScript

```text
hotel-booking/   end of P07
├── db/
│   └── init.sql   NEW: the rooms and reservations tables
├── docs/
│   ├── requests/   NEW: the request bodies curl sends
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── api-check.md   NEW: seven requests and their answers
│   ├── capacity.md   NEW: the estimate
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   NEW: the API contract
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
│   │   │   ├── postgres-store.js   NEW
│   │   │   └── store.js   CHANGED: one comment
│   │   ├── booking.js   CHANGED: awaits the store
│   │   ├── domain.js
│   │   └── index.js
│   ├── rooms/
│   │   ├── internal/
│   │   │   ├── catalog.js
│   │   │   └── postgres-catalog.js   NEW
│   │   └── index.js
│   └── api.js   NEW
├── test/
│   ├── api.test.js   NEW
│   ├── architecture.test.js
│   └── booking.test.js   CHANGED: + await in one test
├── docker-compose.yml   NEW: PostgreSQL in Docker
├── main.js   CHANGED: web app; the wiring picks the PostgreSQL stores
├── package-lock.json   NEW: written by npm install
└── package.json   CHANGED: + express, pg
```

#### Dart

```text
hotel-booking/   end of P07
├── bin/
│   └── main.dart   CHANGED: web app; the wiring picks the PostgreSQL stores
├── db/
│   └── init.sql   NEW: the rooms and reservations tables
├── docs/
│   ├── requests/   NEW: the request bodies curl sends
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── api-check.md   NEW: seven requests and their answers
│   ├── capacity.md   NEW: the estimate
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   NEW: the API contract
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
│   │   │   ├── booking.dart   CHANGED: awaits the store
│   │   │   ├── domain.dart
│   │   │   ├── postgres_store.dart   NEW
│   │   │   └── store.dart   CHANGED: one comment
│   │   └── reservations.dart
│   ├── rooms/
│   │   ├── src/
│   │   │   ├── catalog.dart
│   │   │   └── postgres_catalog.dart   NEW
│   │   └── rooms.dart   CHANGED: all() may answer later
│   └── api.dart   NEW
├── test/
│   ├── api_test.dart   NEW
│   ├── architecture_test.dart
│   └── booking_service_test.dart   CHANGED: + await in one test
├── analysis_options.yaml
├── docker-compose.yml   NEW: PostgreSQL in Docker
└── pubspec.yaml   CHANGED: + shelf, shelf_router, postgres
```

### Your turn · open your project

1. Start Docker Desktop and wait until it says “Engine running”.
2. Open your hotel-booking folder in your editor, and open a terminal in the same folder.
3. Run your P06 checks once: 11 green (Python: 8 tests, then “Contracts: 3 kept”).
4. Open docs/design-notes.md and add a new heading at the bottom: # P07 design notes
5. Open docs/l04-notes.md: its first line, the peak, is where Step 1 starts.

**When you finish, you have:** Docker Desktop running, your P06 project open with green checks, and a “P07 design notes” heading.
### Explanation

Nothing in BookingService’s rule changes today. The new code sits at the edges: an Api folder for HTTP, two PostgreSQL stores in the modules’ internal code, and a few lines in the wiring that choose them.



---

## 2. Step 1 · Write the capacity estimate

Before building, write down how busy the app will be: the assumptions, what they give, and what P08 will measure.





### The five assumptions

| Assumption | Value | Why this value |
| --- | --- | --- |
| Visitors per day | 1,440 | The booking page today: about one visitor a minute |
| Searches per visitor | 20 | Guests try several dates |
| Bookings per day | 20 | 40 rooms, most stays are two nights |
| Peak factor | 10 | Friday evenings and holiday sales |
| Queries per search | 40 | FindFreeRooms asks once per room |

**How each number in the estimate is calculated** In short: Multiply the assumptions together, then divide by the seconds in a day; the peak is the average times the peak factor. This is L04’s program: the same calculation, printed line by line. You can check every result with your laptop’s calculator.

1. 1,440 visitors × 20 searches = 28,800 searches a day.
2. 28,800 ÷ 86,400 seconds = 0.33 searches a second on average.
3. 0.33 × 10 (peak factor) = 3.33 a second = 200 a minute: that is S1.
4. 3.33 searches × 40 rooms = 133 database questions a second at the peak, because the search asks once per room.

C#:

```text
static class Capacity
{
    // Assumptions: our guesses, written down so anyone can check them and change them.
    const int VisitorsPerDay = 1440;     // people who open the hotel's booking page
    const int SearchesPerVisitor = 20;   // each one tries several dates
    const int BookingsPerDay = 20;       // 40 rooms, most stays are two nights
    const int PeakFactor = 10;           // the busiest minutes compared with the average
    const int Rooms = 40;                // one availability query per room per search
    const int RowBytes = 600;            // one reservation row plus its index
    const int AnswerKb = 2;              // one search answer as JSON
    const int PhotosKb = 2000;           // ten room photos of 200 KB on the booking page
    const int SecondsPerDay = 86_400;

    public static void Estimate()
    {
        var searchesPerDay = VisitorsPerDay * SearchesPerVisitor;
        var average = (double)searchesPerDay / SecondsPerDay;
        var peak = average * PeakFactor;
        var peakPerMinute = searchesPerDay * PeakFactor / 1440;
        var bookingsAtPeak = (double)BookingsPerDay / SecondsPerDay * PeakFactor;
        var visitorsAtPeak = (double)VisitorsPerDay / SecondsPerDay * PeakFactor;
        var storageMb = (double)BookingsPerDay * 365 * RowBytes / 1_000_000;
        Console.WriteLine($"Searches per day: {VisitorsPerDay} visitors x {SearchesPerVisitor} = {searchesPerDay}");
        Console.WriteLine($"Searches per second: average {average:F2}, peak {peak:F2} ({peakPerMinute} per minute)");
        Console.WriteLine($"Bookings: {BookingsPerDay} per day, {bookingsAtPeak:F4} per second at peak, {searchesPerDay / BookingsPerDay} searches per booking");
        Console.WriteLine($"Database queries at peak: {peak:F2} searches/s x {Rooms} rooms = {peak * Rooms:F2} per second");
        Console.WriteLine($"Reservation storage: {BookingsPerDay} x 365 x {RowBytes} bytes = {storageMb:F2} MB per year");
        Console.WriteLine($"Bandwidth at peak: search answers {peak * AnswerKb:F2} KB/s, room photos {visitorsAtPeak * PhotosKb:F2} KB/s");
    }
}
```

Python:

```text
# Assumptions: our guesses, written down so anyone can check them and change them.
VISITORS_PER_DAY = 1440      # people who open the hotel's booking page
SEARCHES_PER_VISITOR = 20    # each one tries several dates
BOOKINGS_PER_DAY = 20        # 40 rooms, most stays are two nights
PEAK_FACTOR = 10             # the busiest minutes compared with the average
ROOMS = 40                   # one availability query per room per search
ROW_BYTES = 600              # one reservation row plus its index
ANSWER_KB = 2                # one search answer as JSON
PHOTOS_KB = 2000             # ten room photos of 200 KB on the booking page
SECONDS_PER_DAY = 86_400


def capacity_estimate() -> None:
    searches_per_day = VISITORS_PER_DAY * SEARCHES_PER_VISITOR
    average = searches_per_day / SECONDS_PER_DAY
    peak = average * PEAK_FACTOR
    peak_per_minute = searches_per_day * PEAK_FACTOR // 1440
    bookings_at_peak = BOOKINGS_PER_DAY / SECONDS_PER_DAY * PEAK_FACTOR
    visitors_at_peak = VISITORS_PER_DAY / SECONDS_PER_DAY * PEAK_FACTOR
    storage_mb = BOOKINGS_PER_DAY * 365 * ROW_BYTES / 1_000_000
    print(f"Searches per day: {VISITORS_PER_DAY} visitors x {SEARCHES_PER_VISITOR} = {searches_per_day}")
    print(f"Searches per second: average {average:.2f}, peak {peak:.2f} ({peak_per_minute} per minute)")
    print(f"Bookings: {BOOKINGS_PER_DAY} per day, {bookings_at_peak:.4f} per second at peak, "
          f"{searches_per_day // BOOKINGS_PER_DAY} searches per booking")
    print(f"Database queries at peak: {peak:.2f} searches/s x {ROOMS} rooms = {peak * ROOMS:.2f} per second")
    print(f"Reservation storage: {BOOKINGS_PER_DAY} x 365 x {ROW_BYTES} bytes = {storage_mb:.2f} MB per year")
    print(f"Bandwidth at peak: search answers {peak * ANSWER_KB:.2f} KB/s, room photos {visitors_at_peak * PHOTOS_KB:.2f} KB/s")
```

Java:

```text
// Assumptions: our guesses, written down so anyone can check them and change them.
static final int VISITORS_PER_DAY = 1440;     // people who open the hotel's booking page
static final int SEARCHES_PER_VISITOR = 20;   // each one tries several dates
static final int BOOKINGS_PER_DAY = 20;       // 40 rooms, most stays are two nights
static final int PEAK_FACTOR = 10;            // the busiest minutes compared with the average
static final int ROOMS = 40;                  // one availability query per room per search
static final int ROW_BYTES = 600;             // one reservation row plus its index
static final int ANSWER_KB = 2;               // one search answer as JSON
static final int PHOTOS_KB = 2000;            // ten room photos of 200 KB on the booking page
static final int SECONDS_PER_DAY = 86_400;

static void capacityEstimate() {
    int searchesPerDay = VISITORS_PER_DAY * SEARCHES_PER_VISITOR;
    double average = (double) searchesPerDay / SECONDS_PER_DAY;
    double peak = average * PEAK_FACTOR;
    int peakPerMinute = searchesPerDay * PEAK_FACTOR / 1440;
    double bookingsAtPeak = (double) BOOKINGS_PER_DAY / SECONDS_PER_DAY * PEAK_FACTOR;
    double visitorsAtPeak = (double) VISITORS_PER_DAY / SECONDS_PER_DAY * PEAK_FACTOR;
    double storageMb = (double) BOOKINGS_PER_DAY * 365 * ROW_BYTES / 1_000_000;
    System.out.printf("Searches per day: %d visitors x %d = %d%n", VISITORS_PER_DAY, SEARCHES_PER_VISITOR, searchesPerDay);
    System.out.printf("Searches per second: average %.2f, peak %.2f (%d per minute)%n", average, peak, peakPerMinute);
    System.out.printf("Bookings: %d per day, %.4f per second at peak, %d searches per booking%n", BOOKINGS_PER_DAY, bookingsAtPeak, searchesPerDay / BOOKINGS_PER_DAY);
    System.out.printf("Database queries at peak: %.2f searches/s x %d rooms = %.2f per second%n", peak, ROOMS, peak * ROOMS);
    System.out.printf("Reservation storage: %d x 365 x %d bytes = %.2f MB per year%n", BOOKINGS_PER_DAY, ROW_BYTES, storageMb);
    System.out.printf("Bandwidth at peak: search answers %.2f KB/s, room photos %.2f KB/s%n", peak * ANSWER_KB, visitorsAtPeak * PHOTOS_KB);
}
```

Go:

```text
// Assumptions: our guesses, written down so anyone can check them and change them.
const (
	visitorsPerDay     = 1440 // people who open the hotel's booking page
	searchesPerVisitor = 20   // each one tries several dates
	bookingsPerDay     = 20   // 40 rooms, most stays are two nights
	peakFactor         = 10   // the busiest minutes compared with the average
	rooms              = 40   // one availability query per room per search
	rowBytes           = 600  // one reservation row plus its index
	answerKB           = 2    // one search answer as JSON
	photosKB           = 2000 // ten room photos of 200 KB on the booking page
	secondsPerDay      = 86_400
)

func capacityEstimate() {
	searchesPerDay := visitorsPerDay * searchesPerVisitor
	average := float64(searchesPerDay) / secondsPerDay
	peak := average * peakFactor
	peakPerMinute := searchesPerDay * peakFactor / 1440
	bookingsAtPeak := float64(bookingsPerDay) / secondsPerDay * peakFactor
	visitorsAtPeak := float64(visitorsPerDay) / secondsPerDay * peakFactor
	storageMB := float64(bookingsPerDay) * 365 * rowBytes / 1_000_000
	fmt.Printf("Searches per day: %d visitors x %d = %d\n", visitorsPerDay, searchesPerVisitor, searchesPerDay)
	fmt.Printf("Searches per second: average %.2f, peak %.2f (%d per minute)\n", average, peak, peakPerMinute)
	fmt.Printf("Bookings: %d per day, %.4f per second at peak, %d searches per booking\n", bookingsPerDay, bookingsAtPeak, searchesPerDay/bookingsPerDay)
	fmt.Printf("Database queries at peak: %.2f searches/s x %d rooms = %.2f per second\n", peak, rooms, peak*rooms)
	fmt.Printf("Reservation storage: %d x 365 x %d bytes = %.2f MB per year\n", bookingsPerDay, rowBytes, storageMB)
	fmt.Printf("Bandwidth at peak: search answers %.2f KB/s, room photos %.2f KB/s\n", peak*answerKB, visitorsAtPeak*photosKB)
}
```

JavaScript:

```text
// Assumptions: our guesses, written down so anyone can check them and change them.
const VISITORS_PER_DAY = 1440;     // people who open the hotel's booking page
const SEARCHES_PER_VISITOR = 20;   // each one tries several dates
const BOOKINGS_PER_DAY = 20;       // 40 rooms, most stays are two nights
const PEAK_FACTOR = 10;            // the busiest minutes compared with the average
const ROOMS = 40;                  // one availability query per room per search
const ROW_BYTES = 600;             // one reservation row plus its index
const ANSWER_KB = 2;               // one search answer as JSON
const PHOTOS_KB = 2000;            // ten room photos of 200 KB on the booking page
const SECONDS_PER_DAY = 86_400;

function capacityEstimate() {
  const searchesPerDay = VISITORS_PER_DAY * SEARCHES_PER_VISITOR;
  const average = searchesPerDay / SECONDS_PER_DAY;
  const peak = average * PEAK_FACTOR;
  const peakPerMinute = Math.trunc(searchesPerDay * PEAK_FACTOR / 1440);
  const bookingsAtPeak = BOOKINGS_PER_DAY / SECONDS_PER_DAY * PEAK_FACTOR;
  const visitorsAtPeak = VISITORS_PER_DAY / SECONDS_PER_DAY * PEAK_FACTOR;
  const storageMb = BOOKINGS_PER_DAY * 365 * ROW_BYTES / 1_000_000;
  console.log(`Searches per day: ${VISITORS_PER_DAY} visitors x ${SEARCHES_PER_VISITOR} = ${searchesPerDay}`);
  console.log(`Searches per second: average ${average.toFixed(2)}, peak ${peak.toFixed(2)} (${peakPerMinute} per minute)`);
  console.log(`Bookings: ${BOOKINGS_PER_DAY} per day, ${bookingsAtPeak.toFixed(4)} per second at peak, ${Math.trunc(searchesPerDay / BOOKINGS_PER_DAY)} searches per booking`);
  console.log(`Database queries at peak: ${peak.toFixed(2)} searches/s x ${ROOMS} rooms = ${(peak * ROOMS).toFixed(2)} per second`);
  console.log(`Reservation storage: ${BOOKINGS_PER_DAY} x 365 x ${ROW_BYTES} bytes = ${storageMb.toFixed(2)} MB per year`);
  console.log(`Bandwidth at peak: search answers ${(peak * ANSWER_KB).toFixed(2)} KB/s, room photos ${(visitorsAtPeak * PHOTOS_KB).toFixed(2)} KB/s`);
}
```

Dart:

```text
// Assumptions: our guesses, written down so anyone can check them and change them.
const visitorsPerDay = 1440; // people who open the hotel's booking page
const searchesPerVisitor = 20; // each one tries several dates
const bookingsPerDay = 20; // 40 rooms, most stays are two nights
const peakFactor = 10; // the busiest minutes compared with the average
const rooms = 40; // one availability query per room per search
const rowBytes = 600; // one reservation row plus its index
const answerKb = 2; // one search answer as JSON
const photosKb = 2000; // ten room photos of 200 KB on the booking page
const secondsPerDay = 86400;

void capacityEstimate() {
  final searchesPerDay = visitorsPerDay * searchesPerVisitor;
  final average = searchesPerDay / secondsPerDay;
  final peak = average * peakFactor;
  final peakPerMinute = searchesPerDay * peakFactor ~/ 1440;
  final bookingsAtPeak = bookingsPerDay / secondsPerDay * peakFactor;
  final visitorsAtPeak = visitorsPerDay / secondsPerDay * peakFactor;
  final storageMb = bookingsPerDay * 365 * rowBytes / 1000000;
  print('Searches per day: $visitorsPerDay visitors x $searchesPerVisitor = $searchesPerDay');
  print('Searches per second: average ${average.toStringAsFixed(2)}, peak ${peak.toStringAsFixed(2)} ($peakPerMinute per minute)');
  print('Bookings: $bookingsPerDay per day, ${bookingsAtPeak.toStringAsFixed(4)} per second at peak, '
      '${searchesPerDay ~/ bookingsPerDay} searches per booking');
  print('Database queries at peak: ${peak.toStringAsFixed(2)} searches/s x $rooms rooms = ${(peak * rooms).toStringAsFixed(2)} per second');
  print('Reservation storage: $bookingsPerDay x 365 x $rowBytes bytes = ${storageMb.toStringAsFixed(2)} MB per year');
  print('Bandwidth at peak: search answers ${(peak * answerKb).toStringAsFixed(2)} KB/s, '
      'room photos ${(visitorsAtPeak * photosKb).toStringAsFixed(2)} KB/s');
}
```

### What they give

| Number | Calculation | Result |
| --- | --- | --- |
| Searches, average | 1,440 × 20 = 28,800 a day; 28,800 ÷ 86,400 s | 0.33 per second |
| Searches, peak | 0.33 × 10 | 3.33 per second = 200 a minute (S1) |
| Database queries, peak | 3.33 × 40 rooms | 133 per second |
| Storage | 20 bookings × 365 days × 600 bytes | 4.38 MB a year |
| Bandwidth, peak | 3.33 × 2 KB search answers; 0.17 × 2 MB photos | search 6.67 KB/s, photos 333 KB/s |

### Completed example: docs/capacity.md

### docs/capacity.md

```text
# Capacity estimate: hotel booking app

These are estimates from assumptions, not measurements. P08 measures the real app.

## Assumptions
| Assumption | Value | Reason |
|---|---|---|
| Visitors per day | 1,440 | the booking page today: about one visitor a minute |
| Searches per visitor | 20 | guests try several dates |
| Bookings per day | 20 | 40 rooms, most stays are two nights |
| Peak factor | 10 | Friday evenings and holiday sales |
| Queries per search | 40 | FindFreeRooms asks once per room |

## Results
| Number | Calculation | Result |
|---|---|---|
| Searches, average | 28,800 ÷ 86,400 s | 0.33 per second |
| Searches, peak | 0.33 × 10 | 3.33 per second = 200 a minute (S1) |
| Database queries, peak | 3.33 × 40 rooms | 133 per second |
| Storage | 20 × 365 × 600 bytes | 4.38 MB a year |
| Bandwidth, peak | 3.33 × 2 KB and 0.17 × 2 MB | search 6.67 KB/s, photos 333 KB/s |

## To measure later
- p95 of 1,000 searches for 12–14 Nov, target ≤ 1 s (S1): P08.
- Database queries per search (GET /stats): P08.
```

### Your turn · write docs/capacity.md

1. Create the file docs/capacity.md in your editor.
2. Write the heading and the line “These are estimates from assumptions, not measurements. P08 measures the real app.”
3. Copy the “Assumptions” table.
4. For each row of “Results”, work out the number with your laptop’s calculator first, then write the row.
5. Compare the peak with the first line of docs/l04-notes.md: they must agree (3.33 a second, 200 a minute).
6. Add “To measure later” with the two lines from the completed example.

**When you finish, you have:** docs/capacity.md with five assumptions, five calculated results and the two things P08 measures.
### Explanation

The estimate decides what to build first. 133 database queries a second at the peak, almost all from searches, is why P08 caches the search and not the bookings.



---

## 3. Step 2 · Start PostgreSQL in Docker

Two files describe the database: docker-compose.yml says how to run PostgreSQL, db/init.sql says which tables it has. One command starts it.





### docker-compose.yml, line by line

| Line | What it does |
| --- | --- |
| image: postgres:18 | Runs the official PostgreSQL 18 image; Docker downloads it the first time |
| POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD: hotel | Creates the database hotel and the user hotel with the password hotel (for your laptop only) |
| ports: "5432:5432" | Your laptop’s port 5432 leads to PostgreSQL in the container |
| ./db/init.sql:/docker-entrypoint-initdb.d/init.sql | PostgreSQL runs db/init.sql once, when the database is first created |
| hotel-data:/var/lib/postgresql | The data lives in a Docker volume, so it survives a restart |

**What docker compose up -d does** In short: It reads docker-compose.yml, downloads PostgreSQL the first time, and starts it in the background on your laptop. Docker Desktop must be running first (the whale icon says “Engine running”). -d means “detached”: the terminal is free again while PostgreSQL keeps running.

1. docker compose up -d: Docker downloads postgres:18 once (about 150 MB) and starts the container db.
2. On the very first start PostgreSQL runs db/init.sql: it creates the two tables and adds R7 and R8.
3. The data lives in the volume hotel-data, so docker compose down and up again keep every booking.
4. docker compose down -v also deletes the volume: the next start is an empty database again.

```text
docker compose up -d       # start PostgreSQL in the background
docker compose ps          # db   Up
docker compose down        # stop it, keep the data
docker compose down -v     # stop it and delete the data
```

**If port 5432 is already taken on your laptop** In short: Publish PostgreSQL on another port in docker-compose.yml and tell the app that port with HOTEL_DB_PORT. Some laptops already run their own PostgreSQL on 5432. docker compose up then says “port is already allocated”.

1. In docker-compose.yml change "5432:5432" to "15432:5432" and run docker compose up -d again.
2. Before starting the app, set HOTEL_DB_PORT=15432 in the same terminal.
3. The wiring reads HOTEL_DB_PORT; without it, the app uses 5432.

```text
PowerShell:   $env:HOTEL_DB_PORT = "15432"
cmd:          set HOTEL_DB_PORT=15432
Git Bash:     export HOTEL_DB_PORT=15432
```

### db/init.sql: two tables, one per module

There is no foreign key between the tables: each module owns its own data (L03).

| Table | Owner | Rules the database keeps itself |
| --- | --- | --- |
| rooms | Rooms | number is the primary key: no room twice |
| reservations | Reservations | id is the primary key (a second R101 is refused); status is Confirmed or Cancelled; check_out after check_in |

**The database refuses a wrong row by itself** In short: The primary key refuses a second R101, and the CHECK refuses a stay whose check-out is not after its check-in, even if the app has a bug.

1. Two app instances both count no reservations yet and both try to save R101.
2. The first INSERT works; the second gets “duplicate key value violates unique constraint”.
3. A same-day stay (15–15 November) breaks CHECK (check_out > check_in) and is refused too.
4. In P07 BookingService already refuses a same-day stay; the CHECK is the database’s own safety net.

```text
INSERT INTO reservations VALUES ('R101', 'Omar', 'omar@example.com', 'R7', '2026-11-12', '2026-11-14', 'Confirmed');
ERROR:  duplicate key value violates unique constraint "reservations_pkey"
DETAIL:  Key (id)=(R101) already exists.

INSERT INTO reservations VALUES ('R102', 'Omar', 'omar@example.com', 'R8', '2026-11-15', '2026-11-15', 'Confirmed');
ERROR:  new row for relation "reservations" violates check constraint "reservations_check"
```

### Completed example: docker-compose.yml

### docker-compose.yml

```text
# The hotel booking app's database. Start: docker compose up -d   Stop: docker compose down
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

volumes:
  hotel-data:

```

### Completed example: db/init.sql

### db/init.sql

```text
-- The hotel booking app's tables. PostgreSQL runs this file once, when the database is first created.
-- Rooms owns the rooms table and Reservations owns the reservations table, so there is no foreign key
-- between them: each module keeps its own data (L03).

CREATE TABLE rooms (
    number text PRIMARY KEY,
    type   text NOT NULL
);

CREATE TABLE reservations (
    id          text PRIMARY KEY,   -- R101, R102 ...: a second R101 is refused
    guest_name  text NOT NULL,
    guest_email text NOT NULL,
    room        text NOT NULL,
    check_in    date NOT NULL,
    check_out   date NOT NULL,
    status      text NOT NULL CHECK (status IN ('Confirmed', 'Cancelled')),
    CHECK (check_out > check_in)
);

INSERT INTO rooms (number, type) VALUES ('R7', 'Standard'), ('R8', 'Deluxe');

```

### Check it: start the database and look inside

**Looking inside the database with psql** In short: psql is PostgreSQL’s own command-line client; docker compose exec runs it inside the db container, so you install nothing.

1. docker compose exec db: run a command inside the running container called db.
2. psql -U hotel -d hotel: connect as the user hotel to the database hotel.
3. -c "SELECT * FROM rooms;": run one SQL statement and print the rows.

```text
docker compose exec db psql -U hotel -d hotel -c "SELECT * FROM rooms;"

 number |   type
--------+----------
 R7     | Standard
 R8     | Deluxe
(2 rows)
```

### Terminal, in the hotel-booking folder

```text
docker compose up -d
docker compose exec db psql -U hotel -d hotel -c "SELECT * FROM rooms;"

 number |   type
--------+----------
 R7     | Standard
 R8     | Deluxe
(2 rows)
```

### Your turn · start PostgreSQL

1. Create docker-compose.yml in the hotel-booking folder (next to docs/) and copy the completed example.
2. Create the folder db and the file db/init.sql, and copy the completed example.
3. Run docker compose up -d. The first time it downloads PostgreSQL; wait until it says “Started”.
4. Run docker compose ps: the service db is “Up”.
5. Run the psql command above: two rows, R7 Standard and R8 Deluxe.

**When you finish, you have:** PostgreSQL running in Docker with the tables rooms and reservations, and R7 and R8 in rooms.
### Explanation

The database keeps two rules by itself: no second R101 and no stay without a night. The booking rule, no overlapping Confirmed stays, is still checked by BookingService; P09 moves it into the database too.



---

## 4. Step 3 · Write the API contract

docs/openapi.yaml says what the API promises: three operations, their answers and their errors. Every language serves this same file.





### The three operations

| Operation | Calls | Success | Errors |
| --- | --- | --- | --- |
| GET /rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14 | FindFreeRooms | 200 with the free rooms and next | 400 InvalidDates · 400 InvalidRequest |
| POST /reservations with guestName, guestEmail, room, checkIn, checkOut | Reserve | 201 {"id":"R101","status":"Confirmed","notificationFailed":false} | 409 Unavailable · 400 InvalidDates · 400 UnknownRoom · 400 InvalidRequest |
| DELETE /reservations/R101 | Cancel | 204, no body | 404 NotFound |

### The five error titles

| Title | Status | When |
| --- | --- | --- |
| Unavailable | 409 | The room is booked for at least one night of the stay |
| InvalidDates | 400 | A date is missing or not a date such as 2026-11-12, or checkOut is not after checkIn |
| UnknownRoom | 400 | The hotel has no room with that number, for example R99 |
| InvalidRequest | 400 | The body is not JSON or a field is missing, empty or not text; limit is not a whole number from 1 to 100 |
| NotFound | 404 | There is no confirmed reservation with that id |

**One shape for every error** In short: Every error answer is problem details: a title clients compare, the HTTP status again, and a sentence for people. RFC 9457 also allows a type field; the hotel API keeps three keys, as L04 taught, and its contract says that clients compare title.

1. Omar asks for R7 on a night Maya has: 409 with title Unavailable.
2. A booking for R99: 400 with title UnknownRoom; a body without guestEmail: 400 with title InvalidRequest.
3. Cancelling R101 twice: 404 with title NotFound.
4. The Content-Type is application/problem+json, so a client knows it is an error without reading the body.

C#:

```text
// Problem details (RFC 9457): every error has the same three keys.
private static IResult Problem(int status, string title, string detail) =>
    Results.Json(new { title, status, detail }, contentType: "application/problem+json", statusCode: status);
```

Python:

```text
def problem(status: int, title: str, detail: str) -> JSONResponse:
    """Problem details (RFC 9457): every error has the same three keys."""
    return JSONResponse({"title": title, "status": status, "detail": detail}, status,
                        media_type="application/problem+json")
```

Java:

```text
static ResponseEntity<?> problem(int status, String title, String detail) {
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(new Problem(title, status, detail));
}
```

Go:

```text
func problem(w http.ResponseWriter, status int, title, detail string) {
	write(w, status, "application/problem+json", problemJSON{Title: title, Status: status, Detail: detail})
}
```

JavaScript:

```text
// Problem details (RFC 9457): every error has the same three keys.
const problem = (response, status, title, detail) =>
  response.status(status).type('application/problem+json').send(JSON.stringify({ title, status, detail }));
```

Dart:

```text
// Problem details (RFC 9457): every error has the same three keys.
Response problem(int status, String title, String detail) => Response(status,
    body: jsonEncode({'title': title, 'status': status, 'detail': detail}),
    headers: {'content-type': 'application/problem+json'});
```

### Three more decisions the contract writes down

| Question | What the contract says |
| --- | --- |
| In which order do the free rooms come? | In text order of the room number: R10 comes before R7. “after” compares text too. |
| Which key does a client compare? | title. detail is a sentence for people and may change. |
| Is the room type a fixed list? | No: it is catalog data from the rooms table, such as Standard or Deluxe. |

**Why R10 comes before R7** In short: Room numbers are text, and text is compared letter by letter: “R1…” comes before “R7…”, whatever follows. The contract says so (“in text order of the room number”), so every client knows what “after” means. The hotel’s demo rooms R7 and R8 are not affected.

1. Compare R10 and R7: the first letters are both R.
2. Then 1 and 7: 1 comes first, so R10 comes before R7.
3. A page that ends with R10 continues with “after=R10”: R11, R12 … and later R7.

C#:

```text
// Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
```

Python:

```text
# Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
```

Java:

```text
// Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
```

Go:

```text
// Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
```

JavaScript:

```text
// Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
```

Dart:

```text
// Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
```

### Completed example: docs/openapi.yaml

**Why the contract is written by hand** In short: docs/openapi.yaml is written first and every language serves the same file, so the six versions of the API cannot drift apart. Many frameworks can generate an OpenAPI file from the code. Here the contract comes first and the tests and docs/api-check.md check the code against it.

1. The contract says: POST /reservations answers 201 with id, status and notificationFailed.
2. Each language’s POST endpoint is written to give exactly that answer.
3. The API tests and docs/api-check.md check the real answers against the contract’s examples.
4. GET /openapi.yaml returns the file itself, so a client can read the contract from the running app.

### docs/openapi.yaml

```text
openapi: 3.1.0
info:
  title: Hotel booking API
  version: 1.0.0
  description: Search free rooms, book a room and cancel a reservation.
servers:
  - url: http://localhost:5080
paths:
  /rooms/availability:
    get:
      operationId: findFreeRooms
      summary: Free rooms for a stay
      parameters:
        - name: checkIn
          in: query
          required: true
          schema: { type: string, format: date }
          example: "2026-11-12"
        - name: checkOut
          in: query
          required: true
          description: The day the guest leaves. That night is not booked.
          schema: { type: string, format: date }
          example: "2026-11-14"
        - name: limit
          in: query
          schema: { type: integer, minimum: 1, maximum: 100, default: 20 }
        - name: after
          in: query
          description: The last room number of the previous page (the value of "next").
          schema: { type: string }
      responses:
        "200":
          description: The free rooms, in text order of the room number (R10 comes before R7).
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Availability" }
              example:
                checkIn: "2026-11-12"
                checkOut: "2026-11-14"
                rooms:
                  - { number: R7, type: Standard }
                  - { number: R8, type: Deluxe }
                next: null
        "400":
          $ref: "#/components/responses/Problem"
          description: InvalidDates when a date is missing, is not a date such as 2026-11-12, or checkOut is not after checkIn; InvalidRequest when limit is not a whole number from 1 to 100.
  /reservations:
    post:
      operationId: reserve
      summary: Book a room for a stay
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: "#/components/schemas/ReservationRequest" }
            example: { guestName: Maya, guestEmail: maya@example.com, room: R7, checkIn: "2026-11-12", checkOut: "2026-11-14" }
      responses:
        "201":
          description: Booked. The id names the new reservation.
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Reservation" }
              example: { id: R101, status: Confirmed, notificationFailed: false }
        "400":
          $ref: "#/components/responses/Problem"
          description: InvalidRequest when the body is not JSON (or not sent as application/json) or a field is missing, empty or not text; InvalidDates when a date is not a date such as 2026-11-12 or checkOut is not after checkIn; UnknownRoom when the hotel has no such room.
        "409":
          $ref: "#/components/responses/Problem"
          description: Unavailable when the room is booked for at least one night of the stay.
  /reservations/{id}:
    delete:
      operationId: cancel
      summary: Cancel a confirmed reservation
      parameters:
        - name: id
          in: path
          required: true
          schema: { type: string }
          example: R101
      responses:
        "204":
          description: Cancelled. The nights are free again.
        "404":
          $ref: "#/components/responses/Problem"
          description: NotFound when there is no confirmed reservation with this id.
components:
  schemas:
    Room:
      type: object
      required: [number, type]
      properties:
        number: { type: string, example: R7 }
        type:
          type: string
          description: Catalog data from the rooms table, such as Standard or Deluxe. A new type needs no change to the API.
          example: Standard
    Availability:
      type: object
      required: [checkIn, checkOut, rooms, next]
      properties:
        checkIn: { type: string, format: date }
        checkOut: { type: string, format: date }
        rooms: { type: array, items: { $ref: "#/components/schemas/Room" } }
        next:
          type: [string, "null"]
          description: Pass it as "after" to get the next page; null on the last page.
    ReservationRequest:
      type: object
      required: [guestName, guestEmail, room, checkIn, checkOut]
      properties:
        guestName: { type: string, minLength: 1 }
        guestEmail: { type: string, minLength: 1 }
        room: { type: string, minLength: 1 }
        checkIn: { type: string, format: date, minLength: 1 }
        checkOut: { type: string, format: date, minLength: 1 }
    Reservation:
      type: object
      required: [id, status, notificationFailed]
      properties:
        id: { type: string, example: R101 }
        status: { type: string, enum: [Confirmed] }
        notificationFailed: { type: boolean }
    Problem:
      type: object
      required: [title, status, detail]
      properties:
        title:
          type: string
          enum: [Unavailable, InvalidDates, UnknownRoom, InvalidRequest, NotFound]
          description: The error's name. Clients compare this value; detail is for people.
        status: { type: integer }
        detail: { type: string }
  responses:
    Problem:
      description: What went wrong, as problem details (RFC 9457).
      content:
        application/problem+json:
          schema: { $ref: "#/components/schemas/Problem" }

```

### Your turn · write docs/openapi.yaml

1. Create docs/openapi.yaml and copy the completed example.
2. In your browser open https://editor.swagger.io, choose File → Import file and pick docs/openapi.yaml.
3. Check the right-hand side: three operations, five error titles in the Problem schema, and no red errors.
4. In docs/design-notes.md add ## API decisions and write the three decisions from the last table in your own words.

**When you finish, you have:** docs/openapi.yaml that the Swagger editor shows without errors, and your API decisions in design-notes.md.
### Explanation

Writing the contract before the code means every pair builds the same API, whatever the language. Steps 5, 6 and 8 check the code against it.



---

## 5. Step 4 · From console program to web app

Install your web framework, give the app a first Api file that serves the contract, and let the program start the web app when it gets no argument.





### What changes in your project

| Language | Project change | Web framework and database library |
| --- | --- | --- |
| C# | HotelBooking.csproj: the Web SDK, EF Core for PostgreSQL, and the build copies docs/openapi.yaml next to the program | ASP.NET Core minimal API + EF Core (Npgsql) |
| Python | requirements.txt lists the packages; main.py starts the app with Uvicorn | FastAPI + Uvicorn + SQLAlchemy (psycopg) |
| Java | pom.xml: Spring Boot becomes the parent (web, JPA, the PostgreSQL driver); application.properties: port 5080 and the database | Spring Boot (Web MVC + Data JPA) |
| Go | go.mod gets pgx (go get); the web server, net/http, is part of Go itself | net/http from the standard library + pgx |
| JavaScript | package.json gets express and pg (npm install) | Express + pg |
| Dart | pubspec.yaml gets shelf, shelf_router and postgres (dart pub add) | shelf + shelf_router + postgres |

### Install (once, internet needed)

### Install

#### C#

```text
# 1. HotelBooking/HotelBooking.csproj: make the first line
#      <Project Sdk="Microsoft.NET.Sdk.Web">
# 2. then, once (internet needed):
dotnet add HotelBooking package Npgsql.EntityFrameworkCore.PostgreSQL --version 10.0.3
dotnet add HotelBooking.Tests package Microsoft.AspNetCore.Mvc.Testing --version 10.0.14
```

#### Python

```text
.venv\Scripts\activate          # macOS/Linux: source .venv/bin/activate
python -m pip install fastapi==0.162.0 uvicorn==0.54.0 sqlalchemy==2.1.6 "psycopg[binary]==3.3.6" httpx==0.28.1
```

#### Java

```text
# pom.xml: Spring Boot 4.1.2 becomes the parent; the dependencies become
#   spring-boot-starter-webmvc, spring-boot-starter-data-jpa,
#   org.postgresql:postgresql (runtime), spring-boot-starter-webmvc-test (test)
#   and archunit-junit5 from P06; the build gets spring-boot-maven-plugin
mvn -q test                      # downloads them once (internet needed)
```

#### Go

```text
go get github.com/jackc/pgx/v5/pgxpool@v5.11.0
```

#### JavaScript

```text
npm install express@5.3.0 pg@8.23.1
```

#### Dart

```text
dart pub add shelf:1.4.3 shelf_router:1.1.5 postgres:3.5.20
```

### Completed example 4.1 · the project file

### Project file

#### C#

```csharp
<!-- HotelBooking/HotelBooking.csproj -->
<Project Sdk="Microsoft.NET.Sdk.Web"><PropertyGroup><OutputType>Exe</OutputType><TargetFramework>net10.0</TargetFramework><ImplicitUsings>enable</ImplicitUsings><Nullable>enable</Nullable></PropertyGroup><ItemGroup>
  <PackageReference Include="Npgsql.EntityFrameworkCore.PostgreSQL" Version="10.0.3" />
  <!-- The API serves the contract: the build copies docs/openapi.yaml next to the program. -->
  <None Include="..\docs\openapi.yaml" Link="openapi.yaml" CopyToOutputDirectory="PreserveNewest" />
</ItemGroup></Project>
```

#### Python

```python
# requirements.txt
# Everything the Python project needs: import-linter from P06 and the five packages P07 adds.
# Install them all at once:  python -m pip install -r requirements.txt
fastapi==0.162.0
uvicorn==0.54.0
sqlalchemy==2.1.6
psycopg[binary]==3.3.6
httpx==0.28.1
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

  <!-- P07: Spring Boot chooses matching versions for the web, database and test libraries. -->
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

go 1.25.0

require github.com/jackc/pgx/v5 v5.11.0

require (
	github.com/jackc/pgpassfile v1.0.0 // indirect
	github.com/jackc/pgservicefile v0.0.0-20240606120523-5a60cdf6a761 // indirect
	github.com/jackc/puddle/v2 v2.2.2 // indirect
	golang.org/x/sync v0.17.0 // indirect
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
    "pg": "^8.23.1"
  }
}
```

#### Dart

```dart
# pubspec.yaml
name: hotel_booking
description: P07 · Hotel booking - the P06 modules behind an HTTP API, with PostgreSQL.
environment:
  sdk: ^3.0.0

dev_dependencies:
  test: ^1.25.0
dependencies:
  postgres: 3.5.20
  shelf: 1.4.3
  shelf_router: 1.1.5
```

### Java only: src/main/resources/application.properties

Spring Boot reads the port and the database from this file.

### src/main/resources/application.properties

```text
# src/main/resources/application.properties
# The web app on http://localhost:5080, with PostgreSQL from docker-compose.yml
# (set HOTEL_DB_PORT only if port 5432 is taken on your laptop).
server.address=localhost
server.port=5080
spring.datasource.url=jdbc:postgresql://localhost:${HOTEL_DB_PORT:5432}/hotel
spring.datasource.username=hotel
spring.datasource.password=hotel
# db/init.sql creates the tables; Hibernate only reads and writes rows.
spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=false
spring.main.banner-mode=off
```

### Completed example 4.2 · the first Api file: it serves the contract

**The first version of the Api file** In short: Step 4’s Api file only serves the contract; Step 5 adds the three endpoints to the same file.

1. The file declares the unit of work: what an endpoint needs to run its booking work.
2. It creates the web app and answers GET /openapi.yaml with the text of docs/openapi.yaml.
3. Open http://localhost:5080/openapi.yaml in your browser: you see the contract.

C#:

```text
// HotelBooking/Api/HotelApi.cs
using HotelBooking.Reservations;
using HotelBooking.Rooms;

namespace HotelBooking.Api;

// Runs one request's work with a BookingService, then saves whatever changed.
public delegate IResult UnitOfWork(Func<BookingService, IResult> work);

// The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back. It lives
// outside the four modules and uses only their public contracts; Program.cs decides which store the
// endpoints use, through the unit of work.
public static class HotelApi
{
    public static void Map(WebApplication app, IReadOnlyList<Room> rooms, UnitOfWork unitOfWork)
    {
        app.MapGet("/openapi.yaml", () => Results.Text(File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "openapi.yaml")), "application/yaml"));
    }
}
```

Python:

```text
# hotel_booking/api.py
"""The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back.

It lives outside the four modules and uses only their public contracts. main.py decides
which store the endpoints use, through unit_of_work.
"""
from __future__ import annotations

from contextlib import AbstractContextManager
from pathlib import Path
from typing import Callable

from fastapi import FastAPI
from fastapi.responses import Response

from hotel_booking.reservations import BookingService
from hotel_booking.rooms import Room

# One request's work: "with unit_of_work() as service:" gives the endpoint a BookingService,
# and whatever changed is saved when the with-block ends.
UnitOfWork = Callable[[], AbstractContextManager[BookingService]]


def create_app(rooms: list[Room], unit_of_work: UnitOfWork) -> FastAPI:
    # docs/openapi.yaml is the contract, so FastAPI must not publish one of its own.
    app = FastAPI(openapi_url=None, docs_url=None, redoc_url=None)

    @app.get("/openapi.yaml")
    def contract() -> Response:
        return Response(Path("docs/openapi.yaml").read_text(encoding="utf-8"), media_type="application/yaml")

    return app
```

Java:

```text
// src/main/java/hotelbooking/api/HotelApi.java
package hotelbooking.api;

import hotelbooking.reservations.BookingService;
import hotelbooking.rooms.Room;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back. It lives
// outside the four modules and uses only their public contracts; Main decides which store the
// endpoints use, through the unit of work.
@RestController
public class HotelApi {
    // Runs one request's work with a BookingService, then saves whatever changed.
    @FunctionalInterface
    public interface UnitOfWork {
        ResponseEntity<?> run(Function<BookingService, ResponseEntity<?>> work);
    }

    private final List<Room> rooms;
    private final UnitOfWork unitOfWork;

    public HotelApi(List<Room> rooms, UnitOfWork unitOfWork) {
        this.rooms = rooms;
        this.unitOfWork = unitOfWork;
    }

    @GetMapping(value = "/openapi.yaml", produces = "application/yaml")
    public String contract() throws IOException {
        return Files.readString(Path.of("docs/openapi.yaml"));
    }
}
```

Go:

```text
// api/api.go
// Package api is the HTTP API: three endpoints that turn HTTP requests into BookingService calls
// and back. It lives outside the four modules and uses only their public contracts; main.go
// decides which store the endpoints use, through the unit of work.
package api

import (
	"net/http"
	"os"

	"hotelbooking/reservations"
	"hotelbooking/rooms"
)

// UnitOfWork runs one request's work with a BookingService and then saves whatever changed.
type UnitOfWork func(work func(service *reservations.BookingService)) error

// New returns the web app: the three endpoints and the contract.
func New(hotelRooms []rooms.Room, unitOfWork UnitOfWork) http.Handler {
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

	return mux
}
```

JavaScript:

```text
// src/api.js
// The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back.
// It lives outside the four modules and uses only their public contracts. main.js decides which
// store the endpoints use, through unitOfWork:
//
//   unitOfWork(work)   runs one request's work with a BookingService, then saves what changed
import fs from 'node:fs';
import express from 'express';

export function createApi(rooms, unitOfWork) {
  const app = express();
  app.use(express.json());

  app.get('/openapi.yaml', (request, response) => {
    response.type('application/yaml').send(fs.readFileSync('docs/openapi.yaml', 'utf8'));
  });

  return app;
}
```

Dart:

```text
// lib/api.dart
/// The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back.
/// It lives outside the four modules and uses only their public contracts. bin/main.dart decides
/// which store the endpoints use, through the unit of work.
library;

import 'dart:async';
import 'dart:io';

import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';

import 'reservations/reservations.dart';
import 'rooms/rooms.dart';

/// Runs one request's work with a BookingService, then saves whatever changed.
typedef UnitOfWork = Future<T> Function<T>(FutureOr<T> Function(BookingService service) work);

Handler createApi(List<Room> rooms, UnitOfWork unitOfWork) {
  final router = Router();

  router.get('/openapi.yaml', (Request request) {
    return Response.ok(File('docs/openapi.yaml').readAsStringSync(),
        headers: {'content-type': 'application/yaml'});
  });

  return router.call;
}
```

### The Api file after Step 4

#### C#

```csharp
// HotelBooking/Api/HotelApi.cs
using HotelBooking.Reservations;
using HotelBooking.Rooms;

namespace HotelBooking.Api;

// Runs one request's work with a BookingService, then saves whatever changed.
public delegate IResult UnitOfWork(Func<BookingService, IResult> work);

// The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back. It lives
// outside the four modules and uses only their public contracts; Program.cs decides which store the
// endpoints use, through the unit of work.
public static class HotelApi
{
    public static void Map(WebApplication app, IReadOnlyList<Room> rooms, UnitOfWork unitOfWork)
    {
        app.MapGet("/openapi.yaml", () => Results.Text(File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "openapi.yaml")), "application/yaml"));
    }
}
```

#### Python

```python
# hotel_booking/api.py
"""The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back.

It lives outside the four modules and uses only their public contracts. main.py decides
which store the endpoints use, through unit_of_work.
"""
from __future__ import annotations

from contextlib import AbstractContextManager
from pathlib import Path
from typing import Callable

from fastapi import FastAPI
from fastapi.responses import Response

from hotel_booking.reservations import BookingService
from hotel_booking.rooms import Room

# One request's work: "with unit_of_work() as service:" gives the endpoint a BookingService,
# and whatever changed is saved when the with-block ends.
UnitOfWork = Callable[[], AbstractContextManager[BookingService]]


def create_app(rooms: list[Room], unit_of_work: UnitOfWork) -> FastAPI:
    # docs/openapi.yaml is the contract, so FastAPI must not publish one of its own.
    app = FastAPI(openapi_url=None, docs_url=None, redoc_url=None)

    @app.get("/openapi.yaml")
    def contract() -> Response:
        return Response(Path("docs/openapi.yaml").read_text(encoding="utf-8"), media_type="application/yaml")

    return app
```

#### Java

```java
// src/main/java/hotelbooking/api/HotelApi.java
package hotelbooking.api;

import hotelbooking.reservations.BookingService;
import hotelbooking.rooms.Room;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back. It lives
// outside the four modules and uses only their public contracts; Main decides which store the
// endpoints use, through the unit of work.
@RestController
public class HotelApi {
    // Runs one request's work with a BookingService, then saves whatever changed.
    @FunctionalInterface
    public interface UnitOfWork {
        ResponseEntity<?> run(Function<BookingService, ResponseEntity<?>> work);
    }

    private final List<Room> rooms;
    private final UnitOfWork unitOfWork;

    public HotelApi(List<Room> rooms, UnitOfWork unitOfWork) {
        this.rooms = rooms;
        this.unitOfWork = unitOfWork;
    }

    @GetMapping(value = "/openapi.yaml", produces = "application/yaml")
    public String contract() throws IOException {
        return Files.readString(Path.of("docs/openapi.yaml"));
    }
}
```

#### Go

```go
// api/api.go
// Package api is the HTTP API: three endpoints that turn HTTP requests into BookingService calls
// and back. It lives outside the four modules and uses only their public contracts; main.go
// decides which store the endpoints use, through the unit of work.
package api

import (
	"net/http"
	"os"

	"hotelbooking/reservations"
	"hotelbooking/rooms"
)

// UnitOfWork runs one request's work with a BookingService and then saves whatever changed.
type UnitOfWork func(work func(service *reservations.BookingService)) error

// New returns the web app: the three endpoints and the contract.
func New(hotelRooms []rooms.Room, unitOfWork UnitOfWork) http.Handler {
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

	return mux
}
```

#### JavaScript

```javascript
// src/api.js
// The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back.
// It lives outside the four modules and uses only their public contracts. main.js decides which
// store the endpoints use, through unitOfWork:
//
//   unitOfWork(work)   runs one request's work with a BookingService, then saves what changed
import fs from 'node:fs';
import express from 'express';

export function createApi(rooms, unitOfWork) {
  const app = express();
  app.use(express.json());

  app.get('/openapi.yaml', (request, response) => {
    response.type('application/yaml').send(fs.readFileSync('docs/openapi.yaml', 'utf8'));
  });

  return app;
}
```

#### Dart

```dart
// lib/api.dart
/// The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back.
/// It lives outside the four modules and uses only their public contracts. bin/main.dart decides
/// which store the endpoints use, through the unit of work.
library;

import 'dart:async';
import 'dart:io';

import 'package:shelf/shelf.dart';
import 'package:shelf_router/shelf_router.dart';

import 'reservations/reservations.dart';
import 'rooms/rooms.dart';

/// Runs one request's work with a BookingService, then saves whatever changed.
typedef UnitOfWork = Future<T> Function<T>(FutureOr<T> Function(BookingService service) work);

Handler createApi(List<Room> rooms, UnitOfWork unitOfWork) {
  final router = Router();

  router.get('/openapi.yaml', (Request request) {
    return Response.ok(File('docs/openapi.yaml').readAsStringSync(),
        headers: {'content-type': 'application/yaml'});
  });

  return router.call;
}
```

### Completed example 4.3 · the wiring starts the web app (the list store until Step 8)

**One program, three ways to start it** In short: Without an argument the program now starts the web app; --demo and --race still run the P06 console programs. Until Step 8 the web app keeps its reservations in the same list store as the demo, so you can build and test the API before PostgreSQL is involved.

1. python main.py (or your language’s run command): the API on http://localhost:5080.
2. python main.py --demo: the nine lines from P06, unchanged.
3. python main.py --race: Maya and Omar still both get R7 (P09 fixes it).

C#:

```text
void RunApi()
{
    // The wiring for the web app. Until Step 8 the reservations stay in a list in memory.
    var app = WebApplication.CreateBuilder(args).Build();
    var rooms = catalog.All();
    var service = new BookingService(rooms, new InMemoryReservationStore(), new EmailNotificationSender());
    HotelApi.Map(app, rooms, work => work(service)); // a list in memory has nothing to save at the end of a request
    app.Run("http://localhost:5080");
}
```

Python:

```text
def run_api() -> None:
    # The wiring for the web app. Until Step 8 the reservations stay in a list in memory.
    rooms = CATALOG.all()
    service = BookingService(rooms, InMemoryReservationStore(), EmailNotificationSender())

    @contextmanager
    def unit_of_work():
        yield service  # a list in memory has nothing to save at the end of a request

    uvicorn.run(create_app(rooms, unit_of_work), host="localhost", port=5080)
```

Java:

```text
// The wiring for the web app. Until Step 8 the reservations stay in a list in memory.
@Bean
HotelApi hotelApi() {
    List<Room> rooms = CATALOG.all();
    BookingService service = new BookingService(rooms, new InMemoryReservationStore(), new EmailNotificationSender());
    // A list in memory has nothing to save at the end of a request.
    return new HotelApi(rooms, work -> work.apply(service));
}
```

Go:

```text
func runAPI() {
	// The wiring for the web app. Until Step 8 the reservations stay in a list in memory.
	hotelRooms := catalog.All()
	service := reservations.NewBookingService(hotelRooms, &reservations.InMemoryReservationStore{}, &notifications.EmailNotificationSender{}, 0)
	unitOfWork := func(work func(service *reservations.BookingService)) error {
		work(service)
		return nil // a list in memory has nothing to save at the end of a request
	}

	fmt.Println("Listening on http://localhost:5080")
	log.Fatal(http.ListenAndServe("localhost:5080", api.New(hotelRooms, unitOfWork)))
}
```

JavaScript:

```text
async function runApi() {
  // The wiring for the web app. Until Step 8 the reservations stay in a list in memory.
  const rooms = catalog.all();
  const service = new BookingService(rooms, new InMemoryReservationStore(), new EmailNotificationSender());
  // A list in memory has nothing to save at the end of a request.
  createApi(rooms, work => work(service)).listen(5080, 'localhost', () => console.log('Listening on http://localhost:5080'));
}
```

Dart:

```text
Future<void> runApi() async {
  // The wiring for the web app. Until Step 8 the reservations stay in a list in memory.
  final rooms = catalog.all();
  final service = BookingService(rooms, InMemoryReservationStore(), EmailNotificationSender());
  // A list in memory has nothing to save at the end of a request.
  Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async => work(service);

  await serve(createApi(rooms, unitOfWork), 'localhost', 5080);
  print('Listening on http://localhost:5080');
}
```

### The web app in the wiring

#### C#

```csharp
void RunApi()
{
    // The wiring for the web app. Until Step 8 the reservations stay in a list in memory.
    var app = WebApplication.CreateBuilder(args).Build();
    var rooms = catalog.All();
    var service = new BookingService(rooms, new InMemoryReservationStore(), new EmailNotificationSender());
    HotelApi.Map(app, rooms, work => work(service)); // a list in memory has nothing to save at the end of a request
    app.Run("http://localhost:5080");
}
```

#### Python

```python
def run_api() -> None:
    # The wiring for the web app. Until Step 8 the reservations stay in a list in memory.
    rooms = CATALOG.all()
    service = BookingService(rooms, InMemoryReservationStore(), EmailNotificationSender())

    @contextmanager
    def unit_of_work():
        yield service  # a list in memory has nothing to save at the end of a request

    uvicorn.run(create_app(rooms, unit_of_work), host="localhost", port=5080)
```

#### Java

```java
// The wiring for the web app. Until Step 8 the reservations stay in a list in memory.
@Bean
HotelApi hotelApi() {
    List<Room> rooms = CATALOG.all();
    BookingService service = new BookingService(rooms, new InMemoryReservationStore(), new EmailNotificationSender());
    // A list in memory has nothing to save at the end of a request.
    return new HotelApi(rooms, work -> work.apply(service));
}
```

#### Go

```go
func runAPI() {
	// The wiring for the web app. Until Step 8 the reservations stay in a list in memory.
	hotelRooms := catalog.All()
	service := reservations.NewBookingService(hotelRooms, &reservations.InMemoryReservationStore{}, &notifications.EmailNotificationSender{}, 0)
	unitOfWork := func(work func(service *reservations.BookingService)) error {
		work(service)
		return nil // a list in memory has nothing to save at the end of a request
	}

	fmt.Println("Listening on http://localhost:5080")
	log.Fatal(http.ListenAndServe("localhost:5080", api.New(hotelRooms, unitOfWork)))
}
```

#### JavaScript

```javascript
async function runApi() {
  // The wiring for the web app. Until Step 8 the reservations stay in a list in memory.
  const rooms = catalog.all();
  const service = new BookingService(rooms, new InMemoryReservationStore(), new EmailNotificationSender());
  // A list in memory has nothing to save at the end of a request.
  createApi(rooms, work => work(service)).listen(5080, 'localhost', () => console.log('Listening on http://localhost:5080'));
}
```

#### Dart

```dart
Future<void> runApi() async {
  // The wiring for the web app. Until Step 8 the reservations stay in a list in memory.
  final rooms = catalog.all();
  final service = BookingService(rooms, InMemoryReservationStore(), EmailNotificationSender());
  // A list in memory has nothing to save at the end of a request.
  Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async => work(service);

  await serve(createApi(rooms, unitOfWork), 'localhost', 5080);
  print('Listening on http://localhost:5080');
}
```

### Three ways to start the program

### Run

#### C#

```text
dotnet run --project HotelBooking
dotnet run --project HotelBooking -- --demo
dotnet run --project HotelBooking -- --race
```

#### Python

```text
python main.py
python main.py --demo
python main.py --race
```

#### Java

```text
mvn -q spring-boot:run
mvn -q package
java -jar target/hotel-booking-1.0.jar --demo
java -jar target/hotel-booking-1.0.jar --race
```

#### Go

```text
go run .
go run . --demo
go run . --race
```

#### JavaScript

```text
node main.js
node main.js --demo
node main.js --race
```

#### Dart

```text
dart run bin/main.dart
dart run bin/main.dart --demo
dart run bin/main.dart --race
```

### What --demo prints (unchanged since P04)

### Expected demo output

```text
Free rooms 12-14 Nov: R7, R8
Maya books R7 12-14 Nov: Accepted R101
Omar books R7 13-15 Nov: Unavailable
Nora books R7 14-16 Nov: Accepted R102
Omar books R8 15-15 Nov: InvalidDates
Free rooms 12-14 Nov: R8
Maya cancels R101: True
Free rooms 12-14 Nov: R7, R8
Confirmation emails sent: 2
```

### Your turn · make it a web app

1. Run the install line for your language once.
2. Change your project file as in 4.1 (Java: also create application.properties).
3. Create the Api file from 4.2 in the place your tab shows: outside the four module folders.
4. In the wiring, make the program start the web app when it gets no argument, run the demo with --demo and the race with --race (4.3).
5. Start the app, open http://localhost:5080/openapi.yaml in your browser: you see your contract. Stop it with Ctrl+C.
6. Run the demo and the race: the same lines as in P06.

**When you finish, you have:** a web app on http://localhost:5080 that serves docs/openapi.yaml, and the demo and race unchanged.
### Explanation

The Api file lives outside the four modules: it only uses their public contracts, so the three border checks need nothing new. Until Step 8 the web app uses the same list store as the demo.



---

## 6. Step 5 · The three endpoints

Each endpoint reads the request, checks what the booking rule cannot check, calls BookingService once, and turns the answer into HTTP.





### What each endpoint does, in order

| Endpoint | 1 · Read | 2 · Check | 3 · Call | 4 · Answer |
| --- | --- | --- | --- | --- |
| GET /rooms/availability | checkIn, checkOut, limit, after | Real dates, check-out after check-in, limit 1–100 | FindFreeRooms | The page of free rooms in text order, and next |
| POST /reservations | The five fields of the JSON body | All five present; real dates; the room exists | Reserve | toHttp: 201, 409 or 400 |
| DELETE /reservations/{id} | The id from the path | — | Cancel | 204, or 404 NotFound |

**What an endpoint does: read, call, answer** In short: An endpoint reads the request, checks what the booking rule cannot check, calls BookingService once, and turns the answer into HTTP. The booking rule stays in BookingService. The endpoint only translates, so the same rule works for the console demo, the API and the tests.

1. Read: the five fields of the JSON body; a missing one is 400 InvalidRequest.
2. Check: the dates are real dates, and the room exists in the catalog (400 UnknownRoom).
3. Call: service.Reserve(guest, room, stay) inside the unit of work.
4. Answer: toHttp turns Accepted, Unavailable or InvalidDates into 201, 409 or 400.

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
    return unitOfWork(service => ToHttp(service.Reserve(guest, room, new DateRange(start, end))));
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
    return unitOfWork.run(service -> toHttp(service.reserve(guest, room, new DateRange(start, end))));
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
  return toHttp(result);
});
```

### Completed example 5.1 · problem details and toHttp (L04)

**From a booking answer to an HTTP answer** In short: toHttp is L04’s function: Accepted becomes 201 with the new id, Unavailable 409, InvalidDates 400.

1. BookingService answers Accepted R101: the endpoint sends 201 {"id":"R101","status":"Confirmed","notificationFailed":false}.
2. It answers Unavailable: 409 with problem details titled Unavailable.
3. It answers InvalidDates: 400 with problem details titled InvalidDates.

C#:

```text
// Turns a booking answer into an HTTP status code and a JSON body (L04).
private static IResult ToHttp(BookingResult result) => result.Outcome switch
{
    "Accepted" => Results.Json(new { id = result.ReservationId, status = "Confirmed", notificationFailed = result.NotificationFailed }, statusCode: 201),
    "Unavailable" => Problem(409, "Unavailable", "The room is booked for at least one night of this stay."),
    _ => Problem(400, "InvalidDates", "checkOut must be after checkIn."),
};
```

Python:

```text
def to_http(result: BookingResult) -> JSONResponse:
    """Turns a booking answer into an HTTP status code and a JSON body (L04)."""
    if result.outcome == "Accepted":
        return JSONResponse({"id": result.reservation_id, "status": "Confirmed",
                             "notificationFailed": result.notification_failed}, 201)
    if result.outcome == "Unavailable":
        return problem(409, "Unavailable", "The room is booked for at least one night of this stay.")
    return problem(400, "InvalidDates", "checkOut must be after checkIn.")
```

Java:

```text
// Turns a booking answer into an HTTP status code and a JSON body (L04).
static ResponseEntity<?> toHttp(BookingResult result) {
    return switch (result.outcome()) {
        case "Accepted" -> ResponseEntity.status(201).body(new Booked(result.reservationId(), "Confirmed", result.notificationFailed()));
        case "Unavailable" -> problem(409, "Unavailable", "The room is booked for at least one night of this stay.");
        default -> problem(400, "InvalidDates", "checkOut must be after checkIn.");
    };
}
```

Go:

```text
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
```

JavaScript:

```text
// Turns a booking answer into an HTTP status code and a JSON body (L04).
function toHttp(response, result) {
  if (result.outcome === 'Accepted') {
    return response.status(201).json({ id: result.reservationId, status: 'Confirmed', notificationFailed: result.notificationFailed });
  }
  if (result.outcome === 'Unavailable') return problem(response, 409, 'Unavailable', 'The room is booked for at least one night of this stay.');
  return problem(response, 400, 'InvalidDates', 'checkOut must be after checkIn.');
}
```

Dart:

```text
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
```

### Errors and booking answers

#### C#

```csharp
// Problem details (RFC 9457): every error has the same three keys.
private static IResult Problem(int status, string title, string detail) =>
    Results.Json(new { title, status, detail }, contentType: "application/problem+json", statusCode: status);

// Turns a booking answer into an HTTP status code and a JSON body (L04).
private static IResult ToHttp(BookingResult result) => result.Outcome switch
{
    "Accepted" => Results.Json(new { id = result.ReservationId, status = "Confirmed", notificationFailed = result.NotificationFailed }, statusCode: 201),
    "Unavailable" => Problem(409, "Unavailable", "The room is booked for at least one night of this stay."),
    _ => Problem(400, "InvalidDates", "checkOut must be after checkIn."),
};
```

#### Python

```python
def problem(status: int, title: str, detail: str) -> JSONResponse:
    """Problem details (RFC 9457): every error has the same three keys."""
    return JSONResponse({"title": title, "status": status, "detail": detail}, status,
                        media_type="application/problem+json")

def to_http(result: BookingResult) -> JSONResponse:
    """Turns a booking answer into an HTTP status code and a JSON body (L04)."""
    if result.outcome == "Accepted":
        return JSONResponse({"id": result.reservation_id, "status": "Confirmed",
                             "notificationFailed": result.notification_failed}, 201)
    if result.outcome == "Unavailable":
        return problem(409, "Unavailable", "The room is booked for at least one night of this stay.")
    return problem(400, "InvalidDates", "checkOut must be after checkIn.")
```

#### Java

```java
static ResponseEntity<?> problem(int status, String title, String detail) {
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(new Problem(title, status, detail));
}

// Turns a booking answer into an HTTP status code and a JSON body (L04).
static ResponseEntity<?> toHttp(BookingResult result) {
    return switch (result.outcome()) {
        case "Accepted" -> ResponseEntity.status(201).body(new Booked(result.reservationId(), "Confirmed", result.notificationFailed()));
        case "Unavailable" -> problem(409, "Unavailable", "The room is booked for at least one night of this stay.");
        default -> problem(400, "InvalidDates", "checkOut must be after checkIn.");
    };
}
```

#### Go

```go
func problem(w http.ResponseWriter, status int, title, detail string) {
	write(w, status, "application/problem+json", problemJSON{Title: title, Status: status, Detail: detail})
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
```

#### JavaScript

```javascript
// Problem details (RFC 9457): every error has the same three keys.
const problem = (response, status, title, detail) =>
  response.status(status).type('application/problem+json').send(JSON.stringify({ title, status, detail }));

// Turns a booking answer into an HTTP status code and a JSON body (L04).
function toHttp(response, result) {
  if (result.outcome === 'Accepted') {
    return response.status(201).json({ id: result.reservationId, status: 'Confirmed', notificationFailed: result.notificationFailed });
  }
  if (result.outcome === 'Unavailable') return problem(response, 409, 'Unavailable', 'The room is booked for at least one night of this stay.');
  return problem(response, 400, 'InvalidDates', 'checkOut must be after checkIn.');
}
```

#### Dart

```dart
// Problem details (RFC 9457): every error has the same three keys.
Response problem(int status, String title, String detail) => Response(status,
    body: jsonEncode({'title': title, 'status': status, 'detail': detail}),
    headers: {'content-type': 'application/problem+json'});

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
```

### Completed example 5.2 · GET /rooms/availability with limit and after

**Why R10 comes before R7** In short: Room numbers are text, and text is compared letter by letter: “R1…” comes before “R7…”, whatever follows. The contract says so (“in text order of the room number”), so every client knows what “after” means. The hotel’s demo rooms R7 and R8 are not affected.

1. Compare R10 and R7: the first letters are both R.
2. Then 1 and 7: 1 comes first, so R10 comes before R7.
3. A page that ends with R10 continues with “after=R10”: R11, R12 … and later R7.

C#:

```text
// Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
```

Python:

```text
# Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
```

Java:

```text
// Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
```

Go:

```text
// Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
```

JavaScript:

```text
// Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
```

Dart:

```text
// Cursor pagination (L04): the rooms after the last one the client has seen, in text order.
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

### Completed example 5.3 · POST /reservations

### The booking endpoint

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
    return unitOfWork(service => ToHttp(service.Reserve(guest, room, new DateRange(start, end))));
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
    return to_http(result)
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
    return unitOfWork.run(service -> toHttp(service.reserve(guest, room, new DateRange(start, end))));
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
	toHttp(w, result)
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
  toHttp(response, result);
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
  return toHttp(result);
});
```

### Completed example 5.4 · DELETE /reservations/{id}

### The cancel endpoint

#### C#

```csharp
app.MapDelete("/reservations/{id}", (string id) => unitOfWork(service => service.Cancel(id)
    ? Results.NoContent()
    : Problem(404, "NotFound", $"There is no confirmed reservation {id}.")));
```

#### Python

```python
@app.delete("/reservations/{reservation_id}")
def cancel(reservation_id: str) -> Response:
    with unit_of_work() as service:
        cancelled = service.cancel(reservation_id)
    if not cancelled:
        return problem(404, "NotFound", f"There is no confirmed reservation {reservation_id}.")
    return Response(status_code=204)
```

#### Java

```java
@DeleteMapping("/reservations/{id}")
public ResponseEntity<?> cancel(@PathVariable String id) {
    return unitOfWork.run(service -> service.cancel(id)
        ? ResponseEntity.noContent().build()
        : problem(404, "NotFound", "There is no confirmed reservation " + id + "."));
}
```

#### Go

```go
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
```

#### JavaScript

```javascript
app.delete('/reservations/:id', async (request, response) => {
  const cancelled = await unitOfWork(service => service.cancel(request.params.id));
  if (!cancelled) return problem(response, 404, 'NotFound', `There is no confirmed reservation ${request.params.id}.`);
  response.status(204).end();
});
```

#### Dart

```dart
router.delete('/reservations/<id>', (Request request, String id) async {
  final cancelled = await unitOfWork((service) => service.cancel(id));
  if (!cancelled) return problem(404, 'NotFound', 'There is no confirmed reservation $id.');
  return Response(204);
});
```

### Completed example 5.5 · the whole Api file at the end of Step 5

5.1–5.4 explain the parts. This is the complete file, with its imports, constants and the date and error helpers: compare yours with it line by line.

### The Api file after Step 5

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

// The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back. It lives
// outside the four modules and uses only their public contracts; Program.cs decides which store the
// endpoints use, through the unit of work.
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

    public static void Map(WebApplication app, IReadOnlyList<Room> rooms, UnitOfWork unitOfWork)
    {
        app.MapGet("/openapi.yaml", () => Results.Text(File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "openapi.yaml")), "application/yaml"));

        app.MapGet("/rooms/availability", (string? checkIn, string? checkOut, string? limit, string? after) =>
        {
            if (ParseDate(checkIn) is not { } start || ParseDate(checkOut) is not { } end) return Problem(400, "InvalidDates", DateDetail);
            var stay = new DateRange(start, end);
            if (!stay.IsValid()) return Problem(400, "InvalidDates", "checkOut must be after checkIn.");
            limit ??= "20";
            if (!Regex.IsMatch(limit, @"^[0-9]{1,3}\z") || int.Parse(limit) is < 1 or > 100)
                return Problem(400, "InvalidRequest", "limit must be a whole number from 1 to 100.");

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
"""The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back.

It lives outside the four modules and uses only their public contracts. main.py decides
which store the endpoints use, through unit_of_work.
"""
from __future__ import annotations

import re
from contextlib import AbstractContextManager
from datetime import date
from pathlib import Path
from typing import Any, Callable

from fastapi import Body, FastAPI
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse, Response

from hotel_booking.reservations import BookingResult, BookingService, DateRange, Guest
from hotel_booking.rooms import Room

# One request's work: "with unit_of_work() as service:" gives the endpoint a BookingService,
# and whatever changed is saved when the with-block ends.
UnitOfWork = Callable[[], AbstractContextManager[BookingService]]

FIELDS = ("guestName", "guestEmail", "room", "checkIn", "checkOut")
BODY_DETAIL = "The body must be JSON with guestName, guestEmail, room, checkIn and checkOut."
DATE_DETAIL = "checkIn and checkOut must be dates such as 2026-11-12."


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


def create_app(rooms: list[Room], unit_of_work: UnitOfWork) -> FastAPI:
    # docs/openapi.yaml is the contract, so FastAPI must not publish one of its own.
    app = FastAPI(openapi_url=None, docs_url=None, redoc_url=None)

    @app.exception_handler(RequestValidationError)
    def not_json(request, error) -> JSONResponse:
        return problem(400, "InvalidRequest", BODY_DETAIL)

    @app.get("/openapi.yaml")
    def contract() -> Response:
        return Response(Path("docs/openapi.yaml").read_text(encoding="utf-8"), media_type="application/yaml")

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

// The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back. It lives
// outside the four modules and uses only their public contracts; Main decides which store the
// endpoints use, through the unit of work.
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

    public HotelApi(List<Room> rooms, UnitOfWork unitOfWork) {
        this.rooms = rooms;
        this.unitOfWork = unitOfWork;
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
// and back. It lives outside the four modules and uses only their public contracts; main.go
// decides which store the endpoints use, through the unit of work.
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

// New returns the web app: the three endpoints and the contract.
func New(hotelRooms []rooms.Room, unitOfWork UnitOfWork) http.Handler {
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
// The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back.
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

export function createApi(rooms, unitOfWork) {
  const app = express();
  app.use(express.json());

  app.get('/openapi.yaml', (request, response) => {
    response.type('application/yaml').send(fs.readFileSync('docs/openapi.yaml', 'utf8'));
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
/// The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back.
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

Handler createApi(List<Room> rooms, UnitOfWork unitOfWork) {
  final router = Router();

  router.get('/openapi.yaml', (Request request) {
    return Response.ok(File('docs/openapi.yaml').readAsStringSync(),
        headers: {'content-type': 'application/yaml'});
  });

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

### Completed example: the request bodies for curl.exe

Three small JSON files in docs/requests/. curl.exe sends one with --data "@docs/requests/maya-r7.json".

### docs/requests/

```text
docs/requests/maya-r7.json
{"guestName":"Maya","guestEmail":"maya@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}

docs/requests/omar-r7.json
{"guestName":"Omar","guestEmail":"omar@example.com","room":"R7","checkIn":"2026-11-13","checkOut":"2026-11-15"}

docs/requests/omar-r8.json
{"guestName":"Omar","guestEmail":"omar@example.com","room":"R8","checkIn":"2026-11-15","checkOut":"2026-11-15"}
```

### Check it with curl.exe

**Reading curl.exe’s answer** In short: curl.exe sends one HTTP request; -s hides the progress bar and -w prints the status code after the body. curl.exe is built into Windows 10 and 11 and works the same in PowerShell, cmd and Git Bash. On macOS and Linux type curl.

1. The first line curl.exe prints is the body, for example {"id":"R101",…}.
2. The second line is the status code, for example 201.
3. A DELETE that works has no body: you see an empty line and 204.

```text
curl.exe -s -w "\n%{http_code}\n" "http://localhost:5080/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14"
curl.exe -s -w "\n%{http_code}\n" -X POST -H "Content-Type: application/json" --data "@docs/requests/maya-r7.json" http://localhost:5080/reservations
curl.exe -s -w "\n%{http_code}\n" -X DELETE http://localhost:5080/reservations/R101
```

### Search, book, cancel

```text
curl.exe -s -w "\n%{http_code}\n" "http://localhost:5080/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14"
curl.exe -s -w "\n%{http_code}\n" -X POST -H "Content-Type: application/json" --data "@docs/requests/maya-r7.json" http://localhost:5080/reservations
curl.exe -s -w "\n%{http_code}\n" -X DELETE http://localhost:5080/reservations/R101
```

### Your turn · add the three endpoints

Restart the app after each change (Ctrl+C, then your run command).

1. Copy the top of the whole file (5.5) into your Api file: the imports, the constants, the error answer, the date check and toHttp (5.1).
2. Add the search endpoint from 5.2 and run the first curl.exe line: 200 with R7 and R8.
3. Create docs/requests/ with the three JSON files, and add the booking endpoint from 5.3.
4. Book Maya twice with docs/requests/maya-r7.json: 201 R101, then 409 Unavailable.
5. Add the cancel endpoint from 5.4. Cancel R101 twice: 204, then 404 NotFound.
6. Add the search with &limit=1 to the URL: one room and "next":"R7".

**When you finish, you have:** three endpoints that answer exactly like the contract, on the list store, and three request files in docs/requests/.
### Explanation

The endpoint checks only what BookingService cannot know: the shape of the request and whether the room exists. Overlaps and dates in the wrong order are still decided by BookingService, so the rule lives in one place.



---

## 7. Step 6 · Four API tests

The API tests send HTTP requests to the app inside the test, with the list store: no server, no database, one second.





### The four tests

| Test | Request | Expected |
| --- | --- | --- |
| Search | GET /rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14 | 200 and the exact body with R7 and R8 |
| Book, then book again | POST Maya for R7 12–14 Nov; POST Omar for R7 13–15 Nov | 201 {"id":"R101",…}, then 409 Unavailable |
| Wrong dates | POST with checkOut equal to checkIn | 400 InvalidDates as application/problem+json |
| Cancel twice | POST Maya, DELETE /reservations/R101 twice | 204, then 404 NotFound |

**Testing HTTP without a network** In short: Each framework can send a request straight to the app inside the test, so the API tests need no running server and no database. ASP.NET Core has a test server, FastAPI a TestClient, Spring MockMvc and Go httptest; a shelf app is a function you can call, and the Express test listens on a free port of your laptop.

1. The test builds the web app with the list store, exactly as the wiring did before Step 8.
2. It sends POST /reservations with Maya’s JSON body.
3. It checks the status (201) and the exact body.

C#:

```text
// The web app with the in-memory store, on ASP.NET Core's test server: the client sends requests without a network.
private static async Task<HttpClient> NewClient()
{
    var service = new BookingService(Rooms, new InMemoryReservationStore(), new EmailNotificationSender());
    var builder = WebApplication.CreateBuilder();
    builder.WebHost.UseTestServer();
    var app = builder.Build();
    HotelApi.Map(app, Rooms, work => work(service)); // a list in memory has nothing to save at the end of a request
    await app.StartAsync();
    return app.GetTestClient();
}
```

Python:

```text
def new_client() -> TestClient:
    """The web app with the in-memory store; FastAPI's TestClient sends the requests without a network."""
    service = BookingService([R7, R8], InMemoryReservationStore(), EmailNotificationSender())

    @contextmanager
    def unit_of_work():
        yield service  # a list in memory has nothing to save at the end of a request

    return TestClient(create_app([R7, R8], unit_of_work))
```

Java:

```text
// The web app with the in-memory store; MockMvc sends requests to it without a network.
static MockMvc newApp() {
    BookingService service = new BookingService(ROOMS, new InMemoryReservationStore(), new EmailNotificationSender());
    // A list in memory has nothing to save at the end of a request.
    return MockMvcBuilders.standaloneSetup(new HotelApi(ROOMS, work -> work.apply(service))).build();
}
```

Go:

```text
// newApp is the web app with the in-memory store; httptest sends requests to it without a network.
func newApp() http.Handler {
	hotelRooms := []rooms.Room{{Number: "R7", Type: "Standard"}, {Number: "R8", Type: "Deluxe"}}
	service := reservations.NewBookingService(hotelRooms, &reservations.InMemoryReservationStore{}, &notifications.EmailNotificationSender{}, 0)
	return New(hotelRooms, func(work func(*reservations.BookingService)) error {
		work(service)
		return nil // a list in memory has nothing to save at the end of a request
	})
}
```

JavaScript:

```text
// The web app with the in-memory store, on a free port of this laptop. send() makes one request.
async function startApp(t) {
  const service = new BookingService(rooms, new InMemoryReservationStore(), new EmailNotificationSender());
  // A list in memory has nothing to save at the end of a request.
  const server = createApi(rooms, work => work(service)).listen(0, 'localhost');
  await once(server, 'listening');
  t.after(() => server.close());
  return (method, path, body) => fetch(`http://localhost:${server.address().port}${path}`, {
    method, body: body && JSON.stringify(body), headers: { 'Content-Type': 'application/json', Connection: 'close' },
  });
}
```

Dart:

```text
// The web app with the in-memory store. A shelf app is a function from Request to Response, so the
// tests call it directly, without a network.
Handler newApp() {
  final service = BookingService(rooms, InMemoryReservationStore(), EmailNotificationSender());
  // A list in memory has nothing to save at the end of a request.
  Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async => work(service);
  return createApi(rooms, unitOfWork);
}
```

### Completed example: the API test file

### The API tests

#### C#

```csharp
// HotelBooking.Tests/ApiTests.cs
// The four API tests: HTTP in, HTTP out, with the reservations in a list.
// Run them with the other tests from the hotel-booking folder:  dotnet test HotelBooking.Tests
using System.Net;
using System.Net.Http.Json;
using HotelBooking.Api;
using HotelBooking.Notifications.Internal;
using HotelBooking.Reservations;
using HotelBooking.Reservations.Internal;
using HotelBooking.Rooms;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.TestHost;

namespace HotelBooking.Tests;

public class ApiTests
{
    private static readonly Room[] Rooms = [new("R7", "Standard"), new("R8", "Deluxe")];
    private const string Search = "/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14";
    private static readonly object Maya = new { guestName = "Maya", guestEmail = "maya@example.com", room = "R7", checkIn = "2026-11-12", checkOut = "2026-11-14" };
    private static readonly object Omar = new { guestName = "Omar", guestEmail = "omar@example.com", room = "R7", checkIn = "2026-11-13", checkOut = "2026-11-15" };

    // The web app with the in-memory store, on ASP.NET Core's test server: the client sends requests without a network.
    private static async Task<HttpClient> NewClient()
    {
        var service = new BookingService(Rooms, new InMemoryReservationStore(), new EmailNotificationSender());
        var builder = WebApplication.CreateBuilder();
        builder.WebHost.UseTestServer();
        var app = builder.Build();
        HotelApi.Map(app, Rooms, work => work(service)); // a list in memory has nothing to save at the end of a request
        await app.StartAsync();
        return app.GetTestClient();
    }

    private static async Task<string?> Title(HttpResponseMessage response) =>
        (await response.Content.ReadFromJsonAsync<Dictionary<string, object>>())?["title"].ToString();

    [Fact]
    public async Task Search_answers_200_with_the_free_rooms()
    {
        var response = await (await NewClient()).GetAsync(Search);
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        Assert.Equal("""{"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}""",
            await response.Content.ReadAsStringAsync());
    }

    [Fact]
    public async Task Booking_answers_201_then_409_for_a_shared_night()
    {
        var client = await NewClient();
        var first = await client.PostAsJsonAsync("/reservations", Maya);
        Assert.Equal(HttpStatusCode.Created, first.StatusCode);
        Assert.Equal("""{"id":"R101","status":"Confirmed","notificationFailed":false}""", await first.Content.ReadAsStringAsync());
        var second = await client.PostAsJsonAsync("/reservations", Omar);
        Assert.Equal(HttpStatusCode.Conflict, second.StatusCode);
        Assert.Equal("Unavailable", await Title(second));
    }

    [Fact]
    public async Task Wrong_dates_answer_400_with_problem_details()
    {
        var response = await (await NewClient()).PostAsJsonAsync("/reservations",
            new { guestName = "Maya", guestEmail = "maya@example.com", room = "R7", checkIn = "2026-11-12", checkOut = "2026-11-12" });
        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
        Assert.Equal("application/problem+json", response.Content.Headers.ContentType?.MediaType);
        Assert.Equal("""{"title":"InvalidDates","status":400,"detail":"checkOut must be after checkIn."}""", await response.Content.ReadAsStringAsync());
    }

    [Fact]
    public async Task Cancel_answers_204_then_404()
    {
        var client = await NewClient();
        await client.PostAsJsonAsync("/reservations", Maya);
        Assert.Equal(HttpStatusCode.NoContent, (await client.DeleteAsync("/reservations/R101")).StatusCode);
        var second = await client.DeleteAsync("/reservations/R101");
        Assert.Equal(HttpStatusCode.NotFound, second.StatusCode);
        Assert.Equal("NotFound", await Title(second));
    }
}
```

#### Python

```python
# test_api.py
"""The four API tests: HTTP in, HTTP out, with the reservations in a list.

Run them with the other tests from the hotel-booking folder:  python -m unittest -v
"""
import unittest
from contextlib import contextmanager

from fastapi.testclient import TestClient

from hotel_booking.api import create_app
from hotel_booking.notifications.internal import EmailNotificationSender
from hotel_booking.reservations import BookingService
from hotel_booking.reservations.internal import InMemoryReservationStore
from hotel_booking.rooms import Room

R7 = Room("R7", "Standard")
R8 = Room("R8", "Deluxe")
SEARCH = "/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14"
MAYA = {"guestName": "Maya", "guestEmail": "maya@example.com", "room": "R7", "checkIn": "2026-11-12", "checkOut": "2026-11-14"}
OMAR = {"guestName": "Omar", "guestEmail": "omar@example.com", "room": "R7", "checkIn": "2026-11-13", "checkOut": "2026-11-15"}


def new_client() -> TestClient:
    """The web app with the in-memory store; FastAPI's TestClient sends the requests without a network."""
    service = BookingService([R7, R8], InMemoryReservationStore(), EmailNotificationSender())

    @contextmanager
    def unit_of_work():
        yield service  # a list in memory has nothing to save at the end of a request

    return TestClient(create_app([R7, R8], unit_of_work))


class ApiTests(unittest.TestCase):
    def test_search_answers_200_with_the_free_rooms(self):
        response = new_client().get(SEARCH)
        self.assertEqual(200, response.status_code)
        self.assertEqual('{"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},'
                         '{"number":"R8","type":"Deluxe"}],"next":null}', response.text)

    def test_booking_answers_201_then_409_for_a_shared_night(self):
        client = new_client()
        first = client.post("/reservations", json=MAYA)
        self.assertEqual(201, first.status_code)
        self.assertEqual('{"id":"R101","status":"Confirmed","notificationFailed":false}', first.text)
        second = client.post("/reservations", json=OMAR)
        self.assertEqual(409, second.status_code)
        self.assertEqual("Unavailable", second.json()["title"])

    def test_wrong_dates_answer_400_with_problem_details(self):
        response = new_client().post("/reservations", json={**OMAR, "room": "R8", "checkIn": "2026-11-15", "checkOut": "2026-11-15"})
        self.assertEqual(400, response.status_code)
        self.assertEqual("application/problem+json", response.headers["content-type"])
        self.assertEqual('{"title":"InvalidDates","status":400,"detail":"checkOut must be after checkIn."}', response.text)

    def test_cancel_answers_204_then_404(self):
        client = new_client()
        client.post("/reservations", json=MAYA)
        self.assertEqual(204, client.delete("/reservations/R101").status_code)
        second = client.delete("/reservations/R101")
        self.assertEqual(404, second.status_code)
        self.assertEqual("NotFound", second.json()["title"])


if __name__ == "__main__":
    unittest.main()
```

#### Java

```java
// src/test/java/hotelbooking/ApiTests.java
package hotelbooking;

// The four API tests: HTTP in, HTTP out, with the reservations in a list.
// Run them with the other tests from the hotel-booking folder:  mvn -q test
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import hotelbooking.api.HotelApi;
import hotelbooking.notifications.internal.EmailNotificationSender;
import hotelbooking.reservations.BookingService;
import hotelbooking.reservations.internal.InMemoryReservationStore;
import hotelbooking.rooms.Room;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ApiTests {
    static final List<Room> ROOMS = List.of(new Room("R7", "Standard"), new Room("R8", "Deluxe"));
    static final String SEARCH = "/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14";
    static final String MAYA = """
        {"guestName":"Maya","guestEmail":"maya@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}""";
    static final String OMAR = """
        {"guestName":"Omar","guestEmail":"omar@example.com","room":"R7","checkIn":"2026-11-13","checkOut":"2026-11-15"}""";

    // The web app with the in-memory store; MockMvc sends requests to it without a network.
    static MockMvc newApp() {
        BookingService service = new BookingService(ROOMS, new InMemoryReservationStore(), new EmailNotificationSender());
        // A list in memory has nothing to save at the end of a request.
        return MockMvcBuilders.standaloneSetup(new HotelApi(ROOMS, work -> work.apply(service))).build();
    }

    @Test
    void searchAnswers200WithTheFreeRooms() throws Exception {
        newApp().perform(get(SEARCH))
            .andExpect(status().isOk())
            .andExpect(content().string("""
                {"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}"""));
    }

    @Test
    void bookingAnswers201Then409ForASharedNight() throws Exception {
        MockMvc app = newApp();
        app.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content(MAYA))
            .andExpect(status().isCreated())
            .andExpect(content().string("""
                {"id":"R101","status":"Confirmed","notificationFailed":false}"""));
        app.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content(OMAR))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Unavailable"));
    }

    @Test
    void wrongDatesAnswer400WithProblemDetails() throws Exception {
        newApp().perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content(MAYA.replace("\"checkOut\":\"2026-11-14\"", "\"checkOut\":\"2026-11-12\"")))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(content().string("""
                {"title":"InvalidDates","status":400,"detail":"checkOut must be after checkIn."}"""));
    }

    @Test
    void cancelAnswers204Then404() throws Exception {
        MockMvc app = newApp();
        app.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content(MAYA));
        app.perform(delete("/reservations/R101")).andExpect(status().isNoContent());
        app.perform(delete("/reservations/R101"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("NotFound"));
    }
}
```

#### Go

```go
// api/api_test.go
// The four API tests: HTTP in, HTTP out, with the reservations in a list.
// Run them with the other tests from the hotel-booking folder:  go test -v ./...
package api

import (
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"hotelbooking/notifications"
	"hotelbooking/reservations"
	"hotelbooking/rooms"
)

const search = "/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14"
const maya = `{"guestName":"Maya","guestEmail":"maya@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}`
const omar = `{"guestName":"Omar","guestEmail":"omar@example.com","room":"R7","checkIn":"2026-11-13","checkOut":"2026-11-15"}`

// newApp is the web app with the in-memory store; httptest sends requests to it without a network.
func newApp() http.Handler {
	hotelRooms := []rooms.Room{{Number: "R7", Type: "Standard"}, {Number: "R8", Type: "Deluxe"}}
	service := reservations.NewBookingService(hotelRooms, &reservations.InMemoryReservationStore{}, &notifications.EmailNotificationSender{}, 0)
	return New(hotelRooms, func(work func(*reservations.BookingService)) error {
		work(service)
		return nil // a list in memory has nothing to save at the end of a request
	})
}

func send(app http.Handler, method, path, body string) *httptest.ResponseRecorder {
	request := httptest.NewRequest(method, path, strings.NewReader(body))
	request.Header.Set("Content-Type", "application/json")
	response := httptest.NewRecorder()
	app.ServeHTTP(response, request)
	return response
}

func expect(t *testing.T, response *httptest.ResponseRecorder, status int, body string) {
	t.Helper()
	if response.Code != status || !strings.Contains(response.Body.String(), body) {
		t.Errorf("got %d %s, want %d with %s", response.Code, response.Body.String(), status, body)
	}
}

func TestSearchAnswers200WithTheFreeRooms(t *testing.T) {
	expect(t, send(newApp(), "GET", search, ""), 200,
		`{"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}`)
}

func TestBookingAnswers201Then409ForASharedNight(t *testing.T) {
	app := newApp()
	expect(t, send(app, "POST", "/reservations", maya), 201, `{"id":"R101","status":"Confirmed","notificationFailed":false}`)
	expect(t, send(app, "POST", "/reservations", omar), 409, `"title":"Unavailable"`)
}

func TestWrongDatesAnswer400WithProblemDetails(t *testing.T) {
	response := send(newApp(), "POST", "/reservations", strings.Replace(maya, `"checkOut":"2026-11-14"`, `"checkOut":"2026-11-12"`, 1))
	expect(t, response, 400, `{"title":"InvalidDates","status":400,"detail":"checkOut must be after checkIn."}`)
	if response.Header().Get("Content-Type") != "application/problem+json" {
		t.Errorf("Content-Type is %s", response.Header().Get("Content-Type"))
	}
}

func TestCancelAnswers204Then404(t *testing.T) {
	app := newApp()
	send(app, "POST", "/reservations", maya)
	expect(t, send(app, "DELETE", "/reservations/R101", ""), 204, "")
	expect(t, send(app, "DELETE", "/reservations/R101", ""), 404, `"title":"NotFound"`)
}
```

#### JavaScript

```javascript
// test/api.test.js
// The four API tests: HTTP in, HTTP out, with the reservations in a list.
// Run them with the other tests from the hotel-booking folder:  node --test
import assert from 'node:assert/strict';
import { once } from 'node:events';
import test from 'node:test';

import { createApi } from '../src/api.js';
import { EmailNotificationSender } from '../src/notifications/internal/email.js';
import { BookingService } from '../src/reservations/index.js';
import { InMemoryReservationStore } from '../src/reservations/internal/store.js';
import { Room } from '../src/rooms/index.js';

const rooms = [new Room('R7', 'Standard'), new Room('R8', 'Deluxe')];
const search = '/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14';
const maya = { guestName: 'Maya', guestEmail: 'maya@example.com', room: 'R7', checkIn: '2026-11-12', checkOut: '2026-11-14' };
const omar = { guestName: 'Omar', guestEmail: 'omar@example.com', room: 'R7', checkIn: '2026-11-13', checkOut: '2026-11-15' };

// The web app with the in-memory store, on a free port of this laptop. send() makes one request.
async function startApp(t) {
  const service = new BookingService(rooms, new InMemoryReservationStore(), new EmailNotificationSender());
  // A list in memory has nothing to save at the end of a request.
  const server = createApi(rooms, work => work(service)).listen(0, 'localhost');
  await once(server, 'listening');
  t.after(() => server.close());
  return (method, path, body) => fetch(`http://localhost:${server.address().port}${path}`, {
    method, body: body && JSON.stringify(body), headers: { 'Content-Type': 'application/json', Connection: 'close' },
  });
}

test('search answers 200 with the free rooms', async t => {
  const send = await startApp(t);
  const response = await send('GET', search);
  assert.equal(response.status, 200);
  assert.equal(await response.text(),
    '{"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}');
});

test('booking answers 201, then 409 for a shared night', async t => {
  const send = await startApp(t);
  const first = await send('POST', '/reservations', maya);
  assert.equal(first.status, 201);
  assert.equal(await first.text(), '{"id":"R101","status":"Confirmed","notificationFailed":false}');
  const second = await send('POST', '/reservations', omar);
  assert.equal(second.status, 409);
  assert.equal((await second.json()).title, 'Unavailable');
});

test('wrong dates answer 400 with problem details', async t => {
  const send = await startApp(t);
  const response = await send('POST', '/reservations', { ...maya, checkOut: '2026-11-12' });
  assert.equal(response.status, 400);
  assert.match(response.headers.get('content-type'), /^application\/problem\+json/);
  assert.equal(await response.text(), '{"title":"InvalidDates","status":400,"detail":"checkOut must be after checkIn."}');
});

test('cancel answers 204, then 404', async t => {
  const send = await startApp(t);
  await send('POST', '/reservations', maya);
  assert.equal((await send('DELETE', '/reservations/R101')).status, 204);
  const second = await send('DELETE', '/reservations/R101');
  assert.equal(second.status, 404);
  assert.equal((await second.json()).title, 'NotFound');
});
```

#### Dart

```dart
// test/api_test.dart
// The four API tests: HTTP in, HTTP out, with the reservations in a list.
// Run them with the other tests from the hotel-booking folder:  dart test
import 'dart:async';
import 'dart:convert';

import 'package:hotel_booking/api.dart';
import 'package:hotel_booking/notifications/src/email.dart';
import 'package:hotel_booking/reservations/reservations.dart';
import 'package:hotel_booking/reservations/src/store.dart';
import 'package:hotel_booking/rooms/rooms.dart';
import 'package:shelf/shelf.dart';
import 'package:test/test.dart';

final rooms = [Room('R7', 'Standard'), Room('R8', 'Deluxe')];
const search = '/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14';
const maya = {
  'guestName': 'Maya',
  'guestEmail': 'maya@example.com',
  'room': 'R7',
  'checkIn': '2026-11-12',
  'checkOut': '2026-11-14'
};
const omar = {
  'guestName': 'Omar',
  'guestEmail': 'omar@example.com',
  'room': 'R7',
  'checkIn': '2026-11-13',
  'checkOut': '2026-11-15'
};

// The web app with the in-memory store. A shelf app is a function from Request to Response, so the
// tests call it directly, without a network.
Handler newApp() {
  final service = BookingService(rooms, InMemoryReservationStore(), EmailNotificationSender());
  // A list in memory has nothing to save at the end of a request.
  Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async => work(service);
  return createApi(rooms, unitOfWork);
}

Future<Response> send(Handler app, String method, String path, [Object? body]) async =>
    app(Request(method, Uri.parse('http://localhost$path'),
        body: body == null ? null : jsonEncode(body),
        headers: {'content-type': 'application/json'}));

void main() {
  test('search answers 200 with the free rooms', () async {
    final response = await send(newApp(), 'GET', search);
    expect(response.statusCode, 200);
    expect(await response.readAsString(),
        '{"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}');
  });

  test('booking answers 201, then 409 for a shared night', () async {
    final app = newApp();
    final first = await send(app, 'POST', '/reservations', maya);
    expect(first.statusCode, 201);
    expect(await first.readAsString(),
        '{"id":"R101","status":"Confirmed","notificationFailed":false}');
    final second = await send(app, 'POST', '/reservations', omar);
    expect(second.statusCode, 409);
    expect(jsonDecode(await second.readAsString())['title'], 'Unavailable');
  });

  test('wrong dates answer 400 with problem details', () async {
    final response =
        await send(newApp(), 'POST', '/reservations', {...maya, 'checkOut': '2026-11-12'});
    expect(response.statusCode, 400);
    expect(response.headers['content-type'], 'application/problem+json');
    expect(await response.readAsString(),
        '{"title":"InvalidDates","status":400,"detail":"checkOut must be after checkIn."}');
  });

  test('cancel answers 204, then 404', () async {
    final app = newApp();
    await send(app, 'POST', '/reservations', maya);
    expect((await send(app, 'DELETE', '/reservations/R101')).statusCode, 204);
    final second = await send(app, 'DELETE', '/reservations/R101');
    expect(second.statusCode, 404);
    expect(jsonDecode(await second.readAsString())['title'], 'NotFound');
  });
}
```

### What you will see

### Green: 8 behaviour + 3 architecture + 4 API

#### C#

```text
Passed!  - Failed: 0, Passed: 15, Skipped: 0, Total: 15
```

#### Python

```text
python -m unittest
Ran 12 tests
OK

lint-imports
Contracts: 3 kept, 0 broken.
```

#### Java

```text
Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
```

#### Go

```text
go test ./...
ok      hotelbooking                 (3 architecture tests)
ok      hotelbooking/api             (4 API tests)
ok      hotelbooking/reservations    (8 booking tests)
```

#### JavaScript

```text
node --test
ℹ tests 15
ℹ pass 15
ℹ fail 0
```

#### Dart

```text
dart test
+15: All tests passed!
```

### Your turn · test the API

1. Create the API test file in the place your tab shows.
2. Type the helper that builds the app with the list store, then the four tests.
3. Run your checks: 15 green (Python: 12 tests, then “Contracts: 3 kept”).
4. Change 409 to 201 in the second test, run it and watch it fail; change it back.
5. In docs/design-notes.md add ## API tests with one line per test: request and expected answer.

**When you finish, you have:** four API tests that pass, 15 green checks, and the tests listed in design-notes.md.
### Explanation

The API tests use the list store on purpose: they check the HTTP answers, quickly and on any laptop. The PostgreSQL stores are checked by running the app against the database in Step 8.



---

## 8. Step 7 · The PostgreSQL stores

A room catalog and a reservation store that read and write the init.sql tables. They live in the internal code of Rooms and Reservations; the booking rule does not change.





### Where the new code goes

| New code | Module | Public or internal | What it does |
| --- | --- | --- | --- |
| PostgresRoomCatalog | Rooms | Internal | Reads the rooms table once, when the app starts |
| PostgresReservationStore and its row class | Reservations | Internal | Keeps the store promise with SQL; remembers what it hands out |
| JavaScript and Dart: BookingService awaits the store | Reservations | Public | pg and postgres answer later, so the service waits for each answer |

**The room catalog from the rooms table** In short: PostgresRoomCatalog answers the same question as the list catalog, which rooms the hotel sells, by reading the rooms table. The wiring asks it once, when the app starts, and passes the room list to BookingService as before. A search then asks one overlap question per room.

1. SELECT number, type FROM rooms returns R7 Standard and R8 Deluxe.
2. The catalog turns each row into a Room.
3. A new room needs one INSERT into rooms and a restart of the app.

C#:

```text
public IReadOnlyList<Room> All()
{
    using var db = databases.CreateDbContext();
    return db.Rooms.Select(row => new Room(row.Number, row.Type)).ToList();
}
```

Python:

```text
def all(self) -> list[Room]:
    with Session(self._engine) as session:
        return [Room(row.number, row.type) for row in session.scalars(select(RoomRow))]
```

Java:

```text
@Override
public List<Room> all() {
    return db.createQuery("SELECT r FROM RoomRow r", RoomRow.class).getResultList().stream()
        .map(row -> new Room(row.number, row.type))
        .toList();
}
```

Go:

```text
// All panics when the database cannot be read: the app cannot start without its rooms.
func (catalog *PostgresRoomCatalog) All() []Room {
	rows, err := catalog.pool.Query(context.Background(), "SELECT number, type FROM rooms")
	if err != nil {
		panic(err)
	}
	all, err := pgx.CollectRows(rows, pgx.RowToStructByPos[Room])
	if err != nil {
		panic(err)
	}
	return all
}
```

JavaScript:

```text
async all() {
  const { rows } = await this.#pool.query('SELECT number, type FROM rooms');
  return rows.map(row => new Room(row.number, row.type));
}
```

Dart:

```text
@override
Future<List<Room>> all() async {
  final rows = await _pool.execute('SELECT number, type FROM rooms');
  return [for (final [number, type] in rows) Room(number as String, type as String)];
}
```

**A row class next to the domain class** In short: The store reads and writes a small row class that matches the table; it builds the domain Reservation from it, so Reservation stays free of database code. The table only stores the room number, so the store looks the Room up in the list of rooms it receives from the wiring.

1. The row has seven fields, one per column of db/init.sql.
2. Find reads one row and builds new Reservation(id, guest, room, stay).
3. A Cancelled row becomes a reservation on which Cancel was already called.

C#:

```text
// One row of the reservations table in db/init.sql.
public class ReservationRow
{
    public required string Id { get; set; }
    public required string GuestName { get; set; }
    public required string GuestEmail { get; set; }
    public required string Room { get; set; }
    public DateOnly CheckIn { get; set; }
    public DateOnly CheckOut { get; set; }
    public required string Status { get; set; }
}
```

Python:

```text
class ReservationRow(ReservationsTables):
    """One row of the reservations table in db/init.sql."""
    __tablename__ = "reservations"
    id: Mapped[str] = mapped_column(primary_key=True)
    guest_name: Mapped[str]
    guest_email: Mapped[str]
    room: Mapped[str]
    check_in: Mapped[date]
    check_out: Mapped[date]
    status: Mapped[str]
```

Java:

```text
// One row of the reservations table in db/init.sql (Spring Boot maps guestName to guest_name).
@Entity
@Table(name = "reservations")
class ReservationRow {
    @Id String id;
    String guestName;
    String guestEmail;
    String room;
    LocalDate checkIn;
    LocalDate checkOut;
    String status;
}
```

Go:

```text
// PostgresReservationStore keeps reservations in PostgreSQL. main.go creates one per request
// (a unit of work): it remembers the reservations it hands out, and SaveChanges writes their
// changes. The store's methods return no errors, so the store keeps the first error, and
// SaveChanges reports it instead of writing anything.
type PostgresReservationStore struct {
	ctx    context.Context
	pool   *pgxpool.Pool
	rooms  []rooms.Room
	loaded map[*Reservation]ReservationStatus // the status each found reservation had in the database
	err    error
}
```

JavaScript:

```text
// Keeps reservations in PostgreSQL. main.js creates one per request (a unit of work): it remembers
// the reservations it hands out, and saveChanges writes their changes.
export class PostgresReservationStore {
  #pool;
  #rooms;
  #loaded = new Map(); // each found reservation and the status it had in the database

  constructor(pool, rooms) {
    this.#pool = pool;
    this.#rooms = rooms;
  }

  // A Promise of the number: BookingService awaits it.
  get count() {
    return this.#number('SELECT count(*) FROM reservations');
  }

  async hasOverlap(room, stay) {
    return (await this.#number(`SELECT count(*) ${confirmedOverlaps}`, [room.number, day(stay.checkIn), day(stay.checkOut)])) > 0;
  }

  // INSERT now: a second R101 is refused here.
  async save(reservation) {
    await this.#pool.query('INSERT INTO reservations VALUES ($1, $2, $3, $4, $5, $6, $7)', [reservation.id, reservation.guest.name,
      reservation.guest.email, reservation.room.number, day(reservation.stay.checkIn), day(reservation.stay.checkOut), reservation.status]);
  }

  // Returns null when no reservation has that id.
  async find(id) {
    const { rows } = await this.#pool.query(
      'SELECT guest_name, guest_email, room, check_in::text, check_out::text, status FROM reservations WHERE id = $1', [id]);
    if (rows.length === 0) return null;
    const [row] = rows;
    const room = this.#rooms.find(candidate => candidate.number === row.room);
    const stay = new DateRange(new Date(`${row.check_in}T00:00:00Z`), new Date(`${row.check_out}T00:00:00Z`));
    const reservation = new Reservation(id, new Guest(row.guest_name, row.guest_email), room, stay);
    reservation.status = row.status;
    this.#loaded.set(reservation, row.status);
    return reservation;
  }

  countConfirmed(room, stay) {
    return this.#number(`SELECT count(*) ${confirmedOverlaps}`, [room.number, day(stay.checkIn), day(stay.checkOut)]);
  }

  // The end of the request: write the status of every loaded reservation that changed.
  async saveChanges() {
    for (const [reservation, statusInDatabase] of this.#loaded) {
      if (reservation.status !== statusInDatabase) {
        await this.#pool.query('UPDATE reservations SET status = $1 WHERE id = $2', [reservation.status, reservation.id]);
      }
    }
  }

  async #number(sql, values = []) {
    const { rows } = await this.#pool.query(sql, values);
    return Number(rows[0].count);
  }
}
```

Dart:

```text
// Keeps reservations in PostgreSQL. bin/main.dart creates one per request (a unit of work): it
// remembers the reservations it hands out, and saveChanges writes their changes.
class PostgresReservationStore implements ReservationStore {
  PostgresReservationStore(this._pool, this._rooms);

  final Pool<void> _pool;
  final List<Room> _rooms;
  // Each found reservation and the status it had in the database.
  final Map<Reservation, ReservationStatus> _loaded = {};

  @override
  Future<int> get count => _number('SELECT count(*) FROM reservations', {});

  @override
  Future<bool> hasOverlap(Room room, DateRange stay) async =>
      await _number('SELECT count(*) $confirmedOverlaps', _overlapValues(room, stay)) > 0;

  // INSERT now: a second R101 is refused here.
  @override
  Future<void> save(Reservation reservation) => _pool.execute(
          Sql.named('INSERT INTO reservations '
              'VALUES (@id, @name, @email, @room, @checkIn:date, @checkOut:date, @status)'),
          parameters: {
            'id': reservation.id,
            'name': reservation.guest.name,
            'email': reservation.guest.email,
            'room': reservation.room.number,
            'checkIn': reservation.stay.checkIn,
            'checkOut': reservation.stay.checkOut,
            'status': statusName(reservation.status),
          });

  // Returns null when no reservation has that id.
  @override
  Future<Reservation?> find(String id) async {
    final rows = await _pool.execute(
        Sql.named('SELECT guest_name, guest_email, room, check_in, check_out, status '
            'FROM reservations WHERE id = @id'),
        parameters: {'id': id});
    if (rows.isEmpty) return null;
    final [guestName, guestEmail, roomNumber, checkIn, checkOut, status] = rows.first;
    final room = _rooms.firstWhere((room) => room.number == roomNumber);
    final reservation = Reservation(id, Guest(guestName as String, guestEmail as String), room,
        DateRange(checkIn as DateTime, checkOut as DateTime));
    if (status == 'Cancelled') reservation.cancel();
    _loaded[reservation] = reservation.status;
    return reservation;
  }

  @override
  Future<int> countConfirmed(Room room, DateRange stay) =>
      _number('SELECT count(*) $confirmedOverlaps', _overlapValues(room, stay));

  // The end of the request: write the status of every loaded reservation that changed.
  Future<void> saveChanges() async {
    for (final MapEntry(key: reservation, value: statusInDatabase) in _loaded.entries) {
      if (reservation.status != statusInDatabase) {
        await _pool.execute(Sql.named('UPDATE reservations SET status = @status WHERE id = @id'),
            parameters: {'status': statusName(reservation.status), 'id': reservation.id});
      }
    }
  }

  Map<String, Object> _overlapValues(Room room, DateRange stay) =>
      {'room': room.number, 'checkIn': stay.checkIn, 'checkOut': stay.checkOut};

  Future<int> _number(String sql, Map<String, Object> values) async =>
      (await _pool.execute(Sql.named(sql), parameters: values)).first.first as int;
}
```

**BookingService in all six languages after P07** In short: In C#, Python, Java and Go BookingService is exactly the P06 file; in JavaScript and Dart it awaits the store, because pg and postgres answer later. The rule and its order do not change in any language: check the dates, check for an overlap, save, send the confirmation.

1. JavaScript and Dart: if (await store.hasOverlap(room, stay)) … instead of if (store.hasOverlap(room, stay)) ….
2. FindFreeRooms and Cancel become async, so the one test that calls them adds await.
3. The list store still answers at once: awaiting a plain value gives that value.

C#:

```text
// HotelBooking/Reservations/BookingService.cs
using HotelBooking.Notifications;
using HotelBooking.Rooms;

namespace HotelBooking.Reservations;

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
public class BookingService(IReadOnlyList<Room> rooms, IReservationStore store, INotificationSender notifications, int slowStepMs = 0)
{
    public IReadOnlyList<Room> FindFreeRooms(DateRange stay) =>
        rooms.Where(room => !store.HasOverlap(room, stay)).ToList();

    public BookingResult Reserve(Guest guest, Room room, DateRange stay)
    {
        if (!stay.IsValid()) return new("InvalidDates");
        if (store.HasOverlap(room, stay)) return new("Unavailable");

        Thread.Sleep(slowStepMs); // 0 normally; the race demo makes this step slow

        var reservation = new Reservation($"R{101 + store.Count}", guest, room, stay);
        store.Save(reservation);
        try
        {
            // Notifications gets its own message: the five fields it needs, not the Reservation.
            notifications.SendConfirmation(new Confirmation(reservation.Id, guest.Email, room.Number, stay.CheckIn, stay.CheckOut));
        }
        catch (Exception)
        {
            // R6: the booking is already saved, so a failed message must not undo or hide it.
            return new("Accepted", reservation.Id, NotificationFailed: true);
        }
        return new("Accepted", reservation.Id);
    }

    public bool Cancel(string reservationId) => store.Find(reservationId)?.Cancel() ?? false;
}
```

Python:

```text
# hotel_booking/reservations/booking.py
from __future__ import annotations

import time
from dataclasses import dataclass
from typing import Protocol

from hotel_booking.notifications import Confirmation, NotificationSender
from hotel_booking.reservations.domain import DateRange, Guest, Reservation
from hotel_booking.rooms import Room


class ReservationStore(Protocol):
    """What BookingService needs from storage. Today a list in memory; from P07 a database."""

    def count(self) -> int: ...
    def has_overlap(self, room: Room, stay: DateRange) -> bool: ...
    def save(self, reservation: Reservation) -> None: ...
    def find(self, reservation_id: str) -> Reservation | None: ...
    def count_confirmed(self, room: Room, stay: DateRange) -> int: ...


@dataclass(frozen=True)
class BookingResult:
    outcome: str
    reservation_id: str | None = None
    notification_failed: bool = False

    def __str__(self) -> str:
        return self.outcome if self.reservation_id is None else f"{self.outcome} {self.reservation_id}"


class BookingService:
    """Owns the booking rule. Uses only the public contracts of Rooms and Notifications."""

    def __init__(self, rooms: list[Room], store: ReservationStore, notifications: NotificationSender,
                 slow_step_seconds: float = 0) -> None:
        self.rooms = rooms
        self.store = store
        self.notifications = notifications
        self.slow_step_seconds = slow_step_seconds

    def find_free_rooms(self, stay: DateRange) -> list[Room]:
        return [room for room in self.rooms if not self.store.has_overlap(room, stay)]

    def reserve(self, guest: Guest, room: Room, stay: DateRange) -> BookingResult:
        if not stay.is_valid():
            return BookingResult("InvalidDates")
        if self.store.has_overlap(room, stay):
            return BookingResult("Unavailable")

        time.sleep(self.slow_step_seconds)  # 0 normally; the race demo makes this step slow

        reservation = Reservation(f"R{101 + self.store.count()}", guest, room, stay)
        self.store.save(reservation)
        try:
            # Notifications gets its own message: the five fields it needs, not the Reservation.
            self.notifications.send_confirmation(
                Confirmation(reservation.id, guest.email, room.number, stay.check_in, stay.check_out))
        except Exception:
            # R6: the booking is already saved, so a failed message must not undo or hide it.
            return BookingResult("Accepted", reservation.id, notification_failed=True)
        return BookingResult("Accepted", reservation.id)

    def cancel(self, reservation_id: str) -> bool:
        reservation = self.store.find(reservation_id)
        return reservation is not None and reservation.cancel()
```

Java:

```text
// src/main/java/hotelbooking/reservations/BookingService.java
package hotelbooking.reservations;

import java.util.List;
import hotelbooking.notifications.Confirmation;
import hotelbooking.notifications.NotificationSender;
import hotelbooking.rooms.Room;

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
public class BookingService {
    private final List<Room> rooms;
    private final ReservationStore store;
    private final NotificationSender notifications;
    private final int slowStepMs;

    public BookingService(List<Room> rooms, ReservationStore store, NotificationSender notifications) {
        this(rooms, store, notifications, 0);
    }

    public BookingService(List<Room> rooms, ReservationStore store, NotificationSender notifications, int slowStepMs) {
        this.rooms = rooms;
        this.store = store;
        this.notifications = notifications;
        this.slowStepMs = slowStepMs;
    }

    public List<Room> findFreeRooms(DateRange stay) {
        return rooms.stream().filter(room -> !store.hasOverlap(room, stay)).toList();
    }

    public BookingResult reserve(Guest guest, Room room, DateRange stay) {
        if (!stay.isValid()) return new BookingResult("InvalidDates");
        if (store.hasOverlap(room, stay)) return new BookingResult("Unavailable");

        pause(); // does nothing normally; the race demo makes this step slow

        Reservation reservation = new Reservation("R" + (101 + store.count()), guest, room, stay);
        store.save(reservation);
        try {
            // Notifications gets its own message: the five fields it needs, not the Reservation.
            notifications.sendConfirmation(new Confirmation(reservation.id(), guest.email(), room.number(), stay.checkIn(), stay.checkOut()));
        } catch (RuntimeException failure) {
            // R6: the booking is already saved, so a failed message must not undo or hide it.
            return new BookingResult("Accepted", reservation.id(), true);
        }
        return new BookingResult("Accepted", reservation.id());
    }

    public boolean cancel(String reservationId) {
        Reservation reservation = store.find(reservationId);
        return reservation != null && reservation.cancel();
    }

    private void pause() {
        try {
            Thread.sleep(slowStepMs);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
```

Go:

```text
// reservations/booking.go
package reservations

import (
	"fmt"
	"time"

	"hotelbooking/notifications"
	"hotelbooking/rooms"
)

// ReservationStore is what BookingService needs from storage.
// Today a list in memory; from P07 a database.
type ReservationStore interface {
	Count() int
	HasOverlap(room rooms.Room, stay DateRange) bool
	Save(reservation *Reservation)
	Find(id string) *Reservation
	CountConfirmed(room rooms.Room, stay DateRange) int
}

type BookingResult struct {
	Outcome            string
	ReservationID      string
	NotificationFailed bool
}

// Printing a BookingResult uses this method, the way ToString does in C#.
func (result BookingResult) String() string {
	if result.ReservationID == "" {
		return result.Outcome
	}
	return result.Outcome + " " + result.ReservationID
}

// BookingService owns the booking rule. It uses only the public contracts of Rooms and Notifications.
type BookingService struct {
	rooms         []rooms.Room
	store         ReservationStore
	notifications notifications.NotificationSender
	slowStep      time.Duration
}

func NewBookingService(rooms []rooms.Room, store ReservationStore, notifications notifications.NotificationSender, slowStep time.Duration) *BookingService {
	return &BookingService{rooms: rooms, store: store, notifications: notifications, slowStep: slowStep}
}

func (service *BookingService) FindFreeRooms(stay DateRange) []rooms.Room {
	free := []rooms.Room{}
	for _, room := range service.rooms {
		if !service.store.HasOverlap(room, stay) {
			free = append(free, room)
		}
	}
	return free
}

func (service *BookingService) Reserve(guest Guest, room rooms.Room, stay DateRange) BookingResult {
	if !stay.IsValid() {
		return BookingResult{Outcome: "InvalidDates"}
	}
	if service.store.HasOverlap(room, stay) {
		return BookingResult{Outcome: "Unavailable"}
	}

	time.Sleep(service.slowStep) // zero normally; the race demo makes this step slow

	reservation := NewReservation(fmt.Sprintf("R%d", 101+service.store.Count()), guest, room, stay)
	service.store.Save(reservation)
	// Notifications gets its own message: the five fields it needs, not the Reservation.
	confirmation := notifications.Confirmation{
		ReservationID: reservation.ID, GuestEmail: guest.Email, RoomNumber: room.Number,
		CheckIn: stay.CheckIn, CheckOut: stay.CheckOut,
	}
	if err := service.notifications.SendConfirmation(confirmation); err != nil {
		// R6: the booking is already saved, so a failed message must not undo or hide it.
		return BookingResult{Outcome: "Accepted", ReservationID: reservation.ID, NotificationFailed: true}
	}
	return BookingResult{Outcome: "Accepted", ReservationID: reservation.ID}
}

func (service *BookingService) Cancel(reservationID string) bool {
	reservation := service.store.Find(reservationID)
	return reservation != nil && reservation.Cancel()
}
```

JavaScript:

```text
// src/reservations/booking.js
import { Confirmation } from '../notifications/index.js';
import { Reservation } from './domain.js';

// A pause that lets other requests run while this one waits.
const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

export class BookingResult {
  constructor(outcome, reservationId = null, notificationFailed = false) {
    this.outcome = outcome;
    this.reservationId = reservationId;
    this.notificationFailed = notificationFailed;
  }

  toString() {
    return this.reservationId === null ? this.outcome : `${this.outcome} ${this.reservationId}`;
  }
}

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
export class BookingService {
  constructor(rooms, store, notifications, slowStepMs = 0) {
    this.rooms = rooms;
    this.store = store;
    this.notifications = notifications;
    this.slowStepMs = slowStepMs;
  }

  async findFreeRooms(stay) {
    const free = [];
    for (const room of this.rooms) {
      if (!(await this.store.hasOverlap(room, stay))) free.push(room);
    }
    return free;
  }

  async reserve(guest, room, stay) {
    if (!stay.isValid()) return new BookingResult('InvalidDates');
    if (await this.store.hasOverlap(room, stay)) return new BookingResult('Unavailable');

    await wait(this.slowStepMs); // zero normally; the race demo makes this step slow

    const reservation = new Reservation(`R${101 + (await this.store.count)}`, guest, room, stay);
    await this.store.save(reservation);
    try {
      // Notifications gets its own message: the five fields it needs, not the Reservation.
      this.notifications.sendConfirmation(
        new Confirmation(reservation.id, guest.email, room.number, stay.checkIn, stay.checkOut));
    } catch {
      // R6: the booking is already saved, so a failed message must not undo or hide it.
      return new BookingResult('Accepted', reservation.id, true);
    }
    return new BookingResult('Accepted', reservation.id);
  }

  async cancel(reservationId) {
    return (await this.store.find(reservationId))?.cancel() ?? false;
  }
}
```

Dart:

```text
// lib/reservations/src/booking.dart
import 'dart:async';

import 'package:hotel_booking/notifications/notifications.dart';
import 'package:hotel_booking/rooms/rooms.dart';

import 'domain.dart';

// What BookingService needs from storage: a list in memory answers at once, a database
// answers later (a Future). FutureOr allows both, and BookingService awaits the answer.
abstract interface class ReservationStore {
  FutureOr<int> get count;

  FutureOr<bool> hasOverlap(Room room, DateRange stay);

  FutureOr<void> save(Reservation reservation);

  // Returns null when no reservation has that id.
  FutureOr<Reservation?> find(String id);

  FutureOr<int> countConfirmed(Room room, DateRange stay);
}

class BookingResult {
  BookingResult(this.outcome, [this.reservationId, this.notificationFailed = false]);

  final String outcome;
  final String? reservationId;
  final bool notificationFailed;

  @override
  String toString() => reservationId == null ? outcome : '$outcome $reservationId';
}

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
class BookingService {
  BookingService(this.rooms, this.store, this.notifications, {this.slowStep = Duration.zero});

  final List<Room> rooms;
  final ReservationStore store;
  final NotificationSender notifications;
  final Duration slowStep;

  Future<List<Room>> findFreeRooms(DateRange stay) async => [
        for (final room in rooms)
          if (!await store.hasOverlap(room, stay)) room,
      ];

  Future<BookingResult> reserve(Guest guest, Room room, DateRange stay) async {
    if (!stay.isValid()) return BookingResult('InvalidDates');
    if (await store.hasOverlap(room, stay)) return BookingResult('Unavailable');

    // A pause that lets other requests run while this one waits.
    await Future<void>.delayed(slowStep);

    final reservation = Reservation('R${101 + await store.count}', guest, room, stay);
    await store.save(reservation);
    try {
      // Notifications gets its own message: the five fields it needs, not the Reservation.
      notifications.sendConfirmation(
          Confirmation(reservation.id, guest.email, room.number, stay.checkIn, stay.checkOut));
    } catch (failure) {
      // R6: the booking is already saved, so a failed message must not undo or hide it.
      return BookingResult('Accepted', reservation.id, true);
    }
    return BookingResult('Accepted', reservation.id);
  }

  Future<bool> cancel(String reservationId) async =>
      (await store.find(reservationId))?.cancel() ?? false;
}
```

### Completed example 7.1 · the room catalog

### PostgresRoomCatalog

#### C#

```csharp
// HotelBooking/Rooms/Internal/PostgresRoomCatalog.cs
using Microsoft.EntityFrameworkCore;

namespace HotelBooking.Rooms.Internal;

// One row of the rooms table in db/init.sql.
public class RoomRow
{
    public required string Number { get; set; }
    public required string Type { get; set; }
}

// The tables the Rooms module owns.
public class RoomsDb(DbContextOptions<RoomsDb> options) : DbContext(options)
{
    public DbSet<RoomRow> Rooms => Set<RoomRow>();

    protected override void OnModelCreating(ModelBuilder model) => model.Entity<RoomRow>(table =>
    {
        table.ToTable("rooms");
        table.HasKey(row => row.Number);
        table.Property(row => row.Number).HasColumnName("number");
        table.Property(row => row.Type).HasColumnName("type");
    });
}

// Reads the hotel's rooms from the rooms table. Only Program.cs creates it.
public class PostgresRoomCatalog(IDbContextFactory<RoomsDb> databases) : IRoomCatalog
{
    public IReadOnlyList<Room> All()
    {
        using var db = databases.CreateDbContext();
        return db.Rooms.Select(row => new Room(row.Number, row.Type)).ToList();
    }
}
```

#### Python

```python
# hotel_booking/rooms/internal.py
from __future__ import annotations

from sqlalchemy import Engine, select
from sqlalchemy.orm import DeclarativeBase, Mapped, Session, mapped_column

from hotel_booking.rooms.catalog import Room


class InMemoryRoomCatalog:
    """Keeps the hotel's rooms in a list. Only main.py and the tests create it."""

    def __init__(self, rooms: list[Room]) -> None:
        self._rooms = list(rooms)

    def all(self) -> list[Room]:
        return list(self._rooms)


class RoomsTables(DeclarativeBase):
    """The tables the Rooms module owns."""


class RoomRow(RoomsTables):
    """One row of the rooms table in db/init.sql."""
    __tablename__ = "rooms"
    number: Mapped[str] = mapped_column(primary_key=True)
    type: Mapped[str]


class PostgresRoomCatalog:
    """Reads the hotel's rooms from PostgreSQL. Only main.py creates it."""

    def __init__(self, engine: Engine) -> None:
        self._engine = engine

    def all(self) -> list[Room]:
        with Session(self._engine) as session:
            return [Room(row.number, row.type) for row in session.scalars(select(RoomRow))]
```

#### Java

```java
// src/main/java/hotelbooking/rooms/internal/PostgresRoomCatalog.java
package hotelbooking.rooms.internal;

import hotelbooking.rooms.Room;
import hotelbooking.rooms.RoomCatalog;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;

// One row of the rooms table in db/init.sql.
@Entity
@Table(name = "rooms")
class RoomRow {
    @Id String number;
    String type;
}

// Reads the hotel's rooms from the rooms table. Only Main creates it.
public class PostgresRoomCatalog implements RoomCatalog {
    private final EntityManager db;

    public PostgresRoomCatalog(EntityManager db) {
        this.db = db;
    }

    @Override
    public List<Room> all() {
        return db.createQuery("SELECT r FROM RoomRow r", RoomRow.class).getResultList().stream()
            .map(row -> new Room(row.number, row.type))
            .toList();
    }
}
```

#### Go

```go
// rooms/internal.go
package rooms

import (
	"context"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

// InMemoryRoomCatalog keeps the hotel's rooms in a list. Only main.go creates it.
type InMemoryRoomCatalog struct {
	rooms []Room
}

func NewInMemoryRoomCatalog(rooms ...Room) *InMemoryRoomCatalog {
	return &InMemoryRoomCatalog{rooms: rooms}
}

func (catalog *InMemoryRoomCatalog) All() []Room {
	return catalog.rooms
}

// PostgresRoomCatalog reads the hotel's rooms from the rooms table. Only main.go creates it.
type PostgresRoomCatalog struct {
	pool *pgxpool.Pool
}

func NewPostgresRoomCatalog(pool *pgxpool.Pool) *PostgresRoomCatalog {
	return &PostgresRoomCatalog{pool: pool}
}

// All panics when the database cannot be read: the app cannot start without its rooms.
func (catalog *PostgresRoomCatalog) All() []Room {
	rows, err := catalog.pool.Query(context.Background(), "SELECT number, type FROM rooms")
	if err != nil {
		panic(err)
	}
	all, err := pgx.CollectRows(rows, pgx.RowToStructByPos[Room])
	if err != nil {
		panic(err)
	}
	return all
}
```

#### JavaScript

```javascript
// src/rooms/internal/postgres-catalog.js
import { Room } from '../index.js';

// Reads the hotel's rooms from the rooms table. Only main.js creates it.
export class PostgresRoomCatalog {
  #pool;

  constructor(pool) {
    this.#pool = pool;
  }

  async all() {
    const { rows } = await this.#pool.query('SELECT number, type FROM rooms');
    return rows.map(row => new Room(row.number, row.type));
  }
}
```

#### Dart

```dart
// lib/rooms/src/postgres_catalog.dart
import 'package:postgres/postgres.dart';

import '../rooms.dart';

// Reads the hotel's rooms from the rooms table. Only bin/main.dart creates it.
class PostgresRoomCatalog implements RoomCatalog {
  PostgresRoomCatalog(this._pool);

  final Pool<void> _pool;

  @override
  Future<List<Room>> all() async {
    final rows = await _pool.execute('SELECT number, type FROM rooms');
    return [for (final [number, type] in rows) Room(number as String, type as String)];
  }
}
```

### Completed example 7.2 · the reservation store

**The unit of work: save at the end of the request** In short: For one request the wiring creates a fresh store, runs the endpoint’s work with BookingService, and then tells the store to save what changed. That is why BookingService does not change: Cancel still only sets the status on the reservation object; the store writes that change to PostgreSQL when the request ends. In JavaScript and Dart the service awaits the store, because those database libraries always answer later.

1. DELETE /reservations/R101 arrives. The wiring creates a store for this request only.
2. Cancel asks the store for R101: SELECT … WHERE id = 'R101'. The store remembers that reservation and its status, Confirmed.
3. Cancel sets the status to Cancelled on the object, in memory.
4. The request ends: SaveChanges sees the status changed and runs UPDATE reservations SET status = 'Cancelled' WHERE id = 'R101'.

C#:

```text
// One request: a fresh store over its own DbContext; save what changed at the end.
IResult UnitOfWork(Func<BookingService, IResult> work)
{
    using var db = databases.CreateDbContext();
    var store = new PostgresReservationStore(db, rooms);
    var result = work(new BookingService(rooms, store, email));
    store.SaveChanges();
    return result;
}
```

Python:

```text
@contextmanager
def unit_of_work():
    # One request: a fresh store over its own session; save what changed at the end.
    with Session(engine) as session:
        store = PostgresReservationStore(session, rooms)
        yield BookingService(rooms, store, email)
        store.save_changes()
```

Java:

```text
// One request: a fresh store in its own transaction; save what changed at the end.
HotelApi.UnitOfWork unitOfWork = work -> transactions.execute(transaction -> {
    PostgresReservationStore store = new PostgresReservationStore(db, rooms);
    var result = work.apply(new BookingService(rooms, store, email));
    store.saveChanges();
    return result;
});
```

Go:

```text
// One request: a fresh store; save what changed at the end.
unitOfWork := func(work func(service *reservations.BookingService)) error {
	store := reservations.NewPostgresReservationStore(ctx, pool, hotelRooms)
	work(reservations.NewBookingService(hotelRooms, store, email, 0))
	return store.SaveChanges()
}
```

JavaScript:

```text
// One request: a fresh store; save what changed at the end.
const unitOfWork = async work => {
  const store = new PostgresReservationStore(pool, rooms);
  const result = await work(new BookingService(rooms, store, email));
  await store.saveChanges();
  return result;
};
```

Dart:

```text
// One request: a fresh store; save what changed at the end.
Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async {
  final store = PostgresReservationStore(pool, rooms);
  final result = await work(BookingService(rooms, store, email));
  await store.saveChanges();
  return result;
}
```

### PostgresReservationStore

#### C#

```csharp
// HotelBooking/Reservations/Internal/PostgresReservationStore.cs
using HotelBooking.Rooms;
using Microsoft.EntityFrameworkCore;

namespace HotelBooking.Reservations.Internal;

// One row of the reservations table in db/init.sql.
public class ReservationRow
{
    public required string Id { get; set; }
    public required string GuestName { get; set; }
    public required string GuestEmail { get; set; }
    public required string Room { get; set; }
    public DateOnly CheckIn { get; set; }
    public DateOnly CheckOut { get; set; }
    public required string Status { get; set; }
}

// The tables the Reservations module owns.
public class ReservationsDb(DbContextOptions<ReservationsDb> options) : DbContext(options)
{
    public DbSet<ReservationRow> Reservations => Set<ReservationRow>();

    protected override void OnModelCreating(ModelBuilder model) => model.Entity<ReservationRow>(table =>
    {
        table.ToTable("reservations");
        table.Property(row => row.Id).HasColumnName("id");
        table.Property(row => row.GuestName).HasColumnName("guest_name");
        table.Property(row => row.GuestEmail).HasColumnName("guest_email");
        table.Property(row => row.Room).HasColumnName("room");
        table.Property(row => row.CheckIn).HasColumnName("check_in");
        table.Property(row => row.CheckOut).HasColumnName("check_out");
        table.Property(row => row.Status).HasColumnName("status");
    });
}

// Keeps reservations in PostgreSQL. Program.cs creates one per request (a unit of work): it remembers
// the reservations it hands out, and SaveChanges writes their changes.
public class PostgresReservationStore(ReservationsDb db, IReadOnlyList<Room> rooms) : IReservationStore
{
    private readonly List<(ReservationRow Row, Reservation Reservation)> loaded = [];

    public int Count => db.Reservations.Count();

    public bool HasOverlap(Room room, DateRange stay) => Confirmed(room, stay).Any();

    public void Save(Reservation reservation)
    {
        db.Reservations.Add(new ReservationRow
        {
            Id = reservation.Id, GuestName = reservation.Guest.Name, GuestEmail = reservation.Guest.Email,
            Room = reservation.Room.Number, CheckIn = reservation.Stay.CheckIn, CheckOut = reservation.Stay.CheckOut,
            Status = reservation.Status.ToString(),
        });
        db.SaveChanges(); // INSERT now: a second R101 is refused here
    }

    public Reservation? Find(string id)
    {
        var row = db.Reservations.Find(id);
        if (row is null) return null;
        var reservation = new Reservation(row.Id, new Guest(row.GuestName, row.GuestEmail),
            rooms.First(room => room.Number == row.Room), new DateRange(row.CheckIn, row.CheckOut));
        if (row.Status == "Cancelled") reservation.Cancel();
        loaded.Add((row, reservation));
        return reservation;
    }

    public int CountConfirmed(Room room, DateRange stay) => Confirmed(room, stay).Count();

    // The end of the request: copy each loaded reservation's status to its row; EF Core writes what changed.
    public void SaveChanges()
    {
        foreach (var (row, reservation) in loaded) row.Status = reservation.Status.ToString();
        db.SaveChanges();
    }

    // Confirmed reservations of this room that share at least one night with the stay.
    private IQueryable<ReservationRow> Confirmed(Room room, DateRange stay) =>
        db.Reservations.Where(row => row.Room == room.Number && row.Status == "Confirmed"
            && row.CheckIn < stay.CheckOut && stay.CheckIn < row.CheckOut);
}
```

#### Python

```python
# hotel_booking/reservations/internal.py
from __future__ import annotations

from datetime import date

from sqlalchemy import exists, func, select
from sqlalchemy.orm import DeclarativeBase, Mapped, Session, mapped_column

from hotel_booking.reservations.domain import DateRange, Guest, Reservation
from hotel_booking.rooms import Room


class InMemoryReservationStore:
    """Keeps reservations in a list. PostgresReservationStore below has the same methods."""

    def __init__(self) -> None:
        self._rows: list[Reservation] = []

    def count(self) -> int:
        return len(self._rows)

    def has_overlap(self, room: Room, stay: DateRange) -> bool:
        return any(row.blocks(room, stay) for row in self._rows)

    def save(self, reservation: Reservation) -> None:
        self._rows.append(reservation)

    def find(self, reservation_id: str) -> Reservation | None:
        return next((row for row in self._rows if row.id == reservation_id), None)

    def count_confirmed(self, room: Room, stay: DateRange) -> int:
        return sum(1 for row in self._rows if row.blocks(room, stay))


class ReservationsTables(DeclarativeBase):
    """The tables the Reservations module owns."""


class ReservationRow(ReservationsTables):
    """One row of the reservations table in db/init.sql."""
    __tablename__ = "reservations"
    id: Mapped[str] = mapped_column(primary_key=True)
    guest_name: Mapped[str]
    guest_email: Mapped[str]
    room: Mapped[str]
    check_in: Mapped[date]
    check_out: Mapped[date]
    status: Mapped[str]


class PostgresReservationStore:
    """Keeps reservations in PostgreSQL. main.py creates one per request (a unit of work): it
    remembers the reservations it hands out, and save_changes writes their changes and commits."""

    def __init__(self, session: Session, rooms: list[Room]) -> None:
        self._session = session
        self._rooms = {room.number: room for room in rooms}
        self._loaded: list[tuple[ReservationRow, Reservation]] = []

    def count(self) -> int:
        return self._session.scalar(select(func.count()).select_from(ReservationRow))

    def has_overlap(self, room: Room, stay: DateRange) -> bool:
        return self._session.scalar(select(exists(self._confirmed(room, stay))))

    def save(self, reservation: Reservation) -> None:
        row = ReservationRow(id=reservation.id, guest_name=reservation.guest.name, guest_email=reservation.guest.email,
                             room=reservation.room.number, check_in=reservation.stay.check_in,
                             check_out=reservation.stay.check_out, status=reservation.status.value)
        self._session.add(row)
        self._session.flush()  # INSERT now: a second R101 is refused here

    def find(self, reservation_id: str) -> Reservation | None:
        row = self._session.get(ReservationRow, reservation_id)
        if row is None:
            return None
        reservation = Reservation(row.id, Guest(row.guest_name, row.guest_email), self._rooms[row.room],
                                  DateRange(row.check_in, row.check_out))
        if row.status == "Cancelled":
            reservation.cancel()
        self._loaded.append((row, reservation))
        return reservation

    def count_confirmed(self, room: Room, stay: DateRange) -> int:
        return self._session.scalar(select(func.count()).select_from(self._confirmed(room, stay).subquery()))

    def save_changes(self) -> None:
        """The end of the request: copy each loaded reservation's status to its row; SQLAlchemy writes what changed."""
        for row, reservation in self._loaded:
            row.status = reservation.status.value
        self._session.commit()

    @staticmethod
    def _confirmed(room: Room, stay: DateRange):
        # Confirmed reservations of this room that share at least one night with the stay.
        return select(ReservationRow.id).where(
            ReservationRow.room == room.number, ReservationRow.status == "Confirmed",
            ReservationRow.check_in < stay.check_out, stay.check_in < ReservationRow.check_out)
```

#### Java

```java
// src/main/java/hotelbooking/reservations/internal/PostgresReservationStore.java
package hotelbooking.reservations.internal;

import hotelbooking.reservations.DateRange;
import hotelbooking.reservations.Guest;
import hotelbooking.reservations.Reservation;
import hotelbooking.reservations.ReservationStatus;
import hotelbooking.reservations.ReservationStore;
import hotelbooking.rooms.Room;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// One row of the reservations table in db/init.sql (Spring Boot maps guestName to guest_name).
@Entity
@Table(name = "reservations")
class ReservationRow {
    @Id String id;
    String guestName;
    String guestEmail;
    String room;
    LocalDate checkIn;
    LocalDate checkOut;
    String status;
}

// Keeps reservations in PostgreSQL. Main creates one per request (a unit of work): it remembers
// the reservations it hands out, and saveChanges writes their changes.
public class PostgresReservationStore implements ReservationStore {
    private final EntityManager db;
    private final List<Room> rooms;
    private final Map<ReservationRow, Reservation> loaded = new LinkedHashMap<>();

    public PostgresReservationStore(EntityManager db, List<Room> rooms) {
        this.db = db;
        this.rooms = rooms;
    }

    @Override
    public int count() {
        return db.createQuery("SELECT count(r) FROM ReservationRow r", Long.class).getSingleResult().intValue();
    }

    @Override
    public boolean hasOverlap(Room room, DateRange stay) {
        return countConfirmed(room, stay) > 0;
    }

    @Override
    public void save(Reservation reservation) {
        ReservationRow row = new ReservationRow();
        row.id = reservation.id();
        row.guestName = reservation.guest().name();
        row.guestEmail = reservation.guest().email();
        row.room = reservation.room().number();
        row.checkIn = reservation.stay().checkIn();
        row.checkOut = reservation.stay().checkOut();
        row.status = statusName(reservation.status());
        db.persist(row);
        db.flush(); // INSERT now: a second R101 is refused here
    }

    // Returns null when no reservation has that id.
    @Override
    public Reservation find(String id) {
        ReservationRow row = db.find(ReservationRow.class, id);
        if (row == null) return null;
        Room room = rooms.stream().filter(candidate -> candidate.number().equals(row.room)).findFirst().orElseThrow();
        Reservation reservation = new Reservation(row.id, new Guest(row.guestName, row.guestEmail), room, new DateRange(row.checkIn, row.checkOut));
        if (row.status.equals("Cancelled")) reservation.cancel();
        loaded.put(row, reservation);
        return reservation;
    }

    // Confirmed reservations of this room that share at least one night with the stay.
    @Override
    public int countConfirmed(Room room, DateRange stay) {
        return db.createQuery("""
                SELECT count(r) FROM ReservationRow r WHERE r.room = :room AND r.status = 'Confirmed'
                AND r.checkIn < :checkOut AND :checkIn < r.checkOut""", Long.class)
            .setParameter("room", room.number())
            .setParameter("checkIn", stay.checkIn())
            .setParameter("checkOut", stay.checkOut())
            .getSingleResult().intValue();
    }

    // The end of the request: copy each loaded reservation's status to its row; JPA writes what changed.
    public void saveChanges() {
        loaded.forEach((row, reservation) -> row.status = statusName(reservation.status()));
        db.flush();
    }

    // The status as the reservations table writes it: Confirmed or Cancelled.
    private static String statusName(ReservationStatus status) {
        return status == ReservationStatus.CONFIRMED ? "Confirmed" : "Cancelled";
    }
}
```

#### Go

```go
// reservations/internal.go
package reservations

import (
	"cmp"
	"context"
	"errors"
	"slices"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"

	"hotelbooking/rooms"
)

// InMemoryReservationStore keeps reservations in a list.
// PostgresReservationStore below has the same methods.
type InMemoryReservationStore struct {
	rows []*Reservation
}

func (store *InMemoryReservationStore) Count() int {
	return len(store.rows)
}

func (store *InMemoryReservationStore) HasOverlap(room rooms.Room, stay DateRange) bool {
	for _, row := range store.rows {
		if row.Blocks(room, stay) {
			return true
		}
	}
	return false
}

func (store *InMemoryReservationStore) Save(reservation *Reservation) {
	store.rows = append(store.rows, reservation)
}

// Returns nil when no reservation has that id.
func (store *InMemoryReservationStore) Find(id string) *Reservation {
	for _, row := range store.rows {
		if row.ID == id {
			return row
		}
	}
	return nil
}

func (store *InMemoryReservationStore) CountConfirmed(room rooms.Room, stay DateRange) int {
	confirmed := 0
	for _, row := range store.rows {
		if row.Blocks(room, stay) {
			confirmed++
		}
	}
	return confirmed
}

// PostgresReservationStore keeps reservations in PostgreSQL. main.go creates one per request
// (a unit of work): it remembers the reservations it hands out, and SaveChanges writes their
// changes. The store's methods return no errors, so the store keeps the first error, and
// SaveChanges reports it instead of writing anything.
type PostgresReservationStore struct {
	ctx    context.Context
	pool   *pgxpool.Pool
	rooms  []rooms.Room
	loaded map[*Reservation]ReservationStatus // the status each found reservation had in the database
	err    error
}

func NewPostgresReservationStore(ctx context.Context, pool *pgxpool.Pool, hotelRooms []rooms.Room) *PostgresReservationStore {
	return &PostgresReservationStore{ctx: ctx, pool: pool, rooms: hotelRooms, loaded: map[*Reservation]ReservationStatus{}}
}

// Confirmed reservations of a room that share at least one night with a stay.
const confirmedOverlaps = `FROM reservations WHERE room = $1 AND status = 'Confirmed' AND check_in < $3 AND $2 < check_out`

func (store *PostgresReservationStore) Count() int {
	return store.number("SELECT count(*) FROM reservations")
}

func (store *PostgresReservationStore) HasOverlap(room rooms.Room, stay DateRange) bool {
	return store.number("SELECT count(*) "+confirmedOverlaps, room.Number, stay.CheckIn, stay.CheckOut) > 0
}

func (store *PostgresReservationStore) Save(reservation *Reservation) {
	if store.err != nil {
		return // an earlier query failed: write nothing, SaveChanges reports the error
	}
	_, err := store.pool.Exec(store.ctx, "INSERT INTO reservations VALUES ($1, $2, $3, $4, $5, $6, $7)",
		reservation.ID, reservation.Guest.Name, reservation.Guest.Email, reservation.Room.Number,
		reservation.Stay.CheckIn, reservation.Stay.CheckOut, string(reservation.Status))
	store.err = cmp.Or(store.err, err) // a second R101 is refused here
}

// Returns nil when no reservation has that id.
func (store *PostgresReservationStore) Find(id string) *Reservation {
	var guest Guest
	var roomNumber, status string
	var stay DateRange
	err := store.pool.QueryRow(store.ctx,
		"SELECT guest_name, guest_email, room, check_in, check_out, status FROM reservations WHERE id = $1", id,
	).Scan(&guest.Name, &guest.Email, &roomNumber, &stay.CheckIn, &stay.CheckOut, &status)
	if err != nil {
		if !errors.Is(err, pgx.ErrNoRows) {
			store.err = cmp.Or(store.err, err)
		}
		return nil
	}
	index := slices.IndexFunc(store.rooms, func(room rooms.Room) bool { return room.Number == roomNumber })
	reservation := NewReservation(id, guest, store.rooms[index], stay)
	reservation.Status = ReservationStatus(status)
	store.loaded[reservation] = reservation.Status
	return reservation
}

func (store *PostgresReservationStore) CountConfirmed(room rooms.Room, stay DateRange) int {
	return store.number("SELECT count(*) "+confirmedOverlaps, room.Number, stay.CheckIn, stay.CheckOut)
}

// SaveChanges is the end of the request: write the status of every loaded reservation that changed.
func (store *PostgresReservationStore) SaveChanges() error {
	if store.err != nil {
		return store.err
	}
	for reservation, statusInDatabase := range store.loaded {
		if reservation.Status != statusInDatabase {
			if _, err := store.pool.Exec(store.ctx, "UPDATE reservations SET status = $1 WHERE id = $2",
				string(reservation.Status), reservation.ID); err != nil {
				return err
			}
		}
	}
	return nil
}

func (store *PostgresReservationStore) number(sql string, args ...any) int {
	var count int
	if err := store.pool.QueryRow(store.ctx, sql, args...).Scan(&count); err != nil {
		store.err = cmp.Or(store.err, err)
	}
	return count
}
```

#### JavaScript

```javascript
// src/reservations/internal/postgres-store.js
import { DateRange, Guest, Reservation } from '../domain.js';

// A date as PostgreSQL's date type writes it: 2026-11-12.
const day = date => date.toISOString().slice(0, 10);

// Confirmed reservations of a room that share at least one night with a stay.
const confirmedOverlaps = "FROM reservations WHERE room = $1 AND status = 'Confirmed' AND check_in < $3 AND $2 < check_out";

// Keeps reservations in PostgreSQL. main.js creates one per request (a unit of work): it remembers
// the reservations it hands out, and saveChanges writes their changes.
export class PostgresReservationStore {
  #pool;
  #rooms;
  #loaded = new Map(); // each found reservation and the status it had in the database

  constructor(pool, rooms) {
    this.#pool = pool;
    this.#rooms = rooms;
  }

  // A Promise of the number: BookingService awaits it.
  get count() {
    return this.#number('SELECT count(*) FROM reservations');
  }

  async hasOverlap(room, stay) {
    return (await this.#number(`SELECT count(*) ${confirmedOverlaps}`, [room.number, day(stay.checkIn), day(stay.checkOut)])) > 0;
  }

  // INSERT now: a second R101 is refused here.
  async save(reservation) {
    await this.#pool.query('INSERT INTO reservations VALUES ($1, $2, $3, $4, $5, $6, $7)', [reservation.id, reservation.guest.name,
      reservation.guest.email, reservation.room.number, day(reservation.stay.checkIn), day(reservation.stay.checkOut), reservation.status]);
  }

  // Returns null when no reservation has that id.
  async find(id) {
    const { rows } = await this.#pool.query(
      'SELECT guest_name, guest_email, room, check_in::text, check_out::text, status FROM reservations WHERE id = $1', [id]);
    if (rows.length === 0) return null;
    const [row] = rows;
    const room = this.#rooms.find(candidate => candidate.number === row.room);
    const stay = new DateRange(new Date(`${row.check_in}T00:00:00Z`), new Date(`${row.check_out}T00:00:00Z`));
    const reservation = new Reservation(id, new Guest(row.guest_name, row.guest_email), room, stay);
    reservation.status = row.status;
    this.#loaded.set(reservation, row.status);
    return reservation;
  }

  countConfirmed(room, stay) {
    return this.#number(`SELECT count(*) ${confirmedOverlaps}`, [room.number, day(stay.checkIn), day(stay.checkOut)]);
  }

  // The end of the request: write the status of every loaded reservation that changed.
  async saveChanges() {
    for (const [reservation, statusInDatabase] of this.#loaded) {
      if (reservation.status !== statusInDatabase) {
        await this.#pool.query('UPDATE reservations SET status = $1 WHERE id = $2', [reservation.status, reservation.id]);
      }
    }
  }

  async #number(sql, values = []) {
    const { rows } = await this.#pool.query(sql, values);
    return Number(rows[0].count);
  }
}
```

#### Dart

```dart
// lib/reservations/src/postgres_store.dart
import 'package:hotel_booking/rooms/rooms.dart';
import 'package:postgres/postgres.dart' hide DateRange; // the stay is our own DateRange

import 'booking.dart';
import 'domain.dart';

// Confirmed reservations of a room that share at least one night with a stay.
const confirmedOverlaps = "FROM reservations WHERE room = @room AND status = 'Confirmed' "
    'AND check_in < @checkOut:date AND @checkIn:date < check_out';

// Keeps reservations in PostgreSQL. bin/main.dart creates one per request (a unit of work): it
// remembers the reservations it hands out, and saveChanges writes their changes.
class PostgresReservationStore implements ReservationStore {
  PostgresReservationStore(this._pool, this._rooms);

  final Pool<void> _pool;
  final List<Room> _rooms;
  // Each found reservation and the status it had in the database.
  final Map<Reservation, ReservationStatus> _loaded = {};

  @override
  Future<int> get count => _number('SELECT count(*) FROM reservations', {});

  @override
  Future<bool> hasOverlap(Room room, DateRange stay) async =>
      await _number('SELECT count(*) $confirmedOverlaps', _overlapValues(room, stay)) > 0;

  // INSERT now: a second R101 is refused here.
  @override
  Future<void> save(Reservation reservation) => _pool.execute(
          Sql.named('INSERT INTO reservations '
              'VALUES (@id, @name, @email, @room, @checkIn:date, @checkOut:date, @status)'),
          parameters: {
            'id': reservation.id,
            'name': reservation.guest.name,
            'email': reservation.guest.email,
            'room': reservation.room.number,
            'checkIn': reservation.stay.checkIn,
            'checkOut': reservation.stay.checkOut,
            'status': statusName(reservation.status),
          });

  // Returns null when no reservation has that id.
  @override
  Future<Reservation?> find(String id) async {
    final rows = await _pool.execute(
        Sql.named('SELECT guest_name, guest_email, room, check_in, check_out, status '
            'FROM reservations WHERE id = @id'),
        parameters: {'id': id});
    if (rows.isEmpty) return null;
    final [guestName, guestEmail, roomNumber, checkIn, checkOut, status] = rows.first;
    final room = _rooms.firstWhere((room) => room.number == roomNumber);
    final reservation = Reservation(id, Guest(guestName as String, guestEmail as String), room,
        DateRange(checkIn as DateTime, checkOut as DateTime));
    if (status == 'Cancelled') reservation.cancel();
    _loaded[reservation] = reservation.status;
    return reservation;
  }

  @override
  Future<int> countConfirmed(Room room, DateRange stay) =>
      _number('SELECT count(*) $confirmedOverlaps', _overlapValues(room, stay));

  // The end of the request: write the status of every loaded reservation that changed.
  Future<void> saveChanges() async {
    for (final MapEntry(key: reservation, value: statusInDatabase) in _loaded.entries) {
      if (reservation.status != statusInDatabase) {
        await _pool.execute(Sql.named('UPDATE reservations SET status = @status WHERE id = @id'),
            parameters: {'status': statusName(reservation.status), 'id': reservation.id});
      }
    }
  }

  Map<String, Object> _overlapValues(Room room, DateRange stay) =>
      {'room': room.number, 'checkIn': stay.checkIn, 'checkOut': stay.checkOut};

  Future<int> _number(String sql, Map<String, Object> values) async =>
      (await _pool.execute(Sql.named(sql), parameters: values)).first.first as int;
}

// The status as the reservations table writes it: Confirmed or Cancelled.
String statusName(ReservationStatus status) =>
    status == ReservationStatus.confirmed ? 'Confirmed' : 'Cancelled';
```

### How each store method talks to PostgreSQL

| Method | SQL | Why |
| --- | --- | --- |
| Count | SELECT count(*) FROM reservations | The next id is R{101 + count}, as before |
| HasOverlap | SELECT count(*) or EXISTS … room, Confirmed, check_in < out AND in < check_out | One question per room: the 2 (or 40) queries of a search |
| Save | INSERT INTO reservations VALUES (…) | At once, so a second R101 is refused here |
| Find | SELECT … WHERE id = … | Builds the Reservation and remembers it |
| CountConfirmed | The same as HasOverlap, as a number | The race demo’s check |
| SaveChanges | UPDATE reservations SET status = … WHERE id = … | Only for found reservations whose status changed |

**Find: one row becomes one reservation** In short: Find reads the row with that id, builds a Reservation from it and remembers it for SaveChanges.

1. SELECT … FROM reservations WHERE id = 'R101' returns Maya’s row.
2. The store builds Maya’s Reservation with R7 and 12–14 November.
3. It remembers the reservation and its status, Confirmed, until the end of the request.

C#:

```text
public Reservation? Find(string id)
{
    var row = db.Reservations.Find(id);
    if (row is null) return null;
    var reservation = new Reservation(row.Id, new Guest(row.GuestName, row.GuestEmail),
        rooms.First(room => room.Number == row.Room), new DateRange(row.CheckIn, row.CheckOut));
    if (row.Status == "Cancelled") reservation.Cancel();
    loaded.Add((row, reservation));
    return reservation;
}
```

Python:

```text
def find(self, reservation_id: str) -> Reservation | None:
    row = self._session.get(ReservationRow, reservation_id)
    if row is None:
        return None
    reservation = Reservation(row.id, Guest(row.guest_name, row.guest_email), self._rooms[row.room],
                              DateRange(row.check_in, row.check_out))
    if row.status == "Cancelled":
        reservation.cancel()
    self._loaded.append((row, reservation))
    return reservation
```

Java:

```text
// Returns null when no reservation has that id.
@Override
public Reservation find(String id) {
    ReservationRow row = db.find(ReservationRow.class, id);
    if (row == null) return null;
    Room room = rooms.stream().filter(candidate -> candidate.number().equals(row.room)).findFirst().orElseThrow();
    Reservation reservation = new Reservation(row.id, new Guest(row.guestName, row.guestEmail), room, new DateRange(row.checkIn, row.checkOut));
    if (row.status.equals("Cancelled")) reservation.cancel();
    loaded.put(row, reservation);
    return reservation;
}
```

Go:

```text
// Returns nil when no reservation has that id.
func (store *PostgresReservationStore) Find(id string) *Reservation {
	var guest Guest
	var roomNumber, status string
	var stay DateRange
	err := store.pool.QueryRow(store.ctx,
		"SELECT guest_name, guest_email, room, check_in, check_out, status FROM reservations WHERE id = $1", id,
	).Scan(&guest.Name, &guest.Email, &roomNumber, &stay.CheckIn, &stay.CheckOut, &status)
	if err != nil {
		if !errors.Is(err, pgx.ErrNoRows) {
			store.err = cmp.Or(store.err, err)
		}
		return nil
	}
	index := slices.IndexFunc(store.rooms, func(room rooms.Room) bool { return room.Number == roomNumber })
	reservation := NewReservation(id, guest, store.rooms[index], stay)
	reservation.Status = ReservationStatus(status)
	store.loaded[reservation] = reservation.Status
	return reservation
}
```

JavaScript:

```text
// Returns null when no reservation has that id.
async find(id) {
  const { rows } = await this.#pool.query(
    'SELECT guest_name, guest_email, room, check_in::text, check_out::text, status FROM reservations WHERE id = $1', [id]);
  if (rows.length === 0) return null;
  const [row] = rows;
  const room = this.#rooms.find(candidate => candidate.number === row.room);
  const stay = new DateRange(new Date(`${row.check_in}T00:00:00Z`), new Date(`${row.check_out}T00:00:00Z`));
  const reservation = new Reservation(id, new Guest(row.guest_name, row.guest_email), room, stay);
  reservation.status = row.status;
  this.#loaded.set(reservation, row.status);
  return reservation;
}
```

Dart:

```text
// Returns null when no reservation has that id.
@override
Future<Reservation?> find(String id) async {
  final rows = await _pool.execute(
      Sql.named('SELECT guest_name, guest_email, room, check_in, check_out, status '
          'FROM reservations WHERE id = @id'),
      parameters: {'id': id});
  if (rows.isEmpty) return null;
  final [guestName, guestEmail, roomNumber, checkIn, checkOut, status] = rows.first;
  final room = _rooms.firstWhere((room) => room.number == roomNumber);
  final reservation = Reservation(id, Guest(guestName as String, guestEmail as String), room,
      DateRange(checkIn as DateTime, checkOut as DateTime));
  if (status == 'Cancelled') reservation.cancel();
  _loaded[reservation] = reservation.status;
  return reservation;
}
```

**SaveChanges writes only what changed** In short: The store compares each reservation it handed out with the status it had in the database, and updates only the changed ones. EF Core, SQLAlchemy and JPA keep the row objects and notice the change themselves; the Go, JavaScript and Dart stores remember the old status in a map.

1. A search hands out no reservation: SaveChanges has nothing to do.
2. A booking inserts its row at once in Save, so a second R101 is refused there (Go’s store keeps that error, writes nothing more, and SaveChanges reports it).
3. A cancel hands out R101 and changes its status: SaveChanges runs one UPDATE.

C#:

```text
// The end of the request: copy each loaded reservation's status to its row; EF Core writes what changed.
public void SaveChanges()
{
    foreach (var (row, reservation) in loaded) row.Status = reservation.Status.ToString();
    db.SaveChanges();
}
```

Python:

```text
def save_changes(self) -> None:
    """The end of the request: copy each loaded reservation's status to its row; SQLAlchemy writes what changed."""
    for row, reservation in self._loaded:
        row.status = reservation.status.value
    self._session.commit()
```

Java:

```text
// The end of the request: copy each loaded reservation's status to its row; JPA writes what changed.
public void saveChanges() {
    loaded.forEach((row, reservation) -> row.status = statusName(reservation.status()));
    db.flush();
}
```

Go:

```text
// SaveChanges is the end of the request: write the status of every loaded reservation that changed.
func (store *PostgresReservationStore) SaveChanges() error {
	if store.err != nil {
		return store.err
	}
	for reservation, statusInDatabase := range store.loaded {
		if reservation.Status != statusInDatabase {
			if _, err := store.pool.Exec(store.ctx, "UPDATE reservations SET status = $1 WHERE id = $2",
				string(reservation.Status), reservation.ID); err != nil {
				return err
			}
		}
	}
	return nil
}
```

JavaScript:

```text
// The end of the request: write the status of every loaded reservation that changed.
async saveChanges() {
  for (const [reservation, statusInDatabase] of this.#loaded) {
    if (reservation.status !== statusInDatabase) {
      await this.#pool.query('UPDATE reservations SET status = $1 WHERE id = $2', [reservation.status, reservation.id]);
    }
  }
}
```

Dart:

```text
// The end of the request: write the status of every loaded reservation that changed.
Future<void> saveChanges() async {
  for (final MapEntry(key: reservation, value: statusInDatabase) in _loaded.entries) {
    if (reservation.status != statusInDatabase) {
      await _pool.execute(Sql.named('UPDATE reservations SET status = @status WHERE id = @id'),
          parameters: {'status': statusName(reservation.status), 'id': reservation.id});
    }
  }
}
```

### The room catalog promise after P07

Unchanged in five languages. Dart: all() may answer later (FutureOr), because PostgresRoomCatalog reads the table with an async query.

### The room catalog contract

#### C#

```csharp
// HotelBooking/Rooms/IRoomCatalog.cs
namespace HotelBooking.Rooms;

// Rooms' public contract: which rooms the hotel sells. From P07 they come from a database.
public interface IRoomCatalog
{
    IReadOnlyList<Room> All();
}
```

#### Python

```python
# hotel_booking/rooms/catalog.py
from __future__ import annotations

from dataclasses import dataclass
from typing import Protocol


@dataclass(frozen=True)
class Room:
    number: str
    type: str


class RoomCatalog(Protocol):
    """Rooms' public contract: which rooms the hotel sells. From P07 they come from a database."""

    def all(self) -> list[Room]: ...
```

#### Java

```java
// src/main/java/hotelbooking/rooms/RoomCatalog.java
package hotelbooking.rooms;

import java.util.List;

// Rooms' public contract: which rooms the hotel sells. From P07 they come from a database.
public interface RoomCatalog {
    List<Room> all();
}
```

#### Go

```go
// rooms/rooms.go
package rooms

// Room is a room the hotel sells.
type Room struct {
	Number string
	Type   string
}

// RoomCatalog is Rooms' public contract: which rooms the hotel sells.
// From P07 the rooms come from a database.
type RoomCatalog interface {
	All() []Room
}
```

#### JavaScript

```javascript
// src/rooms/index.js
// Rooms: which rooms the hotel sells. This file is the module's public contract.
//
// A room catalog keeps one promise (JavaScript has no interface keyword):
//
//   all()   every room the hotel sells
//
// From P07 the rooms come from a database.
export class Room {
  constructor(number, type) {
    this.number = number;
    this.type = type;
  }

  // JavaScript compares objects by identity, so Room says when two values mean the same room.
  equals(other) {
    return this.number === other.number && this.type === other.type;
  }
}
```

#### Dart

```dart
// lib/rooms/rooms.dart
/// Rooms: which rooms the hotel sells. This file is the module's public contract.
library;

import 'dart:async';

class Room {
  Room(this.number, this.type);

  final String number;
  final String type;

  // Dart compares objects by identity until a class says what equal means.
  @override
  bool operator ==(Object other) => other is Room && other.number == number && other.type == type;

  @override
  int get hashCode => Object.hash(number, type);
}

// Rooms' public contract: which rooms the hotel sells. A list answers at once, the database later.
abstract interface class RoomCatalog {
  FutureOr<List<Room>> all();
}
```

### BookingService after P07, in every language

C#, Python, Java and Go: the P06 file, unchanged. JavaScript and Dart: the same rule, with await before each store call.

**BookingService in all six languages after P07** In short: In C#, Python, Java and Go BookingService is exactly the P06 file; in JavaScript and Dart it awaits the store, because pg and postgres answer later. The rule and its order do not change in any language: check the dates, check for an overlap, save, send the confirmation.

1. JavaScript and Dart: if (await store.hasOverlap(room, stay)) … instead of if (store.hasOverlap(room, stay)) ….
2. FindFreeRooms and Cancel become async, so the one test that calls them adds await.
3. The list store still answers at once: awaiting a plain value gives that value.

C#:

```text
// HotelBooking/Reservations/BookingService.cs
using HotelBooking.Notifications;
using HotelBooking.Rooms;

namespace HotelBooking.Reservations;

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
public class BookingService(IReadOnlyList<Room> rooms, IReservationStore store, INotificationSender notifications, int slowStepMs = 0)
{
    public IReadOnlyList<Room> FindFreeRooms(DateRange stay) =>
        rooms.Where(room => !store.HasOverlap(room, stay)).ToList();

    public BookingResult Reserve(Guest guest, Room room, DateRange stay)
    {
        if (!stay.IsValid()) return new("InvalidDates");
        if (store.HasOverlap(room, stay)) return new("Unavailable");

        Thread.Sleep(slowStepMs); // 0 normally; the race demo makes this step slow

        var reservation = new Reservation($"R{101 + store.Count}", guest, room, stay);
        store.Save(reservation);
        try
        {
            // Notifications gets its own message: the five fields it needs, not the Reservation.
            notifications.SendConfirmation(new Confirmation(reservation.Id, guest.Email, room.Number, stay.CheckIn, stay.CheckOut));
        }
        catch (Exception)
        {
            // R6: the booking is already saved, so a failed message must not undo or hide it.
            return new("Accepted", reservation.Id, NotificationFailed: true);
        }
        return new("Accepted", reservation.Id);
    }

    public bool Cancel(string reservationId) => store.Find(reservationId)?.Cancel() ?? false;
}
```

Python:

```text
# hotel_booking/reservations/booking.py
from __future__ import annotations

import time
from dataclasses import dataclass
from typing import Protocol

from hotel_booking.notifications import Confirmation, NotificationSender
from hotel_booking.reservations.domain import DateRange, Guest, Reservation
from hotel_booking.rooms import Room


class ReservationStore(Protocol):
    """What BookingService needs from storage. Today a list in memory; from P07 a database."""

    def count(self) -> int: ...
    def has_overlap(self, room: Room, stay: DateRange) -> bool: ...
    def save(self, reservation: Reservation) -> None: ...
    def find(self, reservation_id: str) -> Reservation | None: ...
    def count_confirmed(self, room: Room, stay: DateRange) -> int: ...


@dataclass(frozen=True)
class BookingResult:
    outcome: str
    reservation_id: str | None = None
    notification_failed: bool = False

    def __str__(self) -> str:
        return self.outcome if self.reservation_id is None else f"{self.outcome} {self.reservation_id}"


class BookingService:
    """Owns the booking rule. Uses only the public contracts of Rooms and Notifications."""

    def __init__(self, rooms: list[Room], store: ReservationStore, notifications: NotificationSender,
                 slow_step_seconds: float = 0) -> None:
        self.rooms = rooms
        self.store = store
        self.notifications = notifications
        self.slow_step_seconds = slow_step_seconds

    def find_free_rooms(self, stay: DateRange) -> list[Room]:
        return [room for room in self.rooms if not self.store.has_overlap(room, stay)]

    def reserve(self, guest: Guest, room: Room, stay: DateRange) -> BookingResult:
        if not stay.is_valid():
            return BookingResult("InvalidDates")
        if self.store.has_overlap(room, stay):
            return BookingResult("Unavailable")

        time.sleep(self.slow_step_seconds)  # 0 normally; the race demo makes this step slow

        reservation = Reservation(f"R{101 + self.store.count()}", guest, room, stay)
        self.store.save(reservation)
        try:
            # Notifications gets its own message: the five fields it needs, not the Reservation.
            self.notifications.send_confirmation(
                Confirmation(reservation.id, guest.email, room.number, stay.check_in, stay.check_out))
        except Exception:
            # R6: the booking is already saved, so a failed message must not undo or hide it.
            return BookingResult("Accepted", reservation.id, notification_failed=True)
        return BookingResult("Accepted", reservation.id)

    def cancel(self, reservation_id: str) -> bool:
        reservation = self.store.find(reservation_id)
        return reservation is not None and reservation.cancel()
```

Java:

```text
// src/main/java/hotelbooking/reservations/BookingService.java
package hotelbooking.reservations;

import java.util.List;
import hotelbooking.notifications.Confirmation;
import hotelbooking.notifications.NotificationSender;
import hotelbooking.rooms.Room;

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
public class BookingService {
    private final List<Room> rooms;
    private final ReservationStore store;
    private final NotificationSender notifications;
    private final int slowStepMs;

    public BookingService(List<Room> rooms, ReservationStore store, NotificationSender notifications) {
        this(rooms, store, notifications, 0);
    }

    public BookingService(List<Room> rooms, ReservationStore store, NotificationSender notifications, int slowStepMs) {
        this.rooms = rooms;
        this.store = store;
        this.notifications = notifications;
        this.slowStepMs = slowStepMs;
    }

    public List<Room> findFreeRooms(DateRange stay) {
        return rooms.stream().filter(room -> !store.hasOverlap(room, stay)).toList();
    }

    public BookingResult reserve(Guest guest, Room room, DateRange stay) {
        if (!stay.isValid()) return new BookingResult("InvalidDates");
        if (store.hasOverlap(room, stay)) return new BookingResult("Unavailable");

        pause(); // does nothing normally; the race demo makes this step slow

        Reservation reservation = new Reservation("R" + (101 + store.count()), guest, room, stay);
        store.save(reservation);
        try {
            // Notifications gets its own message: the five fields it needs, not the Reservation.
            notifications.sendConfirmation(new Confirmation(reservation.id(), guest.email(), room.number(), stay.checkIn(), stay.checkOut()));
        } catch (RuntimeException failure) {
            // R6: the booking is already saved, so a failed message must not undo or hide it.
            return new BookingResult("Accepted", reservation.id(), true);
        }
        return new BookingResult("Accepted", reservation.id());
    }

    public boolean cancel(String reservationId) {
        Reservation reservation = store.find(reservationId);
        return reservation != null && reservation.cancel();
    }

    private void pause() {
        try {
            Thread.sleep(slowStepMs);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
```

Go:

```text
// reservations/booking.go
package reservations

import (
	"fmt"
	"time"

	"hotelbooking/notifications"
	"hotelbooking/rooms"
)

// ReservationStore is what BookingService needs from storage.
// Today a list in memory; from P07 a database.
type ReservationStore interface {
	Count() int
	HasOverlap(room rooms.Room, stay DateRange) bool
	Save(reservation *Reservation)
	Find(id string) *Reservation
	CountConfirmed(room rooms.Room, stay DateRange) int
}

type BookingResult struct {
	Outcome            string
	ReservationID      string
	NotificationFailed bool
}

// Printing a BookingResult uses this method, the way ToString does in C#.
func (result BookingResult) String() string {
	if result.ReservationID == "" {
		return result.Outcome
	}
	return result.Outcome + " " + result.ReservationID
}

// BookingService owns the booking rule. It uses only the public contracts of Rooms and Notifications.
type BookingService struct {
	rooms         []rooms.Room
	store         ReservationStore
	notifications notifications.NotificationSender
	slowStep      time.Duration
}

func NewBookingService(rooms []rooms.Room, store ReservationStore, notifications notifications.NotificationSender, slowStep time.Duration) *BookingService {
	return &BookingService{rooms: rooms, store: store, notifications: notifications, slowStep: slowStep}
}

func (service *BookingService) FindFreeRooms(stay DateRange) []rooms.Room {
	free := []rooms.Room{}
	for _, room := range service.rooms {
		if !service.store.HasOverlap(room, stay) {
			free = append(free, room)
		}
	}
	return free
}

func (service *BookingService) Reserve(guest Guest, room rooms.Room, stay DateRange) BookingResult {
	if !stay.IsValid() {
		return BookingResult{Outcome: "InvalidDates"}
	}
	if service.store.HasOverlap(room, stay) {
		return BookingResult{Outcome: "Unavailable"}
	}

	time.Sleep(service.slowStep) // zero normally; the race demo makes this step slow

	reservation := NewReservation(fmt.Sprintf("R%d", 101+service.store.Count()), guest, room, stay)
	service.store.Save(reservation)
	// Notifications gets its own message: the five fields it needs, not the Reservation.
	confirmation := notifications.Confirmation{
		ReservationID: reservation.ID, GuestEmail: guest.Email, RoomNumber: room.Number,
		CheckIn: stay.CheckIn, CheckOut: stay.CheckOut,
	}
	if err := service.notifications.SendConfirmation(confirmation); err != nil {
		// R6: the booking is already saved, so a failed message must not undo or hide it.
		return BookingResult{Outcome: "Accepted", ReservationID: reservation.ID, NotificationFailed: true}
	}
	return BookingResult{Outcome: "Accepted", ReservationID: reservation.ID}
}

func (service *BookingService) Cancel(reservationID string) bool {
	reservation := service.store.Find(reservationID)
	return reservation != nil && reservation.Cancel()
}
```

JavaScript:

```text
// src/reservations/booking.js
import { Confirmation } from '../notifications/index.js';
import { Reservation } from './domain.js';

// A pause that lets other requests run while this one waits.
const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

export class BookingResult {
  constructor(outcome, reservationId = null, notificationFailed = false) {
    this.outcome = outcome;
    this.reservationId = reservationId;
    this.notificationFailed = notificationFailed;
  }

  toString() {
    return this.reservationId === null ? this.outcome : `${this.outcome} ${this.reservationId}`;
  }
}

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
export class BookingService {
  constructor(rooms, store, notifications, slowStepMs = 0) {
    this.rooms = rooms;
    this.store = store;
    this.notifications = notifications;
    this.slowStepMs = slowStepMs;
  }

  async findFreeRooms(stay) {
    const free = [];
    for (const room of this.rooms) {
      if (!(await this.store.hasOverlap(room, stay))) free.push(room);
    }
    return free;
  }

  async reserve(guest, room, stay) {
    if (!stay.isValid()) return new BookingResult('InvalidDates');
    if (await this.store.hasOverlap(room, stay)) return new BookingResult('Unavailable');

    await wait(this.slowStepMs); // zero normally; the race demo makes this step slow

    const reservation = new Reservation(`R${101 + (await this.store.count)}`, guest, room, stay);
    await this.store.save(reservation);
    try {
      // Notifications gets its own message: the five fields it needs, not the Reservation.
      this.notifications.sendConfirmation(
        new Confirmation(reservation.id, guest.email, room.number, stay.checkIn, stay.checkOut));
    } catch {
      // R6: the booking is already saved, so a failed message must not undo or hide it.
      return new BookingResult('Accepted', reservation.id, true);
    }
    return new BookingResult('Accepted', reservation.id);
  }

  async cancel(reservationId) {
    return (await this.store.find(reservationId))?.cancel() ?? false;
  }
}
```

Dart:

```text
// lib/reservations/src/booking.dart
import 'dart:async';

import 'package:hotel_booking/notifications/notifications.dart';
import 'package:hotel_booking/rooms/rooms.dart';

import 'domain.dart';

// What BookingService needs from storage: a list in memory answers at once, a database
// answers later (a Future). FutureOr allows both, and BookingService awaits the answer.
abstract interface class ReservationStore {
  FutureOr<int> get count;

  FutureOr<bool> hasOverlap(Room room, DateRange stay);

  FutureOr<void> save(Reservation reservation);

  // Returns null when no reservation has that id.
  FutureOr<Reservation?> find(String id);

  FutureOr<int> countConfirmed(Room room, DateRange stay);
}

class BookingResult {
  BookingResult(this.outcome, [this.reservationId, this.notificationFailed = false]);

  final String outcome;
  final String? reservationId;
  final bool notificationFailed;

  @override
  String toString() => reservationId == null ? outcome : '$outcome $reservationId';
}

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
class BookingService {
  BookingService(this.rooms, this.store, this.notifications, {this.slowStep = Duration.zero});

  final List<Room> rooms;
  final ReservationStore store;
  final NotificationSender notifications;
  final Duration slowStep;

  Future<List<Room>> findFreeRooms(DateRange stay) async => [
        for (final room in rooms)
          if (!await store.hasOverlap(room, stay)) room,
      ];

  Future<BookingResult> reserve(Guest guest, Room room, DateRange stay) async {
    if (!stay.isValid()) return BookingResult('InvalidDates');
    if (await store.hasOverlap(room, stay)) return BookingResult('Unavailable');

    // A pause that lets other requests run while this one waits.
    await Future<void>.delayed(slowStep);

    final reservation = Reservation('R${101 + await store.count}', guest, room, stay);
    await store.save(reservation);
    try {
      // Notifications gets its own message: the five fields it needs, not the Reservation.
      notifications.sendConfirmation(
          Confirmation(reservation.id, guest.email, room.number, stay.checkIn, stay.checkOut));
    } catch (failure) {
      // R6: the booking is already saved, so a failed message must not undo or hide it.
      return BookingResult('Accepted', reservation.id, true);
    }
    return BookingResult('Accepted', reservation.id);
  }

  Future<bool> cancel(String reservationId) async =>
      (await store.find(reservationId))?.cancel() ?? false;
}
```

### BookingService

#### C#

```csharp
// HotelBooking/Reservations/BookingService.cs
using HotelBooking.Notifications;
using HotelBooking.Rooms;

namespace HotelBooking.Reservations;

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
public class BookingService(IReadOnlyList<Room> rooms, IReservationStore store, INotificationSender notifications, int slowStepMs = 0)
{
    public IReadOnlyList<Room> FindFreeRooms(DateRange stay) =>
        rooms.Where(room => !store.HasOverlap(room, stay)).ToList();

    public BookingResult Reserve(Guest guest, Room room, DateRange stay)
    {
        if (!stay.IsValid()) return new("InvalidDates");
        if (store.HasOverlap(room, stay)) return new("Unavailable");

        Thread.Sleep(slowStepMs); // 0 normally; the race demo makes this step slow

        var reservation = new Reservation($"R{101 + store.Count}", guest, room, stay);
        store.Save(reservation);
        try
        {
            // Notifications gets its own message: the five fields it needs, not the Reservation.
            notifications.SendConfirmation(new Confirmation(reservation.Id, guest.Email, room.Number, stay.CheckIn, stay.CheckOut));
        }
        catch (Exception)
        {
            // R6: the booking is already saved, so a failed message must not undo or hide it.
            return new("Accepted", reservation.Id, NotificationFailed: true);
        }
        return new("Accepted", reservation.Id);
    }

    public bool Cancel(string reservationId) => store.Find(reservationId)?.Cancel() ?? false;
}
```

#### Python

```python
# hotel_booking/reservations/booking.py
from __future__ import annotations

import time
from dataclasses import dataclass
from typing import Protocol

from hotel_booking.notifications import Confirmation, NotificationSender
from hotel_booking.reservations.domain import DateRange, Guest, Reservation
from hotel_booking.rooms import Room


class ReservationStore(Protocol):
    """What BookingService needs from storage. Today a list in memory; from P07 a database."""

    def count(self) -> int: ...
    def has_overlap(self, room: Room, stay: DateRange) -> bool: ...
    def save(self, reservation: Reservation) -> None: ...
    def find(self, reservation_id: str) -> Reservation | None: ...
    def count_confirmed(self, room: Room, stay: DateRange) -> int: ...


@dataclass(frozen=True)
class BookingResult:
    outcome: str
    reservation_id: str | None = None
    notification_failed: bool = False

    def __str__(self) -> str:
        return self.outcome if self.reservation_id is None else f"{self.outcome} {self.reservation_id}"


class BookingService:
    """Owns the booking rule. Uses only the public contracts of Rooms and Notifications."""

    def __init__(self, rooms: list[Room], store: ReservationStore, notifications: NotificationSender,
                 slow_step_seconds: float = 0) -> None:
        self.rooms = rooms
        self.store = store
        self.notifications = notifications
        self.slow_step_seconds = slow_step_seconds

    def find_free_rooms(self, stay: DateRange) -> list[Room]:
        return [room for room in self.rooms if not self.store.has_overlap(room, stay)]

    def reserve(self, guest: Guest, room: Room, stay: DateRange) -> BookingResult:
        if not stay.is_valid():
            return BookingResult("InvalidDates")
        if self.store.has_overlap(room, stay):
            return BookingResult("Unavailable")

        time.sleep(self.slow_step_seconds)  # 0 normally; the race demo makes this step slow

        reservation = Reservation(f"R{101 + self.store.count()}", guest, room, stay)
        self.store.save(reservation)
        try:
            # Notifications gets its own message: the five fields it needs, not the Reservation.
            self.notifications.send_confirmation(
                Confirmation(reservation.id, guest.email, room.number, stay.check_in, stay.check_out))
        except Exception:
            # R6: the booking is already saved, so a failed message must not undo or hide it.
            return BookingResult("Accepted", reservation.id, notification_failed=True)
        return BookingResult("Accepted", reservation.id)

    def cancel(self, reservation_id: str) -> bool:
        reservation = self.store.find(reservation_id)
        return reservation is not None and reservation.cancel()
```

#### Java

```java
// src/main/java/hotelbooking/reservations/BookingService.java
package hotelbooking.reservations;

import java.util.List;
import hotelbooking.notifications.Confirmation;
import hotelbooking.notifications.NotificationSender;
import hotelbooking.rooms.Room;

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
public class BookingService {
    private final List<Room> rooms;
    private final ReservationStore store;
    private final NotificationSender notifications;
    private final int slowStepMs;

    public BookingService(List<Room> rooms, ReservationStore store, NotificationSender notifications) {
        this(rooms, store, notifications, 0);
    }

    public BookingService(List<Room> rooms, ReservationStore store, NotificationSender notifications, int slowStepMs) {
        this.rooms = rooms;
        this.store = store;
        this.notifications = notifications;
        this.slowStepMs = slowStepMs;
    }

    public List<Room> findFreeRooms(DateRange stay) {
        return rooms.stream().filter(room -> !store.hasOverlap(room, stay)).toList();
    }

    public BookingResult reserve(Guest guest, Room room, DateRange stay) {
        if (!stay.isValid()) return new BookingResult("InvalidDates");
        if (store.hasOverlap(room, stay)) return new BookingResult("Unavailable");

        pause(); // does nothing normally; the race demo makes this step slow

        Reservation reservation = new Reservation("R" + (101 + store.count()), guest, room, stay);
        store.save(reservation);
        try {
            // Notifications gets its own message: the five fields it needs, not the Reservation.
            notifications.sendConfirmation(new Confirmation(reservation.id(), guest.email(), room.number(), stay.checkIn(), stay.checkOut()));
        } catch (RuntimeException failure) {
            // R6: the booking is already saved, so a failed message must not undo or hide it.
            return new BookingResult("Accepted", reservation.id(), true);
        }
        return new BookingResult("Accepted", reservation.id());
    }

    public boolean cancel(String reservationId) {
        Reservation reservation = store.find(reservationId);
        return reservation != null && reservation.cancel();
    }

    private void pause() {
        try {
            Thread.sleep(slowStepMs);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
```

#### Go

```go
// reservations/booking.go
package reservations

import (
	"fmt"
	"time"

	"hotelbooking/notifications"
	"hotelbooking/rooms"
)

// ReservationStore is what BookingService needs from storage.
// Today a list in memory; from P07 a database.
type ReservationStore interface {
	Count() int
	HasOverlap(room rooms.Room, stay DateRange) bool
	Save(reservation *Reservation)
	Find(id string) *Reservation
	CountConfirmed(room rooms.Room, stay DateRange) int
}

type BookingResult struct {
	Outcome            string
	ReservationID      string
	NotificationFailed bool
}

// Printing a BookingResult uses this method, the way ToString does in C#.
func (result BookingResult) String() string {
	if result.ReservationID == "" {
		return result.Outcome
	}
	return result.Outcome + " " + result.ReservationID
}

// BookingService owns the booking rule. It uses only the public contracts of Rooms and Notifications.
type BookingService struct {
	rooms         []rooms.Room
	store         ReservationStore
	notifications notifications.NotificationSender
	slowStep      time.Duration
}

func NewBookingService(rooms []rooms.Room, store ReservationStore, notifications notifications.NotificationSender, slowStep time.Duration) *BookingService {
	return &BookingService{rooms: rooms, store: store, notifications: notifications, slowStep: slowStep}
}

func (service *BookingService) FindFreeRooms(stay DateRange) []rooms.Room {
	free := []rooms.Room{}
	for _, room := range service.rooms {
		if !service.store.HasOverlap(room, stay) {
			free = append(free, room)
		}
	}
	return free
}

func (service *BookingService) Reserve(guest Guest, room rooms.Room, stay DateRange) BookingResult {
	if !stay.IsValid() {
		return BookingResult{Outcome: "InvalidDates"}
	}
	if service.store.HasOverlap(room, stay) {
		return BookingResult{Outcome: "Unavailable"}
	}

	time.Sleep(service.slowStep) // zero normally; the race demo makes this step slow

	reservation := NewReservation(fmt.Sprintf("R%d", 101+service.store.Count()), guest, room, stay)
	service.store.Save(reservation)
	// Notifications gets its own message: the five fields it needs, not the Reservation.
	confirmation := notifications.Confirmation{
		ReservationID: reservation.ID, GuestEmail: guest.Email, RoomNumber: room.Number,
		CheckIn: stay.CheckIn, CheckOut: stay.CheckOut,
	}
	if err := service.notifications.SendConfirmation(confirmation); err != nil {
		// R6: the booking is already saved, so a failed message must not undo or hide it.
		return BookingResult{Outcome: "Accepted", ReservationID: reservation.ID, NotificationFailed: true}
	}
	return BookingResult{Outcome: "Accepted", ReservationID: reservation.ID}
}

func (service *BookingService) Cancel(reservationID string) bool {
	reservation := service.store.Find(reservationID)
	return reservation != nil && reservation.Cancel()
}
```

#### JavaScript

```javascript
// src/reservations/booking.js
import { Confirmation } from '../notifications/index.js';
import { Reservation } from './domain.js';

// A pause that lets other requests run while this one waits.
const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

export class BookingResult {
  constructor(outcome, reservationId = null, notificationFailed = false) {
    this.outcome = outcome;
    this.reservationId = reservationId;
    this.notificationFailed = notificationFailed;
  }

  toString() {
    return this.reservationId === null ? this.outcome : `${this.outcome} ${this.reservationId}`;
  }
}

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
export class BookingService {
  constructor(rooms, store, notifications, slowStepMs = 0) {
    this.rooms = rooms;
    this.store = store;
    this.notifications = notifications;
    this.slowStepMs = slowStepMs;
  }

  async findFreeRooms(stay) {
    const free = [];
    for (const room of this.rooms) {
      if (!(await this.store.hasOverlap(room, stay))) free.push(room);
    }
    return free;
  }

  async reserve(guest, room, stay) {
    if (!stay.isValid()) return new BookingResult('InvalidDates');
    if (await this.store.hasOverlap(room, stay)) return new BookingResult('Unavailable');

    await wait(this.slowStepMs); // zero normally; the race demo makes this step slow

    const reservation = new Reservation(`R${101 + (await this.store.count)}`, guest, room, stay);
    await this.store.save(reservation);
    try {
      // Notifications gets its own message: the five fields it needs, not the Reservation.
      this.notifications.sendConfirmation(
        new Confirmation(reservation.id, guest.email, room.number, stay.checkIn, stay.checkOut));
    } catch {
      // R6: the booking is already saved, so a failed message must not undo or hide it.
      return new BookingResult('Accepted', reservation.id, true);
    }
    return new BookingResult('Accepted', reservation.id);
  }

  async cancel(reservationId) {
    return (await this.store.find(reservationId))?.cancel() ?? false;
  }
}
```

#### Dart

```dart
// lib/reservations/src/booking.dart
import 'dart:async';

import 'package:hotel_booking/notifications/notifications.dart';
import 'package:hotel_booking/rooms/rooms.dart';

import 'domain.dart';

// What BookingService needs from storage: a list in memory answers at once, a database
// answers later (a Future). FutureOr allows both, and BookingService awaits the answer.
abstract interface class ReservationStore {
  FutureOr<int> get count;

  FutureOr<bool> hasOverlap(Room room, DateRange stay);

  FutureOr<void> save(Reservation reservation);

  // Returns null when no reservation has that id.
  FutureOr<Reservation?> find(String id);

  FutureOr<int> countConfirmed(Room room, DateRange stay);
}

class BookingResult {
  BookingResult(this.outcome, [this.reservationId, this.notificationFailed = false]);

  final String outcome;
  final String? reservationId;
  final bool notificationFailed;

  @override
  String toString() => reservationId == null ? outcome : '$outcome $reservationId';
}

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
class BookingService {
  BookingService(this.rooms, this.store, this.notifications, {this.slowStep = Duration.zero});

  final List<Room> rooms;
  final ReservationStore store;
  final NotificationSender notifications;
  final Duration slowStep;

  Future<List<Room>> findFreeRooms(DateRange stay) async => [
        for (final room in rooms)
          if (!await store.hasOverlap(room, stay)) room,
      ];

  Future<BookingResult> reserve(Guest guest, Room room, DateRange stay) async {
    if (!stay.isValid()) return BookingResult('InvalidDates');
    if (await store.hasOverlap(room, stay)) return BookingResult('Unavailable');

    // A pause that lets other requests run while this one waits.
    await Future<void>.delayed(slowStep);

    final reservation = Reservation('R${101 + await store.count}', guest, room, stay);
    await store.save(reservation);
    try {
      // Notifications gets its own message: the five fields it needs, not the Reservation.
      notifications.sendConfirmation(
          Confirmation(reservation.id, guest.email, room.number, stay.checkIn, stay.checkOut));
    } catch (failure) {
      // R6: the booking is already saved, so a failed message must not undo or hide it.
      return BookingResult('Accepted', reservation.id, true);
    }
    return BookingResult('Accepted', reservation.id);
  }

  Future<bool> cancel(String reservationId) async =>
      (await store.find(reservationId))?.cancel() ?? false;
}
```

### Completed example: one cancel through the store

### Diagram: One cancel, from curl.exe to PostgreSQL and back

DELETE /reservations/R101 on your laptop. The endpoint asks the unit of work for a BookingService; Cancel finds R101 through the PostgreSQL store; at the end of the request the store writes the changed status with one UPDATE.

Diagram as text:

curl.exe sends DELETE /reservations/R101 to the app on http://localhost:5080. The DELETE endpoint reads the id and hands its work to the unit of work. The unit of work creates a PostgreSQL store for this request and a BookingService over it, and runs Cancel("R101"). Cancel asks the store for R101; the store reads the row with SELECT and remembers the reservation and its status. Cancel sets the status to Cancelled on that reservation, in memory. The work is done: the unit of work calls SaveChanges, and the store sends UPDATE reservations SET status = 'Cancelled' WHERE id = 'R101'. The endpoint answers 204 No Content. A second DELETE finds R101 already Cancelled and answers 404 NotFound.

**Step by step:** Follow the numbers: they are the order in which one cancel travels. Boxes on the right belong to the web app’s edges (the Api and the wiring); boxes on the left are the Reservations module; the grey box is the database.

1. Yellow box “curl.exe” and arrow 1: you send the request from a terminal. curl.exe prints the body (none here) and the status code.

   ```text
   curl.exe -s -w "\n%{http_code}\n" -X DELETE http://localhost:5080/reservations/R101
   ```
2. White box “Api: the DELETE endpoint”: it reads R101 from the path. It does not know which store is used; it only asks the unit of work to run its work, and turns true into 204 and false into 404 NotFound.

   C#:
   
   ```text
   app.MapDelete("/reservations/{id}", (string id) => unitOfWork(service => service.Cancel(id)
       ? Results.NoContent()
       : Problem(404, "NotFound", $"There is no confirmed reservation {id}.")));
   ```
   
   Python:
   
   ```text
   @app.delete("/reservations/{reservation_id}")
   def cancel(reservation_id: str) -> Response:
       with unit_of_work() as service:
           cancelled = service.cancel(reservation_id)
       if not cancelled:
           return problem(404, "NotFound", f"There is no confirmed reservation {reservation_id}.")
       return Response(status_code=204)
   ```
   
   Java:
   
   ```text
   @DeleteMapping("/reservations/{id}")
   public ResponseEntity<?> cancel(@PathVariable String id) {
       return unitOfWork.run(service -> service.cancel(id)
           ? ResponseEntity.noContent().build()
           : problem(404, "NotFound", "There is no confirmed reservation " + id + "."));
   }
   ```
   
   Go:
   
   ```text
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
   ```
   
   JavaScript:
   
   ```text
   app.delete('/reservations/:id', async (request, response) => {
     const cancelled = await unitOfWork(service => service.cancel(request.params.id));
     if (!cancelled) return problem(response, 404, 'NotFound', `There is no confirmed reservation ${request.params.id}.`);
     response.status(204).end();
   });
   ```
   
   Dart:
   
   ```text
   router.delete('/reservations/<id>', (Request request, String id) async {
     final cancelled = await unitOfWork((service) => service.cancel(id));
     if (!cancelled) return problem(404, 'NotFound', 'There is no confirmed reservation $id.');
     return Response(204);
   });
   ```
3. Arrow 2: unitOfWork(work). The Api file says what it needs, a function that runs some work with a BookingService; the wiring gives it one.

   C#:
   
   ```text
   // Runs one request's work with a BookingService, then saves whatever changed.
   public delegate IResult UnitOfWork(Func<BookingService, IResult> work);
   ```
   
   Python:
   
   ```text
   # One request's work: "with unit_of_work() as service:" gives the endpoint a BookingService,
   # and whatever changed is saved when the with-block ends.
   UnitOfWork = Callable[[], AbstractContextManager[BookingService]]
   ```
   
   Java:
   
   ```text
   // Runs one request's work with a BookingService, then saves whatever changed.
   @FunctionalInterface
   public interface UnitOfWork {
       ResponseEntity<?> run(Function<BookingService, ResponseEntity<?>> work);
   }
   ```
   
   Go:
   
   ```text
   // UnitOfWork runs one request's work with a BookingService and then saves whatever changed.
   type UnitOfWork func(work func(service *reservations.BookingService)) error
   ```
   
   JavaScript:
   
   ```text
   // The HTTP API: three endpoints that turn HTTP requests into BookingService calls and back.
   // It lives outside the four modules and uses only their public contracts. main.js decides which
   // store the endpoints use, through unitOfWork:
   //
   //   unitOfWork(work)   runs one request's work with a BookingService, then saves what changed
   ```
   
   Dart:
   
   ```text
   /// Runs one request's work with a BookingService, then saves whatever changed.
   typedef UnitOfWork = Future<T> Function<T>(FutureOr<T> Function(BookingService service) work);
   ```
4. White box “Wiring: the unit of work” and arrow 3: for this one request it creates a PostgresReservationStore and a BookingService over it, runs the endpoint’s work, then calls SaveChanges. In the API tests the same place simply passes the service with the list store.

   C#:
   
   ```text
   // One request: a fresh store over its own DbContext; save what changed at the end.
   IResult UnitOfWork(Func<BookingService, IResult> work)
   {
       using var db = databases.CreateDbContext();
       var store = new PostgresReservationStore(db, rooms);
       var result = work(new BookingService(rooms, store, email));
       store.SaveChanges();
       return result;
   }
   ```
   
   Python:
   
   ```text
   @contextmanager
   def unit_of_work():
       # One request: a fresh store over its own session; save what changed at the end.
       with Session(engine) as session:
           store = PostgresReservationStore(session, rooms)
           yield BookingService(rooms, store, email)
           store.save_changes()
   ```
   
   Java:
   
   ```text
   // One request: a fresh store in its own transaction; save what changed at the end.
   HotelApi.UnitOfWork unitOfWork = work -> transactions.execute(transaction -> {
       PostgresReservationStore store = new PostgresReservationStore(db, rooms);
       var result = work.apply(new BookingService(rooms, store, email));
       store.saveChanges();
       return result;
   });
   ```
   
   Go:
   
   ```text
   // One request: a fresh store; save what changed at the end.
   unitOfWork := func(work func(service *reservations.BookingService)) error {
   	store := reservations.NewPostgresReservationStore(ctx, pool, hotelRooms)
   	work(reservations.NewBookingService(hotelRooms, store, email, 0))
   	return store.SaveChanges()
   }
   ```
   
   JavaScript:
   
   ```text
   // One request: a fresh store; save what changed at the end.
   const unitOfWork = async work => {
     const store = new PostgresReservationStore(pool, rooms);
     const result = await work(new BookingService(rooms, store, email));
     await store.saveChanges();
     return result;
   };
   ```
   
   Dart:
   
   ```text
   // One request: a fresh store; save what changed at the end.
   Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async {
     final store = PostgresReservationStore(pool, rooms);
     final result = await work(BookingService(rooms, store, email));
     await store.saveChanges();
     return result;
   }
   ```
5. Green box “BookingService.Cancel” and arrow 4: the code from P06, unchanged in C#, Python, Java and Go. In JavaScript and Dart it awaits the store, because their database libraries always answer later.

   C#:
   
   ```text
   // HotelBooking/Reservations/BookingService.cs
   using HotelBooking.Notifications;
   using HotelBooking.Rooms;
   
   namespace HotelBooking.Reservations;
   
   // Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
   public class BookingService(IReadOnlyList<Room> rooms, IReservationStore store, INotificationSender notifications, int slowStepMs = 0)
   {
       public IReadOnlyList<Room> FindFreeRooms(DateRange stay) =>
           rooms.Where(room => !store.HasOverlap(room, stay)).ToList();
   
       public BookingResult Reserve(Guest guest, Room room, DateRange stay)
       {
           if (!stay.IsValid()) return new("InvalidDates");
           if (store.HasOverlap(room, stay)) return new("Unavailable");
   
           Thread.Sleep(slowStepMs); // 0 normally; the race demo makes this step slow
   
           var reservation = new Reservation($"R{101 + store.Count}", guest, room, stay);
           store.Save(reservation);
           try
           {
               // Notifications gets its own message: the five fields it needs, not the Reservation.
               notifications.SendConfirmation(new Confirmation(reservation.Id, guest.Email, room.Number, stay.CheckIn, stay.CheckOut));
           }
           catch (Exception)
           {
               // R6: the booking is already saved, so a failed message must not undo or hide it.
               return new("Accepted", reservation.Id, NotificationFailed: true);
           }
           return new("Accepted", reservation.Id);
       }
   
       public bool Cancel(string reservationId) => store.Find(reservationId)?.Cancel() ?? false;
   }
   ```
   
   Python:
   
   ```text
   # hotel_booking/reservations/booking.py
   from __future__ import annotations
   
   import time
   from dataclasses import dataclass
   from typing import Protocol
   
   from hotel_booking.notifications import Confirmation, NotificationSender
   from hotel_booking.reservations.domain import DateRange, Guest, Reservation
   from hotel_booking.rooms import Room
   
   
   class ReservationStore(Protocol):
       """What BookingService needs from storage. Today a list in memory; from P07 a database."""
   
       def count(self) -> int: ...
       def has_overlap(self, room: Room, stay: DateRange) -> bool: ...
       def save(self, reservation: Reservation) -> None: ...
       def find(self, reservation_id: str) -> Reservation | None: ...
       def count_confirmed(self, room: Room, stay: DateRange) -> int: ...
   
   
   @dataclass(frozen=True)
   class BookingResult:
       outcome: str
       reservation_id: str | None = None
       notification_failed: bool = False
   
       def __str__(self) -> str:
           return self.outcome if self.reservation_id is None else f"{self.outcome} {self.reservation_id}"
   
   
   class BookingService:
       """Owns the booking rule. Uses only the public contracts of Rooms and Notifications."""
   
       def __init__(self, rooms: list[Room], store: ReservationStore, notifications: NotificationSender,
                    slow_step_seconds: float = 0) -> None:
           self.rooms = rooms
           self.store = store
           self.notifications = notifications
           self.slow_step_seconds = slow_step_seconds
   
       def find_free_rooms(self, stay: DateRange) -> list[Room]:
           return [room for room in self.rooms if not self.store.has_overlap(room, stay)]
   
       def reserve(self, guest: Guest, room: Room, stay: DateRange) -> BookingResult:
           if not stay.is_valid():
               return BookingResult("InvalidDates")
           if self.store.has_overlap(room, stay):
               return BookingResult("Unavailable")
   
           time.sleep(self.slow_step_seconds)  # 0 normally; the race demo makes this step slow
   
           reservation = Reservation(f"R{101 + self.store.count()}", guest, room, stay)
           self.store.save(reservation)
           try:
               # Notifications gets its own message: the five fields it needs, not the Reservation.
               self.notifications.send_confirmation(
                   Confirmation(reservation.id, guest.email, room.number, stay.check_in, stay.check_out))
           except Exception:
               # R6: the booking is already saved, so a failed message must not undo or hide it.
               return BookingResult("Accepted", reservation.id, notification_failed=True)
           return BookingResult("Accepted", reservation.id)
   
       def cancel(self, reservation_id: str) -> bool:
           reservation = self.store.find(reservation_id)
           return reservation is not None and reservation.cancel()
   ```
   
   Java:
   
   ```text
   // src/main/java/hotelbooking/reservations/BookingService.java
   package hotelbooking.reservations;
   
   import java.util.List;
   import hotelbooking.notifications.Confirmation;
   import hotelbooking.notifications.NotificationSender;
   import hotelbooking.rooms.Room;
   
   // Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
   public class BookingService {
       private final List<Room> rooms;
       private final ReservationStore store;
       private final NotificationSender notifications;
       private final int slowStepMs;
   
       public BookingService(List<Room> rooms, ReservationStore store, NotificationSender notifications) {
           this(rooms, store, notifications, 0);
       }
   
       public BookingService(List<Room> rooms, ReservationStore store, NotificationSender notifications, int slowStepMs) {
           this.rooms = rooms;
           this.store = store;
           this.notifications = notifications;
           this.slowStepMs = slowStepMs;
       }
   
       public List<Room> findFreeRooms(DateRange stay) {
           return rooms.stream().filter(room -> !store.hasOverlap(room, stay)).toList();
       }
   
       public BookingResult reserve(Guest guest, Room room, DateRange stay) {
           if (!stay.isValid()) return new BookingResult("InvalidDates");
           if (store.hasOverlap(room, stay)) return new BookingResult("Unavailable");
   
           pause(); // does nothing normally; the race demo makes this step slow
   
           Reservation reservation = new Reservation("R" + (101 + store.count()), guest, room, stay);
           store.save(reservation);
           try {
               // Notifications gets its own message: the five fields it needs, not the Reservation.
               notifications.sendConfirmation(new Confirmation(reservation.id(), guest.email(), room.number(), stay.checkIn(), stay.checkOut()));
           } catch (RuntimeException failure) {
               // R6: the booking is already saved, so a failed message must not undo or hide it.
               return new BookingResult("Accepted", reservation.id(), true);
           }
           return new BookingResult("Accepted", reservation.id());
       }
   
       public boolean cancel(String reservationId) {
           Reservation reservation = store.find(reservationId);
           return reservation != null && reservation.cancel();
       }
   
       private void pause() {
           try {
               Thread.sleep(slowStepMs);
           } catch (InterruptedException interrupted) {
               Thread.currentThread().interrupt();
           }
       }
   }
   ```
   
   Go:
   
   ```text
   // reservations/booking.go
   package reservations
   
   import (
   	"fmt"
   	"time"
   
   	"hotelbooking/notifications"
   	"hotelbooking/rooms"
   )
   
   // ReservationStore is what BookingService needs from storage.
   // Today a list in memory; from P07 a database.
   type ReservationStore interface {
   	Count() int
   	HasOverlap(room rooms.Room, stay DateRange) bool
   	Save(reservation *Reservation)
   	Find(id string) *Reservation
   	CountConfirmed(room rooms.Room, stay DateRange) int
   }
   
   type BookingResult struct {
   	Outcome            string
   	ReservationID      string
   	NotificationFailed bool
   }
   
   // Printing a BookingResult uses this method, the way ToString does in C#.
   func (result BookingResult) String() string {
   	if result.ReservationID == "" {
   		return result.Outcome
   	}
   	return result.Outcome + " " + result.ReservationID
   }
   
   // BookingService owns the booking rule. It uses only the public contracts of Rooms and Notifications.
   type BookingService struct {
   	rooms         []rooms.Room
   	store         ReservationStore
   	notifications notifications.NotificationSender
   	slowStep      time.Duration
   }
   
   func NewBookingService(rooms []rooms.Room, store ReservationStore, notifications notifications.NotificationSender, slowStep time.Duration) *BookingService {
   	return &BookingService{rooms: rooms, store: store, notifications: notifications, slowStep: slowStep}
   }
   
   func (service *BookingService) FindFreeRooms(stay DateRange) []rooms.Room {
   	free := []rooms.Room{}
   	for _, room := range service.rooms {
   		if !service.store.HasOverlap(room, stay) {
   			free = append(free, room)
   		}
   	}
   	return free
   }
   
   func (service *BookingService) Reserve(guest Guest, room rooms.Room, stay DateRange) BookingResult {
   	if !stay.IsValid() {
   		return BookingResult{Outcome: "InvalidDates"}
   	}
   	if service.store.HasOverlap(room, stay) {
   		return BookingResult{Outcome: "Unavailable"}
   	}
   
   	time.Sleep(service.slowStep) // zero normally; the race demo makes this step slow
   
   	reservation := NewReservation(fmt.Sprintf("R%d", 101+service.store.Count()), guest, room, stay)
   	service.store.Save(reservation)
   	// Notifications gets its own message: the five fields it needs, not the Reservation.
   	confirmation := notifications.Confirmation{
   		ReservationID: reservation.ID, GuestEmail: guest.Email, RoomNumber: room.Number,
   		CheckIn: stay.CheckIn, CheckOut: stay.CheckOut,
   	}
   	if err := service.notifications.SendConfirmation(confirmation); err != nil {
   		// R6: the booking is already saved, so a failed message must not undo or hide it.
   		return BookingResult{Outcome: "Accepted", ReservationID: reservation.ID, NotificationFailed: true}
   	}
   	return BookingResult{Outcome: "Accepted", ReservationID: reservation.ID}
   }
   
   func (service *BookingService) Cancel(reservationID string) bool {
   	reservation := service.store.Find(reservationID)
   	return reservation != nil && reservation.Cancel()
   }
   ```
   
   JavaScript:
   
   ```text
   // src/reservations/booking.js
   import { Confirmation } from '../notifications/index.js';
   import { Reservation } from './domain.js';
   
   // A pause that lets other requests run while this one waits.
   const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));
   
   export class BookingResult {
     constructor(outcome, reservationId = null, notificationFailed = false) {
       this.outcome = outcome;
       this.reservationId = reservationId;
       this.notificationFailed = notificationFailed;
     }
   
     toString() {
       return this.reservationId === null ? this.outcome : `${this.outcome} ${this.reservationId}`;
     }
   }
   
   // Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
   export class BookingService {
     constructor(rooms, store, notifications, slowStepMs = 0) {
       this.rooms = rooms;
       this.store = store;
       this.notifications = notifications;
       this.slowStepMs = slowStepMs;
     }
   
     async findFreeRooms(stay) {
       const free = [];
       for (const room of this.rooms) {
         if (!(await this.store.hasOverlap(room, stay))) free.push(room);
       }
       return free;
     }
   
     async reserve(guest, room, stay) {
       if (!stay.isValid()) return new BookingResult('InvalidDates');
       if (await this.store.hasOverlap(room, stay)) return new BookingResult('Unavailable');
   
       await wait(this.slowStepMs); // zero normally; the race demo makes this step slow
   
       const reservation = new Reservation(`R${101 + (await this.store.count)}`, guest, room, stay);
       await this.store.save(reservation);
       try {
         // Notifications gets its own message: the five fields it needs, not the Reservation.
         this.notifications.sendConfirmation(
           new Confirmation(reservation.id, guest.email, room.number, stay.checkIn, stay.checkOut));
       } catch {
         // R6: the booking is already saved, so a failed message must not undo or hide it.
         return new BookingResult('Accepted', reservation.id, true);
       }
       return new BookingResult('Accepted', reservation.id);
     }
   
     async cancel(reservationId) {
       return (await this.store.find(reservationId))?.cancel() ?? false;
     }
   }
   ```
   
   Dart:
   
   ```text
   // lib/reservations/src/booking.dart
   import 'dart:async';
   
   import 'package:hotel_booking/notifications/notifications.dart';
   import 'package:hotel_booking/rooms/rooms.dart';
   
   import 'domain.dart';
   
   // What BookingService needs from storage: a list in memory answers at once, a database
   // answers later (a Future). FutureOr allows both, and BookingService awaits the answer.
   abstract interface class ReservationStore {
     FutureOr<int> get count;
   
     FutureOr<bool> hasOverlap(Room room, DateRange stay);
   
     FutureOr<void> save(Reservation reservation);
   
     // Returns null when no reservation has that id.
     FutureOr<Reservation?> find(String id);
   
     FutureOr<int> countConfirmed(Room room, DateRange stay);
   }
   
   class BookingResult {
     BookingResult(this.outcome, [this.reservationId, this.notificationFailed = false]);
   
     final String outcome;
     final String? reservationId;
     final bool notificationFailed;
   
     @override
     String toString() => reservationId == null ? outcome : '$outcome $reservationId';
   }
   
   // Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
   class BookingService {
     BookingService(this.rooms, this.store, this.notifications, {this.slowStep = Duration.zero});
   
     final List<Room> rooms;
     final ReservationStore store;
     final NotificationSender notifications;
     final Duration slowStep;
   
     Future<List<Room>> findFreeRooms(DateRange stay) async => [
           for (final room in rooms)
             if (!await store.hasOverlap(room, stay)) room,
         ];
   
     Future<BookingResult> reserve(Guest guest, Room room, DateRange stay) async {
       if (!stay.isValid()) return BookingResult('InvalidDates');
       if (await store.hasOverlap(room, stay)) return BookingResult('Unavailable');
   
       // A pause that lets other requests run while this one waits.
       await Future<void>.delayed(slowStep);
   
       final reservation = Reservation('R${101 + await store.count}', guest, room, stay);
       await store.save(reservation);
       try {
         // Notifications gets its own message: the five fields it needs, not the Reservation.
         notifications.sendConfirmation(
             Confirmation(reservation.id, guest.email, room.number, stay.checkIn, stay.checkOut));
       } catch (failure) {
         // R6: the booking is already saved, so a failed message must not undo or hide it.
         return BookingResult('Accepted', reservation.id, true);
       }
       return BookingResult('Accepted', reservation.id);
     }
   
     Future<bool> cancel(String reservationId) async =>
         (await store.find(reservationId))?.cancel() ?? false;
   }
   ```
6. White box “PostgresReservationStore” and arrow 5: Find reads R101’s row with one SELECT, builds Maya’s Reservation and remembers it with its status, Confirmed.

   C#:
   
   ```text
   public Reservation? Find(string id)
   {
       var row = db.Reservations.Find(id);
       if (row is null) return null;
       var reservation = new Reservation(row.Id, new Guest(row.GuestName, row.GuestEmail),
           rooms.First(room => room.Number == row.Room), new DateRange(row.CheckIn, row.CheckOut));
       if (row.Status == "Cancelled") reservation.Cancel();
       loaded.Add((row, reservation));
       return reservation;
   }
   ```
   
   Python:
   
   ```text
   def find(self, reservation_id: str) -> Reservation | None:
       row = self._session.get(ReservationRow, reservation_id)
       if row is None:
           return None
       reservation = Reservation(row.id, Guest(row.guest_name, row.guest_email), self._rooms[row.room],
                                 DateRange(row.check_in, row.check_out))
       if row.status == "Cancelled":
           reservation.cancel()
       self._loaded.append((row, reservation))
       return reservation
   ```
   
   Java:
   
   ```text
   // Returns null when no reservation has that id.
   @Override
   public Reservation find(String id) {
       ReservationRow row = db.find(ReservationRow.class, id);
       if (row == null) return null;
       Room room = rooms.stream().filter(candidate -> candidate.number().equals(row.room)).findFirst().orElseThrow();
       Reservation reservation = new Reservation(row.id, new Guest(row.guestName, row.guestEmail), room, new DateRange(row.checkIn, row.checkOut));
       if (row.status.equals("Cancelled")) reservation.cancel();
       loaded.put(row, reservation);
       return reservation;
   }
   ```
   
   Go:
   
   ```text
   // Returns nil when no reservation has that id.
   func (store *PostgresReservationStore) Find(id string) *Reservation {
   	var guest Guest
   	var roomNumber, status string
   	var stay DateRange
   	err := store.pool.QueryRow(store.ctx,
   		"SELECT guest_name, guest_email, room, check_in, check_out, status FROM reservations WHERE id = $1", id,
   	).Scan(&guest.Name, &guest.Email, &roomNumber, &stay.CheckIn, &stay.CheckOut, &status)
   	if err != nil {
   		if !errors.Is(err, pgx.ErrNoRows) {
   			store.err = cmp.Or(store.err, err)
   		}
   		return nil
   	}
   	index := slices.IndexFunc(store.rooms, func(room rooms.Room) bool { return room.Number == roomNumber })
   	reservation := NewReservation(id, guest, store.rooms[index], stay)
   	reservation.Status = ReservationStatus(status)
   	store.loaded[reservation] = reservation.Status
   	return reservation
   }
   ```
   
   JavaScript:
   
   ```text
   // Returns null when no reservation has that id.
   async find(id) {
     const { rows } = await this.#pool.query(
       'SELECT guest_name, guest_email, room, check_in::text, check_out::text, status FROM reservations WHERE id = $1', [id]);
     if (rows.length === 0) return null;
     const [row] = rows;
     const room = this.#rooms.find(candidate => candidate.number === row.room);
     const stay = new DateRange(new Date(`${row.check_in}T00:00:00Z`), new Date(`${row.check_out}T00:00:00Z`));
     const reservation = new Reservation(id, new Guest(row.guest_name, row.guest_email), room, stay);
     reservation.status = row.status;
     this.#loaded.set(reservation, row.status);
     return reservation;
   }
   ```
   
   Dart:
   
   ```text
   // Returns null when no reservation has that id.
   @override
   Future<Reservation?> find(String id) async {
     final rows = await _pool.execute(
         Sql.named('SELECT guest_name, guest_email, room, check_in, check_out, status '
             'FROM reservations WHERE id = @id'),
         parameters: {'id': id});
     if (rows.isEmpty) return null;
     final [guestName, guestEmail, roomNumber, checkIn, checkOut, status] = rows.first;
     final room = _rooms.firstWhere((room) => room.number == roomNumber);
     final reservation = Reservation(id, Guest(guestName as String, guestEmail as String), room,
         DateRange(checkIn as DateTime, checkOut as DateTime));
     if (status == 'Cancelled') reservation.cancel();
     _loaded[reservation] = reservation.status;
     return reservation;
   }
   ```
7. Back in BookingService: reservation.Cancel() sets the status to Cancelled. Nothing is written to the database yet.
8. Arrow 6 and arrow 7: the work is done, so the wiring calls SaveChanges. The store sees that R101’s status changed and sends one UPDATE. Only now is the cancel in PostgreSQL.

   C#:
   
   ```text
   // The end of the request: copy each loaded reservation's status to its row; EF Core writes what changed.
   public void SaveChanges()
   {
       foreach (var (row, reservation) in loaded) row.Status = reservation.Status.ToString();
       db.SaveChanges();
   }
   ```
   
   Python:
   
   ```text
   def save_changes(self) -> None:
       """The end of the request: copy each loaded reservation's status to its row; SQLAlchemy writes what changed."""
       for row, reservation in self._loaded:
           row.status = reservation.status.value
       self._session.commit()
   ```
   
   Java:
   
   ```text
   // The end of the request: copy each loaded reservation's status to its row; JPA writes what changed.
   public void saveChanges() {
       loaded.forEach((row, reservation) -> row.status = statusName(reservation.status()));
       db.flush();
   }
   ```
   
   Go:
   
   ```text
   // SaveChanges is the end of the request: write the status of every loaded reservation that changed.
   func (store *PostgresReservationStore) SaveChanges() error {
   	if store.err != nil {
   		return store.err
   	}
   	for reservation, statusInDatabase := range store.loaded {
   		if reservation.Status != statusInDatabase {
   			if _, err := store.pool.Exec(store.ctx, "UPDATE reservations SET status = $1 WHERE id = $2",
   				string(reservation.Status), reservation.ID); err != nil {
   				return err
   			}
   		}
   	}
   	return nil
   }
   ```
   
   JavaScript:
   
   ```text
   // The end of the request: write the status of every loaded reservation that changed.
   async saveChanges() {
     for (const [reservation, statusInDatabase] of this.#loaded) {
       if (reservation.status !== statusInDatabase) {
         await this.#pool.query('UPDATE reservations SET status = $1 WHERE id = $2', [reservation.status, reservation.id]);
       }
     }
   }
   ```
   
   Dart:
   
   ```text
   // The end of the request: write the status of every loaded reservation that changed.
   Future<void> saveChanges() async {
     for (final MapEntry(key: reservation, value: statusInDatabase) in _loaded.entries) {
       if (reservation.status != statusInDatabase) {
         await _pool.execute(Sql.named('UPDATE reservations SET status = @status WHERE id = @id'),
             parameters: {'status': statusName(reservation.status), 'id': reservation.id});
       }
     }
   }
   ```
9. Grey box “PostgreSQL”: the reservations table now says Cancelled for R101. The endpoint answers 204. Send the same DELETE again: Find returns a Cancelled reservation, Cancel answers false, and the endpoint answers 404 NotFound.

**Legend:**
- **Yellow box.** A person or a program outside our code. We use it; we do not build it. In this diagram: curl.exe in your terminal, standing in for a guest’s browser.
- **White box.** Our code that is not the booking rule: it passes requests on or stores data. In this diagram: The DELETE endpoint, the wiring’s unit of work, and the PostgreSQL store in Reservations’ internal code.
- **Green box.** The code that owns the hotel’s booking rule. In this diagram: BookingService.Cancel, the same code as in P06.
- **Grey box.** Stored data that is the source of truth: if it says R101 is Cancelled, it is. In this diagram: PostgreSQL in Docker, with the rooms and reservations tables of db/init.sql.
- **Solid line with a filled arrowhead.** A request at run time: the part at the start asks the part at the arrowhead for something. In this diagram: An HTTP request, a method call or an SQL statement.
- **Number at the start of a line’s label.** The order of the requests: 1 happens first, then 2, and so on. In this diagram: 1 · DELETE, … 5 · SELECT, then 6 · SaveChanges and 7 · UPDATE at the end of the request.

### Your turn · write the two stores

1. Create PostgresRoomCatalog from 7.1 in Rooms’ internal code (Python, Go: add it to the internal file).
2. Create PostgresReservationStore and its row class from 7.2 in Reservations’ internal code.
3. Java only: add the two small methods room() and stay() to Reservation; the store needs them in Save.
4. JavaScript and Dart only: put await before the store calls in BookingService and before the two calls in the test “cancelling frees the room again”. Dart also: in lib/rooms/rooms.dart, all() returns FutureOr<List<Room>> (import dart:async), as the room catalog tab shows.
5. Run your checks: still 15 green. The tests use the list store, and the border checks pass because the new classes are internal code.
6. Open “Explain this diagram step by step” above and find each numbered arrow in your own code.

**When you finish, you have:** two PostgreSQL stores in the modules’ internal code, BookingService’s rule unchanged, and 15 green checks.
### Explanation

The stores keep the same promise as the list store, so BookingService needs nothing new in four languages. Cancel changes a reservation in memory; the store writes that change at the end of the request: that is the unit of work.



---

## 9. Step 8 · Plug PostgreSQL in and prove it

The wiring picks the PostgreSQL stores. Then the seven requests of docs/api-check.md, and a restart: Maya’s booking is still there.





### Completed example: the wiring picks the PostgreSQL stores

The only change: the web app’s part of the wiring creates the stores over PostgreSQL and saves at the end of each request. --demo and --race still use the lists.

**The unit of work: save at the end of the request** In short: For one request the wiring creates a fresh store, runs the endpoint’s work with BookingService, and then tells the store to save what changed. That is why BookingService does not change: Cancel still only sets the status on the reservation object; the store writes that change to PostgreSQL when the request ends. In JavaScript and Dart the service awaits the store, because those database libraries always answer later.

1. DELETE /reservations/R101 arrives. The wiring creates a store for this request only.
2. Cancel asks the store for R101: SELECT … WHERE id = 'R101'. The store remembers that reservation and its status, Confirmed.
3. Cancel sets the status to Cancelled on the object, in memory.
4. The request ends: SaveChanges sees the status changed and runs UPDATE reservations SET status = 'Cancelled' WHERE id = 'R101'.

C#:

```text
// One request: a fresh store over its own DbContext; save what changed at the end.
IResult UnitOfWork(Func<BookingService, IResult> work)
{
    using var db = databases.CreateDbContext();
    var store = new PostgresReservationStore(db, rooms);
    var result = work(new BookingService(rooms, store, email));
    store.SaveChanges();
    return result;
}
```

Python:

```text
@contextmanager
def unit_of_work():
    # One request: a fresh store over its own session; save what changed at the end.
    with Session(engine) as session:
        store = PostgresReservationStore(session, rooms)
        yield BookingService(rooms, store, email)
        store.save_changes()
```

Java:

```text
// One request: a fresh store in its own transaction; save what changed at the end.
HotelApi.UnitOfWork unitOfWork = work -> transactions.execute(transaction -> {
    PostgresReservationStore store = new PostgresReservationStore(db, rooms);
    var result = work.apply(new BookingService(rooms, store, email));
    store.saveChanges();
    return result;
});
```

Go:

```text
// One request: a fresh store; save what changed at the end.
unitOfWork := func(work func(service *reservations.BookingService)) error {
	store := reservations.NewPostgresReservationStore(ctx, pool, hotelRooms)
	work(reservations.NewBookingService(hotelRooms, store, email, 0))
	return store.SaveChanges()
}
```

JavaScript:

```text
// One request: a fresh store; save what changed at the end.
const unitOfWork = async work => {
  const store = new PostgresReservationStore(pool, rooms);
  const result = await work(new BookingService(rooms, store, email));
  await store.saveChanges();
  return result;
};
```

Dart:

```text
// One request: a fresh store; save what changed at the end.
Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async {
  final store = PostgresReservationStore(pool, rooms);
  final result = await work(BookingService(rooms, store, email));
  await store.saveChanges();
  return result;
}
```

### The web app in the wiring

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

    // One request: a fresh store over its own DbContext; save what changed at the end.
    IResult UnitOfWork(Func<BookingService, IResult> work)
    {
        using var db = databases.CreateDbContext();
        var store = new PostgresReservationStore(db, rooms);
        var result = work(new BookingService(rooms, store, email));
        store.SaveChanges();
        return result;
    }

    HotelApi.Map(app, rooms, UnitOfWork);
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

    @contextmanager
    def unit_of_work():
        # One request: a fresh store over its own session; save what changed at the end.
        with Session(engine) as session:
            store = PostgresReservationStore(session, rooms)
            yield BookingService(rooms, store, email)
            store.save_changes()

    uvicorn.run(create_app(rooms, unit_of_work), host="localhost", port=5080)
```

#### Java

```java
// The wiring for the web app.
@Bean
HotelApi hotelApi(EntityManager db, TransactionTemplate transactions) {
    List<Room> rooms = new PostgresRoomCatalog(db).all();
    EmailNotificationSender email = new EmailNotificationSender();
    // One request: a fresh store in its own transaction; save what changed at the end.
    HotelApi.UnitOfWork unitOfWork = work -> transactions.execute(transaction -> {
        PostgresReservationStore store = new PostgresReservationStore(db, rooms);
        var result = work.apply(new BookingService(rooms, store, email));
        store.saveChanges();
        return result;
    });
    return new HotelApi(rooms, unitOfWork);
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

	// One request: a fresh store; save what changed at the end.
	unitOfWork := func(work func(service *reservations.BookingService)) error {
		store := reservations.NewPostgresReservationStore(ctx, pool, hotelRooms)
		work(reservations.NewBookingService(hotelRooms, store, email, 0))
		return store.SaveChanges()
	}

	fmt.Println("Listening on http://localhost:5080")
	log.Fatal(http.ListenAndServe("localhost:5080", api.New(hotelRooms, unitOfWork)))
}
```

#### JavaScript

```javascript
async function runApi() {
  // The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
  const pool = new pg.Pool({ host: 'localhost', port: Number(process.env.HOTEL_DB_PORT ?? 5432), database: 'hotel', user: 'hotel', password: 'hotel' });
  const rooms = await new PostgresRoomCatalog(pool).all();
  const email = new EmailNotificationSender();

  // One request: a fresh store; save what changed at the end.
  const unitOfWork = async work => {
    const store = new PostgresReservationStore(pool, rooms);
    const result = await work(new BookingService(rooms, store, email));
    await store.saveChanges();
    return result;
  };

  createApi(rooms, unitOfWork).listen(5080, 'localhost', () => console.log('Listening on http://localhost:5080'));
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

  // One request: a fresh store; save what changed at the end.
  Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async {
    final store = PostgresReservationStore(pool, rooms);
    final result = await work(BookingService(rooms, store, email));
    await store.saveChanges();
    return result;
  }

  await serve(createApi(rooms, unitOfWork), 'localhost', 5080);
  print('Listening on http://localhost:5080');
}
```

### Completed example: the whole wiring file at the end of P07

With the imports the PostgreSQL wiring needs at the top; the demo and the race below are unchanged.

### The wiring

#### C#

```csharp
// HotelBooking/Program.cs
// P07 · Hotel booking: the P06 app behind an HTTP API, with its reservations in PostgreSQL.
// dotnet run --project HotelBooking            -> the API on http://localhost:5080 (first: docker compose up -d)
// dotnet run --project HotelBooking -- --demo  -> the P06 demo, with the data in lists
// dotnet run --project HotelBooking -- --race  -> two guests book the same room at the same moment
using HotelBooking.Api;
using HotelBooking.Notifications.Internal;
using HotelBooking.Reservations;
using HotelBooking.Reservations.Internal;
using HotelBooking.Rooms;
using HotelBooking.Rooms.Internal;
using Microsoft.EntityFrameworkCore;

var r7 = new Room("R7", "Standard");
var r8 = new Room("R8", "Deluxe");
var maya = new Guest("Maya", "maya@example.com");
var omar = new Guest("Omar", "omar@example.com");
var nora = new Guest("Nora", "nora@example.com");

// The wiring: the only app code that names the Internal classes of the modules.
var catalog = new InMemoryRoomCatalog([r7, r8]);

if (args.Contains("--race")) RunRace();
else if (args.Contains("--demo")) RunDemo();
else RunApi();

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

    // One request: a fresh store over its own DbContext; save what changed at the end.
    IResult UnitOfWork(Func<BookingService, IResult> work)
    {
        using var db = databases.CreateDbContext();
        var store = new PostgresReservationStore(db, rooms);
        var result = work(new BookingService(rooms, store, email));
        store.SaveChanges();
        return result;
    }

    HotelApi.Map(app, rooms, UnitOfWork);
    app.Run("http://localhost:5080");
}

void RunDemo()
{
    var store = new InMemoryReservationStore();
    var email = new EmailNotificationSender();
    var service = new BookingService(catalog.All(), store, email);

    Console.WriteLine($"Free rooms 12-14 Nov: {Names(service.FindFreeRooms(Nov(12, 14)))}");
    Console.WriteLine($"Maya books R7 12-14 Nov: {service.Reserve(maya, r7, Nov(12, 14))}");
    Console.WriteLine($"Omar books R7 13-15 Nov: {service.Reserve(omar, r7, Nov(13, 15))}");
    Console.WriteLine($"Nora books R7 14-16 Nov: {service.Reserve(nora, r7, Nov(14, 16))}");
    Console.WriteLine($"Omar books R8 15-15 Nov: {service.Reserve(omar, r8, Nov(15, 15))}");
    Console.WriteLine($"Free rooms 12-14 Nov: {Names(service.FindFreeRooms(Nov(12, 14)))}");
    Console.WriteLine($"Maya cancels R101: {service.Cancel("R101")}");
    Console.WriteLine($"Free rooms 12-14 Nov: {Names(service.FindFreeRooms(Nov(12, 14)))}");
    Console.WriteLine($"Confirmation emails sent: {email.Sent}");
}

void RunRace()
{
    var store = new InMemoryReservationStore();
    // Each request now spends half a second between checking and saving.
    var service = new BookingService(catalog.All(), store, new EmailNotificationSender(), slowStepMs: 500);
    var stay = Nov(12, 14);
    BookingResult? mayaResult = null, omarResult = null;

    Console.WriteLine("Maya and Omar both book R7 for 12-14 Nov at the same moment...");
    var mayaRequest = new Thread(() => mayaResult = service.Reserve(maya, r7, stay));
    var omarRequest = new Thread(() => omarResult = service.Reserve(omar, r7, stay));
    mayaRequest.Start();
    Thread.Sleep(100); // Omar clicks a moment after Maya
    omarRequest.Start();
    mayaRequest.Join();
    omarRequest.Join();

    Console.WriteLine($"Maya: {mayaResult}");
    Console.WriteLine($"Omar: {omarResult}");
    var confirmed = store.CountConfirmed(r7, stay);
    Console.WriteLine($"Confirmed reservations for R7 on 12-14 Nov: {confirmed}");
    Console.WriteLine(confirmed > 1
        ? "Double booking! Both requests checked before either one saved."
        : "Only one guest has R7.");
}

DateRange Nov(int checkIn, int checkOut) =>
    new(new DateOnly(2026, 11, checkIn), new DateOnly(2026, 11, checkOut));

string Names(IEnumerable<Room> rooms) => string.Join(", ", rooms.Select(room => room.Number));
```

#### Python

```python
# main.py
"""P07 - Hotel booking: the P06 app behind an HTTP API, with its reservations in PostgreSQL.

python main.py          -> the API on http://localhost:5080 (first: docker compose up -d)
python main.py --demo   -> the P06 demo, with the data in lists
python main.py --race   -> two guests book the same room at the same moment
"""
from __future__ import annotations

import os
import sys
import threading
import time
from contextlib import contextmanager
from datetime import date

import uvicorn
from sqlalchemy import create_engine
from sqlalchemy.orm import Session

from hotel_booking.api import create_app
from hotel_booking.notifications.internal import EmailNotificationSender
from hotel_booking.reservations import BookingResult, BookingService, DateRange, Guest
from hotel_booking.reservations.internal import InMemoryReservationStore, PostgresReservationStore
from hotel_booking.rooms import Room
from hotel_booking.rooms.internal import InMemoryRoomCatalog, PostgresRoomCatalog

R7 = Room("R7", "Standard")
R8 = Room("R8", "Deluxe")
MAYA = Guest("Maya", "maya@example.com")
OMAR = Guest("Omar", "omar@example.com")
NORA = Guest("Nora", "nora@example.com")

# The wiring: the only app code that imports the internal modules.
CATALOG = InMemoryRoomCatalog([R7, R8])


def nov(check_in: int, check_out: int) -> DateRange:
    return DateRange(date(2026, 11, check_in), date(2026, 11, check_out))


def names(rooms: list[Room]) -> str:
    return ", ".join(room.number for room in rooms)


def run_demo() -> None:
    store = InMemoryReservationStore()
    email = EmailNotificationSender()
    service = BookingService(CATALOG.all(), store, email)

    print(f"Free rooms 12-14 Nov: {names(service.find_free_rooms(nov(12, 14)))}")
    print(f"Maya books R7 12-14 Nov: {service.reserve(MAYA, R7, nov(12, 14))}")
    print(f"Omar books R7 13-15 Nov: {service.reserve(OMAR, R7, nov(13, 15))}")
    print(f"Nora books R7 14-16 Nov: {service.reserve(NORA, R7, nov(14, 16))}")
    print(f"Omar books R8 15-15 Nov: {service.reserve(OMAR, R8, nov(15, 15))}")
    print(f"Free rooms 12-14 Nov: {names(service.find_free_rooms(nov(12, 14)))}")
    print(f"Maya cancels R101: {service.cancel('R101')}")
    print(f"Free rooms 12-14 Nov: {names(service.find_free_rooms(nov(12, 14)))}")
    print(f"Confirmation emails sent: {email.sent}")


def run_race() -> None:
    store = InMemoryReservationStore()
    # Each request now spends half a second between checking and saving.
    service = BookingService(CATALOG.all(), store, EmailNotificationSender(), slow_step_seconds=0.5)
    stay = nov(12, 14)
    results: dict[str, BookingResult] = {}

    print("Maya and Omar both book R7 for 12-14 Nov at the same moment...")
    maya_request = threading.Thread(target=lambda: results.update(Maya=service.reserve(MAYA, R7, stay)))
    omar_request = threading.Thread(target=lambda: results.update(Omar=service.reserve(OMAR, R7, stay)))
    maya_request.start()
    time.sleep(0.1)  # Omar clicks a moment after Maya
    omar_request.start()
    maya_request.join()
    omar_request.join()

    print(f"Maya: {results['Maya']}")
    print(f"Omar: {results['Omar']}")
    confirmed = store.count_confirmed(R7, stay)
    print(f"Confirmed reservations for R7 on 12-14 Nov: {confirmed}")
    print("Double booking! Both requests checked before either one saved."
          if confirmed > 1 else "Only one guest has R7.")


def run_api() -> None:
    # The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
    engine = create_engine(f"postgresql+psycopg://hotel:hotel@localhost:{os.getenv('HOTEL_DB_PORT', '5432')}/hotel")
    rooms = PostgresRoomCatalog(engine).all()
    email = EmailNotificationSender()

    @contextmanager
    def unit_of_work():
        # One request: a fresh store over its own session; save what changed at the end.
        with Session(engine) as session:
            store = PostgresReservationStore(session, rooms)
            yield BookingService(rooms, store, email)
            store.save_changes()

    uvicorn.run(create_app(rooms, unit_of_work), host="localhost", port=5080)


if __name__ == "__main__":
    if "--race" in sys.argv:
        run_race()
    elif "--demo" in sys.argv:
        run_demo()
    else:
        run_api()
```

#### Java

```java
// src/main/java/hotelbooking/Main.java
// P07 · Hotel booking: the P06 app behind an HTTP API, with its reservations in PostgreSQL.
// mvn -q spring-boot:run                              -> the API on http://localhost:5080 (first: docker compose up -d)
// java -jar target/hotel-booking-1.0.jar --demo       -> the P06 demo, with the data in lists (after mvn -q package)
// java -jar target/hotel-booking-1.0.jar --race       -> two guests book the same room at the same moment
package hotelbooking;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import hotelbooking.api.HotelApi;
import hotelbooking.notifications.internal.EmailNotificationSender;
import hotelbooking.reservations.BookingResult;
import hotelbooking.reservations.BookingService;
import hotelbooking.reservations.DateRange;
import hotelbooking.reservations.Guest;
import hotelbooking.reservations.internal.InMemoryReservationStore;
import hotelbooking.reservations.internal.PostgresReservationStore;
import hotelbooking.rooms.Room;
import hotelbooking.rooms.RoomCatalog;
import hotelbooking.rooms.internal.InMemoryRoomCatalog;
import hotelbooking.rooms.internal.PostgresRoomCatalog;
import jakarta.persistence.EntityManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionTemplate;

// Spring Boot starts the web server and connects to PostgreSQL (application.properties);
// hotelApi below is the wiring that hands it the endpoints.
@SpringBootConfiguration
@EnableAutoConfiguration
public class Main {
    static final Room R7 = new Room("R7", "Standard");
    static final Room R8 = new Room("R8", "Deluxe");
    static final Guest MAYA = new Guest("Maya", "maya@example.com");
    static final Guest OMAR = new Guest("Omar", "omar@example.com");
    static final Guest NORA = new Guest("Nora", "nora@example.com");

    // The wiring: the only app class that names the internal classes of the modules.
    static final RoomCatalog CATALOG = new InMemoryRoomCatalog(List.of(R7, R8));

    public static void main(String[] args) throws InterruptedException {
        if (List.of(args).contains("--race")) runRace();
        else if (List.of(args).contains("--demo")) runDemo();
        else SpringApplication.run(Main.class, args);
    }

    // The wiring for the web app.
    @Bean
    HotelApi hotelApi(EntityManager db, TransactionTemplate transactions) {
        List<Room> rooms = new PostgresRoomCatalog(db).all();
        EmailNotificationSender email = new EmailNotificationSender();
        // One request: a fresh store in its own transaction; save what changed at the end.
        HotelApi.UnitOfWork unitOfWork = work -> transactions.execute(transaction -> {
            PostgresReservationStore store = new PostgresReservationStore(db, rooms);
            var result = work.apply(new BookingService(rooms, store, email));
            store.saveChanges();
            return result;
        });
        return new HotelApi(rooms, unitOfWork);
    }

    static void runDemo() {
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

    static void runRace() throws InterruptedException {
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

    static DateRange nov(int checkIn, int checkOut) {
        return new DateRange(LocalDate.of(2026, 11, checkIn), LocalDate.of(2026, 11, checkOut));
    }

    static String names(List<Room> rooms) {
        return rooms.stream().map(Room::number).collect(Collectors.joining(", "));
    }

    // Java prints booleans as true/false; the course output says True, so the demo spells it out.
    static String boolText(boolean value) {
        return value ? "True" : "False";
    }
}
```

#### Go

```go
// main.go
// P07 · Hotel booking: the P06 app behind an HTTP API, with its reservations in PostgreSQL.
// go run .          -> the API on http://localhost:5080 (first: docker compose up -d)
// go run . --demo   -> the P06 demo, with the data in lists
// go run . --race   -> two guests book the same room at the same moment
package main

import (
	"cmp"
	"context"
	"fmt"
	"log"
	"net/http"
	"os"
	"slices"
	"strings"
	"sync"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"

	"hotelbooking/api"
	"hotelbooking/notifications"
	"hotelbooking/reservations"
	"hotelbooking/rooms"
)

var (
	r7   = rooms.Room{Number: "R7", Type: "Standard"}
	r8   = rooms.Room{Number: "R8", Type: "Deluxe"}
	maya = reservations.Guest{Name: "Maya", Email: "maya@example.com"}
	omar = reservations.Guest{Name: "Omar", Email: "omar@example.com"}
	nora = reservations.Guest{Name: "Nora", Email: "nora@example.com"}

	// The wiring: main.go is the only app code that uses the internal.go types of the modules.
	catalog = rooms.NewInMemoryRoomCatalog(r7, r8)
)

func main() {
	switch {
	case slices.Contains(os.Args[1:], "--race"):
		runRace()
	case slices.Contains(os.Args[1:], "--demo"):
		runDemo()
	default:
		runAPI()
	}
}

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

	// One request: a fresh store; save what changed at the end.
	unitOfWork := func(work func(service *reservations.BookingService)) error {
		store := reservations.NewPostgresReservationStore(ctx, pool, hotelRooms)
		work(reservations.NewBookingService(hotelRooms, store, email, 0))
		return store.SaveChanges()
	}

	fmt.Println("Listening on http://localhost:5080")
	log.Fatal(http.ListenAndServe("localhost:5080", api.New(hotelRooms, unitOfWork)))
}

func runDemo() {
	store := &reservations.InMemoryReservationStore{}
	email := &notifications.EmailNotificationSender{}
	service := reservations.NewBookingService(catalog.All(), store, email, 0)

	fmt.Printf("Free rooms 12-14 Nov: %s\n", names(service.FindFreeRooms(nov(12, 14))))
	fmt.Printf("Maya books R7 12-14 Nov: %s\n", service.Reserve(maya, r7, nov(12, 14)))
	fmt.Printf("Omar books R7 13-15 Nov: %s\n", service.Reserve(omar, r7, nov(13, 15)))
	fmt.Printf("Nora books R7 14-16 Nov: %s\n", service.Reserve(nora, r7, nov(14, 16)))
	fmt.Printf("Omar books R8 15-15 Nov: %s\n", service.Reserve(omar, r8, nov(15, 15)))
	fmt.Printf("Free rooms 12-14 Nov: %s\n", names(service.FindFreeRooms(nov(12, 14))))
	fmt.Printf("Maya cancels R101: %s\n", boolText(service.Cancel("R101")))
	fmt.Printf("Free rooms 12-14 Nov: %s\n", names(service.FindFreeRooms(nov(12, 14))))
	fmt.Printf("Confirmation emails sent: %d\n", email.Sent)
}

func runRace() {
	store := &reservations.InMemoryReservationStore{}
	// Each request now spends half a second between checking and saving.
	service := reservations.NewBookingService(catalog.All(), store, &notifications.EmailNotificationSender{}, 500*time.Millisecond)
	stay := nov(12, 14)
	var mayaResult, omarResult reservations.BookingResult
	var requests sync.WaitGroup

	fmt.Println("Maya and Omar both book R7 for 12-14 Nov at the same moment...")
	requests.Add(2)
	go func() {
		defer requests.Done()
		mayaResult = service.Reserve(maya, r7, stay)
	}()
	time.Sleep(100 * time.Millisecond) // Omar clicks a moment after Maya
	go func() {
		defer requests.Done()
		omarResult = service.Reserve(omar, r7, stay)
	}()
	requests.Wait()

	fmt.Printf("Maya: %s\n", mayaResult)
	fmt.Printf("Omar: %s\n", omarResult)
	confirmed := store.CountConfirmed(r7, stay)
	fmt.Printf("Confirmed reservations for R7 on 12-14 Nov: %d\n", confirmed)
	if confirmed > 1 {
		fmt.Println("Double booking! Both requests checked before either one saved.")
	} else {
		fmt.Println("Only one guest has R7.")
	}
}

func nov(checkIn, checkOut int) reservations.DateRange {
	return reservations.DateRange{
		CheckIn:  time.Date(2026, time.November, checkIn, 0, 0, 0, 0, time.UTC),
		CheckOut: time.Date(2026, time.November, checkOut, 0, 0, 0, 0, time.UTC),
	}
}

func names(free []rooms.Room) string {
	numbers := make([]string, len(free))
	for index, room := range free {
		numbers[index] = room.Number
	}
	return strings.Join(numbers, ", ")
}

// Go prints booleans as true/false; the course output says True, so the demo spells it out.
func boolText(value bool) string {
	if value {
		return "True"
	}
	return "False"
}
```

#### JavaScript

```javascript
// main.js
// P07 · Hotel booking: the P06 app behind an HTTP API, with its reservations in PostgreSQL.
// node main.js          -> the API on http://localhost:5080 (first: docker compose up -d)
// node main.js --demo   -> the P06 demo, with the data in lists
// node main.js --race   -> two guests book the same room at the same moment
import pg from 'pg';
import { createApi } from './src/api.js';
import { EmailNotificationSender } from './src/notifications/internal/email.js';
import { BookingService, DateRange, Guest } from './src/reservations/index.js';
import { PostgresReservationStore } from './src/reservations/internal/postgres-store.js';
import { InMemoryReservationStore } from './src/reservations/internal/store.js';
import { Room } from './src/rooms/index.js';
import { InMemoryRoomCatalog } from './src/rooms/internal/catalog.js';
import { PostgresRoomCatalog } from './src/rooms/internal/postgres-catalog.js';

const r7 = new Room('R7', 'Standard');
const r8 = new Room('R8', 'Deluxe');
const maya = new Guest('Maya', 'maya@example.com');
const omar = new Guest('Omar', 'omar@example.com');
const nora = new Guest('Nora', 'nora@example.com');

// The wiring: the only app code that imports the internal files of the modules.
const catalog = new InMemoryRoomCatalog([r7, r8]);

const nov = (checkIn, checkOut) =>
  new DateRange(new Date(Date.UTC(2026, 10, checkIn)), new Date(Date.UTC(2026, 10, checkOut)));

const names = rooms => rooms.map(room => room.number).join(', ');

const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

// JavaScript prints booleans as true/false; the course output says True, so the demo spells it out.
const boolText = value => (value ? 'True' : 'False');

async function runDemo() {
  const store = new InMemoryReservationStore();
  const email = new EmailNotificationSender();
  const service = new BookingService(catalog.all(), store, email);

  console.log(`Free rooms 12-14 Nov: ${names(await service.findFreeRooms(nov(12, 14)))}`);
  console.log(`Maya books R7 12-14 Nov: ${await service.reserve(maya, r7, nov(12, 14))}`);
  console.log(`Omar books R7 13-15 Nov: ${await service.reserve(omar, r7, nov(13, 15))}`);
  console.log(`Nora books R7 14-16 Nov: ${await service.reserve(nora, r7, nov(14, 16))}`);
  console.log(`Omar books R8 15-15 Nov: ${await service.reserve(omar, r8, nov(15, 15))}`);
  console.log(`Free rooms 12-14 Nov: ${names(await service.findFreeRooms(nov(12, 14)))}`);
  console.log(`Maya cancels R101: ${boolText(await service.cancel('R101'))}`);
  console.log(`Free rooms 12-14 Nov: ${names(await service.findFreeRooms(nov(12, 14)))}`);
  console.log(`Confirmation emails sent: ${email.sent}`);
}

async function runRace() {
  const store = new InMemoryReservationStore();
  // Each request now spends half a second between checking and saving.
  const service = new BookingService(catalog.all(), store, new EmailNotificationSender(), 500);
  const stay = nov(12, 14);

  console.log('Maya and Omar both book R7 for 12-14 Nov at the same moment...');
  const mayaRequest = service.reserve(maya, r7, stay);
  await wait(100); // Omar clicks a moment after Maya
  const omarRequest = service.reserve(omar, r7, stay);
  const [mayaResult, omarResult] = await Promise.all([mayaRequest, omarRequest]);

  console.log(`Maya: ${mayaResult}`);
  console.log(`Omar: ${omarResult}`);
  const confirmed = store.countConfirmed(r7, stay);
  console.log(`Confirmed reservations for R7 on 12-14 Nov: ${confirmed}`);
  console.log(confirmed > 1
    ? 'Double booking! Both requests checked before either one saved.'
    : 'Only one guest has R7.');
}

async function runApi() {
  // The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
  const pool = new pg.Pool({ host: 'localhost', port: Number(process.env.HOTEL_DB_PORT ?? 5432), database: 'hotel', user: 'hotel', password: 'hotel' });
  const rooms = await new PostgresRoomCatalog(pool).all();
  const email = new EmailNotificationSender();

  // One request: a fresh store; save what changed at the end.
  const unitOfWork = async work => {
    const store = new PostgresReservationStore(pool, rooms);
    const result = await work(new BookingService(rooms, store, email));
    await store.saveChanges();
    return result;
  };

  createApi(rooms, unitOfWork).listen(5080, 'localhost', () => console.log('Listening on http://localhost:5080'));
}

if (process.argv.includes('--race')) runRace();
else if (process.argv.includes('--demo')) runDemo();
else runApi();
```

#### Dart

```dart
// bin/main.dart
// P07 · Hotel booking: the P06 app behind an HTTP API, with its reservations in PostgreSQL.
// dart run bin/main.dart          -> the API on http://localhost:5080 (first: docker compose up -d)
// dart run bin/main.dart --demo   -> the P06 demo, with the data in lists
// dart run bin/main.dart --race   -> two guests book the same room at the same moment
import 'dart:async';
import 'dart:io';

import 'package:hotel_booking/api.dart';
import 'package:hotel_booking/notifications/src/email.dart';
import 'package:hotel_booking/reservations/reservations.dart';
import 'package:hotel_booking/reservations/src/postgres_store.dart';
import 'package:hotel_booking/reservations/src/store.dart';
import 'package:hotel_booking/rooms/rooms.dart';
import 'package:hotel_booking/rooms/src/catalog.dart';
import 'package:hotel_booking/rooms/src/postgres_catalog.dart';
import 'package:postgres/postgres.dart' show Endpoint, Pool, PoolSettings, SslMode;
import 'package:shelf/shelf_io.dart';

final r7 = Room('R7', 'Standard');
final r8 = Room('R8', 'Deluxe');
final maya = Guest('Maya', 'maya@example.com');
final omar = Guest('Omar', 'omar@example.com');
final nora = Guest('Nora', 'nora@example.com');

// The wiring: the only app code that imports the src/ files of the modules.
final catalog = InMemoryRoomCatalog([r7, r8]);

DateRange nov(int checkIn, int checkOut) =>
    DateRange(DateTime.utc(2026, 11, checkIn), DateTime.utc(2026, 11, checkOut));

String names(List<Room> rooms) => rooms.map((room) => room.number).join(', ');

// Dart prints booleans as true/false; the course output says True, so the demo spells it out.
String boolText(bool value) => value ? 'True' : 'False';

Future<void> main(List<String> args) async {
  if (args.contains('--race')) {
    await runRace();
  } else if (args.contains('--demo')) {
    await runDemo();
  } else {
    await runApi();
  }
}

Future<void> runApi() async {
  // The wiring for the web app: PostgreSQL from docker-compose.yml (HOTEL_DB_PORT only if 5432 is taken).
  final port = int.parse(Platform.environment['HOTEL_DB_PORT'] ?? '5432');
  final pool = Pool.withEndpoints([
    Endpoint(host: 'localhost', port: port, database: 'hotel', username: 'hotel', password: 'hotel')
  ], settings: PoolSettings(sslMode: SslMode.disable));
  final rooms = await PostgresRoomCatalog(pool).all();
  final email = EmailNotificationSender();

  // One request: a fresh store; save what changed at the end.
  Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async {
    final store = PostgresReservationStore(pool, rooms);
    final result = await work(BookingService(rooms, store, email));
    await store.saveChanges();
    return result;
  }

  await serve(createApi(rooms, unitOfWork), 'localhost', 5080);
  print('Listening on http://localhost:5080');
}

Future<void> runDemo() async {
  final store = InMemoryReservationStore();
  final email = EmailNotificationSender();
  final service = BookingService(catalog.all(), store, email);

  print('Free rooms 12-14 Nov: ${names(await service.findFreeRooms(nov(12, 14)))}');
  print('Maya books R7 12-14 Nov: ${await service.reserve(maya, r7, nov(12, 14))}');
  print('Omar books R7 13-15 Nov: ${await service.reserve(omar, r7, nov(13, 15))}');
  print('Nora books R7 14-16 Nov: ${await service.reserve(nora, r7, nov(14, 16))}');
  print('Omar books R8 15-15 Nov: ${await service.reserve(omar, r8, nov(15, 15))}');
  print('Free rooms 12-14 Nov: ${names(await service.findFreeRooms(nov(12, 14)))}');
  print('Maya cancels R101: ${boolText(await service.cancel('R101'))}');
  print('Free rooms 12-14 Nov: ${names(await service.findFreeRooms(nov(12, 14)))}');
  print('Confirmation emails sent: ${email.sent}');
}

Future<void> runRace() async {
  final store = InMemoryReservationStore();
  // Each request now spends half a second between checking and saving.
  final service = BookingService(
    catalog.all(),
    store,
    EmailNotificationSender(),
    slowStep: const Duration(milliseconds: 500),
  );
  final stay = nov(12, 14);

  print('Maya and Omar both book R7 for 12-14 Nov at the same moment...');
  final mayaRequest = service.reserve(maya, r7, stay);
  // Omar clicks a moment after Maya
  await Future<void>.delayed(const Duration(milliseconds: 100));
  final omarRequest = service.reserve(omar, r7, stay);
  final results = await Future.wait([mayaRequest, omarRequest]);

  print('Maya: ${results[0]}');
  print('Omar: ${results[1]}');
  final confirmed = store.countConfirmed(r7, stay);
  print('Confirmed reservations for R7 on 12-14 Nov: $confirmed');
  print(
    confirmed > 1
        ? 'Double booking! Both requests checked before either one saved.'
        : 'Only one guest has R7.',
  );
}
```

### Completed example: docs/api-check.md

**Reading curl.exe’s answer** In short: curl.exe sends one HTTP request; -s hides the progress bar and -w prints the status code after the body. curl.exe is built into Windows 10 and 11 and works the same in PowerShell, cmd and Git Bash. On macOS and Linux type curl.

1. The first line curl.exe prints is the body, for example {"id":"R101",…}.
2. The second line is the status code, for example 201.
3. A DELETE that works has no body: you see an empty line and 204.

```text
curl.exe -s -w "\n%{http_code}\n" "http://localhost:5080/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14"
curl.exe -s -w "\n%{http_code}\n" -X POST -H "Content-Type: application/json" --data "@docs/requests/maya-r7.json" http://localhost:5080/reservations
curl.exe -s -w "\n%{http_code}\n" -X DELETE http://localhost:5080/reservations/R101
```

### docs/api-check.md

```text
# API check: seven requests against PostgreSQL

Start from an empty database, then start the app in a second terminal:

```text
docker compose down -v
docker compose up -d
```

Run each request from the hotel-booking folder. curl.exe prints the body, then the status code.
Write ✓ after a request when your answer is exactly the expected one.

## 1. Search 12–14 Nov: R7 and R8 are free

```text
curl.exe -s -w "\n%{http_code}\n" "http://localhost:5080/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14"
```

Expected:

```text
{"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}
200
```

## 2. Maya books R7 for 12–14 Nov

```text
curl.exe -s -w "\n%{http_code}\n" -X POST -H "Content-Type: application/json" --data "@docs/requests/maya-r7.json" http://localhost:5080/reservations
```

Expected:

```text
{"id":"R101","status":"Confirmed","notificationFailed":false}
201
```

## 3. Omar asks for R7 for 13–15 Nov: the night of the 13th is Maya’s

```text
curl.exe -s -w "\n%{http_code}\n" -X POST -H "Content-Type: application/json" --data "@docs/requests/omar-r7.json" http://localhost:5080/reservations
```

Expected:

```text
{"title":"Unavailable","status":409,"detail":"The room is booked for at least one night of this stay."}
409
```

## 4. Omar asks for R8 for 15–15 Nov: no night at all

```text
curl.exe -s -w "\n%{http_code}\n" -X POST -H "Content-Type: application/json" --data "@docs/requests/omar-r8.json" http://localhost:5080/reservations
```

Expected:

```text
{"title":"InvalidDates","status":400,"detail":"checkOut must be after checkIn."}
400
```

## 5. Search 12–14 Nov again: only R8 is free

```text
curl.exe -s -w "\n%{http_code}\n" "http://localhost:5080/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14"
```

Expected:

```text
{"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R8","type":"Deluxe"}],"next":null}
200
```

## 6. Cancel R101

```text
curl.exe -s -w "\n%{http_code}\n" -X DELETE http://localhost:5080/reservations/R101
```

Expected:

```text
204
```

## 7. Cancel R101 a second time

```text
curl.exe -s -w "\n%{http_code}\n" -X DELETE http://localhost:5080/reservations/R101
```

Expected:

```text
{"title":"NotFound","status":404,"detail":"There is no confirmed reservation R101."}
404
```

## Restart check: the booking survives

## 8. Maya books R7 for 12–14 Nov again

```text
curl.exe -s -w "\n%{http_code}\n" -X POST -H "Content-Type: application/json" --data "@docs/requests/maya-r7.json" http://localhost:5080/reservations
```

Expected:

```text
{"id":"R102","status":"Confirmed","notificationFailed":false}
201
```

## 9. Stop the app (Ctrl+C in its terminal) and start it again

## 10. After the restart, search 12–14 Nov: R7 is still booked

```text
curl.exe -s -w "\n%{http_code}\n" "http://localhost:5080/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14"
```

Expected:

```text
{"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R8","type":"Deluxe"}],"next":null}
200
```

R7 is still booked after the restart: the reservation lives in PostgreSQL, not in the app's memory.

```

### The seven answers, in every language

### Status and body of each answer

```text
GET /rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14
200 {"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R7","type":"Standard"},{"number":"R8","type":"Deluxe"}],"next":null}

POST /reservations   {"guestName":"Maya","guestEmail":"maya@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}
201 {"id":"R101","status":"Confirmed","notificationFailed":false}

POST /reservations   {"guestName":"Omar","guestEmail":"omar@example.com","room":"R7","checkIn":"2026-11-13","checkOut":"2026-11-15"}
409 {"title":"Unavailable","status":409,"detail":"The room is booked for at least one night of this stay."}

POST /reservations   {"guestName":"Omar","guestEmail":"omar@example.com","room":"R8","checkIn":"2026-11-15","checkOut":"2026-11-15"}
400 {"title":"InvalidDates","status":400,"detail":"checkOut must be after checkIn."}

GET /rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14
200 {"checkIn":"2026-11-12","checkOut":"2026-11-14","rooms":[{"number":"R8","type":"Deluxe"}],"next":null}

DELETE /reservations/R101
204

DELETE /reservations/R101
404 {"title":"NotFound","status":404,"detail":"There is no confirmed reservation R101."}
```

### After the restart check: the reservations table

**Why the booking survives a restart** In short: The app keeps nothing between requests; every reservation is a row in PostgreSQL, which keeps running in Docker while the app stops and starts. Before Step 8 the same test loses the booking: the list lived inside the app and disappeared with it. That is L04’s “stateless instance”.

1. Maya books R7 again: 201 R102.
2. Ctrl+C stops the app; start it again with the same command.
3. Search 12–14 November: only R8 is free, because R102 is still a Confirmed row in PostgreSQL.

### Terminal

```text
docker compose exec db psql -U hotel -d hotel -c "SELECT id, guest_name, room, check_in, check_out, status FROM reservations ORDER BY id;"

  id  | guest_name | room |  check_in  | check_out  |  status
------+------------+------+------------+------------+-----------
 R101 | Maya       | R7   | 2026-11-12 | 2026-11-14 | Cancelled
 R102 | Maya       | R7   | 2026-11-12 | 2026-11-14 | Confirmed
(2 rows)
```

### Your turn · plug it in and prove it

1. Replace the web app part of your wiring with the completed example, then add the import or using lines your editor marks red (compare with the whole wiring file).
2. Create docs/api-check.md and copy the completed example.
3. Start from an empty database: docker compose down -v, then docker compose up -d. Start the app.
4. Run the seven requests one by one and write ✓ after each answer that matches exactly.
5. Restart check: book Maya again (201 R102), stop the app with Ctrl+C, start it again, search: only R8 is free.
6. Run the psql query above: R101 Cancelled, R102 Confirmed.

**When you finish, you have:** the app on PostgreSQL, docs/api-check.md with seven ✓ and the restart check, and the two rows in the reservations table.
### Explanation

Before Step 8 the same restart lost Maya’s booking: the list lived inside the app. Now every reservation is a row in PostgreSQL, so any copy of the app, started at any time, sees it.



---

## 10. Check your project and see what comes next

Run everything once more, check your folder against the list, and see the problem P08 solves.





### Run everything

### Commands

#### C#

```text
docker compose up -d
dotnet test HotelBooking.Tests
dotnet run --project HotelBooking -- --demo
dotnet run --project HotelBooking -- --race
dotnet run --project HotelBooking            # the API on http://localhost:5080
```

#### Python

```text
docker compose up -d
python -m unittest -v
lint-imports
python main.py --demo
python main.py --race
python main.py            # the API on http://localhost:5080
```

#### Java

```text
docker compose up -d
mvn -q test
mvn -q package
java -jar target/hotel-booking-1.0.jar --demo
java -jar target/hotel-booking-1.0.jar --race
mvn -q spring-boot:run            # the API on http://localhost:5080
```

#### Go

```text
docker compose up -d
go test -v ./...
go run . --demo
go run . --race
go run .            # the API on http://localhost:5080
```

#### JavaScript

```text
docker compose up -d
node --test
node main.js --demo
node main.js --race
node main.js            # the API on http://localhost:5080
```

#### Dart

```text
docker compose up -d
dart test
dart run bin/main.dart --demo
dart run bin/main.dart --race
dart run bin/main.dart            # the API on http://localhost:5080
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

### Your P07 checklist

| File or page | It is finished when |
| --- | --- |
| docs/capacity.md | Five assumptions, five results, two things to measure in P08. |
| docker-compose.yml, db/init.sql | docker compose up -d starts PostgreSQL with R7 and R8. |
| docs/openapi.yaml | Three operations, five error titles; the Swagger editor shows no errors. |
| The Api file | Three endpoints outside the four modules; GET /openapi.yaml serves the contract. |
| Checks | 15 green: 8 behaviour + 3 architecture + 4 API tests (Python: 12 tests and 3 contracts). |
| The PostgreSQL stores | In Rooms’ and Reservations’ internal code; only the wiring names them. |
| docs/api-check.md | Seven ✓, and the booking survived the restart. |
| docs/design-notes.md | P07 sections: API decisions, API tests, After. |

### Where the hotel app goes next

| Lesson | What changes in the hotel app |
| --- | --- |
| P08 | Every search asks PostgreSQL once per room. You measure 1,000 searches, add a Redis cache for the search, measure again and see the stale-cache risk. |
| L05 | Transactions, locks and isolation: why two requests at the same moment can still both book R7. |
| P09 | You reproduce the double booking with two HTTP calls and fix it in PostgreSQL. |

### The open question P07 leaves for P08

At the peak, 3.33 searches a second ask PostgreSQL 40 questions each: 133 queries a second for answers that hardly change. P08 measures that and adds a cache.

### Completed example: the finished P07 folder

### hotel-booking folder

#### C#

```text
hotel-booking/   end of P07
├── db/
│   └── init.sql   NEW: the rooms and reservations tables
├── docs/
│   ├── requests/   NEW: the request bodies curl sends
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── api-check.md   NEW: seven requests and their answers
│   ├── capacity.md   NEW: the estimate
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   NEW: the API contract
│   ├── quality-scenarios.md
│   └── requirements.md
├── HotelBooking/
│   ├── Api/
│   │   └── HotelApi.cs   NEW
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
│   │   │   └── PostgresReservationStore.cs   NEW
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
│   │   │   └── PostgresRoomCatalog.cs   NEW
│   │   ├── IRoomCatalog.cs
│   │   └── Room.cs
│   ├── HotelBooking.csproj   CHANGED: Web SDK + EF Core
│   └── Program.cs   CHANGED: web app; the wiring picks the PostgreSQL stores
├── HotelBooking.Tests/
│   ├── ApiTests.cs   NEW
│   ├── ArchitectureTests.cs
│   ├── BookingServiceTests.cs
│   └── HotelBooking.Tests.csproj   CHANGED: + the ASP.NET Core test server
└── docker-compose.yml   NEW: PostgreSQL in Docker
```

#### Python

```text
hotel-booking/   end of P07
├── db/
│   └── init.sql   NEW: the rooms and reservations tables
├── docs/
│   ├── requests/   NEW: the request bodies curl sends
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── api-check.md   NEW: seven requests and their answers
│   ├── capacity.md   NEW: the estimate
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   NEW: the API contract
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
│   │   └── internal.py   CHANGED: + PostgresReservationStore
│   ├── rooms/
│   │   ├── __init__.py
│   │   ├── catalog.py
│   │   └── internal.py   CHANGED: + PostgresRoomCatalog
│   ├── __init__.py
│   └── api.py   NEW
├── docker-compose.yml   NEW: PostgreSQL in Docker
├── main.py   CHANGED: web app; the wiring picks the PostgreSQL stores
├── pyproject.toml
├── requirements.txt   NEW
├── test_api.py   NEW
└── test_booking_service.py
```

#### Java

```text
hotel-booking/   end of P07
├── db/
│   └── init.sql   NEW: the rooms and reservations tables
├── docs/
│   ├── requests/   NEW: the request bodies curl sends
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── api-check.md   NEW: seven requests and their answers
│   ├── capacity.md   NEW: the estimate
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   NEW: the API contract
│   ├── quality-scenarios.md
│   └── requirements.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── hotelbooking/
│   │   │       ├── api/
│   │   │       │   └── HotelApi.java   NEW
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
│   │   │       │   │   └── PostgresReservationStore.java   NEW
│   │   │       │   ├── BookingResult.java
│   │   │       │   ├── BookingService.java
│   │   │       │   ├── DateRange.java
│   │   │       │   ├── Guest.java
│   │   │       │   ├── Reservation.java   CHANGED: + room() and stay(), for the store
│   │   │       │   ├── ReservationStatus.java
│   │   │       │   └── ReservationStore.java
│   │   │       ├── rooms/
│   │   │       │   ├── internal/
│   │   │       │   │   ├── InMemoryRoomCatalog.java
│   │   │       │   │   └── PostgresRoomCatalog.java   NEW
│   │   │       │   ├── Room.java
│   │   │       │   └── RoomCatalog.java
│   │   │       └── Main.java   CHANGED: web app; the wiring picks the PostgreSQL stores
│   │   └── resources/
│   │       └── application.properties   NEW
│   └── test/
│       └── java/
│           └── hotelbooking/
│               ├── ApiTests.java   NEW
│               ├── ArchitectureTests.java
│               └── BookingServiceTests.java
├── docker-compose.yml   NEW: PostgreSQL in Docker
└── pom.xml   CHANGED: Spring Boot parent and starters
```

#### Go

```text
hotel-booking/   end of P07
├── api/
│   ├── api_test.go   NEW
│   └── api.go   NEW
├── db/
│   └── init.sql   NEW: the rooms and reservations tables
├── docs/
│   ├── requests/   NEW: the request bodies curl sends
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── api-check.md   NEW: seven requests and their answers
│   ├── capacity.md   NEW: the estimate
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   NEW: the API contract
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
│   └── internal.go   CHANGED: + PostgresReservationStore
├── rooms/
│   ├── internal.go   CHANGED: + PostgresRoomCatalog
│   └── rooms.go
├── architecture_test.go
├── docker-compose.yml   NEW: PostgreSQL in Docker
├── go.mod   CHANGED: + pgx
├── go.sum   NEW
└── main.go   CHANGED: web app; the wiring picks the PostgreSQL stores
```

#### JavaScript

```text
hotel-booking/   end of P07
├── db/
│   └── init.sql   NEW: the rooms and reservations tables
├── docs/
│   ├── requests/   NEW: the request bodies curl sends
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── api-check.md   NEW: seven requests and their answers
│   ├── capacity.md   NEW: the estimate
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   NEW: the API contract
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
│   │   │   ├── postgres-store.js   NEW
│   │   │   └── store.js   CHANGED: one comment
│   │   ├── booking.js   CHANGED: awaits the store
│   │   ├── domain.js
│   │   └── index.js
│   ├── rooms/
│   │   ├── internal/
│   │   │   ├── catalog.js
│   │   │   └── postgres-catalog.js   NEW
│   │   └── index.js
│   └── api.js   NEW
├── test/
│   ├── api.test.js   NEW
│   ├── architecture.test.js
│   └── booking.test.js   CHANGED: + await in one test
├── docker-compose.yml   NEW: PostgreSQL in Docker
├── main.js   CHANGED: web app; the wiring picks the PostgreSQL stores
├── package-lock.json   NEW: written by npm install
└── package.json   CHANGED: + express, pg
```

#### Dart

```text
hotel-booking/   end of P07
├── bin/
│   └── main.dart   CHANGED: web app; the wiring picks the PostgreSQL stores
├── db/
│   └── init.sql   NEW: the rooms and reservations tables
├── docs/
│   ├── requests/   NEW: the request bodies curl sends
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── api-check.md   NEW: seven requests and their answers
│   ├── capacity.md   NEW: the estimate
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml   NEW: the API contract
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
│   │   │   ├── booking.dart   CHANGED: awaits the store
│   │   │   ├── domain.dart
│   │   │   ├── postgres_store.dart   NEW
│   │   │   └── store.dart   CHANGED: one comment
│   │   └── reservations.dart
│   ├── rooms/
│   │   ├── src/
│   │   │   ├── catalog.dart
│   │   │   └── postgres_catalog.dart   NEW
│   │   └── rooms.dart   CHANGED: all() may answer later
│   └── api.dart   NEW
├── test/
│   ├── api_test.dart   NEW
│   ├── architecture_test.dart
│   └── booking_service_test.dart   CHANGED: + await in one test
├── analysis_options.yaml
├── docker-compose.yml   NEW: PostgreSQL in Docker
└── pubspec.yaml   CHANGED: + shelf, shelf_router, postgres
```

### Your turn · check and hand over

1. Run the commands above: 15 green, the demo, the race, then the API.
2. Go through the checklist and fix anything that is missing.
3. In docs/design-notes.md add ## After with three lines: the checks, the seven answers, the restart check.
4. Stop PostgreSQL with docker compose down (without -v: P08 starts from this data).
5. Copy the hotel-booking folder to your partner’s laptop, so both of you start P08 from it.

**When you finish, you have:** the complete P07 folder on both laptops: estimate, contract, web app, PostgreSQL stores and the checked requests.
### Explanation

The app now has an address, a written contract and a database. Its search is the next bottleneck, exactly as the estimate predicted: that is P08.
