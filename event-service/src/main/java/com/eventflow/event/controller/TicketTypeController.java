package com.eventflow.event.controller;

import com.eventflow.event.dto.CreateTicketTypeRequest;
import com.eventflow.event.dto.TicketTypeResponse;
import com.eventflow.event.dto.UpdateTicketTypeRequest;
import com.eventflow.event.service.TicketTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/events/{eventId}/ticket-types")
@Tag(name = "Ticket Types", description = "Ticket type management operations")
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    public TicketTypeController(TicketTypeService ticketTypeService) {
        this.ticketTypeService = ticketTypeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a ticket type")
    public TicketTypeResponse create(@PathVariable UUID eventId, @Valid @RequestBody CreateTicketTypeRequest request) {
        return ticketTypeService.create(eventId, request);
    }

    @GetMapping
    @Operation(summary = "Get ticket types for an event")
    public List<TicketTypeResponse> getByEvent(@PathVariable UUID eventId) {
        return ticketTypeService.getByEvent(eventId);
    }

    @PutMapping("/{ticketTypeId}")
    @Operation(summary = "Update a ticket type")
    public TicketTypeResponse update(@PathVariable UUID eventId, @PathVariable UUID ticketTypeId,
                                     @Valid @RequestBody UpdateTicketTypeRequest request) {
        return ticketTypeService.update(eventId, ticketTypeId, request);
    }

    @DeleteMapping("/{ticketTypeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a ticket type")
    public void delete(@PathVariable UUID eventId, @PathVariable UUID ticketTypeId) {
        ticketTypeService.delete(eventId, ticketTypeId);
    }
}