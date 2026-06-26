package me.diegomcha.autoparte.api.address.dto;

import jakarta.annotation.Nonnull;

import java.time.Instant;
import java.util.UUID;

public record AddressDtoResponse(
        @Nonnull UUID id,
        @Nonnull Instant createdAt,
        @Nonnull Instant updatedAt,

        @Nonnull String addressLine1,
        String addressLine2,
        @Nonnull String municipality,
        @Nonnull String postalCode,
        @Nonnull String country
) {
}
