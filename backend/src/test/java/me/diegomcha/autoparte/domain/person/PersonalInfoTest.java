package me.diegomcha.autoparte.domain.person;

import me.diegomcha.autoparte.TestingUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PersonalInfoTest {

    @Test
    void testNationalityValidation() {
        // Valid
        new PersonalInfo("name", "surname");
        new PersonalInfo("name", "surname", "ESP");

        // Invalid country code
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PersonalInfo("name", "surname", null, "INVALID", null, null));
    }

    @Test
    void testBirthDateValidation() {
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            // Valid
            new PersonalInfo("name", "surname", "ESP");
            new PersonalInfo("name", "surname", "ESP", null, TestingUtils.INSTANT, null);

            // Future birthdate
            Assertions.assertThrows(IllegalArgumentException.class, () -> new PersonalInfo("name", "surname", "ESP", null, TestingUtils.FUTURE_INSTANT, null));
        }
    }
}
