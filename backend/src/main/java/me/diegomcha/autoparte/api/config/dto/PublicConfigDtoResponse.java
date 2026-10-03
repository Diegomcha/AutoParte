package me.diegomcha.autoparte.api.config.dto;

import jakarta.validation.constraints.NotNull;

public record PublicConfigDtoResponse(
        @NotNull boolean digitalSignatureEnabled
) {
}
