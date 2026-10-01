package me.diegomcha.autoparte.api.config.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import me.diegomcha.autoparte.core.validation.annotations.NullableNotBlank;

public record ConfigDtoRequest(
        @NullableNotBlank String sesUsername,
        @NullableNotBlank String sesPassword,
        @NullableNotBlank String sesLandlordCode,

        @NotNull boolean digitalSignatureEnabled,
        @NotNull boolean manualReviewEnabled,

        @NotNull @Min(0) int logsRetentionDays,
        @NotNull @Min(0) int bookingsRetentionDays
) {
}
