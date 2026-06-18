package me.diegomcha.autoparte.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "autoparte")
public class AutoparteProperties {
    private LocationCatalogueProperties locationCatalogue = new LocationCatalogueProperties();
    private SecurityProperties security = new SecurityProperties();
    private SesProperties ses = new SesProperties();
    private OcrProperties ocr = new OcrProperties();

    @Data
    public static class LocationCatalogueProperties {
        private String provincesPath = "classpath:ine/provincias.csv";
        private String municipalitiesPath = "classpath:ine/municipios.csv";
        private String postalCodesPath = "classpath:ine/codigos_postales.csv";
        private String separator = ";";
    }

    @Data
    public static class OcrProperties {
        private String url;
    }

    @Data
    public static class SesProperties {
        // These can be set through properties for development purposes
        private String initialUsername;
        private String initialPassword;
        private String initialLandlordCode;
        // -----
        private String url = "https://hospedajes.ses.mir.es/hospedajes-web/ws/v1/comunicacion";
    }

    @Data
    public static class SecurityProperties {
        private String initialAdminPassword = "admin";
    }
}
