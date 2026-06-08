package me.diegomcha.autoparte.api.employee;

import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoCreate;
import me.diegomcha.autoparte.api.employee.dto.EmployeeDtoPatch;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import me.diegomcha.autoparte.domain.Employee;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Stream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestEntityManager
class EmployeeServiceTest {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private EmployeeRepo employeeRepo;

    private Employee employee;

    @BeforeEach
    void setUp() {
        this.employee = entityManager.persist(new Employee("Name", "Surname", "email@email.com", "hashedpassword"));
    }

    @Test
    void testGetEmployees() {
        // One employee in the database
        var employeesPage = employeeService.getEmployees(Pageable.unpaged());

        Assertions.assertEquals(1, employeesPage.getTotalElements());
        Assertions.assertEquals(employee.getId(), employeesPage.getContent().getFirst().id());

        // No employees in the database
        entityManager.remove(employee);
        employeesPage = employeeService.getEmployees(Pageable.unpaged());

        Assertions.assertEquals(0, employeesPage.getTotalElements());
    }

    @Test
    void testGetEmployee() throws ResourceNotFoundException {
        Assertions.assertNotNull(employee.getId());
        var employeeResponse = employeeService.getEmployee(employee.getId());

        Assertions.assertEquals(employee.getId(), employeeResponse.id());
        Assertions.assertEquals(employee.getName(), employeeResponse.name());
        Assertions.assertEquals(employee.getSurname(), employeeResponse.surname());
        Assertions.assertEquals(employee.getEmail(), employeeResponse.email());

        // Non-existing employee
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                employeeService.getEmployee(UUID.randomUUID())
        );
    }

    @Test
    void testCreateEmployee() throws ResourceConflictException {
        var createdEmployee = employeeService.createEmployee(new EmployeeDtoCreate("Name1", "Surname1", "email1@email.com"));

        Assertions.assertEquals("email1@email.com", createdEmployee.email());
        Assertions.assertNotNull(createdEmployee.id());
        Assertions.assertNotNull(createdEmployee.createdAt());
        Assertions.assertNotNull(createdEmployee.password());

        var dbEmployee = entityManager.find(Employee.class, createdEmployee.id());

        Assertions.assertNotNull(dbEmployee);
        Assertions.assertEquals("email1@email.com", dbEmployee.getEmail());
    }

    @Test
    void testCreateEmployeeFailed() {
        Assertions.assertThrows(ResourceConflictException.class, () ->
                employeeService.createEmployee(new EmployeeDtoCreate("Name2", "Surname2", "email@email.com"))
        );

        Assertions.assertEquals(1, Stream.of(employeeRepo.findAll()).count());
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

        var employeeDb = entityManager.find(Employee.class, employee.getId());

        Assertions.assertNotNull(employeeDb);
        Assertions.assertEquals(expected.enabled(), employeeDb.getAccount().isEnabled());
        Assertions.assertEquals(expected.name(), employeeDb.getName());
        Assertions.assertEquals(expected.surname(), employeeDb.getSurname());
        Assertions.assertEquals(expected.email(), employeeDb.getEmail());
    }

    @Test
    void testUpdateEmployeeFailed() {
        // Non-existing employee
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                employeeService.updateEmployee(UUID.randomUUID(), new EmployeeDtoPatch(null, null, null, null))
        );

        // Same email as another employee
        entityManager.persist(new Employee("Name1", "Surname1", "email1@email.com", "hashedpassword"));

        Assertions.assertNotNull(employee.getId());
        Assertions.assertThrows(ResourceConflictException.class, () ->
                employeeService.updateEmployee(employee.getId(), new EmployeeDtoPatch(null,null, null, "email1@email.com"))
        );

        Assertions.assertNotNull(employee.getId());
        var employeeDb = entityManager.find(Employee.class, employee.getId());

        Assertions.assertNotNull(employeeDb);
        Assertions.assertEquals("email@email.com", employeeDb.getEmail());
    }
}
