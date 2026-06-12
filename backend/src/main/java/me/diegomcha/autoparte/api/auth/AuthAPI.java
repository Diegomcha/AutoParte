package me.diegomcha.autoparte.api.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import me.diegomcha.autoparte.api.auth.dto.AccountDto;
import me.diegomcha.autoparte.api.auth.dto.UpdatePasswordDto;
import me.diegomcha.autoparte.core.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "Authentication", description = "Operations related to authentication")
public interface AuthAPI {

    @Operation(summary = "Get current authenticated user")
    AccountDto me();

    @Operation(summary = "Update current authenticated user's password")
    void updatePassword(UpdatePasswordDto dto, HttpServletRequest context) throws UnauthorizedException;

    // -- Documentation for Spring Security's authentication endpoints --

    record LoginRequest(String username, String password, Boolean rememberMe) {
    }

    @Operation(summary = "Login")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/login", consumes = "application/x-www-form-urlencoded")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    default void login(@RequestBody LoginRequest request) {
        throw new UnsupportedOperationException("This endpoint should never be called. It is only here to be intercepted by Spring Security's authentication filter.");
    }

    @Operation(summary = "Logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout")
    default void logout() {
        throw new UnsupportedOperationException("This endpoint should never be called. It is only here to be intercepted by Spring Security's authentication filter.");
    }
}
