package com.eventflow.event.repository;

import com.eventflow.event.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketTypeRepository extends JpaRepository<TicketType, UUID> {

    List<TicketType> findAllByEvent_IdOrderByCreatedAtAsc(UUID eventId);

    Optional<TicketType> findByIdAndEvent_Id(UUID ticketTypeId, UUID eventId);

    boolean existsByEvent_Id(UUID eventId);

    boolean existsByEvent_IdAndNameIgnoreCase(UUID eventId, String name);

    boolean existsByEvent_IdAndNameIgnoreCaseAndIdNot(UUID eventId, String name, UUID ticketTypeId);
}
