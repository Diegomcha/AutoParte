package me.diegomcha.autoparte.api.config.dto;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;

public record ConfigDtoResponse(
        @Nonnull String sesUsername,
        @Nonnull String sesPassword,
        @Nonnull String sesLandlordCode,

        @NotNull boolean sesCredentialsValid,
        @NotNull boolean digitalSignatureEnabled,
        @NotNull boolean manualReviewEnabled
) {
}
