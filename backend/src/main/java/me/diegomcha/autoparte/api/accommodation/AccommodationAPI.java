package me.diegomcha.autoparte.api.accommodation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.diegomcha.autoparte.api.accommodation.dto.AccommodationDtoCreate;
import me.diegomcha.autoparte.api.accommodation.dto.AccommodationDtoPatch;
import me.diegomcha.autoparte.api.accommodation.dto.AccommodationDtoResponse;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Tag(name = "Accommodations", description = "Operations related to accommodations")
@SuppressWarnings("unused")
interface AccommodationAPI {

    @Operation(summary = "List accommodations")
    Page<AccommodationDtoResponse> getAccommodations(Pageable pageable);

    @Operation(summary = "Get accommodation by id")
    AccommodationDtoResponse getAccommodation(UUID id) throws ResourceNotFoundException;

    @Operation(summary = "Create accommodation")
    void createAccommodation(AccommodationDtoCreate accommodation) throws ResourceConflictException;

    @Operation(summary = "Update accommodation")
    void updateAccommodation(UUID id, AccommodationDtoPatch accommodation) throws ResourceNotFoundException, ResourceConflictException;

    @Operation(summary = "Delete accommodation")
    void deleteAccommodation(UUID id) throws ResourceNotFoundException;

    @Operation(summary = "Assign employee to accommodation")
    void assignEmployeeToAccommodation(UUID accommodationId, UUID employeeId) throws ResourceNotFoundException, ResourceConflictException;

    @Operation(summary = "Unassign employee from accommodation")
    void unassignEmployeeFromAccommodation(UUID accommodationId, UUID employeeId) throws ResourceNotFoundException, ResourceConflictException;
}
