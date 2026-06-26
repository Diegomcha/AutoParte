package me.diegomcha.autoparte.domain.address;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AddressTest {

    @Test
    void testFactoryMethod() {
        Assertions.assertSame(Address.class, Address.of("line1", null, "muni", "postal", "USA").getClass());
    }

    @Test
    void testCountryValidation() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Address.of("line1", null, "muni", "postal", "INVALID"));

        Address.of("line1", null, "muni", "postal", "USA");
    }
}

