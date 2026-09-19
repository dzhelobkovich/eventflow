package com.eventflow.booking.dto;

import com.eventflow.booking.entity.BookingStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID customerId,
        UUID eventId,
        BookingStatus status,
        Instant expiresAt,
        List<BookingItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
}
