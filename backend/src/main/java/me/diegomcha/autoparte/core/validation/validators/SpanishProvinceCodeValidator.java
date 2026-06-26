package me.diegomcha.autoparte.core.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.catalogue.services.LocationCatalogueService;
import me.diegomcha.autoparte.core.validation.annotations.SpanishProvinceCode;

@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class SpanishProvinceCodeValidator implements ConstraintValidator<SpanishProvinceCode, CharSequence> {

    private final LocationCatalogueService catalogueService;

    @Override
    public boolean isValid(CharSequence o, ConstraintValidatorContext constraintValidatorContext) {
        return catalogueService.getSpanishProvinces().containsKey(o.toString());
    }
}