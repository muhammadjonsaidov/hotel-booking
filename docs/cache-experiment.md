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

## After invalidation (Step 6)

A successful POST (201) or DELETE (204) raises availability:version after the unit of work has saved the change;
a 409, 400 or 404 leaves it unchanged. The same four requests as in Step 5, from an empty database and cache:

1. Search 12–14 Nov: R7 and R8 (a miss under version 0: availability:v0:2026-11-12:2026-11-14 = "R7,R8").
2. Maya books R7 for 12–14 Nov: 201 R101. The version goes from 0 to 1.
3. Search again within 30 s: R8 only. FRESH: the key is now availability:v1:2026-11-12:2026-11-14,
   a miss, so PostgreSQL answered. The old v0 copy is never read again and expires with its TTL.
4. Omar asks for R7 for 13–15 Nov: 409 Unavailable. Nothing was saved, so the version stays 1.

GET /stats after the four requests: {"searches":2,"databaseQueries":8,"cacheHits":0,"cacheMisses":2}.
Without the invalidate line in POST, the test aBookingAndACancelMakeTheNextSearchFresh fails: the stale R7 comes back.

## Hot key and the next bottleneck (Step 7)

- New Year: when everyone searches 31 Dec–2 Jan, a single key answers almost every search,
  availability:v{version}:2026-12-31:2027-01-02. Measured: 1,000 searches gave 999 hits, 1 miss
  and 2 database queries, all from availability:v1:2026-12-31:2027-01-02. That key is hot: one Redis
  server does nearly all the work, even if Redis is later spread over several servers.
- Each New Year booking raises the version and so throws that hot copy away. Measured: Maya's booking
  for R7 (201 R102) moved the version from 1 to 2; the next search missed, asked PostgreSQL and stored
  availability:v2:2026-12-31:2027-01-02 (R8 only), while the old v1 copy waited out its 30 seconds.
  In a busy sale every booking sends the next search back to PostgreSQL.
- Searches now cost Redis, not PostgreSQL. What is left for PostgreSQL is POST /reservations:
  that booking cost exactly 3 questions (the overlap check, the count for the new id and the save),
  and they must be exact. Two bookings for the same room at the same moment can still both pass the
  check (the P03 race). That is the next bottleneck: P09.
