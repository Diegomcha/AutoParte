package me.diegomcha.autoparte.api.accommodation;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.accommodation.dto.AccommodationDtoRequest;
import me.diegomcha.autoparte.api.accommodation.dto.AccommodationDtoResponse;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.api.common.EntityMapper;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.EmployeeRepo;
import me.diegomcha.autoparte.core.security.SecurityService;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional(readOnly = true)
class AccommodationService {

    private static final Supplier<ResourceNotFoundException> NOT_FOUND_EXCEPTION = () ->
            new ResourceNotFoundException("Accommodation not found");
    private static final Supplier<ResourceNotFoundException> EMPLOYEE_NOT_FOUND = () ->
            new ResourceNotFoundException("Employee not found");
    private static final Supplier<ResourceConflictException> SAME_SESCODE_EXCEPTION = () ->
            new ResourceConflictException("An accommodation with the same sesCode already exists");
    private static final Supplier<ResourceConflictException> SAME_NAME_EXCEPTION = () ->
            new ResourceConflictException("An accommodation with the same name already exists");

    private final AccommodationMapper accommodationMapper;
    private final AccommodationRepo accommodationRepo;
    private final EmployeeRepo employeeRepo;
    private final EntityMapper entityMapper;
    private final SecurityService securityService;

    /**
     * Returns a paginated list of all accommodations taking into account the logged-in user access.
     *
     * @param pageable Pagination information (page number, size, sorting)
     * @return A page of accommodations
     */
    public Page<AccommodationDtoResponse> getAccommodations(@NonNull Pageable pageable) {
        var employee = Optional
                .ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(securityService::getAccountFromAuthentication)
                .map(securityService::getEmployeeFromAccount);

        return employee
                // Employee
                .map(emp -> accommodationMapper.toResponse(accommodationRepo.findByEmployeesId(emp.getId(), pageable)))
                // Admin
                .orElseGet(() -> accommodationMapper.toResponse(accommodationRepo.findAll(Pageable.unpaged())));
    }

    /**
     * Returns the accommodation with the given ID, if it exists.
     *
     * @param id The ID of the accommodation to retrieve
     * @return The accommodation with the given ID, or empty if it does not exist
     * @throws ResourceNotFoundException if no accommodation with the given ID exists
     */
    public AccommodationDtoResponse getAccommodation(@NonNull UUID id) throws ResourceNotFoundException {
        return accommodationRepo
                .findById(id)
                .map(accommodationMapper::toResponse)
                .orElseThrow(NOT_FOUND_EXCEPTION);
    }

    /**
     * Creates a new accommodation with the given data.
     *
     * @param dto The data for the new accommodation
     * @return The created accommodation's ID and creation timestamp
     * @throws ResourceConflictException if an accommodation with the same name or sesCode already exists
     */
    @Transactional
    public EntityDtoCreated createAccommodation(@NonNull AccommodationDtoRequest dto) throws ResourceConflictException {
        Accommodation newAccommodation = accommodationMapper.fromCreate(dto);

        // Ensure name is unique
        if (accommodationRepo.existsByName(newAccommodation.getName()))
            throw SAME_NAME_EXCEPTION.get();

        // Ensure sesCode is unique
        if (accommodationRepo.existsBySesCode(newAccommodation.getSesCode()))
            throw SAME_SESCODE_EXCEPTION.get();

        newAccommodation = accommodationRepo.save(newAccommodation);
        return entityMapper.toCreated(newAccommodation);
    }

    /**
     * Updates the accommodation with the given ID using the provided update data.
     *
     * @param id     The ID of the accommodation to update
     * @param update The update data to apply to the accommodation
     * @throws ResourceConflictException if the update contains a name or sesCode that is already used by another accommodation
     * @throws ResourceNotFoundException if no accommodation with the given ID exists
     */
    @Transactional
    public void updateAccommodation(@NonNull UUID id, @NonNull AccommodationDtoRequest update) throws ResourceConflictException, ResourceNotFoundException {
        // Get accommodation to update
        Accommodation accommodation = accommodationRepo
                .findById(id)
                .orElseThrow(NOT_FOUND_EXCEPTION);

        // Ensure name is unique
        if (!accommodation.getName().equals(update.name()) && accommodationRepo.existsByName(update.name()))
            throw SAME_NAME_EXCEPTION.get();

        // Ensure sesCode is unique
        if (!accommodation.getSesCode().equals(update.sesCode()) && accommodationRepo.existsBySesCode(update.sesCode()))
            throw SAME_SESCODE_EXCEPTION.get();

        accommodationMapper.fromUpdate(update, accommodation);
    }

    /**
     * Deletes the accommodation with the given ID.
     *
     * @param id The ID of the accommodation to delete
     * @throws ResourceNotFoundException if no accommodation with the given ID exists
     */
    @Transactional
    public void deleteAccommodation(@NonNull UUID id) throws ResourceNotFoundException {
        // Ensure accommodation exists
        if (!accommodationRepo.existsById(id))
            throw NOT_FOUND_EXCEPTION.get();

        accommodationRepo.deleteById(id);
    }

    /**
     * Assigns the employee with the given ID to the accommodation with the given ID.
     *
     * @param accommodationId The ID of the accommodation to assign the employee to
     * @param employeeId      The ID of the employee to assign to the accommodation
     * @throws ResourceNotFoundException if no accommodation with the given ID exists, or if no employee with the given ID exists
     * @throws ResourceConflictException if the employee is already assigned to the accommodation
     */
    @Transactional
    public void assignEmployeeToAccommodation(@NonNull UUID accommodationId, @NonNull UUID employeeId) throws ResourceNotFoundException, ResourceConflictException {
        Accommodation accommodation = accommodationRepo
                .findById(accommodationId)
                .orElseThrow(NOT_FOUND_EXCEPTION);

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(EMPLOYEE_NOT_FOUND);

        if (accommodation.getEmployees().contains(employee))
            throw new ResourceConflictException("Employee is already assigned to this accommodation");

        accommodation.addEmployee(employee);
    }

    /**
     * Unassigns the employee with the given ID from the accommodation with the given ID.
     *
     * @param accommodationId The ID of the accommodation to unassign the employee from
     * @param employeeId      The ID of the employee to unassign from the accommodation
     * @throws ResourceNotFoundException if no accommodation with the given ID exists, or if no employee with the given ID exists
     * @throws ResourceConflictException if the employee is not assigned to the accommodation
     */
    @Transactional
    public void unassignEmployeeFromAccommodation(@NonNull UUID accommodationId, @NonNull UUID employeeId) throws ResourceNotFoundException, ResourceConflictException {
        Accommodation accommodation = accommodationRepo
                .findById(accommodationId)
                .orElseThrow(NOT_FOUND_EXCEPTION);

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(EMPLOYEE_NOT_FOUND);

        if (!accommodation.getEmployees().contains(employee))
            throw new ResourceConflictException("Employee is not assigned to this accommodation");

        accommodation.removeEmployee(employee);
    }
}
