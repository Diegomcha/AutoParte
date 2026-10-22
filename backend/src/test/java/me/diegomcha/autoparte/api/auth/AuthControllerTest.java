package me.diegomcha.autoparte.api.auth;

import me.diegomcha.autoparte.api.BaseControllerTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends BaseControllerTest {

    @Test
    void currentAccountIsExposedForAuthenticatedUsers() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles").value(hasItem("ROLE_ADMIN")));
    }

    @Test
    void currentAccountRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginSucceedsAndInvalidCredentialsAreRejected() throws Exception {
        mockMvc.perform(formLogin("/api/auth/login")
                        .user("admin")
                        .password("admin"))
                .andExpect(status().isNoContent());

        mockMvc.perform(formLogin("/api/auth/login")
                        .user("admin")
                        .password("wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void logoutRequiresCsrfAndSucceedsWithCsrf() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void authenticatedUsersCanUpdateTheirPassword() throws Exception {
        mockMvc.perform(post("/api/auth/update-password")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "admin",
                                  "currentPassword": "admin",
                                  "newPassword": "new-password"
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void invalidPasswordUpdateParametersAreRejected() throws Exception {
        mockMvc.perform(post("/api/auth/update-password")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "",
                                  "currentPassword": "",
                                  "newPassword": "short"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
