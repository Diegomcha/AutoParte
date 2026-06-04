package me.diegomcha.autoparte.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

class SecurityEventTest {

    @Test
    void testAccountAssociation() {
        var account = new Account("testuser", "hashedpassword", Set.of("ROLE_TEST"));
        var event = new SecurityEvent(
                Instant.now(),
                "addr",
                SecurityEvent.SecurityEventType.LOGIN,
                SecurityEvent.SecurityEventMethod.USERNAME_PASSWORD,
                account
        );

        Assertions.assertTrue(account.getSecurityLog().contains(event));
    }
}
