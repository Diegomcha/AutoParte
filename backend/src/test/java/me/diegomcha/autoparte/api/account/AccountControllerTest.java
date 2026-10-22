package me.diegomcha.autoparte.api.account;

import me.diegomcha.autoparte.api.BaseControllerTest;
import me.diegomcha.autoparte.core.repos.EmployeeRepo;
import me.diegomcha.autoparte.core.repos.SecurityEventRepo;
import me.diegomcha.autoparte.domain.Employee;
import me.diegomcha.autoparte.domain.SecurityEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountControllerTest extends BaseControllerTest {

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private SecurityEventRepo securityEventRepo;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = employeeRepo.save(new Employee("Test", "Employee", "test.employee@example.com", "password"));
    }

    @Test
    void accountsAreExposedByTheApi() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[?(@.username == 'admin')]").isNotEmpty())
                .andExpect(jsonPath("$.content[?(@.username == 'test.employee@example.com')]").isNotEmpty());
    }

    @Test
    void securityEventsAreExposedGloballyAndByAccount() throws Exception {
        var employeeAccount = employee.getAccount();
        var employeeEvent = securityEventRepo.save(new SecurityEvent(
                Instant.now(),
                "127.0.0.1",
                SecurityEvent.SecurityEventType.LOGIN,
                SecurityEvent.SecurityEventMethod.USERNAME_PASSWORD,
                employeeAccount
        ));
        var globalEvent = securityEventRepo.save(new SecurityEvent(
                Instant.now(),
                "127.0.0.1",
                SecurityEvent.SecurityEventType.LOGIN_FAILED_CREDENTIALS,
                SecurityEvent.SecurityEventMethod.REMEMBER_ME,
                null
        ));

        mockMvc.perform(get("/api/accounts/global/security-events")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[?(@.id == '" + employeeEvent.getId() + "')].accountId")
                        .value(employeeAccount.getId().toString()))
                .andExpect(jsonPath("$.content[?(@.id == '" + globalEvent.getId() + "')].accountId")
                        .value(hasItem(nullValue())));

        mockMvc.perform(get("/api/accounts/{accountId}/security-events", employeeAccount.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(employeeEvent.getId().toString()))
                .andExpect(jsonPath("$.content[0].accountId").value(employeeAccount.getId().toString()))
                .andExpect(jsonPath("$.content[0].type").value("LOGIN"))
                .andExpect(jsonPath("$.content[0].method").value("USERNAME_PASSWORD"))
                .andExpect(jsonPath("$.content[0].remoteAddress").value("127.0.0.1"));
    }

    @Test
    void securityEventsForMissingAccountAreNotFound() throws Exception {
        mockMvc.perform(get("/api/accounts/{accountId}/security-events", UUID.randomUUID())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void employeesCannotAccessAdminAccountRoutes() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .with(user("test.employee@example.com").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidAccountParametersAreRejected() throws Exception {
        mockMvc.perform(get("/api/accounts/not-a-uuid/security-events")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
    }
}
