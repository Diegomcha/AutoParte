package me.diegomcha.autoparte.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.catalogue.dto.ProvinceMunicipalityCodesDto;
import me.diegomcha.autoparte.api.catalogue.services.LocationCatalogueService;
import me.diegomcha.autoparte.validation.annotations.ProvinceMunicipalityCodes;

@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class ProvinceMunicipalityCodesValidator implements ConstraintValidator<ProvinceMunicipalityCodes, ProvinceMunicipalityCodesDto> {

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