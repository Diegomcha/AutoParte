package me.diegomcha.autoparte.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmployeeTest {

    private Employee employee;

    @BeforeEach
    void setUp() {
        this.employee = new Employee("Name", "Surname", "email@email.com", "hashedpassword");
    }

    @Test
    void testConstructionContract() {
        Assertions.assertNotNull(employee.getAccount());
    }

    @Test
    void testEmailAlias() {
        Assertions.assertEquals(employee.getEmail(), employee.getAccount().getUsername());
        employee.setEmail("other@email.com");
        Assertions.assertEquals(employee.getEmail(), employee.getAccount().getUsername());
    }

    @Test
    void testEmailValidation() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> employee.setEmail("invalid-email"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Employee("Name", "Surname", "invalid-email", "hashedpassword"));
        Assertions.assertDoesNotThrow(() -> employee.setEmail("t@t.io"));
    }
}
