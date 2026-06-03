package me.diegomcha.autoparte.api.employee;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoCreate;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoCreatedResponse;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoPatch;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoResponse;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/employees")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class EmployeeController implements EmployeeAPI {

    private final EmployeeService employeeService;

    @GetMapping
    @Override
    public Page<EmployeeDtoResponse> getEmployees(@ParameterObject Pageable pageable) {
        return employeeService.getEmployees(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public EmployeeDtoCreatedResponse createEmployee(@Valid @RequestBody EmployeeDtoCreate employee) throws ResourceConflictException {
        return employeeService.createEmployee(employee);
    }

    @GetMapping("/{id}")
    @Override
    public EmployeeDtoResponse getEmployee(@PathVariable UUID id) throws ResourceNotFoundException {
        return employeeService.getEmployee(id);
    }

    @PatchMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void updateEmployee(@PathVariable UUID id, @Valid @RequestBody EmployeeDtoPatch employee) throws ResourceNotFoundException, ResourceConflictException {
        employeeService.updateEmployee(id, employee);
    }
}
