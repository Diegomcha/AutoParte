package me.diegomcha.autoparte.integration.ses;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.config.DynamicConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SesChecker {

    private final Logger logger = LoggerFactory.getLogger(SesChecker.class);

    private final DynamicConfigService dynamicConfigService;
    private final SesPersistencyService persistencyService;
    private final SesClient sesClient;

    // TODO: Complete!

}
