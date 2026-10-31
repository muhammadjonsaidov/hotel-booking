# P04 · Activity sheet

## 1. P04 · The booking app works. Now make its design easy to change.

In P03 you built the first version of the hotel booking app. Today you improve its structure without changing a single thing it does.





### Where we are

Your hotel-booking folder has the P03 app: it searches free rooms, books, refuses clashes, cancels, and shows the race. It works. But BookingService is tied directly to two concrete classes: ReservationStore (a list) and EmailSender.

### Why that becomes a problem soon

| What the hotel wants | What you would have to do today |
| --- | --- |
| “Send SMS confirmations too.” | Open BookingService and change its constructor and Reserve(). |
| “Keep bookings in PostgreSQL” (P07). | Open BookingService again and change it, even though the booking rules stay the same. |
| “Prove a failed email never loses a booking.” | You cannot: tests always go through the real EmailSender. |

### Today’s plan

| Step | What you do | What you add to your project folder |
| --- | --- | --- |
| 1 · Refactoring | Learn what refactoring is and what must stay the same | design-notes.md: P04 section |
| 2 · Lock the behaviour | Write 6 tests that pass on today’s code | A test file your language’s test runner finds |
| 3 · SOLID | Learn the five principles and find what our code breaks | design-notes.md: what our code breaks |
| 4 · Target design | Draw the “after” class diagram with interfaces | draw.io page “Classes - after” |
| 5 · Refactor | Move step by step to interfaces and separate files | Refactored code |
| 6 · Prove nothing changed | Run the tests, the demo and the race again | design-notes.md: proof |
| 7 · Feel the benefit | Use a fake sender, add rule R6, add SMS without touching BookingService | 8 green tests, R6 in requirements.md |
| 8 · Check and hand over | Check every file against the list | The complete P04 folder |

### How you work today

Work in pairs on one laptop, in the same hotel-booking folder you finished in P03. Swap who types at every new step. Keep the same language you used in P03: every code slide today has a tab for each of the six languages.

Each language runs the tests with its usual tool. Python (unittest), Go (go test) and JavaScript (node --test) have a test runner built in, so there is nothing to install. C# (xUnit), Java (JUnit with Maven) and Dart (package:test) download their test package the first time, so those three need internet once.

If you missed P03: open the P03 lesson page, choose your language under “Runnable code”, download that ZIP and extract it into a new hotel-booking folder. The “Runnable code” link on this page has the finished P04 project in the same six languages if you get stuck.

### Completed example: your folder at the end of P04

### hotel-booking folder

#### C#

```text
hotel-booking/
├── docs/
│   ├── requirements.md       R1–R5 from P03, plus R6 (new today)
│   ├── design-notes.md       P03 answers, plus today's P04 sections
│   └── diagrams.drawio       P03 pages, plus the new page "Classes - after"
├── HotelBooking.Tests/BookingServiceTests.cs   8 tests
├── HotelBooking.Tests/HotelBooking.Tests.csproj
├── HotelBooking/BookingResult.cs
├── HotelBooking/BookingService.cs
├── HotelBooking/DateRange.cs
├── HotelBooking/EmailNotificationSender.cs
├── HotelBooking/Guest.cs
├── HotelBooking/HotelBooking.csproj
├── HotelBooking/InMemoryReservationStore.cs
├── HotelBooking/INotificationSender.cs
├── HotelBooking/IReservationStore.cs
├── HotelBooking/Program.cs
├── HotelBooking/Reservation.cs
├── HotelBooking/ReservationStatus.cs
├── HotelBooking/Room.cs
└── HotelBooking/SmsNotificationSender.cs
```

#### Python

```text
hotel-booking/
├── docs/
│   ├── requirements.md       R1–R5 from P03, plus R6 (new today)
│   ├── design-notes.md       P03 answers, plus today's P04 sections
│   └── diagrams.drawio       P03 pages, plus the new page "Classes - after"
├── hotel_booking/__init__.py
├── hotel_booking/booking.py
├── hotel_booking/domain.py
├── hotel_booking/notifications.py
├── hotel_booking/stores.py
├── main.py
└── test_booking_service.py   8 tests
```

#### Java

```text
hotel-booking/
├── docs/
│   ├── requirements.md       R1–R5 from P03, plus R6 (new today)
│   ├── design-notes.md       P03 answers, plus today's P04 sections
│   └── diagrams.drawio       P03 pages, plus the new page "Classes - after"
├── pom.xml
├── src/main/java/hotelbooking/BookingResult.java
├── src/main/java/hotelbooking/BookingService.java
├── src/main/java/hotelbooking/DateRange.java
├── src/main/java/hotelbooking/EmailNotificationSender.java
├── src/main/java/hotelbooking/Guest.java
├── src/main/java/hotelbooking/InMemoryReservationStore.java
├── src/main/java/hotelbooking/Main.java
├── src/main/java/hotelbooking/NotificationSender.java
├── src/main/java/hotelbooking/Reservation.java
├── src/main/java/hotelbooking/ReservationStatus.java
├── src/main/java/hotelbooking/ReservationStore.java
├── src/main/java/hotelbooking/Room.java
├── src/main/java/hotelbooking/SmsNotificationSender.java
└── src/test/java/hotelbooking/BookingServiceTests.java   8 tests
```

#### Go

```text
hotel-booking/
├── docs/
│   ├── requirements.md       R1–R5 from P03, plus R6 (new today)
│   ├── design-notes.md       P03 answers, plus today's P04 sections
│   └── diagrams.drawio       P03 pages, plus the new page "Classes - after"
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
hotel-booking/
├── docs/
│   ├── requirements.md       R1–R5 from P03, plus R6 (new today)
│   ├── design-notes.md       P03 answers, plus today's P04 sections
│   └── diagrams.drawio       P03 pages, plus the new page "Classes - after"
├── main.js
├── package.json
├── src/booking.js
├── src/domain.js
├── src/notifications.js
├── src/stores.js
└── test/booking.test.js   8 tests
```

#### Dart

```text
hotel-booking/
├── docs/
│   ├── requirements.md       R1–R5 from P03, plus R6 (new today)
│   ├── design-notes.md       P03 answers, plus today's P04 sections
│   └── diagrams.drawio       P03 pages, plus the new page "Classes - after"
├── analysis_options.yaml
├── bin/main.dart
├── lib/booking.dart
├── lib/domain.dart
├── lib/hotel_booking.dart
├── lib/notifications.dart
├── lib/stores.dart
├── pubspec.yaml
└── test/booking_service_test.dart   8 tests
```

### Your turn · check that your P03 app still runs

1. Open your hotel-booking folder in your editor.
2. In the terminal, run the P03 demo with the command for your language. You should see the nine lines from P03.
3. Run the race demo too, by adding --race to that command. You should see “Double booking!”.
4. Open docs/design-notes.md and add a new heading at the bottom: # P04 design notes

**When you finish, you have:** the P03 app running in your folder, and a “P04 design notes” heading in design-notes.md.
### Explanation

Today the app must behave exactly as before. Only the structure changes. That is what makes the next practices (modules, database, services) cheap.



---

## 2. Step 1 · What is refactoring?

Refactoring means changing how the code is organised while keeping exactly what it does.





### Refactoring or not?

| Change | Refactoring? | Why |
| --- | --- | --- |
| Rename EmailSender to EmailNotificationSender | Yes | The name changes; every booking gives the same result. |
| Move DateRange into its own file | Yes | The code is in a different place; it does the same thing. |
| Make BookingService accept an interface instead of a concrete class | Yes | The demo still prints the same nine lines. |
| Fix the race so Omar gets Unavailable | No | That changes the behaviour. It is a bug fix, planned for P09. |
| Add SMS confirmations | No | That is a new feature. Refactoring makes it easy, but it is a separate step (Step 7). |

**Refactoring: what changes and what must not** In short: You change how the code is organised; what the app does for the guest stays exactly the same. Think of moving furniture in the hotel lobby: the room looks different, but guests still check in at the same desk with the same result.

1. Before: the class is called EmailSender and BookingService asks for exactly that class.
2. You rename it to EmailNotificationSender and let BookingService ask for an interface instead.
3. You run the demo: it still prints “Maya books R7 12-14 Nov: Accepted R101” and the other eight lines.
4. Same output, better structure: that is a refactoring. If the output changed, it was not.

C#:

```text
// before
public class EmailSender { ... }

// after: new name, same behaviour
public class EmailNotificationSender : INotificationSender { ... }
```

Python:

```text
# before
class EmailSender: ...

# after: new name, same behaviour
class EmailNotificationSender: ...   # has send_confirmation, like before
```

### What must stay exactly the same today

| Check | Expected result |
| --- | --- |
| The tests from Step 2 | All 6 still pass after every move. |
| The demo output | The same nine lines as in P03. |
| The race demo | Still “Double booking!”: refactoring does not fix bugs. |

**Why the tests come before the refactoring** In short: Tests are your safety net: if a move breaks something, a test turns red immediately.

1. You write six tests that pass on today’s P03 code.
2. You make one small refactoring move, for example renaming ReservationStore.
3. You run the tests: still 6 green, so the move was safe.
4. If one turns red, you undo or fix that one small move, before making the next one.

C#:

```text
dotnet test HotelBooking.Tests
// Passed!  - Failed: 0, Passed: 6
```

Python:

```text
python -m unittest -v
# Ran 6 tests ... OK
```

### Your turn · write down the rule for today

1. In docs/design-notes.md, under “# P04 design notes”, add the heading ## Refactoring
2. Write one sentence in your own words: what changes in a refactoring, and what must not change.
3. Under it, write the line “Must stay the same:” and list the three checks from the table above.

**When you finish, you have:** a “Refactoring” section in design-notes.md with your definition and the three checks.
### Explanation

If behaviour changes during a refactoring, you can no longer tell whether a difference is an improvement or a bug. That is why behaviour changes (bug fixes, features) are always separate steps.



---

## 3. Step 2 · Lock today’s behaviour with tests

Before changing any structure, write tests that describe what the app does now. They must pass on the P03 code, and keep passing after every refactoring move.





### What a test is

| Part | What it does | Example from the first test |
| --- | --- | --- |
| Arrange | Build the situation. | A fresh BookingService with rooms R7 and R8. |
| Act | Do one action. | Maya books R7 for 12–14 November. |
| Assert | Check the result. | The answer is “Accepted R101”. |

**How to read a test: Arrange, Act, Assert** In short: Every test does three things: prepare the situation, do one action, check the result.

1. Arrange: build a fresh BookingService and book R7 for Maya, 12–14 November.
2. Act: Omar asks for R7 on 13–15 November.
3. Assert: the answer is “Unavailable” and the store still has only one reservation.

C#:

```text
[Fact]
public void Omar_is_refused_when_a_night_is_shared()
{
    var (service, store, _) = NewService();               // Arrange
    service.Reserve(Maya, R7, Nov(12, 14));
    var result = service.Reserve(Omar, R7, Nov(13, 15));  // Act
    Assert.Equal("Unavailable", result.ToString());       // Assert
    Assert.Equal(1, store.Count);
}
```

Python:

```text
def test_omar_is_refused_when_a_night_is_shared(self):
    service, store, _ = new_service()                  # Arrange
    service.reserve(MAYA, R7, nov(12, 14))
    result = service.reserve(OMAR, R7, nov(13, 15))     # Act
    self.assertEqual("Unavailable", str(result))       # Assert
    self.assertEqual(1, store.count())
```

### Completed example: the first test

Each test is a small method. Its name says what it checks. NewService() builds a fresh service for every test, so tests never affect each other.

### The first test

#### C#

```csharp
    [Fact]
    public void Maya_gets_R7_when_it_is_free()
    {
        var (service, _, _) = NewService();
        Assert.Equal("Accepted R101", service.Reserve(Maya, R7, Nov(12, 14)).ToString());
    }
```

#### Python

```python
    def test_maya_gets_r7_when_it_is_free(self):
        service, _, _ = new_service()
        self.assertEqual("Accepted R101", str(service.reserve(MAYA, R7, nov(12, 14))))
```

#### Java

```java
    @Test
    void maya_gets_R7_when_it_is_free() {
        Fixture fixture = newService();
        assertEquals("Accepted R101", fixture.service().reserve(MAYA, R7, nov(12, 14)).toString());
    }
```

#### Go

```go
func TestMayaGetsR7WhenItIsFree(t *testing.T) {
	service, _, _ := newService()
	if got := service.Reserve(maya, r7, nov(12, 14)).String(); got != "Accepted R101" {
		t.Errorf("got %q, want %q", got, "Accepted R101")
	}
}
```

#### JavaScript

```javascript
test('Maya gets R7 when it is free', async () => {
  const { service } = newService();
  assert.equal(String(await service.reserve(maya, r7, nov(12, 14))), 'Accepted R101');
});
```

#### Dart

```dart
  test('Maya gets R7 when it is free', () async {
    final fixture = newService();
    expect('${await fixture.service.reserve(maya, r7, nov(12, 14))}', 'Accepted R101');
  });
```

### Make the code reachable from the tests

A test file can only use what the program lets it see, and each language draws that line differently. Open your tab to see what to change: C# marks every type public, JavaScript exports them, Dart moves the file into lib/, and Python, Java and Go need no change at all.

**Why C# needs “public” before the tests can run** In short: The test project is a separate program, and it can only see classes marked public. In P03 every class was written without an access word, which in C# means “only this project can use it”. Python has no such rule, so Python students skip this sub-step.

1. The test file writes new BookingService(...).
2. Without “public”, the compiler says BookingService is inaccessible.
3. You add the word public in front of each class, record and enum.
4. Nothing about the behaviour changes; the tests can now see the classes.

C#:

```text
// before
class BookingService(IReadOnlyList<Room> rooms, ...)
record Guest(string Name, string Email);

// after
public class BookingService(IReadOnlyList<Room> rooms, ...)
public record Guest(string Name, string Email);
```

Python:

```text
# nothing to do: every class in hotel_booking.py can already be imported
from hotel_booking import BookingService, Guest
```

### What changes before the tests compile

#### C#

```csharp
public readonly record struct DateRange(...)
public record Guest(string Name, string Email);
public record Room(string Number, string Type);
public enum ReservationStatus { Confirmed, Cancelled }
public class Reservation
public class ReservationStore
public class EmailSender
public record BookingResult(...)
public class BookingService(...)
```

#### Python

```python
# nothing to change in hotel_booking.py
```

#### Java

```java
// nothing to change in HotelBooking.java: the test class sits in the same
// package, so it can already use every type in the file
```

#### Go

```go
// nothing to change in main.go: a Go test file shares the package
// and can already use every type in it
```

#### JavaScript

```javascript
export { BookingService, DateRange, EmailSender, Guest, Reservation, ReservationStatus, ReservationStore, Room };

// Run the demo only when this file is started directly, not when a test imports it.
if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  if (process.argv.includes('--race')) runRace(); else runDemo();
}
```

#### Dart

```dart
// lib/hotel_booking.dart  <- the P03 file, moved here so tests can import it
// bin/main.dart          <- only starts runDemo or runRace
```

### Create the test project

Run these in the terminal, inside the hotel-booking folder.

### Create the tests

#### C#

```text
dotnet new xunit -n HotelBooking.Tests
dotnet add HotelBooking.Tests reference HotelBooking
(delete HotelBooking.Tests/UnitTest1.cs)
(create HotelBooking.Tests/BookingServiceTests.cs)
```

#### Python

```text
(create test_booking_service.py in the hotel-booking folder,
 next to hotel_booking.py)
```

#### Java

```text
(create src/test/java/hotelbooking/BookingServiceTests.java)
```

#### Go

```text
(create booking_service_test.go next to main.go)
```

#### JavaScript

```text
(create test/booking.test.js in the hotel-booking folder)
```

#### Dart

```text
(create test/booking_service_test.dart in the hotel-booking folder)
```

### The complete test file

Six tests, one for each behaviour from P03: free room, clash, adjacent stay, invalid dates, cancel, and one confirmation per accepted booking.

### Step 2 tests

#### C#

