package me.diegomcha.autoparte.api.auth.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record AccountDto(
    @NotNull String username,
    @NotNull Set<@NotNull String> roles
) {
}
