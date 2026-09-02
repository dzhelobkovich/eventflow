package com.eventflow.event.controller;

import com.eventflow.event.dto.CreateEventRequest;
import com.eventflow.event.dto.EventResponse;
import com.eventflow.event.dto.UpdateEventRequest;
import com.eventflow.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events", description = "Event management operations")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an event")
    public EventResponse create(@Valid @RequestBody CreateEventRequest request) {
        return eventService.create(request);
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get an event by ID")
    public EventResponse getById(@PathVariable UUID eventId) {
        return eventService.getById(eventId);
    }

    @GetMapping
    @Operation(summary = "Get all events")
    public List<EventResponse> getAll() {
        return eventService.getAll();
    }

    @PutMapping("/{eventId}")
    @Operation(summary = "Update an event")
    public EventResponse update(
            @PathVariable UUID eventId,
            @Valid @RequestBody UpdateEventRequest request
    ) {
        return eventService.update(eventId, request);
    }

    @PostMapping("/{eventId}/publish")
    @Operation(summary = "Publish an event")
    public EventResponse publish(@PathVariable UUID eventId) {
        return eventService.publish(eventId);
    }

    @PostMapping("/{eventId}/cancel")
    @Operation(summary = "Cancel an event")
    public EventResponse cancel(@PathVariable UUID eventId) {
        return eventService.cancel(eventId);
    }

    @PostMapping("/{eventId}/complete")
    @Operation(summary = "Complete an event")
    public EventResponse complete(@PathVariable UUID eventId) {
        return eventService.complete(eventId);
    }
}
