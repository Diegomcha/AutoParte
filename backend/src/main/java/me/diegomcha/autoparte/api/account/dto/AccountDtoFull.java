package me.diegomcha.autoparte.api.account.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AccountDtoFull(
        @NotNull UUID id,
        @NotNull Instant createdAt,
        @NotNull Instant updatedAt,

        @NotNull boolean enabled,
        Instant disabledAt,
        @NotNull boolean requiresReset,

        @NotNull String username,
        @NotNull Set<String> roles,

        UUID employeeId
) {
}
