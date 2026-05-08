package me.diegomcha.autoparte.employees.dto;

import jakarta.validation.constraints.Email;
import me.diegomcha.autoparte.validations.NullableNotBlank;

public record EmployeeDtoPatch(
        Boolean enabled,

        @NullableNotBlank String name,
        @NullableNotBlank String surname,
        @Email String email
) {
}
