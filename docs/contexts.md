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

Needs from other contexts: nothing.

Tells other contexts: the room list, with each room’s number and type, as the answer to Reservations.

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

Needs from other contexts: the room list from Rooms; Paid or Declined from Payments when deposit processing is introduced.

Tells other contexts: ReservationConfirmed to Notifications after saving; a deposit request to Payments when deposit processing is introduced.

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

Needs from other contexts: the reservation id and deposit amount from Reservations when deposit processing is introduced.

Tells other contexts: Paid or Declined as the answer to Reservations; it never decides room availability.

## Notifications

Job: tells guests what happened, by email or SMS.

| Word | Meaning here |
| --- | --- |
| Confirmation message | An email or SMS telling a guest that their reservation is confirmed. |
| Sent | The confirmation message was sent successfully; this is a message status. |
| Failed | The confirmation message could not be sent; this is a message status that never changes the reservation. |

Rules this context owns:

- A failed message is recorded as Failed and never changes the reservation.

Needs from other contexts: ReservationConfirmed with the reservation id, guest email, room number, check-in and check-out from Reservations.

Tells other contexts: nothing to other contexts; it owns message delivery and records its outcome without changing a reservation.

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

## Between contexts

These contracts describe the intended context boundaries: payment processing and event delivery are planned, while the current notification interface still makes a synchronous call with a Reservation object.

### Reservations → Notifications: ReservationConfirmed (event)

Sent when: a reservation is saved as Confirmed.

Fields:

- reservationId: R101
- guestEmail: maya@example.com
- roomNumber: R7
- checkIn: 2026-11-12
- checkOut: 2026-11-14

Reservations does not wait for an answer; Notifications chooses email or SMS and records delivery success or failure.
A failed message never changes R101 (R6).

Never crosses: Reservations’ list of reservations, its internal Reservation object or its booking rules; Notifications receives only the five fields above and never reads or changes reservation storage.

### Reservations → Rooms: which rooms exist? (question)

Sent when: Reservations needs the room catalog to search free rooms or book a room.

What Reservations sends: a request for the room list, with no guest, stay or reservation fields.

The answer and its fields: a list of rooms, each with number and type, such as number R7 with type Standard and number R8 with type Deluxe; Reservations waits for the answer and checks booked nights itself.

Never crosses: Rooms’ private storage or implementation; Reservations’ guest data, stays and list of reservations remain in Reservations.

### Reservations → Payments: take a deposit (question, planned)

Sent when: a booking reaches the deposit stage after payment processing is introduced.

What Reservations sends:

- reservationId: R101
- amount: 72.00, representing a 30% deposit for a stay priced at 240.00

The answer and its fields: the deposit status, Paid or Declined; Reservations waits for that answer and decides what it means for the booking.

Never crosses: payment credentials, provider internals or Payments’ private deposit records; Reservations’ list of reservations and booking rules also stay private, and Payments never changes reservation status or decides room availability.
