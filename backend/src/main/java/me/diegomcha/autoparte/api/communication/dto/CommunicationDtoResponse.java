package me.diegomcha.autoparte.api.communication.dto;

import jakarta.validation.constraints.NotNull;
import me.diegomcha.autoparte.domain.communication.Communication;

import java.time.Instant;
import java.util.UUID;

public record CommunicationDtoResponse(
        @NotNull UUID id,
        @NotNull Instant createdAt,
        @NotNull Instant updatedAt,

        @NotNull UUID accommodationId,
        @NotNull UUID bookingId,

        @NotNull Communication.CommunicationType type,
        @NotNull Communication.CommunicationStatus status,
        Instant sentTimestamp,
        String error
) {
}