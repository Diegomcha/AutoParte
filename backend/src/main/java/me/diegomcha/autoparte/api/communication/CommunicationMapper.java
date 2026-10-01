package me.diegomcha.autoparte.api.communication;

import me.diegomcha.autoparte.api.common.PageableMapper;
import me.diegomcha.autoparte.api.communication.dto.CommunicationDtoResponse;
import me.diegomcha.autoparte.domain.communication.Communication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Pageable;

import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class CommunicationMapper extends PageableMapper {

    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "accommodationId", source = "booking.accommodation.id")
    public abstract CommunicationDtoResponse toDto(Communication communication);

    public Pageable translatePageable(Pageable pageable) {
        return super.translatePageable(Map.of(
                "accommodationId", "booking.accommodation.id",
                "bookingId", "booking.id"
        ), pageable);
    }
}
