# Quality scenarios

## Stakeholders

| Who | What they care about |
| --- | --- |
| Guest | Finds a free room quickly; never loses a booking. |
| Receptionist | Never promises one room twice; can cancel at the desk. |
| Hotel manager (domain expert) | Knows the rules; wants deposits and SMS. |
| Development team | Can change one business area without breaking the others. |
| Email/SMS and payment providers | External systems that can be slow or unavailable. |

## S3 · Availability: a failed message keeps the booking

| Part | S3 |
| --- | --- |
| Source | The email provider. |
| Stimulus | It stops answering. |
| Environment | For 10 minutes, during normal bookings. |
| Artifact | BookingService.reserve and the notification sender. |
| Response | Every valid booking is still Accepted and saved; the failed message is reported. |
| Measure | 0 lost bookings; 100% of failed messages reported. |
| Checked in | The existing R6 test verifies a simulated sending failure; the 10-minute provider outage remains a target for failure-handling checks. |

## S1 · Performance

| Part | S1 |
| --- | --- |
| Source | 200 guests. |
| Stimulus | They search for free rooms for 12–14 November. |
| Environment | All 200 searches arrive within one minute. |
| Artifact | BookingService.findFreeRooms. |
| Response | Return the correct free rooms promptly. |
| Measure | p95 response time ≤ 1 second, with correct results for every search. |
| Checked in | Planned caching benchmarks and load tests. |

## S2 · Data integrity

| Part | S2 |
| --- | --- |
| Source | 100 pairs of guests. |
| Stimulus | Each pair tries to book the same room for the same nights simultaneously. |
| Environment | Concurrent requests against a fresh available room and stay for each pair. |
| Artifact | BookingService.reserve and reservation storage. |
| Response | Accept exactly one booking per pair and refuse the conflicting request. |
| Measure | Exactly 1 Confirmed reservation per pair; 0 double bookings across 100 pairs. |
| Checked in | Planned database concurrency checks; the current race demo deliberately demonstrates the unresolved problem. |

## S4 · Security

| Part | S4 |
| --- | --- |
| Source | A guest. |
| Stimulus | The guest makes 100 attempts to read another guest’s reservation. |
| Environment | Normal operation with guest identity and access controls in place. |
| Artifact | Reservation lookup and authorization. |
| Response | Deny access to reservations belonging to another guest. |
| Measure | 0 reservations disclosed to the wrong guest across 100 attempts. |
| Checked in | Planned authorization and security tests. |

## S5 · Modifiability

| Part | S5 |
| --- | --- |
| Source | The hotel manager. |
| Stimulus | The hotel adds SMS confirmations. |
| Environment | The booking app already uses NotificationSender and email confirmations. |
| Artifact | NotificationSender implementations and their tests. |
| Response | Add an SMS sender and its test without editing BookingService. |
| Measure | 0 edits to BookingService; at most 2 files change. |
| Checked in | Verified when SmsNotificationSender and its test were added; the existing SMS test confirms the sender works with BookingService. |

## S6 · Sustainability

| Part | S6 |
| --- | --- |
| Source | A benchmark client issuing 1,000 searches. |
| Stimulus | Repeat the same search workload before and after an optimization. |
| Environment | The same hardware, data and workload, with 1,000 completed searches in each run. |
| Artifact | Free-room search and its database/cache access. |
| Response | Complete the same useful work correctly while consuming less energy. |
| Measure | At least 20% fewer watt-hours per 1,000 completed searches than the baseline. |
| Checked in | Planned caching comparisons and energy measurements. |
