package me.diegomcha.autoparte.core.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import me.diegomcha.autoparte.core.validation.Validations;
import me.diegomcha.autoparte.core.validation.annotations.PhoneNumber;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, CharSequence> {
    @Override
    public boolean isValid(CharSequence o, ConstraintValidatorContext constraintValidatorContext) {
        return Validations.isValidPhone(o.toString());
    }
}