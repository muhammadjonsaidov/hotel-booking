# P06 · Activity sheet

## 1. P06 · The design is on paper. Now make the borders real in the code.

In P05 you wrote the four contexts down. Today the code moves into four modules, and a test fails the moment one module reaches into another’s internal code.





### Where we are

Your hotel-booking folder has the P04 code (8 green tests) and the P05 design: docs/contexts.md with the four contexts, their rules and what crosses each border, and the draw.io page “Context map”.

**What is a module here?** In short: A module is one business area of the app in its own folder, with a public part other modules may use and an internal part they may not. A modular monolith is still one program that you build and run once. The modules only make the borders inside it visible, and the architecture tests keep them.

1. In P05 you wrote four contexts: Rooms, Reservations, Payments, Notifications.
2. Today each one becomes a folder with the same name.
3. Its public contract is the few types other modules need: for Rooms, the Room type and the room catalog promise.
4. Its internal code is everything else: for Rooms, the list-based catalog that only the wiring creates.

C#:

```text
// a module is a namespace (and a folder)
namespace HotelBooking.Rooms;            // public contract: Room, IRoomCatalog
namespace HotelBooking.Rooms.Internal;   // internal code: InMemoryRoomCatalog
```

Python:

```text
# a module is a package: a folder with __init__.py
hotel_booking/rooms/__init__.py   # public contract: Room, RoomCatalog
hotel_booking/rooms/internal.py   # internal code: InMemoryRoomCatalog
```

Java:

```text
// a module is a package (and a folder)
package hotelbooking.rooms;            // public contract: Room, RoomCatalog
package hotelbooking.rooms.internal;   // internal code: InMemoryRoomCatalog
```

Go:

```text
// a module is a package: one folder, one package name
rooms/rooms.go      // package rooms - public contract: Room, RoomCatalog
rooms/internal.go   // package rooms - internal code: InMemoryRoomCatalog
```

JavaScript:

```text
// a module is a folder under src/
src/rooms/index.js              // public contract: Room
src/rooms/internal/catalog.js   // internal code: InMemoryRoomCatalog
```

Dart:

```text
// a module is a folder under lib/
lib/rooms/rooms.dart         // public contract: Room, RoomCatalog
lib/rooms/src/catalog.dart   // internal code: InMemoryRoomCatalog
```

### Why that is not enough yet

| What the design says | What the code still allows |
| --- | --- |
| Reservations uses only the Notifications contract. | BookingService could create EmailNotificationSender directly; nothing would stop it. |
| Notifications never depends on Reservations. | The sender takes the whole Reservation class: the two areas depend on each other. |
| Payments is its own business area. | There is no Payments code at all. |

### Today’s plan

| Step | What you do | What you have after it |
| --- | --- | --- |
| 1 · Lock the behaviour | Run the 8 tests, the demo and the race from P04: they are the safety net | Nothing new yet |
| 2 · Module folders | Create Rooms, Reservations, Payments and Notifications, each with a place for its internal code | Four empty module folders |
| 3 · Move Rooms and Notifications | Room and the room catalog into Rooms; Notifications gets its own Confirmation message; tests after each move | Two modules with their public contracts |
| 4 · Move Reservations, add Payments | BookingService and its store into Reservations; the Payments contract with a simulated payment | All four modules |
| 5 · Wiring | Program.cs or main is the only app file that names internal classes (the tests may name them too) | Program.cs or main |
| 6 · Architecture tests | Three checks that guard the borders between the modules | The architecture test file (Python: pyproject.toml) |
| 7 · Break it on purpose | Make Reservations use EmailNotificationSender directly, watch the check fail, undo | A red check that names the bad dependency |
| 8 · Prove and decide | The same 8 behaviour tests (only the imports and the fake sender adapt), the same nine demo lines, the race still there; write ADR-001 and draw the page “Modules” | docs/adr-001-modular-monolith.md and the draw.io page “Modules” |

### How you work today

Work in pairs on one laptop, in the hotel-booking folder from P05, in the same language as in P04. Swap who types at every new step. Every code slide has a tab for each of the six languages.

C#, Java and Python download one checking tool in Step 6 (internet needed once). Go, JavaScript and Dart need nothing new: their border checks are small tests that read the import lines.

If you missed P04 or P05: download the P04 runnable code in your language from the P04 lesson page, and copy docs/contexts.md from your partner. The “Runnable code” link on this page has the finished P06 project in all six languages.

### Completed example: your folder at the end of P06

### hotel-booking folder

#### C#

```text
hotel-booking/   end of P06
├── docs/
│   ├── adr-001-modular-monolith.md   NEW
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio   NEW page: "Modules"
│   ├── quality-scenarios.md
│   └── requirements.md
├── HotelBooking/
│   ├── Notifications/
│   │   ├── Internal/
│   │   │   ├── EmailNotificationSender.cs
│   │   │   └── SmsNotificationSender.cs
│   │   ├── Confirmation.cs
│   │   └── INotificationSender.cs
│   ├── Payments/
│   │   ├── Internal/
│   │   │   └── SimulatedPaymentService.cs
│   │   ├── IPaymentService.cs
│   │   └── PaymentStatus.cs
│   ├── Reservations/
│   │   ├── Internal/
│   │   │   └── InMemoryReservationStore.cs
│   │   ├── BookingResult.cs
│   │   ├── BookingService.cs
│   │   ├── DateRange.cs
│   │   ├── Guest.cs
│   │   ├── IReservationStore.cs
│   │   ├── Reservation.cs
│   │   └── ReservationStatus.cs
│   ├── Rooms/
│   │   ├── Internal/
│   │   │   └── InMemoryRoomCatalog.cs
│   │   ├── IRoomCatalog.cs
│   │   └── Room.cs
│   ├── HotelBooking.csproj
│   └── Program.cs   wiring: the only app file that names internal code
└── HotelBooking.Tests/
    ├── ArchitectureTests.cs   NEW: the 3 border checks
    ├── BookingServiceTests.cs   the 8 tests from P04
    └── HotelBooking.Tests.csproj
```

#### Python

```text
hotel-booking/   end of P06
├── docs/
│   ├── adr-001-modular-monolith.md   NEW
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio   NEW page: "Modules"
│   ├── quality-scenarios.md
│   └── requirements.md
├── hotel_booking/
│   ├── notifications/
│   │   ├── __init__.py
│   │   └── internal.py
│   ├── payments/
│   │   ├── __init__.py
│   │   └── internal.py
│   ├── reservations/
│   │   ├── __init__.py
│   │   ├── booking.py
│   │   ├── domain.py
│   │   └── internal.py
│   ├── rooms/
│   │   ├── __init__.py
│   │   ├── catalog.py
│   │   └── internal.py
│   └── __init__.py
├── main.py   wiring: the only app file that names internal code
├── pyproject.toml   NEW: the 3 border checks
└── test_booking_service.py   the 8 tests from P04
```

#### Java

```text
hotel-booking/   end of P06
├── docs/
│   ├── adr-001-modular-monolith.md   NEW
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio   NEW page: "Modules"
│   ├── quality-scenarios.md
│   └── requirements.md
├── src/
│   ├── main/
│   │   └── java/
│   │       └── hotelbooking/
│   │           ├── notifications/
│   │           │   ├── internal/
│   │           │   │   ├── EmailNotificationSender.java
│   │           │   │   └── SmsNotificationSender.java
│   │           │   ├── Confirmation.java
│   │           │   └── NotificationSender.java
│   │           ├── payments/
│   │           │   ├── internal/
│   │           │   │   └── SimulatedPaymentService.java
│   │           │   ├── PaymentService.java
│   │           │   └── PaymentStatus.java
│   │           ├── reservations/
│   │           │   ├── internal/
│   │           │   │   └── InMemoryReservationStore.java
│   │           │   ├── BookingResult.java
│   │           │   ├── BookingService.java
│   │           │   ├── DateRange.java
│   │           │   ├── Guest.java
│   │           │   ├── Reservation.java
│   │           │   ├── ReservationStatus.java
│   │           │   └── ReservationStore.java
│   │           ├── rooms/
│   │           │   ├── internal/
│   │           │   │   └── InMemoryRoomCatalog.java
│   │           │   ├── Room.java
│   │           │   └── RoomCatalog.java
│   │           └── Main.java   wiring: the only app file that names internal code
│   └── test/
│       └── java/
│           └── hotelbooking/
│               ├── ArchitectureTests.java   NEW: the 3 border checks
│               └── BookingServiceTests.java   the 8 tests from P04
└── pom.xml
```

#### Go

```text
hotel-booking/   end of P06
├── docs/
│   ├── adr-001-modular-monolith.md   NEW
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio   NEW page: "Modules"
│   ├── quality-scenarios.md
│   └── requirements.md
├── notifications/
│   ├── internal.go
│   └── notifications.go
├── payments/
│   ├── internal.go
│   └── payments.go
├── reservations/
│   ├── booking_test.go   the 8 tests from P04
│   ├── booking.go
│   ├── domain.go
│   └── internal.go
├── rooms/
│   ├── internal.go
│   └── rooms.go
├── architecture_test.go   NEW: the 3 border checks
├── go.mod
└── main.go   wiring: the only app file that names internal code
```

#### JavaScript

```text
hotel-booking/   end of P06
├── docs/
│   ├── adr-001-modular-monolith.md   NEW
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio   NEW page: "Modules"
│   ├── quality-scenarios.md
│   └── requirements.md
├── src/
│   ├── notifications/
│   │   ├── internal/
│   │   │   ├── email.js
│   │   │   └── sms.js
│   │   └── index.js
│   ├── payments/
│   │   ├── internal/
│   │   │   └── simulated.js
│   │   └── index.js
│   ├── reservations/
│   │   ├── internal/
│   │   │   └── store.js
│   │   ├── booking.js
│   │   ├── domain.js
│   │   └── index.js
│   └── rooms/
│       ├── internal/
│       │   └── catalog.js
│       └── index.js
├── test/
│   ├── architecture.test.js   NEW: the 3 border checks
│   └── booking.test.js   the 8 tests from P04
├── main.js   wiring: the only app file that names internal code
└── package.json
```

#### Dart

```text
hotel-booking/   end of P06
├── bin/
│   └── main.dart   wiring: the only app file that names internal code
├── docs/
│   ├── adr-001-modular-monolith.md   NEW
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio   NEW page: "Modules"
│   ├── quality-scenarios.md
│   └── requirements.md
├── lib/
│   ├── notifications/
│   │   ├── src/
│   │   │   ├── email.dart
│   │   │   └── sms.dart
│   │   └── notifications.dart
│   ├── payments/
│   │   ├── src/
│   │   │   └── simulated.dart
│   │   └── payments.dart
│   ├── reservations/
│   │   ├── src/
│   │   │   ├── booking.dart
│   │   │   ├── domain.dart
│   │   │   └── store.dart
│   │   └── reservations.dart
│   └── rooms/
│       ├── src/
│       │   └── catalog.dart
│       └── rooms.dart
├── test/
│   ├── architecture_test.dart   NEW: the 3 border checks
│   └── booking_service_test.dart   the 8 tests from P04
├── analysis_options.yaml
└── pubspec.yaml
```

### Your turn · open your project

1. Open your hotel-booking folder in your editor.
2. Open docs/contexts.md next to the code: today’s moves follow its four sections and its “Between contexts” section.
3. Open docs/design-notes.md and add a new heading at the bottom: # P06 design notes

**When you finish, you have:** your P05 project open, with docs/contexts.md beside the code and a “P06 design notes” heading.
### Explanation

Today is a refactoring with a safety net: the app must do exactly what it did in P04. What changes is where the code lives and what each module may use. The architecture checks make the P05 design something the computer can check.



---

## 2. Step 1 · Lock the behaviour

Before moving anything, run the three checks from P04. They must give exactly the same results at the end of today.





### Your folder before P06

### hotel-booking folder (end of P04)

#### C#

```text
hotel-booking/   end of P04 (and P05’s docs)
├── docs/
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── quality-scenarios.md
│   └── requirements.md
├── HotelBooking/
│   ├── BookingResult.cs
│   ├── BookingService.cs
│   ├── DateRange.cs
│   ├── EmailNotificationSender.cs
│   ├── Guest.cs
│   ├── HotelBooking.csproj
│   ├── InMemoryReservationStore.cs
│   ├── INotificationSender.cs
│   ├── IReservationStore.cs
│   ├── Program.cs
│   ├── Reservation.cs
│   ├── ReservationStatus.cs
│   ├── Room.cs
│   └── SmsNotificationSender.cs
└── HotelBooking.Tests/
    ├── BookingServiceTests.cs   8 tests
    └── HotelBooking.Tests.csproj
```

#### Python

```text
hotel-booking/   end of P04 (and P05’s docs)
├── docs/
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── quality-scenarios.md
│   └── requirements.md
├── hotel_booking/
│   ├── __init__.py
│   ├── booking.py
│   ├── domain.py
│   ├── notifications.py
│   └── stores.py
├── main.py
└── test_booking_service.py   8 tests
```

#### Java

```text
hotel-booking/   end of P04 (and P05’s docs)
├── docs/
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── quality-scenarios.md
│   └── requirements.md
├── src/
│   ├── main/
│   │   └── java/
│   │       └── hotelbooking/
│   │           ├── BookingResult.java
│   │           ├── BookingService.java
│   │           ├── DateRange.java
│   │           ├── EmailNotificationSender.java
│   │           ├── Guest.java
│   │           ├── InMemoryReservationStore.java
│   │           ├── Main.java
│   │           ├── NotificationSender.java
│   │           ├── Reservation.java
│   │           ├── ReservationStatus.java
│   │           ├── ReservationStore.java
│   │           ├── Room.java
│   │           └── SmsNotificationSender.java
│   └── test/
│       └── java/
│           └── hotelbooking/
│               └── BookingServiceTests.java   8 tests
└── pom.xml
```

#### Go

```text
hotel-booking/   end of P04 (and P05’s docs)
├── docs/
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── quality-scenarios.md
│   └── requirements.md
├── booking_service_test.go   8 tests
├── booking.go
├── domain.go
├── go.mod
├── main.go
├── notifications.go
└── stores.go
```

#### JavaScript

```text
hotel-booking/   end of P04 (and P05’s docs)
├── docs/
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── quality-scenarios.md
│   └── requirements.md
├── src/
│   ├── booking.js
│   ├── domain.js
│   ├── notifications.js
│   └── stores.js
├── test/
│   └── booking.test.js   8 tests
├── main.js
└── package.json
```

#### Dart

```text
hotel-booking/   end of P04 (and P05’s docs)
├── bin/
│   └── main.dart
├── docs/
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio
│   ├── quality-scenarios.md
│   └── requirements.md
├── lib/
│   ├── booking.dart
│   ├── domain.dart
│   ├── hotel_booking.dart
│   ├── notifications.dart
│   └── stores.dart
├── test/
│   └── booking_service_test.dart   8 tests
├── analysis_options.yaml
└── pubspec.yaml
```

### Run all three checks

### Commands

#### C#

```text
dotnet test HotelBooking.Tests
dotnet run --project HotelBooking
dotnet run --project HotelBooking -- --race
```

#### Python

```text
python -m unittest -v
python main.py
python main.py --race
```

#### Java

```text
mvn -q test
mvn -q compile
java -cp target/classes hotelbooking.Main
java -cp target/classes hotelbooking.Main --race
```

#### Go

```text
go test ./...
go run .
go run . --race
```

#### JavaScript

```text
node --test
node main.js
node main.js --race
```

#### Dart

```text
dart test
dart run bin/main.dart
dart run bin/main.dart --race
```

### Completed example: what they print

### Expected demo output

#### C#

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

#### Python

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

#### Java

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

#### Go

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

#### JavaScript

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

#### Dart

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

### The race still double-books

Moving code into modules does not fix bugs: the race is still there today. P09 fixes it in the database.

### Expected race output

#### C#

```text
Maya and Omar both book R7 for 12-14 Nov at the same moment...
Maya: Accepted R101
Omar: Accepted R102
Confirmed reservations for R7 on 12-14 Nov: 2
Double booking! Both requests checked before either one saved.
```

#### Python

```text
Maya and Omar both book R7 for 12-14 Nov at the same moment...
Maya: Accepted R101
Omar: Accepted R102
Confirmed reservations for R7 on 12-14 Nov: 2
Double booking! Both requests checked before either one saved.
```

#### Java

```text
Maya and Omar both book R7 for 12-14 Nov at the same moment...
Maya: Accepted R101
Omar: Accepted R102
Confirmed reservations for R7 on 12-14 Nov: 2
Double booking! Both requests checked before either one saved.
```

#### Go

```text
Maya and Omar both book R7 for 12-14 Nov at the same moment...
Maya: Accepted R101
Omar: Accepted R102
Confirmed reservations for R7 on 12-14 Nov: 2
Double booking! Both requests checked before either one saved.
```

#### JavaScript

```text
Maya and Omar both book R7 for 12-14 Nov at the same moment...
Maya: Accepted R101
Omar: Accepted R102
Confirmed reservations for R7 on 12-14 Nov: 2
Double booking! Both requests checked before either one saved.
```

#### Dart

```text
Maya and Omar both book R7 for 12-14 Nov at the same moment...
Maya: Accepted R101
Omar: Accepted R102
Confirmed reservations for R7 on 12-14 Nov: 2
Double booking! Both requests checked before either one saved.
```

### Your turn · run the safety net

**Why the same 8 tests prove nothing broke** In short: Moving code into modules is a refactoring: what the app does must not change, so the tests from P04 must still pass.

1. Each test still books R7 for Maya, refuses Omar, lets Nora arrive on the 14th and so on.
2. Only the import or using lines at the top change, plus the fake sender, which now receives a Confirmation.
3. If one test turns red, the last move changed behaviour: undo it and try again.

1. Run the tests with the first command for your language: 8 passing.
2. Run the demo and compare it with the expected output, line by line.
3. Run the race: “Double booking!”.
4. In docs/design-notes.md, under “# P06 design notes”, add ## Before and write three lines: tests, demo, race.

**When you finish, you have:** a “Before” section in design-notes.md: 8 green tests, the nine demo lines, the race still double-booking.
### Explanation

These three results are the contract for today. After every move the tests must be green again; at the end the demo and the race must print exactly the same lines.



---

## 3. Step 2 · Create the four module folders

Each P05 context becomes a module: a folder with a public contract that other modules may use and internal code that they may not.





### The four modules

| Module | Public contract (others may use) | Internal code (only the module, the wiring and the tests) |
| --- | --- | --- |
| Rooms | Room, the room catalog promise | InMemoryRoomCatalog |
| Reservations | BookingService, BookingResult, Reservation, DateRange, Guest, ReservationStatus, the store promise | InMemoryReservationStore |
| Payments | PaymentStatus, the payment service promise | SimulatedPaymentService |
| Notifications | Confirmation, the notification sender promise | EmailNotificationSender, SmsNotificationSender |

**Public contract and internal code** In short: The contract is what a module promises to the others; the internal code is how it keeps that promise, and can change without telling anyone.

1. Notifications promises one thing: “give me a Confirmation and I will tell the guest”.
2. Today it keeps the promise by counting emails. In P15 it will send a message through RabbitMQ.
3. Reservations only knows the promise, so that change will not touch Reservations at all.
4. Every module has the same shape: its contract file(s) on top, its internal code beside them.

C#:

```text
// HotelBooking/Notifications/Confirmation.cs
namespace HotelBooking.Notifications;

// Notifications' own message: it no longer needs the Reservation class.
public record Confirmation(string ReservationId, string GuestEmail, string RoomNumber, DateOnly CheckIn, DateOnly CheckOut);

// HotelBooking/Notifications/INotificationSender.cs
namespace HotelBooking.Notifications;

// Notifications' public contract: "tell the guest". How the message travels is its own business.
public interface INotificationSender
{
    void SendConfirmation(Confirmation confirmation);
}

// HotelBooking/Notifications/Internal/EmailNotificationSender.cs
namespace HotelBooking.Notifications.Internal;

// Simulated: counts the emails it would send and needs no network.
public class EmailNotificationSender : INotificationSender
{
    public int Sent { get; private set; }

    public void SendConfirmation(Confirmation confirmation) => Sent++;
}
```

Python:

```text
# hotel_booking/notifications/__init__.py
"""Notifications: telling guests what happened. Public contract: Confirmation and NotificationSender."""
from __future__ import annotations

from dataclasses import dataclass
from datetime import date
from typing import Protocol


@dataclass(frozen=True)
class Confirmation:
    """Notifications' own message: it no longer needs the Reservation class."""
    reservation_id: str
    guest_email: str
    room_number: str
    check_in: date
    check_out: date


class NotificationSender(Protocol):
    def send_confirmation(self, confirmation: Confirmation) -> None: ...

# hotel_booking/notifications/internal.py
from __future__ import annotations

from hotel_booking.notifications import Confirmation


class EmailNotificationSender:
    """Simulated: counts the emails it would send and needs no network."""

    def __init__(self) -> None:
        self.sent = 0

    def send_confirmation(self, confirmation: Confirmation) -> None:
        self.sent += 1


class SmsNotificationSender:
    """Simulated, like the email sender."""

    def __init__(self) -> None:
        self.sent = 0

    def send_confirmation(self, confirmation: Confirmation) -> None:
        self.sent += 1
```

Java:

```text
// src/main/java/hotelbooking/notifications/Confirmation.java
package hotelbooking.notifications;

import java.time.LocalDate;

// Notifications' own message: it no longer needs the Reservation class.
public record Confirmation(String reservationId, String guestEmail, String roomNumber,
                           LocalDate checkIn, LocalDate checkOut) {}

// src/main/java/hotelbooking/notifications/NotificationSender.java
package hotelbooking.notifications;

// Notifications' public contract: "tell the guest". How the message travels is its own business.
public interface NotificationSender {
    void sendConfirmation(Confirmation confirmation);
}

// src/main/java/hotelbooking/notifications/internal/EmailNotificationSender.java
package hotelbooking.notifications.internal;

import hotelbooking.notifications.Confirmation;
import hotelbooking.notifications.NotificationSender;

// Simulated: counts the emails it would send and needs no network.
public class EmailNotificationSender implements NotificationSender {
    private int sent;

    public int sent() {
        return sent;
    }

    @Override
    public void sendConfirmation(Confirmation confirmation) {
        sent++;
    }
}
```

Go:

```text
// notifications/notifications.go
package notifications

import "time"

// Confirmation is Notifications' own message: it no longer needs the Reservation type.
type Confirmation struct {
	ReservationID string
	GuestEmail    string
	RoomNumber    string
	CheckIn       time.Time
	CheckOut      time.Time
}

// NotificationSender is Notifications' public contract: "tell the guest".
// How the message travels is its own business. It reports a failure instead of hiding it.
type NotificationSender interface {
	SendConfirmation(confirmation Confirmation) error
}

// notifications/internal.go
package notifications

// EmailNotificationSender is simulated: it counts the emails it would send and needs no network.
type EmailNotificationSender struct {
	Sent int
}

func (email *EmailNotificationSender) SendConfirmation(confirmation Confirmation) error {
	email.Sent++
	return nil
}

// SmsNotificationSender is simulated, like the email sender.
type SmsNotificationSender struct {
	Sent int
}

func (sms *SmsNotificationSender) SendConfirmation(confirmation Confirmation) error {
	sms.Sent++
	return nil
}
```

JavaScript:

```text
// src/notifications/index.js
// Notifications: telling guests what happened. This file is the module's public contract.
//
// A notification sender keeps one promise (JavaScript has no interface keyword):
//
//   sendConfirmation(confirmation)   tell this guest their booking is confirmed,
//                                    or throw when the message cannot be sent

// Notifications' own message: it no longer needs the Reservation class.
export class Confirmation {
  constructor(reservationId, guestEmail, roomNumber, checkIn, checkOut) {
    Object.assign(this, { reservationId, guestEmail, roomNumber, checkIn, checkOut });
    Object.freeze(this);
  }
}

// src/notifications/internal/email.js
// Simulated: counts the messages it would send and needs no network.
export class EmailNotificationSender {
  sent = 0;

  sendConfirmation(confirmation) {
    this.sent += 1;
  }
}
```

Dart:

```text
// lib/notifications/notifications.dart
/// Notifications: telling guests what happened. This file is the module's public contract.
library;

/// Notifications' own message: it no longer needs the Reservation class.
class Confirmation {
  const Confirmation(this.reservationId, this.guestEmail, this.roomNumber, this.checkIn, this.checkOut);

  final String reservationId;
  final String guestEmail;
  final String roomNumber;
  final DateTime checkIn;
  final DateTime checkOut;
}

// Notifications' public contract: "tell the guest". How the message travels is its own business.
abstract interface class NotificationSender {
  void sendConfirmation(Confirmation confirmation);
}

// lib/notifications/src/email.dart
import '../notifications.dart';

// Simulated: counts the emails it would send and needs no network.
class EmailNotificationSender implements NotificationSender {
  int sent = 0;

  @override
  void sendConfirmation(Confirmation confirmation) => sent++;
}
```

### How your language marks internal code

| Language | A module is | Example |
| --- | --- | --- |
| C# | A namespace per module: HotelBooking.Rooms, and HotelBooking.Rooms.Internal for its internal classes | HotelBooking/Rooms/ and HotelBooking/Rooms/Internal/ |
| Python | A package per module; internal.py holds the internal classes | hotel_booking/rooms/__init__.py, catalog.py, internal.py |
| Java | A package per module, with an internal sub-package | src/main/java/hotelbooking/rooms/ and rooms/internal/ |
| Go | A package per module; internal.go holds the code only main.go may use | rooms/rooms.go and rooms/internal.go |
| JavaScript | A folder per module; index.js is the contract, internal/ the rest | src/rooms/index.js and src/rooms/internal/ |
| Dart | A folder per module; <name>.dart is the contract, src/ the rest | lib/rooms/rooms.dart and lib/rooms/src/ |

