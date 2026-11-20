# ADR-002: Cache the free-room search in Redis

Status: Accepted, 20 November 2026

## Context

The free-room search asks PostgreSQL once per room. With two rooms, 1,000 searches for 12–14 November cost 2,000 database queries (docs/cache-experiment.md); with the 40 rooms of the estimate that is 133 queries a second at the peak (docs/capacity.md). A search answer only changes when someone books or cancels, so most searches recompute the same answer. The booking rule (S2: no double booking) must never depend on an old copy.

## Decision

Use cache-aside in Redis for GET /rooms/availability only. The key is availability:v{version}:{checkIn}:{checkOut}, the value is the free room numbers in text order (such as "R7,R8"), and the TTL is 30 seconds. A successful booking (201) or cancel (204) raises availability:version after its change is saved in PostgreSQL, so every older copy becomes unreachable. POST /reservations and DELETE /reservations/{id} never read the cache: PostgreSQL is the source of truth.

## Alternatives considered

- No cache: always fresh and the simplest code, but every search costs one database query per room.
- TTL only: simple, but right after a booking a search can still list the booked room for up to 30 seconds (seen in Step 5: R7 listed after Maya's 201).
- Invalidation only: fresh after every change, but a single missed invalidation would keep a wrong copy until Redis restarts.

## Consequences

- 1,000 searches for the same stay cost 2 database queries instead of 2,000: 999 hits and 1 miss (docs/cache-experiment.md); average 2.0 → 1.4 ms and p95 3.4 → 2.4 ms on one laptop.
- If an invalidation is ever missed, a copy is wrong for at most 30 seconds, and only for searches: booking still asks PostgreSQL, so a stale copy cannot cause a double booking.
- Every booking or cancel makes every cached search old. In a busy sale, such as New Year where one hot key answers almost every search, the hit rate falls and searches return to PostgreSQL after each booking.
- Redis is one more program to run. The app starts only when Redis answers. While Redis is down, a search fails at once with 500 (measured: 16 ms), and a booking or cancel is saved in PostgreSQL but answers with an error, because the version cannot be raised. There is no fallback to PostgreSQL yet.
- Two misses at the same moment both ask PostgreSQL and both store a copy.

## Revisit when

Bookings rather than searches limit the app (each booking costs 3 exact database queries, and two at the same moment can still both pass the check: P09), or the hit rate measured under real load is much lower than in docs/cache-experiment.md.
