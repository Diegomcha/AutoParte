package me.diegomcha.autoparte.core.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.catalogue.dto.ProvinceMunicipalityCodesDto;
import me.diegomcha.autoparte.api.catalogue.services.LocationCatalogueService;
import me.diegomcha.autoparte.core.validation.annotations.SpanishProvinceMunicipalityCodes;

@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class SpanishProvinceMunicipalityCodesValidator implements ConstraintValidator<SpanishProvinceMunicipalityCodes, ProvinceMunicipalityCodesDto> {

    private final LocationCatalogueService catalogueService;

    @Override
    public boolean isValid(ProvinceMunicipalityCodesDto dto, ConstraintValidatorContext constraintValidatorContext) {
        try {
            return catalogueService
                    .getSpanishMunicipalities(dto.provinceCode())
                    .containsKey(dto.municipalityCode());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}