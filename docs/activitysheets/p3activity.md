# P03 · Activity sheet

## 1. P03 · The hotel booking app we start building today

A small hotel takes bookings on its website and at the reception desk. Today we design the app that handles those bookings, step by step, and write its first version.





### Who needs this app?

The hotel has rooms such as R7 (standard) and R8 (deluxe). Guests book on the website. A receptionist books for guests who phone or walk in. Both kinds of booking must go into one shared list, so everyone gets the same answer to “is R7 free on the 13th?”.

### The everyday problem

Right now the hotel keeps bookings in one spreadsheet. On Monday morning Maya books R7 for 12–14 November on the website. At the same moment a receptionist, on the phone with Omar, looks at the spreadsheet, sees R7 still free and promises it to him for 13–15 November. On the 13th both guests arrive with a booking for the same room.

Our app must make that impossible: one room, one night, at most one guest. This rule follows us through the whole course.

### What version 1 of the app does

| Job | Example |
| --- | --- |
| Search free rooms | Which rooms are free from 12 to 14 November? → R7, R8 |
| Book a room | Maya books R7 for 12–14 November → Accepted, id R101 |
| Refuse a clash | Omar asks for R7 on 13–15 November → Unavailable |
| Cancel a booking | Maya cancels R101 → R7 is free again for those nights |
| View a booking | Reception looks up R101 → Maya, R7, 12–14 November, Confirmed |

### Today’s plan

| Step | What you do | What you add to your project folder |
| --- | --- | --- |
| 1 · Why design first | See why developers draw before they code | design-notes.md: first answer |
| 2 · Requirements | Write down the five rules of the app | requirements.md |
| 3 · Use case diagram | Draw who uses the app and for what | draw.io page “Use cases” |
| 4 · Class diagram | Draw the classes, their data and methods | draw.io page “Classes” |
| 5 · Sequence diagrams | Draw how the classes talk during one request | draw.io pages “Sequence - book” and “Sequence - cancel” |
| 6 · Activity diagram | Draw the decisions of a booking | draw.io page “Activity - book” |
| 7 · Code | Turn the class diagram into C# or Python and run it | A running program |
| 8 · The race | Book the same room twice at the same moment | design-notes.md: what went wrong |
| 9 · Check and hand over | Check every file against the list | The complete P03 folder |

### How you work today

Work in pairs on one laptop. One of you types, the other reads the steps aloud and checks; swap at every new step. Everything happens on the laptop: diagrams in draw.io, text in Markdown files, code in your editor (VS Code, Rider, Visual Studio or PyCharm).

Choose one language with your partner: C# (you need the .NET 10 SDK) or Python (you need Python 3.10 or newer). All the code is on the slides, and the “Runnable code · C# and Python” link at the top of this page has the finished program if you get stuck.

Today is version 1 only: reservations are kept in memory. P04 improves its design. Later practices add a database, an HTTP API and, in P09, a real fix for the problem you will see in Step 8.

### Completed example: your folder at the end of P03

This is what you will have after the last step. Today you start it with empty files.

### hotel-booking folder

```text
hotel-booking/
├── docs/
│   ├── requirements.md     R1–R5, examples and proof lines
│   ├── design-notes.md     your short answers from each step
│   └── diagrams.drawio     5 pages: Use cases, Classes,
│                           Sequence - book, Sequence - cancel, Activity - book
├── HotelBooking/           if you chose C#: Program.cs, HotelBooking.csproj
└── hotel_booking.py        if you chose Python
```

### Your turn · set up your P03 project folder

Do this on the laptop you will use for the whole lesson.

1. Create a folder called hotel-booking (for example in Documents). Inside it, create a folder called docs.
2. Open your editor and open the hotel-booking folder (in VS Code: File → Open Folder).
3. In docs, create two empty files: requirements.md and design-notes.md. A .md (Markdown) file is plain text; a line starting with # becomes a heading.
4. Open app.diagrams.net in your browser. Choose “Device” as the place to save, click “Create New Diagram”, choose “Blank Diagram”, name it diagrams.drawio and save it into hotel-booking/docs.
5. Open a terminal in your editor (VS Code: Terminal → New Terminal). For C#, type dotnet --version and check that it starts with 10. For Python, type python --version (on Windows you can also try py --version) and check that it is 3.10 or newer.

**When you finish, you have:** a hotel-booking folder with docs/requirements.md, docs/design-notes.md and docs/diagrams.drawio, and a terminal that shows your .NET or Python version.
### Explanation

Every later practice changes this same booking app, in this same folder. That is why version 1 stays small: guests, rooms, dates and reservations, plus the rule that a room has at most one guest per night. Payments, prices and housekeeping are left out on purpose.



---

## 2. Step 1 · Why design before writing code?

Low-level design means deciding the classes, what each one stores and does, and which class calls which, before you type the code.





### What low-level design decides

Which classes exist (Guest, Room, Reservation…), what information each one keeps, which methods it has, and which class calls which. We draw these decisions as UML diagrams. UML is a standard set of diagram shapes, so every developer reads them the same way.

It is called “low-level” because it is about the inside of one program. High-level design, later in the course, is about whole services, databases and the messages between them.

### Completed example: the same booking app, without and with a design

| Situation | Without a design | With a design |
| --- | --- | --- |
| Where is the “is the room already booked?” check? | The developer writes the same if-check twice: once in the website code and once in the reception-desk code. | The check is written once, in DateRange.Overlaps(). Both screens call that one method. |
| The hotel changes a rule: from now on a room must stay empty for one day after each guest, for cleaning. | You have to find and change both copies. If you miss one, the website follows the new rule and the reception desk does not, so the two screens give different answers for the same room. | You change one method, Overlaps(). Both screens follow the new rule at once. |
| A new developer joins and asks “how does a booking work?” | They read the website code and the reception code and compare them line by line. | They open one sequence diagram and see every step in order. |

**Why two copies of the same check is a problem** In short: If the same rule is typed in two places, sooner or later the two copies stop matching. Two developers build two screens: the website and the reception desk. Nobody agreed where the booking rules live, so each one writes the check inside their own screen.

1. The website developer writes the “is R7 already booked on these nights?” check in the code behind the Book button.
2. The reception developer writes the same check again in the reception screen.
3. Today both work: R7 cannot be double booked from either screen.
4. But the rule now lives in two places, so every future change must be made twice.

C#:

```text
// website: Book button
if (requested.CheckIn < existing.CheckOut && existing.CheckIn < requested.CheckOut)
    return "Unavailable";

// reception desk: the same check, typed again
if (requested.CheckIn < existing.CheckOut && existing.CheckIn < requested.CheckOut)
    return "Unavailable";
```

Python:

```text
# website: Book button
if requested.check_in < existing.check_out and existing.check_in < requested.check_out:
    return "Unavailable"

# reception desk: the same check, typed again
if requested.check_in < existing.check_out and existing.check_in < requested.check_out:
    return "Unavailable"
```

**One method, used by both screens** In short: Put the rule in one method, and let every screen call that method. The class diagram gives the date check one home: the DateRange class. The screens do not contain the rule; they ask DateRange.

1. DateRange.Overlaps() is the only place where the overlap rule is written.
2. The website calls existing.Stay.Overlaps(requested).
3. The reception desk calls exactly the same method.
4. When the rule changes, you edit Overlaps() once and both screens follow it.

C#:

```text
// DateRange: the only place with the rule
public bool Overlaps(DateRange other) =>
    CheckIn < other.CheckOut && other.CheckIn < CheckOut;

// website and reception desk both just call it
if (existing.Stay.Overlaps(requested)) return "Unavailable";
```

Python:

```text
# DateRange: the only place with the rule
def overlaps(self, other: DateRange) -> bool:
    return self.check_in < other.check_out and other.check_in < self.check_out

# website and reception desk both just call it
if existing.stay.overlaps(requested):
    return "Unavailable"
```

**What happens when one copy is forgotten** In short: The two screens start giving different answers for the same room.

1. The hotel adds a rule: after each guest, the room stays empty for one day for cleaning.
2. The developer updates the website copy of the check, but forgets the reception copy.
3. Maya leaves R7 on the 14th. Nora phones and asks for R7 from the 14th.
4. The reception desk says Accepted, while the website would have said Unavailable.
5. The hotel now has a booking that breaks its own rule, and nobody notices until the cleaner arrives.

C#:

```text
// website: updated, the day after check-out is now blocked too
if (requested.CheckIn < existing.CheckOut.AddDays(1) && existing.CheckIn < requested.CheckOut)
    return "Unavailable";

// reception desk: forgotten, still the old rule
if (requested.CheckIn < existing.CheckOut && existing.CheckIn < requested.CheckOut)
    return "Unavailable";
```

Python:

```text
# website: updated, the day after check-out is now blocked too
if requested.check_in < existing.check_out + timedelta(days=1) and existing.check_in < requested.check_out:
    return "Unavailable"

# reception desk: forgotten, still the old rule
if requested.check_in < existing.check_out and existing.check_in < requested.check_out:
    return "Unavailable"
```

