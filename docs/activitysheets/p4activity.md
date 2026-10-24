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
| 2 · Lock the behaviour | Write 6 tests that pass on today’s code | A test project (C#) or test file (Python) |
| 3 · SOLID | Learn the five principles and find what our code breaks | design-notes.md: what our code breaks |
| 4 · Target design | Draw the “after” class diagram with interfaces | draw.io page “Classes - after” |
| 5 · Refactor | Move step by step to interfaces and separate files | Refactored code |
| 6 · Prove nothing changed | Run the tests, the demo and the race again | design-notes.md: proof |
| 7 · Feel the benefit | Use a fake sender, add rule R6, add SMS without touching BookingService | 8 green tests, R6 in requirements.md |
| 8 · Check and hand over | Check every file against the list | The complete P04 folder |

### How you work today

Work in pairs on one laptop, in the same hotel-booking folder you finished in P03. Swap who types at every new step. Keep the same language as in P03: C# (.NET 10 SDK) or Python (3.10 or newer).

C# tests use xUnit: the first “dotnet test” downloads its packages, so you need internet once. Python tests use the built-in unittest, so there is nothing to install.

If you missed P03: open the P03 lesson page, download “Runnable code · C# and Python”, and put its HotelBooking folder (C#) or hotel_booking.py (Python) into a new hotel-booking folder. The “Runnable code” link on this page has the finished P04 project if you get stuck.

### Completed example: your folder at the end of P04

### hotel-booking folder

```text
hotel-booking/
├── docs/
│   ├── requirements.md       R1–R5 from P03, plus R6 (new today)
│   ├── design-notes.md       P03 answers, plus today's P04 sections
│   └── diagrams.drawio       P03 pages, plus the new page "Classes - after"
├── HotelBooking/             C#: one file per class; Program.cs wires them together
├── HotelBooking.Tests/       C#: BookingServiceTests.cs with 8 tests
├── hotel_booking/            Python: domain.py, stores.py, notifications.py, booking.py
├── main.py                   Python: wires everything together and runs the demo
└── test_booking_service.py   Python: 8 tests
```

### Your turn · check that your P03 app still runs

1. Open your hotel-booking folder in your editor.
2. In the terminal, run the demo: dotnet run --project HotelBooking (C#) or python hotel_booking.py (Python). You should see the nine lines from P03.
3. Run the race demo too: add -- --race (C#) or --race (Python). You should see “Double booking!”.
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

### C# only: make the classes public

The C# test project is a separate program and can only see classes marked public. Add the word public in front of every class, record and enum in Program.cs. Python needs no change.

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

### What changes in Program.cs

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

### Your turn · write the six tests and run them

1. C#: add public in front of each class, record and enum in Program.cs, as shown above. Python: skip this step.
2. Create the test project (C#) or the test file (Python) with the commands above.
3. Type the first test from the completed example, plus the start of the file (the rooms, guests, Nov() and NewService()). Run the tests: 1 should pass.
4. Add the other five tests from “The complete test file”. Typist types, partner reads; swap after three tests.
5. Run the tests again. You should see 6 passed (C#) or “Ran 6 tests … OK” (Python).
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
| Guest, Room, DateRange, Reservation, ReservationStatus | Unchanged, each in its own file (C#) or in domain.py (Python) | One job per file (S). |
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

### 5.4 · The data classes get their own files

C#: one file per type, each starting with “namespace HotelBooking;”. Python: the data classes go into domain.py, and __init__.py lets other files import everything from hotel_booking.

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

### 5.5 · Program wires everything together

Program.cs (C#) or main.py (Python) is now the only place that chooses the real store and sender and passes them into BookingService.

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

### 5.6 · The tests use the new names

Only the helper that builds the service changes. C#: also add “using HotelBooking;” at the top of the test file. Python: import the new names from hotel_booking.

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

### Your turn · refactor in six moves

After every move, run the tests (dotnet test HotelBooking.Tests or python -m unittest -v). If they turn red, fix that move before the next one.

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

1. C#: create HotelBooking/INotificationSender.cs and HotelBooking/IReservationStore.cs from 5.1 and 5.2. Python: create the folder hotel_booking/ next to hotel_booking.py (leave the old file for now).
2. C#: in Program.cs, click on EmailSender, press F2 and rename it to EmailNotificationSender, then add “: INotificationSender”. Do the same for ReservationStore → InMemoryReservationStore : IReservationStore. Python: create hotel_booking/notifications.py and stores.py from 5.1 and 5.2.
3. Change BookingService as in 5.3 (C#: still in Program.cs; Python: create hotel_booking/booking.py), and update the test helper as in 5.6. Run the tests: C# should show 6 passed.
4. C#: move each class out of Program.cs into its own file under HotelBooking/ (5.1–5.4), with “namespace HotelBooking;” at the top. Python: create hotel_booking/domain.py and __init__.py (5.4).
5. Make Program.cs (C#) or a new main.py (Python) look like 5.5. C#: add “using HotelBooking;” to the test file. Python: delete hotel_booking.py now that the folder works.
6. Run the tests one more time: 6 passed (C#) or “Ran 6 tests … OK” (Python).

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

### 7.3 · SMS confirmations without touching BookingService

Add one new class that keeps the INotificationSender promise, and one test. BookingService does not change at all: that is the O in SOLID. Python: add the class at the end of notifications.py and export it in __init__.py.

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

### Your turn · add the fake, rule R6 and SMS

1. Add FakeNotificationSender to the test file and change the helper as in 7.1. Change the sixth test to check the ids: R101 and R102 (see the complete test file). Run the tests: 6 green.
2. In docs/requirements.md, add ## R6 · Failed message: “If the confirmation cannot be sent, the booking stays saved and Accepted, and the failure is reported.”
3. Add the R6 test from 7.2 and run the tests. It fails (red): that is expected, because Reserve does not handle the error yet.
4. Change BookingResult and Reserve as in 7.2. Run the tests: 7 green.
5. Add SmsNotificationSender and its test from 7.3 (Python: also add it to the import line in __init__.py and in the test file). Run the tests: 8 green.
6. Check that you did not edit BookingService for the SMS step.

**When you finish, you have:** a fake sender in the tests, rule R6 in requirements.md and in the code, an SMS sender, and 8 green tests.
### Explanation

This is the payoff of the refactoring: testing a failure, adding a rule and adding a new sender each touched only the part they were about.



---

## 9. Step 8 · Check your project and see what comes next

Check your folder against the list, then see what the next practices do with this cleaner design.





### Completed example: the finished P04 folder

### hotel-booking folder

```text
hotel-booking/
├── docs/
│   ├── requirements.md       R1–R5 from P03, plus R6 (new today)
│   ├── design-notes.md       P03 answers, plus today's P04 sections
│   └── diagrams.drawio       P03 pages, plus the new page "Classes - after"
├── HotelBooking/             C#: one file per class; Program.cs wires them together
├── HotelBooking.Tests/       C#: BookingServiceTests.cs with 8 tests
├── hotel_booking/            Python: domain.py, stores.py, notifications.py, booking.py
├── main.py                   Python: wires everything together and runs the demo
└── test_booking_service.py   Python: 8 tests
```

### Your P04 checklist

| File or page | It is finished when |
| --- | --- |
| Tests | 8 tests pass: the 6 from Step 2 plus R6 and SMS. |
| App code | BookingService’s constructor takes IReservationStore and INotificationSender; every class is in its own file (C#) or module (Python). |
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
