package com.eventflow.booking.service;

import com.eventflow.booking.dto.BookingResponse;
import com.eventflow.booking.dto.CreateBookingRequest;
import com.eventflow.booking.entity.Booking;
import com.eventflow.booking.entity.TicketInventory;
import com.eventflow.booking.exception.ConflictException;
import com.eventflow.booking.exception.ResourceNotFoundException;
import com.eventflow.booking.mapper.BookingMapper;
import com.eventflow.booking.repository.BookingRepository;
import com.eventflow.booking.repository.TicketInventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class BookingService {

    private static final Duration RESERVATION_DURATION = Duration.ofMinutes(15);

    private final BookingRepository bookingRepository;
    private final TicketInventoryRepository ticketInventoryRepository;
    private final BookingMapper bookingMapper;
    private final Clock clock;

    public BookingService(
            BookingRepository bookingRepository,
            TicketInventoryRepository ticketInventoryRepository,
            BookingMapper bookingMapper,
            Clock clock
    ) {
        this.bookingRepository = bookingRepository;
        this.ticketInventoryRepository = ticketInventoryRepository;
        this.bookingMapper = bookingMapper;
        this.clock = clock;
    }

    @Transactional
    public BookingResponse create(CreateBookingRequest request) {
        Instant now = clock.instant();

        TicketInventory inventory = ticketInventoryRepository
                .findById(request.ticketTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket inventory not found: " + request.ticketTypeId()
                ));

        if (!inventory.getEventId().equals(request.eventId())) {
            throw new ConflictException(
                    "Ticket type does not belong to the specified event"
            );
        }

        int updatedRows = ticketInventoryRepository.reserve(
                request.ticketTypeId(),
                request.eventId(),
                request.quantity(),
                now
        );

        if (updatedRows == 0) {
            throw new ConflictException(
                    "Not enough tickets available"
            );
        }

        Booking booking = Booking.builder()
                .customerId(request.customerId())
                .eventId(request.eventId())
                .expiresAt(now.plus(RESERVATION_DURATION))
                .createdAt(now)
                .updatedAt(now)
                .build();

        booking.addItem(
                inventory.getTicketTypeId(),
                request.quantity(),
                inventory.getPrice(),
                inventory.getCurrency()
        );

        return bookingMapper.toResponse(
                bookingRepository.save(booking)
        );
    }
}
