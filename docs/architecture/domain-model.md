# EventFlow — Domain Model

**Status:** Draft
**Version:** 0.1

## 1. Purpose

This document defines the initial domain model for EventFlow.

The model focuses on business concepts, invariants, aggregate boundaries, and domain events. It intentionally does not define microservice boundaries, database schemas, REST APIs, or infrastructure technologies.

---

## 2. Ubiquitous Language

| Term             | Meaning                                                                       |
| ---------------- | ----------------------------------------------------------------------------- |
| **Event**        | An event published by an organizer for which tickets may be sold.             |
| **Ticket Type**  | A purchasable ticket category for an event, such as General Admission or VIP. |
| **Inventory**    | The quantity of tickets available for a specific ticket type.                 |
| **Booking**      | The attendee's ticket purchase workflow.                                      |
| **Reservation**  | A temporary inventory hold created as part of a booking.                      |
| **Booking Item** | A requested quantity of a particular ticket type within a booking.            |
| **Ticket**       | An issued admission credential created after successful booking confirmation. |
| **Organizer**    | An actor responsible for managing owned events.                               |
| **Attendee**     | An actor who reserves and purchases tickets.                                  |

---

## 3. Core Domain

The most important business problem in EventFlow is:

> Safely allocating limited ticket inventory while multiple attendees may attempt to reserve the same tickets concurrently.

For this reason, booking and inventory consistency form the core domain.

Event management, payment integration, search, and notification support the core workflow but should not weaken inventory correctness.

---

## 4. Aggregate: Event

### Aggregate Root

`Event`

### Responsibilities

The Event aggregate owns the lifecycle and commercial configuration of an event.

It is responsible for:

* event identity;
* organizer ownership;
* name and description;
* schedule;
* location;
* lifecycle state;
* ticket type configuration;
* publication rules.

### Event State

```text
DRAFT
  |
  v
PUBLISHED
  |
  v
COMPLETED

DRAFT/PUBLISHED -> CANCELLED
```

### Invariants

* only a draft event may be published;
* an event must contain valid ticket configuration before publication;
* a cancelled event cannot return to the published state;
* a completed event cannot accept new ticket sales;
* event ownership cannot change implicitly;
* changes that invalidate existing purchases must not be allowed after publication.

### Child Entity: TicketType

A `TicketType` defines the commercial characteristics of a ticket category.

Typical attributes:

```text
TicketTypeId
name
price
salesStart
salesEnd
```

Ticket inventory is intentionally not treated as mutable state inside the Event aggregate.

Inventory has different concurrency requirements and should be modeled independently.

---

## 5. Aggregate: TicketInventory

### Aggregate Root

`TicketInventory`

One inventory aggregate exists for each sellable ticket type.

Conceptually:

```text
TicketInventory
    ticketTypeId
    totalQuantity
    reservedQuantity
    soldQuantity
```

### Responsibilities

TicketInventory owns the allocation of limited ticket capacity.

It answers the business question:

> Can this quantity still be reserved?

### Core Invariant

At all times:

```text
reservedQuantity + soldQuantity <= totalQuantity
```

Other invariants:

* quantities cannot be negative;
* a reservation cannot exceed currently available inventory;
* confirmed inventory cannot be released as if it were only temporarily reserved;
* expired or cancelled reservations must release their reserved quantity exactly once.

### Derived Availability

```text
availableQuantity =
    totalQuantity
    - reservedQuantity
    - soldQuantity
```

Availability is derived state and must not be independently modified.

### Important Note

The implementation mechanism for protecting this aggregate under concurrent access is intentionally undecided.

Possible strategies will be evaluated later.

---

## 6. Aggregate: Booking

### Aggregate Root

`Booking`

A Booking represents one attendee's purchase workflow.

A separate `Reservation` aggregate is not introduced in the initial model.

Temporary reservation is modeled as a state of Booking because reservation and purchase belong to the same business lifecycle.

### Booking Items

A Booking contains one or more `BookingItem` entries.

Each item contains:

```text
ticketTypeId
quantity
unitPrice
```

The price used for the booking must be captured at reservation time rather than recalculated from mutable event data later.

### Booking Lifecycle

```text
RESERVED
    |
    v
PAYMENT_PENDING
    |
    v
CONFIRMED
```

Alternative transitions:

```text
RESERVED -> EXPIRED

PAYMENT_PENDING -> PAYMENT_FAILED

CONFIRMED -> CANCELLED
```

### Responsibilities

Booking owns:

* attendee identity;
* selected ticket items;
* price snapshot;
* reservation expiration time;
* booking state;
* payment correlation information;
* booking confirmation state.

