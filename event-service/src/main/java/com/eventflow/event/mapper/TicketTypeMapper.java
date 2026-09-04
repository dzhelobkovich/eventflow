package com.eventflow.event.mapper;

import com.eventflow.event.dto.TicketTypeResponse;
import com.eventflow.event.entity.TicketType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface TicketTypeMapper {

    @Mapping(target = "eventId", source = "event.id")
    TicketTypeResponse toResponse(TicketType ticketType);
}
