package me.diegomcha.autoparte.api.accommodation;

import me.diegomcha.autoparte.api.BaseControllerTest;
import me.diegomcha.autoparte.api.accommodation.dto.AccommodationDtoRequest;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.EmployeeRepo;
import me.diegomcha.autoparte.domain.Accommodation;
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

class AccommodationControllerTest extends BaseControllerTest {

    @Autowired
    private AccommodationRepo accommodationRepo;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private Accommodation accommodation;
    private Employee employee;

    @BeforeEach
    void setUp() {
        accommodation = accommodationRepo.save(new Accommodation("Test accommodation", "TEST-001", true));
        employee = employeeRepo.save(new Employee("Test", "Employee", "test.employee@example.com", "password"));
    }

    @Test
    void accommodationLifecycleIsExposedByTheApi() throws Exception {
        var requestDto = new AccommodationDtoRequest("Created accommodation", "TEST-002", false);

        var result = mockMvc.perform(post("/api/accommodations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();

        var created = objectMapper.readValue(result.getResponse().getContentAsString(), EntityDtoCreated.class);
        var accommodationId = created.id();

        mockMvc.perform(get("/api/accommodations/{id}", accommodationId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accommodationId.toString()))
                .andExpect(jsonPath("$.name").value("Created accommodation"))
                .andExpect(jsonPath("$.sesCode").value("TEST-002"))
                .andExpect(jsonPath("$.internetConnection").value(false))
                .andExpect(jsonPath("$.employees").isArray())
                .andExpect(jsonPath("$.employees.length()").value(0));

        var updateDto = new AccommodationDtoRequest("Updated accommodation", "TEST-003", true);
        mockMvc.perform(put("/api/accommodations/{id}", accommodationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/accommodations/{id}", accommodationId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated accommodation"))
                .andExpect(jsonPath("$.sesCode").value("TEST-003"))
                .andExpect(jsonPath("$.internetConnection").value(true));

        mockMvc.perform(get("/api/accommodations")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));

        mockMvc.perform(delete("/api/accommodations/{id}", accommodationId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isAccepted());

        mockMvc.perform(get("/api/accommodations/{id}", accommodationId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void employeesCanBeAssignedAndUnassigned() throws Exception {
        mockMvc.perform(get("/api/accommodations")
                        .with(user("test.employee@example.com").roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));

        mockMvc.perform(post("/api/accommodations/{accommodationId}/employees/{employeeId}", accommodation.getId(), employee.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/accommodations")
                        .with(user("test.employee@example.com").roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/api/accommodations/{id}", accommodation.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employees.length()").value(1))
                .andExpect(jsonPath("$.employees[0].id").value(employee.getId().toString()))
                .andExpect(jsonPath("$.employees[0].email").value(employee.getEmail()));

        mockMvc.perform(delete("/api/accommodations/{accommodationId}/employees/{employeeId}", accommodation.getId(), employee.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/accommodations")
                        .with(user("test.employee@example.com").roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));

        mockMvc.perform(get("/api/accommodations/{id}", accommodation.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employees.length()").value(0));
    }

    @Test
    void missingAccommodationIsReported() throws Exception {
        mockMvc.perform(get("/api/accommodations/{id}", UUID.randomUUID())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void employeesCannotAccessAdminAccommodationRoutes() throws Exception {
        mockMvc.perform(get("/api/accommodations/{id}", accommodation.getId())
                        .with(user("test.employee@example.com").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidAccommodationParametersAreRejected() throws Exception {
        mockMvc.perform(get("/api/accommodations/not-a-uuid")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/accommodations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "sesCode": "TEST-002", "internetConnection": true}
                                """)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/accommodations/{id}", accommodation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Updated accommodation", "sesCode": "", "internetConnection": true}
                                """)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}