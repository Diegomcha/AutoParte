package me.diegomcha.autoparte.domain.person;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ContactInfoTest {

    @Test
    void testAtLeastOneContactMethod() {
        new ContactInfo(null, null, "email@email.com");
        new ContactInfo("971 49 28 05", null, "email@email.com");
        new ContactInfo("971 49 28 05", "+1 (314) 849-1998", "email@email.com");

        // No contact method
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ContactInfo(null, null, null));
    }

    @Test
    void testPhoneValidation() {
        new ContactInfo("971 49 28 05", null, null);
        new ContactInfo(null, "+1 (314) 849-1998", null);
        new ContactInfo("+34 971492805", null, null);

        Assertions.assertThrows(IllegalArgumentException.class, () -> new ContactInfo("invalid", null, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ContactInfo("971 49 28 05", "invalid", null));
    }

    @Test
    void testEmailValidation() {
        new ContactInfo(null, null, "t@t.cc");
        new ContactInfo(null, null, "email@email.com");

        Assertions.assertThrows(IllegalArgumentException.class, () -> new ContactInfo(null, null, "invalid"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ContactInfo("971 49 28 05", null, "invalid@."));
    }
}
