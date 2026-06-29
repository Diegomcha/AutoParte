package me.diegomcha.autoparte.api.config;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.config.dto.ConfigDtoRequest;
import me.diegomcha.autoparte.api.config.dto.ConfigDtoResponse;
import me.diegomcha.autoparte.core.exception.BadConfigurationException;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class ConfigController implements ConfigAPI {

    private final ConfigService configService;

    @GetMapping
    @Override
    public ConfigDtoResponse getConfig() {
        return configService.getConfig();
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void updateConfig(@RequestBody @Valid ConfigDtoRequest patch) {
        configService.updateConfig(patch);
    }

    @PostMapping("/validate-ses")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void validateSesCredentials() throws BadConfigurationException, ServiceUnavailableException {
        configService.validateSesCredentials();
    }
}
