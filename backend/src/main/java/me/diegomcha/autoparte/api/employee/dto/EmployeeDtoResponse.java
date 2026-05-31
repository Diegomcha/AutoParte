package me.diegomcha.autoparte.api.employee.dto;

import jakarta.annotation.Nonnull;

import java.time.Instant;
import java.util.UUID;

public record EmployeeDtoResponse(
        @Nonnull UUID id,
        @Nonnull Instant createdAt,
        @Nonnull Instant updatedAt,

        @Nonnull Boolean enabled,
        Instant disabledAt,

        @Nonnull String name,
        @Nonnull String surname,
        @Nonnull String email
) {

}