### Low-level and high-level design

|  | Low-level design (today) | High-level design (later lessons) |
| --- | --- | --- |
| Main question | Which classes and methods, and who calls whom? | Which separate programs (services), databases and messages? |
| Hotel example | Before saving a booking, BookingService asks ReservationStore: “does anyone already have this room on these nights?” | The Booking service and the Payment service are two separate programs that send each other messages through RabbitMQ. |
| Diagrams | Use case, class, sequence, activity | Architecture and deployment diagrams |

**What does “BookingService asks ReservationStore” mean?** In short: “Asks” means BookingService calls a method of ReservationStore and uses the answer it gets back. Think of a real hotel. BookingService is the receptionist; ReservationStore is the booking book on the desk.

1. Maya wants R7 for 12–14 November.
2. Before writing her name, the receptionist opens the book and looks at R7 for the nights of the 12th and 13th.
3. Nobody is there, so the receptionist writes Maya in the book.
4. If someone had been there, the receptionist would say “sorry, unavailable” and write nothing.

C#:

```text
if (store.HasOverlap(room, stay))   // the receptionist looks in the book
    return new("Unavailable");      // someone already has the room
store.Save(reservation);            // write Maya in the book
```

Python:

```text
if self.store.has_overlap(room, stay):   # the receptionist looks in the book
    return BookingResult("Unavailable")  # someone already has the room
self.store.save(reservation)             # write Maya in the book
```

**What does “exchange messages through RabbitMQ” mean?** In short: Two separate programs talk by leaving messages in a shared post box, instead of calling each other directly. Later in the course the app grows into several programs. One handles bookings, another handles payments. RabbitMQ is the post box between them. You build this in P15–P17; today you only need the idea.

1. The Booking service saves R101 and posts the message “R101 needs a payment of $240”.
2. The Payment service picks the message up when it is ready and charges the card.
3. The Payment service posts “R101 is paid”.
4. The Booking service reads that message and marks R101 as paid. Neither program had to wait for the other.

C#:

```text
// Booking service (a sketch of what you will write later)
await bus.Publish(new PaymentRequested("R101", 240));

// Payment service
public async Task Handle(PaymentRequested message)
{
    await cards.Charge(message.ReservationId, message.Amount);
    await bus.Publish(new PaymentSucceeded(message.ReservationId));
}
```

Python:

```text
# Booking service (a sketch of what you will write later)
bus.publish(PaymentRequested("R101", 240))

# Payment service
def handle(message: PaymentRequested) -> None:
    cards.charge(message.reservation_id, message.amount)
    bus.publish(PaymentSucceeded(message.reservation_id))
```

### Why we draw the diagrams in this order

| Order | Diagram | The question it answers | Why it comes here |
| --- | --- | --- | --- |
| 1 | Requirements | What must the app do? | Everything else is built to meet them. |
| 2 | Use case diagram | Who uses the app, and for which goals? | The goals tell you which features and methods you need. You cannot pick classes before you know what people want to do. |
| 3 | Class diagram | Which classes keep the data and do the jobs? | Now that the goals are known, you decide where each job lives. |
| 4 | Sequence diagram | In which order do the classes call each other during one request? | It needs the classes and methods from step 3. |
| 5 | Activity diagram | Which decisions can stop a request? | It checks the sequence for the paths where something is refused. |
| 6 | Code | How does it really run? | Each class box becomes one class in the code. |

### Your turn · write your first design note

1. Open docs/design-notes.md.
2. Type the title: # P03 design notes
3. Add a heading on the next line: ## Why design before code
4. Under the heading, finish this sentence in your own words: “Low-level design decides …”
5. Read it to your partner. Check: does your sentence mention the classes, what they keep or do, and who calls whom?

**When you finish, you have:** design-notes.md with a title and your first answer.
### Explanation

Design is cheap to change in a diagram and expensive to change in code. A diagram also gives a pair one shared picture to agree on before anyone types.



---

## 3. Step 2 · Write the requirements

A requirement is one job or rule the app must support, written so that we can check it with an example.





### The date rule: count nights, not dates

A booking for 12–14 November means the guest sleeps in the room on the night of the 12th and the night of the 13th. On the 14th they check out, so a new guest can check in that same day. Developers call this a half-open date range: the check-in day counts, the check-out day does not.

**The check-out day is free: a simple example** In short: A stay uses the nights from check-in up to, but not including, the check-out day. Think of R7 as one bed per night. A clash only happens when two guests need the bed on the same night.

1. Maya books 12–14: she sleeps in R7 on the night of the 12th and the night of the 13th.
2. On the morning of the 14th she leaves and the room is cleaned.
3. Nora books 14–16: she arrives in the afternoon of the 14th. No shared night, so she is Accepted.
4. Omar asks for 13–15: he needs the night of the 13th, which is Maya’s, so he is Unavailable.

C#:

```text
// check-in counts, check-out does not: compare with < (not <=)
public bool Overlaps(DateRange other) =>
    CheckIn < other.CheckOut && other.CheckIn < CheckOut;

// Maya 12–14 vs Nora 14–16: 12 < 16 is true, 14 < 14 is false → no overlap
// Maya 12–14 vs Omar 13–15: 12 < 15 is true, 13 < 14 is true  → overlap
```

Python:

```text
# check-in counts, check-out does not: compare with < (not <=)
def overlaps(self, other: DateRange) -> bool:
    return self.check_in < other.check_out and other.check_in < self.check_out

# Maya 12–14 vs Nora 14–16: 12 < 16 is True, 14 < 14 is False → no overlap
# Maya 12–14 vs Omar 13–15: 12 < 15 is True, 13 < 14 is True  → overlap
```

### R7, night by night

| Booking | Night of 12th | Night of 13th | Night of 14th | Night of 15th | Result |
| --- | --- | --- | --- | --- | --- |
| Maya 12–14 (already booked) | Maya | Maya | — | — | Confirmed |
| Omar asks for 13–15 | — | Omar | Omar | — | Unavailable: both need the 13th |
| Nora asks for 14–16 | — | — | Nora | Nora | Accepted: no shared night |

### Agreed requirements for version 1

Not in version 1: prices, payments, invoices, logins, housekeeping. Later practices add only what the course needs.

| ID | Requirement | How we check it |
| --- | --- | --- |
| R1 · Search | A guest or receptionist can list the rooms that are free for given dates. | Empty hotel, 12–14 November → R7, R8. |
| R2 · Book | A guest or receptionist can book a free room. The app answers with a reservation id. | Maya books R7 for 12–14 November → Accepted R101. |
| R3 · No clash | A room never has two Confirmed bookings on the same night. Check-out must be after check-in. | Omar asks for R7 on 13–15 → Unavailable. 15–15 → InvalidDates. |
| R4 · Cancel | A booking can be cancelled. It is kept as Cancelled and no longer blocks the room. | Maya cancels R101 → R7 is free again for 12–14. |
| R5 · View | A booking can be looked up by its id. | R101 → Maya, R7, 12–14 November, Confirmed. |

### Completed example: the start of requirements.md

This is how the first two requirements look in the file. You write all five in the same way.

### docs/requirements.md (first part)

```text
# Requirements: hotel booking app, version 1

## R1 · Search
A guest or receptionist can list the rooms that are free for given dates.
Check: empty hotel, 12–14 November → R7, R8.

## R2 · Book
A guest or receptionist can book a free room. The app answers with a reservation id.
Check: Maya books R7 for 12–14 November → Accepted R101.
```

### Your turn · write requirements.md

1. Open docs/requirements.md.
2. Type the title and R1 and R2 exactly as in the completed example above.
3. Add R3, R4 and R5 in the same format: a ## heading, the requirement sentence, and a “Check:” line. Copy the text from the requirements table.
4. Under R3, add the line “More examples:” and copy the three rows of the “R7, night by night” table as three short lines, for example “Omar R7 13–15 → Unavailable (night of the 13th)”.
5. Still under R3, add your own two examples for room R8, which is already booked for 20–22 November: one request that is Accepted and one that is Unavailable. For the Unavailable one, write which night is shared.
6. Read only your two R8 requests to your partner, without the results. Your partner says Accepted or Unavailable; compare with what you wrote.

**When you finish, you have:** docs/requirements.md with R1–R5, each with a Check line, and five examples under R3 (three for R7, two of your own for R8).
### Explanation

Every requirement has a check, so “done” means something concrete. R3 is the rule the whole course protects: today with an if-statement, later with database locks, retries and messages.



---

## 4. Step 3 · Draw the use case diagram

A use case diagram shows who uses the app and which goals they can finish with it. We draw it before the class diagram, because the goals tell us which classes and methods we need.





### The four shapes

