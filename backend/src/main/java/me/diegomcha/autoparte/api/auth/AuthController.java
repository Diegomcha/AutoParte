package me.diegomcha.autoparte.api.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.auth.dto.AccountDto;
import me.diegomcha.autoparte.api.auth.dto.UpdatePasswordDto;
import me.diegomcha.autoparte.core.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class AuthController implements AuthAPI {

    private final AuthService service;

    @GetMapping("/me")
    @Override
    public AccountDto me() {
        return service.getLoggedInAccount();
    }

    @PostMapping("/update-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void updatePassword(@Valid @RequestBody UpdatePasswordDto dto, HttpServletRequest context) throws UnauthorizedException {
        service.updatePassword(dto, new WebAuthenticationDetails(context));
    }
}
