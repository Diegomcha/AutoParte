package me.diegomcha.autoparte.api.employee.dto;

import jakarta.annotation.Nonnull;

import java.time.Instant;
import java.util.UUID;

public record EmployeeDtoCreatedResponse(
        @Nonnull UUID id,
        @Nonnull Instant createdAt,

        @Nonnull String name,
        @Nonnull String surname,
        @Nonnull String email,
        @Nonnull String password
) {

}
