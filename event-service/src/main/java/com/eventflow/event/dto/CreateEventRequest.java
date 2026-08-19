package com.eventflow.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateEventRequest(

        @NotBlank
        @Size(max = 200)
        String name,

        String description,

        @NotBlank
        @Size(max = 300)
        String venue,

        @NotNull
        Instant startsAt,

        @NotNull
        Instant endsAt
) {}
