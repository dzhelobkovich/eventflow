package com.eventflow.event.validator;

import com.eventflow.event.entity.Event;
import com.eventflow.event.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventValidatorTest {

    private static final Instant NOW = Instant.parse("2026-08-19T12:00:00Z");

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private final EventValidator validator = new EventValidator(clock);

    @Test
    void shouldAllowPublishingDraftEventWithTicketTypes() {
        Event event = new Event(
                "Concert",
                "Description",
                "Warsaw",
                NOW.plusSeconds(3600),
                NOW.plusSeconds(7200),
                NOW
        );

        assertDoesNotThrow(() -> validator.validateCanPublish(event, true)
        );
    }

    @Test
    void shouldRejectPublishingEventWithoutTicketTypes() {
        Event event = new Event(
                "Concert",
                "Description",
                "Warsaw",
                NOW.plusSeconds(3600),
                NOW.plusSeconds(7200),
                NOW
        );

        assertThrows(BusinessRuleException.class, () -> validator.validateCanPublish(event, false));
    }

    @Test
    void shouldRejectInvalidEventTimeRange() {
        assertThrows(BusinessRuleException.class, () -> validator.validateDetails(
                "Concert",
                "Warsaw",
                NOW.plusSeconds(7200),
                NOW.plusSeconds(3600)
                )
        );
    }
}
