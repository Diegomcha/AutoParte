package me.diegomcha.autoparte.domain;

import me.diegomcha.autoparte.TestingUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

class SecurityEventTest {

    private Account account;

    @BeforeEach
    void setUp() {
        this.account = new Account("testuser", "hashedpassword", Set.of("ROLE_TEST"));
    }

    @Test
    void testAccountAssociation() {
        var event = new SecurityEvent(
                TestingUtils.INSTANT,
                "addr",
                SecurityEvent.SecurityEventType.LOGIN,
                SecurityEvent.SecurityEventMethod.USERNAME_PASSWORD,
                this.account
        );

        Assertions.assertTrue(this.account.getSecurityLog().contains(event));
    }

    @Test
    void testTimestampValidation() {
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            new SecurityEvent(
                    TestingUtils.INSTANT,
                    "addr",
                    SecurityEvent.SecurityEventType.LOGIN,
                    SecurityEvent.SecurityEventMethod.USERNAME_PASSWORD,
                    this.account
            );

            new SecurityEvent(
                    TestingUtils.PAST_INSTANT,
                    "addr",
                    SecurityEvent.SecurityEventType.LOGIN,
                    SecurityEvent.SecurityEventMethod.USERNAME_PASSWORD,
                    this.account
            );

            Assertions.assertThrows(IllegalArgumentException.class, () -> new SecurityEvent(
                    TestingUtils.FUTURE_INSTANT,
                    "addr",
                    SecurityEvent.SecurityEventType.LOGIN,
                    SecurityEvent.SecurityEventMethod.USERNAME_PASSWORD,
                    this.account
            ));
        }
    }
}
