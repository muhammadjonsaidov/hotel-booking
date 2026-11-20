# The interleaving that double-books R7

## What I ran

`tools/RaceHttp.java` sends two `POST /reservations` for room R7, 2026-11-12 to
2026-11-14: Maya's first and Omar's 500 ms later, to the app started with
`HOTEL_SLOW_STEP=1` (`HOTEL_SLOW_STEP=1 ./gradlew bootRun`, then `java tools/RaceHttp.java`).
The database was empty, so R7 was free for those nights.

```
Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.

Maya -> 201 Created   {"id":"R101","status":"Confirmed","notificationFailed":false}
Omar -> 201 Created   {"id":"R102","status":"Confirmed","notificationFailed":false}

Confirmed reservations for R7 on 12-14 Nov: 2
DOUBLE BOOKED: the room has two confirmed reservations for the same nights.
```

## The interleaving, statement by statement

Taken from PostgreSQL's own statement log (`log_statement = 'all'`, switched on only for this
run). Maya's transaction was process 108, Omar's process 109; times are UTC on 20 November 2026.

| # | Time | Maya's request | Omar's request | What the database holds |
|---|---|---|---|---|
| 1 | 42.032 | `BEGIN` | | no confirmed reservation for R7 |
| 2 | 42.033 | `SELECT count(*)` of overlapping Confirmed → 0 | | unchanged |
| 3 | 42.484 | | `BEGIN` | unchanged |
| 4 | 42.486 | | `SELECT count(*)` of overlapping Confirmed → 0 | unchanged: Maya has inserted nothing yet |
| 5 | 43.061 | `nextval` → 101, `INSERT R101` | | R101 exists, uncommitted and invisible to Omar |
| 6 | 43.083 | `COMMIT` | | R101 is now visible to everyone |
| 7 | 43.490 | | `nextval` → 102, `INSERT R102` | R101 and R102 both exist |
| 8 | 43.492 | | `COMMIT` | R101 and R102 are both Confirmed for the same nights |

Both requests read "no clash", and both were telling the truth when they read it. The gap is
between each request's check (rows 2 and 4) and its insert (rows 5 and 7): the check and the save
are separate statements, and the other transaction fits between them.

In this run Maya even committed (row 6) before Omar inserted (row 7). It made no difference:
Omar had already decided at row 4, while R7 was still free, and nothing made him look again.
Committing sooner does not close the gap; only something that stops the second insert does.

## Which anomaly this is

Write skew: both transactions read the same set (the Confirmed reservations overlapping the stay),
found it empty, and each inserted a different row; neither overwrote the other, so it is not a lost update.
Repeatable Read does not stop it, because it only protects rows a transaction has read, and no row
either request read ever changed; Serializable does, by aborting one of the two.

## The invariant this breaks

> A room never has two Confirmed reservations covering the same night.

## Why locking the reservation rows cannot fix it

`SELECT … FOR UPDATE` on the overlapping reservations locks the rows that query returns, and at
check time (rows 2 and 4) it returns none: you cannot lock a row that does not exist yet.

## Two instances (Step 4)

The same race, sent to two copies of the app that share one PostgreSQL and one Redis:
`HOTEL_SLOW_STEP=1` on port 5080 and `HOTEL_SLOW_STEP=1 HOTEL_PORT=5081` on port 5081
(built once with `./gradlew bootJar` and started twice with `java -jar`), then
`java tools/RaceHttp.java http://localhost:5081`. The code did not change.

```
Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.
Maya -> http://localhost:5080   Omar -> http://localhost:5081

Maya -> 201 Created   {"id":"R101","status":"Confirmed","notificationFailed":false}
Omar -> 201 Created   {"id":"R102","status":"Confirmed","notificationFailed":false}

Confirmed reservations for R7 on 12-14 Nov: 2
DOUBLE BOOKED: the room has two confirmed reservations for the same nights.
```

psql showed R101 and R102, both Confirmed for R7, 2026-11-12 to 2026-11-14. Each instance's
GET /stats counted only its own 3 database queries: the two processes share nothing but the database.
The ids still differ because the sequence lives in that shared database; the booking rule does not.

## SELECT … FOR UPDATE on a guard row (Step 5)

A guard table owned by Reservations, one row per room, created in the running database:
`CREATE TABLE room_booking_guard (room text PRIMARY KEY, version integer NOT NULL DEFAULT 0)`
with rows for R7 and R8. Two psql sessions on an empty database: terminal A (Maya) held its
transaction open for one second after its check; terminal B (Omar) started 0.1 s after A.
Times are from PostgreSQL's own clock.

```
18:12:37.287  Maya   BEGIN
18:12:37.288  Maya   SELECT ... FOR UPDATE returned, so Maya holds the lock on R7
18:12:37.288  Maya   overlap check sees 0
18:12:37.386  Omar   BEGIN
18:12:37.387  Omar   SELECT ... FOR UPDATE  ... waiting ...
18:12:38.293  Maya   INSERT R101
18:12:38.293  Maya   COMMIT, which releases the lock
18:12:38.295  Omar   ... the wait ends, Omar now holds the lock (waited 0.91 s)
18:12:38.296  Omar   overlap check sees 1
18:12:38.296  Omar   refuses: Unavailable (nothing inserted)

Confirmed reservations for R7: 1
```

Because both sessions lock the same row, which always exists, the second check runs only after the
first booking has committed, and it sees it.

## Optimistic version checking (Step 6)

