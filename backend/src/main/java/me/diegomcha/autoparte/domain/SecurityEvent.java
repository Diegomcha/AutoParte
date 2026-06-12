package me.diegomcha.autoparte.domain;

import lombok.*;
import me.diegomcha.autoparte.domain.base.BaseEntity;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
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
