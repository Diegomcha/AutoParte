package me.diegomcha.autoparte.core.exception;

public class BadConfigurationException extends Exception {

    public enum BadConfigurationType {
        SES_UNKNOWN_LANDLORD_CODE,
        SES_BAD_CREDENTIALS,
        SES_LANDLORD_CANNOT_COMMUNICATE_TYPE,
        SES_LANDLORD_DISABLED_WEB_SERVICE
    }

    public BadConfigurationException(BadConfigurationType type) {
        super(type.name());
    }
}
