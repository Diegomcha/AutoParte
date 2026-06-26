package me.diegomcha.autoparte.core.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.catalogue.services.LocationCatalogueService;
import me.diegomcha.autoparte.core.validation.annotations.CountryCode;

import java.util.Arrays;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class CountryCodeValidator implements ConstraintValidator<CountryCode, CharSequence> {

    private final LocationCatalogueService catalogueService;
    private boolean nullable;

    @Override
    public void initialize(CountryCode constraintAnnotation) {
        this.nullable = constraintAnnotation.nullable();
    }

    @Override
    public boolean isValid(CharSequence o, ConstraintValidatorContext constraintValidatorContext) {
        if (nullable && o == null)
            return true;

        return Arrays
                .asList(catalogueService.getCountries())
                .contains(o.toString());
    }
}