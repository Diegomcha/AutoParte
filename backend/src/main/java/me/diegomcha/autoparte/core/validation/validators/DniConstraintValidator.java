package me.diegomcha.autoparte.core.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import me.diegomcha.autoparte.api.person.dto.PersonDtoRequest;
import me.diegomcha.autoparte.core.validation.Validations;
import me.diegomcha.autoparte.core.validation.annotations.DniConstraint;
import me.diegomcha.autoparte.domain.person.document.Document;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class DniConstraintValidator implements ConstraintValidator<DniConstraint, PersonDtoRequest.DocumentDtoRequest> {

    @Override
    public boolean isValid(PersonDtoRequest.DocumentDtoRequest o, ConstraintValidatorContext constraintValidatorContext) {
        // Skip if not DNI
        if (o.type() != Document.DocumentType.NIF && o.type() != Document.DocumentType.NIE)
            return true;

        if (!Validations.isValidNif(o.number())) {
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("Invalid DNI number")
                    .addPropertyNode("number")
                    .addConstraintViolation();
            return false;
        }

        if (o.supportNumber() == null) {
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("Support number is required for DNIs")
                    .addPropertyNode("supportNumber")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}