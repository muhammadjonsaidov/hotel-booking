# P05 · Activity sheet

## 1. P05 · The hotel app works. Now decide which business area owns what.

In P04 you made the code easy to change. Today you write down the hotel’s business areas, so that P06 can turn each one into a module.





### Where we are

Your hotel-booking folder has the P04 app: BookingService depends on two interfaces, and 8 tests pass. In L03 you saw the four business areas of the hotel: Rooms, Reservations, Payments and Notifications. Today you write them down in detail, in your project folder.

**Why split the hotel into contexts before touching code?** In short: A context is one business area with its own words and rules; writing them down first turns the P06 code move into a copying job.

1. The manager asks for a 30% deposit. Today you would open BookingService.
2. With four contexts written down, you look up who owns money rules: Payments.
3. In P06, Payments becomes its own module, so the deposit code goes there and BookingService stays as it is.
4. Today you only write the design: which words, rules and messages belong to which context.

### Why that matters now

| What the hotel wants | What you would edit today |
| --- | --- |
| “Take a 30% deposit when a guest books.” | BookingService |
| “R8 is out of service for repairs next week.” | BookingService, or every place that lists rooms |
| “Deluxe rooms cost more per night.” | BookingService |
| “Send an SMS as well as the email.” | Only a new sender class: P04 already solved this one |

### Today’s plan

| Step | What you do | What you add to your project folder |
| --- | --- | --- |
| 1 · Stakeholders and S1–S6 | Name who cares about the app; write six measurable quality scenarios | docs/quality-scenarios.md |
| 2 · The hotel’s words | List every word from R1–R6 and the code: Room, Stay, Deposit, Confirmed, Sent … | docs/contexts.md: “Words” |
| 3 · Four contexts | Sort the words into Rooms, Reservations, Payments, Notifications; write each context’s job | docs/contexts.md: one section per context |
| 4 · Same word, two meanings | Find words that mean different things in different contexts: Room, Cancel, Confirmed/Paid/Sent | docs/contexts.md: “Same word, different meaning” |
| 5 · Who owns each rule | Give each of eight rule cards one owner, with a reason; the booking rule is one of them | docs/contexts.md: “Rule owners” |
| 6 · Context map | Draw the four contexts and the arrows between them | draw.io page “Context map” |
| 7 · What crosses each border | For every arrow: the question asked or the event announced, with its fields | docs/contexts.md: “Between contexts” |
| 8 · Check and hand over | Check every file against the list; P06 starts from this folder | The complete P05 folder |

### How you work today

Work in pairs on one laptop, in the hotel-booking folder you finished in P04. Swap who types at every new step.

There is no coding and nothing to install today. You write two Markdown files in your editor (VS Code or similar) and draw one page in draw.io (app.diagrams.net or the desktop app). The code stays exactly as it is.

If you missed P04: open the P04 lesson page, choose your language under “Runnable code”, download that ZIP and extract it as your hotel-booking folder. The ZIP has the code but no docs folder: create an empty docs folder now, and create docs/diagrams.drawio in draw.io when Step 6 asks for it.

### Completed example: your folder at the end of P05

### hotel-booking folder

```text
hotel-booking/
├── docs/
│   ├── requirements.md         R1–R6, unchanged
│   ├── design-notes.md         P03 and P04 notes, unchanged
│   ├── quality-scenarios.md    NEW: stakeholders and S1–S6, each with a measure
│   ├── contexts.md             NEW: four contexts, their words, their rules, who owns each rule
│   └── diagrams.drawio         NEW page: "Context map"
└── your P04 code                   not changed in P05
```

### Run the P04 tests once

This is your starting point. The same command as in P04; the runner should report 8 passing tests.

### Run the tests

#### C#

```text
dotnet test HotelBooking.Tests
```

#### Python

```text
python -m unittest -v
```

#### Java

```text
mvn -q test
```

#### Go

```text
go test -v ./...
```

#### JavaScript

```text
node --test
```

#### Dart

```text
dart pub get
dart test
```

### Your turn · check your starting point

1. Open your hotel-booking folder in your editor.
2. Run the tests with the command for your language above. You should see 8 passing tests.
3. Open the docs folder. Check that requirements.md, design-notes.md and diagrams.drawio are there (from the P04 ZIP: only an empty docs folder). If you were at L03, docs/l03-notes.md is there too: keep it open, today starts from its first answer.
4. Create two empty files in the docs folder: quality-scenarios.md and contexts.md.

**When you finish, you have:** the P04 app with 8 green tests, and two empty files ready for today: docs/quality-scenarios.md and docs/contexts.md.
### Explanation

Today is design only. Every decision you write down today becomes code in P06: each context becomes a module, each rule owner decides which module the rule goes into, and each border becomes a contract.



---

## 2. Step 1 · Who cares about the app, and six quality scenarios

Before splitting the app, write down who cares about it and what “good” means, with a number for each. You will use these scenarios to judge every design from now on.





### Stakeholders: who cares about the hotel app

| Who | What they care about |
| --- | --- |
| Guest | Finds a free room quickly; never loses a booking. |
| Receptionist | Never promises one room twice; can cancel at the desk. |
| Hotel manager (the domain expert) | Knows the rules; wants deposits and SMS soon. |
| Development team | Can change one area without breaking the others. |
| Email/SMS and payment providers | Outside systems that can be slow or down. |

### The six quality scenarios

|  | Situation | Required result and measure | Checked in |
| --- | --- | --- | --- |
| S1 · Performance | 200 guests search free rooms for 12–14 Nov within one minute | Correct free rooms; p95 response ≤ 1 second | P08 (cache), P23 (load test) |
| S2 · Data integrity | 100 pairs of guests book the same room and nights at the same moment | Exactly one Confirmed reservation per pair; 0 double bookings | P09 |
| S3 · Availability | The email provider is down for 10 minutes | Every valid booking is still Accepted and saved; failed messages are reported | P04 (R6 test, done), P10 |
| S4 · Security | A guest tries 100 times to read another guest’s reservation | 0 reservations shown to the wrong guest | P21 |
| S5 · Modifiability | The hotel adds SMS confirmations | BookingService is not edited; at most 2 files change | P04 (SMS sender, done) |
| S6 · Sustainability | The same 1,000 searches, before and after a change | At least 20% fewer watt-hours per 1,000 completed searches | P08, P24 |

