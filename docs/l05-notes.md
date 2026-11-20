Repair chosen: teach PostgreSQL the rule itself with an exclusion constraint, so no code path can save two Confirmed reservations for the same room and night.

# L05 notes

## 1 · Why two bookings at the same moment can both succeed

BookingService checks for a clash and then saves: two separate statements inside a transaction. Two requests for R7 can both run the check before either inserts, both read "no confirmed reservation", and both insert. The anomaly is write skew: each transaction reads the same set of rows, finds it empty, and inserts a different row, so nothing is overwritten and no lost update happens. PostgreSQL's default Read Committed does not stop it, and neither does Repeatable Read, because no row either transaction read ever changed. Serializable does stop it, by aborting one of the two with a serialization failure.

## 2 · Why locking the reservation rows cannot fix it

SELECT … FOR UPDATE locks the rows a query returns. When R7 is free, the query for overlapping reservations returns no rows, so there is nothing to lock: you cannot lock a row that does not exist yet. A lock only helps if both requests lock the same existing row, such as a guard row per room, and then every booking for that room waits in line behind the one in front.

## 3 · The four repairs, and the one to build

- A lock inside the app: protects one process only; a second instance breaks the rule again.
- SELECT … FOR UPDATE on a guard row (pessimistic): correct, but the loser waits.
- A version number checked on update (optimistic): correct, nobody waits, but the loser redoes its work.
- An exclusion constraint on (room, date range) for Confirmed reservations: the database refuses the second row whatever code sent it. The app keeps its pre-check for a clean 409 and turns the constraint violation into 409 Unavailable too.

The one to build is the exclusion constraint, because it is the only repair that no new endpoint, background job or manual INSERT can forget.
