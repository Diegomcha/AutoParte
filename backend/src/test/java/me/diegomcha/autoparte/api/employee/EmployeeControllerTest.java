package me.diegomcha.autoparte.api.employee;

import me.diegomcha.autoparte.api.BaseControllerTest;
import me.diegomcha.autoparte.core.repos.EmployeeRepo;
import me.diegomcha.autoparte.domain.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmployeeControllerTest extends BaseControllerTest {

    @Autowired
    private EmployeeRepo employeeRepo;
    @Autowired
    private ObjectMapper objectMapper;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = employeeRepo.save(new Employee("Test", "Employee", "test.employee@example.com", "password"));
    }

    @Test
    void employeeLifecycleAndPasswordResetAreExposedByTheApi() throws Exception {
        var created = mockMvc.perform(post("/api/employees")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Created", "surname": "Employee", "email": "created.employee@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("created.employee@example.com"))
                .andExpect(jsonPath("$.password").isString())
                .andReturn();

        var createdId = UUID.fromString(
                objectMapper.readTree(created.getResponse().getContentAsString())
                        .get("id").asString()
        );

        mockMvc.perform(get("/api/employees/{id}", createdId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId.toString()))
                .andExpect(jsonPath("$.name").value("Created"))
                .andExpect(jsonPath("$.surname").value("Employee"))
                .andExpect(jsonPath("$.email").value("created.employee@example.com"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.accommodations").isArray());

        mockMvc.perform(patch("/api/employees/{id}", createdId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"enabled": false, "name": "Updated", "surname": "User", "email": "updated.employee@example.com"}
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/employees/{id}", createdId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.surname").value("User"))
                .andExpect(jsonPath("$.email").value("updated.employee@example.com"));

        mockMvc.perform(post("/api/employees/{id}/reset-password", createdId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId.toString()))
                .andExpect(jsonPath("$.email").value("updated.employee@example.com"))
                .andExpect(jsonPath("$.password").isString());

        mockMvc.perform(get("/api/employees")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        mockMvc.perform(delete("/api/employees/{id}", createdId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isAccepted());

        mockMvc.perform(get("/api/employees/{id}", createdId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void employeesCannotAccessAdminEmployeeRoutes() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .with(user("employee").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/employees/{id}", employee.getId())
                        .with(user("employee").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeeStateChangingRoutesRequireCsrf() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Created", "surname": "Employee", "email": "created.employee@example.com"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/employees/{id}", employee.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/employees/{id}", employee.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingAndConflictingEmployeesAreReported() throws Exception {
        mockMvc.perform(get("/api/employees/{id}", UUID.randomUUID())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/employees")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Another", "surname": "Employee", "email": "test.employee@example.com"}
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/api/employees/{id}", UUID.randomUUID())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidEmployeeParametersAreRejected() throws Exception {
        mockMvc.perform(get("/api/employees/not-a-uuid")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/employees")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "surname": "Employee", "email": "not-an-email"}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/employees/{id}", employee.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": " ", "surname": null, "email": "invalid-email"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
