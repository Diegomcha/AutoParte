package me.diegomcha.autoparte.api.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.config.dto.ConfigDtoRequest;
import me.diegomcha.autoparte.api.config.dto.ConfigDtoResponse;
import me.diegomcha.autoparte.config.DynamicConfigService;
import me.diegomcha.autoparte.core.exception.BadConfigurationException;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.integration.ses.SesClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO: Test this service
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional(readOnly = true)
class ConfigService {

    private final DynamicConfigService dynamicConfigService;
    private final ConfigMapper configMapper;
    private final SesClient sesClient;

    /**
     * Get the current configuration.
     *
     * @return ConfigDtoResponse containing the current configuration.
     */
    public ConfigDtoResponse getConfig() {
        return configMapper.toResponse(dynamicConfigService.getConfig());
    }

    /**
     * Update the configuration based on the provided DTO.
     *
     * @param dto ConfigDtoRequest containing the new configuration values.
     */
    @Transactional
    public void updateConfig(ConfigDtoRequest dto) {
        dynamicConfigService.updateConfig(config -> configMapper.fromUpdate(dto, config));
        dynamicConfigService.updateConfig(config -> config.setSesCredentialsValid(false)); // Reset SES credentials validation status after update
    }

    /**
     * Validate the SES credentials by checking the connection to the SES service.
     *
     * @throws BadConfigurationException   if the SES credentials are invalid or misconfigured.
     * @throws ServiceUnavailableException if the SES service is unavailable or cannot be reached.
     */
    @Transactional
    public void validateSesCredentials() throws BadConfigurationException, ServiceUnavailableException {
        // Check if SES is configured in the application configuration
        if (!dynamicConfigService.getConfig().isSesConfigured())
            throw new BadConfigurationException(BadConfigurationException.BadConfigurationType.NO_CREDENTIALS_PROVIDED);

        sesClient.checkConnection();

        // If the connection is successful, update the configuration to mark SES credentials as valid
        dynamicConfigService.updateConfig(config -> config.setSesCredentialsValid(true));
    }
}
