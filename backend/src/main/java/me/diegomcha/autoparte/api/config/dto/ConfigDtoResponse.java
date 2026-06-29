package me.diegomcha.autoparte.api.config.dto;

public record ConfigDtoResponse(
        String sesUsername,
        String sesPassword,
        String sesLandlordCode,

        boolean sesCredentialsValid,
        boolean digitalSignatureEnabled,
        boolean manualReviewEnabled
) {
}
