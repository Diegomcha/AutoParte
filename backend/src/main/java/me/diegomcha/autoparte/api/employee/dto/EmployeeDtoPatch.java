package me.diegomcha.autoparte.api.employee.dto;

import jakarta.validation.constraints.Email;
import me.diegomcha.autoparte.validation.NullableNotBlank;

public record EmployeeDtoPatch(
        Boolean enabled,

        @NullableNotBlank String name,
        @NullableNotBlank String surname,
        @Email String email
) {
}
