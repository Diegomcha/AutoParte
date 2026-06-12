package me.diegomcha.autoparte.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        this.account = new Account("testuser", "hashedpassword", Set.of("ROLE_TEST"));
    }

    @Test
    void testConstructionContract() {
        Assertions.assertTrue(account.isEnabled());
        Assertions.assertNull(account.getDisabledAt());
        Assertions.assertTrue(account.isRequiresReset());
    }

    @Test
    void testEnablement() {
        Assertions.assertTrue(account.isEnabled());
        Assertions.assertNull(account.getDisabledAt());

        account.setEnabled(false);

        Assertions.assertFalse(account.isEnabled());
        Assertions.assertNotNull(account.getDisabledAt());

        account.setEnabled(true);

        Assertions.assertTrue(account.isEnabled());
        Assertions.assertNull(account.getDisabledAt());
    }

    @Test
    void testPasswordReset() {
        Assertions.assertTrue(account.isRequiresReset());

        account.setHashedPassword("newhashedpassword");

        Assertions.assertFalse(account.isRequiresReset());
    }
}
