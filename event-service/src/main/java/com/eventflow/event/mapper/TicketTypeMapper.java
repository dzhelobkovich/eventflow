package com.eventflow.event.mapper;

import com.eventflow.event.dto.TicketTypeResponse;
import com.eventflow.event.entity.TicketType;
import org.springframework.stereotype.Component;

@Component
public class TicketTypeMapper {

    public TicketTypeResponse toResponse(TicketType ticketType) {
        return new TicketTypeResponse(
                ticketType.getId(),
                ticketType.getEvent().getId(),
                ticketType.getName(),
                ticketType.getPrice(),
                ticketType.getCurrency(),
                ticketType.getCapacity(),
                ticketType.getCreatedAt(),
                ticketType.getUpdatedAt()
        );
    }
}
