package me.diegomcha.autoparte.config;

import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.webservices.client.WebServiceTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpComponents5ClientFactory;
import org.springframework.ws.transport.http.SimpleHttpComponents5MessageSender;

import java.nio.charset.StandardCharsets;

@Configuration
class SesClientConfig {
    @Bean
    Jaxb2Marshaller marshaller() {
        var marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("es.mir.hospedajes");
        return marshaller;
    }

    @Bean
    WebServiceTemplate wsSesClient(WebServiceTemplateBuilder builder, AutoparteProperties autoparteProperties, SslBundles sslBundles, Jaxb2Marshaller marshaller, DynamicConfigService dynamicConfigService) {
        // Create HTTP client factory with custom TLS strategy and basic auth
        var clientFactory = HttpComponents5ClientFactory.withDefaults();
        clientFactory.addConnectionManagerBuilderCustomizer(b ->
                b.setTlsSocketStrategy(new DefaultClientTlsStrategy(sslBundles.getBundle("fnmt").createSslContext()))
        );
        clientFactory.addClientBuilderCustomizer(clientBuilder ->
                clientBuilder.addRequestInterceptorFirst((request, entity, context) -> {
                    var config = dynamicConfigService.getConfig();
                    var authorization = "Basic " + HttpHeaders.encodeBasicAuth(config.getSesUsername(), config.getSesPassword(), StandardCharsets.UTF_8);
                    request.setHeader("Authorization", authorization);

                    request.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    request.setHeader("Pragma", "no-cache");
                    request.setHeader("Expires", "0");
                })
        );

        // Build and return the WebServiceTemplate
        return builder
                .setDefaultUri(autoparteProperties.getSes().getUrl())
                .setMarshaller(marshaller)
                .setUnmarshaller(marshaller)
                .messageSenders(new SimpleHttpComponents5MessageSender(clientFactory))
                .build();
    }
}
