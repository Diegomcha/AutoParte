package me.diegomcha.autoparte.api.accommodation.dto;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AccommodationDtoResponse(
        @Nonnull UUID id,
        @Nonnull Instant createdAt,
        @Nonnull Instant updatedAt,

        @Nonnull String name,
        @Nonnull String sesCode,
        Boolean internetConnection,

        @Nonnull Set<AccommodationDtoEmployeeResponse> employees
) {
    public record AccommodationDtoEmployeeResponse(
            @NotNull boolean enabled,
            @Nonnull UUID id,
            @Nonnull String name,
            @Nonnull String email
    ) {
    }
}
