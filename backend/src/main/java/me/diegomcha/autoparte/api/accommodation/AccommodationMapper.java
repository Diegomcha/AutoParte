package me.diegomcha.autoparte.api.accommodation;

import me.diegomcha.autoparte.api.accommodation.dto.AccommodationDtoRequest;
import me.diegomcha.autoparte.api.accommodation.dto.AccommodationDtoResponse;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
abstract class AccommodationMapper {

    public abstract AccommodationDtoResponse toResponse(Accommodation accommodation);

    public Page<AccommodationDtoResponse> toResponse(Page<Accommodation> page) {
        return page.map(this::toResponse);
    }

    public abstract Accommodation fromCreate(AccommodationDtoRequest accommodationDtoCreate);

    public abstract void fromUpdate(AccommodationDtoRequest update, @MappingTarget Accommodation accommodation);

    protected Set<UUID> mapEmployees(Set<Employee> employees) {
        return employees.stream()
                .map(Employee::getId)
                .collect(Collectors.toSet());
    }
}
