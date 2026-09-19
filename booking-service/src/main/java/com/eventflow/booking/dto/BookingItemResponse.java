package com.eventflow.booking.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BookingItemResponse(
        UUID id,
        UUID ticketTypeId,
        int quantity,
        BigDecimal unitPrice,
        String currency
) {
}
