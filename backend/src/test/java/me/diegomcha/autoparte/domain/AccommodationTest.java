package me.diegomcha.autoparte.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccommodationTest {

    private Accommodation accommodation;

    @BeforeEach
    void setUp() {
        this.accommodation = new Accommodation("Test", "SESCODE", null);
    }

    @Test
    void testEmployeeAssociation() {
        Employee employee = new Employee("Name", "Surname", "email@email.com", "hashedpassword");

        this.accommodation.addEmployee(employee);
        Assertions.assertTrue(this.accommodation.getEmployees().contains(employee));
        Assertions.assertTrue(employee.getAccommodations().contains(this.accommodation));

        this.accommodation.removeEmployee(employee);
        Assertions.assertFalse(this.accommodation.getEmployees().contains(employee));
        Assertions.assertFalse(employee.getAccommodations().contains(this.accommodation));
    }
}
