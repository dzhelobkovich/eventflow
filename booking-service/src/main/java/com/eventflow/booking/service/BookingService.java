package com.eventflow.booking.service;

import com.eventflow.booking.dto.BookingResponse;
import com.eventflow.booking.dto.CreateBookingRequest;
import com.eventflow.booking.entity.Booking;
import com.eventflow.booking.entity.BookingItem;
import com.eventflow.booking.entity.BookingStatus;
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
import java.util.UUID;

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
                        "Ticket inventory not found: " + request.ticketTypeId()));

        if (!inventory.getEventId().equals(request.eventId())) {
            throw new ConflictException("Ticket type does not belong to the specified event");
        }

        int updatedRows = ticketInventoryRepository.reserve(
                request.ticketTypeId(),
                request.eventId(),
                request.quantity(),
                now
        );

        if (updatedRows == 0) {
            throw new ConflictException("Not enough tickets available");
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

    @Transactional
    public BookingResponse confirm(UUID bookingId) {
        Booking booking = findBooking(bookingId);

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return bookingMapper.toResponse(booking);
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ConflictException("Only a pending booking can be confirmed");
        }

        Instant now = clock.instant();

        if (!booking.getExpiresAt().isAfter(now)) {
            throw new ConflictException("The booking reservation has expired");
        }

        for (BookingItem item : booking.getItems()) {
            int updatedRows = ticketInventoryRepository.confirmReservation(
                    item.getTicketTypeId(),
                    booking.getEventId(),
                    item.getQuantity(),
                    now
            );

            if (updatedRows == 0) {
                throw new ConflictException("Unable to confirm the ticket reservation");
            }
        }

        booking.confirm(now);

        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse cancel(UUID bookingId) {
        Booking booking = findBooking(bookingId);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return bookingMapper.toResponse(booking);
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ConflictException("Only a pending booking can be cancelled");
        }

        Instant now = clock.instant();

        releaseInventory(booking, now);
        booking.cancel(now);

        return bookingMapper.toResponse(booking);
    }

    private void releaseInventory(Booking booking, Instant now) {
        for (BookingItem item : booking.getItems()) {
            int updatedRows = ticketInventoryRepository.releaseReservation(
                    item.getTicketTypeId(),
                    booking.getEventId(),
                    item.getQuantity(),
                    now
            );

            if (updatedRows == 0) {
                throw new ConflictException("Unable to release the ticket reservation");
            }
        }
    }

    private Booking findBooking(UUID bookingId) {
        return bookingRepository.findWithItemsById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
    }
}
