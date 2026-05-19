package me.diegomcha.autoparte.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import me.diegomcha.autoparte.validation.validators.ProvinceCodeValidator;

import java.lang.annotation.*;

@Constraint(validatedBy = ProvinceCodeValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProvinceCode {
    String message() default "The field must be a valid Spanish province code";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}