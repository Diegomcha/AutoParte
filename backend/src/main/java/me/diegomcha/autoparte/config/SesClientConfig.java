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
public class SesClientConfig {
    @Bean
    public WebServiceTemplate sesClient(WebServiceTemplateBuilder builder, AutoparteProperties autoparteProperties, SslBundles sslBundles) {
        // Create marshaller
        var marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("me.diegomcha.autoparte.integration.ses.wsdl");

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