### Invariants

* a booking must contain at least one item;
* every quantity must be greater than zero;
* an expired booking cannot be confirmed;
* only a reserved booking may begin payment;
* a booking may be confirmed only after successful payment;
* confirmation must occur at most once;
* a cancelled booking cannot return to confirmed state;
* booking ownership cannot change after creation.

---

## 7. Reservation

Reservation is a domain concept, but not an independent aggregate in the initial model.

A reservation exists while:

```text
booking.status == RESERVED
```

and:

```text
currentTime < booking.expiresAt
```

Creating a reservation requires both:

1. creation of the Booking;
2. successful reservation of required TicketInventory.

If the booking expires, the associated inventory must eventually be released.

The exact consistency mechanism between Booking and TicketInventory will be defined during architecture design.

---

## 8. Payment

Payment is an external process interacting with the Booking domain.

The domain does not assume a specific payment provider.

Relevant concepts are:

```text
paymentId
bookingId
status
providerReference
```

Expected payment states:

```text
INITIATED
SUCCEEDED
FAILED
```

### Invariants

* one successful payment must confirm a booking at most once;
* duplicate successful payment notifications must be safe;
* payment failure must not consume inventory permanently;
* payment success must remain recoverable if later processing temporarily fails.

Payment-provider-specific objects must not leak into the core Booking model.

---

## 9. Ticket

A Ticket represents admission created from a confirmed booking.

A ticket contains at least:

```text
TicketId
BookingId
TicketTypeId
EventId
AttendeeId
```

### Invariants

* a ticket may only be issued from a confirmed booking;
* every ticket identifier must be unique;
* the number of issued tickets must match the confirmed booking quantities;
* issuing the same booking more than once must not produce duplicate tickets.

The initial model does not require ticket validation or scanning behavior.

If ticket validation later becomes substantial, Ticket may evolve into a richer independent aggregate.

---

## 10. Value Objects

The initial domain model should prefer value objects for concepts without independent identity.

Expected value objects include:

### Money

```text
amount
currency
```

Money should not be represented by floating-point values.

### EventSchedule

```text
startsAt
endsAt
```

Invariant:

```text
startsAt < endsAt
```

### ReservationPeriod

```text
createdAt
expiresAt
```

Invariant:

```text
createdAt < expiresAt
```

### TicketQuantity

Represents a strictly positive ticket quantity.

Using explicit value objects for these concepts prevents invalid values from spreading through the domain model.

---

## 11. Domain Events

Domain events describe meaningful business facts that have already happened.

Initial events include:

```text
EventPublished
EventCancelled

BookingReserved
BookingExpired
BookingPaymentStarted
BookingConfirmed
BookingCancelled

InventoryReserved
InventoryReleased
InventorySold

PaymentSucceeded
PaymentFailed

TicketIssued
```

These are domain concepts.

Their existence does not imply that every event must be published through Kafka.

Transport mechanisms will be selected later.

---

## 12. Cross-Aggregate Business Flow

The primary successful flow is:

```text
Event is published
        |
        v
Booking reservation requested
        |
        v
TicketInventory reserves quantity
        |
        v
Booking becomes RESERVED
        |
        v
Payment starts
        |
        v
Payment succeeds
        |
        v
Booking becomes CONFIRMED
        |
        v
Reserved inventory becomes SOLD
        |
        v
Tickets are issued
```

Failure paths must handle inventory release and idempotent recovery.

---

## 13. Transactional Boundaries

The following operations require strong local consistency inside their aggregate:

### TicketInventory

```text
check availability
+
reserve inventory
```

must behave as one atomic domain operation.

### Booking

```text
validate current state
+
perform state transition
```

must behave atomically.

Cross-aggregate workflows must not rely on modifying multiple aggregates through one shared object graph.

The coordination strategy will be defined during architecture design.

---

## 14. Aggregate Summary

```text
Event
 └── TicketType

TicketInventory

Booking
 └── BookingItem

Ticket
```

External/supporting concepts:

```text
Payment
Notification
Search
Identity
```

This model does not imply equivalent microservice boundaries.

---

## 15. Open Design Questions

The following decisions remain intentionally open:

* how Booking and TicketInventory consistency should be coordinated;
* which aggregate is the authoritative source when a reservation expires;
* how reservation expiration is triggered;
* whether payment requires its own aggregate later;
* whether Ticket should remain lightweight or become a richer aggregate;
* which domain events require external publication;
* where bounded-context boundaries should be placed;
* which consistency guarantees require synchronous interaction.

These questions should be answered before defining concrete service boundaries.
