package me.diegomcha.autoparte.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EstablishmentTest {

    private Establishment establishment;

    @BeforeEach
    void setUp() {
        this.establishment = new Establishment("Test", "SESCODE");
    }

    @Test
    void testEmployeeAssociation() {
        Employee employee = new Employee("Name", "Surname", "email@email.com", "hashedpassword");

        this.establishment.addEmployee(employee);
        Assertions.assertTrue(this.establishment.getEmployees().contains(employee));
        Assertions.assertTrue(employee.getEstablishments().contains(this.establishment));

        this.establishment.removeEmployee(employee);
        Assertions.assertFalse(this.establishment.getEmployees().contains(employee));
        Assertions.assertFalse(employee.getEstablishments().contains(this.establishment));
    }
}
