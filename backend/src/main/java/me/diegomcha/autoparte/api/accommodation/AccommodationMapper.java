package me.diegomcha.autoparte.api.accommodation;

import me.diegomcha.autoparte.api.accommodation.dto.AccommodationDtoCreate;
import me.diegomcha.autoparte.api.accommodation.dto.AccommodationDtoPatch;
import me.diegomcha.autoparte.api.accommodation.dto.AccommodationDtoResponse;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.Employee;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
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

    public abstract Accommodation fromCreate(AccommodationDtoCreate accommodationDtoCreate);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void patchAccommodation(AccommodationDtoPatch patch, @MappingTarget Accommodation accommodation);

    protected Set<UUID> mapEmployees(Set<Employee> employees) {
        return employees.stream()
                .map(Employee::getId)
                .collect(Collectors.toSet());
    }

    protected Set<UUID> mapBookings(Set<Booking> bookings) {
        return bookings.stream()
                .map(Booking::getId)
                .collect(Collectors.toSet());
    }
}
