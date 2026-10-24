# P03 design notes

## Why design before code

Low-level design decides which classes the app needs, what each class stores and does, and which class calls which before we write the code.

## Where the methods live

Cancel() lives in Reservation because cancelling changes the reservation’s own status from Confirmed to Cancelled.

HasOverlap() lives in ReservationStore because only the store can see all reservations and check whether any blocks the requested room and dates.
