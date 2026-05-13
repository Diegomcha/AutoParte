package me.diegomcha.autoparte.integration.ocr;

import io.sentry.Sentry;
import me.diegomcha.autoparte.config.AutoparteProperties;
import me.diegomcha.autoparte.util.exception.ResourceUnprocessableException;
import me.diegomcha.autoparte.util.exception.ServiceUnavailable;
import me.diegomcha.autoparte.integration.ocr.dto.MrzDto;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Service
public class OcrService {

    private final RestClient client;

    protected OcrService(AutoparteProperties properties) {
        this.client = RestClient.builder()
                .baseUrl(properties.getOcrUrl())
                .build();
    }

    // TODO: Remove this test method
//    @Bean
//    CommandLineRunner testOcr() {
//        return args -> {
//            Resource image = new FileSystemResource("D:/dni.jpg");
//            var response = convertImageToMrz(image);
//            System.out.println(response);
//        };
//    }

    public MrzDto convertImageToMrz(Resource image) throws ServiceUnavailable, ResourceUnprocessableException {
        // Create the multipart request body
        MultiValueMap<String, Resource> body = new LinkedMultiValueMap<>();
        body.add("image", image);

        // Send the POST request to the OCR server and parse the response
        try {
            return client
                    .post()
                    .uri("/mrz")
                    .body(body)
                    .retrieve()
                    .body(MrzDto.class);
        } catch (HttpClientErrorException e) {
            throw new ResourceUnprocessableException("Unable to process the provided image");
        } catch (HttpServerErrorException e) {
            Sentry.captureException(e);
            throw new ServiceUnavailable("MRZ service is unavailable");
        }
    }
}
