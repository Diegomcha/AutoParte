package me.diegomcha.autoparte.core.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import me.diegomcha.autoparte.core.validation.validators.DniConstraintValidator;

import java.lang.annotation.*;

@Constraint(validatedBy = DniConstraintValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DniConstraint {
    String message() default "Any of the DNI document fields provided is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}