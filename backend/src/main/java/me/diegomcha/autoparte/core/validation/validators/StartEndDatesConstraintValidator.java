package me.diegomcha.autoparte.core.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import me.diegomcha.autoparte.api.booking.dto.BookingDtoRequest;
import me.diegomcha.autoparte.core.validation.annotations.StartEndDatesConstraint;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class StartEndDatesConstraintValidator implements ConstraintValidator<StartEndDatesConstraint, BookingDtoRequest> {

    @Override
    public boolean isValid(BookingDtoRequest o, ConstraintValidatorContext constraintValidatorContext) {
        return o.startTime().isBefore(o.endTime());
    }
}