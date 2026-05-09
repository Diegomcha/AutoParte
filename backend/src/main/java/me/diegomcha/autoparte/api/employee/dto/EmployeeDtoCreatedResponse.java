package me.diegomcha.autoparte.api.employee.dto;

import java.time.Instant;
import java.util.UUID;

public record EmployeeDtoCreatedResponse(
        UUID id,
        Instant createdAt,

        String name,
        String surname,
        String email,
        String password
) {

}
