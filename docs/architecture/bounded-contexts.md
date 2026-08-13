# EventFlow — Bounded Contexts

**Status:** Draft
**Version:** 0.1

## 1. Purpose

This document defines the initial bounded contexts of EventFlow.

A bounded context represents a clear business area with its own responsibilities and data ownership.

A bounded context does not always mean a separate microservice.

---

## 2. Context Overview

The initial EventFlow domain is divided into these contexts:

* Identity
* Event Catalog
* Booking
* Payment
* Ticketing
* Notification
* Search

The core business logic is mainly inside:

* Event Catalog
* Booking

Other contexts support the main booking flow.

---

## 3. Identity Context

### Responsibility

Identity manages user identity and authentication-related data.

It owns:

* user identifier;
* authentication credentials;
* account status;
* security-related identity data.

Other contexts should reference users only by `UserId`.

Identity does not own events, bookings, tickets, or payments.

---

## 4. Event Catalog Context

### Responsibility

Event Catalog manages event information and ticket configuration.

It owns:

* Event;
* TicketType;
* event schedule;
* event location;
* organizer ownership;
* publication state;
* ticket price configuration;
* ticket sales period.

Main responsibilities:

* create and update events;
* configure ticket types;
* publish events;
* cancel events;
* provide authoritative event data.

Event Catalog does not manage active reservations or sold ticket inventory.

---

## 5. Booking Context

### Responsibility

Booking manages the attendee purchase flow and ticket inventory allocation.

It owns:

* Booking;
* BookingItem;
* TicketInventory;
* reservation expiration;
* booking state;
* reserved and sold quantities.

Main responsibilities:

* create temporary reservations;
* check ticket availability;
* prevent overselling;
* reserve and release inventory;
* start the payment flow;
* confirm bookings;
* expire unfinished reservations;
* process booking cancellation rules.

Ticket inventory belongs to Booking because reservation and inventory consistency are closely connected.

The operation:

```text
check availability
+
reserve quantity
```

must be handled as one consistency-sensitive business operation.

---

## 6. Payment Context

### Responsibility

Payment handles payment processing and provider-specific integration.

It owns:

* Payment;
* payment status;
* provider reference;
* payment processing state.

Main responsibilities:

* initiate payment;
* process payment results;
* handle repeated payment callbacks safely;
* expose payment result to Booking.

Payment does not decide whether a booking should become confirmed.

Booking remains responsible for booking state changes.

---

## 7. Ticketing Context

### Responsibility

Ticketing manages issued admission tickets.

It owns:

* Ticket;
* ticket identifier;
* ticket issuance state.

Tickets may only be created for confirmed bookings.

Ticketing is a separate domain responsibility, but it does not need to become a separate microservice in the first version.

It may initially remain inside the Booking service.

---

## 8. Notification Context

### Responsibility

Notification sends messages caused by business events.

Examples:

* booking confirmed;
* reservation expired;
* payment failed;
* ticket issued;
* event cancelled.

Notification must not change Booking or Event business state.

Notification failure must not cancel a successful booking.

This context is suitable for asynchronous processing.

---

## 9. Search Context

### Responsibility

Search provides fast event discovery.

It may store a search-oriented representation of:

* event title;
* category;
* city;
* date;
* description;
* publication state.

Search is not the source of truth.

Event Catalog remains authoritative for event data.

Search data may be rebuilt from Event Catalog if necessary.

Eventual consistency is acceptable for search updates.

---

## 10. Data Ownership

| Context       | Owned Data                                   |
| ------------- | -------------------------------------------- |
| Identity      | User identity and authentication data        |
| Event Catalog | Events and ticket type configuration         |
| Booking       | Bookings, reservations, inventory allocation |
| Payment       | Payment records and provider references      |
| Ticketing     | Issued tickets                               |
| Notification  | Notification delivery state                  |
| Search        | Derived search data                          |

A context must not directly modify another context's database.

Communication must use explicit contracts.

---

## 11. Consistency Rules

Strong consistency is required for:

* ticket availability;
* inventory reservation;
* inventory release;
* booking state transitions.

Eventual consistency is acceptable for:

* notifications;
* search synchronization;
* analytics;
* other derived data.

Payment and Booking coordination requires idempotent processing and explicit failure handling.

---

## 12. Initial Service Direction

Bounded contexts will not be mapped one-to-one to microservices.

The initial service structure is expected to be:

```text
identity-service
event-service
booking-service
notification-service
```

Initial mapping:

```text
Identity        -> identity-service
Event Catalog   -> event-service
Booking         -> booking-service
Notification    -> notification-service
```

For the first version:

* Payment may remain behind a dedicated boundary inside `booking-service`;
* Ticketing may remain inside `booking-service`;
* Search may initially stay close to `event-service`.

They may become separate services later if their complexity or scaling needs justify it.

---

## 13. Why Some Services Are Not Separate Yet

The following services are intentionally not created at the beginning:

```text
inventory-service
payment-service
ticket-service
search-service
```

Splitting them too early would add:

* more network calls;
* more deployment units;
* more failure points;
* more distributed consistency problems;
* more operational complexity.

A new service should be introduced only when there is a clear business or technical reason.

---

## 14. Context Relationship Summary

```text
Identity
   |
   | UserId
   v

Event Catalog --------> Search
      |
      |
      v
    Booking --------> Payment
      |
      +-------------> Ticketing
      |
      +-------------> Notification
```

The arrows show logical dependencies only.

The final communication type — REST or asynchronous messaging — will be defined in the next architecture step.

---

## 15. Next Design Step

The next step is to define context interactions.

It should decide:

* which interactions use synchronous communication;
* which interactions use asynchronous events;
* which domain events cross context boundaries;
* where idempotency is required;
* how booking and payment failures are handled;
* whether an Outbox Pattern is needed.
