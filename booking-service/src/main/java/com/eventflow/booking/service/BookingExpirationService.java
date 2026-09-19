package com.eventflow.booking.service;

import com.eventflow.booking.entity.Booking;
import com.eventflow.booking.entity.BookingItem;
import com.eventflow.booking.entity.BookingStatus;
import com.eventflow.booking.exception.ConflictException;
import com.eventflow.booking.repository.BookingRepository;
import com.eventflow.booking.repository.TicketInventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class BookingExpirationService {

    private final BookingRepository bookingRepository;
    private final TicketInventoryRepository inventoryRepository;
    private final Clock clock;

    public BookingExpirationService(
            BookingRepository bookingRepository,
            TicketInventoryRepository inventoryRepository,
            Clock clock
    ) {
        this.bookingRepository = bookingRepository;
        this.inventoryRepository = inventoryRepository;
        this.clock = clock;
    }

    @Transactional
    public void expire(UUID bookingId) {
        Booking booking = bookingRepository.findForUpdate(bookingId)
                .orElse(null);

        if (booking == null) {
            return;
        }

        Instant now = clock.instant();

        if (booking.getStatus() != BookingStatus.PENDING) {
            return;
        }

        if (booking.getExpiresAt().isAfter(now)) {
            return;
        }

        for (BookingItem item : booking.getItems()) {
            int updatedRows = inventoryRepository.releaseReservation(
                    item.getTicketTypeId(),
                    booking.getEventId(),
                    item.getQuantity(),
                    now
            );

            if (updatedRows == 0) {
                throw new ConflictException("Unable to release inventory for booking: " + bookingId);
            }
        }

        booking.expire(now);
    }
}
