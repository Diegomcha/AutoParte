package me.diegomcha.autoparte.integration.ses;

import es.mir.hospedajes.servicios.soap.comunicacion.ComunicacionRequest;
import es.mir.hospedajes.servicios.soap.comunicacion.ComunicacionResponse;
import es.mir.hospedajes.servicios.soap.comunicacion.ConsultaLoteRequest;
import es.mir.hospedajes.servicios.soap.comunicacion.ConsultaLoteResponse;
import jakarta.xml.bind.JAXBElement;
import me.diegomcha.autoparte.config.DynamicConfigService;
import me.diegomcha.autoparte.core.exception.BadConfigurationException;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.Configuration;
import me.diegomcha.autoparte.integration.ses.dto.BatchDto;
import me.diegomcha.autoparte.integration.ses.mappers.RequestMapper;
import me.diegomcha.autoparte.integration.ses.mappers.ResponseMapper;
import me.diegomcha.autoparte.integration.ses.mappers.TypesMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.core.WebServiceTemplate;

import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;
import java.util.zip.ZipInputStream;

class SesClientTest {

    private static final String APP_ID = "test-application";
    private static final String LANDLORD_CODE = "00000";
    private static final String MOCK_XML = "<mocked-xml/>";

    private final ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
    private final WebServiceTemplate wsClient = Mockito.mock(WebServiceTemplate.class);
    private final Jaxb2Marshaller marshaller = Mockito.mock(Jaxb2Marshaller.class);
    private final DynamicConfigService dynamicConfigService = Mockito.mock(DynamicConfigService.class);
    private final RequestMapper requestMapper = Mockito.mock(RequestMapper.class);
    private final ResponseMapper responseMapper = Mockito.mock(ResponseMapper.class);
    private final TypesMapper typesMapper = Mockito.mock(TypesMapper.class);

    private SesClient client;

    @BeforeEach
    void setUp() {
        client = new SesClient(
                applicationContext,
                wsClient,
                marshaller,
                dynamicConfigService,
                requestMapper,
                responseMapper,
                typesMapper
        );
    }

    @Test
    void prepareSendBookingCreatesRequestWithEncodedZip() {
        var booking = Mockito.mock(Booking.class);
        var request = mockCommunicationRequest();
        var jaxbElement = Mockito.mock(JAXBElement.class);

        stubCommonContextAndMarshaller();

        Mockito.when(typesMapper.toPeticionReserva(List.of(booking))).thenReturn(jaxbElement);
        Mockito.doAnswer(invocation -> {
            assertEncodedZipContainsMockXml(invocation.getArgument(3, String.class));
            return request;
        }).when(requestMapper).toSubmitCommunicationRequest(
                Mockito.eq(APP_ID), Mockito.eq(LANDLORD_CODE),
                Mockito.eq(RequestMapper.SesCommunicationType.BOOKING), Mockito.any()
        );

        client.prepareSendBooking(List.of(booking));

        Mockito.verify(requestMapper).toSubmitCommunicationRequest(
                Mockito.eq(APP_ID), Mockito.eq(LANDLORD_CODE),
                Mockito.eq(RequestMapper.SesCommunicationType.BOOKING), Mockito.any()
        );
    }

