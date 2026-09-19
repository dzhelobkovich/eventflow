package com.eventflow.booking.repository;

import com.eventflow.booking.entity.Booking;
import com.eventflow.booking.entity.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT booking
            FROM Booking booking
            WHERE booking.id = :bookingId
            """)
    Optional<Booking> findForUpdate(
            @Param("bookingId") UUID bookingId
    );

    @Query("""
            SELECT booking.id
            FROM Booking booking
            WHERE booking.status = :status
              AND booking.expiresAt <= :now
            ORDER BY booking.expiresAt
            """)
    List<UUID> findExpiredBookingIds(
            @Param("status") BookingStatus status,
            @Param("now") Instant now,
            Pageable pageable
    );
}
