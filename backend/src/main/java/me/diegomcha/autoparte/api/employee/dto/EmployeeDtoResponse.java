package me.diegomcha.autoparte.api.employee.dto;

import java.time.Instant;
import java.util.UUID;

public record EmployeeDtoResponse(
        UUID id,
        Instant createdAt,
        Instant updatedAt,

        boolean enabled,
        Instant disabledAt,

        String name,
        String surname,
        String email
) {

}
