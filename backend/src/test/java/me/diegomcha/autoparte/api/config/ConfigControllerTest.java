package me.diegomcha.autoparte.api.config;

import me.diegomcha.autoparte.api.BaseControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConfigControllerTest extends BaseControllerTest {

    @Test
    void publicConfigIsAvailableWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/config/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.digitalSignatureEnabled").value(false));
    }

    @Test
    void fullConfigIsRestrictedToAdmins() throws Exception {
        mockMvc.perform(get("/api/config")
                        .with(user("employee").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/config")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sesUsername").value("test"))
                .andExpect(jsonPath("$.sesLandlordCode").value("00000"))
                .andExpect(jsonPath("$.sesCredentialsValid").value(true))
                .andExpect(jsonPath("$.logsRetentionDays").value(365))
                .andExpect(jsonPath("$.bookingsRetentionDays").value(1095));
    }

    @Test
    void adminsCanUpdateConfigAndSesValidationIsReset() throws Exception {
        mockMvc.perform(put("/api/config")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sesUsername": "updated-user",
                                  "sesPassword": "updated-password",
                                  "sesLandlordCode": "updated-code",
                                  "digitalSignatureEnabled": true,
                                  "manualReviewEnabled": true,
                                  "logsRetentionDays": 30,
                                  "bookingsRetentionDays": 60
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/config")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sesUsername").value("updated-user"))
                .andExpect(jsonPath("$.sesLandlordCode").value("updated-code"))
                .andExpect(jsonPath("$.sesCredentialsValid").value(false))
                .andExpect(jsonPath("$.digitalSignatureEnabled").value(true))
                .andExpect(jsonPath("$.manualReviewEnabled").value(true))
                .andExpect(jsonPath("$.logsRetentionDays").value(30))
                .andExpect(jsonPath("$.bookingsRetentionDays").value(60));
    }

    @Test
    void configUpdatesRequireAdminAndCsrf() throws Exception {
        var body = """
                {
                  "sesUsername": "user",
                  "sesPassword": "password",
                  "sesLandlordCode": "code",
                  "digitalSignatureEnabled": false,
                  "manualReviewEnabled": false,
                  "logsRetentionDays": 1,
                  "bookingsRetentionDays": 1
                }
                """;

        mockMvc.perform(put("/api/config")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/config")
                        .with(user("employee").roles("EMPLOYEE"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/config/validate-ses")
                        .with(user("employee").roles("EMPLOYEE"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidConfigUpdatesAreRejected() throws Exception {
        mockMvc.perform(put("/api/config")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sesUsername": " ",
                                  "sesPassword": "password",
                                  "sesLandlordCode": "code",
                                  "digitalSignatureEnabled": false,
                                  "manualReviewEnabled": false,
                                  "logsRetentionDays": -1,
                                  "bookingsRetentionDays": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sesValidationWithoutCredentialsIsReported() throws Exception {
        mockMvc.perform(put("/api/config")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sesUsername": null,
                                  "sesPassword": null,
                                  "sesLandlordCode": null,
                                  "digitalSignatureEnabled": false,
                                  "manualReviewEnabled": false,
                                  "logsRetentionDays": 365,
                                  "bookingsRetentionDays": 1095
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/config/validate-ses")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
