package me.diegomcha.autoparte.integration.ocr;

import io.sentry.Sentry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class OcrClient {

    private final RestClient client;

    public MrzDto convertImageToMrz(@NonNull Resource image) throws ServiceUnavailableException, ResourceUnprocessableException {
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
