package me.diegomcha.autoparte.infra.config;

import me.diegomcha.autoparte.core.config.AutoparteProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OcrClientConfig {
    @Bean
    RestClient ocrRestClient(AutoparteProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getOcr().getUrl())
                .build();
    }
}