| Shape | Name | What it means here |
| --- | --- | --- |
| Large rectangle | System boundary | The hotel booking app itself. Everything inside it is done by the app. |
| Stick figure, outside the rectangle | Actor | A person (or another system) who uses the app: Guest, Receptionist. |
| Oval, inside the rectangle | Use case | One goal the person can finish, written as a verb: “Book a room”. |
| Plain line | Association | This actor uses this goal. |

### Completed diagram: three of the four goals

Guest and Receptionist can both search, book and view. Checking dates and saving are steps inside the app, so they are not separate goals.

### Diagram: Who uses the hotel booking app, and for what?

The box is the hotel booking app. Guest and Receptionist stand outside it because they are people using it. Each oval is one goal a person can finish. A receptionist does the same jobs for a guest who phones or walks in. Checking dates and saving happen inside the app, so they are not drawn as goals.

Diagram as text:

Guest and Receptionist use the Hotel booking app. Search free rooms. Book a room. View a booking. The box is the hotel booking app. Guest and Receptionist stand outside it because they are people using it. Each oval is one goal a person can finish. A receptionist does the same jobs for a guest who phones or walks in. Checking dates and saving happen inside the app, so they are not drawn as goals.

**Step by step:** Read the diagram from left to right: who stands outside the app, which goals are inside it, and which lines join them.

1. The big rectangle is the hotel booking app. Everything drawn inside it is something the app does for people.
2. Outside the rectangle, on the left, stand two actors: Guest and Receptionist. They are people who use the app; they are not part of it.
3. Inside are three goals (the ovals): Search free rooms, Book a room and View a booking. Each one is something a person can finish from start to end.
4. Follow the three lines from Guest: a guest can search, book and view a booking on the website.
5. Follow the three lines from Receptionist: the receptionist can do the same three jobs for a guest who phones or walks in.
6. Checking the dates and saving are not on the diagram. They are steps inside “Book a room”, not goals of their own.
7. Each goal later becomes a method in the code: Search free rooms → FindFreeRooms, Book a room → Reserve, View a booking → Find.

   C#:
   
   ```text
   service.FindFreeRooms(Nov(12, 14));   // Search free rooms
   service.Reserve(maya, r7, Nov(12, 14)); // Book a room
   store.Find("R101");                     // View a booking
   ```
   
   Python:
   
   ```text
   service.find_free_rooms(nov(12, 14))    # Search free rooms
   service.reserve(MAYA, R7, nov(12, 14))  # Book a room
   store.find("R101")                      # View a booking
   ```

**Legend:**
- **Large rectangle (system boundary).** The app itself. Inside the rectangle = what the app does. In this diagram: The rectangle titled “Hotel booking app”.
- **Stick figure (actor).** A person, or another system, outside the app that uses it. In this diagram: Guest (books on the website) and Receptionist (books for a guest on the phone).
- **Oval (use case).** One goal a person can finish with the app, written as a verb. In this diagram: “Book a room”: the guest ends up with a reservation id.
- **Plain line (association).** This actor uses this goal. The line has no arrow and says nothing about order. In this diagram: Guest —— Book a room: a guest can book a room.

### Who is the actor when Nora phones the hotel?

The actor is the receptionist, because the receptionist is the one using the app. Nora only talks to a person. Nora is still the guest stored on the reservation, but she is not an actor in that call.

### One use case in words: “Book a room”

Each oval can also be written as a short story of steps. Developers call this a use case description. It is useful because each step becomes something the code must do. Here is the story for the oval “Book a room”, when everything goes well:

1. The guest enters 12–14 November and asks for free rooms.
2. The app shows the free rooms: R7 and R8.
3. The guest chooses R7 and presses Book.
4. The app checks that nobody has R7 on the nights of the 12th and 13th.
5. The app saves reservation R101 and sends a confirmation email.
6. The guest sees: “Accepted, your reservation id is R101.”

### What can go wrong in this story

| At step | What happens | What the guest sees | What is saved |
| --- | --- | --- | --- |
| 1 | Check-out is not after check-in, for example 15–15 | “InvalidDates” | Nothing |
| 4 | Someone already has R7 on one of those nights | “Unavailable” | Nothing |

### Your turn · draw the use case diagram in draw.io

Open docs/diagrams.drawio in app.diagrams.net (File → Open from → Device).

1. At the bottom of the window, double-click the tab “Page-1” and rename it to Use cases.
2. In the “Search Shapes” box on the left, type rectangle. Drag a large rectangle onto the page and type its title: Hotel booking app.
3. Search for actor. Drag two stick figures to the left of the rectangle, outside it. Double-click each one and name them Guest and Receptionist.
4. Search for ellipse. Drag four ovals inside the rectangle and name them: Search free rooms, Book a room, View a booking, Cancel a booking.
5. Connect each actor to each of the four ovals: hover over the stick figure, drag one of the blue arrows to the oval and let go. Select the line and, in the Format panel on the right, set the line end to “none” so it is a plain line.
6. Check with your partner: 2 stick figures outside the box, 4 ovals inside it, 8 lines. Say one sentence per line for Cancel, for example “A receptionist cancels a booking for a guest who phones.”
7. Save (Ctrl+S).

**When you finish, you have:** the draw.io page “Use cases” with 2 actors, 4 goals and 8 lines, saved in docs/diagrams.drawio.
### Explanation

The use case diagram says nothing about screens or classes; it only lists who wants what. That list is the input for the class diagram: every goal needs at least one method somewhere (Search → FindFreeRooms, Book → Reserve, Cancel → Cancel, View → Find).



---

## 5. Step 4 · Draw the class diagram

Each goal from the use case diagram needs classes that keep the data and do the work. Find them in the requirements: nouns usually become classes, verbs usually become methods.





### From the requirements to classes

| Words in the requirements | Becomes |
| --- | --- |
| guest | class Guest (Name, Email) |
| room | class Room (Number, Type) |
| check-in and check-out dates | class DateRange (CheckIn, CheckOut) |
| booking | class Reservation (Id, Status) |
| Confirmed / Cancelled | enumeration ReservationStatus |
| “share a night” | method DateRange.Overlaps(other) |
| “keep the bookings” | class ReservationStore |
| “search”, “book”, “cancel” (the use cases) | methods FindFreeRooms, Reserve, Cancel of BookingService |
| “send a confirmation” | class EmailSender |

### How to read the diagram

| You see | It means | Example from the hotel |
| --- | --- | --- |
| A box with three parts | Class name on top, its data (attributes) in the middle, its methods at the bottom. | DateRange keeps CheckIn and CheckOut and can answer Overlaps(other). |
| A plain line with numbers at the ends | The objects are linked. The numbers say how many. | Guest 1 —— 0..* Reservation: one guest can make zero or many reservations. |
| A line with a filled diamond | Ownership: the part belongs to the whole. | Reservation ◆—— DateRange: each reservation owns its own stay dates. |
| A dashed arrow | Uses: one class calls the other’s methods. | BookingService - - -> ReservationStore |
| «enumeration» above a name | A fixed list of allowed values. | ReservationStatus: Confirmed or Cancelled, nothing else. |

**What does the filled diamond mean?** In short: The class at the diamond owns the thing on the other end of the line. The diamond sits at the owner. Reservation owns its DateRange. Room is different: R7 exists before Maya books it and after she leaves, so Reservation only refers to Room with a plain line.

1. Maya books R7 for 12–14 November and reservation R101 is created.
2. R101 gets its own DateRange: 12–14 November. Those dates belong to R101 and to nothing else.
3. R101 also points to room R7, but it does not own R7: other reservations point to R7 in other weeks.
4. If R101 were deleted, its dates would mean nothing; room R7 would still be there.

C#:

```text
class Reservation
{
    public DateRange Stay { get; }   // owned: filled diamond
    public Room Room { get; }        // referred to: plain line
}
```

Python:

```text
class Reservation:
    def __init__(self, reservation_id, guest, room, stay):
        self.stay = stay   # owned: filled diamond
        self.room = room   # referred to: plain line
```

**What does the dashed arrow mean?** In short: A dashed arrow means “uses”: one class calls methods of another class to do its job. The arrow points to the class that is being used. The user needs it to work, but the used class does not belong to it.

1. BookingService - - -> ReservationStore: during Reserve(), BookingService calls store.HasOverlap(...) and then store.Save(...).
2. BookingService - - -> EmailSender: after saving, BookingService calls email.SendConfirmation(...).
3. Without the store, BookingService could not check or save anything.
4. The store is created outside and handed to BookingService; it is not part of BookingService.

C#:

```text
class BookingService(IReadOnlyList<Room> rooms, ReservationStore store, EmailSender email, int slowStepMs = 0)
{
    // inside Reserve():
    // store.HasOverlap(room, stay)   -> uses ReservationStore
    // store.Save(reservation)        -> uses ReservationStore
    // email.SendConfirmation(...)    -> uses EmailSender
}
```

Python:

