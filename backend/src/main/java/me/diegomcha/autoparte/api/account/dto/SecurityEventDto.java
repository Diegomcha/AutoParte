package me.diegomcha.autoparte.api.account.dto;

import jakarta.validation.constraints.NotNull;
import me.diegomcha.autoparte.domain.SecurityEvent;

import java.time.Instant;
import java.util.UUID;

public record SecurityEventDto(
        @NotNull UUID id,
        @NotNull Instant createdAt,
        @NotNull Instant updatedAt,

        UUID accountId,

        @NotNull Instant timestamp,
        @NotNull SecurityEvent.SecurityEventType type,
        @NotNull SecurityEvent.SecurityEventMethod method,
        @NotNull String remoteAddress
) {
}
