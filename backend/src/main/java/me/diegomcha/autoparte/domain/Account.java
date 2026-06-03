package me.diegomcha.autoparte.domain;

import lombok.*;
import me.diegomcha.autoparte.domain.base.BaseEntity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@ToString
public class Account extends BaseEntity {

    private @NonNull String username;
    @ToString.Exclude
    private @NonNull String hashedPassword;
    private @NonNull Set<@NonNull String> roles;

    @ToString.Exclude
    @Setter(AccessLevel.NONE)
    private @NonNull Set<@NonNull SecurityEvent> securityLog = new HashSet<>();

    private boolean enabled = true;
    @Setter(AccessLevel.NONE)
    private Instant disabledAt;

    private boolean requiresReset = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.disabledAt = enabled ? null : Instant.now();
    }

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