```text
class BookingService:
    def __init__(self, rooms, store, email, slow_step_seconds=0):
        self.store = store   # handed in from outside
        self.email = email
    # inside reserve():
    # self.store.has_overlap(room, stay)   -> uses ReservationStore
    # self.store.save(reservation)         -> uses ReservationStore
    # self.email.send_confirmation(...)    -> uses EmailSender
```

### Completed class diagram

### Diagram: Completed class diagram: the hotel booking app, version 1

Top: BookingService runs every request. Middle: the helpers it uses (dashed arrows): ReservationStore keeps all reservations, EmailSender sends confirmations, and ReservationStatus lists the two allowed statuses. Bottom: the data. Each Reservation belongs to one Guest, uses one Room and owns one DateRange. This is the diagram you will turn into code in Step 7.

Diagram as text:

BookingService: rooms, FindFreeRooms(stay), Reserve(guest, room, stay), Cancel(id). ReservationStatus: Confirmed, Cancelled. ReservationStore: rows, HasOverlap(room, stay), Save(reservation), Find(id), CountConfirmed(room, stay). EmailSender: Sent, SendConfirmation(reservation). Guest: Name, Email. Reservation: Id, Status, Blocks(room, stay), Cancel(). Room: Number, Type. DateRange: CheckIn, CheckOut, IsValid(), Overlaps(other). BookingService dependency ReservationStore, uses. BookingService dependency EmailSender, uses. Reservation association 1 ReservationStatus, has. ReservationStore association 0..* Reservation, keeps. Guest 1 association 0..* Reservation, makes. Reservation 0..* association 1 Room, reserves. Reservation 1 composition 1 DateRange, owns stay dates. Top: BookingService runs every request. Middle: the helpers it uses (dashed arrows): ReservationStore keeps all reservations, EmailSender sends confirmations, and ReservationStatus lists the two allowed statuses. Bottom: the data. Each Reservation belongs to one Guest, uses one Room and owns one DateRange. This is the diagram you will turn into code in Step 7.

**Step by step:** Start at the top and follow every line. Each step says what the line means and how it looks in the code.

1. BookingService, at the top, runs every request. It keeps the list of rooms and has one method per use case: FindFreeRooms, Reserve and Cancel.

   C#:
   
   ```text
   class BookingService(IReadOnlyList<Room> rooms, ReservationStore store, EmailSender email, int slowStepMs = 0)
   {
       public IReadOnlyList<Room> FindFreeRooms(DateRange stay) { ... }
       public BookingResult Reserve(Guest guest, Room room, DateRange stay) { ... }
       public bool Cancel(string reservationId) { ... }
   }
   ```
   
   Python:
   
   ```text
   class BookingService:
       def __init__(self, rooms, store, email, slow_step_seconds=0): ...
       def find_free_rooms(self, stay): ...
       def reserve(self, guest, room, stay): ...
       def cancel(self, reservation_id): ...
   ```
2. Dashed arrow BookingService → ReservationStore, “uses”: BookingService calls the store’s methods to check for a clash and to save. It is handed a store; it does not own one.

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
3. Dashed arrow BookingService → EmailSender, “uses”: after saving, BookingService asks EmailSender to send the confirmation.

   C#:
   
   ```text
   email.SendConfirmation(reservation);
   ```
   
   Python:
   
   ```text
   self.email.send_confirmation(reservation)
   ```
4. Line ReservationStore —— Reservation, “keeps”, with 0..* next to Reservation: the store keeps a list of zero or many reservations.

   C#:
   
   ```text
   private readonly List<Reservation> _rows = [];
   ```
   
   Python:
   
   ```text
   self._rows: list[Reservation] = []
   ```
5. Line Reservation —— ReservationStatus, “has”, with 1 next to the status: every reservation has exactly one status, Confirmed or Cancelled.

   C#:
   
   ```text
   public ReservationStatus Status { get; private set; } = ReservationStatus.Confirmed;
   ```
   
   Python:
   
   ```text
   self.status = ReservationStatus.CONFIRMED
   ```
6. Line Guest 1 —— 0..* Reservation, “makes”: one guest can make zero or many reservations, and each reservation belongs to exactly one guest.

   C#:
   
   ```text
   public Guest Guest { get; }   // inside Reservation
   ```
   
   Python:
   
   ```text
   self.guest = guest   # inside Reservation
   ```
7. Line Reservation 0..* —— 1 Room, “reserves”: each reservation is for exactly one room, and a room appears in many reservations over time (different weeks).

   C#:
   
   ```text
   public Room Room { get; }   // inside Reservation
   ```
   
   Python:
   
   ```text
   self.room = room   # inside Reservation
   ```
8. Line with a filled diamond, Reservation ◆—— DateRange, “owns stay dates”: each reservation owns its own stay dates. The dates mean nothing without that reservation.

   C#:
   
   ```text
   public DateRange Stay { get; }   // inside Reservation
   ```
   
   Python:
   
   ```text
   self.stay = stay   # inside Reservation
   ```
9. All together, when Maya books R7: BookingService asks ReservationStore (dashed arrow), creates Reservation R101 that points to Guest Maya and Room R7, owns DateRange 12–14 November and has status Confirmed, saves it into the store’s list (keeps), and then calls EmailSender (dashed arrow).

**Legend:**
- **Box with three parts (class).** Top: the class name. Middle: the data it keeps (attributes). Bottom: what it can do (methods). In this diagram: DateRange keeps CheckIn and CheckOut and can do IsValid() and Overlaps(other).
- **Box marked «enumeration».** A fixed list of allowed values. In this diagram: ReservationStatus: only Confirmed or Cancelled.
- **Solid line with no arrow (association).** One object knows another object. Neither owns the other. In this diagram: Reservation —— Room, “reserves”: R101 points to room R7, but R7 exists before and after R101.
- **Numbers at the ends of a line (multiplicity).** How many objects can be at that end: 1 = exactly one, 0..* = zero or many. In this diagram: Guest 1 —— 0..* Reservation: Maya can have no bookings or many; each booking has exactly one guest.
- **Filled diamond at one end of a line (composition).** Ownership. The class at the diamond owns the thing at the other end; that thing belongs to it alone. In this diagram: Reservation ◆—— DateRange: R101 owns its own 12–14 November dates.
- **Dashed line with an open arrow (dependency, “uses”).** The class at the start calls methods of the class the arrow points to. In this diagram: BookingService - - -> ReservationStore: Reserve() calls store.HasOverlap(...) and store.Save(...).
- **Solid line with a hollow triangle (inheritance): not in this diagram.** “Is a kind of”. The triangle touches the more general class. In this diagram: There is none here: a Reservation is not a kind of Room, so there is no triangle between them.

### Why each method lives where it does

| Method | Lives in | Why there |
| --- | --- | --- |
| Overlaps(other) | DateRange | It needs only the two pairs of dates, which DateRange already has. |
| Blocks(room, stay) | Reservation | Only the reservation knows its own status, room and dates. |
| Cancel() | Reservation | Cancelling changes the reservation’s own status. |
| HasOverlap(room, stay) | ReservationStore | Only the store can see all the reservations. |
| Reserve(guest, room, stay) | BookingService | It runs the steps in the right order: check, save, email, answer. |

### A common mistake: “Reservation inherits from Room”

Inheritance (an arrow with a hollow triangle) means “is a kind of”. A reservation is not a kind of room; it refers to a room. So the link between Reservation and Room is a plain association: Reservation 0..* —— 1 Room.

### Your turn · draw the class diagram in draw.io

Keep the completed diagram on screen next to draw.io.

1. In diagrams.drawio, click + at the bottom to add a page and rename it to Classes.
2. Search for class and drag the UML “Class” shape onto the page (a box with a name and two lines below). Make 8 of them: copy one with Ctrl+C, Ctrl+V.
3. Double-click each box and type its name, attributes and methods exactly as in the completed diagram. For ReservationStatus, type «enumeration» above the name and list Confirmed and Cancelled.
4. Place them like the completed diagram: BookingService at the top, ReservationStatus, ReservationStore and EmailSender in the middle, Guest, Reservation, Room and DateRange at the bottom.
5. Draw the plain lines (makes, reserves, keeps, has): drag from one box to the other, double-click the line to type its label, and add the numbers (1, 0..*) as text next to the ends.
6. Draw the composition between Reservation and DateRange: select the line, and in the Format panel set “Line start” to the filled diamond, on the Reservation side.
7. Draw the two “uses” arrows from BookingService: select each line, set “Pattern” to dashed and “Line end” to an open arrow. Save (Ctrl+S).
8. Open docs/design-notes.md, add the heading ## Where the methods live, and write one sentence each for Cancel() and HasOverlap(): which class, and why there.

**When you finish, you have:** the draw.io page “Classes” with all 8 boxes and 7 links, and a “Where the methods live” section in design-notes.md.
### Explanation

The class diagram is the plan for the code in Step 7: every box becomes a class, every method in a box becomes a method. When a rule changes, the diagram tells you which one class to change.



---

## 6. Step 5 · Draw the sequence diagrams