### Completed example: the module folders in your language

### Module folders

#### C#

```text

```

#### Python

```text
hotel_booking/notifications
hotel_booking/payments
hotel_booking/reservations
hotel_booking/rooms
```

#### Java

```text
src/main/java/hotelbooking/notifications
src/main/java/hotelbooking/notifications/internal
src/main/java/hotelbooking/payments
src/main/java/hotelbooking/payments/internal
src/main/java/hotelbooking/reservations
src/main/java/hotelbooking/reservations/internal
src/main/java/hotelbooking/rooms
src/main/java/hotelbooking/rooms/internal
```

#### Go

```text
notifications
payments
reservations
rooms
```

#### JavaScript

```text
src/notifications
src/notifications/internal
src/payments
src/payments/internal
src/reservations
src/reservations/internal
src/rooms
src/rooms/internal
```

#### Dart

```text
lib/notifications
lib/notifications/src
lib/payments
lib/payments/src
lib/reservations
lib/reservations/src
lib/rooms
lib/rooms/src
```

### Your turn · create the folders

1. In your editor, create the module folders exactly as your tab shows (right-click → New Folder). Leave them empty for now.
2. Python only: put an empty __init__.py in each of the four module folders.
3. Copy the table “The four modules” into docs/design-notes.md under ## Modules.
4. Run the tests once more: still 8 passing, because no code has moved yet.

**When you finish, you have:** four empty module folders in your project, and the module table in design-notes.md.
### Explanation

Every language has its own way of saying “internal”, but none of them stops another module from using internal code on its own. The folder or namespace is the signal; the architecture checks in Step 6 are what keeps the border.



---

## 4. Step 3 · Move Rooms and Notifications

Move the modules that depend on nothing first. Notifications gets its own Confirmation message on the way, so it never needs the Reservation class.





### Why this order

Rooms and Notifications need no other module. Reservations needs both. Moving them first means every move leaves code that compiles, and the arrows between modules always point one way.

**A dependency circle, and why it hurts** In short: Two modules that each use the other cannot be changed, tested or later split apart on their own.

1. Reservations calls the notification sender, so Reservations depends on Notifications.
2. If the sender also took a Reservation, Notifications would depend on Reservations.
3. Changing Reservation could now break the email code, and the email code could break booking.
4. Moving the modules in the right order (Rooms, then Notifications, then Reservations) never creates the circle.

### Completed example 3.1 · The Rooms module

Room moves into Rooms unchanged. The room catalog promise and its list-based class are new: Program will ask the catalog for the rooms instead of writing them into BookingService itself.

### 3.1 · Rooms

#### C#

```csharp
// HotelBooking/Rooms/Room.cs
namespace HotelBooking.Rooms;

public record Room(string Number, string Type);

// HotelBooking/Rooms/IRoomCatalog.cs
namespace HotelBooking.Rooms;

// Rooms' public contract: which rooms the hotel sells. From P07 they come from a database.
public interface IRoomCatalog
{
    IReadOnlyList<Room> All();
}

// HotelBooking/Rooms/Internal/InMemoryRoomCatalog.cs
namespace HotelBooking.Rooms.Internal;

// Keeps the hotel's rooms in a list. Only Program.cs creates it.
public class InMemoryRoomCatalog(IReadOnlyList<Room> rooms) : IRoomCatalog
{
    public IReadOnlyList<Room> All() => rooms;
}
```

#### Python

```python
# hotel_booking/rooms/__init__.py
"""Rooms: which rooms the hotel sells. Public contract: Room and RoomCatalog."""
from hotel_booking.rooms.catalog import Room, RoomCatalog

__all__ = ["Room", "RoomCatalog"]

# hotel_booking/rooms/catalog.py
from __future__ import annotations

from dataclasses import dataclass
from typing import Protocol


@dataclass(frozen=True)
class Room:
    number: str
    type: str


class RoomCatalog(Protocol):
    """Rooms' public contract: which rooms the hotel sells. From P07 they come from a database."""

    def all(self) -> list[Room]: ...

# hotel_booking/rooms/internal.py
from __future__ import annotations

from hotel_booking.rooms.catalog import Room


class InMemoryRoomCatalog:
    """Keeps the hotel's rooms in a list. Only main.py creates it."""

    def __init__(self, rooms: list[Room]) -> None:
        self._rooms = list(rooms)

    def all(self) -> list[Room]:
        return list(self._rooms)
```

#### Java

```java
// src/main/java/hotelbooking/rooms/Room.java
package hotelbooking.rooms;

public record Room(String number, String type) {}

// src/main/java/hotelbooking/rooms/RoomCatalog.java
package hotelbooking.rooms;

import java.util.List;

// Rooms' public contract: which rooms the hotel sells. From P07 they come from a database.
public interface RoomCatalog {
    List<Room> all();
}

// src/main/java/hotelbooking/rooms/internal/InMemoryRoomCatalog.java
package hotelbooking.rooms.internal;

import java.util.List;
import hotelbooking.rooms.Room;
import hotelbooking.rooms.RoomCatalog;

// Keeps the hotel's rooms in a list. Only Main creates it.
public class InMemoryRoomCatalog implements RoomCatalog {
    private final List<Room> rooms;

    public InMemoryRoomCatalog(List<Room> rooms) {
        this.rooms = List.copyOf(rooms);
    }

    @Override
    public List<Room> all() {
        return rooms;
    }
}
```

#### Go

```go
// rooms/rooms.go
package rooms

// Room is a room the hotel sells.
type Room struct {
	Number string
	Type   string
}

// RoomCatalog is Rooms' public contract: which rooms the hotel sells.
// From P07 the rooms come from a database.
type RoomCatalog interface {
	All() []Room
}

// rooms/internal.go
package rooms

// InMemoryRoomCatalog keeps the hotel's rooms in a list. Only main.go creates it.
type InMemoryRoomCatalog struct {
	rooms []Room
}

func NewInMemoryRoomCatalog(rooms ...Room) *InMemoryRoomCatalog {
	return &InMemoryRoomCatalog{rooms: rooms}
}

func (catalog *InMemoryRoomCatalog) All() []Room {
	return catalog.rooms
}
```

#### JavaScript

```javascript
// src/rooms/index.js
// Rooms: which rooms the hotel sells. This file is the module's public contract.
//
// A room catalog keeps one promise (JavaScript has no interface keyword):
//
//   all()   every room the hotel sells
//
// From P07 the rooms come from a database.
export class Room {
  constructor(number, type) {
    this.number = number;
    this.type = type;
  }

  // JavaScript compares objects by identity, so Room says when two values mean the same room.
  equals(other) {
    return this.number === other.number && this.type === other.type;
  }
}

// src/rooms/internal/catalog.js
import { Room } from '../index.js';

// Keeps the hotel's rooms in a list. Only main.js creates it.
export class InMemoryRoomCatalog {
  #rooms;

  constructor(rooms) {
    this.#rooms = rooms.map(room => new Room(room.number, room.type));
  }

  all() {
    return [...this.#rooms];
  }
}
```

#### Dart

```dart
// lib/rooms/rooms.dart
/// Rooms: which rooms the hotel sells. This file is the module's public contract.
library;

class Room {
  Room(this.number, this.type);

  final String number;
  final String type;

  // Dart compares objects by identity until a class says what equal means.
  @override
  bool operator ==(Object other) => other is Room && other.number == number && other.type == type;

  @override
  int get hashCode => Object.hash(number, type);
}

// Rooms' public contract: which rooms the hotel sells. From P07 they come from a database.
abstract interface class RoomCatalog {
  List<Room> all();
}

// lib/rooms/src/catalog.dart
import '../rooms.dart';

// Keeps the hotel's rooms in a list. Only bin/main.dart creates it.
class InMemoryRoomCatalog implements RoomCatalog {
  InMemoryRoomCatalog(List<Room> rooms) : _rooms = List.unmodifiable(rooms);

  final List<Room> _rooms;

  @override
  List<Room> all() => _rooms;
}
```

### Completed example 3.2 · The Notifications module, with its own message

The sender promise now takes a Confirmation with the five fields from your P05 “Between contexts” section. The email and SMS senders move into the internal code and take the Confirmation too.

**Why Notifications gets its own Confirmation message** In short: If Notifications used the Reservation class, Notifications would depend on Reservations and Reservations on Notifications: a circle. In P05 you wrote which five fields cross the border for ReservationConfirmed. The Confirmation type is exactly those five fields.

1. P04: SendConfirmation(Reservation reservation). Notifications needs the Reservation class.
2. Reservations also needs Notifications, to call the sender. That is a dependency circle.
3. P06: SendConfirmation(Confirmation confirmation). Notifications defines Confirmation itself.
4. Now only Reservations depends on Notifications, never the other way round. Go would not even compile the circle.

C#:

```text
// HotelBooking/Reservations/BookingService.cs
using HotelBooking.Notifications;
using HotelBooking.Rooms;

namespace HotelBooking.Reservations;

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
public class BookingService(IReadOnlyList<Room> rooms, IReservationStore store, INotificationSender notifications, int slowStepMs = 0)
{
    public IReadOnlyList<Room> FindFreeRooms(DateRange stay) =>
        rooms.Where(room => !store.HasOverlap(room, stay)).ToList();

    public BookingResult Reserve(Guest guest, Room room, DateRange stay)
    {
        if (!stay.IsValid()) return new("InvalidDates");
        if (store.HasOverlap(room, stay)) return new("Unavailable");

        Thread.Sleep(slowStepMs); // 0 normally; the race demo makes this step slow

        var reservation = new Reservation($"R{101 + store.Count}", guest, room, stay);
        store.Save(reservation);
        try
        {
            // Notifications gets its own message: the five fields it needs, not the Reservation.
            notifications.SendConfirmation(new Confirmation(reservation.Id, guest.Email, room.Number, stay.CheckIn, stay.CheckOut));
        }
        catch (Exception)
        {
            // R6: the booking is already saved, so a failed message must not undo or hide it.
            return new("Accepted", reservation.Id, NotificationFailed: true);
        }
        return new("Accepted", reservation.Id);
    }

    public bool Cancel(string reservationId) => store.Find(reservationId)?.Cancel() ?? false;
}
```

Python:

```text
# hotel_booking/reservations/booking.py
from __future__ import annotations

import time
from dataclasses import dataclass
from typing import Protocol

from hotel_booking.notifications import Confirmation, NotificationSender
from hotel_booking.reservations.domain import DateRange, Guest, Reservation
from hotel_booking.rooms import Room


class ReservationStore(Protocol):
    """What BookingService needs from storage. Today a list in memory; from P07 a database."""

    def count(self) -> int: ...
    def has_overlap(self, room: Room, stay: DateRange) -> bool: ...
    def save(self, reservation: Reservation) -> None: ...
    def find(self, reservation_id: str) -> Reservation | None: ...
    def count_confirmed(self, room: Room, stay: DateRange) -> int: ...


@dataclass(frozen=True)
class BookingResult:
    outcome: str
    reservation_id: str | None = None
    notification_failed: bool = False

    def __str__(self) -> str:
        return self.outcome if self.reservation_id is None else f"{self.outcome} {self.reservation_id}"


class BookingService:
    """Owns the booking rule. Uses only the public contracts of Rooms and Notifications."""

    def __init__(self, rooms: list[Room], store: ReservationStore, notifications: NotificationSender,
                 slow_step_seconds: float = 0) -> None:
        self.rooms = rooms
        self.store = store
        self.notifications = notifications
        self.slow_step_seconds = slow_step_seconds

    def find_free_rooms(self, stay: DateRange) -> list[Room]:
        return [room for room in self.rooms if not self.store.has_overlap(room, stay)]

    def reserve(self, guest: Guest, room: Room, stay: DateRange) -> BookingResult:
        if not stay.is_valid():
            return BookingResult("InvalidDates")
        if self.store.has_overlap(room, stay):
            return BookingResult("Unavailable")

        time.sleep(self.slow_step_seconds)  # 0 normally; the race demo makes this step slow

        reservation = Reservation(f"R{101 + self.store.count()}", guest, room, stay)
        self.store.save(reservation)
        try:
            # Notifications gets its own message: the five fields it needs, not the Reservation.
            self.notifications.send_confirmation(
                Confirmation(reservation.id, guest.email, room.number, stay.check_in, stay.check_out))
        except Exception:
            # R6: the booking is already saved, so a failed message must not undo or hide it.
            return BookingResult("Accepted", reservation.id, notification_failed=True)
        return BookingResult("Accepted", reservation.id)

    def cancel(self, reservation_id: str) -> bool:
        reservation = self.store.find(reservation_id)
        return reservation is not None and reservation.cancel()
```

Java:

```text
// src/main/java/hotelbooking/reservations/BookingService.java
package hotelbooking.reservations;

import java.util.List;
import hotelbooking.notifications.Confirmation;
import hotelbooking.notifications.NotificationSender;
import hotelbooking.rooms.Room;

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
public class BookingService {
    private final List<Room> rooms;
    private final ReservationStore store;
    private final NotificationSender notifications;
    private final int slowStepMs;

    public BookingService(List<Room> rooms, ReservationStore store, NotificationSender notifications) {
        this(rooms, store, notifications, 0);
    }

    public BookingService(List<Room> rooms, ReservationStore store, NotificationSender notifications, int slowStepMs) {
        this.rooms = rooms;
        this.store = store;
        this.notifications = notifications;
        this.slowStepMs = slowStepMs;
    }

    public List<Room> findFreeRooms(DateRange stay) {
        return rooms.stream().filter(room -> !store.hasOverlap(room, stay)).toList();
    }

    public BookingResult reserve(Guest guest, Room room, DateRange stay) {
        if (!stay.isValid()) return new BookingResult("InvalidDates");
        if (store.hasOverlap(room, stay)) return new BookingResult("Unavailable");

        pause(); // does nothing normally; the race demo makes this step slow

        Reservation reservation = new Reservation("R" + (101 + store.count()), guest, room, stay);
        store.save(reservation);
        try {
            // Notifications gets its own message: the five fields it needs, not the Reservation.
            notifications.sendConfirmation(new Confirmation(reservation.id(), guest.email(), room.number(), stay.checkIn(), stay.checkOut()));
        } catch (RuntimeException failure) {
            // R6: the booking is already saved, so a failed message must not undo or hide it.
            return new BookingResult("Accepted", reservation.id(), true);
        }
        return new BookingResult("Accepted", reservation.id());
    }

    public boolean cancel(String reservationId) {
        Reservation reservation = store.find(reservationId);
        return reservation != null && reservation.cancel();
    }

    private void pause() {
        try {
            Thread.sleep(slowStepMs);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
```

Go:

```text
// reservations/booking.go
package reservations

import (
	"fmt"
	"time"

	"hotelbooking/notifications"
	"hotelbooking/rooms"
)

// ReservationStore is what BookingService needs from storage.
// Today a list in memory; from P07 a database.
type ReservationStore interface {
	Count() int
	HasOverlap(room rooms.Room, stay DateRange) bool
	Save(reservation *Reservation)
	Find(id string) *Reservation
	CountConfirmed(room rooms.Room, stay DateRange) int
}

type BookingResult struct {
	Outcome            string
	ReservationID      string
	NotificationFailed bool
}

// Printing a BookingResult uses this method, the way ToString does in C#.
func (result BookingResult) String() string {
	if result.ReservationID == "" {
		return result.Outcome
	}
	return result.Outcome + " " + result.ReservationID
}

// BookingService owns the booking rule. It uses only the public contracts of Rooms and Notifications.
type BookingService struct {
	rooms         []rooms.Room
	store         ReservationStore
	notifications notifications.NotificationSender
	slowStep      time.Duration
}

func NewBookingService(rooms []rooms.Room, store ReservationStore, notifications notifications.NotificationSender, slowStep time.Duration) *BookingService {
	return &BookingService{rooms: rooms, store: store, notifications: notifications, slowStep: slowStep}
}

func (service *BookingService) FindFreeRooms(stay DateRange) []rooms.Room {
	free := []rooms.Room{}
	for _, room := range service.rooms {
		if !service.store.HasOverlap(room, stay) {
			free = append(free, room)
		}
	}
	return free
}

func (service *BookingService) Reserve(guest Guest, room rooms.Room, stay DateRange) BookingResult {
	if !stay.IsValid() {
		return BookingResult{Outcome: "InvalidDates"}
	}
	if service.store.HasOverlap(room, stay) {
		return BookingResult{Outcome: "Unavailable"}
	}

	time.Sleep(service.slowStep) // zero normally; the race demo makes this step slow

	reservation := NewReservation(fmt.Sprintf("R%d", 101+service.store.Count()), guest, room, stay)
	service.store.Save(reservation)
	// Notifications gets its own message: the five fields it needs, not the Reservation.
	confirmation := notifications.Confirmation{
		ReservationID: reservation.ID, GuestEmail: guest.Email, RoomNumber: room.Number,
		CheckIn: stay.CheckIn, CheckOut: stay.CheckOut,
	}
	if err := service.notifications.SendConfirmation(confirmation); err != nil {
		// R6: the booking is already saved, so a failed message must not undo or hide it.
		return BookingResult{Outcome: "Accepted", ReservationID: reservation.ID, NotificationFailed: true}
	}
	return BookingResult{Outcome: "Accepted", ReservationID: reservation.ID}
}

func (service *BookingService) Cancel(reservationID string) bool {
	reservation := service.store.Find(reservationID)
	return reservation != nil && reservation.Cancel()
}
```

JavaScript:

```text
// src/reservations/booking.js
import { Confirmation } from '../notifications/index.js';
import { Reservation } from './domain.js';

// A pause that lets other requests run while this one waits.
const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

export class BookingResult {
  constructor(outcome, reservationId = null, notificationFailed = false) {
    this.outcome = outcome;
    this.reservationId = reservationId;
    this.notificationFailed = notificationFailed;
  }

  toString() {
    return this.reservationId === null ? this.outcome : `${this.outcome} ${this.reservationId}`;
  }
}

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
export class BookingService {
  constructor(rooms, store, notifications, slowStepMs = 0) {
    this.rooms = rooms;
    this.store = store;
    this.notifications = notifications;
    this.slowStepMs = slowStepMs;
  }

  findFreeRooms(stay) {
    return this.rooms.filter(room => !this.store.hasOverlap(room, stay));
  }

  async reserve(guest, room, stay) {
    if (!stay.isValid()) return new BookingResult('InvalidDates');
    if (this.store.hasOverlap(room, stay)) return new BookingResult('Unavailable');

    await wait(this.slowStepMs); // zero normally; the race demo makes this step slow

    const reservation = new Reservation(`R${101 + this.store.count}`, guest, room, stay);
    this.store.save(reservation);
    try {
      // Notifications gets its own message: the five fields it needs, not the Reservation.
      this.notifications.sendConfirmation(
        new Confirmation(reservation.id, guest.email, room.number, stay.checkIn, stay.checkOut));
    } catch {
      // R6: the booking is already saved, so a failed message must not undo or hide it.
      return new BookingResult('Accepted', reservation.id, true);
    }
    return new BookingResult('Accepted', reservation.id);
  }

  cancel(reservationId) {
    return this.store.find(reservationId)?.cancel() ?? false;
  }
}
```

Dart:

```text
// lib/reservations/src/booking.dart
import 'package:hotel_booking/notifications/notifications.dart';
import 'package:hotel_booking/rooms/rooms.dart';

import 'domain.dart';

// What BookingService needs from storage. Today a list in memory; from P07 a database.
abstract interface class ReservationStore {
  int get count;

  bool hasOverlap(Room room, DateRange stay);

  void save(Reservation reservation);

  // Returns null when no reservation has that id.
  Reservation? find(String id);

  int countConfirmed(Room room, DateRange stay);
}

class BookingResult {
  BookingResult(this.outcome, [this.reservationId, this.notificationFailed = false]);

  final String outcome;
  final String? reservationId;
  final bool notificationFailed;

  @override
  String toString() => reservationId == null ? outcome : '$outcome $reservationId';
}

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
class BookingService {
  BookingService(this.rooms, this.store, this.notifications, {this.slowStep = Duration.zero});

  final List<Room> rooms;
  final ReservationStore store;
  final NotificationSender notifications;
  final Duration slowStep;

  List<Room> findFreeRooms(DateRange stay) =>
      rooms.where((room) => !store.hasOverlap(room, stay)).toList();

  Future<BookingResult> reserve(Guest guest, Room room, DateRange stay) async {
    if (!stay.isValid()) return BookingResult('InvalidDates');
    if (store.hasOverlap(room, stay)) return BookingResult('Unavailable');

    // A pause that lets other requests run while this one waits.
    await Future<void>.delayed(slowStep);

    final reservation = Reservation('R${101 + store.count}', guest, room, stay);
    store.save(reservation);
    try {
      // Notifications gets its own message: the five fields it needs, not the Reservation.
      notifications.sendConfirmation(
          Confirmation(reservation.id, guest.email, room.number, stay.checkIn, stay.checkOut));
    } catch (failure) {
      // R6: the booking is already saved, so a failed message must not undo or hide it.
      return BookingResult('Accepted', reservation.id, true);
    }
    return BookingResult('Accepted', reservation.id);
  }

  bool cancel(String reservationId) => store.find(reservationId)?.cancel() ?? false;
}
```

### 3.2 · Notifications

#### C#

```csharp
// HotelBooking/Notifications/Confirmation.cs
namespace HotelBooking.Notifications;

// Notifications' own message: it no longer needs the Reservation class.
public record Confirmation(string ReservationId, string GuestEmail, string RoomNumber, DateOnly CheckIn, DateOnly CheckOut);

// HotelBooking/Notifications/INotificationSender.cs
namespace HotelBooking.Notifications;

// Notifications' public contract: "tell the guest". How the message travels is its own business.
public interface INotificationSender
{
    void SendConfirmation(Confirmation confirmation);
}

// HotelBooking/Notifications/Internal/EmailNotificationSender.cs
namespace HotelBooking.Notifications.Internal;

// Simulated: counts the emails it would send and needs no network.
public class EmailNotificationSender : INotificationSender
{
    public int Sent { get; private set; }

    public void SendConfirmation(Confirmation confirmation) => Sent++;
}
```

#### Python

```python
# hotel_booking/notifications/__init__.py
"""Notifications: telling guests what happened. Public contract: Confirmation and NotificationSender."""
from __future__ import annotations

from dataclasses import dataclass
from datetime import date
from typing import Protocol


@dataclass(frozen=True)
class Confirmation:
    """Notifications' own message: it no longer needs the Reservation class."""
    reservation_id: str
    guest_email: str
    room_number: str
    check_in: date
    check_out: date


class NotificationSender(Protocol):
    def send_confirmation(self, confirmation: Confirmation) -> None: ...

# hotel_booking/notifications/internal.py
from __future__ import annotations

from hotel_booking.notifications import Confirmation


class EmailNotificationSender:
    """Simulated: counts the emails it would send and needs no network."""

    def __init__(self) -> None:
        self.sent = 0

    def send_confirmation(self, confirmation: Confirmation) -> None:
        self.sent += 1


class SmsNotificationSender:
    """Simulated, like the email sender."""

    def __init__(self) -> None:
        self.sent = 0

    def send_confirmation(self, confirmation: Confirmation) -> None:
        self.sent += 1
```

#### Java

```java
// src/main/java/hotelbooking/notifications/Confirmation.java
package hotelbooking.notifications;

import java.time.LocalDate;

// Notifications' own message: it no longer needs the Reservation class.
public record Confirmation(String reservationId, String guestEmail, String roomNumber,
                           LocalDate checkIn, LocalDate checkOut) {}

// src/main/java/hotelbooking/notifications/NotificationSender.java
package hotelbooking.notifications;

// Notifications' public contract: "tell the guest". How the message travels is its own business.
public interface NotificationSender {
    void sendConfirmation(Confirmation confirmation);
}

// src/main/java/hotelbooking/notifications/internal/EmailNotificationSender.java
package hotelbooking.notifications.internal;

import hotelbooking.notifications.Confirmation;
import hotelbooking.notifications.NotificationSender;

// Simulated: counts the emails it would send and needs no network.
public class EmailNotificationSender implements NotificationSender {
    private int sent;

    public int sent() {
        return sent;
    }

    @Override
    public void sendConfirmation(Confirmation confirmation) {
        sent++;
    }
}
```

#### Go

```go
// notifications/notifications.go
package notifications

import "time"

// Confirmation is Notifications' own message: it no longer needs the Reservation type.
type Confirmation struct {
	ReservationID string
	GuestEmail    string
	RoomNumber    string
	CheckIn       time.Time
	CheckOut      time.Time
}

// NotificationSender is Notifications' public contract: "tell the guest".
// How the message travels is its own business. It reports a failure instead of hiding it.
type NotificationSender interface {
	SendConfirmation(confirmation Confirmation) error
}

// notifications/internal.go
package notifications

// EmailNotificationSender is simulated: it counts the emails it would send and needs no network.
type EmailNotificationSender struct {
	Sent int
}

func (email *EmailNotificationSender) SendConfirmation(confirmation Confirmation) error {
	email.Sent++
	return nil
}

// SmsNotificationSender is simulated, like the email sender.
type SmsNotificationSender struct {
	Sent int
}

func (sms *SmsNotificationSender) SendConfirmation(confirmation Confirmation) error {
	sms.Sent++
	return nil
}
```

#### JavaScript

