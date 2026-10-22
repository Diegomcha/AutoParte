package me.diegomcha.autoparte.core.security;

import me.diegomcha.autoparte.core.event.UpdatePasswordSuccessEvent;
import me.diegomcha.autoparte.core.exception.UnauthorizedException;
import me.diegomcha.autoparte.core.repos.AccountRepo;
import me.diegomcha.autoparte.core.repos.EmployeeRepo;
import me.diegomcha.autoparte.domain.Account;
import me.diegomcha.autoparte.domain.Employee;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@RecordApplicationEvents
class SecurityServiceTest {

    @Autowired
    private SecurityService securityService;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private EmployeeRepo employeeRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ApplicationEvents applicationEvents;

    private Account account;

    @BeforeEach
    void setUp() {
        this.account = accountRepo.save(new Account(
                "security-test@example.com",
                Objects.requireNonNull(passwordEncoder.encode("old-password")),
                Set.of("ROLE_EMPLOYEE")
        ));
        this.account.setRequiresReset(false);
    }

    @Test
    void getsAccountFromSupportedAuthenticationPrincipals() {
        Assertions.assertSame(account, securityService.getAccountFromAuthentication(
                authentication(new UserAccount(account))
        ));
        Assertions.assertSame(account, securityService.getAccountFromAuthentication(
                authentication(User.withUsername(account.getUsername()).password("ignored").roles("EMPLOYEE").build())
        ));
        Assertions.assertSame(account, securityService.getAccountFromAuthentication(
                authentication(account.getUsername())
        ));
    }

    @Test
    void returnsEmbeddedUserAccountWhenUpdatedLookupIsDisabled() {
        var userAccount = new UserAccount(account);

        Assertions.assertSame(account, securityService.getAccountFromAuthentication(authentication(userAccount), false));
        Assertions.assertNull(securityService.getAccountFromAuthentication(
                authentication(User.withUsername(account.getUsername()).password("ignored").build()), false
        ));
    }

    @Test
    void returnsNullForMissingOrUnsupportedAuthentication() {
        Assertions.assertNull(securityService.getAccountFromAuthentication(null));
        Assertions.assertNull(securityService.getAccountFromAuthentication(
                authentication("anonymousUser")
        ));
        Assertions.assertNull(securityService.getAccountFromAuthentication(
                authentication(new Object())
        ));
    }

    @Test
    void getsEmployeeFromAccount() {
        var employee = employeeRepo.save(new Employee(
                "Test",
                "Employee",
                "employee-security-test@example.com",
                Objects.requireNonNull(passwordEncoder.encode("password"))
        ));

        Assertions.assertSame(employee, securityService.getEmployeeFromAccount(employee.getAccount()));
        Assertions.assertNull(securityService.getEmployeeFromAccount(account));
    }

    @Test
    void updatesPasswordAndPublishesSuccessEvent() throws UnauthorizedException {
        securityService.updatePassword(account.getUsername(), "old-password", "new-password", authenticationDetails());

        var updatedAccount = accountRepo.findByUsername(account.getUsername()).orElseThrow();
        Assertions.assertTrue(passwordEncoder.matches("new-password", updatedAccount.getHashedPassword()));
        Assertions.assertEquals(1, applicationEvents.stream(UpdatePasswordSuccessEvent.class).count());
    }

    @Test
    void rejectsInvalidCurrentPassword() {
        Assertions.assertThrows(UnauthorizedException.class, () ->
                securityService.updatePassword(account.getUsername(), "wrong-password", "new-password", authenticationDetails())
        );

        Assertions.assertTrue(passwordEncoder.matches("old-password", account.getHashedPassword()));
        Assertions.assertEquals(0, applicationEvents.stream(UpdatePasswordSuccessEvent.class).count());
    }

    @Test
    void allowsPasswordUpdateWhenCredentialsHaveExpired() throws UnauthorizedException {
        account.setRequiresReset(true);

        securityService.updatePassword(account.getUsername(), "old-password", "new-password", authenticationDetails());

        Assertions.assertTrue(passwordEncoder.matches("new-password", account.getHashedPassword()));
        Assertions.assertFalse(account.isRequiresReset());
    }

    // * Helper methods

    private Authentication authentication(Object principal) {
        return UsernamePasswordAuthenticationToken.authenticated(principal, "credentials", Set.of());
    }

    private WebAuthenticationDetails authenticationDetails() {
        return new WebAuthenticationDetails("test", null);
    }
}
