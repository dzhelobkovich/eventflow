package com.eventflow.event.service;

import com.eventflow.event.dto.CreateEventRequest;
import com.eventflow.event.dto.EventResponse;
import com.eventflow.event.dto.UpdateEventRequest;
import com.eventflow.event.entity.Event;
import com.eventflow.event.exception.ResourceNotFoundException;
import com.eventflow.event.mapper.EventMapper;
import com.eventflow.event.repository.EventRepository;
import com.eventflow.event.repository.TicketTypeRepository;
import com.eventflow.event.validator.EventValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventValidator eventValidator;
    private final EventMapper eventMapper;
    private final Clock clock;

    public EventService(EventRepository eventRepository, TicketTypeRepository ticketTypeRepository,
            EventValidator eventValidator, EventMapper eventMapper, Clock clock) {
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.eventValidator = eventValidator;
        this.eventMapper = eventMapper;
        this.clock = clock;
    }

    @Transactional
    public EventResponse create(CreateEventRequest request) {
        eventValidator.validateDetails(
                request.name(),
                request.venue(),
                request.startsAt(),
                request.endsAt()
        );

        Instant now = clock.instant();

        Event event = new Event(
                request.name().trim(),
                request.description(),
                request.venue().trim(),
                request.startsAt(),
                request.endsAt(),
                now
        );

        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public EventResponse getById(UUID eventId) {
        return eventMapper.toResponse(findEvent(eventId));
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAll() {
        return eventRepository.findAll()
                .stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    @Transactional
    public EventResponse update(UUID eventId, UpdateEventRequest request) {
        Event event = findEvent(eventId);

        eventValidator.validateCanModify(event);

        eventValidator.validateDetails(
                request.name(),
                request.venue(),
                request.startsAt(),
                request.endsAt()
        );

        event.updateDetails(
                request.name().trim(),
                request.description(),
                request.venue().trim(),
                request.startsAt(),
                request.endsAt(),
                clock.instant()
        );

        return eventMapper.toResponse(event);
    }

    @Transactional
    public EventResponse publish(UUID eventId) {
        Event event = findEvent(eventId);

        boolean hasTicketTypes = ticketTypeRepository.existsByEvent_Id(eventId);

        eventValidator.validateCanPublish(event, hasTicketTypes);

        event.publish(clock.instant());

        return eventMapper.toResponse(event);
    }

    @Transactional
    public EventResponse cancel(UUID eventId) {
        Event event = findEvent(eventId);

        eventValidator.validateCanCancel(event);

        event.cancel(clock.instant());

        return eventMapper.toResponse(event);
    }

    @Transactional
    public EventResponse complete(UUID eventId) {
        Event event = findEvent(eventId);

        eventValidator.validateCanComplete(event);

        event.complete(clock.instant());

        return eventMapper.toResponse(event);
    }

    private Event findEvent(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Event not found: " + eventId));
    }
}
