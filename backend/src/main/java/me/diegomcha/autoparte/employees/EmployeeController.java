package me.diegomcha.autoparte.employees;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.employees.dto.EmployeeDtoCreatedResponse;
import me.diegomcha.autoparte.employees.dto.EmployeeDtoPatch;
import me.diegomcha.autoparte.employees.dto.EmployeeDtoResponse;
import me.diegomcha.autoparte.employees.dto.EmployeeDtoCreate;
import me.diegomcha.autoparte.exceptions.ResourceConflictException;
import me.diegomcha.autoparte.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/employees")
@RequiredArgsConstructor
class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public Page<EmployeeDtoResponse> getEmployees(Pageable pageable) {
        return employeeService.getEmployees(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeDtoCreatedResponse createEmployee(@Valid @RequestBody EmployeeDtoCreate employee) throws ResourceConflictException {
        return employeeService.createEmployee(employee);
    }

    @GetMapping("/{id}")
    public EmployeeDtoResponse getEmployee(@PathVariable UUID id) throws ResourceNotFoundException {
        return employeeService.getEmployee(id);
    }

    @PatchMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateEmployee(@PathVariable UUID id, @Valid @RequestBody EmployeeDtoPatch employee) throws ResourceNotFoundException, ResourceConflictException {
        employeeService.updateEmployee(id, employee);
    }
}
