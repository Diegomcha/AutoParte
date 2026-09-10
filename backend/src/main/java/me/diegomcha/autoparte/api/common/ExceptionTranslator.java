package me.diegomcha.autoparte.api.common;

import me.diegomcha.autoparte.core.exception.*;
import org.apache.coyote.BadRequestException;
import org.jspecify.annotations.Nullable;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ControllerAdvice
class ExceptionTranslator extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleResourceConflictException(ResourceConflictException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ProblemDetail handleResourceUnprocessableException(ResourceUnprocessableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ProblemDetail handleServiceUnavailableException(ServiceUnavailableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail handleUnauthorizedException(UnauthorizedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ProblemDetail handleUnsupportedMediaException(UnsupportedMediaException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail handleBadConfigurationException(BadConfigurationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleBadRequestException(BadRequestException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problemDetail = ex.getBody();

        problemDetail.setDetail("Validation error. See 'errors' property for details.");

        // 1. Extract field-level validation errors (@PathVariable, @RequestParam, DTO fields)
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Objects.requireNonNullElse(error.getDefaultMessage(), "Invalid value")
                ));

        // 2. Extract class-level/global validation errors (e.g. @SpanishProvinceMunicipalityCodes on DTO)
        var globalErrors = ex.getBindingResult().getGlobalErrors().stream()
                .map(error -> Objects.requireNonNullElse(error.getDefaultMessage(), "Invalid object state"))
                .toList();

        // 3. Combine both field-level and global errors into a single structure
        problemDetail.setProperty("errors", Map.of(
                "global", globalErrors,
                "fields", fieldErrors
        ));

        return super.handleMethodArgumentNotValid(ex, headers, status, request);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ProblemDetail problemDetail = ex.getBody();

        problemDetail.setDetail("Validation error. See 'errors' property for details.");

        var errors = ex.getValueResults().stream()
                .flatMap(result -> {
                    final String name = String.format("%s (%d)", result.getMethodParameter().getParameterName(), result.getMethodParameter().getParameterIndex());

                    return result.getResolvableErrors().stream()
                            .map(error -> Map.entry(
                                    name,
                                    Objects.requireNonNullElse(error.getDefaultMessage(), "Invalid value")
                            ));
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existingMsg, newMsg) -> existingMsg + "; " + newMsg // Combines multiple errors for the same parameter
                ));

        problemDetail.setProperty("errors", errors);

        return super.handleExceptionInternal(ex, problemDetail, headers, status, request);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handlePropertyReferenceException(PropertyReferenceException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                String.format(
                        "The property '%s' does not exist on the target resource type '%s'.",
                        ex.getPropertyName(),
                        ex.getType().getType().getSimpleName()
                )
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleGenericException(Exception ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
