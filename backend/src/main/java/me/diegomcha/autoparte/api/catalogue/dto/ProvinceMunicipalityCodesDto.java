package me.diegomcha.autoparte.api.catalogue.dto;

import me.diegomcha.autoparte.core.validation.annotations.ProvinceCode;
import me.diegomcha.autoparte.core.validation.annotations.ProvinceMunicipalityCodes;
import org.springframework.web.bind.annotation.PathVariable;

@ProvinceMunicipalityCodes
public record ProvinceMunicipalityCodesDto(
        @PathVariable @ProvinceCode String provinceCode,
        @PathVariable String municipalityCode
) {
}