```javascript
// src/notifications/index.js
// Notifications: telling guests what happened. This file is the module's public contract.
//
// A notification sender keeps one promise (JavaScript has no interface keyword):
//
//   sendConfirmation(confirmation)   tell this guest their booking is confirmed,
//                                    or throw when the message cannot be sent

// Notifications' own message: it no longer needs the Reservation class.
export class Confirmation {
  constructor(reservationId, guestEmail, roomNumber, checkIn, checkOut) {
    Object.assign(this, { reservationId, guestEmail, roomNumber, checkIn, checkOut });
    Object.freeze(this);
  }
}

// src/notifications/internal/email.js
// Simulated: counts the messages it would send and needs no network.
export class EmailNotificationSender {
  sent = 0;

  sendConfirmation(confirmation) {
    this.sent += 1;
  }
}
```

#### Dart

```dart
// lib/notifications/notifications.dart
/// Notifications: telling guests what happened. This file is the module's public contract.
library;

/// Notifications' own message: it no longer needs the Reservation class.
class Confirmation {
  const Confirmation(this.reservationId, this.guestEmail, this.roomNumber, this.checkIn, this.checkOut);

  final String reservationId;
  final String guestEmail;
  final String roomNumber;
  final DateTime checkIn;
  final DateTime checkOut;
}

// Notifications' public contract: "tell the guest". How the message travels is its own business.
abstract interface class NotificationSender {
  void sendConfirmation(Confirmation confirmation);
}

// lib/notifications/src/email.dart
import '../notifications.dart';

// Simulated: counts the emails it would send and needs no network.
class EmailNotificationSender implements NotificationSender {
  int sent = 0;

  @override
  void sendConfirmation(Confirmation confirmation) => sent++;
}
```

### Completed example 3.3 · The one change in BookingService

Reserve now builds a Confirmation instead of passing the whole reservation. Everything else in BookingService stays the same. In the test file, the fake sender records confirmation’s reservation id instead of reservation’s id.

### BookingService after Step 4 (only the Confirmation line is new today)

#### C#

```csharp
// HotelBooking/Reservations/BookingService.cs
using HotelBooking.Notifications;
using HotelBooking.Rooms;

namespace HotelBooking.Reservations;

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
public class BookingService(IReadOnlyList<Room> rooms, IReservationStore store, INotificationSender notifications, int slowStepMs = 0)
{
    public IReadOnlyList<Room> FindFreeRooms(DateRange stay) =>
        rooms.Where(room => !store.HasOverlap(room, stay)).ToList();

    public BookingResult Reserve(Guest guest, Room room, DateRange stay)
    {
        if (!stay.IsValid()) return new("InvalidDates");
        if (store.HasOverlap(room, stay)) return new("Unavailable");

        Thread.Sleep(slowStepMs); // 0 normally; the race demo makes this step slow

        var reservation = new Reservation($"R{101 + store.Count}", guest, room, stay);
        store.Save(reservation);
        try
        {
            // Notifications gets its own message: the five fields it needs, not the Reservation.
            notifications.SendConfirmation(new Confirmation(reservation.Id, guest.Email, room.Number, stay.CheckIn, stay.CheckOut));
        }
        catch (Exception)
        {
            // R6: the booking is already saved, so a failed message must not undo or hide it.
            return new("Accepted", reservation.Id, NotificationFailed: true);
        }
        return new("Accepted", reservation.Id);
    }

    public bool Cancel(string reservationId) => store.Find(reservationId)?.Cancel() ?? false;
}
```

#### Python

```python
# hotel_booking/reservations/booking.py
from __future__ import annotations

import time
from dataclasses import dataclass
from typing import Protocol

from hotel_booking.notifications import Confirmation, NotificationSender
from hotel_booking.reservations.domain import DateRange, Guest, Reservation
from hotel_booking.rooms import Room


class ReservationStore(Protocol):
    """What BookingService needs from storage. Today a list in memory; from P07 a database."""

    def count(self) -> int: ...
    def has_overlap(self, room: Room, stay: DateRange) -> bool: ...
    def save(self, reservation: Reservation) -> None: ...
    def find(self, reservation_id: str) -> Reservation | None: ...
    def count_confirmed(self, room: Room, stay: DateRange) -> int: ...


@dataclass(frozen=True)
class BookingResult:
    outcome: str
    reservation_id: str | None = None
    notification_failed: bool = False

    def __str__(self) -> str:
        return self.outcome if self.reservation_id is None else f"{self.outcome} {self.reservation_id}"


class BookingService:
    """Owns the booking rule. Uses only the public contracts of Rooms and Notifications."""

    def __init__(self, rooms: list[Room], store: ReservationStore, notifications: NotificationSender,
                 slow_step_seconds: float = 0) -> None:
        self.rooms = rooms
        self.store = store
        self.notifications = notifications
        self.slow_step_seconds = slow_step_seconds

    def find_free_rooms(self, stay: DateRange) -> list[Room]:
        return [room for room in self.rooms if not self.store.has_overlap(room, stay)]

    def reserve(self, guest: Guest, room: Room, stay: DateRange) -> BookingResult:
        if not stay.is_valid():
            return BookingResult("InvalidDates")
        if self.store.has_overlap(room, stay):
            return BookingResult("Unavailable")

        time.sleep(self.slow_step_seconds)  # 0 normally; the race demo makes this step slow

        reservation = Reservation(f"R{101 + self.store.count()}", guest, room, stay)
        self.store.save(reservation)
        try:
            # Notifications gets its own message: the five fields it needs, not the Reservation.
            self.notifications.send_confirmation(
                Confirmation(reservation.id, guest.email, room.number, stay.check_in, stay.check_out))
        except Exception:
            # R6: the booking is already saved, so a failed message must not undo or hide it.
            return BookingResult("Accepted", reservation.id, notification_failed=True)
        return BookingResult("Accepted", reservation.id)

    def cancel(self, reservation_id: str) -> bool:
        reservation = self.store.find(reservation_id)
        return reservation is not None and reservation.cancel()
```

#### Java

```java
// src/main/java/hotelbooking/reservations/BookingService.java
package hotelbooking.reservations;

import java.util.List;
import hotelbooking.notifications.Confirmation;
import hotelbooking.notifications.NotificationSender;
import hotelbooking.rooms.Room;

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
public class BookingService {
    private final List<Room> rooms;
    private final ReservationStore store;
    private final NotificationSender notifications;
    private final int slowStepMs;

    public BookingService(List<Room> rooms, ReservationStore store, NotificationSender notifications) {
        this(rooms, store, notifications, 0);
    }

    public BookingService(List<Room> rooms, ReservationStore store, NotificationSender notifications, int slowStepMs) {
        this.rooms = rooms;
        this.store = store;
        this.notifications = notifications;
        this.slowStepMs = slowStepMs;
    }

    public List<Room> findFreeRooms(DateRange stay) {
        return rooms.stream().filter(room -> !store.hasOverlap(room, stay)).toList();
    }

    public BookingResult reserve(Guest guest, Room room, DateRange stay) {
        if (!stay.isValid()) return new BookingResult("InvalidDates");
        if (store.hasOverlap(room, stay)) return new BookingResult("Unavailable");

        pause(); // does nothing normally; the race demo makes this step slow

        Reservation reservation = new Reservation("R" + (101 + store.count()), guest, room, stay);
        store.save(reservation);
        try {
            // Notifications gets its own message: the five fields it needs, not the Reservation.
            notifications.sendConfirmation(new Confirmation(reservation.id(), guest.email(), room.number(), stay.checkIn(), stay.checkOut()));
        } catch (RuntimeException failure) {
            // R6: the booking is already saved, so a failed message must not undo or hide it.
            return new BookingResult("Accepted", reservation.id(), true);
        }
        return new BookingResult("Accepted", reservation.id());
    }

    public boolean cancel(String reservationId) {
        Reservation reservation = store.find(reservationId);
        return reservation != null && reservation.cancel();
    }

    private void pause() {
        try {
            Thread.sleep(slowStepMs);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
```

#### Go

```go
// reservations/booking.go
package reservations

import (
	"fmt"
	"time"

	"hotelbooking/notifications"
	"hotelbooking/rooms"
)

// ReservationStore is what BookingService needs from storage.
// Today a list in memory; from P07 a database.
type ReservationStore interface {
	Count() int
	HasOverlap(room rooms.Room, stay DateRange) bool
	Save(reservation *Reservation)
	Find(id string) *Reservation
	CountConfirmed(room rooms.Room, stay DateRange) int
}

type BookingResult struct {
	Outcome            string
	ReservationID      string
	NotificationFailed bool
}

// Printing a BookingResult uses this method, the way ToString does in C#.
func (result BookingResult) String() string {
	if result.ReservationID == "" {
		return result.Outcome
	}
	return result.Outcome + " " + result.ReservationID
}

// BookingService owns the booking rule. It uses only the public contracts of Rooms and Notifications.
type BookingService struct {
	rooms         []rooms.Room
	store         ReservationStore
	notifications notifications.NotificationSender
	slowStep      time.Duration
}

func NewBookingService(rooms []rooms.Room, store ReservationStore, notifications notifications.NotificationSender, slowStep time.Duration) *BookingService {
	return &BookingService{rooms: rooms, store: store, notifications: notifications, slowStep: slowStep}
}

func (service *BookingService) FindFreeRooms(stay DateRange) []rooms.Room {
	free := []rooms.Room{}
	for _, room := range service.rooms {
		if !service.store.HasOverlap(room, stay) {
			free = append(free, room)
		}
	}
	return free
}

func (service *BookingService) Reserve(guest Guest, room rooms.Room, stay DateRange) BookingResult {
	if !stay.IsValid() {
		return BookingResult{Outcome: "InvalidDates"}
	}
	if service.store.HasOverlap(room, stay) {
		return BookingResult{Outcome: "Unavailable"}
	}

	time.Sleep(service.slowStep) // zero normally; the race demo makes this step slow

	reservation := NewReservation(fmt.Sprintf("R%d", 101+service.store.Count()), guest, room, stay)
	service.store.Save(reservation)
	// Notifications gets its own message: the five fields it needs, not the Reservation.
	confirmation := notifications.Confirmation{
		ReservationID: reservation.ID, GuestEmail: guest.Email, RoomNumber: room.Number,
		CheckIn: stay.CheckIn, CheckOut: stay.CheckOut,
	}
	if err := service.notifications.SendConfirmation(confirmation); err != nil {
		// R6: the booking is already saved, so a failed message must not undo or hide it.
		return BookingResult{Outcome: "Accepted", ReservationID: reservation.ID, NotificationFailed: true}
	}
	return BookingResult{Outcome: "Accepted", ReservationID: reservation.ID}
}

func (service *BookingService) Cancel(reservationID string) bool {
	reservation := service.store.Find(reservationID)
	return reservation != nil && reservation.Cancel()
}
```

#### JavaScript

```javascript
// src/reservations/booking.js
import { Confirmation } from '../notifications/index.js';
import { Reservation } from './domain.js';

// A pause that lets other requests run while this one waits.
const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

export class BookingResult {
  constructor(outcome, reservationId = null, notificationFailed = false) {
    this.outcome = outcome;
    this.reservationId = reservationId;
    this.notificationFailed = notificationFailed;
  }

  toString() {
    return this.reservationId === null ? this.outcome : `${this.outcome} ${this.reservationId}`;
  }
}

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
export class BookingService {
  constructor(rooms, store, notifications, slowStepMs = 0) {
    this.rooms = rooms;
    this.store = store;
    this.notifications = notifications;
    this.slowStepMs = slowStepMs;
  }

  findFreeRooms(stay) {
    return this.rooms.filter(room => !this.store.hasOverlap(room, stay));
  }

  async reserve(guest, room, stay) {
    if (!stay.isValid()) return new BookingResult('InvalidDates');
    if (this.store.hasOverlap(room, stay)) return new BookingResult('Unavailable');

    await wait(this.slowStepMs); // zero normally; the race demo makes this step slow

    const reservation = new Reservation(`R${101 + this.store.count}`, guest, room, stay);
    this.store.save(reservation);
    try {
      // Notifications gets its own message: the five fields it needs, not the Reservation.
      this.notifications.sendConfirmation(
        new Confirmation(reservation.id, guest.email, room.number, stay.checkIn, stay.checkOut));
    } catch {
      // R6: the booking is already saved, so a failed message must not undo or hide it.
      return new BookingResult('Accepted', reservation.id, true);
    }
    return new BookingResult('Accepted', reservation.id);
  }

  cancel(reservationId) {
    return this.store.find(reservationId)?.cancel() ?? false;
  }
}
```

#### Dart

```dart
// lib/reservations/src/booking.dart
import 'package:hotel_booking/notifications/notifications.dart';
import 'package:hotel_booking/rooms/rooms.dart';

import 'domain.dart';

// What BookingService needs from storage. Today a list in memory; from P07 a database.
abstract interface class ReservationStore {
  int get count;

  bool hasOverlap(Room room, DateRange stay);

  void save(Reservation reservation);

  // Returns null when no reservation has that id.
  Reservation? find(String id);

  int countConfirmed(Room room, DateRange stay);
}

class BookingResult {
  BookingResult(this.outcome, [this.reservationId, this.notificationFailed = false]);

  final String outcome;
  final String? reservationId;
  final bool notificationFailed;

  @override
  String toString() => reservationId == null ? outcome : '$outcome $reservationId';
}

// Owns the booking rule. Uses only the public contracts of Rooms and Notifications.
class BookingService {
  BookingService(this.rooms, this.store, this.notifications, {this.slowStep = Duration.zero});

  final List<Room> rooms;
  final ReservationStore store;
  final NotificationSender notifications;
  final Duration slowStep;

  List<Room> findFreeRooms(DateRange stay) =>
      rooms.where((room) => !store.hasOverlap(room, stay)).toList();

  Future<BookingResult> reserve(Guest guest, Room room, DateRange stay) async {
    if (!stay.isValid()) return BookingResult('InvalidDates');
    if (store.hasOverlap(room, stay)) return BookingResult('Unavailable');

    // A pause that lets other requests run while this one waits.
    await Future<void>.delayed(slowStep);

    final reservation = Reservation('R${101 + store.count}', guest, room, stay);
    store.save(reservation);
    try {
      // Notifications gets its own message: the five fields it needs, not the Reservation.
      notifications.sendConfirmation(
          Confirmation(reservation.id, guest.email, room.number, stay.checkIn, stay.checkOut));
    } catch (failure) {
      // R6: the booking is already saved, so a failed message must not undo or hide it.
      return BookingResult('Accepted', reservation.id, true);
    }
    return BookingResult('Accepted', reservation.id);
  }

  bool cancel(String reservationId) => store.find(reservationId)?.cancel() ?? false;
}
```

### Your turn · move Rooms, then Notifications

Run the tests at the end of each numbered move. If they turn red, fix that move before the next one.

**Why the same 8 tests prove nothing broke** In short: Moving code into modules is a refactoring: what the app does must not change, so the tests from P04 must still pass.

1. Each test still books R7 for Maya, refuses Omar, lets Nora arrive on the 14th and so on.
2. Only the import or using lines at the top change, plus the fake sender, which now receives a Confirmation.
3. If one test turns red, the last move changed behaviour: undo it and try again.

1. Python only, first: replace hotel_booking/__init__.py with the P06 version (only its docstring, from the runnable code). It no longer re-exports every class, so from now on main.py and the tests import from the module packages, for example from hotel_booking.rooms import Room.
2. Create the Rooms files from 3.1 in your language. Delete Room from its old file, then fix the imports or using lines your editor marks red (Reservation, the store, BookingService, Program, the tests). Run the tests: 8 passing.
3. Create the Notifications contract file(s) and the internal sender file(s) from 3.2. Delete the old notification file(s).
4. In BookingService’s Reserve, replace the line that sends the reservation with the line from 3.3 that sends a Confirmation.
5. In the test file, change the fake sender: it receives a Confirmation and records its reservation id. Fix the remaining imports.
6. Run the tests: 8 passing.

**When you finish, you have:** Rooms and Notifications as modules, Notifications with its own Confirmation message, and the same 8 tests green.
### Explanation

The Confirmation type is the P05 “Between contexts” section turned into code: the five fields Notifications needs to tell the guest, and nothing about how Reservations stores reservations.



---

## 5. Step 4 · Move Reservations and add Payments

Reservations moves last, because it uses the other modules. Payments gets its contract and a simulated implementation, ready for the booking saga in P17.





### Completed example 4.1 · Reservations: the domain types and the internal store

Reservation, DateRange, Guest, ReservationStatus, the store promise, BookingResult and BookingService move into Reservations. Only the list-based store goes into its internal code.

### 4.1 · Reservations

#### C#

```csharp
// HotelBooking/Reservations/Reservation.cs
using HotelBooking.Rooms;

namespace HotelBooking.Reservations;

public class Reservation
{
    public Reservation(string id, Guest guest, Room room, DateRange stay)
    {
        Id = id;
        Guest = guest;
        Room = room;
        Stay = stay;
    }

    public string Id { get; }
    public Guest Guest { get; }
    public Room Room { get; }
    public DateRange Stay { get; }
    public ReservationStatus Status { get; private set; } = ReservationStatus.Confirmed;

    // A reservation blocks a request when it is still Confirmed for the same room and shares a night.
    public bool Blocks(Room room, DateRange stay) =>
        Status == ReservationStatus.Confirmed && Room == room && Stay.Overlaps(stay);

    public bool Cancel()
    {
        if (Status == ReservationStatus.Cancelled) return false;
        Status = ReservationStatus.Cancelled;
        return true;
    }
}

// HotelBooking/Reservations/Internal/InMemoryReservationStore.cs
using HotelBooking.Rooms;

namespace HotelBooking.Reservations.Internal;

// Keeps reservations in a list. P07 adds a PostgreSQL store with the same interface.
public class InMemoryReservationStore : IReservationStore
{
    private readonly List<Reservation> _rows = [];

    public int Count => _rows.Count;

    public bool HasOverlap(Room room, DateRange stay) => _rows.Any(row => row.Blocks(room, stay));

    public void Save(Reservation reservation) => _rows.Add(reservation);

    public Reservation? Find(string id) => _rows.FirstOrDefault(row => row.Id == id);

    public int CountConfirmed(Room room, DateRange stay) => _rows.Count(row => row.Blocks(room, stay));
}
```

#### Python

```python
# hotel_booking/reservations/__init__.py
"""Reservations: who has which room for which nights. Owns the booking rule."""
from hotel_booking.reservations.booking import BookingResult, BookingService, ReservationStore
from hotel_booking.reservations.domain import DateRange, Guest, Reservation, ReservationStatus

__all__ = ["BookingResult", "BookingService", "DateRange", "Guest", "Reservation", "ReservationStatus", "ReservationStore"]

# hotel_booking/reservations/internal.py
from __future__ import annotations

from hotel_booking.reservations.domain import DateRange, Reservation
from hotel_booking.rooms import Room


class InMemoryReservationStore:
    """Keeps reservations in a list. P07 adds a PostgreSQL store with the same methods."""

    def __init__(self) -> None:
        self._rows: list[Reservation] = []

    def count(self) -> int:
        return len(self._rows)

    def has_overlap(self, room: Room, stay: DateRange) -> bool:
        return any(row.blocks(room, stay) for row in self._rows)

    def save(self, reservation: Reservation) -> None:
        self._rows.append(reservation)

    def find(self, reservation_id: str) -> Reservation | None:
        return next((row for row in self._rows if row.id == reservation_id), None)

    def count_confirmed(self, room: Room, stay: DateRange) -> int:
        return sum(1 for row in self._rows if row.blocks(room, stay))
```

#### Java

```java
// src/main/java/hotelbooking/reservations/Reservation.java
package hotelbooking.reservations;

import hotelbooking.rooms.Room;

public class Reservation {
    private final String id;
    private final Guest guest;
    private final Room room;
    private final DateRange stay;
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    public Reservation(String id, Guest guest, Room room, DateRange stay) {
        this.id = id;
        this.guest = guest;
        this.room = room;
        this.stay = stay;
    }

    public String id() {
        return id;
    }

    public Guest guest() {
        return guest;
    }

    public ReservationStatus status() {
        return status;
    }

    // A reservation blocks a request when it is still Confirmed for the same room and shares a night.
    public boolean blocks(Room room, DateRange stay) {
        return status == ReservationStatus.CONFIRMED && this.room.equals(room) && this.stay.overlaps(stay);
    }

    public boolean cancel() {
        if (status == ReservationStatus.CANCELLED) return false;
        status = ReservationStatus.CANCELLED;
        return true;
    }
}

// src/main/java/hotelbooking/reservations/internal/InMemoryReservationStore.java
package hotelbooking.reservations.internal;

import java.util.ArrayList;
import java.util.List;
import hotelbooking.reservations.DateRange;
import hotelbooking.reservations.Reservation;
import hotelbooking.reservations.ReservationStore;
import hotelbooking.rooms.Room;

// Keeps reservations in a list. P07 adds a PostgreSQL store with the same interface.
public class InMemoryReservationStore implements ReservationStore {
    private final List<Reservation> rows = new ArrayList<>();

    @Override
    public int count() {
        return rows.size();
    }

    @Override
    public boolean hasOverlap(Room room, DateRange stay) {
        return rows.stream().anyMatch(row -> row.blocks(room, stay));
    }

    @Override
    public void save(Reservation reservation) {
        rows.add(reservation);
    }

    @Override
    public Reservation find(String id) {
        return rows.stream().filter(row -> row.id().equals(id)).findFirst().orElse(null);
    }

    @Override
    public int countConfirmed(Room room, DateRange stay) {
        return (int) rows.stream().filter(row -> row.blocks(room, stay)).count();
    }
}
```

#### Go

```go
// reservations/domain.go
package reservations

import (
	"time"

	"hotelbooking/rooms"
)

// The stay: check-in day included, check-out day excluded (half-open).
type DateRange struct {
	CheckIn  time.Time
	CheckOut time.Time
}

func (stay DateRange) IsValid() bool {
	return stay.CheckOut.After(stay.CheckIn)
}

// Two stays overlap when they share at least one night.
func (stay DateRange) Overlaps(other DateRange) bool {
	return stay.CheckIn.Before(other.CheckOut) && other.CheckIn.Before(stay.CheckOut)
}

type Guest struct {
	Name  string
	Email string
}

// Go has no enum keyword: a named string type with two constants does the same job.
type ReservationStatus string

const (
	Confirmed ReservationStatus = "Confirmed"
	Cancelled ReservationStatus = "Cancelled"
)

type Reservation struct {
	ID     string
	Guest  Guest
	Room   rooms.Room
	Stay   DateRange
	Status ReservationStatus
}

func NewReservation(id string, guest Guest, room rooms.Room, stay DateRange) *Reservation {
	return &Reservation{ID: id, Guest: guest, Room: room, Stay: stay, Status: Confirmed}
}

// A reservation blocks a request when it is still Confirmed for the same room and shares a night.
func (reservation *Reservation) Blocks(room rooms.Room, stay DateRange) bool {
	return reservation.Status == Confirmed && reservation.Room == room && reservation.Stay.Overlaps(stay)
}

func (reservation *Reservation) Cancel() bool {
	if reservation.Status == Cancelled {
		return false
	}
	reservation.Status = Cancelled
	return true
}

// reservations/internal.go
package reservations

import "hotelbooking/rooms"

// InMemoryReservationStore keeps reservations in a list.
// P07 adds a PostgreSQL store with the same methods.
type InMemoryReservationStore struct {
	rows []*Reservation
}

func (store *InMemoryReservationStore) Count() int {
	return len(store.rows)
}

func (store *InMemoryReservationStore) HasOverlap(room rooms.Room, stay DateRange) bool {
	for _, row := range store.rows {
		if row.Blocks(room, stay) {
			return true
		}
	}
	return false
}

func (store *InMemoryReservationStore) Save(reservation *Reservation) {
	store.rows = append(store.rows, reservation)
}

// Returns nil when no reservation has that id.
func (store *InMemoryReservationStore) Find(id string) *Reservation {
	for _, row := range store.rows {
		if row.ID == id {
			return row
		}
	}
	return nil
}

func (store *InMemoryReservationStore) CountConfirmed(room rooms.Room, stay DateRange) int {
	confirmed := 0
	for _, row := range store.rows {
		if row.Blocks(room, stay) {
			confirmed++
		}
	}
	return confirmed
}
```

#### JavaScript

```javascript
// src/reservations/index.js
// Reservations: who has which room for which nights. Owns the booking rule.
// This file is the module's public contract.
export { BookingResult, BookingService } from './booking.js';
export { DateRange, Guest, Reservation, ReservationStatus } from './domain.js';

// src/reservations/internal/store.js
// What BookingService needs from storage. JavaScript has no interface keyword, so the
// contract is written down here and every store keeps to it:
//
//   count                       how many reservations are saved
//   hasOverlap(room, stay)      is a Confirmed reservation already using those nights?
//   save(reservation)           keep this reservation
//   find(id)                    the reservation with that id, or null
//   countConfirmed(room, stay)  how many Confirmed reservations use those nights
//
// Today a list in memory; from P07 a database with the same five members.
export class InMemoryReservationStore {
  #rows = [];

  get count() {
    return this.#rows.length;
  }

  hasOverlap(room, stay) {
    return this.#rows.some(row => row.blocks(room, stay));
  }

  save(reservation) {
    this.#rows.push(reservation);
  }

  // Returns null when no reservation has that id.
  find(id) {
    return this.#rows.find(row => row.id === id) ?? null;
  }

  countConfirmed(room, stay) {
    return this.#rows.filter(row => row.blocks(room, stay)).length;
  }
}
```

#### Dart