```csharp
// Step 2: tests that describe what the P03 app does today.
// They must pass before the refactoring and still pass after it.
public class BookingServiceTests
{
    private static readonly Room R7 = new("R7", "Standard");
    private static readonly Room R8 = new("R8", "Deluxe");
    private static readonly Guest Maya = new("Maya", "maya@example.com");
    private static readonly Guest Omar = new("Omar", "omar@example.com");
    private static readonly Guest Nora = new("Nora", "nora@example.com");

    private static DateRange Nov(int checkIn, int checkOut) =>
        new(new DateOnly(2026, 11, checkIn), new DateOnly(2026, 11, checkOut));

    // The only place that builds the service: a refactoring changes this method, not every test.
    private static (BookingService Service, ReservationStore Store, EmailSender Email) NewService()
    {
        var store = new ReservationStore();
        var email = new EmailSender();
        return (new BookingService([R7, R8], store, email), store, email);
    }

    [Fact]
    public void Maya_gets_R7_when_it_is_free()
    {
        var (service, _, _) = NewService();
        Assert.Equal("Accepted R101", service.Reserve(Maya, R7, Nov(12, 14)).ToString());
    }

    [Fact]
    public void Omar_is_refused_when_a_night_is_shared()
    {
        var (service, store, _) = NewService();
        service.Reserve(Maya, R7, Nov(12, 14));
        Assert.Equal("Unavailable", service.Reserve(Omar, R7, Nov(13, 15)).ToString());
        Assert.Equal(1, store.Count);
    }

    [Fact]
    public void Nora_can_arrive_on_the_day_Maya_leaves()
    {
        var (service, _, _) = NewService();
        service.Reserve(Maya, R7, Nov(12, 14));
        Assert.Equal("Accepted R102", service.Reserve(Nora, R7, Nov(14, 16)).ToString());
    }

    [Fact]
    public void Check_out_must_be_after_check_in()
    {
        var (service, store, _) = NewService();
        Assert.Equal("InvalidDates", service.Reserve(Omar, R8, Nov(15, 15)).ToString());
        Assert.Equal(0, store.Count);
    }

    [Fact]
    public void Cancelling_frees_the_room_again()
    {
        var (service, _, _) = NewService();
        service.Reserve(Maya, R7, Nov(12, 14));
        Assert.True(service.Cancel("R101"));
        Assert.Contains(R7, service.FindFreeRooms(Nov(12, 14)));
    }

    [Fact]
    public void One_confirmation_per_accepted_booking()
    {
        var (service, _, email) = NewService();
        service.Reserve(Maya, R7, Nov(12, 14));
        service.Reserve(Omar, R7, Nov(13, 15));
        service.Reserve(Nora, R7, Nov(14, 16));
        Assert.Equal(2, email.Sent);
    }
}

```

#### Python

```python
"""Step 2: tests that describe what the P03 app does today.

They must pass before the refactoring and still pass after it.
"""
import unittest
from datetime import date

from hotel_booking import BookingService, DateRange, EmailSender, Guest, ReservationStore, Room

R7 = Room("R7", "Standard")
R8 = Room("R8", "Deluxe")
MAYA = Guest("Maya", "maya@example.com")
OMAR = Guest("Omar", "omar@example.com")
NORA = Guest("Nora", "nora@example.com")


def nov(check_in: int, check_out: int) -> DateRange:
    return DateRange(date(2026, 11, check_in), date(2026, 11, check_out))


def new_service():
    """The only place that builds the service: a refactoring changes this function, not every test."""
    store = ReservationStore()
    email = EmailSender()
    return BookingService([R7, R8], store, email), store, email


class BookingServiceTests(unittest.TestCase):
    def test_maya_gets_r7_when_it_is_free(self):
        service, _, _ = new_service()
        self.assertEqual("Accepted R101", str(service.reserve(MAYA, R7, nov(12, 14))))

    def test_omar_is_refused_when_a_night_is_shared(self):
        service, store, _ = new_service()
        service.reserve(MAYA, R7, nov(12, 14))
        self.assertEqual("Unavailable", str(service.reserve(OMAR, R7, nov(13, 15))))
        self.assertEqual(1, store.count())

    def test_nora_can_arrive_on_the_day_maya_leaves(self):
        service, _, _ = new_service()
        service.reserve(MAYA, R7, nov(12, 14))
        self.assertEqual("Accepted R102", str(service.reserve(NORA, R7, nov(14, 16))))

    def test_check_out_must_be_after_check_in(self):
        service, store, _ = new_service()
        self.assertEqual("InvalidDates", str(service.reserve(OMAR, R8, nov(15, 15))))
        self.assertEqual(0, store.count())

    def test_cancelling_frees_the_room_again(self):
        service, _, _ = new_service()
        service.reserve(MAYA, R7, nov(12, 14))
        self.assertTrue(service.cancel("R101"))
        self.assertIn(R7, service.find_free_rooms(nov(12, 14)))

    def test_one_confirmation_per_accepted_booking(self):
        service, _, email = new_service()
        service.reserve(MAYA, R7, nov(12, 14))
        service.reserve(OMAR, R7, nov(13, 15))
        service.reserve(NORA, R7, nov(14, 16))
        self.assertEqual(2, email.sent)


if __name__ == "__main__":
    unittest.main()

```

#### Java

```java
// Step 2: tests that describe what the P03 app does today.
// They must pass before the refactoring and still pass after it.
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class BookingServiceTests {
    static final Room R7 = new Room("R7", "Standard");
    static final Room R8 = new Room("R8", "Deluxe");
    static final Guest MAYA = new Guest("Maya", "maya@example.com");
    static final Guest OMAR = new Guest("Omar", "omar@example.com");
    static final Guest NORA = new Guest("Nora", "nora@example.com");

    static DateRange nov(int checkIn, int checkOut) {
        return new DateRange(LocalDate.of(2026, 11, checkIn), LocalDate.of(2026, 11, checkOut));
    }

    // The only place that builds the service: a refactoring changes this record, not every test.
    record Fixture(BookingService service, ReservationStore store, EmailSender email) {}

    static Fixture newService() {
        ReservationStore store = new ReservationStore();
        EmailSender email = new EmailSender();
        return new Fixture(new BookingService(List.of(R7, R8), store, email), store, email);
    }

    @Test
    void maya_gets_R7_when_it_is_free() {
        Fixture fixture = newService();
        assertEquals("Accepted R101", fixture.service().reserve(MAYA, R7, nov(12, 14)).toString());
    }

    @Test
    void omar_is_refused_when_a_night_is_shared() {
        Fixture fixture = newService();
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertEquals("Unavailable", fixture.service().reserve(OMAR, R7, nov(13, 15)).toString());
        assertEquals(1, fixture.store().count());
    }

    @Test
    void nora_can_arrive_on_the_day_maya_leaves() {
        Fixture fixture = newService();
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertEquals("Accepted R102", fixture.service().reserve(NORA, R7, nov(14, 16)).toString());
    }

    @Test
    void check_out_must_be_after_check_in() {
        Fixture fixture = newService();
        assertEquals("InvalidDates", fixture.service().reserve(OMAR, R8, nov(15, 15)).toString());
        assertEquals(0, fixture.store().count());
    }

    @Test
    void cancelling_frees_the_room_again() {
        Fixture fixture = newService();
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertTrue(fixture.service().cancel("R101"));
        assertTrue(fixture.service().findFreeRooms(nov(12, 14)).contains(R7));
    }

    @Test
    void one_confirmation_per_accepted_booking() {
        Fixture fixture = newService();
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        fixture.service().reserve(OMAR, R7, nov(13, 15));
        fixture.service().reserve(NORA, R7, nov(14, 16));
        assertEquals(2, fixture.email().sent());
    }
}

```

#### Go

```go
// Step 2: tests that describe what the P03 app does today.
// They must pass before the refactoring and still pass after it.
package main

import "testing"

// The only place that builds the service: a refactoring changes this function, not every test.
func newService() (*BookingService, *ReservationStore, *EmailSender) {
	store := &ReservationStore{}
	email := &EmailSender{}
	return NewBookingService([]Room{r7, r8}, store, email, 0), store, email
}

func TestMayaGetsR7WhenItIsFree(t *testing.T) {
	service, _, _ := newService()
	if got := service.Reserve(maya, r7, nov(12, 14)).String(); got != "Accepted R101" {
		t.Errorf("got %q, want %q", got, "Accepted R101")
	}
}

func TestOmarIsRefusedWhenANightIsShared(t *testing.T) {
	service, store, _ := newService()
	service.Reserve(maya, r7, nov(12, 14))
	if got := service.Reserve(omar, r7, nov(13, 15)).String(); got != "Unavailable" {
		t.Errorf("got %q, want %q", got, "Unavailable")
	}
	if store.Count() != 1 {
		t.Errorf("saved %d reservations, want 1", store.Count())
	}
}

func TestNoraCanArriveOnTheDayMayaLeaves(t *testing.T) {
	service, _, _ := newService()
	service.Reserve(maya, r7, nov(12, 14))
	if got := service.Reserve(nora, r7, nov(14, 16)).String(); got != "Accepted R102" {
		t.Errorf("got %q, want %q", got, "Accepted R102")
	}
}

func TestCheckOutMustBeAfterCheckIn(t *testing.T) {
	service, store, _ := newService()
	if got := service.Reserve(omar, r8, nov(15, 15)).String(); got != "InvalidDates" {
		t.Errorf("got %q, want %q", got, "InvalidDates")
	}
	if store.Count() != 0 {
		t.Errorf("saved %d reservations, want 0", store.Count())
	}
}

func TestCancellingFreesTheRoomAgain(t *testing.T) {
	service, _, _ := newService()
	service.Reserve(maya, r7, nov(12, 14))
	if !service.Cancel("R101") {
		t.Error("cancelling R101 should succeed")
	}
	if len(service.FindFreeRooms(nov(12, 14))) != 2 {
		t.Error("R7 should be free again")
	}
}

func TestOneConfirmationPerAcceptedBooking(t *testing.T) {
	service, _, email := newService()
	service.Reserve(maya, r7, nov(12, 14))
	service.Reserve(omar, r7, nov(13, 15))
	service.Reserve(nora, r7, nov(14, 16))
	if email.Sent != 2 {
		t.Errorf("sent %d confirmations, want 2", email.Sent)
	}
}

```

#### JavaScript

```javascript
// Step 2: tests that describe what the P03 app does today.
// They must pass before the refactoring and still pass after it.
import assert from 'node:assert/strict';
import test from 'node:test';

import { BookingService, DateRange, EmailSender, Guest, ReservationStore, Room } from './hotel-booking.js';

const r7 = new Room('R7', 'Standard');
const r8 = new Room('R8', 'Deluxe');
const maya = new Guest('Maya', 'maya@example.com');
const omar = new Guest('Omar', 'omar@example.com');
const nora = new Guest('Nora', 'nora@example.com');

const nov = (checkIn, checkOut) =>
  new DateRange(new Date(Date.UTC(2026, 10, checkIn)), new Date(Date.UTC(2026, 10, checkOut)));

// The only place that builds the service: a refactoring changes this function, not every test.
function newService() {
  const store = new ReservationStore();
  const email = new EmailSender();
  return { service: new BookingService([r7, r8], store, email), store, email };
}

test('Maya gets R7 when it is free', async () => {
  const { service } = newService();
  assert.equal(String(await service.reserve(maya, r7, nov(12, 14))), 'Accepted R101');
});

test('Omar is refused when a night is shared', async () => {
  const { service, store } = newService();
  await service.reserve(maya, r7, nov(12, 14));
  assert.equal(String(await service.reserve(omar, r7, nov(13, 15))), 'Unavailable');
  assert.equal(store.count, 1);
});

test('Nora can arrive on the day Maya leaves', async () => {
  const { service } = newService();
  await service.reserve(maya, r7, nov(12, 14));
  assert.equal(String(await service.reserve(nora, r7, nov(14, 16))), 'Accepted R102');
});

test('check-out must be after check-in', async () => {
  const { service, store } = newService();
  assert.equal(String(await service.reserve(omar, r8, nov(15, 15))), 'InvalidDates');
  assert.equal(store.count, 0);
});

test('cancelling frees the room again', async () => {
  const { service } = newService();
  await service.reserve(maya, r7, nov(12, 14));
  assert.equal(service.cancel('R101'), true);
  assert.ok(service.findFreeRooms(nov(12, 14)).some(room => room.equals(r7)));
});

test('one confirmation per accepted booking', async () => {
  const { service, email } = newService();
  await service.reserve(maya, r7, nov(12, 14));
  await service.reserve(omar, r7, nov(13, 15));
  await service.reserve(nora, r7, nov(14, 16));
  assert.equal(email.sent, 2);
});

```

#### Dart

```dart
// Step 2: tests that describe what the P03 app does today.
// They must pass before the refactoring and still pass after it.
import 'package:hotel_booking/hotel_booking.dart';
import 'package:test/test.dart';

final r7 = Room('R7', 'Standard');
final r8 = Room('R8', 'Deluxe');
final maya = Guest('Maya', 'maya@example.com');
final omar = Guest('Omar', 'omar@example.com');
final nora = Guest('Nora', 'nora@example.com');

DateRange nov(int checkIn, int checkOut) =>
    DateRange(DateTime.utc(2026, 11, checkIn), DateTime.utc(2026, 11, checkOut));

// The only place that builds the service: a refactoring changes this record, not every test.
({BookingService service, ReservationStore store, EmailSender email}) newService() {
  final store = ReservationStore();
  final email = EmailSender();
  return (
    service: BookingService([r7, r8], store, email),
    store: store,
    email: email,
  );
}

void main() {
  test('Maya gets R7 when it is free', () async {
    final fixture = newService();
    expect('${await fixture.service.reserve(maya, r7, nov(12, 14))}', 'Accepted R101');
  });

  test('Omar is refused when a night is shared', () async {
    final fixture = newService();
    await fixture.service.reserve(maya, r7, nov(12, 14));
    expect('${await fixture.service.reserve(omar, r7, nov(13, 15))}', 'Unavailable');
    expect(fixture.store.count, 1);
  });

  test('Nora can arrive on the day Maya leaves', () async {
    final fixture = newService();
    await fixture.service.reserve(maya, r7, nov(12, 14));
    expect('${await fixture.service.reserve(nora, r7, nov(14, 16))}', 'Accepted R102');
  });

  test('check-out must be after check-in', () async {
    final fixture = newService();
    expect('${await fixture.service.reserve(omar, r8, nov(15, 15))}', 'InvalidDates');
    expect(fixture.store.count, 0);
  });

  test('cancelling frees the room again', () async {
    final fixture = newService();
    await fixture.service.reserve(maya, r7, nov(12, 14));
    expect(fixture.service.cancel('R101'), isTrue);
    expect(fixture.service.findFreeRooms(nov(12, 14)), contains(r7));
  });

  test('one confirmation per accepted booking', () async {
    final fixture = newService();
    await fixture.service.reserve(maya, r7, nov(12, 14));
    await fixture.service.reserve(omar, r7, nov(13, 15));
    await fixture.service.reserve(nora, r7, nov(14, 16));
    expect(fixture.email.sent, 2);
  });
}

```

### Run the tests

### Expected result

#### C#

```text
dotnet test HotelBooking.Tests
...
Passed!  - Failed: 0, Passed: 6, Skipped: 0, Total: 6
```

#### Python

```text
python -m unittest -v
...
Ran 6 tests in 0.002s

OK
```

#### Java

```text
mvn -q test
...
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
```

#### Go

```text
go test -v ./...
...
PASS
ok      hotelbooking
```

#### JavaScript

```text
node --test
...
# pass 6
# fail 0
```

#### Dart

```text
dart test
...
00:00 +6: All tests passed!
```

### Your turn · write the six tests and run them

