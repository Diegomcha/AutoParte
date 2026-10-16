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
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            // Valid
            new PersonalInfo("name", "surname", null, null, TestingUtils.PAST_INSTANT, null);
            new PersonalInfo("name", "surname", "ESP", null, TestingUtils.PAST_INSTANT, null);

            // Invalid country code
            Assertions.assertThrows(IllegalArgumentException.class, () -> new PersonalInfo("name", "surname", null, "INVALID", TestingUtils.PAST_INSTANT, null));
        }
    }

    @Test
    void testBirthDateValidation() {
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            // Valid
            new PersonalInfo("name", "surname", "ESP", null, TestingUtils.PAST_INSTANT, null);
            new PersonalInfo("name", "surname", "ESP", null, TestingUtils.INSTANT, null);
            new PersonalInfo("name", "surname", "ESP", null, null, null);

            // Future birthdate
            Assertions.assertThrows(IllegalArgumentException.class, () -> new PersonalInfo("name", "surname", "ESP", null, TestingUtils.FUTURE_INSTANT, null));
        }
    }

    private static final Object[][] IS_COMPLETE_MCDC = new Boolean[][]{
            new Boolean[]{true, true, true,     /* = */ true},
            new Boolean[]{false, true, true,    /* = */ false},
            new Boolean[]{true, true, false,    /* = */ false},
            new Boolean[]{true, false, false,   /* = */ true},
    };

    @ParameterizedTest
    @FieldSource("IS_COMPLETE_MCDC")
    void testIsComplete(boolean hasBirthdate, boolean reqSSur, boolean hasSSur, boolean expected) {
        var secondSurname = hasSSur ? "2surname" : null;
        var birthDate = hasBirthdate ? TestingUtils.PAST_INSTANT : null;

        try (var ignored = TestingUtils.getMockedInstantNow()) {
            var pInfo = new PersonalInfo("name", "surname", secondSurname, null, birthDate, null);
            Assertions.assertEquals(expected, pInfo.isCompleteForCheckIn(reqSSur));
        }
    }

    private static final Object[][] ADULT_BIRTHDATES = new Object[][]{
            new Object[]{
                    TestingUtils.INSTANT.minus((PersonalInfo.SPAIN_ADULT_AGE - 1) * 365, ChronoUnit.DAYS), // Minor
                    false
            },
            new Object[]{
                    TestingUtils.INSTANT.minus((PersonalInfo.SPAIN_ADULT_AGE * 365) - 1, ChronoUnit.DAYS), // Almost adult
                    false
            },
            new Object[]{
                    TestingUtils.INSTANT.minus(PersonalInfo.SPAIN_ADULT_AGE * 365, ChronoUnit.DAYS), // Just adult
                    true
            },
            new Object[]{
                    TestingUtils.INSTANT.minus((PersonalInfo.SPAIN_ADULT_AGE + 1) * 365, ChronoUnit.DAYS), // Adult
                    true
            },
            new Object[]{
                    null, // No birthdate
                    null
            }
    };

    @ParameterizedTest
    @FieldSource("ADULT_BIRTHDATES")
    void testIsAdult(Instant birthDate, Boolean expected) {
        var pInfo = new PersonalInfo("name", "surname", null, null, birthDate, null);
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            Assertions.assertEquals(expected, pInfo.isAdult());
        }
    }

    private static final Object[][] SIGNER_BIRTHDATES = new Object[][]{
            new Object[]{
                    TestingUtils.INSTANT.minus((PersonalInfo.SPAIN_MUST_SIGN_AGE - 1) * 365, ChronoUnit.DAYS), // No required
                    false
            },
            new Object[]{
                    TestingUtils.INSTANT.minus((PersonalInfo.SPAIN_MUST_SIGN_AGE * 365) - 1, ChronoUnit.DAYS), // Almost required
                    false
            },
            new Object[]{
                    TestingUtils.INSTANT.minus(PersonalInfo.SPAIN_MUST_SIGN_AGE * 365, ChronoUnit.DAYS), // Just required
                    true
            },
            new Object[]{
                    TestingUtils.INSTANT.minus((PersonalInfo.SPAIN_MUST_SIGN_AGE + 1) * 365, ChronoUnit.DAYS), // Required
                    true
            },
            new Object[]{
                    null, // No birthdate
                    null
            }
    };

    @ParameterizedTest
    @FieldSource("SIGNER_BIRTHDATES")
    void testMustSign(Instant birthDate, Boolean expected) {
        var pInfo = new PersonalInfo("name", "surname", null, null, birthDate, null);
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            Assertions.assertEquals(expected, pInfo.mustSign());
        }
    }
}
