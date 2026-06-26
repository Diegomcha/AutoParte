package me.diegomcha.autoparte.api.accommodation;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.accommodation.dto.AccommodationDtoRequest;
import me.diegomcha.autoparte.api.accommodation.dto.AccommodationDtoResponse;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/accommodations")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class AccommodationController implements AccommodationAPI {

    private final AccommodationService accommodationService;

    @GetMapping
    @Override
    public Page<AccommodationDtoResponse> getAccommodations(@ParameterObject Pageable pageable) {
        return accommodationService.getAccommodations(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public EntityDtoCreated createAccommodation(@Valid @RequestBody AccommodationDtoRequest accommodation) throws ResourceConflictException {
        return accommodationService.createAccommodation(accommodation);
    }

    @GetMapping("/{id}")
    @Override
    public AccommodationDtoResponse getAccommodation(@PathVariable UUID id) throws ResourceNotFoundException {
        return accommodationService.getAccommodation(id);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void updateAccommodation(@PathVariable UUID id, @Valid @RequestBody AccommodationDtoRequest accommodation) throws ResourceNotFoundException, ResourceConflictException {
        accommodationService.updateAccommodation(id, accommodation);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Override
    public void deleteAccommodation(@PathVariable UUID id) throws ResourceNotFoundException {
        accommodationService.deleteAccommodation(id);
    }

    @PostMapping("/{accommodationId}/employees/{employeeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void assignEmployeeToAccommodation(@PathVariable UUID accommodationId, @PathVariable UUID employeeId) throws ResourceNotFoundException, ResourceConflictException {
        accommodationService.assignEmployeeToAccommodation(accommodationId, employeeId);
    }

    @DeleteMapping("/{accommodationId}/employees/{employeeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void unassignEmployeeFromAccommodation(@PathVariable UUID accommodationId, @PathVariable UUID employeeId) throws ResourceNotFoundException, ResourceConflictException {
        accommodationService.unassignEmployeeFromAccommodation(accommodationId, employeeId);
    }
}
