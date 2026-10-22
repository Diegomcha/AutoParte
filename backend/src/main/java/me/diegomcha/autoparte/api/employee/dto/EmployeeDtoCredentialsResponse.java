package me.diegomcha.autoparte.api.employee.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EmployeeDtoCredentialsResponse(
        @NotNull UUID id,

        @NotNull String email,
        @NotNull String password
) {

}
