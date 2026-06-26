package me.diegomcha.autoparte.core.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import me.diegomcha.autoparte.core.validation.validators.OneContactValidator;

import java.lang.annotation.*;

@Constraint(validatedBy = OneContactValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OneContact {
    String message() default "At least a contact method (email or phone) must be provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}