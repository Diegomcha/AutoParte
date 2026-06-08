package me.diegomcha.autoparte.api.auth;

import me.diegomcha.autoparte.api.auth.dto.UpdatePasswordDto;
import me.diegomcha.autoparte.core.event.UpdatePasswordSuccessEvent;
import me.diegomcha.autoparte.core.exception.UnauthorizedException;
import me.diegomcha.autoparte.domain.Account;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@RecordApplicationEvents
class AuthServiceTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TestEntityManager testEntityManager;
    @Autowired
    private ApplicationEvents applicationEvents;

    private Account account;

    @BeforeEach
    void setUp() {
        this.account = testEntityManager.persist(new Account("test", Objects.requireNonNull(passwordEncoder.encode("test")), Set.of("ROLE_TEST")));
        this.account.setRequiresReset(false);
    }

    @Test
    void testGetLoggedInAccountAnonymous() {
        Assertions.assertNull(authService.getLoggedInAccount());
    }

    @Test
    @WithMockUser("test")
    void testGetLoggedInAccountLoggedIn() {
        var loggedInAccount = authService.getLoggedInAccount();

        Assertions.assertNotNull(loggedInAccount);
        Assertions.assertEquals("test", loggedInAccount.username());
        Assertions.assertEquals(Set.of("ROLE_TEST"), loggedInAccount.roles());
    }

    @Test
    void testUpdatePassword() throws UnauthorizedException {
        authService.updatePassword(new UpdatePasswordDto("test", "test", "newpassword"), new WebAuthenticationDetails("test", null));

        Assertions.assertNotNull(this.account.getId());
        Account dbAccount = testEntityManager.find(Account.class, this.account.getId());

        Assertions.assertNotNull(dbAccount);
        Assertions.assertTrue(passwordEncoder.matches("newpassword", dbAccount.getHashedPassword()));

        Assertions.assertEquals(1, applicationEvents.stream(UpdatePasswordSuccessEvent.class).count());
    }

    @Test
    void testUpdatePasswordInvalidCurrentPassword() {
        Assertions.assertThrows(UnauthorizedException.class, () ->
                authService.updatePassword(new UpdatePasswordDto("test", "wrongpassword", "newpassword"), new WebAuthenticationDetails("test", null))
        );
    }
}
