package me.diegomcha.autoparte.api.common.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import me.diegomcha.autoparte.api.common.validation.annotations.PhoneNumber;
import me.diegomcha.autoparte.domain.Validations;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, CharSequence> {
    @Override
    public boolean isValid(CharSequence o, ConstraintValidatorContext constraintValidatorContext) {
        return Validations.isValidPhone(o.toString());
    }
}