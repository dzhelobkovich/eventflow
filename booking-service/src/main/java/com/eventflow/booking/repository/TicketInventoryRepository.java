package com.eventflow.booking.repository;

import com.eventflow.booking.entity.TicketInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface TicketInventoryRepository extends JpaRepository<TicketInventory, UUID> {

    @Modifying
    @Query("""
            UPDATE TicketInventory inventory
               SET inventory.availableQuantity =
                       inventory.availableQuantity - :quantity,
                   inventory.reservedQuantity =
                       inventory.reservedQuantity + :quantity,
                   inventory.updatedAt = :now,
                   inventory.version = inventory.version + 1
             WHERE inventory.ticketTypeId = :ticketTypeId
               AND inventory.eventId = :eventId
               AND :quantity > 0
               AND inventory.availableQuantity >= :quantity
            """)
    int reserve(@Param("ticketTypeId") UUID ticketTypeId, @Param("eventId") UUID eventId,
                @Param("quantity") int quantity, @Param("now") Instant now
    );

    @Modifying
    @Query("""
            UPDATE TicketInventory inventory
               SET inventory.reservedQuantity =
                       inventory.reservedQuantity - :quantity,
                   inventory.soldQuantity =
                       inventory.soldQuantity + :quantity,
                   inventory.updatedAt = :now,
                   inventory.version = inventory.version + 1
             WHERE inventory.ticketTypeId = :ticketTypeId
               AND inventory.eventId = :eventId
               AND :quantity > 0
               AND inventory.reservedQuantity >= :quantity
            """)
    int confirmReservation(@Param("ticketTypeId") UUID ticketTypeId, @Param("eventId") UUID eventId,
                           @Param("quantity") int quantity, @Param("now") Instant now
    );

    @Modifying
    @Query("""
            UPDATE TicketInventory inventory
               SET inventory.reservedQuantity =
                       inventory.reservedQuantity - :quantity,
                   inventory.availableQuantity =
                       inventory.availableQuantity + :quantity,
                   inventory.updatedAt = :now,
                   inventory.version = inventory.version + 1
             WHERE inventory.ticketTypeId = :ticketTypeId
               AND inventory.eventId = :eventId
               AND :quantity > 0
               AND inventory.reservedQuantity >= :quantity
            """)
    int releaseReservation(@Param("ticketTypeId") UUID ticketTypeId, @Param("eventId") UUID eventId,
                           @Param("quantity") int quantity, @Param("now") Instant now
    );
}