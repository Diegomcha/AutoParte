package me.diegomcha.autoparte.integration.ocr;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import me.diegomcha.autoparte.config.AutoparteProperties;
import me.diegomcha.autoparte.core.exception.ResourceUnprocessableException;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.integration.ocr.dto.MrzDto;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;


class OcrClientTest {

    private static HttpServer server;
    private static String response;
    private static int responseStatus;

    private OcrClient client;
    private Resource image;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/mrz", OcrClientTest::handleRequest);
        server.start();
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @BeforeEach
    void setUp() {
        var properties = new AutoparteProperties();
        properties.getOcr().setUrl("http://localhost:" + server.getAddress().getPort());

        client = new OcrClient(properties);
        image = new ByteArrayResource(new byte[]{1, 2, 3}) {
            @Override
            public String getFilename() {
                return "image.png";
            }
        };
    }

    @Test
    void convertImageToMrzReturnsParsedResponse()
            throws ServiceUnavailableException, ResourceUnprocessableException {

        responseStatus = 200;
        response = """
                {
                  "valid": true,
                  "raw": "IDESP...",
                  "errors": [],
                  "warnings": [],
                  "data": {
                    "surname": "DOE JOHN",
                    "name": "JANE",
                    "country": "ESP",
                    "nationality": "ESP",
                    "birth_date": "900102",
                    "expiry_date": "300102",
                    "sex": "F",
                    "document_type": "ID",
                    "document_number": "ABC123",
                    "optional_data": "12345678Z"
                  }
                }
                """;

        MrzDto result = client.convertImageToMrz(image);

        Assertions.assertTrue(result.valid());
        Assertions.assertEquals("JANE", result.data().name());
        Assertions.assertEquals("DOE JOHN", result.data().surname());
        Assertions.assertEquals("ESP", result.data().nationality());
        Assertions.assertEquals("12345678Z", result.data().optionalData());
    }

    @Test
    void convertImageToMrzSendsMultipartRequest() {
        responseStatus = 200;
        response = """
                {
                  "valid": true,
                  "raw": "IDESP...",
                  "errors": [],
                  "warnings": [],
                  "data": null
                }
                """;

        Assertions.assertDoesNotThrow(() -> client.convertImageToMrz(image));
        // The fake server validates the HTTP method, URI, and content type.
        // If the request is not multipart, it returns HTTP 400 and the test fails.
    }

    @Test
    void convertImageToMrzThrowsResourceUnprocessableException() {
        responseStatus = 422;
        response = "";

        Assertions.assertThrows(ResourceUnprocessableException.class, () -> client.convertImageToMrz(image));
    }

    @Test
    void convertImageToMrzThrowsServiceUnavailableException() {
        responseStatus = 500;
        response = "";

        Assertions.assertThrows(ServiceUnavailableException.class, () -> client.convertImageToMrz(image));
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        try (exchange) {
            if (!"POST".equals(exchange.getRequestMethod())
                    || !"/mrz".equals(exchange.getRequestURI().getPath())
                    || !exchange.getRequestHeaders()
                    .getFirst("Content-Type")
                    .startsWith("multipart/form-data")) {
                exchange.sendResponseHeaders(400, 0);
                return;
            }

            byte[] body = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(responseStatus, body.length);
            exchange.getResponseBody().write(body);
        }
    }
}