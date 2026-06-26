package me.diegomcha.autoparte.domain.address;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SpanishAddressTest {

    @Test
    void testFactoryMethod() {
        Assertions.assertInstanceOf(SpanishAddress.class, Address.of("line1", null, "12345", "12345", "ESP"));
    }

    @Test
    void testSpanishMunicipalityValidation() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Address.of("line1", null, "invalid", "12345", "ESP"));

        Address.of("line1", null, "12345", "12345", "ESP");
    }

    @Test
    void testSpanishPostalCodeValidation() {
        // Invalid format
        Assertions.assertThrows(IllegalArgumentException.class, () -> Address.of("line1", null, "12345", "invalid", "ESP"));
        // Does not match municipality code
        Assertions.assertThrows(IllegalArgumentException.class, () -> Address.of("line1", "54321", "12345", "invalid", "ESP"));

        Address.of("line1", null, "12345", "12345", "ESP");
    }
}
