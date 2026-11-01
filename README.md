# Hotel Booking

A Java command-line hotel booking app organised as a modular monolith. It searches available rooms, accepts reservations, rejects overlapping stays, and cancels bookings while keeping their history.

## Run locally

Use JDK 25, the Java version used to verify this project. The Gradle wrapper is included; the first build needs internet access to download Gradle and test dependencies.

```sh
./gradlew test
./gradlew run
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

The normal demo prints:

```text
Free rooms 12-14 Nov: R7, R8
Maya books R7 12-14 Nov: Accepted R101
Omar books R7 13-15 Nov: Unavailable
Nora books R7 14-16 Nov: Accepted R102
Omar books R8 15-15 Nov: InvalidDates
Free rooms 12-14 Nov: R8
Maya cancels R101: True
Free rooms 12-14 Nov: R7, R8
Confirmation emails sent: 2
```

## Booking behavior

- Stays include the check-in date and exclude the check-out date: 12–14 November occupies the nights of the 12th and 13th, so another guest can arrive on the 14th.
- Check-out must be after check-in; invalid dates return `InvalidDates`.
- An overlapping Confirmed reservation for the same room returns `Unavailable` when requests run sequentially.
- Accepted bookings return a reservation id, such as `Accepted R101`, and can be looked up by id in the store.
- Cancellation keeps the reservation as Cancelled and frees its nights.
- Notification failure keeps the booking saved and Accepted, with `notificationFailed` set to `true`.

All data lives in memory and is lost when the process exits. Email and SMS senders are simulated counters. The payment simulation returns `PAID` for positive amounts and `DECLINED` for zero or negative amounts; booking does not call it yet.

## Demonstrate the booking race

```sh
./gradlew run --args="--race"
```

The race deliberately pauses between checking availability and saving. Maya and Omar can both see R7 as free and both receive Accepted:

```text
Maya and Omar both book R7 for 12-14 Nov at the same moment...
Maya: Accepted R101
Omar: Accepted R102
Confirmed reservations for R7 on 12-14 Nov: 2
Double booking! Both requests checked before either one saved.
```

This concurrency defect remains intentional. Module boundaries do not make checking and saving atomic, and the exact thread timing is not guaranteed.

## Module structure

The app is one program with four business modules. Each has a public contract and an `internal` package for implementations.

| Module | Public contract | Internal implementation |
| --- | --- | --- |
| Rooms | `Room`, `RoomCatalog` | `InMemoryRoomCatalog` |
| Reservations | Booking service, result, domain types and `ReservationStore` | `InMemoryReservationStore` |
| Payments | `PaymentService`, `PaymentStatus` | `SimulatedPaymentService` |
| Notifications | `Confirmation`, `NotificationSender` | Email and SMS senders |

`Main` creates the concrete implementations and connects them. Reservations uses the other modules’ public contracts. Notifications receives its own five-field `Confirmation` message: reservation id, guest email, room number, check-in and check-out.

```text
src/main/java/hotelbooking/
├── Main.java
├── rooms/
│   └── internal/
├── reservations/
│   └── internal/
├── payments/
│   └── internal/
└── notifications/
    └── internal/
```

## Verification

`./gradlew test` runs 11 checks:

- `BookingServiceTests`: five tests for acceptance, overlap refusal, adjacent stays, invalid dates and cancellation.
- `BookingNotificationTests`: three tests for confirmation ids, failure handling and SMS.
- `ArchitectureTests`: three ArchUnit rules enforcing module dependencies.

The architecture rules prevent Reservations from depending on other modules’ internals, prevent Rooms from depending on other modules, and prevent Payments and Notifications from depending on Reservations. Test code is excluded from those dependency checks.

The boundary checker was verified by temporarily making `BookingService` reference the internal email sender: the expected architecture rule failed while the eight behavior tests passed. The forbidden reference was removed, restoring all 11 passing checks.

The HTML test report is available at `build/reports/tests/test/index.html` after a test run.

## Design documents

- [Requirements](docs/requirements.md): booking rules and verification examples.
- [Design notes](docs/design-notes.md): method responsibilities, booking traces, refactoring results and module verification.
- [Quality scenarios](docs/quality-scenarios.md): stakeholders and measurable quality targets; planned targets are distinguished from existing checks.
- [Business contexts](docs/contexts.md): hotel vocabulary, rule ownership and boundary contracts.
- [Architecture decision](docs/adr-001-modular-monolith.md): why the app uses a modular monolith and when to reconsider that choice.
- [Diagrams](docs/diagrams.drawio): open in draw.io; the Context map and Modules pages show business ownership and module dependencies.

## Development progression

1. Built the in-memory booking model, documented requirements and demonstrated the check-then-save race.
2. Added behavior tests, extracted storage and notification interfaces, and introduced failure reporting and SMS support.
3. Defined four business contexts, a shared vocabulary, quality scenarios, rule ownership and boundary contracts.
4. Moved the code into four modules, introduced the room catalog and Confirmation message, added simulated payments, and enforced dependencies with architecture tests.
