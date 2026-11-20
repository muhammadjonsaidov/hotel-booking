# P10 · Activity sheet

## 1. P10 · The guest is waiting for an email

P09 made the booking rule true. It left the booking slow: about three milliseconds of real work inside a request that takes a full second, because the request waits for a mail server before it answers.





### Where we are

Your hotel-booking folder has the P09 project: the app on http://localhost:5080, PostgreSQL 18 and Redis 8 in Docker, 19 green checks, a booking rule the database enforces, docs/interleaving.md and docs/adr-003-booking-concurrency.md.

**Why the booking is slow again** In short: The confirmation email is sent inside the request, so the guest waits a full second for a room that was already theirs. P09 made the booking rule true. It did not make the request quick: about three milliseconds of that request is the booking, and the rest is a mail server. The room is the guest’s the moment the reservation commits. Everything the request does after that point is the app making them wait for something that cannot change their answer.

1. Start: the app is started with HOTEL_EMAIL_SECONDS=1, so the sender behaves like a real mail server.
2. Action: a guest books one night in December.
3. Result:
BookingService.reserve(Maya, R7, 1-2 December)
  Accepted in 1.02 s

Of that second, the booking itself took under a millisecond. The rest was the confirmation email,
sent inside the request while the guest waited for a room that was already theirs.

### Why that is a problem now

| What the hotel needs | What the P09 app does |
| --- | --- |
| A booking that answers at once | The request sends the confirmation email before answering, so the guest waits for a mail server to tell them about a room that is already theirs. |
| A booking that works when the mail server does not | A mail server that is down, rather than slow, makes a booking that genuinely succeeded look to the guest like one that failed. |
| A limit chosen on purpose | Nothing in the app limits background work, because there is no background work yet. The first version of one is where that goes wrong. |

**Why the booking is slow again** In short: The confirmation email is sent inside the request, so the guest waits a full second for a room that was already theirs. P09 made the booking rule true. It did not make the request quick: about three milliseconds of that request is the booking, and the rest is a mail server. The room is the guest’s the moment the reservation commits. Everything the request does after that point is the app making them wait for something that cannot change their answer.

1. Start: the app is started with HOTEL_EMAIL_SECONDS=1, so the sender behaves like a real mail server.
2. Action: a guest books one night in December.
3. Result:
BookingService.reserve(Maya, R7, 1-2 December)
  Accepted in 1.02 s

Of that second, the booking itself took under a millisecond. The rest was the confirmation email,
sent inside the request while the guest waited for a room that was already theirs.

**Why an unbounded queue is not the answer** In short: It answers every request at once and keeps every job, so the limit becomes the machine rather than a number you chose. A burst of thirty leaves twenty-nine jobs in memory. A burst of thirty thousand leaves thirty thousand, and nothing anywhere refuses them. An unbounded queue does not remove the problem of a slow consumer; it hides it, until the process runs out of memory and takes the bookings down with the emails.

1. Start: the app is started with HOTEL_QUEUE_CAPACITY=10000.
2. Action: you run the burst tool.
3. Result:
Sending 30 bookings, one after another...

Accepted: 30
Rejected: 0
Slowest booking: 31 ms
Total: 0.1 s

GET /stats
{"notificationsAccepted":30,"notificationsRejected":0,"notificationsCompleted":0,"notificationsQueued":29}

Every guest was answered at once - and 29 emails are sitting in memory, waiting for a worker
that sends one a second. Nothing refused them, and nothing would have refused 30,000.

### Today’s plan

| Step | What you do | What it adds to the project folder |
| --- | --- | --- |
| 1 · Measure the cost | Make the confirmation sender slow and watch the booking wait for it | A slow sender, and the first row of docs/queue-trace.md |
| 2 · Put it on a queue | Hand the email to a background worker so the request answers at once | The queued sender; the request drops to milliseconds |
| 3 · Burst it | Send thirty bookings and see how much is sitting in memory | docs/queue-trace.md gains the unbounded result |
| 4 · Put a bound on it | Cap the queue and publish four counters on GET /stats | A bounded queue, and the counters |
| 5 · Backpressure | Watch the bound refuse work, countably, while every booking still succeeds | docs/queue-trace.md gains the bounded result |
| 6 · Shutdown | Drain the queue when the app stops, and see what happens when it does not | The drain, and what it does not cover |
| 7 · Choose a model | Compare six concurrency shapes against what this work actually is | The reasoning for ADR-004 |
| 8 · Write ADR-004 | Record the choice, the five rejected shapes and the loss you accept | docs/adr-004-notification-concurrency.md |

### How you work today

Work in pairs on one laptop, in the hotel-booking folder from P09, in the same language as before. Swap who types at every new step. Every code slide has a tab for each of the six languages.

You need Docker Desktop running, your editor, and two terminals in the hotel-booking folder: one for the app, one for the burst tool. No internet is needed today: nothing new is downloaded.

If you missed P09: download the P09 runnable code in your language from the P09 lesson page. The “Runnable code” link on this page has the finished P10 project in all six languages.

### Completed example: your folder at the end of P10

### hotel-booking folder

#### C#

```text
hotel-booking/   end of P10
├── db/
│   └── init.sql
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
│   │   ├── HotelApi.cs   CHANGED: takes the queue and publishes its counters
│   │   └── Stats.cs   CHANGED: GET /stats can also publish the queue’s four counters
│   ├── Notifications/
│   │   ├── Internal/
│   │   │   ├── EmailNotificationSender.cs   CHANGED: sendSeconds makes it as slow as a real mail server
│   │   │   ├── QueuedNotificationSender.cs   NEW
│   │   │   └── SmsNotificationSender.cs
│   │   ├── Confirmation.cs
│   │   ├── INotificationSender.cs
│   │   └── NotificationCounts.cs   NEW
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
│   │   ├── OverlapRejectedException.cs
│   │   ├── Reservation.cs
│   │   └── ReservationStatus.cs
│   ├── Rooms/
│   │   ├── Internal/
│   │   │   ├── InMemoryRoomCatalog.cs
│   │   │   └── PostgresRoomCatalog.cs
│   │   ├── IRoomCatalog.cs
│   │   └── Room.cs
│   ├── HotelBooking.csproj
│   └── Program.cs   CHANGED: wires the queue and drains it on shutdown
├── HotelBooking.Tests/
│   ├── ApiTests.cs   CHANGED: + 3 queue tests
│   ├── ArchitectureTests.cs
│   ├── BookingServiceTests.cs
│   └── HotelBooking.Tests.csproj
├── tools/
│   ├── burst.cs   NEW
│   ├── measure.cs
│   └── race-http.cs
└── docker-compose.yml
```

#### Python

```text
hotel-booking/   end of P10
├── db/
│   └── init.sql
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
│   │   ├── __init__.py   CHANGED: + NotificationCounts
│   │   └── internal.py   CHANGED: a slow sender, and the queued sender
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
│   ├── api_cache.py
│   └── api.py   CHANGED: GET /stats also publishes the queue’s four counters
├── tools/
│   ├── burst.py   NEW
│   ├── measure.py
│   └── race_http.py
├── docker-compose.yml
├── main.py   CHANGED: wires the queue and drains it on shutdown
├── pyproject.toml
├── requirements.txt
├── test_api.py   CHANGED: + 3 queue tests
└── test_booking_service.py
```

#### Java

```text
hotel-booking/   end of P10
├── db/
│   └── init.sql
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
│   │   │       │   ├── CountingReservationStore.java
│   │   │       │   ├── HotelApi.java   CHANGED: takes the queue and publishes its counters
│   │   │       │   └── Stats.java   CHANGED: GET /stats can also publish the queue’s four counters
│   │   │       ├── notifications/
│   │   │       │   ├── internal/
│   │   │       │   │   ├── EmailNotificationSender.java   CHANGED: sendSeconds makes it as slow as a real mail server
│   │   │       │   │   ├── QueuedNotificationSender.java   NEW
│   │   │       │   │   └── SmsNotificationSender.java
│   │   │       │   ├── Confirmation.java
│   │   │       │   ├── CountingNotificationSender.java   NEW
│   │   │       │   ├── NotificationCounts.java   NEW
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
│   │   │       │   ├── OverlapRejectedException.java
│   │   │       │   ├── Reservation.java
│   │   │       │   ├── ReservationStatus.java
│   │   │       │   └── ReservationStore.java
│   │   │       ├── rooms/
│   │   │       │   ├── internal/
│   │   │       │   │   ├── InMemoryRoomCatalog.java
│   │   │       │   │   └── PostgresRoomCatalog.java
│   │   │       │   ├── Room.java
│   │   │       │   └── RoomCatalog.java
│   │   │       └── Main.java   CHANGED: wires the queue and drains it on shutdown
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── hotelbooking/
│               ├── ApiTests.java   CHANGED: + 3 queue tests
│               ├── ArchitectureTests.java
│               └── BookingServiceTests.java
├── tools/
│   ├── Burst.java   NEW
│   ├── Measure.java
│   └── RaceHttp.java
├── docker-compose.yml
└── pom.xml
```

#### Go

```text
hotel-booking/   end of P10
├── api/
│   ├── api_test.go   CHANGED: passes nil for the queue
│   ├── api.go   CHANGED: takes the queue and publishes its counters
│   ├── cache.go
│   └── stats.go   CHANGED: GET /stats can also publish the queue’s four counters
├── db/
│   └── init.sql
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
│   ├── internal.go   CHANGED: a slow sender, and the queued sender
│   ├── notifications.go   CHANGED: + Counts and CountingNotificationSender
│   └── queue_test.go   NEW
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
│   ├── burst/
│   │   └── main.go   NEW
│   ├── measure/
│   │   └── main.go
│   └── race/
│       └── main.go
├── architecture_test.go
├── docker-compose.yml
├── go.mod
├── go.sum
└── main.go   CHANGED: wires the queue and drains it on shutdown
```

#### JavaScript

```text
hotel-booking/   end of P10
├── db/
│   └── init.sql
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
│   │   │   ├── email.js   CHANGED: sendSeconds makes it as slow as a real mail server
│   │   │   ├── queued.js   NEW
│   │   │   └── sms.js
│   │   └── index.js   CHANGED: the contract now allows a promise, and names the counters
│   ├── payments/
│   │   ├── internal/
│   │   │   └── simulated.js
│   │   └── index.js
│   ├── reservations/
│   │   ├── internal/
│   │   │   ├── postgres-store.js
│   │   │   └── store.js
│   │   ├── booking.js   CHANGED: reserve awaits the confirmation
│   │   ├── domain.js
│   │   └── index.js
│   ├── rooms/
│   │   ├── internal/
│   │   │   ├── catalog.js
│   │   │   └── postgres-catalog.js
│   │   └── index.js
│   ├── api-cache.js
│   └── api.js   CHANGED: GET /stats also publishes the queue’s four counters
├── test/
│   ├── api.test.js   CHANGED: + 3 queue tests
│   ├── architecture.test.js
│   └── booking.test.js
├── tools/
│   ├── burst.js   NEW
│   ├── measure.js
│   └── race-http.js
├── docker-compose.yml
├── main.js   CHANGED: wires the queue and drains it on shutdown
├── package-lock.json
└── package.json
```

#### Dart

```text
hotel-booking/   end of P10
├── bin/
│   └── main.dart   CHANGED: wires the queue and drains it on shutdown
├── db/
│   └── init.sql
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
│   │   │   ├── email.dart   CHANGED: sendSeconds makes it as slow as a real mail server
│   │   │   ├── queued.dart   NEW
│   │   │   └── sms.dart
│   │   └── notifications.dart   CHANGED: the contract now allows a Future, and names the counters
│   ├── payments/
│   │   ├── src/
│   │   │   └── simulated.dart
│   │   └── payments.dart
│   ├── reservations/
│   │   ├── src/
│   │   │   ├── booking.dart   CHANGED: reserve awaits the confirmation
│   │   │   ├── domain.dart
│   │   │   ├── postgres_store.dart
│   │   │   └── store.dart
│   │   └── reservations.dart
│   ├── rooms/
│   │   ├── src/
│   │   │   ├── catalog.dart
│   │   │   └── postgres_catalog.dart
│   │   └── rooms.dart
│   ├── api_cache.dart
│   └── api.dart   CHANGED: GET /stats also publishes the queue’s four counters
├── test/
│   ├── api_test.dart   CHANGED: + 3 queue tests
│   ├── architecture_test.dart
│   └── booking_service_test.dart
├── tool/
│   ├── burst.dart   NEW
│   ├── measure.dart
│   └── race_http.dart
├── analysis_options.yaml
├── docker-compose.yml
└── pubspec.yaml
```

### Your turn · open your project

1. Start Docker Desktop and wait until it says “Engine running”.
2. Open your hotel-booking folder in your editor, and open two terminals in the same folder.
3. In the first terminal run docker compose up -d, then your P09 checks: 19 green (Python: 16 tests, then “Contracts: 3 kept”).
4. Open docs/adr-003-booking-concurrency.md and read the Consequences section. Today adds a fourth ADR to that folder.
5. Open docs/design-notes.md and add a new heading at the bottom: # P10 design notes

**When you finish, you have:** Docker Desktop running, your P09 project open with 19 green checks, and a “P10 design notes” heading.
### Explanation

Nothing today changes the booking rule. The reservation is committed before any of this happens; what changes is whether the guest is kept waiting afterwards, and what the app does when there is more background work than it can keep up with.



---

## 2. Step 1 · Measure what the email costs

The simulated sender has counted emails since P04 and never taken any time, which is exactly why nobody noticed where it was being called from. Give it a delay and the cost becomes visible.





### One booking, step by step

### Diagram: One booking, with the confirmation email sent inside the request

POST /reservations for one night in December, with the app started with HOTEL_EMAIL_SECONDS=1 so the sender is as slow as a real mail server. Follow the numbers: the room is the guest’s from step 4 onwards, and they are made to wait for step 5 anyway.

Diagram as text:

A POST /reservations arrives for R7, one night in December, at an app started with HOTEL_EMAIL_SECONDS=1. Step 2: the overlap check runs against PostgreSQL - about a millisecond. Step 3: the reservation is inserted and committed, and the P09 exclusion constraint is checked as it goes in - another millisecond or two. Step 4: the room is now the guest’s. Everything the booking promised has happened, and nothing later changes it. Step 5: sendConfirmation is called inside the request, so the request waits a full second for the mail server. Step 6: the guest is told 201 Created, 1.02 seconds after asking, having owned the room for almost all of it. Thirty such bookings, one after another, take 30.3 seconds to do roughly thirty milliseconds of work.

**Step by step:** Follow the numbers 1 to 6. The thing to watch for is box 4: after it, the guest has the room, and every arrow after it is the app making them wait for something that has nothing to do with whether they got it.

1. Yellow box 1: the request arrives. Nothing here is new - this is the P09 endpoint, unchanged.
2. White boxes 2 and 3: the overlap check and the insert. Both are fast, and both are the real work of a booking.
3. Green box 4 is the important one. The transaction has committed, so the reservation exists and the room is taken. From here on, the guest has what they came for.
4. The red dashed arrow into box 5 is the problem. sendConfirmation is called from inside reserve, so the request stops and waits for it. Here is the sender, with the delay that makes it behave like a real mail server:

   C#:
   
   ```text
   // HotelBooking/Notifications/Internal/EmailNotificationSender.cs
   namespace HotelBooking.Notifications.Internal;
   
   // Simulated: counts the emails it would send and needs no network.
   //
   // sendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which
   // is how a booking that does well under a millisecond of work becomes a one-second request.
   public class EmailNotificationSender(double sendSeconds = 0) : INotificationSender
   {
       private int _sent;
   
       public int Sent => Volatile.Read(ref _sent);
   
       public void SendConfirmation(Confirmation confirmation)
       {
           if (sendSeconds > 0) Thread.Sleep(TimeSpan.FromSeconds(sendSeconds));
           Interlocked.Increment(ref _sent);
       }
   }
   ```
   
   Python:
   
   ```text
   # hotel_booking/notifications/internal.py
   from __future__ import annotations
   
   import queue
   import threading
   
   from hotel_booking.notifications import Confirmation, NotificationCounts, NotificationSender
   
   
   class EmailNotificationSender:
       """Simulated: counts the emails it would send and needs no network.
   
       send_seconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second,
       which is how a 4 ms booking becomes a one-second request.
       """
   
       def __init__(self, send_seconds: float = 0) -> None:
           self.sent = 0
           self._send_seconds = send_seconds
           self._lock = threading.Lock()
   
       def send_confirmation(self, confirmation: Confirmation) -> None:
           if self._send_seconds:
               threading.Event().wait(self._send_seconds)
           with self._lock:
               self.sent += 1
   
   
   class SmsNotificationSender:
       """Simulated, like the email sender."""
   
       def __init__(self) -> None:
           self.sent = 0
   
       def send_confirmation(self, confirmation: Confirmation) -> None:
           self.sent += 1
   
   
   class QueuedNotificationSender:
       """Takes confirmations off the request and gives them to a background worker.
   
       It keeps the NotificationSender promise, so BookingService cannot tell the difference: the
       booking hands over a Confirmation and carries on. What changes is when the email is sent, and
       what happens when there are more of them than the worker can keep up with.
   
       The queue is bounded. When it is full a confirmation is refused and counted rather than waited
       for, because making the booking wait is the thing this class exists to stop. A refused
       confirmation is not a failed booking - the room is already the guest's - but it is not free
       either, and the rejected counter is what makes the cost visible instead of invisible.
       """
   
       def __init__(self, sender: NotificationSender, capacity: int = 10, workers: int = 1) -> None:
           self._sender = sender
           self._queue: queue.Queue[Confirmation] = queue.Queue(maxsize=capacity)
           self._lock = threading.Lock()
           self._accepted = 0
           self._rejected = 0
           self._completed = 0
           self._stopping = threading.Event()
           self._workers = [
               threading.Thread(target=self._drain, name=f"notifications-{number}", daemon=True)
               for number in range(workers)
           ]
           for worker in self._workers:
               worker.start()
   
       def send_confirmation(self, confirmation: Confirmation) -> None:
           """Put the confirmation on the queue, or count it as refused. Never waits, never raises."""
           try:
               self._queue.put_nowait(confirmation)
           except queue.Full:
               with self._lock:
                   self._rejected += 1
               return
           with self._lock:
               self._accepted += 1
   
       def counts(self) -> NotificationCounts:
           with self._lock:
               return NotificationCounts(self._accepted, self._rejected, self._completed, self._queue.qsize())
   
       def close(self, drain: bool = True) -> None:
           """Stop the workers. With drain=True the queue is emptied first, so a clean shutdown owes
           nobody an email; without it, whatever is still queued dies with the process."""
           if drain:
               self._queue.join()
           self._stopping.set()
           for worker in self._workers:
               worker.join(timeout=5)
   
       def _drain(self) -> None:
           while not self._stopping.is_set():
               try:
                   confirmation = self._queue.get(timeout=0.05)
               except queue.Empty:
                   continue
               try:
                   self._sender.send_confirmation(confirmation)
                   with self._lock:
                       self._completed += 1
               except Exception:
                   # A confirmation that could not be sent is lost, and saying so is the point:
                   # it stays counted as accepted but never completed, so accepted != completed
                   # + queued, which is exactly the signal that work went missing.
                   pass
               finally:
                   self._queue.task_done()
   ```
   
   Java:
   
   ```text
   // src/main/java/hotelbooking/notifications/internal/EmailNotificationSender.java
   package hotelbooking.notifications.internal;
   
   import hotelbooking.notifications.Confirmation;
   import hotelbooking.notifications.NotificationSender;
   import java.util.concurrent.atomic.AtomicInteger;
   
   // Simulated: counts the emails it would send and needs no network.
   //
   // sendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which
   // is how a booking that does well under a millisecond of work becomes a one-second request.
   public class EmailNotificationSender implements NotificationSender {
       private final double sendSeconds;
       private final AtomicInteger sent = new AtomicInteger();
   
       public EmailNotificationSender() {
           this(0);
       }
   
       public EmailNotificationSender(double sendSeconds) {
           this.sendSeconds = sendSeconds;
       }
   
       public int sent() {
           return sent.get();
       }
   
       @Override
       public void sendConfirmation(Confirmation confirmation) {
           if (sendSeconds > 0) {
               try {
                   Thread.sleep((long) (sendSeconds * 1000));
               } catch (InterruptedException interrupted) {
                   Thread.currentThread().interrupt();
                   return;
               }
           }
           sent.incrementAndGet();
       }
   }
   ```
   
   Go:
   
   ```text
   // notifications/internal.go
   package notifications
   
   import (
   	"sync"
   	"sync/atomic"
   	"time"
   )
   
   // EmailNotificationSender is simulated: it counts the emails it would send and needs no network.
   //
   // SendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which is
   // how a booking that does well under a millisecond of work becomes a one-second request.
   type EmailNotificationSender struct {
   	SendSeconds float64
   	sent        atomic.Int64
   }
   
   func (email *EmailNotificationSender) SendConfirmation(confirmation Confirmation) error {
   	if email.SendSeconds > 0 {
   		time.Sleep(time.Duration(email.SendSeconds * float64(time.Second)))
   	}
   	email.sent.Add(1)
   	return nil
   }
   
   // Sent is how many emails this sender has actually sent.
   func (email *EmailNotificationSender) Sent() int64 {
   	return email.sent.Load()
   }
   
   // SmsNotificationSender is simulated, like the email sender.
   type SmsNotificationSender struct {
   	Sent int
   }
   
   func (sms *SmsNotificationSender) SendConfirmation(confirmation Confirmation) error {
   	sms.Sent++
   	return nil
   }
   
   // QueuedNotificationSender takes confirmations off the request and gives them to a background
   // worker.
   //
   // It keeps the NotificationSender contract, so BookingService cannot tell the difference: the
   // booking hands over a Confirmation and carries on. What changes is when the email is sent, and what
   // happens when there are more of them than the worker can keep up with.
   //
   // The channel is buffered, which is the bound, and the send is a select with a default branch, which
   // is what makes a full queue refuse instead of wait. A refused confirmation is not a failed booking
   // - the room is already the guest's - but it is not free either, and Rejected is what makes that
   // cost visible instead of invisible.
   type QueuedNotificationSender struct {
   	sender    NotificationSender
   	jobs      chan Confirmation
   	stopping  chan struct{}
   	done      sync.WaitGroup
   	accepted  atomic.Int64
   	rejected  atomic.Int64
   	completed atomic.Int64
   	closeOnce sync.Once
   }
   
   func NewQueuedNotificationSender(sender NotificationSender, capacity, workers int) *QueuedNotificationSender {
   	queue := &QueuedNotificationSender{
   		sender:   sender,
   		jobs:     make(chan Confirmation, capacity),
   		stopping: make(chan struct{}),
   	}
   	for worker := 0; worker < workers; worker++ {
   		queue.done.Add(1)
   		go queue.drain()
   	}
   	return queue
   }
   
   // SendConfirmation never waits and never fails: the whole point is that the booking is not held up.
   func (queue *QueuedNotificationSender) SendConfirmation(confirmation Confirmation) error {
   	select {
   	case queue.jobs <- confirmation:
   		queue.accepted.Add(1)
   	default:
   		queue.rejected.Add(1)
   	}
   	return nil
   }
   
   func (queue *QueuedNotificationSender) Counts() Counts {
   	return Counts{
   		Accepted:  queue.accepted.Load(),
   		Rejected:  queue.rejected.Load(),
   		Completed: queue.completed.Load(),
   		Queued:    len(queue.jobs),
   	}
   }
   
   // Close stops the workers. With drain the queue is emptied first, so a polite shutdown owes nobody
   // an email; without it, whatever is still queued dies with the process. Nothing here helps with a
   // process that is killed rather than asked to stop: see docs/queue-trace.md.
   func (queue *QueuedNotificationSender) Close(drain bool) {
   	queue.closeOnce.Do(func() {
   		if drain {
   			for len(queue.jobs) > 0 {
   				time.Sleep(10 * time.Millisecond)
   			}
   		}
   		close(queue.stopping)
   		queue.done.Wait()
   	})
   }
   
   func (queue *QueuedNotificationSender) drain() {
   	defer queue.done.Done()
   	for {
   		select {
   		case <-queue.stopping:
   			return
   		case confirmation := <-queue.jobs:
   			if err := queue.sender.SendConfirmation(confirmation); err != nil {
   				// A confirmation that could not be sent is lost, and saying so is the point: it
   				// stays counted as accepted but never completed, so Accepted != Completed + Queued,
   				// which is exactly the signal that work went missing.
   				continue
   			}
   			queue.completed.Add(1)
   		}
   	}
   }
   ```
   
   JavaScript:
   
   ```text
   // src/notifications/internal/email.js
   // Simulated: counts the messages it would send and needs no network.
   //
   // sendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which
   // is how a booking that does well under a millisecond of work becomes a one-second request.
   const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));
   
   export class EmailNotificationSender {
     sent = 0;
   
     constructor(sendSeconds = 0) {
       this.sendSeconds = sendSeconds;
     }
   
     async sendConfirmation(confirmation) {
       if (this.sendSeconds) await wait(this.sendSeconds * 1000);
       this.sent += 1;
     }
   }
   ```
   
   Dart:
   
   ```text
   // lib/notifications/src/email.dart
   import '../notifications.dart';
   
   // Simulated: counts the emails it would send and needs no network.
   //
   // sendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which
   // is how a booking that does well under a millisecond of work becomes a one-second request.
   class EmailNotificationSender implements NotificationSender {
     EmailNotificationSender({this.sendSeconds = 0});
   
     final double sendSeconds;
     int sent = 0;
   
     @override
     Future<void> sendConfirmation(Confirmation confirmation) async {
       if (sendSeconds > 0) {
         await Future<void>.delayed(Duration(milliseconds: (sendSeconds * 1000).round()));
       }
       sent++;
     }
   }
   ```
5. Yellow box 5: one second of waiting. Measured against the booking itself, which is under a millisecond:

   text:
   
   ```text
   BookingService.reserve(Maya, R7, 1-2 December)
     Accepted in 1.02 s
   
   Of that second, the booking itself took under a millisecond. The rest was the confirmation email,
   sent inside the request while the guest waited for a room that was already theirs.
   ```
6. Yellow box 6: the answer, a second late. Thirty bookings in a row make the shape obvious:

   text:
   
   ```text
   Thirty bookings, one after another, with the email sent inside the request:
   
   Accepted: 30
   Rejected: 0
   Slowest booking: 1.02 s
   Total: 30.3 s
   
   Thirty seconds to do about thirty milliseconds of work. Every guest waited a full second for a
   room that was already theirs.
   ```
7. The guest is not waiting for their room. They are waiting for an email about a room they already have - and if the mail server is down rather than slow, this becomes a failed-looking booking that actually succeeded.
8. The fix is not to make the email faster. It is to stop the request waiting for it at all, which is what the next diagram does.

**Legend:**
- **Yellow box.** Something outside the app, or the step where the guest is kept waiting. In this diagram: The request arriving, the wait for the mail server, and the answer that comes a second late.
- **White box.** An ordinary step: one piece of the app doing one thing. In this diagram: The two steps that are the actual booking: the overlap check and the insert.
- **Green box.** The step that does the thing this diagram is about, or the outcome it is aiming at. In this diagram: The moment the room becomes the guest’s. Everything after it is the app, not the booking.
- **Solid line with a filled arrowhead.** What happens next. Follow these in order. In this diagram: What happens next in the request.
- **Red dashed line with an open arrowhead.** The case the design refuses, counted rather than hidden. In this diagram: The step the design is about to remove: waiting for the email before answering.
- **Number in front of a box label.** The order things happen in: 1 first, then 2, and so on. In this diagram: 1 to 6 in order; 4 is the moment the guest has the room and 6 is when they are told.

### What Step 1 adds

| What | Where | What it does |
| --- | --- | --- |
| sendSeconds | The email sender | Makes the sender as slow as a real mail server, from HOTEL_EMAIL_SECONDS |
| The burst tool | tools/ (Dart: tool/) | Thirty bookings as fast as it can, then GET /stats |
| docs/queue-trace.md | docs/ | The measurement, so there is a “before” to compare with |

**What HOTEL_EMAIL_SECONDS is for** In short: It makes the confirmation sender as slow as a real mail server, so the cost of sending it inside the request is visible. The simulated sender has counted emails since P04 and never taken any time, which is exactly why nobody noticed where it was being called from. One second is not a realistic mail-server latency so much as a legible one. The point is not the number; it is that the request waits for it.

1. Start: the sender is given sendSeconds = 1.
2. Action: thirty bookings are made, one after another.
3. Result:
Thirty bookings, one after another, with the email sent inside the request:

Accepted: 30
Rejected: 0
Slowest booking: 1.02 s
Total: 30.3 s

Thirty seconds to do about thirty milliseconds of work. Every guest waited a full second for a
room that was already theirs.

C#:

```text
// HotelBooking/Notifications/Internal/EmailNotificationSender.cs
namespace HotelBooking.Notifications.Internal;

// Simulated: counts the emails it would send and needs no network.
//
// sendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which
// is how a booking that does well under a millisecond of work becomes a one-second request.
public class EmailNotificationSender(double sendSeconds = 0) : INotificationSender
{
    private int _sent;

    public int Sent => Volatile.Read(ref _sent);

    public void SendConfirmation(Confirmation confirmation)
    {
        if (sendSeconds > 0) Thread.Sleep(TimeSpan.FromSeconds(sendSeconds));
        Interlocked.Increment(ref _sent);
    }
}
```

Python:

```text
# hotel_booking/notifications/internal.py
from __future__ import annotations

import queue
import threading

from hotel_booking.notifications import Confirmation, NotificationCounts, NotificationSender


class EmailNotificationSender:
    """Simulated: counts the emails it would send and needs no network.

    send_seconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second,
    which is how a 4 ms booking becomes a one-second request.
    """

    def __init__(self, send_seconds: float = 0) -> None:
        self.sent = 0
        self._send_seconds = send_seconds
        self._lock = threading.Lock()

    def send_confirmation(self, confirmation: Confirmation) -> None:
        if self._send_seconds:
            threading.Event().wait(self._send_seconds)
        with self._lock:
            self.sent += 1


class SmsNotificationSender:
    """Simulated, like the email sender."""

    def __init__(self) -> None:
        self.sent = 0

    def send_confirmation(self, confirmation: Confirmation) -> None:
        self.sent += 1


class QueuedNotificationSender:
    """Takes confirmations off the request and gives them to a background worker.

    It keeps the NotificationSender promise, so BookingService cannot tell the difference: the
    booking hands over a Confirmation and carries on. What changes is when the email is sent, and
    what happens when there are more of them than the worker can keep up with.

    The queue is bounded. When it is full a confirmation is refused and counted rather than waited
    for, because making the booking wait is the thing this class exists to stop. A refused
    confirmation is not a failed booking - the room is already the guest's - but it is not free
    either, and the rejected counter is what makes the cost visible instead of invisible.
    """

    def __init__(self, sender: NotificationSender, capacity: int = 10, workers: int = 1) -> None:
        self._sender = sender
        self._queue: queue.Queue[Confirmation] = queue.Queue(maxsize=capacity)
        self._lock = threading.Lock()
        self._accepted = 0
        self._rejected = 0
        self._completed = 0
        self._stopping = threading.Event()
        self._workers = [
            threading.Thread(target=self._drain, name=f"notifications-{number}", daemon=True)
            for number in range(workers)
        ]
        for worker in self._workers:
            worker.start()

    def send_confirmation(self, confirmation: Confirmation) -> None:
        """Put the confirmation on the queue, or count it as refused. Never waits, never raises."""
        try:
            self._queue.put_nowait(confirmation)
        except queue.Full:
            with self._lock:
                self._rejected += 1
            return
        with self._lock:
            self._accepted += 1

    def counts(self) -> NotificationCounts:
        with self._lock:
            return NotificationCounts(self._accepted, self._rejected, self._completed, self._queue.qsize())

    def close(self, drain: bool = True) -> None:
        """Stop the workers. With drain=True the queue is emptied first, so a clean shutdown owes
        nobody an email; without it, whatever is still queued dies with the process."""
        if drain:
            self._queue.join()
        self._stopping.set()
        for worker in self._workers:
            worker.join(timeout=5)

    def _drain(self) -> None:
        while not self._stopping.is_set():
            try:
                confirmation = self._queue.get(timeout=0.05)
            except queue.Empty:
                continue
            try:
                self._sender.send_confirmation(confirmation)
                with self._lock:
                    self._completed += 1
            except Exception:
                # A confirmation that could not be sent is lost, and saying so is the point:
                # it stays counted as accepted but never completed, so accepted != completed
                # + queued, which is exactly the signal that work went missing.
                pass
            finally:
                self._queue.task_done()
```

Java:

