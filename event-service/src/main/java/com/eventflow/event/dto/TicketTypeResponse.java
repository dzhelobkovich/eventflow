package com.eventflow.event.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TicketTypeResponse(
        UUID id,
        UUID eventId,
        String name,
        BigDecimal price,
        String currency,
        int capacity,
        Instant createdAt,
        Instant updatedAt
) {
}
