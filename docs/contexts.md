# Hotel contexts

## Words

| Word | Meaning here | Name in our code |
| --- | --- | --- |
| Stay | Check-in to check-out, half-open: 12–14 November means the nights of the 12th and 13th. | DateRange |
| Reservation | A guest, room and stay with a status and an id such as R101. | Reservation |
| Confirmed | The room is promised to this guest for these nights. | ReservationStatus.CONFIRMED |
| Guest | The person staying at the hotel, such as Maya, with a name and email address. | Guest |
| Room | A room the hotel sells, such as R7 Standard or R8 Deluxe. | Room |
| Room type | The category of a room, such as Standard or Deluxe. | Room.type() |
| Nightly price | The amount charged for one night in a room, based on its type. | not in code yet |
| Out of service | A room is unavailable for sale, for example while R8 is being repaired. | not in code yet |
| Cancelled | A reservation’s promise is withdrawn, so it no longer blocks the room’s nights. | ReservationStatus.CANCELLED |
| Book | Request a room for a guest and stay, receiving a reservation id if accepted. | BookingService.reserve(guest, room, stay) |
| Cancel | Withdraw a reservation by its id, such as R101, while keeping its history. | BookingService.cancel(reservationId) |
| Free rooms | The rooms with no Confirmed reservation sharing a night with the requested stay. | BookingService.findFreeRooms(stay) |
| Deposit | A payment of 30% of the stay’s price, taken when the guest books. | not in code yet |
| Paid | The deposit was successfully collected. | not in code yet |
| Declined | The payment provider refused the deposit payment. | not in code yet |
| Confirmation message | An email or SMS telling a guest that their reservation is confirmed. | NotificationSender.sendConfirmation(reservation) |
| Sent | The confirmation message was sent successfully; this is a message status. | not in code yet |
| Failed | The confirmation message could not be sent; this is a message status that never changes the reservation. | not in code yet |

## Rooms

Job: knows which rooms the hotel sells.

| Word | Meaning here |
| --- | --- |
| Room | A room the hotel sells, such as R7 Standard or R8 Deluxe. |
| Room type | The category of a room, such as Standard or Deluxe. |
| Nightly price | The amount charged for one night in a room, based on its type. |
| Out of service | A room is unavailable for sale, for example while R8 is being repaired. |

## Reservations

Job: decides whether a guest gets a room for some nights, and keeps every reservation.

| Word | Meaning here |
| --- | --- |
| Stay | Check-in to check-out, half-open: 12–14 November means the nights of the 12th and 13th. |
| Reservation | A guest, room number and stay with a status and an id such as R101. |
| Confirmed | The room is promised to this guest for these nights. |
| Cancelled | A reservation’s promise is withdrawn, so it no longer blocks the room’s nights. |
| Guest | The person staying at the hotel, such as Maya, with a name and email address. |
| Book | Request a room for a guest and stay, receiving a reservation id if accepted. |
| Cancel | Withdraw a reservation by its id, such as R101, while keeping its history. |
| Free rooms | The rooms with no Confirmed reservation sharing a night with the requested stay. |

## Payments

Job: takes deposits and records whether they were paid.

| Word | Meaning here |
| --- | --- |
| Deposit | A payment of 30% of the stay’s price, taken when the guest books. |
| Paid | The deposit was successfully collected. |
| Declined | The payment provider refused the deposit payment. |

## Notifications

Job: tells guests what happened, by email or SMS.

| Word | Meaning here |
| --- | --- |
| Confirmation message | An email or SMS telling a guest that their reservation is confirmed. |
| Sent | The confirmation message was sent successfully; this is a message status. |
| Failed | The confirmation message could not be sent; this is a message status that never changes the reservation. |
