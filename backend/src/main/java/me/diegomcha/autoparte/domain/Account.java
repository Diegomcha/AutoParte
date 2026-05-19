package me.diegomcha.autoparte.domain;

import lombok.*;
import me.diegomcha.autoparte.domain.base.BaseEntity;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@ToString
public class Account extends BaseEntity {

    private @NonNull String username;
    private @NonNull String hashedPassword;
    private @NonNull Set<@NonNull String> roles;

    private boolean enabled = true;
    @Setter(AccessLevel.NONE)
    private Instant disabledAt;

    private boolean requiresReset = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.disabledAt = enabled ? null : Instant.now();
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
        this.requiresReset = false;
    }
}