# ADR-003: Protect the booking rule in the database

Status: Accepted, 20 November 2026

Extends ADR-001 (the modular monolith) and ADR-002 (the search cache); supersedes nothing.

## Context

The booking rule is: a room never has two Confirmed reservations covering the same night. Until P09 it lived only in `BookingService.reserve`, which checks for an overlap and then saves. With one request at a time it holds. With two at the same moment it does not: both checks read "no clash", both requests insert, both commit, and R7 is double-booked. `tools/RaceHttp.java` reproduces this on every run against an app started with `HOTEL_SLOW_STEP=1`, and `docs/interleaving.md` has the statement-by-statement trace taken from PostgreSQL's own log. In that trace Maya even committed before Omar inserted; Omar was double-booked anyway, because his check had already read 0.

The anomaly is write skew: both transactions read the same empty set and each inserted a different row. Repeatable Read does not stop it, and `SELECT … FOR UPDATE` on the reservations does not either, because at check time there is no overlapping row to lock.

## Decision

Enforce the rule in PostgreSQL with an exclusion constraint, in `db/init.sql` and in the running database:

```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE reservations
    ADD CONSTRAINT reservations_no_overlap
    EXCLUDE USING gist (
        room WITH =,
        daterange(check_in, check_out, '[)') WITH &&
    ) WHERE (status = 'Confirmed');
```

Keep the pre-check in `BookingService.reserve` in front of it. The pre-check answers the ordinary case with a readable `409 Unavailable`; the constraint is what makes the rule true when two requests check at the same moment. `PostgresReservationStore.save` recognises exactly SQLSTATE `23P01` on `reservations_no_overlap`, throws `OverlapRejectedException`, and the request's transaction is rolled back; `BookingService` answers `Unavailable`, so the guest gets the same `409` and never sees a database error. Every `ReservationStore` makes the same promise: `save` refuses an overlap. The list store keeps it with a check under a lock.

## Alternatives considered

- **An in-process lock** (check and save under `synchronized` in the list store). With one process it works: `--race` gives one Accepted and one Unavailable. With two instances on ports 5080 and 5081 sharing one database, the double booking came straight back (Step 4), because each process has its own lock and neither waits for the other. Rejected: `docs/capacity.md` already assumes the hotel can add instances.
- **`SELECT … FOR UPDATE` on a guard row** (one row per room in `room_booking_guard`, locked at the start of every booking). Correct, and measured: the losing booking waited **0.91 seconds** at its `FOR UPDATE` for a room it was never going to get. Rejected because every booking for a room queues behind the one in front for as long as that transaction lasts, including any slow work inside it, and because every write path must remember to take the lock and every room needs a guard row forever.
- **Optimistic version checking** (the same guard row with a `version` column, claimed with `UPDATE … WHERE version = <what was read>`). Correct, and measured: **nobody waited**, every statement took about 1 ms, but the loser's claim matched 0 rows, so it rolled back and redid its read and its check. Rejected because it adds a retry loop with a limit to every write path, and like the guard row it protects only code that remembers to use it.
- **Serializable isolation.** Would stop write skew with no schema change, but every transaction in the app would then have to handle serialization failures and retry. Not tried in P09; rejected for now as a larger change than one rule needs.

## Consequences

- The rule is now true whichever code path writes a reservation: the race across two instances gave one 201 and one 409, and a direct `INSERT` typed into psql was refused by `reservations_no_overlap`.
- A booking can now fail at the INSERT, and the app must turn that into `409`. Two tests keep this true: two bookings at the same moment leave one reservation, and `save` refuses an overlap even without the pre-check (19 checks in all).
- The constraint could not be switched on while the table already held the double booking: PostgreSQL refused the `ALTER TABLE` until R102 was cancelled. Turning any rule on over live data means finding and fixing the rows that already break it, and in a real hotel somebody would have to phone one of the two guests.
- A Cancelled reservation blocks nothing, so a guest can rebook a stay they cancelled.
- The constraint covers only a rule that fits inside the `reservations` table; a rule spanning tables would need one of the rejected repairs.
- Every reservation write also maintains a GiST index. At the load in `docs/capacity.md` (20 bookings a day, 3 database questions each) this does not matter.
- `db/init.sql` and the running database both have to change, because `init.sql` runs only when the database is created.

## When to revisit

- When a second rule needs protecting that does not fit inside one table.
- When bookings become frequent enough for the GiST index writes to show up in a measurement.
- When reservations are written by more than one service, at which point the constraint becomes the only thing that every writer is guaranteed to pass through.
