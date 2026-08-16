package me.diegomcha.autoparte.core.security;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.core.event.UpdatePasswordSuccessEvent;
import me.diegomcha.autoparte.core.exception.UnauthorizedException;
import me.diegomcha.autoparte.core.repos.AccountRepo;
import me.diegomcha.autoparte.core.repos.EmployeeRepo;
import me.diegomcha.autoparte.domain.Account;
import me.diegomcha.autoparte.domain.Employee;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class SecurityService {

    private final AccountRepo accountRepo;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final EmployeeRepo employeeRepo;

    /**
     * Updates the password of the user with the given username, if the current password matches.
     *
     * @param username    The username of the account for which to update the password
     * @param oldPassword The current password of the account, used for authentication before updating the password
     * @param newPassword The new password to set for the account if authentication is successful
     * @param details     The details of the current authentication, used for logging the password change event
     * @throws UnauthorizedException if the username is not found or the current password does not match
     */
    public void updatePassword(String username, String oldPassword, String newPassword, Object details) throws UnauthorizedException {
        Authentication authentication = this.checkCredentials(username, oldPassword, details);
        Account account = this.getAccountFromAuthentication(authentication);

        assert account != null;

        account.setHashedPassword(Objects.requireNonNull(passwordEncoder.encode(newPassword)));
        eventPublisher.publishEvent(new UpdatePasswordSuccessEvent(authentication));
    }

    private Authentication checkCredentials(String username, String password, Object details) throws UnauthorizedException {
        // Create an unauthenticated token with the credentials to be checked & details
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        authentication.setDetails(details);

        // Attempt authentication
        try {
            return authenticationManager.authenticate(authentication);
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Unauthorized");
        }
    }

    /**
     * Extracts the Account from the given Authentication object if possible, fetching it from the database to ensure it is up-to-date.
     *
     * @param authentication The Authentication object from which to extract the Account. Can be null.
     * @return The Account associated with the given Authentication, or null.
     */
    public @Nullable Account getAccountFromAuthentication(@Nullable Authentication authentication) {
        return this.getAccountFromAuthentication(authentication, true);
    }

    /**
     * Extracts the Employee associated with the given Account if possible.
     *
     * @param account The Account object from which to extract the Employee. Must not be null.
     * @return The Employee associated with the given Account, or null if no such Employee exists.
     */
    public @Nullable Employee getEmployeeFromAccount(@NonNull Account account) {
        return employeeRepo.findByAccountId(account.getId()).orElse(null);
    }

    /**
     * Extracts the Account from the given Authentication object if possible.
     *
     * @param authentication The Authentication object from which to extract the Account. Can be null.
     * @param updated        If true, the method will attempt to fetch the Account from the database to ensure it is up-to-date.
     *                       If false, it will return the Account directly from the Authentication principal if it is of type UserAccount.
     * @return The Account associated with the given Authentication, or null.
     */
    public @Nullable Account getAccountFromAuthentication(@Nullable Authentication authentication, boolean updated) {
        return Optional
                .ofNullable(authentication)
                .map(Authentication::getPrincipal)
                .filter(principal -> !"anonymousUser".equals(principal))
                .filter(principal -> updated || principal instanceof UserAccount)
                .map(principal -> switch (principal) {
                    case UserAccount userAccount -> updated
                            ? accountRepo.findByUsername(userAccount.getUsername()).orElse(null)
                            : userAccount.getAccount();
                    case User user ->
                            accountRepo.findByUsername(user.getUsername()).orElse(null);
                    case String username ->
                            accountRepo.findByUsername(username).orElse(null);
                    default -> null;
                })
                .orElse(null);
    }
}
