package me.diegomcha.autoparte.api.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.diegomcha.autoparte.api.config.dto.ConfigDtoRequest;
import me.diegomcha.autoparte.api.config.dto.ConfigDtoResponse;
import me.diegomcha.autoparte.core.exception.BadConfigurationException;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;

@Tag(name = "Configuration", description = "Operations related to application configuration")
@SuppressWarnings("unused")
public interface ConfigAPI {

    @Operation(summary = "Get current application configuration")
    ConfigDtoResponse getConfig();

    @Operation(summary = "Update application configuration")
    void updateConfig(ConfigDtoRequest patch);

    @Operation(summary = "Validate SES credentials")
    void validateSesCredentials() throws BadConfigurationException, ServiceUnavailableException;
}
