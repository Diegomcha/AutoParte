package me.diegomcha.autoparte.api.employee;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoCreate;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoCreatedResponse;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoPatch;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoResponse;
import me.diegomcha.autoparte.util.exception.ResourceConflictException;
import me.diegomcha.autoparte.util.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

// TODO: ....
@Tag(name = "Employees", description = "Operations related to employees")
@SuppressWarnings("unused")
public interface EmployeeAPI {

    @Operation(summary = "List employees")
    Page<EmployeeDtoResponse> getEmployees(Pageable pageable);

    @Operation(summary = "Create employee")
    EmployeeDtoCreatedResponse createEmployee(EmployeeDtoCreate employee) throws ResourceConflictException;

    @Operation(summary = "Get employee by id")
    EmployeeDtoResponse getEmployee(UUID id) throws ResourceNotFoundException;

    @Operation(summary = "Update employee")
    void updateEmployee(UUID id, EmployeeDtoPatch employee) throws ResourceNotFoundException, ResourceConflictException;
}