**What “p95 response ≤ 1 second” means** In short: 95 out of every 100 searches must answer within 1 second; the slowest 5 may take longer. An average hides slow requests: 99 fast searches and one 30-second search still average under 1 second. The 95th percentile (p95) says how slow the slow ones are allowed to be.

1. 200 guests search free rooms for 12–14 November.
2. You sort the 200 response times from fastest to slowest.
3. The 190th time (95% of 200) is the p95. Here it is 0.8 seconds.
4. 0.8 ≤ 1 second, so S1 is met. If it were 1.4 seconds, S1 would fail even if the average were 0.3 seconds.

**The six parts of a quality scenario** In short: A quality scenario turns a wish like “reliable” into a situation you can test, with a number. The six parts are: who or what causes it (source), what happens (stimulus), when (environment), which part of the app is hit (artifact), what the app must do (response), and the number that proves it (measure).

1. Source: the email provider. Stimulus: it stops answering.
2. Environment: for 10 minutes, during normal bookings.
3. Artifact: BookingService.Reserve and the notification sender.
4. Response: every valid booking is still Accepted and saved; the failed message is reported.
5. Measure: 0 lost bookings; every failure counted. The P04 test below already proves it for one booking.

C#:

```text
    [Fact]
    public void A_failed_confirmation_keeps_the_booking()
    {
        var (service, store, _) = NewService(failingSender: true);
        var result = service.Reserve(Maya, R7, Nov(12, 14));
        Assert.Equal("Accepted R101", result.ToString());
        Assert.True(result.NotificationFailed);
        Assert.Equal(1, store.Count);
    }
```

Python:

```text
    def test_a_failed_confirmation_keeps_the_booking(self):
        service, store, _ = new_service(failing_sender=True)
        result = service.reserve(MAYA, R7, nov(12, 14))
        self.assertEqual("Accepted R101", str(result))
        self.assertTrue(result.notification_failed)
        self.assertEqual(1, store.count())
```

Java:

```text
    @Test
    void a_failed_confirmation_keeps_the_booking() {
        Fixture fixture = newService(true);
        BookingResult result = fixture.service().reserve(MAYA, R7, nov(12, 14));
        assertEquals("Accepted R101", result.toString());
        assertTrue(result.notificationFailed());
        assertEquals(1, fixture.store().count());
    }
```

Go:

```text
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
```

JavaScript:

```text
test('a failed confirmation keeps the booking', async () => {
  const { service, store } = newService(true);
  const result = await service.reserve(maya, r7, nov(12, 14));
  assert.equal(String(result), 'Accepted R101');
  assert.equal(result.notificationFailed, true);
  assert.equal(store.count, 1);
});
```

Dart:

```text
  test('a failed confirmation keeps the booking', () async {
    final fixture = newService(failingSender: true);
    final result = await fixture.service.reserve(maya, r7, nov(12, 14));
    expect('$result', 'Accepted R101');
    expect(result.notificationFailed, isTrue);
    expect(fixture.store.count, 1);
  });
```

**S6: measuring energy per piece of useful work** In short: Compare the watt-hours used for the same useful work (1,000 completed searches), before and after a change. Energy is measured per completed search, not per hour: a change that answers fewer searches must not look “greener” just because it did less work.

1. Before the change: 1,000 completed searches use 50 Wh on the server.
2. P08 adds a cache, so most searches no longer read the database.
3. After the change: the same 1,000 completed searches use 38 Wh.
4. 38 is 24% less than 50, so S6 (at least 20% fewer watt-hours) is met. P24 measures it for real.

### The seven lines of every scenario

| Line | Question it answers | In S3 |
| --- | --- | --- |
| Source | Who or what causes it? | The email provider |
| Stimulus | What happens? | It stops answering |
| Environment | When, under which conditions? | For 10 minutes, during normal bookings |
| Artifact | Which part of our app is hit? | BookingService.Reserve and the notification sender |
| Response | What must the app do? | Keep every valid booking Accepted and saved; report the failed message |
| Measure | Which number proves it? | 0 lost bookings; every failed message counted |
| Checked in | Which practice checks it? | P04 (R6 test, done) and P10 |

### Completed example: the start of docs/quality-scenarios.md

### docs/quality-scenarios.md

```text
# Quality scenarios

## Stakeholders

| Who | What they care about |
|---|---|
| Guest | Finds a free room quickly; never loses a booking |
| Receptionist | Never promises one room twice; can cancel at the desk |
| Hotel manager (domain expert) | Knows the rules; wants deposits and SMS soon |
| Development team | Can change one area without breaking the others |
| Email/SMS and payment providers | Outside systems that can be slow or down |

## S3 · Availability: a failed message keeps the booking

| Part | S3 |
|---|---|
| Source | The email provider |
| Stimulus | It stops answering |
| Environment | For 10 minutes, during normal bookings |
| Artifact | BookingService.Reserve and the notification sender |
| Response | Every valid booking is still Accepted and saved; the failed message is reported |
| Measure | 0 lost bookings; every failed message is counted |
| Checked in | P04 (R6 test, done) and P10 |
```

### Your turn · write the six scenarios

**How to write a table in a Markdown file** In short: Each row is one line; the cells are separated by | and the second line is made of dashes. VS Code shows the formatted table when you open the preview (Ctrl+Shift+V). The columns do not have to line up in the file.

1. Line 1 is the header: | Word | Meaning here |
2. Line 2 tells Markdown it is a table: |---|---|
3. Every next line is one row: | Stay | Check-in to check-out, half-open |
4. Leave an empty line before and after the table.

```text
| Word | Meaning here |
|---|---|
| Stay | Check-in to check-out, half-open: 12–14 Nov = the nights of the 12th and 13th |
| Confirmed | The room is promised to this guest for these nights |
```

1. Open docs/quality-scenarios.md and copy the completed example: the heading, the Stakeholders table and S3.
2. Below S3, add ## S1 · Performance with the same seven-line table. Split the situation from the “six scenarios” table into Source (200 guests), Stimulus (they search free rooms for 12–14 Nov) and Environment (within one minute); Artifact is the free-room search in BookingService.
3. Add S2, S4, S5 and S6 the same way, one section each, taking the situation, the result and the measure from the “six scenarios” table.
4. Open the Markdown preview (Ctrl+Shift+V in VS Code) and check that all six tables show as tables.
5. Check each Measure line: it must contain a number (≤ 1 second, 0, 2 files, 20%). If one has no number, add it.

**When you finish, you have:** docs/quality-scenarios.md with the stakeholders and six scenarios S1–S6, each with seven lines and a measured result.
### Explanation

