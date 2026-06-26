package me.diegomcha.autoparte.integration.ses.mappers;

import es.mir.hospedajes.servicios.soap.comunicacion.ComunicacionRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

class RequestMapperTest {

    private RequestMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new RequestMapper();
    }

    private static final Object[][] BATCH_IDS = {
            {Collections.emptyList(), Collections.emptyList()},
            {
                    List.of(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")),
                    List.of("123e4567-e89b-12d3-a456-426614174000")
            },
            {
                    List.of(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), UUID.fromString("123e4567-e89b-12d3-a456-426614174001")),
                    List.of("123e4567-e89b-12d3-a456-426614174000", "123e4567-e89b-12d3-a456-426614174001")
            }
    };

    @ParameterizedTest
    @FieldSource("BATCH_IDS")
    void testToConsultaLoteRequest(Collection<UUID> sesId, Collection<String> expectedLote) {
        var request = mapper.toConsultaLoteRequest(sesId);

        Assertions.assertEquals(expectedLote, request.getCodigosLote().getLote());
    }

    private static final Object[][] COMMUNICATION_TYPES = {
            {RequestMapper.SesCommunicationType.BOOKING, "RH"},
            {RequestMapper.SesCommunicationType.CHECKIN, "PV"}
    };

    @ParameterizedTest
    @FieldSource("COMMUNICATION_TYPES")
    void testToSubmitCommunicationRequest(RequestMapper.SesCommunicationType type, String expectedTipoComunicacion) throws IOException {
        ComunicacionRequest request = mapper.toSubmitCommunicationRequest("test-application", "00000", type, "base64-encoded-zip-content");

        Assertions.assertEquals("test-application", request.getPeticion().getCabecera().getAplicacion());
        Assertions.assertEquals("00000", request.getPeticion().getCabecera().getCodigoArrendador());
        Assertions.assertEquals(expectedTipoComunicacion, request.getPeticion().getCabecera().getTipoComunicacion());
        Assertions.assertEquals("A", request.getPeticion().getCabecera().getTipoOperacion());
        Assertions.assertEquals("base64-encoded-zip-content", request.getPeticion().getSolicitud());
    }

    @Test
    void testToCancelCommunicationRequest() throws IOException {
        ComunicacionRequest request = mapper.toCancelCommunicationRequest("test-application", "00000", "base64-encoded-zip-content");

        Assertions.assertEquals("test-application", request.getPeticion().getCabecera().getAplicacion());
        Assertions.assertEquals("00000", request.getPeticion().getCabecera().getCodigoArrendador());
        Assertions.assertNull(request.getPeticion().getCabecera().getTipoComunicacion());
        Assertions.assertEquals("B", request.getPeticion().getCabecera().getTipoOperacion());
        Assertions.assertEquals("base64-encoded-zip-content", request.getPeticion().getSolicitud());
    }

    // TODO: Move to SesClient test
//    private void assertBase64ZipContent(String encoded) {
//        try (var stringInput = new ByteArrayInputStream(Base64.getDecoder().decode(encoded))) {
//            try (var unzipStream = new ZipInputStream(stringInput)) {
//                unzipStream.getNextEntry();
//                Assertions.assertEquals("<mocked-xml/>", new String(unzipStream.readAllBytes()));
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to decode and unzip the base64 content", e);
//        }
//    }
}
