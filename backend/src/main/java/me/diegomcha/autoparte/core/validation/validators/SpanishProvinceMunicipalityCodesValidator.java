package me.diegomcha.autoparte.core.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.catalogue.services.LocationCatalogueService;
import me.diegomcha.autoparte.core.validation.annotations.SpanishProvinceMunicipalityCodes;

@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class SpanishProvinceMunicipalityCodesValidator implements ConstraintValidator<SpanishProvinceMunicipalityCodes, Object[]> {

    private final LocationCatalogueService catalogueService;

    @Override
    public boolean isValid(Object[] values, ConstraintValidatorContext constraintValidatorContext) {
        String provinceCode = (String) values[0];
        String municipalityCode = (String) values[1];

        try {
            return catalogueService
                    .getSpanishMunicipalities(provinceCode)
                    .containsKey(municipalityCode);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}