```text
// src/main/java/hotelbooking/notifications/internal/EmailNotificationSender.java
package hotelbooking.notifications.internal;

import hotelbooking.notifications.Confirmation;
import hotelbooking.notifications.NotificationSender;
import java.util.concurrent.atomic.AtomicInteger;

// Simulated: counts the emails it would send and needs no network.
//
// sendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which
// is how a booking that does well under a millisecond of work becomes a one-second request.
public class EmailNotificationSender implements NotificationSender {
    private final double sendSeconds;
    private final AtomicInteger sent = new AtomicInteger();

    public EmailNotificationSender() {
        this(0);
    }

    public EmailNotificationSender(double sendSeconds) {
        this.sendSeconds = sendSeconds;
    }

    public int sent() {
        return sent.get();
    }

    @Override
    public void sendConfirmation(Confirmation confirmation) {
        if (sendSeconds > 0) {
            try {
                Thread.sleep((long) (sendSeconds * 1000));
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        sent.incrementAndGet();
    }
}
```

Go:

```text
// notifications/internal.go
package notifications

import (
	"sync"
	"sync/atomic"
	"time"
)

// EmailNotificationSender is simulated: it counts the emails it would send and needs no network.
//
// SendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which is
// how a booking that does well under a millisecond of work becomes a one-second request.
type EmailNotificationSender struct {
	SendSeconds float64
	sent        atomic.Int64
}

func (email *EmailNotificationSender) SendConfirmation(confirmation Confirmation) error {
	if email.SendSeconds > 0 {
		time.Sleep(time.Duration(email.SendSeconds * float64(time.Second)))
	}
	email.sent.Add(1)
	return nil
}

// Sent is how many emails this sender has actually sent.
func (email *EmailNotificationSender) Sent() int64 {
	return email.sent.Load()
}

// SmsNotificationSender is simulated, like the email sender.
type SmsNotificationSender struct {
	Sent int
}

func (sms *SmsNotificationSender) SendConfirmation(confirmation Confirmation) error {
	sms.Sent++
	return nil
}

// QueuedNotificationSender takes confirmations off the request and gives them to a background
// worker.
//
// It keeps the NotificationSender contract, so BookingService cannot tell the difference: the
// booking hands over a Confirmation and carries on. What changes is when the email is sent, and what
// happens when there are more of them than the worker can keep up with.
//
// The channel is buffered, which is the bound, and the send is a select with a default branch, which
// is what makes a full queue refuse instead of wait. A refused confirmation is not a failed booking
// - the room is already the guest's - but it is not free either, and Rejected is what makes that
// cost visible instead of invisible.
type QueuedNotificationSender struct {
	sender    NotificationSender
	jobs      chan Confirmation
	stopping  chan struct{}
	done      sync.WaitGroup
	accepted  atomic.Int64
	rejected  atomic.Int64
	completed atomic.Int64
	closeOnce sync.Once
}

func NewQueuedNotificationSender(sender NotificationSender, capacity, workers int) *QueuedNotificationSender {
	queue := &QueuedNotificationSender{
		sender:   sender,
		jobs:     make(chan Confirmation, capacity),
		stopping: make(chan struct{}),
	}
	for worker := 0; worker < workers; worker++ {
		queue.done.Add(1)
		go queue.drain()
	}
	return queue
}

// SendConfirmation never waits and never fails: the whole point is that the booking is not held up.
func (queue *QueuedNotificationSender) SendConfirmation(confirmation Confirmation) error {
	select {
	case queue.jobs <- confirmation:
		queue.accepted.Add(1)
	default:
		queue.rejected.Add(1)
	}
	return nil
}

func (queue *QueuedNotificationSender) Counts() Counts {
	return Counts{
		Accepted:  queue.accepted.Load(),
		Rejected:  queue.rejected.Load(),
		Completed: queue.completed.Load(),
		Queued:    len(queue.jobs),
	}
}

// Close stops the workers. With drain the queue is emptied first, so a polite shutdown owes nobody
// an email; without it, whatever is still queued dies with the process. Nothing here helps with a
// process that is killed rather than asked to stop: see docs/queue-trace.md.
func (queue *QueuedNotificationSender) Close(drain bool) {
	queue.closeOnce.Do(func() {
		if drain {
			for len(queue.jobs) > 0 {
				time.Sleep(10 * time.Millisecond)
			}
		}
		close(queue.stopping)
		queue.done.Wait()
	})
}

func (queue *QueuedNotificationSender) drain() {
	defer queue.done.Done()
	for {
		select {
		case <-queue.stopping:
			return
		case confirmation := <-queue.jobs:
			if err := queue.sender.SendConfirmation(confirmation); err != nil {
				// A confirmation that could not be sent is lost, and saying so is the point: it
				// stays counted as accepted but never completed, so Accepted != Completed + Queued,
				// which is exactly the signal that work went missing.
				continue
			}
			queue.completed.Add(1)
		}
	}
}
```

JavaScript:

```text
// src/notifications/internal/email.js
// Simulated: counts the messages it would send and needs no network.
//
// sendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which
// is how a booking that does well under a millisecond of work becomes a one-second request.
const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

export class EmailNotificationSender {
  sent = 0;

  constructor(sendSeconds = 0) {
    this.sendSeconds = sendSeconds;
  }

  async sendConfirmation(confirmation) {
    if (this.sendSeconds) await wait(this.sendSeconds * 1000);
    this.sent += 1;
  }
}
```

Dart:

```text
// lib/notifications/src/email.dart
import '../notifications.dart';

// Simulated: counts the emails it would send and needs no network.
//
// sendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which
// is how a booking that does well under a millisecond of work becomes a one-second request.
class EmailNotificationSender implements NotificationSender {
  EmailNotificationSender({this.sendSeconds = 0});

  final double sendSeconds;
  int sent = 0;

  @override
  Future<void> sendConfirmation(Confirmation confirmation) async {
    if (sendSeconds > 0) {
      await Future<void>.delayed(Duration(milliseconds: (sendSeconds * 1000).round()));
    }
    sent++;
  }
}
```

**What the burst tool does** In short: It sends thirty bookings as fast as it can and then reads GET /stats, so you can see what the queue did with them. The burst sends 30 bookings as fast as it can: rooms R7 and R8, one night each,
1 to 15 December. Every one of them is a legal booking - no two share a room and a night - so
nothing is refused by the booking rule and everything that happens to them happens in the queue. Everything it reports as refused was refused by the notification queue, not by the booking rule. That separation is the whole reason the dates are chosen the way they are.

1. Start: the app is running with HOTEL_EMAIL_SECONDS=1 and a queue that holds ten.
2. Action: you run the burst tool.
3. Result:
Sending 30 bookings, one after another...

Accepted: 30
Rejected: 0
Slowest booking: 47 ms
Total: 0.1 s

GET /stats
{"notificationsAccepted":11,"notificationsRejected":19,"notificationsCompleted":0,"notificationsQueued":10}

Read the two lines together. Every booking was accepted - all 30 guests have their rooms - and
19 confirmation emails were refused on purpose, because the queue was full. The booking and the
email now succeed and fail separately, which is the whole point.

C#:

```text
// tools/burst.cs
// Sends a burst of bookings as fast as it can, then reads GET /stats.
// 
// Every booking is for a different room and night - R7 and R8, one night each, 1 to 15 December - so
// all 30 are legal under P09's exclusion constraint and nothing is refused by the booking rule.
// Whatever is refused here was refused by the notification queue, which is the thing being measured.
// 
// Start the app with HOTEL_EMAIL_SECONDS=1 first, so the confirmation sender is as slow as a real
// mail server.
//
//   dotnet run tools/burst.cs
//
// A file-based app (dotnet run <file>.cs) is compiled ready for ahead-of-time publishing, and that
// turns off the reflection the typed JSON helpers below need - PostAsJsonAsync would throw
// "Reflection-based serialization has been disabled for this application". This tool is run, never
// published, so the AOT settings do nothing for it and are turned off.
#:property PublishAot=false
using System.Net.Http.Json;
using System.Diagnostics;

const string Base = "http://localhost:5080";
const int Burst = 30;

using var client = new HttpClient();
Console.WriteLine($"Sending {Burst} bookings, one after another...");
Console.WriteLine();

var accepted = 0;
var rejected = 0;
double slowest = 0;
var total = Stopwatch.StartNew();
for (var index = 0; index < Burst; index++)
{
    var night = 1 + index / 2;
    var room = index % 2 == 0 ? "R7" : "R8";
    var body = new
    {
        guestName = $"Guest{night}{room}", guestEmail = $"guest{night}{room}@example.com",
        room, checkIn = $"2026-12-{night:00}", checkOut = $"2026-12-{night + 1:00}",
    };
    var one = Stopwatch.StartNew();
    var response = await client.PostAsJsonAsync($"{Base}/reservations", body);
    one.Stop();
    slowest = Math.Max(slowest, one.Elapsed.TotalMilliseconds);
    if ((int)response.StatusCode == 201) accepted++; else rejected++;
}
total.Stop();

Console.WriteLine($"Accepted: {accepted}");
Console.WriteLine($"Rejected: {rejected}");
Console.WriteLine($"Slowest booking: {slowest:0} ms");
Console.WriteLine($"Total: {total.Elapsed.TotalSeconds:0.0} s");
Console.WriteLine();
Console.WriteLine("GET /stats");
Console.WriteLine(await client.GetStringAsync($"{Base}/stats"));
```

Python:

```text
# tools/burst.py
"""Sends a burst of bookings as fast as it can, then reads GET /stats.

  python tools/burst.py

Every booking is for a different room and night - R7 and R8, one night each, 1 to 15 December -
so all 30 are legal under P09's exclusion constraint and nothing is refused by the booking rule.
Whatever is refused here was refused by the notification queue, which is the thing being measured.

Start the app with HOTEL_EMAIL_SECONDS=1 first, so the confirmation sender is as slow as a real
mail server.
"""
from __future__ import annotations

import json
import time
import urllib.error
import urllib.request

BASE = "http://localhost:5080"
BURST = 30


def book(night: int, room: str) -> tuple[int, float]:
    body = {
        "guestName": f"Guest{night}{room}", "guestEmail": f"guest{night}{room}@example.com",
        "room": room, "checkIn": f"2026-12-{night:02d}", "checkOut": f"2026-12-{night + 1:02d}",
    }
    request = urllib.request.Request(f"{BASE}/reservations", method="POST",
                                     data=json.dumps(body).encode(),
                                     headers={"Content-Type": "application/json"})
    started = time.monotonic()
    try:
        with urllib.request.urlopen(request) as response:
            response.read()
            return response.status, time.monotonic() - started
    except urllib.error.HTTPError as error:
        error.read()
        return error.code, time.monotonic() - started


def main() -> None:
    print(f"Sending {BURST} bookings, one after another...")
    print()
    accepted = rejected = 0
    slowest = 0.0
    started = time.monotonic()
    for index in range(BURST):
        night, room = 1 + index // 2, "R7" if index % 2 == 0 else "R8"
        status, seconds = book(night, room)
        slowest = max(slowest, seconds)
        if status == 201:
            accepted += 1
        else:
            rejected += 1
    total = time.monotonic() - started

    print(f"Accepted: {accepted}")
    print(f"Rejected: {rejected}")
    print(f"Slowest booking: {slowest * 1000:.0f} ms" if slowest < 1 else f"Slowest booking: {slowest:.2f} s")
    print(f"Total: {total:.1f} s")
    print()
    with urllib.request.urlopen(f"{BASE}/stats") as response:
        print("GET /stats")
        print(response.read().decode())


if __name__ == "__main__":
    main()
```

Java:

```text
// tools/Burst.java
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
                    {"guestName":"Guest%1$d%2$s","guestEmail":"guest%1$d%2$s@example.com","room":"%2$s",                    "checkIn":"2026-12-%1$02d","checkOut":"2026-12-%3$02d"}"""
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
```

Go:

```text
// tools/burst/main.go
// Sends a burst of bookings as fast as it can, then reads GET /stats.
// 
// Every booking is for a different room and night - R7 and R8, one night each, 1 to 15 December - so
// all 30 are legal under P09's exclusion constraint and nothing is refused by the booking rule.
// Whatever is refused here was refused by the notification queue, which is the thing being measured.
// 
// Start the app with HOTEL_EMAIL_SECONDS=1 first, so the confirmation sender is as slow as a real
// mail server.
//
//	go run ./tools/burst
package main

import (
	"fmt"
	"io"
	"net/http"
	"strings"
	"time"
)

const base = "http://localhost:5080"
const burst = 30

func main() {
	fmt.Printf("Sending %d bookings, one after another...\n", burst)
	fmt.Println()

	accepted, rejected := 0, 0
	var slowest time.Duration
	started := time.Now()
	for index := 0; index < burst; index++ {
		night := 1 + index/2
		room := "R7"
		if index%2 == 1 {
			room = "R8"
		}
		body := fmt.Sprintf(`{"guestName":"Guest%d%s","guestEmail":"guest%d%s@example.com","room":"%s","checkIn":"2026-12-%02d","checkOut":"2026-12-%02d"}`,
			night, room, night, room, room, night, night+1)
		one := time.Now()
		response, err := http.Post(base+"/reservations", "application/json", strings.NewReader(body))
		if err != nil {
			fmt.Printf("the request failed: %v\n", err)
			return
		}
		io.Copy(io.Discard, response.Body)
		response.Body.Close()
		if elapsed := time.Since(one); elapsed > slowest {
			slowest = elapsed
		}
		if response.StatusCode == http.StatusCreated {
			accepted++
		} else {
			rejected++
		}
	}
	total := time.Since(started)

	fmt.Printf("Accepted: %d\n", accepted)
	fmt.Printf("Rejected: %d\n", rejected)
	fmt.Printf("Slowest booking: %d ms\n", slowest.Milliseconds())
	fmt.Printf("Total: %.1f s\n", total.Seconds())
	fmt.Println()
	fmt.Println("GET /stats")
	stats, err := http.Get(base + "/stats")
	if err != nil {
		fmt.Printf("could not read /stats: %v\n", err)
		return
	}
	defer stats.Body.Close()
	text, _ := io.ReadAll(stats.Body)
	fmt.Println(string(text))
}
```

JavaScript:

```text
// tools/burst.js
// Sends a burst of bookings as fast as it can, then reads GET /stats.
//
//   node tools/burst.js
//
// Every booking is for a different room and night - R7 and R8, one night each, 1 to 15 December -
// so all 30 are legal under P09's exclusion constraint and nothing is refused by the booking rule.
// Whatever is refused here was refused by the notification queue, which is the thing being measured.
//
// Start the app with HOTEL_EMAIL_SECONDS=1 first, so the confirmation sender is as slow as a real
// mail server.
const BASE = 'http://localhost:5080';
const BURST = 30;

async function book(night, room) {
  const body = {
    guestName: `Guest${night}${room}`, guestEmail: `guest${night}${room}@example.com`,
    room, checkIn: `2026-12-${String(night).padStart(2, '0')}`,
    checkOut: `2026-12-${String(night + 1).padStart(2, '0')}`,
  };
  const started = performance.now();
  const response = await fetch(`${BASE}/reservations`, {
    method: 'POST', body: JSON.stringify(body),
    headers: { 'Content-Type': 'application/json', Connection: 'close' },
  });
  await response.text();
  return { status: response.status, ms: performance.now() - started };
}

console.log(`Sending ${BURST} bookings, one after another...`);
console.log();

let accepted = 0; let rejected = 0; let slowest = 0;
const started = performance.now();
for (let index = 0; index < BURST; index += 1) {
  const night = 1 + Math.floor(index / 2);
  const room = index % 2 === 0 ? 'R7' : 'R8';
  const { status, ms } = await book(night, room);
  slowest = Math.max(slowest, ms);
  if (status === 201) accepted += 1; else rejected += 1;
}
const total = (performance.now() - started) / 1000;

console.log(`Accepted: ${accepted}`);
console.log(`Rejected: ${rejected}`);
console.log(`Slowest booking: ${slowest.toFixed(0)} ms`);
console.log(`Total: ${total.toFixed(1)} s`);
console.log();
console.log('GET /stats');
console.log(await (await fetch(`${BASE}/stats`)).text());
```

Dart:

```text
// tool/burst.dart
// Sends a burst of bookings as fast as it can, then reads GET /stats.
// 
// Every booking is for a different room and night - R7 and R8, one night each, 1 to 15 December - so
// all 30 are legal under P09's exclusion constraint and nothing is refused by the booking rule.
// Whatever is refused here was refused by the notification queue, which is the thing being measured.
// 
// Start the app with HOTEL_EMAIL_SECONDS=1 first, so the confirmation sender is as slow as a real
// mail server.
//
//   dart run tool/burst.dart
import 'dart:convert';
import 'dart:io';

const base = 'http://localhost:5080';
const burst = 30;

Future<void> main() async {
  final client = HttpClient();
  print('Sending $burst bookings, one after another...');
  print('');

  var accepted = 0;
  var rejected = 0;
  var slowest = 0;
  final started = DateTime.now();
  for (var index = 0; index < burst; index++) {
    final night = 1 + index ~/ 2;
    final room = index % 2 == 0 ? 'R7' : 'R8';
    final body = {
      'guestName': 'Guest$night$room',
      'guestEmail': 'guest$night$room@example.com',
      'room': room,
      'checkIn': '2026-12-${night.toString().padLeft(2, '0')}',
      'checkOut': '2026-12-${(night + 1).toString().padLeft(2, '0')}',
    };
    final one = DateTime.now();
    final request = await client.postUrl(Uri.parse('$base/reservations'));
    request.headers.contentType = ContentType.json;
    request.write(jsonEncode(body));
    final response = await request.close();
    await response.drain<void>();
    final elapsed = DateTime.now().difference(one).inMilliseconds;
    if (elapsed > slowest) slowest = elapsed;
    if (response.statusCode == 201) {
      accepted++;
    } else {
      rejected++;
    }
  }
  final total = DateTime.now().difference(started).inMilliseconds / 1000;

  print('Accepted: $accepted');
  print('Rejected: $rejected');
  print('Slowest booking: $slowest ms');
  print('Total: ${total.toStringAsFixed(1)} s');
  print('');
  print('GET /stats');
  final stats = await (await client.getUrl(Uri.parse('$base/stats'))).close();
  print(await stats.transform(utf8.decoder).join());
  client.close();
}
```

### Completed example 1.1 · a sender that takes time

One parameter, defaulting to zero, so every existing test and the demo behave exactly as before.

**What HOTEL_EMAIL_SECONDS is for** In short: It makes the confirmation sender as slow as a real mail server, so the cost of sending it inside the request is visible. The simulated sender has counted emails since P04 and never taken any time, which is exactly why nobody noticed where it was being called from. One second is not a realistic mail-server latency so much as a legible one. The point is not the number; it is that the request waits for it.

1. Start: the sender is given sendSeconds = 1.
2. Action: thirty bookings are made, one after another.
3. Result:
Thirty bookings, one after another, with the email sent inside the request:

Accepted: 30
Rejected: 0
Slowest booking: 1.02 s
Total: 30.3 s

Thirty seconds to do about thirty milliseconds of work. Every guest waited a full second for a
room that was already theirs.

C#:

```text
// HotelBooking/Notifications/Internal/EmailNotificationSender.cs
namespace HotelBooking.Notifications.Internal;

// Simulated: counts the emails it would send and needs no network.
//
// sendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which
// is how a booking that does well under a millisecond of work becomes a one-second request.
public class EmailNotificationSender(double sendSeconds = 0) : INotificationSender
{
    private int _sent;

    public int Sent => Volatile.Read(ref _sent);

    public void SendConfirmation(Confirmation confirmation)
    {
        if (sendSeconds > 0) Thread.Sleep(TimeSpan.FromSeconds(sendSeconds));
        Interlocked.Increment(ref _sent);
    }
}
```

Python:

```text
# hotel_booking/notifications/internal.py
from __future__ import annotations

import queue
import threading

from hotel_booking.notifications import Confirmation, NotificationCounts, NotificationSender


class EmailNotificationSender:
    """Simulated: counts the emails it would send and needs no network.

    send_seconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second,
    which is how a 4 ms booking becomes a one-second request.
    """

    def __init__(self, send_seconds: float = 0) -> None:
        self.sent = 0
        self._send_seconds = send_seconds
        self._lock = threading.Lock()

    def send_confirmation(self, confirmation: Confirmation) -> None:
        if self._send_seconds:
            threading.Event().wait(self._send_seconds)
        with self._lock:
            self.sent += 1


class SmsNotificationSender:
    """Simulated, like the email sender."""

    def __init__(self) -> None:
        self.sent = 0

    def send_confirmation(self, confirmation: Confirmation) -> None:
        self.sent += 1


class QueuedNotificationSender:
    """Takes confirmations off the request and gives them to a background worker.

    It keeps the NotificationSender promise, so BookingService cannot tell the difference: the
    booking hands over a Confirmation and carries on. What changes is when the email is sent, and
    what happens when there are more of them than the worker can keep up with.

    The queue is bounded. When it is full a confirmation is refused and counted rather than waited
    for, because making the booking wait is the thing this class exists to stop. A refused
    confirmation is not a failed booking - the room is already the guest's - but it is not free
    either, and the rejected counter is what makes the cost visible instead of invisible.
    """

    def __init__(self, sender: NotificationSender, capacity: int = 10, workers: int = 1) -> None:
        self._sender = sender
        self._queue: queue.Queue[Confirmation] = queue.Queue(maxsize=capacity)
        self._lock = threading.Lock()
        self._accepted = 0
        self._rejected = 0
        self._completed = 0
        self._stopping = threading.Event()
        self._workers = [
            threading.Thread(target=self._drain, name=f"notifications-{number}", daemon=True)
            for number in range(workers)
        ]
        for worker in self._workers:
            worker.start()

    def send_confirmation(self, confirmation: Confirmation) -> None:
        """Put the confirmation on the queue, or count it as refused. Never waits, never raises."""
        try:
            self._queue.put_nowait(confirmation)
        except queue.Full:
            with self._lock:
                self._rejected += 1
            return
        with self._lock:
            self._accepted += 1

    def counts(self) -> NotificationCounts:
        with self._lock:
            return NotificationCounts(self._accepted, self._rejected, self._completed, self._queue.qsize())

    def close(self, drain: bool = True) -> None:
        """Stop the workers. With drain=True the queue is emptied first, so a clean shutdown owes
        nobody an email; without it, whatever is still queued dies with the process."""
        if drain:
            self._queue.join()
        self._stopping.set()
        for worker in self._workers:
            worker.join(timeout=5)

    def _drain(self) -> None:
        while not self._stopping.is_set():
            try:
                confirmation = self._queue.get(timeout=0.05)
            except queue.Empty:
                continue
            try:
                self._sender.send_confirmation(confirmation)
                with self._lock:
                    self._completed += 1
            except Exception:
                # A confirmation that could not be sent is lost, and saying so is the point:
                # it stays counted as accepted but never completed, so accepted != completed
                # + queued, which is exactly the signal that work went missing.
                pass
            finally:
                self._queue.task_done()
```

Java:

```text
// src/main/java/hotelbooking/notifications/internal/EmailNotificationSender.java
package hotelbooking.notifications.internal;

import hotelbooking.notifications.Confirmation;
import hotelbooking.notifications.NotificationSender;
import java.util.concurrent.atomic.AtomicInteger;

// Simulated: counts the emails it would send and needs no network.
//
// sendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which
// is how a booking that does well under a millisecond of work becomes a one-second request.
public class EmailNotificationSender implements NotificationSender {
    private final double sendSeconds;
    private final AtomicInteger sent = new AtomicInteger();

    public EmailNotificationSender() {
        this(0);
    }

    public EmailNotificationSender(double sendSeconds) {
        this.sendSeconds = sendSeconds;
    }

    public int sent() {
        return sent.get();
    }

    @Override
    public void sendConfirmation(Confirmation confirmation) {
        if (sendSeconds > 0) {
            try {
                Thread.sleep((long) (sendSeconds * 1000));
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        sent.incrementAndGet();
    }
}
```

Go:

```text
// notifications/internal.go
package notifications

import (
	"sync"
	"sync/atomic"
	"time"
)

// EmailNotificationSender is simulated: it counts the emails it would send and needs no network.
//
// SendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which is
// how a booking that does well under a millisecond of work becomes a one-second request.
type EmailNotificationSender struct {
	SendSeconds float64
	sent        atomic.Int64
}

func (email *EmailNotificationSender) SendConfirmation(confirmation Confirmation) error {
	if email.SendSeconds > 0 {
		time.Sleep(time.Duration(email.SendSeconds * float64(time.Second)))
	}
	email.sent.Add(1)
	return nil
}

// Sent is how many emails this sender has actually sent.
func (email *EmailNotificationSender) Sent() int64 {
	return email.sent.Load()
}

// SmsNotificationSender is simulated, like the email sender.
type SmsNotificationSender struct {
	Sent int
}

func (sms *SmsNotificationSender) SendConfirmation(confirmation Confirmation) error {
	sms.Sent++
	return nil
}

// QueuedNotificationSender takes confirmations off the request and gives them to a background
// worker.
//
// It keeps the NotificationSender contract, so BookingService cannot tell the difference: the
// booking hands over a Confirmation and carries on. What changes is when the email is sent, and what
// happens when there are more of them than the worker can keep up with.
//
// The channel is buffered, which is the bound, and the send is a select with a default branch, which
// is what makes a full queue refuse instead of wait. A refused confirmation is not a failed booking
// - the room is already the guest's - but it is not free either, and Rejected is what makes that
// cost visible instead of invisible.
type QueuedNotificationSender struct {
	sender    NotificationSender
	jobs      chan Confirmation
	stopping  chan struct{}
	done      sync.WaitGroup
	accepted  atomic.Int64
	rejected  atomic.Int64
	completed atomic.Int64
	closeOnce sync.Once
}

func NewQueuedNotificationSender(sender NotificationSender, capacity, workers int) *QueuedNotificationSender {
	queue := &QueuedNotificationSender{
		sender:   sender,
		jobs:     make(chan Confirmation, capacity),
		stopping: make(chan struct{}),
	}
	for worker := 0; worker < workers; worker++ {
		queue.done.Add(1)
		go queue.drain()
	}
	return queue
}

// SendConfirmation never waits and never fails: the whole point is that the booking is not held up.
func (queue *QueuedNotificationSender) SendConfirmation(confirmation Confirmation) error {
	select {
	case queue.jobs <- confirmation:
		queue.accepted.Add(1)
	default:
		queue.rejected.Add(1)
	}
	return nil
}

func (queue *QueuedNotificationSender) Counts() Counts {
	return Counts{
		Accepted:  queue.accepted.Load(),
		Rejected:  queue.rejected.Load(),
		Completed: queue.completed.Load(),
		Queued:    len(queue.jobs),
	}
}

// Close stops the workers. With drain the queue is emptied first, so a polite shutdown owes nobody
// an email; without it, whatever is still queued dies with the process. Nothing here helps with a
// process that is killed rather than asked to stop: see docs/queue-trace.md.
func (queue *QueuedNotificationSender) Close(drain bool) {
	queue.closeOnce.Do(func() {
		if drain {
			for len(queue.jobs) > 0 {
				time.Sleep(10 * time.Millisecond)
			}
		}
		close(queue.stopping)
		queue.done.Wait()
	})
}

func (queue *QueuedNotificationSender) drain() {
	defer queue.done.Done()
	for {
		select {
		case <-queue.stopping:
			return
		case confirmation := <-queue.jobs:
			if err := queue.sender.SendConfirmation(confirmation); err != nil {
				// A confirmation that could not be sent is lost, and saying so is the point: it
				// stays counted as accepted but never completed, so Accepted != Completed + Queued,
				// which is exactly the signal that work went missing.
				continue
			}
			queue.completed.Add(1)
		}
	}
}
```

JavaScript:

```text
// src/notifications/internal/email.js
// Simulated: counts the messages it would send and needs no network.
//
// sendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which
// is how a booking that does well under a millisecond of work becomes a one-second request.
const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

export class EmailNotificationSender {
  sent = 0;

  constructor(sendSeconds = 0) {
    this.sendSeconds = sendSeconds;
  }

  async sendConfirmation(confirmation) {
    if (this.sendSeconds) await wait(this.sendSeconds * 1000);
    this.sent += 1;
  }
}
```

Dart:

```text
// lib/notifications/src/email.dart
import '../notifications.dart';

// Simulated: counts the emails it would send and needs no network.
//
// sendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which
// is how a booking that does well under a millisecond of work becomes a one-second request.
class EmailNotificationSender implements NotificationSender {
  EmailNotificationSender({this.sendSeconds = 0});

  final double sendSeconds;
  int sent = 0;

  @override
  Future<void> sendConfirmation(Confirmation confirmation) async {
    if (sendSeconds > 0) {
      await Future<void>.delayed(Duration(milliseconds: (sendSeconds * 1000).round()));
    }
    sent++;
  }
}
```

### The email sender

#### C#

```csharp
// HotelBooking/Notifications/Internal/EmailNotificationSender.cs
namespace HotelBooking.Notifications.Internal;

// Simulated: counts the emails it would send and needs no network.
//
// sendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which
// is how a booking that does well under a millisecond of work becomes a one-second request.
public class EmailNotificationSender(double sendSeconds = 0) : INotificationSender
{
    private int _sent;

    public int Sent => Volatile.Read(ref _sent);

    public void SendConfirmation(Confirmation confirmation)
    {
        if (sendSeconds > 0) Thread.Sleep(TimeSpan.FromSeconds(sendSeconds));
        Interlocked.Increment(ref _sent);
    }
}
```

#### Python

```python
# hotel_booking/notifications/internal.py
from __future__ import annotations

import queue
import threading

from hotel_booking.notifications import Confirmation, NotificationCounts, NotificationSender


class EmailNotificationSender:
    """Simulated: counts the emails it would send and needs no network.

    send_seconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second,
    which is how a 4 ms booking becomes a one-second request.
    """

    def __init__(self, send_seconds: float = 0) -> None:
        self.sent = 0
        self._send_seconds = send_seconds
        self._lock = threading.Lock()

    def send_confirmation(self, confirmation: Confirmation) -> None:
        if self._send_seconds:
            threading.Event().wait(self._send_seconds)
        with self._lock:
            self.sent += 1


class SmsNotificationSender:
    """Simulated, like the email sender."""

    def __init__(self) -> None:
        self.sent = 0

    def send_confirmation(self, confirmation: Confirmation) -> None:
        self.sent += 1


class QueuedNotificationSender:
    """Takes confirmations off the request and gives them to a background worker.

    It keeps the NotificationSender promise, so BookingService cannot tell the difference: the
    booking hands over a Confirmation and carries on. What changes is when the email is sent, and
    what happens when there are more of them than the worker can keep up with.

    The queue is bounded. When it is full a confirmation is refused and counted rather than waited
    for, because making the booking wait is the thing this class exists to stop. A refused
    confirmation is not a failed booking - the room is already the guest's - but it is not free
    either, and the rejected counter is what makes the cost visible instead of invisible.
    """

    def __init__(self, sender: NotificationSender, capacity: int = 10, workers: int = 1) -> None:
        self._sender = sender
        self._queue: queue.Queue[Confirmation] = queue.Queue(maxsize=capacity)
        self._lock = threading.Lock()
        self._accepted = 0
        self._rejected = 0
        self._completed = 0
        self._stopping = threading.Event()
        self._workers = [
            threading.Thread(target=self._drain, name=f"notifications-{number}", daemon=True)
            for number in range(workers)
        ]
        for worker in self._workers:
            worker.start()

    def send_confirmation(self, confirmation: Confirmation) -> None:
        """Put the confirmation on the queue, or count it as refused. Never waits, never raises."""
        try:
            self._queue.put_nowait(confirmation)
        except queue.Full:
            with self._lock:
                self._rejected += 1
            return
        with self._lock:
            self._accepted += 1

    def counts(self) -> NotificationCounts:
        with self._lock:
            return NotificationCounts(self._accepted, self._rejected, self._completed, self._queue.qsize())

    def close(self, drain: bool = True) -> None:
        """Stop the workers. With drain=True the queue is emptied first, so a clean shutdown owes
        nobody an email; without it, whatever is still queued dies with the process."""
        if drain:
            self._queue.join()
        self._stopping.set()
        for worker in self._workers:
            worker.join(timeout=5)

    def _drain(self) -> None:
        while not self._stopping.is_set():
            try:
                confirmation = self._queue.get(timeout=0.05)
            except queue.Empty:
                continue
            try:
                self._sender.send_confirmation(confirmation)
                with self._lock:
                    self._completed += 1
            except Exception:
                # A confirmation that could not be sent is lost, and saying so is the point:
                # it stays counted as accepted but never completed, so accepted != completed
                # + queued, which is exactly the signal that work went missing.
                pass
            finally:
                self._queue.task_done()
```

#### Java

