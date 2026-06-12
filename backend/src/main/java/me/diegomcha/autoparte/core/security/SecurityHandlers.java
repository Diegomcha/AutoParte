package me.diegomcha.autoparte.core.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SecurityHandlers implements AuthenticationFailureHandler, AuthenticationSuccessHandler, LogoutSuccessHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Authentication authentication) {
        this.onSuccess(response);
    }

    @Override
    public void onLogoutSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @Nullable Authentication authentication) {
        this.onSuccess(response);
    }

    @Override
    public void onAuthenticationFailure(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull AuthenticationException exception) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/problem+json");
        response.getOutputStream().println(objectMapper.writeValueAsString(ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage())));
    }

    private void onSuccess(HttpServletResponse response) {
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }
}