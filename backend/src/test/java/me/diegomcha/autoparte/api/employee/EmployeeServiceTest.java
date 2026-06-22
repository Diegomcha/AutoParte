package me.diegomcha.autoparte.api.employee;

import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoCreate;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoPatch;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import me.diegomcha.autoparte.core.repos.AccountRepo;
import me.diegomcha.autoparte.core.repos.EmployeeRepo;
import me.diegomcha.autoparte.domain.Employee;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.StreamSupport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase
class EmployeeServiceTest {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private EmployeeRepo employeeRepo;
    @Autowired
    private AccountRepo accountRepo;

    private Employee employee;

    @BeforeEach
    void setUp() {
        this.employee = employeeRepo.save(new Employee("Name", "Surname", "email@email.com", "hashedpassword"));
    }

    @Test
    void testGetEmployees() {
        // One employee in the database
        var employeesPage = employeeService.getEmployees(Pageable.unpaged());

        Assertions.assertEquals(1, employeesPage.getTotalElements());
        Assertions.assertEquals(employee.getId(), employeesPage.getContent().getFirst().id());

        // No employees in the database
        employeeRepo.delete(employee);
        employeesPage = employeeService.getEmployees(Pageable.unpaged());

        Assertions.assertEquals(0, employeesPage.getTotalElements());
    }

    @Test
    void testGetEmployee() throws ResourceNotFoundException {
        Assertions.assertNotNull(employee.getId());
        var employeeResponse = employeeService.getEmployee(employee.getId());

        Assertions.assertEquals(employee.getId(), employeeResponse.id());

        Assertions.assertEquals(employee.getAccount().isEnabled(), employeeResponse.enabled());
        Assertions.assertEquals(employee.getAccount().getDisabledAt(), employeeResponse.disabledAt());

        Assertions.assertNotNull(employeeResponse.accommodations());

        // Non-existing employee
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                employeeService.getEmployee(UUID.randomUUID())
        );
    }

    @Test
    void testCreateEmployee() throws ResourceConflictException {
        var createdEmployee = employeeService.createEmployee(new EmployeeDtoCreate("Name1", "Surname1", "email1@email.com"));

        Assertions.assertEquals("email1@email.com", createdEmployee.email());
        Assertions.assertNotNull(createdEmployee.password());

        var dbEmployee = employeeRepo.findByEmail("email1@email.com");

        Assertions.assertTrue(dbEmployee.isPresent());
        Assertions.assertEquals("email1@email.com", dbEmployee.get().getEmail());
    }

    @Test
    void testResetEmployeePassword() throws ResourceNotFoundException {
        Assertions.assertNotNull(employee.getId());

        var credentials = employeeService.resetEmployeePassword(employee.getId());

        Assertions.assertNotNull(credentials.password());
        Assertions.assertNotEquals("hashedpassword", employee.getAccount().getHashedPassword());
    }

    @Test
    void testResetEmployeePasswordFailed() {
        // Non-existing employee
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                employeeService.resetEmployeePassword(UUID.randomUUID())
        );
    }

    @Test
    void testCreateEmployeeFailed() {
        Assertions.assertThrows(ResourceConflictException.class, () ->
                employeeService.createEmployee(new EmployeeDtoCreate("Name2", "Surname2", "email@email.com"))
        );

        Assertions.assertEquals(1, StreamSupport.stream(employeeRepo.findAll().spliterator(), false).count());
    }

    private static final EmployeeDtoPatch[][] UPDATE_PATCHES = new EmployeeDtoPatch[][]{
            new EmployeeDtoPatch[]{
                    new EmployeeDtoPatch(null, null, null, null),                     // Patch
                    new EmployeeDtoPatch(true, "Name", "Surname", "email@email.com")  // Expected
            },
            new EmployeeDtoPatch[]{
                    new EmployeeDtoPatch(false, null, null, null),
                    new EmployeeDtoPatch(false, "Name", "Surname", "email@email.com")
            },
            new EmployeeDtoPatch[]{
                    new EmployeeDtoPatch(false, "Name1", null, null),
                    new EmployeeDtoPatch(false, "Name1", "Surname", "email@email.com")
            },
            new EmployeeDtoPatch[]{
                    new EmployeeDtoPatch(false, "Name1", "Surname1", null),
                    new EmployeeDtoPatch(false, "Name1", "Surname1", "email@email.com")
            },
            new EmployeeDtoPatch[]{
                    new EmployeeDtoPatch(false, "Name1", "Surname1", "email1@email.com"),
                    new EmployeeDtoPatch(false, "Name1", "Surname1", "email1@email.com")
            }
    };

    @ParameterizedTest
    @FieldSource("UPDATE_PATCHES")
    void testUpdateEmployee(EmployeeDtoPatch patch, EmployeeDtoPatch expected) throws ResourceNotFoundException, ResourceConflictException {
        Assertions.assertNotNull(employee.getId());

        employeeService.updateEmployee(employee.getId(), patch);

        var employeeDb = employeeRepo.findById(employee.getId());

        Assertions.assertTrue(employeeDb.isPresent());
        Assertions.assertEquals(expected.enabled(), employeeDb.get().getAccount().isEnabled());
        Assertions.assertEquals(expected.name(), employeeDb.get().getName());
        Assertions.assertEquals(expected.surname(), employeeDb.get().getSurname());
        Assertions.assertEquals(expected.email(), employeeDb.get().getEmail());
    }

    @Test
    void testUpdateEmployeeFailed() {
        // Non-existing employee
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                employeeService.updateEmployee(UUID.randomUUID(), new EmployeeDtoPatch(null, null, null, null))
        );

        // Same email as another employee
        employeeRepo.save(new Employee("Name1", "Surname1", "email1@email.com", "hashedpassword"));

        Assertions.assertNotNull(employee.getId());
        Assertions.assertThrows(ResourceConflictException.class, () ->
                employeeService.updateEmployee(employee.getId(), new EmployeeDtoPatch(null,null, null, "email1@email.com"))
        );

        Assertions.assertNotNull(employee.getId());
        var employeeDb = employeeRepo.findById(employee.getId());

        Assertions.assertTrue(employeeDb.isPresent());
        Assertions.assertEquals("email@email.com", employeeDb.get().getEmail());
    }

    @Test
    void testDeleteEmployee() throws ResourceNotFoundException {
        Assertions.assertNotNull(employee.getId());
        Assertions.assertNotNull(employee.getAccount().getId());

        employeeService.deleteEmployee(employee.getId());

        Assertions.assertFalse(employeeRepo.existsById(employee.getId()));
        Assertions.assertFalse(accountRepo.existsById(employee.getAccount().getId()));
    }

    @Test
    void testDeleteEmployeeFailed() {
        Assertions.assertNotNull(employee.getId());

        // Non-existing employee
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                employeeService.deleteEmployee(UUID.randomUUID())
        );

        Assertions.assertTrue(employeeRepo.existsById(employee.getId()));
    }
}