```java
// src/main/java/hotelbooking/notifications/internal/EmailNotificationSender.java
package hotelbooking.notifications.internal;

import hotelbooking.notifications.Confirmation;
import hotelbooking.notifications.NotificationSender;
import java.util.concurrent.atomic.AtomicInteger;

// Simulated: counts the emails it would send and needs no network.
//
// sendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which
// is how a booking that does well under a millisecond of work becomes a one-second request.
public class EmailNotificationSender implements NotificationSender {
    private final double sendSeconds;
    private final AtomicInteger sent = new AtomicInteger();

    public EmailNotificationSender() {
        this(0);
    }

    public EmailNotificationSender(double sendSeconds) {
        this.sendSeconds = sendSeconds;
    }

    public int sent() {
        return sent.get();
    }

    @Override
    public void sendConfirmation(Confirmation confirmation) {
        if (sendSeconds > 0) {
            try {
                Thread.sleep((long) (sendSeconds * 1000));
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        sent.incrementAndGet();
    }
}
```

#### Go

```go
// notifications/internal.go
package notifications

import (
	"sync"
	"sync/atomic"
	"time"
)

// EmailNotificationSender is simulated: it counts the emails it would send and needs no network.
//
// SendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which is
// how a booking that does well under a millisecond of work becomes a one-second request.
type EmailNotificationSender struct {
	SendSeconds float64
	sent        atomic.Int64
}

func (email *EmailNotificationSender) SendConfirmation(confirmation Confirmation) error {
	if email.SendSeconds > 0 {
		time.Sleep(time.Duration(email.SendSeconds * float64(time.Second)))
	}
	email.sent.Add(1)
	return nil
}

// Sent is how many emails this sender has actually sent.
func (email *EmailNotificationSender) Sent() int64 {
	return email.sent.Load()
}

// SmsNotificationSender is simulated, like the email sender.
type SmsNotificationSender struct {
	Sent int
}

func (sms *SmsNotificationSender) SendConfirmation(confirmation Confirmation) error {
	sms.Sent++
	return nil
}

// QueuedNotificationSender takes confirmations off the request and gives them to a background
// worker.
//
// It keeps the NotificationSender contract, so BookingService cannot tell the difference: the
// booking hands over a Confirmation and carries on. What changes is when the email is sent, and what
// happens when there are more of them than the worker can keep up with.
//
// The channel is buffered, which is the bound, and the send is a select with a default branch, which
// is what makes a full queue refuse instead of wait. A refused confirmation is not a failed booking
// - the room is already the guest's - but it is not free either, and Rejected is what makes that
// cost visible instead of invisible.
type QueuedNotificationSender struct {
	sender    NotificationSender
	jobs      chan Confirmation
	stopping  chan struct{}
	done      sync.WaitGroup
	accepted  atomic.Int64
	rejected  atomic.Int64
	completed atomic.Int64
	closeOnce sync.Once
}

func NewQueuedNotificationSender(sender NotificationSender, capacity, workers int) *QueuedNotificationSender {
	queue := &QueuedNotificationSender{
		sender:   sender,
		jobs:     make(chan Confirmation, capacity),
		stopping: make(chan struct{}),
	}
	for worker := 0; worker < workers; worker++ {
		queue.done.Add(1)
		go queue.drain()
	}
	return queue
}

// SendConfirmation never waits and never fails: the whole point is that the booking is not held up.
func (queue *QueuedNotificationSender) SendConfirmation(confirmation Confirmation) error {
	select {
	case queue.jobs <- confirmation:
		queue.accepted.Add(1)
	default:
		queue.rejected.Add(1)
	}
	return nil
}

func (queue *QueuedNotificationSender) Counts() Counts {
	return Counts{
		Accepted:  queue.accepted.Load(),
		Rejected:  queue.rejected.Load(),
		Completed: queue.completed.Load(),
		Queued:    len(queue.jobs),
	}
}

// Close stops the workers. With drain the queue is emptied first, so a polite shutdown owes nobody
// an email; without it, whatever is still queued dies with the process. Nothing here helps with a
// process that is killed rather than asked to stop: see docs/queue-trace.md.
func (queue *QueuedNotificationSender) Close(drain bool) {
	queue.closeOnce.Do(func() {
		if drain {
			for len(queue.jobs) > 0 {
				time.Sleep(10 * time.Millisecond)
			}
		}
		close(queue.stopping)
		queue.done.Wait()
	})
}

func (queue *QueuedNotificationSender) drain() {
	defer queue.done.Done()
	for {
		select {
		case <-queue.stopping:
			return
		case confirmation := <-queue.jobs:
			if err := queue.sender.SendConfirmation(confirmation); err != nil {
				// A confirmation that could not be sent is lost, and saying so is the point: it
				// stays counted as accepted but never completed, so Accepted != Completed + Queued,
				// which is exactly the signal that work went missing.
				continue
			}
			queue.completed.Add(1)
		}
	}
}
```

#### JavaScript

```javascript
// src/notifications/internal/email.js
// Simulated: counts the messages it would send and needs no network.
//
// sendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which
// is how a booking that does well under a millisecond of work becomes a one-second request.
const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

export class EmailNotificationSender {
  sent = 0;

  constructor(sendSeconds = 0) {
    this.sendSeconds = sendSeconds;
  }

  async sendConfirmation(confirmation) {
    if (this.sendSeconds) await wait(this.sendSeconds * 1000);
    this.sent += 1;
  }
}
```

#### Dart

```dart
// lib/notifications/src/email.dart
import '../notifications.dart';

// Simulated: counts the emails it would send and needs no network.
//
// sendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which
// is how a booking that does well under a millisecond of work becomes a one-second request.
class EmailNotificationSender implements NotificationSender {
  EmailNotificationSender({this.sendSeconds = 0});

  final double sendSeconds;
  int sent = 0;

  @override
  Future<void> sendConfirmation(Confirmation confirmation) async {
    if (sendSeconds > 0) {
      await Future<void>.delayed(Duration(milliseconds: (sendSeconds * 1000).round()));
    }
    sent++;
  }
}
```

### Completed example 1.2 · the burst tool

The burst sends 30 bookings as fast as it can: rooms R7 and R8, one night each,
1 to 15 December. Every one of them is a legal booking - no two share a room and a night - so
nothing is refused by the booking rule and everything that happens to them happens in the queue.

**What the burst tool does** In short: It sends thirty bookings as fast as it can and then reads GET /stats, so you can see what the queue did with them. The burst sends 30 bookings as fast as it can: rooms R7 and R8, one night each,
1 to 15 December. Every one of them is a legal booking - no two share a room and a night - so
nothing is refused by the booking rule and everything that happens to them happens in the queue. Everything it reports as refused was refused by the notification queue, not by the booking rule. That separation is the whole reason the dates are chosen the way they are.

1. Start: the app is running with HOTEL_EMAIL_SECONDS=1 and a queue that holds ten.
2. Action: you run the burst tool.
3. Result:
Sending 30 bookings, one after another...

Accepted: 30
Rejected: 0
Slowest booking: 47 ms
Total: 0.1 s

GET /stats
{"notificationsAccepted":11,"notificationsRejected":19,"notificationsCompleted":0,"notificationsQueued":10}

Read the two lines together. Every booking was accepted - all 30 guests have their rooms - and
19 confirmation emails were refused on purpose, because the queue was full. The booking and the
email now succeed and fail separately, which is the whole point.

C#:

```text
// tools/burst.cs
// Sends a burst of bookings as fast as it can, then reads GET /stats.
// 
// Every booking is for a different room and night - R7 and R8, one night each, 1 to 15 December - so
// all 30 are legal under P09's exclusion constraint and nothing is refused by the booking rule.
// Whatever is refused here was refused by the notification queue, which is the thing being measured.
// 
// Start the app with HOTEL_EMAIL_SECONDS=1 first, so the confirmation sender is as slow as a real
// mail server.
//
//   dotnet run tools/burst.cs
//
// A file-based app (dotnet run <file>.cs) is compiled ready for ahead-of-time publishing, and that
// turns off the reflection the typed JSON helpers below need - PostAsJsonAsync would throw
// "Reflection-based serialization has been disabled for this application". This tool is run, never
// published, so the AOT settings do nothing for it and are turned off.
#:property PublishAot=false
using System.Net.Http.Json;
using System.Diagnostics;

const string Base = "http://localhost:5080";
const int Burst = 30;

using var client = new HttpClient();
Console.WriteLine($"Sending {Burst} bookings, one after another...");
Console.WriteLine();

var accepted = 0;
var rejected = 0;
double slowest = 0;
var total = Stopwatch.StartNew();
for (var index = 0; index < Burst; index++)
{
    var night = 1 + index / 2;
    var room = index % 2 == 0 ? "R7" : "R8";
    var body = new
    {
        guestName = $"Guest{night}{room}", guestEmail = $"guest{night}{room}@example.com",
        room, checkIn = $"2026-12-{night:00}", checkOut = $"2026-12-{night + 1:00}",
    };
    var one = Stopwatch.StartNew();
    var response = await client.PostAsJsonAsync($"{Base}/reservations", body);
    one.Stop();
    slowest = Math.Max(slowest, one.Elapsed.TotalMilliseconds);
    if ((int)response.StatusCode == 201) accepted++; else rejected++;
}
total.Stop();

Console.WriteLine($"Accepted: {accepted}");
Console.WriteLine($"Rejected: {rejected}");
Console.WriteLine($"Slowest booking: {slowest:0} ms");
Console.WriteLine($"Total: {total.Elapsed.TotalSeconds:0.0} s");
Console.WriteLine();
Console.WriteLine("GET /stats");
Console.WriteLine(await client.GetStringAsync($"{Base}/stats"));
```

Python:

```text
# tools/burst.py
"""Sends a burst of bookings as fast as it can, then reads GET /stats.

  python tools/burst.py

Every booking is for a different room and night - R7 and R8, one night each, 1 to 15 December -
so all 30 are legal under P09's exclusion constraint and nothing is refused by the booking rule.
Whatever is refused here was refused by the notification queue, which is the thing being measured.

Start the app with HOTEL_EMAIL_SECONDS=1 first, so the confirmation sender is as slow as a real
mail server.
"""
from __future__ import annotations

import json
import time
import urllib.error
import urllib.request

BASE = "http://localhost:5080"
BURST = 30


def book(night: int, room: str) -> tuple[int, float]:
    body = {
        "guestName": f"Guest{night}{room}", "guestEmail": f"guest{night}{room}@example.com",
        "room": room, "checkIn": f"2026-12-{night:02d}", "checkOut": f"2026-12-{night + 1:02d}",
    }
    request = urllib.request.Request(f"{BASE}/reservations", method="POST",
                                     data=json.dumps(body).encode(),
                                     headers={"Content-Type": "application/json"})
    started = time.monotonic()
    try:
        with urllib.request.urlopen(request) as response:
            response.read()
            return response.status, time.monotonic() - started
    except urllib.error.HTTPError as error:
        error.read()
        return error.code, time.monotonic() - started


def main() -> None:
    print(f"Sending {BURST} bookings, one after another...")
    print()
    accepted = rejected = 0
    slowest = 0.0
    started = time.monotonic()
    for index in range(BURST):
        night, room = 1 + index // 2, "R7" if index % 2 == 0 else "R8"
        status, seconds = book(night, room)
        slowest = max(slowest, seconds)
        if status == 201:
            accepted += 1
        else:
            rejected += 1
    total = time.monotonic() - started

    print(f"Accepted: {accepted}")
    print(f"Rejected: {rejected}")
    print(f"Slowest booking: {slowest * 1000:.0f} ms" if slowest < 1 else f"Slowest booking: {slowest:.2f} s")
    print(f"Total: {total:.1f} s")
    print()
    with urllib.request.urlopen(f"{BASE}/stats") as response:
        print("GET /stats")
        print(response.read().decode())


if __name__ == "__main__":
    main()
```

Java:

```text
// tools/Burst.java
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
                    {"guestName":"Guest%1$d%2$s","guestEmail":"guest%1$d%2$s@example.com","room":"%2$s",                    "checkIn":"2026-12-%1$02d","checkOut":"2026-12-%3$02d"}"""
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
```

Go:

```text
// tools/burst/main.go
// Sends a burst of bookings as fast as it can, then reads GET /stats.
// 
// Every booking is for a different room and night - R7 and R8, one night each, 1 to 15 December - so
// all 30 are legal under P09's exclusion constraint and nothing is refused by the booking rule.
// Whatever is refused here was refused by the notification queue, which is the thing being measured.
// 
// Start the app with HOTEL_EMAIL_SECONDS=1 first, so the confirmation sender is as slow as a real
// mail server.
//
//	go run ./tools/burst
package main

import (
	"fmt"
	"io"
	"net/http"
	"strings"
	"time"
)

const base = "http://localhost:5080"
const burst = 30

func main() {
	fmt.Printf("Sending %d bookings, one after another...\n", burst)
	fmt.Println()

	accepted, rejected := 0, 0
	var slowest time.Duration
	started := time.Now()
	for index := 0; index < burst; index++ {
		night := 1 + index/2
		room := "R7"
		if index%2 == 1 {
			room = "R8"
		}
		body := fmt.Sprintf(`{"guestName":"Guest%d%s","guestEmail":"guest%d%s@example.com","room":"%s","checkIn":"2026-12-%02d","checkOut":"2026-12-%02d"}`,
			night, room, night, room, room, night, night+1)
		one := time.Now()
		response, err := http.Post(base+"/reservations", "application/json", strings.NewReader(body))
		if err != nil {
			fmt.Printf("the request failed: %v\n", err)
			return
		}
		io.Copy(io.Discard, response.Body)
		response.Body.Close()
		if elapsed := time.Since(one); elapsed > slowest {
			slowest = elapsed
		}
		if response.StatusCode == http.StatusCreated {
			accepted++
		} else {
			rejected++
		}
	}
	total := time.Since(started)

	fmt.Printf("Accepted: %d\n", accepted)
	fmt.Printf("Rejected: %d\n", rejected)
	fmt.Printf("Slowest booking: %d ms\n", slowest.Milliseconds())
	fmt.Printf("Total: %.1f s\n", total.Seconds())
	fmt.Println()
	fmt.Println("GET /stats")
	stats, err := http.Get(base + "/stats")
	if err != nil {
		fmt.Printf("could not read /stats: %v\n", err)
		return
	}
	defer stats.Body.Close()
	text, _ := io.ReadAll(stats.Body)
	fmt.Println(string(text))
}
```

JavaScript:

```text
// tools/burst.js
// Sends a burst of bookings as fast as it can, then reads GET /stats.
//
//   node tools/burst.js
//
// Every booking is for a different room and night - R7 and R8, one night each, 1 to 15 December -
// so all 30 are legal under P09's exclusion constraint and nothing is refused by the booking rule.
// Whatever is refused here was refused by the notification queue, which is the thing being measured.
//
// Start the app with HOTEL_EMAIL_SECONDS=1 first, so the confirmation sender is as slow as a real
// mail server.
const BASE = 'http://localhost:5080';
const BURST = 30;

async function book(night, room) {
  const body = {
    guestName: `Guest${night}${room}`, guestEmail: `guest${night}${room}@example.com`,
    room, checkIn: `2026-12-${String(night).padStart(2, '0')}`,
    checkOut: `2026-12-${String(night + 1).padStart(2, '0')}`,
  };
  const started = performance.now();
  const response = await fetch(`${BASE}/reservations`, {
    method: 'POST', body: JSON.stringify(body),
    headers: { 'Content-Type': 'application/json', Connection: 'close' },
  });
  await response.text();
  return { status: response.status, ms: performance.now() - started };
}

console.log(`Sending ${BURST} bookings, one after another...`);
console.log();

let accepted = 0; let rejected = 0; let slowest = 0;
const started = performance.now();
for (let index = 0; index < BURST; index += 1) {
  const night = 1 + Math.floor(index / 2);
  const room = index % 2 === 0 ? 'R7' : 'R8';
  const { status, ms } = await book(night, room);
  slowest = Math.max(slowest, ms);
  if (status === 201) accepted += 1; else rejected += 1;
}
const total = (performance.now() - started) / 1000;

console.log(`Accepted: ${accepted}`);
console.log(`Rejected: ${rejected}`);
console.log(`Slowest booking: ${slowest.toFixed(0)} ms`);
console.log(`Total: ${total.toFixed(1)} s`);
console.log();
console.log('GET /stats');
console.log(await (await fetch(`${BASE}/stats`)).text());
```

Dart:

```text
// tool/burst.dart
// Sends a burst of bookings as fast as it can, then reads GET /stats.
// 
// Every booking is for a different room and night - R7 and R8, one night each, 1 to 15 December - so
// all 30 are legal under P09's exclusion constraint and nothing is refused by the booking rule.
// Whatever is refused here was refused by the notification queue, which is the thing being measured.
// 
// Start the app with HOTEL_EMAIL_SECONDS=1 first, so the confirmation sender is as slow as a real
// mail server.
//
//   dart run tool/burst.dart
import 'dart:convert';
import 'dart:io';

const base = 'http://localhost:5080';
const burst = 30;

Future<void> main() async {
  final client = HttpClient();
  print('Sending $burst bookings, one after another...');
  print('');

  var accepted = 0;
  var rejected = 0;
  var slowest = 0;
  final started = DateTime.now();
  for (var index = 0; index < burst; index++) {
    final night = 1 + index ~/ 2;
    final room = index % 2 == 0 ? 'R7' : 'R8';
    final body = {
      'guestName': 'Guest$night$room',
      'guestEmail': 'guest$night$room@example.com',
      'room': room,
      'checkIn': '2026-12-${night.toString().padLeft(2, '0')}',
      'checkOut': '2026-12-${(night + 1).toString().padLeft(2, '0')}',
    };
    final one = DateTime.now();
    final request = await client.postUrl(Uri.parse('$base/reservations'));
    request.headers.contentType = ContentType.json;
    request.write(jsonEncode(body));
    final response = await request.close();
    await response.drain<void>();
    final elapsed = DateTime.now().difference(one).inMilliseconds;
    if (elapsed > slowest) slowest = elapsed;
    if (response.statusCode == 201) {
      accepted++;
    } else {
      rejected++;
    }
  }
  final total = DateTime.now().difference(started).inMilliseconds / 1000;

  print('Accepted: $accepted');
  print('Rejected: $rejected');
  print('Slowest booking: $slowest ms');
  print('Total: ${total.toStringAsFixed(1)} s');
  print('');
  print('GET /stats');
  final stats = await (await client.getUrl(Uri.parse('$base/stats'))).close();
  print(await stats.transform(utf8.decoder).join());
  client.close();
}
```

### The burst tool

#### C#

```csharp
// tools/burst.cs
// Sends a burst of bookings as fast as it can, then reads GET /stats.
// 
// Every booking is for a different room and night - R7 and R8, one night each, 1 to 15 December - so
// all 30 are legal under P09's exclusion constraint and nothing is refused by the booking rule.
// Whatever is refused here was refused by the notification queue, which is the thing being measured.
// 
// Start the app with HOTEL_EMAIL_SECONDS=1 first, so the confirmation sender is as slow as a real
// mail server.
//
//   dotnet run tools/burst.cs
//
// A file-based app (dotnet run <file>.cs) is compiled ready for ahead-of-time publishing, and that
// turns off the reflection the typed JSON helpers below need - PostAsJsonAsync would throw
// "Reflection-based serialization has been disabled for this application". This tool is run, never
// published, so the AOT settings do nothing for it and are turned off.
#:property PublishAot=false
using System.Net.Http.Json;
using System.Diagnostics;

const string Base = "http://localhost:5080";
const int Burst = 30;

using var client = new HttpClient();
Console.WriteLine($"Sending {Burst} bookings, one after another...");
Console.WriteLine();

var accepted = 0;
var rejected = 0;
double slowest = 0;
var total = Stopwatch.StartNew();
for (var index = 0; index < Burst; index++)
{
    var night = 1 + index / 2;
    var room = index % 2 == 0 ? "R7" : "R8";
    var body = new
    {
        guestName = $"Guest{night}{room}", guestEmail = $"guest{night}{room}@example.com",
        room, checkIn = $"2026-12-{night:00}", checkOut = $"2026-12-{night + 1:00}",
    };
    var one = Stopwatch.StartNew();
    var response = await client.PostAsJsonAsync($"{Base}/reservations", body);
    one.Stop();
    slowest = Math.Max(slowest, one.Elapsed.TotalMilliseconds);
    if ((int)response.StatusCode == 201) accepted++; else rejected++;
}
total.Stop();

Console.WriteLine($"Accepted: {accepted}");
Console.WriteLine($"Rejected: {rejected}");
Console.WriteLine($"Slowest booking: {slowest:0} ms");
Console.WriteLine($"Total: {total.Elapsed.TotalSeconds:0.0} s");
Console.WriteLine();
Console.WriteLine("GET /stats");
Console.WriteLine(await client.GetStringAsync($"{Base}/stats"));
```

#### Python

```python
# tools/burst.py
"""Sends a burst of bookings as fast as it can, then reads GET /stats.

  python tools/burst.py

Every booking is for a different room and night - R7 and R8, one night each, 1 to 15 December -
so all 30 are legal under P09's exclusion constraint and nothing is refused by the booking rule.
Whatever is refused here was refused by the notification queue, which is the thing being measured.

Start the app with HOTEL_EMAIL_SECONDS=1 first, so the confirmation sender is as slow as a real
mail server.
"""
from __future__ import annotations

import json
import time
import urllib.error
import urllib.request

BASE = "http://localhost:5080"
BURST = 30


def book(night: int, room: str) -> tuple[int, float]:
    body = {
        "guestName": f"Guest{night}{room}", "guestEmail": f"guest{night}{room}@example.com",
        "room": room, "checkIn": f"2026-12-{night:02d}", "checkOut": f"2026-12-{night + 1:02d}",
    }
    request = urllib.request.Request(f"{BASE}/reservations", method="POST",
                                     data=json.dumps(body).encode(),
                                     headers={"Content-Type": "application/json"})
    started = time.monotonic()
    try:
        with urllib.request.urlopen(request) as response:
            response.read()
            return response.status, time.monotonic() - started
    except urllib.error.HTTPError as error:
        error.read()
        return error.code, time.monotonic() - started


def main() -> None:
    print(f"Sending {BURST} bookings, one after another...")
    print()
    accepted = rejected = 0
    slowest = 0.0
    started = time.monotonic()
    for index in range(BURST):
        night, room = 1 + index // 2, "R7" if index % 2 == 0 else "R8"
        status, seconds = book(night, room)
        slowest = max(slowest, seconds)
        if status == 201:
            accepted += 1
        else:
            rejected += 1
    total = time.monotonic() - started

    print(f"Accepted: {accepted}")
    print(f"Rejected: {rejected}")
    print(f"Slowest booking: {slowest * 1000:.0f} ms" if slowest < 1 else f"Slowest booking: {slowest:.2f} s")
    print(f"Total: {total:.1f} s")
    print()
    with urllib.request.urlopen(f"{BASE}/stats") as response:
        print("GET /stats")
        print(response.read().decode())


if __name__ == "__main__":
    main()
```

#### Java

```java
// tools/Burst.java
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
                    {"guestName":"Guest%1$d%2$s","guestEmail":"guest%1$d%2$s@example.com","room":"%2$s",                    "checkIn":"2026-12-%1$02d","checkOut":"2026-12-%3$02d"}"""
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
```

#### Go

```go
// tools/burst/main.go
// Sends a burst of bookings as fast as it can, then reads GET /stats.
// 
// Every booking is for a different room and night - R7 and R8, one night each, 1 to 15 December - so
// all 30 are legal under P09's exclusion constraint and nothing is refused by the booking rule.
// Whatever is refused here was refused by the notification queue, which is the thing being measured.
// 
// Start the app with HOTEL_EMAIL_SECONDS=1 first, so the confirmation sender is as slow as a real
// mail server.
//
//	go run ./tools/burst
package main

import (
	"fmt"
	"io"
	"net/http"
	"strings"
	"time"
)

const base = "http://localhost:5080"
const burst = 30

func main() {
	fmt.Printf("Sending %d bookings, one after another...\n", burst)
	fmt.Println()

	accepted, rejected := 0, 0
	var slowest time.Duration
	started := time.Now()
	for index := 0; index < burst; index++ {
		night := 1 + index/2
		room := "R7"
		if index%2 == 1 {
			room = "R8"
		}
		body := fmt.Sprintf(`{"guestName":"Guest%d%s","guestEmail":"guest%d%s@example.com","room":"%s","checkIn":"2026-12-%02d","checkOut":"2026-12-%02d"}`,
			night, room, night, room, room, night, night+1)
		one := time.Now()
		response, err := http.Post(base+"/reservations", "application/json", strings.NewReader(body))
		if err != nil {
			fmt.Printf("the request failed: %v\n", err)
			return
		}
		io.Copy(io.Discard, response.Body)
		response.Body.Close()
		if elapsed := time.Since(one); elapsed > slowest {
			slowest = elapsed
		}
		if response.StatusCode == http.StatusCreated {
			accepted++
		} else {
			rejected++
		}
	}
	total := time.Since(started)

	fmt.Printf("Accepted: %d\n", accepted)
	fmt.Printf("Rejected: %d\n", rejected)
	fmt.Printf("Slowest booking: %d ms\n", slowest.Milliseconds())
	fmt.Printf("Total: %.1f s\n", total.Seconds())
	fmt.Println()
	fmt.Println("GET /stats")
	stats, err := http.Get(base + "/stats")
	if err != nil {
		fmt.Printf("could not read /stats: %v\n", err)
		return
	}
	defer stats.Body.Close()
	text, _ := io.ReadAll(stats.Body)
	fmt.Println(string(text))
}
```

#### JavaScript

```javascript
// tools/burst.js
// Sends a burst of bookings as fast as it can, then reads GET /stats.
//
//   node tools/burst.js
//
// Every booking is for a different room and night - R7 and R8, one night each, 1 to 15 December -
// so all 30 are legal under P09's exclusion constraint and nothing is refused by the booking rule.
// Whatever is refused here was refused by the notification queue, which is the thing being measured.
//
// Start the app with HOTEL_EMAIL_SECONDS=1 first, so the confirmation sender is as slow as a real
// mail server.
const BASE = 'http://localhost:5080';
const BURST = 30;

async function book(night, room) {
  const body = {
    guestName: `Guest${night}${room}`, guestEmail: `guest${night}${room}@example.com`,
    room, checkIn: `2026-12-${String(night).padStart(2, '0')}`,
    checkOut: `2026-12-${String(night + 1).padStart(2, '0')}`,
  };
  const started = performance.now();
  const response = await fetch(`${BASE}/reservations`, {
    method: 'POST', body: JSON.stringify(body),
    headers: { 'Content-Type': 'application/json', Connection: 'close' },
  });
  await response.text();
  return { status: response.status, ms: performance.now() - started };
}

console.log(`Sending ${BURST} bookings, one after another...`);
console.log();

let accepted = 0; let rejected = 0; let slowest = 0;
const started = performance.now();
for (let index = 0; index < BURST; index += 1) {
  const night = 1 + Math.floor(index / 2);
  const room = index % 2 === 0 ? 'R7' : 'R8';
  const { status, ms } = await book(night, room);
  slowest = Math.max(slowest, ms);
  if (status === 201) accepted += 1; else rejected += 1;
}
const total = (performance.now() - started) / 1000;

console.log(`Accepted: ${accepted}`);
console.log(`Rejected: ${rejected}`);
console.log(`Slowest booking: ${slowest.toFixed(0)} ms`);
console.log(`Total: ${total.toFixed(1)} s`);
console.log();
console.log('GET /stats');
console.log(await (await fetch(`${BASE}/stats`)).text());
```

#### Dart

```dart
// tool/burst.dart
// Sends a burst of bookings as fast as it can, then reads GET /stats.
// 
// Every booking is for a different room and night - R7 and R8, one night each, 1 to 15 December - so
// all 30 are legal under P09's exclusion constraint and nothing is refused by the booking rule.
// Whatever is refused here was refused by the notification queue, which is the thing being measured.
// 
// Start the app with HOTEL_EMAIL_SECONDS=1 first, so the confirmation sender is as slow as a real
// mail server.
//
//   dart run tool/burst.dart
import 'dart:convert';
import 'dart:io';

const base = 'http://localhost:5080';
const burst = 30;

Future<void> main() async {
  final client = HttpClient();
  print('Sending $burst bookings, one after another...');
  print('');

  var accepted = 0;
  var rejected = 0;
  var slowest = 0;
  final started = DateTime.now();
  for (var index = 0; index < burst; index++) {
    final night = 1 + index ~/ 2;
    final room = index % 2 == 0 ? 'R7' : 'R8';
    final body = {
      'guestName': 'Guest$night$room',
      'guestEmail': 'guest$night$room@example.com',
      'room': room,
      'checkIn': '2026-12-${night.toString().padLeft(2, '0')}',
      'checkOut': '2026-12-${(night + 1).toString().padLeft(2, '0')}',
    };
    final one = DateTime.now();
    final request = await client.postUrl(Uri.parse('$base/reservations'));
    request.headers.contentType = ContentType.json;
    request.write(jsonEncode(body));
    final response = await request.close();
    await response.drain<void>();
    final elapsed = DateTime.now().difference(one).inMilliseconds;
    if (elapsed > slowest) slowest = elapsed;
    if (response.statusCode == 201) {
      accepted++;
    } else {
      rejected++;
    }
  }
  final total = DateTime.now().difference(started).inMilliseconds / 1000;

  print('Accepted: $accepted');
  print('Rejected: $rejected');
  print('Slowest booking: $slowest ms');
  print('Total: ${total.toStringAsFixed(1)} s');
  print('');
  print('GET /stats');
  final stats = await (await client.getUrl(Uri.parse('$base/stats'))).close();
  print(await stats.transform(utf8.decoder).join());
  client.close();
}
```

### Run it

Terminal 1 runs the app with a slow sender; terminal 2 runs the burst.

### Two terminals

#### C#

```text
# terminal 1
$env:HOTEL_EMAIL_SECONDS = "1"; dotnet run --project HotelBooking
# terminal 2
dotnet run tools/burst.cs
```

#### Python

```text
# terminal 1
$env:HOTEL_EMAIL_SECONDS = "1"; python main.py
# terminal 2
python tools/burst.py
```

#### Java

```text
# terminal 1
$env:HOTEL_EMAIL_SECONDS = "1"; mvn -q spring-boot:run
# terminal 2
java tools/Burst.java
```

#### Go

```text
# terminal 1
$env:HOTEL_EMAIL_SECONDS = "1"; go run .
# terminal 2
go run ./tools/burst
```

#### JavaScript

```text
# terminal 1
$env:HOTEL_EMAIL_SECONDS = "1"; node main.js
# terminal 2
node tools/burst.js
```

#### Dart

```text
# terminal 1
$env:HOTEL_EMAIL_SECONDS = "1"; dart run bin/main.dart
# terminal 2
dart run tool/burst.dart
```

### What one booking costs

### One booking

```text
BookingService.reserve(Maya, R7, 1-2 December)
  Accepted in 1.02 s

Of that second, the booking itself took under a millisecond. The rest was the confirmation email,
sent inside the request while the guest waited for a room that was already theirs.
```

### And what thirty cost

Thirty seconds to do about thirty milliseconds of work.

### Terminal 2

```text
Thirty bookings, one after another, with the email sent inside the request:

Accepted: 30
Rejected: 0
Slowest booking: 1.02 s
Total: 30.3 s

Thirty seconds to do about thirty milliseconds of work. Every guest waited a full second for a
room that was already theirs.
```

### Your turn · measure the cost

1. Add the sendSeconds parameter to the email sender (1.1), defaulting to zero.
2. Read HOTEL_EMAIL_SECONDS in your wiring file and pass it to the sender.
3. Run your checks: still 19 green. Nothing is repaired yet; the sender is only slower when you ask it to be.
4. Create the burst tool (1.2) in tools/ (Dart: tool/).
5. Start the app with HOTEL_EMAIL_SECONDS=1 in terminal 1, and run the burst in terminal 2.
6. Create docs/queue-trace.md with the heading # The confirmation email, taken out of the request, and paste your own burst output under “What the email was costing the guest”.

**When you finish, you have:** A slow sender, a burst tool, and your own 30-second measurement in docs/queue-trace.md.
### Explanation

The room is the guest’s the moment the reservation commits. Everything the request does after that is the app making them wait for a system that has no say in the answer they are waiting for.



---

## 3. Step 2 · Hand the email to a background worker

The booking writes the job onto a queue and answers. A worker sends the email afterwards, and nobody is waiting for it.





### What Step 2 adds

| What | Where | What it does |
| --- | --- | --- |
| A queued sender | Next to the email sender | Keeps the same sender contract, so BookingService cannot tell the difference |
| A background worker | Inside the queued sender | Takes one job at a time off the queue and sends it |
| The wiring | Your wiring file | Wraps the email sender in the queued sender and hands that to BookingService |

**The background worker** In short: A loop outside any request: take a job, send it, count it, repeat — with a way to be told to stop. Nothing is waiting for it. The request that produced the job has already answered, so the worker’s speed decides how late the email is, not whether the booking succeeded. The only subtlety is the timeout on taking a job. Without it the loop would block for ever on an empty queue and never notice it had been asked to stop.

