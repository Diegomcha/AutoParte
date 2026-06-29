package me.diegomcha.autoparte.api.config.dto;

import me.diegomcha.autoparte.core.validation.annotations.NullableNotBlank;

public record ConfigDtoRequest(
        @NullableNotBlank String sesUsername,
        @NullableNotBlank String sesPassword,
        @NullableNotBlank String sesLandlordCode,

        boolean digitalSignatureEnabled,
        boolean manualReviewEnabled
) {
}
