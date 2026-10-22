package me.diegomcha.autoparte.core.exception;

import lombok.NonNull;

import java.util.Set;

public class ServiceValidationException extends Exception {

    public enum ServiceValidationType {
        GENERIC_ERROR,
        INVALID_VALUE,
        MISSING_VALUE,
        TOO_LONG
    }

    private static final Set<Integer> ERROR_CODES = Set.of(
            10121, // Error de validación
            10130, // Valor incorrecto para el campo: $NOMBRE_CAMPO
            10131, // Es obligatorio indicar un valor en el campo: $NOMBRE_CAMPO
            10150  // Se ha superado el número máximo de caracteres de la aplicación
    );

    /**
     * Checks if the given error code matches any of the known service validation error codes.
     *
     * @param errorCode the error code to check
     * @return true if the error code matches any known service validation error codes, false otherwise
     */
    public static boolean matchesCode(@NonNull Integer errorCode) {
        return ERROR_CODES.contains(errorCode);
    }

    private static ServiceValidationType getTypeFromErrorCode(@NonNull Integer errorCode) {
        return switch (errorCode) {
            case 10121 -> ServiceValidationType.GENERIC_ERROR;
            case 10130 -> ServiceValidationType.INVALID_VALUE;
            case 10131 -> ServiceValidationType.MISSING_VALUE;
            case 10150 -> ServiceValidationType.TOO_LONG;
            default ->
                    throw new IllegalArgumentException("Unknown error code: " + errorCode);
        };
    }

    public ServiceValidationException(@NonNull Integer errorCode, String description) {
        super(getTypeFromErrorCode(errorCode).name() + ": " + description);
    }

    public ServiceValidationException(@NonNull ServiceValidationType type) {
        super(type.name());
    }

}
