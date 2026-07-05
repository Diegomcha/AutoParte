package me.diegomcha.autoparte.core.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import me.diegomcha.autoparte.core.validation.validators.ExpiryDateAfterDateConstraintValidator;
import me.diegomcha.autoparte.core.validation.validators.StartEndDatesConstraintValidator;

import java.lang.annotation.*;

@Constraint(validatedBy = ExpiryDateAfterDateConstraintValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExpiryDateAfterDateConstraint {
    String message() default "Expiry date must be after payment date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}