```dart
// lib/reservations/reservations.dart
/// Reservations: who has which room for which nights. Owns the booking rule.
/// This file is the module's public contract.
library;

export 'src/booking.dart';
export 'src/domain.dart';

// lib/reservations/src/store.dart
import 'package:hotel_booking/rooms/rooms.dart';

import 'booking.dart';
import 'domain.dart';

// Keeps reservations in a list. P07 adds a PostgreSQL store with the same interface.
class InMemoryReservationStore implements ReservationStore {
  final List<Reservation> _rows = [];

  @override
  int get count => _rows.length;

  @override
  bool hasOverlap(Room room, DateRange stay) => _rows.any((row) => row.blocks(room, stay));

  @override
  void save(Reservation reservation) => _rows.add(reservation);

  @override
  Reservation? find(String id) {
    for (final row in _rows) {
      if (row.id == id) return row;
    }
    return null;
  }

  @override
  int countConfirmed(Room room, DateRange stay) =>
      _rows.where((row) => row.blocks(room, stay)).length;
}
```

### Completed example 4.2 · The Payments module

One promise, TakeDeposit, answering Paid or Declined, and a simulated class that keeps it. Nothing calls it yet.

**A simulated payment service** In short: A class that keeps the payment promise without a card or a network: a positive amount is Paid, anything else is Declined.

1. Payments gets its contract today, so the module exists and its border is tested.
2. Booking does not call it yet: a deposit belongs to the booking saga in P17.
3. In P17 the saga calls TakeDeposit after holding the room, and releases the room if the answer is Declined.

C#:

```text
// HotelBooking/Payments/PaymentStatus.cs
namespace HotelBooking.Payments;

public enum PaymentStatus { Paid, Declined }

// HotelBooking/Payments/IPaymentService.cs
namespace HotelBooking.Payments;

// Payments' public contract. Booking does not call it yet: the booking saga uses it in P17.
public interface IPaymentService
{
    PaymentStatus TakeDeposit(string reservationId, decimal amount);
}

// HotelBooking/Payments/Internal/SimulatedPaymentService.cs
namespace HotelBooking.Payments.Internal;

// Simulated: no card and no network. A positive amount is Paid; anything else is Declined.
public class SimulatedPaymentService : IPaymentService
{
    public PaymentStatus TakeDeposit(string reservationId, decimal amount) =>
        amount > 0 ? PaymentStatus.Paid : PaymentStatus.Declined;
}
```

Python:

```text
# hotel_booking/payments/__init__.py
"""Payments: deposits and whether they were paid. Public contract: PaymentStatus and PaymentService."""
from __future__ import annotations

from decimal import Decimal
from enum import Enum
from typing import Protocol


class PaymentStatus(Enum):
    PAID = "Paid"
    DECLINED = "Declined"


class PaymentService(Protocol):
    """Booking does not call it yet: the booking saga uses it in P17."""

    def take_deposit(self, reservation_id: str, amount: Decimal) -> PaymentStatus: ...

# hotel_booking/payments/internal.py
from __future__ import annotations

from decimal import Decimal

from hotel_booking.payments import PaymentStatus


class SimulatedPaymentService:
    """Simulated: no card and no network. A positive amount is Paid; anything else is Declined."""

    def take_deposit(self, reservation_id: str, amount: Decimal) -> PaymentStatus:
        return PaymentStatus.PAID if amount > 0 else PaymentStatus.DECLINED
```

Java:

```text
// src/main/java/hotelbooking/payments/PaymentStatus.java
package hotelbooking.payments;

public enum PaymentStatus { PAID, DECLINED }

// src/main/java/hotelbooking/payments/PaymentService.java
package hotelbooking.payments;

import java.math.BigDecimal;

// Payments' public contract. Booking does not call it yet: the booking saga uses it in P17.
public interface PaymentService {
    PaymentStatus takeDeposit(String reservationId, BigDecimal amount);
}

// src/main/java/hotelbooking/payments/internal/SimulatedPaymentService.java
package hotelbooking.payments.internal;

import java.math.BigDecimal;
import hotelbooking.payments.PaymentService;
import hotelbooking.payments.PaymentStatus;

// Simulated: no card and no network. A positive amount is Paid; anything else is Declined.
public class SimulatedPaymentService implements PaymentService {
    @Override
    public PaymentStatus takeDeposit(String reservationId, BigDecimal amount) {
        return amount.signum() > 0 ? PaymentStatus.PAID : PaymentStatus.DECLINED;
    }
}
```

Go:

```text
// payments/payments.go
package payments

// PaymentStatus says whether a deposit was taken.
type PaymentStatus string

const (
	Paid     PaymentStatus = "Paid"
	Declined PaymentStatus = "Declined"
)

// PaymentService is Payments' public contract. Booking does not call it yet:
// the booking saga uses it in P17. The amount is in cents.
type PaymentService interface {
	TakeDeposit(reservationID string, amountCents int64) PaymentStatus
}

// payments/internal.go
package payments

// SimulatedPaymentService takes no card and needs no network.
// A positive amount is Paid; anything else is Declined.
type SimulatedPaymentService struct{}

func (SimulatedPaymentService) TakeDeposit(reservationID string, amountCents int64) PaymentStatus {
	if amountCents > 0 {
		return Paid
	}
	return Declined
}
```

JavaScript:

```text
// src/payments/index.js
// Payments: deposits and whether they were paid. This file is the module's public contract.
//
// A payment service keeps one promise (JavaScript has no interface keyword):
//
//   takeDeposit(reservationId, amount)   returns PaymentStatus.Paid or PaymentStatus.Declined
//
// Booking does not call it yet: the booking saga uses it in P17.
export const PaymentStatus = Object.freeze({ Paid: 'Paid', Declined: 'Declined' });

// src/payments/internal/simulated.js
import { PaymentStatus } from '../index.js';

// Simulated: no card and no network. A positive amount is Paid; anything else is Declined.
export class SimulatedPaymentService {
  takeDeposit(reservationId, amount) {
    return amount > 0 ? PaymentStatus.Paid : PaymentStatus.Declined;
  }
}
```

Dart:

```text
// lib/payments/payments.dart
/// Payments: deposits and whether they were paid. This file is the module's public contract.
library;

enum PaymentStatus { paid, declined }

// Booking does not call it yet: the booking saga uses it in P17. The amount is in cents.
abstract interface class PaymentService {
  PaymentStatus takeDeposit(String reservationId, int amountCents);
}

// lib/payments/src/simulated.dart
import '../payments.dart';

// Simulated: no card and no network. A positive amount is Paid; anything else is Declined.
class SimulatedPaymentService implements PaymentService {
  @override
  PaymentStatus takeDeposit(String reservationId, int amountCents) =>
      amountCents > 0 ? PaymentStatus.paid : PaymentStatus.declined;
}
```

### 4.2 · Payments

#### C#

```csharp
// HotelBooking/Payments/PaymentStatus.cs
namespace HotelBooking.Payments;

public enum PaymentStatus { Paid, Declined }

// HotelBooking/Payments/IPaymentService.cs
namespace HotelBooking.Payments;

// Payments' public contract. Booking does not call it yet: the booking saga uses it in P17.
public interface IPaymentService
{
    PaymentStatus TakeDeposit(string reservationId, decimal amount);
}

// HotelBooking/Payments/Internal/SimulatedPaymentService.cs
namespace HotelBooking.Payments.Internal;

// Simulated: no card and no network. A positive amount is Paid; anything else is Declined.
public class SimulatedPaymentService : IPaymentService
{
    public PaymentStatus TakeDeposit(string reservationId, decimal amount) =>
        amount > 0 ? PaymentStatus.Paid : PaymentStatus.Declined;
}
```

#### Python

```python
# hotel_booking/payments/__init__.py
"""Payments: deposits and whether they were paid. Public contract: PaymentStatus and PaymentService."""
from __future__ import annotations

from decimal import Decimal
from enum import Enum
from typing import Protocol


class PaymentStatus(Enum):
    PAID = "Paid"
    DECLINED = "Declined"


class PaymentService(Protocol):
    """Booking does not call it yet: the booking saga uses it in P17."""

    def take_deposit(self, reservation_id: str, amount: Decimal) -> PaymentStatus: ...

# hotel_booking/payments/internal.py
from __future__ import annotations

from decimal import Decimal

from hotel_booking.payments import PaymentStatus


class SimulatedPaymentService:
    """Simulated: no card and no network. A positive amount is Paid; anything else is Declined."""

    def take_deposit(self, reservation_id: str, amount: Decimal) -> PaymentStatus:
        return PaymentStatus.PAID if amount > 0 else PaymentStatus.DECLINED
```

#### Java

```java
// src/main/java/hotelbooking/payments/PaymentStatus.java
package hotelbooking.payments;

public enum PaymentStatus { PAID, DECLINED }

// src/main/java/hotelbooking/payments/PaymentService.java
package hotelbooking.payments;

import java.math.BigDecimal;

// Payments' public contract. Booking does not call it yet: the booking saga uses it in P17.
public interface PaymentService {
    PaymentStatus takeDeposit(String reservationId, BigDecimal amount);
}

// src/main/java/hotelbooking/payments/internal/SimulatedPaymentService.java
package hotelbooking.payments.internal;

import java.math.BigDecimal;
import hotelbooking.payments.PaymentService;
import hotelbooking.payments.PaymentStatus;

// Simulated: no card and no network. A positive amount is Paid; anything else is Declined.
public class SimulatedPaymentService implements PaymentService {
    @Override
    public PaymentStatus takeDeposit(String reservationId, BigDecimal amount) {
        return amount.signum() > 0 ? PaymentStatus.PAID : PaymentStatus.DECLINED;
    }
}
```

#### Go

```go
// payments/payments.go
package payments

// PaymentStatus says whether a deposit was taken.
type PaymentStatus string

const (
	Paid     PaymentStatus = "Paid"
	Declined PaymentStatus = "Declined"
)

// PaymentService is Payments' public contract. Booking does not call it yet:
// the booking saga uses it in P17. The amount is in cents.
type PaymentService interface {
	TakeDeposit(reservationID string, amountCents int64) PaymentStatus
}

// payments/internal.go
package payments

// SimulatedPaymentService takes no card and needs no network.
// A positive amount is Paid; anything else is Declined.
type SimulatedPaymentService struct{}

func (SimulatedPaymentService) TakeDeposit(reservationID string, amountCents int64) PaymentStatus {
	if amountCents > 0 {
		return Paid
	}
	return Declined
}
```

#### JavaScript

```javascript
// src/payments/index.js
// Payments: deposits and whether they were paid. This file is the module's public contract.
//
// A payment service keeps one promise (JavaScript has no interface keyword):
//
//   takeDeposit(reservationId, amount)   returns PaymentStatus.Paid or PaymentStatus.Declined
//
// Booking does not call it yet: the booking saga uses it in P17.
export const PaymentStatus = Object.freeze({ Paid: 'Paid', Declined: 'Declined' });

// src/payments/internal/simulated.js
import { PaymentStatus } from '../index.js';

// Simulated: no card and no network. A positive amount is Paid; anything else is Declined.
export class SimulatedPaymentService {
  takeDeposit(reservationId, amount) {
    return amount > 0 ? PaymentStatus.Paid : PaymentStatus.Declined;
  }
}
```

#### Dart

```dart
// lib/payments/payments.dart
/// Payments: deposits and whether they were paid. This file is the module's public contract.
library;

enum PaymentStatus { paid, declined }

// Booking does not call it yet: the booking saga uses it in P17. The amount is in cents.
abstract interface class PaymentService {
  PaymentStatus takeDeposit(String reservationId, int amountCents);
}

// lib/payments/src/simulated.dart
import '../payments.dart';

// Simulated: no card and no network. A positive amount is Paid; anything else is Declined.
class SimulatedPaymentService implements PaymentService {
  @override
  PaymentStatus takeDeposit(String reservationId, int amountCents) =>
      amountCents > 0 ? PaymentStatus.paid : PaymentStatus.declined;
}
```

### Where every P04 class is now

| P04 class | Module | Public or internal |
| --- | --- | --- |
| Room | Rooms | Public |
| (new) room catalog promise, InMemoryRoomCatalog | Rooms | Public, internal |
| DateRange, Guest, Reservation, ReservationStatus, BookingResult, BookingService, store promise | Reservations | Public |
| InMemoryReservationStore | Reservations | Internal |
| (new) PaymentStatus, payment service promise, SimulatedPaymentService | Payments | Public, public, internal |
| (new) Confirmation, notification sender promise | Notifications | Public |
| EmailNotificationSender, SmsNotificationSender | Notifications | Internal |

### Your turn · move Reservations, then add Payments

Run the tests at the end of each numbered move.

1. Create the Reservations files from 4.1 in your language and move the remaining classes into them. Delete the old files only when the new ones compile.
2. Fix the imports or using lines in Program (main) and in the test file. Go: the test file moves into the reservations folder, as your tab shows.
3. Run the tests: 8 passing.
4. Create the Payments files from 4.2.
5. Check that no class is left in its old place: every file now lives in one of the four module folders, except Program (main) and the tests.

**When you finish, you have:** all four modules in place, every P04 class in the module that owns it, and the same 8 tests green.
### Explanation

BookingService keeps the same constructor as in P04: a room list, a store and a sender. That is why the 8 test methods do not change: only their imports and the fake sender do.



---

## 6. Step 5 · The wiring: one place that names internal classes

Program (main) creates the real classes from every module and connects them. It is the only app file allowed to name internal code.





### Completed example: the wiring

**The wiring: the one place that names internal classes** In short: Program.cs (or main) creates the real classes and passes them in; every other file only knows the contracts. The tests may name internal classes too: they build the app for one situation, just as the wiring does.

1. The wiring creates the room catalog, the list store and the email sender.
2. It asks the catalog for the rooms and passes rooms, store and sender into BookingService.
3. BookingService sees only the room list, the store promise and the sender promise.
4. To switch to SMS or, in P07, to a PostgreSQL store, only the wiring changes.

C#:

```text
// HotelBooking/Program.cs
// P06 · Hotel booking: the P04 app as a modular monolith with four modules.
// dotnet run --project HotelBooking            -> the normal demo
// dotnet run --project HotelBooking -- --race  -> two guests book the same room at the same moment
using HotelBooking.Notifications.Internal;
using HotelBooking.Reservations;
using HotelBooking.Reservations.Internal;
using HotelBooking.Rooms;
using HotelBooking.Rooms.Internal;

var r7 = new Room("R7", "Standard");
var r8 = new Room("R8", "Deluxe");
var maya = new Guest("Maya", "maya@example.com");
var omar = new Guest("Omar", "omar@example.com");
var nora = new Guest("Nora", "nora@example.com");

// The wiring: the only app code that names the Internal classes of the modules.
var catalog = new InMemoryRoomCatalog([r7, r8]);

if (args.Contains("--race")) RunRace(); else RunDemo();

void RunDemo()
{
    var store = new InMemoryReservationStore();
    var email = new EmailNotificationSender();
    var service = new BookingService(catalog.All(), store, email);

    Console.WriteLine($"Free rooms 12-14 Nov: {Names(service.FindFreeRooms(Nov(12, 14)))}");
    Console.WriteLine($"Maya books R7 12-14 Nov: {service.Reserve(maya, r7, Nov(12, 14))}");
    Console.WriteLine($"Omar books R7 13-15 Nov: {service.Reserve(omar, r7, Nov(13, 15))}");
    Console.WriteLine($"Nora books R7 14-16 Nov: {service.Reserve(nora, r7, Nov(14, 16))}");
    Console.WriteLine($"Omar books R8 15-15 Nov: {service.Reserve(omar, r8, Nov(15, 15))}");
    Console.WriteLine($"Free rooms 12-14 Nov: {Names(service.FindFreeRooms(Nov(12, 14)))}");
    Console.WriteLine($"Maya cancels R101: {service.Cancel("R101")}");
    Console.WriteLine($"Free rooms 12-14 Nov: {Names(service.FindFreeRooms(Nov(12, 14)))}");
    Console.WriteLine($"Confirmation emails sent: {email.Sent}");
}

void RunRace()
{
    var store = new InMemoryReservationStore();
    // Each request now spends half a second between checking and saving.
    var service = new BookingService(catalog.All(), store, new EmailNotificationSender(), slowStepMs: 500);
    var stay = Nov(12, 14);
    BookingResult? mayaResult = null, omarResult = null;

    Console.WriteLine("Maya and Omar both book R7 for 12-14 Nov at the same moment...");
    var mayaRequest = new Thread(() => mayaResult = service.Reserve(maya, r7, stay));
    var omarRequest = new Thread(() => omarResult = service.Reserve(omar, r7, stay));
    mayaRequest.Start();
    Thread.Sleep(100); // Omar clicks a moment after Maya
    omarRequest.Start();
    mayaRequest.Join();
    omarRequest.Join();

    Console.WriteLine($"Maya: {mayaResult}");
    Console.WriteLine($"Omar: {omarResult}");
    var confirmed = store.CountConfirmed(r7, stay);
    Console.WriteLine($"Confirmed reservations for R7 on 12-14 Nov: {confirmed}");
    Console.WriteLine(confirmed > 1
        ? "Double booking! Both requests checked before either one saved."
        : "Only one guest has R7.");
}

DateRange Nov(int checkIn, int checkOut) =>
    new(new DateOnly(2026, 11, checkIn), new DateOnly(2026, 11, checkOut));

string Names(IEnumerable<Room> rooms) => string.Join(", ", rooms.Select(room => room.Number));
```

Python:

```text
# main.py
"""P06 - Hotel booking: the P04 app as a modular monolith with four modules.

python main.py          -> the normal demo
python main.py --race   -> two guests book the same room at the same moment
"""
from __future__ import annotations

import sys
import threading
import time
from datetime import date

from hotel_booking.notifications.internal import EmailNotificationSender
from hotel_booking.reservations import BookingResult, BookingService, DateRange, Guest
from hotel_booking.reservations.internal import InMemoryReservationStore
from hotel_booking.rooms import Room
from hotel_booking.rooms.internal import InMemoryRoomCatalog

R7 = Room("R7", "Standard")
R8 = Room("R8", "Deluxe")
MAYA = Guest("Maya", "maya@example.com")
OMAR = Guest("Omar", "omar@example.com")
NORA = Guest("Nora", "nora@example.com")

# The wiring: the only app code that imports the internal modules.
CATALOG = InMemoryRoomCatalog([R7, R8])


def nov(check_in: int, check_out: int) -> DateRange:
    return DateRange(date(2026, 11, check_in), date(2026, 11, check_out))


def names(rooms: list[Room]) -> str:
    return ", ".join(room.number for room in rooms)


def run_demo() -> None:
    store = InMemoryReservationStore()
    email = EmailNotificationSender()
    service = BookingService(CATALOG.all(), store, email)

    print(f"Free rooms 12-14 Nov: {names(service.find_free_rooms(nov(12, 14)))}")
    print(f"Maya books R7 12-14 Nov: {service.reserve(MAYA, R7, nov(12, 14))}")
    print(f"Omar books R7 13-15 Nov: {service.reserve(OMAR, R7, nov(13, 15))}")
    print(f"Nora books R7 14-16 Nov: {service.reserve(NORA, R7, nov(14, 16))}")
    print(f"Omar books R8 15-15 Nov: {service.reserve(OMAR, R8, nov(15, 15))}")
    print(f"Free rooms 12-14 Nov: {names(service.find_free_rooms(nov(12, 14)))}")
    print(f"Maya cancels R101: {service.cancel('R101')}")
    print(f"Free rooms 12-14 Nov: {names(service.find_free_rooms(nov(12, 14)))}")
    print(f"Confirmation emails sent: {email.sent}")


def run_race() -> None:
    store = InMemoryReservationStore()
    # Each request now spends half a second between checking and saving.
    service = BookingService(CATALOG.all(), store, EmailNotificationSender(), slow_step_seconds=0.5)
    stay = nov(12, 14)
    results: dict[str, BookingResult] = {}

    print("Maya and Omar both book R7 for 12-14 Nov at the same moment...")
    maya_request = threading.Thread(target=lambda: results.update(Maya=service.reserve(MAYA, R7, stay)))
    omar_request = threading.Thread(target=lambda: results.update(Omar=service.reserve(OMAR, R7, stay)))
    maya_request.start()
    time.sleep(0.1)  # Omar clicks a moment after Maya
    omar_request.start()
    maya_request.join()
    omar_request.join()

    print(f"Maya: {results['Maya']}")
    print(f"Omar: {results['Omar']}")
    confirmed = store.count_confirmed(R7, stay)
    print(f"Confirmed reservations for R7 on 12-14 Nov: {confirmed}")
    print("Double booking! Both requests checked before either one saved."
          if confirmed > 1 else "Only one guest has R7.")


if __name__ == "__main__":
    run_race() if "--race" in sys.argv else run_demo()
```

Java:

```text
// src/main/java/hotelbooking/Main.java
// P06 · Hotel booking: the P04 app as a modular monolith with four modules.
// java -cp target/classes hotelbooking.Main           -> the normal demo
// java -cp target/classes hotelbooking.Main --race    -> two guests book the same room at the same moment
package hotelbooking;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import hotelbooking.notifications.internal.EmailNotificationSender;
import hotelbooking.reservations.BookingResult;
import hotelbooking.reservations.BookingService;
import hotelbooking.reservations.DateRange;
import hotelbooking.reservations.Guest;
import hotelbooking.reservations.internal.InMemoryReservationStore;
import hotelbooking.rooms.Room;
import hotelbooking.rooms.RoomCatalog;
import hotelbooking.rooms.internal.InMemoryRoomCatalog;

public class Main {
    static final Room R7 = new Room("R7", "Standard");
    static final Room R8 = new Room("R8", "Deluxe");
    static final Guest MAYA = new Guest("Maya", "maya@example.com");
    static final Guest OMAR = new Guest("Omar", "omar@example.com");
    static final Guest NORA = new Guest("Nora", "nora@example.com");

    // The wiring: the only app class that names the internal classes of the modules.
    static final RoomCatalog CATALOG = new InMemoryRoomCatalog(List.of(R7, R8));

    public static void main(String[] args) throws InterruptedException {
        if (List.of(args).contains("--race")) runRace(); else runDemo();
    }

    static void runDemo() {
        InMemoryReservationStore store = new InMemoryReservationStore();
        EmailNotificationSender email = new EmailNotificationSender();
        BookingService service = new BookingService(CATALOG.all(), store, email);

        System.out.println("Free rooms 12-14 Nov: " + names(service.findFreeRooms(nov(12, 14))));
        System.out.println("Maya books R7 12-14 Nov: " + service.reserve(MAYA, R7, nov(12, 14)));
        System.out.println("Omar books R7 13-15 Nov: " + service.reserve(OMAR, R7, nov(13, 15)));
        System.out.println("Nora books R7 14-16 Nov: " + service.reserve(NORA, R7, nov(14, 16)));
        System.out.println("Omar books R8 15-15 Nov: " + service.reserve(OMAR, R8, nov(15, 15)));
        System.out.println("Free rooms 12-14 Nov: " + names(service.findFreeRooms(nov(12, 14))));
        System.out.println("Maya cancels R101: " + boolText(service.cancel("R101")));
        System.out.println("Free rooms 12-14 Nov: " + names(service.findFreeRooms(nov(12, 14))));
        System.out.println("Confirmation emails sent: " + email.sent());
    }

    static void runRace() throws InterruptedException {
        InMemoryReservationStore store = new InMemoryReservationStore();
        // Each request now spends half a second between checking and saving.
        BookingService service = new BookingService(CATALOG.all(), store, new EmailNotificationSender(), 500);
        DateRange stay = nov(12, 14);
        BookingResult[] results = new BookingResult[2];

        System.out.println("Maya and Omar both book R7 for 12-14 Nov at the same moment...");
        Thread mayaRequest = new Thread(() -> results[0] = service.reserve(MAYA, R7, stay));
        Thread omarRequest = new Thread(() -> results[1] = service.reserve(OMAR, R7, stay));
        mayaRequest.start();
        Thread.sleep(100); // Omar clicks a moment after Maya
        omarRequest.start();
        mayaRequest.join();
        omarRequest.join();

        System.out.println("Maya: " + results[0]);
        System.out.println("Omar: " + results[1]);
        int confirmed = store.countConfirmed(R7, stay);
        System.out.println("Confirmed reservations for R7 on 12-14 Nov: " + confirmed);
        System.out.println(confirmed > 1
            ? "Double booking! Both requests checked before either one saved."
            : "Only one guest has R7.");
    }

    static DateRange nov(int checkIn, int checkOut) {
        return new DateRange(LocalDate.of(2026, 11, checkIn), LocalDate.of(2026, 11, checkOut));
    }

    static String names(List<Room> rooms) {
        return rooms.stream().map(Room::number).collect(Collectors.joining(", "));
    }

    // Java prints booleans as true/false; the course output says True, so the demo spells it out.
    static String boolText(boolean value) {
        return value ? "True" : "False";
    }
}
```

Go:

