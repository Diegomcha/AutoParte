package me.diegomcha.autoparte.api.auth.dto;

import jakarta.annotation.Nonnull;

import java.util.Set;

public record AccountDto(
    @Nonnull String username,
    @Nonnull Set<String> roles
) {
}
