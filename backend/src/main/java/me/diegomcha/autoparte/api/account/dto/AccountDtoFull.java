package me.diegomcha.autoparte.api.account.dto;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AccountDtoFull(
        @Nonnull UUID id,
        @Nonnull Instant createdAt,
        @Nonnull Instant updatedAt,

        @NotNull boolean enabled,
        Instant disabledAt,
        @NotNull boolean requiresReset,

        @Nonnull String username,
        @Nonnull Set<String> roles,

        UUID employeeId
) {
}
