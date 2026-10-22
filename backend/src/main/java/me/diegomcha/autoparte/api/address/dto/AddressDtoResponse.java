package me.diegomcha.autoparte.api.address.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record AddressDtoResponse(
        @NotNull UUID id,
        @NotNull Instant createdAt,
        @NotNull Instant updatedAt,

        @NotNull String addressLine1,
        String addressLine2,
        @NotNull String municipality,
        @NotNull String postalCode,
        @NotNull String country
) {
}
