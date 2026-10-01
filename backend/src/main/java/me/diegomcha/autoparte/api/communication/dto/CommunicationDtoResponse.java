package me.diegomcha.autoparte.api.communication.dto;

import jakarta.annotation.Nonnull;
import me.diegomcha.autoparte.domain.communication.Communication;

import java.time.Instant;
import java.util.UUID;

public record CommunicationDtoResponse(
        @Nonnull UUID id,
        @Nonnull Instant createdAt,
        @Nonnull Instant updatedAt,

        @Nonnull UUID accommodationId,
        @Nonnull UUID bookingId,

        @Nonnull Communication.CommunicationType type,
        @Nonnull Communication.CommunicationStatus status,
        Instant sentTimestamp,
        String error
) {
}