A sequence diagram shows one request as messages between objects, in time order from top to bottom.





### How to read it

| You see | It means |
| --- | --- |
| A box at the top with a dashed line below it | One participant (a person or an object) and its lifeline: time runs down the line. |
| A solid arrow with a filled head | A call: one object asks another to do something, for example HasOverlap(...). |
| A dashed arrow | A reply with the result, for example “false”. |
| Numbers 1, 2, 3… | The order. Read from top to bottom. |

### Completed diagram: Maya books R7

### Diagram: Maya books R7 for 12–14 November

Read from top to bottom. Before step 1 the store is empty. BookingService asks the store before it saves anything, saves R101 before sending the email, and only then answers Maya. Afterwards the store holds one Confirmed reservation: R101, Maya, R7, 12–14 November.

Diagram as text:

Maya sends BookingService: 1. Reserve(Maya, R7, 12–14 Nov). BookingService sends ReservationStore: 2. HasOverlap(R7, 12–14 Nov). ReservationStore replies to BookingService: 3. false: no booking shares a night. BookingService sends ReservationStore: 4. Save(R101, Confirmed). BookingService sends EmailSender: 5. SendConfirmation(R101). BookingService replies to Maya: 6. Accepted R101. Read from top to bottom. Before step 1 the store is empty. BookingService asks the store before it saves anything, saves R101 before sending the email, and only then answers Maya. Afterwards the store holds one Confirmed reservation: R101, Maya, R7, 12–14 November.

**Step by step:** Read from top to bottom. Each step below is one arrow.

1. Four participants stand at the top: Maya (the guest), BookingService, ReservationStore and EmailSender. The dashed line under each one is its lifeline: time runs downwards.
2. Arrow 1, solid, Maya → BookingService: Maya asks to book R7 for 12–14 November.

   C#:
   
   ```text
   service.Reserve(maya, r7, Nov(12, 14));
   ```
   
   Python:
   
   ```text
   service.reserve(MAYA, R7, nov(12, 14))
   ```
3. Arrow 2, solid, BookingService → ReservationStore: before saving anything, BookingService asks whether a Confirmed booking of R7 shares a night with 12–14.

   C#:
   
   ```text
   store.HasOverlap(room, stay)
   ```
   
   Python:
   
   ```text
   self.store.has_overlap(room, stay)
   ```
4. Arrow 3, dashed, back to BookingService: the store answers false, nobody has R7 on those nights. A dashed arrow is always the answer to an earlier call.

   C#:
   
   ```text
   if (store.HasOverlap(room, stay)) return new("Unavailable");   // false, so we go on
   ```
   
   Python:
   
   ```text
   if self.store.has_overlap(room, stay):   # False, so we go on
       return BookingResult("Unavailable")
   ```
5. Arrow 4, solid, BookingService → ReservationStore: BookingService creates reservation R101 and asks the store to save it.

   C#:
   
   ```text
   var reservation = new Reservation($"R{101 + store.Count}", guest, room, stay);
   store.Save(reservation);
   ```
   
   Python:
   
   ```text
   reservation = Reservation(f"R{101 + self.store.count()}", guest, room, stay)
   self.store.save(reservation)
   ```
6. Arrow 5, solid, BookingService → EmailSender: only now, after saving, the confirmation email is sent.

   C#:
   
   ```text
   email.SendConfirmation(reservation);
   ```
   
   Python:
   
   ```text
   self.email.send_confirmation(reservation)
   ```
7. Arrow 6, dashed, back to Maya: BookingService answers “Accepted R101”.

   C#:
   
   ```text
   return new("Accepted", reservation.Id);
   ```
   
   Python:
   
   ```text
   return BookingResult("Accepted", reservation.id)
   ```
8. Afterwards the store holds one reservation: R101, Maya, R7, 12–14 November, Confirmed.

**Legend:**
- **Box with a dashed line below it (participant and lifeline).** One person or object that takes part. Time runs down its dashed line. In this diagram: Maya, BookingService, ReservationStore and EmailSender.
- **Solid arrow with a filled head (call).** One participant asks another to do something. In code this is a method call. In this diagram: Arrow 2: BookingService calls store.HasOverlap(R7, 12–14 Nov).
- **Dashed arrow with an open head (reply).** The answer to an earlier call, going back to whoever asked. In this diagram: Arrow 3: the store answers false.
- **Numbers 1, 2, 3 … (order).** The order in which things happen. Read from top to bottom. In this diagram: Save (4) comes before SendConfirmation (5), so no email is ever sent for a booking that was not saved.

### Your turn · draw both sequence diagrams in draw.io

1. In diagrams.drawio, add a page and rename it to Sequence - book.
2. Search for lifeline and drag 4 lifeline shapes in a row. Name them Maya, BookingService, ReservationStore and EmailSender.
3. Draw the 6 messages from the completed diagram, from top to bottom: drag from one dashed line to the other, then double-click the arrow and type its number and text, for example “1. Reserve(Maya, R7, 12–14 Nov)”.
4. Make the replies (3 and 6) dashed: select the arrow and set “Pattern” to dashed in the Format panel.
5. Add another page and rename it to Sequence - cancel. Draw 4 lifelines: Maya, BookingService, ReservationStore and R101.
6. Draw the cancel request yourself: Maya asks BookingService to cancel R101; BookingService asks the store to find R101; the store replies with R101; BookingService asks R101 to cancel itself; R101 replies true; BookingService replies true to Maya. Number the messages 1–6.
7. Compare your cancel diagram with the answer below and fix any difference. Save (Ctrl+S).

**When you finish, you have:** two draw.io pages, “Sequence - book” and “Sequence - cancel”, each with 4 lifelines and 6 numbered messages.

### Check your answer: Maya cancels R101

### Diagram: Maya cancels R101

The reservation changes its own status; nothing is deleted. R101 stays in the store as Cancelled, so it no longer blocks R7 for 12–14 November and the history is kept.

Diagram as text:

Maya sends BookingService: 1. Cancel("R101"). BookingService sends ReservationStore: 2. Find("R101"). ReservationStore replies to BookingService: 3. R101. BookingService sends R101: 4. Cancel(). R101 replies to BookingService: 5. true: now Cancelled. BookingService replies to Maya: 6. true. The reservation changes its own status; nothing is deleted. R101 stays in the store as Cancelled, so it no longer blocks R7 for 12–14 November and the history is kept.

**Step by step:** Read from top to bottom. Here the fourth participant is not a class but one object: reservation R101 itself.

1. Four participants: Maya, BookingService, ReservationStore and R101, the reservation object Maya wants to cancel.
2. Arrow 1, solid, Maya → BookingService: Maya asks to cancel R101.

   C#:
   
   ```text
   service.Cancel("R101");
   ```
   
   Python:
   
   ```text
   service.cancel("R101")
   ```
3. Arrow 2, solid, BookingService → ReservationStore: BookingService asks the store to find R101.

   C#:
   
   ```text
   store.Find(reservationId)
   ```
   
   Python:
   
   ```text
   self.store.find(reservation_id)
   ```
4. Arrow 3, dashed: the store answers with the R101 object.
5. Arrow 4, solid, BookingService → R101: BookingService asks the reservation to cancel itself. The reservation changes its own status; nothing is deleted.

   C#:
   
   ```text
   public bool Cancel()
   {
       if (Status == ReservationStatus.Cancelled) return false;
       Status = ReservationStatus.Cancelled;
       return true;
   }
   ```
   
   Python:
   
   ```text
   def cancel(self) -> bool:
       if self.status == ReservationStatus.CANCELLED:
           return False
       self.status = ReservationStatus.CANCELLED
       return True
   ```
6. Arrow 5, dashed: R101 answers true, it is now Cancelled.
7. Arrow 6, dashed, back to Maya: BookingService answers true. R7 is free again for 12–14 November, and R101 stays in the store as history.

   C#:
   
   ```text
   public bool Cancel(string reservationId) => store.Find(reservationId)?.Cancel() ?? false;
   ```
   
   Python:
   
   ```text
   reservation = self.store.find(reservation_id)
   return reservation is not None and reservation.cancel()
   ```

**Legend:**
- **Box with a dashed line below it (participant and lifeline).** One person or object that takes part. Time runs down its dashed line. In this diagram: Maya, BookingService, ReservationStore and the reservation object R101.
- **Solid arrow with a filled head (call).** One participant asks another to do something. In code this is a method call. In this diagram: Arrow 4: BookingService calls Cancel() on R101.
- **Dashed arrow with an open head (reply).** The answer to an earlier call, going back to whoever asked. In this diagram: Arrow 5: R101 answers true.
- **Numbers 1, 2, 3 … (order).** The order in which things happen. Read from top to bottom. In this diagram: Find (2) must come before Cancel (4): you cannot cancel a reservation you have not found.
### Explanation

