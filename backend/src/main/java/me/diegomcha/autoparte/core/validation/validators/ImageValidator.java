package me.diegomcha.autoparte.core.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import me.diegomcha.autoparte.core.validation.Validations;
import me.diegomcha.autoparte.core.validation.annotations.Image;
import org.springframework.web.multipart.MultipartFile;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class ImageValidator implements ConstraintValidator<Image, MultipartFile> {

    @Override
    public boolean isValid(MultipartFile o, ConstraintValidatorContext constraintValidatorContext) {
        return Validations.isValidImage(o);
    }
}