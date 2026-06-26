package me.diegomcha.autoparte.core.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import me.diegomcha.autoparte.core.validation.validators.ImageValidator;

import java.lang.annotation.*;

@Constraint(validatedBy = ImageValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Image {
    String message() default "Invalid image file";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}