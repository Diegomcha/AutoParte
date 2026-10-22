package me.diegomcha.autoparte.api.employee.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record EmployeeDtoResponse(
        @NotNull UUID id,
        @NotNull Instant createdAt,
        @NotNull Instant updatedAt,

        @NotNull Boolean enabled,
        Instant disabledAt,

        @NotNull String name,
        @NotNull String surname,
        @NotNull String email,

        @NotNull Set<EmployeeDtoAccommodationResponse> accommodations
        ) {
        public record EmployeeDtoAccommodationResponse(
                @NotNull UUID id,
                @NotNull String name
        ) {
        }
}
