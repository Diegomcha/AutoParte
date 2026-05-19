package me.diegomcha.autoparte.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import me.diegomcha.autoparte.validation.validators.ProvinceMunicipalityCodesValidator;

import java.lang.annotation.*;

@Constraint(validatedBy = ProvinceMunicipalityCodesValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProvinceMunicipalityCodes {
    String message() default "The municipality code must be valid for the given province code";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}