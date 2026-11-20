# Cache experiment: the free-room search

PostgreSQL is the source of truth. Redis keeps copies of search answers for 30 seconds.

## Measurements

1,000 searches for 12–14 Nov, one after another, with the measurement tool.
Times from one laptop; yours will differ.

| Run | Searches | Database queries | Cache hits | Average | p95 |
|---|---|---|---|---|---|
| No cache | 1,000 | 2,000 | 0 | 2.0 ms | 3.4 ms |
| Cache-aside, TTL 30 s | 1,000 | 2 | 999 | 1.4 ms | 2.4 ms |

S1 asks for p95 ≤ 1 second: both runs meet it here (3.4 ms and 2.4 ms), with two rooms and one user.
The cache removes 1,998 of 2,000 database queries. With 40 rooms it would remove 39,960 of 40,000.

## Stale result (Step 5, before invalidation)

1. Empty cache. Search 12–14 Nov: R7 and R8 (a miss: the answer is cached for 30 s).
2. Maya books R7 for 12–14 Nov: 201 R101.
3. Search again within 30 s: R7 and R8. STALE: R7 is booked.
4. Omar asks for R7 for 13–15 Nov: 409 Unavailable. Booking reads PostgreSQL,
   not the cache, so the booking rule still holds.

Seen on this app: all four requests ran within one second of the first search. At that moment
PostgreSQL held R101 for R7 (Confirmed) while Redis still held availability:v0:2026-11-12:2026-11-14 = "R7,R8".
