package me.diegomcha.autoparte.domain.person;

import me.diegomcha.autoparte.TestingUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

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

    private static final Object[][] IS_COMPLETE_MCDC = new Boolean[][]{
            new Boolean[]{true, true, true,    /* = */ true},
            new Boolean[]{true, false, true,   /* = */ false},
            new Boolean[]{false, false, true,  /* = */ true},
            new Boolean[]{false, false, false, /* = */ false},
    };

    @ParameterizedTest
    @FieldSource("IS_COMPLETE_MCDC")
    void testIsComplete(boolean reqSSur, boolean hasSSur, boolean hasBirth, boolean expected) {
        var secondSurname = hasSSur ? "2surname" : null;
        var birthDate = hasBirth ? TestingUtils.PAST_INSTANT : null;

        var pInfo = new PersonalInfo("name", "surname", secondSurname, null, birthDate, null);
        Assertions.assertEquals(expected, pInfo.isComplete(reqSSur));
    }

    private static final Object[][] BIRTHDATES = new Object[][]{
            new Object[]{
                    null,
                    false
            },
            new Object[]{
                    TestingUtils.INSTANT.minus(17 * 365, ChronoUnit.DAYS), // Minor
                    false
            },
            new Object[]{
                    TestingUtils.INSTANT.minus((18 * 365) - 1, ChronoUnit.DAYS), // Almost adult
                    false
            },
            new Object[]{
                    TestingUtils.INSTANT.minus(18 * 365, ChronoUnit.DAYS), // Just adult
                    true
            },
            new Object[]{
                    TestingUtils.INSTANT.minus(19 * 365, ChronoUnit.DAYS), // Adult
                    true
            },
    };

    @ParameterizedTest
    @FieldSource("BIRTHDATES")
    void testIsAdult(Instant birthDate, boolean expected) {
        var pInfo = new PersonalInfo("name", "surname", null, null, birthDate, null);
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            Assertions.assertEquals(expected, pInfo.isAdult());
        }
    }
}