A quality scenario turns a wish into a test you could run. S3 and S5 are already proven by your P04 tests; the others are targets that later practices measure, as the “Checked in” line says.



---

## 3. Step 2 · The hotel’s words

Write down every word the hotel uses, what it means here, and where it already lives in your code. These words are the raw material for the four contexts.





### Why the words come first

The manager says “stay”; your code says DateRange. If everybody uses the same words with the same meaning, a request from the hotel points straight at the right code. The shared word list is called the ubiquitous language.

**One language for the hotel staff, the documents and the code** In short: Use the manager’s words everywhere: in conversation, in docs/contexts.md and as class and method names.

1. The manager says “stay”: check-in to check-out, and the guest leaves on the check-out morning.
2. The code calls it DateRange and checks overlap half-open: 12–14 November and 14–16 November share no night.
3. You write the word “Stay” in the Words table with that meaning and the code name DateRange.
4. Now a new developer who reads “stay” in a ticket knows exactly which class and which rule it means.

C#:

```text
// HotelBooking/DateRange.cs
public readonly record struct DateRange(DateOnly CheckIn, DateOnly CheckOut)
{
    public bool IsValid() => CheckOut > CheckIn;

    // Two stays overlap when they share at least one night.
    public bool Overlaps(DateRange other) =>
        CheckIn < other.CheckOut && other.CheckIn < CheckOut;
}
```

Python:

```text
# hotel_booking/domain.py
@dataclass(frozen=True)
class DateRange:
    check_in: date
    check_out: date

    def is_valid(self) -> bool:
        return self.check_out > self.check_in

    # Two stays overlap when they share at least one night.
    def overlaps(self, other: DateRange) -> bool:
        return self.check_in < other.check_out and other.check_in < self.check_out
```

Java:

```text
// src/main/java/hotelbooking/DateRange.java
public record DateRange(LocalDate checkIn, LocalDate checkOut) {
    public boolean isValid() {
        return checkOut.isAfter(checkIn);
    }

    // Two stays overlap when they share at least one night.
    public boolean overlaps(DateRange other) {
        return checkIn.isBefore(other.checkOut) && other.checkIn.isBefore(checkOut);
    }
}
```

Go:

```text
// domain.go
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
```

JavaScript:

```text
// src/domain.js
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
```

Dart:

```text
// lib/domain.dart
class DateRange {
  DateRange(this.checkIn, this.checkOut);

  final DateTime checkIn;
  final DateTime checkOut;

  bool isValid() => checkOut.isAfter(checkIn);

  // Two stays overlap when they share at least one night.
  bool overlaps(DateRange other) =>
      checkIn.isBefore(other.checkOut) && other.checkIn.isBefore(checkOut);
}
```

### Where the words already live in your P04 code

Open your language’s tab. The names on the right are the ones in your files; the words at the bottom do not exist in the code yet.

### Hotel words in your code

#### C#

```text
Hotel word              Name in your P04 code
Guest                   Guest
Room                    Room (Number, Type)
Stay                    DateRange (CheckIn, CheckOut)
Reservation             Reservation (Id, Guest, Room, Stay, Status)
Confirmed / Cancelled   ReservationStatus.Confirmed / .Cancelled
Book                    BookingService.Reserve(guest, room, stay)
Cancel a reservation    BookingService.Cancel(id)
Free rooms              BookingService.FindFreeRooms(stay)
Confirmation message    INotificationSender.SendConfirmation(reservation)

Not in the code yet:
Nightly price           (comes with its practice)
Out of service          (comes with its practice)
Deposit                 (comes with its practice)
Paid / Declined         (comes with its practice)
Sent / Failed           (comes with its practice)
```

#### Python

```text
Hotel word              Name in your P04 code
Guest                   Guest
Room                    Room (number, type)
Stay                    DateRange (check_in, check_out)
Reservation             Reservation (id, guest, room, stay, status)
Confirmed / Cancelled   ReservationStatus.CONFIRMED / .CANCELLED
Book                    BookingService.reserve(guest, room, stay)
Cancel a reservation    BookingService.cancel(reservation_id)
Free rooms              BookingService.find_free_rooms(stay)
Confirmation message    NotificationSender.send_confirmation(reservation)

Not in the code yet:
Nightly price           (comes with its practice)
Out of service          (comes with its practice)
Deposit                 (comes with its practice)
Paid / Declined         (comes with its practice)
Sent / Failed           (comes with its practice)
```

#### Java

```text
Hotel word              Name in your P04 code
Guest                   Guest
Room                    Room (number, type)
Stay                    DateRange (checkIn, checkOut)
Reservation             Reservation (id, guest, room, stay, status)
Confirmed / Cancelled   ReservationStatus.CONFIRMED / .CANCELLED
Book                    BookingService.reserve(guest, room, stay)
Cancel a reservation    BookingService.cancel(reservationId)
Free rooms              BookingService.findFreeRooms(stay)
Confirmation message    NotificationSender.sendConfirmation(reservation)

Not in the code yet:
Nightly price           (comes with its practice)
Out of service          (comes with its practice)
Deposit                 (comes with its practice)
Paid / Declined         (comes with its practice)
Sent / Failed           (comes with its practice)
```

#### Go

```text
Hotel word              Name in your P04 code
Guest                   Guest
Room                    Room (Number, Type)
Stay                    DateRange (CheckIn, CheckOut)
Reservation             Reservation (ID, Guest, Room, Stay, Status)
Confirmed / Cancelled   Confirmed / Cancelled (ReservationStatus)
Book                    BookingService.Reserve(guest, room, stay)
Cancel a reservation    BookingService.Cancel(reservationID)
Free rooms              BookingService.FindFreeRooms(stay)
Confirmation message    NotificationSender.SendConfirmation(reservation)

Not in the code yet:
Nightly price           (comes with its practice)
Out of service          (comes with its practice)
Deposit                 (comes with its practice)
Paid / Declined         (comes with its practice)
Sent / Failed           (comes with its practice)
```

#### JavaScript

```text
Hotel word              Name in your P04 code
Guest                   Guest
Room                    Room (number, type)
Stay                    DateRange (checkIn, checkOut)
Reservation             Reservation (id, guest, room, stay, status)
Confirmed / Cancelled   ReservationStatus.Confirmed / .Cancelled
Book                    BookingService.reserve(guest, room, stay)
Cancel a reservation    BookingService.cancel(reservationId)
Free rooms              BookingService.findFreeRooms(stay)
Confirmation message    sendConfirmation(reservation) on the sender

Not in the code yet:
Nightly price           (comes with its practice)
Out of service          (comes with its practice)
Deposit                 (comes with its practice)
Paid / Declined         (comes with its practice)
Sent / Failed           (comes with its practice)
```

