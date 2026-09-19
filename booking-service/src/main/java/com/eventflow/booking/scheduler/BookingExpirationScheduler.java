package com.eventflow.booking.scheduler;

import com.eventflow.booking.entity.BookingStatus;
import com.eventflow.booking.repository.BookingRepository;
import com.eventflow.booking.service.BookingExpirationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Component
public class BookingExpirationScheduler {

    private final BookingRepository bookingRepository;
    private final BookingExpirationService expirationService;
    private final Clock clock;
    private final int batchSize;

    public BookingExpirationScheduler(
            BookingRepository bookingRepository,
            BookingExpirationService expirationService,
            Clock clock,
            @Value("${booking.expiration.batch-size:100}") int batchSize
    ) {
        this.bookingRepository = bookingRepository;
        this.expirationService = expirationService;
        this.clock = clock;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${booking.expiration.fixed-delay-ms:30000}")
    public void expireBookings() {
        List<UUID> bookingIds =
                bookingRepository.findExpiredBookingIds(
                        BookingStatus.PENDING,
                        clock.instant(),
                        PageRequest.of(0, batchSize)
                );

        bookingIds.forEach(expirationService::expire);
    }
}