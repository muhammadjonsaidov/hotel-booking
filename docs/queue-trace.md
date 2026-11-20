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