1. Start: ten confirmations are on the queue and the sender takes a second each.
2. Action: one worker drains them.
3. Result: ten seconds later, completed is 10 and queued is 0.

C#:

```text
private async Task DrainAsync()
{
    try
    {
        await foreach (var confirmation in _queue.Reader.ReadAllAsync(_stopping.Token))
        {
            try
            {
                _sender.SendConfirmation(confirmation);
                Interlocked.Increment(ref _completed);
            }
            catch (Exception)
            {
                // A confirmation that could not be sent is lost, and saying so is the point: it
                // stays counted as accepted but never completed, so Accepted != Completed +
                // Queued, which is exactly the signal that work went missing.
            }
        }
    }
    catch (OperationCanceledException)
    {
        // Asked to stop without draining.
    }
}
```

Python:

```text
def _drain(self) -> None:
    while not self._stopping.is_set():
        try:
            confirmation = self._queue.get(timeout=0.05)
        except queue.Empty:
            continue
        try:
            self._sender.send_confirmation(confirmation)
            with self._lock:
                self._completed += 1
        except Exception:
            # A confirmation that could not be sent is lost, and saying so is the point:
            # it stays counted as accepted but never completed, so accepted != completed
            # + queued, which is exactly the signal that work went missing.
            pass
        finally:
            self._queue.task_done()
```

Java:

```text
private void drain() {
    while (!stopping) {
        Confirmation confirmation;
        try {
            confirmation = queue.poll(50, TimeUnit.MILLISECONDS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return;
        }
        if (confirmation == null) {
            continue;
        }
        try {
            sender.sendConfirmation(confirmation);
            completed.incrementAndGet();
        } catch (RuntimeException failure) {
            // A confirmation that could not be sent is lost, and saying so is the point: it
            // stays counted as accepted but never completed, so accepted != completed + queued,
            // which is exactly the signal that work went missing.
        }
    }
}
```

Go:

```text
func (queue *QueuedNotificationSender) drain() {
	defer queue.done.Done()
	for {
		select {
		case <-queue.stopping:
			return
		case confirmation := <-queue.jobs:
			if err := queue.sender.SendConfirmation(confirmation); err != nil {
				// A confirmation that could not be sent is lost, and saying so is the point: it
				// stays counted as accepted but never completed, so Accepted != Completed + Queued,
				// which is exactly the signal that work went missing.
				continue
			}
			queue.completed.Add(1)
		}
	}
}
```

JavaScript:

```text
async #drain() {
  while (!this.#stopping) {
    const confirmation = this.#jobs.shift();
    if (confirmation === undefined) {
      await wait(5);
      continue;
    }
    try {
      await this.#sender.sendConfirmation(confirmation);
      this.#completed += 1;
    } catch {
      // A confirmation that could not be sent is lost, and saying so is the point: it stays
      // counted as accepted but never completed, so accepted !== completed + queued, which is
      // exactly the signal that work went missing.
    }
  }
}
```

Dart:

```text
Future<void> _drain() async {
  while (!_stopping) {
    if (_jobs.isEmpty) {
      await Future<void>.delayed(const Duration(milliseconds: 5));
      continue;
    }
    final confirmation = _jobs.removeFirst();
    try {
      await _sender.sendConfirmation(confirmation);
      _completed++;
    } catch (failure) {
      // A confirmation that could not be sent is lost, and saying so is the point: it stays
      // counted as accepted but never completed, so accepted != completed + queued, which is
      // exactly the signal that work went missing.
    }
  }
}
```

**The background worker** In short: A loop outside any request: take a job, send it, count it, repeat — with a way to be told to stop. Nothing is waiting for it. The request that produced the job has already answered, so the worker’s speed decides how late the email is, not whether the booking succeeded. The only subtlety is the timeout on taking a job. Without it the loop would block for ever on an empty queue and never notice it had been asked to stop.

1. Start: ten confirmations are on the queue and the sender takes a second each.
2. Action: one worker drains them.
3. Result: ten seconds later, completed is 10 and queued is 0.

C#:

```text
private async Task DrainAsync()
{
    try
    {
        await foreach (var confirmation in _queue.Reader.ReadAllAsync(_stopping.Token))
        {
            try
            {
                _sender.SendConfirmation(confirmation);
                Interlocked.Increment(ref _completed);
            }
            catch (Exception)
            {
                // A confirmation that could not be sent is lost, and saying so is the point: it
                // stays counted as accepted but never completed, so Accepted != Completed +
                // Queued, which is exactly the signal that work went missing.
            }
        }
    }
    catch (OperationCanceledException)
    {
        // Asked to stop without draining.
    }
}
```

Python:

```text
def _drain(self) -> None:
    while not self._stopping.is_set():
        try:
            confirmation = self._queue.get(timeout=0.05)
        except queue.Empty:
            continue
        try:
            self._sender.send_confirmation(confirmation)
            with self._lock:
                self._completed += 1
        except Exception:
            # A confirmation that could not be sent is lost, and saying so is the point:
            # it stays counted as accepted but never completed, so accepted != completed
            # + queued, which is exactly the signal that work went missing.
            pass
        finally:
            self._queue.task_done()
```

Java:

```text
private void drain() {
    while (!stopping) {
        Confirmation confirmation;
        try {
            confirmation = queue.poll(50, TimeUnit.MILLISECONDS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return;
        }
        if (confirmation == null) {
            continue;
        }
        try {
            sender.sendConfirmation(confirmation);
            completed.incrementAndGet();
        } catch (RuntimeException failure) {
            // A confirmation that could not be sent is lost, and saying so is the point: it
            // stays counted as accepted but never completed, so accepted != completed + queued,
            // which is exactly the signal that work went missing.
        }
    }
}
```

Go:

```text
func (queue *QueuedNotificationSender) drain() {
	defer queue.done.Done()
	for {
		select {
		case <-queue.stopping:
			return
		case confirmation := <-queue.jobs:
			if err := queue.sender.SendConfirmation(confirmation); err != nil {
				// A confirmation that could not be sent is lost, and saying so is the point: it
				// stays counted as accepted but never completed, so Accepted != Completed + Queued,
				// which is exactly the signal that work went missing.
				continue
			}
			queue.completed.Add(1)
		}
	}
}
```

JavaScript:

```text
async #drain() {
  while (!this.#stopping) {
    const confirmation = this.#jobs.shift();
    if (confirmation === undefined) {
      await wait(5);
      continue;
    }
    try {
      await this.#sender.sendConfirmation(confirmation);
      this.#completed += 1;
    } catch {
      // A confirmation that could not be sent is lost, and saying so is the point: it stays
      // counted as accepted but never completed, so accepted !== completed + queued, which is
      // exactly the signal that work went missing.
    }
  }
}
```

Dart:

```text
Future<void> _drain() async {
  while (!_stopping) {
    if (_jobs.isEmpty) {
      await Future<void>.delayed(const Duration(milliseconds: 5));
      continue;
    }
    final confirmation = _jobs.removeFirst();
    try {
      await _sender.sendConfirmation(confirmation);
      _completed++;
    } catch (failure) {
      // A confirmation that could not be sent is lost, and saying so is the point: it stays
      // counted as accepted but never completed, so accepted != completed + queued, which is
      // exactly the signal that work went missing.
    }
  }
}
```

### Where the queued sender lives

| Language | File |
| --- | --- |
| C# | HotelBooking/Notifications/Internal/QueuedNotificationSender.cs |
| Python | hotel_booking/notifications/internal.py |
| Java | src/main/java/hotelbooking/notifications/internal/QueuedNotificationSender.java |
| Go | notifications/internal.go |
| JavaScript | src/notifications/internal/queued.js |
| Dart | lib/notifications/src/queued.dart |

### Completed example 2.1 · the queued sender

It keeps the NotificationSender promise, so nothing in Reservations changes. What changes is when the email is sent.

### The queued sender

#### C#

```csharp
// HotelBooking/Notifications/Internal/QueuedNotificationSender.cs
using System.Threading.Channels;

namespace HotelBooking.Notifications.Internal;

// Takes confirmations off the request and gives them to a background worker.
//
// It keeps the INotificationSender promise, so BookingService cannot tell the difference: the
// booking hands over a Confirmation and carries on. What changes is when the email is sent, and what
// happens when there are more of them than the worker can keep up with.
//
// The channel is bounded, and DropWrite means a write to a full channel is refused rather than
// waited for - because making the booking wait is the thing this class exists to stop. A refused
// confirmation is not a failed booking: the room is already the guest's. It is not free either, and
// the Rejected counter is what makes that cost visible instead of invisible.
public sealed class QueuedNotificationSender : ICountingNotificationSender
{
    private readonly INotificationSender _sender;
    private readonly Channel<Confirmation> _queue;
    private readonly CancellationTokenSource _stopping = new();
    private readonly Task[] _workers;
    private long _accepted, _rejected, _completed;

    public QueuedNotificationSender(INotificationSender sender, int capacity = 10, int workers = 1)
    {
        _sender = sender;
        // FullMode is Wait, and the write is always TryWrite. That pairing is what gives "refuse and
        // tell me": TryWrite on a full channel returns false straight away rather than waiting.
        // Do not reach for DropWrite here, however much its name suggests it - DropWrite silently
        // discards the item being written and still returns true, so every confirmation would be
        // counted as accepted and a fifth of them would vanish without appearing in any counter.
        _queue = Channel.CreateBounded<Confirmation>(new BoundedChannelOptions(capacity)
        {
            FullMode = BoundedChannelFullMode.Wait,
            SingleReader = workers == 1,
        });
        _workers = Enumerable.Range(0, workers).Select(_ => Task.Run(DrainAsync)).ToArray();
    }

    // Never waits and never throws: the whole point is that the booking is not held up.
    public void SendConfirmation(Confirmation confirmation)
    {
        if (_queue.Writer.TryWrite(confirmation)) Interlocked.Increment(ref _accepted);
        else Interlocked.Increment(ref _rejected);
    }

    public NotificationCounts Counts() => new(
        Interlocked.Read(ref _accepted), Interlocked.Read(ref _rejected),
        Interlocked.Read(ref _completed), _queue.Reader.Count);

    // Stop the workers. With drain the queue is emptied first, so a polite shutdown owes nobody an
    // email; without it, whatever is still queued dies with the process. Nothing here helps with a
    // process that is killed rather than asked to stop: see docs/queue-trace.md.
    public void Close(bool drain = true)
    {
        if (drain) _queue.Writer.TryComplete();
        else _stopping.Cancel();
        try { Task.WaitAll(_workers); } catch (AggregateException) { }
        _stopping.Dispose();
    }

    private async Task DrainAsync()
    {
        try
        {
            await foreach (var confirmation in _queue.Reader.ReadAllAsync(_stopping.Token))
            {
                try
                {
                    _sender.SendConfirmation(confirmation);
                    Interlocked.Increment(ref _completed);
                }
                catch (Exception)
                {
                    // A confirmation that could not be sent is lost, and saying so is the point: it
                    // stays counted as accepted but never completed, so Accepted != Completed +
                    // Queued, which is exactly the signal that work went missing.
                }
            }
        }
        catch (OperationCanceledException)
        {
            // Asked to stop without draining.
        }
    }
}
```

#### Python

```python
# hotel_booking/notifications/internal.py
from __future__ import annotations

import queue
import threading

from hotel_booking.notifications import Confirmation, NotificationCounts, NotificationSender


class EmailNotificationSender:
    """Simulated: counts the emails it would send and needs no network.

    send_seconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second,
    which is how a 4 ms booking becomes a one-second request.
    """

    def __init__(self, send_seconds: float = 0) -> None:
        self.sent = 0
        self._send_seconds = send_seconds
        self._lock = threading.Lock()

    def send_confirmation(self, confirmation: Confirmation) -> None:
        if self._send_seconds:
            threading.Event().wait(self._send_seconds)
        with self._lock:
            self.sent += 1


class SmsNotificationSender:
    """Simulated, like the email sender."""

    def __init__(self) -> None:
        self.sent = 0

    def send_confirmation(self, confirmation: Confirmation) -> None:
        self.sent += 1


class QueuedNotificationSender:
    """Takes confirmations off the request and gives them to a background worker.

    It keeps the NotificationSender promise, so BookingService cannot tell the difference: the
    booking hands over a Confirmation and carries on. What changes is when the email is sent, and
    what happens when there are more of them than the worker can keep up with.

    The queue is bounded. When it is full a confirmation is refused and counted rather than waited
    for, because making the booking wait is the thing this class exists to stop. A refused
    confirmation is not a failed booking - the room is already the guest's - but it is not free
    either, and the rejected counter is what makes the cost visible instead of invisible.
    """

    def __init__(self, sender: NotificationSender, capacity: int = 10, workers: int = 1) -> None:
        self._sender = sender
        self._queue: queue.Queue[Confirmation] = queue.Queue(maxsize=capacity)
        self._lock = threading.Lock()
        self._accepted = 0
        self._rejected = 0
        self._completed = 0
        self._stopping = threading.Event()
        self._workers = [
            threading.Thread(target=self._drain, name=f"notifications-{number}", daemon=True)
            for number in range(workers)
        ]
        for worker in self._workers:
            worker.start()

    def send_confirmation(self, confirmation: Confirmation) -> None:
        """Put the confirmation on the queue, or count it as refused. Never waits, never raises."""
        try:
            self._queue.put_nowait(confirmation)
        except queue.Full:
            with self._lock:
                self._rejected += 1
            return
        with self._lock:
            self._accepted += 1

    def counts(self) -> NotificationCounts:
        with self._lock:
            return NotificationCounts(self._accepted, self._rejected, self._completed, self._queue.qsize())

    def close(self, drain: bool = True) -> None:
        """Stop the workers. With drain=True the queue is emptied first, so a clean shutdown owes
        nobody an email; without it, whatever is still queued dies with the process."""
        if drain:
            self._queue.join()
        self._stopping.set()
        for worker in self._workers:
            worker.join(timeout=5)

    def _drain(self) -> None:
        while not self._stopping.is_set():
            try:
                confirmation = self._queue.get(timeout=0.05)
            except queue.Empty:
                continue
            try:
                self._sender.send_confirmation(confirmation)
                with self._lock:
                    self._completed += 1
            except Exception:
                # A confirmation that could not be sent is lost, and saying so is the point:
                # it stays counted as accepted but never completed, so accepted != completed
                # + queued, which is exactly the signal that work went missing.
                pass
            finally:
                self._queue.task_done()
```

#### Java

```java
// src/main/java/hotelbooking/notifications/internal/QueuedNotificationSender.java
package hotelbooking.notifications.internal;

import hotelbooking.notifications.Confirmation;
import hotelbooking.notifications.CountingNotificationSender;
import hotelbooking.notifications.NotificationCounts;
import hotelbooking.notifications.NotificationSender;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

// Takes confirmations off the request and gives them to a background worker.
//
// It keeps the NotificationSender promise, so BookingService cannot tell the difference: the
// booking hands over a Confirmation and carries on. What changes is when the email is sent, and
// what happens when there are more of them than the worker can keep up with.
//
// The queue is bounded and the write is offer(), not put(). offer returns false straight away when
// the queue is full; put would wait, and making the booking wait is the thing this class exists to
// stop. A refused confirmation is not a failed booking - the room is already the guest's - but it
// is not free either, and the rejected counter is what makes that cost visible instead of invisible.
public class QueuedNotificationSender implements CountingNotificationSender, AutoCloseable {
    private final NotificationSender sender;
    private final BlockingQueue<Confirmation> queue;
    private final List<Thread> workers = new ArrayList<>();
    private final AtomicLong accepted = new AtomicLong();
    private final AtomicLong rejected = new AtomicLong();
    private final AtomicLong completed = new AtomicLong();
    private volatile boolean stopping;

    public QueuedNotificationSender(NotificationSender sender) {
        this(sender, 10, 1);
    }

    public QueuedNotificationSender(NotificationSender sender, int capacity, int workerCount) {
        this.sender = sender;
        this.queue = new ArrayBlockingQueue<>(capacity);
        for (int number = 0; number < workerCount; number++) {
            Thread worker = new Thread(this::drain, "notifications-" + number);
            worker.setDaemon(true);
            worker.start();
            workers.add(worker);
        }
    }

    // Never waits and never throws: the whole point is that the booking is not held up.
    @Override
    public void sendConfirmation(Confirmation confirmation) {
        if (queue.offer(confirmation)) {
            accepted.incrementAndGet();
        } else {
            rejected.incrementAndGet();
        }
    }

    @Override
    public NotificationCounts counts() {
        return new NotificationCounts(accepted.get(), rejected.get(), completed.get(), queue.size());
    }

    // Stop the workers, emptying the queue first so a polite shutdown owes nobody an email.
    @Override
    public void close() {
        close(true);
    }

    // With drain the queue is emptied first; without it, whatever is still queued dies with the
    // process. Nothing here helps with a process that is killed: see docs/queue-trace.md.
    public void close(boolean drain) {
        try {
            if (drain) {
                while (!queue.isEmpty()) {
                    Thread.sleep(10);
                }
            }
            stopping = true;
            for (Thread worker : workers) {
                worker.join(5000);
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private void drain() {
        while (!stopping) {
            Confirmation confirmation;
            try {
                confirmation = queue.poll(50, TimeUnit.MILLISECONDS);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            }
            if (confirmation == null) {
                continue;
            }
            try {
                sender.sendConfirmation(confirmation);
                completed.incrementAndGet();
            } catch (RuntimeException failure) {
                // A confirmation that could not be sent is lost, and saying so is the point: it
                // stays counted as accepted but never completed, so accepted != completed + queued,
                // which is exactly the signal that work went missing.
            }
        }
    }
}
```

#### Go

```go
// notifications/internal.go
package notifications

import (
	"sync"
	"sync/atomic"
	"time"
)

// EmailNotificationSender is simulated: it counts the emails it would send and needs no network.
//
// SendSeconds makes it as slow as a real mail server. P10 Step 1 turns it up to one second, which is
// how a booking that does well under a millisecond of work becomes a one-second request.
type EmailNotificationSender struct {
	SendSeconds float64
	sent        atomic.Int64
}

func (email *EmailNotificationSender) SendConfirmation(confirmation Confirmation) error {
	if email.SendSeconds > 0 {
		time.Sleep(time.Duration(email.SendSeconds * float64(time.Second)))
	}
	email.sent.Add(1)
	return nil
}

// Sent is how many emails this sender has actually sent.
func (email *EmailNotificationSender) Sent() int64 {
	return email.sent.Load()
}

// SmsNotificationSender is simulated, like the email sender.
type SmsNotificationSender struct {
	Sent int
}

func (sms *SmsNotificationSender) SendConfirmation(confirmation Confirmation) error {
	sms.Sent++
	return nil
}

// QueuedNotificationSender takes confirmations off the request and gives them to a background
// worker.
//
// It keeps the NotificationSender contract, so BookingService cannot tell the difference: the
// booking hands over a Confirmation and carries on. What changes is when the email is sent, and what
// happens when there are more of them than the worker can keep up with.
//
// The channel is buffered, which is the bound, and the send is a select with a default branch, which
// is what makes a full queue refuse instead of wait. A refused confirmation is not a failed booking
// - the room is already the guest's - but it is not free either, and Rejected is what makes that
// cost visible instead of invisible.
type QueuedNotificationSender struct {
	sender    NotificationSender
	jobs      chan Confirmation
	stopping  chan struct{}
	done      sync.WaitGroup
	accepted  atomic.Int64
	rejected  atomic.Int64
	completed atomic.Int64
	closeOnce sync.Once
}

func NewQueuedNotificationSender(sender NotificationSender, capacity, workers int) *QueuedNotificationSender {
	queue := &QueuedNotificationSender{
		sender:   sender,
		jobs:     make(chan Confirmation, capacity),
		stopping: make(chan struct{}),
	}
	for worker := 0; worker < workers; worker++ {
		queue.done.Add(1)
		go queue.drain()
	}
	return queue
}

// SendConfirmation never waits and never fails: the whole point is that the booking is not held up.
func (queue *QueuedNotificationSender) SendConfirmation(confirmation Confirmation) error {
	select {
	case queue.jobs <- confirmation:
		queue.accepted.Add(1)
	default:
		queue.rejected.Add(1)
	}
	return nil
}

func (queue *QueuedNotificationSender) Counts() Counts {
	return Counts{
		Accepted:  queue.accepted.Load(),
		Rejected:  queue.rejected.Load(),
		Completed: queue.completed.Load(),
		Queued:    len(queue.jobs),
	}
}

// Close stops the workers. With drain the queue is emptied first, so a polite shutdown owes nobody
// an email; without it, whatever is still queued dies with the process. Nothing here helps with a
// process that is killed rather than asked to stop: see docs/queue-trace.md.
func (queue *QueuedNotificationSender) Close(drain bool) {
	queue.closeOnce.Do(func() {
		if drain {
			for len(queue.jobs) > 0 {
				time.Sleep(10 * time.Millisecond)
			}
		}
		close(queue.stopping)
		queue.done.Wait()
	})
}

func (queue *QueuedNotificationSender) drain() {
	defer queue.done.Done()
	for {
		select {
		case <-queue.stopping:
			return
		case confirmation := <-queue.jobs:
			if err := queue.sender.SendConfirmation(confirmation); err != nil {
				// A confirmation that could not be sent is lost, and saying so is the point: it
				// stays counted as accepted but never completed, so Accepted != Completed + Queued,
				// which is exactly the signal that work went missing.
				continue
			}
			queue.completed.Add(1)
		}
	}
}
```

#### JavaScript

```javascript
// src/notifications/internal/queued.js
// Takes confirmations off the request and gives them to a background worker.
//
// It keeps the sender promise, so BookingService cannot tell the difference: the booking hands over
// a Confirmation and carries on. What changes is when the email is sent, and what happens when
// there are more of them than the worker can keep up with.
//
// The queue is bounded. When it is full a confirmation is refused and counted rather than waited
// for, because making the booking wait is the thing this class exists to stop. A refused
// confirmation is not a failed booking - the room is already the guest's - but it is not free
// either, and the rejected counter is what makes that cost visible instead of invisible.
//
// No locks anywhere, and none are needed: Node runs one request's code at a time, so pushing onto
// the array and reading its length cannot be interleaved with anything.
const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

export class QueuedNotificationSender {
  #sender;
  #capacity;
  #jobs = [];
  #accepted = 0;
  #rejected = 0;
  #completed = 0;
  #stopping = false;
  #workers;

  constructor(sender, capacity = 10, workers = 1) {
    this.#sender = sender;
    this.#capacity = capacity;
    this.#workers = Array.from({ length: workers }, () => this.#drain());
  }

  // Never waits and never throws: the whole point is that the booking is not held up.
  sendConfirmation(confirmation) {
    if (this.#jobs.length >= this.#capacity) {
      this.#rejected += 1;
      return;
    }
    this.#jobs.push(confirmation);
    this.#accepted += 1;
  }

  counts() {
    return {
      accepted: this.#accepted, rejected: this.#rejected,
      completed: this.#completed, queued: this.#jobs.length,
    };
  }

  // Stop the workers. With drain the queue is emptied first, so a polite shutdown owes nobody an
  // email; without it, whatever is still queued dies with the process.
  async close({ drain = true } = {}) {
    if (drain) {
      while (this.#jobs.length) await wait(10);
    }
    this.#stopping = true;
    await Promise.all(this.#workers);
  }

  async #drain() {
    while (!this.#stopping) {
      const confirmation = this.#jobs.shift();
      if (confirmation === undefined) {
        await wait(5);
        continue;
      }
      try {
        await this.#sender.sendConfirmation(confirmation);
        this.#completed += 1;
      } catch {
        // A confirmation that could not be sent is lost, and saying so is the point: it stays
        // counted as accepted but never completed, so accepted !== completed + queued, which is
        // exactly the signal that work went missing.
      }
    }
  }
}
```

#### Dart

```dart
// lib/notifications/src/queued.dart
import 'dart:async';
import 'dart:collection';

import '../notifications.dart';

// Takes confirmations off the request and gives them to a background worker.
//
// It keeps the NotificationSender promise, so BookingService cannot tell the difference: the
// booking hands over a Confirmation and carries on. What changes is when the email is sent, and
// what happens when there are more of them than the worker can keep up with.
//
// The queue is bounded. When it is full a confirmation is refused and counted rather than waited
// for, because making the booking wait is the thing this class exists to stop. A refused
// confirmation is not a failed booking - the room is already the guest's - but it is not free
// either, and the rejected counter is what makes that cost visible instead of invisible.
//
// No locks anywhere, and none are needed: Dart runs one request's code at a time on this isolate,
// so adding to the queue and reading its length cannot be interleaved with anything.
class QueuedNotificationSender implements CountingNotificationSender {
  QueuedNotificationSender(this._sender, {int capacity = 10, int workers = 1})
      : _capacity = capacity {
    _workers = List.generate(workers, (_) => _drain());
  }

  final NotificationSender _sender;
  final int _capacity;
  final Queue<Confirmation> _jobs = Queue<Confirmation>();
  late final List<Future<void>> _workers;
  int _accepted = 0;
  int _rejected = 0;
  int _completed = 0;
  bool _stopping = false;

  // Never waits and never throws: the whole point is that the booking is not held up.
  @override
  void sendConfirmation(Confirmation confirmation) {
    if (_jobs.length >= _capacity) {
      _rejected++;
      return;
    }
    _jobs.add(confirmation);
    _accepted++;
  }

  @override
  NotificationCounts counts() =>
      NotificationCounts(_accepted, _rejected, _completed, _jobs.length);

  /// Stop the workers. With drain the queue is emptied first, so a polite shutdown owes nobody an
  /// email; without it, whatever is still queued dies with the process.
  Future<void> close({bool drain = true}) async {
    if (drain) {
      while (_jobs.isNotEmpty) {
        await Future<void>.delayed(const Duration(milliseconds: 10));
      }
    }
    _stopping = true;
    await Future.wait(_workers);
  }

  Future<void> _drain() async {
    while (!_stopping) {
      if (_jobs.isEmpty) {
        await Future<void>.delayed(const Duration(milliseconds: 5));
        continue;
      }
      final confirmation = _jobs.removeFirst();
      try {
        await _sender.sendConfirmation(confirmation);
        _completed++;
      } catch (failure) {
        // A confirmation that could not be sent is lost, and saying so is the point: it stays
        // counted as accepted but never completed, so accepted != completed + queued, which is
        // exactly the signal that work went missing.
      }
    }
  }
}
```

### Completed example 2.2 · the wiring

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
    // P10: the mail server is slow, and the booking no longer waits for it. HOTEL_EMAIL_SECONDS
    // makes the sender as slow as a real one; the queue is what keeps that out of the request.
    var email = new EmailNotificationSender(double.Parse(Environment.GetEnvironmentVariable("HOTEL_EMAIL_SECONDS") ?? "0"));
    var notifications = new QueuedNotificationSender(
        email,
        int.Parse(Environment.GetEnvironmentVariable("HOTEL_QUEUE_CAPACITY") ?? "10"),
        int.Parse(Environment.GetEnvironmentVariable("HOTEL_WORKERS") ?? "1"));
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
        var result = work(new BookingService(rooms, new CountingReservationStore(store, stats), notifications, slowStepMs));
        store.SaveChanges();
        return result;
    }

    HotelApi.Map(app, rooms, UnitOfWork, stats, cache, notifications);

    // P10 Step 6: drain the queue before the worker stops, so a polite shutdown owes nobody an email.
    app.Lifetime.ApplicationStopping.Register(() =>
    {
        Console.WriteLine("Draining the notification queue...");
        notifications.Close(drain: true);
        Console.WriteLine($"Notification worker stopped. {notifications.Counts()}");
    });
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
from hotel_booking.notifications.internal import EmailNotificationSender, QueuedNotificationSender
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
    # P10: the mail server is slow, and the booking no longer waits for it. HOTEL_EMAIL_SECONDS
    # makes the sender as slow as a real one; the queue is what keeps that out of the request.
    email = EmailNotificationSender(send_seconds=float(os.getenv("HOTEL_EMAIL_SECONDS", "0")))
    notifications = QueuedNotificationSender(
        email,
        capacity=int(os.getenv("HOTEL_QUEUE_CAPACITY", "10")),
        workers=int(os.getenv("HOTEL_WORKERS", "1")),
    )
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
            yield BookingService(rooms, CountingReservationStore(store, stats), notifications, slow_step_seconds=slow_step)
            store.save_changes()

    # Step 4 of P09 starts a second instance with HOTEL_PORT=5081, against the same database.
    try:
        uvicorn.run(create_app(rooms, unit_of_work, stats, cache, notifications.counts),
                    host="localhost", port=int(os.getenv("HOTEL_PORT", "5080")))
    finally:
        # P10 Step 6: drain the queue before the worker stops, so a polite shutdown owes nobody
        # an email. Nothing here helps with kill -9: see docs/queue-trace.md.
        print("Draining the notification queue...")
        notifications.close(drain=True)
        print(f"Notification worker stopped. {notifications.counts()}")


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
import hotelbooking.notifications.internal.QueuedNotificationSender;
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
        // P10: the mail server is slow, and the booking no longer waits for it.
        // HOTEL_EMAIL_SECONDS makes the sender as slow as a real one; the queue is what keeps that
        // out of the request.
        EmailNotificationSender email = new EmailNotificationSender(
                Double.parseDouble(System.getenv().getOrDefault("HOTEL_EMAIL_SECONDS", "0")));
        QueuedNotificationSender notifications = new QueuedNotificationSender(email,
                Integer.parseInt(System.getenv().getOrDefault("HOTEL_QUEUE_CAPACITY", "10")),
                Integer.parseInt(System.getenv().getOrDefault("HOTEL_WORKERS", "1")));
        // P10 Step 6: drain the queue before the worker stops, so a polite shutdown owes nobody an
        // email. Nothing here helps with kill -9: see docs/queue-trace.md.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Draining the notification queue...");
            notifications.close(true);
            System.out.println("Notification worker stopped. " + notifications.counts());
        }));
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
            var result = work.apply(new BookingService(rooms, new CountingReservationStore(store, stats), notifications, slowStepMs));
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
        return new HotelApi(rooms, unitOfWork, stats, cache, notifications);
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
	// P10: the mail server is slow, and the booking no longer waits for it. HOTEL_EMAIL_SECONDS
	// makes the sender as slow as a real one; the queue is what keeps that out of the request.
	emailSeconds, _ := strconv.ParseFloat(cmp.Or(os.Getenv("HOTEL_EMAIL_SECONDS"), "0"), 64)
	email := &notifications.EmailNotificationSender{SendSeconds: emailSeconds}
	queueCapacity, _ := strconv.Atoi(cmp.Or(os.Getenv("HOTEL_QUEUE_CAPACITY"), "10"))
	workerCount, _ := strconv.Atoi(cmp.Or(os.Getenv("HOTEL_WORKERS"), "1"))
	queue := notifications.NewQueuedNotificationSender(email, queueCapacity, workerCount)
	// P10 Step 6: drain the queue before the worker stops, so a polite shutdown owes nobody an
	// email. Nothing here helps with a killed process: see docs/queue-trace.md.
	defer func() {
		fmt.Println("Draining the notification queue...")
		queue.Close(true)
		fmt.Printf("Notification worker stopped. %+v\n", queue.Counts())
	}()
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
		work(reservations.NewBookingService(hotelRooms, api.NewCountingReservationStore(store, stats), queue, slowStep))
		return store.SaveChanges()
	}

	// Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
	address := "localhost:" + cmp.Or(os.Getenv("HOTEL_PORT"), "5080")
	fmt.Println("Listening on http://" + address)
	log.Fatal(http.ListenAndServe(address, api.New(hotelRooms, unitOfWork, stats, cache, queue)))
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
	fmt.Printf("Confirmation emails sent: %d\n", email.Sent())
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
import { QueuedNotificationSender } from './src/notifications/internal/queued.js';
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
  // P10: the mail server is slow, and the booking no longer waits for it. HOTEL_EMAIL_SECONDS
  // makes the sender as slow as a real one; the queue is what keeps that out of the request.
  const email = new EmailNotificationSender(Number(process.env.HOTEL_EMAIL_SECONDS ?? 0));
  const notifications = new QueuedNotificationSender(
    email,
    Number(process.env.HOTEL_QUEUE_CAPACITY ?? 10),
    Number(process.env.HOTEL_WORKERS ?? 1),
  );
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
    const result = await work(new BookingService(rooms, new CountingReservationStore(store, stats), notifications, slowStepMs));
    await store.saveChanges();
    return result;
  };

  // Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
  const port = Number(process.env.HOTEL_PORT ?? 5080);
  // P10 Step 6: drain the queue before the worker stops, so a polite shutdown owes nobody an
  // email. Nothing here helps with kill -9: see docs/queue-trace.md.
  for (const signal of ['SIGINT', 'SIGTERM']) {
    process.on(signal, async () => {
      console.log('Draining the notification queue...');
      await notifications.close({ drain: true });
      console.log('Notification worker stopped.', notifications.counts());
      process.exit(0);
    });
  }

  createApi(rooms, unitOfWork, stats, cache, () => notifications.counts()).listen(port, 'localhost', () => console.log(`Listening on http://localhost:${port}`));
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
import 'package:hotel_booking/notifications/src/queued.dart';
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
  // P10: the mail server is slow, and the booking no longer waits for it. HOTEL_EMAIL_SECONDS
  // makes the sender as slow as a real one; the queue is what keeps that out of the request.
  final email = EmailNotificationSender(
      sendSeconds: double.parse(Platform.environment['HOTEL_EMAIL_SECONDS'] ?? '0'));
  final notifications = QueuedNotificationSender(
    email,
    capacity: int.parse(Platform.environment['HOTEL_QUEUE_CAPACITY'] ?? '10'),
    workers: int.parse(Platform.environment['HOTEL_WORKERS'] ?? '1'),
  );
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
        BookingService(rooms, CountingReservationStore(store, stats), notifications, slowStep: slowStep));
    await store.saveChanges();
    return result;
  }

  // Step 4 starts a second instance with HOTEL_PORT=5081, against the same database.
  final apiPort = int.parse(Platform.environment['HOTEL_PORT'] ?? '5080');
  // P10 Step 6: drain the queue before the worker stops, so a polite shutdown owes nobody an
  // email. Nothing here helps with a killed process: see docs/queue-trace.md.
  ProcessSignal.sigint.watch().listen((_) async {
    print('Draining the notification queue...');
    await notifications.close(drain: true);
    final left = notifications.counts();
    print('Notification worker stopped. accepted=${left.accepted} completed=${left.completed} queued=${left.queued}');
    exit(0);
  });

  await serve(
      createApi(rooms, unitOfWork, stats, cache, notificationCounts: notifications.counts),
      'localhost', apiPort);
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

