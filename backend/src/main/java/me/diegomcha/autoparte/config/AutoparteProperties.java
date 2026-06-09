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
    private SesProperties ses = new SesProperties();

    @Data
    public static class LocationCatalogueProperties {
        private String provincesPath = "classpath:ine/provincias.csv";
        private String municipalitiesPath = "classpath:ine/municipios.csv";
        private String postalCodesPath = "classpath:ine/codigos_postales.csv";
        private String separator = ";";
    }

    @Data
    public static class SesProperties {
        private String username;
        private String password;
        private String landlordCode;
        private String endpoint = "https://hospedajes.ses.mir.es/hospedajes-web/ws/v1/comunicacion";
    }

    @Data
    public static class SecurityProperties {
        private String initialAdminPassword;
        private String rememberMeKey;
    }
}
