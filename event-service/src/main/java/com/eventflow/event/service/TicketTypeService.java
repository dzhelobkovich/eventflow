package com.eventflow.event.service;

import com.eventflow.event.dto.CreateTicketTypeRequest;
import com.eventflow.event.dto.TicketTypeResponse;
import com.eventflow.event.dto.UpdateTicketTypeRequest;
import com.eventflow.event.entity.Event;
import com.eventflow.event.entity.TicketType;
import com.eventflow.event.exception.ConflictException;
import com.eventflow.event.exception.ResourceNotFoundException;
import com.eventflow.event.mapper.TicketTypeMapper;
import com.eventflow.event.repository.EventRepository;
import com.eventflow.event.repository.TicketTypeRepository;
import com.eventflow.event.validator.TicketTypeValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TicketTypeService {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketTypeValidator ticketTypeValidator;
    private final TicketTypeMapper ticketTypeMapper;
    private final Clock clock;

    public TicketTypeService(
            EventRepository eventRepository,
            TicketTypeRepository ticketTypeRepository,
            TicketTypeValidator ticketTypeValidator,
            TicketTypeMapper ticketTypeMapper,
            Clock clock
    ) {
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.ticketTypeValidator = ticketTypeValidator;
        this.ticketTypeMapper = ticketTypeMapper;
        this.clock = clock;
    }

    @Transactional
    public TicketTypeResponse create(UUID eventId, CreateTicketTypeRequest request) {
        Event event = findEvent(eventId);

        ticketTypeValidator.validateCanModify(event);

        ticketTypeValidator.validateDetails(
                request.name(),
                request.price(),
                request.currency(),
                request.capacity()
        );

        if (ticketTypeRepository.existsByEvent_IdAndNameIgnoreCase(eventId, request.name().trim())) {
            throw new ConflictException("Ticket type with this name already exists");
        }

        TicketType ticketType = new TicketType(
                event,
                request.name().trim(),
                request.price(),
                normalizeCurrency(request.currency()),
                request.capacity(),
                clock.instant()
        );

        return ticketTypeMapper.toResponse(ticketTypeRepository.save(ticketType));
    }

    @Transactional(readOnly = true)
    public List<TicketTypeResponse> getByEvent(UUID eventId) {
        ensureEventExists(eventId);

        return ticketTypeRepository
                .findAllByEvent_IdOrderByCreatedAtAsc(eventId)
                .stream()
                .map(ticketTypeMapper::toResponse)
                .toList();
    }

    @Transactional
    public TicketTypeResponse update(
            UUID eventId,
            UUID ticketTypeId,
            UpdateTicketTypeRequest request
    ) {
        TicketType ticketType = findTicketType(eventId, ticketTypeId);

        ticketTypeValidator.validateCanModify(ticketType.getEvent());

        ticketTypeValidator.validateDetails(
                request.name(),
                request.price(),
                request.currency(),
                request.capacity()
        );

        if (ticketTypeRepository.existsByEvent_IdAndNameIgnoreCaseAndIdNot(
                        eventId,
                        request.name().trim(),
                        ticketTypeId)) {

            throw new ConflictException("Ticket type with this name already exists");
        }

        ticketType.update(
                request.name().trim(),
                request.price(),
                normalizeCurrency(request.currency()),
                request.capacity(),
                clock.instant()
        );

        return ticketTypeMapper.toResponse(ticketType);
    }

    @Transactional
    public void delete(UUID eventId, UUID ticketTypeId) {
        TicketType ticketType = findTicketType(eventId, ticketTypeId);

        ticketTypeValidator.validateCanModify(ticketType.getEvent());

        ticketTypeRepository.delete(ticketType);
    }

    private Event findEvent(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
    }

    private void ensureEventExists(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found: " + eventId);
        }
    }

    private TicketType findTicketType(UUID eventId, UUID ticketTypeId) {
        return ticketTypeRepository.findByIdAndEvent_Id(ticketTypeId, eventId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Ticket type not found: " + ticketTypeId));
    }

    private String normalizeCurrency(String currency) {
        return currency.trim().toUpperCase(Locale.ROOT);
    }
}
