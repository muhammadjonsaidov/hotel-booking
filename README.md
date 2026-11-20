# Hotel Booking

A Java command-line hotel booking app organised as a modular monolith. It searches available rooms, accepts reservations, rejects overlapping stays, and cancels bookings while keeping their history.

## Run locally

Use JDK 25, the Java version used to verify this project. The Gradle wrapper is included; the first build needs internet access to download Gradle and test dependencies.

```sh
./gradlew test
docker compose up -d            # PostgreSQL 18 (rooms and reservations) and Redis 8 (search cache)
./gradlew bootRun               # the web app on http://localhost:5080 (contract: /openapi.yaml)
./gradlew run --args="--demo"   # the console demo
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

The demo prints:

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

## Booking behavior

- Stays include the check-in date and exclude the check-out date: 12–14 November occupies the nights of the 12th and 13th, so another guest can arrive on the 14th.
- Check-out must be after check-in; invalid dates return `InvalidDates`.
- An overlapping Confirmed reservation for the same room returns `Unavailable` when requests run sequentially.
- Accepted bookings return a reservation id, such as `Accepted R101`, and can be looked up by id in the store.
- Cancellation keeps the reservation as Cancelled and frees its nights.
- Notification failure keeps the booking saved and Accepted, with `notificationFailed` set to `true`.

The web app keeps rooms and reservations in PostgreSQL, so bookings survive a restart; the console demo and race still use in-memory lists that are lost when the process exits. Email and SMS senders are simulated counters. The payment simulation returns `PAID` for positive amounts and `DECLINED` for zero or negative amounts; booking does not call it yet.

## Demonstrate the booking race

```sh
./gradlew run --args="--race"
```

The console race pauses between checking availability and saving, so Maya and Omar both see R7 as free. The in-memory store checks again and saves under one lock, so only one of them gets it:

```text
Maya and Omar both book R7 for 12-14 Nov at the same moment...
Maya: Accepted R101
Omar: Unavailable
Confirmed reservations for R7 on 12-14 Nov: 1
Only one guest has R7.
```

The web app is protected by PostgreSQL itself, so the rule also holds across several instances. The exclusion constraint `reservations_no_overlap` in `db/init.sql` refuses a second Confirmed reservation for the same room and night, whatever code sends it. Reproduce the race over HTTP with two instances:

```sh
./gradlew bootJar                                                                  # build once
HOTEL_SLOW_STEP=1 java -jar build/libs/hotel-booking-1.0-SNAPSHOT.jar                   # terminal 1: pauses 1 s between check and save
HOTEL_SLOW_STEP=1 HOTEL_PORT=5081 java -jar build/libs/hotel-booking-1.0-SNAPSHOT.jar   # terminal 2: a second instance, same database
java tools/RaceHttp.java http://localhost:5081                                     # terminal 3: one 201 Created and one 409 Conflict
```

The pre-check still answers the ordinary case; the constraint's refusal is mapped to the same `409 Unavailable`. [docs/interleaving.md](docs/interleaving.md) has the statement-by-statement trace and the four repairs that were compared.

## Module structure

The app is one program with four business modules. Each has a public contract and an `internal` package for implementations.

| Module | Public contract | Internal implementation |
| --- | --- | --- |
| Rooms | `Room`, `RoomCatalog` | `InMemoryRoomCatalog` |
| Reservations | Booking service, result, domain types and `ReservationStore` | `InMemoryReservationStore` |
| Payments | `PaymentService`, `PaymentStatus` | `SimulatedPaymentService` |
| Notifications | `Confirmation`, `NotificationSender` | Email and SMS senders |

`Main` creates the concrete implementations and connects them. Reservations uses the other modules’ public contracts. Notifications receives its own five-field `Confirmation` message: reservation id, guest email, room number, check-in and check-out.

```text
src/main/java/hotelbooking/
├── Main.java
├── rooms/
│   └── internal/
├── reservations/
│   └── internal/
├── payments/
│   └── internal/
└── notifications/
    └── internal/
```

## Verification

`./gradlew test` runs 19 checks:

- `BookingServiceTests`: five tests for acceptance, overlap refusal, adjacent stays, invalid dates and cancellation.
- `BookingNotificationTests`: three tests for confirmation ids, failure handling and SMS.
- `ArchitectureTests`: three ArchUnit rules enforcing module dependencies.
- `ApiTests`: six HTTP tests through MockMvc with the in-memory store and a map-backed cache: search, booking and conflict, invalid dates, cancel twice, a cache hit, invalidation after a booking and a cancel, two bookings at the same moment leaving one reservation, and the store refusing an overlapping save even without the pre-check.

The architecture rules prevent Reservations from depending on other modules’ internals, prevent Rooms from depending on other modules, and prevent Payments and Notifications from depending on Reservations. Test code is excluded from those dependency checks.

The boundary checker was verified by temporarily making `BookingService` reference the internal email sender: the expected architecture rule failed while the eight behavior tests passed. The forbidden reference was removed, restoring all 11 passing checks.

The HTML test report is available at `build/reports/tests/test/index.html` after a test run.

## Design documents

- [Requirements](docs/requirements.md): booking rules and verification examples.
- [Design notes](docs/design-notes.md): method responsibilities, booking traces, refactoring results and module verification.
- [Quality scenarios](docs/quality-scenarios.md): stakeholders and measurable quality targets; planned targets are distinguished from existing checks.
- [Business contexts](docs/contexts.md): hotel vocabulary, rule ownership and boundary contracts.
- [Architecture decision](docs/adr-001-modular-monolith.md): why the app uses a modular monolith and when to reconsider that choice.
- [Cache decision](docs/adr-002-cache-availability.md): why only the free-room search is cached in Redis, and why booking never reads the cache.
- [Concurrency decision](docs/adr-003-booking-concurrency.md) and [interleaving](docs/interleaving.md): why the booking rule is enforced by a PostgreSQL exclusion constraint, and what the rejected repairs cost.
- [API contract](docs/openapi.yaml), [capacity estimate](docs/capacity.md), [API check](docs/api-check.md) and [cache experiment](docs/cache-experiment.md): the HTTP API, its expected load, its verified answers against PostgreSQL, and the search measured with and without the cache (`java tools/Measure.java` while the app runs).
- [Diagrams](docs/diagrams.drawio): open in draw.io; the Context map and Modules pages show business ownership and module dependencies.

## Development progression

1. Built the in-memory booking model, documented requirements and demonstrated the check-then-save race.
2. Added behavior tests, extracted storage and notification interfaces, and introduced failure reporting and SMS support.
3. Defined four business contexts, a shared vocabulary, quality scenarios, rule ownership and boundary contracts.
4. Moved the code into four modules, introduced the room catalog and Confirmation message, added simulated payments, and enforced dependencies with architecture tests.
5. Put the app behind a Spring Boot HTTP API with an OpenAPI contract and API tests, and moved rooms and reservations into PostgreSQL.
6. Measured the search, cached it in Redis with a 30-second TTL and version-based invalidation, and recorded the cache decision in ADR-002.
7. Reproduced the double booking across two instances, compared four repairs with measurements, enforced the booking rule with a PostgreSQL exclusion constraint, and recorded the decision in ADR-003.
