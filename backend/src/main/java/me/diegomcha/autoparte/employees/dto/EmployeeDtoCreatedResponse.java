package me.diegomcha.autoparte.employees.dto;

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
