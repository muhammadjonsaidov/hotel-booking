# The confirmation email, taken out of the request

## What the email was costing the guest

The app was started with `HOTEL_EMAIL_SECONDS=1 ./gradlew bootRun`, so the simulated email sender takes
one second, like a real mail server. The email is still sent inside the booking request.

One booking on its own (R7, 20–21 December), timed with curl:

```
HTTP 201 in 1.110788 s
```

Thirty bookings, one after another, with `java tools/Burst.java` (R7 and R8, one night each, 1–15 December,
so the booking rule refuses none of them):

```
Sending 30 bookings, one after another...

Accepted: 30
Rejected: 0
Slowest booking: 1063 ms
Total: 30.6 s

GET /stats
{"searches":0,"databaseQueries":93,"cacheHits":0,"cacheMisses":0}
```

The real work of a booking is three database questions (93 for 31 bookings) and takes a few milliseconds;
the rest of every second is the confirmation email. Thirty seconds for about thirty bookings' worth of
milliseconds: every guest waited a full second for a room that was already theirs from the moment the
reservation committed.

## With a queue in front of the sender

`QueuedNotificationSender` keeps the `NotificationSender` promise, so `BookingService` did not change: the
booking hands over a `Confirmation`, the queued sender puts it on a bounded queue with `offer` (it never
waits), and one background worker sends it with the same one-second email sender. The app was started
again with `HOTEL_EMAIL_SECONDS=1 ./gradlew bootRun`, and three single bookings were timed with curl:

```
{"id":"R136","status":"Confirmed","notificationFailed":false}  -> HTTP 201 in 0.096228 s
{"id":"R137","status":"Confirmed","notificationFailed":false}  -> HTTP 201 in 0.007657 s
{"id":"R138","status":"Confirmed","notificationFailed":false}  -> HTTP 201 in 0.006063 s
```

The same booking that took 1.11 s now answers in 6–8 ms (the first request pays for warming up the JVM and
the connections). The email is no longer sent inside the request: it waits on the queue, and the worker
sends it a second later, while nobody is waiting.

## Why an unbounded queue is not the answer

The app was started with `HOTEL_EMAIL_SECONDS=1 HOTEL_QUEUE_CAPACITY=10000 ./gradlew bootRun`, a queue so
large that nothing is ever refused, on an empty database. GET /stats now also publishes the queue's four
counters. Then `java tools/Burst.java`:

```
Sending 30 bookings, one after another...

Accepted: 30
Rejected: 0
Slowest booking: 150 ms
Total: 0.3 s

GET /stats
{"searches":0,"databaseQueries":90,"cacheHits":0,"cacheMisses":0,"notificationsAccepted":30,"notificationsRejected":0,"notificationsCompleted":0,"notificationsQueued":29}
```

Ten seconds later:

```
{"searches":0,"databaseQueries":90,"cacheHits":0,"cacheMisses":0,"notificationsAccepted":30,"notificationsRejected":0,"notificationsCompleted":10,"notificationsQueued":19}
```

Accepted 30, rejected 0, queued 29: every guest was answered at once (0.3 s for all thirty, against 30.6 s
before), and 29 emails sat in memory behind a worker that sends one a second (the thirtieth was being sent).
Nothing refused them.

A burst of thirty thousand would have been accepted just the same: thirty thousand emails held in memory,
more than eight hours of sending at one a second, until the process ran out of memory and took the bookings
down with the emails.

## With a bound, and counted rejections

The same burst against the default queue of ten: `HOTEL_EMAIL_SECONDS=1 ./gradlew bootRun` on an empty
database, then `java tools/Burst.java`.

```
Sending 30 bookings, one after another...

Accepted: 30
Rejected: 0
Slowest booking: 150 ms
Total: 0.3 s

GET /stats
{"searches":0,"databaseQueries":90,"cacheHits":0,"cacheMisses":0,"notificationsAccepted":11,"notificationsRejected":19,"notificationsCompleted":0,"notificationsQueued":10}
```

Eleven seconds later, once the worker had caught up:

```
{"searches":0,"databaseQueries":90,"cacheHits":0,"cacheMisses":0,"notificationsAccepted":11,"notificationsRejected":19,"notificationsCompleted":11,"notificationsQueued":0}
```

- Every booking succeeded: 30 times 201, and PostgreSQL held 30 Confirmed reservations. A refused email is
  not a refused room.
- accepted 11 + rejected 19 = 30, and queued never went above the bound of 10. Over HTTP the worker had time
  to take one job during the burst and free a slot, so it is 11 and 19 rather than 10 and 20.
- Once the worker caught up, accepted 11 = completed 11 + queued 0: everything the queue took, it finished.
- The 19 refusals are a real cost (19 guests with a reservation nobody told them about), but a counted one:
  a queue that dropped them silently would have looked exactly like one that was coping.

Rejected above zero in normal running, with no burst going on, means the queue is too small, there are too
few workers, or the mail sender is too slow; queued tells you which way it is leaning.