1. Make the change your tab shows under “Make the code reachable from the tests”. For Python, Java and Go there is nothing to do.
2. Create the test file (and, for C# and Java, the test project) with the commands for your language above.
3. Type the first test from the completed example, plus the start of the file (the rooms, guests, Nov() and NewService()). Run the tests: 1 should pass.
4. Add the other five tests from “The complete test file”. Typist types, partner reads; swap after three tests.
5. Run the tests again. Your language’s runner should report 6 passing tests, as in “Expected result” above.
6. If a test fails, do not change the app: compare your test with the slide. The app is correct; it is the description that must match it.

**When you finish, you have:** six passing tests that describe exactly what the P03 app does.
### Explanation

These tests are called characterization tests: they record what the code does today, right or wrong. The race is not tested here, because it depends on timing; P09 adds that test.



---

## 4. Step 3 · SOLID: five principles in plain words

SOLID is five rules of thumb that make code easy to change. Open the “?” next to each letter for a hotel example with code.





### The five principles

| Letter | In plain words | In our hotel app |
| --- | --- | --- |
| S · Single responsibility | A class has one reason to change. | DateRange changes only for date rules; EmailNotificationSender only for sending. |
| O · Open/closed | Add behaviour with new code, not by editing working code. | SMS is a new class; BookingService is not touched. |
| L · Liskov substitution | Every implementation can replace another one. | Email, SMS or a fake sender: BookingService works with each. |
| I · Interface segregation | Keep interfaces small. | INotificationSender has one method, not ten. |
| D · Dependency inversion | Important classes depend on interfaces, not on concrete helpers. | BookingService asks for INotificationSender, not EmailSender. |

**S · Single responsibility** In short: A class should have one reason to change.

1. DateRange changes only if the date rules change.
2. EmailNotificationSender changes only if the way emails are sent changes.
3. BookingService changes only if the booking steps change.
4. If one class had to change for email reasons and for booking reasons, it would have two jobs.

C#:

```text
// one job each
public readonly record struct DateRange(...)          // date rules
public class EmailNotificationSender : INotificationSender   // sending emails
public class BookingService(...)                    // the booking steps
```

Python:

```text
# one job each
class DateRange: ...                  # date rules
class EmailNotificationSender: ...    # sending emails
class BookingService: ...             # the booking steps
```

**O · Open for extension, closed for modification** In short: Add new behaviour by adding new code, not by editing code that already works.

1. The hotel wants SMS confirmations as well as email.
2. You add a new class, SmsNotificationSender, that implements INotificationSender.
3. BookingService is not opened or edited at all.
4. Program passes the SMS sender in instead of the email sender.

C#:

```text
public class SmsNotificationSender : INotificationSender
{
    public int Sent { get; private set; }
    public void SendConfirmation(Reservation reservation) => Sent++;
}

var service = new BookingService(rooms, store, new SmsNotificationSender());
```

Python:

```text
class SmsNotificationSender:
    def __init__(self) -> None:
        self.sent = 0

    def send_confirmation(self, reservation) -> None:
        self.sent += 1

service = BookingService(rooms, store, SmsNotificationSender())
```

**L · Liskov substitution** In short: Any class that implements an interface must work everywhere the interface is expected.

1. BookingService expects an INotificationSender that sends a confirmation and returns.
2. Email, SMS and the fake sender all do exactly that, so each can replace the others.
3. A bad example: a sender whose SendConfirmation deletes the reservation. It has the right method name, but it breaks what callers expect.

C#:

```text
// all three can be passed in; BookingService works the same way with each
new BookingService(rooms, store, new EmailNotificationSender());
new BookingService(rooms, store, new SmsNotificationSender());
new BookingService(rooms, store, new FakeNotificationSender());
```

Python:

```text
# all three can be passed in; BookingService works the same way with each
BookingService(rooms, store, EmailNotificationSender())
BookingService(rooms, store, SmsNotificationSender())
BookingService(rooms, store, FakeNotificationSender())
```

**I · Interface segregation** In short: Keep interfaces small, so a class never has to promise methods it does not need.

1. INotificationSender has one method: SendConfirmation.
2. If it also had ChargeCard() and PrintInvoice(), the SMS sender would have to pretend to charge cards.
3. So sending messages and storing reservations stay in two separate interfaces.

C#:

```text
// small and focused
public interface INotificationSender
{
    void SendConfirmation(Reservation reservation);
}
```

Python:

```text
# small and focused
class NotificationSender(Protocol):
    def send_confirmation(self, reservation: Reservation) -> None: ...
```

**D · Dependency inversion** In short: Important classes should depend on a promise (an interface), not on a concrete helper. BookingService holds the booking rules: it is the important class. The list and the email are details that will change.

1. Before: BookingService asks for exactly ReservationStore and EmailSender.
2. After: it asks for IReservationStore and INotificationSender.
3. Program decides which real classes to plug in.
4. In P07 a PostgreSQL store is plugged in without touching BookingService.

C#:

```text
// before: depends on concrete helpers
public class BookingService(IReadOnlyList<Room> rooms, ReservationStore store, EmailSender email, ...)

// after: depends on promises
public class BookingService(IReadOnlyList<Room> rooms, IReservationStore store, INotificationSender notifications, ...)
```

Python:

```text
# before: depends on concrete helpers
def __init__(self, rooms, store: ReservationStore, email: EmailSender, ...): ...

# after: depends on promises (protocols)
def __init__(self, rooms, store: ReservationStore, notifications: NotificationSender, ...): ...
```

### What our P03 code breaks

| Problem in the code | Principle | Why it hurts |
| --- | --- | --- |
| BookingService’s constructor asks for exactly ReservationStore. | D | Switching to PostgreSQL in P07 means editing BookingService. |
| BookingService’s constructor asks for exactly EmailSender. | D and O | Adding SMS means editing BookingService. |
| There is no way to make the email fail in a test. | D | You cannot prove that a failed email keeps the booking. |
| All nine classes live in one file. | S | Hard to find code, and every change touches the same file. |
| A demo pause sits inside Reserve(). | (noted) | Left on purpose for the race; it goes when the database arrives. |

**Why is the slow step still inside Reserve?** In short: It is a demo tool, left there on purpose so the race can still be shown; it will go when the database arrives. A real booking service would not pause on purpose. We keep it today because refactoring must not change behaviour, and P09 uses this exact race to show why the database must protect the rule.

1. In the normal app slowStepMs is 0, so the pause does nothing.
2. Only the race demo passes 500, to make the gap between check and save visible.
3. You notice it as a design smell, but you do not fix it today.

C#:

```text
Thread.Sleep(slowStepMs); // 0 normally; the race demo makes this step slow
```

Python:

```text
time.sleep(self.slow_step_seconds)  # 0 normally; the race demo makes this step slow
```

### Completed example: one problem written down

### docs/design-notes.md

```text
## What our code breaks
- BookingService asks for exactly ReservationStore (D: dependency inversion).
  In P07 we would have to edit BookingService just to use a database.
```

### Your turn · write down what our code breaks

1. In docs/design-notes.md, add the heading ## What our code breaks
2. Copy the completed example above.
3. Add a second problem from the table in your own words: name the class, the principle letter, and what would hurt.

**When you finish, you have:** a “What our code breaks” section in design-notes.md with two problems.
### Explanation

You do not need to memorise SOLID today. S and D matter most for this lesson: give each class one job, and let the important class depend on interfaces.



---

## 5. Step 4 · Draw the target design

Before changing code, draw where you are going. Compare the P03 design with the design you will build.





### Before: the P03 design

### Diagram: Before: BookingService is tied to two concrete classes

This is the P03 code. BookingService’s constructor asks for exactly a ReservationStore (a list in memory) and exactly an EmailSender. To use a database, to send SMS, or to test without sending emails, you would have to edit BookingService itself.

Diagram as text:

BookingService: rooms, store, email, FindFreeRooms(stay), Reserve(guest, room, stay), Cancel(id). ReservationStore: rows, HasOverlap(room, stay), Save(reservation), Find(id). EmailSender: Sent, SendConfirmation(reservation). BookingService dependency ReservationStore, uses this exact class. BookingService dependency EmailSender, uses this exact class. This is the P03 code. BookingService’s constructor asks for exactly a ReservationStore (a list in memory) and exactly an EmailSender. To use a database, to send SMS, or to test without sending emails, you would have to edit BookingService itself.

**Step by step:** Read it from the top. Both arrows point at concrete classes: real, finished implementations.

1. BookingService keeps three things: the rooms, a store and an email sender. All three arrive through its constructor.

   C#:
   
   ```text
   class BookingService(IReadOnlyList<Room> rooms, ReservationStore store, EmailSender email, int slowStepMs = 0)
   ```
   
   Python:
   
   ```text
   class BookingService:
       def __init__(self, rooms, store: ReservationStore, email: EmailSender, slow_step_seconds=0): ...
   ```
2. Dashed arrow to ReservationStore: Reserve() calls store.HasOverlap(...) and store.Save(...). The constructor accepts only this exact class, which keeps reservations in a list.

   C#:
   
   ```text
   if (store.HasOverlap(room, stay)) return new("Unavailable");
   store.Save(reservation);
   ```
   
   Python:
   
   ```text
   if self.store.has_overlap(room, stay):
       return BookingResult("Unavailable")
   self.store.save(reservation)
   ```
3. Dashed arrow to EmailSender: after saving, Reserve() calls email.SendConfirmation(...). Again, only this exact class fits.

   C#:
   
   ```text
   email.SendConfirmation(reservation);
   ```
   
   Python:
   
   ```text
   self.email.send_confirmation(reservation)
   ```
4. Problem 1, testing: every test of Reserve() goes through the real EmailSender. You cannot easily check “what if the email fails?”.
5. Problem 2, change: the hotel wants SMS confirmations. Today that means opening BookingService and changing its constructor and its Reserve() code.
6. Problem 3, the future: in P07 the list becomes a PostgreSQL database. With this design BookingService would have to change again, even though its booking rules stay the same.

**Legend:**
- **Box with three parts (class).** A concrete class: it has real code inside its methods. In this diagram: ReservationStore really keeps a list; EmailSender really counts emails.
- **Dashed line with an open arrow (dependency, “uses”).** The class at the start calls methods of the class or interface the arrow points to. In this diagram: BookingService - - -> EmailSender: Reserve() calls email.SendConfirmation(...).

### The key idea: an interface

An interface is a list of methods without code: a promise. BookingService will ask for the promise, and Program will choose which class keeps it.

**What is an interface (Protocol in Python)?** In short: A list of methods without code: a promise that “whoever you give me can do these things”. Think of a power socket. The kettle does not care which power station made the electricity; it only needs the socket shape. The interface is the socket shape.

1. INotificationSender says: “has SendConfirmation(reservation)”.
2. EmailNotificationSender and SmsNotificationSender both keep that promise.
3. BookingService plugs into the promise, so either one fits.
4. In Python a Protocol plays the same role: any class with send_confirmation fits.

C#:

```text
public interface INotificationSender
{
    void SendConfirmation(Reservation reservation);
}

public class EmailNotificationSender : INotificationSender
{
    public int Sent { get; private set; }
    public void SendConfirmation(Reservation reservation) => Sent++;
}
```

Python:

```text
class NotificationSender(Protocol):
    def send_confirmation(self, reservation: Reservation) -> None: ...

class EmailNotificationSender:
    def __init__(self) -> None:
        self.sent = 0

    def send_confirmation(self, reservation: Reservation) -> None:
        self.sent += 1
```

### After: the design you will build

### Diagram: After: BookingService depends only on two interfaces

Top: BookingService. Middle: the two interfaces it uses: a list of methods with no code inside. Bottom: the classes that implement them. BookingService never names a bottom-row class, so any of them can be plugged in: email or SMS in the app, a fake in the tests, and later a PostgreSQL store. Guest, Room, DateRange and Reservation are unchanged from P03 and are left out here.

Diagram as text:

BookingService: rooms, store, notifications, FindFreeRooms(stay), Reserve(guest, room, stay), Cancel(id). IReservationStore: Count, HasOverlap(room, stay), Save(reservation), Find(id), CountConfirmed(room, stay). INotificationSender: SendConfirmation(reservation). InMemoryReservationStore: rows. EmailNotificationSender: Sent. SmsNotificationSender: Sent. FakeNotificationSender: Sent. BookingService dependency IReservationStore, uses. BookingService dependency INotificationSender, uses. InMemoryReservationStore implements IReservationStore. EmailNotificationSender implements INotificationSender. SmsNotificationSender implements INotificationSender. FakeNotificationSender implements INotificationSender. Top: BookingService. Middle: the two interfaces it uses: a list of methods with no code inside. Bottom: the classes that implement them. BookingService never names a bottom-row class, so any of them can be plugged in: email or SMS in the app, a fake in the tests, and later a PostgreSQL store. Guest, Room, DateRange and Reservation are unchanged from P03 and are left out here.

**Step by step:** Start at the top. The dashed arrows with an open head mean “uses”; the dashed lines with a hollow triangle mean “implements”.

1. BookingService still has the same three methods. Its constructor now asks for interfaces, not concrete classes.

   C#:
   
   ```text
   public class BookingService(IReadOnlyList<Room> rooms, IReservationStore store, INotificationSender notifications, int slowStepMs = 0)
   ```
   
   Python:
   
   ```text
   class BookingService:
       def __init__(self, rooms, store: ReservationStore, notifications: NotificationSender, slow_step_seconds=0): ...
   ```
2. “uses” arrow to IReservationStore: Reserve() still calls HasOverlap and Save, but through the interface. It no longer knows that the rows live in a list.

   C#:
   
   ```text
   public interface IReservationStore
   {
       int Count { get; }
       bool HasOverlap(Room room, DateRange stay);
       void Save(Reservation reservation);
       Reservation? Find(string id);
       int CountConfirmed(Room room, DateRange stay);
   }
   ```
   
   Python:
   
   ```text
   class ReservationStore(Protocol):
       def count(self) -> int: ...
       def has_overlap(self, room: Room, stay: DateRange) -> bool: ...
       def save(self, reservation: Reservation) -> None: ...
       def find(self, reservation_id: str) -> Reservation | None: ...
       def count_confirmed(self, room: Room, stay: DateRange) -> int: ...
   ```
3. “uses” arrow to INotificationSender: Reserve() asks “tell the guest”. How the message travels is somebody else’s job.

   C#:
   
   ```text
   public interface INotificationSender
   {
       void SendConfirmation(Reservation reservation);
   }
   ```
   
   Python:
   
   ```text
   class NotificationSender(Protocol):
       def send_confirmation(self, reservation: Reservation) -> None: ...
   ```
4. “implements” from InMemoryReservationStore: the P03 list-based store, renamed. It promises to have every method of IReservationStore.

   C#:
   
   ```text
   public class InMemoryReservationStore : IReservationStore
   ```
   
   Python:
   
   ```text
   class InMemoryReservationStore:   # has every method of ReservationStore
   ```
5. “implements” from EmailNotificationSender and SmsNotificationSender: two ways to tell the guest. Swapping one for the other changes one line in Program, not BookingService.

   C#:
   
   ```text
   public class EmailNotificationSender : INotificationSender
   public class SmsNotificationSender : INotificationSender
   ```
   
   Python:
   
   ```text
   class EmailNotificationSender:   # has send_confirmation
   class SmsNotificationSender:     # has send_confirmation
   ```
6. “implements” from FakeNotificationSender: it lives only in the tests. It remembers what it would send, or fails on purpose, so tests can check situations the real sender cannot easily produce.

   C#:
   
   ```text
   public class FakeNotificationSender(bool fail = false) : INotificationSender
   ```
   
   Python:
   
   ```text
   class FakeNotificationSender:   # in test_booking_service.py
   ```
7. Who connects the pieces? Program (C#) or main.py (Python): the one place that chooses the real store and sender and passes them in.

   C#:
   
   ```text
   var service = new BookingService([r7, r8], new InMemoryReservationStore(), new EmailNotificationSender());
   ```
   
   Python:
   
   ```text
   service = BookingService([R7, R8], InMemoryReservationStore(), EmailNotificationSender())
   ```

**Legend:**
- **Box marked «interface».** A list of methods with no code inside: a promise of what a class can do. In this diagram: INotificationSender promises SendConfirmation(reservation).
- **Box with no «interface» mark (class).** A concrete class that really does the work. In this diagram: EmailNotificationSender counts the emails it sends.
- **Dashed line with an open arrow (dependency, “uses”).** The class at the start calls methods of the class or interface the arrow points to. In this diagram: BookingService - - -> INotificationSender: Reserve() calls notifications.SendConfirmation(...) without knowing which class answers.
- **Dashed line with a hollow triangle (implements).** The class at the start keeps the promise of the interface the triangle touches: it has all of its methods. In this diagram: SmsNotificationSender - - -▷ INotificationSender: it has SendConfirmation, so it can be used wherever an INotificationSender is needed.

### What happens to each class

| P03 class | After P04 | Why |
| --- | --- | --- |
| EmailSender | EmailNotificationSender, implements INotificationSender | The name says what it does; BookingService only knows the interface. |
| ReservationStore | InMemoryReservationStore, implements IReservationStore | Says where the rows live; P07 adds a database store next to it. |
| BookingService | Same methods; constructor takes the two interfaces | Depends on promises, not on concrete helpers (D). |
| Guest, Room, DateRange, Reservation, ReservationStatus | Unchanged, moved into the domain file or files your language uses | One job per file (S). |
| (new) SmsNotificationSender, FakeNotificationSender | Implement INotificationSender | Added in Step 7 without touching BookingService (O). |

### Your turn · draw “Classes - after” in draw.io

Keep the “after” diagram on screen next to draw.io.

1. Open docs/diagrams.drawio, click + at the bottom, and rename the new page to Classes - after.
2. Search for class and drag 7 Class shapes onto the page. Arrange them in three rows like the “after” diagram.
3. Fill in the names, fields and methods. For the two interfaces, type «interface» above the name; for FakeNotificationSender type «tests only».
4. Draw the two “uses” arrows from BookingService: set “Pattern” to dashed and “Line end” to an open arrow in the Format panel.
5. Draw the four “implements” lines from each class up to its interface: “Pattern” dashed, and “Line end” a hollow triangle (block, unfilled).
6. Save (Ctrl+S). Check with your partner: 2 interfaces, 5 classes, 2 “uses” arrows, 4 “implements” lines.

**When you finish, you have:** the draw.io page “Classes - after” matching the target design.
### Explanation

Drawing the target first gives the refactoring a clear end point. Each move in Step 5 brings the code one step closer to this picture.



---

## 6. Step 5 · Refactor in small, safe moves

Move the code towards the target design one small step at a time. Run the tests after each move: they must stay green.





### 5.1 · The notification promise and the email sender

Add the interface, then let the old EmailSender keep that promise under a clearer name.

### 5.1

#### C#

```csharp
// HotelBooking/INotificationSender.cs
namespace HotelBooking;

// What BookingService needs: "tell the guest". How the message travels is not its business.
public interface INotificationSender
{
    void SendConfirmation(Reservation reservation);
}

// HotelBooking/EmailNotificationSender.cs
namespace HotelBooking;

// Simulated: counts the emails it would send and needs no network.
public class EmailNotificationSender : INotificationSender
{
    public int Sent { get; private set; }

    public void SendConfirmation(Reservation reservation) => Sent++;
}

```

#### Python

```python
# hotel_booking/notifications.py
from __future__ import annotations

from typing import Protocol

from hotel_booking.domain import Reservation


class NotificationSender(Protocol):
    """What BookingService needs: "tell the guest". How the message travels is not its business."""

    def send_confirmation(self, reservation: Reservation) -> None: ...


class EmailNotificationSender:
    """Simulated: counts the emails it would send and needs no network."""

    def __init__(self) -> None:
        self.sent = 0

    def send_confirmation(self, reservation: Reservation) -> None:
        self.sent += 1


```

#### Java

```java
// src/main/java/hotelbooking/NotificationSender.java
package hotelbooking;

// What BookingService needs: "tell the guest". How the message travels is not its business.
public interface NotificationSender {
    void sendConfirmation(Reservation reservation);
}
// src/main/java/hotelbooking/EmailNotificationSender.java
package hotelbooking;

// Simulated: counts the emails it would send and needs no network.
public class EmailNotificationSender implements NotificationSender {
    private int sent;

    public int sent() {
        return sent;
    }

    @Override
    public void sendConfirmation(Reservation reservation) {
        sent++;
    }
}

```

#### Go

```go
package main

// NotificationSender is what BookingService needs: "tell the guest".
// How the message travels is not its business.
type NotificationSender interface {
	SendConfirmation(reservation *Reservation)
}

// EmailNotificationSender is simulated: it counts the emails it would send and needs no network.
type EmailNotificationSender struct {
	Sent int
}

func (email *EmailNotificationSender) SendConfirmation(reservation *Reservation) {
	email.Sent++
}

```

#### JavaScript

```javascript
// What BookingService needs: "tell the guest". How the message travels is not its business.
// The contract is one method:
//
//   sendConfirmation(reservation)   tell this guest their booking is confirmed,
//                                   or throw when the message cannot be sent
//
// Simulated: counts the messages it would send and needs no network.
export class EmailNotificationSender {
  sent = 0;

  sendConfirmation(reservation) {
    this.sent += 1;
  }
}

```

#### Dart

```dart
// lib/notifications.dart
import 'domain.dart';

// What BookingService needs: "tell the guest". How the message travels is not its business.
abstract interface class NotificationSender {
  void sendConfirmation(Reservation reservation);
}

// Simulated: counts the emails it would send and needs no network.
class EmailNotificationSender implements NotificationSender {
  int sent = 0;

  @override
  void sendConfirmation(Reservation reservation) => sent++;
}

```

### 5.2 · The storage promise and the list store

Same move for storage: the interface lists what BookingService needs, and the list-based store keeps that promise.

### 5.2

#### C#

```csharp
// HotelBooking/IReservationStore.cs
namespace HotelBooking;

// What BookingService needs from storage. Today a list in memory; from P07 a database.
public interface IReservationStore
{
    int Count { get; }
    bool HasOverlap(Room room, DateRange stay);
    void Save(Reservation reservation);
    Reservation? Find(string id);
    int CountConfirmed(Room room, DateRange stay);
}

// HotelBooking/InMemoryReservationStore.cs
namespace HotelBooking;

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
# hotel_booking/stores.py
from __future__ import annotations

from typing import Protocol

from hotel_booking.domain import DateRange, Reservation, Room


class ReservationStore(Protocol):
    """What BookingService needs from storage. Today a list in memory; from P07 a database."""

    def count(self) -> int: ...
    def has_overlap(self, room: Room, stay: DateRange) -> bool: ...
    def save(self, reservation: Reservation) -> None: ...
    def find(self, reservation_id: str) -> Reservation | None: ...
    def count_confirmed(self, room: Room, stay: DateRange) -> int: ...


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
// src/main/java/hotelbooking/ReservationStore.java
package hotelbooking;

// What BookingService needs from storage. Today a list in memory; from P07 a database.
public interface ReservationStore {
    int count();

    boolean hasOverlap(Room room, DateRange stay);

    void save(Reservation reservation);

    // Returns null when no reservation has that id.
    Reservation find(String id);

    int countConfirmed(Room room, DateRange stay);
}
// src/main/java/hotelbooking/InMemoryReservationStore.java
package hotelbooking;

import java.util.ArrayList;
import java.util.List;

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
package main

// ReservationStore is what BookingService needs from storage.
// Today a list in memory; from P07 a database.
type ReservationStore interface {
	Count() int
	HasOverlap(room Room, stay DateRange) bool
	Save(reservation *Reservation)
	Find(id string) *Reservation
	CountConfirmed(room Room, stay DateRange) int
}

// InMemoryReservationStore keeps reservations in a list.
// P07 adds a PostgreSQL store with the same methods.
type InMemoryReservationStore struct {
	rows []*Reservation
}

func (store *InMemoryReservationStore) Count() int {
	return len(store.rows)
}

func (store *InMemoryReservationStore) HasOverlap(room Room, stay DateRange) bool {
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

func (store *InMemoryReservationStore) CountConfirmed(room Room, stay DateRange) int {
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
// lib/stores.dart
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

### 5.3 · BookingService asks for the promises

Only the constructor changes: it takes IReservationStore and INotificationSender. The body of every method stays the same.

### 5.3

#### C#

```csharp
// HotelBooking/BookingService.cs
namespace HotelBooking;

// Depends only on the two interfaces; Program decides which store and sender to use.
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
        notifications.SendConfirmation(reservation);
        return new("Accepted", reservation.Id);
    }

    public bool Cancel(string reservationId) => store.Find(reservationId)?.Cancel() ?? false;
}

