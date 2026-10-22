package me.diegomcha.autoparte.api.common;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record EntityDtoCreated(
        @NotNull UUID id,
        @NotNull Instant createdAt
) {
}
