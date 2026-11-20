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

## The four repairs, and what each one cost

| Repair | Did it stop the double booking? | What it cost, measured |
|---|---|---|
| In-process lock | | |
| `SELECT ... FOR UPDATE` on a guard row | | |
| Optimistic version checking | | |
| Exclusion constraint | | |
