package me.diegomcha.autoparte.api.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmployeeDtoCreate(
        @NotBlank String name,
        @NotBlank String surname,
        @Email @NotBlank String email
) {
}
