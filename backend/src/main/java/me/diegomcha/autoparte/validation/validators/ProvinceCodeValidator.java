package me.diegomcha.autoparte.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.catalogue.services.LocationCatalogueService;
import me.diegomcha.autoparte.validation.annotations.ProvinceCode;

@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class ProvinceCodeValidator implements ConstraintValidator<ProvinceCode, CharSequence> {

    private final LocationCatalogueService catalogueService;

    @Override
    public boolean isValid(CharSequence o, ConstraintValidatorContext constraintValidatorContext) {
        return catalogueService.getSpanishProvinces().containsKey(o.toString());
    }
}