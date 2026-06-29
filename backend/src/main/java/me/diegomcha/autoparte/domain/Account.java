package me.diegomcha.autoparte.domain;

import lombok.*;
import me.diegomcha.autoparte.domain.base.BaseEntity;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@ToString
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Account extends BaseEntity {

    @Setter
    private @NonNull String username;
    @ToString.Exclude
    private @NonNull String hashedPassword;
    @Setter
    private @NonNull Set<@NonNull String> roles;

    @ToString.Exclude
    private final @NonNull Set<@NonNull SecurityEvent> securityLog = new HashSet<>();

    private boolean enabled = true;
    private Instant disabledAt;

    private Instant deletedAt;

    @Setter
    private boolean requiresReset = true;

    /**
     * Sets the enabled status of the account.
     * If the account is disabled, the disabledAt timestamp is set to the current time.
     * If the account is enabled, the disabledAt timestamp is cleared (set to null).
     * @param enabled true to enable the account, false to disable it.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.disabledAt = enabled ? null : Instant.now();
    }

    /**
     * Resets the account's password to the provided hashed password and
     * marks the account as requiring a password reset.
     * @param hashedPassword The new hashed password to set for the account. Must not be null.
     * @throws IllegalArgumentException if the hashedPassword is null.
     */
    public void resetPassword(@NonNull String hashedPassword) {
        this.setHashedPassword(hashedPassword);
        this.requiresReset = true;
    }

    /**
     * Sets the hashed password for the account and
     * marks the account as not requiring a password reset.
     * @param hashedPassword The new hashed password to set for the account. Must not be null.
     * @throws IllegalArgumentException if the hashedPassword is null.
     */
    public void setHashedPassword(@NonNull String hashedPassword) {
        this.hashedPassword = hashedPassword;
        this.requiresReset = false;
    }

    Set<SecurityEvent> _getSecurityLog() {
        return this.securityLog;
    }
    
    public @NonNull Set<@NonNull SecurityEvent> getSecurityLog() {
        return Set.copyOf(securityLog);
    }

}