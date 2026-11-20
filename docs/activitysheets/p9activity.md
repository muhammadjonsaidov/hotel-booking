# P09 · Activity sheet

## 1. P09 · Two guests, one room

P08 left the hotel app fast and still wrong. Every one of its 17 checks passes, and two guests who book the same room in the same moment both get it. Today you make that happen on purpose, then repair it four ways and keep the repair that cannot be forgotten.





### Where we are

Your hotel-booking folder has the P08 project: the app on http://localhost:5080 with three endpoints and GET /stats, PostgreSQL 18 and Redis 8 in Docker, 17 green checks, docs/capacity.md, docs/openapi.yaml, docs/cache-experiment.md and docs/adr-002-cache-availability.md.

**Why the app is wrong even though every test passes** In short: Every test sends one request at a time. The bug needs two requests in the same moment, so no test the project has could ever have found it. P06 to P08 built an app that is correct for one guest and fast for a thousand searches. None of that is wasted, and none of it is enough: the booking rule is checked by code that reads the database, decides, and then writes, and two requests can both read before either writes. That is not a bug in the check. The check is right every time it runs. It is a bug in the assumption that nothing happens in between.

1. Start: R7 is free for 12–14 November, and the 19 checks are green.
2. Action: two guests book R7 for those nights 500 ms apart.
3. Result: both are told 201 Created, and the room has two confirmed reservations. The tests are still green.

### Why that is a problem now

| What the hotel needs | What the P08 app does |
| --- | --- |
| A room is never sold twice for the same night | BookingService checks for a clash and then saves. Two requests can both check before either saves, and both are accepted. |
| The rule holds however many copies of the app are running | docs/capacity.md already plans for more than one instance, and nothing in the app or the database stops two of them double-booking. |
| Tests that would catch it | All 17 checks send one request at a time, so none of them could ever have found this. |

**What counts as evidence that it is fixed** In short: The race tool prints one 201 and one 409, the table holds one confirmed row, and two tests keep it that way. The tool and the table are the evidence for today. The two tests are the evidence for next month, when somebody changes this code and has forgotten the whole afternoon. One test sends two bookings at the same moment and expects one 201 and one 409. The other calls save directly, with the pre-check bypassed, and expects it to refuse — because the pre-check is a convenience and the promise is the rule.

1. Start: the constraint is in place and R7 has no reservations.
2. Action: you run the race tool, then look at the table.
3. Result:
Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.

Maya -> 201 Created   {"id":"R101","status":"Confirmed","notificationFailed":false}
Omar -> 409 Conflict  {"title":"Unavailable","status":409,"detail":"The room is booked for at least one night of this stay."}

Confirmed reservations for R7 on 12-14 Nov: 1
CORRECT: one booking was accepted and one was refused.

  id  | room |  check_in  | check_out  |  status
------+------+------------+------------+-----------
 R101 | R7   | 2026-11-12 | 2026-11-14 | Confirmed
(1 row)

Before the repair the same two commands gave:
  id  | room |  check_in  | check_out  |  status
------+------+------------+------------+-----------
 R101 | R7   | 2026-11-12 | 2026-11-14 | Confirmed
 R102 | R7   | 2026-11-12 | 2026-11-14 | Confirmed
(2 rows)

C#:

```text
[Fact]
public async Task Two_bookings_at_the_same_moment_give_the_room_one_reservation()
{
    // Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
    var (client, store) = await NewClientWithStore(slowStepMs: 300);
    var sameStay = new { guestName = "Omar", guestEmail = "omar@example.com", room = "R7", checkIn = "2026-11-12", checkOut = "2026-11-14" };

    // Started together, awaited together: Omar's check runs while Maya is still in the gap.
    var maya = client.PostAsJsonAsync("/reservations", Maya);
    var omar = client.PostAsJsonAsync("/reservations", sameStay);
    var answers = await Task.WhenAll(maya, omar);

    Assert.Equal([HttpStatusCode.Created, HttpStatusCode.Conflict], answers.Select(answer => answer.StatusCode).OrderBy(code => code));
    Assert.Equal(1, store.CountConfirmed(Rooms[0], new DateRange(new DateOnly(2026, 11, 12), new DateOnly(2026, 11, 14))));
}

[Fact]
public void The_store_refuses_an_overlapping_save_even_without_the_pre_check()
{
    // The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
    // Nothing calls HasOverlap here: Save itself must refuse.
    var store = new InMemoryReservationStore();
    var stay = new DateRange(new DateOnly(2026, 11, 12), new DateOnly(2026, 11, 14));
    store.Save(new Reservation("R101", new Guest("Maya", "maya@example.com"), Rooms[0], stay));

    var overlapping = new Reservation("R102", new Guest("Omar", "omar@example.com"), Rooms[0],
        new DateRange(new DateOnly(2026, 11, 13), new DateOnly(2026, 11, 15)));
    Assert.Throws<OverlapRejectedException>(() => store.Save(overlapping));
    Assert.Equal(1, store.CountConfirmed(Rooms[0], stay));
}
```

Python:

```text
# ---------------------------------------------------------------- P09
def test_two_bookings_at_the_same_moment_give_the_room_one_reservation(self) -> None:
    """Step 1's race, as a test. Both requests pass the pre-check; only one may be saved."""
    app, store = new_app(slow_step_seconds=0.3)  # wide enough that both pass the check first
    client = TestClient(app)
    answers: dict[str, int] = {}

    maya = threading.Thread(target=lambda: answers.update(maya=client.post("/reservations", json=MAYA).status_code))
    omar = threading.Thread(target=lambda: answers.update(omar=client.post("/reservations", json=OMAR).status_code))
    maya.start()
    omar.start()
    maya.join()
    omar.join()

    self.assertEqual([201, 409], sorted(answers.values()))
    stay = DateRange(date(2026, 11, 12), date(2026, 11, 14))
    self.assertEqual(1, store.count_confirmed(R7, stay))

def test_the_store_refuses_an_overlapping_save_even_without_the_pre_check(self) -> None:
    """The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
    Nothing calls has_overlap here: save itself must refuse."""
    store = InMemoryReservationStore()
    stay = DateRange(date(2026, 11, 12), date(2026, 11, 14))
    store.save(Reservation("R101", Guest("Maya", "maya@example.com"), R7, stay))

    overlapping = Reservation("R102", Guest("Omar", "omar@example.com"), R7,
                              DateRange(date(2026, 11, 13), date(2026, 11, 15)))
    with self.assertRaises(OverlapRejected):
        store.save(overlapping)
    self.assertEqual(1, store.count_confirmed(R7, stay))
```

Java:

```text
@Test
// ---------------------------------------------------------------- P09
@Test
void two_bookings_at_the_same_moment_give_the_room_one_reservation() throws Exception {
    // Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
    InMemoryReservationStore rows = new InMemoryReservationStore();
    Stats stats = new Stats();
    BookingService service = new BookingService(ROOMS, new CountingReservationStore(rows, stats),
            new EmailNotificationSender(), 300);

    // Two threads, both inside the gap between the check and the save.
    var maya = CompletableFuture.supplyAsync(
            () -> service.reserve(new Guest("Maya", "maya@example.com"), ROOMS.get(0), NOV_12_14));
    var omar = CompletableFuture.supplyAsync(
            () -> service.reserve(new Guest("Omar", "omar@example.com"), ROOMS.get(0), NOV_12_14));

    var outcomes = Stream.of(maya.join(), omar.join())
            .map(BookingResult::outcome).sorted().toList();
    assertEquals(List.of("Accepted", "Unavailable"), outcomes);
    assertEquals(1, rows.countConfirmed(ROOMS.get(0), NOV_12_14));
}

@Test
@Test
void the_store_refuses_an_overlapping_save_even_without_the_pre_check() {
    // The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
    // Nothing calls hasOverlap here: save itself must refuse.
    InMemoryReservationStore store = new InMemoryReservationStore();
    store.save(new Reservation("R101", new Guest("Maya", "maya@example.com"), ROOMS.get(0), NOV_12_14));

    Reservation overlapping = new Reservation("R102", new Guest("Omar", "omar@example.com"), ROOMS.get(0),
            new DateRange(LocalDate.of(2026, 11, 13), LocalDate.of(2026, 11, 15)));
    assertThrows(OverlapRejectedException.class, () -> store.save(overlapping));
    assertEquals(1, store.countConfirmed(ROOMS.get(0), NOV_12_14));
}
```

Go:

```text
// Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
func TestTwoBookingsAtTheSameMomentGiveTheRoomOneReservation(t *testing.T) {
	store := &InMemoryReservationStore{}
	service := NewBookingService([]rooms.Room{r7, r8}, store, &fakeNotificationSender{}, 300*time.Millisecond)
	stay := nov(12, 14)

	// Two goroutines, both inside the gap between the check and the save.
	outcomes := make([]string, 2)
	var waiting sync.WaitGroup
	waiting.Add(2)
	go func() {
		defer waiting.Done()
		outcomes[0] = service.Reserve(maya, r7, stay).Outcome
	}()
	go func() {
		defer waiting.Done()
		outcomes[1] = service.Reserve(omar, r7, stay).Outcome
	}()
	waiting.Wait()

	slices.Sort(outcomes)
	if want := []string{"Accepted", "Unavailable"}; !slices.Equal(outcomes, want) {
		t.Errorf("outcomes = %v, want %v", outcomes, want)
	}
	if confirmed := store.CountConfirmed(r7, stay); confirmed != 1 {
		t.Errorf("confirmed reservations for R7 = %d, want 1", confirmed)
	}
}

// The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
// Nothing calls HasOverlap here: Save itself must refuse.
func TestTheStoreRefusesAnOverlappingSaveEvenWithoutThePreCheck(t *testing.T) {
	store := &InMemoryReservationStore{}
	stay := nov(12, 14)
	if err := store.Save(NewReservation("R101", maya, r7, stay)); err != nil {
		t.Fatalf("the first save failed: %v", err)
	}

	err := store.Save(NewReservation("R102", omar, r7, nov(13, 15)))
	if !errors.Is(err, ErrOverlapRejected) {
		t.Errorf("err = %v, want it to wrap ErrOverlapRejected", err)
	}
	if confirmed := store.CountConfirmed(r7, stay); confirmed != 1 {
		t.Errorf("confirmed reservations for R7 = %d, want 1", confirmed)
	}
}
```

JavaScript:

```text
// ---------------------------------------------------------------- P09
test('two bookings at the same moment give the room one reservation', async t => {
  // Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
  const send = await startApp(t, 300);
  const sameStay = { ...omar, checkIn: '2026-11-12', checkOut: '2026-11-14' };

  // Started together, awaited together: Omar's check runs while Maya is still in the gap.
  const [first, second] = await Promise.all([
    send('POST', '/reservations', maya),
    send('POST', '/reservations', sameStay),
  ]);

  assert.deepEqual([first.status, second.status].sort(), [201, 409]);
  const stay = new DateRange(new Date('2026-11-12T00:00:00Z'), new Date('2026-11-14T00:00:00Z'));
  assert.equal(send.store.countConfirmed(rooms[0], stay), 1);
});

test('the store refuses an overlapping save even without the pre-check', async () => {
  // The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
  // Nothing calls hasOverlap here: save itself must refuse.
  const store = new InMemoryReservationStore();
  const stay = new DateRange(new Date('2026-11-12T00:00:00Z'), new Date('2026-11-14T00:00:00Z'));
  store.save(new Reservation('R101', new Guest('Maya', 'maya@example.com'), rooms[0], stay));

  const overlapping = new Reservation('R102', new Guest('Omar', 'omar@example.com'), rooms[0],
    new DateRange(new Date('2026-11-13T00:00:00Z'), new Date('2026-11-15T00:00:00Z')));
  assert.throws(() => store.save(overlapping), OverlapRejected);
  assert.equal(store.countConfirmed(rooms[0], stay), 1);
});
```

Dart:

```text
// ---------------------------------------------------------------- P09
test('two bookings at the same moment give the room one reservation', () async {
  // Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
  final app = newAppWithStore(slowStep: const Duration(milliseconds: 300));
  const sameStay = {
    'guestName': 'Omar',
    'guestEmail': 'omar@example.com',
    'room': 'R7',
    'checkIn': '2026-11-12',
    'checkOut': '2026-11-14'
  };

  // Started together, awaited together: Omar's check runs while Maya is still in the gap.
  final answers = await Future.wait([
    send(app.handler, 'POST', '/reservations', maya),
    send(app.handler, 'POST', '/reservations', sameStay),
  ]);

  expect(answers.map((answer) => answer.statusCode).toList()..sort(), [201, 409]);
  expect(
      app.store.countConfirmed(
          rooms[0], DateRange(DateTime.utc(2026, 11, 12), DateTime.utc(2026, 11, 14))),
      1);
});

test('the store refuses an overlapping save even without the pre-check', () {
  // The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
  // Nothing calls hasOverlap here: save itself must refuse.
  final store = InMemoryReservationStore();
  final stay = DateRange(DateTime.utc(2026, 11, 12), DateTime.utc(2026, 11, 14));
  store.save(Reservation('R101', Guest('Maya', 'maya@example.com'), rooms[0], stay));

  final overlapping = Reservation('R102', Guest('Omar', 'omar@example.com'), rooms[0],
      DateRange(DateTime.utc(2026, 11, 13), DateTime.utc(2026, 11, 15)));
  expect(() => store.save(overlapping), throwsA(isA<OverlapRejected>()));
  expect(store.countConfirmed(rooms[0], stay), 1);
});
```

### Today’s plan

| Step | What you do | What it adds to the project folder |
| --- | --- | --- |
| 1 · Reproduce it | Send two bookings at the same moment and watch the room get booked twice | tools/ gains the race tool; db/init.sql gains the id sequence |
| 2 · Write it down | Put both requests’ statements in one list, in time order | docs/interleaving.md |
| 3 · A lock inside the app | See the lock the list store already holds, and what it protects | nothing new: --race proves it |
| 4 · A second instance | Run the app twice against one database and watch the double booking come back | docs/interleaving.md gains the result |
| 5 · SELECT … FOR UPDATE | Lock a guard row in two psql terminals and measure the wait | docs/interleaving.md gains the measurement |
| 6 · Optimistic version | The same guard row without a lock, and measure the retry instead | docs/interleaving.md gains the measurement |
| 7 · The exclusion constraint | Teach the database the rule itself, and prove no code path can bypass it | db/init.sql, and the app maps the refusal to 409 |
| 8 · Write ADR-003 | Record the decision, the three rejected repairs and what each cost | docs/adr-003-booking-concurrency.md |

### How you work today

Work in pairs on one laptop, in the hotel-booking folder from P08, in the same language as before. Swap who types at every new step. Every code slide has a tab for each of the six languages.

You need Docker Desktop running (PostgreSQL and Redis run inside it), your editor, and three terminals opened in the hotel-booking folder: one for the app, one for a second copy of the app in Step 4, and one for the race tool and psql. No internet is needed today: nothing new is downloaded.

If you missed P08: download the P08 runnable code in your language from the P08 lesson page. The “Runnable code” link on this page has the finished P09 project in all six languages.

### Completed example: your folder at the end of P09

### hotel-booking folder

#### C#

```text
hotel-booking/   end of P09
├── db/
│   └── init.sql   CHANGED: + btree_gist, the exclusion constraint and the id sequence
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md
│   ├── api-check.md
│   ├── cache-experiment.md
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml
│   ├── quality-scenarios.md
│   └── requirements.md
├── HotelBooking/
│   ├── Api/
│   │   ├── AvailabilityCache.cs
│   │   ├── HotelApi.cs
│   │   └── Stats.cs   CHANGED: the counting store passes NextReservationId on
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
│   │   │   ├── InMemoryReservationStore.cs   CHANGED: checks under a lock
│   │   │   └── PostgresReservationStore.cs   CHANGED: the constraint violation becomes OverlapRejected; ids from the sequence
│   │   ├── BookingResult.cs
│   │   ├── BookingService.cs   CHANGED: a refused save becomes Unavailable
│   │   ├── DateRange.cs
│   │   ├── Guest.cs
│   │   ├── IReservationStore.cs   CHANGED: + NextReservationId, and Save now promises to refuse an overlap
│   │   ├── OverlapRejectedException.cs   NEW
│   │   ├── Reservation.cs
│   │   └── ReservationStatus.cs
│   ├── Rooms/
│   │   ├── Internal/
│   │   │   ├── InMemoryRoomCatalog.cs
│   │   │   └── PostgresRoomCatalog.cs
│   │   ├── IRoomCatalog.cs
│   │   └── Room.cs
│   ├── HotelBooking.csproj
│   └── Program.cs   CHANGED: HOTEL_SLOW_STEP and HOTEL_PORT
├── HotelBooking.Tests/
│   ├── ApiTests.cs   CHANGED: + 2 concurrency tests
│   ├── ArchitectureTests.cs
│   ├── BookingServiceTests.cs
│   └── HotelBooking.Tests.csproj
├── tools/
│   ├── measure.cs
│   └── race-http.cs   NEW
└── docker-compose.yml
```

#### Python

```text
hotel-booking/   end of P09
├── db/
│   └── init.sql   CHANGED: + btree_gist, the exclusion constraint and the id sequence
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md
│   ├── api-check.md
│   ├── cache-experiment.md
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml
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
│   │   ├── __init__.py   CHANGED: + OverlapRejected
│   │   ├── booking.py   CHANGED: + OverlapRejected and NextReservationId; a refused save becomes Unavailable
│   │   ├── domain.py
│   │   └── internal.py   CHANGED: the list store checks under a lock; PostgreSQL maps the constraint and uses the sequence
│   ├── rooms/
│   │   ├── __init__.py
│   │   ├── catalog.py
│   │   └── internal.py
│   ├── __init__.py
│   ├── api_cache.py
│   └── api.py   CHANGED: the counting store passes next_reservation_id on
├── tools/
│   ├── measure.py
│   └── race_http.py   NEW
├── docker-compose.yml
├── main.py   CHANGED: HOTEL_SLOW_STEP and HOTEL_PORT
├── pyproject.toml
├── requirements.txt
├── test_api.py   CHANGED: + 2 concurrency tests
└── test_booking_service.py
```

#### Java

```text
hotel-booking/   end of P09
├── db/
│   └── init.sql   CHANGED: + btree_gist, the exclusion constraint and the id sequence
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md
│   ├── api-check.md
│   ├── cache-experiment.md
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml
│   ├── quality-scenarios.md
│   └── requirements.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── hotelbooking/
│   │   │       ├── api/
│   │   │       │   ├── AvailabilityCache.java
│   │   │       │   ├── CountingReservationStore.java   CHANGED: passes nextReservationId on
│   │   │       │   ├── HotelApi.java
│   │   │       │   └── Stats.java
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
│   │   │       │   │   ├── InMemoryReservationStore.java   CHANGED: checks inside a synchronized block
│   │   │       │   │   └── PostgresReservationStore.java   CHANGED: the constraint violation becomes OverlapRejectedException; ids from the sequence
│   │   │       │   ├── BookingResult.java
│   │   │       │   ├── BookingService.java   CHANGED: a refused save becomes Unavailable
│   │   │       │   ├── DateRange.java
│   │   │       │   ├── Guest.java
│   │   │       │   ├── OverlapRejectedException.java   NEW
│   │   │       │   ├── Reservation.java
│   │   │       │   ├── ReservationStatus.java
│   │   │       │   └── ReservationStore.java   CHANGED: + nextReservationId, and save now promises to refuse an overlap
│   │   │       ├── rooms/
│   │   │       │   ├── internal/
│   │   │       │   │   ├── InMemoryRoomCatalog.java
│   │   │       │   │   └── PostgresRoomCatalog.java
│   │   │       │   ├── Room.java
│   │   │       │   └── RoomCatalog.java
│   │   │       └── Main.java   CHANGED: HOTEL_SLOW_STEP, and a refused booking rolls the transaction back
│   │   └── resources/
│   │       └── application.properties   CHANGED: server.port follows HOTEL_PORT
│   └── test/
│       └── java/
│           └── hotelbooking/
│               ├── ApiTests.java   CHANGED: + 2 concurrency tests
│               ├── ArchitectureTests.java
│               └── BookingServiceTests.java
├── tools/
│   ├── Measure.java
│   └── RaceHttp.java   NEW
├── docker-compose.yml
└── pom.xml
```

#### Go

```text
hotel-booking/   end of P09
├── api/
│   ├── api_test.go
│   ├── api.go
│   ├── cache.go
│   └── stats.go   CHANGED: the counting store passes NextReservationID on
├── db/
│   └── init.sql   CHANGED: + btree_gist, the exclusion constraint and the id sequence
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md
│   ├── api-check.md
│   ├── cache-experiment.md
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml
│   ├── quality-scenarios.md
│   └── requirements.md
├── notifications/
│   ├── internal.go
│   └── notifications.go
├── payments/
│   ├── internal.go
│   └── payments.go
├── reservations/
│   ├── booking_test.go   CHANGED: + 2 concurrency tests
│   ├── booking.go   CHANGED: + ErrOverlapRejected and NextReservationID; Save now returns an error
│   ├── domain.go
│   └── internal.go   CHANGED: the list store takes a mutex; PostgreSQL maps the constraint and uses the sequence
├── rooms/
│   ├── internal.go
│   └── rooms.go
├── tools/
│   ├── measure/
│   │   └── main.go
│   └── race/
│       └── main.go   NEW
├── architecture_test.go
├── docker-compose.yml
├── go.mod
├── go.sum
└── main.go   CHANGED: HOTEL_SLOW_STEP and HOTEL_PORT
```

#### JavaScript

```text
hotel-booking/   end of P09
├── db/
│   └── init.sql   CHANGED: + btree_gist, the exclusion constraint and the id sequence
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md
│   ├── api-check.md
│   ├── cache-experiment.md
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml
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
│   │   │   ├── postgres-store.js   CHANGED: the constraint violation becomes OverlapRejected; ids from the sequence
│   │   │   └── store.js   CHANGED: save refuses an overlap; + nextReservationId
│   │   ├── booking.js   CHANGED: + OverlapRejected; a refused save becomes Unavailable
│   │   ├── domain.js
│   │   └── index.js   CHANGED: + OverlapRejected
│   ├── rooms/
│   │   ├── internal/
│   │   │   ├── catalog.js
│   │   │   └── postgres-catalog.js
│   │   └── index.js
│   ├── api-cache.js
│   └── api.js   CHANGED: the counting store passes nextReservationId on
├── test/
│   ├── api.test.js   CHANGED: + 2 concurrency tests
│   ├── architecture.test.js
│   └── booking.test.js
├── tools/
│   ├── measure.js
│   └── race-http.js   NEW
├── docker-compose.yml
├── main.js   CHANGED: HOTEL_SLOW_STEP and HOTEL_PORT
├── package-lock.json
└── package.json
```

#### Dart

```text
hotel-booking/   end of P09
├── bin/
│   └── main.dart   CHANGED: HOTEL_SLOW_STEP and HOTEL_PORT
├── db/
│   └── init.sql   CHANGED: + btree_gist, the exclusion constraint and the id sequence
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md
│   ├── api-check.md
│   ├── cache-experiment.md
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml
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
│   │   │   ├── booking.dart   CHANGED: + OverlapRejected and nextReservationId; a refused save becomes Unavailable
│   │   │   ├── domain.dart
│   │   │   ├── postgres_store.dart   CHANGED: the constraint violation becomes OverlapRejected; ids from the sequence
│   │   │   └── store.dart   CHANGED: save refuses an overlap
│   │   └── reservations.dart
│   ├── rooms/
│   │   ├── src/
│   │   │   ├── catalog.dart
│   │   │   └── postgres_catalog.dart
│   │   └── rooms.dart
│   ├── api_cache.dart
│   └── api.dart   CHANGED: the counting store passes nextReservationId on
├── test/
│   ├── api_test.dart   CHANGED: + 2 concurrency tests
│   ├── architecture_test.dart
│   └── booking_service_test.dart
├── tool/
│   ├── measure.dart
│   └── race_http.dart   NEW
├── analysis_options.yaml
├── docker-compose.yml
└── pubspec.yaml
```

### Your turn · open your project

1. Start Docker Desktop and wait until it says “Engine running”.
2. Open your hotel-booking folder in your editor, and open three terminals in the same folder.
3. In the first terminal run docker compose up -d, then your P08 checks: 17 green (Python: 14 tests, then “Contracts: 3 kept”).
4. Open docs/l05-notes.md from the end of L05 and read your three answers again. The repair you chose there is the one Step 7 builds.
5. Open docs/design-notes.md and add a new heading at the bottom: # P09 design notes

**When you finish, you have:** Docker Desktop running, your P08 project open with 17 green checks, and a “P09 design notes” heading.
### Explanation

Nothing about the app is slow or badly written. The bug is in an assumption: that nothing happens between checking and saving. Every repair today closes that gap in a different place — in the process, in a row, in a version number, or in the table itself.



---

## 2. Step 1 · Reproduce the double booking

A small tool sends two bookings for the same room and the same nights at the same moment. First the database gets a sequence for reservation ids, because the old way of making an id is itself a race and it would hide the one we are here for.





### What Step 1 adds

| What | Where | What it does |
| --- | --- | --- |
| An id sequence | db/init.sql and your running database | Hands every request a different reservation id, so two bookings cannot both be called R101 |
| nextReservationId | the store contract and both stores | The store hands out the id: PostgreSQL from the sequence, the list store by counting |
| The race tool | tools/ (Dart: tool/) | Two POST /reservations at the same moment, and prints what each one got back |
| HOTEL_SLOW_STEP | Your wiring file | Makes every booking pause between checking and saving, so the race happens on every run |
| HOTEL_PORT | Your wiring file | Lets Step 4 start a second instance on another port |

**Why the reservation id needed fixing too** In short: Ids came from counting the rows, so two requests that counted before either had saved both got R101 — a second race hiding behind the first. It shows up in Java first, because Spring wraps each request in one transaction, so an uncommitted row is invisible to the other request’s count. The other five languages commit each statement as it is made and happened to dodge it. With the count-based id the second INSERT failed on the primary key instead of on the booking rule — the wrong error, for the wrong reason, hiding the thing the lesson is about. A sequence never hands the same number to two callers, and never waits for or rolls back with the transaction that asked. Because a sequence does not roll back, running the race tool twice gives R103 and R104. Reset the database if you want the ids in the slides.

1. Start: two bookings arrive at the same moment, and neither has committed.
2. Action: each asks SELECT nextval('reservation_ids').
3. Result: one gets 101 and the other 102. Nobody waits, and the numbers are never the same.

sql:

```text
-- P09 Step 1: reservation ids come from here, not from counting the rows.
-- nextval never returns the same number twice, even to two transactions running at the same moment.
CREATE SEQUENCE reservation_ids START 101;

-- The running database needs it too, because init.sql only runs when the database is created.
-- Start it above the ids that already exist, so it never collides with them.
SELECT setval('reservation_ids', GREATEST(101, (SELECT count(*) FROM reservations) + 101), false);
```

**What the store now promises** In short: save refuses an overlapping reservation rather than writing one. Every store keeps that promise; how is its own business. The list store keeps it with a check under a lock. PostgreSQL keeps it with the exclusion constraint. Because both keep the same promise, the two new tests need no database and still test the rule the constraint protects. The pre-check in reserve was deliberately kept in front of it. It answers the ordinary case with a readable 409; the promise is what makes the rule true when two requests check at the same moment.

1. Start: R7 has a confirmed reservation for 12–14 November.
2. Action: something calls save directly with an overlapping reservation, skipping the pre-check.
3. Result: save raises OverlapRejected, and nothing is written.

C#:

```text
// HotelBooking/Reservations/IReservationStore.cs
using HotelBooking.Rooms;

namespace HotelBooking.Reservations;

// What BookingService needs from storage. Today a list in memory; from P07 a database.
public interface IReservationStore
{
    int Count { get; }
    bool HasOverlap(Room room, DateRange stay);

    // The id for the next reservation.
    //
    // The store owns this, because only the store can hand out an id that no other request can
    // also be given. Counting the rows and adding one cannot: two requests that count before
    // either has saved both get R101, and the second INSERT then fails on the primary key instead
    // of on the booking rule. PostgreSQL answers from a sequence; the list store counts, which is
    // safe there because it holds a lock.
    string NextReservationId();

    // Writes the reservation.
    //
    // Throws OverlapRejectedException if writing it would give the room two Confirmed reservations
    // covering the same night. The pre-check in Reserve usually catches that first; this promise is
    // what makes the rule true when two requests check at the same moment.
    void Save(Reservation reservation);

    Reservation? Find(string id);
    int CountConfirmed(Room room, DateRange stay);
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


class OverlapRejected(Exception):
    """The store refused a save because it would give the room two Confirmed reservations
    covering the same night.

    Every store promises this, and the promise is part of the ReservationStore contract. How a
    store keeps it is its own business: the list store checks inside save, and PostgreSQL refuses
    the INSERT with the exclusion constraint reservations_no_overlap. BookingService turns it into
    Unavailable - the same answer the pre-check gives - so a guest never sees a database error.
    """


class ReservationStore(Protocol):
    """What BookingService needs from storage. Today a list in memory; from P07 a database."""

    def count(self) -> int: ...
    def has_overlap(self, room: Room, stay: DateRange) -> bool: ...

    def next_reservation_id(self) -> str:
        """The id for the next reservation.

        The store owns this, because only the store can hand out an id that no other request can
        also be given. Counting the rows and adding one cannot: two requests that count before
        either has saved both get R101, and the second INSERT then fails on the primary key instead
        of on the booking rule. PostgreSQL answers from a sequence; the list store counts, which is
        safe there because it holds a lock.
        """
        ...

    def save(self, reservation: Reservation) -> None:
        """Writes the reservation.

        Raises OverlapRejected if writing it would give the room two Confirmed reservations
        covering the same night. The pre-check in reserve() usually catches that first; this
        promise is what makes the rule true when two requests check at the same moment.
        """
        ...

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

        time.sleep(self.slow_step_seconds)  # 0 normally; the race tool makes this step slow

        reservation = Reservation(self.store.next_reservation_id(), guest, room, stay)
        try:
            self.store.save(reservation)
        except OverlapRejected:
            # Another request saved an overlapping reservation after our check and before our save.
            # The check was honest when it ran; it had simply stopped being true. The guest gets the
            # same answer either way.
            return BookingResult("Unavailable")
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
// src/main/java/hotelbooking/reservations/ReservationStore.java
package hotelbooking.reservations;

import hotelbooking.rooms.Room;

// What BookingService needs from storage. Today a list in memory; from P07 a database.
public interface ReservationStore {
    int count();

    boolean hasOverlap(Room room, DateRange stay);

    // The id for the next reservation.
    //
    // The store owns this, because only the store can hand out an id that no other request can
    // also be given. Counting the rows and adding one cannot: two requests that count before either
    // has saved both get R101, and the second INSERT then fails on the primary key instead of on
    // the booking rule. That is exactly what happens here, because Spring wraps each request in one
    // transaction, so an uncommitted row is invisible to the other request's count.
    // PostgreSQL answers from a sequence; the list store counts, which is safe there because it
    // holds a lock.
    String nextReservationId();

    // Writes the reservation.
    //
    // Throws OverlapRejectedException if writing it would give the room two Confirmed reservations
    // covering the same night. The pre-check in reserve usually catches that first; this promise is
    // what makes the rule true when two requests check at the same moment.
    void save(Reservation reservation);

    // Returns null when no reservation has that id.
    Reservation find(String id);

    int countConfirmed(Room room, DateRange stay);
}
```

Go:

```text
// reservations/booking.go
package reservations

import (
	"errors"
	"time"

	"hotelbooking/notifications"
	"hotelbooking/rooms"
)

// ErrOverlapRejected is returned by Save when writing a reservation would give the room two
// Confirmed reservations covering the same night.
//
// Every store promises this, and the promise is part of ReservationStore. How a store keeps it is
// its own business: the list store checks inside Save, and PostgreSQL refuses the INSERT with the
// exclusion constraint reservations_no_overlap. BookingService turns it into Unavailable - the same
// answer the pre-check gives - so a guest never sees a database error.
var ErrOverlapRejected = errors.New("the room already has a confirmed reservation for these nights")

// ReservationStore is what BookingService needs from storage.
// Today a list in memory; from P07 a database.
type ReservationStore interface {
	Count() int
	HasOverlap(room rooms.Room, stay DateRange) bool

	// NextReservationID is the id for the next reservation.
	//
	// The store owns this, because only the store can hand out an id that no other request can
	// also be given. Counting the rows and adding one cannot: two requests that count before
	// either has saved both get R101, and the second INSERT then fails on the primary key instead
	// of on the booking rule. PostgreSQL answers from a sequence; the list store counts, which is
	// safe there because it holds the mutex.
	NextReservationID() string

	// Save writes the reservation. It returns an error wrapping ErrOverlapRejected if writing it
	// would give the room two Confirmed reservations covering the same night. The pre-check in
	// Reserve usually catches that first; this promise is what makes the rule true when two
	// requests check at the same moment.
	//
	// Any other kind of failure is not returned here: the store keeps it and SaveChanges reports
	// it, the same as in P07.
	Save(reservation *Reservation) error

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

	time.Sleep(service.slowStep) // zero normally; the race tool makes this step slow

	reservation := NewReservation(service.store.NextReservationID(), guest, room, stay)
	if err := service.store.Save(reservation); errors.Is(err, ErrOverlapRejected) {
		// Another request saved an overlapping reservation after our check and before our save.
		// The check was honest when it ran; it had simply stopped being true. The guest gets the
		// same answer either way.
		return BookingResult{Outcome: "Unavailable"}
	}
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
// src/reservations/internal/store.js
import { OverlapRejected } from '../booking.js';

// What BookingService needs from storage. JavaScript has no interface keyword, so the
// contract is written down here and every store keeps to it:
//
//   count                       how many reservations are saved
//   hasOverlap(room, stay)      is a Confirmed reservation already using those nights?
//   nextReservationId()         the id for the next reservation. The store owns this, because only
//                               the store can hand out an id no other request can also be given:
//                               counting the rows and adding one cannot, since two requests that
//                               count before either has saved both get R101
//   save(reservation)           keep this reservation; throws OverlapRejected rather than
//                               giving the room two Confirmed reservations for the same night
//   find(id)                    the reservation with that id, or null
//   countConfirmed(room, stay)  how many Confirmed reservations use those nights
//
// A store may answer count, hasOverlap, save and find with a Promise (a database does);
// BookingService awaits them. This store keeps a list in memory and answers at once.
export class InMemoryReservationStore {
  #rows = [];

  get count() {
    return this.#rows.length;
  }

  hasOverlap(room, stay) {
    return this.#rows.some(row => row.blocks(room, stay));
  }

  // Safe here for the same reason save is: nothing else can run between the read and the use.
  nextReservationId() {
    return `R${101 + this.#rows.length}`;
  }

  // No lock, and none is needed: Node runs one request's code at a time, and there is no await
  // between the check and the push, so no other request can run in the middle of this method.
  // This check is the list store's version of the exclusion constraint.
  save(reservation) {
    if (this.hasOverlap(reservation.room, reservation.stay)) {
      throw new OverlapRejected(`${reservation.room.number} already has a confirmed reservation for these nights.`);
    }
    this.#rows.push(reservation);
  }

  // Returns null when no reservation has that id.
  find(id) {
    return this.#rows.find(row => row.id === id) ?? null;
  }

  countConfirmed(room, stay) {
    return this.#rows.filter(row => row.blocks(room, stay)).length;
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

/// The store refused a save because it would give the room two Confirmed reservations covering the
/// same night.
///
/// Every store promises this, and the promise is part of [ReservationStore]. How a store keeps it
/// is its own business: the list store checks inside save, and PostgreSQL refuses the INSERT with
/// the exclusion constraint reservations_no_overlap. BookingService turns it into Unavailable - the
/// same answer the pre-check gives - so a guest never sees a database error.
class OverlapRejected implements Exception {
  OverlapRejected(this.message);

  final String message;

  @override
  String toString() => 'OverlapRejected: $message';
}

// What BookingService needs from storage: a list in memory answers at once, a database
// answers later (a Future). FutureOr allows both, and BookingService awaits the answer.
abstract interface class ReservationStore {
  FutureOr<int> get count;

  FutureOr<bool> hasOverlap(Room room, DateRange stay);

  /// The id for the next reservation.
  ///
  /// The store owns this, because only the store can hand out an id that no other request can
  /// also be given. Counting the rows and adding one cannot: two requests that count before either
  /// has saved both get R101, and the second INSERT then fails on the primary key instead of on the
  /// booking rule. PostgreSQL answers from a sequence; the list store counts, which is safe there
  /// because nothing can run in the middle of it.
  FutureOr<String> nextReservationId();

  /// Writes the reservation.
  ///
  /// Throws [OverlapRejected] if writing it would give the room two Confirmed reservations
  /// covering the same night. The pre-check in reserve usually catches that first; this promise is
  /// what makes the rule true when two requests check at the same moment.
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

    final reservation = Reservation(await store.nextReservationId(), guest, room, stay);
    try {
      await store.save(reservation);
    } on OverlapRejected {
      // Another request saved an overlapping reservation after our check and before our save. The
      // check was honest when it ran; it had simply stopped being true.
      return BookingResult('Unavailable');
    }
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

**What the race tool does** In short: It sends two POST /reservations for the same room and the same nights at the same moment, and prints what each one got back. Maya’s request goes first and Omar’s 500 ms later, so both reach the gap together and leave it in a known order. That stagger is what makes the printed output the same on every run; it is not what makes the bug possible. One warm-up request is sent before the two bookings. The first request to a freshly started app pays for connection pools, servlet initialisation and JIT, which can take longer than Maya’s head start — and then Omar wins and the output comes out the other way round. The warm-up cannot reach the booking path itself, because GET /stats never inserts, so Maya’s request is still the first to compile the insert: half a second of head start is what covers it.

1. Start: the app is running with HOTEL_SLOW_STEP=1 and R7 has no reservations.
2. Action: you run the race tool.
3. Result:
Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.

Maya -> 201 Created   {"id":"R101","status":"Confirmed","notificationFailed":false}
Omar -> 201 Created   {"id":"R102","status":"Confirmed","notificationFailed":false}

Confirmed reservations for R7 on 12-14 Nov: 2
DOUBLE BOOKED: the room has two confirmed reservations for the same nights.

C#:

```text
// tools/race-http.cs
// Sends two POST /reservations for the same room and the same nights at the same moment, and prints
// what each one got back and how many confirmed reservations the room ended up with.
//
//   dotnet run tools/race-http.cs                            both requests to http://localhost:5080
//   dotnet run tools/race-http.cs http://localhost:5081      Omar's request to the second instance
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
//
// A file-based app (dotnet run <file>.cs) is compiled ready for ahead-of-time publishing, and that
// turns off the reflection the typed JSON helpers below need - PostAsJsonAsync would throw
// "Reflection-based serialization has been disabled for this application". This tool is run, never
// published, so the AOT settings do nothing for it and are turned off.
#:property PublishAot=false
using System.Net;
using System.Net.Http.Json;

var mayaBase = "http://localhost:5080";
var omarBase = args.Length > 0 ? args[0] : mayaBase;
var maya = new { guestName = "Maya", guestEmail = "maya@example.com", room = "R7", checkIn = "2026-11-12", checkOut = "2026-11-14" };
var omar = new { guestName = "Omar", guestEmail = "omar@example.com", room = "R7", checkIn = "2026-11-12", checkOut = "2026-11-14" };

using var client = new HttpClient();
await client.GetStringAsync($"{mayaBase}/stats"); // the warm-up: see the note at the top

Console.WriteLine("Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.");
if (omarBase != mayaBase) Console.WriteLine($"Maya -> {mayaBase}   Omar -> {omarBase}");
Console.WriteLine();

// Both requests are started before either is awaited: that is what "at the same moment" means here.
var mayaRequest = Book(mayaBase, maya);
var omarRequest = Delayed(500, () => Book(omarBase, omar)); // Omar clicks a moment after Maya
var answers = await Task.WhenAll(mayaRequest, omarRequest);

Console.WriteLine($"Maya -> {answers[0]}");
Console.WriteLine($"Omar -> {answers[1]}");
Console.WriteLine();

var accepted = answers.Count(answer => answer.StartsWith("201"));
Console.WriteLine($"Confirmed reservations for R7 on 12-14 Nov: {accepted}");
Console.WriteLine(accepted > 1
    ? "DOUBLE BOOKED: the room has two confirmed reservations for the same nights."
    : "CORRECT: one booking was accepted and one was refused.");

async Task<string> Book(string baseAddress, object body)
{
    var response = await client.PostAsJsonAsync($"{baseAddress}/reservations", body);
    var reason = response.StatusCode switch
    {
        HttpStatusCode.Created => "201 Created   ",
        HttpStatusCode.Conflict => "409 Conflict  ",
        HttpStatusCode.BadRequest => "400 Bad Request  ",
        var other => $"{(int)other} {other}  ",
    };
    return reason + await response.Content.ReadAsStringAsync();
}

async Task<string> Delayed(int milliseconds, Func<Task<string>> work)
{
    await Task.Delay(milliseconds);
    return await work();
}
```

Python:

```text
# tools/race_http.py
"""Sends two POST /reservations for the same room and the same nights at the same moment, and
prints what each one got back and how many confirmed reservations the room ended up with.

  python tools/race_http.py                             both requests to http://localhost:5080
  python tools/race_http.py http://localhost:5081        Omar's request to the second instance

Start the app with HOTEL_SLOW_STEP=1 first. Without it the gap between the check and the save is
too small to land in reliably, and the race shows up only now and then.

Maya's request goes first and Omar's 500 ms later, so the two reach the gap together and leave it
in a known order. That is what makes the output the same on every run; it is not what makes the
bug possible. The bug is possible because checking and saving are two separate statements.

A warm-up request is sent before the two bookings. The very first request to a freshly started app
pays for connection pools and import work, which can cost more than the head start Maya is given -
and then Omar wins instead and the output comes out the other way round. The warm-up cannot reach
the booking path itself, though - GET /stats never inserts - so Maya's request is still the first to
compile the insert, which measured as much as 140 ms more than Omar's. Half a second of head start
covers that and leaves both requests inside the gap.
"""
from __future__ import annotations

import json
import sys
import threading
import urllib.error
import urllib.request

MAYA = {"guestName": "Maya", "guestEmail": "maya@example.com", "room": "R7",
        "checkIn": "2026-11-12", "checkOut": "2026-11-14"}
OMAR = {**MAYA, "guestName": "Omar", "guestEmail": "omar@example.com"}
REASONS = {201: "201 Created", 409: "409 Conflict", 400: "400 Bad Request"}


def book(base: str, body: dict) -> str:
    request = urllib.request.Request(f"{base}/reservations", method="POST",
                                     data=json.dumps(body).encode(),
                                     headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(request) as response:
            return f"{REASONS.get(response.status, response.status)}   {response.read().decode()}"
    except urllib.error.HTTPError as error:
        return f"{REASONS.get(error.code, error.code)}  {error.read().decode()}"


def confirmed_count(base: str) -> int:
    """How many rooms the search still offers tells us nothing; count the reservations instead."""
    with urllib.request.urlopen(f"{base}/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14") as response:
        free = [room["number"] for room in json.loads(response.read())["rooms"]]
    return 0 if "R7" in free else 1


def main() -> None:
    maya_base = "http://localhost:5080"
    omar_base = sys.argv[1] if len(sys.argv) > 1 else maya_base
    answers: dict[str, str] = {}

    with urllib.request.urlopen(f"{maya_base}/stats") as warm_up:  # see the note at the top
        warm_up.read()

    print("Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.")
    if omar_base != maya_base:
        print(f"Maya -> {maya_base}   Omar -> {omar_base}")
    print()

    maya_request = threading.Thread(target=lambda: answers.update(Maya=book(maya_base, MAYA)))
    omar_request = threading.Thread(target=lambda: answers.update(Omar=book(omar_base, OMAR)))
    maya_request.start()
    threading.Event().wait(0.5)  # Omar clicks a moment after Maya
    omar_request.start()
    maya_request.join()
    omar_request.join()

    print(f"Maya -> {answers['Maya']}")
    print(f"Omar -> {answers['Omar']}")
    print()
    accepted = sum(1 for answer in answers.values() if answer.startswith("201"))
    print(f"Confirmed reservations for R7 on 12-14 Nov: {accepted}")
    print("DOUBLE BOOKED: the room has two confirmed reservations for the same nights."
          if accepted > 1 else "CORRECT: one booking was accepted and one was refused.")


if __name__ == "__main__":
    main()
```

Java:

```text
// tools/RaceHttp.java
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
```

Go:

```text
// tools/race/main.go
// Sends two POST /reservations for the same room and the same nights at the same moment, and prints
// what each one got back and how many confirmed reservations the room ended up with.
//
//	go run ./tools/race                          both requests to http://localhost:5080
//	go run ./tools/race http://localhost:5081    Omar's request to the second instance
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
package main

import (
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"
	"sync"
	"time"
)

const maya = `{"guestName":"Maya","guestEmail":"maya@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}`
const omar = `{"guestName":"Omar","guestEmail":"omar@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}`

func book(base, body string) string {
	response, err := http.Post(base+"/reservations", "application/json", strings.NewReader(body))
	if err != nil {
		return fmt.Sprintf("the request failed: %v", err)
	}
	defer response.Body.Close()
	text, _ := io.ReadAll(response.Body)
	return reason(response.StatusCode) + string(text)
}

func reason(status int) string {
	switch status {
	case 201:
		return "201 Created   "
	case 409:
		return "409 Conflict  "
	case 400:
		return "400 Bad Request  "
	default:
		return fmt.Sprintf("%d  ", status)
	}
}

func main() {
	mayaBase := "http://localhost:5080"
	omarBase := mayaBase
	if len(os.Args) > 1 {
		omarBase = os.Args[1]
	}

	// One warm-up request first: see the note above main.
	if response, err := http.Get(mayaBase + "/stats"); err == nil {
		response.Body.Close()
	}

	fmt.Println("Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.")
	if omarBase != mayaBase {
		fmt.Printf("Maya -> %s   Omar -> %s\n", mayaBase, omarBase)
	}
	fmt.Println()

	answers := make([]string, 2)
	var waiting sync.WaitGroup
	waiting.Add(2)
	go func() {
		defer waiting.Done()
		answers[0] = book(mayaBase, maya)
	}()
	go func() {
		defer waiting.Done()
		time.Sleep(500 * time.Millisecond) // Omar clicks a moment after Maya
		answers[1] = book(omarBase, omar)
	}()
	waiting.Wait()

	fmt.Printf("Maya -> %s\n", answers[0])
	fmt.Printf("Omar -> %s\n", answers[1])
	fmt.Println()

	accepted := 0
	for _, answer := range answers {
		if strings.HasPrefix(answer, "201") {
			accepted++
		}
	}
	fmt.Printf("Confirmed reservations for R7 on 12-14 Nov: %d\n", accepted)
	if accepted > 1 {
		fmt.Println("DOUBLE BOOKED: the room has two confirmed reservations for the same nights.")
	} else {
		fmt.Println("CORRECT: one booking was accepted and one was refused.")
	}
}
```

JavaScript:

```text
// tools/race-http.js
// Sends two POST /reservations for the same room and the same nights at the same moment, and
// prints what each one got back and how many confirmed reservations the room ended up with.
//
//   node tools/race-http.js                          both requests to http://localhost:5080
//   node tools/race-http.js http://localhost:5081    Omar's request to the second instance
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
const maya = { guestName: 'Maya', guestEmail: 'maya@example.com', room: 'R7', checkIn: '2026-11-12', checkOut: '2026-11-14' };
const omar = { ...maya, guestName: 'Omar', guestEmail: 'omar@example.com' };
const reasons = { 201: '201 Created', 409: '409 Conflict', 400: '400 Bad Request' };
const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

async function book(base, body) {
  const response = await fetch(`${base}/reservations`, {
    method: 'POST', body: JSON.stringify(body),
    headers: { 'Content-Type': 'application/json', Connection: 'close' },
  });
  const padding = response.status === 201 ? '   ' : '  ';
  return `${reasons[response.status] ?? response.status}${padding}${await response.text()}`;
}

const mayaBase = 'http://localhost:5080';
const omarBase = process.argv[2] ?? mayaBase;

await fetch(`${mayaBase}/stats`); // the warm-up: see the note at the top

console.log('Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.');
if (omarBase !== mayaBase) console.log(`Maya -> ${mayaBase}   Omar -> ${omarBase}`);
console.log();

// Both requests are started before either is awaited: that is what "at the same moment" means here.
const mayaRequest = book(mayaBase, maya);
const omarRequest = wait(500).then(() => book(omarBase, omar)); // Omar clicks a moment after Maya
const [mayaAnswer, omarAnswer] = await Promise.all([mayaRequest, omarRequest]);

console.log(`Maya -> ${mayaAnswer}`);
console.log(`Omar -> ${omarAnswer}`);
console.log();

const accepted = [mayaAnswer, omarAnswer].filter(answer => answer.startsWith('201')).length;
console.log(`Confirmed reservations for R7 on 12-14 Nov: ${accepted}`);
console.log(accepted > 1
  ? 'DOUBLE BOOKED: the room has two confirmed reservations for the same nights.'
  : 'CORRECT: one booking was accepted and one was refused.');
```

Dart:

```text
// tool/race_http.dart
// Sends two POST /reservations for the same room and the same nights at the same moment, and prints
// what each one got back and how many confirmed reservations the room ended up with.
//
//   dart run tool/race_http.dart                          both requests to http://localhost:5080
//   dart run tool/race_http.dart http://localhost:5081    Omar's request to the second instance
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
import 'dart:convert';
import 'dart:io';

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
  'checkIn': '2026-11-12',
  'checkOut': '2026-11-14'
};
const reasons = {201: '201 Created   ', 409: '409 Conflict  ', 400: '400 Bad Request  '};

Future<String> book(HttpClient client, String base, Map<String, String> body) async {
  final request = await client.postUrl(Uri.parse('$base/reservations'));
  request.headers.contentType = ContentType.json;
  request.write(jsonEncode(body));
  final response = await request.close();
  final text = await response.transform(utf8.decoder).join();
  return '${reasons[response.statusCode] ?? '${response.statusCode}  '}$text';
}

Future<void> main(List<String> args) async {
  const mayaBase = 'http://localhost:5080';
  final omarBase = args.isNotEmpty ? args[0] : mayaBase;
  final client = HttpClient();

  // The warm-up: see the note at the top.
  final warmUp = await (await client.getUrl(Uri.parse('$mayaBase/stats'))).close();
  await warmUp.drain<void>();

  print('Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.');
  if (omarBase != mayaBase) print('Maya -> $mayaBase   Omar -> $omarBase');
  print('');

  // Both requests are started before either is awaited: that is what "at the same moment" means.
  final mayaRequest = book(client, mayaBase, maya);
  final omarRequest = Future<void>.delayed(const Duration(milliseconds: 500))
      .then((_) => book(client, omarBase, omar)); // Omar clicks a moment after Maya
  final answers = await Future.wait([mayaRequest, omarRequest]);
  client.close();

  print('Maya -> ${answers[0]}');
  print('Omar -> ${answers[1]}');
  print('');

  final accepted = answers.where((answer) => answer.startsWith('201')).length;
  print('Confirmed reservations for R7 on 12-14 Nov: $accepted');
  print(accepted > 1
      ? 'DOUBLE BOOKED: the room has two confirmed reservations for the same nights.'
      : 'CORRECT: one booking was accepted and one was refused.');
}
```

**What HOTEL_SLOW_STEP is for** In short: It makes every booking pause between checking and saving, so a gap that normally lasts microseconds is wide enough to aim at. The gap is always there. Without the pause you would have to send thousands of bookings to land in it by luck, and the lesson would depend on luck. With HOTEL_SLOW_STEP=1 the same race happens on every single run. Nothing about the pause creates the bug. Take it away and the bug is still there; you just cannot aim at it. Never leave it set on anything but your own laptop.

1. Start: the app is started with HOTEL_SLOW_STEP=1.
2. Action: a booking arrives. It checks for a clash, waits one second, then asks for an id and inserts.
3. Result: a second booking sent within that second passes its own check before the first one has saved anything.

C#:

```text
// HotelBooking/Program.cs
// P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
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
using StackExchange.Redis;

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
    // Step 1 widens the gap between the check and the save, so the race happens on every run.
    var slowStepMs = int.Parse(Environment.GetEnvironmentVariable("HOTEL_SLOW_STEP") ?? "0") * 1000;

    // One request: a fresh store over its own DbContext, counted for GET /stats; save what changed at the end.
    IResult UnitOfWork(Func<BookingService, IResult> work)
    {
        using var db = databases.CreateDbContext();
        var store = new PostgresReservationStore(db, rooms);
        var result = work(new BookingService(rooms, new CountingReservationStore(store, stats), email, slowStepMs));
        store.SaveChanges();
        return result;
    }

    HotelApi.Map(app, rooms, UnitOfWork, stats, cache);
    // Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
    app.Run($"http://localhost:{Environment.GetEnvironmentVariable("HOTEL_PORT") ?? "5080"}");
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

Python:

```text
# main.py
"""P09 - Hotel booking: the P08 app with the booking rule protected by PostgreSQL.

  python main.py             the HTTP API on http://localhost:5080 (HOTEL_PORT changes the port)
  python main.py --demo      the P06 demo, on a list in memory
  python main.py --race      the P06 race, on a list in memory

HOTEL_SLOW_STEP makes every booking wait that many seconds between checking and saving, which is
what makes the race in tools/race_http.py happen on every run instead of now and then.
"""
from __future__ import annotations

import os
import sys
import threading
import time
from contextlib import contextmanager
from datetime import date

import uvicorn
from redis import Redis
from sqlalchemy import create_engine
from sqlalchemy.orm import Session

from hotel_booking.api import CountingReservationStore, Stats, create_app
from hotel_booking.api_cache import AvailabilityCache, RedisKeyValueStore
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
    # The wiring for the web app: PostgreSQL and Redis from docker-compose.yml
    # (HOTEL_DB_PORT and HOTEL_REDIS_PORT only if 5432 or 6379 is taken).
    engine = create_engine(f"postgresql+psycopg://hotel:hotel@localhost:{os.getenv('HOTEL_DB_PORT', '5432')}/hotel")
    rooms = PostgresRoomCatalog(engine).all()
    email = EmailNotificationSender()
    stats = Stats()
    redis = Redis(host="localhost", port=int(os.getenv("HOTEL_REDIS_PORT", "6379")), decode_responses=True)
    redis.ping()  # stop here if Redis is not running
    cache = AvailabilityCache(RedisKeyValueStore(redis))
    # Step 1 widens the gap between the check and the save, so the race happens every run.
    slow_step = float(os.getenv("HOTEL_SLOW_STEP", "0"))

    @contextmanager
    def unit_of_work():
        # One request: a fresh store over its own session, counted for GET /stats; save what changed at the end.
        with Session(engine) as session:
            store = PostgresReservationStore(session, rooms)
            yield BookingService(rooms, CountingReservationStore(store, stats), email, slow_step_seconds=slow_step)
            store.save_changes()

    # Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
    uvicorn.run(create_app(rooms, unit_of_work, stats, cache),
                host="localhost", port=int(os.getenv("HOTEL_PORT", "5080")))


if __name__ == "__main__":
    if "--race" in sys.argv:
        run_race()
    elif "--demo" in sys.argv:
        run_demo()
    else:
        run_api()
```

Java:

```text
// src/main/java/hotelbooking/Main.java
// P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
// mvn -q spring-boot:run                              -> the API on http://localhost:5080 (first: docker compose up -d)
// java -jar target/hotel-booking-1.0.jar --demo       -> the P06 demo, with the data in lists (after mvn -q package)
// java -jar target/hotel-booking-1.0.jar --race       -> two guests book the same room at the same moment
package hotelbooking;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import hotelbooking.api.AvailabilityCache;
import hotelbooking.api.CountingReservationStore;
import hotelbooking.api.HotelApi;
import hotelbooking.api.Stats;
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
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.support.TransactionTemplate;

// Spring Boot starts the web server and connects to PostgreSQL and Redis (application.properties);
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
    HotelApi hotelApi(EntityManager db, TransactionTemplate transactions, StringRedisTemplate redis) {
        List<Room> rooms = new PostgresRoomCatalog(db).all();
        EmailNotificationSender email = new EmailNotificationSender();
        Stats stats = new Stats();
        try (RedisConnection connection = redis.getRequiredConnectionFactory().getConnection()) {
            connection.ping(); // stop here if Redis is not running
        }
        AvailabilityCache cache = new AvailabilityCache(new AvailabilityCache.RedisKeyValueStore(redis));
        // Step 1 widens the gap between the check and the save, so the race happens on every run.
        int slowStepMs = (int) (Double.parseDouble(System.getenv().getOrDefault("HOTEL_SLOW_STEP", "0")) * 1000);
        // One request: a fresh store in its own transaction, counted for GET /stats; save what changed at the end.
        HotelApi.UnitOfWork unitOfWork = work -> transactions.execute(transaction -> {
            PostgresReservationStore store = new PostgresReservationStore(db, rooms);
            var result = work.apply(new BookingService(rooms, new CountingReservationStore(store, stats), email, slowStepMs));
            if (store.wasRefused()) {
                // The booking rule refused a save, so PostgreSQL has aborted this transaction and
                // no further statement in it can run. Nothing should be written anyway: roll back
                // and let the endpoint answer 409.
                transaction.setRollbackOnly();
                return result;
            }
            store.saveChanges();
            return result;
        });
        return new HotelApi(rooms, unitOfWork, stats, cache);
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

Go:

```text
// main.go
// P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
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
	"strconv"
	"strings"
	"sync"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/redis/go-redis/v9"

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
	// Step 1 widens the gap between the check and the save, so the race happens on every run.
	slowStepSeconds, _ := strconv.ParseFloat(cmp.Or(os.Getenv("HOTEL_SLOW_STEP"), "0"), 64)
	slowStep := time.Duration(slowStepSeconds * float64(time.Second))

	// One request: a fresh store, counted for GET /stats; save what changed at the end.
	unitOfWork := func(work func(service *reservations.BookingService)) error {
		store := reservations.NewPostgresReservationStore(ctx, pool, hotelRooms)
		work(reservations.NewBookingService(hotelRooms, api.NewCountingReservationStore(store, stats), email, slowStep))
		return store.SaveChanges()
	}

	// Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
	address := "localhost:" + cmp.Or(os.Getenv("HOTEL_PORT"), "5080")
	fmt.Println("Listening on http://" + address)
	log.Fatal(http.ListenAndServe(address, api.New(hotelRooms, unitOfWork, stats, cache)))
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

JavaScript:

```text
// main.js
// P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
// node main.js          -> the API on http://localhost:5080 (first: docker compose up -d)
// node main.js --demo   -> the P06 demo, with the data in lists
// node main.js --race   -> two guests book the same room at the same moment
import pg from 'pg';
import { createClient } from 'redis';
import { CountingReservationStore, Stats, createApi } from './src/api.js';
import { AvailabilityCache, RedisKeyValueStore } from './src/api-cache.js';
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
  // Step 1 widens the gap between the check and the save, so the race happens on every run.
  const slowStepMs = Number(process.env.HOTEL_SLOW_STEP ?? 0) * 1000;

  // One request: a fresh store, counted for GET /stats; save what changed at the end.
  const unitOfWork = async work => {
    const store = new PostgresReservationStore(pool, rooms);
    const result = await work(new BookingService(rooms, new CountingReservationStore(store, stats), email, slowStepMs));
    await store.saveChanges();
    return result;
  };

  // Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
  const port = Number(process.env.HOTEL_PORT ?? 5080);
  createApi(rooms, unitOfWork, stats, cache).listen(port, 'localhost', () => console.log(`Listening on http://localhost:${port}`));
}

if (process.argv.includes('--race')) runRace();
else if (process.argv.includes('--demo')) runDemo();
else runApi();
```

Dart:

```text
// bin/main.dart
// P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
// dart run bin/main.dart          -> the API on http://localhost:5080 (first: docker compose up -d)
// dart run bin/main.dart --demo   -> the P06 demo, with the data in lists
// dart run bin/main.dart --race   -> two guests book the same room at the same moment
import 'dart:async';
import 'dart:io';

import 'package:hotel_booking/api.dart';
import 'package:hotel_booking/api_cache.dart';
import 'package:hotel_booking/notifications/src/email.dart';
import 'package:hotel_booking/reservations/reservations.dart';
import 'package:hotel_booking/reservations/src/postgres_store.dart';
import 'package:hotel_booking/reservations/src/store.dart';
import 'package:hotel_booking/rooms/rooms.dart';
import 'package:hotel_booking/rooms/src/catalog.dart';
import 'package:hotel_booking/rooms/src/postgres_catalog.dart';
import 'package:postgres/postgres.dart' show Endpoint, Pool, PoolSettings, SslMode;
import 'package:redis/redis.dart' show RedisConnection;
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
  // Step 1 widens the gap between the check and the save, so the race happens on every run.
  final slowStep =
      Duration(milliseconds: (double.parse(Platform.environment['HOTEL_SLOW_STEP'] ?? '0') * 1000).round());

  // One request: a fresh store, counted for GET /stats; save what changed at the end.
  Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async {
    final store = PostgresReservationStore(pool, rooms);
    final result = await work(
        BookingService(rooms, CountingReservationStore(store, stats), email, slowStep: slowStep));
    await store.saveChanges();
    return result;
  }

  // Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
  final apiPort = int.parse(Platform.environment['HOTEL_PORT'] ?? '5080');
  await serve(createApi(rooms, unitOfWork, stats, cache), 'localhost', apiPort);
  print('Listening on http://localhost:$apiPort');
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

### Completed example 1.1 · the database gets a sequence

Reservation ids used to be “R” plus the number of rows, and two requests that counted before either had saved both got R101 — so the second INSERT failed on the primary key instead of on the booking rule. That is a second race, and it hides the one this practice is about. Put it in db/init.sql and in the database that is already running.

**Why the reservation id needed fixing too** In short: Ids came from counting the rows, so two requests that counted before either had saved both got R101 — a second race hiding behind the first. It shows up in Java first, because Spring wraps each request in one transaction, so an uncommitted row is invisible to the other request’s count. The other five languages commit each statement as it is made and happened to dodge it. With the count-based id the second INSERT failed on the primary key instead of on the booking rule — the wrong error, for the wrong reason, hiding the thing the lesson is about. A sequence never hands the same number to two callers, and never waits for or rolls back with the transaction that asked. Because a sequence does not roll back, running the race tool twice gives R103 and R104. Reset the database if you want the ids in the slides.

1. Start: two bookings arrive at the same moment, and neither has committed.
2. Action: each asks SELECT nextval('reservation_ids').
3. Result: one gets 101 and the other 102. Nobody waits, and the numbers are never the same.

sql:

```text
-- P09 Step 1: reservation ids come from here, not from counting the rows.
-- nextval never returns the same number twice, even to two transactions running at the same moment.
CREATE SEQUENCE reservation_ids START 101;

-- The running database needs it too, because init.sql only runs when the database is created.
-- Start it above the ids that already exist, so it never collides with them.
SELECT setval('reservation_ids', GREATEST(101, (SELECT count(*) FROM reservations) + 101), false);
```

### In psql, and in db/init.sql

```text
-- P09 Step 1: reservation ids come from here, not from counting the rows.
-- nextval never returns the same number twice, even to two transactions running at the same moment.
CREATE SEQUENCE reservation_ids START 101;

-- The running database needs it too, because init.sql only runs when the database is created.
-- Start it above the ids that already exist, so it never collides with them.
SELECT setval('reservation_ids', GREATEST(101, (SELECT count(*) FROM reservations) + 101), false);
```

### Completed example 1.2 · the store hands out the id

Only the store can give out an id that no other request can also be given. The contract gains one method, and both stores answer it their own way.

**What the store now promises** In short: save refuses an overlapping reservation rather than writing one. Every store keeps that promise; how is its own business. The list store keeps it with a check under a lock. PostgreSQL keeps it with the exclusion constraint. Because both keep the same promise, the two new tests need no database and still test the rule the constraint protects. The pre-check in reserve was deliberately kept in front of it. It answers the ordinary case with a readable 409; the promise is what makes the rule true when two requests check at the same moment.

1. Start: R7 has a confirmed reservation for 12–14 November.
2. Action: something calls save directly with an overlapping reservation, skipping the pre-check.
3. Result: save raises OverlapRejected, and nothing is written.

C#:

```text
// HotelBooking/Reservations/IReservationStore.cs
using HotelBooking.Rooms;

namespace HotelBooking.Reservations;

// What BookingService needs from storage. Today a list in memory; from P07 a database.
public interface IReservationStore
{
    int Count { get; }
    bool HasOverlap(Room room, DateRange stay);

    // The id for the next reservation.
    //
    // The store owns this, because only the store can hand out an id that no other request can
    // also be given. Counting the rows and adding one cannot: two requests that count before
    // either has saved both get R101, and the second INSERT then fails on the primary key instead
    // of on the booking rule. PostgreSQL answers from a sequence; the list store counts, which is
    // safe there because it holds a lock.
    string NextReservationId();

    // Writes the reservation.
    //
    // Throws OverlapRejectedException if writing it would give the room two Confirmed reservations
    // covering the same night. The pre-check in Reserve usually catches that first; this promise is
    // what makes the rule true when two requests check at the same moment.
    void Save(Reservation reservation);

    Reservation? Find(string id);
    int CountConfirmed(Room room, DateRange stay);
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


class OverlapRejected(Exception):
    """The store refused a save because it would give the room two Confirmed reservations
    covering the same night.

    Every store promises this, and the promise is part of the ReservationStore contract. How a
    store keeps it is its own business: the list store checks inside save, and PostgreSQL refuses
    the INSERT with the exclusion constraint reservations_no_overlap. BookingService turns it into
    Unavailable - the same answer the pre-check gives - so a guest never sees a database error.
    """


class ReservationStore(Protocol):
    """What BookingService needs from storage. Today a list in memory; from P07 a database."""

    def count(self) -> int: ...
    def has_overlap(self, room: Room, stay: DateRange) -> bool: ...

    def next_reservation_id(self) -> str:
        """The id for the next reservation.

        The store owns this, because only the store can hand out an id that no other request can
        also be given. Counting the rows and adding one cannot: two requests that count before
        either has saved both get R101, and the second INSERT then fails on the primary key instead
        of on the booking rule. PostgreSQL answers from a sequence; the list store counts, which is
        safe there because it holds a lock.
        """
        ...

    def save(self, reservation: Reservation) -> None:
        """Writes the reservation.

        Raises OverlapRejected if writing it would give the room two Confirmed reservations
        covering the same night. The pre-check in reserve() usually catches that first; this
        promise is what makes the rule true when two requests check at the same moment.
        """
        ...

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

        time.sleep(self.slow_step_seconds)  # 0 normally; the race tool makes this step slow

        reservation = Reservation(self.store.next_reservation_id(), guest, room, stay)
        try:
            self.store.save(reservation)
        except OverlapRejected:
            # Another request saved an overlapping reservation after our check and before our save.
            # The check was honest when it ran; it had simply stopped being true. The guest gets the
            # same answer either way.
            return BookingResult("Unavailable")
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
// src/main/java/hotelbooking/reservations/ReservationStore.java
package hotelbooking.reservations;

import hotelbooking.rooms.Room;

// What BookingService needs from storage. Today a list in memory; from P07 a database.
public interface ReservationStore {
    int count();

    boolean hasOverlap(Room room, DateRange stay);

    // The id for the next reservation.
    //
    // The store owns this, because only the store can hand out an id that no other request can
    // also be given. Counting the rows and adding one cannot: two requests that count before either
    // has saved both get R101, and the second INSERT then fails on the primary key instead of on
    // the booking rule. That is exactly what happens here, because Spring wraps each request in one
    // transaction, so an uncommitted row is invisible to the other request's count.
    // PostgreSQL answers from a sequence; the list store counts, which is safe there because it
    // holds a lock.
    String nextReservationId();

    // Writes the reservation.
    //
    // Throws OverlapRejectedException if writing it would give the room two Confirmed reservations
    // covering the same night. The pre-check in reserve usually catches that first; this promise is
    // what makes the rule true when two requests check at the same moment.
    void save(Reservation reservation);

    // Returns null when no reservation has that id.
    Reservation find(String id);

    int countConfirmed(Room room, DateRange stay);
}
```

Go:

```text
// reservations/booking.go
package reservations

import (
	"errors"
	"time"

	"hotelbooking/notifications"
	"hotelbooking/rooms"
)

// ErrOverlapRejected is returned by Save when writing a reservation would give the room two
// Confirmed reservations covering the same night.
//
// Every store promises this, and the promise is part of ReservationStore. How a store keeps it is
// its own business: the list store checks inside Save, and PostgreSQL refuses the INSERT with the
// exclusion constraint reservations_no_overlap. BookingService turns it into Unavailable - the same
// answer the pre-check gives - so a guest never sees a database error.
var ErrOverlapRejected = errors.New("the room already has a confirmed reservation for these nights")

// ReservationStore is what BookingService needs from storage.
// Today a list in memory; from P07 a database.
type ReservationStore interface {
	Count() int
	HasOverlap(room rooms.Room, stay DateRange) bool

	// NextReservationID is the id for the next reservation.
	//
	// The store owns this, because only the store can hand out an id that no other request can
	// also be given. Counting the rows and adding one cannot: two requests that count before
	// either has saved both get R101, and the second INSERT then fails on the primary key instead
	// of on the booking rule. PostgreSQL answers from a sequence; the list store counts, which is
	// safe there because it holds the mutex.
	NextReservationID() string

	// Save writes the reservation. It returns an error wrapping ErrOverlapRejected if writing it
	// would give the room two Confirmed reservations covering the same night. The pre-check in
	// Reserve usually catches that first; this promise is what makes the rule true when two
	// requests check at the same moment.
	//
	// Any other kind of failure is not returned here: the store keeps it and SaveChanges reports
	// it, the same as in P07.
	Save(reservation *Reservation) error

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

	time.Sleep(service.slowStep) // zero normally; the race tool makes this step slow

	reservation := NewReservation(service.store.NextReservationID(), guest, room, stay)
	if err := service.store.Save(reservation); errors.Is(err, ErrOverlapRejected) {
		// Another request saved an overlapping reservation after our check and before our save.
		// The check was honest when it ran; it had simply stopped being true. The guest gets the
		// same answer either way.
		return BookingResult{Outcome: "Unavailable"}
	}
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
// src/reservations/internal/store.js
import { OverlapRejected } from '../booking.js';

// What BookingService needs from storage. JavaScript has no interface keyword, so the
// contract is written down here and every store keeps to it:
//
//   count                       how many reservations are saved
//   hasOverlap(room, stay)      is a Confirmed reservation already using those nights?
//   nextReservationId()         the id for the next reservation. The store owns this, because only
//                               the store can hand out an id no other request can also be given:
//                               counting the rows and adding one cannot, since two requests that
//                               count before either has saved both get R101
//   save(reservation)           keep this reservation; throws OverlapRejected rather than
//                               giving the room two Confirmed reservations for the same night
//   find(id)                    the reservation with that id, or null
//   countConfirmed(room, stay)  how many Confirmed reservations use those nights
//
// A store may answer count, hasOverlap, save and find with a Promise (a database does);
// BookingService awaits them. This store keeps a list in memory and answers at once.
export class InMemoryReservationStore {
  #rows = [];

  get count() {
    return this.#rows.length;
  }

  hasOverlap(room, stay) {
    return this.#rows.some(row => row.blocks(room, stay));
  }

  // Safe here for the same reason save is: nothing else can run between the read and the use.
  nextReservationId() {
    return `R${101 + this.#rows.length}`;
  }

  // No lock, and none is needed: Node runs one request's code at a time, and there is no await
  // between the check and the push, so no other request can run in the middle of this method.
  // This check is the list store's version of the exclusion constraint.
  save(reservation) {
    if (this.hasOverlap(reservation.room, reservation.stay)) {
      throw new OverlapRejected(`${reservation.room.number} already has a confirmed reservation for these nights.`);
    }
    this.#rows.push(reservation);
  }

  // Returns null when no reservation has that id.
  find(id) {
    return this.#rows.find(row => row.id === id) ?? null;
  }

  countConfirmed(room, stay) {
    return this.#rows.filter(row => row.blocks(room, stay)).length;
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

/// The store refused a save because it would give the room two Confirmed reservations covering the
/// same night.
///
/// Every store promises this, and the promise is part of [ReservationStore]. How a store keeps it
/// is its own business: the list store checks inside save, and PostgreSQL refuses the INSERT with
/// the exclusion constraint reservations_no_overlap. BookingService turns it into Unavailable - the
/// same answer the pre-check gives - so a guest never sees a database error.
class OverlapRejected implements Exception {
  OverlapRejected(this.message);

  final String message;

  @override
  String toString() => 'OverlapRejected: $message';
}

// What BookingService needs from storage: a list in memory answers at once, a database
// answers later (a Future). FutureOr allows both, and BookingService awaits the answer.
abstract interface class ReservationStore {
  FutureOr<int> get count;

  FutureOr<bool> hasOverlap(Room room, DateRange stay);

  /// The id for the next reservation.
  ///
  /// The store owns this, because only the store can hand out an id that no other request can
  /// also be given. Counting the rows and adding one cannot: two requests that count before either
  /// has saved both get R101, and the second INSERT then fails on the primary key instead of on the
  /// booking rule. PostgreSQL answers from a sequence; the list store counts, which is safe there
  /// because nothing can run in the middle of it.
  FutureOr<String> nextReservationId();

  /// Writes the reservation.
  ///
  /// Throws [OverlapRejected] if writing it would give the room two Confirmed reservations
  /// covering the same night. The pre-check in reserve usually catches that first; this promise is
  /// what makes the rule true when two requests check at the same moment.
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

    final reservation = Reservation(await store.nextReservationId(), guest, room, stay);
    try {
      await store.save(reservation);
    } on OverlapRejected {
      // Another request saved an overlapping reservation after our check and before our save. The
      // check was honest when it ran; it had simply stopped being true.
      return BookingResult('Unavailable');
    }
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

### The contract, and both stores’ answers

#### C#

```csharp
// Safe here for the same reason Save is: it holds the same lock.
public string NextReservationId()
{
    lock (_gate) return $"R{101 + _rows.Count}";
}

// nextval never hands the same number to two callers, and it does not wait for, or roll back
// with, the transaction that asked for it. That is exactly what an id needs.
// EF Core's SqlQuery<T> reads a scalar from a column it expects to be called "Value".
public string NextReservationId() =>
    $"R{db.Database.SqlQuery<long>($"SELECT nextval('reservation_ids') AS \"Value\"").Single()}";
```

#### Python

```python
def next_reservation_id(self) -> str:
    with self._lock:
        return f"R{101 + len(self._rows)}"

def next_reservation_id(self) -> str:
    with self._lock:
        return f"R{101 + len(self._rows)}"
```

#### Java

```java
// Safe here for the same reason save is: it holds the same lock.
@Override
public String nextReservationId() {
    synchronized (gate) {
        return "R" + (101 + rows.size());
    }
}

// nextval never hands the same number to two callers, and it does not wait for, or roll back
// with, the transaction that asked for it. That is exactly what an id needs, and it is what
// stops two requests in this language both being given R101.
@Override
public String nextReservationId() {
    Number next = (Number) db.createNativeQuery("SELECT nextval('reservation_ids')").getSingleResult();
    return "R" + next.longValue();
}
```

#### Go

```go
// NextReservationID is safe here for the same reason Save is: it holds the same mutex.
func (store *InMemoryReservationStore) NextReservationID() string {
	store.mutex.Lock()
	defer store.mutex.Unlock()
	return fmt.Sprintf("R%d", 101+len(store.rows))
}

// NextReservationID takes the id from the sequence. nextval never hands the same number to two
// callers, and it does not wait for, or roll back with, the transaction that asked for it.
func (store *PostgresReservationStore) NextReservationID() string {
	var next int64
	if err := store.pool.QueryRow(store.ctx, "SELECT nextval('reservation_ids')").Scan(&next); err != nil {
		store.err = cmp.Or(store.err, err)
		return ""
	}
	return fmt.Sprintf("R%d", next)
}
```

#### JavaScript

```javascript
// Safe here for the same reason save is: nothing else can run between the read and the use.
nextReservationId() {
  return `R${101 + this.#rows.length}`;
}

// nextval never hands the same number to two callers, and it does not wait for, or roll back
// with, the transaction that asked for it. That is exactly what an id needs.
async nextReservationId() {
  const { rows } = await this.#pool.query("SELECT nextval('reservation_ids') AS id");
  return `R${rows[0].id}`;
}
```

#### Dart

```dart
// Safe here for the same reason save is: nothing else can run between the read and the use.
@override
String nextReservationId() => 'R${101 + _rows.length}';

// nextval never hands the same number to two callers, and it does not wait for, or roll back
// with, the transaction that asked for it. That is exactly what an id needs.
@override
Future<String> nextReservationId() async {
  final rows = await _pool.execute("SELECT nextval('reservation_ids')");
  return 'R${rows.first.first}';
}
```

### Completed example 1.3 · the wiring reads HOTEL_SLOW_STEP and HOTEL_PORT

**What HOTEL_SLOW_STEP is for** In short: It makes every booking pause between checking and saving, so a gap that normally lasts microseconds is wide enough to aim at. The gap is always there. Without the pause you would have to send thousands of bookings to land in it by luck, and the lesson would depend on luck. With HOTEL_SLOW_STEP=1 the same race happens on every single run. Nothing about the pause creates the bug. Take it away and the bug is still there; you just cannot aim at it. Never leave it set on anything but your own laptop.

1. Start: the app is started with HOTEL_SLOW_STEP=1.
2. Action: a booking arrives. It checks for a clash, waits one second, then asks for an id and inserts.
3. Result: a second booking sent within that second passes its own check before the first one has saved anything.

C#:

```text
// HotelBooking/Program.cs
// P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
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
using StackExchange.Redis;

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
    // Step 1 widens the gap between the check and the save, so the race happens on every run.
    var slowStepMs = int.Parse(Environment.GetEnvironmentVariable("HOTEL_SLOW_STEP") ?? "0") * 1000;

    // One request: a fresh store over its own DbContext, counted for GET /stats; save what changed at the end.
    IResult UnitOfWork(Func<BookingService, IResult> work)
    {
        using var db = databases.CreateDbContext();
        var store = new PostgresReservationStore(db, rooms);
        var result = work(new BookingService(rooms, new CountingReservationStore(store, stats), email, slowStepMs));
        store.SaveChanges();
        return result;
    }

    HotelApi.Map(app, rooms, UnitOfWork, stats, cache);
    // Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
    app.Run($"http://localhost:{Environment.GetEnvironmentVariable("HOTEL_PORT") ?? "5080"}");
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

Python:

```text
# main.py
"""P09 - Hotel booking: the P08 app with the booking rule protected by PostgreSQL.

  python main.py             the HTTP API on http://localhost:5080 (HOTEL_PORT changes the port)
  python main.py --demo      the P06 demo, on a list in memory
  python main.py --race      the P06 race, on a list in memory

HOTEL_SLOW_STEP makes every booking wait that many seconds between checking and saving, which is
what makes the race in tools/race_http.py happen on every run instead of now and then.
"""
from __future__ import annotations

import os
import sys
import threading
import time
from contextlib import contextmanager
from datetime import date

import uvicorn
from redis import Redis
from sqlalchemy import create_engine
from sqlalchemy.orm import Session

from hotel_booking.api import CountingReservationStore, Stats, create_app
from hotel_booking.api_cache import AvailabilityCache, RedisKeyValueStore
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
    # The wiring for the web app: PostgreSQL and Redis from docker-compose.yml
    # (HOTEL_DB_PORT and HOTEL_REDIS_PORT only if 5432 or 6379 is taken).
    engine = create_engine(f"postgresql+psycopg://hotel:hotel@localhost:{os.getenv('HOTEL_DB_PORT', '5432')}/hotel")
    rooms = PostgresRoomCatalog(engine).all()
    email = EmailNotificationSender()
    stats = Stats()
    redis = Redis(host="localhost", port=int(os.getenv("HOTEL_REDIS_PORT", "6379")), decode_responses=True)
    redis.ping()  # stop here if Redis is not running
    cache = AvailabilityCache(RedisKeyValueStore(redis))
    # Step 1 widens the gap between the check and the save, so the race happens every run.
    slow_step = float(os.getenv("HOTEL_SLOW_STEP", "0"))

    @contextmanager
    def unit_of_work():
        # One request: a fresh store over its own session, counted for GET /stats; save what changed at the end.
        with Session(engine) as session:
            store = PostgresReservationStore(session, rooms)
            yield BookingService(rooms, CountingReservationStore(store, stats), email, slow_step_seconds=slow_step)
            store.save_changes()

    # Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
    uvicorn.run(create_app(rooms, unit_of_work, stats, cache),
                host="localhost", port=int(os.getenv("HOTEL_PORT", "5080")))


if __name__ == "__main__":
    if "--race" in sys.argv:
        run_race()
    elif "--demo" in sys.argv:
        run_demo()
    else:
        run_api()
```

Java:

```text
// src/main/java/hotelbooking/Main.java
// P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
// mvn -q spring-boot:run                              -> the API on http://localhost:5080 (first: docker compose up -d)
// java -jar target/hotel-booking-1.0.jar --demo       -> the P06 demo, with the data in lists (after mvn -q package)
// java -jar target/hotel-booking-1.0.jar --race       -> two guests book the same room at the same moment
package hotelbooking;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import hotelbooking.api.AvailabilityCache;
import hotelbooking.api.CountingReservationStore;
import hotelbooking.api.HotelApi;
import hotelbooking.api.Stats;
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
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.support.TransactionTemplate;

// Spring Boot starts the web server and connects to PostgreSQL and Redis (application.properties);
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
    HotelApi hotelApi(EntityManager db, TransactionTemplate transactions, StringRedisTemplate redis) {
        List<Room> rooms = new PostgresRoomCatalog(db).all();
        EmailNotificationSender email = new EmailNotificationSender();
        Stats stats = new Stats();
        try (RedisConnection connection = redis.getRequiredConnectionFactory().getConnection()) {
            connection.ping(); // stop here if Redis is not running
        }
        AvailabilityCache cache = new AvailabilityCache(new AvailabilityCache.RedisKeyValueStore(redis));
        // Step 1 widens the gap between the check and the save, so the race happens on every run.
        int slowStepMs = (int) (Double.parseDouble(System.getenv().getOrDefault("HOTEL_SLOW_STEP", "0")) * 1000);
        // One request: a fresh store in its own transaction, counted for GET /stats; save what changed at the end.
        HotelApi.UnitOfWork unitOfWork = work -> transactions.execute(transaction -> {
            PostgresReservationStore store = new PostgresReservationStore(db, rooms);
            var result = work.apply(new BookingService(rooms, new CountingReservationStore(store, stats), email, slowStepMs));
            if (store.wasRefused()) {
                // The booking rule refused a save, so PostgreSQL has aborted this transaction and
                // no further statement in it can run. Nothing should be written anyway: roll back
                // and let the endpoint answer 409.
                transaction.setRollbackOnly();
                return result;
            }
            store.saveChanges();
            return result;
        });
        return new HotelApi(rooms, unitOfWork, stats, cache);
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

Go:

```text
// main.go
// P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
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
	"strconv"
	"strings"
	"sync"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/redis/go-redis/v9"

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
	// Step 1 widens the gap between the check and the save, so the race happens on every run.
	slowStepSeconds, _ := strconv.ParseFloat(cmp.Or(os.Getenv("HOTEL_SLOW_STEP"), "0"), 64)
	slowStep := time.Duration(slowStepSeconds * float64(time.Second))

	// One request: a fresh store, counted for GET /stats; save what changed at the end.
	unitOfWork := func(work func(service *reservations.BookingService)) error {
		store := reservations.NewPostgresReservationStore(ctx, pool, hotelRooms)
		work(reservations.NewBookingService(hotelRooms, api.NewCountingReservationStore(store, stats), email, slowStep))
		return store.SaveChanges()
	}

	// Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
	address := "localhost:" + cmp.Or(os.Getenv("HOTEL_PORT"), "5080")
	fmt.Println("Listening on http://" + address)
	log.Fatal(http.ListenAndServe(address, api.New(hotelRooms, unitOfWork, stats, cache)))
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

JavaScript:

```text
// main.js
// P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
// node main.js          -> the API on http://localhost:5080 (first: docker compose up -d)
// node main.js --demo   -> the P06 demo, with the data in lists
// node main.js --race   -> two guests book the same room at the same moment
import pg from 'pg';
import { createClient } from 'redis';
import { CountingReservationStore, Stats, createApi } from './src/api.js';
import { AvailabilityCache, RedisKeyValueStore } from './src/api-cache.js';
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
  // Step 1 widens the gap between the check and the save, so the race happens on every run.
  const slowStepMs = Number(process.env.HOTEL_SLOW_STEP ?? 0) * 1000;

  // One request: a fresh store, counted for GET /stats; save what changed at the end.
  const unitOfWork = async work => {
    const store = new PostgresReservationStore(pool, rooms);
    const result = await work(new BookingService(rooms, new CountingReservationStore(store, stats), email, slowStepMs));
    await store.saveChanges();
    return result;
  };

  // Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
  const port = Number(process.env.HOTEL_PORT ?? 5080);
  createApi(rooms, unitOfWork, stats, cache).listen(port, 'localhost', () => console.log(`Listening on http://localhost:${port}`));
}

if (process.argv.includes('--race')) runRace();
else if (process.argv.includes('--demo')) runDemo();
else runApi();
```

Dart:

```text
// bin/main.dart
// P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
// dart run bin/main.dart          -> the API on http://localhost:5080 (first: docker compose up -d)
// dart run bin/main.dart --demo   -> the P06 demo, with the data in lists
// dart run bin/main.dart --race   -> two guests book the same room at the same moment
import 'dart:async';
import 'dart:io';

import 'package:hotel_booking/api.dart';
import 'package:hotel_booking/api_cache.dart';
import 'package:hotel_booking/notifications/src/email.dart';
import 'package:hotel_booking/reservations/reservations.dart';
import 'package:hotel_booking/reservations/src/postgres_store.dart';
import 'package:hotel_booking/reservations/src/store.dart';
import 'package:hotel_booking/rooms/rooms.dart';
import 'package:hotel_booking/rooms/src/catalog.dart';
import 'package:hotel_booking/rooms/src/postgres_catalog.dart';
import 'package:postgres/postgres.dart' show Endpoint, Pool, PoolSettings, SslMode;
import 'package:redis/redis.dart' show RedisConnection;
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
  // Step 1 widens the gap between the check and the save, so the race happens on every run.
  final slowStep =
      Duration(milliseconds: (double.parse(Platform.environment['HOTEL_SLOW_STEP'] ?? '0') * 1000).round());

  // One request: a fresh store, counted for GET /stats; save what changed at the end.
  Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async {
    final store = PostgresReservationStore(pool, rooms);
    final result = await work(
        BookingService(rooms, CountingReservationStore(store, stats), email, slowStep: slowStep));
    await store.saveChanges();
    return result;
  }

  // Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
  final apiPort = int.parse(Platform.environment['HOTEL_PORT'] ?? '5080');
  await serve(createApi(rooms, unitOfWork, stats, cache), 'localhost', apiPort);
  print('Listening on http://localhost:$apiPort');
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

### Your wiring file

#### C#

```csharp
// HotelBooking/Program.cs
// P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
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
using StackExchange.Redis;

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
    // Step 1 widens the gap between the check and the save, so the race happens on every run.
    var slowStepMs = int.Parse(Environment.GetEnvironmentVariable("HOTEL_SLOW_STEP") ?? "0") * 1000;

    // One request: a fresh store over its own DbContext, counted for GET /stats; save what changed at the end.
    IResult UnitOfWork(Func<BookingService, IResult> work)
    {
        using var db = databases.CreateDbContext();
        var store = new PostgresReservationStore(db, rooms);
        var result = work(new BookingService(rooms, new CountingReservationStore(store, stats), email, slowStepMs));
        store.SaveChanges();
        return result;
    }

    HotelApi.Map(app, rooms, UnitOfWork, stats, cache);
    // Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
    app.Run($"http://localhost:{Environment.GetEnvironmentVariable("HOTEL_PORT") ?? "5080"}");
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
"""P09 - Hotel booking: the P08 app with the booking rule protected by PostgreSQL.

  python main.py             the HTTP API on http://localhost:5080 (HOTEL_PORT changes the port)
  python main.py --demo      the P06 demo, on a list in memory
  python main.py --race      the P06 race, on a list in memory

HOTEL_SLOW_STEP makes every booking wait that many seconds between checking and saving, which is
what makes the race in tools/race_http.py happen on every run instead of now and then.
"""
from __future__ import annotations

import os
import sys
import threading
import time
from contextlib import contextmanager
from datetime import date

import uvicorn
from redis import Redis
from sqlalchemy import create_engine
from sqlalchemy.orm import Session

from hotel_booking.api import CountingReservationStore, Stats, create_app
from hotel_booking.api_cache import AvailabilityCache, RedisKeyValueStore
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
    # The wiring for the web app: PostgreSQL and Redis from docker-compose.yml
    # (HOTEL_DB_PORT and HOTEL_REDIS_PORT only if 5432 or 6379 is taken).
    engine = create_engine(f"postgresql+psycopg://hotel:hotel@localhost:{os.getenv('HOTEL_DB_PORT', '5432')}/hotel")
    rooms = PostgresRoomCatalog(engine).all()
    email = EmailNotificationSender()
    stats = Stats()
    redis = Redis(host="localhost", port=int(os.getenv("HOTEL_REDIS_PORT", "6379")), decode_responses=True)
    redis.ping()  # stop here if Redis is not running
    cache = AvailabilityCache(RedisKeyValueStore(redis))
    # Step 1 widens the gap between the check and the save, so the race happens every run.
    slow_step = float(os.getenv("HOTEL_SLOW_STEP", "0"))

    @contextmanager
    def unit_of_work():
        # One request: a fresh store over its own session, counted for GET /stats; save what changed at the end.
        with Session(engine) as session:
            store = PostgresReservationStore(session, rooms)
            yield BookingService(rooms, CountingReservationStore(store, stats), email, slow_step_seconds=slow_step)
            store.save_changes()

    # Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
    uvicorn.run(create_app(rooms, unit_of_work, stats, cache),
                host="localhost", port=int(os.getenv("HOTEL_PORT", "5080")))


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
// P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
// mvn -q spring-boot:run                              -> the API on http://localhost:5080 (first: docker compose up -d)
// java -jar target/hotel-booking-1.0.jar --demo       -> the P06 demo, with the data in lists (after mvn -q package)
// java -jar target/hotel-booking-1.0.jar --race       -> two guests book the same room at the same moment
package hotelbooking;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import hotelbooking.api.AvailabilityCache;
import hotelbooking.api.CountingReservationStore;
import hotelbooking.api.HotelApi;
import hotelbooking.api.Stats;
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
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.support.TransactionTemplate;

// Spring Boot starts the web server and connects to PostgreSQL and Redis (application.properties);
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
    HotelApi hotelApi(EntityManager db, TransactionTemplate transactions, StringRedisTemplate redis) {
        List<Room> rooms = new PostgresRoomCatalog(db).all();
        EmailNotificationSender email = new EmailNotificationSender();
        Stats stats = new Stats();
        try (RedisConnection connection = redis.getRequiredConnectionFactory().getConnection()) {
            connection.ping(); // stop here if Redis is not running
        }
        AvailabilityCache cache = new AvailabilityCache(new AvailabilityCache.RedisKeyValueStore(redis));
        // Step 1 widens the gap between the check and the save, so the race happens on every run.
        int slowStepMs = (int) (Double.parseDouble(System.getenv().getOrDefault("HOTEL_SLOW_STEP", "0")) * 1000);
        // One request: a fresh store in its own transaction, counted for GET /stats; save what changed at the end.
        HotelApi.UnitOfWork unitOfWork = work -> transactions.execute(transaction -> {
            PostgresReservationStore store = new PostgresReservationStore(db, rooms);
            var result = work.apply(new BookingService(rooms, new CountingReservationStore(store, stats), email, slowStepMs));
            if (store.wasRefused()) {
                // The booking rule refused a save, so PostgreSQL has aborted this transaction and
                // no further statement in it can run. Nothing should be written anyway: roll back
                // and let the endpoint answer 409.
                transaction.setRollbackOnly();
                return result;
            }
            store.saveChanges();
            return result;
        });
        return new HotelApi(rooms, unitOfWork, stats, cache);
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
// P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
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
	"strconv"
	"strings"
	"sync"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/redis/go-redis/v9"

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
	// Step 1 widens the gap between the check and the save, so the race happens on every run.
	slowStepSeconds, _ := strconv.ParseFloat(cmp.Or(os.Getenv("HOTEL_SLOW_STEP"), "0"), 64)
	slowStep := time.Duration(slowStepSeconds * float64(time.Second))

	// One request: a fresh store, counted for GET /stats; save what changed at the end.
	unitOfWork := func(work func(service *reservations.BookingService)) error {
		store := reservations.NewPostgresReservationStore(ctx, pool, hotelRooms)
		work(reservations.NewBookingService(hotelRooms, api.NewCountingReservationStore(store, stats), email, slowStep))
		return store.SaveChanges()
	}

	// Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
	address := "localhost:" + cmp.Or(os.Getenv("HOTEL_PORT"), "5080")
	fmt.Println("Listening on http://" + address)
	log.Fatal(http.ListenAndServe(address, api.New(hotelRooms, unitOfWork, stats, cache)))
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
// P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
// node main.js          -> the API on http://localhost:5080 (first: docker compose up -d)
// node main.js --demo   -> the P06 demo, with the data in lists
// node main.js --race   -> two guests book the same room at the same moment
import pg from 'pg';
import { createClient } from 'redis';
import { CountingReservationStore, Stats, createApi } from './src/api.js';
import { AvailabilityCache, RedisKeyValueStore } from './src/api-cache.js';
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
  // Step 1 widens the gap between the check and the save, so the race happens on every run.
  const slowStepMs = Number(process.env.HOTEL_SLOW_STEP ?? 0) * 1000;

  // One request: a fresh store, counted for GET /stats; save what changed at the end.
  const unitOfWork = async work => {
    const store = new PostgresReservationStore(pool, rooms);
    const result = await work(new BookingService(rooms, new CountingReservationStore(store, stats), email, slowStepMs));
    await store.saveChanges();
    return result;
  };

  // Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
  const port = Number(process.env.HOTEL_PORT ?? 5080);
  createApi(rooms, unitOfWork, stats, cache).listen(port, 'localhost', () => console.log(`Listening on http://localhost:${port}`));
}

if (process.argv.includes('--race')) runRace();
else if (process.argv.includes('--demo')) runDemo();
else runApi();
```

#### Dart

```dart
// bin/main.dart
// P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
// dart run bin/main.dart          -> the API on http://localhost:5080 (first: docker compose up -d)
// dart run bin/main.dart --demo   -> the P06 demo, with the data in lists
// dart run bin/main.dart --race   -> two guests book the same room at the same moment
import 'dart:async';
import 'dart:io';

import 'package:hotel_booking/api.dart';
import 'package:hotel_booking/api_cache.dart';
import 'package:hotel_booking/notifications/src/email.dart';
import 'package:hotel_booking/reservations/reservations.dart';
import 'package:hotel_booking/reservations/src/postgres_store.dart';
import 'package:hotel_booking/reservations/src/store.dart';
import 'package:hotel_booking/rooms/rooms.dart';
import 'package:hotel_booking/rooms/src/catalog.dart';
import 'package:hotel_booking/rooms/src/postgres_catalog.dart';
import 'package:postgres/postgres.dart' show Endpoint, Pool, PoolSettings, SslMode;
import 'package:redis/redis.dart' show RedisConnection;
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
  // Step 1 widens the gap between the check and the save, so the race happens on every run.
  final slowStep =
      Duration(milliseconds: (double.parse(Platform.environment['HOTEL_SLOW_STEP'] ?? '0') * 1000).round());

  // One request: a fresh store, counted for GET /stats; save what changed at the end.
  Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async {
    final store = PostgresReservationStore(pool, rooms);
    final result = await work(
        BookingService(rooms, CountingReservationStore(store, stats), email, slowStep: slowStep));
    await store.saveChanges();
    return result;
  }

  // Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
  final apiPort = int.parse(Platform.environment['HOTEL_PORT'] ?? '5080');
  await serve(createApi(rooms, unitOfWork, stats, cache), 'localhost', apiPort);
  print('Listening on http://localhost:$apiPort');
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

### Completed example 1.4 · the race tool

Maya’s request goes first and Omar’s 500 ms later, so both reach the gap together and leave it in a known order. One warm-up request is sent first, because the very first request to a freshly started app is always the slowest.

**What the race tool does** In short: It sends two POST /reservations for the same room and the same nights at the same moment, and prints what each one got back. Maya’s request goes first and Omar’s 500 ms later, so both reach the gap together and leave it in a known order. That stagger is what makes the printed output the same on every run; it is not what makes the bug possible. One warm-up request is sent before the two bookings. The first request to a freshly started app pays for connection pools, servlet initialisation and JIT, which can take longer than Maya’s head start — and then Omar wins and the output comes out the other way round. The warm-up cannot reach the booking path itself, because GET /stats never inserts, so Maya’s request is still the first to compile the insert: half a second of head start is what covers it.

1. Start: the app is running with HOTEL_SLOW_STEP=1 and R7 has no reservations.
2. Action: you run the race tool.
3. Result:
Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.

Maya -> 201 Created   {"id":"R101","status":"Confirmed","notificationFailed":false}
Omar -> 201 Created   {"id":"R102","status":"Confirmed","notificationFailed":false}

Confirmed reservations for R7 on 12-14 Nov: 2
DOUBLE BOOKED: the room has two confirmed reservations for the same nights.

C#:

```text
// tools/race-http.cs
// Sends two POST /reservations for the same room and the same nights at the same moment, and prints
// what each one got back and how many confirmed reservations the room ended up with.
//
//   dotnet run tools/race-http.cs                            both requests to http://localhost:5080
//   dotnet run tools/race-http.cs http://localhost:5081      Omar's request to the second instance
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
//
// A file-based app (dotnet run <file>.cs) is compiled ready for ahead-of-time publishing, and that
// turns off the reflection the typed JSON helpers below need - PostAsJsonAsync would throw
// "Reflection-based serialization has been disabled for this application". This tool is run, never
// published, so the AOT settings do nothing for it and are turned off.
#:property PublishAot=false
using System.Net;
using System.Net.Http.Json;

var mayaBase = "http://localhost:5080";
var omarBase = args.Length > 0 ? args[0] : mayaBase;
var maya = new { guestName = "Maya", guestEmail = "maya@example.com", room = "R7", checkIn = "2026-11-12", checkOut = "2026-11-14" };
var omar = new { guestName = "Omar", guestEmail = "omar@example.com", room = "R7", checkIn = "2026-11-12", checkOut = "2026-11-14" };

using var client = new HttpClient();
await client.GetStringAsync($"{mayaBase}/stats"); // the warm-up: see the note at the top

Console.WriteLine("Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.");
if (omarBase != mayaBase) Console.WriteLine($"Maya -> {mayaBase}   Omar -> {omarBase}");
Console.WriteLine();

// Both requests are started before either is awaited: that is what "at the same moment" means here.
var mayaRequest = Book(mayaBase, maya);
var omarRequest = Delayed(500, () => Book(omarBase, omar)); // Omar clicks a moment after Maya
var answers = await Task.WhenAll(mayaRequest, omarRequest);

Console.WriteLine($"Maya -> {answers[0]}");
Console.WriteLine($"Omar -> {answers[1]}");
Console.WriteLine();

var accepted = answers.Count(answer => answer.StartsWith("201"));
Console.WriteLine($"Confirmed reservations for R7 on 12-14 Nov: {accepted}");
Console.WriteLine(accepted > 1
    ? "DOUBLE BOOKED: the room has two confirmed reservations for the same nights."
    : "CORRECT: one booking was accepted and one was refused.");

async Task<string> Book(string baseAddress, object body)
{
    var response = await client.PostAsJsonAsync($"{baseAddress}/reservations", body);
    var reason = response.StatusCode switch
    {
        HttpStatusCode.Created => "201 Created   ",
        HttpStatusCode.Conflict => "409 Conflict  ",
        HttpStatusCode.BadRequest => "400 Bad Request  ",
        var other => $"{(int)other} {other}  ",
    };
    return reason + await response.Content.ReadAsStringAsync();
}

async Task<string> Delayed(int milliseconds, Func<Task<string>> work)
{
    await Task.Delay(milliseconds);
    return await work();
}
```

Python:

```text
# tools/race_http.py
"""Sends two POST /reservations for the same room and the same nights at the same moment, and
prints what each one got back and how many confirmed reservations the room ended up with.

  python tools/race_http.py                             both requests to http://localhost:5080
  python tools/race_http.py http://localhost:5081        Omar's request to the second instance

Start the app with HOTEL_SLOW_STEP=1 first. Without it the gap between the check and the save is
too small to land in reliably, and the race shows up only now and then.

Maya's request goes first and Omar's 500 ms later, so the two reach the gap together and leave it
in a known order. That is what makes the output the same on every run; it is not what makes the
bug possible. The bug is possible because checking and saving are two separate statements.

A warm-up request is sent before the two bookings. The very first request to a freshly started app
pays for connection pools and import work, which can cost more than the head start Maya is given -
and then Omar wins instead and the output comes out the other way round. The warm-up cannot reach
the booking path itself, though - GET /stats never inserts - so Maya's request is still the first to
compile the insert, which measured as much as 140 ms more than Omar's. Half a second of head start
covers that and leaves both requests inside the gap.
"""
from __future__ import annotations

import json
import sys
import threading
import urllib.error
import urllib.request

MAYA = {"guestName": "Maya", "guestEmail": "maya@example.com", "room": "R7",
        "checkIn": "2026-11-12", "checkOut": "2026-11-14"}
OMAR = {**MAYA, "guestName": "Omar", "guestEmail": "omar@example.com"}
REASONS = {201: "201 Created", 409: "409 Conflict", 400: "400 Bad Request"}


def book(base: str, body: dict) -> str:
    request = urllib.request.Request(f"{base}/reservations", method="POST",
                                     data=json.dumps(body).encode(),
                                     headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(request) as response:
            return f"{REASONS.get(response.status, response.status)}   {response.read().decode()}"
    except urllib.error.HTTPError as error:
        return f"{REASONS.get(error.code, error.code)}  {error.read().decode()}"


def confirmed_count(base: str) -> int:
    """How many rooms the search still offers tells us nothing; count the reservations instead."""
    with urllib.request.urlopen(f"{base}/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14") as response:
        free = [room["number"] for room in json.loads(response.read())["rooms"]]
    return 0 if "R7" in free else 1


def main() -> None:
    maya_base = "http://localhost:5080"
    omar_base = sys.argv[1] if len(sys.argv) > 1 else maya_base
    answers: dict[str, str] = {}

    with urllib.request.urlopen(f"{maya_base}/stats") as warm_up:  # see the note at the top
        warm_up.read()

    print("Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.")
    if omar_base != maya_base:
        print(f"Maya -> {maya_base}   Omar -> {omar_base}")
    print()

    maya_request = threading.Thread(target=lambda: answers.update(Maya=book(maya_base, MAYA)))
    omar_request = threading.Thread(target=lambda: answers.update(Omar=book(omar_base, OMAR)))
    maya_request.start()
    threading.Event().wait(0.5)  # Omar clicks a moment after Maya
    omar_request.start()
    maya_request.join()
    omar_request.join()

    print(f"Maya -> {answers['Maya']}")
    print(f"Omar -> {answers['Omar']}")
    print()
    accepted = sum(1 for answer in answers.values() if answer.startswith("201"))
    print(f"Confirmed reservations for R7 on 12-14 Nov: {accepted}")
    print("DOUBLE BOOKED: the room has two confirmed reservations for the same nights."
          if accepted > 1 else "CORRECT: one booking was accepted and one was refused.")


if __name__ == "__main__":
    main()
```

Java:

```text
// tools/RaceHttp.java
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
```

Go:

```text
// tools/race/main.go
// Sends two POST /reservations for the same room and the same nights at the same moment, and prints
// what each one got back and how many confirmed reservations the room ended up with.
//
//	go run ./tools/race                          both requests to http://localhost:5080
//	go run ./tools/race http://localhost:5081    Omar's request to the second instance
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
package main

import (
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"
	"sync"
	"time"
)

const maya = `{"guestName":"Maya","guestEmail":"maya@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}`
const omar = `{"guestName":"Omar","guestEmail":"omar@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}`

func book(base, body string) string {
	response, err := http.Post(base+"/reservations", "application/json", strings.NewReader(body))
	if err != nil {
		return fmt.Sprintf("the request failed: %v", err)
	}
	defer response.Body.Close()
	text, _ := io.ReadAll(response.Body)
	return reason(response.StatusCode) + string(text)
}

func reason(status int) string {
	switch status {
	case 201:
		return "201 Created   "
	case 409:
		return "409 Conflict  "
	case 400:
		return "400 Bad Request  "
	default:
		return fmt.Sprintf("%d  ", status)
	}
}

func main() {
	mayaBase := "http://localhost:5080"
	omarBase := mayaBase
	if len(os.Args) > 1 {
		omarBase = os.Args[1]
	}

	// One warm-up request first: see the note above main.
	if response, err := http.Get(mayaBase + "/stats"); err == nil {
		response.Body.Close()
	}

	fmt.Println("Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.")
	if omarBase != mayaBase {
		fmt.Printf("Maya -> %s   Omar -> %s\n", mayaBase, omarBase)
	}
	fmt.Println()

	answers := make([]string, 2)
	var waiting sync.WaitGroup
	waiting.Add(2)
	go func() {
		defer waiting.Done()
		answers[0] = book(mayaBase, maya)
	}()
	go func() {
		defer waiting.Done()
		time.Sleep(500 * time.Millisecond) // Omar clicks a moment after Maya
		answers[1] = book(omarBase, omar)
	}()
	waiting.Wait()

	fmt.Printf("Maya -> %s\n", answers[0])
	fmt.Printf("Omar -> %s\n", answers[1])
	fmt.Println()

	accepted := 0
	for _, answer := range answers {
		if strings.HasPrefix(answer, "201") {
			accepted++
		}
	}
	fmt.Printf("Confirmed reservations for R7 on 12-14 Nov: %d\n", accepted)
	if accepted > 1 {
		fmt.Println("DOUBLE BOOKED: the room has two confirmed reservations for the same nights.")
	} else {
		fmt.Println("CORRECT: one booking was accepted and one was refused.")
	}
}
```

JavaScript:

```text
// tools/race-http.js
// Sends two POST /reservations for the same room and the same nights at the same moment, and
// prints what each one got back and how many confirmed reservations the room ended up with.
//
//   node tools/race-http.js                          both requests to http://localhost:5080
//   node tools/race-http.js http://localhost:5081    Omar's request to the second instance
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
const maya = { guestName: 'Maya', guestEmail: 'maya@example.com', room: 'R7', checkIn: '2026-11-12', checkOut: '2026-11-14' };
const omar = { ...maya, guestName: 'Omar', guestEmail: 'omar@example.com' };
const reasons = { 201: '201 Created', 409: '409 Conflict', 400: '400 Bad Request' };
const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

async function book(base, body) {
  const response = await fetch(`${base}/reservations`, {
    method: 'POST', body: JSON.stringify(body),
    headers: { 'Content-Type': 'application/json', Connection: 'close' },
  });
  const padding = response.status === 201 ? '   ' : '  ';
  return `${reasons[response.status] ?? response.status}${padding}${await response.text()}`;
}

const mayaBase = 'http://localhost:5080';
const omarBase = process.argv[2] ?? mayaBase;

await fetch(`${mayaBase}/stats`); // the warm-up: see the note at the top

console.log('Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.');
if (omarBase !== mayaBase) console.log(`Maya -> ${mayaBase}   Omar -> ${omarBase}`);
console.log();

// Both requests are started before either is awaited: that is what "at the same moment" means here.
const mayaRequest = book(mayaBase, maya);
const omarRequest = wait(500).then(() => book(omarBase, omar)); // Omar clicks a moment after Maya
const [mayaAnswer, omarAnswer] = await Promise.all([mayaRequest, omarRequest]);

console.log(`Maya -> ${mayaAnswer}`);
console.log(`Omar -> ${omarAnswer}`);
console.log();

const accepted = [mayaAnswer, omarAnswer].filter(answer => answer.startsWith('201')).length;
console.log(`Confirmed reservations for R7 on 12-14 Nov: ${accepted}`);
console.log(accepted > 1
  ? 'DOUBLE BOOKED: the room has two confirmed reservations for the same nights.'
  : 'CORRECT: one booking was accepted and one was refused.');
```

Dart:

```text
// tool/race_http.dart
// Sends two POST /reservations for the same room and the same nights at the same moment, and prints
// what each one got back and how many confirmed reservations the room ended up with.
//
//   dart run tool/race_http.dart                          both requests to http://localhost:5080
//   dart run tool/race_http.dart http://localhost:5081    Omar's request to the second instance
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
import 'dart:convert';
import 'dart:io';

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
  'checkIn': '2026-11-12',
  'checkOut': '2026-11-14'
};
const reasons = {201: '201 Created   ', 409: '409 Conflict  ', 400: '400 Bad Request  '};

Future<String> book(HttpClient client, String base, Map<String, String> body) async {
  final request = await client.postUrl(Uri.parse('$base/reservations'));
  request.headers.contentType = ContentType.json;
  request.write(jsonEncode(body));
  final response = await request.close();
  final text = await response.transform(utf8.decoder).join();
  return '${reasons[response.statusCode] ?? '${response.statusCode}  '}$text';
}

Future<void> main(List<String> args) async {
  const mayaBase = 'http://localhost:5080';
  final omarBase = args.isNotEmpty ? args[0] : mayaBase;
  final client = HttpClient();

  // The warm-up: see the note at the top.
  final warmUp = await (await client.getUrl(Uri.parse('$mayaBase/stats'))).close();
  await warmUp.drain<void>();

  print('Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.');
  if (omarBase != mayaBase) print('Maya -> $mayaBase   Omar -> $omarBase');
  print('');

  // Both requests are started before either is awaited: that is what "at the same moment" means.
  final mayaRequest = book(client, mayaBase, maya);
  final omarRequest = Future<void>.delayed(const Duration(milliseconds: 500))
      .then((_) => book(client, omarBase, omar)); // Omar clicks a moment after Maya
  final answers = await Future.wait([mayaRequest, omarRequest]);
  client.close();

  print('Maya -> ${answers[0]}');
  print('Omar -> ${answers[1]}');
  print('');

  final accepted = answers.where((answer) => answer.startsWith('201')).length;
  print('Confirmed reservations for R7 on 12-14 Nov: $accepted');
  print(accepted > 1
      ? 'DOUBLE BOOKED: the room has two confirmed reservations for the same nights.'
      : 'CORRECT: one booking was accepted and one was refused.');
}
```

### The race tool

#### C#

```csharp
// tools/race-http.cs
// Sends two POST /reservations for the same room and the same nights at the same moment, and prints
// what each one got back and how many confirmed reservations the room ended up with.
//
//   dotnet run tools/race-http.cs                            both requests to http://localhost:5080
//   dotnet run tools/race-http.cs http://localhost:5081      Omar's request to the second instance
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
//
// A file-based app (dotnet run <file>.cs) is compiled ready for ahead-of-time publishing, and that
// turns off the reflection the typed JSON helpers below need - PostAsJsonAsync would throw
// "Reflection-based serialization has been disabled for this application". This tool is run, never
// published, so the AOT settings do nothing for it and are turned off.
#:property PublishAot=false
using System.Net;
using System.Net.Http.Json;

var mayaBase = "http://localhost:5080";
var omarBase = args.Length > 0 ? args[0] : mayaBase;
var maya = new { guestName = "Maya", guestEmail = "maya@example.com", room = "R7", checkIn = "2026-11-12", checkOut = "2026-11-14" };
var omar = new { guestName = "Omar", guestEmail = "omar@example.com", room = "R7", checkIn = "2026-11-12", checkOut = "2026-11-14" };

using var client = new HttpClient();
await client.GetStringAsync($"{mayaBase}/stats"); // the warm-up: see the note at the top

Console.WriteLine("Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.");
if (omarBase != mayaBase) Console.WriteLine($"Maya -> {mayaBase}   Omar -> {omarBase}");
Console.WriteLine();

// Both requests are started before either is awaited: that is what "at the same moment" means here.
var mayaRequest = Book(mayaBase, maya);
var omarRequest = Delayed(500, () => Book(omarBase, omar)); // Omar clicks a moment after Maya
var answers = await Task.WhenAll(mayaRequest, omarRequest);

Console.WriteLine($"Maya -> {answers[0]}");
Console.WriteLine($"Omar -> {answers[1]}");
Console.WriteLine();

var accepted = answers.Count(answer => answer.StartsWith("201"));
Console.WriteLine($"Confirmed reservations for R7 on 12-14 Nov: {accepted}");
Console.WriteLine(accepted > 1
    ? "DOUBLE BOOKED: the room has two confirmed reservations for the same nights."
    : "CORRECT: one booking was accepted and one was refused.");

async Task<string> Book(string baseAddress, object body)
{
    var response = await client.PostAsJsonAsync($"{baseAddress}/reservations", body);
    var reason = response.StatusCode switch
    {
        HttpStatusCode.Created => "201 Created   ",
        HttpStatusCode.Conflict => "409 Conflict  ",
        HttpStatusCode.BadRequest => "400 Bad Request  ",
        var other => $"{(int)other} {other}  ",
    };
    return reason + await response.Content.ReadAsStringAsync();
}

async Task<string> Delayed(int milliseconds, Func<Task<string>> work)
{
    await Task.Delay(milliseconds);
    return await work();
}
```

#### Python

```python
# tools/race_http.py
"""Sends two POST /reservations for the same room and the same nights at the same moment, and
prints what each one got back and how many confirmed reservations the room ended up with.

  python tools/race_http.py                             both requests to http://localhost:5080
  python tools/race_http.py http://localhost:5081        Omar's request to the second instance

Start the app with HOTEL_SLOW_STEP=1 first. Without it the gap between the check and the save is
too small to land in reliably, and the race shows up only now and then.

Maya's request goes first and Omar's 500 ms later, so the two reach the gap together and leave it
in a known order. That is what makes the output the same on every run; it is not what makes the
bug possible. The bug is possible because checking and saving are two separate statements.

A warm-up request is sent before the two bookings. The very first request to a freshly started app
pays for connection pools and import work, which can cost more than the head start Maya is given -
and then Omar wins instead and the output comes out the other way round. The warm-up cannot reach
the booking path itself, though - GET /stats never inserts - so Maya's request is still the first to
compile the insert, which measured as much as 140 ms more than Omar's. Half a second of head start
covers that and leaves both requests inside the gap.
"""
from __future__ import annotations

import json
import sys
import threading
import urllib.error
import urllib.request

MAYA = {"guestName": "Maya", "guestEmail": "maya@example.com", "room": "R7",
        "checkIn": "2026-11-12", "checkOut": "2026-11-14"}
OMAR = {**MAYA, "guestName": "Omar", "guestEmail": "omar@example.com"}
REASONS = {201: "201 Created", 409: "409 Conflict", 400: "400 Bad Request"}


def book(base: str, body: dict) -> str:
    request = urllib.request.Request(f"{base}/reservations", method="POST",
                                     data=json.dumps(body).encode(),
                                     headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(request) as response:
            return f"{REASONS.get(response.status, response.status)}   {response.read().decode()}"
    except urllib.error.HTTPError as error:
        return f"{REASONS.get(error.code, error.code)}  {error.read().decode()}"


def confirmed_count(base: str) -> int:
    """How many rooms the search still offers tells us nothing; count the reservations instead."""
    with urllib.request.urlopen(f"{base}/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14") as response:
        free = [room["number"] for room in json.loads(response.read())["rooms"]]
    return 0 if "R7" in free else 1


def main() -> None:
    maya_base = "http://localhost:5080"
    omar_base = sys.argv[1] if len(sys.argv) > 1 else maya_base
    answers: dict[str, str] = {}

    with urllib.request.urlopen(f"{maya_base}/stats") as warm_up:  # see the note at the top
        warm_up.read()

    print("Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.")
    if omar_base != maya_base:
        print(f"Maya -> {maya_base}   Omar -> {omar_base}")
    print()

    maya_request = threading.Thread(target=lambda: answers.update(Maya=book(maya_base, MAYA)))
    omar_request = threading.Thread(target=lambda: answers.update(Omar=book(omar_base, OMAR)))
    maya_request.start()
    threading.Event().wait(0.5)  # Omar clicks a moment after Maya
    omar_request.start()
    maya_request.join()
    omar_request.join()

    print(f"Maya -> {answers['Maya']}")
    print(f"Omar -> {answers['Omar']}")
    print()
    accepted = sum(1 for answer in answers.values() if answer.startswith("201"))
    print(f"Confirmed reservations for R7 on 12-14 Nov: {accepted}")
    print("DOUBLE BOOKED: the room has two confirmed reservations for the same nights."
          if accepted > 1 else "CORRECT: one booking was accepted and one was refused.")


if __name__ == "__main__":
    main()
```

#### Java

```java
// tools/RaceHttp.java
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
```

#### Go

```go
// tools/race/main.go
// Sends two POST /reservations for the same room and the same nights at the same moment, and prints
// what each one got back and how many confirmed reservations the room ended up with.
//
//	go run ./tools/race                          both requests to http://localhost:5080
//	go run ./tools/race http://localhost:5081    Omar's request to the second instance
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
package main

import (
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"
	"sync"
	"time"
)

const maya = `{"guestName":"Maya","guestEmail":"maya@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}`
const omar = `{"guestName":"Omar","guestEmail":"omar@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}`

func book(base, body string) string {
	response, err := http.Post(base+"/reservations", "application/json", strings.NewReader(body))
	if err != nil {
		return fmt.Sprintf("the request failed: %v", err)
	}
	defer response.Body.Close()
	text, _ := io.ReadAll(response.Body)
	return reason(response.StatusCode) + string(text)
}

func reason(status int) string {
	switch status {
	case 201:
		return "201 Created   "
	case 409:
		return "409 Conflict  "
	case 400:
		return "400 Bad Request  "
	default:
		return fmt.Sprintf("%d  ", status)
	}
}

func main() {
	mayaBase := "http://localhost:5080"
	omarBase := mayaBase
	if len(os.Args) > 1 {
		omarBase = os.Args[1]
	}

	// One warm-up request first: see the note above main.
	if response, err := http.Get(mayaBase + "/stats"); err == nil {
		response.Body.Close()
	}

	fmt.Println("Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.")
	if omarBase != mayaBase {
		fmt.Printf("Maya -> %s   Omar -> %s\n", mayaBase, omarBase)
	}
	fmt.Println()

	answers := make([]string, 2)
	var waiting sync.WaitGroup
	waiting.Add(2)
	go func() {
		defer waiting.Done()
		answers[0] = book(mayaBase, maya)
	}()
	go func() {
		defer waiting.Done()
		time.Sleep(500 * time.Millisecond) // Omar clicks a moment after Maya
		answers[1] = book(omarBase, omar)
	}()
	waiting.Wait()

	fmt.Printf("Maya -> %s\n", answers[0])
	fmt.Printf("Omar -> %s\n", answers[1])
	fmt.Println()

	accepted := 0
	for _, answer := range answers {
		if strings.HasPrefix(answer, "201") {
			accepted++
		}
	}
	fmt.Printf("Confirmed reservations for R7 on 12-14 Nov: %d\n", accepted)
	if accepted > 1 {
		fmt.Println("DOUBLE BOOKED: the room has two confirmed reservations for the same nights.")
	} else {
		fmt.Println("CORRECT: one booking was accepted and one was refused.")
	}
}
```

#### JavaScript

```javascript
// tools/race-http.js
// Sends two POST /reservations for the same room and the same nights at the same moment, and
// prints what each one got back and how many confirmed reservations the room ended up with.
//
//   node tools/race-http.js                          both requests to http://localhost:5080
//   node tools/race-http.js http://localhost:5081    Omar's request to the second instance
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
const maya = { guestName: 'Maya', guestEmail: 'maya@example.com', room: 'R7', checkIn: '2026-11-12', checkOut: '2026-11-14' };
const omar = { ...maya, guestName: 'Omar', guestEmail: 'omar@example.com' };
const reasons = { 201: '201 Created', 409: '409 Conflict', 400: '400 Bad Request' };
const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

async function book(base, body) {
  const response = await fetch(`${base}/reservations`, {
    method: 'POST', body: JSON.stringify(body),
    headers: { 'Content-Type': 'application/json', Connection: 'close' },
  });
  const padding = response.status === 201 ? '   ' : '  ';
  return `${reasons[response.status] ?? response.status}${padding}${await response.text()}`;
}

const mayaBase = 'http://localhost:5080';
const omarBase = process.argv[2] ?? mayaBase;

await fetch(`${mayaBase}/stats`); // the warm-up: see the note at the top

console.log('Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.');
if (omarBase !== mayaBase) console.log(`Maya -> ${mayaBase}   Omar -> ${omarBase}`);
console.log();

// Both requests are started before either is awaited: that is what "at the same moment" means here.
const mayaRequest = book(mayaBase, maya);
const omarRequest = wait(500).then(() => book(omarBase, omar)); // Omar clicks a moment after Maya
const [mayaAnswer, omarAnswer] = await Promise.all([mayaRequest, omarRequest]);

console.log(`Maya -> ${mayaAnswer}`);
console.log(`Omar -> ${omarAnswer}`);
console.log();

const accepted = [mayaAnswer, omarAnswer].filter(answer => answer.startsWith('201')).length;
console.log(`Confirmed reservations for R7 on 12-14 Nov: ${accepted}`);
console.log(accepted > 1
  ? 'DOUBLE BOOKED: the room has two confirmed reservations for the same nights.'
  : 'CORRECT: one booking was accepted and one was refused.');
```

#### Dart

```dart
// tool/race_http.dart
// Sends two POST /reservations for the same room and the same nights at the same moment, and prints
// what each one got back and how many confirmed reservations the room ended up with.
//
//   dart run tool/race_http.dart                          both requests to http://localhost:5080
//   dart run tool/race_http.dart http://localhost:5081    Omar's request to the second instance
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
import 'dart:convert';
import 'dart:io';

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
  'checkIn': '2026-11-12',
  'checkOut': '2026-11-14'
};
const reasons = {201: '201 Created   ', 409: '409 Conflict  ', 400: '400 Bad Request  '};

Future<String> book(HttpClient client, String base, Map<String, String> body) async {
  final request = await client.postUrl(Uri.parse('$base/reservations'));
  request.headers.contentType = ContentType.json;
  request.write(jsonEncode(body));
  final response = await request.close();
  final text = await response.transform(utf8.decoder).join();
  return '${reasons[response.statusCode] ?? '${response.statusCode}  '}$text';
}

Future<void> main(List<String> args) async {
  const mayaBase = 'http://localhost:5080';
  final omarBase = args.isNotEmpty ? args[0] : mayaBase;
  final client = HttpClient();

  // The warm-up: see the note at the top.
  final warmUp = await (await client.getUrl(Uri.parse('$mayaBase/stats'))).close();
  await warmUp.drain<void>();

  print('Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.');
  if (omarBase != mayaBase) print('Maya -> $mayaBase   Omar -> $omarBase');
  print('');

  // Both requests are started before either is awaited: that is what "at the same moment" means.
  final mayaRequest = book(client, mayaBase, maya);
  final omarRequest = Future<void>.delayed(const Duration(milliseconds: 500))
      .then((_) => book(client, omarBase, omar)); // Omar clicks a moment after Maya
  final answers = await Future.wait([mayaRequest, omarRequest]);
  client.close();

  print('Maya -> ${answers[0]}');
  print('Omar -> ${answers[1]}');
  print('');

  final accepted = answers.where((answer) => answer.startsWith('201')).length;
  print('Confirmed reservations for R7 on 12-14 Nov: $accepted');
  print(accepted > 1
      ? 'DOUBLE BOOKED: the room has two confirmed reservations for the same nights.'
      : 'CORRECT: one booking was accepted and one was refused.');
}
```

### Run it

Terminal 1 runs the app with the slow step; terminal 3 runs the tool.

### Two terminals

#### C#

```text
# terminal 1
$env:HOTEL_SLOW_STEP = "1"; dotnet run --project HotelBooking
# terminal 3
dotnet run tools/race-http.cs
```

#### Python

```text
# terminal 1
$env:HOTEL_SLOW_STEP = "1"; python main.py
# terminal 3
python tools/race_http.py
```

#### Java

```text
# terminal 1
$env:HOTEL_SLOW_STEP = "1"; mvn -q spring-boot:run
# terminal 3
java tools/RaceHttp.java
```

#### Go

```text
# terminal 1
$env:HOTEL_SLOW_STEP = "1"; go run .
# terminal 3
go run ./tools/race
```

#### JavaScript

```text
# terminal 1
$env:HOTEL_SLOW_STEP = "1"; node main.js
# terminal 3
node tools/race-http.js
```

#### Dart

```text
# terminal 1
$env:HOTEL_SLOW_STEP = "1"; dart run bin/main.dart
# terminal 3
dart run tool/race_http.dart
```

### What you will see

Both guests are told their booking was created.

### Terminal 3

```text
Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.

Maya -> 201 Created   {"id":"R101","status":"Confirmed","notificationFailed":false}
Omar -> 201 Created   {"id":"R102","status":"Confirmed","notificationFailed":false}

Confirmed reservations for R7 on 12-14 Nov: 2
DOUBLE BOOKED: the room has two confirmed reservations for the same nights.
```

### And in the database

Two confirmed reservations, for one room, for the same nights.

### docker compose exec db psql -U hotel -d hotel

```text
SELECT id, room, check_in, check_out, status
FROM reservations
WHERE room = 'R7' AND status = 'Confirmed'
ORDER BY id;
```

### 

### psql

```text
  id  | room |  check_in  | check_out  |  status
------+------+------------+------------+-----------
 R101 | R7   | 2026-11-12 | 2026-11-14 | Confirmed
 R102 | R7   | 2026-11-12 | 2026-11-14 | Confirmed
(2 rows)
```

### Your turn · make it happen on purpose

1. Add the sequence to db/init.sql (1.1), then run the same two statements in psql against your running database.
2. Add nextReservationId to the store contract and to both stores (1.2), and make reserve use it instead of counting.
3. Add HOTEL_SLOW_STEP and HOTEL_PORT to your wiring file (1.3).
4. Run your checks: still 17 green. Nothing has been repaired yet — this step only removes the id race and adds a tool.
5. Create the race tool (1.4) in tools/ (Dart: tool/).
6. Start the app in terminal 1 with HOTEL_SLOW_STEP=1, then run the race tool in terminal 3.
7. Open psql and run the SELECT above. Count the confirmed reservations for R7.

**When you finish, you have:** A race tool that double-books R7 every time you run it, and two Confirmed rows in your own database.
### Explanation

The pause is not what makes the bug possible; it only makes it easy to aim at. Take HOTEL_SLOW_STEP away and the gap is still there, a few microseconds wide, and a busy hotel will find it for you.



---

## 3. Step 2 · Write the interleaving down

A race is impossible to reason about until you can see which statement of each request ran when. Put both requests in one list, in time order, and the gap becomes obvious.





### The two requests, statement by statement

Read down one column to follow one guest, and across to see what both were doing at the same instant. Every statement does exactly what it was asked to do.

### Diagram: Two bookings for one room, and why both are accepted

Both requests are sent by tools/race-http at the same moment, for room R7 and the same nights, with the app started with HOTEL_SLOW_STEP=1 so the gap between checking and saving is wide enough to land in. Read down one column to follow one guest; read across to see what both were doing at the same instant. Nothing here is broken: every statement does exactly what it was asked to do.

Diagram as text:

tools/race-http sends two POST /reservations for R7, 12-14 November, 500 ms apart, to an app started with HOTEL_SLOW_STEP=1. Step 1 and 2: each request checks its body and dates and opens its own unit of work, which is its own database transaction. Step 3 and 4: each asks whether R7 already has a confirmed reservation sharing a night with the stay. Both get 0, and both answers are correct at the moment they are given. Step 5 and 6: each asks the sequence for an id - Maya gets R101, Omar gets R102 - and sends its INSERT. Each row exists only inside its own transaction and is invisible to the other. Step 7 and 8: both transactions commit. Nothing in the database says the two rows may not both exist, so both are accepted and both guests are told 201 Created. The result is one room with two confirmed reservations for the same nights. The anomaly is write skew: both transactions read the same set, found it empty, and then inserted different rows.

**Step by step:** Follow the numbers 1 to 8: they run down the two columns in the order things really happened. The left column is Maya, the right is Omar. The important thing to notice is that no step is wrong - the bug is in the gap between step 3 and step 5.

1. Yellow box 1 and yellow box 2, the two POST /reservations. The race tool starts Maya, waits 500 ms, then starts Omar, so both land in the gap together and leave it in a known order. Here is the tool:

   C#:
   
   ```text
   // tools/race-http.cs
   // Sends two POST /reservations for the same room and the same nights at the same moment, and prints
   // what each one got back and how many confirmed reservations the room ended up with.
   //
   //   dotnet run tools/race-http.cs                            both requests to http://localhost:5080
   //   dotnet run tools/race-http.cs http://localhost:5081      Omar's request to the second instance
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
   //
   // A file-based app (dotnet run <file>.cs) is compiled ready for ahead-of-time publishing, and that
   // turns off the reflection the typed JSON helpers below need - PostAsJsonAsync would throw
   // "Reflection-based serialization has been disabled for this application". This tool is run, never
   // published, so the AOT settings do nothing for it and are turned off.
   #:property PublishAot=false
   using System.Net;
   using System.Net.Http.Json;
   
   var mayaBase = "http://localhost:5080";
   var omarBase = args.Length > 0 ? args[0] : mayaBase;
   var maya = new { guestName = "Maya", guestEmail = "maya@example.com", room = "R7", checkIn = "2026-11-12", checkOut = "2026-11-14" };
   var omar = new { guestName = "Omar", guestEmail = "omar@example.com", room = "R7", checkIn = "2026-11-12", checkOut = "2026-11-14" };
   
   using var client = new HttpClient();
   await client.GetStringAsync($"{mayaBase}/stats"); // the warm-up: see the note at the top
   
   Console.WriteLine("Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.");
   if (omarBase != mayaBase) Console.WriteLine($"Maya -> {mayaBase}   Omar -> {omarBase}");
   Console.WriteLine();
   
   // Both requests are started before either is awaited: that is what "at the same moment" means here.
   var mayaRequest = Book(mayaBase, maya);
   var omarRequest = Delayed(500, () => Book(omarBase, omar)); // Omar clicks a moment after Maya
   var answers = await Task.WhenAll(mayaRequest, omarRequest);
   
   Console.WriteLine($"Maya -> {answers[0]}");
   Console.WriteLine($"Omar -> {answers[1]}");
   Console.WriteLine();
   
   var accepted = answers.Count(answer => answer.StartsWith("201"));
   Console.WriteLine($"Confirmed reservations for R7 on 12-14 Nov: {accepted}");
   Console.WriteLine(accepted > 1
       ? "DOUBLE BOOKED: the room has two confirmed reservations for the same nights."
       : "CORRECT: one booking was accepted and one was refused.");
   
   async Task<string> Book(string baseAddress, object body)
   {
       var response = await client.PostAsJsonAsync($"{baseAddress}/reservations", body);
       var reason = response.StatusCode switch
       {
           HttpStatusCode.Created => "201 Created   ",
           HttpStatusCode.Conflict => "409 Conflict  ",
           HttpStatusCode.BadRequest => "400 Bad Request  ",
           var other => $"{(int)other} {other}  ",
       };
       return reason + await response.Content.ReadAsStringAsync();
   }
   
   async Task<string> Delayed(int milliseconds, Func<Task<string>> work)
   {
       await Task.Delay(milliseconds);
       return await work();
   }
   ```
   
   Python:
   
   ```text
   # tools/race_http.py
   """Sends two POST /reservations for the same room and the same nights at the same moment, and
   prints what each one got back and how many confirmed reservations the room ended up with.
   
     python tools/race_http.py                             both requests to http://localhost:5080
     python tools/race_http.py http://localhost:5081        Omar's request to the second instance
   
   Start the app with HOTEL_SLOW_STEP=1 first. Without it the gap between the check and the save is
   too small to land in reliably, and the race shows up only now and then.
   
   Maya's request goes first and Omar's 500 ms later, so the two reach the gap together and leave it
   in a known order. That is what makes the output the same on every run; it is not what makes the
   bug possible. The bug is possible because checking and saving are two separate statements.
   
   A warm-up request is sent before the two bookings. The very first request to a freshly started app
   pays for connection pools and import work, which can cost more than the head start Maya is given -
   and then Omar wins instead and the output comes out the other way round. The warm-up cannot reach
   the booking path itself, though - GET /stats never inserts - so Maya's request is still the first to
   compile the insert, which measured as much as 140 ms more than Omar's. Half a second of head start
   covers that and leaves both requests inside the gap.
   """
   from __future__ import annotations
   
   import json
   import sys
   import threading
   import urllib.error
   import urllib.request
   
   MAYA = {"guestName": "Maya", "guestEmail": "maya@example.com", "room": "R7",
           "checkIn": "2026-11-12", "checkOut": "2026-11-14"}
   OMAR = {**MAYA, "guestName": "Omar", "guestEmail": "omar@example.com"}
   REASONS = {201: "201 Created", 409: "409 Conflict", 400: "400 Bad Request"}
   
   
   def book(base: str, body: dict) -> str:
       request = urllib.request.Request(f"{base}/reservations", method="POST",
                                        data=json.dumps(body).encode(),
                                        headers={"Content-Type": "application/json"})
       try:
           with urllib.request.urlopen(request) as response:
               return f"{REASONS.get(response.status, response.status)}   {response.read().decode()}"
       except urllib.error.HTTPError as error:
           return f"{REASONS.get(error.code, error.code)}  {error.read().decode()}"
   
   
   def confirmed_count(base: str) -> int:
       """How many rooms the search still offers tells us nothing; count the reservations instead."""
       with urllib.request.urlopen(f"{base}/rooms/availability?checkIn=2026-11-12&checkOut=2026-11-14") as response:
           free = [room["number"] for room in json.loads(response.read())["rooms"]]
       return 0 if "R7" in free else 1
   
   
   def main() -> None:
       maya_base = "http://localhost:5080"
       omar_base = sys.argv[1] if len(sys.argv) > 1 else maya_base
       answers: dict[str, str] = {}
   
       with urllib.request.urlopen(f"{maya_base}/stats") as warm_up:  # see the note at the top
           warm_up.read()
   
       print("Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.")
       if omar_base != maya_base:
           print(f"Maya -> {maya_base}   Omar -> {omar_base}")
       print()
   
       maya_request = threading.Thread(target=lambda: answers.update(Maya=book(maya_base, MAYA)))
       omar_request = threading.Thread(target=lambda: answers.update(Omar=book(omar_base, OMAR)))
       maya_request.start()
       threading.Event().wait(0.5)  # Omar clicks a moment after Maya
       omar_request.start()
       maya_request.join()
       omar_request.join()
   
       print(f"Maya -> {answers['Maya']}")
       print(f"Omar -> {answers['Omar']}")
       print()
       accepted = sum(1 for answer in answers.values() if answer.startswith("201"))
       print(f"Confirmed reservations for R7 on 12-14 Nov: {accepted}")
       print("DOUBLE BOOKED: the room has two confirmed reservations for the same nights."
             if accepted > 1 else "CORRECT: one booking was accepted and one was refused.")
   
   
   if __name__ == "__main__":
       main()
   ```
   
   Java:
   
   ```text
   // tools/RaceHttp.java
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
   ```
   
   Go:
   
   ```text
   // tools/race/main.go
   // Sends two POST /reservations for the same room and the same nights at the same moment, and prints
   // what each one got back and how many confirmed reservations the room ended up with.
   //
   //	go run ./tools/race                          both requests to http://localhost:5080
   //	go run ./tools/race http://localhost:5081    Omar's request to the second instance
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
   package main
   
   import (
   	"fmt"
   	"io"
   	"net/http"
   	"os"
   	"strings"
   	"sync"
   	"time"
   )
   
   const maya = `{"guestName":"Maya","guestEmail":"maya@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}`
   const omar = `{"guestName":"Omar","guestEmail":"omar@example.com","room":"R7","checkIn":"2026-11-12","checkOut":"2026-11-14"}`
   
   func book(base, body string) string {
   	response, err := http.Post(base+"/reservations", "application/json", strings.NewReader(body))
   	if err != nil {
   		return fmt.Sprintf("the request failed: %v", err)
   	}
   	defer response.Body.Close()
   	text, _ := io.ReadAll(response.Body)
   	return reason(response.StatusCode) + string(text)
   }
   
   func reason(status int) string {
   	switch status {
   	case 201:
   		return "201 Created   "
   	case 409:
   		return "409 Conflict  "
   	case 400:
   		return "400 Bad Request  "
   	default:
   		return fmt.Sprintf("%d  ", status)
   	}
   }
   
   func main() {
   	mayaBase := "http://localhost:5080"
   	omarBase := mayaBase
   	if len(os.Args) > 1 {
   		omarBase = os.Args[1]
   	}
   
   	// One warm-up request first: see the note above main.
   	if response, err := http.Get(mayaBase + "/stats"); err == nil {
   		response.Body.Close()
   	}
   
   	fmt.Println("Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.")
   	if omarBase != mayaBase {
   		fmt.Printf("Maya -> %s   Omar -> %s\n", mayaBase, omarBase)
   	}
   	fmt.Println()
   
   	answers := make([]string, 2)
   	var waiting sync.WaitGroup
   	waiting.Add(2)
   	go func() {
   		defer waiting.Done()
   		answers[0] = book(mayaBase, maya)
   	}()
   	go func() {
   		defer waiting.Done()
   		time.Sleep(500 * time.Millisecond) // Omar clicks a moment after Maya
   		answers[1] = book(omarBase, omar)
   	}()
   	waiting.Wait()
   
   	fmt.Printf("Maya -> %s\n", answers[0])
   	fmt.Printf("Omar -> %s\n", answers[1])
   	fmt.Println()
   
   	accepted := 0
   	for _, answer := range answers {
   		if strings.HasPrefix(answer, "201") {
   			accepted++
   		}
   	}
   	fmt.Printf("Confirmed reservations for R7 on 12-14 Nov: %d\n", accepted)
   	if accepted > 1 {
   		fmt.Println("DOUBLE BOOKED: the room has two confirmed reservations for the same nights.")
   	} else {
   		fmt.Println("CORRECT: one booking was accepted and one was refused.")
   	}
   }
   ```
   
   JavaScript:
   
   ```text
   // tools/race-http.js
   // Sends two POST /reservations for the same room and the same nights at the same moment, and
   // prints what each one got back and how many confirmed reservations the room ended up with.
   //
   //   node tools/race-http.js                          both requests to http://localhost:5080
   //   node tools/race-http.js http://localhost:5081    Omar's request to the second instance
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
   const maya = { guestName: 'Maya', guestEmail: 'maya@example.com', room: 'R7', checkIn: '2026-11-12', checkOut: '2026-11-14' };
   const omar = { ...maya, guestName: 'Omar', guestEmail: 'omar@example.com' };
   const reasons = { 201: '201 Created', 409: '409 Conflict', 400: '400 Bad Request' };
   const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));
   
   async function book(base, body) {
     const response = await fetch(`${base}/reservations`, {
       method: 'POST', body: JSON.stringify(body),
       headers: { 'Content-Type': 'application/json', Connection: 'close' },
     });
     const padding = response.status === 201 ? '   ' : '  ';
     return `${reasons[response.status] ?? response.status}${padding}${await response.text()}`;
   }
   
   const mayaBase = 'http://localhost:5080';
   const omarBase = process.argv[2] ?? mayaBase;
   
   await fetch(`${mayaBase}/stats`); // the warm-up: see the note at the top
   
   console.log('Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.');
   if (omarBase !== mayaBase) console.log(`Maya -> ${mayaBase}   Omar -> ${omarBase}`);
   console.log();
   
   // Both requests are started before either is awaited: that is what "at the same moment" means here.
   const mayaRequest = book(mayaBase, maya);
   const omarRequest = wait(500).then(() => book(omarBase, omar)); // Omar clicks a moment after Maya
   const [mayaAnswer, omarAnswer] = await Promise.all([mayaRequest, omarRequest]);
   
   console.log(`Maya -> ${mayaAnswer}`);
   console.log(`Omar -> ${omarAnswer}`);
   console.log();
   
   const accepted = [mayaAnswer, omarAnswer].filter(answer => answer.startsWith('201')).length;
   console.log(`Confirmed reservations for R7 on 12-14 Nov: ${accepted}`);
   console.log(accepted > 1
     ? 'DOUBLE BOOKED: the room has two confirmed reservations for the same nights.'
     : 'CORRECT: one booking was accepted and one was refused.');
   ```
   
   Dart:
   
   ```text
   // tool/race_http.dart
   // Sends two POST /reservations for the same room and the same nights at the same moment, and prints
   // what each one got back and how many confirmed reservations the room ended up with.
   //
   //   dart run tool/race_http.dart                          both requests to http://localhost:5080
   //   dart run tool/race_http.dart http://localhost:5081    Omar's request to the second instance
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
   import 'dart:convert';
   import 'dart:io';
   
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
     'checkIn': '2026-11-12',
     'checkOut': '2026-11-14'
   };
   const reasons = {201: '201 Created   ', 409: '409 Conflict  ', 400: '400 Bad Request  '};
   
   Future<String> book(HttpClient client, String base, Map<String, String> body) async {
     final request = await client.postUrl(Uri.parse('$base/reservations'));
     request.headers.contentType = ContentType.json;
     request.write(jsonEncode(body));
     final response = await request.close();
     final text = await response.transform(utf8.decoder).join();
     return '${reasons[response.statusCode] ?? '${response.statusCode}  '}$text';
   }
   
   Future<void> main(List<String> args) async {
     const mayaBase = 'http://localhost:5080';
     final omarBase = args.isNotEmpty ? args[0] : mayaBase;
     final client = HttpClient();
   
     // The warm-up: see the note at the top.
     final warmUp = await (await client.getUrl(Uri.parse('$mayaBase/stats'))).close();
     await warmUp.drain<void>();
   
     print('Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.');
     if (omarBase != mayaBase) print('Maya -> $mayaBase   Omar -> $omarBase');
     print('');
   
     // Both requests are started before either is awaited: that is what "at the same moment" means.
     final mayaRequest = book(client, mayaBase, maya);
     final omarRequest = Future<void>.delayed(const Duration(milliseconds: 500))
         .then((_) => book(client, omarBase, omar)); // Omar clicks a moment after Maya
     final answers = await Future.wait([mayaRequest, omarRequest]);
     client.close();
   
     print('Maya -> ${answers[0]}');
     print('Omar -> ${answers[1]}');
     print('');
   
     final accepted = answers.where((answer) => answer.startsWith('201')).length;
     print('Confirmed reservations for R7 on 12-14 Nov: $accepted');
     print(accepted > 1
         ? 'DOUBLE BOOKED: the room has two confirmed reservations for the same nights.'
         : 'CORRECT: one booking was accepted and one was refused.');
   }
   ```
2. The app has to be started with HOTEL_SLOW_STEP=1 first. That makes every booking pause between checking and saving, which is how a gap that normally lasts microseconds becomes wide enough to aim at. The gap is always there; the pause only makes it visible.

   C#:
   
   ```text
   // HotelBooking/Program.cs
   // P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
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
   using StackExchange.Redis;
   
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
       // Step 1 widens the gap between the check and the save, so the race happens on every run.
       var slowStepMs = int.Parse(Environment.GetEnvironmentVariable("HOTEL_SLOW_STEP") ?? "0") * 1000;
   
       // One request: a fresh store over its own DbContext, counted for GET /stats; save what changed at the end.
       IResult UnitOfWork(Func<BookingService, IResult> work)
       {
           using var db = databases.CreateDbContext();
           var store = new PostgresReservationStore(db, rooms);
           var result = work(new BookingService(rooms, new CountingReservationStore(store, stats), email, slowStepMs));
           store.SaveChanges();
           return result;
       }
   
       HotelApi.Map(app, rooms, UnitOfWork, stats, cache);
       // Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
       app.Run($"http://localhost:{Environment.GetEnvironmentVariable("HOTEL_PORT") ?? "5080"}");
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
   
   Python:
   
   ```text
   # main.py
   """P09 - Hotel booking: the P08 app with the booking rule protected by PostgreSQL.
   
     python main.py             the HTTP API on http://localhost:5080 (HOTEL_PORT changes the port)
     python main.py --demo      the P06 demo, on a list in memory
     python main.py --race      the P06 race, on a list in memory
   
   HOTEL_SLOW_STEP makes every booking wait that many seconds between checking and saving, which is
   what makes the race in tools/race_http.py happen on every run instead of now and then.
   """
   from __future__ import annotations
   
   import os
   import sys
   import threading
   import time
   from contextlib import contextmanager
   from datetime import date
   
   import uvicorn
   from redis import Redis
   from sqlalchemy import create_engine
   from sqlalchemy.orm import Session
   
   from hotel_booking.api import CountingReservationStore, Stats, create_app
   from hotel_booking.api_cache import AvailabilityCache, RedisKeyValueStore
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
       # The wiring for the web app: PostgreSQL and Redis from docker-compose.yml
       # (HOTEL_DB_PORT and HOTEL_REDIS_PORT only if 5432 or 6379 is taken).
       engine = create_engine(f"postgresql+psycopg://hotel:hotel@localhost:{os.getenv('HOTEL_DB_PORT', '5432')}/hotel")
       rooms = PostgresRoomCatalog(engine).all()
       email = EmailNotificationSender()
       stats = Stats()
       redis = Redis(host="localhost", port=int(os.getenv("HOTEL_REDIS_PORT", "6379")), decode_responses=True)
       redis.ping()  # stop here if Redis is not running
       cache = AvailabilityCache(RedisKeyValueStore(redis))
       # Step 1 widens the gap between the check and the save, so the race happens every run.
       slow_step = float(os.getenv("HOTEL_SLOW_STEP", "0"))
   
       @contextmanager
       def unit_of_work():
           # One request: a fresh store over its own session, counted for GET /stats; save what changed at the end.
           with Session(engine) as session:
               store = PostgresReservationStore(session, rooms)
               yield BookingService(rooms, CountingReservationStore(store, stats), email, slow_step_seconds=slow_step)
               store.save_changes()
   
       # Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
       uvicorn.run(create_app(rooms, unit_of_work, stats, cache),
                   host="localhost", port=int(os.getenv("HOTEL_PORT", "5080")))
   
   
   if __name__ == "__main__":
       if "--race" in sys.argv:
           run_race()
       elif "--demo" in sys.argv:
           run_demo()
       else:
           run_api()
   ```
   
   Java:
   
   ```text
   // src/main/java/hotelbooking/Main.java
   // P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
   // mvn -q spring-boot:run                              -> the API on http://localhost:5080 (first: docker compose up -d)
   // java -jar target/hotel-booking-1.0.jar --demo       -> the P06 demo, with the data in lists (after mvn -q package)
   // java -jar target/hotel-booking-1.0.jar --race       -> two guests book the same room at the same moment
   package hotelbooking;
   
   import java.time.LocalDate;
   import java.util.List;
   import java.util.stream.Collectors;
   import hotelbooking.api.AvailabilityCache;
   import hotelbooking.api.CountingReservationStore;
   import hotelbooking.api.HotelApi;
   import hotelbooking.api.Stats;
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
   import org.springframework.data.redis.connection.RedisConnection;
   import org.springframework.data.redis.core.StringRedisTemplate;
   import org.springframework.transaction.support.TransactionTemplate;
   
   // Spring Boot starts the web server and connects to PostgreSQL and Redis (application.properties);
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
       HotelApi hotelApi(EntityManager db, TransactionTemplate transactions, StringRedisTemplate redis) {
           List<Room> rooms = new PostgresRoomCatalog(db).all();
           EmailNotificationSender email = new EmailNotificationSender();
           Stats stats = new Stats();
           try (RedisConnection connection = redis.getRequiredConnectionFactory().getConnection()) {
               connection.ping(); // stop here if Redis is not running
           }
           AvailabilityCache cache = new AvailabilityCache(new AvailabilityCache.RedisKeyValueStore(redis));
           // Step 1 widens the gap between the check and the save, so the race happens on every run.
           int slowStepMs = (int) (Double.parseDouble(System.getenv().getOrDefault("HOTEL_SLOW_STEP", "0")) * 1000);
           // One request: a fresh store in its own transaction, counted for GET /stats; save what changed at the end.
           HotelApi.UnitOfWork unitOfWork = work -> transactions.execute(transaction -> {
               PostgresReservationStore store = new PostgresReservationStore(db, rooms);
               var result = work.apply(new BookingService(rooms, new CountingReservationStore(store, stats), email, slowStepMs));
               if (store.wasRefused()) {
                   // The booking rule refused a save, so PostgreSQL has aborted this transaction and
                   // no further statement in it can run. Nothing should be written anyway: roll back
                   // and let the endpoint answer 409.
                   transaction.setRollbackOnly();
                   return result;
               }
               store.saveChanges();
               return result;
           });
           return new HotelApi(rooms, unitOfWork, stats, cache);
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
   
   Go:
   
   ```text
   // main.go
   // P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
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
   	"strconv"
   	"strings"
   	"sync"
   	"time"
   
   	"github.com/jackc/pgx/v5/pgxpool"
   	"github.com/redis/go-redis/v9"
   
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
   	// Step 1 widens the gap between the check and the save, so the race happens on every run.
   	slowStepSeconds, _ := strconv.ParseFloat(cmp.Or(os.Getenv("HOTEL_SLOW_STEP"), "0"), 64)
   	slowStep := time.Duration(slowStepSeconds * float64(time.Second))
   
   	// One request: a fresh store, counted for GET /stats; save what changed at the end.
   	unitOfWork := func(work func(service *reservations.BookingService)) error {
   		store := reservations.NewPostgresReservationStore(ctx, pool, hotelRooms)
   		work(reservations.NewBookingService(hotelRooms, api.NewCountingReservationStore(store, stats), email, slowStep))
   		return store.SaveChanges()
   	}
   
   	// Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
   	address := "localhost:" + cmp.Or(os.Getenv("HOTEL_PORT"), "5080")
   	fmt.Println("Listening on http://" + address)
   	log.Fatal(http.ListenAndServe(address, api.New(hotelRooms, unitOfWork, stats, cache)))
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
   
   JavaScript:
   
   ```text
   // main.js
   // P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
   // node main.js          -> the API on http://localhost:5080 (first: docker compose up -d)
   // node main.js --demo   -> the P06 demo, with the data in lists
   // node main.js --race   -> two guests book the same room at the same moment
   import pg from 'pg';
   import { createClient } from 'redis';
   import { CountingReservationStore, Stats, createApi } from './src/api.js';
   import { AvailabilityCache, RedisKeyValueStore } from './src/api-cache.js';
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
     // Step 1 widens the gap between the check and the save, so the race happens on every run.
     const slowStepMs = Number(process.env.HOTEL_SLOW_STEP ?? 0) * 1000;
   
     // One request: a fresh store, counted for GET /stats; save what changed at the end.
     const unitOfWork = async work => {
       const store = new PostgresReservationStore(pool, rooms);
       const result = await work(new BookingService(rooms, new CountingReservationStore(store, stats), email, slowStepMs));
       await store.saveChanges();
       return result;
     };
   
     // Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
     const port = Number(process.env.HOTEL_PORT ?? 5080);
     createApi(rooms, unitOfWork, stats, cache).listen(port, 'localhost', () => console.log(`Listening on http://localhost:${port}`));
   }
   
   if (process.argv.includes('--race')) runRace();
   else if (process.argv.includes('--demo')) runDemo();
   else runApi();
   ```
   
   Dart:
   
   ```text
   // bin/main.dart
   // P08 · Hotel booking: the P07 API with a Redis cache for the free-room search.
   // dart run bin/main.dart          -> the API on http://localhost:5080 (first: docker compose up -d)
   // dart run bin/main.dart --demo   -> the P06 demo, with the data in lists
   // dart run bin/main.dart --race   -> two guests book the same room at the same moment
   import 'dart:async';
   import 'dart:io';
   
   import 'package:hotel_booking/api.dart';
   import 'package:hotel_booking/api_cache.dart';
   import 'package:hotel_booking/notifications/src/email.dart';
   import 'package:hotel_booking/reservations/reservations.dart';
   import 'package:hotel_booking/reservations/src/postgres_store.dart';
   import 'package:hotel_booking/reservations/src/store.dart';
   import 'package:hotel_booking/rooms/rooms.dart';
   import 'package:hotel_booking/rooms/src/catalog.dart';
   import 'package:hotel_booking/rooms/src/postgres_catalog.dart';
   import 'package:postgres/postgres.dart' show Endpoint, Pool, PoolSettings, SslMode;
   import 'package:redis/redis.dart' show RedisConnection;
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
     // Step 1 widens the gap between the check and the save, so the race happens on every run.
     final slowStep =
         Duration(milliseconds: (double.parse(Platform.environment['HOTEL_SLOW_STEP'] ?? '0') * 1000).round());
   
     // One request: a fresh store, counted for GET /stats; save what changed at the end.
     Future<T> unitOfWork<T>(FutureOr<T> Function(BookingService service) work) async {
       final store = PostgresReservationStore(pool, rooms);
       final result = await work(
           BookingService(rooms, CountingReservationStore(store, stats), email, slowStep: slowStep));
       await store.saveChanges();
       return result;
     }
   
     // Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
     final apiPort = int.parse(Platform.environment['HOTEL_PORT'] ?? '5080');
     await serve(createApi(rooms, unitOfWork, stats, cache), 'localhost', apiPort);
     print('Listening on http://localhost:$apiPort');
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
3. White box 3: Maya’s overlap check. It asks for confirmed reservations of R7 that share at least one night with 12-14 November, and gets 0. This is the pre-check inside BookingService.reserve:

   C#:
   
   ```text
   public BookingResult Reserve(Guest guest, Room room, DateRange stay)
   {
       if (!stay.IsValid()) return new("InvalidDates");
       if (store.HasOverlap(room, stay)) return new("Unavailable");
   
       Thread.Sleep(slowStepMs); // 0 normally; the race tool makes this step slow
   
       var reservation = new Reservation(store.NextReservationId(), guest, room, stay);
       try
       {
           store.Save(reservation);
       }
       catch (OverlapRejectedException)
       {
           // Another request saved an overlapping reservation after our check and before our save.
           // The check was honest when it ran; it had simply stopped being true. The guest gets
           // the same answer either way.
           return new("Unavailable");
       }
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
   ```
   
   Python:
   
   ```text
   def reserve(self, guest: Guest, room: Room, stay: DateRange) -> BookingResult:
       if not stay.is_valid():
           return BookingResult("InvalidDates")
       if self.store.has_overlap(room, stay):
           return BookingResult("Unavailable")
   
       time.sleep(self.slow_step_seconds)  # 0 normally; the race tool makes this step slow
   
       reservation = Reservation(self.store.next_reservation_id(), guest, room, stay)
       try:
           self.store.save(reservation)
       except OverlapRejected:
           # Another request saved an overlapping reservation after our check and before our save.
           # The check was honest when it ran; it had simply stopped being true. The guest gets the
           # same answer either way.
           return BookingResult("Unavailable")
       try:
           # Notifications gets its own message: the five fields it needs, not the Reservation.
           self.notifications.send_confirmation(
               Confirmation(reservation.id, guest.email, room.number, stay.check_in, stay.check_out))
       except Exception:
           # R6: the booking is already saved, so a failed message must not undo or hide it.
           return BookingResult("Accepted", reservation.id, notification_failed=True)
       return BookingResult("Accepted", reservation.id)
   ```
   
   Java:
   
   ```text
   public BookingResult reserve(Guest guest, Room room, DateRange stay) {
       if (!stay.isValid()) return new BookingResult("InvalidDates");
       if (store.hasOverlap(room, stay)) return new BookingResult("Unavailable");
   
       pause(); // does nothing normally; the race demo makes this step slow
   
       Reservation reservation = new Reservation(store.nextReservationId(), guest, room, stay);
       try {
           store.save(reservation);
       } catch (OverlapRejectedException rejected) {
           // Another request saved an overlapping reservation after our check and before our save.
           // The check was honest when it ran; it had simply stopped being true. The guest gets the
           // same answer either way.
           return new BookingResult("Unavailable");
       }
       try {
           // Notifications gets its own message: the five fields it needs, not the Reservation.
           notifications.sendConfirmation(new Confirmation(reservation.id(), guest.email(), room.number(), stay.checkIn(), stay.checkOut()));
       } catch (RuntimeException failure) {
           // R6: the booking is already saved, so a failed message must not undo or hide it.
           return new BookingResult("Accepted", reservation.id(), true);
       }
       return new BookingResult("Accepted", reservation.id());
   }
   ```
   
   Go:
   
   ```text
   func (service *BookingService) Reserve(guest Guest, room rooms.Room, stay DateRange) BookingResult {
   	if !stay.IsValid() {
   		return BookingResult{Outcome: "InvalidDates"}
   	}
   	if service.store.HasOverlap(room, stay) {
   		return BookingResult{Outcome: "Unavailable"}
   	}
   
   	time.Sleep(service.slowStep) // zero normally; the race tool makes this step slow
   
   	reservation := NewReservation(service.store.NextReservationID(), guest, room, stay)
   	if err := service.store.Save(reservation); errors.Is(err, ErrOverlapRejected) {
   		// Another request saved an overlapping reservation after our check and before our save.
   		// The check was honest when it ran; it had simply stopped being true. The guest gets the
   		// same answer either way.
   		return BookingResult{Outcome: "Unavailable"}
   	}
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
   ```
   
   JavaScript:
   
   ```text
   async reserve(guest, room, stay) {
     if (!stay.isValid()) return new BookingResult('InvalidDates');
     if (await this.store.hasOverlap(room, stay)) return new BookingResult('Unavailable');
   
     await wait(this.slowStepMs); // zero normally; the race tool makes this step slow
   
     const reservation = new Reservation(await this.store.nextReservationId(), guest, room, stay);
     try {
       await this.store.save(reservation);
     } catch (error) {
       // Another request saved an overlapping reservation after our check and before our save. The
       // check was honest when it ran; it had simply stopped being true.
       if (error instanceof OverlapRejected) return new BookingResult('Unavailable');
       throw error;
     }
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
   ```
   
   Dart:
   
   ```text
   Future<BookingResult> reserve(Guest guest, Room room, DateRange stay) async {
     if (!stay.isValid()) return BookingResult('InvalidDates');
     if (await store.hasOverlap(room, stay)) return BookingResult('Unavailable');
   
     // A pause that lets other requests run while this one waits.
     await Future<void>.delayed(slowStep);
   
     final reservation = Reservation(await store.nextReservationId(), guest, room, stay);
     try {
       await store.save(reservation);
     } on OverlapRejected {
       // Another request saved an overlapping reservation after our check and before our save. The
       // check was honest when it ran; it had simply stopped being true.
       return BookingResult('Unavailable');
     }
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
   ```
4. White box 4: Omar asks exactly the same question and also gets 0. This is the part people find hardest to accept. Omar’s answer is not stale or wrong: Maya has not inserted anything yet, so there is genuinely nothing to find.
5. White box 5: Maya asks the sequence for an id and sends her INSERT. The row now exists inside Maya's transaction only. Until she commits, no other session can see it - not even with a fresh query.
6. White box 6: Omar gets a different id, because nextval never hands the same number to two callers, and sends his INSERT as well. Before P09 the id came from counting the rows, and both requests got R101 - a second race hiding behind this one. The sequence removes it:

   sql:
   
   ```text
   -- P09 Step 1: reservation ids come from here, not from counting the rows.
   -- nextval never returns the same number twice, even to two transactions running at the same moment.
   CREATE SEQUENCE reservation_ids START 101;
   
   -- The running database needs it too, because init.sql only runs when the database is created.
   -- Start it above the ids that already exist, so it never collides with them.
   SELECT setval('reservation_ids', GREATEST(101, (SELECT count(*) FROM reservations) + 101), false);
   ```
7. White box 7 and white box 8: both transactions commit, in that order. The database checks everything it has been told to check - the primary key, the status values, that check-out is after check-in - and none of those say anything about two reservations overlapping.
8. The red dashed arrow, and the box it points at — “R7 now has two confirmed reservations for the same nights” — are the one thing here that should not exist. Omar’s commit succeeds when it should have been refused:

   text:
   
   ```text
   Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.
   
   Maya -> 201 Created   {"id":"R101","status":"Confirmed","notificationFailed":false}
   Omar -> 201 Created   {"id":"R102","status":"Confirmed","notificationFailed":false}
   
   Confirmed reservations for R7 on 12-14 Nov: 2
   DOUBLE BOOKED: the room has two confirmed reservations for the same nights.
   ```
9. Looking at the reservations table afterwards shows it plainly:

   sql:
   
   ```text
     id  | room |  check_in  | check_out  |  status
   ------+------+------------+------------+-----------
    R101 | R7   | 2026-11-12 | 2026-11-14 | Confirmed
    R102 | R7   | 2026-11-12 | 2026-11-14 | Confirmed
   (2 rows)
   ```

**Legend:**
- **Two columns with headings.** One column per request. Reading down a column follows one guest; reading across compares what the two were doing at the same moment. In this diagram: The left column is Maya’s request and the right column is Omar’s. They are two separate HTTP requests, two connections and two transactions.
- **Yellow box.** Something outside the app, or the step where it goes wrong. In this diagram: The two POST requests at the top, and the broken outcome at the bottom.
- **White box.** An ordinary step in the sequence: one request doing one thing. In this diagram: Each statement one request sends to the database: the overlap check, the id, the INSERT and the COMMIT.
- **Solid line with a filled arrowhead.** What happens next in that request. Follow these in order. In this diagram: What that request does next. Maya’s chain and Omar’s chain are independent; neither waits for the other.
- **Red dashed line with an open arrowhead.** The case that goes wrong, or the one the design refuses. In this diagram: Omar’s commit succeeding. It is the one thing in this diagram that should not have been allowed.
- **Number in front of a box label.** The order things happen in, across both columns: 1 first, then 2, and so on. In this diagram: 1 and 2 are the two requests arriving, 3 and 4 the two checks, 5 and 6 the two inserts, 7 and 8 the two commits.

### The same thing as a table

| # | Maya’s request | Omar’s request | What the database holds |
| --- | --- | --- | --- |
| 1 | BEGIN |  | no confirmed reservation for R7 |
| 2 | SELECT count(*) → 0 |  | unchanged |
| 3 |  | BEGIN | unchanged |
| 4 |  | SELECT count(*) → 0 | unchanged: Maya has inserted nothing yet |
| 5 | INSERT R101 |  | R101 exists, uncommitted and invisible to Omar |
| 6 |  | INSERT R102 | both rows exist, each invisible to the other |
| 7 | COMMIT |  | R101 is now visible to everyone |
| 8 |  | COMMIT | R101 and R102 are both Confirmed for the same nights |

### Which anomaly this is

| Question | Answer |
| --- | --- |
| Is it a lost update? | No. Neither request overwrote the other’s row; they inserted different rows. |
| So what is it? | Write skew: both read the same set, found it empty, and then each inserted something new. |
| Does Repeatable Read stop it? | No. Repeatable Read promises that a row you read does not change under you, and no row either request read changed. |
| What does? | Serializable does, by spotting the pattern and aborting one transaction. So does each of today’s four repairs, in its own way. |
| Why not SELECT … FOR UPDATE on the reservations? | There is no overlapping reservation to lock at check time. You cannot lock a row that does not exist yet. |

**Why this is write skew and not a lost update** In short: Neither request overwrote the other. They each read the same empty set and then inserted a different row. A lost update is two transactions writing the same row, where the second one’s write replaces the first. Repeatable Read catches that, because it notices the row you read has changed under you. Here nothing either request read ever changed. The overlap check read a set of rows — the confirmed reservations overlapping the stay — and that set was empty for both, and stayed empty until both had already decided. Raising the isolation level to Repeatable Read does not help. Serializable does, by spotting the pattern and aborting one transaction.

1. Start: R7 has no confirmed reservations for 12–14 November.
2. Action: both requests run SELECT count(*) and get 0, then both INSERT different rows and commit.
3. Result: two confirmed reservations. No row was overwritten, so there is no lost update for Repeatable Read to find.

**Why locking the reservations cannot fix it** In short: SELECT … FOR UPDATE locks the rows a query returns, and at check time the query returns none. This is the repair everybody reaches for first. It does nothing here: there is no overlapping reservation yet, so there is no row to lock, and locking nothing protects nothing. That is exactly why write skew is its own anomaly. The thing you need to stop from existing does not exist yet, so you cannot point a lock at it. Steps 5 and 6 get round it by locking a row that always exists — one per room, kept only to be locked.

1. Start: R7 has no reservations, and a booking runs SELECT … FOR UPDATE over its overlapping reservations.
2. Action: a second booking runs the same statement.
3. Result: neither waits, because neither query matched a row. Both go on to insert.

### Completed example: docs/interleaving.md

This is the finished file, with the measurements from Steps 4, 5 and 6 already filled in. Write the first half today and come back to it as you go.

### docs/interleaving.md

```text
# The interleaving that double-books R7

## What I ran

`tools/race-http` sends two `POST /reservations` for room R7, 2026-11-12 to
2026-11-14, at the same moment: one for Maya, one for Omar.

```
Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.

Maya -> 201 Created   {"id":"R101","status":"Confirmed","notificationFailed":false}
Omar -> 201 Created   {"id":"R102","status":"Confirmed","notificationFailed":false}

Confirmed reservations for R7 on 12-14 Nov: 2
DOUBLE BOOKED: the room has two confirmed reservations for the same nights.
```

## The interleaving, statement by statement

| # | Maya's request | Omar's request | What the database holds |
|---|---|---|---|
| 1 | `BEGIN` | | no confirmed reservation for R7 |
| 2 | `SELECT count(*)` -> 0 | | unchanged |
| 3 | | `BEGIN` | unchanged |
| 4 | | `SELECT count(*)` -> 0 | unchanged: Maya has inserted nothing yet |
| 5 | `INSERT R101` | | R101 exists, uncommitted and invisible to Omar |
| 6 | | `INSERT R102` | both rows exist, each invisible to the other |
| 7 | `COMMIT` | | R101 is now visible to everyone |
| 8 | | `COMMIT` | R101 and R102 are both Confirmed for the same nights |

Both requests read "no clash", and both were telling the truth when they read
it. The gap between step 2 and step 5 is the whole bug: the check and the save
are two separate statements, and another transaction fits between them.

## Which anomaly this is

Write skew. Each transaction reads a set of rows (the confirmed reservations
that overlap the stay), finds it empty, and then **inserts a different row**.
There is no lost update, because neither transaction overwrites the other's
row. Raising the isolation level to Repeatable Read does not stop it: Repeatable
Read guarantees that a row you read does not change under you, and no row that
either transaction read changed. Serializable does stop it, by detecting the
dangerous read-write pattern and aborting one transaction with a
serialization failure.

## The invariant this breaks

> A room never has two Confirmed reservations covering the same night.

## Why locking the reservation rows cannot fix it

The obvious repair is `SELECT ... FOR UPDATE` on the overlapping reservations.
It does nothing here, because at step 2 **there are no overlapping reservations
to lock**. `FOR UPDATE` locks the rows a query returns, and that query returns
none. You cannot lock a row that does not exist yet. That is exactly why write
skew is its own anomaly and not a lost update, and it is why Step 5 locks a
guard row instead.

## The four repairs, and what each one cost

| Repair | Did it stop the double booking? | What it cost, measured |
|---|---|---|
| In-process lock | With one instance, yes. With two, no. | Nothing, and it is worth nothing: the moment there are two processes the rule is broken again. |
| `SELECT ... FOR UPDATE` on a guard row | Yes | Omar waited **0.91 seconds** at his `FOR UPDATE` for a room he was never going to get. Every booking for a room queues behind the one in front, for as long as that transaction lasts. |
| Optimistic version checking | Yes | **Nobody waited.** Both sessions read `version = 0` without taking a lock; the loser's `UPDATE ... WHERE version = 0` matched 0 rows, so it rolled back and did its work a second time. Cheap when clashes are rare, wasteful when they are common. |
| Exclusion constraint | Yes | Only covers a rule that fits inside one table; the error arrives as a constraint violation the app must turn into a readable 409; and it could not be switched on until the rows that already broke it were cleaned up. |

The two middle repairs are the pessimistic and the optimistic answer to the same
question, and the timings say exactly how they differ: one makes the loser
**wait**, the other makes the loser **redo**. Neither is wrong. Which one is
better depends on how often two guests really do reach for the same room at the
same moment.

## What it took to turn the constraint on

The constraint could not simply be added. The table already held the two
confirmed reservations Step 1 created, and PostgreSQL refuses to create a
constraint that the existing rows already break:

```
ERROR:  could not create exclusion constraint "reservations_no_overlap"
DETAIL:  Key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-12,2026-11-14)) conflicts with key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-12,2026-11-14)).
```

So one of the two bookings had to be cancelled first. That is not an artefact of
the exercise - it is what adding a constraint to a real database is always like.
The rule you are about to enforce has been broken for as long as it went
unenforced, and the broken rows are still there. Finding them, and deciding what
to do about them, is part of the work; in a real hotel somebody would have to
telephone one of the two guests.

## What I kept

The exclusion constraint, with the pre-check left in front of it. The constraint
is the only one of the four that no code path can forget: a new endpoint, a
background job, a migration script or somebody typing `INSERT` into `psql` all
hit it. The pre-check stays so that the ordinary case still answers a clean
`409 Unavailable` with a readable message, instead of the app having to explain
a database error to a guest.

```

### Your turn · write the trace

1. Create docs/interleaving.md in your project folder.
2. Add the heading # The interleaving that double-books R7, and paste the output of your own race tool under “What I ran”.
3. Write the eight-row table of what each request did, in time order. Use your own run, not the example.
4. Write one line naming the anomaly and one line saying why Repeatable Read does not stop it.
5. Write one line answering: why does SELECT … FOR UPDATE on the reservations not help?
6. Leave a heading “The four repairs, and what each one cost” with an empty table. You will fill it in as you go.

**When you finish, you have:** docs/interleaving.md with your own race output, the eight-row trace, the anomaly named, and an empty table ready for the four repairs.
### Explanation

Steps 3 to 7 each close the gap between row 2 and row 5 in a different place. Writing the trace first is what lets you say, at the end, which one you chose and why — which is all an ADR really is.



---

## 4. Step 3 · A lock inside the app

The first repair everybody reaches for is a lock, and your project already has one: the list store checks and appends while holding it. Run --race and watch it work.





### What a lock is, and is not

|  |  |
| --- | --- |
| What it protects | One process’s own threads. While a thread holds the lock, no other thread of that program can be in the same piece of code. |
| Where yours is | In the list store: save checks for an overlap and appends while holding the lock. That is why `--race` is safe. |
| What it costs | Almost nothing. One lock, held for the length of a check and an append. |
| What it knows about a second copy of the app | Nothing at all. Step 4 is where that matters. |

**Why a lock inside the app is not enough** In short: A lock lives in one process. Run a second copy of the app and it has its own, and neither waits for the other. The list store’s lock really does work: --race is accepted by one guest and refused for the other, inside one process. Then the same race is sent to two instances sharing one PostgreSQL, and both bookings are accepted again. Nothing about the lock changed; there is simply a second one now. A rule has to be enforced somewhere every writer must pass through. For one process that can be a lock. For several it has to be the thing they share, which is the database — and that is the whole argument for the constraint.

1. Start: two instances on ports 5080 and 5081, one database, no constraint yet.
2. Action: the race tool sends Maya to 5080 and Omar to 5081.
3. Result:
Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.
Maya -> http://localhost:5080   Omar -> http://localhost:5081

Maya -> 201 Created   {"id":"R101","status":"Confirmed","notificationFailed":false}
Omar -> 201 Created   {"id":"R102","status":"Confirmed","notificationFailed":false}

Confirmed reservations for R7 on 12-14 Nov: 2
DOUBLE BOOKED: the room has two confirmed reservations for the same nights.

Add the constraint and run the very same commands again:
Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.
Maya -> http://localhost:5080   Omar -> http://localhost:5081

Maya -> 201 Created   {"id":"R101","status":"Confirmed","notificationFailed":false}
Omar -> 409 Conflict  {"title":"Unavailable","status":409,"detail":"The room is booked for at least one night of this stay."}

Confirmed reservations for R7 on 12-14 Nov: 1
CORRECT: one booking was accepted and one was refused.

### Completed example 3.1 · the list store checks under its lock

In C#, Java, Python and Go this needs a real lock, because those runtimes serve requests on several threads at once. In JavaScript and Dart it needs none: they run one request’s code at a time, and there is no await between the check and the append, so nothing can fit in the middle.

### The list store’s save

#### C#

```csharp
public void Save(Reservation reservation)
{
    lock (_gate)
    {
        if (_rows.Any(row => row.Blocks(reservation.Room, reservation.Stay)))
            throw new OverlapRejectedException($"{reservation.Room.Number} already has a confirmed reservation for these nights.");
        _rows.Add(reservation);
    }
}
```

#### Python

```python
def save(self, reservation: Reservation) -> None:
    with self._lock:
        if self.has_overlap(reservation.room, reservation.stay):
            raise OverlapRejected(
                f"{reservation.room.number} already has a confirmed reservation for these nights.")
        self._rows.append(reservation)
```

#### Java

```java
@Override
public void save(Reservation reservation) {
    synchronized (gate) {
        if (rows.stream().anyMatch(row -> row.blocks(reservation.room(), reservation.stay()))) {
            throw new OverlapRejectedException(
                    reservation.room().number() + " already has a confirmed reservation for these nights.");
        }
        rows.add(reservation);
    }
}
```

#### Go

```go
func (store *InMemoryReservationStore) Save(reservation *Reservation) error {
	store.mutex.Lock()
	defer store.mutex.Unlock()
	if store.hasOverlap(reservation.Room, reservation.Stay) {
		return fmt.Errorf("%w: %s", ErrOverlapRejected, reservation.Room.Number)
	}
	store.rows = append(store.rows, reservation)
	return nil
}
```

#### JavaScript

```javascript
// No lock, and none is needed: Node runs one request's code at a time, and there is no await
// between the check and the push, so no other request can run in the middle of this method.
// This check is the list store's version of the exclusion constraint.
save(reservation) {
  if (this.hasOverlap(reservation.room, reservation.stay)) {
    throw new OverlapRejected(`${reservation.room.number} already has a confirmed reservation for these nights.`);
  }
  this.#rows.push(reservation);
}
```

#### Dart

```dart
// No lock, and none is needed: Dart runs one request's code at a time on this isolate, and there
// is no await between the check and the add, so no other request can run in the middle of this
// method. This check is the list store's version of the exclusion constraint.
@override
void save(Reservation reservation) {
  if (_rows.any((row) => row.blocks(reservation.room, reservation.stay))) {
    throw OverlapRejected(
        '${reservation.room.number} already has a confirmed reservation for these nights.');
  }
  _rows.add(reservation);
}
```

### Run it

This uses the list store, inside one process. No database is involved.

### One terminal

#### C#

```text
dotnet run --project HotelBooking -- --race
```

#### Python

```text
python main.py --race
```

#### Java

```text
java -jar target/hotel-booking-1.0.jar --race
```

#### Go

```text
go run . --race
```

#### JavaScript

```text
node main.js --race
```

#### Dart

```text
dart run bin/main.dart --race
```

### What you will see

Before P09 this printed “Double booking!”. The list store now keeps the same promise the database will keep in Step 7, so one guest is refused.

### Terminal

```text
Maya and Omar both book R7 for 12-14 Nov at the same moment...
Maya: Accepted R101
Omar: Unavailable
Confirmed reservations for R7 on 12-14 Nov: 1
Only one guest has R7.
```

### Your turn · watch the lock work

1. Open the list store in your editor and find save. Read how it checks and then appends.
2. Run the --race command for your language.
3. Check that it now prints “Only one guest has R7.” rather than “Double booking!”.
4. In docs/interleaving.md, add a row to the four-repairs table: In-process lock · stopped it? · what it cost. Leave the “stopped it?” answer until after Step 4.

**When you finish, you have:** --race printing “Only one guest has R7.”, and the first row of your four-repairs table started.
### Explanation

This is a real repair and it really works — inside one process. Step 4 is not about the lock being written badly. It is about where the lock lives.



---

## 5. Step 4 · Start a second instance, and watch it fail

docs/capacity.md already assumes the hotel can add instances. Start the app twice against one database and send the same race at both.





### What changes, and what does not

|  |  |
| --- | --- |
| The code | Nothing. Not one line. |
| What is running | Two copies of the app, on ports 5080 and 5081, sharing one PostgreSQL and one Redis. |
| The lock | Still there, still working — and now there are two of them, one per process, with no connection between them. |
| Which store is in use | The PostgreSQL store, not the list store. The lock you watched in Step 3 is not even on this path. |

**Why a lock inside the app is not enough** In short: A lock lives in one process. Run a second copy of the app and it has its own, and neither waits for the other. The list store’s lock really does work: --race is accepted by one guest and refused for the other, inside one process. Then the same race is sent to two instances sharing one PostgreSQL, and both bookings are accepted again. Nothing about the lock changed; there is simply a second one now. A rule has to be enforced somewhere every writer must pass through. For one process that can be a lock. For several it has to be the thing they share, which is the database — and that is the whole argument for the constraint.

1. Start: two instances on ports 5080 and 5081, one database, no constraint yet.
2. Action: the race tool sends Maya to 5080 and Omar to 5081.
3. Result:
Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.
Maya -> http://localhost:5080   Omar -> http://localhost:5081

Maya -> 201 Created   {"id":"R101","status":"Confirmed","notificationFailed":false}
Omar -> 201 Created   {"id":"R102","status":"Confirmed","notificationFailed":false}

Confirmed reservations for R7 on 12-14 Nov: 2
DOUBLE BOOKED: the room has two confirmed reservations for the same nights.

Add the constraint and run the very same commands again:
Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.
Maya -> http://localhost:5080   Omar -> http://localhost:5081

Maya -> 201 Created   {"id":"R101","status":"Confirmed","notificationFailed":false}
Omar -> 409 Conflict  {"title":"Unavailable","status":409,"detail":"The room is booked for at least one night of this stay."}

Confirmed reservations for R7 on 12-14 Nov: 1
CORRECT: one booking was accepted and one was refused.

### Run it

Three terminals: two apps and the tool. Omar’s request is sent to the second instance.

### Three terminals

#### C#

```text
# terminal 1
$env:HOTEL_SLOW_STEP = "1"; dotnet run --project HotelBooking
# terminal 2
$env:HOTEL_SLOW_STEP = "1"; $env:HOTEL_PORT = "5081"; dotnet run --project HotelBooking
# terminal 3
dotnet run tools/race-http.cs http://localhost:5081
```

#### Python

```text
# terminal 1
$env:HOTEL_SLOW_STEP = "1"; python main.py
# terminal 2
$env:HOTEL_SLOW_STEP = "1"; $env:HOTEL_PORT = "5081"; python main.py
# terminal 3
python tools/race_http.py http://localhost:5081
```

#### Java

```text
# terminal 1
$env:HOTEL_SLOW_STEP = "1"; mvn -q spring-boot:run
# terminal 2
$env:HOTEL_SLOW_STEP = "1"; $env:HOTEL_PORT = "5081"; mvn -q spring-boot:run
# terminal 3
java tools/RaceHttp.java http://localhost:5081
```

#### Go

```text
# terminal 1
$env:HOTEL_SLOW_STEP = "1"; go run .
# terminal 2
$env:HOTEL_SLOW_STEP = "1"; $env:HOTEL_PORT = "5081"; go run .
# terminal 3
go run ./tools/race http://localhost:5081
```

#### JavaScript

```text
# terminal 1
$env:HOTEL_SLOW_STEP = "1"; node main.js
# terminal 2
$env:HOTEL_SLOW_STEP = "1"; $env:HOTEL_PORT = "5081"; node main.js
# terminal 3
node tools/race-http.js http://localhost:5081
```

#### Dart

```text
# terminal 1
$env:HOTEL_SLOW_STEP = "1"; dart run bin/main.dart
# terminal 2
$env:HOTEL_SLOW_STEP = "1"; $env:HOTEL_PORT = "5081"; dart run bin/main.dart
# terminal 3
dart run tool/race_http.dart http://localhost:5081
```

### What you will see

The double booking is back.

### Terminal 3

```text
Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.
Maya -> http://localhost:5080   Omar -> http://localhost:5081

Maya -> 201 Created   {"id":"R101","status":"Confirmed","notificationFailed":false}
Omar -> 201 Created   {"id":"R102","status":"Confirmed","notificationFailed":false}

Confirmed reservations for R7 on 12-14 Nov: 2
DOUBLE BOOKED: the room has two confirmed reservations for the same nights.
```

### Why

A lock is a promise about one program’s own threads. A second copy of the program has a second lock, in its own memory, and neither waits for the other. A rule guarded by two separate locks is not guarded at all.

And on this path the lock was never involved anyway: the real app uses the PostgreSQL store, where the rule is enforced by nothing whatsoever.

### Your turn · break it again

1. Start the app in terminal 1 with HOTEL_SLOW_STEP=1, and a second copy in terminal 2 with HOTEL_PORT=5081 as well.
2. Check both answer: open http://localhost:5080/stats and http://localhost:5081/stats in your browser.
3. Run the race tool in terminal 3, pointing Omar at http://localhost:5081.
4. Count the confirmed reservations in psql again.
5. In docs/interleaving.md, finish the in-process lock row: “With one instance, yes. With two, no.”
6. Write one line under the table: where does a rule have to live if several processes must obey it?

**When you finish, you have:** A second instance running, the double booking reproduced across two processes, and the first row of your table finished.
### Explanation

The answer to the question in the last step is: in the thing they share. That is the database, and Steps 5 to 7 are three ways of putting it there.



---

## 6. Step 5 · SELECT … FOR UPDATE on a guard row

Move the lock into the database. You cannot lock an overlapping reservation, because at check time there is not one — so lock a row that always exists, one per room.





### Why a guard row at all

| Question | Answer |
| --- | --- |
| Why not lock the reservations? | FOR UPDATE locks the rows a query returns, and the overlap query returns none. Locking nothing protects nothing. |
| What is a guard row? | One row per room whose only job is to be locked. Because it always exists, the lock always has a target. |
| Who owns the table? | Reservations. The module boundary from L03 still holds: Reservations never writes the rooms table, which belongs to Rooms. |
| What happens to the second booking? | It waits at its FOR UPDATE until the first transaction ends, then re-reads and finds the clash. |

**Why locking the reservations cannot fix it** In short: SELECT … FOR UPDATE locks the rows a query returns, and at check time the query returns none. This is the repair everybody reaches for first. It does nothing here: there is no overlapping reservation yet, so there is no row to lock, and locking nothing protects nothing. That is exactly why write skew is its own anomaly. The thing you need to stop from existing does not exist yet, so you cannot point a lock at it. Steps 5 and 6 get round it by locking a row that always exists — one per room, kept only to be locked.

1. Start: R7 has no reservations, and a booking runs SELECT … FOR UPDATE over its overlapping reservations.
2. Action: a second booking runs the same statement.
3. Result: neither waits, because neither query matched a row. Both go on to insert.

**The guard row** In short: One row per room, whose only job is to be locked, so the lock has somewhere to live even when the room has no reservations. Reservations owns this table, so the module boundary from L03 still holds: Reservations never writes the rooms table, which belongs to Rooms. It works, and it is measured: the losing request sat at its FOR UPDATE for 0.91 seconds waiting for a room it was never going to get. Every booking for a room queues behind the one in front of it, for as long as that transaction lasts.

1. Start: the guard table has one row for R7 and one for R8, both version 0.
2. Action: two bookings for R7 each run SELECT … FOR UPDATE on the R7 guard row.
3. Result:
 0.03s  Maya   BEGIN
 0.03s  Maya   SELECT ... FOR UPDATE returned, so Maya holds the lock on R7
 0.03s  Maya   overlap check sees 0
 0.12s  Omar   BEGIN
 0.12s  Omar   SELECT ... FOR UPDATE  ... waiting ...
 1.03s  Maya   INSERT R101
 1.03s  Maya   COMMIT, which releases the lock
 1.03s  Omar   ... the wait ends, Omar now holds the lock
 1.03s  Omar   overlap check sees 1
 1.03s  Omar   refuses: Unavailable

Confirmed reservations for R7: 1
Omar waited 0.91 seconds for a room he was never going to get.

sql:

```text
-- P09 Step 5: one row per room for the Reservations module to lock.
-- Reservations owns this table, so the module boundary of L03 still holds: Reservations never
-- writes the rooms table, which belongs to Rooms.
CREATE TABLE room_booking_guard (
    room    text PRIMARY KEY,
    version integer NOT NULL DEFAULT 0
);

INSERT INTO room_booking_guard (room) VALUES ('R7'), ('R8');
```

### Completed example 5.1 · the guard table

**The guard row** In short: One row per room, whose only job is to be locked, so the lock has somewhere to live even when the room has no reservations. Reservations owns this table, so the module boundary from L03 still holds: Reservations never writes the rooms table, which belongs to Rooms. It works, and it is measured: the losing request sat at its FOR UPDATE for 0.91 seconds waiting for a room it was never going to get. Every booking for a room queues behind the one in front of it, for as long as that transaction lasts.

1. Start: the guard table has one row for R7 and one for R8, both version 0.
2. Action: two bookings for R7 each run SELECT … FOR UPDATE on the R7 guard row.
3. Result:
 0.03s  Maya   BEGIN
 0.03s  Maya   SELECT ... FOR UPDATE returned, so Maya holds the lock on R7
 0.03s  Maya   overlap check sees 0
 0.12s  Omar   BEGIN
 0.12s  Omar   SELECT ... FOR UPDATE  ... waiting ...
 1.03s  Maya   INSERT R101
 1.03s  Maya   COMMIT, which releases the lock
 1.03s  Omar   ... the wait ends, Omar now holds the lock
 1.03s  Omar   overlap check sees 1
 1.03s  Omar   refuses: Unavailable

Confirmed reservations for R7: 1
Omar waited 0.91 seconds for a room he was never going to get.

sql:

```text
-- P09 Step 5: one row per room for the Reservations module to lock.
-- Reservations owns this table, so the module boundary of L03 still holds: Reservations never
-- writes the rooms table, which belongs to Rooms.
CREATE TABLE room_booking_guard (
    room    text PRIMARY KEY,
    version integer NOT NULL DEFAULT 0
);

INSERT INTO room_booking_guard (room) VALUES ('R7'), ('R8');
```

### In psql

```text
-- P09 Step 5: one row per room for the Reservations module to lock.
-- Reservations owns this table, so the module boundary of L03 still holds: Reservations never
-- writes the rooms table, which belongs to Rooms.
CREATE TABLE room_booking_guard (
    room    text PRIMARY KEY,
    version integer NOT NULL DEFAULT 0
);

INSERT INTO room_booking_guard (room) VALUES ('R7'), ('R8');
```

### Completed example 5.2 · two terminals, side by side

Run terminal A down to its overlap check, then run all of terminal B, then finish terminal A. Terminal B will stop at its FOR UPDATE and stay stopped.

### Terminal A — Maya

```text
BEGIN;

-- Take the room's guard row. Any other session that asks for the same row now waits here.
SELECT version FROM room_booking_guard WHERE room = 'R7' FOR UPDATE;

SELECT count(*) FROM reservations
WHERE room = 'R7' AND status = 'Confirmed'
  AND check_in < '2026-11-14' AND '2026-11-12' < check_out;
-- 0, so this booking may go ahead

-- >>> now start terminal B and watch it stop at its FOR UPDATE <<<

INSERT INTO reservations (id, guest_name, guest_email, room, check_in, check_out, status)
VALUES ('R101', 'Maya', 'maya@example.com', 'R7', '2026-11-12', '2026-11-14', 'Confirmed');

COMMIT;   -- the guard row's lock is released here, and only now does terminal B move
```

### 

### Terminal B — Omar

```text
BEGIN;

-- This is where terminal B stops and waits, for as long as terminal A's transaction lasts.
SELECT version FROM room_booking_guard WHERE room = 'R7' FOR UPDATE;

SELECT count(*) FROM reservations
WHERE room = 'R7' AND status = 'Confirmed'
  AND check_in < '2026-11-14' AND '2026-11-12' < check_out;
-- 1  <-- the wait is why this is 1 and not 0: Maya's row is committed and visible

-- so this booking is refused, and nothing is inserted
COMMIT;
```

### What you will see

These are the timings from one laptop, with terminal A holding its transaction open for a second. The number that matters is the wait.

### Both terminals, in time order

```text
 0.03s  Maya   BEGIN
 0.03s  Maya   SELECT ... FOR UPDATE returned, so Maya holds the lock on R7
 0.03s  Maya   overlap check sees 0
 0.12s  Omar   BEGIN
 0.12s  Omar   SELECT ... FOR UPDATE  ... waiting ...
 1.03s  Maya   INSERT R101
 1.03s  Maya   COMMIT, which releases the lock
 1.03s  Omar   ... the wait ends, Omar now holds the lock
 1.03s  Omar   overlap check sees 1
 1.03s  Omar   refuses: Unavailable

Confirmed reservations for R7: 1
Omar waited 0.91 seconds for a room he was never going to get.
```

### Your turn · make the second booking wait

1. In psql, create the guard table and its two rows (5.1).
2. Open a second psql terminal, so you have A and B side by side.
3. Run terminal A down to and including its overlap check. Leave the transaction open.
4. Run terminal B. Watch the prompt not come back: it is waiting at the FOR UPDATE.
5. Finish terminal A with its INSERT and COMMIT. Watch terminal B’s SELECT return the instant A commits.
6. In terminal B, run the overlap check: it now says 1, not 0. Commit, and nothing is inserted.
7. Add the FOR UPDATE row to your four-repairs table, with how long your terminal B waited.

**When you finish, you have:** A guard table in your database, terminal B observed waiting and then refusing, and the second row of your table filled in.
### Explanation

It works, and the cost is in plain sight: the losing request waited almost a second for a room it was never going to get. Every booking for a room queues behind the one in front, for as long as that transaction lasts — including any email or payment call inside it.



---

## 7. Step 6 · Optimistic version checking

The same guard row, without anybody waiting. Read the version, do the work, and write only if the version is still what you read.





### Pessimistic and optimistic, side by side

|  | SELECT … FOR UPDATE (Step 5) | Version check (Step 6) |
| --- | --- | --- |
| What the read does | Takes a lock. Everybody else waits. | Takes nothing. Nobody waits. |
| How the loser finds out | It does not lose — it waits, then sees the truth. | Its UPDATE matches 0 rows at the end. |
| What the loser does | Carries on, having waited. | Rolls back and starts the whole thing again. |
| Measured cost | The loser waited 0.91 seconds. | Nobody waited; the loser did its work twice. |
| When it is the better bet | When two guests often want the same room at once. | When they almost never do — which is a hotel. |

**Optimistic version checking** In short: Read the version, do the work, then write only if the version is still what you read. The one that lost finds out at the end and starts again. Both requests read version 0 without taking a lock, so neither waits. Only one UPDATE … WHERE version = 0 can match, because the first one to run moves the version on; the other matches zero rows, which is not an error, just a fact. Compare the two measurements. FOR UPDATE made the loser wait 0.91 seconds and then do its work once. This makes nobody wait, and the loser does its work twice. Which is cheaper depends entirely on how often two guests really do reach for the same room at the same moment.

1. Start: the R7 guard row is at version 0 and both bookings have read it.
2. Action: both try UPDATE room_booking_guard SET version = version + 1 WHERE room = 'R7' AND version = 0.
3. Result:
 0.03s  Maya   BEGIN
 0.03s  Maya   reads version = 0   (a plain SELECT: no lock, no wait)
 0.03s  Maya   overlap check sees 0
 0.12s  Omar   BEGIN
 0.12s  Omar   reads version = 0   (the same 0; Omar did not wait either)
 0.12s  Omar   overlap check sees 0
 0.12s  Omar   UPDATE ... WHERE version = 0 matched 1 row, so Omar owns the claim
 0.12s  Omar   INSERT R102
 0.12s  Omar   COMMIT
 1.03s  Maya   UPDATE ... WHERE version = 0 matched 0 rows, so somebody else booked first
 1.03s  Maya   ROLLBACK, then try again
 1.03s  Maya   reads version = 1
 1.03s  Maya   overlap check sees 1
 1.03s  Maya   refuses: Unavailable

Confirmed reservations for R7: 1
Nobody waited for a lock. The request that lost did its work twice instead.

### Completed example 6.1 · two terminals again

Run terminal A down to its overlap check, then all of terminal B, then come back to A and watch its claim fail.

**Optimistic version checking** In short: Read the version, do the work, then write only if the version is still what you read. The one that lost finds out at the end and starts again. Both requests read version 0 without taking a lock, so neither waits. Only one UPDATE … WHERE version = 0 can match, because the first one to run moves the version on; the other matches zero rows, which is not an error, just a fact. Compare the two measurements. FOR UPDATE made the loser wait 0.91 seconds and then do its work once. This makes nobody wait, and the loser does its work twice. Which is cheaper depends entirely on how often two guests really do reach for the same room at the same moment.

1. Start: the R7 guard row is at version 0 and both bookings have read it.
2. Action: both try UPDATE room_booking_guard SET version = version + 1 WHERE room = 'R7' AND version = 0.
3. Result:
 0.03s  Maya   BEGIN
 0.03s  Maya   reads version = 0   (a plain SELECT: no lock, no wait)
 0.03s  Maya   overlap check sees 0
 0.12s  Omar   BEGIN
 0.12s  Omar   reads version = 0   (the same 0; Omar did not wait either)
 0.12s  Omar   overlap check sees 0
 0.12s  Omar   UPDATE ... WHERE version = 0 matched 1 row, so Omar owns the claim
 0.12s  Omar   INSERT R102
 0.12s  Omar   COMMIT
 1.03s  Maya   UPDATE ... WHERE version = 0 matched 0 rows, so somebody else booked first
 1.03s  Maya   ROLLBACK, then try again
 1.03s  Maya   reads version = 1
 1.03s  Maya   overlap check sees 1
 1.03s  Maya   refuses: Unavailable

Confirmed reservations for R7: 1
Nobody waited for a lock. The request that lost did its work twice instead.

### Terminal A — Maya

```text
BEGIN;

-- A plain SELECT. No lock, so no other session waits for this one.
SELECT version FROM room_booking_guard WHERE room = 'R7';
-- 0

SELECT count(*) FROM reservations
WHERE room = 'R7' AND status = 'Confirmed'
  AND check_in < '2026-11-14' AND '2026-11-12' < check_out;
-- 0, so this booking may go ahead

-- >>> now run all of terminal B, which will claim the version first <<<

-- Claim the version we read. If somebody else already moved it on, this matches no row.
UPDATE room_booking_guard SET version = version + 1
WHERE room = 'R7' AND version = 0;
-- UPDATE 0  <-- we lost; nothing we planned is valid any more

ROLLBACK;

-- Try again from the start. This time the overlap check tells the truth.
BEGIN;
SELECT count(*) FROM reservations
WHERE room = 'R7' AND status = 'Confirmed'
  AND check_in < '2026-11-14' AND '2026-11-12' < check_out;
-- 1, so this booking is refused
COMMIT;
```

### 

### Terminal B — Omar

```text
BEGIN;

SELECT version FROM room_booking_guard WHERE room = 'R7';
-- 0   <-- the same 0 terminal A read, and neither of them waited

SELECT count(*) FROM reservations
WHERE room = 'R7' AND status = 'Confirmed'
  AND check_in < '2026-11-14' AND '2026-11-12' < check_out;
-- 0

UPDATE room_booking_guard SET version = version + 1
WHERE room = 'R7' AND version = 0;
-- UPDATE 1  <-- this request owns the claim

INSERT INTO reservations (id, guest_name, guest_email, room, check_in, check_out, status)
VALUES ('R102', 'Omar', 'omar@example.com', 'R7', '2026-11-12', '2026-11-14', 'Confirmed');

COMMIT;
```

### What you will see

The same two guests, and not a single wait anywhere.

### Both terminals, in time order

```text
 0.03s  Maya   BEGIN
 0.03s  Maya   reads version = 0   (a plain SELECT: no lock, no wait)
 0.03s  Maya   overlap check sees 0
 0.12s  Omar   BEGIN
 0.12s  Omar   reads version = 0   (the same 0; Omar did not wait either)
 0.12s  Omar   overlap check sees 0
 0.12s  Omar   UPDATE ... WHERE version = 0 matched 1 row, so Omar owns the claim
 0.12s  Omar   INSERT R102
 0.12s  Omar   COMMIT
 1.03s  Maya   UPDATE ... WHERE version = 0 matched 0 rows, so somebody else booked first
 1.03s  Maya   ROLLBACK, then try again
 1.03s  Maya   reads version = 1
 1.03s  Maya   overlap check sees 1
 1.03s  Maya   refuses: Unavailable

Confirmed reservations for R7: 1
Nobody waited for a lock. The request that lost did its work twice instead.
```

### What the retry has to redo

The retry must read everything again, including the overlap check. That decision was made on information that has just been proven stale, so retrying only the UPDATE would save a booking the check never approved. And a retry loop always needs a limit, or a busy moment becomes an outage.

### Your turn · lose a race without waiting

1. Reset the guard row: UPDATE room_booking_guard SET version = 0; and delete any reservations left from Step 5.
2. In terminal A, run BEGIN, read the version, and run the overlap check. Note that nothing waits.
3. In terminal B, do the same, then run the UPDATE … WHERE version = 0 and COMMIT. It prints UPDATE 1.
4. Back in terminal A, run the same UPDATE. It prints UPDATE 0 — no error, just a fact: A has lost.
5. Roll A back, start again, and read the version and the overlap check once more. They now say 1 and 1.
6. Add the version-check row to your four-repairs table, next to the FOR UPDATE row, with both measurements.

**When you finish, you have:** A version check observed matching 0 rows, a retry that then refuses the booking, and the third row of your table filled in.
### Explanation

Both of these repairs work and both have an honest cost: one makes the loser wait, the other makes the loser redo. Neither is wrong. What both share is that they only protect code that remembers to use them — which is what Step 7 fixes.



---

## 8. Step 7 · Teach the database the rule

The three repairs so far all have to be remembered. This one cannot be forgotten by anything: the table itself refuses an overlapping reservation, whoever sends it.





### One rule, checked by the index as the row goes in

### Diagram: The same two bookings, with the exclusion constraint in place

Nothing in the application has changed: the same race tool, the same two requests, the same pre-check that still answers 0 for both. The only difference is that the reservations table now carries a rule, and PostgreSQL checks it on every INSERT - so the second one cannot succeed, no matter which code path sent it or which instance of the app it came from.

Diagram as text:

The same two POST /reservations as the previous diagram, from the same tool, against the same application code. Steps 3 and 4: both overlap checks still answer 0. The pre-check was deliberately kept, and it is still unable to see the problem. Step 5: Maya’s INSERT is checked against the exclusion constraint as it goes in. Nothing conflicts, so it is written and committed, and Maya gets 201 Created. Step 6: Omar’s INSERT is checked against the same constraint, and R7 already has a confirmed reservation covering those nights. PostgreSQL refuses the row with SQLSTATE 23P01 on reservations_no_overlap. Nothing is written. The Reservations module recognises that one violation by name, raises OverlapRejected, and BookingService answers Unavailable, so Omar receives 409 Conflict with a readable message rather than a database error. The room ends with exactly one confirmed reservation. The same thing happens if the two requests go to two different instances of the app, because the rule lives in the database rather than in any one process.

**Step by step:** The first four boxes are identical to the previous diagram - that is the point. Follow what happens from box 5 onwards, where the database starts doing the work the application could not do for itself.

1. Boxes 1 to 4 are unchanged: the same two requests, and two overlap checks that both answer 0. The repair is not in the application, so nothing here could be expected to look different.
2. What changed is the table. The constraint says that no two confirmed reservations of the same room may cover overlapping dates, and PostgreSQL checks it on every INSERT:

   sql:
   
   ```text
   CREATE EXTENSION IF NOT EXISTS btree_gist;
   
   ALTER TABLE reservations
       ADD CONSTRAINT reservations_no_overlap
       EXCLUDE USING gist (
           room WITH =,
           daterange(check_in, check_out, '[)') WITH &&
       ) WHERE (status = 'Confirmed');
   ```
3. btree_gist is what makes the constraint possible: a GiST index can hold a range test, but not plain equality on text, so the extension teaches it how. Without it PostgreSQL refuses to create the constraint at all.
4. White box 5: Maya’s INSERT is checked as it is written, finds nothing to conflict with, and commits.
5. White box 6 and the purple box “PostgreSQL refuses the INSERT”: Omar’s INSERT is checked the same way and is refused. This is the error, exactly as psql prints it:

   text:
   
   ```text
   ERROR:  conflicting key value violates exclusion constraint "reservations_no_overlap"
   DETAIL:  Key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-13,2026-11-15)) conflicts with existing key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-12,2026-11-14)).
   ```
6. The purple box is drawn dashed because it is work PostgreSQL does that nobody wrote. There is no code in the project that compares Omar’s dates with Maya’s; the index does it.
7. White box "the store turns the refusal into Unavailable": the PostgreSQL store recognises this one constraint by name and raises OverlapRejected. Any other violation is a real fault and is left alone:

   C#:
   
   ```text
   // HotelBooking/Reservations/Internal/PostgresReservationStore.cs
   using HotelBooking.Rooms;
   using Microsoft.EntityFrameworkCore;
   using Npgsql;
   
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
       // The name of the exclusion constraint in db/init.sql, and PostgreSQL's SQLSTATE for "a row
       // conflicted with an exclusion constraint". The store recognises exactly this one violation.
       private const string NoOverlapConstraint = "reservations_no_overlap";
       private const string ExclusionViolation = "23P01";
   
       private readonly List<(ReservationRow Row, Reservation Reservation)> loaded = [];
   
       public int Count => db.Reservations.Count();
   
       public bool HasOverlap(Room room, DateRange stay) => Confirmed(room, stay).Any();
   
       // nextval never hands the same number to two callers, and it does not wait for, or roll back
       // with, the transaction that asked for it. That is exactly what an id needs.
       // EF Core's SqlQuery<T> reads a scalar from a column it expects to be called "Value".
       public string NextReservationId() =>
           $"R{db.Database.SqlQuery<long>($"SELECT nextval('reservation_ids') AS \"Value\"").Single()}";
   
       public void Save(Reservation reservation)
       {
           var row = new ReservationRow
           {
               Id = reservation.Id, GuestName = reservation.Guest.Name, GuestEmail = reservation.Guest.Email,
               Room = reservation.Room.Number, CheckIn = reservation.Stay.CheckIn, CheckOut = reservation.Stay.CheckOut,
               Status = reservation.Status.ToString(),
           };
           var entry = db.Reservations.Add(row);
           try
           {
               db.SaveChanges(); // INSERT now: the constraint answers here, not at the end of the request
           }
           catch (DbUpdateException error) when (IsOverlapRefusal(error))
           {
               // EF Core still has this row tracked as Added, so the SaveChanges at the end of the
               // request would try the same INSERT again. Detaching it is this language's version of
               // rolling back to a savepoint: the refused statement is forgotten, the request is not.
               entry.State = EntityState.Detached;
               throw new OverlapRejectedException($"{reservation.Room.Number} already has a confirmed reservation for these nights.");
           }
       }
   
       // A duplicate id, or anything else, is a real fault and must not be read as a double booking.
       private static bool IsOverlapRefusal(DbUpdateException error) =>
           error.InnerException is PostgresException { SqlState: ExclusionViolation } postgres
           && postgres.ConstraintName == NoOverlapConstraint;
   
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
   
   Python:
   
   ```text
   # hotel_booking/reservations/internal.py
   from __future__ import annotations
   
   import threading
   from datetime import date
   
   from sqlalchemy import exists, func, select, text
   from sqlalchemy.exc import IntegrityError
   from sqlalchemy.orm import DeclarativeBase, Mapped, Session, mapped_column
   
   from hotel_booking.reservations.booking import OverlapRejected
   from hotel_booking.reservations.domain import DateRange, Guest, Reservation
   from hotel_booking.rooms import Room
   
   # The name of the exclusion constraint in db/init.sql. The store recognises this one violation
   # and turns it into OverlapRejected; any other IntegrityError is a real fault and is re-raised.
   NO_OVERLAP_CONSTRAINT = "reservations_no_overlap"
   
   
   class InMemoryReservationStore:
       """Keeps reservations in a list. PostgresReservationStore below has the same methods."""
   
       def __init__(self) -> None:
           self._rows: list[Reservation] = []
           # save must check and append without another thread fitting in between: this list is the
           # whole database here, so this lock is this store's version of the exclusion constraint.
           self._lock = threading.Lock()
   
       def count(self) -> int:
           return len(self._rows)
   
       def has_overlap(self, room: Room, stay: DateRange) -> bool:
           return any(row.blocks(room, stay) for row in self._rows)
   
       def next_reservation_id(self) -> str:
           with self._lock:
               return f"R{101 + len(self._rows)}"
   
       def save(self, reservation: Reservation) -> None:
           with self._lock:
               if self.has_overlap(reservation.room, reservation.stay):
                   raise OverlapRejected(
                       f"{reservation.room.number} already has a confirmed reservation for these nights.")
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
   
       def next_reservation_id(self) -> str:
           # nextval never hands the same number to two callers, and it does not wait for, or roll
           # back with, the transaction that asked for it. That is exactly what an id needs.
           return f"R{self._session.scalar(text("SELECT nextval('reservation_ids')"))}"
   
       def save(self, reservation: Reservation) -> None:
           row = ReservationRow(id=reservation.id, guest_name=reservation.guest.name, guest_email=reservation.guest.email,
                                room=reservation.room.number, check_in=reservation.stay.check_in,
                                check_out=reservation.stay.check_out, status=reservation.status.value)
           try:
               # A SAVEPOINT, so a refused INSERT rolls back only this statement. Without it the whole
               # request's transaction would be poisoned and save_changes could not commit.
               with self._session.begin_nested():
                   self._session.add(row)
                   self._session.flush()  # INSERT now: the constraint answers here, not at commit
           except IntegrityError as error:
               if NO_OVERLAP_CONSTRAINT in str(error.orig):
                   raise OverlapRejected(
                       f"{reservation.room.number} already has a confirmed reservation for these nights.") from error
               raise  # a duplicate id, or anything else: a real fault, not a double booking
   
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
   
   Java:
   
   ```text
   // src/main/java/hotelbooking/reservations/internal/PostgresReservationStore.java
   package hotelbooking.reservations.internal;
   
   import hotelbooking.reservations.DateRange;
   import hotelbooking.reservations.Guest;
   import hotelbooking.reservations.Reservation;
   import hotelbooking.reservations.ReservationStatus;
   import hotelbooking.reservations.OverlapRejectedException;
   import hotelbooking.reservations.ReservationStore;
   import hotelbooking.rooms.Room;
   import jakarta.persistence.Entity;
   import jakarta.persistence.PersistenceException;
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
   
       // The name of the exclusion constraint in db/init.sql, and PostgreSQL's SQLSTATE for "a row
       // conflicted with an exclusion constraint". The store recognises exactly this one violation.
       private static final String NO_OVERLAP_CONSTRAINT = "reservations_no_overlap";
       private static final String EXCLUSION_VIOLATION = "23P01";
   
       // Set when the constraint refused a save. Main.java reads it and rolls the request back.
       private boolean refused;
   
       // True when this request's transaction has been aborted by the booking rule, and so must be
       // rolled back instead of committed.
       public boolean wasRefused() {
           return refused;
       }
   
       // nextval never hands the same number to two callers, and it does not wait for, or roll back
       // with, the transaction that asked for it. That is exactly what an id needs, and it is what
       // stops two requests in this language both being given R101.
       @Override
       public String nextReservationId() {
           Number next = (Number) db.createNativeQuery("SELECT nextval('reservation_ids')").getSingleResult();
           return "R" + next.longValue();
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
           try {
               db.persist(row);
               db.flush(); // INSERT now: the constraint answers here, not at the end of the request
           } catch (PersistenceException failure) {
               if (!isOverlapRefusal(failure)) {
                   throw failure; // a duplicate id, or anything else: a real fault, not a double booking
               }
               // Two things have to happen here, and both are specific to running inside one
               // transaction. First, take the refused row back out of the persistence context, so
               // Hibernate does not try to write it again when it next flushes. Second, remember that
               // the booking was refused: PostgreSQL has marked this transaction as aborted and will
               // refuse every further statement in it with SQLSTATE 25P02, so the unit of work must
               // roll back rather than commit. A refused booking saves nothing, so that is exactly
               // the right ending.
               db.detach(row);
               refused = true;
               throw new OverlapRejectedException(
                       reservation.room().number() + " already has a confirmed reservation for these nights.");
           }
       }
   
       // Hibernate wraps the driver's error, so the cause chain is what has to be read.
       private static boolean isOverlapRefusal(PersistenceException failure) {
           for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
               if (cause instanceof java.sql.SQLException sql
                       && EXCLUSION_VIOLATION.equals(sql.getSQLState())
                       && String.valueOf(sql.getMessage()).contains(NO_OVERLAP_CONSTRAINT)) {
                   return true;
               }
           }
           return false;
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
   
   Go:
   
   ```text
   // reservations/internal.go
   package reservations
   
   import (
   	"cmp"
   	"context"
   	"errors"
   	"fmt"
   	"slices"
   	"sync"
   
   	"github.com/jackc/pgx/v5"
   	"github.com/jackc/pgx/v5/pgconn"
   	"github.com/jackc/pgx/v5/pgxpool"
   
   	"hotelbooking/rooms"
   )
   
   // The name of the exclusion constraint in db/init.sql, and PostgreSQL's SQLSTATE for "a row
   // conflicted with an exclusion constraint". The store recognises exactly this one violation.
   const (
   	noOverlapConstraint = "reservations_no_overlap"
   	exclusionViolation  = "23P01"
   )
   
   // InMemoryReservationStore keeps reservations in a list.
   // PostgresReservationStore below has the same methods.
   type InMemoryReservationStore struct {
   	// net/http serves every request on its own goroutine, and this list is the whole database
   	// here. Checking and appending while holding the mutex is this store's version of the
   	// exclusion constraint.
   	mutex sync.Mutex
   	rows  []*Reservation
   }
   
   func (store *InMemoryReservationStore) Count() int {
   	store.mutex.Lock()
   	defer store.mutex.Unlock()
   	return len(store.rows)
   }
   
   func (store *InMemoryReservationStore) HasOverlap(room rooms.Room, stay DateRange) bool {
   	store.mutex.Lock()
   	defer store.mutex.Unlock()
   	return store.hasOverlap(room, stay)
   }
   
   // hasOverlap expects the mutex to be held already.
   func (store *InMemoryReservationStore) hasOverlap(room rooms.Room, stay DateRange) bool {
   	for _, row := range store.rows {
   		if row.Blocks(room, stay) {
   			return true
   		}
   	}
   	return false
   }
   
   // NextReservationID is safe here for the same reason Save is: it holds the same mutex.
   func (store *InMemoryReservationStore) NextReservationID() string {
   	store.mutex.Lock()
   	defer store.mutex.Unlock()
   	return fmt.Sprintf("R%d", 101+len(store.rows))
   }
   
   func (store *InMemoryReservationStore) Save(reservation *Reservation) error {
   	store.mutex.Lock()
   	defer store.mutex.Unlock()
   	if store.hasOverlap(reservation.Room, reservation.Stay) {
   		return fmt.Errorf("%w: %s", ErrOverlapRejected, reservation.Room.Number)
   	}
   	store.rows = append(store.rows, reservation)
   	return nil
   }
   
   // Returns nil when no reservation has that id.
   func (store *InMemoryReservationStore) Find(id string) *Reservation {
   	store.mutex.Lock()
   	defer store.mutex.Unlock()
   	for _, row := range store.rows {
   		if row.ID == id {
   			return row
   		}
   	}
   	return nil
   }
   
   func (store *InMemoryReservationStore) CountConfirmed(room rooms.Room, stay DateRange) int {
   	store.mutex.Lock()
   	defer store.mutex.Unlock()
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
   
   // NextReservationID takes the id from the sequence. nextval never hands the same number to two
   // callers, and it does not wait for, or roll back with, the transaction that asked for it.
   func (store *PostgresReservationStore) NextReservationID() string {
   	var next int64
   	if err := store.pool.QueryRow(store.ctx, "SELECT nextval('reservation_ids')").Scan(&next); err != nil {
   		store.err = cmp.Or(store.err, err)
   		return ""
   	}
   	return fmt.Sprintf("R%d", next)
   }
   
   func (store *PostgresReservationStore) Save(reservation *Reservation) error {
   	if store.err != nil {
   		return nil // an earlier query failed: write nothing, SaveChanges reports the error
   	}
   	_, err := store.pool.Exec(store.ctx, "INSERT INTO reservations VALUES ($1, $2, $3, $4, $5, $6, $7)",
   		reservation.ID, reservation.Guest.Name, reservation.Guest.Email, reservation.Room.Number,
   		reservation.Stay.CheckIn, reservation.Stay.CheckOut, string(reservation.Status))
   	// The INSERT happens here, so the constraint answers here too.
   	if isOverlapRefusal(err) {
   		// Each statement stands on its own in pgx, so nothing has to be rolled back: the refused
   		// INSERT changed nothing and the pool is still usable for the rest of the request.
   		return fmt.Errorf("%w: %s", ErrOverlapRejected, reservation.Room.Number)
   	}
   	store.err = cmp.Or(store.err, err) // a duplicate id, or anything else: a real fault
   	return nil
   }
   
   // isOverlapRefusal is true only for the exclusion constraint of db/init.sql, so a duplicate id or
   // a broken connection is never read as a double booking.
   func isOverlapRefusal(err error) bool {
   	var postgres *pgconn.PgError
   	return errors.As(err, &postgres) &&
   		postgres.Code == exclusionViolation &&
   		postgres.ConstraintName == noOverlapConstraint
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
   
   JavaScript:
   
   ```text
   // src/reservations/internal/postgres-store.js
   import { DateRange, Guest, Reservation } from '../domain.js';
   import { OverlapRejected } from '../booking.js';
   
   // A date as PostgreSQL's date type writes it: 2026-11-12.
   const day = date => date.toISOString().slice(0, 10);
   
   // Confirmed reservations of a room that share at least one night with a stay.
   const confirmedOverlaps = "FROM reservations WHERE room = $1 AND status = 'Confirmed' AND check_in < $3 AND $2 < check_out";
   
   // The name of the exclusion constraint in db/init.sql. node-postgres puts it in error.constraint,
   // so the store recognises this one violation and leaves every other error alone.
   const NO_OVERLAP_CONSTRAINT = 'reservations_no_overlap';
   
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
   
     // nextval never hands the same number to two callers, and it does not wait for, or roll back
     // with, the transaction that asked for it. That is exactly what an id needs.
     async nextReservationId() {
       const { rows } = await this.#pool.query("SELECT nextval('reservation_ids') AS id");
       return `R${rows[0].id}`;
     }
   
     // INSERT now: a second R101 is refused here, and so is a stay that overlaps one already saved.
     async save(reservation) {
       try {
         await this.#pool.query('INSERT INTO reservations VALUES ($1, $2, $3, $4, $5, $6, $7)', [reservation.id, reservation.guest.name,
           reservation.guest.email, reservation.room.number, day(reservation.stay.checkIn), day(reservation.stay.checkOut), reservation.status]);
       } catch (error) {
         if (error.constraint === NO_OVERLAP_CONSTRAINT) {
           throw new OverlapRejected(`${reservation.room.number} already has a confirmed reservation for these nights.`);
         }
         throw error; // a duplicate id, or anything else: a real fault, not a double booking
       }
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
   // lib/reservations/src/postgres_store.dart
   import 'package:hotel_booking/rooms/rooms.dart';
   import 'package:postgres/postgres.dart' hide DateRange; // the stay is our own DateRange
   
   import 'booking.dart';
   import 'domain.dart';
   
   // The name of the exclusion constraint in db/init.sql, and PostgreSQL's SQLSTATE for "a row
   // conflicted with an exclusion constraint". The store recognises exactly this one violation and
   // leaves every other error alone.
   const noOverlapConstraint = 'reservations_no_overlap';
   const exclusionViolation = '23P01';
   
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
   
     // nextval never hands the same number to two callers, and it does not wait for, or roll back
     // with, the transaction that asked for it. That is exactly what an id needs.
     @override
     Future<String> nextReservationId() async {
       final rows = await _pool.execute("SELECT nextval('reservation_ids')");
       return 'R${rows.first.first}';
     }
   
     // INSERT now: the constraint answers here, not at the end of the request.
     @override
     Future<void> save(Reservation reservation) async {
       try {
         await _pool.execute(
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
       } on ServerException catch (error) {
         if (error.code == exclusionViolation && error.constraintName == noOverlapConstraint) {
           throw OverlapRejected(
               '${reservation.room.number} already has a confirmed reservation for these nights.');
         }
         rethrow; // a duplicate id, or anything else: a real fault, not a double booking
       }
     }
   
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
8. BookingService catches it and answers Unavailable - the same answer the pre-check gives - so the guest never sees a database error:

   C#:
   
   ```text
   public BookingResult Reserve(Guest guest, Room room, DateRange stay)
   {
       if (!stay.IsValid()) return new("InvalidDates");
       if (store.HasOverlap(room, stay)) return new("Unavailable");
   
       Thread.Sleep(slowStepMs); // 0 normally; the race tool makes this step slow
   
       var reservation = new Reservation(store.NextReservationId(), guest, room, stay);
       try
       {
           store.Save(reservation);
       }
       catch (OverlapRejectedException)
       {
           // Another request saved an overlapping reservation after our check and before our save.
           // The check was honest when it ran; it had simply stopped being true. The guest gets
           // the same answer either way.
           return new("Unavailable");
       }
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
   ```
   
   Python:
   
   ```text
   def reserve(self, guest: Guest, room: Room, stay: DateRange) -> BookingResult:
       if not stay.is_valid():
           return BookingResult("InvalidDates")
       if self.store.has_overlap(room, stay):
           return BookingResult("Unavailable")
   
       time.sleep(self.slow_step_seconds)  # 0 normally; the race tool makes this step slow
   
       reservation = Reservation(self.store.next_reservation_id(), guest, room, stay)
       try:
           self.store.save(reservation)
       except OverlapRejected:
           # Another request saved an overlapping reservation after our check and before our save.
           # The check was honest when it ran; it had simply stopped being true. The guest gets the
           # same answer either way.
           return BookingResult("Unavailable")
       try:
           # Notifications gets its own message: the five fields it needs, not the Reservation.
           self.notifications.send_confirmation(
               Confirmation(reservation.id, guest.email, room.number, stay.check_in, stay.check_out))
       except Exception:
           # R6: the booking is already saved, so a failed message must not undo or hide it.
           return BookingResult("Accepted", reservation.id, notification_failed=True)
       return BookingResult("Accepted", reservation.id)
   ```
   
   Java:
   
   ```text
   public BookingResult reserve(Guest guest, Room room, DateRange stay) {
       if (!stay.isValid()) return new BookingResult("InvalidDates");
       if (store.hasOverlap(room, stay)) return new BookingResult("Unavailable");
   
       pause(); // does nothing normally; the race demo makes this step slow
   
       Reservation reservation = new Reservation(store.nextReservationId(), guest, room, stay);
       try {
           store.save(reservation);
       } catch (OverlapRejectedException rejected) {
           // Another request saved an overlapping reservation after our check and before our save.
           // The check was honest when it ran; it had simply stopped being true. The guest gets the
           // same answer either way.
           return new BookingResult("Unavailable");
       }
       try {
           // Notifications gets its own message: the five fields it needs, not the Reservation.
           notifications.sendConfirmation(new Confirmation(reservation.id(), guest.email(), room.number(), stay.checkIn(), stay.checkOut()));
       } catch (RuntimeException failure) {
           // R6: the booking is already saved, so a failed message must not undo or hide it.
           return new BookingResult("Accepted", reservation.id(), true);
       }
       return new BookingResult("Accepted", reservation.id());
   }
   ```
   
   Go:
   
   ```text
   func (service *BookingService) Reserve(guest Guest, room rooms.Room, stay DateRange) BookingResult {
   	if !stay.IsValid() {
   		return BookingResult{Outcome: "InvalidDates"}
   	}
   	if service.store.HasOverlap(room, stay) {
   		return BookingResult{Outcome: "Unavailable"}
   	}
   
   	time.Sleep(service.slowStep) // zero normally; the race tool makes this step slow
   
   	reservation := NewReservation(service.store.NextReservationID(), guest, room, stay)
   	if err := service.store.Save(reservation); errors.Is(err, ErrOverlapRejected) {
   		// Another request saved an overlapping reservation after our check and before our save.
   		// The check was honest when it ran; it had simply stopped being true. The guest gets the
   		// same answer either way.
   		return BookingResult{Outcome: "Unavailable"}
   	}
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
   ```
   
   JavaScript:
   
   ```text
   async reserve(guest, room, stay) {
     if (!stay.isValid()) return new BookingResult('InvalidDates');
     if (await this.store.hasOverlap(room, stay)) return new BookingResult('Unavailable');
   
     await wait(this.slowStepMs); // zero normally; the race tool makes this step slow
   
     const reservation = new Reservation(await this.store.nextReservationId(), guest, room, stay);
     try {
       await this.store.save(reservation);
     } catch (error) {
       // Another request saved an overlapping reservation after our check and before our save. The
       // check was honest when it ran; it had simply stopped being true.
       if (error instanceof OverlapRejected) return new BookingResult('Unavailable');
       throw error;
     }
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
   ```
   
   Dart:
   
   ```text
   Future<BookingResult> reserve(Guest guest, Room room, DateRange stay) async {
     if (!stay.isValid()) return BookingResult('InvalidDates');
     if (await store.hasOverlap(room, stay)) return BookingResult('Unavailable');
   
     // A pause that lets other requests run while this one waits.
     await Future<void>.delayed(slowStep);
   
     final reservation = Reservation(await store.nextReservationId(), guest, room, stay);
     try {
       await store.save(reservation);
     } on OverlapRejected {
       // Another request saved an overlapping reservation after our check and before our save. The
       // check was honest when it ran; it had simply stopped being true.
       return BookingResult('Unavailable');
     }
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
   ```
9. Green box “R7 has exactly one confirmed reservation”: one guest booked the room and one was told it was taken, which is the whole of the booking rule. The race tool says so:

   text:
   
   ```text
   Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.
   
   Maya -> 201 Created   {"id":"R101","status":"Confirmed","notificationFailed":false}
   Omar -> 409 Conflict  {"title":"Unavailable","status":409,"detail":"The room is booked for at least one night of this stay."}
   
   Confirmed reservations for R7 on 12-14 Nov: 1
   CORRECT: one booking was accepted and one was refused.
   ```
10. The two tests that keep this true are the last two in the suite. One sends two bookings at the same moment and expects one 201 and one 409; the other calls save directly, with the pre-check bypassed, and expects it to refuse:

    C#:
    
    ```text
    [Fact]
    public async Task Two_bookings_at_the_same_moment_give_the_room_one_reservation()
    {
        // Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
        var (client, store) = await NewClientWithStore(slowStepMs: 300);
        var sameStay = new { guestName = "Omar", guestEmail = "omar@example.com", room = "R7", checkIn = "2026-11-12", checkOut = "2026-11-14" };
    
        // Started together, awaited together: Omar's check runs while Maya is still in the gap.
        var maya = client.PostAsJsonAsync("/reservations", Maya);
        var omar = client.PostAsJsonAsync("/reservations", sameStay);
        var answers = await Task.WhenAll(maya, omar);
    
        Assert.Equal([HttpStatusCode.Created, HttpStatusCode.Conflict], answers.Select(answer => answer.StatusCode).OrderBy(code => code));
        Assert.Equal(1, store.CountConfirmed(Rooms[0], new DateRange(new DateOnly(2026, 11, 12), new DateOnly(2026, 11, 14))));
    }
    
    [Fact]
    public void The_store_refuses_an_overlapping_save_even_without_the_pre_check()
    {
        // The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
        // Nothing calls HasOverlap here: Save itself must refuse.
        var store = new InMemoryReservationStore();
        var stay = new DateRange(new DateOnly(2026, 11, 12), new DateOnly(2026, 11, 14));
        store.Save(new Reservation("R101", new Guest("Maya", "maya@example.com"), Rooms[0], stay));
    
        var overlapping = new Reservation("R102", new Guest("Omar", "omar@example.com"), Rooms[0],
            new DateRange(new DateOnly(2026, 11, 13), new DateOnly(2026, 11, 15)));
        Assert.Throws<OverlapRejectedException>(() => store.Save(overlapping));
        Assert.Equal(1, store.CountConfirmed(Rooms[0], stay));
    }
    ```
    
    Python:
    
    ```text
    # ---------------------------------------------------------------- P09
    def test_two_bookings_at_the_same_moment_give_the_room_one_reservation(self) -> None:
        """Step 1's race, as a test. Both requests pass the pre-check; only one may be saved."""
        app, store = new_app(slow_step_seconds=0.3)  # wide enough that both pass the check first
        client = TestClient(app)
        answers: dict[str, int] = {}
    
        maya = threading.Thread(target=lambda: answers.update(maya=client.post("/reservations", json=MAYA).status_code))
        omar = threading.Thread(target=lambda: answers.update(omar=client.post("/reservations", json=OMAR).status_code))
        maya.start()
        omar.start()
        maya.join()
        omar.join()
    
        self.assertEqual([201, 409], sorted(answers.values()))
        stay = DateRange(date(2026, 11, 12), date(2026, 11, 14))
        self.assertEqual(1, store.count_confirmed(R7, stay))
    
    def test_the_store_refuses_an_overlapping_save_even_without_the_pre_check(self) -> None:
        """The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
        Nothing calls has_overlap here: save itself must refuse."""
        store = InMemoryReservationStore()
        stay = DateRange(date(2026, 11, 12), date(2026, 11, 14))
        store.save(Reservation("R101", Guest("Maya", "maya@example.com"), R7, stay))
    
        overlapping = Reservation("R102", Guest("Omar", "omar@example.com"), R7,
                                  DateRange(date(2026, 11, 13), date(2026, 11, 15)))
        with self.assertRaises(OverlapRejected):
            store.save(overlapping)
        self.assertEqual(1, store.count_confirmed(R7, stay))
    ```
    
    Java:
    
    ```text
    @Test
    // ---------------------------------------------------------------- P09
    @Test
    void two_bookings_at_the_same_moment_give_the_room_one_reservation() throws Exception {
        // Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
        InMemoryReservationStore rows = new InMemoryReservationStore();
        Stats stats = new Stats();
        BookingService service = new BookingService(ROOMS, new CountingReservationStore(rows, stats),
                new EmailNotificationSender(), 300);
    
        // Two threads, both inside the gap between the check and the save.
        var maya = CompletableFuture.supplyAsync(
                () -> service.reserve(new Guest("Maya", "maya@example.com"), ROOMS.get(0), NOV_12_14));
        var omar = CompletableFuture.supplyAsync(
                () -> service.reserve(new Guest("Omar", "omar@example.com"), ROOMS.get(0), NOV_12_14));
    
        var outcomes = Stream.of(maya.join(), omar.join())
                .map(BookingResult::outcome).sorted().toList();
        assertEquals(List.of("Accepted", "Unavailable"), outcomes);
        assertEquals(1, rows.countConfirmed(ROOMS.get(0), NOV_12_14));
    }
    
    @Test
    @Test
    void the_store_refuses_an_overlapping_save_even_without_the_pre_check() {
        // The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
        // Nothing calls hasOverlap here: save itself must refuse.
        InMemoryReservationStore store = new InMemoryReservationStore();
        store.save(new Reservation("R101", new Guest("Maya", "maya@example.com"), ROOMS.get(0), NOV_12_14));
    
        Reservation overlapping = new Reservation("R102", new Guest("Omar", "omar@example.com"), ROOMS.get(0),
                new DateRange(LocalDate.of(2026, 11, 13), LocalDate.of(2026, 11, 15)));
        assertThrows(OverlapRejectedException.class, () -> store.save(overlapping));
        assertEquals(1, store.countConfirmed(ROOMS.get(0), NOV_12_14));
    }
    ```
    
    Go:
    
    ```text
    // Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
    func TestTwoBookingsAtTheSameMomentGiveTheRoomOneReservation(t *testing.T) {
    	store := &InMemoryReservationStore{}
    	service := NewBookingService([]rooms.Room{r7, r8}, store, &fakeNotificationSender{}, 300*time.Millisecond)
    	stay := nov(12, 14)
    
    	// Two goroutines, both inside the gap between the check and the save.
    	outcomes := make([]string, 2)
    	var waiting sync.WaitGroup
    	waiting.Add(2)
    	go func() {
    		defer waiting.Done()
    		outcomes[0] = service.Reserve(maya, r7, stay).Outcome
    	}()
    	go func() {
    		defer waiting.Done()
    		outcomes[1] = service.Reserve(omar, r7, stay).Outcome
    	}()
    	waiting.Wait()
    
    	slices.Sort(outcomes)
    	if want := []string{"Accepted", "Unavailable"}; !slices.Equal(outcomes, want) {
    		t.Errorf("outcomes = %v, want %v", outcomes, want)
    	}
    	if confirmed := store.CountConfirmed(r7, stay); confirmed != 1 {
    		t.Errorf("confirmed reservations for R7 = %d, want 1", confirmed)
    	}
    }
    
    // The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
    // Nothing calls HasOverlap here: Save itself must refuse.
    func TestTheStoreRefusesAnOverlappingSaveEvenWithoutThePreCheck(t *testing.T) {
    	store := &InMemoryReservationStore{}
    	stay := nov(12, 14)
    	if err := store.Save(NewReservation("R101", maya, r7, stay)); err != nil {
    		t.Fatalf("the first save failed: %v", err)
    	}
    
    	err := store.Save(NewReservation("R102", omar, r7, nov(13, 15)))
    	if !errors.Is(err, ErrOverlapRejected) {
    		t.Errorf("err = %v, want it to wrap ErrOverlapRejected", err)
    	}
    	if confirmed := store.CountConfirmed(r7, stay); confirmed != 1 {
    		t.Errorf("confirmed reservations for R7 = %d, want 1", confirmed)
    	}
    }
    ```
    
    JavaScript:
    
    ```text
    // ---------------------------------------------------------------- P09
    test('two bookings at the same moment give the room one reservation', async t => {
      // Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
      const send = await startApp(t, 300);
      const sameStay = { ...omar, checkIn: '2026-11-12', checkOut: '2026-11-14' };
    
      // Started together, awaited together: Omar's check runs while Maya is still in the gap.
      const [first, second] = await Promise.all([
        send('POST', '/reservations', maya),
        send('POST', '/reservations', sameStay),
      ]);
    
      assert.deepEqual([first.status, second.status].sort(), [201, 409]);
      const stay = new DateRange(new Date('2026-11-12T00:00:00Z'), new Date('2026-11-14T00:00:00Z'));
      assert.equal(send.store.countConfirmed(rooms[0], stay), 1);
    });
    
    test('the store refuses an overlapping save even without the pre-check', async () => {
      // The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
      // Nothing calls hasOverlap here: save itself must refuse.
      const store = new InMemoryReservationStore();
      const stay = new DateRange(new Date('2026-11-12T00:00:00Z'), new Date('2026-11-14T00:00:00Z'));
      store.save(new Reservation('R101', new Guest('Maya', 'maya@example.com'), rooms[0], stay));
    
      const overlapping = new Reservation('R102', new Guest('Omar', 'omar@example.com'), rooms[0],
        new DateRange(new Date('2026-11-13T00:00:00Z'), new Date('2026-11-15T00:00:00Z')));
      assert.throws(() => store.save(overlapping), OverlapRejected);
      assert.equal(store.countConfirmed(rooms[0], stay), 1);
    });
    ```
    
    Dart:
    
    ```text
    // ---------------------------------------------------------------- P09
    test('two bookings at the same moment give the room one reservation', () async {
      // Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
      final app = newAppWithStore(slowStep: const Duration(milliseconds: 300));
      const sameStay = {
        'guestName': 'Omar',
        'guestEmail': 'omar@example.com',
        'room': 'R7',
        'checkIn': '2026-11-12',
        'checkOut': '2026-11-14'
      };
    
      // Started together, awaited together: Omar's check runs while Maya is still in the gap.
      final answers = await Future.wait([
        send(app.handler, 'POST', '/reservations', maya),
        send(app.handler, 'POST', '/reservations', sameStay),
      ]);
    
      expect(answers.map((answer) => answer.statusCode).toList()..sort(), [201, 409]);
      expect(
          app.store.countConfirmed(
              rooms[0], DateRange(DateTime.utc(2026, 11, 12), DateTime.utc(2026, 11, 14))),
          1);
    });
    
    test('the store refuses an overlapping save even without the pre-check', () {
      // The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
      // Nothing calls hasOverlap here: save itself must refuse.
      final store = InMemoryReservationStore();
      final stay = DateRange(DateTime.utc(2026, 11, 12), DateTime.utc(2026, 11, 14));
      store.save(Reservation('R101', Guest('Maya', 'maya@example.com'), rooms[0], stay));
    
      final overlapping = Reservation('R102', Guest('Omar', 'omar@example.com'), rooms[0],
          DateRange(DateTime.utc(2026, 11, 13), DateTime.utc(2026, 11, 15)));
      expect(() => store.save(overlapping), throwsA(isA<OverlapRejected>()));
      expect(store.countConfirmed(rooms[0], stay), 1);
    });
    ```

**Legend:**
- **Two columns with headings.** One column per request. Reading down a column follows one guest; reading across compares what the two were doing at the same moment. In this diagram: The left column is Maya’s request and the right column is Omar’s, the same as in the previous diagram.
- **Yellow box.** Something outside the app, or the step where it goes wrong. In this diagram: The two POST requests arriving.
- **White box.** An ordinary step in the sequence: one request doing one thing. In this diagram: A step the application takes: the overlap check, the INSERT, and turning the refusal into a 409.
- **Purple dashed box.** Something PostgreSQL does for you that you never write, but can observe. In this diagram: Work PostgreSQL does that nobody wrote: checking the exclusion constraint as the row is inserted.
- **Green box.** The step that does the thing this diagram is about, or the outcome it is aiming at. In this diagram: The outcome the design is aiming at: one confirmed reservation for the room.
- **Solid line with a filled arrowhead.** What happens next in that request. Follow these in order. In this diagram: What happens next in that request.
- **Number in front of a box label.** The order things happen in, across both columns: 1 first, then 2, and so on. In this diagram: 1 and 2 are the two requests, 3 and 4 the two checks, 5 is Maya being accepted and 6 is Omar being refused.

### What Step 7 adds

| What | Where | What it does |
| --- | --- | --- |
| btree_gist | db/init.sql | Teaches a GiST index how to compare text with =, so the room and the dates can live in one index |
| The exclusion constraint | db/init.sql and your running database | Refuses a Confirmed reservation whose room and dates overlap one already there |
| Mapping the refusal | The PostgreSQL store | Recognises SQLSTATE 23P01 on reservations_no_overlap and raises OverlapRejected |
| Answering Unavailable | BookingService.reserve | Turns OverlapRejected into the same Unavailable the pre-check gives, so the guest gets a readable 409 |
| The guard table | Gone | The constraint needs no row to lock, so Step 5’s table comes out again |

**Why btree_gist is needed** In short: A GiST index knows how to compare ranges but not how to compare text with =, and the constraint needs both in one index. The rule is "same room AND overlapping dates", so both comparisons have to live in the same index — they cannot be split, because the rule is about rows that satisfy both at once. btree_gist adds the missing operator class. Without it the ALTER TABLE fails with: data type text has no default operator class for access method "gist".

1. Start: a database with no extensions.
2. Action: you try to add the exclusion constraint.
3. Result: PostgreSQL refuses. After CREATE EXTENSION btree_gist the very same statement is accepted.

**The exclusion constraint** In short: A rule on the reservations table that refuses a new row when the room is the same and the dates overlap. A unique constraint refuses a row whose key equals an existing one. An exclusion constraint generalises the comparison: give it = on the room and && on the dates and it refuses exactly the rows the hotel’s rule forbids. The WHERE clause makes it apply only to Confirmed rows, so a cancelled reservation blocks nothing. Without it a guest could never rebook a stay they had cancelled. It is checked as the row goes in, by the index, so no code path can get round it — not a new endpoint, not a background job, not somebody typing INSERT into psql.

1. Start: R7 has a confirmed reservation for 12–14 November.
2. Action: INSERT INTO reservations (id, guest_name, guest_email, room, check_in, check_out, status) … for 13–15 November, straight into psql, with the app not involved at all.
3. Result:
ERROR:  conflicting key value violates exclusion constraint "reservations_no_overlap"
DETAIL:  Key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-13,2026-11-15)) conflicts with existing key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-12,2026-11-14)).

sql:

```text
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE reservations
    ADD CONSTRAINT reservations_no_overlap
    EXCLUDE USING gist (
        room WITH =,
        daterange(check_in, check_out, '[)') WITH &&
    ) WHERE (status = 'Confirmed');
```

**Turning a constraint violation into a 409** In short: The store recognises that one violation by its code and its name, and raises OverlapRejected; anything else is a real fault and is left alone. The code is SQLSTATE 23P01 and the name is reservations_no_overlap. Matching both matters: a duplicate id is 23505, and answering "that room is taken" to a duplicate id would be a lie that hid a genuine bug. That is also why the constraint is named in db/init.sql rather than letting PostgreSQL invent one — an unnamed EXCLUDE becomes reservations_room_daterange_excl, and the store would never match it.

1. Start: Omar’s INSERT is refused by the constraint.
2. Action: the store reads the error’s SQLSTATE and constraint name and finds 23P01 on reservations_no_overlap.
3. Result: it raises OverlapRejected, BookingService answers Unavailable, and Omar gets 409 Conflict with a readable message.

C#:

```text
// HotelBooking/Reservations/Internal/PostgresReservationStore.cs
using HotelBooking.Rooms;
using Microsoft.EntityFrameworkCore;
using Npgsql;

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
    // The name of the exclusion constraint in db/init.sql, and PostgreSQL's SQLSTATE for "a row
    // conflicted with an exclusion constraint". The store recognises exactly this one violation.
    private const string NoOverlapConstraint = "reservations_no_overlap";
    private const string ExclusionViolation = "23P01";

    private readonly List<(ReservationRow Row, Reservation Reservation)> loaded = [];

    public int Count => db.Reservations.Count();

    public bool HasOverlap(Room room, DateRange stay) => Confirmed(room, stay).Any();

    // nextval never hands the same number to two callers, and it does not wait for, or roll back
    // with, the transaction that asked for it. That is exactly what an id needs.
    // EF Core's SqlQuery<T> reads a scalar from a column it expects to be called "Value".
    public string NextReservationId() =>
        $"R{db.Database.SqlQuery<long>($"SELECT nextval('reservation_ids') AS \"Value\"").Single()}";

    public void Save(Reservation reservation)
    {
        var row = new ReservationRow
        {
            Id = reservation.Id, GuestName = reservation.Guest.Name, GuestEmail = reservation.Guest.Email,
            Room = reservation.Room.Number, CheckIn = reservation.Stay.CheckIn, CheckOut = reservation.Stay.CheckOut,
            Status = reservation.Status.ToString(),
        };
        var entry = db.Reservations.Add(row);
        try
        {
            db.SaveChanges(); // INSERT now: the constraint answers here, not at the end of the request
        }
        catch (DbUpdateException error) when (IsOverlapRefusal(error))
        {
            // EF Core still has this row tracked as Added, so the SaveChanges at the end of the
            // request would try the same INSERT again. Detaching it is this language's version of
            // rolling back to a savepoint: the refused statement is forgotten, the request is not.
            entry.State = EntityState.Detached;
            throw new OverlapRejectedException($"{reservation.Room.Number} already has a confirmed reservation for these nights.");
        }
    }

    // A duplicate id, or anything else, is a real fault and must not be read as a double booking.
    private static bool IsOverlapRefusal(DbUpdateException error) =>
        error.InnerException is PostgresException { SqlState: ExclusionViolation } postgres
        && postgres.ConstraintName == NoOverlapConstraint;

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

Python:

```text
# hotel_booking/reservations/internal.py
from __future__ import annotations

import threading
from datetime import date

from sqlalchemy import exists, func, select, text
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import DeclarativeBase, Mapped, Session, mapped_column

from hotel_booking.reservations.booking import OverlapRejected
from hotel_booking.reservations.domain import DateRange, Guest, Reservation
from hotel_booking.rooms import Room

# The name of the exclusion constraint in db/init.sql. The store recognises this one violation
# and turns it into OverlapRejected; any other IntegrityError is a real fault and is re-raised.
NO_OVERLAP_CONSTRAINT = "reservations_no_overlap"


class InMemoryReservationStore:
    """Keeps reservations in a list. PostgresReservationStore below has the same methods."""

    def __init__(self) -> None:
        self._rows: list[Reservation] = []
        # save must check and append without another thread fitting in between: this list is the
        # whole database here, so this lock is this store's version of the exclusion constraint.
        self._lock = threading.Lock()

    def count(self) -> int:
        return len(self._rows)

    def has_overlap(self, room: Room, stay: DateRange) -> bool:
        return any(row.blocks(room, stay) for row in self._rows)

    def next_reservation_id(self) -> str:
        with self._lock:
            return f"R{101 + len(self._rows)}"

    def save(self, reservation: Reservation) -> None:
        with self._lock:
            if self.has_overlap(reservation.room, reservation.stay):
                raise OverlapRejected(
                    f"{reservation.room.number} already has a confirmed reservation for these nights.")
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

    def next_reservation_id(self) -> str:
        # nextval never hands the same number to two callers, and it does not wait for, or roll
        # back with, the transaction that asked for it. That is exactly what an id needs.
        return f"R{self._session.scalar(text("SELECT nextval('reservation_ids')"))}"

    def save(self, reservation: Reservation) -> None:
        row = ReservationRow(id=reservation.id, guest_name=reservation.guest.name, guest_email=reservation.guest.email,
                             room=reservation.room.number, check_in=reservation.stay.check_in,
                             check_out=reservation.stay.check_out, status=reservation.status.value)
        try:
            # A SAVEPOINT, so a refused INSERT rolls back only this statement. Without it the whole
            # request's transaction would be poisoned and save_changes could not commit.
            with self._session.begin_nested():
                self._session.add(row)
                self._session.flush()  # INSERT now: the constraint answers here, not at commit
        except IntegrityError as error:
            if NO_OVERLAP_CONSTRAINT in str(error.orig):
                raise OverlapRejected(
                    f"{reservation.room.number} already has a confirmed reservation for these nights.") from error
            raise  # a duplicate id, or anything else: a real fault, not a double booking

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

Java:

```text
// src/main/java/hotelbooking/reservations/internal/PostgresReservationStore.java
package hotelbooking.reservations.internal;

import hotelbooking.reservations.DateRange;
import hotelbooking.reservations.Guest;
import hotelbooking.reservations.Reservation;
import hotelbooking.reservations.ReservationStatus;
import hotelbooking.reservations.OverlapRejectedException;
import hotelbooking.reservations.ReservationStore;
import hotelbooking.rooms.Room;
import jakarta.persistence.Entity;
import jakarta.persistence.PersistenceException;
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

    // The name of the exclusion constraint in db/init.sql, and PostgreSQL's SQLSTATE for "a row
    // conflicted with an exclusion constraint". The store recognises exactly this one violation.
    private static final String NO_OVERLAP_CONSTRAINT = "reservations_no_overlap";
    private static final String EXCLUSION_VIOLATION = "23P01";

    // Set when the constraint refused a save. Main.java reads it and rolls the request back.
    private boolean refused;

    // True when this request's transaction has been aborted by the booking rule, and so must be
    // rolled back instead of committed.
    public boolean wasRefused() {
        return refused;
    }

    // nextval never hands the same number to two callers, and it does not wait for, or roll back
    // with, the transaction that asked for it. That is exactly what an id needs, and it is what
    // stops two requests in this language both being given R101.
    @Override
    public String nextReservationId() {
        Number next = (Number) db.createNativeQuery("SELECT nextval('reservation_ids')").getSingleResult();
        return "R" + next.longValue();
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
        try {
            db.persist(row);
            db.flush(); // INSERT now: the constraint answers here, not at the end of the request
        } catch (PersistenceException failure) {
            if (!isOverlapRefusal(failure)) {
                throw failure; // a duplicate id, or anything else: a real fault, not a double booking
            }
            // Two things have to happen here, and both are specific to running inside one
            // transaction. First, take the refused row back out of the persistence context, so
            // Hibernate does not try to write it again when it next flushes. Second, remember that
            // the booking was refused: PostgreSQL has marked this transaction as aborted and will
            // refuse every further statement in it with SQLSTATE 25P02, so the unit of work must
            // roll back rather than commit. A refused booking saves nothing, so that is exactly
            // the right ending.
            db.detach(row);
            refused = true;
            throw new OverlapRejectedException(
                    reservation.room().number() + " already has a confirmed reservation for these nights.");
        }
    }

    // Hibernate wraps the driver's error, so the cause chain is what has to be read.
    private static boolean isOverlapRefusal(PersistenceException failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof java.sql.SQLException sql
                    && EXCLUSION_VIOLATION.equals(sql.getSQLState())
                    && String.valueOf(sql.getMessage()).contains(NO_OVERLAP_CONSTRAINT)) {
                return true;
            }
        }
        return false;
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

Go:

```text
// reservations/internal.go
package reservations

import (
	"cmp"
	"context"
	"errors"
	"fmt"
	"slices"
	"sync"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgconn"
	"github.com/jackc/pgx/v5/pgxpool"

	"hotelbooking/rooms"
)

// The name of the exclusion constraint in db/init.sql, and PostgreSQL's SQLSTATE for "a row
// conflicted with an exclusion constraint". The store recognises exactly this one violation.
const (
	noOverlapConstraint = "reservations_no_overlap"
	exclusionViolation  = "23P01"
)

// InMemoryReservationStore keeps reservations in a list.
// PostgresReservationStore below has the same methods.
type InMemoryReservationStore struct {
	// net/http serves every request on its own goroutine, and this list is the whole database
	// here. Checking and appending while holding the mutex is this store's version of the
	// exclusion constraint.
	mutex sync.Mutex
	rows  []*Reservation
}

func (store *InMemoryReservationStore) Count() int {
	store.mutex.Lock()
	defer store.mutex.Unlock()
	return len(store.rows)
}

func (store *InMemoryReservationStore) HasOverlap(room rooms.Room, stay DateRange) bool {
	store.mutex.Lock()
	defer store.mutex.Unlock()
	return store.hasOverlap(room, stay)
}

// hasOverlap expects the mutex to be held already.
func (store *InMemoryReservationStore) hasOverlap(room rooms.Room, stay DateRange) bool {
	for _, row := range store.rows {
		if row.Blocks(room, stay) {
			return true
		}
	}
	return false
}

// NextReservationID is safe here for the same reason Save is: it holds the same mutex.
func (store *InMemoryReservationStore) NextReservationID() string {
	store.mutex.Lock()
	defer store.mutex.Unlock()
	return fmt.Sprintf("R%d", 101+len(store.rows))
}

func (store *InMemoryReservationStore) Save(reservation *Reservation) error {
	store.mutex.Lock()
	defer store.mutex.Unlock()
	if store.hasOverlap(reservation.Room, reservation.Stay) {
		return fmt.Errorf("%w: %s", ErrOverlapRejected, reservation.Room.Number)
	}
	store.rows = append(store.rows, reservation)
	return nil
}

// Returns nil when no reservation has that id.
func (store *InMemoryReservationStore) Find(id string) *Reservation {
	store.mutex.Lock()
	defer store.mutex.Unlock()
	for _, row := range store.rows {
		if row.ID == id {
			return row
		}
	}
	return nil
}

func (store *InMemoryReservationStore) CountConfirmed(room rooms.Room, stay DateRange) int {
	store.mutex.Lock()
	defer store.mutex.Unlock()
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

// NextReservationID takes the id from the sequence. nextval never hands the same number to two
// callers, and it does not wait for, or roll back with, the transaction that asked for it.
func (store *PostgresReservationStore) NextReservationID() string {
	var next int64
	if err := store.pool.QueryRow(store.ctx, "SELECT nextval('reservation_ids')").Scan(&next); err != nil {
		store.err = cmp.Or(store.err, err)
		return ""
	}
	return fmt.Sprintf("R%d", next)
}

func (store *PostgresReservationStore) Save(reservation *Reservation) error {
	if store.err != nil {
		return nil // an earlier query failed: write nothing, SaveChanges reports the error
	}
	_, err := store.pool.Exec(store.ctx, "INSERT INTO reservations VALUES ($1, $2, $3, $4, $5, $6, $7)",
		reservation.ID, reservation.Guest.Name, reservation.Guest.Email, reservation.Room.Number,
		reservation.Stay.CheckIn, reservation.Stay.CheckOut, string(reservation.Status))
	// The INSERT happens here, so the constraint answers here too.
	if isOverlapRefusal(err) {
		// Each statement stands on its own in pgx, so nothing has to be rolled back: the refused
		// INSERT changed nothing and the pool is still usable for the rest of the request.
		return fmt.Errorf("%w: %s", ErrOverlapRejected, reservation.Room.Number)
	}
	store.err = cmp.Or(store.err, err) // a duplicate id, or anything else: a real fault
	return nil
}

// isOverlapRefusal is true only for the exclusion constraint of db/init.sql, so a duplicate id or
// a broken connection is never read as a double booking.
func isOverlapRefusal(err error) bool {
	var postgres *pgconn.PgError
	return errors.As(err, &postgres) &&
		postgres.Code == exclusionViolation &&
		postgres.ConstraintName == noOverlapConstraint
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

JavaScript:

```text
// src/reservations/internal/postgres-store.js
import { DateRange, Guest, Reservation } from '../domain.js';
import { OverlapRejected } from '../booking.js';

// A date as PostgreSQL's date type writes it: 2026-11-12.
const day = date => date.toISOString().slice(0, 10);

// Confirmed reservations of a room that share at least one night with a stay.
const confirmedOverlaps = "FROM reservations WHERE room = $1 AND status = 'Confirmed' AND check_in < $3 AND $2 < check_out";

// The name of the exclusion constraint in db/init.sql. node-postgres puts it in error.constraint,
// so the store recognises this one violation and leaves every other error alone.
const NO_OVERLAP_CONSTRAINT = 'reservations_no_overlap';

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

  // nextval never hands the same number to two callers, and it does not wait for, or roll back
  // with, the transaction that asked for it. That is exactly what an id needs.
  async nextReservationId() {
    const { rows } = await this.#pool.query("SELECT nextval('reservation_ids') AS id");
    return `R${rows[0].id}`;
  }

  // INSERT now: a second R101 is refused here, and so is a stay that overlaps one already saved.
  async save(reservation) {
    try {
      await this.#pool.query('INSERT INTO reservations VALUES ($1, $2, $3, $4, $5, $6, $7)', [reservation.id, reservation.guest.name,
        reservation.guest.email, reservation.room.number, day(reservation.stay.checkIn), day(reservation.stay.checkOut), reservation.status]);
    } catch (error) {
      if (error.constraint === NO_OVERLAP_CONSTRAINT) {
        throw new OverlapRejected(`${reservation.room.number} already has a confirmed reservation for these nights.`);
      }
      throw error; // a duplicate id, or anything else: a real fault, not a double booking
    }
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
// lib/reservations/src/postgres_store.dart
import 'package:hotel_booking/rooms/rooms.dart';
import 'package:postgres/postgres.dart' hide DateRange; // the stay is our own DateRange

import 'booking.dart';
import 'domain.dart';

// The name of the exclusion constraint in db/init.sql, and PostgreSQL's SQLSTATE for "a row
// conflicted with an exclusion constraint". The store recognises exactly this one violation and
// leaves every other error alone.
const noOverlapConstraint = 'reservations_no_overlap';
const exclusionViolation = '23P01';

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

  // nextval never hands the same number to two callers, and it does not wait for, or roll back
  // with, the transaction that asked for it. That is exactly what an id needs.
  @override
  Future<String> nextReservationId() async {
    final rows = await _pool.execute("SELECT nextval('reservation_ids')");
    return 'R${rows.first.first}';
  }

  // INSERT now: the constraint answers here, not at the end of the request.
  @override
  Future<void> save(Reservation reservation) async {
    try {
      await _pool.execute(
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
    } on ServerException catch (error) {
      if (error.code == exclusionViolation && error.constraintName == noOverlapConstraint) {
        throw OverlapRejected(
            '${reservation.room.number} already has a confirmed reservation for these nights.');
      }
      rethrow; // a duplicate id, or anything else: a real fault, not a double booking
    }
  }

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

**What the store now promises** In short: save refuses an overlapping reservation rather than writing one. Every store keeps that promise; how is its own business. The list store keeps it with a check under a lock. PostgreSQL keeps it with the exclusion constraint. Because both keep the same promise, the two new tests need no database and still test the rule the constraint protects. The pre-check in reserve was deliberately kept in front of it. It answers the ordinary case with a readable 409; the promise is what makes the rule true when two requests check at the same moment.

1. Start: R7 has a confirmed reservation for 12–14 November.
2. Action: something calls save directly with an overlapping reservation, skipping the pre-check.
3. Result: save raises OverlapRejected, and nothing is written.

C#:

```text
// HotelBooking/Reservations/IReservationStore.cs
using HotelBooking.Rooms;

namespace HotelBooking.Reservations;

// What BookingService needs from storage. Today a list in memory; from P07 a database.
public interface IReservationStore
{
    int Count { get; }
    bool HasOverlap(Room room, DateRange stay);

    // The id for the next reservation.
    //
    // The store owns this, because only the store can hand out an id that no other request can
    // also be given. Counting the rows and adding one cannot: two requests that count before
    // either has saved both get R101, and the second INSERT then fails on the primary key instead
    // of on the booking rule. PostgreSQL answers from a sequence; the list store counts, which is
    // safe there because it holds a lock.
    string NextReservationId();

    // Writes the reservation.
    //
    // Throws OverlapRejectedException if writing it would give the room two Confirmed reservations
    // covering the same night. The pre-check in Reserve usually catches that first; this promise is
    // what makes the rule true when two requests check at the same moment.
    void Save(Reservation reservation);

    Reservation? Find(string id);
    int CountConfirmed(Room room, DateRange stay);
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


class OverlapRejected(Exception):
    """The store refused a save because it would give the room two Confirmed reservations
    covering the same night.

    Every store promises this, and the promise is part of the ReservationStore contract. How a
    store keeps it is its own business: the list store checks inside save, and PostgreSQL refuses
    the INSERT with the exclusion constraint reservations_no_overlap. BookingService turns it into
    Unavailable - the same answer the pre-check gives - so a guest never sees a database error.
    """


class ReservationStore(Protocol):
    """What BookingService needs from storage. Today a list in memory; from P07 a database."""

    def count(self) -> int: ...
    def has_overlap(self, room: Room, stay: DateRange) -> bool: ...

    def next_reservation_id(self) -> str:
        """The id for the next reservation.

        The store owns this, because only the store can hand out an id that no other request can
        also be given. Counting the rows and adding one cannot: two requests that count before
        either has saved both get R101, and the second INSERT then fails on the primary key instead
        of on the booking rule. PostgreSQL answers from a sequence; the list store counts, which is
        safe there because it holds a lock.
        """
        ...

    def save(self, reservation: Reservation) -> None:
        """Writes the reservation.

        Raises OverlapRejected if writing it would give the room two Confirmed reservations
        covering the same night. The pre-check in reserve() usually catches that first; this
        promise is what makes the rule true when two requests check at the same moment.
        """
        ...

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

        time.sleep(self.slow_step_seconds)  # 0 normally; the race tool makes this step slow

        reservation = Reservation(self.store.next_reservation_id(), guest, room, stay)
        try:
            self.store.save(reservation)
        except OverlapRejected:
            # Another request saved an overlapping reservation after our check and before our save.
            # The check was honest when it ran; it had simply stopped being true. The guest gets the
            # same answer either way.
            return BookingResult("Unavailable")
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
// src/main/java/hotelbooking/reservations/ReservationStore.java
package hotelbooking.reservations;

import hotelbooking.rooms.Room;

// What BookingService needs from storage. Today a list in memory; from P07 a database.
public interface ReservationStore {
    int count();

    boolean hasOverlap(Room room, DateRange stay);

    // The id for the next reservation.
    //
    // The store owns this, because only the store can hand out an id that no other request can
    // also be given. Counting the rows and adding one cannot: two requests that count before either
    // has saved both get R101, and the second INSERT then fails on the primary key instead of on
    // the booking rule. That is exactly what happens here, because Spring wraps each request in one
    // transaction, so an uncommitted row is invisible to the other request's count.
    // PostgreSQL answers from a sequence; the list store counts, which is safe there because it
    // holds a lock.
    String nextReservationId();

    // Writes the reservation.
    //
    // Throws OverlapRejectedException if writing it would give the room two Confirmed reservations
    // covering the same night. The pre-check in reserve usually catches that first; this promise is
    // what makes the rule true when two requests check at the same moment.
    void save(Reservation reservation);

    // Returns null when no reservation has that id.
    Reservation find(String id);

    int countConfirmed(Room room, DateRange stay);
}
```

Go:

```text
// reservations/booking.go
package reservations

import (
	"errors"
	"time"

	"hotelbooking/notifications"
	"hotelbooking/rooms"
)

// ErrOverlapRejected is returned by Save when writing a reservation would give the room two
// Confirmed reservations covering the same night.
//
// Every store promises this, and the promise is part of ReservationStore. How a store keeps it is
// its own business: the list store checks inside Save, and PostgreSQL refuses the INSERT with the
// exclusion constraint reservations_no_overlap. BookingService turns it into Unavailable - the same
// answer the pre-check gives - so a guest never sees a database error.
var ErrOverlapRejected = errors.New("the room already has a confirmed reservation for these nights")

// ReservationStore is what BookingService needs from storage.
// Today a list in memory; from P07 a database.
type ReservationStore interface {
	Count() int
	HasOverlap(room rooms.Room, stay DateRange) bool

	// NextReservationID is the id for the next reservation.
	//
	// The store owns this, because only the store can hand out an id that no other request can
	// also be given. Counting the rows and adding one cannot: two requests that count before
	// either has saved both get R101, and the second INSERT then fails on the primary key instead
	// of on the booking rule. PostgreSQL answers from a sequence; the list store counts, which is
	// safe there because it holds the mutex.
	NextReservationID() string

	// Save writes the reservation. It returns an error wrapping ErrOverlapRejected if writing it
	// would give the room two Confirmed reservations covering the same night. The pre-check in
	// Reserve usually catches that first; this promise is what makes the rule true when two
	// requests check at the same moment.
	//
	// Any other kind of failure is not returned here: the store keeps it and SaveChanges reports
	// it, the same as in P07.
	Save(reservation *Reservation) error

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

	time.Sleep(service.slowStep) // zero normally; the race tool makes this step slow

	reservation := NewReservation(service.store.NextReservationID(), guest, room, stay)
	if err := service.store.Save(reservation); errors.Is(err, ErrOverlapRejected) {
		// Another request saved an overlapping reservation after our check and before our save.
		// The check was honest when it ran; it had simply stopped being true. The guest gets the
		// same answer either way.
		return BookingResult{Outcome: "Unavailable"}
	}
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
// src/reservations/internal/store.js
import { OverlapRejected } from '../booking.js';

// What BookingService needs from storage. JavaScript has no interface keyword, so the
// contract is written down here and every store keeps to it:
//
//   count                       how many reservations are saved
//   hasOverlap(room, stay)      is a Confirmed reservation already using those nights?
//   nextReservationId()         the id for the next reservation. The store owns this, because only
//                               the store can hand out an id no other request can also be given:
//                               counting the rows and adding one cannot, since two requests that
//                               count before either has saved both get R101
//   save(reservation)           keep this reservation; throws OverlapRejected rather than
//                               giving the room two Confirmed reservations for the same night
//   find(id)                    the reservation with that id, or null
//   countConfirmed(room, stay)  how many Confirmed reservations use those nights
//
// A store may answer count, hasOverlap, save and find with a Promise (a database does);
// BookingService awaits them. This store keeps a list in memory and answers at once.
export class InMemoryReservationStore {
  #rows = [];

  get count() {
    return this.#rows.length;
  }

  hasOverlap(room, stay) {
    return this.#rows.some(row => row.blocks(room, stay));
  }

  // Safe here for the same reason save is: nothing else can run between the read and the use.
  nextReservationId() {
    return `R${101 + this.#rows.length}`;
  }

  // No lock, and none is needed: Node runs one request's code at a time, and there is no await
  // between the check and the push, so no other request can run in the middle of this method.
  // This check is the list store's version of the exclusion constraint.
  save(reservation) {
    if (this.hasOverlap(reservation.room, reservation.stay)) {
      throw new OverlapRejected(`${reservation.room.number} already has a confirmed reservation for these nights.`);
    }
    this.#rows.push(reservation);
  }

  // Returns null when no reservation has that id.
  find(id) {
    return this.#rows.find(row => row.id === id) ?? null;
  }

  countConfirmed(room, stay) {
    return this.#rows.filter(row => row.blocks(room, stay)).length;
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

/// The store refused a save because it would give the room two Confirmed reservations covering the
/// same night.
///
/// Every store promises this, and the promise is part of [ReservationStore]. How a store keeps it
/// is its own business: the list store checks inside save, and PostgreSQL refuses the INSERT with
/// the exclusion constraint reservations_no_overlap. BookingService turns it into Unavailable - the
/// same answer the pre-check gives - so a guest never sees a database error.
class OverlapRejected implements Exception {
  OverlapRejected(this.message);

  final String message;

  @override
  String toString() => 'OverlapRejected: $message';
}

// What BookingService needs from storage: a list in memory answers at once, a database
// answers later (a Future). FutureOr allows both, and BookingService awaits the answer.
abstract interface class ReservationStore {
  FutureOr<int> get count;

  FutureOr<bool> hasOverlap(Room room, DateRange stay);

  /// The id for the next reservation.
  ///
  /// The store owns this, because only the store can hand out an id that no other request can
  /// also be given. Counting the rows and adding one cannot: two requests that count before either
  /// has saved both get R101, and the second INSERT then fails on the primary key instead of on the
  /// booking rule. PostgreSQL answers from a sequence; the list store counts, which is safe there
  /// because nothing can run in the middle of it.
  FutureOr<String> nextReservationId();

  /// Writes the reservation.
  ///
  /// Throws [OverlapRejected] if writing it would give the room two Confirmed reservations
  /// covering the same night. The pre-check in reserve usually catches that first; this promise is
  /// what makes the rule true when two requests check at the same moment.
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

    final reservation = Reservation(await store.nextReservationId(), guest, room, stay);
    try {
      await store.save(reservation);
    } on OverlapRejected {
      // Another request saved an overlapping reservation after our check and before our save. The
      // check was honest when it ran; it had simply stopped being true.
      return BookingResult('Unavailable');
    }
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

### Completed example 7.1 · it will not switch on yet

Step 1 deliberately left a double booking in the table, and a constraint cannot be added to rows that already break it. This is not a quirk of the exercise — it is what turning a rule on over live data is always like.

**Why the constraint would not switch on** In short: A constraint cannot be added to a table whose rows already break it, and Step 1 deliberately left a double booking in the table. This is not a quirk of the exercise. It is what turning on a constraint over live data is always like: the rule has been broken for as long as it went unenforced, and the broken rows are still there. Finding them and deciding what to do about them is part of the work. Cancelling is the honest action here — a real hotel would have to telephone one of the two guests — and a Cancelled row does not block the constraint.

1. Start: R7 has the two confirmed reservations Step 1 created.
2. Action: you run the ALTER TABLE.
3. Result:
ERROR:  could not create exclusion constraint "reservations_no_overlap"
DETAIL:  Key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-12,2026-11-14)) conflicts with key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-12,2026-11-14)).

After UPDATE reservations SET status = 'Cancelled' WHERE id = 'R102'; the same ALTER TABLE prints ALTER TABLE.

### psql

```text
ERROR:  could not create exclusion constraint "reservations_no_overlap"
DETAIL:  Key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-12,2026-11-14)) conflicts with key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-12,2026-11-14)).
```

### Completed example 7.2 · clean up first

Cancelling is the honest action: a real hotel would have to telephone one of the two guests. A Cancelled row does not block the constraint.

### In psql

```text
-- Step 1 left two confirmed reservations for R7. The constraint cannot be added while they are
-- both there, so one of them has to go first. Cancelling is the honest action: a real hotel would
-- have to tell one of the two guests, and a Cancelled row does not block the constraint.
UPDATE reservations SET status = 'Cancelled' WHERE id = 'R102';
```

### Completed example 7.3 · now add the rule

**The exclusion constraint** In short: A rule on the reservations table that refuses a new row when the room is the same and the dates overlap. A unique constraint refuses a row whose key equals an existing one. An exclusion constraint generalises the comparison: give it = on the room and && on the dates and it refuses exactly the rows the hotel’s rule forbids. The WHERE clause makes it apply only to Confirmed rows, so a cancelled reservation blocks nothing. Without it a guest could never rebook a stay they had cancelled. It is checked as the row goes in, by the index, so no code path can get round it — not a new endpoint, not a background job, not somebody typing INSERT into psql.

1. Start: R7 has a confirmed reservation for 12–14 November.
2. Action: INSERT INTO reservations (id, guest_name, guest_email, room, check_in, check_out, status) … for 13–15 November, straight into psql, with the app not involved at all.
3. Result:
ERROR:  conflicting key value violates exclusion constraint "reservations_no_overlap"
DETAIL:  Key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-13,2026-11-15)) conflicts with existing key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-12,2026-11-14)).

sql:

```text
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE reservations
    ADD CONSTRAINT reservations_no_overlap
    EXCLUDE USING gist (
        room WITH =,
        daterange(check_in, check_out, '[)') WITH &&
    ) WHERE (status = 'Confirmed');
```

### In psql, and in db/init.sql

```text
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE reservations
    ADD CONSTRAINT reservations_no_overlap
    EXCLUDE USING gist (
        room WITH =,
        daterange(check_in, check_out, '[)') WITH &&
    ) WHERE (status = 'Confirmed');
```

### Completed example 7.4 · db/init.sql at the end of P09

init.sql only runs when the database is created, so the rule has to be in both places: in the running database, and in the file that builds the next one.

### db/init.sql

```text
-- The hotel booking app's tables. PostgreSQL runs this file once, when the database is first created.
-- Rooms owns the rooms table and Reservations owns the reservations table, so there is no foreign key
-- between them: each module keeps its own data (L03).

-- P09 Step 7: the overlap rule, enforced by PostgreSQL itself.
-- btree_gist lets a GiST index hold plain equality (room WITH =) next to a range test
-- (daterange WITH &&). Without it PostgreSQL refuses the constraint: gist has no
-- equality operator class for text on its own.
CREATE EXTENSION IF NOT EXISTS btree_gist;

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
    CHECK (check_out > check_in),

    CONSTRAINT reservations_no_overlap EXCLUDE USING gist (
        room WITH =,
        daterange(check_in, check_out, '[)') WITH &&
    ) WHERE (status = 'Confirmed')
);

-- P09 Step 1: reservation ids come from here, not from counting the rows.
-- nextval never returns the same number twice, even to two transactions running at the same moment.
CREATE SEQUENCE reservation_ids START 101;

INSERT INTO rooms (number, type) VALUES ('R7', 'Standard'), ('R8', 'Deluxe');

```

### Completed example 7.5 · the store maps the refusal

**Turning a constraint violation into a 409** In short: The store recognises that one violation by its code and its name, and raises OverlapRejected; anything else is a real fault and is left alone. The code is SQLSTATE 23P01 and the name is reservations_no_overlap. Matching both matters: a duplicate id is 23505, and answering "that room is taken" to a duplicate id would be a lie that hid a genuine bug. That is also why the constraint is named in db/init.sql rather than letting PostgreSQL invent one — an unnamed EXCLUDE becomes reservations_room_daterange_excl, and the store would never match it.

1. Start: Omar’s INSERT is refused by the constraint.
2. Action: the store reads the error’s SQLSTATE and constraint name and finds 23P01 on reservations_no_overlap.
3. Result: it raises OverlapRejected, BookingService answers Unavailable, and Omar gets 409 Conflict with a readable message.

C#:

```text
// HotelBooking/Reservations/Internal/PostgresReservationStore.cs
using HotelBooking.Rooms;
using Microsoft.EntityFrameworkCore;
using Npgsql;

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
    // The name of the exclusion constraint in db/init.sql, and PostgreSQL's SQLSTATE for "a row
    // conflicted with an exclusion constraint". The store recognises exactly this one violation.
    private const string NoOverlapConstraint = "reservations_no_overlap";
    private const string ExclusionViolation = "23P01";

    private readonly List<(ReservationRow Row, Reservation Reservation)> loaded = [];

    public int Count => db.Reservations.Count();

    public bool HasOverlap(Room room, DateRange stay) => Confirmed(room, stay).Any();

    // nextval never hands the same number to two callers, and it does not wait for, or roll back
    // with, the transaction that asked for it. That is exactly what an id needs.
    // EF Core's SqlQuery<T> reads a scalar from a column it expects to be called "Value".
    public string NextReservationId() =>
        $"R{db.Database.SqlQuery<long>($"SELECT nextval('reservation_ids') AS \"Value\"").Single()}";

    public void Save(Reservation reservation)
    {
        var row = new ReservationRow
        {
            Id = reservation.Id, GuestName = reservation.Guest.Name, GuestEmail = reservation.Guest.Email,
            Room = reservation.Room.Number, CheckIn = reservation.Stay.CheckIn, CheckOut = reservation.Stay.CheckOut,
            Status = reservation.Status.ToString(),
        };
        var entry = db.Reservations.Add(row);
        try
        {
            db.SaveChanges(); // INSERT now: the constraint answers here, not at the end of the request
        }
        catch (DbUpdateException error) when (IsOverlapRefusal(error))
        {
            // EF Core still has this row tracked as Added, so the SaveChanges at the end of the
            // request would try the same INSERT again. Detaching it is this language's version of
            // rolling back to a savepoint: the refused statement is forgotten, the request is not.
            entry.State = EntityState.Detached;
            throw new OverlapRejectedException($"{reservation.Room.Number} already has a confirmed reservation for these nights.");
        }
    }

    // A duplicate id, or anything else, is a real fault and must not be read as a double booking.
    private static bool IsOverlapRefusal(DbUpdateException error) =>
        error.InnerException is PostgresException { SqlState: ExclusionViolation } postgres
        && postgres.ConstraintName == NoOverlapConstraint;

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

Python:

```text
# hotel_booking/reservations/internal.py
from __future__ import annotations

import threading
from datetime import date

from sqlalchemy import exists, func, select, text
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import DeclarativeBase, Mapped, Session, mapped_column

from hotel_booking.reservations.booking import OverlapRejected
from hotel_booking.reservations.domain import DateRange, Guest, Reservation
from hotel_booking.rooms import Room

# The name of the exclusion constraint in db/init.sql. The store recognises this one violation
# and turns it into OverlapRejected; any other IntegrityError is a real fault and is re-raised.
NO_OVERLAP_CONSTRAINT = "reservations_no_overlap"


class InMemoryReservationStore:
    """Keeps reservations in a list. PostgresReservationStore below has the same methods."""

    def __init__(self) -> None:
        self._rows: list[Reservation] = []
        # save must check and append without another thread fitting in between: this list is the
        # whole database here, so this lock is this store's version of the exclusion constraint.
        self._lock = threading.Lock()

    def count(self) -> int:
        return len(self._rows)

    def has_overlap(self, room: Room, stay: DateRange) -> bool:
        return any(row.blocks(room, stay) for row in self._rows)

    def next_reservation_id(self) -> str:
        with self._lock:
            return f"R{101 + len(self._rows)}"

    def save(self, reservation: Reservation) -> None:
        with self._lock:
            if self.has_overlap(reservation.room, reservation.stay):
                raise OverlapRejected(
                    f"{reservation.room.number} already has a confirmed reservation for these nights.")
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

    def next_reservation_id(self) -> str:
        # nextval never hands the same number to two callers, and it does not wait for, or roll
        # back with, the transaction that asked for it. That is exactly what an id needs.
        return f"R{self._session.scalar(text("SELECT nextval('reservation_ids')"))}"

    def save(self, reservation: Reservation) -> None:
        row = ReservationRow(id=reservation.id, guest_name=reservation.guest.name, guest_email=reservation.guest.email,
                             room=reservation.room.number, check_in=reservation.stay.check_in,
                             check_out=reservation.stay.check_out, status=reservation.status.value)
        try:
            # A SAVEPOINT, so a refused INSERT rolls back only this statement. Without it the whole
            # request's transaction would be poisoned and save_changes could not commit.
            with self._session.begin_nested():
                self._session.add(row)
                self._session.flush()  # INSERT now: the constraint answers here, not at commit
        except IntegrityError as error:
            if NO_OVERLAP_CONSTRAINT in str(error.orig):
                raise OverlapRejected(
                    f"{reservation.room.number} already has a confirmed reservation for these nights.") from error
            raise  # a duplicate id, or anything else: a real fault, not a double booking

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

Java:

```text
// src/main/java/hotelbooking/reservations/internal/PostgresReservationStore.java
package hotelbooking.reservations.internal;

import hotelbooking.reservations.DateRange;
import hotelbooking.reservations.Guest;
import hotelbooking.reservations.Reservation;
import hotelbooking.reservations.ReservationStatus;
import hotelbooking.reservations.OverlapRejectedException;
import hotelbooking.reservations.ReservationStore;
import hotelbooking.rooms.Room;
import jakarta.persistence.Entity;
import jakarta.persistence.PersistenceException;
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

    // The name of the exclusion constraint in db/init.sql, and PostgreSQL's SQLSTATE for "a row
    // conflicted with an exclusion constraint". The store recognises exactly this one violation.
    private static final String NO_OVERLAP_CONSTRAINT = "reservations_no_overlap";
    private static final String EXCLUSION_VIOLATION = "23P01";

    // Set when the constraint refused a save. Main.java reads it and rolls the request back.
    private boolean refused;

    // True when this request's transaction has been aborted by the booking rule, and so must be
    // rolled back instead of committed.
    public boolean wasRefused() {
        return refused;
    }

    // nextval never hands the same number to two callers, and it does not wait for, or roll back
    // with, the transaction that asked for it. That is exactly what an id needs, and it is what
    // stops two requests in this language both being given R101.
    @Override
    public String nextReservationId() {
        Number next = (Number) db.createNativeQuery("SELECT nextval('reservation_ids')").getSingleResult();
        return "R" + next.longValue();
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
        try {
            db.persist(row);
            db.flush(); // INSERT now: the constraint answers here, not at the end of the request
        } catch (PersistenceException failure) {
            if (!isOverlapRefusal(failure)) {
                throw failure; // a duplicate id, or anything else: a real fault, not a double booking
            }
            // Two things have to happen here, and both are specific to running inside one
            // transaction. First, take the refused row back out of the persistence context, so
            // Hibernate does not try to write it again when it next flushes. Second, remember that
            // the booking was refused: PostgreSQL has marked this transaction as aborted and will
            // refuse every further statement in it with SQLSTATE 25P02, so the unit of work must
            // roll back rather than commit. A refused booking saves nothing, so that is exactly
            // the right ending.
            db.detach(row);
            refused = true;
            throw new OverlapRejectedException(
                    reservation.room().number() + " already has a confirmed reservation for these nights.");
        }
    }

    // Hibernate wraps the driver's error, so the cause chain is what has to be read.
    private static boolean isOverlapRefusal(PersistenceException failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof java.sql.SQLException sql
                    && EXCLUSION_VIOLATION.equals(sql.getSQLState())
                    && String.valueOf(sql.getMessage()).contains(NO_OVERLAP_CONSTRAINT)) {
                return true;
            }
        }
        return false;
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

Go:

```text
// reservations/internal.go
package reservations

import (
	"cmp"
	"context"
	"errors"
	"fmt"
	"slices"
	"sync"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgconn"
	"github.com/jackc/pgx/v5/pgxpool"

	"hotelbooking/rooms"
)

// The name of the exclusion constraint in db/init.sql, and PostgreSQL's SQLSTATE for "a row
// conflicted with an exclusion constraint". The store recognises exactly this one violation.
const (
	noOverlapConstraint = "reservations_no_overlap"
	exclusionViolation  = "23P01"
)

// InMemoryReservationStore keeps reservations in a list.
// PostgresReservationStore below has the same methods.
type InMemoryReservationStore struct {
	// net/http serves every request on its own goroutine, and this list is the whole database
	// here. Checking and appending while holding the mutex is this store's version of the
	// exclusion constraint.
	mutex sync.Mutex
	rows  []*Reservation
}

func (store *InMemoryReservationStore) Count() int {
	store.mutex.Lock()
	defer store.mutex.Unlock()
	return len(store.rows)
}

func (store *InMemoryReservationStore) HasOverlap(room rooms.Room, stay DateRange) bool {
	store.mutex.Lock()
	defer store.mutex.Unlock()
	return store.hasOverlap(room, stay)
}

// hasOverlap expects the mutex to be held already.
func (store *InMemoryReservationStore) hasOverlap(room rooms.Room, stay DateRange) bool {
	for _, row := range store.rows {
		if row.Blocks(room, stay) {
			return true
		}
	}
	return false
}

// NextReservationID is safe here for the same reason Save is: it holds the same mutex.
func (store *InMemoryReservationStore) NextReservationID() string {
	store.mutex.Lock()
	defer store.mutex.Unlock()
	return fmt.Sprintf("R%d", 101+len(store.rows))
}

func (store *InMemoryReservationStore) Save(reservation *Reservation) error {
	store.mutex.Lock()
	defer store.mutex.Unlock()
	if store.hasOverlap(reservation.Room, reservation.Stay) {
		return fmt.Errorf("%w: %s", ErrOverlapRejected, reservation.Room.Number)
	}
	store.rows = append(store.rows, reservation)
	return nil
}

// Returns nil when no reservation has that id.
func (store *InMemoryReservationStore) Find(id string) *Reservation {
	store.mutex.Lock()
	defer store.mutex.Unlock()
	for _, row := range store.rows {
		if row.ID == id {
			return row
		}
	}
	return nil
}

func (store *InMemoryReservationStore) CountConfirmed(room rooms.Room, stay DateRange) int {
	store.mutex.Lock()
	defer store.mutex.Unlock()
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

// NextReservationID takes the id from the sequence. nextval never hands the same number to two
// callers, and it does not wait for, or roll back with, the transaction that asked for it.
func (store *PostgresReservationStore) NextReservationID() string {
	var next int64
	if err := store.pool.QueryRow(store.ctx, "SELECT nextval('reservation_ids')").Scan(&next); err != nil {
		store.err = cmp.Or(store.err, err)
		return ""
	}
	return fmt.Sprintf("R%d", next)
}

func (store *PostgresReservationStore) Save(reservation *Reservation) error {
	if store.err != nil {
		return nil // an earlier query failed: write nothing, SaveChanges reports the error
	}
	_, err := store.pool.Exec(store.ctx, "INSERT INTO reservations VALUES ($1, $2, $3, $4, $5, $6, $7)",
		reservation.ID, reservation.Guest.Name, reservation.Guest.Email, reservation.Room.Number,
		reservation.Stay.CheckIn, reservation.Stay.CheckOut, string(reservation.Status))
	// The INSERT happens here, so the constraint answers here too.
	if isOverlapRefusal(err) {
		// Each statement stands on its own in pgx, so nothing has to be rolled back: the refused
		// INSERT changed nothing and the pool is still usable for the rest of the request.
		return fmt.Errorf("%w: %s", ErrOverlapRejected, reservation.Room.Number)
	}
	store.err = cmp.Or(store.err, err) // a duplicate id, or anything else: a real fault
	return nil
}

// isOverlapRefusal is true only for the exclusion constraint of db/init.sql, so a duplicate id or
// a broken connection is never read as a double booking.
func isOverlapRefusal(err error) bool {
	var postgres *pgconn.PgError
	return errors.As(err, &postgres) &&
		postgres.Code == exclusionViolation &&
		postgres.ConstraintName == noOverlapConstraint
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

JavaScript:

```text
// src/reservations/internal/postgres-store.js
import { DateRange, Guest, Reservation } from '../domain.js';
import { OverlapRejected } from '../booking.js';

// A date as PostgreSQL's date type writes it: 2026-11-12.
const day = date => date.toISOString().slice(0, 10);

// Confirmed reservations of a room that share at least one night with a stay.
const confirmedOverlaps = "FROM reservations WHERE room = $1 AND status = 'Confirmed' AND check_in < $3 AND $2 < check_out";

// The name of the exclusion constraint in db/init.sql. node-postgres puts it in error.constraint,
// so the store recognises this one violation and leaves every other error alone.
const NO_OVERLAP_CONSTRAINT = 'reservations_no_overlap';

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

  // nextval never hands the same number to two callers, and it does not wait for, or roll back
  // with, the transaction that asked for it. That is exactly what an id needs.
  async nextReservationId() {
    const { rows } = await this.#pool.query("SELECT nextval('reservation_ids') AS id");
    return `R${rows[0].id}`;
  }

  // INSERT now: a second R101 is refused here, and so is a stay that overlaps one already saved.
  async save(reservation) {
    try {
      await this.#pool.query('INSERT INTO reservations VALUES ($1, $2, $3, $4, $5, $6, $7)', [reservation.id, reservation.guest.name,
        reservation.guest.email, reservation.room.number, day(reservation.stay.checkIn), day(reservation.stay.checkOut), reservation.status]);
    } catch (error) {
      if (error.constraint === NO_OVERLAP_CONSTRAINT) {
        throw new OverlapRejected(`${reservation.room.number} already has a confirmed reservation for these nights.`);
      }
      throw error; // a duplicate id, or anything else: a real fault, not a double booking
    }
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
// lib/reservations/src/postgres_store.dart
import 'package:hotel_booking/rooms/rooms.dart';
import 'package:postgres/postgres.dart' hide DateRange; // the stay is our own DateRange

import 'booking.dart';
import 'domain.dart';

// The name of the exclusion constraint in db/init.sql, and PostgreSQL's SQLSTATE for "a row
// conflicted with an exclusion constraint". The store recognises exactly this one violation and
// leaves every other error alone.
const noOverlapConstraint = 'reservations_no_overlap';
const exclusionViolation = '23P01';

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

  // nextval never hands the same number to two callers, and it does not wait for, or roll back
  // with, the transaction that asked for it. That is exactly what an id needs.
  @override
  Future<String> nextReservationId() async {
    final rows = await _pool.execute("SELECT nextval('reservation_ids')");
    return 'R${rows.first.first}';
  }

  // INSERT now: the constraint answers here, not at the end of the request.
  @override
  Future<void> save(Reservation reservation) async {
    try {
      await _pool.execute(
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
    } on ServerException catch (error) {
      if (error.code == exclusionViolation && error.constraintName == noOverlapConstraint) {
        throw OverlapRejected(
            '${reservation.room.number} already has a confirmed reservation for these nights.');
      }
      rethrow; // a duplicate id, or anything else: a real fault, not a double booking
    }
  }

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

### The PostgreSQL store

#### C#

```csharp
// HotelBooking/Reservations/Internal/PostgresReservationStore.cs
using HotelBooking.Rooms;
using Microsoft.EntityFrameworkCore;
using Npgsql;

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
    // The name of the exclusion constraint in db/init.sql, and PostgreSQL's SQLSTATE for "a row
    // conflicted with an exclusion constraint". The store recognises exactly this one violation.
    private const string NoOverlapConstraint = "reservations_no_overlap";
    private const string ExclusionViolation = "23P01";

    private readonly List<(ReservationRow Row, Reservation Reservation)> loaded = [];

    public int Count => db.Reservations.Count();

    public bool HasOverlap(Room room, DateRange stay) => Confirmed(room, stay).Any();

    // nextval never hands the same number to two callers, and it does not wait for, or roll back
    // with, the transaction that asked for it. That is exactly what an id needs.
    // EF Core's SqlQuery<T> reads a scalar from a column it expects to be called "Value".
    public string NextReservationId() =>
        $"R{db.Database.SqlQuery<long>($"SELECT nextval('reservation_ids') AS \"Value\"").Single()}";

    public void Save(Reservation reservation)
    {
        var row = new ReservationRow
        {
            Id = reservation.Id, GuestName = reservation.Guest.Name, GuestEmail = reservation.Guest.Email,
            Room = reservation.Room.Number, CheckIn = reservation.Stay.CheckIn, CheckOut = reservation.Stay.CheckOut,
            Status = reservation.Status.ToString(),
        };
        var entry = db.Reservations.Add(row);
        try
        {
            db.SaveChanges(); // INSERT now: the constraint answers here, not at the end of the request
        }
        catch (DbUpdateException error) when (IsOverlapRefusal(error))
        {
            // EF Core still has this row tracked as Added, so the SaveChanges at the end of the
            // request would try the same INSERT again. Detaching it is this language's version of
            // rolling back to a savepoint: the refused statement is forgotten, the request is not.
            entry.State = EntityState.Detached;
            throw new OverlapRejectedException($"{reservation.Room.Number} already has a confirmed reservation for these nights.");
        }
    }

    // A duplicate id, or anything else, is a real fault and must not be read as a double booking.
    private static bool IsOverlapRefusal(DbUpdateException error) =>
        error.InnerException is PostgresException { SqlState: ExclusionViolation } postgres
        && postgres.ConstraintName == NoOverlapConstraint;

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

import threading
from datetime import date

from sqlalchemy import exists, func, select, text
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import DeclarativeBase, Mapped, Session, mapped_column

from hotel_booking.reservations.booking import OverlapRejected
from hotel_booking.reservations.domain import DateRange, Guest, Reservation
from hotel_booking.rooms import Room

# The name of the exclusion constraint in db/init.sql. The store recognises this one violation
# and turns it into OverlapRejected; any other IntegrityError is a real fault and is re-raised.
NO_OVERLAP_CONSTRAINT = "reservations_no_overlap"


class InMemoryReservationStore:
    """Keeps reservations in a list. PostgresReservationStore below has the same methods."""

    def __init__(self) -> None:
        self._rows: list[Reservation] = []
        # save must check and append without another thread fitting in between: this list is the
        # whole database here, so this lock is this store's version of the exclusion constraint.
        self._lock = threading.Lock()

    def count(self) -> int:
        return len(self._rows)

    def has_overlap(self, room: Room, stay: DateRange) -> bool:
        return any(row.blocks(room, stay) for row in self._rows)

    def next_reservation_id(self) -> str:
        with self._lock:
            return f"R{101 + len(self._rows)}"

    def save(self, reservation: Reservation) -> None:
        with self._lock:
            if self.has_overlap(reservation.room, reservation.stay):
                raise OverlapRejected(
                    f"{reservation.room.number} already has a confirmed reservation for these nights.")
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

    def next_reservation_id(self) -> str:
        # nextval never hands the same number to two callers, and it does not wait for, or roll
        # back with, the transaction that asked for it. That is exactly what an id needs.
        return f"R{self._session.scalar(text("SELECT nextval('reservation_ids')"))}"

    def save(self, reservation: Reservation) -> None:
        row = ReservationRow(id=reservation.id, guest_name=reservation.guest.name, guest_email=reservation.guest.email,
                             room=reservation.room.number, check_in=reservation.stay.check_in,
                             check_out=reservation.stay.check_out, status=reservation.status.value)
        try:
            # A SAVEPOINT, so a refused INSERT rolls back only this statement. Without it the whole
            # request's transaction would be poisoned and save_changes could not commit.
            with self._session.begin_nested():
                self._session.add(row)
                self._session.flush()  # INSERT now: the constraint answers here, not at commit
        except IntegrityError as error:
            if NO_OVERLAP_CONSTRAINT in str(error.orig):
                raise OverlapRejected(
                    f"{reservation.room.number} already has a confirmed reservation for these nights.") from error
            raise  # a duplicate id, or anything else: a real fault, not a double booking

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
import hotelbooking.reservations.OverlapRejectedException;
import hotelbooking.reservations.ReservationStore;
import hotelbooking.rooms.Room;
import jakarta.persistence.Entity;
import jakarta.persistence.PersistenceException;
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

    // The name of the exclusion constraint in db/init.sql, and PostgreSQL's SQLSTATE for "a row
    // conflicted with an exclusion constraint". The store recognises exactly this one violation.
    private static final String NO_OVERLAP_CONSTRAINT = "reservations_no_overlap";
    private static final String EXCLUSION_VIOLATION = "23P01";

    // Set when the constraint refused a save. Main.java reads it and rolls the request back.
    private boolean refused;

    // True when this request's transaction has been aborted by the booking rule, and so must be
    // rolled back instead of committed.
    public boolean wasRefused() {
        return refused;
    }

    // nextval never hands the same number to two callers, and it does not wait for, or roll back
    // with, the transaction that asked for it. That is exactly what an id needs, and it is what
    // stops two requests in this language both being given R101.
    @Override
    public String nextReservationId() {
        Number next = (Number) db.createNativeQuery("SELECT nextval('reservation_ids')").getSingleResult();
        return "R" + next.longValue();
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
        try {
            db.persist(row);
            db.flush(); // INSERT now: the constraint answers here, not at the end of the request
        } catch (PersistenceException failure) {
            if (!isOverlapRefusal(failure)) {
                throw failure; // a duplicate id, or anything else: a real fault, not a double booking
            }
            // Two things have to happen here, and both are specific to running inside one
            // transaction. First, take the refused row back out of the persistence context, so
            // Hibernate does not try to write it again when it next flushes. Second, remember that
            // the booking was refused: PostgreSQL has marked this transaction as aborted and will
            // refuse every further statement in it with SQLSTATE 25P02, so the unit of work must
            // roll back rather than commit. A refused booking saves nothing, so that is exactly
            // the right ending.
            db.detach(row);
            refused = true;
            throw new OverlapRejectedException(
                    reservation.room().number() + " already has a confirmed reservation for these nights.");
        }
    }

    // Hibernate wraps the driver's error, so the cause chain is what has to be read.
    private static boolean isOverlapRefusal(PersistenceException failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof java.sql.SQLException sql
                    && EXCLUSION_VIOLATION.equals(sql.getSQLState())
                    && String.valueOf(sql.getMessage()).contains(NO_OVERLAP_CONSTRAINT)) {
                return true;
            }
        }
        return false;
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
	"fmt"
	"slices"
	"sync"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgconn"
	"github.com/jackc/pgx/v5/pgxpool"

	"hotelbooking/rooms"
)

// The name of the exclusion constraint in db/init.sql, and PostgreSQL's SQLSTATE for "a row
// conflicted with an exclusion constraint". The store recognises exactly this one violation.
const (
	noOverlapConstraint = "reservations_no_overlap"
	exclusionViolation  = "23P01"
)

// InMemoryReservationStore keeps reservations in a list.
// PostgresReservationStore below has the same methods.
type InMemoryReservationStore struct {
	// net/http serves every request on its own goroutine, and this list is the whole database
	// here. Checking and appending while holding the mutex is this store's version of the
	// exclusion constraint.
	mutex sync.Mutex
	rows  []*Reservation
}

func (store *InMemoryReservationStore) Count() int {
	store.mutex.Lock()
	defer store.mutex.Unlock()
	return len(store.rows)
}

func (store *InMemoryReservationStore) HasOverlap(room rooms.Room, stay DateRange) bool {
	store.mutex.Lock()
	defer store.mutex.Unlock()
	return store.hasOverlap(room, stay)
}

// hasOverlap expects the mutex to be held already.
func (store *InMemoryReservationStore) hasOverlap(room rooms.Room, stay DateRange) bool {
	for _, row := range store.rows {
		if row.Blocks(room, stay) {
			return true
		}
	}
	return false
}

// NextReservationID is safe here for the same reason Save is: it holds the same mutex.
func (store *InMemoryReservationStore) NextReservationID() string {
	store.mutex.Lock()
	defer store.mutex.Unlock()
	return fmt.Sprintf("R%d", 101+len(store.rows))
}

func (store *InMemoryReservationStore) Save(reservation *Reservation) error {
	store.mutex.Lock()
	defer store.mutex.Unlock()
	if store.hasOverlap(reservation.Room, reservation.Stay) {
		return fmt.Errorf("%w: %s", ErrOverlapRejected, reservation.Room.Number)
	}
	store.rows = append(store.rows, reservation)
	return nil
}

// Returns nil when no reservation has that id.
func (store *InMemoryReservationStore) Find(id string) *Reservation {
	store.mutex.Lock()
	defer store.mutex.Unlock()
	for _, row := range store.rows {
		if row.ID == id {
			return row
		}
	}
	return nil
}

func (store *InMemoryReservationStore) CountConfirmed(room rooms.Room, stay DateRange) int {
	store.mutex.Lock()
	defer store.mutex.Unlock()
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

// NextReservationID takes the id from the sequence. nextval never hands the same number to two
// callers, and it does not wait for, or roll back with, the transaction that asked for it.
func (store *PostgresReservationStore) NextReservationID() string {
	var next int64
	if err := store.pool.QueryRow(store.ctx, "SELECT nextval('reservation_ids')").Scan(&next); err != nil {
		store.err = cmp.Or(store.err, err)
		return ""
	}
	return fmt.Sprintf("R%d", next)
}

func (store *PostgresReservationStore) Save(reservation *Reservation) error {
	if store.err != nil {
		return nil // an earlier query failed: write nothing, SaveChanges reports the error
	}
	_, err := store.pool.Exec(store.ctx, "INSERT INTO reservations VALUES ($1, $2, $3, $4, $5, $6, $7)",
		reservation.ID, reservation.Guest.Name, reservation.Guest.Email, reservation.Room.Number,
		reservation.Stay.CheckIn, reservation.Stay.CheckOut, string(reservation.Status))
	// The INSERT happens here, so the constraint answers here too.
	if isOverlapRefusal(err) {
		// Each statement stands on its own in pgx, so nothing has to be rolled back: the refused
		// INSERT changed nothing and the pool is still usable for the rest of the request.
		return fmt.Errorf("%w: %s", ErrOverlapRejected, reservation.Room.Number)
	}
	store.err = cmp.Or(store.err, err) // a duplicate id, or anything else: a real fault
	return nil
}

// isOverlapRefusal is true only for the exclusion constraint of db/init.sql, so a duplicate id or
// a broken connection is never read as a double booking.
func isOverlapRefusal(err error) bool {
	var postgres *pgconn.PgError
	return errors.As(err, &postgres) &&
		postgres.Code == exclusionViolation &&
		postgres.ConstraintName == noOverlapConstraint
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
import { OverlapRejected } from '../booking.js';

// A date as PostgreSQL's date type writes it: 2026-11-12.
const day = date => date.toISOString().slice(0, 10);

// Confirmed reservations of a room that share at least one night with a stay.
const confirmedOverlaps = "FROM reservations WHERE room = $1 AND status = 'Confirmed' AND check_in < $3 AND $2 < check_out";

// The name of the exclusion constraint in db/init.sql. node-postgres puts it in error.constraint,
// so the store recognises this one violation and leaves every other error alone.
const NO_OVERLAP_CONSTRAINT = 'reservations_no_overlap';

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

  // nextval never hands the same number to two callers, and it does not wait for, or roll back
  // with, the transaction that asked for it. That is exactly what an id needs.
  async nextReservationId() {
    const { rows } = await this.#pool.query("SELECT nextval('reservation_ids') AS id");
    return `R${rows[0].id}`;
  }

  // INSERT now: a second R101 is refused here, and so is a stay that overlaps one already saved.
  async save(reservation) {
    try {
      await this.#pool.query('INSERT INTO reservations VALUES ($1, $2, $3, $4, $5, $6, $7)', [reservation.id, reservation.guest.name,
        reservation.guest.email, reservation.room.number, day(reservation.stay.checkIn), day(reservation.stay.checkOut), reservation.status]);
    } catch (error) {
      if (error.constraint === NO_OVERLAP_CONSTRAINT) {
        throw new OverlapRejected(`${reservation.room.number} already has a confirmed reservation for these nights.`);
      }
      throw error; // a duplicate id, or anything else: a real fault, not a double booking
    }
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

// The name of the exclusion constraint in db/init.sql, and PostgreSQL's SQLSTATE for "a row
// conflicted with an exclusion constraint". The store recognises exactly this one violation and
// leaves every other error alone.
const noOverlapConstraint = 'reservations_no_overlap';
const exclusionViolation = '23P01';

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

  // nextval never hands the same number to two callers, and it does not wait for, or roll back
  // with, the transaction that asked for it. That is exactly what an id needs.
  @override
  Future<String> nextReservationId() async {
    final rows = await _pool.execute("SELECT nextval('reservation_ids')");
    return 'R${rows.first.first}';
  }

  // INSERT now: the constraint answers here, not at the end of the request.
  @override
  Future<void> save(Reservation reservation) async {
    try {
      await _pool.execute(
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
    } on ServerException catch (error) {
      if (error.code == exclusionViolation && error.constraintName == noOverlapConstraint) {
        throw OverlapRejected(
            '${reservation.room.number} already has a confirmed reservation for these nights.');
      }
      rethrow; // a duplicate id, or anything else: a real fault, not a double booking
    }
  }

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

### Completed example 7.6 · and the service answers Unavailable

### BookingService.reserve

#### C#

```csharp
public BookingResult Reserve(Guest guest, Room room, DateRange stay)
{
    if (!stay.IsValid()) return new("InvalidDates");
    if (store.HasOverlap(room, stay)) return new("Unavailable");

    Thread.Sleep(slowStepMs); // 0 normally; the race tool makes this step slow

    var reservation = new Reservation(store.NextReservationId(), guest, room, stay);
    try
    {
        store.Save(reservation);
    }
    catch (OverlapRejectedException)
    {
        // Another request saved an overlapping reservation after our check and before our save.
        // The check was honest when it ran; it had simply stopped being true. The guest gets
        // the same answer either way.
        return new("Unavailable");
    }
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
```

#### Python

```python
def reserve(self, guest: Guest, room: Room, stay: DateRange) -> BookingResult:
    if not stay.is_valid():
        return BookingResult("InvalidDates")
    if self.store.has_overlap(room, stay):
        return BookingResult("Unavailable")

    time.sleep(self.slow_step_seconds)  # 0 normally; the race tool makes this step slow

    reservation = Reservation(self.store.next_reservation_id(), guest, room, stay)
    try:
        self.store.save(reservation)
    except OverlapRejected:
        # Another request saved an overlapping reservation after our check and before our save.
        # The check was honest when it ran; it had simply stopped being true. The guest gets the
        # same answer either way.
        return BookingResult("Unavailable")
    try:
        # Notifications gets its own message: the five fields it needs, not the Reservation.
        self.notifications.send_confirmation(
            Confirmation(reservation.id, guest.email, room.number, stay.check_in, stay.check_out))
    except Exception:
        # R6: the booking is already saved, so a failed message must not undo or hide it.
        return BookingResult("Accepted", reservation.id, notification_failed=True)
    return BookingResult("Accepted", reservation.id)
```

#### Java

```java
public BookingResult reserve(Guest guest, Room room, DateRange stay) {
    if (!stay.isValid()) return new BookingResult("InvalidDates");
    if (store.hasOverlap(room, stay)) return new BookingResult("Unavailable");

    pause(); // does nothing normally; the race demo makes this step slow

    Reservation reservation = new Reservation(store.nextReservationId(), guest, room, stay);
    try {
        store.save(reservation);
    } catch (OverlapRejectedException rejected) {
        // Another request saved an overlapping reservation after our check and before our save.
        // The check was honest when it ran; it had simply stopped being true. The guest gets the
        // same answer either way.
        return new BookingResult("Unavailable");
    }
    try {
        // Notifications gets its own message: the five fields it needs, not the Reservation.
        notifications.sendConfirmation(new Confirmation(reservation.id(), guest.email(), room.number(), stay.checkIn(), stay.checkOut()));
    } catch (RuntimeException failure) {
        // R6: the booking is already saved, so a failed message must not undo or hide it.
        return new BookingResult("Accepted", reservation.id(), true);
    }
    return new BookingResult("Accepted", reservation.id());
}
```

#### Go

```go
func (service *BookingService) Reserve(guest Guest, room rooms.Room, stay DateRange) BookingResult {
	if !stay.IsValid() {
		return BookingResult{Outcome: "InvalidDates"}
	}
	if service.store.HasOverlap(room, stay) {
		return BookingResult{Outcome: "Unavailable"}
	}

	time.Sleep(service.slowStep) // zero normally; the race tool makes this step slow

	reservation := NewReservation(service.store.NextReservationID(), guest, room, stay)
	if err := service.store.Save(reservation); errors.Is(err, ErrOverlapRejected) {
		// Another request saved an overlapping reservation after our check and before our save.
		// The check was honest when it ran; it had simply stopped being true. The guest gets the
		// same answer either way.
		return BookingResult{Outcome: "Unavailable"}
	}
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
```

#### JavaScript

```javascript
async reserve(guest, room, stay) {
  if (!stay.isValid()) return new BookingResult('InvalidDates');
  if (await this.store.hasOverlap(room, stay)) return new BookingResult('Unavailable');

  await wait(this.slowStepMs); // zero normally; the race tool makes this step slow

  const reservation = new Reservation(await this.store.nextReservationId(), guest, room, stay);
  try {
    await this.store.save(reservation);
  } catch (error) {
    // Another request saved an overlapping reservation after our check and before our save. The
    // check was honest when it ran; it had simply stopped being true.
    if (error instanceof OverlapRejected) return new BookingResult('Unavailable');
    throw error;
  }
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
```

#### Dart

```dart
Future<BookingResult> reserve(Guest guest, Room room, DateRange stay) async {
  if (!stay.isValid()) return BookingResult('InvalidDates');
  if (await store.hasOverlap(room, stay)) return BookingResult('Unavailable');

  // A pause that lets other requests run while this one waits.
  await Future<void>.delayed(slowStep);

  final reservation = Reservation(await store.nextReservationId(), guest, room, stay);
  try {
    await store.save(reservation);
  } on OverlapRejected {
    // Another request saved an overlapping reservation after our check and before our save. The
    // check was honest when it ran; it had simply stopped being true.
    return BookingResult('Unavailable');
  }
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
```

### Completed example 7.7 · the two tests that keep it true

One sends two bookings at the same moment. The other calls save directly, with the pre-check bypassed, because the pre-check is a convenience and the promise is the rule.

**What counts as evidence that it is fixed** In short: The race tool prints one 201 and one 409, the table holds one confirmed row, and two tests keep it that way. The tool and the table are the evidence for today. The two tests are the evidence for next month, when somebody changes this code and has forgotten the whole afternoon. One test sends two bookings at the same moment and expects one 201 and one 409. The other calls save directly, with the pre-check bypassed, and expects it to refuse — because the pre-check is a convenience and the promise is the rule.

1. Start: the constraint is in place and R7 has no reservations.
2. Action: you run the race tool, then look at the table.
3. Result:
Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.

Maya -> 201 Created   {"id":"R101","status":"Confirmed","notificationFailed":false}
Omar -> 409 Conflict  {"title":"Unavailable","status":409,"detail":"The room is booked for at least one night of this stay."}

Confirmed reservations for R7 on 12-14 Nov: 1
CORRECT: one booking was accepted and one was refused.

  id  | room |  check_in  | check_out  |  status
------+------+------------+------------+-----------
 R101 | R7   | 2026-11-12 | 2026-11-14 | Confirmed
(1 row)

Before the repair the same two commands gave:
  id  | room |  check_in  | check_out  |  status
------+------+------------+------------+-----------
 R101 | R7   | 2026-11-12 | 2026-11-14 | Confirmed
 R102 | R7   | 2026-11-12 | 2026-11-14 | Confirmed
(2 rows)

C#:

```text
[Fact]
public async Task Two_bookings_at_the_same_moment_give_the_room_one_reservation()
{
    // Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
    var (client, store) = await NewClientWithStore(slowStepMs: 300);
    var sameStay = new { guestName = "Omar", guestEmail = "omar@example.com", room = "R7", checkIn = "2026-11-12", checkOut = "2026-11-14" };

    // Started together, awaited together: Omar's check runs while Maya is still in the gap.
    var maya = client.PostAsJsonAsync("/reservations", Maya);
    var omar = client.PostAsJsonAsync("/reservations", sameStay);
    var answers = await Task.WhenAll(maya, omar);

    Assert.Equal([HttpStatusCode.Created, HttpStatusCode.Conflict], answers.Select(answer => answer.StatusCode).OrderBy(code => code));
    Assert.Equal(1, store.CountConfirmed(Rooms[0], new DateRange(new DateOnly(2026, 11, 12), new DateOnly(2026, 11, 14))));
}

[Fact]
public void The_store_refuses_an_overlapping_save_even_without_the_pre_check()
{
    // The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
    // Nothing calls HasOverlap here: Save itself must refuse.
    var store = new InMemoryReservationStore();
    var stay = new DateRange(new DateOnly(2026, 11, 12), new DateOnly(2026, 11, 14));
    store.Save(new Reservation("R101", new Guest("Maya", "maya@example.com"), Rooms[0], stay));

    var overlapping = new Reservation("R102", new Guest("Omar", "omar@example.com"), Rooms[0],
        new DateRange(new DateOnly(2026, 11, 13), new DateOnly(2026, 11, 15)));
    Assert.Throws<OverlapRejectedException>(() => store.Save(overlapping));
    Assert.Equal(1, store.CountConfirmed(Rooms[0], stay));
}
```

Python:

```text
# ---------------------------------------------------------------- P09
def test_two_bookings_at_the_same_moment_give_the_room_one_reservation(self) -> None:
    """Step 1's race, as a test. Both requests pass the pre-check; only one may be saved."""
    app, store = new_app(slow_step_seconds=0.3)  # wide enough that both pass the check first
    client = TestClient(app)
    answers: dict[str, int] = {}

    maya = threading.Thread(target=lambda: answers.update(maya=client.post("/reservations", json=MAYA).status_code))
    omar = threading.Thread(target=lambda: answers.update(omar=client.post("/reservations", json=OMAR).status_code))
    maya.start()
    omar.start()
    maya.join()
    omar.join()

    self.assertEqual([201, 409], sorted(answers.values()))
    stay = DateRange(date(2026, 11, 12), date(2026, 11, 14))
    self.assertEqual(1, store.count_confirmed(R7, stay))

def test_the_store_refuses_an_overlapping_save_even_without_the_pre_check(self) -> None:
    """The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
    Nothing calls has_overlap here: save itself must refuse."""
    store = InMemoryReservationStore()
    stay = DateRange(date(2026, 11, 12), date(2026, 11, 14))
    store.save(Reservation("R101", Guest("Maya", "maya@example.com"), R7, stay))

    overlapping = Reservation("R102", Guest("Omar", "omar@example.com"), R7,
                              DateRange(date(2026, 11, 13), date(2026, 11, 15)))
    with self.assertRaises(OverlapRejected):
        store.save(overlapping)
    self.assertEqual(1, store.count_confirmed(R7, stay))
```

Java:

```text
@Test
// ---------------------------------------------------------------- P09
@Test
void two_bookings_at_the_same_moment_give_the_room_one_reservation() throws Exception {
    // Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
    InMemoryReservationStore rows = new InMemoryReservationStore();
    Stats stats = new Stats();
    BookingService service = new BookingService(ROOMS, new CountingReservationStore(rows, stats),
            new EmailNotificationSender(), 300);

    // Two threads, both inside the gap between the check and the save.
    var maya = CompletableFuture.supplyAsync(
            () -> service.reserve(new Guest("Maya", "maya@example.com"), ROOMS.get(0), NOV_12_14));
    var omar = CompletableFuture.supplyAsync(
            () -> service.reserve(new Guest("Omar", "omar@example.com"), ROOMS.get(0), NOV_12_14));

    var outcomes = Stream.of(maya.join(), omar.join())
            .map(BookingResult::outcome).sorted().toList();
    assertEquals(List.of("Accepted", "Unavailable"), outcomes);
    assertEquals(1, rows.countConfirmed(ROOMS.get(0), NOV_12_14));
}

@Test
@Test
void the_store_refuses_an_overlapping_save_even_without_the_pre_check() {
    // The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
    // Nothing calls hasOverlap here: save itself must refuse.
    InMemoryReservationStore store = new InMemoryReservationStore();
    store.save(new Reservation("R101", new Guest("Maya", "maya@example.com"), ROOMS.get(0), NOV_12_14));

    Reservation overlapping = new Reservation("R102", new Guest("Omar", "omar@example.com"), ROOMS.get(0),
            new DateRange(LocalDate.of(2026, 11, 13), LocalDate.of(2026, 11, 15)));
    assertThrows(OverlapRejectedException.class, () -> store.save(overlapping));
    assertEquals(1, store.countConfirmed(ROOMS.get(0), NOV_12_14));
}
```

Go:

```text
// Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
func TestTwoBookingsAtTheSameMomentGiveTheRoomOneReservation(t *testing.T) {
	store := &InMemoryReservationStore{}
	service := NewBookingService([]rooms.Room{r7, r8}, store, &fakeNotificationSender{}, 300*time.Millisecond)
	stay := nov(12, 14)

	// Two goroutines, both inside the gap between the check and the save.
	outcomes := make([]string, 2)
	var waiting sync.WaitGroup
	waiting.Add(2)
	go func() {
		defer waiting.Done()
		outcomes[0] = service.Reserve(maya, r7, stay).Outcome
	}()
	go func() {
		defer waiting.Done()
		outcomes[1] = service.Reserve(omar, r7, stay).Outcome
	}()
	waiting.Wait()

	slices.Sort(outcomes)
	if want := []string{"Accepted", "Unavailable"}; !slices.Equal(outcomes, want) {
		t.Errorf("outcomes = %v, want %v", outcomes, want)
	}
	if confirmed := store.CountConfirmed(r7, stay); confirmed != 1 {
		t.Errorf("confirmed reservations for R7 = %d, want 1", confirmed)
	}
}

// The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
// Nothing calls HasOverlap here: Save itself must refuse.
func TestTheStoreRefusesAnOverlappingSaveEvenWithoutThePreCheck(t *testing.T) {
	store := &InMemoryReservationStore{}
	stay := nov(12, 14)
	if err := store.Save(NewReservation("R101", maya, r7, stay)); err != nil {
		t.Fatalf("the first save failed: %v", err)
	}

	err := store.Save(NewReservation("R102", omar, r7, nov(13, 15)))
	if !errors.Is(err, ErrOverlapRejected) {
		t.Errorf("err = %v, want it to wrap ErrOverlapRejected", err)
	}
	if confirmed := store.CountConfirmed(r7, stay); confirmed != 1 {
		t.Errorf("confirmed reservations for R7 = %d, want 1", confirmed)
	}
}
```

JavaScript:

```text
// ---------------------------------------------------------------- P09
test('two bookings at the same moment give the room one reservation', async t => {
  // Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
  const send = await startApp(t, 300);
  const sameStay = { ...omar, checkIn: '2026-11-12', checkOut: '2026-11-14' };

  // Started together, awaited together: Omar's check runs while Maya is still in the gap.
  const [first, second] = await Promise.all([
    send('POST', '/reservations', maya),
    send('POST', '/reservations', sameStay),
  ]);

  assert.deepEqual([first.status, second.status].sort(), [201, 409]);
  const stay = new DateRange(new Date('2026-11-12T00:00:00Z'), new Date('2026-11-14T00:00:00Z'));
  assert.equal(send.store.countConfirmed(rooms[0], stay), 1);
});

test('the store refuses an overlapping save even without the pre-check', async () => {
  // The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
  // Nothing calls hasOverlap here: save itself must refuse.
  const store = new InMemoryReservationStore();
  const stay = new DateRange(new Date('2026-11-12T00:00:00Z'), new Date('2026-11-14T00:00:00Z'));
  store.save(new Reservation('R101', new Guest('Maya', 'maya@example.com'), rooms[0], stay));

  const overlapping = new Reservation('R102', new Guest('Omar', 'omar@example.com'), rooms[0],
    new DateRange(new Date('2026-11-13T00:00:00Z'), new Date('2026-11-15T00:00:00Z')));
  assert.throws(() => store.save(overlapping), OverlapRejected);
  assert.equal(store.countConfirmed(rooms[0], stay), 1);
});
```

Dart:

```text
// ---------------------------------------------------------------- P09
test('two bookings at the same moment give the room one reservation', () async {
  // Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
  final app = newAppWithStore(slowStep: const Duration(milliseconds: 300));
  const sameStay = {
    'guestName': 'Omar',
    'guestEmail': 'omar@example.com',
    'room': 'R7',
    'checkIn': '2026-11-12',
    'checkOut': '2026-11-14'
  };

  // Started together, awaited together: Omar's check runs while Maya is still in the gap.
  final answers = await Future.wait([
    send(app.handler, 'POST', '/reservations', maya),
    send(app.handler, 'POST', '/reservations', sameStay),
  ]);

  expect(answers.map((answer) => answer.statusCode).toList()..sort(), [201, 409]);
  expect(
      app.store.countConfirmed(
          rooms[0], DateRange(DateTime.utc(2026, 11, 12), DateTime.utc(2026, 11, 14))),
      1);
});

test('the store refuses an overlapping save even without the pre-check', () {
  // The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
  // Nothing calls hasOverlap here: save itself must refuse.
  final store = InMemoryReservationStore();
  final stay = DateRange(DateTime.utc(2026, 11, 12), DateTime.utc(2026, 11, 14));
  store.save(Reservation('R101', Guest('Maya', 'maya@example.com'), rooms[0], stay));

  final overlapping = Reservation('R102', Guest('Omar', 'omar@example.com'), rooms[0],
      DateRange(DateTime.utc(2026, 11, 13), DateTime.utc(2026, 11, 15)));
  expect(() => store.save(overlapping), throwsA(isA<OverlapRejected>()));
  expect(store.countConfirmed(rooms[0], stay), 1);
});
```

### The two new tests

#### C#

```csharp
[Fact]
public async Task Two_bookings_at_the_same_moment_give_the_room_one_reservation()
{
    // Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
    var (client, store) = await NewClientWithStore(slowStepMs: 300);
    var sameStay = new { guestName = "Omar", guestEmail = "omar@example.com", room = "R7", checkIn = "2026-11-12", checkOut = "2026-11-14" };

    // Started together, awaited together: Omar's check runs while Maya is still in the gap.
    var maya = client.PostAsJsonAsync("/reservations", Maya);
    var omar = client.PostAsJsonAsync("/reservations", sameStay);
    var answers = await Task.WhenAll(maya, omar);

    Assert.Equal([HttpStatusCode.Created, HttpStatusCode.Conflict], answers.Select(answer => answer.StatusCode).OrderBy(code => code));
    Assert.Equal(1, store.CountConfirmed(Rooms[0], new DateRange(new DateOnly(2026, 11, 12), new DateOnly(2026, 11, 14))));
}

[Fact]
public void The_store_refuses_an_overlapping_save_even_without_the_pre_check()
{
    // The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
    // Nothing calls HasOverlap here: Save itself must refuse.
    var store = new InMemoryReservationStore();
    var stay = new DateRange(new DateOnly(2026, 11, 12), new DateOnly(2026, 11, 14));
    store.Save(new Reservation("R101", new Guest("Maya", "maya@example.com"), Rooms[0], stay));

    var overlapping = new Reservation("R102", new Guest("Omar", "omar@example.com"), Rooms[0],
        new DateRange(new DateOnly(2026, 11, 13), new DateOnly(2026, 11, 15)));
    Assert.Throws<OverlapRejectedException>(() => store.Save(overlapping));
    Assert.Equal(1, store.CountConfirmed(Rooms[0], stay));
}
```

#### Python

```python
# ---------------------------------------------------------------- P09
def test_two_bookings_at_the_same_moment_give_the_room_one_reservation(self) -> None:
    """Step 1's race, as a test. Both requests pass the pre-check; only one may be saved."""
    app, store = new_app(slow_step_seconds=0.3)  # wide enough that both pass the check first
    client = TestClient(app)
    answers: dict[str, int] = {}

    maya = threading.Thread(target=lambda: answers.update(maya=client.post("/reservations", json=MAYA).status_code))
    omar = threading.Thread(target=lambda: answers.update(omar=client.post("/reservations", json=OMAR).status_code))
    maya.start()
    omar.start()
    maya.join()
    omar.join()

    self.assertEqual([201, 409], sorted(answers.values()))
    stay = DateRange(date(2026, 11, 12), date(2026, 11, 14))
    self.assertEqual(1, store.count_confirmed(R7, stay))

def test_the_store_refuses_an_overlapping_save_even_without_the_pre_check(self) -> None:
    """The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
    Nothing calls has_overlap here: save itself must refuse."""
    store = InMemoryReservationStore()
    stay = DateRange(date(2026, 11, 12), date(2026, 11, 14))
    store.save(Reservation("R101", Guest("Maya", "maya@example.com"), R7, stay))

    overlapping = Reservation("R102", Guest("Omar", "omar@example.com"), R7,
                              DateRange(date(2026, 11, 13), date(2026, 11, 15)))
    with self.assertRaises(OverlapRejected):
        store.save(overlapping)
    self.assertEqual(1, store.count_confirmed(R7, stay))
```

#### Java

```java
@Test
// ---------------------------------------------------------------- P09
@Test
void two_bookings_at_the_same_moment_give_the_room_one_reservation() throws Exception {
    // Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
    InMemoryReservationStore rows = new InMemoryReservationStore();
    Stats stats = new Stats();
    BookingService service = new BookingService(ROOMS, new CountingReservationStore(rows, stats),
            new EmailNotificationSender(), 300);

    // Two threads, both inside the gap between the check and the save.
    var maya = CompletableFuture.supplyAsync(
            () -> service.reserve(new Guest("Maya", "maya@example.com"), ROOMS.get(0), NOV_12_14));
    var omar = CompletableFuture.supplyAsync(
            () -> service.reserve(new Guest("Omar", "omar@example.com"), ROOMS.get(0), NOV_12_14));

    var outcomes = Stream.of(maya.join(), omar.join())
            .map(BookingResult::outcome).sorted().toList();
    assertEquals(List.of("Accepted", "Unavailable"), outcomes);
    assertEquals(1, rows.countConfirmed(ROOMS.get(0), NOV_12_14));
}

@Test
@Test
void the_store_refuses_an_overlapping_save_even_without_the_pre_check() {
    // The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
    // Nothing calls hasOverlap here: save itself must refuse.
    InMemoryReservationStore store = new InMemoryReservationStore();
    store.save(new Reservation("R101", new Guest("Maya", "maya@example.com"), ROOMS.get(0), NOV_12_14));

    Reservation overlapping = new Reservation("R102", new Guest("Omar", "omar@example.com"), ROOMS.get(0),
            new DateRange(LocalDate.of(2026, 11, 13), LocalDate.of(2026, 11, 15)));
    assertThrows(OverlapRejectedException.class, () -> store.save(overlapping));
    assertEquals(1, store.countConfirmed(ROOMS.get(0), NOV_12_14));
}
```

#### Go

```go
// Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
func TestTwoBookingsAtTheSameMomentGiveTheRoomOneReservation(t *testing.T) {
	store := &InMemoryReservationStore{}
	service := NewBookingService([]rooms.Room{r7, r8}, store, &fakeNotificationSender{}, 300*time.Millisecond)
	stay := nov(12, 14)

	// Two goroutines, both inside the gap between the check and the save.
	outcomes := make([]string, 2)
	var waiting sync.WaitGroup
	waiting.Add(2)
	go func() {
		defer waiting.Done()
		outcomes[0] = service.Reserve(maya, r7, stay).Outcome
	}()
	go func() {
		defer waiting.Done()
		outcomes[1] = service.Reserve(omar, r7, stay).Outcome
	}()
	waiting.Wait()

	slices.Sort(outcomes)
	if want := []string{"Accepted", "Unavailable"}; !slices.Equal(outcomes, want) {
		t.Errorf("outcomes = %v, want %v", outcomes, want)
	}
	if confirmed := store.CountConfirmed(r7, stay); confirmed != 1 {
		t.Errorf("confirmed reservations for R7 = %d, want 1", confirmed)
	}
}

// The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
// Nothing calls HasOverlap here: Save itself must refuse.
func TestTheStoreRefusesAnOverlappingSaveEvenWithoutThePreCheck(t *testing.T) {
	store := &InMemoryReservationStore{}
	stay := nov(12, 14)
	if err := store.Save(NewReservation("R101", maya, r7, stay)); err != nil {
		t.Fatalf("the first save failed: %v", err)
	}

	err := store.Save(NewReservation("R102", omar, r7, nov(13, 15)))
	if !errors.Is(err, ErrOverlapRejected) {
		t.Errorf("err = %v, want it to wrap ErrOverlapRejected", err)
	}
	if confirmed := store.CountConfirmed(r7, stay); confirmed != 1 {
		t.Errorf("confirmed reservations for R7 = %d, want 1", confirmed)
	}
}
```

#### JavaScript

```javascript
// ---------------------------------------------------------------- P09
test('two bookings at the same moment give the room one reservation', async t => {
  // Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
  const send = await startApp(t, 300);
  const sameStay = { ...omar, checkIn: '2026-11-12', checkOut: '2026-11-14' };

  // Started together, awaited together: Omar's check runs while Maya is still in the gap.
  const [first, second] = await Promise.all([
    send('POST', '/reservations', maya),
    send('POST', '/reservations', sameStay),
  ]);

  assert.deepEqual([first.status, second.status].sort(), [201, 409]);
  const stay = new DateRange(new Date('2026-11-12T00:00:00Z'), new Date('2026-11-14T00:00:00Z'));
  assert.equal(send.store.countConfirmed(rooms[0], stay), 1);
});

test('the store refuses an overlapping save even without the pre-check', async () => {
  // The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
  // Nothing calls hasOverlap here: save itself must refuse.
  const store = new InMemoryReservationStore();
  const stay = new DateRange(new Date('2026-11-12T00:00:00Z'), new Date('2026-11-14T00:00:00Z'));
  store.save(new Reservation('R101', new Guest('Maya', 'maya@example.com'), rooms[0], stay));

  const overlapping = new Reservation('R102', new Guest('Omar', 'omar@example.com'), rooms[0],
    new DateRange(new Date('2026-11-13T00:00:00Z'), new Date('2026-11-15T00:00:00Z')));
  assert.throws(() => store.save(overlapping), OverlapRejected);
  assert.equal(store.countConfirmed(rooms[0], stay), 1);
});
```

#### Dart

```dart
// ---------------------------------------------------------------- P09
test('two bookings at the same moment give the room one reservation', () async {
  // Step 1's race, as a test. Both requests pass the pre-check; only one may be saved.
  final app = newAppWithStore(slowStep: const Duration(milliseconds: 300));
  const sameStay = {
    'guestName': 'Omar',
    'guestEmail': 'omar@example.com',
    'room': 'R7',
    'checkIn': '2026-11-12',
    'checkOut': '2026-11-14'
  };

  // Started together, awaited together: Omar's check runs while Maya is still in the gap.
  final answers = await Future.wait([
    send(app.handler, 'POST', '/reservations', maya),
    send(app.handler, 'POST', '/reservations', sameStay),
  ]);

  expect(answers.map((answer) => answer.statusCode).toList()..sort(), [201, 409]);
  expect(
      app.store.countConfirmed(
          rooms[0], DateRange(DateTime.utc(2026, 11, 12), DateTime.utc(2026, 11, 14))),
      1);
});

test('the store refuses an overlapping save even without the pre-check', () {
  // The promise the exclusion constraint keeps in PostgreSQL, kept by the list store too.
  // Nothing calls hasOverlap here: save itself must refuse.
  final store = InMemoryReservationStore();
  final stay = DateRange(DateTime.utc(2026, 11, 12), DateTime.utc(2026, 11, 14));
  store.save(Reservation('R101', Guest('Maya', 'maya@example.com'), rooms[0], stay));

  final overlapping = Reservation('R102', Guest('Omar', 'omar@example.com'), rooms[0],
      DateRange(DateTime.utc(2026, 11, 13), DateTime.utc(2026, 11, 15)));
  expect(() => store.save(overlapping), throwsA(isA<OverlapRejected>()));
  expect(store.countConfirmed(rooms[0], stay), 1);
});
```

### Prove no code path can get round it

This INSERT does not go through the app at all. It is typed straight into psql, which is exactly what a migration script or a tired colleague at midnight looks like.

### In psql

```text
INSERT INTO reservations (id, guest_name, guest_email, room, check_in, check_out, status)
VALUES ('R999', 'Direct Insert', 'direct@example.com', 'R7', '2026-11-13', '2026-11-15', 'Confirmed');
```

### 

### psql

```text
ERROR:  conflicting key value violates exclusion constraint "reservations_no_overlap"
DETAIL:  Key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-13,2026-11-15)) conflicts with existing key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-12,2026-11-14)).
```

### And the race, repaired

The same tool, the same two requests, against two instances.

### Terminal 3

```text
Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.
Maya -> http://localhost:5080   Omar -> http://localhost:5081

Maya -> 201 Created   {"id":"R101","status":"Confirmed","notificationFailed":false}
Omar -> 409 Conflict  {"title":"Unavailable","status":409,"detail":"The room is booked for at least one night of this stay."}

Confirmed reservations for R7 on 12-14 Nov: 1
CORRECT: one booking was accepted and one was refused.
```

### Your turn · put the rule in the database

1. Try the ALTER TABLE (7.3) first and read the error. Then cancel the duplicate (7.2) and run it again.
2. Add btree_gist and the constraint to db/init.sql as well (7.4), so a fresh database comes up with the rule.
3. Drop the guard table: DROP TABLE room_booking_guard;
4. Change the PostgreSQL store to map the violation (7.5), and BookingService to answer Unavailable (7.6).
5. Add the two tests (7.7). Run your checks: 19 green (Python: 16 tests, then “Contracts: 3 kept”).
6. Try the direct INSERT in psql and read the refusal.
7. Run the race tool again, against two instances. One 201, one 409, and one confirmed reservation.
8. Add the exclusion-constraint row to your four-repairs table, including that it would not switch on over dirty data.

**When you finish, you have:** 19 green checks, a constraint no code path can bypass, the race repaired across two instances, and your four-repairs table complete.
### Explanation

The pre-check was kept on purpose. It answers the ordinary case with a clean message; the constraint is what makes the rule true. Together they give a guest a readable 409 and the hotel a rule nobody can forget.



---

## 9. Step 8 · Write ADR-003

Four repairs were tried and two were measured. Write down which one you kept, what it costs, and when somebody should look at the decision again.





### What goes in it

| Heading | What to write |
| --- | --- |
| Context | The rule, the race, and the anomaly. One paragraph, pointing at docs/interleaving.md. |
| Decision | The constraint, with the pre-check kept in front of it, and the SQL. |
| Alternatives considered | All three rejected repairs, each with the reason — including the two measurements. |
| Consequences | What is now true, what now costs more, and what the constraint does not cover. |
| When to revisit | The conditions that would make this decision wrong: a rule spanning two tables, or writes frequent enough for the index to show in a measurement. |

**Why the constraint would not switch on** In short: A constraint cannot be added to a table whose rows already break it, and Step 1 deliberately left a double booking in the table. This is not a quirk of the exercise. It is what turning on a constraint over live data is always like: the rule has been broken for as long as it went unenforced, and the broken rows are still there. Finding them and deciding what to do about them is part of the work. Cancelling is the honest action here — a real hotel would have to telephone one of the two guests — and a Cancelled row does not block the constraint.

1. Start: R7 has the two confirmed reservations Step 1 created.
2. Action: you run the ALTER TABLE.
3. Result:
ERROR:  could not create exclusion constraint "reservations_no_overlap"
DETAIL:  Key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-12,2026-11-14)) conflicts with key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-12,2026-11-14)).

After UPDATE reservations SET status = 'Cancelled' WHERE id = 'R102'; the same ALTER TABLE prints ALTER TABLE.

### Completed example: docs/adr-003-booking-concurrency.md

### docs/adr-003-booking-concurrency.md

```text
# ADR-003: Protect the booking rule in the database

- Status: accepted
- Date: 2026-11-09
- Supersedes: nothing. Extends ADR-001 (the modular monolith) and ADR-002 (the cache).

## Context

The booking rule is: a room never has two Confirmed reservations covering the
same night. Until now that rule lived only in `BookingService.Reserve`, which
checks for an overlap and then saves. With one request at a time it holds. With
two requests at the same moment it does not: both read "no clash", both insert,
both commit, and the room is double-booked. `tools/race-http` reproduces it
every run, and `docs/interleaving.md` has the statement-by-statement trace.

The anomaly is write skew, so Repeatable Read does not stop it, and
`SELECT ... FOR UPDATE` on the reservations themselves does not stop it either:
at check time there is no overlapping row to lock.

## Decision

Enforce the rule in PostgreSQL with an exclusion constraint:

```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE reservations
    ADD CONSTRAINT reservations_no_overlap
    EXCLUDE USING gist (
        room WITH =,
        daterange(check_in, check_out, '[)') WITH &&
    ) WHERE (status = 'Confirmed');
```

Keep the existing pre-check in `BookingService.Reserve`. The pre-check answers
the ordinary case with a readable `409 Unavailable`; the constraint is what
makes the rule true. When the constraint fires, the Reservations module turns
the violation into the same `409`, so a guest never sees a database error.

## Alternatives considered

**An in-process lock.** A mutex around check-then-save. Correct with one
instance and worthless with two: a second process has its own mutex and neither
waits for the other. We ran exactly that experiment in Step 4 and watched the
double booking return. Rejected: capacity planning in `docs/capacity.md`
already assumes we can add instances.

**`SELECT ... FOR UPDATE` on a guard row.** One row per room in a
`room_booking_guard` table, locked at the start of every booking. Correct, and
it is the repair to reach for when the rule is too complicated for a constraint.
Rejected here because it serialises every booking for a room, holds the lock
until commit, and needs a guard row to exist for every room forever - including
rooms added later.

**Optimistic version checking.** The same guard row with a `version` column,
updated with the old version in the `WHERE`; no match means someone else booked
first, so retry. Correct, and better than a lock when clashes are rare.
Rejected here because it costs more code in every write path and still leaves
the rule enforceable only by code that remembers to do it.

**Serializable isolation.** Correct, and it needs no schema change at all.
Rejected for now because every transaction in the app would have to handle
serialization failures and retry, which is a larger change than this one rule
needs. Worth revisiting if we find a second write-skew rule.

## Consequences

- The rule is now true no matter which code path writes a reservation, including
  a `psql` session and any future endpoint or background job.
- A booking can now fail with a constraint violation that the app must catch and
  map to `409`. That mapping is covered by a test.
- The constraint only covers a rule that lives inside the `reservations` table.
  A rule that spans tables needs one of the rejected alternatives.
- Writing a reservation now also maintains a GiST index, so writes are slightly
  more expensive. At the capacity in `docs/capacity.md` (3.33 searches a second,
  a booking every few seconds) this does not matter.
- `db/init.sql` and the running database must both be changed, because
  `init.sql` runs only when the database is created.
- The constraint cannot be added while any room already has two overlapping
  Confirmed reservations. Turning it on means finding and fixing the rows that
  already break the rule - we had to cancel R102 before the `ALTER TABLE`
  would run. Any future constraint on live data will need the same step.

## When to revisit

- When a second rule needs protecting that does not fit in one table.
- When bookings become frequent enough for the GiST index writes to show up in a
  measurement.
- When the app starts writing reservations from more than one service, at which
  point the constraint stops being an optimisation and becomes the only thing
  holding the rule.

```

### Your turn · record the decision

1. Create docs/adr-003-booking-concurrency.md with the five headings above.
2. Under Alternatives considered, write all three rejected repairs, each with one reason and its measured cost where you have one.
3. Under Consequences, include that the constraint could not be added until the duplicate rows were cleaned up.
4. Under When to revisit, name one condition that would make you change your mind.
5. Compare yours with the completed example. It is fine for yours to be shorter; it is not fine for it to be missing the costs.

**When you finish, you have:** docs/adr-003-booking-concurrency.md with the decision, three rejected alternatives with their costs, and a revisit condition.
### Explanation

“We used a constraint because constraints are good” is the answer an ADR exists to prevent. Name the repairs you rejected and what they cost, or you are recording a preference rather than a decision.



---

## 10. Check your project and see what comes next

Your hotel app now keeps the booking rule however many copies of it are running, and the rule is written where no code path can miss it.





### Completed example: your folder at the end of P09

### hotel-booking folder

#### C#

```text
hotel-booking/   end of P09
├── db/
│   └── init.sql   CHANGED: + btree_gist, the exclusion constraint and the id sequence
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md
│   ├── api-check.md
│   ├── cache-experiment.md
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml
│   ├── quality-scenarios.md
│   └── requirements.md
├── HotelBooking/
│   ├── Api/
│   │   ├── AvailabilityCache.cs
│   │   ├── HotelApi.cs
│   │   └── Stats.cs   CHANGED: the counting store passes NextReservationId on
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
│   │   │   ├── InMemoryReservationStore.cs   CHANGED: checks under a lock
│   │   │   └── PostgresReservationStore.cs   CHANGED: the constraint violation becomes OverlapRejected; ids from the sequence
│   │   ├── BookingResult.cs
│   │   ├── BookingService.cs   CHANGED: a refused save becomes Unavailable
│   │   ├── DateRange.cs
│   │   ├── Guest.cs
│   │   ├── IReservationStore.cs   CHANGED: + NextReservationId, and Save now promises to refuse an overlap
│   │   ├── OverlapRejectedException.cs   NEW
│   │   ├── Reservation.cs
│   │   └── ReservationStatus.cs
│   ├── Rooms/
│   │   ├── Internal/
│   │   │   ├── InMemoryRoomCatalog.cs
│   │   │   └── PostgresRoomCatalog.cs
│   │   ├── IRoomCatalog.cs
│   │   └── Room.cs
│   ├── HotelBooking.csproj
│   └── Program.cs   CHANGED: HOTEL_SLOW_STEP and HOTEL_PORT
├── HotelBooking.Tests/
│   ├── ApiTests.cs   CHANGED: + 2 concurrency tests
│   ├── ArchitectureTests.cs
│   ├── BookingServiceTests.cs
│   └── HotelBooking.Tests.csproj
├── tools/
│   ├── measure.cs
│   └── race-http.cs   NEW
└── docker-compose.yml
```

#### Python

```text
hotel-booking/   end of P09
├── db/
│   └── init.sql   CHANGED: + btree_gist, the exclusion constraint and the id sequence
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md
│   ├── api-check.md
│   ├── cache-experiment.md
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml
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
│   │   ├── __init__.py   CHANGED: + OverlapRejected
│   │   ├── booking.py   CHANGED: + OverlapRejected and NextReservationId; a refused save becomes Unavailable
│   │   ├── domain.py
│   │   └── internal.py   CHANGED: the list store checks under a lock; PostgreSQL maps the constraint and uses the sequence
│   ├── rooms/
│   │   ├── __init__.py
│   │   ├── catalog.py
│   │   └── internal.py
│   ├── __init__.py
│   ├── api_cache.py
│   └── api.py   CHANGED: the counting store passes next_reservation_id on
├── tools/
│   ├── measure.py
│   └── race_http.py   NEW
├── docker-compose.yml
├── main.py   CHANGED: HOTEL_SLOW_STEP and HOTEL_PORT
├── pyproject.toml
├── requirements.txt
├── test_api.py   CHANGED: + 2 concurrency tests
└── test_booking_service.py
```

#### Java

```text
hotel-booking/   end of P09
├── db/
│   └── init.sql   CHANGED: + btree_gist, the exclusion constraint and the id sequence
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md
│   ├── api-check.md
│   ├── cache-experiment.md
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml
│   ├── quality-scenarios.md
│   └── requirements.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── hotelbooking/
│   │   │       ├── api/
│   │   │       │   ├── AvailabilityCache.java
│   │   │       │   ├── CountingReservationStore.java   CHANGED: passes nextReservationId on
│   │   │       │   ├── HotelApi.java
│   │   │       │   └── Stats.java
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
│   │   │       │   │   ├── InMemoryReservationStore.java   CHANGED: checks inside a synchronized block
│   │   │       │   │   └── PostgresReservationStore.java   CHANGED: the constraint violation becomes OverlapRejectedException; ids from the sequence
│   │   │       │   ├── BookingResult.java
│   │   │       │   ├── BookingService.java   CHANGED: a refused save becomes Unavailable
│   │   │       │   ├── DateRange.java
│   │   │       │   ├── Guest.java
│   │   │       │   ├── OverlapRejectedException.java   NEW
│   │   │       │   ├── Reservation.java
│   │   │       │   ├── ReservationStatus.java
│   │   │       │   └── ReservationStore.java   CHANGED: + nextReservationId, and save now promises to refuse an overlap
│   │   │       ├── rooms/
│   │   │       │   ├── internal/
│   │   │       │   │   ├── InMemoryRoomCatalog.java
│   │   │       │   │   └── PostgresRoomCatalog.java
│   │   │       │   ├── Room.java
│   │   │       │   └── RoomCatalog.java
│   │   │       └── Main.java   CHANGED: HOTEL_SLOW_STEP, and a refused booking rolls the transaction back
│   │   └── resources/
│   │       └── application.properties   CHANGED: server.port follows HOTEL_PORT
│   └── test/
│       └── java/
│           └── hotelbooking/
│               ├── ApiTests.java   CHANGED: + 2 concurrency tests
│               ├── ArchitectureTests.java
│               └── BookingServiceTests.java
├── tools/
│   ├── Measure.java
│   └── RaceHttp.java   NEW
├── docker-compose.yml
└── pom.xml
```

#### Go

```text
hotel-booking/   end of P09
├── api/
│   ├── api_test.go
│   ├── api.go
│   ├── cache.go
│   └── stats.go   CHANGED: the counting store passes NextReservationID on
├── db/
│   └── init.sql   CHANGED: + btree_gist, the exclusion constraint and the id sequence
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md
│   ├── api-check.md
│   ├── cache-experiment.md
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml
│   ├── quality-scenarios.md
│   └── requirements.md
├── notifications/
│   ├── internal.go
│   └── notifications.go
├── payments/
│   ├── internal.go
│   └── payments.go
├── reservations/
│   ├── booking_test.go   CHANGED: + 2 concurrency tests
│   ├── booking.go   CHANGED: + ErrOverlapRejected and NextReservationID; Save now returns an error
│   ├── domain.go
│   └── internal.go   CHANGED: the list store takes a mutex; PostgreSQL maps the constraint and uses the sequence
├── rooms/
│   ├── internal.go
│   └── rooms.go
├── tools/
│   ├── measure/
│   │   └── main.go
│   └── race/
│       └── main.go   NEW
├── architecture_test.go
├── docker-compose.yml
├── go.mod
├── go.sum
└── main.go   CHANGED: HOTEL_SLOW_STEP and HOTEL_PORT
```

#### JavaScript

```text
hotel-booking/   end of P09
├── db/
│   └── init.sql   CHANGED: + btree_gist, the exclusion constraint and the id sequence
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md
│   ├── api-check.md
│   ├── cache-experiment.md
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml
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
│   │   │   ├── postgres-store.js   CHANGED: the constraint violation becomes OverlapRejected; ids from the sequence
│   │   │   └── store.js   CHANGED: save refuses an overlap; + nextReservationId
│   │   ├── booking.js   CHANGED: + OverlapRejected; a refused save becomes Unavailable
│   │   ├── domain.js
│   │   └── index.js   CHANGED: + OverlapRejected
│   ├── rooms/
│   │   ├── internal/
│   │   │   ├── catalog.js
│   │   │   └── postgres-catalog.js
│   │   └── index.js
│   ├── api-cache.js
│   └── api.js   CHANGED: the counting store passes nextReservationId on
├── test/
│   ├── api.test.js   CHANGED: + 2 concurrency tests
│   ├── architecture.test.js
│   └── booking.test.js
├── tools/
│   ├── measure.js
│   └── race-http.js   NEW
├── docker-compose.yml
├── main.js   CHANGED: HOTEL_SLOW_STEP and HOTEL_PORT
├── package-lock.json
└── package.json
```

#### Dart

```text
hotel-booking/   end of P09
├── bin/
│   └── main.dart   CHANGED: HOTEL_SLOW_STEP and HOTEL_PORT
├── db/
│   └── init.sql   CHANGED: + btree_gist, the exclusion constraint and the id sequence
├── docs/
│   ├── requests/
│   │   ├── maya-r7.json
│   │   ├── omar-r7.json
│   │   └── omar-r8.json
│   ├── adr-001-modular-monolith.md
│   ├── adr-002-cache-availability.md
│   ├── api-check.md
│   ├── cache-experiment.md
│   ├── capacity.md
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── l04-notes.md
│   ├── openapi.yaml
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
│   │   │   ├── booking.dart   CHANGED: + OverlapRejected and nextReservationId; a refused save becomes Unavailable
│   │   │   ├── domain.dart
│   │   │   ├── postgres_store.dart   CHANGED: the constraint violation becomes OverlapRejected; ids from the sequence
│   │   │   └── store.dart   CHANGED: save refuses an overlap
│   │   └── reservations.dart
│   ├── rooms/
│   │   ├── src/
│   │   │   ├── catalog.dart
│   │   │   └── postgres_catalog.dart
│   │   └── rooms.dart
│   ├── api_cache.dart
│   └── api.dart   CHANGED: the counting store passes nextReservationId on
├── test/
│   ├── api_test.dart   CHANGED: + 2 concurrency tests
│   ├── architecture_test.dart
│   └── booking_service_test.dart
├── tool/
│   ├── measure.dart
│   └── race_http.dart   NEW
├── analysis_options.yaml
├── docker-compose.yml
└── pubspec.yaml
```

### Check your project

| Check | What you should see |
| --- | --- |
| Your checks | 19 green (Python: 16 tests, then “Contracts: 3 kept, 0 broken”) |
| --race | “Only one guest has R7.” — it used to say “Double booking!” |
| The race tool, one instance | One 201 Created and one 409 Conflict |
| The race tool, two instances | The same: one 201 and one 409 |
| psql: confirmed reservations for R7 | Exactly one |
| A direct INSERT in psql | Refused: conflicting key value violates exclusion constraint "reservations_no_overlap" |
| docs/interleaving.md | Your trace, and all four repairs with what each one cost |
| docs/adr-003-booking-concurrency.md | The decision, three rejected alternatives, consequences, when to revisit |
| db/init.sql | btree_gist, the named constraint, and the id sequence |

### What the four repairs taught

| Repair | Stopped it? | What it cost |
| --- | --- | --- |
| In-process lock | One instance yes, two no | Nothing, and worth nothing once there are two processes |
| SELECT … FOR UPDATE on a guard row | Yes | The loser waited 0.91 s for a room it was never going to get |
| Optimistic version checking | Yes | Nobody waited; the loser did its work twice |
| Exclusion constraint | Yes | One table only, a violation to map, and a clean-up before it would switch on |

### What comes next

P10 takes the confirmation email out of the request. A booking should not wait for a mail server, and a queue that grows without limit is its own kind of outage — so the queue gets a bound, and you measure what it accepts, refuses and completes.

L06 and P11–P12 then go wider: overlapping work across services, and what happens when the remote end fails rather than merely being slow.

P17 comes back to today’s loose end. An in-memory queue loses its jobs when the process stops; the durable answer is to write the job into the database in the same transaction as the booking, which is the transactional outbox.

### Your turn · hand it over

1. Run every check in the table above and tick them off.
2. Commit your work with the message “P09: protect the booking rule in the database”.
3. Read your ADR once more and make sure a stranger could tell why the other three repairs were rejected.
4. Bring this folder to P10.

**When you finish, you have:** A committed project with 19 green checks, a booking rule the database enforces, and two documents explaining why.
### Explanation

The habit to take away is the order: reproduce it, write the interleaving down, compare the repairs with numbers, choose one, and record what it cost. The constraint is today’s answer; the order is the part you will use again.