### What one booking costs now

The same one-second sender, and the same booking.

### One booking

```text
BookingService.reserve(Maya, R7, 1-2 December)
  Accepted in under 1 ms
  completed=0  queued=1

The same booking, with the same one-second email sender. The email has not been sent: it is on the
queue, and the worker will send it. Over HTTP the whole request comes back in about 30 ms, almost
all of which is HTTP and the database rather than anything to do with the email.
```

### Your turn · take the email out of the request

1. Create the queued sender (2.1) next to the email sender, in your language’s Notifications internal folder.
2. In your wiring file, wrap the email sender in the queued sender and pass that to BookingService (2.2).
3. JavaScript and Dart only: make reserve await the confirmation. Without it the request would never have waited for a slow sender in the first place, and a failed send would escape the try/catch.
4. Run your checks: still 19 green.
5. Start the app with HOTEL_EMAIL_SECONDS=1 again and book one room with curl.exe. It should answer in milliseconds.
6. Add the “With a queue in front of the sender” section to docs/queue-trace.md.

**When you finish, you have:** A booking that answers in milliseconds with a one-second email sender, and 19 green checks.
### Explanation

BookingService did not change. That is the point of the contract from P04: the booking hands over a Confirmation and carries on, and whether that Confirmation is sent now, later or never is the Notifications module’s business.



---

## 4. Step 3 · Burst it, and look at the memory

The request is quick now. Send thirty bookings and ask a different question: where did the thirty emails go?





### Run it

Same two terminals, with a queue big enough that nothing is ever refused.

### Two terminals

#### C#

```text
# terminal 1
$env:HOTEL_EMAIL_SECONDS = "1"; $env:HOTEL_QUEUE_CAPACITY = "10000"; dotnet run --project HotelBooking
# terminal 2
dotnet run tools/burst.cs
```

#### Python

```text
# terminal 1
$env:HOTEL_EMAIL_SECONDS = "1"; $env:HOTEL_QUEUE_CAPACITY = "10000"; python main.py
# terminal 2
python tools/burst.py
```

#### Java

```text
# terminal 1
$env:HOTEL_EMAIL_SECONDS = "1"; $env:HOTEL_QUEUE_CAPACITY = "10000"; mvn -q spring-boot:run
# terminal 2
java tools/Burst.java
```

#### Go

```text
# terminal 1
$env:HOTEL_EMAIL_SECONDS = "1"; $env:HOTEL_QUEUE_CAPACITY = "10000"; go run .
# terminal 2
go run ./tools/burst
```

#### JavaScript

```text
# terminal 1
$env:HOTEL_EMAIL_SECONDS = "1"; $env:HOTEL_QUEUE_CAPACITY = "10000"; node main.js
# terminal 2
node tools/burst.js
```

#### Dart

```text
# terminal 1
$env:HOTEL_EMAIL_SECONDS = "1"; $env:HOTEL_QUEUE_CAPACITY = "10000"; dart run bin/main.dart
# terminal 2
dart run tool/burst.dart
```

### What you will see

**Why an unbounded queue is not the answer** In short: It answers every request at once and keeps every job, so the limit becomes the machine rather than a number you chose. A burst of thirty leaves twenty-nine jobs in memory. A burst of thirty thousand leaves thirty thousand, and nothing anywhere refuses them. An unbounded queue does not remove the problem of a slow consumer; it hides it, until the process runs out of memory and takes the bookings down with the emails.

1. Start: the app is started with HOTEL_QUEUE_CAPACITY=10000.
2. Action: you run the burst tool.
3. Result:
Sending 30 bookings, one after another...

Accepted: 30
Rejected: 0
Slowest booking: 31 ms
Total: 0.1 s

GET /stats
{"notificationsAccepted":30,"notificationsRejected":0,"notificationsCompleted":0,"notificationsQueued":29}

Every guest was answered at once - and 29 emails are sitting in memory, waiting for a worker
that sends one a second. Nothing refused them, and nothing would have refused 30,000.

### Terminal 2

```text
Sending 30 bookings, one after another...

Accepted: 30
Rejected: 0
Slowest booking: 31 ms
Total: 0.1 s

GET /stats
{"notificationsAccepted":30,"notificationsRejected":0,"notificationsCompleted":0,"notificationsQueued":29}

Every guest was answered at once - and 29 emails are sitting in memory, waiting for a worker
that sends one a second. Nothing refused them, and nothing would have refused 30,000.
```

### Read those two lines together

| Line | What it means |
| --- | --- |
| Accepted: 30, Total: 0.1 s | Every guest was answered at once. The queue did exactly what it was built for. |
| notificationsQueued: 29 | Twenty-nine emails are sitting in memory, waiting for a worker that sends one a second. |
| notificationsRejected: 0 | Nothing refused them. Nothing would have refused thirty thousand either. |

### Why this is worse than it looks

An unbounded queue does not remove the problem of a slow consumer; it hides it. The queue grows until the process runs out of memory, and when it does it takes the bookings down with the emails — so a mail server problem becomes a booking outage, which is exactly what moving the email out of the request was supposed to prevent.

Every queue has a limit. The only question is whether you chose it, or the operating system chose it for you at the worst possible moment.

### Your turn · watch it fill

1. Start the app with HOTEL_EMAIL_SECONDS=1 and HOTEL_QUEUE_CAPACITY=10000.
2. Run the burst tool and read GET /stats at the end of its output.
3. Write down the three numbers: accepted, rejected and queued.
4. Add the “Why an unbounded queue is not the answer” section to docs/queue-trace.md with your own output.
5. Write one line underneath: what would have happened with a burst of thirty thousand?

**When you finish, you have:** Your own unbounded burst in docs/queue-trace.md, and one line saying where it ends.
### Explanation

This is the step people skip. A queue that answers every request instantly looks like a success, and the counter that says twenty-nine jobs are waiting is the only thing in the output that says otherwise.



---

## 5. Step 4 · Put a bound on it, and count what happens

Cap the queue, and publish four numbers so that being behind is something you can read rather than something you discover.





### The request, and what happens away from it

### Diagram: The same booking, with the email on a bounded queue

The left column is the request, which now ends at step 4. The right column is what happens away from it. The bound is the whole point: when the queue is full the confirmation is refused and counted, because the alternative - waiting - is what this design exists to avoid.

Diagram as text:

The same POST /reservations, doing the same booking work: check, insert, commit, about three milliseconds. Step 2: the room is the guest’s, exactly as before. Step 3: the confirmation is offered to the queue. This is one attempt that never waits - it either fits or it does not. If the queue is full the confirmation is refused and the rejected counter goes up. Nothing waits and the booking is untouched: the guest has a room and will not get an email, which is a real cost and a counted one. Step 4: the request answers 201 Created in milliseconds, whether the confirmation was taken or refused. Away from the request, the queue holds at most ten jobs, and queued says how far behind the worker is. The worker takes one job at a time and sends it, a second each. While it is sending, that job is in neither queued nor completed. When the send finishes, completed goes up. The accounting is accepted = completed + queued + whatever is in flight.

**Step by step:** Read the left column first - it is the whole request, and it now ends at box 4. Then read the right column, which is everything that happens after the guest has already been answered.

1. Boxes 1 and 2 are unchanged from the previous diagram. The booking work is the same work, and it takes the same few milliseconds.
2. White box 3: the confirmation is offered to the queue. The word "offer" is doing a lot of work - this call never waits and never fails. Each language spells it differently but means the same thing: Python put_nowait, Java offer rather than put, Go a select with a default branch, C# TryWrite on a channel whose FullMode is Wait, and JavaScript and Dart a plain length check.

   C#:
   
   ```text
   // Never waits and never throws: the whole point is that the booking is not held up.
   public void SendConfirmation(Confirmation confirmation)
   {
       if (_queue.Writer.TryWrite(confirmation)) Interlocked.Increment(ref _accepted);
       else Interlocked.Increment(ref _rejected);
   }
   ```
   
   Python:
   
   ```text
   def send_confirmation(self, confirmation: Confirmation) -> None:
       """Put the confirmation on the queue, or count it as refused. Never waits, never raises."""
       try:
           self._queue.put_nowait(confirmation)
       except queue.Full:
           with self._lock:
               self._rejected += 1
           return
       with self._lock:
           self._accepted += 1
   ```
   
   Java:
   
   ```text
   // Never waits and never throws: the whole point is that the booking is not held up.
   @Override
   public void sendConfirmation(Confirmation confirmation) {
       if (queue.offer(confirmation)) {
           accepted.incrementAndGet();
       } else {
           rejected.incrementAndGet();
       }
   }
   ```
   
   Go:
   
   ```text
   // SendConfirmation never waits and never fails: the whole point is that the booking is not held up.
   func (queue *QueuedNotificationSender) SendConfirmation(confirmation Confirmation) error {
   	select {
   	case queue.jobs <- confirmation:
   		queue.accepted.Add(1)
   	default:
   		queue.rejected.Add(1)
   	}
   	return nil
   }
   ```
   
   JavaScript:
   
   ```text
   // Never waits and never throws: the whole point is that the booking is not held up.
   sendConfirmation(confirmation) {
     if (this.#jobs.length >= this.#capacity) {
       this.#rejected += 1;
       return;
     }
     this.#jobs.push(confirmation);
     this.#accepted += 1;
   }
   ```
   
   Dart:
   
   ```text
   // Never waits and never throws: the whole point is that the booking is not held up.
   @override
   void sendConfirmation(Confirmation confirmation) {
     if (_jobs.length >= _capacity) {
       _rejected++;
       return;
     }
     _jobs.add(confirmation);
     _accepted++;
   }
   ```
3. The red dashed arrow to the yellow box “Full? Refuse it and count it” is the backpressure. When the queue is full the confirmation is refused and counted, and the booking is not affected at all. This is the R6 rule from P04 once more: the booking is already saved, so a failed message must not undo or hide it.
4. Yellow box 4: the request answers. Whether the confirmation was taken or refused, the guest hears about their room in milliseconds:

   text:
   
   ```text
   BookingService.reserve(Maya, R7, 1-2 December)
     Accepted in under 1 ms
     completed=0  queued=1
   
   The same booking, with the same one-second email sender. The email has not been sent: it is on the
   queue, and the worker will send it. Over HTTP the whole request comes back in about 30 ms, almost
   all of which is HTTP and the database rather than anything to do with the email.
   ```
5. Grey box "The queue holds it": at most ten. That bound is the entire difference between this design and one that falls over. Without it, a burst leaves twenty-nine jobs in memory and a bigger burst leaves thirty thousand:

   text:
   
   ```text
   Sending 30 bookings, one after another...
   
   Accepted: 30
   Rejected: 0
   Slowest booking: 31 ms
   Total: 0.1 s
   
   GET /stats
   {"notificationsAccepted":30,"notificationsRejected":0,"notificationsCompleted":0,"notificationsQueued":29}
   
   Every guest was answered at once - and 29 emails are sitting in memory, waiting for a worker
   that sends one a second. Nothing refused them, and nothing would have refused 30,000.
   ```
6. With the bound, the same burst is answered the same way and the overflow is visible instead of invisible:

   text:
   
   ```text
   Sending 30 bookings, one after another...
   
   Accepted: 30
   Rejected: 0
   Slowest booking: 47 ms
   Total: 0.1 s
   
   GET /stats
   {"notificationsAccepted":11,"notificationsRejected":19,"notificationsCompleted":0,"notificationsQueued":10}
   
   Read the two lines together. Every booking was accepted - all 30 guests have their rooms - and
   19 confirmation emails were refused on purpose, because the queue was full. The booking and the
   email now succeed and fail separately, which is the whole point.
   ```
7. Do not memorise the split. It depends on whether the worker got a turn during the burst, and that differs between runtimes:

   text:
   
   ```text
   The exact split depends on how fast the burst arrives compared with the worker.
   Over HTTP the burst takes about 0.1 s, long enough for the worker to take one job and free a slot,
   so it is 11 and 19. Driven in-process, the burst is over in under a millisecond and the worker has
   not moved, so it is 10 and 20. Do not memorise either number. Three things are always true:
   
     1. queued never goes above the bound. That is what "bounded" means.
     2. every booking succeeds. A refused email is not a refused room.
     3. accepted = completed + queued + whatever is in flight. A worker part-way through sending a
        job has taken it off the queue and not finished it, so at most one job per worker sits in
        neither number. Once nothing is in flight the two sides balance exactly, and if they still
        do not, work was lost.
   ```
8. White box "The worker takes one and sends it": one job at a time. While a job is being sent it has left the queue and has not been completed, which is why the accounting allows for jobs in flight.

   C#:
   
   ```text
   private async Task DrainAsync()
   {
       try
       {
           await foreach (var confirmation in _queue.Reader.ReadAllAsync(_stopping.Token))
           {
               try
               {
                   _sender.SendConfirmation(confirmation);
                   Interlocked.Increment(ref _completed);
               }
               catch (Exception)
               {
                   // A confirmation that could not be sent is lost, and saying so is the point: it
                   // stays counted as accepted but never completed, so Accepted != Completed +
                   // Queued, which is exactly the signal that work went missing.
               }
           }
       }
       catch (OperationCanceledException)
       {
           // Asked to stop without draining.
       }
   }
   ```
   
   Python:
   
   ```text
   def _drain(self) -> None:
       while not self._stopping.is_set():
           try:
               confirmation = self._queue.get(timeout=0.05)
           except queue.Empty:
               continue
           try:
               self._sender.send_confirmation(confirmation)
               with self._lock:
                   self._completed += 1
           except Exception:
               # A confirmation that could not be sent is lost, and saying so is the point:
               # it stays counted as accepted but never completed, so accepted != completed
               # + queued, which is exactly the signal that work went missing.
               pass
           finally:
               self._queue.task_done()
   ```
   
   Java:
   
   ```text
   private void drain() {
       while (!stopping) {
           Confirmation confirmation;
           try {
               confirmation = queue.poll(50, TimeUnit.MILLISECONDS);
           } catch (InterruptedException interrupted) {
               Thread.currentThread().interrupt();
               return;
           }
           if (confirmation == null) {
               continue;
           }
           try {
               sender.sendConfirmation(confirmation);
               completed.incrementAndGet();
           } catch (RuntimeException failure) {
               // A confirmation that could not be sent is lost, and saying so is the point: it
               // stays counted as accepted but never completed, so accepted != completed + queued,
               // which is exactly the signal that work went missing.
           }
       }
   }
   ```
   
   Go:
   
   ```text
   func (queue *QueuedNotificationSender) drain() {
   	defer queue.done.Done()
   	for {
   		select {
   		case <-queue.stopping:
   			return
   		case confirmation := <-queue.jobs:
   			if err := queue.sender.SendConfirmation(confirmation); err != nil {
   				// A confirmation that could not be sent is lost, and saying so is the point: it
   				// stays counted as accepted but never completed, so Accepted != Completed + Queued,
   				// which is exactly the signal that work went missing.
   				continue
   			}
   			queue.completed.Add(1)
   		}
   	}
   }
   ```
   
   JavaScript:
   
   ```text
   async #drain() {
     while (!this.#stopping) {
       const confirmation = this.#jobs.shift();
       if (confirmation === undefined) {
         await wait(5);
         continue;
       }
       try {
         await this.#sender.sendConfirmation(confirmation);
         this.#completed += 1;
       } catch {
         // A confirmation that could not be sent is lost, and saying so is the point: it stays
         // counted as accepted but never completed, so accepted !== completed + queued, which is
         // exactly the signal that work went missing.
       }
     }
   }
   ```
   
   Dart:
   
   ```text
   Future<void> _drain() async {
     while (!_stopping) {
       if (_jobs.isEmpty) {
         await Future<void>.delayed(const Duration(milliseconds: 5));
         continue;
       }
       final confirmation = _jobs.removeFirst();
       try {
         await _sender.sendConfirmation(confirmation);
         _completed++;
       } catch (failure) {
         // A confirmation that could not be sent is lost, and saying so is the point: it stays
         // counted as accepted but never completed, so accepted != completed + queued, which is
         // exactly the signal that work went missing.
       }
     }
   }
   ```
9. Green box "completed + 1", and once the worker has caught up the two sides balance:

   text:
   
   ```text
   GET /stats, eleven seconds later
   {"notificationsAccepted":11,"notificationsRejected":19,"notificationsCompleted":11,"notificationsQueued":0}
   
   accepted 11 = completed 11 + queued 0. Everything the queue took, it finished.
   ```
10. Stopping the app politely drains what is left, so a clean shutdown owes nobody an email:

    text:
    
    ```text
    Stopping the app with 10 confirmations accepted and 3 sent so far...
    
    Draining the notification queue: 7 to go
    Notification worker stopped.
    
    accepted=10  completed=10  queued=0
    Nobody was owed an email.
    ```
11. Without the drain the worker stops where it is - and this is the part worth reading twice:

    text:
    
    ```text
    Stopping the app with 10 confirmations accepted and 3 sent so far...
    
    (no drain) Notification worker stopped.
    
    accepted=10  completed=4  queued=6
    accepted == completed + queued: True
    ```
12. Look carefully at the second one: accepted == completed + queued is still true.

Nothing has been lost yet. Those six confirmations are still on the queue, counted honestly as
queued. They are lost a second later, when the process exits and the queue goes with it - and at
that moment no counter changes, no log line is written, and nothing anywhere remembers that six
guests were owed an email.

That is why the drain exists, and it is also why the drain is not enough. It makes a polite
shutdown lose nothing. It does nothing at all for kill -9.

    C#:
    
    ```text
    // Stop the workers. With drain the queue is emptied first, so a polite shutdown owes nobody an
    // email; without it, whatever is still queued dies with the process. Nothing here helps with a
    // process that is killed rather than asked to stop: see docs/queue-trace.md.
    public void Close(bool drain = true)
    {
        if (drain) _queue.Writer.TryComplete();
        else _stopping.Cancel();
        try { Task.WaitAll(_workers); } catch (AggregateException) { }
        _stopping.Dispose();
    }
    ```
    
    Python:
    
    ```text
    def close(self, drain: bool = True) -> None:
        """Stop the workers. With drain=True the queue is emptied first, so a clean shutdown owes
        nobody an email; without it, whatever is still queued dies with the process."""
        if drain:
            self._queue.join()
        self._stopping.set()
        for worker in self._workers:
            worker.join(timeout=5)
    ```
    
    Java:
    
    ```text
    // With drain the queue is emptied first; without it, whatever is still queued dies with the
    // process. Nothing here helps with a process that is killed: see docs/queue-trace.md.
    public void close(boolean drain) {
        try {
            if (drain) {
                while (!queue.isEmpty()) {
                    Thread.sleep(10);
                }
            }
            stopping = true;
            for (Thread worker : workers) {
                worker.join(5000);
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
    ```
    
    Go:
    
    ```text
    // Close stops the workers. With drain the queue is emptied first, so a polite shutdown owes nobody
    // an email; without it, whatever is still queued dies with the process. Nothing here helps with a
    // process that is killed rather than asked to stop: see docs/queue-trace.md.
    func (queue *QueuedNotificationSender) Close(drain bool) {
    	queue.closeOnce.Do(func() {
    		if drain {
    			for len(queue.jobs) > 0 {
    				time.Sleep(10 * time.Millisecond)
    			}
    		}
    		close(queue.stopping)
    		queue.done.Wait()
    	})
    }
    ```
    
    JavaScript:
    
    ```text
    // Stop the workers. With drain the queue is emptied first, so a polite shutdown owes nobody an
    // email; without it, whatever is still queued dies with the process.
    async close({ drain = true } = {}) {
      if (drain) {
        while (this.#jobs.length) await wait(10);
      }
      this.#stopping = true;
      await Promise.all(this.#workers);
    }
    ```
    
    Dart:
    
    ```text
    /// Stop the workers. With drain the queue is emptied first, so a polite shutdown owes nobody an
    /// email; without it, whatever is still queued dies with the process.
    Future<void> close({bool drain = true}) async {
      if (drain) {
        while (_jobs.isNotEmpty) {
          await Future<void>.delayed(const Duration(milliseconds: 10));
        }
      }
      _stopping = true;
      await Future.wait(_workers);
    }
    ```
13. The three tests that hold all of this in place: the request does not wait, the bound really refuses, and a drained shutdown finishes everything it accepted.

    C#:
    
    ```text
    [Fact]
    public void A_booking_does_not_wait_for_a_slow_confirmation()
    {
        // The whole point of the queue: a one-second email must not cost the guest a second.
        var slow = new EmailNotificationSender(sendSeconds: 1);
        var notifications = new QueuedNotificationSender(slow, capacity: 10, workers: 1);
    
        var watch = Stopwatch.StartNew();
        notifications.SendConfirmation(AConfirmation(1));
        watch.Stop();
    
        Assert.True(watch.ElapsedMilliseconds < 100, "handing the email to the queue must be immediate");
        Assert.Equal(0, slow.Sent);
        Assert.Equal(1, notifications.Counts().Accepted);
        notifications.Close(drain: false);
    }
    
    [Fact]
    public void A_burst_larger_than_the_bound_is_refused_and_counted()
    {
        // Thirty confirmations into a queue of ten. The exact split depends on whether the worker
        // got a turn during the burst, which differs between runtimes, so what is asserted is what
        // the bound promises rather than a number.
        var notifications = new QueuedNotificationSender(new EmailNotificationSender(sendSeconds: 1), capacity: 10, workers: 1);
    
        for (var number = 0; number < 30; number++) notifications.SendConfirmation(AConfirmation(number));
    
        var counts = notifications.Counts();
        Assert.Equal(30, counts.Accepted + counts.Rejected);
        Assert.True(counts.Queued <= 10, "the queue never grows past its bound");
        Assert.True(counts.Rejected > 0, "and the bound really did refuse some");
        // Not an equality: a worker may be part-way through sending one job, so it is neither
        // queued nor completed yet. At most one per worker can be in flight.
        var inFlight = counts.Accepted - counts.Completed - counts.Queued;
        Assert.InRange(inFlight, 0, 1);
        notifications.Close(drain: false);
    }
    
    [Fact]
    public void A_drained_shutdown_finishes_everything_it_accepted()
    {
        var slow = new EmailNotificationSender(sendSeconds: 0.02);
        var notifications = new QueuedNotificationSender(slow, capacity: 10, workers: 1);
        for (var number = 0; number < 10; number++) notifications.SendConfirmation(AConfirmation(number));
    
        notifications.Close(drain: true);
    
        var counts = notifications.Counts();
        Assert.Equal(10, counts.Accepted);
        Assert.Equal(10, counts.Completed);
        Assert.Equal(0, counts.Queued);
        Assert.Equal(10, slow.Sent);
    }
    ```
    
    Python:
    
    ```text
    def test_a_booking_does_not_wait_for_a_slow_confirmation(self):
        """The whole point of the queue: a one-second email must not cost the guest a second."""
        slow = EmailNotificationSender(send_seconds=1)
        notifications = QueuedNotificationSender(slow, capacity=10, workers=1)
        self.addCleanup(notifications.close, False)
    
        started = time.monotonic()
        notifications.send_confirmation(self._confirmation(1))
        handed_over = time.monotonic() - started
    
        self.assertLess(handed_over, 0.1, "handing the email to the queue must be immediate")
        self.assertEqual(0, slow.sent, "and the email has not been sent yet")
        self.assertEqual(1, notifications.counts().accepted)
    
    def test_a_burst_larger_than_the_bound_is_refused_and_counted(self):
        """Thirty confirmations into a queue of ten.
    
        The exact split depends on whether the worker got a turn during the burst, which differs
        between runtimes and between runs, so what is asserted here is what the bound actually
        promises rather than a number."""
        notifications = QueuedNotificationSender(EmailNotificationSender(send_seconds=1),
                                                 capacity=10, workers=1)
        self.addCleanup(notifications.close, False)
    
        for number in range(30):
            notifications.send_confirmation(self._confirmation(number))
    
        counts = notifications.counts()
        self.assertEqual(30, counts.accepted + counts.rejected, "every confirmation is accounted for")
        self.assertLessEqual(counts.queued, 10, "the queue never grows past its bound")
        self.assertGreater(counts.rejected, 0, "and the bound really did refuse some")
        # Not an equality: a worker may be part-way through sending one job, so it is neither
        # queued nor completed yet. At most one per worker can be in flight.
        in_flight = counts.accepted - counts.completed - counts.queued
        self.assertGreaterEqual(in_flight, 0)
        self.assertLessEqual(in_flight, 1, "at most one job per worker can be in flight")
    
    def test_a_drained_shutdown_finishes_everything_it_accepted(self):
        """accepted == completed and nothing left queued, which is what "owes nobody an email" means."""
        slow = EmailNotificationSender(send_seconds=0.02)
        notifications = QueuedNotificationSender(slow, capacity=10, workers=1)
        for number in range(10):
            notifications.send_confirmation(self._confirmation(number))
    
        notifications.close(drain=True)
    
        counts = notifications.counts()
        self.assertEqual(10, counts.accepted)
        self.assertEqual(10, counts.completed)
        self.assertEqual(0, counts.queued)
        self.assertEqual(10, slow.sent)
    ```
    
    Java:
    
    ```text
    @Test
    @Test
    void a_booking_does_not_wait_for_a_slow_confirmation() {
        // The whole point of the queue: a one-second email must not cost the guest a second.
        EmailNotificationSender slow = new EmailNotificationSender(1);
        QueuedNotificationSender notifications = new QueuedNotificationSender(slow, 10, 1);
    
        long started = System.nanoTime();
        notifications.sendConfirmation(aConfirmation(1));
        long handedOverMs = (System.nanoTime() - started) / 1_000_000;
    
        assertTrue(handedOverMs < 100, "handing the email to the queue must be immediate");
        assertEquals(0, slow.sent(), "and the email has not been sent yet");
        assertEquals(1, notifications.counts().accepted());
        notifications.close(false);
    }
    
    @Test
    @Test
    void a_burst_larger_than_the_bound_is_refused_and_counted() {
        // Thirty confirmations into a queue of ten. The exact split depends on whether the worker
        // got a turn during the burst - in Java it usually does, because its worker is a real
        // thread with nothing like a GIL holding it back - so what is asserted is what the bound
        // promises rather than a number.
        QueuedNotificationSender notifications =
                new QueuedNotificationSender(new EmailNotificationSender(1), 10, 1);
    
        for (int number = 0; number < 30; number++) {
            notifications.sendConfirmation(aConfirmation(number));
        }
    
        NotificationCounts counts = notifications.counts();
        assertEquals(30, counts.accepted() + counts.rejected());
        assertTrue(counts.queued() <= 10, "the queue never grows past its bound");
        assertTrue(counts.rejected() > 0, "and the bound really did refuse some");
        // Not an equality: a worker may be part-way through sending one job, so it is neither
        // queued nor completed yet. At most one per worker can be in flight.
        long inFlight = counts.accepted() - counts.completed() - counts.queued();
        assertTrue(inFlight >= 0 && inFlight <= 1, "at most one job per worker can be in flight");
        notifications.close(false);
    }
    
    @Test
    @Test
    void a_drained_shutdown_finishes_everything_it_accepted() {
        EmailNotificationSender slow = new EmailNotificationSender(0.02);
        QueuedNotificationSender notifications = new QueuedNotificationSender(slow, 10, 1);
        for (int number = 0; number < 10; number++) {
            notifications.sendConfirmation(aConfirmation(number));
        }
    
        notifications.close(true);
    
        NotificationCounts counts = notifications.counts();
        assertEquals(10, counts.accepted());
        assertEquals(10, counts.completed());
        assertEquals(0, counts.queued());
        assertEquals(10, slow.sent());
    }
    ```
    
    Go:
    
    ```text
    // The whole point of the queue: a one-second email must not cost the guest a second.
    func TestABookingDoesNotWaitForASlowConfirmation(t *testing.T) {
    	slow := &EmailNotificationSender{SendSeconds: 1}
    	queue := NewQueuedNotificationSender(slow, 10, 1)
    	defer queue.Close(false)
    
    	started := time.Now()
    	if err := queue.SendConfirmation(aConfirmation(1)); err != nil {
    		t.Fatalf("handing the email over must not fail: %v", err)
    	}
    	handedOver := time.Since(started)
    
    	if handedOver > 100*time.Millisecond {
    		t.Errorf("handing the email to the queue took %v, which is not immediate", handedOver)
    	}
    	if sent := slow.Sent(); sent != 0 {
    		t.Errorf("the email has been sent already: Sent() = %d, want 0", sent)
    	}
    	if accepted := queue.Counts().Accepted; accepted != 1 {
    		t.Errorf("Accepted = %d, want 1", accepted)
    	}
    }
    
    // Thirty confirmations into a queue of ten. The exact split depends on whether the worker got a turn
    // during the burst, which differs between runtimes, so what is asserted is what the bound promises
    // rather than a number.
    func TestABurstLargerThanTheBoundIsRefusedAndCounted(t *testing.T) {
    	queue := NewQueuedNotificationSender(&EmailNotificationSender{SendSeconds: 1}, 10, 1)
    	defer queue.Close(false)
    
    	for number := 0; number < 30; number++ {
    		_ = queue.SendConfirmation(aConfirmation(number))
    	}
    
    	counts := queue.Counts()
    	if total := counts.Accepted + counts.Rejected; total != 30 {
    		t.Errorf("accepted + rejected = %d, want 30: every confirmation must be accounted for", total)
    	}
    	if counts.Queued > 10 {
    		t.Errorf("queued = %d, which is past the bound of 10", counts.Queued)
    	}
    	if counts.Rejected == 0 {
    		t.Error("rejected = 0, so the bound refused nothing")
    	}
    	// Not an equality: a worker may be part-way through sending one job, so it is neither queued
    	// nor completed yet. At most one per worker can be in flight.
    	inFlight := counts.Accepted - counts.Completed - int64(counts.Queued)
    	if inFlight < 0 || inFlight > 1 {
    		t.Errorf("accepted %d, completed %d, queued %d: %d in flight, want 0 or 1",
    			counts.Accepted, counts.Completed, counts.Queued, inFlight)
    	}
    }
    
    // accepted == completed and nothing left queued, which is what "owes nobody an email" means.
    func TestADrainedShutdownFinishesEverythingItAccepted(t *testing.T) {
    	slow := &EmailNotificationSender{SendSeconds: 0.02}
    	queue := NewQueuedNotificationSender(slow, 10, 1)
    	for number := 0; number < 10; number++ {
    		_ = queue.SendConfirmation(aConfirmation(number))
    	}
    
    	queue.Close(true)
    
    	counts := queue.Counts()
    	if counts.Accepted != 10 || counts.Completed != 10 || counts.Queued != 0 {
    		t.Errorf("after a drained close: accepted=%d completed=%d queued=%d, want 10/10/0",
    			counts.Accepted, counts.Completed, counts.Queued)
    	}
    	if sent := slow.Sent(); sent != 10 {
    		t.Errorf("the sender sent %d emails, want 10", sent)
    	}
    }
    ```
    
    JavaScript:
    
    ```text
    test('a booking does not wait for a slow confirmation', async () => {
      // The whole point of the queue: a one-second email must not cost the guest a second.
      const slow = new EmailNotificationSender(1);
      const notifications = new QueuedNotificationSender(slow, 10, 1);
    
      const started = performance.now();
      notifications.sendConfirmation(confirmation(1));
      const handedOver = performance.now() - started;
    
      assert.ok(handedOver < 100, 'handing the email to the queue must be immediate');
      assert.equal(slow.sent, 0, 'and the email has not been sent yet');
      assert.equal(notifications.counts().accepted, 1);
      await notifications.close({ drain: false });
    });
    
    test('a burst larger than the bound is refused and counted', async () => {
      // Thirty confirmations into a queue of ten. The exact split depends on whether the worker got a
      // turn during the burst, which differs between runtimes, so what is asserted is what the bound
      // promises rather than a number.
      const notifications = new QueuedNotificationSender(new EmailNotificationSender(1), 10, 1);
    
      for (let number = 0; number < 30; number += 1) notifications.sendConfirmation(confirmation(number));
    
      const counts = notifications.counts();
      assert.equal(counts.accepted + counts.rejected, 30, 'every confirmation is accounted for');
      assert.ok(counts.queued <= 10, 'the queue never grows past its bound');
      assert.ok(counts.rejected > 0, 'and the bound really did refuse some');
      // Not an equality: a worker may be part-way through sending one job, so it is neither queued
      // nor completed yet. At most one per worker can be in flight.
      const inFlight = counts.accepted - counts.completed - counts.queued;
      assert.ok(inFlight >= 0 && inFlight <= 1, 'at most one job per worker can be in flight');
      await notifications.close({ drain: false });
    });
    
    test('a drained shutdown finishes everything it accepted', async () => {
      const slow = new EmailNotificationSender(0.02);
      const notifications = new QueuedNotificationSender(slow, 10, 1);
      for (let number = 0; number < 10; number += 1) notifications.sendConfirmation(confirmation(number));
    
      await notifications.close({ drain: true });
    
      const counts = notifications.counts();
      assert.equal(counts.accepted, 10);
      assert.equal(counts.completed, 10);
      assert.equal(counts.queued, 0);
      assert.equal(slow.sent, 10);
    });
    ```
    
    Dart:
    
    ```text
    test('a booking does not wait for a slow confirmation', () async {
      // The whole point of the queue: a one-second email must not cost the guest a second.
      final slow = EmailNotificationSender(sendSeconds: 1);
      final notifications = QueuedNotificationSender(slow, capacity: 10, workers: 1);
    
      final watch = Stopwatch()..start();
      notifications.sendConfirmation(aConfirmation(1));
      watch.stop();
    
      expect(watch.elapsedMilliseconds, lessThan(100),
          reason: 'handing the email to the queue must be immediate');
      expect(slow.sent, 0, reason: 'and the email has not been sent yet');
      expect(notifications.counts().accepted, 1);
      await notifications.close(drain: false);
    });
    
    test('a burst larger than the bound is refused and counted', () async {
      // Thirty confirmations into a queue of ten. The exact split depends on whether the worker got
      // a turn during the burst, which differs between runtimes, so what is asserted is what the
      // bound promises rather than a number.
      final notifications =
          QueuedNotificationSender(EmailNotificationSender(sendSeconds: 1), capacity: 10, workers: 1);
    
      for (var number = 0; number < 30; number++) {
        notifications.sendConfirmation(aConfirmation(number));
      }
    
      final counts = notifications.counts();
      expect(counts.accepted + counts.rejected, 30, reason: 'every confirmation is accounted for');
      expect(counts.queued, lessThanOrEqualTo(10), reason: 'the queue never grows past its bound');
      expect(counts.rejected, greaterThan(0), reason: 'and the bound really did refuse some');
      // Not an equality: a worker may be part-way through sending one job, so it is neither queued
      // nor completed yet. At most one per worker can be in flight.
      final inFlight = counts.accepted - counts.completed - counts.queued;
      expect(inFlight, inInclusiveRange(0, 1), reason: 'at most one job per worker can be in flight');
      await notifications.close(drain: false);
    });
    
    test('a drained shutdown finishes everything it accepted', () async {
      final slow = EmailNotificationSender(sendSeconds: 0.02);
      final notifications = QueuedNotificationSender(slow, capacity: 10, workers: 1);
      for (var number = 0; number < 10; number++) {
        notifications.sendConfirmation(aConfirmation(number));
      }
    
      await notifications.close(drain: true);
    
      final counts = notifications.counts();
      expect(counts.accepted, 10);
      expect(counts.completed, 10);
      expect(counts.queued, 0);
      expect(slow.sent, 10);
    });
    ```