```text
// main.go
// P06 · Hotel booking: the P04 app as a modular monolith with four modules.
// go run .          -> the normal demo
// go run . --race   -> two guests book the same room at the same moment
package main

import (
	"fmt"
	"os"
	"slices"
	"strings"
	"sync"
	"time"

	"hotelbooking/notifications"
	"hotelbooking/reservations"
	"hotelbooking/rooms"
)

var (
	r7   = rooms.Room{Number: "R7", Type: "Standard"}
	r8   = rooms.Room{Number: "R8", Type: "Deluxe"}
	maya = reservations.Guest{Name: "Maya", Email: "maya@example.com"}
	omar = reservations.Guest{Name: "Omar", Email: "omar@example.com"}
	nora = reservations.Guest{Name: "Nora", Email: "nora@example.com"}

	// The wiring: main.go is the only app code that uses the internal.go types of the modules.
	catalog = rooms.NewInMemoryRoomCatalog(r7, r8)
)

func main() {
	if slices.Contains(os.Args[1:], "--race") {
		runRace()
	} else {
		runDemo()
	}
}

func runDemo() {
	store := &reservations.InMemoryReservationStore{}
	email := &notifications.EmailNotificationSender{}
	service := reservations.NewBookingService(catalog.All(), store, email, 0)

	fmt.Printf("Free rooms 12-14 Nov: %s\n", names(service.FindFreeRooms(nov(12, 14))))
	fmt.Printf("Maya books R7 12-14 Nov: %s\n", service.Reserve(maya, r7, nov(12, 14)))
	fmt.Printf("Omar books R7 13-15 Nov: %s\n", service.Reserve(omar, r7, nov(13, 15)))
	fmt.Printf("Nora books R7 14-16 Nov: %s\n", service.Reserve(nora, r7, nov(14, 16)))
	fmt.Printf("Omar books R8 15-15 Nov: %s\n", service.Reserve(omar, r8, nov(15, 15)))
	fmt.Printf("Free rooms 12-14 Nov: %s\n", names(service.FindFreeRooms(nov(12, 14))))
	fmt.Printf("Maya cancels R101: %s\n", boolText(service.Cancel("R101")))
	fmt.Printf("Free rooms 12-14 Nov: %s\n", names(service.FindFreeRooms(nov(12, 14))))
	fmt.Printf("Confirmation emails sent: %d\n", email.Sent)
}

func runRace() {
	store := &reservations.InMemoryReservationStore{}
	// Each request now spends half a second between checking and saving.
	service := reservations.NewBookingService(catalog.All(), store, &notifications.EmailNotificationSender{}, 500*time.Millisecond)
	stay := nov(12, 14)
	var mayaResult, omarResult reservations.BookingResult
	var requests sync.WaitGroup

	fmt.Println("Maya and Omar both book R7 for 12-14 Nov at the same moment...")
	requests.Add(2)
	go func() {
		defer requests.Done()
		mayaResult = service.Reserve(maya, r7, stay)
	}()
	time.Sleep(100 * time.Millisecond) // Omar clicks a moment after Maya
	go func() {
		defer requests.Done()
		omarResult = service.Reserve(omar, r7, stay)
	}()
	requests.Wait()

	fmt.Printf("Maya: %s\n", mayaResult)
	fmt.Printf("Omar: %s\n", omarResult)
	confirmed := store.CountConfirmed(r7, stay)
	fmt.Printf("Confirmed reservations for R7 on 12-14 Nov: %d\n", confirmed)
	if confirmed > 1 {
		fmt.Println("Double booking! Both requests checked before either one saved.")
	} else {
		fmt.Println("Only one guest has R7.")
	}
}

func nov(checkIn, checkOut int) reservations.DateRange {
	return reservations.DateRange{
		CheckIn:  time.Date(2026, time.November, checkIn, 0, 0, 0, 0, time.UTC),
		CheckOut: time.Date(2026, time.November, checkOut, 0, 0, 0, 0, time.UTC),
	}
}

func names(free []rooms.Room) string {
	numbers := make([]string, len(free))
	for index, room := range free {
		numbers[index] = room.Number
	}
	return strings.Join(numbers, ", ")
}

// Go prints booleans as true/false; the course output says True, so the demo spells it out.
func boolText(value bool) string {
	if value {
		return "True"
	}
	return "False"
}
```

JavaScript:

```text
// main.js
// P06 · Hotel booking: the P04 app as a modular monolith with four modules.
// node main.js          -> the normal demo
// node main.js --race   -> two guests book the same room at the same moment
import { EmailNotificationSender } from './src/notifications/internal/email.js';
import { BookingService, DateRange, Guest } from './src/reservations/index.js';
import { InMemoryReservationStore } from './src/reservations/internal/store.js';
import { Room } from './src/rooms/index.js';
import { InMemoryRoomCatalog } from './src/rooms/internal/catalog.js';

const r7 = new Room('R7', 'Standard');
const r8 = new Room('R8', 'Deluxe');
const maya = new Guest('Maya', 'maya@example.com');
const omar = new Guest('Omar', 'omar@example.com');
const nora = new Guest('Nora', 'nora@example.com');

// The wiring: the only app code that imports the internal files of the modules.
const catalog = new InMemoryRoomCatalog([r7, r8]);

const nov = (checkIn, checkOut) =>
  new DateRange(new Date(Date.UTC(2026, 10, checkIn)), new Date(Date.UTC(2026, 10, checkOut)));

const names = rooms => rooms.map(room => room.number).join(', ');

const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

// JavaScript prints booleans as true/false; the course output says True, so the demo spells it out.
const boolText = value => (value ? 'True' : 'False');

async function runDemo() {
  const store = new InMemoryReservationStore();
  const email = new EmailNotificationSender();
  const service = new BookingService(catalog.all(), store, email);

  console.log(`Free rooms 12-14 Nov: ${names(service.findFreeRooms(nov(12, 14)))}`);
  console.log(`Maya books R7 12-14 Nov: ${await service.reserve(maya, r7, nov(12, 14))}`);
  console.log(`Omar books R7 13-15 Nov: ${await service.reserve(omar, r7, nov(13, 15))}`);
  console.log(`Nora books R7 14-16 Nov: ${await service.reserve(nora, r7, nov(14, 16))}`);
  console.log(`Omar books R8 15-15 Nov: ${await service.reserve(omar, r8, nov(15, 15))}`);
  console.log(`Free rooms 12-14 Nov: ${names(service.findFreeRooms(nov(12, 14)))}`);
  console.log(`Maya cancels R101: ${boolText(service.cancel('R101'))}`);
  console.log(`Free rooms 12-14 Nov: ${names(service.findFreeRooms(nov(12, 14)))}`);
  console.log(`Confirmation emails sent: ${email.sent}`);
}

async function runRace() {
  const store = new InMemoryReservationStore();
  // Each request now spends half a second between checking and saving.
  const service = new BookingService(catalog.all(), store, new EmailNotificationSender(), 500);
  const stay = nov(12, 14);

  console.log('Maya and Omar both book R7 for 12-14 Nov at the same moment...');
  const mayaRequest = service.reserve(maya, r7, stay);
  await wait(100); // Omar clicks a moment after Maya
  const omarRequest = service.reserve(omar, r7, stay);
  const [mayaResult, omarResult] = await Promise.all([mayaRequest, omarRequest]);

  console.log(`Maya: ${mayaResult}`);
  console.log(`Omar: ${omarResult}`);
  const confirmed = store.countConfirmed(r7, stay);
  console.log(`Confirmed reservations for R7 on 12-14 Nov: ${confirmed}`);
  console.log(confirmed > 1
    ? 'Double booking! Both requests checked before either one saved.'
    : 'Only one guest has R7.');
}

if (process.argv.includes('--race')) runRace(); else runDemo();
```

Dart:

```text
// bin/main.dart
// P06 · Hotel booking: the P04 app as a modular monolith with four modules.
// dart run bin/main.dart          -> the normal demo
// dart run bin/main.dart --race   -> two guests book the same room at the same moment
import 'package:hotel_booking/notifications/src/email.dart';
import 'package:hotel_booking/reservations/reservations.dart';
import 'package:hotel_booking/reservations/src/store.dart';
import 'package:hotel_booking/rooms/rooms.dart';
import 'package:hotel_booking/rooms/src/catalog.dart';

final r7 = Room('R7', 'Standard');
final r8 = Room('R8', 'Deluxe');
final maya = Guest('Maya', 'maya@example.com');
final omar = Guest('Omar', 'omar@example.com');
final nora = Guest('Nora', 'nora@example.com');

// The wiring: the only app code that imports the src/ files of the modules.
final catalog = InMemoryRoomCatalog([r7, r8]);

DateRange nov(int checkIn, int checkOut) =>
    DateRange(DateTime.utc(2026, 11, checkIn), DateTime.utc(2026, 11, checkOut));

String names(List<Room> rooms) => rooms.map((room) => room.number).join(', ');

// Dart prints booleans as true/false; the course output says True, so the demo spells it out.
String boolText(bool value) => value ? 'True' : 'False';

Future<void> main(List<String> args) async {
  if (args.contains('--race')) {
    await runRace();
  } else {
    await runDemo();
  }
}

Future<void> runDemo() async {
  final store = InMemoryReservationStore();
  final email = EmailNotificationSender();
  final service = BookingService(catalog.all(), store, email);

  print('Free rooms 12-14 Nov: ${names(service.findFreeRooms(nov(12, 14)))}');
  print('Maya books R7 12-14 Nov: ${await service.reserve(maya, r7, nov(12, 14))}');
  print('Omar books R7 13-15 Nov: ${await service.reserve(omar, r7, nov(13, 15))}');
  print('Nora books R7 14-16 Nov: ${await service.reserve(nora, r7, nov(14, 16))}');
  print('Omar books R8 15-15 Nov: ${await service.reserve(omar, r8, nov(15, 15))}');
  print('Free rooms 12-14 Nov: ${names(service.findFreeRooms(nov(12, 14)))}');
  print('Maya cancels R101: ${boolText(service.cancel('R101'))}');
  print('Free rooms 12-14 Nov: ${names(service.findFreeRooms(nov(12, 14)))}');
  print('Confirmation emails sent: ${email.sent}');
}

Future<void> runRace() async {
  final store = InMemoryReservationStore();
  // Each request now spends half a second between checking and saving.
  final service = BookingService(
    catalog.all(),
    store,
    EmailNotificationSender(),
    slowStep: const Duration(milliseconds: 500),
  );
  final stay = nov(12, 14);

  print('Maya and Omar both book R7 for 12-14 Nov at the same moment...');
  final mayaRequest = service.reserve(maya, r7, stay);
  // Omar clicks a moment after Maya
  await Future<void>.delayed(const Duration(milliseconds: 100));
  final omarRequest = service.reserve(omar, r7, stay);
  final results = await Future.wait([mayaRequest, omarRequest]);

  print('Maya: ${results[0]}');
  print('Omar: ${results[1]}');
  final confirmed = store.countConfirmed(r7, stay);
  print('Confirmed reservations for R7 on 12-14 Nov: $confirmed');
  print(
    confirmed > 1
        ? 'Double booking! Both requests checked before either one saved.'
        : 'Only one guest has R7.',
  );
}
```

### Program / main

#### C#

```csharp
// HotelBooking/Program.cs
// P06 · Hotel booking: the P04 app as a modular monolith with four modules.
// dotnet run --project HotelBooking            -> the normal demo
// dotnet run --project HotelBooking -- --race  -> two guests book the same room at the same moment
using HotelBooking.Notifications.Internal;
using HotelBooking.Reservations;
using HotelBooking.Reservations.Internal;
using HotelBooking.Rooms;
using HotelBooking.Rooms.Internal;

var r7 = new Room("R7", "Standard");
var r8 = new Room("R8", "Deluxe");
var maya = new Guest("Maya", "maya@example.com");
var omar = new Guest("Omar", "omar@example.com");
var nora = new Guest("Nora", "nora@example.com");

// The wiring: the only app code that names the Internal classes of the modules.
var catalog = new InMemoryRoomCatalog([r7, r8]);

if (args.Contains("--race")) RunRace(); else RunDemo();

void RunDemo()
{
    var store = new InMemoryReservationStore();
    var email = new EmailNotificationSender();
    var service = new BookingService(catalog.All(), store, email);

    Console.WriteLine($"Free rooms 12-14 Nov: {Names(service.FindFreeRooms(Nov(12, 14)))}");
    Console.WriteLine($"Maya books R7 12-14 Nov: {service.Reserve(maya, r7, Nov(12, 14))}");
    Console.WriteLine($"Omar books R7 13-15 Nov: {service.Reserve(omar, r7, Nov(13, 15))}");
    Console.WriteLine($"Nora books R7 14-16 Nov: {service.Reserve(nora, r7, Nov(14, 16))}");
    Console.WriteLine($"Omar books R8 15-15 Nov: {service.Reserve(omar, r8, Nov(15, 15))}");
    Console.WriteLine($"Free rooms 12-14 Nov: {Names(service.FindFreeRooms(Nov(12, 14)))}");
    Console.WriteLine($"Maya cancels R101: {service.Cancel("R101")}");
    Console.WriteLine($"Free rooms 12-14 Nov: {Names(service.FindFreeRooms(Nov(12, 14)))}");
    Console.WriteLine($"Confirmation emails sent: {email.Sent}");
}

void RunRace()
{
    var store = new InMemoryReservationStore();
    // Each request now spends half a second between checking and saving.
    var service = new BookingService(catalog.All(), store, new EmailNotificationSender(), slowStepMs: 500);
    var stay = Nov(12, 14);
    BookingResult? mayaResult = null, omarResult = null;

    Console.WriteLine("Maya and Omar both book R7 for 12-14 Nov at the same moment...");
    var mayaRequest = new Thread(() => mayaResult = service.Reserve(maya, r7, stay));
    var omarRequest = new Thread(() => omarResult = service.Reserve(omar, r7, stay));
    mayaRequest.Start();
    Thread.Sleep(100); // Omar clicks a moment after Maya
    omarRequest.Start();
    mayaRequest.Join();
    omarRequest.Join();

    Console.WriteLine($"Maya: {mayaResult}");
    Console.WriteLine($"Omar: {omarResult}");
    var confirmed = store.CountConfirmed(r7, stay);
    Console.WriteLine($"Confirmed reservations for R7 on 12-14 Nov: {confirmed}");
    Console.WriteLine(confirmed > 1
        ? "Double booking! Both requests checked before either one saved."
        : "Only one guest has R7.");
}

DateRange Nov(int checkIn, int checkOut) =>
    new(new DateOnly(2026, 11, checkIn), new DateOnly(2026, 11, checkOut));

string Names(IEnumerable<Room> rooms) => string.Join(", ", rooms.Select(room => room.Number));
```

#### Python

```python
# main.py
"""P06 - Hotel booking: the P04 app as a modular monolith with four modules.

python main.py          -> the normal demo
python main.py --race   -> two guests book the same room at the same moment
"""
from __future__ import annotations

import sys
import threading
import time
from datetime import date

from hotel_booking.notifications.internal import EmailNotificationSender
from hotel_booking.reservations import BookingResult, BookingService, DateRange, Guest
from hotel_booking.reservations.internal import InMemoryReservationStore
from hotel_booking.rooms import Room
from hotel_booking.rooms.internal import InMemoryRoomCatalog

R7 = Room("R7", "Standard")
R8 = Room("R8", "Deluxe")
MAYA = Guest("Maya", "maya@example.com")
OMAR = Guest("Omar", "omar@example.com")
NORA = Guest("Nora", "nora@example.com")

# The wiring: the only app code that imports the internal modules.
CATALOG = InMemoryRoomCatalog([R7, R8])


def nov(check_in: int, check_out: int) -> DateRange:
    return DateRange(date(2026, 11, check_in), date(2026, 11, check_out))


def names(rooms: list[Room]) -> str:
    return ", ".join(room.number for room in rooms)


def run_demo() -> None:
    store = InMemoryReservationStore()
    email = EmailNotificationSender()
    service = BookingService(CATALOG.all(), store, email)

    print(f"Free rooms 12-14 Nov: {names(service.find_free_rooms(nov(12, 14)))}")
    print(f"Maya books R7 12-14 Nov: {service.reserve(MAYA, R7, nov(12, 14))}")
    print(f"Omar books R7 13-15 Nov: {service.reserve(OMAR, R7, nov(13, 15))}")
    print(f"Nora books R7 14-16 Nov: {service.reserve(NORA, R7, nov(14, 16))}")
    print(f"Omar books R8 15-15 Nov: {service.reserve(OMAR, R8, nov(15, 15))}")
    print(f"Free rooms 12-14 Nov: {names(service.find_free_rooms(nov(12, 14)))}")
    print(f"Maya cancels R101: {service.cancel('R101')}")
    print(f"Free rooms 12-14 Nov: {names(service.find_free_rooms(nov(12, 14)))}")
    print(f"Confirmation emails sent: {email.sent}")


def run_race() -> None:
    store = InMemoryReservationStore()
    # Each request now spends half a second between checking and saving.
    service = BookingService(CATALOG.all(), store, EmailNotificationSender(), slow_step_seconds=0.5)
    stay = nov(12, 14)
    results: dict[str, BookingResult] = {}

    print("Maya and Omar both book R7 for 12-14 Nov at the same moment...")
    maya_request = threading.Thread(target=lambda: results.update(Maya=service.reserve(MAYA, R7, stay)))
    omar_request = threading.Thread(target=lambda: results.update(Omar=service.reserve(OMAR, R7, stay)))
    maya_request.start()
    time.sleep(0.1)  # Omar clicks a moment after Maya
    omar_request.start()
    maya_request.join()
    omar_request.join()

    print(f"Maya: {results['Maya']}")
    print(f"Omar: {results['Omar']}")
    confirmed = store.count_confirmed(R7, stay)
    print(f"Confirmed reservations for R7 on 12-14 Nov: {confirmed}")
    print("Double booking! Both requests checked before either one saved."
          if confirmed > 1 else "Only one guest has R7.")


if __name__ == "__main__":
    run_race() if "--race" in sys.argv else run_demo()
```

#### Java

```java
// src/main/java/hotelbooking/Main.java
// P06 · Hotel booking: the P04 app as a modular monolith with four modules.
// java -cp target/classes hotelbooking.Main           -> the normal demo
// java -cp target/classes hotelbooking.Main --race    -> two guests book the same room at the same moment
package hotelbooking;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import hotelbooking.notifications.internal.EmailNotificationSender;
import hotelbooking.reservations.BookingResult;
import hotelbooking.reservations.BookingService;
import hotelbooking.reservations.DateRange;
import hotelbooking.reservations.Guest;
import hotelbooking.reservations.internal.InMemoryReservationStore;
import hotelbooking.rooms.Room;
import hotelbooking.rooms.RoomCatalog;
import hotelbooking.rooms.internal.InMemoryRoomCatalog;

public class Main {
    static final Room R7 = new Room("R7", "Standard");
    static final Room R8 = new Room("R8", "Deluxe");
    static final Guest MAYA = new Guest("Maya", "maya@example.com");
    static final Guest OMAR = new Guest("Omar", "omar@example.com");
    static final Guest NORA = new Guest("Nora", "nora@example.com");

    // The wiring: the only app class that names the internal classes of the modules.
    static final RoomCatalog CATALOG = new InMemoryRoomCatalog(List.of(R7, R8));

    public static void main(String[] args) throws InterruptedException {
        if (List.of(args).contains("--race")) runRace(); else runDemo();
    }

    static void runDemo() {
        InMemoryReservationStore store = new InMemoryReservationStore();
        EmailNotificationSender email = new EmailNotificationSender();
        BookingService service = new BookingService(CATALOG.all(), store, email);

        System.out.println("Free rooms 12-14 Nov: " + names(service.findFreeRooms(nov(12, 14))));
        System.out.println("Maya books R7 12-14 Nov: " + service.reserve(MAYA, R7, nov(12, 14)));
        System.out.println("Omar books R7 13-15 Nov: " + service.reserve(OMAR, R7, nov(13, 15)));
        System.out.println("Nora books R7 14-16 Nov: " + service.reserve(NORA, R7, nov(14, 16)));
        System.out.println("Omar books R8 15-15 Nov: " + service.reserve(OMAR, R8, nov(15, 15)));
        System.out.println("Free rooms 12-14 Nov: " + names(service.findFreeRooms(nov(12, 14))));
        System.out.println("Maya cancels R101: " + boolText(service.cancel("R101")));
        System.out.println("Free rooms 12-14 Nov: " + names(service.findFreeRooms(nov(12, 14))));
        System.out.println("Confirmation emails sent: " + email.sent());
    }

    static void runRace() throws InterruptedException {
        InMemoryReservationStore store = new InMemoryReservationStore();
        // Each request now spends half a second between checking and saving.
        BookingService service = new BookingService(CATALOG.all(), store, new EmailNotificationSender(), 500);
        DateRange stay = nov(12, 14);
        BookingResult[] results = new BookingResult[2];

        System.out.println("Maya and Omar both book R7 for 12-14 Nov at the same moment...");
        Thread mayaRequest = new Thread(() -> results[0] = service.reserve(MAYA, R7, stay));
        Thread omarRequest = new Thread(() -> results[1] = service.reserve(OMAR, R7, stay));
        mayaRequest.start();
        Thread.sleep(100); // Omar clicks a moment after Maya
        omarRequest.start();
        mayaRequest.join();
        omarRequest.join();

        System.out.println("Maya: " + results[0]);
        System.out.println("Omar: " + results[1]);
        int confirmed = store.countConfirmed(R7, stay);
        System.out.println("Confirmed reservations for R7 on 12-14 Nov: " + confirmed);
        System.out.println(confirmed > 1
            ? "Double booking! Both requests checked before either one saved."
            : "Only one guest has R7.");
    }

    static DateRange nov(int checkIn, int checkOut) {
        return new DateRange(LocalDate.of(2026, 11, checkIn), LocalDate.of(2026, 11, checkOut));
    }

    static String names(List<Room> rooms) {
        return rooms.stream().map(Room::number).collect(Collectors.joining(", "));
    }

    // Java prints booleans as true/false; the course output says True, so the demo spells it out.
    static String boolText(boolean value) {
        return value ? "True" : "False";
    }
}
```

#### Go

```go
// main.go
// P06 · Hotel booking: the P04 app as a modular monolith with four modules.
// go run .          -> the normal demo
// go run . --race   -> two guests book the same room at the same moment
package main

import (
	"fmt"
	"os"
	"slices"
	"strings"
	"sync"
	"time"

	"hotelbooking/notifications"
	"hotelbooking/reservations"
	"hotelbooking/rooms"
)

var (
	r7   = rooms.Room{Number: "R7", Type: "Standard"}
	r8   = rooms.Room{Number: "R8", Type: "Deluxe"}
	maya = reservations.Guest{Name: "Maya", Email: "maya@example.com"}
	omar = reservations.Guest{Name: "Omar", Email: "omar@example.com"}
	nora = reservations.Guest{Name: "Nora", Email: "nora@example.com"}

	// The wiring: main.go is the only app code that uses the internal.go types of the modules.
	catalog = rooms.NewInMemoryRoomCatalog(r7, r8)
)

func main() {
	if slices.Contains(os.Args[1:], "--race") {
		runRace()
	} else {
		runDemo()
	}
}

func runDemo() {
	store := &reservations.InMemoryReservationStore{}
	email := &notifications.EmailNotificationSender{}
	service := reservations.NewBookingService(catalog.All(), store, email, 0)

	fmt.Printf("Free rooms 12-14 Nov: %s\n", names(service.FindFreeRooms(nov(12, 14))))
	fmt.Printf("Maya books R7 12-14 Nov: %s\n", service.Reserve(maya, r7, nov(12, 14)))
	fmt.Printf("Omar books R7 13-15 Nov: %s\n", service.Reserve(omar, r7, nov(13, 15)))
	fmt.Printf("Nora books R7 14-16 Nov: %s\n", service.Reserve(nora, r7, nov(14, 16)))
	fmt.Printf("Omar books R8 15-15 Nov: %s\n", service.Reserve(omar, r8, nov(15, 15)))
	fmt.Printf("Free rooms 12-14 Nov: %s\n", names(service.FindFreeRooms(nov(12, 14))))
	fmt.Printf("Maya cancels R101: %s\n", boolText(service.Cancel("R101")))
	fmt.Printf("Free rooms 12-14 Nov: %s\n", names(service.FindFreeRooms(nov(12, 14))))
	fmt.Printf("Confirmation emails sent: %d\n", email.Sent)
}

func runRace() {
	store := &reservations.InMemoryReservationStore{}
	// Each request now spends half a second between checking and saving.
	service := reservations.NewBookingService(catalog.All(), store, &notifications.EmailNotificationSender{}, 500*time.Millisecond)
	stay := nov(12, 14)
	var mayaResult, omarResult reservations.BookingResult
	var requests sync.WaitGroup

	fmt.Println("Maya and Omar both book R7 for 12-14 Nov at the same moment...")
	requests.Add(2)
	go func() {
		defer requests.Done()
		mayaResult = service.Reserve(maya, r7, stay)
	}()
	time.Sleep(100 * time.Millisecond) // Omar clicks a moment after Maya
	go func() {
		defer requests.Done()
		omarResult = service.Reserve(omar, r7, stay)
	}()
	requests.Wait()

	fmt.Printf("Maya: %s\n", mayaResult)
	fmt.Printf("Omar: %s\n", omarResult)
	confirmed := store.CountConfirmed(r7, stay)
	fmt.Printf("Confirmed reservations for R7 on 12-14 Nov: %d\n", confirmed)
	if confirmed > 1 {
		fmt.Println("Double booking! Both requests checked before either one saved.")
	} else {
		fmt.Println("Only one guest has R7.")
	}
}

func nov(checkIn, checkOut int) reservations.DateRange {
	return reservations.DateRange{
		CheckIn:  time.Date(2026, time.November, checkIn, 0, 0, 0, 0, time.UTC),
		CheckOut: time.Date(2026, time.November, checkOut, 0, 0, 0, 0, time.UTC),
	}
}

func names(free []rooms.Room) string {
	numbers := make([]string, len(free))
	for index, room := range free {
		numbers[index] = room.Number
	}
	return strings.Join(numbers, ", ")
}

// Go prints booleans as true/false; the course output says True, so the demo spells it out.
func boolText(value bool) string {
	if value {
		return "True"
	}
	return "False"
}
```

#### JavaScript