The order matters. Checking for an overlap must come before saving; saving must come before the email; the guest gets “Accepted” only at the end. Step 8 shows what happens when two requests run at the same time and their messages interleave between the check and the save.



---

## 7. Step 6 · Draw the activity diagram

An activity diagram shows the decisions a request goes through, including the paths where it stops early.





### How to read it

| You see | It means |
| --- | --- |
| A filled circle | Start. |
| A rounded rectangle | An action the app does, for example “Save the reservation”. |
| A diamond | A yes/no question. Follow the arrow with the right answer. |
| A circle with a dot inside | The successful end. |

### Completed diagram: the booking decision

### Diagram: The booking decision

Start at the filled circle. Each diamond asks one question; follow the labelled answer. The two boxes on the right end the request without saving anything. Only a request that passes both questions is saved, confirmed by email and answered with its id.

Diagram as text:

Reserve request received → Check-out after check-in?. Check-out after check-in? → No: InvalidDates · save nothing. Check-out after check-in? → Yes: A Confirmed booking shares a night?. A Confirmed booking shares a night? → Yes: Unavailable · save nothing. A Confirmed booking shares a night? → No: Save the reservation as Confirmed. Save the reservation as Confirmed → Send the confirmation email. Send the confirmation email → Accepted + reservation id. Start at the filled circle. Each diamond asks one question; follow the labelled answer. The two boxes on the right end the request without saving anything. Only a request that passes both questions is saved, confirmed by email and answered with its id.

**Step by step:** Follow the arrows from the filled circle. Each diamond is one if-statement at the top of Reserve.

1. The filled circle: a Reserve request arrives, with a guest, a room and dates.

   C#:
   
   ```text
   public BookingResult Reserve(Guest guest, Room room, DateRange stay)
   ```
   
   Python:
   
   ```text
   def reserve(self, guest: Guest, room: Room, stay: DateRange) -> BookingResult:
   ```
2. First diamond, “Check-out after check-in?”. No → the request ends at “InvalidDates · save nothing”. Yes → go down.

   C#:
   
   ```text
   if (!stay.IsValid()) return new("InvalidDates");
   ```
   
   Python:
   
   ```text
   if not stay.is_valid():
       return BookingResult("InvalidDates")
   ```
3. Second diamond, “A Confirmed booking shares a night?”. Yes → the request ends at “Unavailable · save nothing”. No → go down.

   C#:
   
   ```text
   if (store.HasOverlap(room, stay)) return new("Unavailable");
   ```
   
   Python:
   
   ```text
   if self.store.has_overlap(room, stay):
       return BookingResult("Unavailable")
   ```
4. Action “Save the reservation as Confirmed”.

   C#:
   
   ```text
   store.Save(reservation);
   ```
   
   Python:
   
   ```text
   self.store.save(reservation)
   ```
5. Action “Send the confirmation email”.

   C#:
   
   ```text
   email.SendConfirmation(reservation);
   ```
   
   Python:
   
   ```text
   self.email.send_confirmation(reservation)
   ```
6. The end circle: the guest gets “Accepted” and the reservation id.

   C#:
   
   ```text
   return new("Accepted", reservation.Id);
   ```
   
   Python:
   
   ```text
   return BookingResult("Accepted", reservation.id)
   ```
7. Example trace, Omar asking for R7 on 13–15 while Maya has 12–14: first diamond Yes, second diamond Yes (the night of the 13th) → Unavailable, nothing saved.

**Legend:**
- **Filled circle (start).** Where the flow begins. In this diagram: A Reserve request has arrived.
- **Diamond (decision).** A yes/no question. Only one of its outgoing arrows is followed. In this diagram: “A Confirmed booking shares a night?”: one if-statement in Reserve.
- **Rounded rectangle (action).** Something the app does. In this diagram: “Save the reservation as Confirmed”: store.Save(reservation).
- **Arrow, sometimes labelled Yes or No (flow).** What happens next. The label says which answer of the diamond leads there. In this diagram: From the second diamond, “Yes” leads to Unavailable and “No” leads to Save.
- **Circle with a dot inside (end).** The successful end of the flow. In this diagram: “Accepted + reservation id”.

### Completed trace: one request through the diagram

Omar asks for R7 on 13–15 November, while Maya has 12–14. First diamond: is check-out after check-in? Yes. Second diamond: does a Confirmed booking share a night? Yes, the 13th. The request ends at “Unavailable · save nothing”.

### Your turn · draw the activity diagram and trace two requests

1. In diagrams.drawio, add a page and rename it to Activity - book.
2. Draw the diagram like the completed one: search for ellipse for the start circle (set its fill to dark in the Format panel), rhombus for the two diamonds, and rounded rectangle for the actions. Connect them with arrows and type Yes / No on the arrows that leave each diamond.
3. Save (Ctrl+S).
4. Open docs/design-notes.md and add the heading ## Activity traces.
5. Write Omar’s trace exactly as in the completed trace above.
6. Write the trace for Nora, R7, 14–16 November in the same way, and then for Omar, R8, 15–15 November. For each one, name the answer at every diamond and where the request ends.

**When you finish, you have:** the draw.io page “Activity - book”, and three traces in design-notes.md.
### Explanation

The activity diagram and the sequence diagram describe the same Reserve method from two sides: the sequence shows who talks to whom, the activity shows the decisions. In Step 7 the two diamonds become the two if-statements at the top of Reserve.



---

## 8. Step 7 · Write the code in C# or Python

Type the classes from your class diagram, one piece at a time, then run the program and compare its output with your requirements.





### Create the project

Use the terminal in your editor. It already opens inside the hotel-booking folder, because you opened that folder at the start of the lesson. The tabs switch between C# and Python.

### Create the project

#### C#

```text
dotnet new console -n HotelBooking
(open HotelBooking/Program.cs and delete everything in it)
```

#### Python

```text
(create an empty file hotel_booking.py in the hotel-booking folder)
```

### The pieces, in the order you type them

Each piece is one box (or two small boxes) from your class diagram. After typing a piece, compare it with the box: same name, same data, same methods. In C#, piece 7.7 goes at the very top of Program.cs, above the classes. In Python, it goes at the bottom of hotel_booking.py.

### 7.1 · DateRange: the stay and the overlap rule

Start with the smallest class from the diagram. DateRange holds check-in and check-out and answers two questions: are the dates valid, and does this stay share a night with another one? This is the half-open rule from the requirements, written as code. In Python, the first piece also contains the imports for the whole file.

### 7.1 · DateRange: the stay and the overlap rule

#### C#

```csharp
// The stay: check-in day included, check-out day excluded (half-open).
readonly record struct DateRange(DateOnly CheckIn, DateOnly CheckOut)
{
    public bool IsValid() => CheckOut > CheckIn;

    // Two stays overlap when they share at least one night.
    public bool Overlaps(DateRange other) =>
        CheckIn < other.CheckOut && other.CheckIn < CheckOut;
}
```

#### Python

```python
"""P03 - Hotel booking, first version: one file, everything kept in memory.

python hotel_booking.py          -> the normal demo
python hotel_booking.py --race   -> two guests book the same room at the same moment
"""
from __future__ import annotations

import sys
import threading
import time
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
```

### 7.2 · Guest and Room: plain data

Guest and Room only carry values, so a C# record or a Python frozen dataclass is enough. Two Room values with the same number and type are equal, which is how the store recognises “the same room”.

### 7.2 · Guest and Room: plain data

#### C#

```csharp
record Guest(string Name, string Email);

record Room(string Number, string Type);
```

#### Python

```python
@dataclass(frozen=True)
class Guest:
    name: str
    email: str


@dataclass(frozen=True)
class Room:
    number: str
    type: str
```

### 7.3 · Reservation: knows its own status

Reservation links a guest, a room and a stay, and keeps its status. It answers “do I block this room for these nights?” and can cancel itself. Cancelling changes the status; it does not delete anything.

### 7.3 · Reservation: knows its own status

#### C#

```csharp
enum ReservationStatus { Confirmed, Cancelled }

class Reservation
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
```

### 7.4 · ReservationStore: where reservations are kept

The store keeps reservations in a list in memory. BookingService asks it whether a booking overlaps, saves new reservations and finds one by id. Later in the course this class is replaced by a real database, and the rest of the code should barely notice.

### 7.4 · ReservationStore: where reservations are kept

#### C#

```csharp
// Keeps reservations in a list. A later practice replaces it with a database.
class ReservationStore
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
# Keeps reservations in a list. A later practice replaces it with a database.
class ReservationStore:
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

### 7.5 · EmailSender and BookingResult

EmailSender is simulated: it only counts confirmations, so you need no email account. BookingResult is what Reserve returns: an outcome such as Accepted, Unavailable or InvalidDates, plus the id when a reservation was saved.

### 7.5 · EmailSender and BookingResult

#### C#

```csharp
// Simulated: sends nothing and needs no network. P04 moves this dependency behind an interface.
class EmailSender
{
    public int Sent { get; private set; }

