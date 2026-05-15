package me.diegomcha.autoparte.api.employee;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoCreate;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoCreatedResponse;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoPatch;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoResponse;
import me.diegomcha.autoparte.model.Employee;
import me.diegomcha.autoparte.util.exception.ResourceConflictException;
import me.diegomcha.autoparte.util.exception.ResourceNotFoundException;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional(readOnly = true)
class EmployeeService {

    private final EmployeeRepo employeeRepo;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    private static final Supplier<ResourceNotFoundException> NOT_FOUND_EXCEPTION = () ->
            new ResourceNotFoundException("Employee not found");
    private static final Supplier<ResourceConflictException> SAME_EMAIL_EXCEPTION = () ->
            new ResourceConflictException("An employee with the same email already exists");

    /**
     * Returns a paginated list of all employees.
     *
     * @param pageable Pagination information (page number, size, sorting)
     * @return A page of employees
     */
    public Page<EmployeeDtoResponse> getEmployees(Pageable pageable) {
        return employeeMapper.toResponse(employeeRepo.findAll(pageable));
    }

    /**
     * Creates a new employee with the given information.
     * The email must be unique, and a random password will be generated for the employee (to be reset on first login).
     *
     * @param dto The information of the employee to create
     * @return The created employee, including the generated password
     * @throws ResourceConflictException If an employee with the same email already exists
     */
    @Transactional
    public EmployeeDtoCreatedResponse createEmployee(@NonNull EmployeeDtoCreate dto) throws ResourceConflictException {
        // Create random password (to be reset on first login)
        String password = RandomStringUtils.secureStrong().nextAlphanumeric(12);
        Employee newEmployee = employeeMapper.fromCreate(dto, passwordEncoder.encode(password));

        // Ensure email is unique
        if (employeeRepo.existsByUsername(newEmployee.getEmail()))
            throw SAME_EMAIL_EXCEPTION.get();

        employeeRepo.save(newEmployee);
        return employeeMapper.toCreated(newEmployee, password);
    }

    /**
     * Returns the employee with the given ID, if it exists.
     *
     * @param id The ID of the employee to retrieve
     * @return The employee with the given ID, or empty if it does not exist
     */
    public EmployeeDtoResponse getEmployee(UUID id) throws ResourceNotFoundException {
        return employeeRepo
                .findById(id)
                .map(employeeMapper::toResponse)
                .orElseThrow(NOT_FOUND_EXCEPTION);
    }


    /**
     * Updates the employee with the given ID using the provided patch data.
     *
     * @param id    The ID of the employee to update
     * @param patch The patch data containing the fields to update (name, surname, email)
     * @throws ResourceNotFoundException If no employee with the given ID exists
     * @throws ResourceConflictException If the new email is already used by another employee
     */
    @Transactional
    public void updateEmployee(UUID id, EmployeeDtoPatch patch) throws ResourceConflictException, ResourceNotFoundException {
        // Get employee to patch
        Employee employee = employeeRepo
                .findById(id)
                .orElseThrow(NOT_FOUND_EXCEPTION);

        // Ensure email is unique
        if (patch.email() != null) {
            String email = employeeMapper.normalizeEmail(patch.email());
            if (!employee.getEmail().equals(email) && employeeRepo.existsByUsername(email))
                throw SAME_EMAIL_EXCEPTION.get();
        }

        employeeMapper.patchEmployee(patch, employee);
    }
}
