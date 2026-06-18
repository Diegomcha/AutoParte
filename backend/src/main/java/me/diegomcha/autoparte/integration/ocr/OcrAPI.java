package me.diegomcha.autoparte.integration.ocr;

import io.sentry.Sentry;
import me.diegomcha.autoparte.config.AutoparteProperties;
import me.diegomcha.autoparte.core.exception.ResourceUnprocessableException;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.integration.ocr.dto.MrzDto;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Service
public class OcrAPI {

    private final RestClient client;

    protected OcrAPI(AutoparteProperties properties) {
        this.client = RestClient.builder()
                .baseUrl(properties.getOcr().getUrl())
                .build();
    }

    public MrzDto convertImageToMrz(Resource image) throws ServiceUnavailableException, ResourceUnprocessableException {
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
            throw new ServiceUnavailableException("MRZ service is unavailable");
        }
    }
}
