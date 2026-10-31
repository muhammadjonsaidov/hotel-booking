# Hotel booking design notes

## Why design before code

Low-level design decides which classes the app needs, what each class stores and does, and which class calls which before we write the code.

## Where the methods live

Cancel() lives in Reservation because cancelling changes the reservation’s own status from Confirmed to Cancelled.

HasOverlap() lives in ReservationStore because only the store can see all reservations and check whether any blocks the requested room and dates.

## Booking request traces

Omar asks for R7 on 13–15 November, while Maya has 12–14. First diamond: is check-out after check-in? Yes. Second diamond: does a Confirmed booking share a night? Yes, the 13th. The request ends at “Unavailable · save nothing”.

Nora asks for R7 on 14–16 November, while Maya has 12–14. First diamond: is check-out after check-in? Yes. Second diamond: does a Confirmed booking share a night? No, Maya checks out on the 14th, so there is no shared night. The reservation is saved as Confirmed, the confirmation email is sent, and the request ends at “Accepted + reservation id”.

Omar asks for R8 on 15–15 November. First diamond: is check-out after check-in? No, the dates are equal. The second diamond is not reached. The request ends at “InvalidDates · save nothing”.

## Prediction

Nora’s request for R7 on 14–16 November should return Accepted R102 because Maya checks out on the 14th and the stays share no night.

Observed: Nora books R7 14-16 Nov: Accepted R102. The result matches the prediction.

## The race

Prediction: R7 will have two Confirmed reservations for 12–14 November because Maya and Omar both check availability before either request saves.

Observed output:

```text
Maya and Omar both book R7 for 12-14 Nov at the same moment...
Maya: Accepted R101
Omar: Accepted R102
Confirmed reservations for R7 on 12-14 Nov: 2
Double booking! Both requests checked before either one saved.
```

Comparison: the output matches the prediction and the expected result of two Confirmed reservations.

Maya and Omar both received Accepted for R7 on 12–14 November, leaving two Confirmed reservations for the same nights and breaking requirement R3.
The race occurs between store.hasOverlap(room, stay) and store.save(reservation) inside reserve(): both checks see an empty store before either request saves, so both requests proceed using their earlier answers.

# Refactoring design notes

## Refactoring

Refactoring changes how code is organised while preserving its existing behavior.

Must stay the same:

- All six characterization tests must pass after every refactoring move once they are written.
- The demo must print the same nine lines.
- The race demo must still show double booking.

## What our code breaks

- BookingService asks for the concrete ReservationStore class (D: dependency inversion). Switching from the in-memory list to a database store would require changing BookingService’s dependency even though the booking rules stay the same.
- BookingService asks for the concrete EmailSender class (D: dependency inversion, and O: open/closed). Supporting SMS would require editing BookingService instead of supplying another implementation of a shared notification interface.

## Proof

Tests: all 6 tests passed, with 0 failures, errors or skipped tests.
Demo: the same nine output lines as before the refactoring, matched line by line.
Race: still double-booked R7 for 12–14 November, with Maya accepted as R101 and Omar as R102; 2 Confirmed reservations, matching the previous output.