#### Dart

```text
Hotel word              Name in your P04 code
Guest                   Guest
Room                    Room (number, type)
Stay                    DateRange (checkIn, checkOut)
Reservation             Reservation (id, guest, room, stay, status)
Confirmed / Cancelled   ReservationStatus.confirmed / .cancelled
Book                    BookingService.reserve(guest, room, stay)
Cancel a reservation    BookingService.cancel(reservationId)
Free rooms              BookingService.findFreeRooms(stay)
Confirmation message    NotificationSender.sendConfirmation(reservation)

Not in the code yet:
Nightly price           (comes with its practice)
Out of service          (comes with its practice)
Deposit                 (comes with its practice)
Paid / Declined         (comes with its practice)
Sent / Failed           (comes with its practice)
```

### Completed example: the start of docs/contexts.md

Three rows are written for you. The code names are the C# ones; use the names from your language’s tab above.

### docs/contexts.md

```text
# Hotel contexts

## Words

| Word | Meaning here | Name in our code |
|---|---|---|
| Stay | Check-in to check-out, half-open: 12–14 Nov = the nights of the 12th and 13th | DateRange |
| Reservation | Guest + room + stay + status, with an id such as R101 | Reservation |
| Confirmed | The room is promised to this guest for these nights | ReservationStatus.Confirmed |
```

### Your turn · write the Words table

**How to write a table in a Markdown file** In short: Each row is one line; the cells are separated by | and the second line is made of dashes. VS Code shows the formatted table when you open the preview (Ctrl+Shift+V). The columns do not have to line up in the file.

1. Line 1 is the header: | Word | Meaning here |
2. Line 2 tells Markdown it is a table: |---|---|
3. Every next line is one row: | Stay | Check-in to check-out, half-open |
4. Leave an empty line before and after the table.

```text
| Word | Meaning here |
|---|---|
| Stay | Check-in to check-out, half-open: 12–14 Nov = the nights of the 12th and 13th |
| Confirmed | The room is promised to this guest for these nights |
```

1. Open docs/contexts.md and copy the completed example: the heading # Hotel contexts and the Words table with its three rows. Change the code names to your language’s names.
2. Add a row for each of these words: Guest, Room, Room type, Nightly price, Out of service, Cancelled, Book, Cancel, Free rooms, Deposit, Paid, Declined, Confirmation message, Sent, Failed.
3. For each word, write its meaning in one short sentence, with a hotel example where you can (“Deposit: 30% of the stay’s price, taken when the guest books”).
4. Fill “Name in our code” from your language’s tab. For words in the “Not in the code yet” list, write: not in code yet.
5. Check: 18 rows, every row has a meaning, and every code name exists in your files.

**When you finish, you have:** a Words table in docs/contexts.md with 18 hotel words, their meanings and their code names.
### Explanation

Most words already exist in your code with exactly one meaning, because P03 and P04 used the hotel’s words. Five do not exist yet; they arrive with the practice that needs them.



---

## 4. Step 3 · Four contexts, each with its own job

Sort the 18 words into four business areas, the bounded contexts, and give each one a single job.





### The four contexts of the hotel

| Context | Job, in one sentence | It keeps |
| --- | --- | --- |
| Rooms (Inventory) | Knows which rooms the hotel sells. | Each room’s number, type, nightly price and whether it is out of service. |
| Reservations | Decides whether a guest gets a room for some nights, and keeps every reservation. | Guests, stays and reservations with their status. |
| Payments | Takes deposits and records whether they were paid. | Each deposit’s amount and status. |
| Notifications | Tells guests what happened, by email or SMS. | Each message, its channel and whether it arrived. |

**Who owns a rule?** In short: The owner of a rule is the context that keeps all the data the rule has to read.

1. Rule: a room never has two Confirmed reservations that share a night.
2. Which data does the rule read? Every reservation of that room, with its stay and status.
3. Which context keeps all reservations? Reservations. So Reservations owns the rule.
4. In your P04 code the rule already lives there: each reservation answers “do I block this request?”.

C#:

```text
// HotelBooking/Reservation.cs
public bool Blocks(Room room, DateRange stay) =>
    Status == ReservationStatus.Confirmed && Room == room && Stay.Overlaps(stay);
```

Python:

```text
# hotel_booking/domain.py
def blocks(self, room: Room, stay: DateRange) -> bool:
    return self.status == ReservationStatus.CONFIRMED and self.room == room and self.stay.overlaps(stay)
```

Java:

```text
// src/main/java/hotelbooking/Reservation.java
public boolean blocks(Room room, DateRange stay) {
    return status == ReservationStatus.CONFIRMED && this.room.equals(room) && this.stay.overlaps(stay);
}
```

Go:

```text
// domain.go
func (reservation *Reservation) Blocks(room Room, stay DateRange) bool {
	return reservation.Status == Confirmed && reservation.Room == room && reservation.Stay.Overlaps(stay)
}
```

JavaScript:

```text
// src/domain.js
blocks(room, stay) {
  return this.status === ReservationStatus.Confirmed && this.room.equals(room) && this.stay.overlaps(stay);
}
```

Dart:

```text
// lib/domain.dart
bool blocks(Room room, DateRange stay) =>
    status == ReservationStatus.confirmed && this.room == room && this.stay.overlaps(stay);
```

### Completed example: the Reservations section of docs/contexts.md

This is how the section looks at the end of P05. Today’s Step 3 writes its first part: the heading, the Job line and the Words table. The “Rules this context owns” bullets come in Step 5, the last two lines in Step 7.

### docs/contexts.md (Reservations section)

```text
## Reservations

Job: decides whether a guest gets a room for some nights, and keeps every reservation.

| Word | Meaning here |
|---|---|
| Stay | Check-in to check-out, half-open: 12–14 Nov = the nights of the 12th and 13th |
| Reservation | Guest + room number + stay + status, with an id such as R101 |
| Confirmed | The room is promised to this guest for these nights |
| Cancelled | The promise is withdrawn; the nights are free again |

Rules this context owns:
- A room never has two Confirmed reservations that share a night.
- Check-out must be after check-in.

Needs from other contexts: the list of rooms (Rooms).
Tells other contexts: ReservationConfirmed (Notifications).
```

