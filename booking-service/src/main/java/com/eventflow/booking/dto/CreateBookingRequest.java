package com.eventflow.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateBookingRequest(

        @NotNull UUID customerId,

        @NotNull UUID eventId,

        @NotNull UUID ticketTypeId,

        @Min(1) int quantity
) {
}
