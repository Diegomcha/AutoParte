package me.diegomcha.autoparte.api.employee.dto;

import jakarta.annotation.Nonnull;

import java.util.UUID;

public record EmployeeDtoCredentialsResponse(
        @Nonnull UUID id,

        @Nonnull String email,
        @Nonnull String password
) {

}
