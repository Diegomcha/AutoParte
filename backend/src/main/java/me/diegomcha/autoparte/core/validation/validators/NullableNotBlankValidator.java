package me.diegomcha.autoparte.core.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import me.diegomcha.autoparte.core.validation.annotations.NullableNotBlank;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class NullableNotBlankValidator implements ConstraintValidator<NullableNotBlank, CharSequence> {
    @Override
    public boolean isValid(CharSequence o, ConstraintValidatorContext constraintValidatorContext) {
        return o == null || !o.chars().allMatch(Character::isWhitespace);
    }
}