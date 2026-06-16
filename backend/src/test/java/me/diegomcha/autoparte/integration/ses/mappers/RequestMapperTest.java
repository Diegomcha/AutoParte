package me.diegomcha.autoparte.integration.ses.mappers;

import es.mir.hospedajes.servicios.soap.comunicacion.ComunicacionRequest;
import me.diegomcha.autoparte.config.AutoparteProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.oxm.Marshaller;

import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipInputStream;

@ExtendWith(MockitoExtension.class)
class RequestMapperTest {

    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private AutoparteProperties autoparteProperties;
    @Mock
    private Marshaller marshaller;

    private RequestMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new RequestMapper(this.applicationContext, this.autoparteProperties, this.marshaller);
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
        this.setUpMocks();
        ComunicacionRequest request = mapper.toSubmitCommunicationRequest(type, new Object());

        Assertions.assertEquals("test-application", request.getPeticion().getCabecera().getAplicacion());
        Assertions.assertEquals("00000", request.getPeticion().getCabecera().getCodigoArrendador());
        Assertions.assertEquals(expectedTipoComunicacion, request.getPeticion().getCabecera().getTipoComunicacion());
        Assertions.assertEquals("A", request.getPeticion().getCabecera().getTipoOperacion());

        this.assertBase64ZipContent(request.getPeticion().getSolicitud());
    }

    @Test
    void testToCancelCommunicationRequest() throws IOException {
        this.setUpMocks();
        ComunicacionRequest request = mapper.toCancelCommunicationRequest(new es.mir.hospedajes.neg.anularcomunicacion.ComunicacionType());

        Assertions.assertEquals("test-application", request.getPeticion().getCabecera().getAplicacion());
        Assertions.assertEquals("00000", request.getPeticion().getCabecera().getCodigoArrendador());
        Assertions.assertNull(request.getPeticion().getCabecera().getTipoComunicacion());
        Assertions.assertEquals("B", request.getPeticion().getCabecera().getTipoOperacion());

        this.assertBase64ZipContent(request.getPeticion().getSolicitud());
    }

    private void setUpMocks() throws IOException {
        // Mock application context
        Mockito.when(applicationContext.getApplicationName()).thenReturn("test-application");

        // Mock properties
        var sesProperties = new AutoparteProperties.SesProperties();
        sesProperties.setUsername("test-username");
        sesProperties.setPassword("test-password");
        sesProperties.setLandlordCode("00000");
        sesProperties.setEndpoint(null);
        Mockito.when(autoparteProperties.getSes()).thenReturn(sesProperties);

        // Mock marshaller
        Mockito.doAnswer(invocation -> {
            if (invocation.getArgument(1) instanceof StreamResult streamResult) {
                streamResult.getOutputStream().write("<mocked-xml/>".getBytes());
            }

            return null;
        }).when(marshaller).marshal(Mockito.any(), Mockito.any());
    }

    private void assertBase64ZipContent(String encoded) {
        try (var stringInput = new ByteArrayInputStream(Base64.getDecoder().decode(encoded))) {
            try (var unzipStream = new ZipInputStream(stringInput)) {
                unzipStream.getNextEntry();
                Assertions.assertEquals("<mocked-xml/>", new String(unzipStream.readAllBytes()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to decode and unzip the base64 content", e);
        }
    }
}
