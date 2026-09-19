package com.eventflow.booking.repository;

import com.eventflow.booking.entity.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @EntityGraph(attributePaths = "items")
    Optional<Booking> findWithItemsById(UUID bookingId);
}
