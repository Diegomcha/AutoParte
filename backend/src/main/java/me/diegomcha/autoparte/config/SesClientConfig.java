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

@Configuration
class SesClientConfig {
    @Bean
    Jaxb2Marshaller marshaller() {
        var marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan(
                "es.mir.hospedajes.servicios.soap.comunicacion",
                "es.mir.hospedajes.servicios.soap.tipocomunicacion");
        return marshaller;
    }

    @Bean
    WebServiceTemplate sesClient(WebServiceTemplateBuilder builder, AutoparteProperties autoparteProperties, SslBundles sslBundles, Jaxb2Marshaller marshaller) {
        // Create HTTP client factory with custom TLS strategy and basic auth
        var clientFactory = HttpComponents5ClientFactory.withDefaults();
        clientFactory.addConnectionManagerBuilderCustomizer(b ->
                b.setTlsSocketStrategy(new DefaultClientTlsStrategy(sslBundles.getBundle("fnmt").createSslContext()))
        );
        var authorization = "Basic " + HttpHeaders.encodeBasicAuth(autoparteProperties.getSes().getUsername(), autoparteProperties.getSes().getPassword(), java.nio.charset.StandardCharsets.UTF_8);
        clientFactory.addClientBuilderCustomizer(clientBuilder ->
                clientBuilder.addRequestInterceptorFirst((request, entity, context) ->
                        request.setHeader("Authorization", authorization)
                )
        );

        // Build and return the WebServiceTemplate
        return builder
                .setDefaultUri(autoparteProperties.getSes().getEndpoint())
                .setMarshaller(marshaller)
                .setUnmarshaller(marshaller)
                .messageSenders(new SimpleHttpComponents5MessageSender(clientFactory))
                .build();
    }
}
