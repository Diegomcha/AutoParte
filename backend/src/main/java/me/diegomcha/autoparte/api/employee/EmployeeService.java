package me.diegomcha.autoparte.api.employee;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoCreate;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoCredentialsResponse;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoPatch;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoResponse;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import me.diegomcha.autoparte.core.repos.EmployeeRepo;
import me.diegomcha.autoparte.domain.Employee;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional(readOnly = true)
class EmployeeService {

    private static final Supplier<ResourceNotFoundException> NOT_FOUND_EXCEPTION = () ->
            new ResourceNotFoundException("Employee not found");
    private static final Supplier<ResourceConflictException> SAME_EMAIL_EXCEPTION = () ->
            new ResourceConflictException("An employee with the same email already exists");

    private static final Map<String, String> EMPLOYEE_SORT_MAP = Map.of(
            "enabled", "account.enabled",
            "email", "account.username"
    );

    private final EmployeeRepo employeeRepo;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Returns a paginated list of all employees.
     *
     * @param pageable Pagination information (page number, size, sorting)
     * @return A page of employees
     */
    public Page<EmployeeDtoResponse> getEmployees(@NonNull Pageable pageable) {
        List<Sort.Order> translatedOrders = pageable.getSort().stream()
                .map(order -> new Sort.Order(
                        order.getDirection(),
                        EMPLOYEE_SORT_MAP.getOrDefault(order.getProperty(), order.getProperty())
                ))
                .toList();

        Pageable translatedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(translatedOrders)
        );

        return employeeMapper.toResponse(employeeRepo.findAll(translatedPageable));
    }

    /**
     * Returns the employee with the given ID, if it exists.
     *
     * @param id The ID of the employee to retrieve
     * @return The employee with the given ID, or empty if it does not exist
     */
    public EmployeeDtoResponse getEmployee(@NonNull UUID id) throws ResourceNotFoundException {
        return employeeRepo
                .findById(id)
                .map(employeeMapper::toResponse)
                .orElseThrow(NOT_FOUND_EXCEPTION);
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
    public EmployeeDtoCredentialsResponse createEmployee(@NonNull EmployeeDtoCreate dto) throws ResourceConflictException {
        // Generate random secure password
        String password = this.getRandomSecurePassword();
        Employee newEmployee = employeeMapper.fromCreate(dto, passwordEncoder.encode(password));

        // Ensure email is unique
        if (employeeRepo.existsByEmail(newEmployee.getEmail()))
            throw SAME_EMAIL_EXCEPTION.get();

        // TODO: Send credentials via email

        newEmployee = employeeRepo.save(newEmployee);
        return employeeMapper.toCredentials(newEmployee, password);
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
    public void updateEmployee(@NonNull UUID id, @NonNull EmployeeDtoPatch patch) throws ResourceConflictException, ResourceNotFoundException {
        // Get employee to patch
        Employee employee = employeeRepo
                .findById(id)
                .orElseThrow(NOT_FOUND_EXCEPTION);

        // Ensure email is unique
        String email = employeeMapper.normalizeEmail(patch.email());
        if (email != null && !employee.getEmail().equals(email) && employeeRepo.existsByEmail(email))
            throw SAME_EMAIL_EXCEPTION.get();

        employeeMapper.patchEmployee(patch, employee);
    }

    /**
     * Resets the password of the employee with the given ID, generating a new random password and returning it in the response.
     *
     * @param id The ID of the employee whose password will be reset
     * @return The employee with the new password
     * @throws ResourceNotFoundException If no employee with the given ID exists
     */
    @Transactional
    public EmployeeDtoCredentialsResponse resetEmployeePassword(@NonNull UUID id) throws ResourceNotFoundException {
        // Get employee whose credentials will be reset
        Employee employee = employeeRepo
                .findById(id)
                .orElseThrow(NOT_FOUND_EXCEPTION);

        // Generate new random password and update employee account
        String password = this.getRandomSecurePassword();
        employee.getAccount().resetPassword(Objects.requireNonNull(passwordEncoder.encode(password)));

        // TODO: Send credentials via email

        return employeeMapper.toCredentials(employee, password);
    }

    /**
     * Deletes the employee with the given ID.
     *
     * @param id The ID of the employee to delete
     * @throws ResourceNotFoundException If no employee with the given ID exists
     */
    @Transactional
    public void deleteEmployee(@NonNull UUID id) throws ResourceNotFoundException {
        // Ensure employee exists
        if (!employeeRepo.existsById(id))
            throw NOT_FOUND_EXCEPTION.get();

        employeeRepo.deleteById(id);
    }

    private String getRandomSecurePassword() {
        return RandomStringUtils.secureStrong().nextAlphanumeric(16);
    }
}
