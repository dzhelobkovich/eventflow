package com.eventflow.event.mapper;

import com.eventflow.event.dto.EventResponse;
import com.eventflow.event.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface EventMapper {

    EventResponse toResponse(Event event);
}
