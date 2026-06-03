package me.diegomcha.autoparte.core.event;

import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.core.Authentication;

/**
 * Event published when a user successfully updates their password.
 */
public class UpdatePasswordSuccessEvent extends AbstractAuthenticationEvent {
    public UpdatePasswordSuccessEvent(Authentication authentication) {
        super(authentication);
    }
}
