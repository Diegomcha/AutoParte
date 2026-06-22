package me.diegomcha.autoparte.config;

import me.diegomcha.autoparte.core.repos.AccountRepo;
import me.diegomcha.autoparte.core.security.SecurityHandlers;
import me.diegomcha.autoparte.core.security.SecurityService;
import me.diegomcha.autoparte.core.security.UserAccount;
import me.diegomcha.autoparte.domain.Account;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.HttpStatusAccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityHandlers securityHandlers, DynamicConfigService dynamicConfigService) {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/api/auth/**", "/api/docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(CsrfConfigurer::spa)
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler(securityHandlers)
                        .failureHandler(securityHandlers)
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(securityHandlers)
                )
                .rememberMe(rMe -> rMe
                        .rememberMeParameter("rememberMe")
                        .key(dynamicConfigService.getConfig().getRememberMeKey())
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler(new HttpStatusAccessDeniedHandler(HttpStatus.FORBIDDEN))
                )
                .build();
    }

    @Bean
    CommandLineRunner defaultAdminAccountCreator(AutoparteProperties properties, AccountRepo accountRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!accountRepo.existsByUsername("admin")) {
                Account admin = new Account(
                        "admin",
                        Objects.requireNonNull(passwordEncoder.encode(properties.getSecurity().getInitialAdminPassword())),
                        Set.of("ROLE_ADMIN")
                );
                admin.setRequiresReset(false);
                accountRepo.save(admin);
            }
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(AccountRepo accountRepo) {
        return username -> accountRepo.findByUsername(username)
                .map(UserAccount::new)
                .orElseThrow(() -> UsernameNotFoundException.fromUsername(username));
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }

    @Bean
    AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new DefaultAuthenticationEventPublisher(applicationEventPublisher);
    }

    @Bean
    AuditorAware<Account> auditorProvider(SecurityService service) {
        return () -> Optional.ofNullable(service.getAccountFromAuthentication(SecurityContextHolder.getContext().getAuthentication(), false));
    }
}
