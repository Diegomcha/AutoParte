package me.diegomcha.autoparte.core.event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.core.security.SecurityService;
import me.diegomcha.autoparte.domain.Account;
import me.diegomcha.autoparte.domain.SecurityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.*;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class AuthEventsListener {

    private final Logger logger = LoggerFactory.getLogger(AuthEventsListener.class);

    private final SecurityService service;
    private final SecurityEventRepo repo;

    @EventListener
    @Transactional
    public void onAuthEvent(AbstractAuthenticationEvent event) {
        SecurityEvent.SecurityEventType type = switch (event) {
            case InteractiveAuthenticationSuccessEvent ignored ->
                    SecurityEvent.SecurityEventType.LOGIN;
            case LogoutSuccessEvent ignored ->
                    SecurityEvent.SecurityEventType.LOGOUT;
            case UpdatePasswordSuccessEvent ignored ->
                    SecurityEvent.SecurityEventType.PASSWORD_CHANGE;
            case AuthenticationFailureBadCredentialsEvent ignored ->
                    SecurityEvent.SecurityEventType.LOGIN_FAILED_CREDENTIALS;
            case AuthenticationFailureDisabledEvent ignored ->
                    SecurityEvent.SecurityEventType.LOGIN_FAILED_ACCOUNT_DISABLED;
            case AuthenticationFailureCredentialsExpiredEvent ignored ->
                    SecurityEvent.SecurityEventType.LOGIN_FAILED_CREDENTIALS_EXPIRED;
            case AuthenticationFailureLockedEvent ignored ->
                    SecurityEvent.SecurityEventType.LOGIN_FAILED_ACCOUNT_LOCKED;

            default -> null;
        };

        // If the event is not one of the expected types, we ignore it
        if (type == null)
            return;

        SecurityEvent.SecurityEventMethod method = switch (event.getAuthentication()) {
            case UsernamePasswordAuthenticationToken ignored ->
                    SecurityEvent.SecurityEventMethod.USERNAME_PASSWORD;
            case RememberMeAuthenticationToken ignored ->
                    SecurityEvent.SecurityEventMethod.REMEMBER_ME;
            default ->
                    throw new IllegalStateException("Unexpected value: " + event.getAuthentication());
        };

        String remoteAddress = Optional
                .ofNullable(event.getAuthentication().getDetails())
                .filter(WebAuthenticationDetails.class::isInstance)
                .map(WebAuthenticationDetails.class::cast)
                .map(WebAuthenticationDetails::getRemoteAddress)
                .orElseThrow(() -> new IllegalStateException("Unexpected authentication details type: " + event.getAuthentication().getDetails()));

        Account account = service.getAccountFromAuthentication(event.getAuthentication());

        repo.save(new SecurityEvent(Instant.ofEpochMilli(event.getTimestamp()), remoteAddress, type, method, account));

        // Logging
        switch (event) {
            case InteractiveAuthenticationSuccessEvent ev ->
                    logger.info("User {} logged in successfully", ev.getAuthentication().getName());
            case LogoutSuccessEvent ev ->
                    logger.info("User {} logged out successfully", ev.getAuthentication().getName());
            case UpdatePasswordSuccessEvent ev ->
                    logger.info("User {} changed password successfully", ev.getAuthentication().getName());
            case AbstractAuthenticationFailureEvent ev ->
                    logger.warn("Authentication failure for user {}: {}", ev.getAuthentication().getName(), ev.getException().getMessage());
            default ->
                    throw new IllegalArgumentException("Received unexpected authentication event type: " + event.getClass().getSimpleName());
        }
    }

}
