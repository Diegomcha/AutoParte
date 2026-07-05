package me.diegomcha.autoparte.core.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.address.dto.AddressDtoRequest;
import me.diegomcha.autoparte.api.catalogue.services.LocationCatalogueService;
import me.diegomcha.autoparte.core.validation.annotations.SpanishMunicipalityPostalCode;

@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class SpanishMunicipalityPostalCodeValidator implements ConstraintValidator<SpanishMunicipalityPostalCode, AddressDtoRequest> {

    private final LocationCatalogueService catalogueService;

    @Override
    public boolean isValid(AddressDtoRequest dto, ConstraintValidatorContext constraintValidatorContext) {
        if (!"ESP".equals(dto.country()))
            return true;

        try {
            // Ensure that the postal code is valid for the given municipality
            return catalogueService
                    .getSpanishPostalCodes(dto.municipality().substring(0, 2), dto.municipality().substring(2))
                    .contains(dto.postalCode());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}