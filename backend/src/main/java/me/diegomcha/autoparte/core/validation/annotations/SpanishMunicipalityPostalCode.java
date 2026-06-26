package me.diegomcha.autoparte.core.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import me.diegomcha.autoparte.core.validation.validators.SpanishMunicipalityPostalCodeValidator;

import java.lang.annotation.*;

@Constraint(validatedBy = SpanishMunicipalityPostalCodeValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SpanishMunicipalityPostalCode {
    String message() default "Postal code must be valid for the given municipality & municipality must exist";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}