// HotelBooking/BookingResult.cs
namespace HotelBooking;

public record BookingResult(string Outcome, string? ReservationId = null)
{
    public override string ToString() => ReservationId is null ? Outcome : $"{Outcome} {ReservationId}";
}

```

#### Python

```python
# hotel_booking/booking.py
from __future__ import annotations

import time
from dataclasses import dataclass

from hotel_booking.domain import DateRange, Guest, Reservation, Room
from hotel_booking.notifications import NotificationSender
from hotel_booking.stores import ReservationStore


@dataclass(frozen=True)
class BookingResult:
    outcome: str
    reservation_id: str | None = None

    def __str__(self) -> str:
        return self.outcome if self.reservation_id is None else f"{self.outcome} {self.reservation_id}"


class BookingService:
    """Depends only on the two protocols; main.py decides which store and sender to use."""

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
        self.notifications.send_confirmation(reservation)
        return BookingResult("Accepted", reservation.id)

    def cancel(self, reservation_id: str) -> bool:
        reservation = self.store.find(reservation_id)
        return reservation is not None and reservation.cancel()

```

#### Java

```java
// src/main/java/hotelbooking/BookingService.java
package hotelbooking;

import java.util.List;

// Depends only on the two interfaces; Main decides which store and sender to use.
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
        notifications.sendConfirmation(reservation);
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
// src/main/java/hotelbooking/BookingResult.java
package hotelbooking;

public record BookingResult(String outcome, String reservationId) {
    public BookingResult(String outcome) {
        this(outcome, null);
    }

    @Override
    public String toString() {
        return reservationId == null ? outcome : outcome + " " + reservationId;
    }
}

```

#### Go

```go
package main

import (
	"fmt"
	"time"
)

type BookingResult struct {
	Outcome       string
	ReservationID string
}

// Printing a BookingResult uses this method, the way ToString does in C#.
func (result BookingResult) String() string {
	if result.ReservationID == "" {
		return result.Outcome
	}
	return result.Outcome + " " + result.ReservationID
}

// BookingService depends only on the two interfaces; main decides which store and sender to use.
type BookingService struct {
	rooms         []Room
	store         ReservationStore
	notifications NotificationSender
	slowStep      time.Duration
}

func NewBookingService(rooms []Room, store ReservationStore, notifications NotificationSender, slowStep time.Duration) *BookingService {
	return &BookingService{rooms: rooms, store: store, notifications: notifications, slowStep: slowStep}
}

func (service *BookingService) FindFreeRooms(stay DateRange) []Room {
	free := []Room{}
	for _, room := range service.rooms {
		if !service.store.HasOverlap(room, stay) {
			free = append(free, room)
		}
	}
	return free
}

func (service *BookingService) Reserve(guest Guest, room Room, stay DateRange) BookingResult {
	if !stay.IsValid() {
		return BookingResult{Outcome: "InvalidDates"}
	}
	if service.store.HasOverlap(room, stay) {
		return BookingResult{Outcome: "Unavailable"}
	}

	time.Sleep(service.slowStep) // zero normally; the race demo makes this step slow

	reservation := NewReservation(fmt.Sprintf("R%d", 101+service.store.Count()), guest, room, stay)
	service.store.Save(reservation)
	service.notifications.SendConfirmation(reservation)
	return BookingResult{Outcome: "Accepted", ReservationID: reservation.ID}
}

func (service *BookingService) Cancel(reservationID string) bool {
	reservation := service.store.Find(reservationID)
	return reservation != nil && reservation.Cancel()
}

```

#### JavaScript

```javascript
import { Reservation } from './domain.js';

// A pause that lets other requests run while this one waits.
const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

export class BookingResult {
  constructor(outcome, reservationId = null) {
    this.outcome = outcome;
    this.reservationId = reservationId;
  }

  toString() {
    return this.reservationId === null ? this.outcome : `${this.outcome} ${this.reservationId}`;
  }
}

// Depends only on the two contracts; main.js decides which store and sender to use.
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
    this.notifications.sendConfirmation(reservation);
    return new BookingResult('Accepted', reservation.id);
  }

  cancel(reservationId) {
    return this.store.find(reservationId)?.cancel() ?? false;
  }
}

```

#### Dart

```dart
// lib/booking.dart
import 'domain.dart';
import 'notifications.dart';
import 'stores.dart';

class BookingResult {
  BookingResult(this.outcome, [this.reservationId]);

  final String outcome;
  final String? reservationId;

  @override
  String toString() => reservationId == null ? outcome : '$outcome $reservationId';
}

// Depends only on the two interfaces; bin/main.dart decides which store and sender to use.
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
    notifications.sendConfirmation(reservation);
    return BookingResult('Accepted', reservation.id);
  }

  bool cancel(String reservationId) => store.find(reservationId)?.cancel() ?? false;
}

```

### 5.4 · The data classes get their own files

The data classes move out of the one big file into the place your language keeps them: one file per type in C# and Java, a single domain module in Python, Go, JavaScript and Dart. Nothing inside them changes.

### 5.4

#### C#

```csharp
// HotelBooking/DateRange.cs
namespace HotelBooking;

// The stay: check-in day included, check-out day excluded (half-open).
public readonly record struct DateRange(DateOnly CheckIn, DateOnly CheckOut)
{
    public bool IsValid() => CheckOut > CheckIn;

    // Two stays overlap when they share at least one night.
    public bool Overlaps(DateRange other) =>
        CheckIn < other.CheckOut && other.CheckIn < CheckOut;
}

// HotelBooking/Guest.cs
namespace HotelBooking;

public record Guest(string Name, string Email);

// HotelBooking/Room.cs
namespace HotelBooking;

public record Room(string Number, string Type);

// HotelBooking/ReservationStatus.cs
namespace HotelBooking;

public enum ReservationStatus { Confirmed, Cancelled }

// HotelBooking/Reservation.cs
namespace HotelBooking;

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

```

#### Python

```python
# hotel_booking/domain.py
from __future__ import annotations

from dataclasses import dataclass
from datetime import date
from enum import Enum


# The stay: check-in day included, check-out day excluded (half-open).
@dataclass(frozen=True)
class DateRange:
    check_in: date
    check_out: date

    def is_valid(self) -> bool:
        return self.check_out > self.check_in

    # Two stays overlap when they share at least one night.
    def overlaps(self, other: DateRange) -> bool:
        return self.check_in < other.check_out and other.check_in < self.check_out


@dataclass(frozen=True)
class Guest:
    name: str
    email: str


@dataclass(frozen=True)
class Room:
    number: str
    type: str


class ReservationStatus(Enum):
    CONFIRMED = "Confirmed"
    CANCELLED = "Cancelled"


class Reservation:
    def __init__(self, reservation_id: str, guest: Guest, room: Room, stay: DateRange) -> None:
        self.id = reservation_id
        self.guest = guest
        self.room = room
        self.stay = stay
        self.status = ReservationStatus.CONFIRMED

    # A reservation blocks a request when it is still Confirmed for the same room and shares a night.
    def blocks(self, room: Room, stay: DateRange) -> bool:
        return self.status == ReservationStatus.CONFIRMED and self.room == room and self.stay.overlaps(stay)

    def cancel(self) -> bool:
        if self.status == ReservationStatus.CANCELLED:
            return False
        self.status = ReservationStatus.CANCELLED
        return True

# hotel_booking/__init__.py
"""Hotel booking app after P04: the P03 app split into modules, with its dependencies behind protocols."""
from hotel_booking.booking import BookingResult, BookingService
from hotel_booking.domain import DateRange, Guest, Reservation, ReservationStatus, Room
from hotel_booking.notifications import EmailNotificationSender, NotificationSender
from hotel_booking.stores import InMemoryReservationStore, ReservationStore

__all__ = [
    "BookingResult", "BookingService", "DateRange", "EmailNotificationSender", "Guest",
    "InMemoryReservationStore", "NotificationSender", "Reservation", "ReservationStatus",
    "ReservationStore", "Room",
]

```

#### Java

```java
// src/main/java/hotelbooking/DateRange.java
package hotelbooking;

import java.time.LocalDate;

