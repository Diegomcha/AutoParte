package me.diegomcha.autoparte.api.employee;

import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoCreate;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoCreatedResponse;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoPatch;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoResponse;
import me.diegomcha.autoparte.domain.Employee;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
abstract class EmployeeMapper {

    public abstract EmployeeDtoResponse toResponse(Employee employee);

    public Page<EmployeeDtoResponse> toResponse(Page<Employee> page) {
        return page.map(this::toResponse);
    }

    public abstract EmployeeDtoCreatedResponse toCreated(Employee employee, String password);

    @Mapping(target = "email", qualifiedByName = "normalizeEmail")
    public abstract Employee fromCreate(EmployeeDtoCreate create, String hashedPassword);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "email", qualifiedByName = "normalizeEmail")
    public abstract void patchEmployee(EmployeeDtoPatch patch, @MappingTarget Employee employee);

    @AfterMapping
    protected void updateAccountEnabled(EmployeeDtoPatch patch, @MappingTarget Employee employee) {
        if (patch.enabled() != null)
            employee.getAccount().setEnabled(patch.enabled());
    }

    @Named("normalizeEmail")
    public String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase().trim();
    }
}