```javascript
// main.js
// P06 · Hotel booking: the P04 app as a modular monolith with four modules.
// node main.js          -> the normal demo
// node main.js --race   -> two guests book the same room at the same moment
import { EmailNotificationSender } from './src/notifications/internal/email.js';
import { BookingService, DateRange, Guest } from './src/reservations/index.js';
import { InMemoryReservationStore } from './src/reservations/internal/store.js';
import { Room } from './src/rooms/index.js';
import { InMemoryRoomCatalog } from './src/rooms/internal/catalog.js';

const r7 = new Room('R7', 'Standard');
const r8 = new Room('R8', 'Deluxe');
const maya = new Guest('Maya', 'maya@example.com');
const omar = new Guest('Omar', 'omar@example.com');
const nora = new Guest('Nora', 'nora@example.com');

// The wiring: the only app code that imports the internal files of the modules.
const catalog = new InMemoryRoomCatalog([r7, r8]);

const nov = (checkIn, checkOut) =>
  new DateRange(new Date(Date.UTC(2026, 10, checkIn)), new Date(Date.UTC(2026, 10, checkOut)));

const names = rooms => rooms.map(room => room.number).join(', ');

const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

// JavaScript prints booleans as true/false; the course output says True, so the demo spells it out.
const boolText = value => (value ? 'True' : 'False');

async function runDemo() {
  const store = new InMemoryReservationStore();
  const email = new EmailNotificationSender();
  const service = new BookingService(catalog.all(), store, email);

  console.log(`Free rooms 12-14 Nov: ${names(service.findFreeRooms(nov(12, 14)))}`);
  console.log(`Maya books R7 12-14 Nov: ${await service.reserve(maya, r7, nov(12, 14))}`);
  console.log(`Omar books R7 13-15 Nov: ${await service.reserve(omar, r7, nov(13, 15))}`);
  console.log(`Nora books R7 14-16 Nov: ${await service.reserve(nora, r7, nov(14, 16))}`);
  console.log(`Omar books R8 15-15 Nov: ${await service.reserve(omar, r8, nov(15, 15))}`);
  console.log(`Free rooms 12-14 Nov: ${names(service.findFreeRooms(nov(12, 14)))}`);
  console.log(`Maya cancels R101: ${boolText(service.cancel('R101'))}`);
  console.log(`Free rooms 12-14 Nov: ${names(service.findFreeRooms(nov(12, 14)))}`);
  console.log(`Confirmation emails sent: ${email.sent}`);
}

async function runRace() {
  const store = new InMemoryReservationStore();
  // Each request now spends half a second between checking and saving.
  const service = new BookingService(catalog.all(), store, new EmailNotificationSender(), 500);
  const stay = nov(12, 14);

  console.log('Maya and Omar both book R7 for 12-14 Nov at the same moment...');
  const mayaRequest = service.reserve(maya, r7, stay);
  await wait(100); // Omar clicks a moment after Maya
  const omarRequest = service.reserve(omar, r7, stay);
  const [mayaResult, omarResult] = await Promise.all([mayaRequest, omarRequest]);

  console.log(`Maya: ${mayaResult}`);
  console.log(`Omar: ${omarResult}`);
  const confirmed = store.countConfirmed(r7, stay);
  console.log(`Confirmed reservations for R7 on 12-14 Nov: ${confirmed}`);
  console.log(confirmed > 1
    ? 'Double booking! Both requests checked before either one saved.'
    : 'Only one guest has R7.');
}

if (process.argv.includes('--race')) runRace(); else runDemo();
```

#### Dart

```dart
// bin/main.dart
// P06 · Hotel booking: the P04 app as a modular monolith with four modules.
// dart run bin/main.dart          -> the normal demo
// dart run bin/main.dart --race   -> two guests book the same room at the same moment
import 'package:hotel_booking/notifications/src/email.dart';
import 'package:hotel_booking/reservations/reservations.dart';
import 'package:hotel_booking/reservations/src/store.dart';
import 'package:hotel_booking/rooms/rooms.dart';
import 'package:hotel_booking/rooms/src/catalog.dart';

final r7 = Room('R7', 'Standard');
final r8 = Room('R8', 'Deluxe');
final maya = Guest('Maya', 'maya@example.com');
final omar = Guest('Omar', 'omar@example.com');
final nora = Guest('Nora', 'nora@example.com');

// The wiring: the only app code that imports the src/ files of the modules.
final catalog = InMemoryRoomCatalog([r7, r8]);

DateRange nov(int checkIn, int checkOut) =>
    DateRange(DateTime.utc(2026, 11, checkIn), DateTime.utc(2026, 11, checkOut));

String names(List<Room> rooms) => rooms.map((room) => room.number).join(', ');

// Dart prints booleans as true/false; the course output says True, so the demo spells it out.
String boolText(bool value) => value ? 'True' : 'False';

Future<void> main(List<String> args) async {
  if (args.contains('--race')) {
    await runRace();
  } else {
    await runDemo();
  }
}

Future<void> runDemo() async {
  final store = InMemoryReservationStore();
  final email = EmailNotificationSender();
  final service = BookingService(catalog.all(), store, email);

  print('Free rooms 12-14 Nov: ${names(service.findFreeRooms(nov(12, 14)))}');
  print('Maya books R7 12-14 Nov: ${await service.reserve(maya, r7, nov(12, 14))}');
  print('Omar books R7 13-15 Nov: ${await service.reserve(omar, r7, nov(13, 15))}');
  print('Nora books R7 14-16 Nov: ${await service.reserve(nora, r7, nov(14, 16))}');
  print('Omar books R8 15-15 Nov: ${await service.reserve(omar, r8, nov(15, 15))}');
  print('Free rooms 12-14 Nov: ${names(service.findFreeRooms(nov(12, 14)))}');
  print('Maya cancels R101: ${boolText(service.cancel('R101'))}');
  print('Free rooms 12-14 Nov: ${names(service.findFreeRooms(nov(12, 14)))}');
  print('Confirmation emails sent: ${email.sent}');
}

Future<void> runRace() async {
  final store = InMemoryReservationStore();
  // Each request now spends half a second between checking and saving.
  final service = BookingService(
    catalog.all(),
    store,
    EmailNotificationSender(),
    slowStep: const Duration(milliseconds: 500),
  );
  final stay = nov(12, 14);

  print('Maya and Omar both book R7 for 12-14 Nov at the same moment...');
  final mayaRequest = service.reserve(maya, r7, stay);
  // Omar clicks a moment after Maya
  await Future<void>.delayed(const Duration(milliseconds: 100));
  final omarRequest = service.reserve(omar, r7, stay);
  final results = await Future.wait([mayaRequest, omarRequest]);

  print('Maya: ${results[0]}');
  print('Omar: ${results[1]}');
  final confirmed = store.countConfirmed(r7, stay);
  print('Confirmed reservations for R7 on 12-14 Nov: $confirmed');
  print(
    confirmed > 1
        ? 'Double booking! Both requests checked before either one saved.'
        : 'Only one guest has R7.',
  );
}
```

### Who may name internal code

| File | May it name internal classes? | Why |
| --- | --- | --- |
| Program.cs / main | Yes | It chooses the real store, sender and catalog. |
| The tests | Yes | They build the app for one situation, like the wiring does. |
| Any module’s own code | Only its own internal code | Reservations may use its store; never Notifications’ email sender. |

### Your turn · finish the wiring

