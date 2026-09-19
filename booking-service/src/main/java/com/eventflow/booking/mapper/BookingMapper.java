package com.eventflow.booking.mapper;

import com.eventflow.booking.dto.BookingResponse;
import com.eventflow.booking.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface BookingMapper {

    BookingResponse toResponse(Booking booking);
}