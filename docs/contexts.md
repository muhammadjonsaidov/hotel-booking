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

Rules this context owns:

- A room that is out of service is not offered to guests.
- The nightly price depends on the room type (Deluxe costs more than Standard).

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

Rules this context owns:

- A room never has two Confirmed reservations that share a night.
- Check-out must be after check-in.
- Cancelling a reservation frees its nights for new bookings.

## Payments

Job: takes deposits and records whether they were paid.

| Word | Meaning here |
| --- | --- |
| Deposit | A payment of 30% of the stay’s price, taken when the guest books. |
| Paid | The deposit was successfully collected. |
| Declined | The payment provider refused the deposit payment. |

Rules this context owns:

- The deposit is 30% of the stay’s price.
- Only a Paid deposit can be refunded.

## Notifications

Job: tells guests what happened, by email or SMS.

| Word | Meaning here |
| --- | --- |
| Confirmation message | An email or SMS telling a guest that their reservation is confirmed. |
| Sent | The confirmation message was sent successfully; this is a message status. |
| Failed | The confirmation message could not be sent; this is a message status that never changes the reservation. |

Rules this context owns:

- A failed message is recorded as Failed and never changes the reservation.

## Same word, different meaning

| Word | Rooms | Reservations | Payments | Notifications |
| --- | --- | --- | --- | --- |
| Status | In service / out of service | Confirmed / Cancelled | Paid / Declined | Sent / Failed |
| Room | A room such as R8, with its type, nightly price and service availability. | Only the room number, such as R8, on a reservation. | Not used. | Text identifying the room in a confirmation, such as “Room R8”. |
| Cancel | Remove a room from sale. | Withdraw the guest’s reservation and free its nights. | Refund a deposit. | Not used. |

If Notifications changed Maya’s reservation status from Confirmed to Failed when an SMS failed, Maya could lose R7 because the reservation would stop blocking its nights, violating R6.

## Rule owners

| Card | Rule | Owner | Why this owner |
| --- | --- | --- | --- |
| 1 | A room never has two Confirmed reservations that share a night. | Reservations | It keeps every reservation of the room, so it can check them all together. |
| 2 | Check-out must be after check-in. | Reservations | It owns the stay dates and validates them before accepting a reservation. |
| 3 | Cancelling a reservation frees its nights for new bookings. | Reservations | It keeps reservation statuses and decides which reservations still block the room. |
| 4 | A room that is out of service is not offered to guests. | Rooms | It keeps each room’s service availability and knows which rooms are on sale. |
| 5 | The nightly price depends on the room type (Deluxe costs more than Standard). | Rooms | It keeps each room’s type and nightly price. |
| 6 | The deposit is 30% of the stay’s price. | Payments | Money rules belong to Payments; it asks Rooms for the nightly price. |
| 7 | Only a Paid deposit can be refunded. | Payments | It keeps each deposit’s payment status and decides whether a refund is allowed. |
| 8 | A failed message is recorded as Failed and never changes the reservation. | Notifications | It owns delivery outcomes and message status, while reservation status belongs to Reservations (R6). |
