package com.eventflow.event.validator;

import com.eventflow.event.entity.Event;
import com.eventflow.event.entity.EventStatus;
import com.eventflow.event.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class EventValidator {

    private final Clock clock;

    public EventValidator(Clock clock) {
        this.clock = clock;
    }

    public void validateDetails(
            String name,
            String venue,
            Instant startsAt,
            Instant endsAt
    ) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("Event name must not be blank");
        }

        if (venue == null || venue.isBlank()) {
            throw new BusinessRuleException("Event venue must not be blank");
        }

        if (startsAt == null || endsAt == null) {
            throw new BusinessRuleException(
                    "Event start and end time are required"
            );
        }

        if (!startsAt.isBefore(endsAt)) {
            throw new BusinessRuleException(
                    "Event start time must be before end time"
            );
        }
    }

    public void validateCanModify(Event event) {
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BusinessRuleException(
                    "Only draft events can be modified"
            );
        }
    }

    public void validateCanPublish(Event event, boolean hasTicketTypes) {
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BusinessRuleException(
                    "Only draft events can be published"
            );
        }

        if (!hasTicketTypes) {
            throw new BusinessRuleException(
                    "Event must contain at least one ticket type before publication"
            );
        }

        if (!event.getStartsAt().isAfter(clock.instant())) {
            throw new BusinessRuleException(
                    "Event starting time must be in the future"
            );
        }
    }

    public void validateCanCancel(Event event) {
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new BusinessRuleException("Event is already cancelled");
        }

        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Completed event cannot be cancelled"
            );
        }
    }

    public void validateCanComplete(Event event) {
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessRuleException(
                    "Only published events can be completed"
            );
        }
    }
}
