Peak: 3.33 searches a second = 200 a minute (1,440 visitors × 20 searches ÷ 86,400 seconds × peak factor 10), the load in S1.

# L04 notes

## 1 · How busy the app must be

1,440 visitors × 20 searches = 28,800 searches a day, 0.33 a second on average. At the peak (× 10) that is 3.33 a second, or 200 a minute. Each search asks once per room, so 40 rooms give 133 database questions a second at the peak. Bookings are rare in comparison: 20 a day, one for every 1,440 searches.

## 2 · How a request travels to the database

A guest's request goes over HTTP to the app's API, which calls BookingService; BookingService asks the store, and the store asks the database. The answer comes back the same way and becomes an HTTP answer: Accepted → 201 with the new id, Unavailable → 409, InvalidDates → 400. Every error is problem details with three keys: title, status and detail; clients compare the title.

The console app cannot do this yet: it has no network address, it loses every booking when it stops, and two copies of it keep separate lists, so both could accept R7.

## 3 · When a cache helps

The search is read-heavy, with 1,440 searches for every booking and 133 database questions a second at the peak, so caching the free-room search can take most of that load off the database. The risk is a stale answer: just after Maya books R7, a cached search can still show R7 as free. The booking rule must still check the database itself.
