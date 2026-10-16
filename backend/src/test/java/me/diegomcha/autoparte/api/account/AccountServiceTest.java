package me.diegomcha.autoparte.api.account;

import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import me.diegomcha.autoparte.core.repos.AccountRepo;
import me.diegomcha.autoparte.core.repos.EmployeeRepo;
import me.diegomcha.autoparte.core.repos.SecurityEventRepo;
import me.diegomcha.autoparte.domain.Account;
import me.diegomcha.autoparte.domain.Employee;
import me.diegomcha.autoparte.domain.SecurityEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
public class AccountServiceTest {

    Employee employee;
    Account admin;

    @Autowired
    private AccountService accountService;
    @Autowired
    private EmployeeRepo employeeRepo;
    @Autowired
    private SecurityEventRepo securityEventRepo;
    @Autowired
    private AccountRepo accountRepo;

    @BeforeEach
    void setUp() {
        this.employee = employeeRepo.save(new Employee("Name", "Surname", "employee@example.com", "hashedpassword"));
        this.admin = accountRepo.findByUsername("admin").get();
    }

    @Test
    void testGetAccounts() {
        var accountsPage = accountService.getAccounts(Pageable.unpaged());

        Assertions.assertEquals(2, accountsPage.getTotalElements()); // One for the employee and one for the admin account
        Assertions.assertTrue(accountsPage.getContent().stream()
                .anyMatch(account -> account.username().equals("employee@example.com")));
        Assertions.assertTrue(accountsPage.getContent().stream()
                .anyMatch(account -> account.username().equals("admin")));
    }

    @Test
    void testGetSecurityEvents() {
        securityEventRepo.saveAll(List.of(
           new SecurityEvent(Instant.now(), "addr", SecurityEvent.SecurityEventType.LOGIN, SecurityEvent.SecurityEventMethod.USERNAME_PASSWORD, employee.getAccount()),
           new SecurityEvent(Instant.now(), "addr", SecurityEvent.SecurityEventType.LOGIN_FAILED_CREDENTIALS, SecurityEvent.SecurityEventMethod.REMEMBER_ME, null),
           new SecurityEvent(Instant.now(), "addr", SecurityEvent.SecurityEventType.PASSWORD_CHANGE, SecurityEvent.SecurityEventMethod.USERNAME_PASSWORD, admin)
        ));

        var securityEventsPage = accountService.getSecurityEvents(Pageable.unpaged());

        Assertions.assertEquals(3, securityEventsPage.getTotalElements());
        Assertions.assertEquals(1, securityEventsPage.getContent().stream()
                .filter(event -> event.accountId() == admin.getId()).count());
        Assertions.assertEquals(1, securityEventsPage.getContent().stream()
                .filter(event -> event.accountId() == employee.getAccount().getId()).count());
        Assertions.assertEquals(1, securityEventsPage.getContent().stream()
                .filter(event -> event.accountId() == null).count());
    }

    @Test
    void testGetSecurityEventsByAccountId() throws ResourceNotFoundException {
        securityEventRepo.saveAll(List.of(
           new SecurityEvent(Instant.now(), "addr", SecurityEvent.SecurityEventType.LOGIN, SecurityEvent.SecurityEventMethod.USERNAME_PASSWORD, employee.getAccount()),
           new SecurityEvent(Instant.now(), "addr", SecurityEvent.SecurityEventType.LOGIN_FAILED_CREDENTIALS, SecurityEvent.SecurityEventMethod.REMEMBER_ME, null),
           new SecurityEvent(Instant.now(), "addr", SecurityEvent.SecurityEventType.PASSWORD_CHANGE, SecurityEvent.SecurityEventMethod.USERNAME_PASSWORD, admin)
        ));

        var employeeEventsPage = accountService.getSecurityEventsByAccountId(employee.getAccount().getId(), Pageable.unpaged());
        var adminEventsPage = accountService.getSecurityEventsByAccountId(admin.getId(), Pageable.unpaged());

        Assertions.assertEquals(1, employeeEventsPage.getTotalElements());
        Assertions.assertEquals(employee.getAccount().getId(), employeeEventsPage.getContent().getFirst().accountId());

        Assertions.assertEquals(1, adminEventsPage.getTotalElements());
        Assertions.assertEquals(admin.getId(), adminEventsPage.getContent().getFirst().accountId());
    }

    @Test
    void testGetSecurityEventsByAccountIdNotFound() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> accountService.getSecurityEventsByAccountId(UUID.randomUUID(), Pageable.unpaged()));
    }
}