### Your turn · write the four contexts

**Check your sorting** In short: Rooms has 4 words, Reservations 8, Payments 3, Notifications 3: 18 in total. If a word could go in two places, put it where its meaning is decided. “Room” goes to Rooms; Reservations only uses the room number.

1. Rooms: Room, Room type, Nightly price, Out of service.
2. Reservations: Guest, Stay, Reservation, Confirmed, Cancelled, Book, Cancel, Free rooms.
3. Payments: Deposit, Paid, Declined.
4. Notifications: Confirmation message, Sent, Failed.

1. In docs/contexts.md, below the Words table, add four headings: ## Rooms, ## Reservations, ## Payments, ## Notifications.
2. Under each heading, write the line “Job: …” from the table above.
3. Under ## Reservations, copy the Words table from the completed example (Stay, Reservation, Confirmed, Cancelled), then add the other Reservations words from your ## Words table: Guest, Book, Cancel, Free rooms.
4. Under each of the other three headings, add a table | Word | Meaning here | and put each remaining word in the context that keeps it, with its meaning from Step 2.
5. Check: all 18 words are placed, each in exactly one context. Then compare with the “?” next to this task.

**When you finish, you have:** four context sections in docs/contexts.md, each with its job and its own words.
### Explanation

A context is not a class or a folder yet: it is a business area with its own words. Reservations is the core domain, because correct bookings are the reason the hotel wants the app.



---

## 5. Step 4 · Same word, different meaning

Some words appear in more than one context and mean something different in each. Write them down: this is exactly why the contexts need separate models.





### One word, four meanings

| Word | Rooms | Reservations | Payments | Notifications |
| --- | --- | --- | --- | --- |
| Room | R8: Deluxe, nightly price, out of service | Just the number R8 on a reservation | Not used | Text in the message: “Room R8” |
| Status | In service / out of service | Confirmed / Cancelled | Paid / Declined | Sent / Failed |
| Cancel | Take a room off sale | Withdraw a reservation | Refund a deposit | Not used |

**Same word, different meaning: one model per context** In short: Inside one context every word has exactly one meaning; the same word may mean something else next door.

1. “Status” in Reservations: Confirmed or Cancelled.
2. “Status” in Payments: Paid or Declined. In Notifications: Sent or Failed.
3. If one shared field held all of them, a failed SMS could overwrite Confirmed with Failed and Maya would lose R7.
4. So each context keeps its own status. Today only the Reservations one exists in the code:

C#:

```text
// HotelBooking/ReservationStatus.cs
public enum ReservationStatus { Confirmed, Cancelled }
```

Python:

```text
# hotel_booking/domain.py
class ReservationStatus(Enum):
    CONFIRMED = "Confirmed"
    CANCELLED = "Cancelled"
```

Java:

```text
// src/main/java/hotelbooking/ReservationStatus.java
public enum ReservationStatus { CONFIRMED, CANCELLED }
```

Go:

```text
// domain.go
type ReservationStatus string

const (
	Confirmed ReservationStatus = "Confirmed"
	Cancelled ReservationStatus = "Cancelled"
)
```

JavaScript:

```text
// src/domain.js
export const ReservationStatus = Object.freeze({ Confirmed: 'Confirmed', Cancelled: 'Cancelled' });
```

Dart:

```text
// lib/domain.dart
enum ReservationStatus { confirmed, cancelled }
```

### Completed example: the Status row

If one shared Status field held Confirmed, Paid and Sent at once, a failed SMS could overwrite Confirmed with Failed, and Maya would lose R7 because of a message. Separate contexts keep each status where its rules live (rule R6).

### docs/contexts.md

```text
## Same word, different meaning

| Word | Rooms | Reservations | Payments | Notifications |
|---|---|---|---|---|
| Status | In service / out of service | Confirmed / Cancelled | Paid / Declined | Sent / Failed |
```

### Your turn · write the words with two meanings

1. In docs/contexts.md, add the heading ## Same word, different meaning and copy the completed table with the Status row.
2. Add the rows Room and Cancel from the table above, in your own words.
3. Below the table, write one sentence: what would go wrong if Notifications could change a reservation’s status to Failed?
4. Compare your sentence with the completed example. It should name the guest who loses the room and the rule it breaks (R6).

**When you finish, you have:** a “Same word, different meaning” table with Room, Status and Cancel, and one sentence about the danger of one shared status.
### Explanation

When one word has two meanings, the code must not share one field or one class for both. Each context gets its own model: in P06 Notifications gets its own Confirmation message instead of using Reservations’ Reservation class.



---

## 6. Step 5 · Who owns each rule

Every rule needs exactly one owner: the context that keeps all the data the rule reads. Give each of eight rule cards its owner and the reason.





### How to find the owner

Ask: which data does this rule read, and which context keeps all of it? The booking rule reads every reservation of a room, so its owner is Reservations, and your P04 code already keeps it there.

**Who owns a rule?** In short: The owner of a rule is the context that keeps all the data the rule has to read.

1. Rule: a room never has two Confirmed reservations that share a night.
2. Which data does the rule read? Every reservation of that room, with its stay and status.
3. Which context keeps all reservations? Reservations. So Reservations owns the rule.
4. In your P04 code the rule already lives there: each reservation answers “do I block this request?”.

C#:

```text
// HotelBooking/Reservation.cs
public bool Blocks(Room room, DateRange stay) =>
    Status == ReservationStatus.Confirmed && Room == room && Stay.Overlaps(stay);
```

Python:

```text
# hotel_booking/domain.py
def blocks(self, room: Room, stay: DateRange) -> bool:
    return self.status == ReservationStatus.CONFIRMED and self.room == room and self.stay.overlaps(stay)
```

Java:

```text
// src/main/java/hotelbooking/Reservation.java
public boolean blocks(Room room, DateRange stay) {
    return status == ReservationStatus.CONFIRMED && this.room.equals(room) && this.stay.overlaps(stay);
}
```

Go:

```text
// domain.go
func (reservation *Reservation) Blocks(room Room, stay DateRange) bool {
	return reservation.Status == Confirmed && reservation.Room == room && reservation.Stay.Overlaps(stay)
}
```

JavaScript:

```text
// src/domain.js
blocks(room, stay) {
  return this.status === ReservationStatus.Confirmed && this.room.equals(room) && this.stay.overlaps(stay);
}
```

