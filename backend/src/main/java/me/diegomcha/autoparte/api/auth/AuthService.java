package me.diegomcha.autoparte.api.auth;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.auth.dto.AccountDto;
import me.diegomcha.autoparte.api.auth.dto.UpdatePasswordDto;
import me.diegomcha.autoparte.core.exception.UnauthorizedException;
import me.diegomcha.autoparte.core.security.SecurityService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class AuthService {

    private final AccountMapper accountMapper;
    private final SecurityService securityService;

    /**
     * Gets the current logged-in account's information.
     *
     * @return The account information of the currently authenticated user, or null if no user is authenticated
     */
    @Transactional(readOnly = true)
    public AccountDto getLoggedInAccount() {
        return Optional
                .ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(securityService::getAccountFromAuthentication)
                .map(accountMapper::toDto)
                .orElse(null);
    }

    /**
     * Updates the password of the user with the given username, if the current password matches.
     *
     * @param dto                   The information needed to update the password (username, current password, new password)
     * @param authenticationDetails The details of the current authentication, used for logging the password change event
     * @throws UnauthorizedException If the username is not found or the current password does not match
     */
    @Transactional
    public void updatePassword(@NonNull UpdatePasswordDto dto, Object authenticationDetails) throws UnauthorizedException {
        securityService.updatePassword(dto.username(), dto.currentPassword(), dto.newPassword(), authenticationDetails);
    }
}
