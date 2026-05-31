package me.diegomcha.autoparte.config.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
class AuthController implements AuthAPI {

    @Override
    @GetMapping("/me")
    public UserDetails me(@AuthenticationPrincipal UserDetails account) {
        return account;
    }
}
