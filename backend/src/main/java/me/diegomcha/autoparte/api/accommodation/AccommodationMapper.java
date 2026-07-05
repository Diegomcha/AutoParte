package me.diegomcha.autoparte.api.accommodation;

import me.diegomcha.autoparte.api.accommodation.dto.AccommodationDtoRequest;
import me.diegomcha.autoparte.api.accommodation.dto.AccommodationDtoResponse;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
abstract class AccommodationMapper {

    public abstract AccommodationDtoResponse toResponse(Accommodation accommodation);

    public Page<AccommodationDtoResponse> toResponse(Page<Accommodation> page) {
        return page.map(this::toResponse);
    }

    public abstract Accommodation fromCreate(AccommodationDtoRequest accommodationDtoCreate);

    public abstract void fromUpdate(AccommodationDtoRequest update, @MappingTarget Accommodation accommodation);

    // Helpers

    @Mapping(target = "enabled", source = "account.enabled")
    protected abstract AccommodationDtoResponse.AccommodationDtoEmployeeResponse map(Employee employee);
}
