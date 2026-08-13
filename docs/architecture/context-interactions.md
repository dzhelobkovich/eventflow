# Context Interactions

## Purpose

This document defines how the main EventFlow bounded contexts communicate.

Synchronous communication is used when an immediate response is required. Asynchronous communication is used when the operation can be processed independently.

## Interaction Overview

| Source | Target | Communication | Purpose |
|---|---|---|---|
| Booking | Event Catalog | Synchronous | Validate event and ticket type data |
| Booking | Payment | Synchronous | Start payment processing |
| Payment | Booking | Asynchronous | Report payment result |
| Booking | Ticketing | Asynchronous | Issue tickets after booking confirmation |
| Booking | Notification | Asynchronous | Send booking-related notifications |
| Event Catalog | Search | Asynchronous | Synchronize searchable event data |
| Event Catalog | Notification | Asynchronous | Send notifications about important event changes |

The concrete transport technology will be defined during implementation.

## Booking Flow

The main booking flow is:

1. The user selects an event and ticket type.
2. Booking validates the required event data.
3. Booking checks ticket availability.
4. Booking creates a temporary reservation.
5. Payment processing is started.
6. Payment reports the final result.
7. Booking confirms the reservation after successful payment.
8. Ticket issuance and notifications are triggered asynchronously.

```text
Client
  |
  v
Booking -----> Event Catalog
  |
  +---------> Payment
                 |
                 | payment result
                 v
              Booking
                 |
          BookingConfirmed
           /            \
          v              v
      Ticketing      Notification
```

Booking remains responsible for the booking lifecycle.

Payment does not directly modify booking data or issue tickets.

## Reservation Expiration

Reservations are temporary.

If payment is not completed within the reservation period, Booking expires the reservation and releases the reserved inventory.

```text
RESERVED
   |
   | timeout
   v
EXPIRED
```

Expiration processing must be safe to execute more than once.

## Failure Handling

### Payment Failure

If payment fails:

- the booking is not confirmed;
- reserved inventory is released according to booking rules;
- no ticket is issued.

### Notification Failure

Notification failure must not roll back a successful booking.

Failed notifications can be retried independently.

### Search Synchronization Failure

Search synchronization failure must not prevent an event from being created or updated.

Search data can be synchronized again later.

## Idempotency

Asynchronous message processing must be idempotent where duplicate delivery is possible.

This is especially important for:

- payment results;
- booking confirmation;
- ticket issuance;
- notifications;
- search synchronization.

Processing the same message multiple times must not create duplicate business results.

## Integration Events

Initial cross-context events may include:

```text
PaymentSucceeded
PaymentFailed
BookingConfirmed
BookingExpired
TicketIssued
EventUpdated
EventCancelled
```

Integration events should contain only the data required by their consumers.

Internal domain entities must not be exposed directly as integration contracts.

## Event Delivery

Database state and published integration events must not become inconsistent.

For services that update business state and publish events, the Transactional Outbox Pattern should be considered.

```text
Database transaction
        |
        +-- update business state
        |
        +-- store outbox event
```

The concrete messaging and outbox implementation will be introduced when asynchronous communication is implemented.