**Legend:**
- **Two columns with headings.** A grouping: what the request does, and what happens away from the request. In this diagram: The left column is the request, which ends at box 4. The right column is everything that happens after the guest has been answered.
- **Yellow box.** Something outside the app, or the step where the guest is kept waiting. In this diagram: The request arriving and answering, and the confirmation being refused.
- **White box.** An ordinary step: one piece of the app doing one thing. In this diagram: A step the app takes: offering the job to the queue, and the worker sending it.
- **Grey box.** Something being held rather than done: the queue and what is sitting in it. In this diagram: The queue itself, holding at most ten jobs.
- **Green box.** The step that does the thing this diagram is about, or the outcome it is aiming at. In this diagram: The room being the guest’s, and a confirmation finally being sent.
- **Solid line with a filled arrowhead.** What happens next. Follow these in order. In this diagram: What happens next.
- **Green dotted line with a filled arrowhead.** Something that happens later, after the step at the start has already finished and answered. In this diagram: Something that happens after the request has already answered. The guest is not waiting for it.
- **Red dashed line with an open arrowhead.** The case the design refuses, counted rather than hidden. In this diagram: The confirmation the bound refuses: counted, never silent.
- **Number in front of a box label.** The order things happen in: 1 first, then 2, and so on. In this diagram: 1 to 4 are the request, in order. The unnumbered boxes happen later, away from it.

### What Step 4 adds

| What | Where | What it does |
| --- | --- | --- |
| A capacity | The queued sender | At most ten jobs, from HOTEL_QUEUE_CAPACITY |
| Offer, not put | The queued sender | One attempt that never waits: it either fits or it is refused |
| Four counters | The Notifications contract and GET /stats | accepted, rejected, completed and queued |

**The bound, and what it is for** In short: A maximum size. When the queue is full, a new job is refused and counted rather than waited for. Choosing the bound is choosing what to do when you are behind. Ten says: if more than ten confirmations are waiting, refuse new ones rather than promise them. The bound does not control throughput. Measured on the same twenty-job burst, one worker and three workers both accepted ten; the three-worker run only finished sooner, in 0.50 s against 1.14 s.

1. Start: the queue holds ten and the sender takes a second.
2. Action: thirty confirmations are offered to it.
3. Result:
Sending 30 bookings, one after another...

Accepted: 30
Rejected: 0
Slowest booking: 47 ms
Total: 0.1 s

GET /stats
{"notificationsAccepted":11,"notificationsRejected":19,"notificationsCompleted":0,"notificationsQueued":10}

Read the two lines together. Every booking was accepted - all 30 guests have their rooms - and
19 confirmation emails were refused on purpose, because the queue was full. The booking and the
email now succeed and fail separately, which is the whole point.

C#:

```text
// Never waits and never throws: the whole point is that the booking is not held up.
public void SendConfirmation(Confirmation confirmation)
{
    if (_queue.Writer.TryWrite(confirmation)) Interlocked.Increment(ref _accepted);
    else Interlocked.Increment(ref _rejected);
}
```

Python:

```text
def send_confirmation(self, confirmation: Confirmation) -> None:
    """Put the confirmation on the queue, or count it as refused. Never waits, never raises."""
    try:
        self._queue.put_nowait(confirmation)
    except queue.Full:
        with self._lock:
            self._rejected += 1
        return
    with self._lock:
        self._accepted += 1
```

Java:

```text
// Never waits and never throws: the whole point is that the booking is not held up.
@Override
public void sendConfirmation(Confirmation confirmation) {
    if (queue.offer(confirmation)) {
        accepted.incrementAndGet();
    } else {
        rejected.incrementAndGet();
    }
}
```

Go:

```text
// SendConfirmation never waits and never fails: the whole point is that the booking is not held up.
func (queue *QueuedNotificationSender) SendConfirmation(confirmation Confirmation) error {
	select {
	case queue.jobs <- confirmation:
		queue.accepted.Add(1)
	default:
		queue.rejected.Add(1)
	}
	return nil
}
```

JavaScript:

```text
// Never waits and never throws: the whole point is that the booking is not held up.
sendConfirmation(confirmation) {
  if (this.#jobs.length >= this.#capacity) {
    this.#rejected += 1;
    return;
  }
  this.#jobs.push(confirmation);
  this.#accepted += 1;
}
```

Dart:

```text
// Never waits and never throws: the whole point is that the booking is not held up.
@override
void sendConfirmation(Confirmation confirmation) {
  if (_jobs.length >= _capacity) {
    _rejected++;
    return;
  }
  _jobs.add(confirmation);
  _accepted++;
}
```

**The bound, and what it is for** In short: A maximum size. When the queue is full, a new job is refused and counted rather than waited for. Choosing the bound is choosing what to do when you are behind. Ten says: if more than ten confirmations are waiting, refuse new ones rather than promise them. The bound does not control throughput. Measured on the same twenty-job burst, one worker and three workers both accepted ten; the three-worker run only finished sooner, in 0.50 s against 1.14 s.

1. Start: the queue holds ten and the sender takes a second.
2. Action: thirty confirmations are offered to it.
3. Result:
Sending 30 bookings, one after another...

Accepted: 30
Rejected: 0
Slowest booking: 47 ms
Total: 0.1 s

GET /stats
{"notificationsAccepted":11,"notificationsRejected":19,"notificationsCompleted":0,"notificationsQueued":10}

Read the two lines together. Every booking was accepted - all 30 guests have their rooms - and
19 confirmation emails were refused on purpose, because the queue was full. The booking and the
email now succeed and fail separately, which is the whole point.

C#:

```text
// Never waits and never throws: the whole point is that the booking is not held up.
public void SendConfirmation(Confirmation confirmation)
{
    if (_queue.Writer.TryWrite(confirmation)) Interlocked.Increment(ref _accepted);
    else Interlocked.Increment(ref _rejected);
}
```

Python:

```text
def send_confirmation(self, confirmation: Confirmation) -> None:
    """Put the confirmation on the queue, or count it as refused. Never waits, never raises."""
    try:
        self._queue.put_nowait(confirmation)
    except queue.Full:
        with self._lock:
            self._rejected += 1
        return
    with self._lock:
        self._accepted += 1
```

Java:

```text
// Never waits and never throws: the whole point is that the booking is not held up.
@Override
public void sendConfirmation(Confirmation confirmation) {
    if (queue.offer(confirmation)) {
        accepted.incrementAndGet();
    } else {
        rejected.incrementAndGet();
    }
}
```

Go:

```text
// SendConfirmation never waits and never fails: the whole point is that the booking is not held up.
func (queue *QueuedNotificationSender) SendConfirmation(confirmation Confirmation) error {
	select {
	case queue.jobs <- confirmation:
		queue.accepted.Add(1)
	default:
		queue.rejected.Add(1)
	}
	return nil
}
```

JavaScript:

```text
// Never waits and never throws: the whole point is that the booking is not held up.
sendConfirmation(confirmation) {
  if (this.#jobs.length >= this.#capacity) {
    this.#rejected += 1;
    return;
  }
  this.#jobs.push(confirmation);
  this.#accepted += 1;
}
```

Dart:

```text
// Never waits and never throws: the whole point is that the booking is not held up.
@override
void sendConfirmation(Confirmation confirmation) {
  if (_jobs.length >= _capacity) {
    _rejected++;
    return;
  }
  _jobs.add(confirmation);
  _accepted++;
}
```

**The four counters, and the sum that must hold** In short: accepted is what the queue took, rejected what it refused, completed what the worker finished, queued what is still waiting. Once nothing is in flight, accepted = completed + queued. Every job the queue took is either done or still waiting, so if the two sides do not balance, a job was taken and neither finished nor kept. The caveat is real and it caught this practice’s own test out: a worker part-way through sending has taken the job off the queue and not completed it, so with one worker the sides can differ by one. The number to watch is rejected. Zero in normal running; climbing only under a genuine burst.

1. Start: a burst of thirty has just been sent.
2. Action: you read GET /stats, then read it again ten seconds later.
3. Result:
GET /stats, eleven seconds later
{"notificationsAccepted":11,"notificationsRejected":19,"notificationsCompleted":11,"notificationsQueued":0}

accepted 11 = completed 11 + queued 0. Everything the queue took, it finished.

text:

```text
# docs/openapi.yaml: the four new properties on the Stats schema
    notificationsAccepted:
      type: integer
      description: Confirmation emails the queue took.
    notificationsRejected:
      type: integer
      description: Confirmation emails refused because the queue was full. The booking still succeeded.
    notificationsCompleted:
      type: integer
      description: Confirmation emails the worker has finished sending.
    notificationsQueued:
      type: integer
      description: Confirmation emails waiting right now.
```

### How each language says “take it if it fits, and do not wait”

| Language | The idiom |
| --- | --- |
| C# | TryWrite on a bounded Channel whose FullMode is Wait |
| Python | Queue.put_nowait, catching queue.Full |
| Java | ArrayBlockingQueue.offer, not put |
| Go | a select with a default branch |
| JavaScript | a length check before pushing (no lock needed: one request runs at a time) |
| Dart | a length check before adding (no lock needed: one request runs at a time) |

### Completed example 4.1 · offering a job to the queue

In C#, reach for TryWrite on a channel whose FullMode is Wait. Do not use DropWrite, however much the name suggests it: DropWrite silently discards the job and still reports success, so every confirmation would count as accepted and a fifth of them would vanish without appearing in any counter.

**The bound, and what it is for** In short: A maximum size. When the queue is full, a new job is refused and counted rather than waited for. Choosing the bound is choosing what to do when you are behind. Ten says: if more than ten confirmations are waiting, refuse new ones rather than promise them. The bound does not control throughput. Measured on the same twenty-job burst, one worker and three workers both accepted ten; the three-worker run only finished sooner, in 0.50 s against 1.14 s.

1. Start: the queue holds ten and the sender takes a second.
2. Action: thirty confirmations are offered to it.
3. Result:
Sending 30 bookings, one after another...

Accepted: 30
Rejected: 0
Slowest booking: 47 ms
Total: 0.1 s

GET /stats
{"notificationsAccepted":11,"notificationsRejected":19,"notificationsCompleted":0,"notificationsQueued":10}

Read the two lines together. Every booking was accepted - all 30 guests have their rooms - and
19 confirmation emails were refused on purpose, because the queue was full. The booking and the
email now succeed and fail separately, which is the whole point.

C#:

```text
// Never waits and never throws: the whole point is that the booking is not held up.
public void SendConfirmation(Confirmation confirmation)
{
    if (_queue.Writer.TryWrite(confirmation)) Interlocked.Increment(ref _accepted);
    else Interlocked.Increment(ref _rejected);
}
```

Python:

```text
def send_confirmation(self, confirmation: Confirmation) -> None:
    """Put the confirmation on the queue, or count it as refused. Never waits, never raises."""
    try:
        self._queue.put_nowait(confirmation)
    except queue.Full:
        with self._lock:
            self._rejected += 1
        return
    with self._lock:
        self._accepted += 1
```

Java:

```text
// Never waits and never throws: the whole point is that the booking is not held up.
@Override
public void sendConfirmation(Confirmation confirmation) {
    if (queue.offer(confirmation)) {
        accepted.incrementAndGet();
    } else {
        rejected.incrementAndGet();
    }
}
```

Go:

```text
// SendConfirmation never waits and never fails: the whole point is that the booking is not held up.
func (queue *QueuedNotificationSender) SendConfirmation(confirmation Confirmation) error {
	select {
	case queue.jobs <- confirmation:
		queue.accepted.Add(1)
	default:
		queue.rejected.Add(1)
	}
	return nil
}
```

JavaScript:

```text
// Never waits and never throws: the whole point is that the booking is not held up.
sendConfirmation(confirmation) {
  if (this.#jobs.length >= this.#capacity) {
    this.#rejected += 1;
    return;
  }
  this.#jobs.push(confirmation);
  this.#accepted += 1;
}
```

Dart:

```text
// Never waits and never throws: the whole point is that the booking is not held up.
@override
void sendConfirmation(Confirmation confirmation) {
  if (_jobs.length >= _capacity) {
    _rejected++;
    return;
  }
  _jobs.add(confirmation);
  _accepted++;
}
```

### Offering a job

#### C#

```csharp
// Never waits and never throws: the whole point is that the booking is not held up.
public void SendConfirmation(Confirmation confirmation)
{
    if (_queue.Writer.TryWrite(confirmation)) Interlocked.Increment(ref _accepted);
    else Interlocked.Increment(ref _rejected);
}
```

#### Python

```python
def send_confirmation(self, confirmation: Confirmation) -> None:
    """Put the confirmation on the queue, or count it as refused. Never waits, never raises."""
    try:
        self._queue.put_nowait(confirmation)
    except queue.Full:
        with self._lock:
            self._rejected += 1
        return
    with self._lock:
        self._accepted += 1
```

#### Java

```java
// Never waits and never throws: the whole point is that the booking is not held up.
@Override
public void sendConfirmation(Confirmation confirmation) {
    if (queue.offer(confirmation)) {
        accepted.incrementAndGet();
    } else {
        rejected.incrementAndGet();
    }
}
```

#### Go

```go
// SendConfirmation never waits and never fails: the whole point is that the booking is not held up.
func (queue *QueuedNotificationSender) SendConfirmation(confirmation Confirmation) error {
	select {
	case queue.jobs <- confirmation:
		queue.accepted.Add(1)
	default:
		queue.rejected.Add(1)
	}
	return nil
}
```

#### JavaScript

```javascript
// Never waits and never throws: the whole point is that the booking is not held up.
sendConfirmation(confirmation) {
  if (this.#jobs.length >= this.#capacity) {
    this.#rejected += 1;
    return;
  }
  this.#jobs.push(confirmation);
  this.#accepted += 1;
}
```

#### Dart

```dart
// Never waits and never throws: the whole point is that the booking is not held up.
@override
void sendConfirmation(Confirmation confirmation) {
  if (_jobs.length >= _capacity) {
    _rejected++;
    return;
  }
  _jobs.add(confirmation);
  _accepted++;
}
```

### Completed example 4.2 · the four counters

**The four counters, and the sum that must hold** In short: accepted is what the queue took, rejected what it refused, completed what the worker finished, queued what is still waiting. Once nothing is in flight, accepted = completed + queued. Every job the queue took is either done or still waiting, so if the two sides do not balance, a job was taken and neither finished nor kept. The caveat is real and it caught this practice’s own test out: a worker part-way through sending has taken the job off the queue and not completed it, so with one worker the sides can differ by one. The number to watch is rejected. Zero in normal running; climbing only under a genuine burst.

1. Start: a burst of thirty has just been sent.
2. Action: you read GET /stats, then read it again ten seconds later.
3. Result:
GET /stats, eleven seconds later
{"notificationsAccepted":11,"notificationsRejected":19,"notificationsCompleted":11,"notificationsQueued":0}

accepted 11 = completed 11 + queued 0. Everything the queue took, it finished.

text:

```text
# docs/openapi.yaml: the four new properties on the Stats schema
    notificationsAccepted:
      type: integer
      description: Confirmation emails the queue took.
    notificationsRejected:
      type: integer
      description: Confirmation emails refused because the queue was full. The booking still succeeded.
    notificationsCompleted:
      type: integer
      description: Confirmation emails the worker has finished sending.
    notificationsQueued:
      type: integer
      description: Confirmation emails waiting right now.
```

### The counters

#### C#

```csharp
public NotificationCounts Counts() => new(
    Interlocked.Read(ref _accepted), Interlocked.Read(ref _rejected),
    Interlocked.Read(ref _completed), _queue.Reader.Count);
```

#### Python

```python
def counts(self) -> NotificationCounts:
    with self._lock:
        return NotificationCounts(self._accepted, self._rejected, self._completed, self._queue.qsize())
```

#### Java

```java
@Override
public NotificationCounts counts() {
    return new NotificationCounts(accepted.get(), rejected.get(), completed.get(), queue.size());
}
```

#### Go

```go
func (queue *QueuedNotificationSender) Counts() Counts {
	return Counts{
		Accepted:  queue.accepted.Load(),
		Rejected:  queue.rejected.Load(),
		Completed: queue.completed.Load(),
		Queued:    len(queue.jobs),
	}
}
```

#### JavaScript

```javascript
counts() {
  return {
    accepted: this.#accepted, rejected: this.#rejected,
    completed: this.#completed, queued: this.#jobs.length,
  };
}
```

#### Dart

```dart
@override
NotificationCounts counts() =>
    NotificationCounts(_accepted, _rejected, _completed, _jobs.length);
```

### Completed example 4.3 · the contract publishes them

The Api reads the counters through the Notifications contract, so it never imports the module’s internal code and the L03 boundary still holds.

### The Notifications contract

#### C#

```csharp
// HotelBooking/Notifications/NotificationCounts.cs
namespace HotelBooking.Notifications;

// What a queued sender has promised, refused, finished and still owes.
//
// Once nothing is in flight, Accepted == Completed + Queued. If that ever stops holding, work has
// been lost somewhere, and these four numbers are the only place it would show.
public readonly record struct NotificationCounts(long Accepted, long Rejected, long Completed, int Queued);

// A sender that publishes those counters. The Api takes this rather than the concrete class, so it
// never has to import Notifications' internal code and the module boundary of L03 still holds.
public interface ICountingNotificationSender : INotificationSender
{
    NotificationCounts Counts();
}
```

#### Python

```python
# hotel_booking/notifications/__init__.py
"""Notifications: telling guests what happened. Public contract: Confirmation, NotificationSender
and the four counters a queued sender publishes."""
from __future__ import annotations

from dataclasses import dataclass
from datetime import date
from typing import Protocol


@dataclass(frozen=True)
class Confirmation:
    """Notifications' own message: it no longer needs the Reservation class."""
    reservation_id: str
    guest_email: str
    room_number: str
    check_in: date
    check_out: date


class NotificationSender(Protocol):
    def send_confirmation(self, confirmation: Confirmation) -> None: ...


@dataclass(frozen=True)
class NotificationCounts:
    """What a queued sender has promised, refused, finished and still owes.

    Once nothing is in flight, accepted == completed + queued. If that ever fails to hold, work
    has been lost somewhere, and these counters are the only place it would show.
    """
    accepted: int
    rejected: int
    completed: int
    queued: int
```

#### Java

```java
// src/main/java/hotelbooking/notifications/NotificationCounts.java
package hotelbooking.notifications;

// What a queued sender has promised, refused, finished and still owes.
//
// Once nothing is in flight, accepted == completed + queued. If that ever stops holding, work has
// been lost somewhere, and these four numbers are the only place it would show.
public record NotificationCounts(long accepted, long rejected, long completed, int queued) {}
```

#### Go

```go
// notifications/notifications.go
package notifications

import "time"

// Confirmation is Notifications' own message: it no longer needs the Reservation type.
type Confirmation struct {
	ReservationID string
	GuestEmail    string
	RoomNumber    string
	CheckIn       time.Time
	CheckOut      time.Time
}

// NotificationSender is Notifications' public contract: "tell the guest".
// How the message travels is its own business. It reports a failure instead of hiding it.
type NotificationSender interface {
	SendConfirmation(confirmation Confirmation) error
}

// Counts is what a queued sender has promised, refused, finished and still owes.
//
// Once nothing is in flight, Accepted == Completed + Queued. If that ever stops holding, work has
// been lost somewhere, and these four numbers are the only place it would show.
type Counts struct {
	Accepted  int64
	Rejected  int64
	Completed int64
	Queued    int
}

// CountingNotificationSender is a sender that also publishes those counters, so the api package can
// read them without reaching into anything private and the module boundary of L03 still holds.
type CountingNotificationSender interface {
	NotificationSender
	Counts() Counts
}
```

#### JavaScript

```javascript
// src/notifications/index.js
// Notifications: telling guests what happened. This file is the module's public contract.
//
// A notification sender keeps one promise (JavaScript has no interface keyword):
//
//   sendConfirmation(confirmation)   tell this guest their booking is confirmed, or throw when the
//                                    message cannot be sent. It may answer with a promise, and
//                                    BookingService awaits it.
//
// A queued sender also publishes its counters:
//
//   counts()   { accepted, rejected, completed, queued }
//
// Once nothing is in flight, accepted === completed + queued. If that ever stops holding, work has
// been lost somewhere, and these four numbers are the only place it would show.

// Notifications' own message: it no longer needs the Reservation class.
export class Confirmation {
  constructor(reservationId, guestEmail, roomNumber, checkIn, checkOut) {
    Object.assign(this, { reservationId, guestEmail, roomNumber, checkIn, checkOut });
    Object.freeze(this);
  }
}
```

#### Dart

```dart
// lib/notifications/notifications.dart
/// Notifications: telling guests what happened. This file is the module's public contract.
library;

import 'dart:async';

/// Notifications' own message: it no longer needs the Reservation class.
class Confirmation {
  const Confirmation(this.reservationId, this.guestEmail, this.roomNumber, this.checkIn, this.checkOut);

  final String reservationId;
  final String guestEmail;
  final String roomNumber;
  final DateTime checkIn;
  final DateTime checkOut;
}

/// What a queued sender has promised, refused, finished and still owes.
///
/// Once nothing is in flight, accepted == completed + queued. If that ever stops holding, work has
/// been lost somewhere, and these four numbers are the only place it would show.
class NotificationCounts {
  const NotificationCounts(this.accepted, this.rejected, this.completed, this.queued);

  final int accepted;
  final int rejected;
  final int completed;
  final int queued;
}

// Notifications' public contract: "tell the guest". How the message travels is its own business,
// and it may answer with a Future - BookingService awaits it.
abstract interface class NotificationSender {
  FutureOr<void> sendConfirmation(Confirmation confirmation);
}

/// A sender that also publishes its counters, so the Api can read them without importing
/// Notifications' internal code and the module boundary of L03 still holds.
abstract interface class CountingNotificationSender implements NotificationSender {
  NotificationCounts counts();
}
```

### Completed example 4.4 · GET /stats gains four properties

### docs/openapi.yaml

```text
# docs/openapi.yaml: the four new properties on the Stats schema
    notificationsAccepted:
      type: integer
      description: Confirmation emails the queue took.
    notificationsRejected:
      type: integer
      description: Confirmation emails refused because the queue was full. The booking still succeeded.
    notificationsCompleted:
      type: integer
      description: Confirmation emails the worker has finished sending.
    notificationsQueued:
      type: integer
      description: Confirmation emails waiting right now.
```

### Your turn · bound it

1. Give the queued sender a capacity, read from HOTEL_QUEUE_CAPACITY with a default of 10.
2. Change the send to offer the job and count it as accepted or rejected (4.1). It must never wait.
3. Add the four counters (4.2) and publish them from the Notifications contract (4.3).
4. Add the four properties to the Stats schema in docs/openapi.yaml (4.4) and raise the version to 1.2.0.
5. Make GET /stats include them when the app is wired with a queue.
6. Run your checks: still 19 green. The three new tests come in Step 5.

**When you finish, you have:** A bounded queue with four counters, visible on GET /stats, and 19 green checks.
### Explanation

Choosing the bound is choosing what to do when you are behind. Ten says: if more than ten confirmations are waiting, refuse new ones rather than promise them. It is a decision, and ADR-004 is where you write it down.



---

## 6. Step 5 · Backpressure: refuse, and say so

Run the same burst against the bounded queue. Every booking still succeeds, and the overflow is now a number instead of a memory leak.





### Run it

### Two terminals

#### C#

```text
# terminal 1
$env:HOTEL_EMAIL_SECONDS = "1"; dotnet run --project HotelBooking
# terminal 2
dotnet run tools/burst.cs
```

#### Python

```text
# terminal 1
$env:HOTEL_EMAIL_SECONDS = "1"; python main.py
# terminal 2
python tools/burst.py
```

#### Java

```text
# terminal 1
$env:HOTEL_EMAIL_SECONDS = "1"; mvn -q spring-boot:run
# terminal 2
java tools/Burst.java
```

#### Go

```text
# terminal 1
$env:HOTEL_EMAIL_SECONDS = "1"; go run .
# terminal 2
go run ./tools/burst
```

#### JavaScript

```text
# terminal 1
$env:HOTEL_EMAIL_SECONDS = "1"; node main.js
# terminal 2
node tools/burst.js
```

#### Dart

```text
# terminal 1
$env:HOTEL_EMAIL_SECONDS = "1"; dart run bin/main.dart
# terminal 2
dart run tool/burst.dart
```

### What you will see

**Backpressure** In short: Letting the slow consumer push back on the fast producer: the queue refuses, loudly and countably, instead of piling up. A full queue has three options: make the producer wait, refuse the work, or throw away something already queued. Waiting is the thing this design exists to avoid, and a confirmation email is not superseded by a newer one, so this one refuses. The refusal has to be counted or it is not backpressure, it is loss. A queue that silently dropped the same jobs would look identical to one that was coping. What is not refused is the booking. The guest gets their room and a 201. That separation is the R6 rule from P04 again.

1. Start: the queue is full and another booking is confirmed.
2. Action: the confirmation is offered to the queue, which refuses it.
3. Result: rejected goes up by one, the booking still answers 201 Created, and nothing waits.

### Terminal 2

```text
Sending 30 bookings, one after another...

Accepted: 30
Rejected: 0
Slowest booking: 47 ms
Total: 0.1 s

GET /stats
{"notificationsAccepted":11,"notificationsRejected":19,"notificationsCompleted":0,"notificationsQueued":10}

Read the two lines together. Every booking was accepted - all 30 guests have their rooms - and
19 confirmation emails were refused on purpose, because the queue was full. The booking and the
email now succeed and fail separately, which is the whole point.
```

### And once the worker has caught up

### GET /stats, eleven seconds later

```text
GET /stats, eleven seconds later
{"notificationsAccepted":11,"notificationsRejected":19,"notificationsCompleted":11,"notificationsQueued":0}

accepted 11 = completed 11 + queued 0. Everything the queue took, it finished.
```

### Why your numbers may differ from these

**Why your numbers may differ from the slides** In short: The exact accepted/rejected split depends on whether the worker got a turn during the burst, which differs between languages and runs. The exact split depends on how fast the burst arrives compared with the worker.
Over HTTP the burst takes about 0.1 s, long enough for the worker to take one job and free a slot,
so it is 11 and 19. Driven in-process, the burst is over in under a millisecond and the worker has
not moved, so it is 10 and 20. Do not memorise either number. Three things are always true:

  1. queued never goes above the bound. That is what "bounded" means.
  2. every booking succeeds. A refused email is not a refused room.
  3. accepted = completed + queued + whatever is in flight. A worker part-way through sending a
     job has taken it off the queue and not finished it, so at most one job per worker sits in
     neither number. Once nothing is in flight the two sides balance exactly, and if they still
     do not, work was lost.

1. Start: the same burst of thirty into the same queue of ten.
2. Action: you run it over HTTP, and then drive the queue directly from a test.
3. Result: 11 and 19 over HTTP, 10 and 20 in-process. Both are correct; the three invariants hold in each.

### The three things that are always true

```text
The exact split depends on how fast the burst arrives compared with the worker.
Over HTTP the burst takes about 0.1 s, long enough for the worker to take one job and free a slot,
so it is 11 and 19. Driven in-process, the burst is over in under a millisecond and the worker has
not moved, so it is 10 and 20. Do not memorise either number. Three things are always true:

  1. queued never goes above the bound. That is what "bounded" means.
  2. every booking succeeds. A refused email is not a refused room.
  3. accepted = completed + queued + whatever is in flight. A worker part-way through sending a
     job has taken it off the queue and not finished it, so at most one job per worker sits in
     neither number. Once nothing is in flight the two sides balance exactly, and if they still
     do not, work was lost.
```

### What a refused confirmation is, and is not

|  |  |
| --- | --- |
| Is it a failed booking? | No. All 30 guests have their rooms. This is the R6 rule from P04 again: the booking is already saved, so a failed message must not undo it or hide it. |
| Is it free? | No. Nineteen guests have a reservation nobody told them about. That is a real cost paid by real people. |
| So why is it acceptable? | Because it is counted and visible. A queue that silently dropped the same jobs would look exactly like a queue that was coping. |
| What should rejected be normally? | Zero. Climbing when nothing unusual is happening means the bound, the worker count or the sender is wrong — and queued tells you which. |