// The stay: check-in day included, check-out day excluded (half-open).
public record DateRange(LocalDate checkIn, LocalDate checkOut) {
    public boolean isValid() {
        return checkOut.isAfter(checkIn);
    }

    // Two stays overlap when they share at least one night.
    public boolean overlaps(DateRange other) {
        return checkIn.isBefore(other.checkOut) && other.checkIn.isBefore(checkOut);
    }
}
// src/main/java/hotelbooking/Guest.java
package hotelbooking;

public record Guest(String name, String email) {}
// src/main/java/hotelbooking/Room.java
package hotelbooking;

public record Room(String number, String type) {}
// src/main/java/hotelbooking/ReservationStatus.java
package hotelbooking;

public enum ReservationStatus { CONFIRMED, CANCELLED }
// src/main/java/hotelbooking/Reservation.java
package hotelbooking;

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

```

#### Go

```go
package main

import "time"

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

type Room struct {
	Number string
	Type   string
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
	Room   Room
	Stay   DateRange
	Status ReservationStatus
}

func NewReservation(id string, guest Guest, room Room, stay DateRange) *Reservation {
	return &Reservation{ID: id, Guest: guest, Room: room, Stay: stay, Status: Confirmed}
}

// A reservation blocks a request when it is still Confirmed for the same room and shares a night.
func (reservation *Reservation) Blocks(room Room, stay DateRange) bool {
	return reservation.Status == Confirmed && reservation.Room == room && reservation.Stay.Overlaps(stay)
}

func (reservation *Reservation) Cancel() bool {
	if reservation.Status == Cancelled {
		return false
	}
	reservation.Status = Cancelled
	return true
}

```

#### JavaScript

```javascript
// The stay: check-in day included, check-out day excluded (half-open).
export class DateRange {
  constructor(checkIn, checkOut) {
    this.checkIn = checkIn;
    this.checkOut = checkOut;
  }

  isValid() {
    return this.checkOut > this.checkIn;
  }

  // Two stays overlap when they share at least one night.
  overlaps(other) {
    return this.checkIn < other.checkOut && other.checkIn < this.checkOut;
  }
}

export class Guest {
  constructor(name, email) {
    this.name = name;
    this.email = email;
  }
}

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

// JavaScript has no enum keyword: a frozen object with two values does the same job.
export const ReservationStatus = Object.freeze({ Confirmed: 'Confirmed', Cancelled: 'Cancelled' });

export class Reservation {
  constructor(id, guest, room, stay) {
    this.id = id;
    this.guest = guest;
    this.room = room;
    this.stay = stay;
    this.status = ReservationStatus.Confirmed;
  }

  // A reservation blocks a request when it is still Confirmed for the same room and shares a night.
  blocks(room, stay) {
    return this.status === ReservationStatus.Confirmed && this.room.equals(room) && this.stay.overlaps(stay);
  }

  cancel() {
    if (this.status === ReservationStatus.Cancelled) return false;
    this.status = ReservationStatus.Cancelled;
    return true;
  }
}

```

#### Dart

```dart
// lib/domain.dart
// The stay: check-in day included, check-out day excluded (half-open).
class DateRange {
  DateRange(this.checkIn, this.checkOut);

  final DateTime checkIn;
  final DateTime checkOut;

  bool isValid() => checkOut.isAfter(checkIn);

  // Two stays overlap when they share at least one night.
  bool overlaps(DateRange other) =>
      checkIn.isBefore(other.checkOut) && other.checkIn.isBefore(checkOut);
}

class Guest {
  Guest(this.name, this.email);

  final String name;
  final String email;
}

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

enum ReservationStatus { confirmed, cancelled }

class Reservation {
  Reservation(this.id, this.guest, this.room, this.stay);

  final String id;
  final Guest guest;
  final Room room;
  final DateRange stay;
  ReservationStatus status = ReservationStatus.confirmed;

  // A reservation blocks a request when it is still Confirmed for the same room and shares a night.
  bool blocks(Room room, DateRange stay) =>
      status == ReservationStatus.confirmed && this.room == room && this.stay.overlaps(stay);

  bool cancel() {
    if (status == ReservationStatus.cancelled) return false;
    status = ReservationStatus.cancelled;
    return true;
  }
}

// lib/hotel_booking.dart
/// Hotel booking app after P04: the P03 app split into libraries,
/// with its dependencies behind interfaces.
library;

export 'booking.dart';
export 'domain.dart';
export 'notifications.dart';
export 'stores.dart';

```

### 5.5 · Program wires everything together

Your program’s entry point is now the only place that chooses the real store and the real sender and passes them into BookingService.

**Who creates the objects now?** In short: One place, at the start of the program, creates the real objects and passes them in. This place is often called the composition root. BookingService never creates its own helpers; it receives them.

1. Program creates InMemoryReservationStore and EmailNotificationSender.
2. It passes both into new BookingService(...).
3. Tests do the same, but pass a FakeNotificationSender instead.
4. To switch to SMS, you change this one line and nothing else.

C#:

```text
var store = new InMemoryReservationStore();
var email = new EmailNotificationSender();
var service = new BookingService([r7, r8], store, email);
```

Python:

```text
store = InMemoryReservationStore()
email = EmailNotificationSender()
service = BookingService([R7, R8], store, email)
```

### 5.5

#### C#

```csharp
// P04 · Hotel booking: the same app as P03, in a cleaner structure.
// dotnet run --project HotelBooking            -> the normal demo
// dotnet run --project HotelBooking -- --race  -> two guests book the same room at the same moment
using HotelBooking;

var r7 = new Room("R7", "Standard");
var r8 = new Room("R8", "Deluxe");
var maya = new Guest("Maya", "maya@example.com");
var omar = new Guest("Omar", "omar@example.com");
var nora = new Guest("Nora", "nora@example.com");

if (args.Contains("--race")) RunRace(); else RunDemo();