    public void SendConfirmation(Reservation reservation) => Sent++;
}

record BookingResult(string Outcome, string? ReservationId = null)
{
    public override string ToString() => ReservationId is null ? Outcome : $"{Outcome} {ReservationId}";
}
```

#### Python

```python
# Simulated: sends nothing and needs no network. P04 moves this dependency behind an interface.
class EmailSender:
    def __init__(self) -> None:
        self.sent = 0

    def send_confirmation(self, reservation: Reservation) -> None:
        self.sent += 1


@dataclass(frozen=True)
class BookingResult:
    outcome: str
    reservation_id: str | None = None

    def __str__(self) -> str:
        return self.outcome if self.reservation_id is None else f"{self.outcome} {self.reservation_id}"
```

### 7.6 · BookingService: runs each request in order

This is the sequence diagram and the activity diagram as code: check the dates, check for an overlap, save, send the email, answer. FindFreeRooms is the “Search free rooms” use case and Cancel is “Cancel a booking”. The slow-step pause is 0 in the normal demo; Step 8 uses it to show what goes wrong.

### 7.6 · BookingService: runs each request in order

#### C#

```csharp
class BookingService(IReadOnlyList<Room> rooms, ReservationStore store, EmailSender email, int slowStepMs = 0)
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
        email.SendConfirmation(reservation);
        return new("Accepted", reservation.Id);
    }

    public bool Cancel(string reservationId) => store.Find(reservationId)?.Cancel() ?? false;
}
```

#### Python

```python
class BookingService:
    def __init__(self, rooms: list[Room], store: ReservationStore, email: EmailSender, slow_step_seconds: float = 0) -> None:
        self.rooms = rooms
        self.store = store
        self.email = email
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
        self.email.send_confirmation(reservation)
        return BookingResult("Accepted", reservation.id)

    def cancel(self, reservation_id: str) -> bool:
        reservation = self.store.find(reservation_id)
        return reservation is not None and reservation.cancel()
```

### 7.7 · The demo: Maya, Omar and Nora

The demo runs the requirement examples in order and prints one line per request. In C# this code goes at the top of Program.cs, before the classes. In Python it goes at the bottom of hotel_booking.py.

### 7.7 · The demo: Maya, Omar and Nora

#### C#

```csharp
// P03 · Hotel booking, first version: one file, everything kept in memory.
// dotnet run            -> the normal demo
// dotnet run -- --race  -> two guests book the same room at the same moment

var r7 = new Room("R7", "Standard");
var r8 = new Room("R8", "Deluxe");
var maya = new Guest("Maya", "maya@example.com");
var omar = new Guest("Omar", "omar@example.com");
var nora = new Guest("Nora", "nora@example.com");

if (args.Contains("--race")) RunRace(); else RunDemo();