Dart:

```text
// lib/domain.dart
bool blocks(Room room, DateRange stay) =>
    status == ReservationStatus.confirmed && this.room == room && this.stay.overlaps(stay);
```

### The eight rule cards

| Card | Rule |
| --- | --- |
| 1 | A room never has two Confirmed reservations that share a night. |
| 2 | Check-out must be after check-in. |
| 3 | Cancelling a reservation frees its nights for new bookings. |
| 4 | A room that is out of service is not offered to guests. |
| 5 | The nightly price depends on the room type (Deluxe costs more than Standard). |
| 6 | The deposit is 30% of the stay’s price. |
| 7 | Only a Paid deposit can be refunded. |
| 8 | A failed message is recorded as Failed and never changes the reservation. |

### Completed example: two cards with their owners

### docs/contexts.md

```text
## Rule owners

| Card | Rule | Owner | Why this owner |
|---|---|---|---|
| 1 | A room never has two Confirmed reservations that share a night. | Reservations | It keeps every reservation of the room, so it can check them all together. |
| 6 | The deposit is 30% of the stay’s price. | Payments | Money rules belong to Payments; it asks Rooms for the nightly price. |
```

### Your turn · give every rule one owner

**Check your eight owners** In short: Compare your “Rule owners” table with these answers after you have filled all eight rows. If your owner is different, ask: which context keeps the data this rule reads? Change the row if the answer points somewhere else.

1. Card 1, no two Confirmed reservations of a room share a night: Reservations. It keeps every reservation of the room.
2. Card 2, check-out must be after check-in: Reservations. The stay is its word, and it refuses wrong dates before anything else.
3. Card 3, cancelling frees the nights: Reservations. Only Confirmed reservations block; Cancelled ones do not.
4. Card 4, a room out of service is not offered: Rooms. It knows which rooms are on sale.
5. Card 5, the nightly price depends on the room type: Rooms. It keeps each room’s type and price.
6. Card 6, the deposit is 30% of the stay’s price: Payments. Money rules belong to money; it asks Rooms for the price.
7. Card 7, only a Paid deposit can be refunded: Payments. It keeps the payment status.
8. Card 8, a failed message is recorded as Failed and never changes the reservation: Notifications. It keeps the message status (rule R6).

1. In docs/contexts.md, add the heading ## Rule owners and copy the completed table with cards 1 and 6.
2. Add cards 2, 3, 4, 5, 7 and 8 as rows. For each, ask “which context keeps the data this rule reads?” and write the owner and the reason in one sentence.
3. Go back to each context section and add the line “Rules this context owns:” with its rules as bullets, as in the Reservations example from Step 3.
4. Check: each card has exactly one owner, and every context section lists at least one rule.
5. Open the “?” next to this task and compare your eight owners. Where your reason points to another context, change the row.

**When you finish, you have:** a Rule owners table with 8 cards, each with one owner and a reason, and each context section listing the rules it owns.
### Explanation

If two contexts both decided the same rule, they would one day decide differently. One owner per rule is what lets P06 put each rule in exactly one module and forbid the others from changing it.



---

## 7. Step 6 · Draw the context map

Draw the four contexts and the three arrows between them in draw.io. Every arrow starts at the context that needs something or has something to tell.





### Two kinds of arrow

A solid arrow asks a question and waits for the answer. A green dotted arrow announces something that has already happened, and does not wait.

**Two kinds of arrow on a context map** In short: A solid arrow asks a question and waits for the answer; a dotted arrow announces something that already happened.

1. Solid, Reservations → Rooms: “Which rooms exist?” Reservations waits for the list.
2. Solid, Reservations → Payments (from P17): “Take a deposit of 30%.” It waits for Paid or Declined.
3. Dotted, Reservations → Notifications: “ReservationConfirmed R101.” Reservations does not wait for the email.
4. The arrow starts at the context that needs something or has something to tell.

### Completed example: the context map

### Diagram: Completed example: the hotel context map

Four boxes, one per context, each with its job in three short lines. Reservations is green because it owns the booking rule. Every arrow starts at Reservations: two solid arrows ask a question, one green dotted arrow announces an event.

Diagram as text:

Rooms (Inventory) knows which rooms the hotel has, their type and nightly price. Reservations owns guests’ stays and the rule that a room never has two Confirmed reservations sharing a night. It asks Rooms for the room list. Payments owns deposits. Reservations will ask it to take a deposit in P17, when one booking becomes a chain of steps across modules (a saga: hold the room, take the deposit, confirm, or undo). Notifications owns messages. Reservations announces ReservationConfirmed; Notifications decides how to tell the guest.

**Step by step:** Draw it in this order: the four boxes first, then the three arrows. Each step below is one thing on the page.

1. Box 1, Rooms (Inventory), top left: which rooms the hotel sells, their type and nightly price, and whether a room is out of service. In your P04 code a room is only a number and a type; the price and “out of service” come later.

   C#:
   
   ```text
   // HotelBooking/Room.cs
   public record Room(string Number, string Type);
   ```
   
   Python:
   
   ```text
   # hotel_booking/domain.py
   @dataclass(frozen=True)
   class Room:
       number: str
       type: str
   ```
   
   Java:
   
   ```text
   // src/main/java/hotelbooking/Room.java
   public record Room(String number, String type) {}
   ```
   
   Go:
   
   ```text
   // domain.go
   type Room struct {
   	Number string
   	Type   string
   }
   ```
   
   JavaScript:
   
   ```text
   // src/domain.js
   export class Room {
     constructor(number, type) {
       this.number = number;
       this.type = type;
     }
   ```
   
   Dart:
   
   ```text
   // lib/domain.dart
   class Room {
     Room(this.number, this.type);
   
     final String number;
     final String type;
   ```
