package me.diegomcha.autoparte.api.accommodation.dto;

import jakarta.annotation.Nonnull;

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
            boolean enabled,
            @Nonnull UUID id,
            @Nonnull String name,
            @Nonnull String email
    ) {
    }
}