The same guard row, without a lock. The guard row was reset to version 0 and the reservation from
Step 5 deleted. Terminal A (Maya) read the version with a plain SELECT, checked, and held its
transaction for one second before claiming; terminal B (Omar) started 0.1 s after A. Each claim is
`UPDATE room_booking_guard SET version = version + 1 WHERE room = 'R7' AND version = <what it read>`.
Times are from PostgreSQL's own clock.

```
18:14:00.220  Maya   BEGIN
18:14:00.221  Maya   reads version = 0   (a plain SELECT: no lock, waited 0.001 s)
18:14:00.221  Maya   overlap check sees 0
18:14:00.337  Omar   BEGIN
18:14:00.337  Omar   reads version = 0   (the same 0; waited 0.001 s)
18:14:00.338  Omar   overlap check sees 0
18:14:00.338  Omar   UPDATE ... WHERE version = 0 matched 1 row, so Omar owns the claim (waited 0.001 s)
18:14:00.339  Omar   INSERT R102, COMMIT
18:14:01.230  Maya   UPDATE ... WHERE version = 0 matched 0 rows (waited 0.001 s)
18:14:01.230  Maya   ROLLBACK, then try again
18:14:01.231  Maya   reads version = 1
18:14:01.232  Maya   overlap check sees 1
18:14:01.232  Maya   refuses: Unavailable (nothing inserted)

Confirmed reservations for R7: 1 (R102 Omar)
```

Matching 0 rows is not an error; it is how the loser learns that what it read is out of date. The
retry has to redo the read and the overlap check too, not just the UPDATE, and a real retry loop
needs a limit.

## The exclusion constraint (Step 7)

```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;
ALTER TABLE reservations
    ADD CONSTRAINT reservations_no_overlap
    EXCLUDE USING gist (room WITH =, daterange(check_in, check_out, '[)') WITH &&)
    WHERE (status = 'Confirmed');
```

A direct INSERT typed into psql, with the app not involved at all, for R7 on 13–15 Nov while R101 held 12–14 Nov:

```
ERROR:  conflicting key value violates exclusion constraint "reservations_no_overlap"
DETAIL:  Key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-13,2026-11-15)) conflicts with existing key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-12,2026-11-14)).
```

The Step 4 race again, on a database created from the new db/init.sql, Maya to 5080 and Omar to 5081:

```
Two bookings for R7, 2026-11-12 to 2026-11-14, sent at the same moment.
Maya -> http://localhost:5080   Omar -> http://localhost:5081

Maya -> 201 Created   {"id":"R101","status":"Confirmed","notificationFailed":false}
Omar -> 409 Conflict  {"title":"Unavailable","status":409,"detail":"The room is booked for at least one night of this stay."}

Confirmed reservations for R7 on 12-14 Nov: 1
CORRECT: one booking was accepted and one was refused.
```

Omar's pre-check still saw 0. His request asked the sequence for R102 (it stood at 102 afterwards) and
sent its INSERT, which PostgreSQL refused with SQLSTATE 23P01 on reservations_no_overlap. The store
turned that into OverlapRejectedException, BookingService answered Unavailable, the unit of work rolled
back, and the API answered 409. Nothing was written for Omar.

## The four repairs, and what each one cost

| Repair | Did it stop the double booking? | What it cost, measured |
|---|---|---|
| In-process lock | With one instance, yes. With two, no. | Almost nothing: one lock, held only while save checks and appends. With one process `--race` now gives Maya Accepted R101, Omar Unavailable, and 1 Confirmed reservation for R7. |
| `SELECT ... FOR UPDATE` on a guard row | Yes | Omar waited **0.91 seconds** at his `FOR UPDATE` for a room he was never going to get. Every booking for a room queues behind the one in front, for as long as that transaction lasts, including any slow work inside it. |
| Optimistic version checking | Yes | **Nobody waited**: every statement, the losing UPDATE included, took about 1 ms. Both sessions read `version = 0`; Maya's `UPDATE ... WHERE version = 0` matched 0 rows, so she rolled back and did her read and check a second time before refusing. Cheap when clashes are rare, wasteful when they are common. |
| Exclusion constraint | Yes, across two instances and for a direct INSERT in psql | Only covers a rule that fits inside one table; the refusal arrives as a database error (23P01) the store must recognise and turn into a readable 409; and it would not switch on until the double booking already in the table was cleaned up (R102 cancelled). |

A rule that several processes must obey has to live in the one thing they all share: the database.


## What it took to turn the constraint on

The constraint could not simply be added. The table held the two Confirmed reservations the race had
created, and PostgreSQL refused to create a rule the existing rows already broke:

```
ERROR:  could not create exclusion constraint "reservations_no_overlap"
DETAIL:  Key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-12,2026-11-14)) conflicts with key (room, daterange(check_in, check_out, '[)'::text))=(R7, [2026-11-12,2026-11-14)).
```

After `UPDATE reservations SET status = 'Cancelled' WHERE id = 'R102';` the same ALTER TABLE succeeded.
The rule had been broken for as long as it went unenforced, and the broken rows were still there; in a
real hotel somebody would have to telephone one of the two guests. A Cancelled row does not block the
constraint, so a guest can still rebook a stay they cancelled.

## What I kept

The exclusion constraint, with the pre-check left in front of it. It is the only one of the four repairs
that no code path can forget: a new endpoint, a background job, a migration script or somebody typing
INSERT into psql all hit it. The pre-check stays so that the ordinary case still answers a clean
409 Unavailable without the database having to refuse anything.
