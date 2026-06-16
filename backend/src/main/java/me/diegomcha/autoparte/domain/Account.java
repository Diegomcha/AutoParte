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

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.disabledAt = enabled ? null : Instant.now();
    }

    public void resetPassword(@NonNull String hashedPassword) {
        this.setHashedPassword(hashedPassword);
        this.requiresReset = true;
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