void RunDemo()
{
    var store = new ReservationStore();
    var email = new EmailSender();
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
    var store = new ReservationStore();
    // Each request now spends half a second between checking and saving.
    var service = new BookingService([r7, r8], store, new EmailSender(), slowStepMs: 500);
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
    store = ReservationStore()
    email = EmailSender()
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
    store = ReservationStore()
    # Each request now spends half a second between checking and saving.
    service = BookingService([R7, R8], store, EmailSender(), slow_step_seconds=0.5)
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

### Expected output

Run the program with dotnet run --project HotelBooking (C#) or python hotel_booking.py (Python). It should print exactly these lines.

### Expected output of the demo

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

### Complete program for checking

This is the whole file, exactly as in the download. Use it to find a missing or mistyped piece.

### Complete program

#### C# · HotelBooking/Program.cs

C#: install the .NET 10 SDK. Everything is in one Program.cs file inside the HotelBooking project.

```csharp
// P03 · Hotel booking, first version: one file, everything kept in memory.
// dotnet run            -> the normal demo
// dotnet run -- --race  -> two guests book the same room at the same moment

var r7 = new Room("R7", "Standard");
var r8 = new Room("R8", "Deluxe");
var maya = new Guest("Maya", "maya@example.com");
var omar = new Guest("Omar", "omar@example.com");
var nora = new Guest("Nora", "nora@example.com");

if (args.Contains("--race")) RunRace(); else RunDemo();

void RunDemo()
{
    var store = new ReservationStore();
    var email = new EmailSender();
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
    var store = new ReservationStore();
    // Each request now spends half a second between checking and saving.
    var service = new BookingService([r7, r8], store, new EmailSender(), slowStepMs: 500);
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

// The stay: check-in day included, check-out day excluded (half-open).
readonly record struct DateRange(DateOnly CheckIn, DateOnly CheckOut)
{
    public bool IsValid() => CheckOut > CheckIn;

    // Two stays overlap when they share at least one night.
    public bool Overlaps(DateRange other) =>
        CheckIn < other.CheckOut && other.CheckIn < CheckOut;
}

record Guest(string Name, string Email);

record Room(string Number, string Type);

enum ReservationStatus { Confirmed, Cancelled }

class Reservation
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

// Keeps reservations in a list. A later practice replaces it with a database.
class ReservationStore
{
    private readonly List<Reservation> _rows = [];

    public int Count => _rows.Count;

    public bool HasOverlap(Room room, DateRange stay) => _rows.Any(row => row.Blocks(room, stay));

    public void Save(Reservation reservation) => _rows.Add(reservation);

    public Reservation? Find(string id) => _rows.FirstOrDefault(row => row.Id == id);

    public int CountConfirmed(Room room, DateRange stay) => _rows.Count(row => row.Blocks(room, stay));
}

// Simulated: sends nothing and needs no network. P04 moves this dependency behind an interface.
class EmailSender
{
    public int Sent { get; private set; }

    public void SendConfirmation(Reservation reservation) => Sent++;
}

record BookingResult(string Outcome, string? ReservationId = null)
{
    public override string ToString() => ReservationId is null ? Outcome : $"{Outcome} {ReservationId}";
}

class BookingService(IReadOnlyList<Room> rooms, ReservationStore store, EmailSender email, int slowStepMs = 0)
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
        email.SendConfirmation(reservation);
        return new("Accepted", reservation.Id);
    }

    public bool Cancel(string reservationId) => store.Find(reservationId)?.Cancel() ?? false;
}

```

#### Python · Python/hotel_booking.py

Python: install Python 3.10 or newer. Everything is in one hotel_booking.py file; no packages are needed.

```python
"""P03 - Hotel booking, first version: one file, everything kept in memory.

python hotel_booking.py          -> the normal demo
python hotel_booking.py --race   -> two guests book the same room at the same moment
"""
from __future__ import annotations

import sys
import threading
import time
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


# Keeps reservations in a list. A later practice replaces it with a database.
class ReservationStore:
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


# Simulated: sends nothing and needs no network. P04 moves this dependency behind an interface.
class EmailSender:
    def __init__(self) -> None:
        self.sent = 0

    def send_confirmation(self, reservation: Reservation) -> None:
        self.sent += 1


@dataclass(frozen=True)
class BookingResult:
    outcome: str
    reservation_id: str | None = None

    def __str__(self) -> str:
        return self.outcome if self.reservation_id is None else f"{self.outcome} {self.reservation_id}"


class BookingService:
    def __init__(self, rooms: list[Room], store: ReservationStore, email: EmailSender, slow_step_seconds: float = 0) -> None:
        self.rooms = rooms
        self.store = store
        self.email = email
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
        self.email.send_confirmation(reservation)
        return BookingResult("Accepted", reservation.id)

    def cancel(self, reservation_id: str) -> bool:
        reservation = self.store.find(reservation_id)
        return reservation is not None and reservation.cancel()


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
    store = ReservationStore()
    email = EmailSender()
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
    store = ReservationStore()
    # Each request now spends half a second between checking and saving.
    service = BookingService([R7, R8], store, EmailSender(), slow_step_seconds=0.5)
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

### Completed example: a proof line

A proof line connects a requirement with the program output that shows it works. Under R3 in requirements.md it looks like this:

### docs/requirements.md (under R3)

```text
Proof: Omar books R7 13-15 Nov: Unavailable
```

### Your turn · write, run and prove the program

1. Create the project with the commands above.
2. Type pieces 7.1 to 7.7 in order. Typist types, partner reads from the slide; swap after 7.4.
3. Before you run it: open docs/design-notes.md, add ## Prediction, and write which line you expect for Nora (Accepted or Unavailable, and which id).
4. Run the program and compare every line with the expected output. If a line differs, find the method that printed it and compare it with the slide.
5. In docs/requirements.md, add a “Proof:” line under each requirement R1–R5 with the output line that shows it works, like the completed example.
6. If you could not finish typing, download “Runnable code · C# and Python” from the top of this page, run it, and do steps 3–5 with it.

**When you finish, you have:** a program that runs and prints the nine expected lines, a prediction in design-notes.md, and a Proof line under every requirement.
### Explanation

The code follows the diagrams one to one: each class box is a class, each method in a box is a method, and Reserve is the sequence diagram with the two decisions of the activity diagram. If the code and the diagram disagree, one of them is wrong; fix it.



---

## 9. Step 8 · Two guests, one room, the same moment

The program passes every example from your requirements. Now Maya and Omar press Book for the same room at almost the same moment.





### The situation

On a real website many people use the app at the same time, and each request takes a little time, for example while a card is being charged. The race demo gives each request half a second between checking and saving. Maya clicks first; Omar clicks a tenth of a second later. Both want R7 for 12–14 November.

### What the program prints

You will run this in the steps below. This is the output you should get:

### Expected output of the race demo

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

### What happened, message by message

### Diagram: Maya and Omar book R7 for 12–14 November at the same moment

Both requests check before either one saves, so both checks see an empty store. Each request then saves on the strength of its own old answer. The store ends with two Confirmed reservations for the same room and nights. This is a race condition: the result depends on timing.

Diagram as text:

Maya sends BookingService: 1. Reserve(Maya, R7, 12–14 Nov). BookingService sends ReservationStore: 2. HasOverlap: false. Omar sends BookingService: 3. Reserve(Omar, R7, 12–14 Nov). BookingService sends ReservationStore: 4. HasOverlap: false. BookingService sends ReservationStore: 5. Save(R101, Maya). BookingService sends ReservationStore: 6. Save(R102, Omar). BookingService replies to Maya: 7. Accepted R101. BookingService replies to Omar: 8. Accepted R102. Both requests check before either one saves, so both checks see an empty store. Each request then saves on the strength of its own old answer. The store ends with two Confirmed reservations for the same room and nights. This is a race condition: the result depends on timing.

**Step by step:** Two requests run at the same time. Read from top to bottom and watch when each one checks and when it saves.

1. Arrow 1, at 0.0 s: Maya’s request asks BookingService to book R7 for 12–14 November.
2. Arrow 2: Maya’s request checks the store. It is empty, so HasOverlap answers false. Her request now waits half a second before saving (the slow step).

   C#:
   
   ```text
   if (store.HasOverlap(room, stay)) return new("Unavailable");   // false
   Thread.Sleep(slowStepMs);                                      // Maya waits here
   ```
   
   Python:
   
   ```text
   if self.store.has_overlap(room, stay):   # False
       return BookingResult("Unavailable")
   time.sleep(self.slow_step_seconds)       # Maya waits here
   ```
3. Arrow 3, at 0.1 s: Omar’s request arrives for the same room and dates.
4. Arrow 4: Omar’s request checks the store. Maya has not saved yet, so the store is still empty and the answer is false again. This is the bug.
5. Arrow 5, at 0.5 s: Maya’s request saves R101.

   C#:
   
   ```text
   store.Save(reservation);   // R101 for Maya
   ```
   
   Python:
   
   ```text
   self.store.save(reservation)   # R101 for Maya
   ```
6. Arrow 6, at 0.6 s: Omar’s request saves R102. It never checks again; it trusts its old answer.
7. Arrows 7 and 8: both guests get Accepted. R7 now has two Confirmed reservations for the same nights, and requirement R3 is broken.

**Legend:**
- **Box with a dashed line below it (participant and lifeline).** One person or object that takes part. Time runs down its dashed line. In this diagram: Maya, Omar, BookingService and ReservationStore.
- **Solid arrow with a filled head (call).** One participant asks another to do something. In code this is a method call. In this diagram: Arrows 2 and 4: the two HasOverlap checks.
- **Dashed arrow with an open head (reply).** The answer to an earlier call, going back to whoever asked. In this diagram: Arrows 7 and 8: “Accepted R101” and “Accepted R102”.
- **Numbers 1, 2, 3 … (order).** The order in which things happen. Read from top to bottom. In this diagram: Both checks (2 and 4) happen before either save (5 and 6). That order is the race condition.

### The same story on a timeline

| Time | Maya’s request | Omar’s request | Reservations in the store |
| --- | --- | --- | --- |
| 0.0 s | Checks R7: nobody there | — | none |
| 0.1 s | Waiting | Checks R7: nobody there | none |
| 0.5 s | Saves R101 → Accepted | Waiting | R101 |
| 0.6 s | — | Saves R102 → Accepted | R101 and R102, same room, same nights |

### Why it happens: check, then act

Reserve first checks (“is R7 free?”) and then acts (“save”). Between the two, another request can change the answer. Both requests acted on an answer that was already out of date. When the result depends on which request runs first, it is called a race condition. Requirement R3 is broken, even though every single-request example passed.

We do not fix it today. A lock in the code helps only while the app runs as one program on one computer, and real hotels run several copies. P09 fixes it properly in the database, after the app has one.

**Check, then act: an everyday example** In short: Between “checking” and “acting”, someone else can change the thing you checked. In our code the “check” is HasOverlap() and the “act” is Save(). The race demo makes the gap between them half a second long.

1. Two drivers see the same empty parking spot from opposite ends of the street.
2. Both check: “free!”.
3. Both drive in.
4. Each check was correct when it was made, but by the time they act it is no longer true.

C#:

```text
if (store.HasOverlap(room, stay)) return new("Unavailable");   // check
Thread.Sleep(slowStepMs);                                      // the gap
store.Save(reservation);                                       // act
```

Python:

```text
if self.store.has_overlap(room, stay):          # check
    return BookingResult("Unavailable")
time.sleep(self.slow_step_seconds)              # the gap
self.store.save(reservation)                    # act
```

### Your turn · run the race and explain it

1. Open docs/design-notes.md and add the heading ## The race.
2. Under it, write your prediction: when Maya and Omar book R7 for 12–14 November at the same moment, how many Confirmed reservations will R7 have?
3. Run the race demo: dotnet run --project HotelBooking -- --race (C#) or python hotel_booking.py --race (Python).
4. Compare the output with the expected output above and with your prediction.
5. Under your prediction, write two sentences: what happened, and between which two calls in Reserve the problem appears.

**When you finish, you have:** the race output on your screen, and a prediction plus a two-sentence explanation in design-notes.md.
### Explanation

Testing one request at a time could never find this bug, because each request on its own is correct. The bug lives in the gap between check and save. Much of the course is about closing that gap: locks in code, locks and constraints in SQL, retries, messages and sagas.



---

## 10. Step 9 · Check your project and see what comes next

Check your folder against the list, then see how the same app changes in the next practices.





### Completed example: the finished P03 folder

### hotel-booking folder

```text
hotel-booking/
├── docs/
│   ├── requirements.md     R1–R5, examples and proof lines
│   ├── design-notes.md     your short answers from each step
│   └── diagrams.drawio     5 pages: Use cases, Classes,
│                           Sequence - book, Sequence - cancel, Activity - book
├── HotelBooking/           if you chose C#: Program.cs, HotelBooking.csproj
└── hotel_booking.py        if you chose Python
```

### Your P03 checklist

| File or page | It is finished when |
| --- | --- |
| docs/requirements.md | R1–R5 each have a Check line and a Proof line; R3 has five examples. |
| docs/design-notes.md | It has: Why design before code, Where the methods live, Activity traces, Prediction, The race. |
| diagrams.drawio · Use cases | 2 actors outside the box, 4 goals inside, 8 lines. |
| diagrams.drawio · Classes | 8 boxes and 7 links, matching the completed class diagram. |
| diagrams.drawio · Sequence - book and Sequence - cancel | 4 lifelines and 6 numbered messages on each page. |
| diagrams.drawio · Activity - book | Start, two diamonds with Yes/No, the actions and the end. |
| Program | Runs, prints the nine demo lines, and --race shows the double booking. |

### Where the hotel app goes next

| Practice | What changes in the hotel app |
| --- | --- |
| P04 | Clean up the design with SOLID: BookingService stops depending directly on EmailSender. Same behaviour, better structure. |
| P05 · P06 | Split the hotel into business areas (rooms, reservations, payments, notifications) and turn them into modules in the code. |
| P07 · P08 | Put the app behind an HTTP API, store reservations in PostgreSQL and speed up search with a cache. |
| P09 | Run today’s race against the database and fix it with database locks and constraints. |
| Later | Retries, separate services, RabbitMQ and Kafka, saga and outbox, monitoring with Grafana. |

### Your turn · check and hand over your project

1. Open every file and draw.io page from the checklist and tick it off in your head. Fix anything that is missing.
2. Run the program once more, normally and with --race, to make sure it still works.
3. Swap laptops with the pair next to you for one minute: they read your design-notes.md “The race” section and tell you whether it is clear.
4. Copy the whole hotel-booking folder to your partner’s laptop (for example as a ZIP in a message), so both of you have it.
5. Keep the folder. P04 starts from it.

**When you finish, you have:** the complete P03 project: requirements, five diagram pages, design notes and a running program, on both partners’ laptops.
### Explanation

You now have version 1 of the course project and its first real bug. Keep the folder: P04 starts from this design and improves it without changing what it does.
