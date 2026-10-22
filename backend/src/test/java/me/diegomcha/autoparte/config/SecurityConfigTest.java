package me.diegomcha.autoparte.config;

import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccommodationRepo accommodationRepo;
    @MockitoBean
    private BookingRepo bookingRepo;

    @Nested
    class PublicEndpoints {

        @Test
        void nonApiPagePermitted() throws Exception {
            mockMvc.perform(get("/some-web-page"))
                    .andExpect(status().isOk());
        }

        @Test
        void publicApiPermitted() throws Exception {
            mockMvc.perform(get("/api/catalogue/countries"))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/config/public"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class AuthenticationTests {

        @Test
        void unauthenticatedReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/accommodations"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void formLoginSuccess() throws Exception {
            mockMvc.perform(formLogin("/api/auth/login")
                            .user("admin")
                            .password("admin"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void formLoginFailure() throws Exception {
            mockMvc.perform(formLogin("/api/auth/login")
                            .user("admin")
                            .password("wrong-password"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.detail").exists());
        }
    }

    @Nested
    class AuthorizationAndHierarchy {

        @Test
        void adminImpliesEmployeeRole() throws Exception {
            UUID accommodationId = UUID.randomUUID();

            when(accommodationRepo.existsByIdAndEmployeesAccountUsername(accommodationId, "adminUser"))
                    .thenReturn(true);

            mockMvc.perform(get("/api/accommodations/" + accommodationId + "/bookings/" + UUID.randomUUID())
                            .with(user("adminUser").roles("ADMIN")))
                    .andExpect(status().isNotFound());
        }

        @Test
        void employeeCannotAccessAdminOnlyRoutes() throws Exception {
            mockMvc.perform(get("/api/employees")
                            .with(user("employeeUser").roles("EMPLOYEE")))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class CustomAccessEvalsTests {

        @Test
        void publicBookingAuthorizationManagerAllowed() throws Exception {
            UUID accommodationId = UUID.randomUUID();
            UUID bookingId = UUID.randomUUID();

            when(bookingRepo.existsByIdAndSelfCheckInRequestedTrue(bookingId)).thenReturn(true);

            mockMvc.perform(get("/api/accommodations/" + accommodationId + "/bookings/" + bookingId))
                    .andExpect(status().isNotFound());
        }

        @Test
        void publicBookingAuthorizationManagerDenied() throws Exception {
            UUID accommodationId = UUID.randomUUID();
            UUID bookingId = UUID.randomUUID();

            when(bookingRepo.existsByIdAndSelfCheckInRequestedTrue(bookingId)).thenReturn(false);

            mockMvc.perform(get("/api/accommodations/" + accommodationId + "/bookings/" + bookingId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class CsrfTests {

        @Test
        void stateChangingRequiresCsrf() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void stateChangingSucceedsWithCsrf() throws Exception {
            mockMvc.perform(post("/api/auth/logout").with(csrf()))
                    .andExpect(status().isNoContent());
        }
    }
}