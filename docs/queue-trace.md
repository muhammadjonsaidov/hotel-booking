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
