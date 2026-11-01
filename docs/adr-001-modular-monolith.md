# ADR-001: Start the hotel booking app as a modular monolith

Status: Accepted, 1 November 2026

## Context

One small team builds and releases the whole app. The hotel has four business areas: Rooms, Reservations, Payments and Notifications. The most important quality scenarios are S2 (no double booking) and S3 (a failed message keeps the booking). No module currently needs an independent release schedule or independent scaling.

## Decision

Keep one application with four modules. Each module has a public contract and an internal implementation package. Reservations owns the booking rule and uses the public contracts of other modules. Main creates the concrete implementations and connects them; tests may also construct internal implementations.

Architecture tests enforce three dependency rules: Reservations cannot use another module’s internals, Rooms depends on no other module, and Payments and Notifications do not depend on Reservations.

## Alternatives considered

Separate services for Payments and Notifications would allow independent releases and scaling, but would introduce network failures, retries, additional deployments and more monitoring before those benefits are needed.

## Consequences

- One build and one deployment, with local method calls between modules.
- One owner for the booking rule, rather than copies of it in several modules.
- All modules are released together.
- The boundaries are enforced only when the architecture tests run; continuous integration should run them too.
- The intentional check-then-save race remains unresolved; module separation does not itself satisfy S2 under concurrent bookings.

## Revisit when

Reconsider when measurements show a module needs independent scaling or an independent release schedule, and the benefits justify the extra operational cost of a separate service.