    @Test
    void prepareSendBookingRejectsBatchesOverMaximumSize() {
        var bookings = createBatch(Booking.class, SesClient.MAX_BOOKING_BATCH_SIZE + 1);
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.prepareSendBooking(bookings));
    }

    @Test
    void prepareSendCheckInCreatesRequestWithEncodedZip() {
        var booking = Mockito.mock(Booking.class);
        var request = mockCommunicationRequest();
        var jaxbElement = Mockito.mock(JAXBElement.class);

        stubCommonContextAndMarshaller();

        Mockito.when(typesMapper.toPeticionAlta("ACC01", List.of(booking)))
                .thenReturn(jaxbElement);
        Mockito.doAnswer(invocation -> {
            assertEncodedZipContainsMockXml(invocation.getArgument(3, String.class));
            return request;
        }).when(requestMapper).toSubmitCommunicationRequest(
                Mockito.eq(APP_ID), Mockito.eq(LANDLORD_CODE),
                Mockito.eq(RequestMapper.SesCommunicationType.CHECKIN), Mockito.any()
        );

        client.prepareSendCheckIn("ACC01", List.of(booking));

        Mockito.verify(requestMapper).toSubmitCommunicationRequest(
                Mockito.eq(APP_ID), Mockito.eq(LANDLORD_CODE),
                Mockito.eq(RequestMapper.SesCommunicationType.CHECKIN), Mockito.any()
        );
    }

    @Test
    void prepareSendCheckInRejectsBatchesOverMaximumSize() {
        var bookings = createBatch(Booking.class, SesClient.MAX_CHECKIN_BATCH_SIZE + 1);
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.prepareSendCheckIn("ACC01", bookings));
    }

    @Test
    void prepareSendCancellationCreatesRequestWithEncodedZip() {
        var batchId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        var request = mockCommunicationRequest();
        var jaxbElement = Mockito.mock(JAXBElement.class);

        stubCommonContextAndMarshaller();

        Mockito.when(typesMapper.toComunicacionAnulacion(List.of(batchId))).thenReturn(jaxbElement);
        Mockito.doAnswer(invocation -> {
            assertEncodedZipContainsMockXml(invocation.getArgument(2, String.class));
            return request;
        }).when(requestMapper).toCancelCommunicationRequest(
                Mockito.eq(APP_ID), Mockito.eq(LANDLORD_CODE), Mockito.any()
        );

        client.prepareSendCancellation(List.of(batchId));

        Mockito.verify(requestMapper).toCancelCommunicationRequest(
                Mockito.eq(APP_ID), Mockito.eq(LANDLORD_CODE), Mockito.any()
        );
    }

    @Test
    void prepareSendCancellationRejectsBatchesOverMaximumSize() {
        var ids = createBatch(UUID.class, SesClient.MAX_CANCEL_BATCH_SIZE + 1);
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.prepareSendCancellation(ids));
    }

    @Test
    void sendCommunicationReturnsBatchId() throws Exception {
        var expected = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        var request = Mockito.mock(ComunicacionRequest.class);
        var response = Mockito.mock(ComunicacionResponse.class);

        Mockito.when(wsClient.marshalSendAndReceive(request)).thenReturn(response);
        Mockito.when(responseMapper.toUUID(response)).thenReturn(expected);

        Assertions.assertEquals(expected, client.sendCommunication(request));
        Mockito.verify(wsClient).marshalSendAndReceive(request);
    }

    @Test
    void sendCommunicationTranslatesTransportFailures() {
        var request = Mockito.mock(ComunicacionRequest.class);
        Mockito.when(wsClient.marshalSendAndReceive(request)).thenThrow(new RuntimeException("connection failed"));

        Assertions.assertThrows(
                ServiceUnavailableException.class,
                () -> client.sendCommunication(request)
        );
    }

    @Test
    void sendCommunicationThrowsBadConfigurationExceptionOn502() {
        var request = Mockito.mock(ComunicacionRequest.class);
        Mockito.when(wsClient.marshalSendAndReceive(request))
                .thenThrow(new WebServiceTransportException("502 Bad Gateway"));

        Assertions.assertThrows(
                BadConfigurationException.class,
                () -> client.sendCommunication(request)
        );
    }

    @Test
    void checkBatchesMapsResponse() throws Exception {
        var batchId = UUID.randomUUID();
        var request = Mockito.mock(ConsultaLoteRequest.class);
        var response = Mockito.mock(ConsultaLoteResponse.class);
        var expected = Map.of(batchId, Mockito.mock(BatchDto.class));

        Mockito.when(requestMapper.toConsultaLoteRequest(List.of(batchId))).thenReturn(request);
        Mockito.when(wsClient.marshalSendAndReceive(request)).thenReturn(response);
        Mockito.when(responseMapper.toBatchMapDto(response)).thenReturn(expected);

        Assertions.assertEquals(expected, client.checkBatches(List.of(batchId)));
        Mockito.verify(requestMapper).toConsultaLoteRequest(List.of(batchId));
    }

    @Test
    void checkBatchesRejectsMoreThanTenIds() {
        var ids = createBatch(UUID.class, SesClient.MAX_CHECK_BATCH_SIZE + 1);
        Assertions.assertThrows(IllegalArgumentException.class, () -> client.checkBatches(ids));
    }

    @Test
    void checkConnectionSuccessfullyCallsCheckBatches() throws Exception {
        var request = Mockito.mock(ConsultaLoteRequest.class);
        var response = Mockito.mock(ConsultaLoteResponse.class);

        Mockito.when(requestMapper.toConsultaLoteRequest(Mockito.any())).thenReturn(request);
        Mockito.when(wsClient.marshalSendAndReceive(request)).thenReturn(response);
        Mockito.when(responseMapper.toBatchMapDto(response)).thenReturn(Map.of());

        Assertions.assertDoesNotThrow(() -> client.checkConnection());
        Mockito.verify(requestMapper).toConsultaLoteRequest(Mockito.any());
    }

    // * Helper methods

    private void stubCommonContextAndMarshaller() {
        var configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getSesLandlordCode()).thenReturn(LANDLORD_CODE);

        Mockito.when(applicationContext.getId()).thenReturn(APP_ID);
        Mockito.when(dynamicConfigService.getConfig()).thenReturn(configuration);

        Mockito.doAnswer(invocation -> {
            var result = invocation.getArgument(1, StreamResult.class);
            result.getOutputStream().write(MOCK_XML.getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(marshaller).marshal(Mockito.any(), Mockito.any(StreamResult.class));
    }

    private ComunicacionRequest mockCommunicationRequest() {
        var request = Mockito.mock(ComunicacionRequest.class);
        var peticion = Mockito.mock(es.mir.hospedajes.servicios.soap.tipocomunicacion.PeticionType.class);
        var header = Mockito.mock(es.mir.hospedajes.servicios.soap.tipocomunicacion.CabeceraLoteType.class);

        Mockito.when(request.getPeticion()).thenReturn(peticion);
        Mockito.when(peticion.getCabecera()).thenReturn(header);
        return request;
    }

    private void assertEncodedZipContainsMockXml(String encodedZip) throws Exception {
        try (var zip = new ZipInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(encodedZip)))) {
            Assertions.assertEquals("peticion.xml", Objects.requireNonNull(zip.getNextEntry()).getName());
            Assertions.assertArrayEquals(MOCK_XML.getBytes(StandardCharsets.UTF_8), zip.readAllBytes());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> createBatch(Class<T> clazz, int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> {
                    if (clazz.equals(UUID.class))
                        return (T) UUID.randomUUID();
                    return (T) Mockito.mock(clazz);
                })
                .toList();
    }
}