package me.diegomcha.autoparte.core.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import me.diegomcha.autoparte.api.booking.dto.BookingDtoRequest;
import me.diegomcha.autoparte.core.validation.annotations.ExpiryDateAfterDateConstraint;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class ExpiryDateAfterDateConstraintValidator implements ConstraintValidator<ExpiryDateAfterDateConstraint, BookingDtoRequest.PaymentDtoRequest> {

    @Override
    public boolean isValid(BookingDtoRequest.PaymentDtoRequest o, ConstraintValidatorContext constraintValidatorContext) {
        return o.date() == null || o.expiryDate() == null || o.date().isBefore(o.expiryDate());
    }
}