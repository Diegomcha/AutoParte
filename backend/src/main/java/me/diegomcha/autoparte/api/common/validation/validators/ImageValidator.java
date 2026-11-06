package me.diegomcha.autoparte.api.common.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import me.diegomcha.autoparte.api.common.validation.annotations.Image;
import me.diegomcha.autoparte.domain.Validations;
import org.springframework.web.multipart.MultipartFile;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class ImageValidator implements ConstraintValidator<Image, MultipartFile> {

    @Override
    public boolean isValid(MultipartFile o, ConstraintValidatorContext constraintValidatorContext) {
        return Validations.isValidImage(o);
    }
}