package me.diegomcha.autoparte.api.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.diegomcha.autoparte.api.config.dto.ConfigDtoPatch;
import me.diegomcha.autoparte.api.config.dto.ConfigDtoResponse;

@Tag(name = "Configuration", description = "Operations related to application configuration")
public interface ConfigAPI {

    @Operation(summary = "Get current application configuration")
    ConfigDtoResponse getConfig();

    @Operation(summary = "Update application configuration")
    void updateConfig(ConfigDtoPatch patch);

}
