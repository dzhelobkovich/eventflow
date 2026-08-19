package com.eventflow.event.dto;

import com.eventflow.event.entity.EventStatus;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String name,
        String description,
        String venue,
        Instant startsAt,
        Instant endsAt,
        EventStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
