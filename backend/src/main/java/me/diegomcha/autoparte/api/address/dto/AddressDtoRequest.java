package me.diegomcha.autoparte.api.address.dto;

import jakarta.validation.constraints.NotBlank;
import me.diegomcha.autoparte.core.validation.annotations.CountryCode;
import me.diegomcha.autoparte.core.validation.annotations.NullableNotBlank;
import me.diegomcha.autoparte.core.validation.annotations.SpanishMunicipalityPostalCode;

@SpanishMunicipalityPostalCode
public record AddressDtoRequest(
        @NotBlank String addressLine1,
        @NullableNotBlank String addressLine2,
        @NotBlank String municipality,
        @NotBlank String postalCode,
        @CountryCode String country
) {
}
