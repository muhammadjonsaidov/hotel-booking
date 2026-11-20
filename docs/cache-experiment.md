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