2. Box 2, Reservations, bottom left, green: guests, stays and reservations such as R101, with the status Confirmed or Cancelled. Green, because it owns the booking rule: no two Confirmed reservations of one room share a night.

   C#:
   
   ```text
   // HotelBooking/Reservation.cs
   public bool Blocks(Room room, DateRange stay) =>
       Status == ReservationStatus.Confirmed && Room == room && Stay.Overlaps(stay);
   ```
   
   Python:
   
   ```text
   # hotel_booking/domain.py
   def blocks(self, room: Room, stay: DateRange) -> bool:
       return self.status == ReservationStatus.CONFIRMED and self.room == room and self.stay.overlaps(stay)
   ```
   
   Java:
   
   ```text
   // src/main/java/hotelbooking/Reservation.java
   public boolean blocks(Room room, DateRange stay) {
       return status == ReservationStatus.CONFIRMED && this.room.equals(room) && this.stay.overlaps(stay);
   }
   ```
   
   Go:
   
   ```text
   // domain.go
   func (reservation *Reservation) Blocks(room Room, stay DateRange) bool {
   	return reservation.Status == Confirmed && reservation.Room == room && reservation.Stay.Overlaps(stay)
   }
   ```
   
   JavaScript:
   
   ```text
   // src/domain.js
   blocks(room, stay) {
     return this.status === ReservationStatus.Confirmed && this.room.equals(room) && this.stay.overlaps(stay);
   }
   ```
   
   Dart:
   
   ```text
   // lib/domain.dart
   bool blocks(Room room, DateRange stay) =>
       status == ReservationStatus.confirmed && this.room == room && this.stay.overlaps(stay);
   ```
3. Box 3, Payments, top right: deposits and their status, Paid or Declined. It owns money and nothing else: a Paid deposit never decides whether R7 is free. There is no payment code yet.
4. Box 4, Notifications, bottom right: messages, the channel (email or SMS) and the delivery status, Sent or Failed. In P04 code this is the notification sender with its email and SMS versions.

   C#:
   
   ```text
   // HotelBooking/INotificationSender.cs
   public interface INotificationSender
   {
       void SendConfirmation(Reservation reservation);
   }
   ```
   
   Python:
   
   ```text
   # hotel_booking/notifications.py
   class NotificationSender(Protocol):
       def send_confirmation(self, reservation: Reservation) -> None: ...
   ```
   
   Java:
   
   ```text
   // src/main/java/hotelbooking/NotificationSender.java
   public interface NotificationSender {
       void sendConfirmation(Reservation reservation);
   }
   ```
   
   Go:
   
   ```text
   // notifications.go
   type NotificationSender interface {
   	SendConfirmation(reservation *Reservation) error
   }
   ```
   
   JavaScript:
   
   ```text
   // src/notifications.js
   // No interface keyword: any object with this method fits.
   //   sendConfirmation(reservation)   tell this guest their booking is confirmed,
   //                                   or throw when the message cannot be sent
   ```
   
   Dart:
   
   ```text
   // lib/notifications.dart
   abstract interface class NotificationSender {
     void sendConfirmation(Reservation reservation);
   }
   ```
5. Solid arrow Reservations → Rooms, label “Asks: which rooms exist?”: to search free rooms and to book, Reservations needs the room list. Rooms never asks about reservations, so there is no arrow back.
6. Solid arrow Reservations → Payments, label “Asks: take a deposit (P17)”: from P17 the booking asks Payments to take the deposit and waits for Paid or Declined. Reservations decides what that answer means for the booking.
7. Green dotted arrow Reservations → Notifications, label “Announces: ReservationConfirmed”: after R101 is saved, Reservations announces it and does not wait. Notifications sends the email or SMS; a failed message never changes R101 (rule R6). In P06 the announcement carries Notifications’ own Confirmation message with five fields:

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
8. Check the finished page: 4 boxes, 1 of them green; 3 arrows, all starting at Reservations; 2 solid, 1 dotted; every arrow has a label that starts with “Asks:” or “Announces:”.

**Legend:**
- **White box.** A part we build ourselves: a module, a class, a business area or an application. In this diagram: Rooms, Payments and Notifications: three business areas, each with its own words.
- **Green box.** The part that owns the rule the diagram is about. In this diagram: Reservations: the context that owns the booking rule.
- **Solid line with a filled arrowhead.** A request at run time: the part at the start asks the part at the arrowhead to do something. In this diagram: Reservations → Rooms: “which rooms exist?”; Reservations → Payments: “take a deposit”.
- **Green dotted line with a filled arrowhead.** An event: the part at the start announces “this happened”; the part at the arrowhead reacts to it. In this diagram: Reservations → Notifications: “ReservationConfirmed” for R101.

### Your turn · draw the page “Context map”

Keep the completed example on screen next to draw.io.

**draw.io: a new page, boxes and arrows** In short: One file, docs/diagrams.drawio, with one page per diagram; today you add the page “Context map”.

1. Open docs/diagrams.drawio in app.diagrams.net (Open Existing Diagram) or in the desktop app.
2. Click + at the bottom, then double-click the new tab and type Context map.
3. Drag a Rectangle from the General shapes; double-click it to type the context name and its job.
4. Hover over a box until blue arrows appear, drag one onto another box: that is an arrow. Double-click the arrow to type its label.
5. Select an arrow and use the Format panel on the right: Line “dotted” for an event, colour green; keep it solid for a request.

1. Open docs/diagrams.drawio, click + at the bottom, and rename the new page to Context map.
2. Draw four rectangles in the same layout as the example: Rooms (Inventory) top left, Payments top right, Reservations bottom left, Notifications bottom right.
3. In each box, type the context name in bold and three short lines: what it keeps, its statuses and what it owns (copy them from the example).
4. Select the Reservations box and set its fill to green in the Format panel.
5. Draw the two solid arrows from Reservations to Rooms and to Payments. Label them “Asks: which rooms exist?” and “Asks: take a deposit (P17)”.
6. Draw the arrow from Reservations to Notifications, set its line to dotted and its colour to green, and label it “Announces: ReservationConfirmed”.
7. Save (Ctrl+S). Check: 4 boxes, 1 of them green; 3 labelled arrows, all starting at Reservations; 2 solid, 1 dotted.

**When you finish, you have:** the draw.io page “Context map” with four contexts and three labelled arrows.
### Explanation

All three arrows start at Reservations, because Reservations is the context that needs the others. Rooms, Payments and Notifications never call Reservations: in P06 an architecture test will keep it that way.



---

## 8. Step 7 · What crosses each border

For every arrow on your map, write the exact question or event and every field it carries. Then each context knows what it may ask, what it must answer, and what it never touches.





### The three borders

| Arrow | Kind | What crosses | Fields |
| --- | --- | --- | --- |
| Reservations → Rooms | Question | Which rooms exist? | Answer: each room’s number and type (R7 Standard, R8 Deluxe) |
| Reservations → Payments | Question, from P17 | Take a deposit | Sends: reservation id and amount. Answer: Paid or Declined |
| Reservations → Notifications | Event | ReservationConfirmed | Reservation id, guest email, room number, check-in, check-out |

