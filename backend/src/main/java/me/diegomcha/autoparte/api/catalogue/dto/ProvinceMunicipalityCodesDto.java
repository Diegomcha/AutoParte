package me.diegomcha.autoparte.api.catalogue.dto;

import me.diegomcha.autoparte.core.validation.annotations.SpanishProvinceCode;
import me.diegomcha.autoparte.core.validation.annotations.SpanishProvinceMunicipalityCodes;
import org.springframework.web.bind.annotation.PathVariable;

@SpanishProvinceMunicipalityCodes
public record ProvinceMunicipalityCodesDto(
        @PathVariable @SpanishProvinceCode String provinceCode,
        @PathVariable String municipalityCode
) {
}
