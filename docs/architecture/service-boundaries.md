# Service Boundaries

## Purpose

This document defines the initial deployable service boundaries of EventFlow.

Bounded contexts are not mapped one-to-one to microservices. Services are separated only when there is a clear responsibility and an independent deployment boundary.

## Initial Services

EventFlow will initially contain four backend services:

```text
identity-service
event-service
booking-service
notification-service
```

Each service is an independently buildable Spring Boot application and owns its persistence model.

## identity-service

### Responsibility

Manages user identity and authentication.

Owns:

- user accounts;
- authentication credentials;
- account status;
- security-related identity data.

Other services reference users by `UserId`.

They must not access Identity data directly.

## event-service

### Responsibility

Manages the event catalog and event lifecycle.

Owns:

- events;
- event schedules;
- locations;
- ticket type configuration;
- publication state;
- ticket sales configuration.

Search functionality may initially remain inside this service.

If search later requires independent scaling or more complex processing, it can be extracted into a separate service.

## booking-service

### Responsibility

Manages the booking lifecycle and ticket inventory allocation.

Owns:

- bookings;
- booking items;
- reservations;
- ticket inventory;
- booking state transitions;
- reservation expiration.

The service is responsible for preventing overselling and maintaining consistency between reservations and inventory.

Initially, payment integration and ticket issuance remain separate internal responsibilities inside `booking-service`.

```text
booking-service
├── booking
├── inventory
├── payment
└── ticketing
```

These boundaries should remain separated in code even though they belong to the same deployment unit.

This keeps the consistency-sensitive booking flow together without introducing unnecessary distributed transactions.

## notification-service

### Responsibility

Processes notifications caused by business events.

Responsible for:

- booking notifications;
- payment-related notifications;
- ticket delivery notifications;
- important event notifications.

Notification processing is asynchronous.

A notification failure must not invalidate a successful booking or another completed business operation.

## Data Ownership

Each service owns its data.

```text
identity-service
    -> identity data

event-service
    -> event and ticket type data

booking-service
    -> booking, reservation and inventory data

notification-service
    -> notification delivery data
```

Services must not directly read or modify another service's database.

Cross-service data must be exchanged through explicit APIs or integration events.

## Service Communication

The initial communication direction is:

```text
Client
  |
  +------> identity-service
  |
  +------> event-service
  |
  +------> booking-service
                  |
                  +------> notification-service
```

Synchronous communication is used when an immediate result is required.

Asynchronous communication is used for operations that can be processed independently.

Detailed interaction rules are defined in `context-interactions.md`.

## Deferred Service Extraction

The following services will not be created initially:

```text
payment-service
ticket-service
inventory-service
search-service
```

They should become separate services only when there is a clear reason, such as:

- independent scaling requirements;
- independent deployment requirements;
- growing domain complexity;
- different availability requirements;
- a clear independent ownership boundary.

Creating additional services without such requirements would increase network communication, failure points, and operational complexity without providing enough benefit.

## Initial Architecture

```text
                    ┌──────────────────┐
                    │ identity-service │
                    └──────────────────┘

Client
  |
  +----------------> event-service
  |                       |
  |                    Search
  |
  +----------------> booking-service
                           |
                  ┌────────┼────────┐
                  │        │        │
              Inventory  Payment  Ticketing
                           |
                           | events
                           v
                  notification-service
```

Payment, Inventory, Ticketing, and Search represent internal responsibility boundaries at this stage.

They are not independent deployment units.

The architecture may evolve when real domain, scaling, or operational requirements justify additional service extraction.