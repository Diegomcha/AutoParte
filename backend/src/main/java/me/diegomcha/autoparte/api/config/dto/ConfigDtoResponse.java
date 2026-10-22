package me.diegomcha.autoparte.api.config.dto;

import jakarta.validation.constraints.NotNull;

public record ConfigDtoResponse(
        @NotNull String sesUsername,
        @NotNull String sesPassword,
        @NotNull String sesLandlordCode,

        @NotNull boolean sesCredentialsValid,

        @NotNull boolean digitalSignatureEnabled,
        @NotNull boolean manualReviewEnabled,

        @NotNull int logsRetentionDays,
        @NotNull int bookingsRetentionDays
) {
}
