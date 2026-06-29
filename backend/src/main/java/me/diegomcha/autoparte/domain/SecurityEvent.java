package me.diegomcha.autoparte.domain;

import lombok.*;
import me.diegomcha.autoparte.domain.base.BaseEntity;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SecurityEvent extends BaseEntity {

    public enum SecurityEventType {
        LOGIN,
        LOGOUT,
        PASSWORD_CHANGE,

        LOGIN_FAILED_CREDENTIALS,
        LOGIN_FAILED_CREDENTIALS_EXPIRED,
        LOGIN_FAILED_ACCOUNT_DISABLED,
        LOGIN_FAILED_ACCOUNT_LOCKED,
    }

    public enum SecurityEventMethod {
        USERNAME_PASSWORD,
        REMEMBER_ME
    }

    private @NonNull Instant timestamp;

    @ToString.Exclude
    private @NonNull String remoteAddress;
    private @NonNull SecurityEventType type;
    private @NonNull SecurityEventMethod method;

    private Account account;

    /**
     * Constructor for creating a SecurityEvent instance.
     *
     * @param timestamp     The timestamp of the security event. Must not be in the future.
     * @param remoteAddress The remote address (IP) from which the event originated.
     * @param type          The type of security event (e.g., LOGIN, LOGOUT, PASSWORD_CHANGE).
     * @param method        The method used for the security event (e.g., USERNAME_PASSWORD, REMEMBER_ME).
     * @param account       The account associated with the security event. Can be null if the event is not associated with any account.
     * @throws IllegalArgumentException if the timestamp is in the future.
     */
    public SecurityEvent(@NonNull Instant timestamp, @NonNull String remoteAddress, @NonNull SecurityEventType type, @NonNull SecurityEventMethod method, Account account) {
        this.setTimestamp(timestamp);
        this.remoteAddress = remoteAddress;
        this.type = type;
        this.method = method;
        this.setAccount(account);
    }

    private void setTimestamp(@NonNull Instant timestamp) {
        if (timestamp.isAfter(Instant.now()))
            throw new IllegalArgumentException("Timestamp cannot be in the future");
        this.timestamp = timestamp;
    }

    private void setAccount(@Nullable Account account) {
        this.account = account;
        if (account != null) this.account._getSecurityLog().add(this);
    }

}
