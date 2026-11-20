# ADR-004: Send confirmation emails from a bounded queue

Status: Accepted, 20 November 2026

Extends ADR-003 (the booking rule in the database); supersedes nothing.

## Context

The confirmation email was sent inside the booking request. With a sender as slow as a real mail server (`HOTEL_EMAIL_SECONDS=1`), one booking took 1.11 s, and thirty bookings one after another took 30.6 s, although each booking's real work is three database questions and a few milliseconds (`docs/queue-trace.md`). The guest waited a second for a room that was already theirs from the moment the reservation committed, and a mail-server outage would have become a booking outage.

Taking the email out of the request needs somewhere to put it. The first answer, a queue with no real limit (`HOTEL_QUEUE_CAPACITY=10000`), answered all thirty bookings in 0.3 s but left 29 emails in memory behind a worker that sends one a second, with nothing refusing them. A burst of thirty thousand would have been accepted the same way, until the process ran out of memory and took the bookings down with the emails.

## Decision

Send confirmations from a **bounded in-memory queue** drained by a background worker (`QueuedNotificationSender`, in the Notifications module's internal code). It keeps the `NotificationSender` promise, so `BookingService` did not change.

- The queue holds **10** jobs (`HOTEL_QUEUE_CAPACITY`) and there is **1** worker (`HOTEL_WORKERS`).
- A confirmation is offered with `offer`, which never waits. When the queue is full the confirmation is **refused and counted**; the booking still succeeds, because a confirmation is not a booking.
- Four counters are published on `GET /stats`: `notificationsAccepted`, `notificationsRejected`, `notificationsCompleted` and `notificationsQueued`, with `accepted = completed + queued` once nothing is in flight.
- When the app is asked to stop (Ctrl+C or SIGTERM), a shutdown hook drains the queue before the worker stops.

The concurrency model is one bounded queue with one worker, because the work is a single fire-and-forget stage that the request does not own, that needs a limit, and that shares no state except the queue. Measured: with the same one-second sender a booking now answers in 6–8 ms, and the burst of thirty against a queue of ten gave 30 bookings, 11 emails accepted and 19 refused and counted, with `queued` never above 10.

## Alternatives considered

- **No queue: keep sending inside the request.** Simplest, and rejected by the measurement: every guest waits a second for the mail server, 30.6 s for thirty bookings.
- **An unbounded queue.** Rejected: the producer is faster than the consumer, so the queue only ever grows; 29 emails waiting after thirty bookings, and no limit except the machine's memory.
- **An actor per room.** Rejected: the work is one email per booking with no per-room state to guard, and an actor per room would also serialise bookings and pull the booking rule out of the database, which ADR-003 put there.
- **A reactive pipeline.** Rejected: there is only one stage, so there is nothing to chain and no stage-to-stage backpressure to carry.
- **Fork–join.** Rejected: the email is not a part of the booking that must be joined back, and nobody waits for it to finish.
- **Structured concurrency.** Rejected: the email must outlive the request that caused it, which is exactly what a scope bound to the request forbids.
- **Software transactional memory.** Rejected: there is no shared in-memory state to compose; the only shared thing is the queue, and it is already thread-safe.

## Consequences

- A booking answers in milliseconds and no longer depends on the mail server: the booking and its email now succeed and fail separately.
- **Under a burst, some guests get a room and no email.** In the measured burst 19 of 30 guests were never told about their reservation. It is a counted, visible number (`notificationsRejected`), not a silent delay, but it is a real cost paid by real guests.
- **A killed process loses every queued email, and nothing records it.** A polite stop drained 7 queued emails in 7 seconds and owed nobody; `kill -9` at the same moment printed nothing, kept all 30 reservations, and lost the 7 emails without a counter or a log line.
- The counters are the monitoring: `notificationsRejected` above zero in normal running means the bound, the worker count or the sender is wrong, and `notificationsQueued` shows which way it is leaning.
- The drain has no time limit and the app keeps accepting work while it drains; a production shutdown would stop accepting first, drain with a deadline, and report what it dropped.

## When to revisit

- When a lost confirmation stops being acceptable: write the job into the database in the same transaction as the booking (a transactional outbox), which P17 builds.
- When `notificationsRejected` is regularly above zero outside a genuine burst.
- When a second kind of background job appears, at which point a real job runner becomes cheaper than hand-rolled queues and workers.
