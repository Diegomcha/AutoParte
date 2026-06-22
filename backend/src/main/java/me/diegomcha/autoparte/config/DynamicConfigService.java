package me.diegomcha.autoparte.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.core.repos.ConfigRepo;
import me.diegomcha.autoparte.domain.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional
public class DynamicConfigService {

    private final ConfigRepo configRepo;
    private final AutoparteProperties autoparteProperties;

    public Configuration getConfig() {
        return configRepo.findAll()
                .stream()
                .findFirst()
                .orElseGet(this::initializeConfig);
    }

    public void updateConfig(Consumer<Configuration> updater) {
        var config = getConfig();
        updater.accept(config);
    }

    private Configuration initializeConfig() {
        var config = Configuration._blank();

        // Initialize SES credentials from application properties (Development environment)
        config.setSesUsername(autoparteProperties.getSes().getInitialUsername());
        config.setSesPassword(autoparteProperties.getSes().getInitialPassword());
        config.setSesLandlordCode(autoparteProperties.getSes().getInitialLandlordCode());
        if (config.isSesConfigured()) config.setSesCredentialsValid(true);
        // ----

        return configRepo.save(config);
    }
}
