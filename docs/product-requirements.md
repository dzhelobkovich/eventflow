# EventFlow — Product Requirements

**Status:** Draft  
**Version:** 0.1

## 1. Product Overview

EventFlow is an event discovery and ticket booking platform.

The platform allows event organizers to create and publish events, while customers can discover events, reserve tickets, complete purchases, and access issued tickets.

The core challenge of the system is reliable management of limited ticket inventory under concurrent demand.

EventFlow must prevent overselling, correctly handle temporary reservations, process payments safely, and maintain a consistent booking state even when parts of the system temporarily fail.

---

## 2. Product Goals

The initial version of EventFlow should support:

- event creation and publication;
- ticket type and inventory configuration;
- event discovery and filtering;
- temporary ticket reservation;
- protection against ticket overselling;
- payment processing;
- booking confirmation;
- ticket issuance;
- reservation expiration;
- event cancellation;
- customer notifications.

The project should prioritize correctness and reliability of the booking workflow over the number of implemented features.

---

## 3. Non-Goals

The initial version will not include:

- ticket resale;
- dynamic pricing;
- recommendation systems;
- advanced seat maps;
- organizer payouts;
- loyalty programs;
- fraud detection;
- social networking features;
- native mobile applications.

These features may be considered later if they introduce a meaningful product or engineering requirement.

---

## 4. System Actors

System actors describe how different participants interact with EventFlow.

They do not define the technical authorization model.

### 4.1 Attendee

An attendee uses EventFlow to discover events and purchase tickets.

An attendee should be able to:

- browse published events;
- search and filter events;
- view event details;
- view available ticket types;
- temporarily reserve tickets;
- initiate payment;
- view their own bookings;
- access issued tickets;
- cancel a booking when cancellation is allowed.

An attendee must not be able to access or modify another attendee's private booking data.

### 4.2 Organizer

An organizer creates and manages events.

An organizer should be able to:

- create an event;
- edit an owned event;
- configure ticket types;
- configure ticket inventory;
- publish an event;
- view booking information for owned events;
- cancel an owned event.

An organizer must not be able to modify events owned by another organizer.

### 4.3 Platform Operator

A platform operator performs privileged platform-level operations.

Possible responsibilities include:

- moderating published events;
- restricting invalid or abusive content;
- restricting abusive accounts;
- resolving exceptional platform-level situations.

Detailed administration functionality is outside the initial implementation scope.

---

## 5. Event Lifecycle

An event initially follows this lifecycle:

```text
DRAFT
  |
  v
PUBLISHED
  |
  v
COMPLETED
```

An event may also become:

```text
CANCELLED
```

### DRAFT

The event is not publicly available.

The organizer may configure event information, ticket types, inventory, location, date, and sales periods.

### PUBLISHED

The event is publicly discoverable and eligible tickets may be reserved.

### CANCELLED

New reservations are prohibited.

Existing reservations and confirmed bookings must be handled according to cancellation rules.

### COMPLETED

The event has already taken place and new reservations are prohibited.

---

## 6. Ticket Types and Inventory

An event may contain one or more ticket types.

Examples:

- General Admission;
- VIP;
- Early Bird;
- Student.

A ticket type should contain at least:

- identifier;
- event reference;
- name;
- price;
- total inventory;
- sales start time;
- sales end time.

Ticket inventory is a critical business resource.

The system must guarantee that the number of reserved and confirmed tickets never exceeds the configured inventory.

For example, if only one ticket remains and several customers attempt to reserve it simultaneously, only one reservation may succeed.

This rule must be enforced by the backend.

---

## 7. Temporary Reservation

Selecting tickets does not immediately complete a purchase.

EventFlow creates a temporary reservation that temporarily removes the requested quantity from available inventory.

Every reservation must have an expiration time.

Initial assumption:

```text
Reservation lifetime: 10 minutes
```

The duration should be configurable.

While the reservation is active:

- reserved inventory is unavailable to other customers;
- the customer may proceed with payment;
- the reservation cannot exceed available inventory.

If the reservation expires before the purchase is completed, the reserved inventory must become available again.

---

## 8. Booking Lifecycle

The initial booking lifecycle is:

```text
RESERVED
    |
    v
PAYMENT_PENDING
    |
    v
CONFIRMED
```

Possible alternative outcomes:

```text
RESERVED -> EXPIRED
PAYMENT_PENDING -> PAYMENT_FAILED
CONFIRMED -> CANCELLED
```

The exact state machine will be refined during domain modeling.

---

## 9. Booking Business Rules

The following business rules must hold:

- a booking belongs to exactly one attendee;
- requested ticket quantity must be positive;
- sufficient inventory must exist before a reservation is accepted;
- an expired reservation cannot be confirmed;
- a successful purchase must not be processed twice;
- tickets may only be issued for a confirmed booking;
- users may only access bookings they are authorized to access;
- cancellation must follow defined business rules.

Business rules must be enforced by the backend independently of client behavior.

---

## 10. Payment Requirements

Payment is an external operation from the perspective of the booking domain.

The system must distinguish at least:

```text
PAYMENT_INITIATED
PAYMENT_SUCCEEDED
PAYMENT_FAILED
```

Payment processing must support idempotent handling.

Receiving the same successful payment confirmation multiple times must not:

- confirm a booking multiple times;
- consume inventory multiple times;
- issue duplicate tickets;
- trigger duplicate business side effects.

The first implementation may use a simulated payment provider behind an abstraction before integrating a real payment provider.

---

## 11. Ticket Issuance

A successfully confirmed booking results in ticket issuance.

Each issued ticket must:

- have a unique identifier;
- reference a confirmed booking;
- reference the corresponding event;
- reference the corresponding ticket type;
- belong to the appropriate attendee.

Advanced entrance validation and ticket scanning are outside the initial scope.

---

## 12. Event Discovery

Customers should be able to discover published events.

Initial search and filtering criteria should include:

- title;
- category;
- city;
- date range.

The implementation technology for search is intentionally not defined at this stage.

---

## 13. Event Cancellation

An organizer may cancel an owned event when business rules permit it.

After cancellation:

- new reservations must be rejected;
- active reservations must no longer be confirmable;
- confirmed bookings must enter a cancellation or refund workflow;
- affected attendees should eventually be notified.

A complete refund implementation is not required for the first milestone, but the domain model should allow it to be introduced later.

---

## 14. Reliability Requirements

The system must assume that infrastructure and external dependencies may temporarily fail.

The architecture must account for:

- concurrent requests;
- duplicate operations;
- retry behavior;
- temporary downstream failures;
- partial workflow failures;
- reservation expiration;
- recovery after failure.

Failure of a secondary operation, such as notification delivery, must not invalidate an otherwise successfully completed booking.

---

## 15. Security Requirements

Protected operations require authentication.

Authorization must consider both:

- permitted capabilities;
- ownership of the requested resource.

For example:

- an attendee may access their own booking but not another attendee's booking;
- an organizer may modify an owned event but not another organizer's event;
- privileged platform operations require elevated permissions.

The concrete authentication and authorization mechanism will be designed separately.

---

## 16. Testing Requirements

Critical business behavior must be covered by automated tests.

Testing should eventually cover:

- domain business rules;
- persistence behavior;
- booking lifecycle;
- reservation expiration;
- concurrent ticket reservation;
- payment idempotency;
- external integration boundaries;
- critical end-to-end workflows.

---

## 17. Primary Business Scenario

The main EventFlow workflow is:

```text
Organizer creates event
        |
        v
Organizer configures ticket inventory
        |
        v
Organizer publishes event
        |
        v
Attendee discovers event
        |
        v
Attendee selects tickets
        |
        v
EventFlow creates temporary reservation
        |
        v
Inventory becomes temporarily unavailable
        |
        v
Attendee initiates payment
        |
        v
Payment succeeds
        |
        v
Booking is confirmed
        |
        v
Tickets are issued
        |
        v
Attendee is notified
```

This workflow is the primary business scenario around which the initial domain model and architecture will be designed.

---

## 18. Critical Failure Scenarios

The architecture must explicitly address the following scenarios.

### Concurrent Reservation

Several attendees attempt to reserve the final available ticket simultaneously.

Only the allowed inventory may be reserved.

### Reservation Expiration

An attendee reserves tickets but does not complete the purchase before the reservation expires.

The inventory must become available again.

### Duplicate Payment Confirmation

The same successful payment result is received multiple times.

The booking must still be confirmed exactly once.

### Partial Failure After Payment

Payment succeeds, but ticket issuance or another subsequent operation temporarily fails.

The system must be able to recover without losing the successful payment state.

### Event Cancellation

An event containing active reservations or confirmed bookings is cancelled.

The system must handle affected bookings consistently.

---

## 19. Engineering Principles

EventFlow should model real production engineering concerns.

The project should prioritize:

- correctness;
- maintainability;
- explicit business rules;
- clear ownership;
- testability;
- observability;
- failure handling;
- reproducible builds.

Technical complexity must be justified by an actual requirement.

Technologies should not be introduced only to make the technology stack larger.

---

## 20. Open Architecture Questions

The following questions intentionally remain unresolved:

- What component owns ticket inventory?
- Should Reservation and Booking be separate domain concepts?
- How should concurrent inventory updates be controlled?
- When exactly should inventory become permanently consumed?
- How should payment and booking consistency be coordinated?
- What are the main aggregates?
- What are the transactional boundaries?
- What domain events exist?
- Which operations should be synchronous?
- Which operations may be asynchronous?
- Is an Outbox Pattern necessary?
- Is Redis necessary?
- Is Kafka necessary?
- Does event discovery require a dedicated search engine?
- Where should independently deployable service boundaries exist?

These questions will be answered during domain and architecture design instead of being decided in advance from the desired technology stack.

---

## 21. Next Design Stage

The next stage is domain modeling.

It should identify:

- ubiquitous language;
- entities;
- value objects;
- aggregates;
- aggregate roots;
- business invariants;
- domain events;
- transactional boundaries;
- bounded contexts.

Only after the domain is sufficiently understood should EventFlow be decomposed into concrete services.