**The four counters, and the sum that must hold** In short: accepted is what the queue took, rejected what it refused, completed what the worker finished, queued what is still waiting. Once nothing is in flight, accepted = completed + queued. Every job the queue took is either done or still waiting, so if the two sides do not balance, a job was taken and neither finished nor kept. The caveat is real and it caught this practice’s own test out: a worker part-way through sending has taken the job off the queue and not completed it, so with one worker the sides can differ by one. The number to watch is rejected. Zero in normal running; climbing only under a genuine burst.

1. Start: a burst of thirty has just been sent.
2. Action: you read GET /stats, then read it again ten seconds later.
3. Result:
GET /stats, eleven seconds later
{"notificationsAccepted":11,"notificationsRejected":19,"notificationsCompleted":11,"notificationsQueued":0}

accepted 11 = completed 11 + queued 0. Everything the queue took, it finished.

text:

```text
# docs/openapi.yaml: the four new properties on the Stats schema
    notificationsAccepted:
      type: integer
      description: Confirmation emails the queue took.
    notificationsRejected:
      type: integer
      description: Confirmation emails refused because the queue was full. The booking still succeeded.
    notificationsCompleted:
      type: integer
      description: Confirmation emails the worker has finished sending.
    notificationsQueued:
      type: integer
      description: Confirmation emails waiting right now.
```

### Completed example 5.1 · the three tests

The burst test asserts the invariants rather than an exact split, because the split depends on whether the worker got a turn and that differs between languages and between runs.

**What counts as evidence that it works** In short: The booking answers in milliseconds with a one-second sender, the bound refuses countably, and three tests keep it that way. The burst output and GET /stats are the evidence for today. The three tests are the evidence for next month, when somebody changes this code and has forgotten the afternoon. One test proves the request does not wait. One proves the bound really refuses, asserting the invariants rather than a brittle number. One proves a drained shutdown finishes everything it accepted.

1. Start: the queue is in place and the sender takes a second.
2. Action: you run your checks and then the burst tool.
3. Result: 22 green checks, and:
BookingService.reserve(Maya, R7, 1-2 December)
  Accepted in under 1 ms
  completed=0  queued=1

The same booking, with the same one-second email sender. The email has not been sent: it is on the
queue, and the worker will send it. Over HTTP the whole request comes back in about 30 ms, almost
all of which is HTTP and the database rather than anything to do with the email.

C#:

```text
[Fact]
public void A_booking_does_not_wait_for_a_slow_confirmation()
{
    // The whole point of the queue: a one-second email must not cost the guest a second.
    var slow = new EmailNotificationSender(sendSeconds: 1);
    var notifications = new QueuedNotificationSender(slow, capacity: 10, workers: 1);

    var watch = Stopwatch.StartNew();
    notifications.SendConfirmation(AConfirmation(1));
    watch.Stop();

    Assert.True(watch.ElapsedMilliseconds < 100, "handing the email to the queue must be immediate");
    Assert.Equal(0, slow.Sent);
    Assert.Equal(1, notifications.Counts().Accepted);
    notifications.Close(drain: false);
}

[Fact]
public void A_burst_larger_than_the_bound_is_refused_and_counted()
{
    // Thirty confirmations into a queue of ten. The exact split depends on whether the worker
    // got a turn during the burst, which differs between runtimes, so what is asserted is what
    // the bound promises rather than a number.
    var notifications = new QueuedNotificationSender(new EmailNotificationSender(sendSeconds: 1), capacity: 10, workers: 1);

    for (var number = 0; number < 30; number++) notifications.SendConfirmation(AConfirmation(number));

    var counts = notifications.Counts();
    Assert.Equal(30, counts.Accepted + counts.Rejected);
    Assert.True(counts.Queued <= 10, "the queue never grows past its bound");
    Assert.True(counts.Rejected > 0, "and the bound really did refuse some");
    // Not an equality: a worker may be part-way through sending one job, so it is neither
    // queued nor completed yet. At most one per worker can be in flight.
    var inFlight = counts.Accepted - counts.Completed - counts.Queued;
    Assert.InRange(inFlight, 0, 1);
    notifications.Close(drain: false);
}

[Fact]
public void A_drained_shutdown_finishes_everything_it_accepted()
{
    var slow = new EmailNotificationSender(sendSeconds: 0.02);
    var notifications = new QueuedNotificationSender(slow, capacity: 10, workers: 1);
    for (var number = 0; number < 10; number++) notifications.SendConfirmation(AConfirmation(number));

    notifications.Close(drain: true);

    var counts = notifications.Counts();
    Assert.Equal(10, counts.Accepted);
    Assert.Equal(10, counts.Completed);
    Assert.Equal(0, counts.Queued);
    Assert.Equal(10, slow.Sent);
}
```

Python:

```text
def test_a_booking_does_not_wait_for_a_slow_confirmation(self):
    """The whole point of the queue: a one-second email must not cost the guest a second."""
    slow = EmailNotificationSender(send_seconds=1)
    notifications = QueuedNotificationSender(slow, capacity=10, workers=1)
    self.addCleanup(notifications.close, False)

    started = time.monotonic()
    notifications.send_confirmation(self._confirmation(1))
    handed_over = time.monotonic() - started

    self.assertLess(handed_over, 0.1, "handing the email to the queue must be immediate")
    self.assertEqual(0, slow.sent, "and the email has not been sent yet")
    self.assertEqual(1, notifications.counts().accepted)

def test_a_burst_larger_than_the_bound_is_refused_and_counted(self):
    """Thirty confirmations into a queue of ten.

    The exact split depends on whether the worker got a turn during the burst, which differs
    between runtimes and between runs, so what is asserted here is what the bound actually
    promises rather than a number."""
    notifications = QueuedNotificationSender(EmailNotificationSender(send_seconds=1),
                                             capacity=10, workers=1)
    self.addCleanup(notifications.close, False)

    for number in range(30):
        notifications.send_confirmation(self._confirmation(number))

    counts = notifications.counts()
    self.assertEqual(30, counts.accepted + counts.rejected, "every confirmation is accounted for")
    self.assertLessEqual(counts.queued, 10, "the queue never grows past its bound")
    self.assertGreater(counts.rejected, 0, "and the bound really did refuse some")
    # Not an equality: a worker may be part-way through sending one job, so it is neither
    # queued nor completed yet. At most one per worker can be in flight.
    in_flight = counts.accepted - counts.completed - counts.queued
    self.assertGreaterEqual(in_flight, 0)
    self.assertLessEqual(in_flight, 1, "at most one job per worker can be in flight")

def test_a_drained_shutdown_finishes_everything_it_accepted(self):
    """accepted == completed and nothing left queued, which is what "owes nobody an email" means."""
    slow = EmailNotificationSender(send_seconds=0.02)
    notifications = QueuedNotificationSender(slow, capacity=10, workers=1)
    for number in range(10):
        notifications.send_confirmation(self._confirmation(number))

    notifications.close(drain=True)

    counts = notifications.counts()
    self.assertEqual(10, counts.accepted)
    self.assertEqual(10, counts.completed)
    self.assertEqual(0, counts.queued)
    self.assertEqual(10, slow.sent)
```

Java:

```text
@Test
@Test
void a_booking_does_not_wait_for_a_slow_confirmation() {
    // The whole point of the queue: a one-second email must not cost the guest a second.
    EmailNotificationSender slow = new EmailNotificationSender(1);
    QueuedNotificationSender notifications = new QueuedNotificationSender(slow, 10, 1);

    long started = System.nanoTime();
    notifications.sendConfirmation(aConfirmation(1));
    long handedOverMs = (System.nanoTime() - started) / 1_000_000;

    assertTrue(handedOverMs < 100, "handing the email to the queue must be immediate");
    assertEquals(0, slow.sent(), "and the email has not been sent yet");
    assertEquals(1, notifications.counts().accepted());
    notifications.close(false);
}

@Test
@Test
void a_burst_larger_than_the_bound_is_refused_and_counted() {
    // Thirty confirmations into a queue of ten. The exact split depends on whether the worker
    // got a turn during the burst - in Java it usually does, because its worker is a real
    // thread with nothing like a GIL holding it back - so what is asserted is what the bound
    // promises rather than a number.
    QueuedNotificationSender notifications =
            new QueuedNotificationSender(new EmailNotificationSender(1), 10, 1);

    for (int number = 0; number < 30; number++) {
        notifications.sendConfirmation(aConfirmation(number));
    }

    NotificationCounts counts = notifications.counts();
    assertEquals(30, counts.accepted() + counts.rejected());
    assertTrue(counts.queued() <= 10, "the queue never grows past its bound");
    assertTrue(counts.rejected() > 0, "and the bound really did refuse some");
    // Not an equality: a worker may be part-way through sending one job, so it is neither
    // queued nor completed yet. At most one per worker can be in flight.
    long inFlight = counts.accepted() - counts.completed() - counts.queued();
    assertTrue(inFlight >= 0 && inFlight <= 1, "at most one job per worker can be in flight");
    notifications.close(false);
}

@Test
@Test
void a_drained_shutdown_finishes_everything_it_accepted() {
    EmailNotificationSender slow = new EmailNotificationSender(0.02);
    QueuedNotificationSender notifications = new QueuedNotificationSender(slow, 10, 1);
    for (int number = 0; number < 10; number++) {
        notifications.sendConfirmation(aConfirmation(number));
    }

    notifications.close(true);

    NotificationCounts counts = notifications.counts();
    assertEquals(10, counts.accepted());
    assertEquals(10, counts.completed());
    assertEquals(0, counts.queued());
    assertEquals(10, slow.sent());
}
```

Go:

```text
// The whole point of the queue: a one-second email must not cost the guest a second.
func TestABookingDoesNotWaitForASlowConfirmation(t *testing.T) {
	slow := &EmailNotificationSender{SendSeconds: 1}
	queue := NewQueuedNotificationSender(slow, 10, 1)
	defer queue.Close(false)

	started := time.Now()
	if err := queue.SendConfirmation(aConfirmation(1)); err != nil {
		t.Fatalf("handing the email over must not fail: %v", err)
	}
	handedOver := time.Since(started)

	if handedOver > 100*time.Millisecond {
		t.Errorf("handing the email to the queue took %v, which is not immediate", handedOver)
	}
	if sent := slow.Sent(); sent != 0 {
		t.Errorf("the email has been sent already: Sent() = %d, want 0", sent)
	}
	if accepted := queue.Counts().Accepted; accepted != 1 {
		t.Errorf("Accepted = %d, want 1", accepted)
	}
}

// Thirty confirmations into a queue of ten. The exact split depends on whether the worker got a turn
// during the burst, which differs between runtimes, so what is asserted is what the bound promises
// rather than a number.
func TestABurstLargerThanTheBoundIsRefusedAndCounted(t *testing.T) {
	queue := NewQueuedNotificationSender(&EmailNotificationSender{SendSeconds: 1}, 10, 1)
	defer queue.Close(false)

	for number := 0; number < 30; number++ {
		_ = queue.SendConfirmation(aConfirmation(number))
	}

	counts := queue.Counts()
	if total := counts.Accepted + counts.Rejected; total != 30 {
		t.Errorf("accepted + rejected = %d, want 30: every confirmation must be accounted for", total)
	}
	if counts.Queued > 10 {
		t.Errorf("queued = %d, which is past the bound of 10", counts.Queued)
	}
	if counts.Rejected == 0 {
		t.Error("rejected = 0, so the bound refused nothing")
	}
	// Not an equality: a worker may be part-way through sending one job, so it is neither queued
	// nor completed yet. At most one per worker can be in flight.
	inFlight := counts.Accepted - counts.Completed - int64(counts.Queued)
	if inFlight < 0 || inFlight > 1 {
		t.Errorf("accepted %d, completed %d, queued %d: %d in flight, want 0 or 1",
			counts.Accepted, counts.Completed, counts.Queued, inFlight)
	}
}

// accepted == completed and nothing left queued, which is what "owes nobody an email" means.
func TestADrainedShutdownFinishesEverythingItAccepted(t *testing.T) {
	slow := &EmailNotificationSender{SendSeconds: 0.02}
	queue := NewQueuedNotificationSender(slow, 10, 1)
	for number := 0; number < 10; number++ {
		_ = queue.SendConfirmation(aConfirmation(number))
	}

	queue.Close(true)

	counts := queue.Counts()
	if counts.Accepted != 10 || counts.Completed != 10 || counts.Queued != 0 {
		t.Errorf("after a drained close: accepted=%d completed=%d queued=%d, want 10/10/0",
			counts.Accepted, counts.Completed, counts.Queued)
	}
	if sent := slow.Sent(); sent != 10 {
		t.Errorf("the sender sent %d emails, want 10", sent)
	}
}
```

JavaScript:

```text
test('a booking does not wait for a slow confirmation', async () => {
  // The whole point of the queue: a one-second email must not cost the guest a second.
  const slow = new EmailNotificationSender(1);
  const notifications = new QueuedNotificationSender(slow, 10, 1);

  const started = performance.now();
  notifications.sendConfirmation(confirmation(1));
  const handedOver = performance.now() - started;

  assert.ok(handedOver < 100, 'handing the email to the queue must be immediate');
  assert.equal(slow.sent, 0, 'and the email has not been sent yet');
  assert.equal(notifications.counts().accepted, 1);
  await notifications.close({ drain: false });
});

test('a burst larger than the bound is refused and counted', async () => {
  // Thirty confirmations into a queue of ten. The exact split depends on whether the worker got a
  // turn during the burst, which differs between runtimes, so what is asserted is what the bound
  // promises rather than a number.
  const notifications = new QueuedNotificationSender(new EmailNotificationSender(1), 10, 1);

  for (let number = 0; number < 30; number += 1) notifications.sendConfirmation(confirmation(number));

  const counts = notifications.counts();
  assert.equal(counts.accepted + counts.rejected, 30, 'every confirmation is accounted for');
  assert.ok(counts.queued <= 10, 'the queue never grows past its bound');
  assert.ok(counts.rejected > 0, 'and the bound really did refuse some');
  // Not an equality: a worker may be part-way through sending one job, so it is neither queued
  // nor completed yet. At most one per worker can be in flight.
  const inFlight = counts.accepted - counts.completed - counts.queued;
  assert.ok(inFlight >= 0 && inFlight <= 1, 'at most one job per worker can be in flight');
  await notifications.close({ drain: false });
});

test('a drained shutdown finishes everything it accepted', async () => {
  const slow = new EmailNotificationSender(0.02);
  const notifications = new QueuedNotificationSender(slow, 10, 1);
  for (let number = 0; number < 10; number += 1) notifications.sendConfirmation(confirmation(number));

  await notifications.close({ drain: true });

  const counts = notifications.counts();
  assert.equal(counts.accepted, 10);
  assert.equal(counts.completed, 10);
  assert.equal(counts.queued, 0);
  assert.equal(slow.sent, 10);
});
```

Dart:

```text
test('a booking does not wait for a slow confirmation', () async {
  // The whole point of the queue: a one-second email must not cost the guest a second.
  final slow = EmailNotificationSender(sendSeconds: 1);
  final notifications = QueuedNotificationSender(slow, capacity: 10, workers: 1);

  final watch = Stopwatch()..start();
  notifications.sendConfirmation(aConfirmation(1));
  watch.stop();

  expect(watch.elapsedMilliseconds, lessThan(100),
      reason: 'handing the email to the queue must be immediate');
  expect(slow.sent, 0, reason: 'and the email has not been sent yet');
  expect(notifications.counts().accepted, 1);
  await notifications.close(drain: false);
});

test('a burst larger than the bound is refused and counted', () async {
  // Thirty confirmations into a queue of ten. The exact split depends on whether the worker got
  // a turn during the burst, which differs between runtimes, so what is asserted is what the
  // bound promises rather than a number.
  final notifications =
      QueuedNotificationSender(EmailNotificationSender(sendSeconds: 1), capacity: 10, workers: 1);

  for (var number = 0; number < 30; number++) {
    notifications.sendConfirmation(aConfirmation(number));
  }

  final counts = notifications.counts();
  expect(counts.accepted + counts.rejected, 30, reason: 'every confirmation is accounted for');
  expect(counts.queued, lessThanOrEqualTo(10), reason: 'the queue never grows past its bound');
  expect(counts.rejected, greaterThan(0), reason: 'and the bound really did refuse some');
  // Not an equality: a worker may be part-way through sending one job, so it is neither queued
  // nor completed yet. At most one per worker can be in flight.
  final inFlight = counts.accepted - counts.completed - counts.queued;
  expect(inFlight, inInclusiveRange(0, 1), reason: 'at most one job per worker can be in flight');
  await notifications.close(drain: false);
});

test('a drained shutdown finishes everything it accepted', () async {
  final slow = EmailNotificationSender(sendSeconds: 0.02);
  final notifications = QueuedNotificationSender(slow, capacity: 10, workers: 1);
  for (var number = 0; number < 10; number++) {
    notifications.sendConfirmation(aConfirmation(number));
  }

  await notifications.close(drain: true);

  final counts = notifications.counts();
  expect(counts.accepted, 10);
  expect(counts.completed, 10);
  expect(counts.queued, 0);
  expect(slow.sent, 10);
});
```

### The three new tests

#### C#

```csharp
[Fact]
public void A_booking_does_not_wait_for_a_slow_confirmation()
{
    // The whole point of the queue: a one-second email must not cost the guest a second.
    var slow = new EmailNotificationSender(sendSeconds: 1);
    var notifications = new QueuedNotificationSender(slow, capacity: 10, workers: 1);

    var watch = Stopwatch.StartNew();
    notifications.SendConfirmation(AConfirmation(1));
    watch.Stop();

    Assert.True(watch.ElapsedMilliseconds < 100, "handing the email to the queue must be immediate");
    Assert.Equal(0, slow.Sent);
    Assert.Equal(1, notifications.Counts().Accepted);
    notifications.Close(drain: false);
}

[Fact]
public void A_burst_larger_than_the_bound_is_refused_and_counted()
{
    // Thirty confirmations into a queue of ten. The exact split depends on whether the worker
    // got a turn during the burst, which differs between runtimes, so what is asserted is what
    // the bound promises rather than a number.
    var notifications = new QueuedNotificationSender(new EmailNotificationSender(sendSeconds: 1), capacity: 10, workers: 1);

    for (var number = 0; number < 30; number++) notifications.SendConfirmation(AConfirmation(number));

    var counts = notifications.Counts();
    Assert.Equal(30, counts.Accepted + counts.Rejected);
    Assert.True(counts.Queued <= 10, "the queue never grows past its bound");
    Assert.True(counts.Rejected > 0, "and the bound really did refuse some");
    // Not an equality: a worker may be part-way through sending one job, so it is neither
    // queued nor completed yet. At most one per worker can be in flight.
    var inFlight = counts.Accepted - counts.Completed - counts.Queued;
    Assert.InRange(inFlight, 0, 1);
    notifications.Close(drain: false);
}

[Fact]
public void A_drained_shutdown_finishes_everything_it_accepted()
{
    var slow = new EmailNotificationSender(sendSeconds: 0.02);
    var notifications = new QueuedNotificationSender(slow, capacity: 10, workers: 1);
    for (var number = 0; number < 10; number++) notifications.SendConfirmation(AConfirmation(number));

    notifications.Close(drain: true);

    var counts = notifications.Counts();
    Assert.Equal(10, counts.Accepted);
    Assert.Equal(10, counts.Completed);
    Assert.Equal(0, counts.Queued);
    Assert.Equal(10, slow.Sent);
}
```

#### Python

```python
def test_a_booking_does_not_wait_for_a_slow_confirmation(self):
    """The whole point of the queue: a one-second email must not cost the guest a second."""
    slow = EmailNotificationSender(send_seconds=1)
    notifications = QueuedNotificationSender(slow, capacity=10, workers=1)
    self.addCleanup(notifications.close, False)

    started = time.monotonic()
    notifications.send_confirmation(self._confirmation(1))
    handed_over = time.monotonic() - started

    self.assertLess(handed_over, 0.1, "handing the email to the queue must be immediate")
    self.assertEqual(0, slow.sent, "and the email has not been sent yet")
    self.assertEqual(1, notifications.counts().accepted)

def test_a_burst_larger_than_the_bound_is_refused_and_counted(self):
    """Thirty confirmations into a queue of ten.

    The exact split depends on whether the worker got a turn during the burst, which differs
    between runtimes and between runs, so what is asserted here is what the bound actually
    promises rather than a number."""
    notifications = QueuedNotificationSender(EmailNotificationSender(send_seconds=1),
                                             capacity=10, workers=1)
    self.addCleanup(notifications.close, False)

    for number in range(30):
        notifications.send_confirmation(self._confirmation(number))

    counts = notifications.counts()
    self.assertEqual(30, counts.accepted + counts.rejected, "every confirmation is accounted for")
    self.assertLessEqual(counts.queued, 10, "the queue never grows past its bound")
    self.assertGreater(counts.rejected, 0, "and the bound really did refuse some")
    # Not an equality: a worker may be part-way through sending one job, so it is neither
    # queued nor completed yet. At most one per worker can be in flight.
    in_flight = counts.accepted - counts.completed - counts.queued
    self.assertGreaterEqual(in_flight, 0)
    self.assertLessEqual(in_flight, 1, "at most one job per worker can be in flight")

def test_a_drained_shutdown_finishes_everything_it_accepted(self):
    """accepted == completed and nothing left queued, which is what "owes nobody an email" means."""
    slow = EmailNotificationSender(send_seconds=0.02)
    notifications = QueuedNotificationSender(slow, capacity=10, workers=1)
    for number in range(10):
        notifications.send_confirmation(self._confirmation(number))

    notifications.close(drain=True)

    counts = notifications.counts()
    self.assertEqual(10, counts.accepted)
    self.assertEqual(10, counts.completed)
    self.assertEqual(0, counts.queued)
    self.assertEqual(10, slow.sent)
```

#### Java

```java
@Test
@Test
void a_booking_does_not_wait_for_a_slow_confirmation() {
    // The whole point of the queue: a one-second email must not cost the guest a second.
    EmailNotificationSender slow = new EmailNotificationSender(1);
    QueuedNotificationSender notifications = new QueuedNotificationSender(slow, 10, 1);

    long started = System.nanoTime();
    notifications.sendConfirmation(aConfirmation(1));
    long handedOverMs = (System.nanoTime() - started) / 1_000_000;

    assertTrue(handedOverMs < 100, "handing the email to the queue must be immediate");
    assertEquals(0, slow.sent(), "and the email has not been sent yet");
    assertEquals(1, notifications.counts().accepted());
    notifications.close(false);
}

@Test
@Test
void a_burst_larger_than_the_bound_is_refused_and_counted() {
    // Thirty confirmations into a queue of ten. The exact split depends on whether the worker
    // got a turn during the burst - in Java it usually does, because its worker is a real
    // thread with nothing like a GIL holding it back - so what is asserted is what the bound
    // promises rather than a number.
    QueuedNotificationSender notifications =
            new QueuedNotificationSender(new EmailNotificationSender(1), 10, 1);

    for (int number = 0; number < 30; number++) {
        notifications.sendConfirmation(aConfirmation(number));
    }

    NotificationCounts counts = notifications.counts();
    assertEquals(30, counts.accepted() + counts.rejected());
    assertTrue(counts.queued() <= 10, "the queue never grows past its bound");
    assertTrue(counts.rejected() > 0, "and the bound really did refuse some");
    // Not an equality: a worker may be part-way through sending one job, so it is neither
    // queued nor completed yet. At most one per worker can be in flight.
    long inFlight = counts.accepted() - counts.completed() - counts.queued();
    assertTrue(inFlight >= 0 && inFlight <= 1, "at most one job per worker can be in flight");
    notifications.close(false);
}

@Test
@Test
void a_drained_shutdown_finishes_everything_it_accepted() {
    EmailNotificationSender slow = new EmailNotificationSender(0.02);
    QueuedNotificationSender notifications = new QueuedNotificationSender(slow, 10, 1);
    for (int number = 0; number < 10; number++) {
        notifications.sendConfirmation(aConfirmation(number));
    }

    notifications.close(true);

    NotificationCounts counts = notifications.counts();
    assertEquals(10, counts.accepted());
    assertEquals(10, counts.completed());
    assertEquals(0, counts.queued());
    assertEquals(10, slow.sent());
}
```

#### Go

```go
// The whole point of the queue: a one-second email must not cost the guest a second.
func TestABookingDoesNotWaitForASlowConfirmation(t *testing.T) {
	slow := &EmailNotificationSender{SendSeconds: 1}
	queue := NewQueuedNotificationSender(slow, 10, 1)
	defer queue.Close(false)

	started := time.Now()
	if err := queue.SendConfirmation(aConfirmation(1)); err != nil {
		t.Fatalf("handing the email over must not fail: %v", err)
	}
	handedOver := time.Since(started)

	if handedOver > 100*time.Millisecond {
		t.Errorf("handing the email to the queue took %v, which is not immediate", handedOver)
	}
	if sent := slow.Sent(); sent != 0 {
		t.Errorf("the email has been sent already: Sent() = %d, want 0", sent)
	}
	if accepted := queue.Counts().Accepted; accepted != 1 {
		t.Errorf("Accepted = %d, want 1", accepted)
	}
}

// Thirty confirmations into a queue of ten. The exact split depends on whether the worker got a turn
// during the burst, which differs between runtimes, so what is asserted is what the bound promises
// rather than a number.
func TestABurstLargerThanTheBoundIsRefusedAndCounted(t *testing.T) {
	queue := NewQueuedNotificationSender(&EmailNotificationSender{SendSeconds: 1}, 10, 1)
	defer queue.Close(false)

	for number := 0; number < 30; number++ {
		_ = queue.SendConfirmation(aConfirmation(number))
	}

	counts := queue.Counts()
	if total := counts.Accepted + counts.Rejected; total != 30 {
		t.Errorf("accepted + rejected = %d, want 30: every confirmation must be accounted for", total)
	}
	if counts.Queued > 10 {
		t.Errorf("queued = %d, which is past the bound of 10", counts.Queued)
	}
	if counts.Rejected == 0 {
		t.Error("rejected = 0, so the bound refused nothing")
	}
	// Not an equality: a worker may be part-way through sending one job, so it is neither queued
	// nor completed yet. At most one per worker can be in flight.
	inFlight := counts.Accepted - counts.Completed - int64(counts.Queued)
	if inFlight < 0 || inFlight > 1 {
		t.Errorf("accepted %d, completed %d, queued %d: %d in flight, want 0 or 1",
			counts.Accepted, counts.Completed, counts.Queued, inFlight)
	}
}

// accepted == completed and nothing left queued, which is what "owes nobody an email" means.
func TestADrainedShutdownFinishesEverythingItAccepted(t *testing.T) {
	slow := &EmailNotificationSender{SendSeconds: 0.02}
	queue := NewQueuedNotificationSender(slow, 10, 1)
	for number := 0; number < 10; number++ {
		_ = queue.SendConfirmation(aConfirmation(number))
	}

	queue.Close(true)

	counts := queue.Counts()
	if counts.Accepted != 10 || counts.Completed != 10 || counts.Queued != 0 {
		t.Errorf("after a drained close: accepted=%d completed=%d queued=%d, want 10/10/0",
			counts.Accepted, counts.Completed, counts.Queued)
	}
	if sent := slow.Sent(); sent != 10 {
		t.Errorf("the sender sent %d emails, want 10", sent)
	}
}
```

#### JavaScript

```javascript
test('a booking does not wait for a slow confirmation', async () => {
  // The whole point of the queue: a one-second email must not cost the guest a second.
  const slow = new EmailNotificationSender(1);
  const notifications = new QueuedNotificationSender(slow, 10, 1);

  const started = performance.now();
  notifications.sendConfirmation(confirmation(1));
  const handedOver = performance.now() - started;

  assert.ok(handedOver < 100, 'handing the email to the queue must be immediate');
  assert.equal(slow.sent, 0, 'and the email has not been sent yet');
  assert.equal(notifications.counts().accepted, 1);
  await notifications.close({ drain: false });
});

test('a burst larger than the bound is refused and counted', async () => {
  // Thirty confirmations into a queue of ten. The exact split depends on whether the worker got a
  // turn during the burst, which differs between runtimes, so what is asserted is what the bound
  // promises rather than a number.
  const notifications = new QueuedNotificationSender(new EmailNotificationSender(1), 10, 1);

  for (let number = 0; number < 30; number += 1) notifications.sendConfirmation(confirmation(number));

  const counts = notifications.counts();
  assert.equal(counts.accepted + counts.rejected, 30, 'every confirmation is accounted for');
  assert.ok(counts.queued <= 10, 'the queue never grows past its bound');
  assert.ok(counts.rejected > 0, 'and the bound really did refuse some');
  // Not an equality: a worker may be part-way through sending one job, so it is neither queued
  // nor completed yet. At most one per worker can be in flight.
  const inFlight = counts.accepted - counts.completed - counts.queued;
  assert.ok(inFlight >= 0 && inFlight <= 1, 'at most one job per worker can be in flight');
  await notifications.close({ drain: false });
});

test('a drained shutdown finishes everything it accepted', async () => {
  const slow = new EmailNotificationSender(0.02);
  const notifications = new QueuedNotificationSender(slow, 10, 1);
  for (let number = 0; number < 10; number += 1) notifications.sendConfirmation(confirmation(number));

  await notifications.close({ drain: true });

  const counts = notifications.counts();
  assert.equal(counts.accepted, 10);
  assert.equal(counts.completed, 10);
  assert.equal(counts.queued, 0);
  assert.equal(slow.sent, 10);
});
```

#### Dart

```dart
test('a booking does not wait for a slow confirmation', () async {
  // The whole point of the queue: a one-second email must not cost the guest a second.
  final slow = EmailNotificationSender(sendSeconds: 1);
  final notifications = QueuedNotificationSender(slow, capacity: 10, workers: 1);

  final watch = Stopwatch()..start();
  notifications.sendConfirmation(aConfirmation(1));
  watch.stop();

  expect(watch.elapsedMilliseconds, lessThan(100),
      reason: 'handing the email to the queue must be immediate');
  expect(slow.sent, 0, reason: 'and the email has not been sent yet');
  expect(notifications.counts().accepted, 1);
  await notifications.close(drain: false);
});

test('a burst larger than the bound is refused and counted', () async {
  // Thirty confirmations into a queue of ten. The exact split depends on whether the worker got
  // a turn during the burst, which differs between runtimes, so what is asserted is what the
  // bound promises rather than a number.
  final notifications =
      QueuedNotificationSender(EmailNotificationSender(sendSeconds: 1), capacity: 10, workers: 1);

  for (var number = 0; number < 30; number++) {
    notifications.sendConfirmation(aConfirmation(number));
  }

  final counts = notifications.counts();
  expect(counts.accepted + counts.rejected, 30, reason: 'every confirmation is accounted for');
  expect(counts.queued, lessThanOrEqualTo(10), reason: 'the queue never grows past its bound');
  expect(counts.rejected, greaterThan(0), reason: 'and the bound really did refuse some');
  // Not an equality: a worker may be part-way through sending one job, so it is neither queued
  // nor completed yet. At most one per worker can be in flight.
  final inFlight = counts.accepted - counts.completed - counts.queued;
  expect(inFlight, inInclusiveRange(0, 1), reason: 'at most one job per worker can be in flight');
  await notifications.close(drain: false);
});

test('a drained shutdown finishes everything it accepted', () async {
  final slow = EmailNotificationSender(sendSeconds: 0.02);
  final notifications = QueuedNotificationSender(slow, capacity: 10, workers: 1);
  for (var number = 0; number < 10; number++) {
    notifications.sendConfirmation(aConfirmation(number));
  }

  await notifications.close(drain: true);

  final counts = notifications.counts();
  expect(counts.accepted, 10);
  expect(counts.completed, 10);
  expect(counts.queued, 0);
  expect(slow.sent, 10);
});
```

### Your turn · make it refuse

1. Start the app with HOTEL_EMAIL_SECONDS=1 and the default capacity of 10, then run the burst.
2. Read all four counters. Check that accepted + rejected is 30 and that queued is not above 10.
3. Wait about eleven seconds and read GET /stats again. accepted should now equal completed, with queued at 0.
4. Add the three tests (5.1). Run your checks: 22 green (Python: 19 tests, then “Contracts: 3 kept”).
5. Add the “With a bound, and counted rejections” section to docs/queue-trace.md, with both of your readings.
6. Write one line: what does rejected being above zero in normal running tell you?

**When you finish, you have:** 22 green checks, a bound that refuses countably, and both readings in docs/queue-trace.md.
### Explanation

Backpressure is the arrow pointing backwards: the slow consumer is allowed to push back on the fast producer rather than letting work pile up silently. The counting is not decoration — without it, refusing and losing look identical.



---

## 7. Step 6 · Stopping the app without losing work

The queue is in memory. What happens to what is in it when the app stops is a decision, and the default is to lose it.





### What Step 6 adds

| What | Where | What it does |
| --- | --- | --- |
| A drain | The queued sender | Empties the queue before the workers stop |
| A shutdown hook | Your wiring file | Calls the drain when the app is asked to stop |

