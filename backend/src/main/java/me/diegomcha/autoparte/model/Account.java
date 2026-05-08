package me.diegomcha.autoparte.model;

import lombok.*;
import me.diegomcha.autoparte.model.base.BaseEntity;

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

    // Account status

    private boolean enabled = true;
    @Setter(AccessLevel.NONE)
    private Instant disabledAt;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.disabledAt = enabled ? null : Instant.now();
    }

    // Password reset

    private boolean requiresReset = true;

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
        this.setRequiresReset(false);
    }
}