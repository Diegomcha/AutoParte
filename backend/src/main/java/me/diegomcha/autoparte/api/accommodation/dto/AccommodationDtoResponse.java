package me.diegomcha.autoparte.api.accommodation.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AccommodationDtoResponse(
        @NotNull UUID id,
        @NotNull Instant createdAt,
        @NotNull Instant updatedAt,

        @NotNull String name,
        @NotNull String sesCode,
        Boolean internetConnection,

        @NotNull Set<AccommodationDtoEmployeeResponse> employees
) {
    public record AccommodationDtoEmployeeResponse(
            @NotNull boolean enabled,
            @NotNull UUID id,
            @NotNull String name,
            @NotNull String email
    ) {
    }
}