1. Open Program (main) and compare it with the completed example in your tab.
2. Create the room catalog with R7 and R8 next to the other wiring, and pass catalog.All() (your language’s spelling) into BookingService instead of writing the rooms there.
3. Search the four module folders for the word Internal (C#), internal (Python, Java, Go, JavaScript) or src/ (Dart): no module may import another module’s internal code.
4. Run the demo: the same nine lines as in Step 1.

**When you finish, you have:** Program (main) as the only app file that names internal classes, and the demo unchanged.
### Explanation

Keeping all the choices in one file is what makes P07 cheap: the PostgreSQL store will be one new class and one changed line in the wiring.



---

## 7. Step 6 · Architecture tests guard the borders

Three rules from your P05 design become checks that run with the tests. From now on, a forbidden dependency fails the build.





### The three rules

| Rule | In plain words | Why |
| --- | --- | --- |
| 1 · Reservations uses only the public contracts of other modules | BookingService may use the Notifications contract, never the email sender. | Notifications can change how it sends without touching booking. |
| 2 · Rooms does not depend on any other module | Rooms knows nothing about reservations, money or messages. | Rooms is the base everything else builds on. |
| 3 · Payments and Notifications do not depend on Reservations | The arrows point one way, from Reservations outwards. | No dependency circle, ever. |

**What an architecture test checks** In short: An architecture test reads the code itself (its imports or compiled types) and fails when one module uses a name it must not. A normal test checks what the app does; an architecture test checks how the code is connected. Both run with the same command.

1. Rule 1: Reservations may use only the public contracts of the other modules.
2. Rule 2: Rooms uses no other module at all.
3. Rule 3: Payments and Notifications never use Reservations.
4. If someone breaks a rule, the test fails and its message names the file or class that did it.

C#:

```text
// HotelBooking.Tests/ArchitectureTests.cs
using HotelBooking.Reservations;
using NetArchTest.Rules;

namespace HotelBooking.Tests;

// The three borders between the modules. Each test names the types that break its rule.
public class ArchitectureTests
{
    private static readonly Types App = Types.InAssembly(typeof(BookingService).Assembly);

    [Fact]
    public void Reservations_uses_only_the_public_contracts_of_other_modules()
    {
        var result = App.That().ResideInNamespace("HotelBooking.Reservations")
            .ShouldNot().HaveDependencyOnAny("HotelBooking.Rooms.Internal", "HotelBooking.Payments.Internal", "HotelBooking.Notifications.Internal")
            .GetResult();
        Assert.True(result.IsSuccessful, string.Join(", ", result.FailingTypeNames ?? []));
    }

    [Fact]
    public void Rooms_does_not_depend_on_any_other_module()
    {
        var result = App.That().ResideInNamespace("HotelBooking.Rooms")
            .ShouldNot().HaveDependencyOnAny("HotelBooking.Reservations", "HotelBooking.Payments", "HotelBooking.Notifications")
            .GetResult();
        Assert.True(result.IsSuccessful, string.Join(", ", result.FailingTypeNames ?? []));
    }

    [Fact]
    public void Payments_and_Notifications_do_not_depend_on_Reservations()
    {
        var result = App.That().ResideInNamespace("HotelBooking.Payments").Or().ResideInNamespace("HotelBooking.Notifications")
            .ShouldNot().HaveDependencyOn("HotelBooking.Reservations")
            .GetResult();
        Assert.True(result.IsSuccessful, string.Join(", ", result.FailingTypeNames ?? []));
    }
}
```

Python:

```text
# pyproject.toml
# The three borders between the modules. Run:  lint-imports
[tool.importlinter]
root_package = "hotel_booking"

[[tool.importlinter.contracts]]
name = "Reservations uses only the public contracts of other modules"
type = "forbidden"
source_modules = ["hotel_booking.reservations"]
forbidden_modules = ["hotel_booking.rooms.internal", "hotel_booking.payments.internal", "hotel_booking.notifications.internal"]

[[tool.importlinter.contracts]]
name = "Rooms does not depend on any other module"
type = "forbidden"
source_modules = ["hotel_booking.rooms"]
forbidden_modules = ["hotel_booking.reservations", "hotel_booking.payments", "hotel_booking.notifications"]

[[tool.importlinter.contracts]]
name = "Payments and Notifications do not depend on Reservations"
type = "forbidden"
source_modules = ["hotel_booking.payments", "hotel_booking.notifications"]
forbidden_modules = ["hotel_booking.reservations"]
```

Java:

```text
// src/test/java/hotelbooking/ArchitectureTests.java
package hotelbooking;

// The three borders between the modules. Run them with the other tests:  mvn -q test
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "hotelbooking", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTests {

    @ArchTest
    static final ArchRule reservationsUsesOnlyPublicContracts =
        noClasses().that().resideInAPackage("hotelbooking.reservations..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "hotelbooking.rooms.internal..", "hotelbooking.payments.internal..",
                "hotelbooking.notifications.internal..");

    @ArchTest
    static final ArchRule roomsDependsOnNoOtherModule =
        noClasses().that().resideInAPackage("hotelbooking.rooms..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "hotelbooking.reservations..", "hotelbooking.payments..", "hotelbooking.notifications..");

    @ArchTest
    static final ArchRule paymentsAndNotificationsDoNotDependOnReservations =
        noClasses().that().resideInAnyPackage("hotelbooking.payments..", "hotelbooking.notifications..")
            .should().dependOnClassesThat().resideInAPackage("hotelbooking.reservations..");
}
```

Go:

```text
// architecture_test.go
// The three borders between the modules. Run them with the other tests:  go test ./...
package main

import (
	"os"
	"path/filepath"
	"regexp"
	"slices"
	"strings"
	"testing"
)

// The public contract of each module: the only names another module may use.
// Everything else, such as the types in internal.go, is for the module itself and main.go.
var contracts = map[string][]string{
	"rooms":         {"Room", "RoomCatalog"},
	"payments":      {"PaymentService", "PaymentStatus", "Paid", "Declined"},
	"notifications": {"Confirmation", "NotificationSender"},
}

// appCode returns the code of one module: every .go file in its folder except the tests.
func appCode(t *testing.T, module string) map[string]string {
	t.Helper()
	files, err := filepath.Glob(filepath.Join(module, "*.go"))
	if err != nil {
		t.Fatal(err)
	}
	code := map[string]string{}
	for _, file := range files {
		if strings.HasSuffix(file, "_test.go") {
			continue
		}
		text, err := os.ReadFile(file)
		if err != nil {
			t.Fatal(err)
		}
		code[filepath.ToSlash(file)] = string(text) // the same path on Windows, macOS and Linux
	}
	return code
}

func TestReservationsUsesOnlyThePublicContractsOfOtherModules(t *testing.T) {
	for file, text := range appCode(t, "reservations") {
		for module, allowed := range contracts {
			// rooms.Room counts; service.rooms (a field with the same name) does not.
			for _, use := range regexp.MustCompile(`(?m)(?:^|[^.\w])`+module+`\.(\w+)`).FindAllStringSubmatch(text, -1) {
				if !slices.Contains(allowed, use[1]) {
					t.Errorf("%s uses %s.%s, which is not in the %s contract", file, module, use[1], module)
				}
			}
		}
	}
}

func TestRoomsDoesNotDependOnAnyOtherModule(t *testing.T) {
	for file, text := range appCode(t, "rooms") {
		for _, other := range []string{"reservations", "payments", "notifications"} {
			if strings.Contains(text, `"hotelbooking/`+other+`"`) {
				t.Errorf("%s imports %s", file, other)
			}
		}
	}
}

func TestPaymentsAndNotificationsDoNotDependOnReservations(t *testing.T) {
	for _, module := range []string{"payments", "notifications"} {
		for file, text := range appCode(t, module) {
			if strings.Contains(text, `"hotelbooking/reservations"`) {
				t.Errorf("%s imports reservations", file)
			}
		}
	}
}
```

JavaScript:

```text
// test/architecture.test.js
// The three borders between the modules. Run them with the other tests:  node --test
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';

// Every import in one module's code, as { file, module, internal }: the module the import
// points into ("rooms", "reservations" ...) and whether it reaches that module's internal/ folder.
function importsOf(module) {
  const folder = path.join('src', module);
  return fs.readdirSync(folder, { recursive: true })
    .filter(name => name.endsWith('.js'))
    .flatMap(name => {
      const file = path.join(folder, name);
      const text = fs.readFileSync(file, 'utf8');
      return [...text.matchAll(/from\s+'([^']+)'/g)].map(([, specifier]) => {
        const [target, next] = path.relative('src', path.resolve(path.dirname(file), specifier)).split(path.sep);
        // The file is shown with / on every system, so the message is the same on Windows.
        return { file: file.split(path.sep).join('/'), specifier, module: target, internal: next === 'internal' };
      });
    });
}

test('Reservations uses only the public contracts of other modules', () => {
  for (const use of importsOf('reservations')) {
    assert.ok(use.module === 'reservations' || !use.internal, `${use.file} imports ${use.specifier}`);
  }
});

test('Rooms does not depend on any other module', () => {
  for (const use of importsOf('rooms')) {
    assert.ok(!['reservations', 'payments', 'notifications'].includes(use.module), `${use.file} imports ${use.specifier}`);
  }
});

test('Payments and Notifications do not depend on Reservations', () => {
  for (const use of [...importsOf('payments'), ...importsOf('notifications')]) {
    assert.notEqual(use.module, 'reservations', `${use.file} imports ${use.specifier}`);
  }
});
```

Dart:

```text
// test/architecture_test.dart
// The three borders between the modules. Run them with the other tests:  dart test
import 'dart:io';

import 'package:test/test.dart';

typedef Use = ({String file, String import, String module, bool internal});

/// Every import in one module's code: the module it points into ("rooms", "reservations" ...)
/// and whether it reaches that module's src/ folder.
List<Use> importsOf(String module) {
  final uses = <Use>[];
  for (final entity in Directory('lib/$module').listSync(recursive: true)) {
    if (entity is! File || !entity.path.endsWith('.dart')) continue;
    final file = entity.path.replaceAll(r'\', '/');
    for (final match in RegExp(r"^import '([^']+)'", multiLine: true).allMatches(entity.readAsStringSync())) {
      final target = match[1]!;
      final ours = target.startsWith('package:hotel_booking/');
      if (!ours && target.contains(':')) continue; // dart:io, package:test and other packages
      final path = ours ? 'lib/${target.substring('package:hotel_booking/'.length)}' : Uri.parse(file).resolve(target).path;
      final parts = path.split('/'); // lib, module, then src or the contract file
      uses.add((file: file, import: target, module: parts[1], internal: parts[2] == 'src'));
    }
  }
  return uses;
}

void main() {
  test('Reservations uses only the public contracts of other modules', () {
    for (final use in importsOf('reservations')) {
      expect(use.module == 'reservations' || !use.internal, isTrue, reason: '${use.file} imports ${use.import}');
    }
  });

  test('Rooms does not depend on any other module', () {
    for (final use in importsOf('rooms')) {
      expect(['reservations', 'payments', 'notifications'], isNot(contains(use.module)),
          reason: '${use.file} imports ${use.import}');
    }
  });

  test('Payments and Notifications do not depend on Reservations', () {
    for (final use in [...importsOf('payments'), ...importsOf('notifications')]) {
      expect(use.module, isNot('reservations'), reason: '${use.file} imports ${use.import}');
    }
  });
}
```

### Install the checker (once)

**The border checker in your language** In short: C# uses NetArchTest, Java uses ArchUnit and Python uses import-linter; Go, JavaScript and Dart need no new tool, because a small test reads the import lines. All of them check the same three rules. Only C#, Java and Python download something, once, with internet.

1. Open your language’s tab below.
2. Run the install line once, if there is one.
3. Run the checks: you get 11 green results (Python: 8 tests, then “Contracts: 3 kept”).

C#:

```text
dotnet add HotelBooking.Tests package NetArchTest.Rules --version 1.3.2

# run the checks
dotnet test HotelBooking.Tests
```

Python:

```text
python -m venv .venv
.venv\Scripts\activate          # macOS/Linux: source .venv/bin/activate
python -m pip install import-linter

# run the checks
python -m unittest -v
lint-imports
```

Java:

```text
# add to pom.xml, inside <dependencies>, then run the tests once (internet needed once)
#   <dependency>
#     <groupId>com.tngtech.archunit</groupId>
#     <artifactId>archunit-junit5</artifactId>
#     <version>1.4.0</version>
#     <scope>test</scope>
#   </dependency>
mvn -q test

# run the checks
mvn -q test
```

Go:

```text
# nothing to install: the architecture tests use only the Go standard library

# run the checks
go test -v ./...
```

JavaScript:

```text
# nothing to install: the architecture tests use only Node.js itself

# run the checks
node --test
```

Dart:

```text
# nothing new to install: the architecture tests use package:test from P04
dart pub get

# run the checks
dart test
```

### Install

#### C#

```text
dotnet add HotelBooking.Tests package NetArchTest.Rules --version 1.3.2
```

#### Python

```text
python -m venv .venv
.venv\Scripts\activate          # macOS/Linux: source .venv/bin/activate
python -m pip install import-linter
```

#### Java

```text
# add to pom.xml, inside <dependencies>, then run the tests once (internet needed once)
#   <dependency>
#     <groupId>com.tngtech.archunit</groupId>
#     <artifactId>archunit-junit5</artifactId>
#     <version>1.4.0</version>
#     <scope>test</scope>
#   </dependency>
mvn -q test
```

#### Go

```text
# nothing to install: the architecture tests use only the Go standard library
```

#### JavaScript

```text
# nothing to install: the architecture tests use only Node.js itself
```

#### Dart

```text
# nothing new to install: the architecture tests use package:test from P04
dart pub get
```

### Completed example: the three rules in your language

### The architecture checks

#### C#

```csharp
using HotelBooking.Reservations;
using NetArchTest.Rules;

namespace HotelBooking.Tests;

// The three borders between the modules. Each test names the types that break its rule.
public class ArchitectureTests
{
    private static readonly Types App = Types.InAssembly(typeof(BookingService).Assembly);

    [Fact]
    public void Reservations_uses_only_the_public_contracts_of_other_modules()
    {
        var result = App.That().ResideInNamespace("HotelBooking.Reservations")
            .ShouldNot().HaveDependencyOnAny("HotelBooking.Rooms.Internal", "HotelBooking.Payments.Internal", "HotelBooking.Notifications.Internal")
            .GetResult();
        Assert.True(result.IsSuccessful, string.Join(", ", result.FailingTypeNames ?? []));
    }

    [Fact]
    public void Rooms_does_not_depend_on_any_other_module()
    {
        var result = App.That().ResideInNamespace("HotelBooking.Rooms")
            .ShouldNot().HaveDependencyOnAny("HotelBooking.Reservations", "HotelBooking.Payments", "HotelBooking.Notifications")
            .GetResult();
        Assert.True(result.IsSuccessful, string.Join(", ", result.FailingTypeNames ?? []));
    }

    [Fact]
    public void Payments_and_Notifications_do_not_depend_on_Reservations()
    {
        var result = App.That().ResideInNamespace("HotelBooking.Payments").Or().ResideInNamespace("HotelBooking.Notifications")
            .ShouldNot().HaveDependencyOn("HotelBooking.Reservations")
            .GetResult();
        Assert.True(result.IsSuccessful, string.Join(", ", result.FailingTypeNames ?? []));
    }
}

```

#### Python

```python
# The three borders between the modules. Run:  lint-imports
[tool.importlinter]
root_package = "hotel_booking"

[[tool.importlinter.contracts]]
name = "Reservations uses only the public contracts of other modules"
type = "forbidden"
source_modules = ["hotel_booking.reservations"]
forbidden_modules = ["hotel_booking.rooms.internal", "hotel_booking.payments.internal", "hotel_booking.notifications.internal"]

[[tool.importlinter.contracts]]
name = "Rooms does not depend on any other module"
type = "forbidden"
source_modules = ["hotel_booking.rooms"]
forbidden_modules = ["hotel_booking.reservations", "hotel_booking.payments", "hotel_booking.notifications"]

[[tool.importlinter.contracts]]
name = "Payments and Notifications do not depend on Reservations"
type = "forbidden"
source_modules = ["hotel_booking.payments", "hotel_booking.notifications"]
forbidden_modules = ["hotel_booking.reservations"]

```

#### Java

```java
package hotelbooking;

// The three borders between the modules. Run them with the other tests:  mvn -q test
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "hotelbooking", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTests {

    @ArchTest
    static final ArchRule reservationsUsesOnlyPublicContracts =
        noClasses().that().resideInAPackage("hotelbooking.reservations..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "hotelbooking.rooms.internal..", "hotelbooking.payments.internal..",
                "hotelbooking.notifications.internal..");

    @ArchTest
    static final ArchRule roomsDependsOnNoOtherModule =
        noClasses().that().resideInAPackage("hotelbooking.rooms..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "hotelbooking.reservations..", "hotelbooking.payments..", "hotelbooking.notifications..");

    @ArchTest
    static final ArchRule paymentsAndNotificationsDoNotDependOnReservations =
        noClasses().that().resideInAnyPackage("hotelbooking.payments..", "hotelbooking.notifications..")
            .should().dependOnClassesThat().resideInAPackage("hotelbooking.reservations..");
}

```

#### Go

```go
// The three borders between the modules. Run them with the other tests:  go test ./...
package main

import (
	"os"
	"path/filepath"
	"regexp"
	"slices"
	"strings"
	"testing"
)

// The public contract of each module: the only names another module may use.
// Everything else, such as the types in internal.go, is for the module itself and main.go.
var contracts = map[string][]string{
	"rooms":         {"Room", "RoomCatalog"},
	"payments":      {"PaymentService", "PaymentStatus", "Paid", "Declined"},
	"notifications": {"Confirmation", "NotificationSender"},
}

// appCode returns the code of one module: every .go file in its folder except the tests.
func appCode(t *testing.T, module string) map[string]string {
	t.Helper()
	files, err := filepath.Glob(filepath.Join(module, "*.go"))
	if err != nil {
		t.Fatal(err)
	}
	code := map[string]string{}
	for _, file := range files {
		if strings.HasSuffix(file, "_test.go") {
			continue
		}
		text, err := os.ReadFile(file)
		if err != nil {
			t.Fatal(err)
		}
		code[filepath.ToSlash(file)] = string(text) // the same path on Windows, macOS and Linux
	}
	return code
}

func TestReservationsUsesOnlyThePublicContractsOfOtherModules(t *testing.T) {
	for file, text := range appCode(t, "reservations") {
		for module, allowed := range contracts {
			// rooms.Room counts; service.rooms (a field with the same name) does not.
			for _, use := range regexp.MustCompile(`(?m)(?:^|[^.\w])`+module+`\.(\w+)`).FindAllStringSubmatch(text, -1) {
				if !slices.Contains(allowed, use[1]) {
					t.Errorf("%s uses %s.%s, which is not in the %s contract", file, module, use[1], module)
				}
			}
		}
	}
}

func TestRoomsDoesNotDependOnAnyOtherModule(t *testing.T) {
	for file, text := range appCode(t, "rooms") {
		for _, other := range []string{"reservations", "payments", "notifications"} {
			if strings.Contains(text, `"hotelbooking/`+other+`"`) {
				t.Errorf("%s imports %s", file, other)
			}
		}
	}
}

func TestPaymentsAndNotificationsDoNotDependOnReservations(t *testing.T) {
	for _, module := range []string{"payments", "notifications"} {
		for file, text := range appCode(t, module) {
			if strings.Contains(text, `"hotelbooking/reservations"`) {
				t.Errorf("%s imports reservations", file)
			}
		}
	}
}

```

#### JavaScript

```javascript
// The three borders between the modules. Run them with the other tests:  node --test
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';

// Every import in one module's code, as { file, module, internal }: the module the import
// points into ("rooms", "reservations" ...) and whether it reaches that module's internal/ folder.
function importsOf(module) {
  const folder = path.join('src', module);
  return fs.readdirSync(folder, { recursive: true })
    .filter(name => name.endsWith('.js'))
    .flatMap(name => {
      const file = path.join(folder, name);
      const text = fs.readFileSync(file, 'utf8');
      return [...text.matchAll(/from\s+'([^']+)'/g)].map(([, specifier]) => {
        const [target, next] = path.relative('src', path.resolve(path.dirname(file), specifier)).split(path.sep);
        // The file is shown with / on every system, so the message is the same on Windows.
        return { file: file.split(path.sep).join('/'), specifier, module: target, internal: next === 'internal' };
      });
    });
}

test('Reservations uses only the public contracts of other modules', () => {
  for (const use of importsOf('reservations')) {
    assert.ok(use.module === 'reservations' || !use.internal, `${use.file} imports ${use.specifier}`);
  }
});

test('Rooms does not depend on any other module', () => {
  for (const use of importsOf('rooms')) {
    assert.ok(!['reservations', 'payments', 'notifications'].includes(use.module), `${use.file} imports ${use.specifier}`);
  }
});

test('Payments and Notifications do not depend on Reservations', () => {
  for (const use of [...importsOf('payments'), ...importsOf('notifications')]) {
    assert.notEqual(use.module, 'reservations', `${use.file} imports ${use.specifier}`);
  }
});

```

#### Dart

```dart
// The three borders between the modules. Run them with the other tests:  dart test
import 'dart:io';

import 'package:test/test.dart';

typedef Use = ({String file, String import, String module, bool internal});

/// Every import in one module's code: the module it points into ("rooms", "reservations" ...)
/// and whether it reaches that module's src/ folder.
List<Use> importsOf(String module) {
  final uses = <Use>[];
  for (final entity in Directory('lib/$module').listSync(recursive: true)) {
    if (entity is! File || !entity.path.endsWith('.dart')) continue;
    final file = entity.path.replaceAll(r'\', '/');
    for (final match in RegExp(r"^import '([^']+)'", multiLine: true).allMatches(entity.readAsStringSync())) {
      final target = match[1]!;
      final ours = target.startsWith('package:hotel_booking/');
      if (!ours && target.contains(':')) continue; // dart:io, package:test and other packages
      final path = ours ? 'lib/${target.substring('package:hotel_booking/'.length)}' : Uri.parse(file).resolve(target).path;
      final parts = path.split('/'); // lib, module, then src or the contract file
      uses.add((file: file, import: target, module: parts[1], internal: parts[2] == 'src'));
    }
  }
  return uses;
}

void main() {
  test('Reservations uses only the public contracts of other modules', () {
    for (final use in importsOf('reservations')) {
      expect(use.module == 'reservations' || !use.internal, isTrue, reason: '${use.file} imports ${use.import}');
    }
  });

  test('Rooms does not depend on any other module', () {
    for (final use in importsOf('rooms')) {
      expect(['reservations', 'payments', 'notifications'], isNot(contains(use.module)),
          reason: '${use.file} imports ${use.import}');
    }
  });

  test('Payments and Notifications do not depend on Reservations', () {
    for (final use in [...importsOf('payments'), ...importsOf('notifications')]) {
      expect(use.module, isNot('reservations'), reason: '${use.file} imports ${use.import}');
    }
  });
}

```

### What you will see

### Green: all checks pass

#### C#

```text
Passed!  - Failed: 0, Passed: 11, Skipped: 0, Total: 11
```

#### Python

```text
python -m unittest
Ran 8 tests
OK

lint-imports
Reservations uses only the public contracts of other modules KEPT
Rooms does not depend on any other module KEPT
Payments and Notifications do not depend on Reservations KEPT

Contracts: 3 kept, 0 broken.
```

#### Java

```text
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0 -- in hotelbooking.BookingServiceTests
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in hotelbooking.ArchitectureTests
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
```

#### Go

```text
go test ./...
ok      hotelbooking                 (3 architecture tests)
ok      hotelbooking/reservations    (8 booking tests)
```

#### JavaScript

```text
node --test
ℹ tests 11
ℹ pass 11
ℹ fail 0
```

#### Dart

```text
dart test
+11: All tests passed!
```

### Your turn · add the three checks

1. Run the install line for your language once (C#, Java and Python; Go, JavaScript and Dart have nothing to install).
2. Create the architecture test file (Python: add the contracts to pyproject.toml) and type the three rules from your tab.
3. Run the checks: 11 green results (Python: 8 tests, then “Contracts: 3 kept, 0 broken.”).
4. In docs/design-notes.md, add ## Architecture rules and copy the three rules in plain words.

**When you finish, you have:** three architecture checks that pass, and the rules written in design-notes.md.
### Explanation

C# and Java check the compiled types, Python checks the import graph, and Go, JavaScript and Dart read the import lines in a small test. All six check the same three rules and fail with a message that names the offending file or class.



---

## 8. Step 7 · Break the border on purpose

Make Reservations reach into Notifications’ internal code, watch the check fail, read its message, then undo. A check you have never seen fail proves nothing.





### Completed example: the forbidden line

One line in BookingService’s file that names the email sender directly. The app would still build and run.

**Why break the border on purpose?** In short: A test you have never seen fail might not be testing anything; making it red once proves it works.

1. You add one line to Reservations that names EmailNotificationSender.
2. The app still builds, and the 8 booking tests still pass.
3. Only the architecture check fails, and its message names BookingService and the internal sender.
4. You remove the line and everything is green again.

C#:

```text
// HotelBooking/Reservations/BookingService.cs: Step 7 adds these lines
using HotelBooking.Notifications.Internal;
    // Step 7: a shortcut past the contract. Undo it after the test fails.
    private readonly EmailNotificationSender _shortcut = new();
```

Python:

```text
# hotel_booking/reservations/booking.py: Step 7 adds these lines
from hotel_booking.notifications.internal import EmailNotificationSender  # step 7: undo after the check fails
```

Java:

```text
// src/main/java/hotelbooking/reservations/BookingService.java: Step 7 adds these lines
import hotelbooking.notifications.internal.EmailNotificationSender;
    // Step 7: a shortcut past the contract. Undo it after the test fails.
    private final EmailNotificationSender shortcut = new EmailNotificationSender();
```

Go:

```text
// reservations/booking.go: Step 7 adds these lines
	shortcut      notifications.EmailNotificationSender // step 7: a shortcut past the contract; undo it after the test fails
```

JavaScript:

```text
// src/reservations/booking.js: Step 7 adds these lines
import { EmailNotificationSender } from '../notifications/internal/email.js'; // step 7: undo after the test fails
```

Dart:

```text
// lib/reservations/src/booking.dart: Step 7 adds these lines
import 'package:hotel_booking/notifications/src/email.dart'; // step 7: undo after the test fails
```

### Step 7 · the shortcut

#### C#

```csharp
// HotelBooking/Reservations/BookingService.cs: Step 7 adds these lines
using HotelBooking.Notifications.Internal;
    // Step 7: a shortcut past the contract. Undo it after the test fails.
    private readonly EmailNotificationSender _shortcut = new();
```

#### Python

```python
# hotel_booking/reservations/booking.py: Step 7 adds these lines
from hotel_booking.notifications.internal import EmailNotificationSender  # step 7: undo after the check fails
```

#### Java

```java
// src/main/java/hotelbooking/reservations/BookingService.java: Step 7 adds these lines
import hotelbooking.notifications.internal.EmailNotificationSender;
    // Step 7: a shortcut past the contract. Undo it after the test fails.
    private final EmailNotificationSender shortcut = new EmailNotificationSender();
```

#### Go

```go
// reservations/booking.go: Step 7 adds these lines
	shortcut      notifications.EmailNotificationSender // step 7: a shortcut past the contract; undo it after the test fails
```

#### JavaScript

```javascript
// src/reservations/booking.js: Step 7 adds these lines
import { EmailNotificationSender } from '../notifications/internal/email.js'; // step 7: undo after the test fails
```

#### Dart

```dart
// lib/reservations/src/booking.dart: Step 7 adds these lines
import 'package:hotel_booking/notifications/src/email.dart'; // step 7: undo after the test fails
```

### Completed example: the red result

### Red: the check names the bad dependency

#### C#

```text
Failed HotelBooking.Tests.ArchitectureTests.Reservations_uses_only_the_public_contracts_of_other_modules
   HotelBooking.Reservations.BookingService
...
Failed!  - Failed: 1, Passed: 10, Skipped: 0, Total: 11
```

#### Python

```text
lint-imports
...
Reservations uses only the public contracts of other modules
------------------------------------------------------------

hotel_booking.reservations is not allowed to import
hotel_booking.notifications.internal:

-   hotel_booking.reservations.booking -> hotel_booking.notifications.internal
(l.8)

Contracts: 2 kept, 1 broken.
```

#### Java

```text
ArchitectureTests.reservationsUsesOnlyPublicContracts <<< FAILURE!
Architecture Violation [Priority: MEDIUM] - Rule 'no classes that reside in a package
'hotelbooking.reservations..' should depend on classes that reside in any package
['hotelbooking.rooms.internal..', 'hotelbooking.payments.internal..',
'hotelbooking.notifications.internal..']' was violated (2 times):
Constructor <hotelbooking.reservations.BookingService.<init>(...)> calls constructor
<hotelbooking.notifications.internal.EmailNotificationSender.<init>()> in (BookingService.java:12)
Field <hotelbooking.reservations.BookingService.shortcut> has type
<hotelbooking.notifications.internal.EmailNotificationSender> in (BookingService.java:0)
Tests run: 11, Failures: 1, Errors: 0, Skipped: 0
```

#### Go

```text
go test ./...
--- FAIL: TestReservationsUsesOnlyThePublicContractsOfOtherModules
    architecture_test.go:48: reservations/booking.go uses notifications.EmailNotificationSender,
    which is not in the notifications contract
FAIL    hotelbooking
ok      hotelbooking/reservations
```

#### JavaScript

```text
node --test
✖ Reservations uses only the public contracts of other modules
  AssertionError [ERR_ASSERTION]: src/reservations/booking.js imports ../notifications/internal/email.js
ℹ tests 11
ℹ pass 10
ℹ fail 1
```

#### Dart

```text
dart test
Reservations uses only the public contracts of other modules [E]
  lib/reservations/src/booking.dart imports package:hotel_booking/notifications/src/email.dart
+10 -1: Some tests failed.
```

### Your turn · break it, read it, undo it

1. Add the line(s) from the completed example to BookingService’s file.
2. Run the checks. Predict first: which check fails, and which file or class will it name?
3. Read the red message and find the name of BookingService (or its file) and of the email sender in it.
4. Remove the line(s) and run the checks again: all green.
5. In docs/design-notes.md, add ## Broken on purpose with one line: which rule failed and what the message named.

**When you finish, you have:** a red check you caused and fixed yourself, recorded in design-notes.md, and all checks green again.
### Explanation

Only the architecture check fails; the 8 booking tests stay green, because the app still works. That is the point: the architecture checks catch a problem in the structure that the behaviour tests cannot see.



---

## 9. Step 8 · Prove nothing changed, record the decision, see what comes next

Run the three checks one last time, write ADR-001 into the project, draw the page “Modules”, and check your folder.





### Run everything

### Commands

#### C#

```text
dotnet test HotelBooking.Tests
dotnet run --project HotelBooking
dotnet run --project HotelBooking -- --race
```

#### Python

```text
python -m unittest -v
lint-imports
python main.py
python main.py --race
```

#### Java

```text
mvn -q test
mvn -q compile
java -cp target/classes hotelbooking.Main
java -cp target/classes hotelbooking.Main --race
```

#### Go

```text
go test -v ./...
go run .
go run . --race
```

#### JavaScript

```text
node --test
node main.js
node main.js --race
```

#### Dart

```text
dart test
dart run bin/main.dart
dart run bin/main.dart --race
```

### Completed example: ADR-001

**An architecture decision record (ADR)** In short: A short file that says what was decided, why, what was rejected and when to look at it again. You saw ADR-001 in L03. Today it goes into your project, because today the decision became real code.

1. Context: one team, four business areas, S2 and S3 matter most.
2. Decision: one app with four modules and architecture tests.
3. Alternatives: separate services, rejected for now.
4. Revisit when a module needs its own release or scaling, measured (L07/P13).

```text
# ADR-001: Start the hotel booking app as a modular monolith

Status: Accepted, 25 October 2026

## Context
One small team builds and releases the whole app. The hotel has four business
areas: Rooms, Reservations, Payments and Notifications. The most important
scenarios are S2 (no double booking) and S3 (a failed message keeps the booking).
Nothing needs to be released or scaled on its own yet.

## Decision
One app with four modules. Each module has a public contract and internal code.
Reservations owns the booking rule. Architecture tests stop modules from using
each other's internal code.

## Alternatives considered
Separate services for Payments and Notifications: independent releases and
scaling, but network failures, retries, more deployments and more monitoring.

## Consequences
+ One build, one deployment, local method calls; S2 is checked in one place.
- All modules are released together.
- The borders hold only while the architecture tests run (P06, and in CI from P25).

## Revisit when
A module needs its own release schedule or its own scaling, measured, not guessed
(L07/P13 makes that decision with real reasons).
```

### docs/adr-001-modular-monolith.md

```text
# ADR-001: Start the hotel booking app as a modular monolith

Status: Accepted, 25 October 2026

## Context
One small team builds and releases the whole app. The hotel has four business
areas: Rooms, Reservations, Payments and Notifications. The most important
scenarios are S2 (no double booking) and S3 (a failed message keeps the booking).
Nothing needs to be released or scaled on its own yet.

## Decision
One app with four modules. Each module has a public contract and internal code.
Reservations owns the booking rule. Architecture tests stop modules from using
each other's internal code.

## Alternatives considered
Separate services for Payments and Notifications: independent releases and
scaling, but network failures, retries, more deployments and more monitoring.

## Consequences
+ One build, one deployment, local method calls; S2 is checked in one place.
- All modules are released together.
- The borders hold only while the architecture tests run (P06, and in CI from P25).

## Revisit when
A module needs its own release schedule or its own scaling, measured, not guessed
(L07/P13 makes that decision with real reasons).
```

### Completed example: the page “Modules”

### Diagram: Completed example: the module dependency diagram

Reservations (green) uses the public contract of each other module. EmailNotificationSender is internal to Notifications (purple). The red arrow is the dependency the architecture test forbids: in Step 7 you write it on purpose and watch the test fail.

Diagram as text:

Reservations uses the Rooms, Payments and Notifications contracts. EmailNotificationSender is internal to Notifications and implements the Notifications contract. Reservations must never name EmailNotificationSender directly. That dependency is forbidden and an architecture test checks it.

**Step by step:** Draw it in this order: the five boxes, then the three “Uses” arrows, the “Implements” arrow and last the red arrow. Every dashed arrow is a line of code: an import or a type name.

1. Green box at the top: the Reservations module, with BookingService and the booking rule. It needs rooms, deposits and messages, so three arrows leave it.
2. Box “Rooms contract” and the arrow “Uses”: Reservations uses the Room type. Program.cs (main) asks the room catalog for the room list and passes it into BookingService, so BookingService itself never names the catalog class.

   C#:
   
   ```text
   // HotelBooking/Rooms/Room.cs
   namespace HotelBooking.Rooms;
   
   public record Room(string Number, string Type);
   
   // HotelBooking/Rooms/IRoomCatalog.cs
   namespace HotelBooking.Rooms;
   
   // Rooms' public contract: which rooms the hotel sells. From P07 they come from a database.
   public interface IRoomCatalog
   {
       IReadOnlyList<Room> All();
   }
   ```
   
   Python:
   
   ```text
   # hotel_booking/rooms/__init__.py
   """Rooms: which rooms the hotel sells. Public contract: Room and RoomCatalog."""
   from hotel_booking.rooms.catalog import Room, RoomCatalog
   
   __all__ = ["Room", "RoomCatalog"]
   
   # hotel_booking/rooms/catalog.py
   from __future__ import annotations
   
   from dataclasses import dataclass
   from typing import Protocol
   
   
   @dataclass(frozen=True)
   class Room:
       number: str
       type: str
   
   
   class RoomCatalog(Protocol):
       """Rooms' public contract: which rooms the hotel sells. From P07 they come from a database."""
   
       def all(self) -> list[Room]: ...
   ```
   
   Java:
   
   ```text
   // src/main/java/hotelbooking/rooms/Room.java
   package hotelbooking.rooms;
   
   public record Room(String number, String type) {}
   
   // src/main/java/hotelbooking/rooms/RoomCatalog.java
   package hotelbooking.rooms;
   
   import java.util.List;
   
   // Rooms' public contract: which rooms the hotel sells. From P07 they come from a database.
   public interface RoomCatalog {
       List<Room> all();
   }
   ```
   
   Go:
   
   ```text
   // rooms/rooms.go
   package rooms
   
   // Room is a room the hotel sells.
   type Room struct {
   	Number string
   	Type   string
   }
   
   // RoomCatalog is Rooms' public contract: which rooms the hotel sells.
   // From P07 the rooms come from a database.
   type RoomCatalog interface {
   	All() []Room
   }
   ```
   
   JavaScript:
   
   ```text
   // src/rooms/index.js
   // Rooms: which rooms the hotel sells. This file is the module's public contract.
   //
   // A room catalog keeps one promise (JavaScript has no interface keyword):
   //
   //   all()   every room the hotel sells
   //
   // From P07 the rooms come from a database.
   export class Room {
     constructor(number, type) {
       this.number = number;
       this.type = type;
     }
   
     // JavaScript compares objects by identity, so Room says when two values mean the same room.
     equals(other) {
       return this.number === other.number && this.type === other.type;
     }
   }
   ```
   
   Dart:
   
   ```text
   // lib/rooms/rooms.dart
   /// Rooms: which rooms the hotel sells. This file is the module's public contract.
   library;
   
   class Room {
     Room(this.number, this.type);
   
     final String number;
     final String type;
   
     // Dart compares objects by identity until a class says what equal means.
     @override
     bool operator ==(Object other) => other is Room && other.number == number && other.type == type;
   
     @override
     int get hashCode => Object.hash(number, type);
   }
   
   // Rooms' public contract: which rooms the hotel sells. From P07 they come from a database.
   abstract interface class RoomCatalog {
     List<Room> all();
   }
   ```
3. Box “Payments contract” and its arrow: one method, TakeDeposit, answering Paid or Declined. Booking does not call it yet; the booking saga does in P17.

   C#:
   
   ```text
   // HotelBooking/Payments/PaymentStatus.cs
   namespace HotelBooking.Payments;
   
   public enum PaymentStatus { Paid, Declined }
   
   // HotelBooking/Payments/IPaymentService.cs
   namespace HotelBooking.Payments;
   
   // Payments' public contract. Booking does not call it yet: the booking saga uses it in P17.
   public interface IPaymentService
   {
       PaymentStatus TakeDeposit(string reservationId, decimal amount);
   }
   ```
   
   Python:
   
   ```text
   # hotel_booking/payments/__init__.py
   """Payments: deposits and whether they were paid. Public contract: PaymentStatus and PaymentService."""
   from __future__ import annotations
   
   from decimal import Decimal
   from enum import Enum
   from typing import Protocol
   
   
   class PaymentStatus(Enum):
       PAID = "Paid"
       DECLINED = "Declined"
   
   
   class PaymentService(Protocol):
       """Booking does not call it yet: the booking saga uses it in P17."""
   
       def take_deposit(self, reservation_id: str, amount: Decimal) -> PaymentStatus: ...
   ```
   
   Java:
   
   ```text
   // src/main/java/hotelbooking/payments/PaymentStatus.java
   package hotelbooking.payments;
   
   public enum PaymentStatus { PAID, DECLINED }
   
   // src/main/java/hotelbooking/payments/PaymentService.java
   package hotelbooking.payments;
   
   import java.math.BigDecimal;
   
   // Payments' public contract. Booking does not call it yet: the booking saga uses it in P17.
   public interface PaymentService {
       PaymentStatus takeDeposit(String reservationId, BigDecimal amount);
   }
   ```
   
   Go:
   
   ```text
   // payments/payments.go
   package payments
   
   // PaymentStatus says whether a deposit was taken.
   type PaymentStatus string
   
   const (
   	Paid     PaymentStatus = "Paid"
   	Declined PaymentStatus = "Declined"
   )
   
   // PaymentService is Payments' public contract. Booking does not call it yet:
   // the booking saga uses it in P17. The amount is in cents.
   type PaymentService interface {
   	TakeDeposit(reservationID string, amountCents int64) PaymentStatus
   }
   ```
   
   JavaScript:
   
   ```text
   // src/payments/index.js
   // Payments: deposits and whether they were paid. This file is the module's public contract.
   //
   // A payment service keeps one promise (JavaScript has no interface keyword):
   //
   //   takeDeposit(reservationId, amount)   returns PaymentStatus.Paid or PaymentStatus.Declined
   //
   // Booking does not call it yet: the booking saga uses it in P17.
   export const PaymentStatus = Object.freeze({ Paid: 'Paid', Declined: 'Declined' });
   ```
   
   Dart:
   
   ```text
   // lib/payments/payments.dart
   /// Payments: deposits and whether they were paid. This file is the module's public contract.
   library;
   
   enum PaymentStatus { paid, declined }
   
   // Booking does not call it yet: the booking saga uses it in P17. The amount is in cents.
   abstract interface class PaymentService {
     PaymentStatus takeDeposit(String reservationId, int amountCents);
   }
   ```
4. Box “Notifications contract” and its arrow: Reservations builds a Confirmation, Notifications’ own message, and hands it to the sender. Notifications no longer needs the Reservation class, so the two modules never depend on each other in a circle.

   C#:
   
   ```text
   // HotelBooking/Notifications/Confirmation.cs
   namespace HotelBooking.Notifications;
   
   // Notifications' own message: it no longer needs the Reservation class.
   public record Confirmation(string ReservationId, string GuestEmail, string RoomNumber, DateOnly CheckIn, DateOnly CheckOut);
   
   // HotelBooking/Notifications/INotificationSender.cs
   namespace HotelBooking.Notifications;
   
   // Notifications' public contract: "tell the guest". How the message travels is its own business.
   public interface INotificationSender
   {
       void SendConfirmation(Confirmation confirmation);
   }
   ```
   
   Python:
   
   ```text
   # hotel_booking/notifications/__init__.py
   """Notifications: telling guests what happened. Public contract: Confirmation and NotificationSender."""
   from __future__ import annotations
   
   from dataclasses import dataclass
   from datetime import date
   from typing import Protocol
   
   
   @dataclass(frozen=True)
   class Confirmation:
       """Notifications' own message: it no longer needs the Reservation class."""
       reservation_id: str
       guest_email: str
       room_number: str
       check_in: date
       check_out: date
   
   
   class NotificationSender(Protocol):
       def send_confirmation(self, confirmation: Confirmation) -> None: ...
   ```
   
   Java:
   
   ```text
   // src/main/java/hotelbooking/notifications/Confirmation.java
   package hotelbooking.notifications;
   
   import java.time.LocalDate;
   
   // Notifications' own message: it no longer needs the Reservation class.
   public record Confirmation(String reservationId, String guestEmail, String roomNumber,
                              LocalDate checkIn, LocalDate checkOut) {}
   
   // src/main/java/hotelbooking/notifications/NotificationSender.java
   package hotelbooking.notifications;
   
   // Notifications' public contract: "tell the guest". How the message travels is its own business.
   public interface NotificationSender {
       void sendConfirmation(Confirmation confirmation);
   }
   ```
   
   Go:
   
   ```text
   // notifications/notifications.go
   package notifications
   
   import "time"
   
   // Confirmation is Notifications' own message: it no longer needs the Reservation type.
   type Confirmation struct {
   	ReservationID string
   	GuestEmail    string
   	RoomNumber    string
   	CheckIn       time.Time
   	CheckOut      time.Time
   }
   
   // NotificationSender is Notifications' public contract: "tell the guest".
   // How the message travels is its own business. It reports a failure instead of hiding it.
   type NotificationSender interface {
   	SendConfirmation(confirmation Confirmation) error
   }
   ```
   
   JavaScript:
   
   ```text
   // src/notifications/index.js
   // Notifications: telling guests what happened. This file is the module's public contract.
   //
   // A notification sender keeps one promise (JavaScript has no interface keyword):
   //
   //   sendConfirmation(confirmation)   tell this guest their booking is confirmed,
   //                                    or throw when the message cannot be sent
   
   // Notifications' own message: it no longer needs the Reservation class.
   export class Confirmation {
     constructor(reservationId, guestEmail, roomNumber, checkIn, checkOut) {
       Object.assign(this, { reservationId, guestEmail, roomNumber, checkIn, checkOut });
       Object.freeze(this);
     }
   }
   ```
   
   Dart:
   
   ```text
   // lib/notifications/notifications.dart
   /// Notifications: telling guests what happened. This file is the module's public contract.
   library;
   
   /// Notifications' own message: it no longer needs the Reservation class.
   class Confirmation {
     const Confirmation(this.reservationId, this.guestEmail, this.roomNumber, this.checkIn, this.checkOut);
   
     final String reservationId;
     final String guestEmail;
     final String roomNumber;
     final DateTime checkIn;
     final DateTime checkOut;
   }
   
   // Notifications' public contract: "tell the guest". How the message travels is its own business.
   abstract interface class NotificationSender {
     void sendConfirmation(Confirmation confirmation);
   }
   ```
5. Purple dashed box and the arrow “Implements”: EmailNotificationSender is internal to Notifications. It keeps the contract’s promise; only the wiring and the tests name it.

   C#:
   
   ```text
   // HotelBooking/Notifications/Internal/EmailNotificationSender.cs
   namespace HotelBooking.Notifications.Internal;
   
   // Simulated: counts the emails it would send and needs no network.
   public class EmailNotificationSender : INotificationSender
   {
       public int Sent { get; private set; }
   
       public void SendConfirmation(Confirmation confirmation) => Sent++;
   }
   ```
   
   Python:
   
   ```text
   # hotel_booking/notifications/internal.py
   from __future__ import annotations
   
   from hotel_booking.notifications import Confirmation
   
   
   class EmailNotificationSender:
       """Simulated: counts the emails it would send and needs no network."""
   
       def __init__(self) -> None:
           self.sent = 0
   
       def send_confirmation(self, confirmation: Confirmation) -> None:
           self.sent += 1
   
   
   class SmsNotificationSender:
       """Simulated, like the email sender."""
   
       def __init__(self) -> None:
           self.sent = 0
   
       def send_confirmation(self, confirmation: Confirmation) -> None:
           self.sent += 1
   ```
   
   Java:
   
   ```text
   // src/main/java/hotelbooking/notifications/internal/EmailNotificationSender.java
   package hotelbooking.notifications.internal;
   
   import hotelbooking.notifications.Confirmation;
   import hotelbooking.notifications.NotificationSender;
   
   // Simulated: counts the emails it would send and needs no network.
   public class EmailNotificationSender implements NotificationSender {
       private int sent;
   
       public int sent() {
           return sent;
       }
   
       @Override
       public void sendConfirmation(Confirmation confirmation) {
           sent++;
       }
   }
   ```
   
   Go:
   
   ```text
   // notifications/internal.go
   package notifications
   
   // EmailNotificationSender is simulated: it counts the emails it would send and needs no network.
   type EmailNotificationSender struct {
   	Sent int
   }
   
   func (email *EmailNotificationSender) SendConfirmation(confirmation Confirmation) error {
   	email.Sent++
   	return nil
   }
   
   // SmsNotificationSender is simulated, like the email sender.
   type SmsNotificationSender struct {
   	Sent int
   }
   
   func (sms *SmsNotificationSender) SendConfirmation(confirmation Confirmation) error {
   	sms.Sent++
   	return nil
   }
   ```
   
   JavaScript:
   
   ```text
   // src/notifications/internal/email.js
   // Simulated: counts the messages it would send and needs no network.
   export class EmailNotificationSender {
     sent = 0;
   
     sendConfirmation(confirmation) {
       this.sent += 1;
     }
   }
   ```
   
   Dart:
   
   ```text
   // lib/notifications/src/email.dart
   import '../notifications.dart';
   
   // Simulated: counts the emails it would send and needs no network.
   class EmailNotificationSender implements NotificationSender {
     int sent = 0;
   
     @override
     void sendConfirmation(Confirmation confirmation) => sent++;
   }
   ```
6. Red arrow: Reservations naming EmailNotificationSender directly. It would compile and run, but changing how emails are sent would then mean editing Reservations again. This is exactly the line Step 7 adds:

   C#:
   
   ```text
   // HotelBooking/Reservations/BookingService.cs: Step 7 adds these lines
   using HotelBooking.Notifications.Internal;
       // Step 7: a shortcut past the contract. Undo it after the test fails.
       private readonly EmailNotificationSender _shortcut = new();
   ```
   
   Python:
   
   ```text
   # hotel_booking/reservations/booking.py: Step 7 adds these lines
   from hotel_booking.notifications.internal import EmailNotificationSender  # step 7: undo after the check fails
   ```
   
   Java:
   
   ```text
   // src/main/java/hotelbooking/reservations/BookingService.java: Step 7 adds these lines
   import hotelbooking.notifications.internal.EmailNotificationSender;
       // Step 7: a shortcut past the contract. Undo it after the test fails.
       private final EmailNotificationSender shortcut = new EmailNotificationSender();
   ```
   
   Go:
   
   ```text
   // reservations/booking.go: Step 7 adds these lines
   	shortcut      notifications.EmailNotificationSender // step 7: a shortcut past the contract; undo it after the test fails
   ```
   
   JavaScript:
   
   ```text
   // src/reservations/booking.js: Step 7 adds these lines
   import { EmailNotificationSender } from '../notifications/internal/email.js'; // step 7: undo after the test fails
   ```
   
   Dart:
   
   ```text
   // lib/reservations/src/booking.dart: Step 7 adds these lines
   import 'package:hotel_booking/notifications/src/email.dart'; // step 7: undo after the test fails
   ```
7. The check: the architecture tests read the code and fail as soon as the red arrow exists. Open your language’s tab for the three rules.

   C#:
   
   ```text
   using HotelBooking.Reservations;
   using NetArchTest.Rules;
   
   namespace HotelBooking.Tests;
   
   // The three borders between the modules. Each test names the types that break its rule.
   public class ArchitectureTests
   {
       private static readonly Types App = Types.InAssembly(typeof(BookingService).Assembly);
   
       [Fact]
       public void Reservations_uses_only_the_public_contracts_of_other_modules()
       {
           var result = App.That().ResideInNamespace("HotelBooking.Reservations")
               .ShouldNot().HaveDependencyOnAny("HotelBooking.Rooms.Internal", "HotelBooking.Payments.Internal", "HotelBooking.Notifications.Internal")
               .GetResult();
           Assert.True(result.IsSuccessful, string.Join(", ", result.FailingTypeNames ?? []));
       }
   
       [Fact]
       public void Rooms_does_not_depend_on_any_other_module()
       {
           var result = App.That().ResideInNamespace("HotelBooking.Rooms")
               .ShouldNot().HaveDependencyOnAny("HotelBooking.Reservations", "HotelBooking.Payments", "HotelBooking.Notifications")
               .GetResult();
           Assert.True(result.IsSuccessful, string.Join(", ", result.FailingTypeNames ?? []));
       }
   
       [Fact]
       public void Payments_and_Notifications_do_not_depend_on_Reservations()
       {
           var result = App.That().ResideInNamespace("HotelBooking.Payments").Or().ResideInNamespace("HotelBooking.Notifications")
               .ShouldNot().HaveDependencyOn("HotelBooking.Reservations")
               .GetResult();
           Assert.True(result.IsSuccessful, string.Join(", ", result.FailingTypeNames ?? []));
       }
   }
   
   ```
   
   Python:
   
   ```text
   # The three borders between the modules. Run:  lint-imports
   [tool.importlinter]
   root_package = "hotel_booking"
   
   [[tool.importlinter.contracts]]
   name = "Reservations uses only the public contracts of other modules"
   type = "forbidden"
   source_modules = ["hotel_booking.reservations"]
   forbidden_modules = ["hotel_booking.rooms.internal", "hotel_booking.payments.internal", "hotel_booking.notifications.internal"]
   
   [[tool.importlinter.contracts]]
   name = "Rooms does not depend on any other module"
   type = "forbidden"
   source_modules = ["hotel_booking.rooms"]
   forbidden_modules = ["hotel_booking.reservations", "hotel_booking.payments", "hotel_booking.notifications"]
   
   [[tool.importlinter.contracts]]
   name = "Payments and Notifications do not depend on Reservations"
   type = "forbidden"
   source_modules = ["hotel_booking.payments", "hotel_booking.notifications"]
   forbidden_modules = ["hotel_booking.reservations"]
   
   ```
   
   Java:
   
   ```text
   package hotelbooking;
   
   // The three borders between the modules. Run them with the other tests:  mvn -q test
   import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
   
   import com.tngtech.archunit.core.importer.ImportOption;
   import com.tngtech.archunit.junit.AnalyzeClasses;
   import com.tngtech.archunit.junit.ArchTest;
   import com.tngtech.archunit.lang.ArchRule;
   
   @AnalyzeClasses(packages = "hotelbooking", importOptions = ImportOption.DoNotIncludeTests.class)
   class ArchitectureTests {
   
       @ArchTest
       static final ArchRule reservationsUsesOnlyPublicContracts =
           noClasses().that().resideInAPackage("hotelbooking.reservations..")
               .should().dependOnClassesThat().resideInAnyPackage(
                   "hotelbooking.rooms.internal..", "hotelbooking.payments.internal..",
                   "hotelbooking.notifications.internal..");
   
       @ArchTest
       static final ArchRule roomsDependsOnNoOtherModule =
           noClasses().that().resideInAPackage("hotelbooking.rooms..")
               .should().dependOnClassesThat().resideInAnyPackage(
                   "hotelbooking.reservations..", "hotelbooking.payments..", "hotelbooking.notifications..");
   
       @ArchTest
       static final ArchRule paymentsAndNotificationsDoNotDependOnReservations =
           noClasses().that().resideInAnyPackage("hotelbooking.payments..", "hotelbooking.notifications..")
               .should().dependOnClassesThat().resideInAPackage("hotelbooking.reservations..");
   }
   
   ```
   
   Go:
   
   ```text
   // The three borders between the modules. Run them with the other tests:  go test ./...
   package main
   
   import (
   	"os"
   	"path/filepath"
   	"regexp"
   	"slices"
   	"strings"
   	"testing"
   )
   
   // The public contract of each module: the only names another module may use.
   // Everything else, such as the types in internal.go, is for the module itself and main.go.
   var contracts = map[string][]string{
   	"rooms":         {"Room", "RoomCatalog"},
   	"payments":      {"PaymentService", "PaymentStatus", "Paid", "Declined"},
   	"notifications": {"Confirmation", "NotificationSender"},
   }
   
   // appCode returns the code of one module: every .go file in its folder except the tests.
   func appCode(t *testing.T, module string) map[string]string {
   	t.Helper()
   	files, err := filepath.Glob(filepath.Join(module, "*.go"))
   	if err != nil {
   		t.Fatal(err)
   	}
   	code := map[string]string{}
   	for _, file := range files {
   		if strings.HasSuffix(file, "_test.go") {
   			continue
   		}
   		text, err := os.ReadFile(file)
   		if err != nil {
   			t.Fatal(err)
   		}
   		code[filepath.ToSlash(file)] = string(text) // the same path on Windows, macOS and Linux
   	}
   	return code
   }
   
   func TestReservationsUsesOnlyThePublicContractsOfOtherModules(t *testing.T) {
   	for file, text := range appCode(t, "reservations") {
   		for module, allowed := range contracts {
   			// rooms.Room counts; service.rooms (a field with the same name) does not.
   			for _, use := range regexp.MustCompile(`(?m)(?:^|[^.\w])`+module+`\.(\w+)`).FindAllStringSubmatch(text, -1) {
   				if !slices.Contains(allowed, use[1]) {
   					t.Errorf("%s uses %s.%s, which is not in the %s contract", file, module, use[1], module)
   				}
   			}
   		}
   	}
   }
   
   func TestRoomsDoesNotDependOnAnyOtherModule(t *testing.T) {
   	for file, text := range appCode(t, "rooms") {
   		for _, other := range []string{"reservations", "payments", "notifications"} {
   			if strings.Contains(text, `"hotelbooking/`+other+`"`) {
   				t.Errorf("%s imports %s", file, other)
   			}
   		}
   	}
   }
   
   func TestPaymentsAndNotificationsDoNotDependOnReservations(t *testing.T) {
   	for _, module := range []string{"payments", "notifications"} {
   		for file, text := range appCode(t, module) {
   			if strings.Contains(text, `"hotelbooking/reservations"`) {
   				t.Errorf("%s imports reservations", file)
   			}
   		}
   	}
   }
   
   ```
   
   JavaScript:
   
   ```text
   // The three borders between the modules. Run them with the other tests:  node --test
   import assert from 'node:assert/strict';
   import fs from 'node:fs';
   import path from 'node:path';
   import test from 'node:test';
   
   // Every import in one module's code, as { file, module, internal }: the module the import
   // points into ("rooms", "reservations" ...) and whether it reaches that module's internal/ folder.
   function importsOf(module) {
     const folder = path.join('src', module);
     return fs.readdirSync(folder, { recursive: true })
       .filter(name => name.endsWith('.js'))
       .flatMap(name => {
         const file = path.join(folder, name);
         const text = fs.readFileSync(file, 'utf8');
         return [...text.matchAll(/from\s+'([^']+)'/g)].map(([, specifier]) => {
           const [target, next] = path.relative('src', path.resolve(path.dirname(file), specifier)).split(path.sep);
           // The file is shown with / on every system, so the message is the same on Windows.
           return { file: file.split(path.sep).join('/'), specifier, module: target, internal: next === 'internal' };
         });
       });
   }
   
   test('Reservations uses only the public contracts of other modules', () => {
     for (const use of importsOf('reservations')) {
       assert.ok(use.module === 'reservations' || !use.internal, `${use.file} imports ${use.specifier}`);
     }
   });
   
   test('Rooms does not depend on any other module', () => {
     for (const use of importsOf('rooms')) {
       assert.ok(!['reservations', 'payments', 'notifications'].includes(use.module), `${use.file} imports ${use.specifier}`);
     }
   });
   
   test('Payments and Notifications do not depend on Reservations', () => {
     for (const use of [...importsOf('payments'), ...importsOf('notifications')]) {
       assert.notEqual(use.module, 'reservations', `${use.file} imports ${use.specifier}`);
     }
   });
   
   ```
   
   Dart:
   
   ```text
   // The three borders between the modules. Run them with the other tests:  dart test
   import 'dart:io';
   
   import 'package:test/test.dart';
   
   typedef Use = ({String file, String import, String module, bool internal});
   
   /// Every import in one module's code: the module it points into ("rooms", "reservations" ...)
   /// and whether it reaches that module's src/ folder.
   List<Use> importsOf(String module) {
     final uses = <Use>[];
     for (final entity in Directory('lib/$module').listSync(recursive: true)) {
       if (entity is! File || !entity.path.endsWith('.dart')) continue;
       final file = entity.path.replaceAll(r'\', '/');
       for (final match in RegExp(r"^import '([^']+)'", multiLine: true).allMatches(entity.readAsStringSync())) {
         final target = match[1]!;
         final ours = target.startsWith('package:hotel_booking/');
         if (!ours && target.contains(':')) continue; // dart:io, package:test and other packages
         final path = ours ? 'lib/${target.substring('package:hotel_booking/'.length)}' : Uri.parse(file).resolve(target).path;
         final parts = path.split('/'); // lib, module, then src or the contract file
         uses.add((file: file, import: target, module: parts[1], internal: parts[2] == 'src'));
       }
     }
     return uses;
   }
   
   void main() {
     test('Reservations uses only the public contracts of other modules', () {
       for (final use in importsOf('reservations')) {
         expect(use.module == 'reservations' || !use.internal, isTrue, reason: '${use.file} imports ${use.import}');
       }
     });
   
     test('Rooms does not depend on any other module', () {
       for (final use in importsOf('rooms')) {
         expect(['reservations', 'payments', 'notifications'], isNot(contains(use.module)),
             reason: '${use.file} imports ${use.import}');
       }
     });
   
     test('Payments and Notifications do not depend on Reservations', () {
       for (final use in [...importsOf('payments'), ...importsOf('notifications')]) {
         expect(use.module, isNot('reservations'), reason: '${use.file} imports ${use.import}');
       }
     });
   }
   
   ```

**Legend:**
- **Green box.** The part that owns the rule the diagram is about. In this diagram: Reservations: the module whose dependencies we check.
- **White box.** A part we build ourselves: a module, a class, a business area or an application. In this diagram: The public contracts of Rooms, Payments and Notifications.
- **Purple dashed box.** Internal code of a module: classes that only their own module and the wiring may name. In this diagram: Notifications internal: EmailNotificationSender, which Reservations must not name.
- **Dashed line with an open arrowhead.** A code dependency: the code at the start names a class or interface that lives at the arrowhead. In this diagram: Reservations → Notifications contract (“uses”); EmailNotificationSender → Notifications contract (“implements”).
- **Red dashed line with an open arrowhead.** A forbidden dependency: code that must not be written. An architecture test fails if it appears. In this diagram: Reservations → EmailNotificationSender: forbidden, caught by the P06 architecture test.

### Your turn · prove, decide and draw

**draw.io: the page “Modules”** In short: One new page in docs/diagrams.drawio with five boxes and five arrows, copied from the completed example.

1. Open docs/diagrams.drawio, click + at the bottom and rename the page to Modules.
2. Draw the five boxes; fill Reservations green and make the internal box purple with a dashed border.
3. Draw the four dashed arrows with an open arrowhead: three “Uses”, one “Implements”.
4. Draw the red dashed arrow from Reservations to EmailNotificationSender and label it Forbidden.

1. Run the commands above: 11 green checks (Python: 8 tests and 3 contracts), the nine demo lines, the race still double-booking.
2. In docs/design-notes.md, add ## After with three lines, and compare them with your “Before” section: they must match.
3. Create docs/adr-001-modular-monolith.md and copy ADR-001 from the completed example.
4. Open docs/diagrams.drawio, add the page Modules and draw the diagram: five boxes, three “Uses” arrows, one “Implements” arrow and the red “Forbidden” arrow.

**When you finish, you have:** proof that nothing changed, ADR-001 in docs/, and the draw.io page “Modules”.

### Your P06 checklist

| File or page | It is finished when |
| --- | --- |
| Code | Every class lives in one of the four modules; Program (main) is the only app file that names internal code. |
| Checks | 11 green: the 8 tests from P04 and 3 architecture checks (Python: 8 tests and 3 import-linter contracts). |
| Demo and race | The same nine demo lines; the race still double-books (P09 fixes it). |
| docs/design-notes.md | P06 sections: Before, Modules, Architecture rules, Broken on purpose, After. |
| docs/adr-001-modular-monolith.md | ADR-001 is there. |
| diagrams.drawio · Modules | 5 boxes (Reservations green, the internal sender purple), 4 dashed arrows and the red forbidden one. |

### Where the hotel app goes next

| Lesson | What changes in the hotel app |
| --- | --- |
| L04 | System design: how many requests the hotel must handle, how a request travels over HTTP to a database, and when a cache helps. |
| P07 | The app goes behind an HTTP API (GET /rooms/availability, POST /reservations, DELETE /reservations/{id}) with PostgreSQL. The new store plugs in through the wiring. |
| P08 | The free-room search gets a cache; you measure the gain and see the stale-cache risk. |

### Completed example: the finished P06 folder

### hotel-booking folder

#### C#

```text
hotel-booking/   end of P06
├── docs/
│   ├── adr-001-modular-monolith.md   NEW
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio   NEW page: "Modules"
│   ├── quality-scenarios.md
│   └── requirements.md
├── HotelBooking/
│   ├── Notifications/
│   │   ├── Internal/
│   │   │   ├── EmailNotificationSender.cs
│   │   │   └── SmsNotificationSender.cs
│   │   ├── Confirmation.cs
│   │   └── INotificationSender.cs
│   ├── Payments/
│   │   ├── Internal/
│   │   │   └── SimulatedPaymentService.cs
│   │   ├── IPaymentService.cs
│   │   └── PaymentStatus.cs
│   ├── Reservations/
│   │   ├── Internal/
│   │   │   └── InMemoryReservationStore.cs
│   │   ├── BookingResult.cs
│   │   ├── BookingService.cs
│   │   ├── DateRange.cs
│   │   ├── Guest.cs
│   │   ├── IReservationStore.cs
│   │   ├── Reservation.cs
│   │   └── ReservationStatus.cs
│   ├── Rooms/
│   │   ├── Internal/
│   │   │   └── InMemoryRoomCatalog.cs
│   │   ├── IRoomCatalog.cs
│   │   └── Room.cs
│   ├── HotelBooking.csproj
│   └── Program.cs   wiring: the only app file that names internal code
└── HotelBooking.Tests/
    ├── ArchitectureTests.cs   NEW: the 3 border checks
    ├── BookingServiceTests.cs   the 8 tests from P04
    └── HotelBooking.Tests.csproj
```

#### Python

```text
hotel-booking/   end of P06
├── docs/
│   ├── adr-001-modular-monolith.md   NEW
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio   NEW page: "Modules"
│   ├── quality-scenarios.md
│   └── requirements.md
├── hotel_booking/
│   ├── notifications/
│   │   ├── __init__.py
│   │   └── internal.py
│   ├── payments/
│   │   ├── __init__.py
│   │   └── internal.py
│   ├── reservations/
│   │   ├── __init__.py
│   │   ├── booking.py
│   │   ├── domain.py
│   │   └── internal.py
│   ├── rooms/
│   │   ├── __init__.py
│   │   ├── catalog.py
│   │   └── internal.py
│   └── __init__.py
├── main.py   wiring: the only app file that names internal code
├── pyproject.toml   NEW: the 3 border checks
└── test_booking_service.py   the 8 tests from P04
```

#### Java

```text
hotel-booking/   end of P06
├── docs/
│   ├── adr-001-modular-monolith.md   NEW
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio   NEW page: "Modules"
│   ├── quality-scenarios.md
│   └── requirements.md
├── src/
│   ├── main/
│   │   └── java/
│   │       └── hotelbooking/
│   │           ├── notifications/
│   │           │   ├── internal/
│   │           │   │   ├── EmailNotificationSender.java
│   │           │   │   └── SmsNotificationSender.java
│   │           │   ├── Confirmation.java
│   │           │   └── NotificationSender.java
│   │           ├── payments/
│   │           │   ├── internal/
│   │           │   │   └── SimulatedPaymentService.java
│   │           │   ├── PaymentService.java
│   │           │   └── PaymentStatus.java
│   │           ├── reservations/
│   │           │   ├── internal/
│   │           │   │   └── InMemoryReservationStore.java
│   │           │   ├── BookingResult.java
│   │           │   ├── BookingService.java
│   │           │   ├── DateRange.java
│   │           │   ├── Guest.java
│   │           │   ├── Reservation.java
│   │           │   ├── ReservationStatus.java
│   │           │   └── ReservationStore.java
│   │           ├── rooms/
│   │           │   ├── internal/
│   │           │   │   └── InMemoryRoomCatalog.java
│   │           │   ├── Room.java
│   │           │   └── RoomCatalog.java
│   │           └── Main.java   wiring: the only app file that names internal code
│   └── test/
│       └── java/
│           └── hotelbooking/
│               ├── ArchitectureTests.java   NEW: the 3 border checks
│               └── BookingServiceTests.java   the 8 tests from P04
└── pom.xml
```

#### Go

```text
hotel-booking/   end of P06
├── docs/
│   ├── adr-001-modular-monolith.md   NEW
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio   NEW page: "Modules"
│   ├── quality-scenarios.md
│   └── requirements.md
├── notifications/
│   ├── internal.go
│   └── notifications.go
├── payments/
│   ├── internal.go
│   └── payments.go
├── reservations/
│   ├── booking_test.go   the 8 tests from P04
│   ├── booking.go
│   ├── domain.go
│   └── internal.go
├── rooms/
│   ├── internal.go
│   └── rooms.go
├── architecture_test.go   NEW: the 3 border checks
├── go.mod
└── main.go   wiring: the only app file that names internal code
```

#### JavaScript

```text
hotel-booking/   end of P06
├── docs/
│   ├── adr-001-modular-monolith.md   NEW
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio   NEW page: "Modules"
│   ├── quality-scenarios.md
│   └── requirements.md
├── src/
│   ├── notifications/
│   │   ├── internal/
│   │   │   ├── email.js
│   │   │   └── sms.js
│   │   └── index.js
│   ├── payments/
│   │   ├── internal/
│   │   │   └── simulated.js
│   │   └── index.js
│   ├── reservations/
│   │   ├── internal/
│   │   │   └── store.js
│   │   ├── booking.js
│   │   ├── domain.js
│   │   └── index.js
│   └── rooms/
│       ├── internal/
│       │   └── catalog.js
│       └── index.js
├── test/
│   ├── architecture.test.js   NEW: the 3 border checks
│   └── booking.test.js   the 8 tests from P04
├── main.js   wiring: the only app file that names internal code
└── package.json
```

#### Dart

```text
hotel-booking/   end of P06
├── bin/
│   └── main.dart   wiring: the only app file that names internal code
├── docs/
│   ├── adr-001-modular-monolith.md   NEW
│   ├── contexts.md
│   ├── design-notes.md
│   ├── diagrams.drawio   NEW page: "Modules"
│   ├── quality-scenarios.md
│   └── requirements.md
├── lib/
│   ├── notifications/
│   │   ├── src/
│   │   │   ├── email.dart
│   │   │   └── sms.dart
│   │   └── notifications.dart
│   ├── payments/
│   │   ├── src/
│   │   │   └── simulated.dart
│   │   └── payments.dart
│   ├── reservations/
│   │   ├── src/
│   │   │   ├── booking.dart
│   │   │   ├── domain.dart
│   │   │   └── store.dart
│   │   └── reservations.dart
│   └── rooms/
│       ├── src/
│       │   └── catalog.dart
│       └── rooms.dart
├── test/
│   ├── architecture_test.dart   NEW: the 3 border checks
│   └── booking_service_test.dart   the 8 tests from P04
├── analysis_options.yaml
└── pubspec.yaml
```
### Explanation

The app does exactly what it did in P04, but every business area now has a border that a test keeps. P07 adds HTTP and a database at the edges, through the wiring, without moving the booking rule.