**What stopping the app does to the queue** In short: With a drain, the worker finishes what it was given. Without one, it stops where it is and the rest dies with the process. Look carefully at the second one: accepted == completed + queued is still true.

Nothing has been lost yet. Those six confirmations are still on the queue, counted honestly as
queued. They are lost a second later, when the process exits and the queue goes with it - and at
that moment no counter changes, no log line is written, and nothing anywhere remembers that six
guests were owed an email.

That is why the drain exists, and it is also why the drain is not enough. It makes a polite
shutdown lose nothing. It does nothing at all for kill -9. A shutdown should also not drain for ever: a queue that is still being filled would never empty, and the deploy would hang. Real systems stop accepting first, drain with a time limit, then say what they dropped.

1. Start: ten confirmations accepted, three sent.
2. Action: the app is stopped, once with the drain and once without.
3. Result:
Stopping the app with 10 confirmations accepted and 3 sent so far...

Draining the notification queue: 7 to go
Notification worker stopped.

accepted=10  completed=10  queued=0
Nobody was owed an email.

and without it:
Stopping the app with 10 confirmations accepted and 3 sent so far...

(no drain) Notification worker stopped.

accepted=10  completed=4  queued=6
accepted == completed + queued: True

C#:

```text
// Stop the workers. With drain the queue is emptied first, so a polite shutdown owes nobody an
// email; without it, whatever is still queued dies with the process. Nothing here helps with a
// process that is killed rather than asked to stop: see docs/queue-trace.md.
public void Close(bool drain = true)
{
    if (drain) _queue.Writer.TryComplete();
    else _stopping.Cancel();
    try { Task.WaitAll(_workers); } catch (AggregateException) { }
    _stopping.Dispose();
}
```

Python:

```text
def close(self, drain: bool = True) -> None:
    """Stop the workers. With drain=True the queue is emptied first, so a clean shutdown owes
    nobody an email; without it, whatever is still queued dies with the process."""
    if drain:
        self._queue.join()
    self._stopping.set()
    for worker in self._workers:
        worker.join(timeout=5)
```

Java:

```text
// With drain the queue is emptied first; without it, whatever is still queued dies with the
// process. Nothing here helps with a process that is killed: see docs/queue-trace.md.
public void close(boolean drain) {
    try {
        if (drain) {
            while (!queue.isEmpty()) {
                Thread.sleep(10);
            }
        }
        stopping = true;
        for (Thread worker : workers) {
            worker.join(5000);
        }
    } catch (InterruptedException interrupted) {
        Thread.currentThread().interrupt();
    }
}
```

Go:

```text
// Close stops the workers. With drain the queue is emptied first, so a polite shutdown owes nobody
// an email; without it, whatever is still queued dies with the process. Nothing here helps with a
// process that is killed rather than asked to stop: see docs/queue-trace.md.
func (queue *QueuedNotificationSender) Close(drain bool) {
	queue.closeOnce.Do(func() {
		if drain {
			for len(queue.jobs) > 0 {
				time.Sleep(10 * time.Millisecond)
			}
		}
		close(queue.stopping)
		queue.done.Wait()
	})
}
```

JavaScript:

```text
// Stop the workers. With drain the queue is emptied first, so a polite shutdown owes nobody an
// email; without it, whatever is still queued dies with the process.
async close({ drain = true } = {}) {
  if (drain) {
    while (this.#jobs.length) await wait(10);
  }
  this.#stopping = true;
  await Promise.all(this.#workers);
}
```

Dart:

```text
/// Stop the workers. With drain the queue is emptied first, so a polite shutdown owes nobody an
/// email; without it, whatever is still queued dies with the process.
Future<void> close({bool drain = true}) async {
  if (drain) {
    while (_jobs.isNotEmpty) {
      await Future<void>.delayed(const Duration(milliseconds: 10));
    }
  }
  _stopping = true;
  await Future.wait(_workers);
}
```

### Completed example 6.1 · closing the sender

**What stopping the app does to the queue** In short: With a drain, the worker finishes what it was given. Without one, it stops where it is and the rest dies with the process. Look carefully at the second one: accepted == completed + queued is still true.

Nothing has been lost yet. Those six confirmations are still on the queue, counted honestly as
queued. They are lost a second later, when the process exits and the queue goes with it - and at
that moment no counter changes, no log line is written, and nothing anywhere remembers that six
guests were owed an email.

That is why the drain exists, and it is also why the drain is not enough. It makes a polite
shutdown lose nothing. It does nothing at all for kill -9. A shutdown should also not drain for ever: a queue that is still being filled would never empty, and the deploy would hang. Real systems stop accepting first, drain with a time limit, then say what they dropped.

1. Start: ten confirmations accepted, three sent.
2. Action: the app is stopped, once with the drain and once without.
3. Result:
Stopping the app with 10 confirmations accepted and 3 sent so far...

Draining the notification queue: 7 to go
Notification worker stopped.

accepted=10  completed=10  queued=0
Nobody was owed an email.

and without it:
Stopping the app with 10 confirmations accepted and 3 sent so far...

(no drain) Notification worker stopped.

accepted=10  completed=4  queued=6
accepted == completed + queued: True

C#:

```text
// Stop the workers. With drain the queue is emptied first, so a polite shutdown owes nobody an
// email; without it, whatever is still queued dies with the process. Nothing here helps with a
// process that is killed rather than asked to stop: see docs/queue-trace.md.
public void Close(bool drain = true)
{
    if (drain) _queue.Writer.TryComplete();
    else _stopping.Cancel();
    try { Task.WaitAll(_workers); } catch (AggregateException) { }
    _stopping.Dispose();
}
```

Python:

```text
def close(self, drain: bool = True) -> None:
    """Stop the workers. With drain=True the queue is emptied first, so a clean shutdown owes
    nobody an email; without it, whatever is still queued dies with the process."""
    if drain:
        self._queue.join()
    self._stopping.set()
    for worker in self._workers:
        worker.join(timeout=5)
```

Java:

```text
// With drain the queue is emptied first; without it, whatever is still queued dies with the
// process. Nothing here helps with a process that is killed: see docs/queue-trace.md.
public void close(boolean drain) {
    try {
        if (drain) {
            while (!queue.isEmpty()) {
                Thread.sleep(10);
            }
        }
        stopping = true;
        for (Thread worker : workers) {
            worker.join(5000);
        }
    } catch (InterruptedException interrupted) {
        Thread.currentThread().interrupt();
    }
}
```

Go:

```text
// Close stops the workers. With drain the queue is emptied first, so a polite shutdown owes nobody
// an email; without it, whatever is still queued dies with the process. Nothing here helps with a
// process that is killed rather than asked to stop: see docs/queue-trace.md.
func (queue *QueuedNotificationSender) Close(drain bool) {
	queue.closeOnce.Do(func() {
		if drain {
			for len(queue.jobs) > 0 {
				time.Sleep(10 * time.Millisecond)
			}
		}
		close(queue.stopping)
		queue.done.Wait()
	})
}
```

JavaScript:

```text
// Stop the workers. With drain the queue is emptied first, so a polite shutdown owes nobody an
// email; without it, whatever is still queued dies with the process.
async close({ drain = true } = {}) {
  if (drain) {
    while (this.#jobs.length) await wait(10);
  }
  this.#stopping = true;
  await Promise.all(this.#workers);
}
```

Dart:

```text
/// Stop the workers. With drain the queue is emptied first, so a polite shutdown owes nobody an
/// email; without it, whatever is still queued dies with the process.
Future<void> close({bool drain = true}) async {
  if (drain) {
    while (_jobs.isNotEmpty) {
      await Future<void>.delayed(const Duration(milliseconds: 10));
    }
  }
  _stopping = true;
  await Future.wait(_workers);
}
```

### Closing the queued sender

#### C#

```csharp
// Stop the workers. With drain the queue is emptied first, so a polite shutdown owes nobody an
// email; without it, whatever is still queued dies with the process. Nothing here helps with a
// process that is killed rather than asked to stop: see docs/queue-trace.md.
public void Close(bool drain = true)
{
    if (drain) _queue.Writer.TryComplete();
    else _stopping.Cancel();
    try { Task.WaitAll(_workers); } catch (AggregateException) { }
    _stopping.Dispose();
}
```

#### Python

```python
def close(self, drain: bool = True) -> None:
    """Stop the workers. With drain=True the queue is emptied first, so a clean shutdown owes
    nobody an email; without it, whatever is still queued dies with the process."""
    if drain:
        self._queue.join()
    self._stopping.set()
    for worker in self._workers:
        worker.join(timeout=5)
```

#### Java

```java
// With drain the queue is emptied first; without it, whatever is still queued dies with the
// process. Nothing here helps with a process that is killed: see docs/queue-trace.md.
public void close(boolean drain) {
    try {
        if (drain) {
            while (!queue.isEmpty()) {
                Thread.sleep(10);
            }
        }
        stopping = true;
        for (Thread worker : workers) {
            worker.join(5000);
        }
    } catch (InterruptedException interrupted) {
        Thread.currentThread().interrupt();
    }
}
```

#### Go

```go
// Close stops the workers. With drain the queue is emptied first, so a polite shutdown owes nobody
// an email; without it, whatever is still queued dies with the process. Nothing here helps with a
// process that is killed rather than asked to stop: see docs/queue-trace.md.
func (queue *QueuedNotificationSender) Close(drain bool) {
	queue.closeOnce.Do(func() {
		if drain {
			for len(queue.jobs) > 0 {
				time.Sleep(10 * time.Millisecond)
			}
		}
		close(queue.stopping)
		queue.done.Wait()
	})
}
```

#### JavaScript

```javascript
// Stop the workers. With drain the queue is emptied first, so a polite shutdown owes nobody an
// email; without it, whatever is still queued dies with the process.
async close({ drain = true } = {}) {
  if (drain) {
    while (this.#jobs.length) await wait(10);
  }
  this.#stopping = true;
  await Promise.all(this.#workers);
}
```

#### Dart

```dart
/// Stop the workers. With drain the queue is emptied first, so a polite shutdown owes nobody an
/// email; without it, whatever is still queued dies with the process.
Future<void> close({bool drain = true}) async {
  if (drain) {
    while (_jobs.isNotEmpty) {
      await Future<void>.delayed(const Duration(milliseconds: 10));
    }
  }
  _stopping = true;
  await Future.wait(_workers);
}
```

### A polite shutdown

### Terminal 1, after Ctrl+C

```text
Stopping the app with 10 confirmations accepted and 3 sent so far...

Draining the notification queue: 7 to go
Notification worker stopped.

accepted=10  completed=10  queued=0
Nobody was owed an email.
```

### And one without the drain

### Without the drain

```text
Stopping the app with 10 confirmations accepted and 3 sent so far...

(no drain) Notification worker stopped.

accepted=10  completed=4  queued=6
accepted == completed + queued: True
```

### Look at that second one again

Look carefully at the second one: accepted == completed + queued is still true.

Nothing has been lost yet. Those six confirmations are still on the queue, counted honestly as
queued. They are lost a second later, when the process exits and the queue goes with it - and at
that moment no counter changes, no log line is written, and nothing anywhere remembers that six
guests were owed an email.

That is why the drain exists, and it is also why the drain is not enough. It makes a polite
shutdown lose nothing. It does nothing at all for kill -9.

**What this design still loses** In short: The queue is in memory. A clean shutdown drains it; a killed process loses it, and nothing records what was lost. The asymmetry is the problem: the reservation is durable and the job is not. Kill the process and the booking survives while the confirmation vanishes, with no row and no log line saying it was owed. The honest fix is a transactional outbox — write the job into the database in the same transaction as the booking, so the two are saved or lost together. P17 builds it. Until then, the bound and the counters make the loss visible rather than invisible, and that is the whole of what they buy.

1. Start: a burst has just been sent and some confirmations are still queued.
2. Action: the app is killed rather than stopped politely.
3. Result: every reservation is still in PostgreSQL and every queued confirmation is gone, with nothing recording how many there were.

### What this design still loses

| Case | What happens |
| --- | --- |
| Ctrl+C, or a normal deploy | The queue is drained. Nobody is owed an email. |
| kill -9, a crashed container, a power cut | Everything queued is gone, and no counter changes and nothing is logged. |
| The honest fix | Write the job into the database in the same transaction as the booking, so the two are saved or lost together. That is the transactional outbox, and P17 builds it. |

**What this design still loses** In short: The queue is in memory. A clean shutdown drains it; a killed process loses it, and nothing records what was lost. The asymmetry is the problem: the reservation is durable and the job is not. Kill the process and the booking survives while the confirmation vanishes, with no row and no log line saying it was owed. The honest fix is a transactional outbox — write the job into the database in the same transaction as the booking, so the two are saved or lost together. P17 builds it. Until then, the bound and the counters make the loss visible rather than invisible, and that is the whole of what they buy.

1. Start: a burst has just been sent and some confirmations are still queued.
2. Action: the app is killed rather than stopped politely.
3. Result: every reservation is still in PostgreSQL and every queued confirmation is gone, with nothing recording how many there were.

### Your turn · drain it

1. Add the close method with a drain to the queued sender (6.1).
2. Call it from your wiring file when the app is asked to stop.
3. Start the app with HOTEL_EMAIL_SECONDS=1, run the burst, then press Ctrl+C in terminal 1 and read what it prints.
4. Check the final counters: accepted should equal completed, with queued at 0.
5. Add the “What stopping the app does” section to docs/queue-trace.md.
6. Write the last section yourself: “What this design still loses”. Say plainly what a killed process costs and name the transactional outbox as the fix.

**When you finish, you have:** A drain on shutdown, and docs/queue-trace.md finished — including an honest last section.
### Explanation

The invariant still holding without the drain is the most useful thing on this page. Nothing has been lost at the moment you read the counters; it is lost a second later, and nothing records it. A design whose failure leaves no trace is the one to be most careful about.



---

## 8. Step 7 · Choose a concurrency model

A bounded queue with a worker is one shape among six. The others are not wrong in general — they are wrong for this work, each for a different reason, and naming the reason is the skill.





### Six shapes, and the question each one answers

| Shape | What it is for | Why it does or does not fit here |
| --- | --- | --- |
| Bounded queue and worker | Work the request does not own, one stage, needs a limit | Fits. That is the confirmation email exactly, which is why this is what P10 builds. |
| Actor | One mailbox per thing, messages handled one at a time | Would also serialise bookings per room — and move the booking rule out of the database, which ADR-003 has just decided against. |
| Reactive pipeline | Many stages, with backpressure built into the protocol | Excellent with many stages. We have one, and the library and operators are a lot to take on for one email. |
| Fork–join | Split one piece of work into parts and wait for all of them | Wrong shape twice over: the email is not part of the booking, and nobody waits for it. |
| Structured concurrency | Work the request owns, guaranteed not to outlive its scope | The right default for owned work, and exactly why it does not fit: the point is that the email outlives the request. |
| Software transactional memory | Composing shared in-memory state without locks | There is no shared state to compose. The queue is the only shared thing and it is already safe. |

**Six shapes, and why five of them are wrong here** In short: A queue and a worker, an actor, a reactive pipeline, fork–join, structured concurrency, or STM. Each answers a different question. An actor per room would also serialise bookings — and move the booking rule out of the database, which ADR-003 has just decided against. A reactive pipeline builds backpressure into the protocol, which is excellent with many stages and a lot of machinery for one. Fork–join splits one piece of work and waits for all of it, and nobody waits for this. Structured concurrency guarantees a task cannot outlive its scope, which is exactly what this work must do. STM composes shared state, and there is none to compose. Name the question first: does the request own this work, does it have stages, does it need a limit, does it share mutable state? Answer those four and the shape is usually forced.

1. Start: the hotel needs confirmation emails sent outside the request.
2. Action: you compare the six shapes against what the work actually is.
3. Result: a bounded queue with a worker — one stage, nobody waiting, and a limit required.

### The four questions that usually decide it

Does the request own this work, or does it merely cause it? Does it have one stage or several? Does it need a limit? Does it share mutable state with anything?

Answer those and the shape is normally forced. Choose the shape first and you will spend the ADR justifying it instead of explaining it.

### The honest answer for this work

A bounded in-memory queue with one worker and counted rejections. It is the simplest thing that has a limit, and for one fire-and-forget stage that is the right size of design. What it does not survive is the process dying, and P17 is where that gets fixed.

**What this design still loses** In short: The queue is in memory. A clean shutdown drains it; a killed process loses it, and nothing records what was lost. The asymmetry is the problem: the reservation is durable and the job is not. Kill the process and the booking survives while the confirmation vanishes, with no row and no log line saying it was owed. The honest fix is a transactional outbox — write the job into the database in the same transaction as the booking, so the two are saved or lost together. P17 builds it. Until then, the bound and the counters make the loss visible rather than invisible, and that is the whole of what they buy.

1. Start: a burst has just been sent and some confirmations are still queued.
2. Action: the app is killed rather than stopped politely.
3. Result: every reservation is still in PostgreSQL and every queued confirmation is gone, with nothing recording how many there were.

### Your turn · pick one and say why

1. In docs/design-notes.md under “# P10 design notes”, write one line per rejected shape: the shape, and one reason it does not fit.
2. Make every reason a property of the work — “nobody waits for this”, “there is only one stage”, “there is no shared state” — not a property of a library.
3. Write one line naming the shape you kept and the single sentence that justifies it.
4. Write one line naming the thing it still cannot do.

**When you finish, you have:** Six lines in docs/design-notes.md: five rejections with reasons, and the choice with its limit.
### Explanation

“We chose actors because they are modern” is the sentence ADR-004 exists to prevent. A reason that mentions a library is not a reason; a reason that mentions the work is.



---

## 9. Step 8 · Write ADR-004

Write down the model you chose, the five you rejected and why, and the loss you are knowingly accepting.





### What goes in it

| Heading | What to write |
| --- | --- |
| Context | What the email was costing, and why an unbounded queue is not the fix. One paragraph, pointing at docs/queue-trace.md. |
| Decision | A bounded queue with a worker, the capacity, the worker count, and the four counters. |
| Alternatives considered | All five rejected shapes, each with a reason that is about the work. |
| Consequences | Including the uncomfortable ones: under a burst some guests get a room and no email, and a killed process loses the queue with nothing recording it. |
| When to revisit | When a lost confirmation stops being acceptable (P17), when rejected is regularly above zero, or when a second kind of background job appears. |

**What this design still loses** In short: The queue is in memory. A clean shutdown drains it; a killed process loses it, and nothing records what was lost. The asymmetry is the problem: the reservation is durable and the job is not. Kill the process and the booking survives while the confirmation vanishes, with no row and no log line saying it was owed. The honest fix is a transactional outbox — write the job into the database in the same transaction as the booking, so the two are saved or lost together. P17 builds it. Until then, the bound and the counters make the loss visible rather than invisible, and that is the whole of what they buy.

1. Start: a burst has just been sent and some confirmations are still queued.
2. Action: the app is killed rather than stopped politely.
3. Result: every reservation is still in PostgreSQL and every queued confirmation is gone, with nothing recording how many there were.

### Completed example: docs/adr-004-notification-concurrency.md

### docs/adr-004-notification-concurrency.md

```text
# ADR-004: Send confirmation emails from a bounded queue

- Status: accepted
- Date: 2026-11-14
- Supersedes: nothing. Extends ADR-003 (the booking rule in the database).

## Context

Sending the confirmation email inside the booking request made a 4 ms booking take a full second
(`docs/queue-trace.md`). The guest waits for a mail server for no reason: the room is already
theirs the moment the reservation is committed, and the email tells them something they have
already been told by the 201 response.

Taking it out of the request needs somewhere to put it, and the obvious first answer - an
unbounded queue - trades a slow request for an unbounded memory leak. A burst of 30 bookings left
29 jobs in memory; a burst of 30,000 would leave 30,000, and the process would die taking the
bookings with it.

## Decision

Send confirmations from a **bounded in-memory queue** drained by a background worker.

- The queue holds 10 jobs and there is 1 worker.
- When the queue is full a job is **refused and counted**, not waited for. The booking still
  succeeds: a confirmation is not a booking.
- Four counters are published on `GET /stats`: accepted, rejected, completed and queued, with the
  invariant `accepted = completed + queued` once nothing is in flight.
- Stopping the app drains the queue before the worker stops.

The concurrency model is **one bounded queue with a worker** - the simplest thing that has a
limit. The alternatives below are all reasonable and all bigger.

## Alternatives considered

**No queue: keep sending inside the request.** Simplest, and already rejected by the measurement:
every guest waits a second for a mail server, and a mail server outage becomes a booking outage.

**An unbounded queue.** One line shorter than a bounded one, and it has no answer to "what happens
when the consumer is slower than the producer for long enough". The answer is always the same:
memory grows until the process dies. Rejected: a queue without a limit means the limit is the
machine, chosen by accident.

**An actor per room.** One mailbox per room, messages processed one at a time. It would also give
us the booking rule for free, since a room's bookings would be serialised. Rejected: it moves the
booking rule out of the database, which ADR-003 just decided against, and it is a much larger
change than sending an email needs.

**A reactive pipeline.** Back-pressure is built into the protocol rather than bolted on, and it
composes well when there are many stages. Rejected for now: we have one stage, and the library and
the operators are a lot to take on for one email.

**Fork-join / parallel streams.** Wrong shape. Fork-join splits one piece of work into independent
parts and waits for all of them; sending an email is not part of the booking and nobody waits for
it at all.

**Structured concurrency.** The right default for work a request genuinely owns, because the task
cannot outlive the scope that started it. That is exactly why it does not fit here: the whole point
is that the email outlives the request.

**Software transactional memory.** Solves a different problem - composing in-memory state changes
without locks. There is no shared state to compose here; the queue is the only shared thing and it
is already thread-safe.

## Consequences

- A booking now answers in milliseconds and no longer depends on the mail server being up.
- Under a burst, some guests get a room and no email. That is now a counted, visible number
  (`notificationsRejected`) rather than a silent delay.
- The counters are the monitoring. A rejected count that is not zero in normal running means the
  bound or the worker count is wrong.
- The queue is in memory. A clean shutdown drains it; `kill -9` loses it, and nothing records what
  was lost.
- The booking and the confirmation now succeed and fail independently, which is what makes the
  first consequence possible and the fourth one tolerable.

## When to revisit

- When a lost confirmation stops being acceptable. That is the transactional outbox, and P17
  builds it.
- When `notificationsRejected` is regularly above zero: the bound, the worker count or the sender
  is wrong, and the counter is telling you which.
- When a second kind of background job appears. Two queues with hand-rolled workers is the point at
  which a real job runner starts being cheaper than maintaining your own.

```

### Your turn · record the decision

1. Create docs/adr-004-notification-concurrency.md with the five headings above.
2. Move your six lines from docs/design-notes.md into the Decision and Alternatives sections.
3. Under Consequences, write the two uncomfortable lines explicitly. If you are unwilling to write them down, you have not really accepted them.
4. Under When to revisit, name at least two conditions.
5. Compare with the completed example. Shorter is fine; missing the costs is not.

**When you finish, you have:** docs/adr-004-notification-concurrency.md with five rejected alternatives and two honest consequences.
### Explanation

This is the fourth ADR in the folder. Read end to end with ADR-001 to ADR-003 they are the design history of the application: what was expected, what was measured, what was chosen and what each choice cost.



---

## 10. Check your project and see what comes next

The hotel app now answers a booking in milliseconds, keeps the booking rule under concurrency, and has a limit on its background work that it chose on purpose.





### Completed example: your folder at the end of P10

### hotel-booking folder

#### C#

```text
hotel-booking/   end of P10
├── db/
│   └── init.sql
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
│   │   ├── HotelApi.cs   CHANGED: takes the queue and publishes its counters
│   │   └── Stats.cs   CHANGED: GET /stats can also publish the queue’s four counters
│   ├── Notifications/
│   │   ├── Internal/
│   │   │   ├── EmailNotificationSender.cs   CHANGED: sendSeconds makes it as slow as a real mail server
│   │   │   ├── QueuedNotificationSender.cs   NEW
│   │   │   └── SmsNotificationSender.cs
│   │   ├── Confirmation.cs
│   │   ├── INotificationSender.cs
│   │   └── NotificationCounts.cs   NEW
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
│   │   ├── OverlapRejectedException.cs
│   │   ├── Reservation.cs
│   │   └── ReservationStatus.cs
│   ├── Rooms/
│   │   ├── Internal/
│   │   │   ├── InMemoryRoomCatalog.cs
│   │   │   └── PostgresRoomCatalog.cs
│   │   ├── IRoomCatalog.cs
│   │   └── Room.cs
│   ├── HotelBooking.csproj
│   └── Program.cs   CHANGED: wires the queue and drains it on shutdown
├── HotelBooking.Tests/
│   ├── ApiTests.cs   CHANGED: + 3 queue tests
│   ├── ArchitectureTests.cs
│   ├── BookingServiceTests.cs
│   └── HotelBooking.Tests.csproj
├── tools/
│   ├── burst.cs   NEW
│   ├── measure.cs
│   └── race-http.cs
└── docker-compose.yml
```

#### Python

```text
hotel-booking/   end of P10
├── db/
│   └── init.sql
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
│   │   ├── __init__.py   CHANGED: + NotificationCounts
│   │   └── internal.py   CHANGED: a slow sender, and the queued sender
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
│   ├── api_cache.py
│   └── api.py   CHANGED: GET /stats also publishes the queue’s four counters
├── tools/
│   ├── burst.py   NEW
│   ├── measure.py
│   └── race_http.py
├── docker-compose.yml
├── main.py   CHANGED: wires the queue and drains it on shutdown
├── pyproject.toml
├── requirements.txt
├── test_api.py   CHANGED: + 3 queue tests
└── test_booking_service.py
```

#### Java

```text
hotel-booking/   end of P10
├── db/
│   └── init.sql
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
│   │   │       │   ├── CountingReservationStore.java
│   │   │       │   ├── HotelApi.java   CHANGED: takes the queue and publishes its counters
│   │   │       │   └── Stats.java   CHANGED: GET /stats can also publish the queue’s four counters
│   │   │       ├── notifications/
│   │   │       │   ├── internal/
│   │   │       │   │   ├── EmailNotificationSender.java   CHANGED: sendSeconds makes it as slow as a real mail server
│   │   │       │   │   ├── QueuedNotificationSender.java   NEW
│   │   │       │   │   └── SmsNotificationSender.java
│   │   │       │   ├── Confirmation.java
│   │   │       │   ├── CountingNotificationSender.java   NEW
│   │   │       │   ├── NotificationCounts.java   NEW
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
│   │   │       │   ├── OverlapRejectedException.java
│   │   │       │   ├── Reservation.java
│   │   │       │   ├── ReservationStatus.java
│   │   │       │   └── ReservationStore.java
│   │   │       ├── rooms/
│   │   │       │   ├── internal/
│   │   │       │   │   ├── InMemoryRoomCatalog.java
│   │   │       │   │   └── PostgresRoomCatalog.java
│   │   │       │   ├── Room.java
│   │   │       │   └── RoomCatalog.java
│   │   │       └── Main.java   CHANGED: wires the queue and drains it on shutdown
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── hotelbooking/
│               ├── ApiTests.java   CHANGED: + 3 queue tests
│               ├── ArchitectureTests.java
│               └── BookingServiceTests.java
├── tools/
│   ├── Burst.java   NEW
│   ├── Measure.java
│   └── RaceHttp.java
├── docker-compose.yml
└── pom.xml
```

#### Go

```text
hotel-booking/   end of P10
├── api/
│   ├── api_test.go   CHANGED: passes nil for the queue
│   ├── api.go   CHANGED: takes the queue and publishes its counters
│   ├── cache.go
│   └── stats.go   CHANGED: GET /stats can also publish the queue’s four counters
├── db/
│   └── init.sql
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
│   ├── internal.go   CHANGED: a slow sender, and the queued sender
│   ├── notifications.go   CHANGED: + Counts and CountingNotificationSender
│   └── queue_test.go   NEW
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
│   ├── burst/
│   │   └── main.go   NEW
│   ├── measure/
│   │   └── main.go
│   └── race/
│       └── main.go
├── architecture_test.go
├── docker-compose.yml
├── go.mod
├── go.sum
└── main.go   CHANGED: wires the queue and drains it on shutdown
```

#### JavaScript

```text
hotel-booking/   end of P10
├── db/
│   └── init.sql
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
│   │   │   ├── email.js   CHANGED: sendSeconds makes it as slow as a real mail server
│   │   │   ├── queued.js   NEW
│   │   │   └── sms.js
│   │   └── index.js   CHANGED: the contract now allows a promise, and names the counters
│   ├── payments/
│   │   ├── internal/
│   │   │   └── simulated.js
│   │   └── index.js
│   ├── reservations/
│   │   ├── internal/
│   │   │   ├── postgres-store.js
│   │   │   └── store.js
│   │   ├── booking.js   CHANGED: reserve awaits the confirmation
│   │   ├── domain.js
│   │   └── index.js
│   ├── rooms/
│   │   ├── internal/
│   │   │   ├── catalog.js
│   │   │   └── postgres-catalog.js
│   │   └── index.js
│   ├── api-cache.js
│   └── api.js   CHANGED: GET /stats also publishes the queue’s four counters
├── test/
│   ├── api.test.js   CHANGED: + 3 queue tests
│   ├── architecture.test.js
│   └── booking.test.js
├── tools/
│   ├── burst.js   NEW
│   ├── measure.js
│   └── race-http.js
├── docker-compose.yml
├── main.js   CHANGED: wires the queue and drains it on shutdown
├── package-lock.json
└── package.json
```

#### Dart

```text
hotel-booking/   end of P10
├── bin/
│   └── main.dart   CHANGED: wires the queue and drains it on shutdown
├── db/
│   └── init.sql
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
│   │   │   ├── email.dart   CHANGED: sendSeconds makes it as slow as a real mail server
│   │   │   ├── queued.dart   NEW
│   │   │   └── sms.dart
│   │   └── notifications.dart   CHANGED: the contract now allows a Future, and names the counters
│   ├── payments/
│   │   ├── src/
│   │   │   └── simulated.dart
│   │   └── payments.dart
│   ├── reservations/
│   │   ├── src/
│   │   │   ├── booking.dart   CHANGED: reserve awaits the confirmation
│   │   │   ├── domain.dart
│   │   │   ├── postgres_store.dart
│   │   │   └── store.dart
│   │   └── reservations.dart
│   ├── rooms/
│   │   ├── src/
│   │   │   ├── catalog.dart
│   │   │   └── postgres_catalog.dart
│   │   └── rooms.dart
│   ├── api_cache.dart
│   └── api.dart   CHANGED: GET /stats also publishes the queue’s four counters
├── test/
│   ├── api_test.dart   CHANGED: + 3 queue tests
│   ├── architecture_test.dart
│   └── booking_service_test.dart
├── tool/
│   ├── burst.dart   NEW
│   ├── measure.dart
│   └── race_http.dart
├── analysis_options.yaml
├── docker-compose.yml
└── pubspec.yaml
```

### Check your project

| Check | What you should see |
| --- | --- |
| Your checks | 22 green (Python: 19 tests, then “Contracts: 3 kept, 0 broken”) |
| A booking with HOTEL_EMAIL_SECONDS=1 | 201 Created in milliseconds, not a second |
| The burst tool | All 30 bookings accepted; some confirmations refused and counted |
| GET /stats | Four notification counters next to the four cache counters |
| accepted + rejected | Exactly 30 after a burst of 30 |
| queued | Never above your capacity |
| After the worker catches up | accepted = completed, queued = 0 |
| Ctrl+C | Drains the queue and says so |
| docs/queue-trace.md | Before, unbounded, bounded, shutdown, and what it still loses |
| docs/adr-004-notification-concurrency.md | The model, five rejected, consequences, when to revisit |

### What the hotel app can now do

| Since | What it got |
| --- | --- |
| P07 | Real HTTP endpoints and a real PostgreSQL database |
| P08 | A cache in front of the search, measured both ways |
| P09 | A booking rule the database enforces, however many instances are running |
| P10 | A booking that answers at once, and background work with a limit it chose |

### What comes next

L06 and P11–P12 widen the question: work that overlaps across services rather than inside one, and what happens when the remote end fails rather than merely being slow.

P17 comes back to today’s loose end. The in-memory queue loses its jobs when the process dies; the durable answer is to write the job into the database in the same transaction as the booking, which is the transactional outbox.

### Your turn · hand it over

1. Run every check in the table above and tick them off.
2. Commit your work with the message “P10: send confirmations from a bounded queue”.
3. Read your ADR once more and check a stranger could tell why the other five shapes were rejected.
4. Bring this folder to P11.

**When you finish, you have:** A committed project with 22 green checks, a booking that answers at once, and two documents explaining the cost.
### Explanation

The habit to take away is the order, again: measure the cost, make the smallest change that removes it, find the new failure mode that change introduced, put a limit on it, count what the limit refuses, and write down what you are still losing.
