package me.diegomcha.autoparte.api.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.config.DynamicConfigService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class ConfigService {

    private final DynamicConfigService dynamicConfigService;

    // TODO: ...
}
