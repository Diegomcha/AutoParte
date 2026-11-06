package me.diegomcha.autoparte.api.catalogue;

import me.diegomcha.autoparte.api.common.validation.annotations.SpanishProvinceCode;
import me.diegomcha.autoparte.api.common.validation.annotations.SpanishProvinceMunicipalityCodes;
import org.springframework.web.bind.annotation.PathVariable;

@SpanishProvinceMunicipalityCodes
public record ProvinceMunicipalityCodesDto(
        @PathVariable @SpanishProvinceCode String provinceCode,
        @PathVariable String municipalityCode
) {
}
