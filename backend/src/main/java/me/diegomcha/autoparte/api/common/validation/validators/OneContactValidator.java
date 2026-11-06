package me.diegomcha.autoparte.api.common.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import me.diegomcha.autoparte.api.common.validation.annotations.OneContact;
import me.diegomcha.autoparte.api.person.dto.PersonDtoRequest;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class OneContactValidator implements ConstraintValidator<OneContact, PersonDtoRequest.ContactInfoDtoRequest> {

    @Override
    public boolean isValid(PersonDtoRequest.ContactInfoDtoRequest o, ConstraintValidatorContext constraintValidatorContext) {
        return o.phoneNumber1() != null || o.phoneNumber2() != null || o.email() != null;
    }
}