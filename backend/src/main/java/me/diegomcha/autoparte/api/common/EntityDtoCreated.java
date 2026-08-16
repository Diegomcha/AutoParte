package me.diegomcha.autoparte.api.common;

import jakarta.annotation.Nonnull;

import java.time.Instant;
import java.util.UUID;

public record EntityDtoCreated(
        @Nonnull UUID id,
        @Nonnull Instant createdAt
) {
}
