package com.eventflow.booking.repository;

import com.eventflow.booking.entity.TicketInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketInventoryRepository extends JpaRepository<TicketInventory, UUID> {
}