void RunDemo()
{
    // The one place that chooses the real store and sender.
    var store = new InMemoryReservationStore();
    var email = new EmailNotificationSender();
    var service = new BookingService([r7, r8], store, email);

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
    var service = new BookingService([r7, r8], store, new EmailNotificationSender(), slowStepMs: 500);
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
"""P04 - Hotel booking: the same app as P03, in a cleaner structure.

python main.py          -> the normal demo
python main.py --race   -> two guests book the same room at the same moment
"""
from __future__ import annotations

import sys
import threading
import time
from datetime import date

from hotel_booking import BookingResult, BookingService, DateRange, EmailNotificationSender, Guest, InMemoryReservationStore, Room

R7 = Room("R7", "Standard")
R8 = Room("R8", "Deluxe")
MAYA = Guest("Maya", "maya@example.com")
OMAR = Guest("Omar", "omar@example.com")
NORA = Guest("Nora", "nora@example.com")


def nov(check_in: int, check_out: int) -> DateRange:
    return DateRange(date(2026, 11, check_in), date(2026, 11, check_out))


def names(rooms: list[Room]) -> str:
    return ", ".join(room.number for room in rooms)


def run_demo() -> None:
    # The one place that chooses the real store and sender.
    store = InMemoryReservationStore()
    email = EmailNotificationSender()
    service = BookingService([R7, R8], store, email)

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
    service = BookingService([R7, R8], store, EmailNotificationSender(), slow_step_seconds=0.5)
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
// P04 · Hotel booking: the same app as P03, in a cleaner structure.
// java -cp target/classes hotelbooking.Main           -> the normal demo
// java -cp target/classes hotelbooking.Main --race    -> two guests book the same room at the same moment
package hotelbooking;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    static final Room R7 = new Room("R7", "Standard");
    static final Room R8 = new Room("R8", "Deluxe");
    static final Guest MAYA = new Guest("Maya", "maya@example.com");
    static final Guest OMAR = new Guest("Omar", "omar@example.com");
    static final Guest NORA = new Guest("Nora", "nora@example.com");

    public static void main(String[] args) throws InterruptedException {
        if (List.of(args).contains("--race")) runRace(); else runDemo();
    }

    static void runDemo() {
        // The one place that chooses the real store and sender.
        InMemoryReservationStore store = new InMemoryReservationStore();
        EmailNotificationSender email = new EmailNotificationSender();
        BookingService service = new BookingService(List.of(R7, R8), store, email);

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
        BookingService service = new BookingService(List.of(R7, R8), store, new EmailNotificationSender(), 500);
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
// P04 · Hotel booking: the same app as P03, in a cleaner structure.
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
)

var (
	r7   = Room{Number: "R7", Type: "Standard"}
	r8   = Room{Number: "R8", Type: "Deluxe"}
	maya = Guest{Name: "Maya", Email: "maya@example.com"}
	omar = Guest{Name: "Omar", Email: "omar@example.com"}
	nora = Guest{Name: "Nora", Email: "nora@example.com"}
)

func main() {
	if slices.Contains(os.Args[1:], "--race") {
		runRace()
	} else {
		runDemo()
	}
}

func runDemo() {
	// The one place that chooses the real store and sender.
	store := &InMemoryReservationStore{}
	email := &EmailNotificationSender{}
	service := NewBookingService([]Room{r7, r8}, store, email, 0)

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
	store := &InMemoryReservationStore{}
	// Each request now spends half a second between checking and saving.
	service := NewBookingService([]Room{r7, r8}, store, &EmailNotificationSender{}, 500*time.Millisecond)
	stay := nov(12, 14)
	var mayaResult, omarResult BookingResult
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

func nov(checkIn, checkOut int) DateRange {
	return DateRange{
		CheckIn:  time.Date(2026, time.November, checkIn, 0, 0, 0, 0, time.UTC),
		CheckOut: time.Date(2026, time.November, checkOut, 0, 0, 0, 0, time.UTC),
	}
}

func names(rooms []Room) string {
	numbers := make([]string, len(rooms))
	for index, room := range rooms {
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
// P04 · Hotel booking: the same app as P03, in a cleaner structure.
// node main.js          -> the normal demo
// node main.js --race   -> two guests book the same room at the same moment
import { BookingService } from './src/booking.js';
import { DateRange, Guest, Room } from './src/domain.js';
import { EmailNotificationSender } from './src/notifications.js';
import { InMemoryReservationStore } from './src/stores.js';

const r7 = new Room('R7', 'Standard');
const r8 = new Room('R8', 'Deluxe');
const maya = new Guest('Maya', 'maya@example.com');
const omar = new Guest('Omar', 'omar@example.com');
const nora = new Guest('Nora', 'nora@example.com');

const nov = (checkIn, checkOut) =>
  new DateRange(new Date(Date.UTC(2026, 10, checkIn)), new Date(Date.UTC(2026, 10, checkOut)));

const names = rooms => rooms.map(room => room.number).join(', ');

const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));

// JavaScript prints booleans as true/false; the course output says True, so the demo spells it out.
const boolText = value => (value ? 'True' : 'False');

async function runDemo() {
  // The one place that chooses the real store and sender.
  const store = new InMemoryReservationStore();
  const email = new EmailNotificationSender();
  const service = new BookingService([r7, r8], store, email);

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
  const service = new BookingService([r7, r8], store, new EmailNotificationSender(), 500);
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
// P04 · Hotel booking: the same app as P03, in a cleaner structure.
// dart run bin/main.dart          -> the normal demo
// dart run bin/main.dart --race   -> two guests book the same room at the same moment
import 'package:hotel_booking/hotel_booking.dart';

final r7 = Room('R7', 'Standard');
final r8 = Room('R8', 'Deluxe');
final maya = Guest('Maya', 'maya@example.com');
final omar = Guest('Omar', 'omar@example.com');
final nora = Guest('Nora', 'nora@example.com');

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
  // The one place that chooses the real store and sender.
  final store = InMemoryReservationStore();
  final email = EmailNotificationSender();
  final service = BookingService([r7, r8], store, email);

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
    [r7, r8],
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

### 5.6 · The tests use the new names

Only the helper that builds the service changes, plus the import or using lines above it. Every test body stays exactly as it was: that is the point of putting the construction in one place.

### 5.6

#### C#

```csharp
    private static (BookingService Service, InMemoryReservationStore Store, EmailNotificationSender Email) NewService()
    {
        var store = new InMemoryReservationStore();
        var email = new EmailNotificationSender();
        return (new BookingService([R7, R8], store, email), store, email);
    }
```

#### Python

```python
from hotel_booking import BookingService, DateRange, EmailNotificationSender, Guest, InMemoryReservationStore, Room

def new_service():
    """The only place that builds the service: a refactoring changes this function, not every test."""
    store = InMemoryReservationStore()
    email = EmailNotificationSender()
    return BookingService([R7, R8], store, email), store, email
```

#### Java

```java
    // The only place that builds the service: a refactoring changes this record, not every test.
    record Fixture(BookingService service, InMemoryReservationStore store, EmailNotificationSender email) {}

    static Fixture newService() {
        InMemoryReservationStore store = new InMemoryReservationStore();
        EmailNotificationSender email = new EmailNotificationSender();
        return new Fixture(new BookingService(List.of(R7, R8), store, email), store, email);
    }
```

#### Go

```go
// The only place that builds the service: a refactoring changes this function, not every test.
func newService() (*BookingService, *InMemoryReservationStore, *EmailNotificationSender) {
	store := &InMemoryReservationStore{}
	email := &EmailNotificationSender{}
	return NewBookingService([]Room{r7, r8}, store, email, 0), store, email
}
```

#### JavaScript

```javascript
import { BookingService } from './src/booking.js';
import { DateRange, Guest, Room } from './src/domain.js';
import { EmailNotificationSender } from './src/notifications.js';
import { InMemoryReservationStore } from './src/stores.js';

// The only place that builds the service: a refactoring changes this function, not every test.
function newService() {
  const store = new InMemoryReservationStore();
  const email = new EmailNotificationSender();
  return { service: new BookingService([r7, r8], store, email), store, email };
}
```

#### Dart

```dart
// The only place that builds the service: a refactoring changes this record, not every test.
({BookingService service, InMemoryReservationStore store, EmailNotificationSender email})
    newService() {
  final store = InMemoryReservationStore();
  final email = EmailNotificationSender();
  return (
    service: BookingService([r7, r8], store, email),
    store: store,
    email: email,
  );
}
```

### Your turn · refactor in six moves

After every move, run the tests with the command for your language. If they turn red, fix that move before the next one.

**Python: why the old hotel_booking.py must be deleted** In short: A folder and a file with the same name confuse imports; Python picks the folder. You create a folder hotel_booking/ (a package) while the file hotel_booking.py still exists. “from hotel_booking import …” now reads the folder, and the old file is silently ignored.

1. Create hotel_booking/ with __init__.py and the new modules.
2. Run the tests: they now use the folder, and they should pass.
3. Only then delete hotel_booking.py, so nobody edits the wrong file by mistake.

C#:

```text
// C# has no such rule: class files in one project can have any names
// HotelBooking/BookingService.cs, HotelBooking/DateRange.cs, ...
```

Python:

```text
hotel_booking/          <- Python imports this folder
    __init__.py
    domain.py
    ...
hotel_booking.py        <- ignored once the folder exists: delete it
```

1. Open your language’s tab on 5.1 and create the notification file it shows: the promise (the interface) and the email sender that keeps it. Rename EmailSender to EmailNotificationSender; your editor can do it with Rename Symbol (F2 in VS Code), which updates every use at once.
2. Do the same for storage from 5.2: create the storage file, and rename ReservationStore to InMemoryReservationStore. The name ReservationStore is now free for the promise itself.
3. Change BookingService as in 5.3 so its constructor asks for the two promises instead of the two concrete classes, and update the test helper as in 5.6. Run the tests: 6 passing.
4. Move the data classes into their own file or files as in 5.4. Keep the old file until the new ones work, then delete it.
5. Make your entry point look like 5.5: it is now the only place that picks the real store and the real sender.
6. Run the tests one more time: still 6 passing, and no test body changed.

**When you finish, you have:** the refactored app in separate files, with BookingService depending only on interfaces, and the same 6 tests green.
### Explanation

Small moves with a green test run after each one are the heart of safe refactoring. If something breaks, you know it was the last small move.



---

## 7. Step 6 · Prove that nothing changed

A refactoring is finished only when the tests, the demo and the race all give exactly the same results as before.





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

### The demo must still print exactly this

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

### The race must still double-book

Refactoring changes the structure, not the behaviour, so the P03 bug is still there. That is correct for today; P09 fixes it.

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

### Your turn · run the checks and write the proof

1. Run the three commands above.
2. Compare the demo output with the expected output, line by line.
3. In docs/design-notes.md, add the heading ## Proof and write three lines: tests (how many passed), demo (same or different), race (still double-booked or not).

**When you finish, you have:** a “Proof” section in design-notes.md showing 6 green tests, the same demo output and the same race.
### Explanation

If any of the three checks differs from before, the change was not a pure refactoring: find which move changed behaviour and undo it.



---

## 8. Step 7 · Feel the benefit

Now that BookingService depends on interfaces, three things that were hard in P03 become easy.





### 7.1 · A fake sender for the tests

Replace the real email sender in the tests with a fake that remembers which reservations it would confirm. The sixth test can now check the exact ids.

**What is a fake?** In short: A tiny class that stands in for a real one in tests, so the test can see or force what happens.

1. The real sender would send an email you cannot inspect in a test.
2. FakeNotificationSender just writes down “R101” in a list.
3. The test checks the list: exactly R101 and R102 were confirmed.
4. With fail: true, the fake throws an error, so you can test what happens when email is down.

C#:

```text
public class FakeNotificationSender(bool fail = false) : INotificationSender
{
    public List<string> Sent { get; } = [];

    public void SendConfirmation(Reservation reservation)
    {
        if (fail) throw new InvalidOperationException("The mail server is down.");
        Sent.Add(reservation.Id);
    }
}
```

Python:

```text
class FakeNotificationSender:
    def __init__(self, fail: bool = False) -> None:
        self.fail = fail
        self.sent: list[str] = []

    def send_confirmation(self, reservation) -> None:
        if self.fail:
            raise RuntimeError("The mail server is down.")
        self.sent.append(reservation.id)
```

### 7.1 · Fake sender and new helper

#### C#

```csharp
// A stand-in sender for tests: remembers what it would send, or fails on purpose.
public class FakeNotificationSender(bool fail = false) : INotificationSender
{
    public List<string> Sent { get; } = [];

    public void SendConfirmation(Reservation reservation)
    {
        if (fail) throw new InvalidOperationException("The mail server is down.");
        Sent.Add(reservation.Id);
    }
}

    private static (BookingService Service, InMemoryReservationStore Store, FakeNotificationSender Sender) NewService(bool failingSender = false)
    {
        var store = new InMemoryReservationStore();
        var sender = new FakeNotificationSender(failingSender);
        return (new BookingService([R7, R8], store, sender), store, sender);
    }
```

#### Python

```python
class FakeNotificationSender:
    """A stand-in sender for tests: remembers what it would send, or fails on purpose."""

    def __init__(self, fail: bool = False) -> None:
        self.fail = fail
        self.sent: list[str] = []

    def send_confirmation(self, reservation) -> None:
        if self.fail:
            raise RuntimeError("The mail server is down.")
        self.sent.append(reservation.id)


def new_service(failing_sender: bool = False):
    """The only place that builds the service: after the refactoring it takes any sender."""
    store = InMemoryReservationStore()
    sender = FakeNotificationSender(failing_sender)
    return BookingService([R7, R8], store, sender), store, sender
```

#### Java

```java
// A stand-in sender for tests: remembers what it would send, or fails on purpose.
class FakeNotificationSender implements NotificationSender {
    private final boolean fail;
    final List<String> sent = new ArrayList<>();

    FakeNotificationSender(boolean fail) {
        this.fail = fail;
    }

    @Override
    public void sendConfirmation(Reservation reservation) {
        if (fail) throw new IllegalStateException("The mail server is down.");
        sent.add(reservation.id());
    }
}

    // The only place that builds the service: after the refactoring it takes any sender.
    record Fixture(BookingService service, InMemoryReservationStore store, FakeNotificationSender sender) {}

    static Fixture newService(boolean failingSender) {
        InMemoryReservationStore store = new InMemoryReservationStore();
        FakeNotificationSender sender = new FakeNotificationSender(failingSender);
        return new Fixture(new BookingService(List.of(R7, R8), store, sender), store, sender);
    }
```

#### Go

```go
// A stand-in sender for tests: remembers what it would send, or fails on purpose.
type fakeNotificationSender struct {
	fail bool
	sent []string
}

func (fake *fakeNotificationSender) SendConfirmation(reservation *Reservation) error {
	if fake.fail {
		return errors.New("the mail server is down")
	}
	fake.sent = append(fake.sent, reservation.ID)
	return nil
}

// The only place that builds the service: after the refactoring it takes any sender.
func newService(failingSender bool) (*BookingService, *InMemoryReservationStore, *fakeNotificationSender) {
	store := &InMemoryReservationStore{}
	sender := &fakeNotificationSender{fail: failingSender}
	return NewBookingService([]Room{r7, r8}, store, sender, 0), store, sender
}
```

#### JavaScript

```javascript
// A stand-in sender for tests: remembers what it would send, or fails on purpose.
class FakeNotificationSender {
  constructor(fail = false) {
    this.fail = fail;
    this.sent = [];
  }

  sendConfirmation(reservation) {
    if (this.fail) throw new Error('The mail server is down.');
    this.sent.push(reservation.id);
  }
}

// The only place that builds the service: after the refactoring it takes any sender.
function newService(failingSender = false) {
  const store = new InMemoryReservationStore();
  const sender = new FakeNotificationSender(failingSender);
  return { service: new BookingService([r7, r8], store, sender), store, sender };
}
```

#### Dart

```dart
// A stand-in sender for tests: remembers what it would send, or fails on purpose.
class FakeNotificationSender implements NotificationSender {
  FakeNotificationSender({this.fail = false});

  final bool fail;
  final List<String> sent = [];

  @override
  void sendConfirmation(Reservation reservation) {
    if (fail) throw StateError('The mail server is down.');
    sent.add(reservation.id);
  }
}

// The only place that builds the service: after the refactoring it takes any sender.
({BookingService service, InMemoryReservationStore store, FakeNotificationSender sender})
    newService({bool failingSender = false}) {
  final store = InMemoryReservationStore();
  final sender = FakeNotificationSender(fail: failingSender);
  return (
    service: BookingService([r7, r8], store, sender),
    store: store,
    sender: sender,
  );
}
```

### 7.2 · A new rule, R6: a failed email must not lose a booking

This is a new behaviour, so it is not a refactoring: write the failing test first, see it fail, then change Reserve.

### Diagram: The email fails after the booking is saved

R101 is saved in step 4, before the email is even tried. When the sender fails in step 6, Maya still has her room: BookingService answers Accepted and reports that the message failed, so the hotel can resend it.

Diagram as text:

Maya sends BookingService: 1. Reserve(Maya, R7, 12–14 Nov). BookingService sends InMemoryReservationStore: 2. HasOverlap(R7, 12–14 Nov). InMemoryReservationStore replies to BookingService: 3. false. BookingService sends InMemoryReservationStore: 4. Save(R101). BookingService sends FakeNotificationSender: 5. SendConfirmation(R101). FakeNotificationSender replies to BookingService: 6. error: the mail server is down. BookingService replies to Maya: 7. Accepted R101. R101 is saved in step 4, before the email is even tried. When the sender fails in step 6, Maya still has her room: BookingService answers Accepted and reports that the message failed, so the hotel can resend it.

**Step by step:** This is the test “a failed confirmation keeps the booking”. The fake sender fails on purpose.

1. Arrows 1–3: the usual start. Maya asks for R7, and the store says nobody has it on those nights.
2. Arrow 4: R101 is saved. From this moment Maya has a booking, whatever happens next.

   C#:
   
   ```text
   store.Save(reservation);
   ```
   
   Python:
   
   ```text
   self.store.save(reservation)
   ```
3. Arrow 5: BookingService asks the sender to confirm. In this test the sender is FakeNotificationSender(fail: true).

   C#:
   
   ```text
   var sender = new FakeNotificationSender(fail: true);
   ```
   
   Python:
   
   ```text
   sender = FakeNotificationSender(fail=True)
   ```
4. Arrow 6: the sender throws an error: “the mail server is down”.

   C#:
   
   ```text
   if (fail) throw new InvalidOperationException("The mail server is down.");
   ```
   
   Python:
   
   ```text
   if self.fail:
       raise RuntimeError("The mail server is down.")
   ```
5. Arrow 7: BookingService catches the error and still answers Accepted R101, with NotificationFailed = true. The saved booking is not undone and not hidden (rule R6).

   C#:
   
   ```text
   try
   {
       notifications.SendConfirmation(reservation);
   }
   catch (Exception)
   {
       return new("Accepted", reservation.Id, NotificationFailed: true);
   }
   ```
   
   Python:
   
   ```text
   try:
       self.notifications.send_confirmation(reservation)
   except Exception:
       return BookingResult("Accepted", reservation.id, notification_failed=True)
   ```

**Legend:**
- **Box with a dashed line below it (participant and lifeline).** One person or object that takes part. Time runs down its dashed line. In this diagram: Maya, BookingService, InMemoryReservationStore and the fake sender.
- **Solid arrow with a filled head (call).** A method call. In this diagram: Arrow 5: BookingService calls SendConfirmation(R101).
- **Dashed arrow with an open head (reply).** An answer going back, including an error. In this diagram: Arrow 6: the sender answers with an error instead of a normal return.
- **Numbers 1, 2, 3 … (order).** The order in which things happen, top to bottom. In this diagram: Save (4) comes before SendConfirmation (5), so a failed email cannot lose the booking.

### The test for R6, and the change in Reserve

### 7.2 · Test and change

#### C#

```csharp
    [Fact]
    public void A_failed_confirmation_keeps_the_booking()
    {
        var (service, store, _) = NewService(failingSender: true);
        var result = service.Reserve(Maya, R7, Nov(12, 14));
        Assert.Equal("Accepted R101", result.ToString());
        Assert.True(result.NotificationFailed);
        Assert.Equal(1, store.Count);
    }

// HotelBooking/BookingResult.cs
namespace HotelBooking;

public record BookingResult(string Outcome, string? ReservationId = null, bool NotificationFailed = false)
{
    public override string ToString() => ReservationId is null ? Outcome : $"{Outcome} {ReservationId}";
}
// HotelBooking/BookingService.cs
namespace HotelBooking;

// Depends only on the two interfaces; Program decides which store and sender to use.
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
            notifications.SendConfirmation(reservation);
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
    def test_a_failed_confirmation_keeps_the_booking(self):
        service, store, _ = new_service(failing_sender=True)
        result = service.reserve(MAYA, R7, nov(12, 14))
        self.assertEqual("Accepted R101", str(result))
        self.assertTrue(result.notification_failed)
        self.assertEqual(1, store.count())

# hotel_booking/booking.py
from __future__ import annotations

import time
from dataclasses import dataclass

from hotel_booking.domain import DateRange, Guest, Reservation, Room
from hotel_booking.notifications import NotificationSender
from hotel_booking.stores import ReservationStore


@dataclass(frozen=True)
class BookingResult:
    outcome: str
    reservation_id: str | None = None
    notification_failed: bool = False

    def __str__(self) -> str:
        return self.outcome if self.reservation_id is None else f"{self.outcome} {self.reservation_id}"


class BookingService:
    """Depends only on the two protocols; main.py decides which store and sender to use."""

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
            self.notifications.send_confirmation(reservation)
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
    @Test
    void a_failed_confirmation_keeps_the_booking() {
        Fixture fixture = newService(true);
        BookingResult result = fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertEquals("Accepted R101", result.toString());
        assertTrue(result.notificationFailed());
        assertEquals(1, fixture.store().count());
    }

// src/main/java/hotelbooking/BookingResult.java
package hotelbooking;

public record BookingResult(String outcome, String reservationId, boolean notificationFailed) {
    public BookingResult(String outcome) {
        this(outcome, null, false);
    }

    public BookingResult(String outcome, String reservationId) {
        this(outcome, reservationId, false);
    }

    @Override
    public String toString() {
        return reservationId == null ? outcome : outcome + " " + reservationId;
    }
}
// src/main/java/hotelbooking/BookingService.java
package hotelbooking;

import java.util.List;

// Depends only on the two interfaces; Main decides which store and sender to use.
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
            notifications.sendConfirmation(reservation);
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
func TestAFailedConfirmationKeepsTheBooking(t *testing.T) {
	service, store, _ := newService(true)
	result := service.Reserve(maya, r7, nov(12, 14))
	if result.String() != "Accepted R101" {
		t.Errorf("got %q, want %q", result.String(), "Accepted R101")
	}
	if !result.NotificationFailed {
		t.Error("the result should report the failed confirmation")
	}
	if store.Count() != 1 {
		t.Errorf("saved %d reservations, want 1", store.Count())
	}
}

// booking.go
package main

import (
	"fmt"
	"time"
)

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

// BookingService depends only on the two interfaces; main decides which store and sender to use.
type BookingService struct {
	rooms         []Room
	store         ReservationStore
	notifications NotificationSender
	slowStep      time.Duration
}

func NewBookingService(rooms []Room, store ReservationStore, notifications NotificationSender, slowStep time.Duration) *BookingService {
	return &BookingService{rooms: rooms, store: store, notifications: notifications, slowStep: slowStep}
}

func (service *BookingService) FindFreeRooms(stay DateRange) []Room {
	free := []Room{}
	for _, room := range service.rooms {
		if !service.store.HasOverlap(room, stay) {
			free = append(free, room)
		}
	}
	return free
}

func (service *BookingService) Reserve(guest Guest, room Room, stay DateRange) BookingResult {
	if !stay.IsValid() {
		return BookingResult{Outcome: "InvalidDates"}
	}
	if service.store.HasOverlap(room, stay) {
		return BookingResult{Outcome: "Unavailable"}
	}

	time.Sleep(service.slowStep) // zero normally; the race demo makes this step slow

	reservation := NewReservation(fmt.Sprintf("R%d", 101+service.store.Count()), guest, room, stay)
	service.store.Save(reservation)
	if err := service.notifications.SendConfirmation(reservation); err != nil {
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
test('a failed confirmation keeps the booking', async () => {
  const { service, store } = newService(true);
  const result = await service.reserve(maya, r7, nov(12, 14));
  assert.equal(String(result), 'Accepted R101');
  assert.equal(result.notificationFailed, true);
  assert.equal(store.count, 1);
});

// src/booking.js
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

// Depends only on the two contracts; main.js decides which store and sender to use.
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
      this.notifications.sendConfirmation(reservation);
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
  test('a failed confirmation keeps the booking', () async {
    final fixture = newService(failingSender: true);
    final result = await fixture.service.reserve(maya, r7, nov(12, 14));
    expect('$result', 'Accepted R101');
    expect(result.notificationFailed, isTrue);
    expect(fixture.store.count, 1);
  });

// lib/booking.dart
import 'domain.dart';
import 'notifications.dart';
import 'stores.dart';

class BookingResult {
  BookingResult(this.outcome, [this.reservationId, this.notificationFailed = false]);

  final String outcome;
  final String? reservationId;
  final bool notificationFailed;

  @override
  String toString() => reservationId == null ? outcome : '$outcome $reservationId';
}

// Depends only on the two interfaces; bin/main.dart decides which store and sender to use.
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
      notifications.sendConfirmation(reservation);
    } catch (failure) {
      // R6: the booking is already saved, so a failed message must not undo or hide it.
      return BookingResult('Accepted', reservation.id, true);
    }
    return BookingResult('Accepted', reservation.id);
  }

  bool cancel(String reservationId) => store.find(reservationId)?.cancel() ?? false;
}

```

### 7.3 · SMS confirmations without touching BookingService

Add one new class that keeps the notification promise, and one test. BookingService does not change at all: that is the O in SOLID. The class goes next to the email sender, in the same file or module.

### 7.3 · SMS sender and its test

#### C#

```csharp
// HotelBooking/SmsNotificationSender.cs
namespace HotelBooking;

// Added in Step 7 without changing BookingService. Simulated, like the email sender.
public class SmsNotificationSender : INotificationSender
{
    public int Sent { get; private set; }

    public void SendConfirmation(Reservation reservation) => Sent++;
}


    [Fact]
    public void Sms_confirmations_need_no_change_to_BookingService()
    {
        var sms = new SmsNotificationSender();
        var service = new BookingService([R7, R8], new InMemoryReservationStore(), sms);
        service.Reserve(Maya, R7, Nov(12, 14));
        Assert.Equal(1, sms.Sent);
    }
```

#### Python

```python
# hotel_booking/notifications.py
class SmsNotificationSender:
    """Added in Step 7 without changing BookingService. Simulated, like the email sender."""

    def __init__(self) -> None:
        self.sent = 0

    def send_confirmation(self, reservation: Reservation) -> None:
        self.sent += 1

    def test_sms_confirmations_need_no_change_to_booking_service(self):
        sms = SmsNotificationSender()
        service = BookingService([R7, R8], InMemoryReservationStore(), sms)
        service.reserve(MAYA, R7, nov(12, 14))
        self.assertEqual(1, sms.sent)
```

#### Java

```java
package hotelbooking;

// Added in Step 7 without changing BookingService. Simulated, like the email sender.
public class SmsNotificationSender implements NotificationSender {
    private int sent;

    public int sent() {
        return sent;
    }

    @Override
    public void sendConfirmation(Reservation reservation) {
        sent++;
    }
}


    @Test
    void sms_confirmations_need_no_change_to_BookingService() {
        SmsNotificationSender sms = new SmsNotificationSender();
        BookingService service = new BookingService(List.of(R7, R8), new InMemoryReservationStore(), sms);
        service.reserve(MAYA, R7, nov(12, 14));
        assertEquals(1, sms.sent());
    }
```

#### Go

```go
// SmsNotificationSender is added in Step 7 without changing BookingService. Simulated, like the email sender.
type SmsNotificationSender struct {
	Sent int
}

func (sms *SmsNotificationSender) SendConfirmation(reservation *Reservation) error {
	sms.Sent++
	return nil
}

func TestSmsConfirmationsNeedNoChangeToBookingService(t *testing.T) {
	sms := &SmsNotificationSender{}
	service := NewBookingService([]Room{r7, r8}, &InMemoryReservationStore{}, sms, 0)
	service.Reserve(maya, r7, nov(12, 14))
	if sms.Sent != 1 {
		t.Errorf("sent %d confirmations, want 1", sms.Sent)
	}
}
```

#### JavaScript

```javascript
// Added in Step 7 without changing BookingService. Simulated, like the email sender.
export class SmsNotificationSender {
  sent = 0;

  sendConfirmation(reservation) {
    this.sent += 1;
  }
}

test('SMS confirmations need no change to BookingService', async () => {
  const sms = new SmsNotificationSender();
  const service = new BookingService([r7, r8], new InMemoryReservationStore(), sms);
  await service.reserve(maya, r7, nov(12, 14));
  assert.equal(sms.sent, 1);
});
```

#### Dart

```dart
// Added in Step 7 without changing BookingService. Simulated, like the email sender.
class SmsNotificationSender implements NotificationSender {
  int sent = 0;

  @override
  void sendConfirmation(Reservation reservation) => sent++;
}

  test('SMS confirmations need no change to BookingService', () async {
    final sms = SmsNotificationSender();
    final service = BookingService([r7, r8], InMemoryReservationStore(), sms);
    await service.reserve(maya, r7, nov(12, 14));
    expect(sms.sent, 1);
  });
```

### Complete test file after Step 7

Use it to check your file: 8 tests in total.

### Complete test file

#### C#

```csharp
using HotelBooking;

namespace HotelBooking.Tests;

// A stand-in sender for tests: remembers what it would send, or fails on purpose.
public class FakeNotificationSender(bool fail = false) : INotificationSender
{
    public List<string> Sent { get; } = [];

    public void SendConfirmation(Reservation reservation)
    {
        if (fail) throw new InvalidOperationException("The mail server is down.");
        Sent.Add(reservation.Id);
    }
}

public class BookingServiceTests
{
    private static readonly Room R7 = new("R7", "Standard");
    private static readonly Room R8 = new("R8", "Deluxe");
    private static readonly Guest Maya = new("Maya", "maya@example.com");
    private static readonly Guest Omar = new("Omar", "omar@example.com");
    private static readonly Guest Nora = new("Nora", "nora@example.com");

    private static DateRange Nov(int checkIn, int checkOut) =>
        new(new DateOnly(2026, 11, checkIn), new DateOnly(2026, 11, checkOut));

    // The only place that builds the service: after the refactoring it takes any sender.
    private static (BookingService Service, InMemoryReservationStore Store, FakeNotificationSender Sender) NewService(bool failingSender = false)
    {
        var store = new InMemoryReservationStore();
        var sender = new FakeNotificationSender(failingSender);
        return (new BookingService([R7, R8], store, sender), store, sender);
    }

    [Fact]
    public void Maya_gets_R7_when_it_is_free()
    {
        var (service, _, _) = NewService();
        Assert.Equal("Accepted R101", service.Reserve(Maya, R7, Nov(12, 14)).ToString());
    }

    [Fact]
    public void Omar_is_refused_when_a_night_is_shared()
    {
        var (service, store, _) = NewService();
        service.Reserve(Maya, R7, Nov(12, 14));
        Assert.Equal("Unavailable", service.Reserve(Omar, R7, Nov(13, 15)).ToString());
        Assert.Equal(1, store.Count);
    }

    [Fact]
    public void Nora_can_arrive_on_the_day_Maya_leaves()
    {
        var (service, _, _) = NewService();
        service.Reserve(Maya, R7, Nov(12, 14));
        Assert.Equal("Accepted R102", service.Reserve(Nora, R7, Nov(14, 16)).ToString());
    }

    [Fact]
    public void Check_out_must_be_after_check_in()
    {
        var (service, store, _) = NewService();
        Assert.Equal("InvalidDates", service.Reserve(Omar, R8, Nov(15, 15)).ToString());
        Assert.Equal(0, store.Count);
    }

    [Fact]
    public void Cancelling_frees_the_room_again()
    {
        var (service, _, _) = NewService();
        service.Reserve(Maya, R7, Nov(12, 14));
        Assert.True(service.Cancel("R101"));
        Assert.Contains(R7, service.FindFreeRooms(Nov(12, 14)));
    }

    [Fact]
    public void One_confirmation_per_accepted_booking()
    {
        var (service, _, sender) = NewService();
        service.Reserve(Maya, R7, Nov(12, 14));
        service.Reserve(Omar, R7, Nov(13, 15));
        service.Reserve(Nora, R7, Nov(14, 16));
        Assert.Equal(new[] { "R101", "R102" }, sender.Sent);
    }

    [Fact]
    public void A_failed_confirmation_keeps_the_booking()
    {
        var (service, store, _) = NewService(failingSender: true);
        var result = service.Reserve(Maya, R7, Nov(12, 14));
        Assert.Equal("Accepted R101", result.ToString());
        Assert.True(result.NotificationFailed);
        Assert.Equal(1, store.Count);
    }

    [Fact]
    public void Sms_confirmations_need_no_change_to_BookingService()
    {
        var sms = new SmsNotificationSender();
        var service = new BookingService([R7, R8], new InMemoryReservationStore(), sms);
        service.Reserve(Maya, R7, Nov(12, 14));
        Assert.Equal(1, sms.Sent);
    }
}

```

#### Python

```python
"""Tests for the booking service after the P04 refactoring.

Run them from the hotel-booking folder:  python -m unittest -v
"""
import unittest
from datetime import date

from hotel_booking import BookingService, DateRange, Guest, InMemoryReservationStore, Room, SmsNotificationSender

R7 = Room("R7", "Standard")
R8 = Room("R8", "Deluxe")
MAYA = Guest("Maya", "maya@example.com")
OMAR = Guest("Omar", "omar@example.com")
NORA = Guest("Nora", "nora@example.com")


def nov(check_in: int, check_out: int) -> DateRange:
    return DateRange(date(2026, 11, check_in), date(2026, 11, check_out))


class FakeNotificationSender:
    """A stand-in sender for tests: remembers what it would send, or fails on purpose."""

    def __init__(self, fail: bool = False) -> None:
        self.fail = fail
        self.sent: list[str] = []

    def send_confirmation(self, reservation) -> None:
        if self.fail:
            raise RuntimeError("The mail server is down.")
        self.sent.append(reservation.id)


def new_service(failing_sender: bool = False):
    """The only place that builds the service: after the refactoring it takes any sender."""
    store = InMemoryReservationStore()
    sender = FakeNotificationSender(failing_sender)
    return BookingService([R7, R8], store, sender), store, sender


class BookingServiceTests(unittest.TestCase):
    def test_maya_gets_r7_when_it_is_free(self):
        service, _, _ = new_service()
        self.assertEqual("Accepted R101", str(service.reserve(MAYA, R7, nov(12, 14))))

    def test_omar_is_refused_when_a_night_is_shared(self):
        service, store, _ = new_service()
        service.reserve(MAYA, R7, nov(12, 14))
        self.assertEqual("Unavailable", str(service.reserve(OMAR, R7, nov(13, 15))))
        self.assertEqual(1, store.count())

    def test_nora_can_arrive_on_the_day_maya_leaves(self):
        service, _, _ = new_service()
        service.reserve(MAYA, R7, nov(12, 14))
        self.assertEqual("Accepted R102", str(service.reserve(NORA, R7, nov(14, 16))))

    def test_check_out_must_be_after_check_in(self):
        service, store, _ = new_service()
        self.assertEqual("InvalidDates", str(service.reserve(OMAR, R8, nov(15, 15))))
        self.assertEqual(0, store.count())

    def test_cancelling_frees_the_room_again(self):
        service, _, _ = new_service()
        service.reserve(MAYA, R7, nov(12, 14))
        self.assertTrue(service.cancel("R101"))
        self.assertIn(R7, service.find_free_rooms(nov(12, 14)))

    def test_one_confirmation_per_accepted_booking(self):
        service, _, sender = new_service()
        service.reserve(MAYA, R7, nov(12, 14))
        service.reserve(OMAR, R7, nov(13, 15))
        service.reserve(NORA, R7, nov(14, 16))
        self.assertEqual(["R101", "R102"], sender.sent)

    def test_a_failed_confirmation_keeps_the_booking(self):
        service, store, _ = new_service(failing_sender=True)
        result = service.reserve(MAYA, R7, nov(12, 14))
        self.assertEqual("Accepted R101", str(result))
        self.assertTrue(result.notification_failed)
        self.assertEqual(1, store.count())

    def test_sms_confirmations_need_no_change_to_booking_service(self):
        sms = SmsNotificationSender()
        service = BookingService([R7, R8], InMemoryReservationStore(), sms)
        service.reserve(MAYA, R7, nov(12, 14))
        self.assertEqual(1, sms.sent)


if __name__ == "__main__":
    unittest.main()

```

#### Java

```java
package hotelbooking;

// Tests for the booking service after the P04 refactoring.
// Run them from the hotel-booking folder:  mvn -q test
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

// A stand-in sender for tests: remembers what it would send, or fails on purpose.
class FakeNotificationSender implements NotificationSender {
    private final boolean fail;
    final List<String> sent = new ArrayList<>();

    FakeNotificationSender(boolean fail) {
        this.fail = fail;
    }

    @Override
    public void sendConfirmation(Reservation reservation) {
        if (fail) throw new IllegalStateException("The mail server is down.");
        sent.add(reservation.id());
    }
}

class BookingServiceTests {
    static final Room R7 = new Room("R7", "Standard");
    static final Room R8 = new Room("R8", "Deluxe");
    static final Guest MAYA = new Guest("Maya", "maya@example.com");
    static final Guest OMAR = new Guest("Omar", "omar@example.com");
    static final Guest NORA = new Guest("Nora", "nora@example.com");

    static DateRange nov(int checkIn, int checkOut) {
        return new DateRange(LocalDate.of(2026, 11, checkIn), LocalDate.of(2026, 11, checkOut));
    }

    // The only place that builds the service: after the refactoring it takes any sender.
    record Fixture(BookingService service, InMemoryReservationStore store, FakeNotificationSender sender) {}

    static Fixture newService(boolean failingSender) {
        InMemoryReservationStore store = new InMemoryReservationStore();
        FakeNotificationSender sender = new FakeNotificationSender(failingSender);
        return new Fixture(new BookingService(List.of(R7, R8), store, sender), store, sender);
    }

    @Test
    void maya_gets_R7_when_it_is_free() {
        Fixture fixture = newService(false);
        assertEquals("Accepted R101", fixture.service().reserve(MAYA, R7, nov(12, 14)).toString());
    }

    @Test
    void omar_is_refused_when_a_night_is_shared() {
        Fixture fixture = newService(false);
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertEquals("Unavailable", fixture.service().reserve(OMAR, R7, nov(13, 15)).toString());
        assertEquals(1, fixture.store().count());
    }

    @Test
    void nora_can_arrive_on_the_day_maya_leaves() {
        Fixture fixture = newService(false);
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertEquals("Accepted R102", fixture.service().reserve(NORA, R7, nov(14, 16)).toString());
    }

    @Test
    void check_out_must_be_after_check_in() {
        Fixture fixture = newService(false);
        assertEquals("InvalidDates", fixture.service().reserve(OMAR, R8, nov(15, 15)).toString());
        assertEquals(0, fixture.store().count());
    }

    @Test
    void cancelling_frees_the_room_again() {
        Fixture fixture = newService(false);
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertTrue(fixture.service().cancel("R101"));
        assertTrue(fixture.service().findFreeRooms(nov(12, 14)).contains(R7));
    }

    @Test
    void one_confirmation_per_accepted_booking() {
        Fixture fixture = newService(false);
        fixture.service().reserve(MAYA, R7, nov(12, 14));
        fixture.service().reserve(OMAR, R7, nov(13, 15));
        fixture.service().reserve(NORA, R7, nov(14, 16));
        assertEquals(List.of("R101", "R102"), fixture.sender().sent);
    }

    @Test
    void a_failed_confirmation_keeps_the_booking() {
        Fixture fixture = newService(true);
        BookingResult result = fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertEquals("Accepted R101", result.toString());
        assertTrue(result.notificationFailed());
        assertEquals(1, fixture.store().count());
    }

    @Test
    void sms_confirmations_need_no_change_to_BookingService() {
        SmsNotificationSender sms = new SmsNotificationSender();
        BookingService service = new BookingService(List.of(R7, R8), new InMemoryReservationStore(), sms);
        service.reserve(MAYA, R7, nov(12, 14));
        assertEquals(1, sms.sent());
    }
}

```

#### Go

```go
// Tests for the booking service after the P04 refactoring.
// Run them from the hotel-booking folder:  go test -v ./...
package main

import (
	"errors"
	"slices"
	"testing"
)

// A stand-in sender for tests: remembers what it would send, or fails on purpose.
type fakeNotificationSender struct {
	fail bool
	sent []string
}

func (fake *fakeNotificationSender) SendConfirmation(reservation *Reservation) error {
	if fake.fail {
		return errors.New("the mail server is down")
	}
	fake.sent = append(fake.sent, reservation.ID)
	return nil
}

// The only place that builds the service: after the refactoring it takes any sender.
func newService(failingSender bool) (*BookingService, *InMemoryReservationStore, *fakeNotificationSender) {
	store := &InMemoryReservationStore{}
	sender := &fakeNotificationSender{fail: failingSender}
	return NewBookingService([]Room{r7, r8}, store, sender, 0), store, sender
}

func TestMayaGetsR7WhenItIsFree(t *testing.T) {
	service, _, _ := newService(false)
	if got := service.Reserve(maya, r7, nov(12, 14)).String(); got != "Accepted R101" {
		t.Errorf("got %q, want %q", got, "Accepted R101")
	}
}

func TestOmarIsRefusedWhenANightIsShared(t *testing.T) {
	service, store, _ := newService(false)
	service.Reserve(maya, r7, nov(12, 14))
	if got := service.Reserve(omar, r7, nov(13, 15)).String(); got != "Unavailable" {
		t.Errorf("got %q, want %q", got, "Unavailable")
	}
	if store.Count() != 1 {
		t.Errorf("saved %d reservations, want 1", store.Count())
	}
}

func TestNoraCanArriveOnTheDayMayaLeaves(t *testing.T) {
	service, _, _ := newService(false)
	service.Reserve(maya, r7, nov(12, 14))
	if got := service.Reserve(nora, r7, nov(14, 16)).String(); got != "Accepted R102" {
		t.Errorf("got %q, want %q", got, "Accepted R102")
	}
}

func TestCheckOutMustBeAfterCheckIn(t *testing.T) {
	service, store, _ := newService(false)
	if got := service.Reserve(omar, r8, nov(15, 15)).String(); got != "InvalidDates" {
		t.Errorf("got %q, want %q", got, "InvalidDates")
	}
	if store.Count() != 0 {
		t.Errorf("saved %d reservations, want 0", store.Count())
	}
}

func TestCancellingFreesTheRoomAgain(t *testing.T) {
	service, _, _ := newService(false)
	service.Reserve(maya, r7, nov(12, 14))
	if !service.Cancel("R101") {
		t.Error("cancelling R101 should succeed")
	}
	if !slices.Contains(service.FindFreeRooms(nov(12, 14)), r7) {
		t.Error("R7 should be free again")
	}
}

func TestOneConfirmationPerAcceptedBooking(t *testing.T) {
	service, _, sender := newService(false)
	service.Reserve(maya, r7, nov(12, 14))
	service.Reserve(omar, r7, nov(13, 15))
	service.Reserve(nora, r7, nov(14, 16))
	if !slices.Equal(sender.sent, []string{"R101", "R102"}) {
		t.Errorf("sent %v, want [R101 R102]", sender.sent)
	}
}

func TestAFailedConfirmationKeepsTheBooking(t *testing.T) {
	service, store, _ := newService(true)
	result := service.Reserve(maya, r7, nov(12, 14))
	if result.String() != "Accepted R101" {
		t.Errorf("got %q, want %q", result.String(), "Accepted R101")
	}
	if !result.NotificationFailed {
		t.Error("the result should report the failed confirmation")
	}
	if store.Count() != 1 {
		t.Errorf("saved %d reservations, want 1", store.Count())
	}
}

func TestSmsConfirmationsNeedNoChangeToBookingService(t *testing.T) {
	sms := &SmsNotificationSender{}
	service := NewBookingService([]Room{r7, r8}, &InMemoryReservationStore{}, sms, 0)
	service.Reserve(maya, r7, nov(12, 14))
	if sms.Sent != 1 {
		t.Errorf("sent %d confirmations, want 1", sms.Sent)
	}
}

```

#### JavaScript

```javascript
// Tests for the booking service after the P04 refactoring.
// Run them from the hotel-booking folder:  node --test
import assert from 'node:assert/strict';
import test from 'node:test';

import { BookingService } from '../src/booking.js';
import { DateRange, Guest, Room } from '../src/domain.js';
import { SmsNotificationSender } from '../src/notifications.js';
import { InMemoryReservationStore } from '../src/stores.js';

const r7 = new Room('R7', 'Standard');
const r8 = new Room('R8', 'Deluxe');
const maya = new Guest('Maya', 'maya@example.com');
const omar = new Guest('Omar', 'omar@example.com');
const nora = new Guest('Nora', 'nora@example.com');

const nov = (checkIn, checkOut) =>
  new DateRange(new Date(Date.UTC(2026, 10, checkIn)), new Date(Date.UTC(2026, 10, checkOut)));

// A stand-in sender for tests: remembers what it would send, or fails on purpose.
class FakeNotificationSender {
  constructor(fail = false) {
    this.fail = fail;
    this.sent = [];
  }

  sendConfirmation(reservation) {
    if (this.fail) throw new Error('The mail server is down.');
    this.sent.push(reservation.id);
  }
}

// The only place that builds the service: after the refactoring it takes any sender.
function newService(failingSender = false) {
  const store = new InMemoryReservationStore();
  const sender = new FakeNotificationSender(failingSender);
  return { service: new BookingService([r7, r8], store, sender), store, sender };
}

test('Maya gets R7 when it is free', async () => {
  const { service } = newService();
  assert.equal(String(await service.reserve(maya, r7, nov(12, 14))), 'Accepted R101');
});

test('Omar is refused when a night is shared', async () => {
  const { service, store } = newService();
  await service.reserve(maya, r7, nov(12, 14));
  assert.equal(String(await service.reserve(omar, r7, nov(13, 15))), 'Unavailable');
  assert.equal(store.count, 1);
});

test('Nora can arrive on the day Maya leaves', async () => {
  const { service } = newService();
  await service.reserve(maya, r7, nov(12, 14));
  assert.equal(String(await service.reserve(nora, r7, nov(14, 16))), 'Accepted R102');
});

test('check-out must be after check-in', async () => {
  const { service, store } = newService();
  assert.equal(String(await service.reserve(omar, r8, nov(15, 15))), 'InvalidDates');
  assert.equal(store.count, 0);
});

test('cancelling frees the room again', async () => {
  const { service } = newService();
  await service.reserve(maya, r7, nov(12, 14));
  assert.equal(service.cancel('R101'), true);
  assert.ok(service.findFreeRooms(nov(12, 14)).some(room => room.equals(r7)));
});

test('one confirmation per accepted booking', async () => {
  const { service, sender } = newService();
  await service.reserve(maya, r7, nov(12, 14));
  await service.reserve(omar, r7, nov(13, 15));
  await service.reserve(nora, r7, nov(14, 16));
  assert.deepEqual(sender.sent, ['R101', 'R102']);
});

test('a failed confirmation keeps the booking', async () => {
  const { service, store } = newService(true);
  const result = await service.reserve(maya, r7, nov(12, 14));
  assert.equal(String(result), 'Accepted R101');
  assert.equal(result.notificationFailed, true);
  assert.equal(store.count, 1);
});

test('SMS confirmations need no change to BookingService', async () => {
  const sms = new SmsNotificationSender();
  const service = new BookingService([r7, r8], new InMemoryReservationStore(), sms);
  await service.reserve(maya, r7, nov(12, 14));
  assert.equal(sms.sent, 1);
});

```

#### Dart

```dart
// Tests for the booking service after the P04 refactoring.
// Run them from the hotel-booking folder:  dart test
import 'package:hotel_booking/hotel_booking.dart';
import 'package:test/test.dart';

final r7 = Room('R7', 'Standard');
final r8 = Room('R8', 'Deluxe');
final maya = Guest('Maya', 'maya@example.com');
final omar = Guest('Omar', 'omar@example.com');
final nora = Guest('Nora', 'nora@example.com');

DateRange nov(int checkIn, int checkOut) =>
    DateRange(DateTime.utc(2026, 11, checkIn), DateTime.utc(2026, 11, checkOut));

// A stand-in sender for tests: remembers what it would send, or fails on purpose.
class FakeNotificationSender implements NotificationSender {
  FakeNotificationSender({this.fail = false});

  final bool fail;
  final List<String> sent = [];

  @override
  void sendConfirmation(Reservation reservation) {
    if (fail) throw StateError('The mail server is down.');
    sent.add(reservation.id);
  }
}

// The only place that builds the service: after the refactoring it takes any sender.
({BookingService service, InMemoryReservationStore store, FakeNotificationSender sender})
    newService({bool failingSender = false}) {
  final store = InMemoryReservationStore();
  final sender = FakeNotificationSender(fail: failingSender);
  return (
    service: BookingService([r7, r8], store, sender),
    store: store,
    sender: sender,
  );
}

void main() {
  test('Maya gets R7 when it is free', () async {
    final fixture = newService();
    expect('${await fixture.service.reserve(maya, r7, nov(12, 14))}', 'Accepted R101');
  });

  test('Omar is refused when a night is shared', () async {
    final fixture = newService();
    await fixture.service.reserve(maya, r7, nov(12, 14));
    expect('${await fixture.service.reserve(omar, r7, nov(13, 15))}', 'Unavailable');
    expect(fixture.store.count, 1);
  });

  test('Nora can arrive on the day Maya leaves', () async {
    final fixture = newService();
    await fixture.service.reserve(maya, r7, nov(12, 14));
    expect('${await fixture.service.reserve(nora, r7, nov(14, 16))}', 'Accepted R102');
  });

  test('check-out must be after check-in', () async {
    final fixture = newService();
    expect('${await fixture.service.reserve(omar, r8, nov(15, 15))}', 'InvalidDates');
    expect(fixture.store.count, 0);
  });

  test('cancelling frees the room again', () async {
    final fixture = newService();
    await fixture.service.reserve(maya, r7, nov(12, 14));
    expect(fixture.service.cancel('R101'), isTrue);
    expect(fixture.service.findFreeRooms(nov(12, 14)), contains(r7));
  });

  test('one confirmation per accepted booking', () async {
    final fixture = newService();
    await fixture.service.reserve(maya, r7, nov(12, 14));
    await fixture.service.reserve(omar, r7, nov(13, 15));
    await fixture.service.reserve(nora, r7, nov(14, 16));
    expect(fixture.sender.sent, ['R101', 'R102']);
  });

  test('a failed confirmation keeps the booking', () async {
    final fixture = newService(failingSender: true);
    final result = await fixture.service.reserve(maya, r7, nov(12, 14));
    expect('$result', 'Accepted R101');
    expect(result.notificationFailed, isTrue);
    expect(fixture.store.count, 1);
  });

  test('SMS confirmations need no change to BookingService', () async {
    final sms = SmsNotificationSender();
    final service = BookingService([r7, r8], InMemoryReservationStore(), sms);
    await service.reserve(maya, r7, nov(12, 14));
    expect(sms.sent, 1);
  });
}

```

### Your turn · add the fake, rule R6 and SMS

1. Add FakeNotificationSender to the test file and change the helper as in 7.1. Change the sixth test to check the ids: R101 and R102 (see the complete test file). Run the tests: 6 green.
2. In docs/requirements.md, add ## R6 · Failed message: “If the confirmation cannot be sent, the booking stays saved and Accepted, and the failure is reported.”
3. Add the R6 test from 7.2 and run the tests. It fails (red): that is expected, because Reserve does not handle the error yet.
4. Change BookingResult and Reserve as in 7.2. Run the tests: 7 green.
5. Add SmsNotificationSender and its test from 7.3, and import it where your test file needs it. Run the tests: 8 green.
6. Check that you did not edit BookingService for the SMS step.

**When you finish, you have:** a fake sender in the tests, rule R6 in requirements.md and in the code, an SMS sender, and 8 green tests.
### Explanation

This is the payoff of the refactoring: testing a failure, adding a rule and adding a new sender each touched only the part they were about.



---

## 9. Step 8 · Check your project and see what comes next

Check your folder against the list, then see what the next practices do with this cleaner design.





### Completed example: the finished P04 folder

### hotel-booking folder

#### C#

```text
hotel-booking/
├── docs/
│   ├── requirements.md       R1–R5 from P03, plus R6 (new today)
│   ├── design-notes.md       P03 answers, plus today's P04 sections
│   └── diagrams.drawio       P03 pages, plus the new page "Classes - after"
├── HotelBooking.Tests/BookingServiceTests.cs   8 tests
├── HotelBooking.Tests/HotelBooking.Tests.csproj
├── HotelBooking/BookingResult.cs
├── HotelBooking/BookingService.cs
├── HotelBooking/DateRange.cs
├── HotelBooking/EmailNotificationSender.cs
├── HotelBooking/Guest.cs
├── HotelBooking/HotelBooking.csproj
├── HotelBooking/InMemoryReservationStore.cs
├── HotelBooking/INotificationSender.cs
├── HotelBooking/IReservationStore.cs
├── HotelBooking/Program.cs
├── HotelBooking/Reservation.cs
├── HotelBooking/ReservationStatus.cs
├── HotelBooking/Room.cs
└── HotelBooking/SmsNotificationSender.cs
```

#### Python

```text
hotel-booking/
├── docs/
│   ├── requirements.md       R1–R5 from P03, plus R6 (new today)
│   ├── design-notes.md       P03 answers, plus today's P04 sections
│   └── diagrams.drawio       P03 pages, plus the new page "Classes - after"
├── hotel_booking/__init__.py
├── hotel_booking/booking.py
├── hotel_booking/domain.py
├── hotel_booking/notifications.py
├── hotel_booking/stores.py
├── main.py
└── test_booking_service.py   8 tests
```

#### Java

```text
hotel-booking/
├── docs/
│   ├── requirements.md       R1–R5 from P03, plus R6 (new today)
│   ├── design-notes.md       P03 answers, plus today's P04 sections
│   └── diagrams.drawio       P03 pages, plus the new page "Classes - after"
├── pom.xml
├── src/main/java/hotelbooking/BookingResult.java
├── src/main/java/hotelbooking/BookingService.java
├── src/main/java/hotelbooking/DateRange.java
├── src/main/java/hotelbooking/EmailNotificationSender.java
├── src/main/java/hotelbooking/Guest.java
├── src/main/java/hotelbooking/InMemoryReservationStore.java
├── src/main/java/hotelbooking/Main.java
├── src/main/java/hotelbooking/NotificationSender.java
├── src/main/java/hotelbooking/Reservation.java
├── src/main/java/hotelbooking/ReservationStatus.java
├── src/main/java/hotelbooking/ReservationStore.java
├── src/main/java/hotelbooking/Room.java
├── src/main/java/hotelbooking/SmsNotificationSender.java
└── src/test/java/hotelbooking/BookingServiceTests.java   8 tests
```

#### Go

```text
hotel-booking/
├── docs/
│   ├── requirements.md       R1–R5 from P03, plus R6 (new today)
│   ├── design-notes.md       P03 answers, plus today's P04 sections
│   └── diagrams.drawio       P03 pages, plus the new page "Classes - after"
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
hotel-booking/
├── docs/
│   ├── requirements.md       R1–R5 from P03, plus R6 (new today)
│   ├── design-notes.md       P03 answers, plus today's P04 sections
│   └── diagrams.drawio       P03 pages, plus the new page "Classes - after"
├── main.js
├── package.json
├── src/booking.js
├── src/domain.js
├── src/notifications.js
├── src/stores.js
└── test/booking.test.js   8 tests
```

#### Dart

```text
hotel-booking/
├── docs/
│   ├── requirements.md       R1–R5 from P03, plus R6 (new today)
│   ├── design-notes.md       P03 answers, plus today's P04 sections
│   └── diagrams.drawio       P03 pages, plus the new page "Classes - after"
├── analysis_options.yaml
├── bin/main.dart
├── lib/booking.dart
├── lib/domain.dart
├── lib/hotel_booking.dart
├── lib/notifications.dart
├── lib/stores.dart
├── pubspec.yaml
└── test/booking_service_test.dart   8 tests
```

### Your P04 checklist

| File or page | It is finished when |
| --- | --- |
| Tests | 8 tests pass: the 6 from Step 2 plus R6 and SMS. |
| App code | BookingService’s constructor takes the storage promise and the notification promise; the classes live in separate files or modules instead of one big file. |
| Demo and race | The same nine demo lines as P03; the race still double-books. |
| docs/requirements.md | R6 is added. |
| docs/design-notes.md | P04 sections: Refactoring, What our code breaks, Proof. |
| diagrams.drawio · Classes - after | 2 interfaces, 5 classes, 2 “uses” arrows, 4 “implements” lines. |

### Where the hotel app goes next

| Practice | What changes in the hotel app |
| --- | --- |
| P05 · P06 | Split the hotel into business areas (rooms, reservations, payments, notifications) and turn them into modules, using the same interface idea. |
| P07 | Add a PostgreSQL store that implements IReservationStore, and put the app behind an HTTP API. BookingService does not change. |
| P09 | Fix the race in the database. |

### Your turn · check and hand over your project

1. Go through the checklist and open each file and draw.io page. Fix anything that is missing.
2. Run the tests, the demo and the race one last time.
3. Copy the whole hotel-booking folder to your partner’s laptop (for example as a ZIP in a message).
4. Keep the folder: P05 starts from it.

**When you finish, you have:** the complete P04 project on both partners’ laptops: same behaviour, cleaner design, 8 green tests.
### Explanation

The app does exactly what it did in P03, but each part now has one job, and BookingService depends only on promises. That is what lets the next practices add modules, a database and services without rewriting the booking rules.
