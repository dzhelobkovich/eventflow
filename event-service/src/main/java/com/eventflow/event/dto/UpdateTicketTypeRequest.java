package com.eventflow.event.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateTicketTypeRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal price,

        @NotBlank
        @Size(min = 3, max = 3)
        String currency,

        @Positive
        int capacity
) {}
