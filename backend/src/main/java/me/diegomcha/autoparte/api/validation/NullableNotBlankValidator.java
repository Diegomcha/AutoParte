package me.diegomcha.autoparte.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class NullableNotBlankValidator implements ConstraintValidator<NullableNotBlank, CharSequence> {
    @Override
    public boolean isValid(CharSequence o, ConstraintValidatorContext constraintValidatorContext) {
        return o == null || !o.chars().allMatch(Character::isWhitespace);
    }
}