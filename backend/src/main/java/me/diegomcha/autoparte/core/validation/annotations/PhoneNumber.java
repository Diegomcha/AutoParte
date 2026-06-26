package me.diegomcha.autoparte.core.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import me.diegomcha.autoparte.core.validation.validators.PhoneNumberValidator;

import java.lang.annotation.*;

@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PhoneNumber {

    String message() default "The field must be a phone number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}