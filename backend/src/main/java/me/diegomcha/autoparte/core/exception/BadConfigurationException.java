package me.diegomcha.autoparte.core.exception;

import lombok.NonNull;

import java.util.Set;

public class BadConfigurationException extends Exception {

    public enum BadConfigurationType {
        NO_CREDENTIALS_PROVIDED,    // Not used in SES

        SES_UNKNOWN_LANDLORD_CODE,
        SES_BAD_CREDENTIALS,
        SES_LANDLORD_CANNOT_COMMUNICATE_TYPE,
        SES_LANDLORD_DISABLED_WEB_SERVICE
    }

    private static final Set<Integer> ERROR_CODES = Set.of(
            10103, // El código de arrendador no existe en el sistema
            10107, // Usuario incorrecto
            10119, // El arrendador no puede realizar ese tipo de comunicaciones
            10120  // El arrendador no tiene habilitada la opción de realizar operaciones a través del servicio web
    );

    /**
     * Checks if the given error code matches any of the known bad configurations error codes.
     *
     * @param errorCode the error code to check
     * @return true if the error code matches any known bad configurations error codes, false otherwise
     */
    public static boolean matchesCode(@NonNull Integer errorCode) {
        return ERROR_CODES.contains(errorCode);
    }

    private static BadConfigurationType getTypeFromErrorCode(@NonNull Integer errorCode) {
        return switch (errorCode) {
            case 10103 -> BadConfigurationType.SES_UNKNOWN_LANDLORD_CODE;
            case 10107 -> BadConfigurationType.SES_BAD_CREDENTIALS;
            case 10119 -> BadConfigurationType.SES_LANDLORD_CANNOT_COMMUNICATE_TYPE;
            case 10120 -> BadConfigurationType.SES_LANDLORD_DISABLED_WEB_SERVICE;
            default ->
                    throw new IllegalArgumentException("Unknown error code: " + errorCode);
        };
    }

    public BadConfigurationException(@NonNull Integer errorCode, String description) {
        super(getTypeFromErrorCode(errorCode).name() + ": " + description);
    }

    public BadConfigurationException(@NonNull BadConfigurationType type) {
        super(type.name());
    }
}
