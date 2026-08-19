package com.eventflow.event.controller;

import com.eventflow.event.dto.CreateEventRequest;
import com.eventflow.event.dto.EventResponse;
import com.eventflow.event.dto.UpdateEventRequest;
import com.eventflow.event.service.EventService;
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
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@Valid @RequestBody CreateEventRequest request) {
        return eventService.create(request);
    }

    @GetMapping("/{eventId}")
    public EventResponse getById(@PathVariable UUID eventId) {
        return eventService.getById(eventId);
    }

    @GetMapping
    public List<EventResponse> getAll() {
        return eventService.getAll();
    }

    @PutMapping("/{eventId}")
    public EventResponse update(@PathVariable UUID eventId, @Valid @RequestBody UpdateEventRequest request) {
        return eventService.update(eventId, request);
    }

    @PostMapping("/{eventId}/publish")
    public EventResponse publish(@PathVariable UUID eventId) {
        return eventService.publish(eventId);
    }

    @PostMapping("/{eventId}/cancel")
    public EventResponse cancel(@PathVariable UUID eventId) {
        return eventService.cancel(eventId);
    }

    @PostMapping("/{eventId}/complete")
    public EventResponse complete(@PathVariable UUID eventId) {
        return eventService.complete(eventId);
    }
}
