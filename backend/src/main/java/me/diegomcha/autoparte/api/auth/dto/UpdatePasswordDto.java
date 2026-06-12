package me.diegomcha.autoparte.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePasswordDto(
        @NotBlank String username,
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8, max = 64) String newPassword
) {
}
