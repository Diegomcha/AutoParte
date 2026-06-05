package me.diegomcha.autoparte.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "autoparte")
public class AutoparteProperties {
    private String ocrUrl;
    private LocationCatalogueProperties locationCatalogue = new LocationCatalogueProperties();
    private SecurityProperties security = new SecurityProperties();

    @Data
    public static class LocationCatalogueProperties {
        private String provincesPath = "classpath:ine/provincias.csv";
        private String municipalitiesPath = "classpath:ine/municipios.csv";
        private String postalCodesPath = "classpath:ine/codigos_postales.csv";
        private String separator = ";";
    }

    @Data
    public static class SecurityProperties {
        private String initialAdminPassword;
        private String rememberMeKey;
    }
}
