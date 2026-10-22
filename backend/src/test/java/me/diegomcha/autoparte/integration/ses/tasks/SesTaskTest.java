package me.diegomcha.autoparte.integration.ses.tasks;

import me.diegomcha.autoparte.config.DynamicConfigService;
import me.diegomcha.autoparte.core.exception.BadConfigurationException;
import me.diegomcha.autoparte.core.exception.ExceptionWrapper;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.domain.Configuration;
import me.diegomcha.autoparte.integration.ses.SesClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.util.function.Consumer;

class SesTaskTest {

    private static final String BAD_CONFIG_LOG_FORMAT = "Bad configuration for SES: {}";
    private static final String BAD_CONFIG_TYPE = "SES_BAD_CREDENTIALS";
    private static final String UNEXPECTED_ERROR_LOG = "Unexpected error occurred during a SES scheduled task";

    private final Logger logger = Mockito.mock(Logger.class);
    private final DynamicConfigService dynamicConfigService = Mockito.mock(DynamicConfigService.class);
    private final SesClient sesClient = Mockito.mock(SesClient.class);
    private final SesPersistencyService persistencyService = Mockito.mock(SesPersistencyService.class);

    private TestSesTask task;

    @BeforeEach
    void setUp() {
        task = new TestSesTask(logger, dynamicConfigService, sesClient, persistencyService);
    }

    @Test
    void handlesServiceUnavailableWithoutInvalidatingCredentials() {
        task.handle(new ServiceUnavailableException("SES service unavailable"));

        Mockito.verify(logger).warn("SES service unavailable, will retry later");
        Mockito.verify(dynamicConfigService, Mockito.never()).updateConfig(Mockito.any());
        Mockito.verify(logger, Mockito.never()).error(Mockito.anyString(), Mockito.any(Object[].class));
    }

    @Test
    void handlesBadConfigurationByInvalidatingCredentials() {
        var configuration = stubValidConfiguration();

        task.handle(new BadConfigurationException(
                BadConfigurationException.BadConfigurationType.SES_BAD_CREDENTIALS
        ));

        Assertions.assertFalse(configuration.isSesCredentialsValid());
        Mockito.verify(logger).error(BAD_CONFIG_LOG_FORMAT, BAD_CONFIG_TYPE);
        Mockito.verify(dynamicConfigService).updateConfig(Mockito.any());
    }

    @Test
    void unwrapsExceptionWrapperBeforeHandling() {
        var configuration = stubValidConfiguration();

        task.handle(new ExceptionWrapper(new BadConfigurationException(
                BadConfigurationException.BadConfigurationType.SES_BAD_CREDENTIALS
        )));

        Assertions.assertFalse(configuration.isSesCredentialsValid());
        Mockito.verify(dynamicConfigService).updateConfig(Mockito.any());
        Mockito.verify(logger).error(BAD_CONFIG_LOG_FORMAT, BAD_CONFIG_TYPE);
    }

    @Test
    void capturesUnexpectedExceptionsAndLogsThem() {
        var exception = new IllegalStateException("unexpected failure");

        task.handle(exception);

        Mockito.verify(logger).error(UNEXPECTED_ERROR_LOG, exception);
        Mockito.verify(dynamicConfigService, Mockito.never()).updateConfig(Mockito.any());
    }

    // * Helper methods

    private Configuration stubValidConfiguration() {
        var configuration = Configuration._blank();
        configuration.setSesCredentialsValid(true);

        Mockito.doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<Configuration> consumer = invocation.getArgument(0, Consumer.class);
            consumer.accept(configuration);
            return null;
        }).when(dynamicConfigService).updateConfig(Mockito.any());

        return configuration;
    }

    private static final class TestSesTask extends SesTask {

        private TestSesTask(
                Logger logger,
                DynamicConfigService dynamicConfigService,
                SesClient sesClient,
                SesPersistencyService persistencyService
        ) {
            super(logger, dynamicConfigService, sesClient, persistencyService);
        }

        private void handle(Exception exception) {
            handleException(exception);
        }
    }
}