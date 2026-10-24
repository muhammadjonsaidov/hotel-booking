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
