package me.diegomcha.autoparte.api.account.dto;

import jakarta.annotation.Nonnull;
import me.diegomcha.autoparte.domain.SecurityEvent;

import java.time.Instant;
import java.util.UUID;

public record SecurityEventDto(
        @Nonnull UUID id,
        @Nonnull Instant createdAt,
        @Nonnull Instant updatedAt,

        @Nonnull UUID accountId,

        @Nonnull Instant timestamp,
        @Nonnull SecurityEvent.SecurityEventType type,
        @Nonnull SecurityEvent.SecurityEventMethod method,
        @Nonnull String remoteAddress
) {
}
