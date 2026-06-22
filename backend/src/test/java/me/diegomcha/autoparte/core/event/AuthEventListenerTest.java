package me.diegomcha.autoparte.core.event;

import me.diegomcha.autoparte.core.repos.SecurityEventRepo;
import me.diegomcha.autoparte.domain.SecurityEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.StreamSupport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase
class AuthEventListenerTest {

    @Autowired
    private SecurityEventRepo repo;
    @Autowired
    private AuthEventsListener authEventsListener;

    private static final UsernamePasswordAuthenticationToken USERPASSWORD_AUTHENTICATION = UsernamePasswordAuthenticationToken.authenticated("test", "test", Set.of());
    private static final RememberMeAuthenticationToken REMEMBERME_AUTHENTICATION = new RememberMeAuthenticationToken("test", "test", Set.of());
    static {
        USERPASSWORD_AUTHENTICATION.setDetails(new WebAuthenticationDetails("test", null));
        REMEMBERME_AUTHENTICATION.setDetails(new WebAuthenticationDetails("test", null));
    }
    private static final AuthenticationException AUTHENTICATION_EXCEPTION = new AuthenticationException("Test authentication") {};

    private static final Object[][] EVENTS = new Object[][]{
            new Object[]{
                    false,
                    new AuthenticationSuccessEvent(USERPASSWORD_AUTHENTICATION),
                    null,
                    null
            },
            new Object[]{
                    true,
                    new InteractiveAuthenticationSuccessEvent(USERPASSWORD_AUTHENTICATION, AuthEventListenerTest.class),
                    SecurityEvent.SecurityEventType.LOGIN,
                    SecurityEvent.SecurityEventMethod.USERNAME_PASSWORD
            },
            new Object[]{
                    true,
                    new LogoutSuccessEvent(REMEMBERME_AUTHENTICATION),
                    SecurityEvent.SecurityEventType.LOGOUT,
                    SecurityEvent.SecurityEventMethod.REMEMBER_ME
            },
            new Object[] {
                    true,
                    new AuthenticationFailureBadCredentialsEvent(USERPASSWORD_AUTHENTICATION, AUTHENTICATION_EXCEPTION),
                    SecurityEvent.SecurityEventType.LOGIN_FAILED_CREDENTIALS,
                    SecurityEvent.SecurityEventMethod.USERNAME_PASSWORD
            },
            new Object[] {
                    true,
                    new AuthenticationFailureCredentialsExpiredEvent(REMEMBERME_AUTHENTICATION, AUTHENTICATION_EXCEPTION),
                    SecurityEvent.SecurityEventType.LOGIN_FAILED_CREDENTIALS_EXPIRED,
                    SecurityEvent.SecurityEventMethod.REMEMBER_ME
            },
            new Object[] {
                    true,
                    new AuthenticationFailureLockedEvent(USERPASSWORD_AUTHENTICATION, AUTHENTICATION_EXCEPTION),
                    SecurityEvent.SecurityEventType.LOGIN_FAILED_ACCOUNT_LOCKED,
                    SecurityEvent.SecurityEventMethod.USERNAME_PASSWORD
            },
            new Object[] {
                    true,
                    new AuthenticationFailureDisabledEvent(REMEMBERME_AUTHENTICATION, AUTHENTICATION_EXCEPTION),
                    SecurityEvent.SecurityEventType.LOGIN_FAILED_ACCOUNT_DISABLED,
                    SecurityEvent.SecurityEventMethod.REMEMBER_ME
            },

    };

    @ParameterizedTest
    @FieldSource("EVENTS")
    void testOnAuthEvent(
            boolean shouldSaveEvent,
            AbstractAuthenticationEvent event,
            SecurityEvent.SecurityEventType expectedType,
            SecurityEvent.SecurityEventMethod expectedMethod) {
        authEventsListener.onAuthEvent(event);

        var events = StreamSupport
                .stream(repo.findAll().spliterator(), false)
                .toList();

        if (!shouldSaveEvent) {
            Assertions.assertTrue(events.isEmpty());
            return;
        }
        Assertions.assertEquals(1, events.size());

        var storedEvent = events.getFirst();

        Assertions.assertEquals(expectedType, storedEvent.getType());
        Assertions.assertEquals(expectedMethod, storedEvent.getMethod());
    }
}