**An event: “this happened”, not “do this”** In short: Reservations announces ReservationConfirmed after saving; Notifications decides how to tell the guest. An event is named in the past tense because it has already happened: Reservations does not ask permission, and it does not care whether the email or the SMS works.

1. Maya books R7 for 12–14 November; Reservations saves R101.
2. Reservations announces: ReservationConfirmed, R101, maya@example.com, R7, 12–14 Nov.
3. Notifications sends the email. If it fails, it records Failed; R101 stays Confirmed (R6).
4. In P04 code the announcement is still a method call that passes the whole Reservation. P06 replaces it with a small Confirmation message.

C#:

```text
// HotelBooking/INotificationSender.cs
public interface INotificationSender
{
    void SendConfirmation(Reservation reservation);
}
```

Python:

```text
# hotel_booking/notifications.py
class NotificationSender(Protocol):
    def send_confirmation(self, reservation: Reservation) -> None: ...
```

Java:

```text
// src/main/java/hotelbooking/NotificationSender.java
public interface NotificationSender {
    void sendConfirmation(Reservation reservation);
}
```

Go:

```text
// notifications.go
type NotificationSender interface {
	SendConfirmation(reservation *Reservation) error
}
```

JavaScript:

```text
// src/notifications.js
// No interface keyword: any object with this method fits.
//   sendConfirmation(reservation)   tell this guest their booking is confirmed,
//                                   or throw when the message cannot be sent
```

Dart:

```text
// lib/notifications.dart
abstract interface class NotificationSender {
  void sendConfirmation(Reservation reservation);
}
```

### Completed example: the event border

### docs/contexts.md

```text
## Between contexts

### Reservations → Notifications: ReservationConfirmed (event)

Sent when: a reservation is saved as Confirmed.

Fields:
- reservationId: R101
- guestEmail: maya@example.com
- roomNumber: R7
- checkIn: 2026-11-12
- checkOut: 2026-11-14

Reservations does not wait for an answer. A failed message never changes R101 (rule R6).

Never crosses: Reservations’ list of reservations. Notifications never reads it.
```

### How P06 will write this message in code

In P06 the five fields become Notifications’ own type, so Notifications no longer needs Reservations’ Reservation class. Open your language’s tab.

**Why only five fields cross the border** In short: A message carries only what the receiver needs; then the receiver never has to read the sender’s private data.

1. Notifications must write: “Dear guest, R101 is confirmed: room R7, 12–14 November.”
2. For that it needs the reservation id, the email address, the room number and the two dates.
3. It does not need the guest’s name list, other reservations or the booking rule.
4. P06 turns these five fields into Notifications’ own Confirmation type:

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

### Notifications’ Confirmation (P06)

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
```

### Your turn · write what crosses each border

1. In docs/contexts.md, add the heading ## Between contexts and copy the completed ReservationConfirmed subsection.
2. Add ### Reservations → Rooms: which rooms exist? (question) with four parts: Sent when, What Reservations sends, The answer and its fields, Never crosses.
3. Add ### Reservations → Payments: take a deposit (question, from P17) the same way: it sends the reservation id and the amount, and gets Paid or Declined back.
4. Go back to each context section and add the last two lines, as in the Reservations example: “Needs from other contexts:” and “Tells other contexts:”. Write “nothing” where there is nothing.
5. Check: one subsection for each arrow on your context map (3 in total), and each one lists its fields.

**When you finish, you have:** a “Between contexts” section with all three borders and their fields, and each context section saying what it needs and what it tells. docs/contexts.md is complete.
### Explanation

Writing the fields is what keeps contexts apart: Notifications gets five fields and nothing else, so it never needs to read Reservations’ data. The “Never crosses” line is the rule P06’s architecture tests will check.



---

## 9. Step 8 · Check your project and see what comes next

Check your folder against the list, then see how P06 turns today’s design into code.





### Completed example: the finished P05 folder

### hotel-booking folder

```text
hotel-booking/
├── docs/
│   ├── requirements.md         R1–R6, unchanged
│   ├── design-notes.md         P03 and P04 notes, unchanged
│   ├── quality-scenarios.md    NEW: stakeholders and S1–S6, each with a measure
│   ├── contexts.md             NEW: four contexts, their words, their rules, who owns each rule
│   └── diagrams.drawio         NEW page: "Context map"
└── your P04 code                   not changed in P05
```

### Your P05 checklist

| File or page | It is finished when |
| --- | --- |
| docs/quality-scenarios.md | The Stakeholders table, and S1–S6 with seven lines each; every Measure has a number. |
| docs/contexts.md · Words | 18 words, each with a meaning and its code name (or “not in code yet”). |
| docs/contexts.md · four context sections | Each has Job, Words, Rules this context owns, Needs from other contexts and Tells other contexts. |
| docs/contexts.md · Same word, different meaning | Room, Status and Cancel, plus your sentence about one shared status. |
| docs/contexts.md · Rule owners | 8 cards, each with one owner and a reason. |
| docs/contexts.md · Between contexts | 3 borders, each with its fields. |
| diagrams.drawio · Context map | 4 boxes (Reservations green), 3 labelled arrows, all starting at Reservations. |
| Code | Unchanged: the same 8 tests pass. |

### Where the hotel app goes next

| Lesson | What changes in the hotel app |
| --- | --- |
| P06 | Each context becomes a module with a public contract (Rooms, Reservations, Payments, Notifications). Notifications gets the Confirmation type from Step 7. Architecture tests fail if a module reaches into another. Same 8 tests, same nine demo lines. |
| L04 | System design: how many requests the hotel must handle, how a request travels over HTTP to a database, and when a cache helps. |
| P07 · P08 | The app goes behind an HTTP API with PostgreSQL, then the free-room search gets a cache, measured against S1 and S6. |

### Your turn · check and hand over your project

1. Go through the checklist and open each file and the draw.io page. Fix anything that is missing.
2. Run the tests once more with your P04 command: still 8 passing, because the code did not change.
3. Copy the whole hotel-booking folder to your partner’s laptop (for example as a ZIP in a message).
4. Keep the folder: P06 starts from it.

**When you finish, you have:** the complete P05 project on both partners’ laptops: two new design files, the context map, and the unchanged P04 code with 8 green tests.
### Explanation

Nothing in the code changed today, and that is the point: P06 now has a written design to follow. Each context section becomes a module, each rule owner says where the rule’s code lives, and each border becomes a contract.
