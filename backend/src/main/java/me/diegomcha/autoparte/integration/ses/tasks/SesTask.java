package me.diegomcha.autoparte.integration.ses.tasks;

import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.config.DynamicConfigService;
import me.diegomcha.autoparte.core.exception.BadConfigurationException;
import me.diegomcha.autoparte.core.exception.ExceptionWrapper;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.integration.ses.SesClient;
import org.slf4j.Logger;

// TODO: Test this class
@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
abstract class SesTask {

    protected final Logger logger;
    protected final DynamicConfigService dynamicConfigService;

    protected final SesClient sesClient;
    protected final SesPersistencyService persistencyService;

    /**
     * Handles the given {@link Exception} by logging the error and capturing it with Sentry.
     * It unwraps the exception if it is an instance of {@link ExceptionWrapper} to get the original cause.
     *
     * @param e the {@link Exception} to handle
     */
    protected void handleException(Exception e) {
        var cause = e instanceof ExceptionWrapper ew ? ew.getCause() : e;

        switch (cause) {
            case ServiceUnavailableException ignored:
                logger.warn("SES service unavailable, will retry later");
                break;
            case BadConfigurationException bce:
                logger.error("Bad configuration for SES: {}", bce.getMessage());
                dynamicConfigService.updateConfig(config -> config.setSesCredentialsValid(false));
                break;
            default:
                logger.error("Unexpected error occurred during a SES scheduled task", cause);
                Sentry.captureException(cause);
                break;
        }
    }

    /**
     * Checks if the SES credentials are valid.
     *
     * @return true if the SES credentials are valid, false otherwise
     */
    protected boolean credentialsAreValid() {
        return dynamicConfigService.getConfig().isSesCredentialsValid();
    }
}
