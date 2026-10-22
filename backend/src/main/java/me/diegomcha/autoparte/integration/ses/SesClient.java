package me.diegomcha.autoparte.integration.ses;

import es.mir.hospedajes.servicios.soap.comunicacion.ComunicacionRequest;
import es.mir.hospedajes.servicios.soap.comunicacion.ComunicacionResponse;
import es.mir.hospedajes.servicios.soap.comunicacion.ConsultaLoteResponse;
import io.sentry.Sentry;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.config.DynamicConfigService;
import me.diegomcha.autoparte.core.exception.BadConfigurationException;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.core.exception.ServiceValidationException;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.integration.ses.dto.BatchDto;
import me.diegomcha.autoparte.integration.ses.mappers.RequestMapper;
import me.diegomcha.autoparte.integration.ses.mappers.ResponseMapper;
import me.diegomcha.autoparte.integration.ses.mappers.TypesMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.core.WebServiceTemplate;

import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SesClient {

    public static final int MAX_BOOKING_BATCH_SIZE = 100;
    public static final int MAX_CHECKIN_BATCH_SIZE = 100;
    public static final int MAX_CHECK_BATCH_SIZE = 10;
    public static final int MAX_CANCEL_BATCH_SIZE = 100;

    private final ApplicationContext applicationContext;
    private final WebServiceTemplate wsClient;
    private final Jaxb2Marshaller marshaller;

    private final DynamicConfigService dynamicConfigService;

    private final RequestMapper reqMapper;
    private final ResponseMapper resMapper;
    private final TypesMapper typesMapper;

    /**
     * Prepares a ComunicacionRequest for sending a booking communication to the SES service.
     *
     * @param bookings A collection of Booking objects to be sent. The size of the collection must not exceed {@link #MAX_BOOKING_BATCH_SIZE}.
     * @return A ComunicacionRequest object ready to be sent to the SES service.
     * @throws IllegalArgumentException if the size of the bookings collection exceeds {@link #MAX_BOOKING_BATCH_SIZE}.
     */
    public ComunicacionRequest prepareSendBooking(@NonNull Collection<@NonNull Booking> bookings) {
        this.checkBatchSize(bookings, MAX_BOOKING_BATCH_SIZE);

        return reqMapper.toSubmitCommunicationRequest(
                applicationContext.getId(),
                dynamicConfigService.getConfig().getSesLandlordCode(),
                RequestMapper.SesCommunicationType.BOOKING,
                this.encodePeticion(typesMapper.toPeticionReserva(bookings))
        );
    }

    /**
     * Prepares a ComunicacionRequest for sending a check-in communication to the SES service.
     *
     * @param accommodationSesCode The SES code of the accommodation.
     * @param bookings             A collection of Booking objects to be sent. The size of the collection must not exceed {@link #MAX_CHECKIN_BATCH_SIZE}.
     * @return A ComunicacionRequest object ready to be sent to the SES service.
     * @throws IllegalArgumentException if the size of the bookings collection exceeds {@link #MAX_CHECKIN_BATCH_SIZE}.
     */
    public ComunicacionRequest prepareSendCheckIn(@NonNull String accommodationSesCode, @NonNull Collection<@NonNull Booking> bookings) {
        this.checkBatchSize(bookings, MAX_CHECKIN_BATCH_SIZE);

        return reqMapper.toSubmitCommunicationRequest(
                applicationContext.getId(),
                dynamicConfigService.getConfig().getSesLandlordCode(),
                RequestMapper.SesCommunicationType.CHECKIN,
                this.encodePeticion(typesMapper.toPeticionAlta(accommodationSesCode, bookings))
        );
    }

    /**
     * Prepares a ComunicacionRequest for sending a cancellation communication to the SES service.
     *
     * @param sesIds A collection of SES IDs to be cancelled. The size of the collection must not exceed {@link #MAX_CANCEL_BATCH_SIZE}.
     * @return A ComunicacionRequest object ready to be sent to the SES service.
     * @throws IllegalArgumentException if the size of the sesIds collection exceeds {@link #MAX_CANCEL_BATCH_SIZE}.
     */
    public ComunicacionRequest prepareSendCancellation(@NonNull Collection<@NonNull UUID> sesIds) {
        this.checkBatchSize(sesIds, MAX_CANCEL_BATCH_SIZE);

        return reqMapper.toCancelCommunicationRequest(
                applicationContext.getId(),
                dynamicConfigService.getConfig().getSesLandlordCode(),
                this.encodePeticion(typesMapper.toComunicacionAnulacion(sesIds))
        );
    }

    /**
     * Sends a communication request to the SES service and returns the batch ID of the submitted communication.
     *
     * @param request The communication request to be sent.
     * @return The batch ID of the submitted communication.
     * @throws ServiceUnavailableException If the SES service is unavailable.
     * @throws BadConfigurationException   If the configuration is bad.
     * @throws ServiceValidationException  If the SES service returns a validation error.
     */
    public UUID sendCommunication(@NonNull ComunicacionRequest request) throws ServiceUnavailableException, BadConfigurationException, ServiceValidationException {
        return resMapper.toUUID(this.sendRequest(request, ComunicacionResponse.class));
    }

    /**
     * Checks the status of the specified batches.
     *
     * @param batchIds The IDs of the batches to check.
     * @return A map of batch IDs to their corresponding status information.
     * @throws ServiceUnavailableException If the SES service is unavailable.
     * @throws BadConfigurationException   If the configuration is bad.
     * @throws ServiceValidationException  If the SES service returns a validation error.
     */
    public Map<UUID, BatchDto> checkBatches(@NonNull Collection<@NonNull UUID> batchIds) throws ServiceUnavailableException, BadConfigurationException, ServiceValidationException {
        this.checkBatchSize(batchIds, MAX_CHECK_BATCH_SIZE);

        return resMapper.toBatchMapDto(
                this.sendRequest(
                        reqMapper.toConsultaLoteRequest(batchIds),
                        ConsultaLoteResponse.class
                )
        );
    }

    /**
     * Checks the connection to the SES service.
     *
     * @throws BadConfigurationException   If the configuration is bad.
     * @throws ServiceUnavailableException If the SES service is unavailable.
     */
    public void checkConnection() throws BadConfigurationException, ServiceUnavailableException {
        // Check connection by sending an empty batch check request
        try {
            this.checkBatches(List.of(UUID.randomUUID()));
        } catch (ServiceValidationException e) {
            // This exception is not expected!
            throw new RuntimeException(e);
        }
    }

    private <T, R> R sendRequest(T request, Class<R> responseClass)
            throws ServiceUnavailableException, BadConfigurationException {
        try {
            // Business exception handling mostly is handled by ResponseMapper
            return responseClass.cast(wsClient.marshalSendAndReceive(request));
        } catch (WebServiceTransportException wsTransport) {
            String msg = wsTransport.getMessage();
            // 502 Bad Gateway is returned by SES (for some unknown reason) when the credentials are invalid, so we treat it as a bad configuration
            if (msg != null && (msg.contains("401") || msg.contains("502") || msg.contains("403")))
                throw new BadConfigurationException(BadConfigurationException.BadConfigurationType.SES_BAD_CREDENTIALS);

            Sentry.captureException(wsTransport);
            throw new ServiceUnavailableException("SES service unavailable due to transport error");

        } catch (WebServiceIOException ioEx) {
            Sentry.captureException(ioEx);
            throw new ServiceUnavailableException("SES service unreachable (network timeout/failure)");

        } catch (Exception e) {
            Sentry.captureException(e);
            throw new ServiceUnavailableException("Unexpected error communicating with SES");
        }
    }

    private void checkBatchSize(Collection<?> collection, int maxSize) {
        if (collection.size() > maxSize)
            throw new IllegalArgumentException("Batch size exceeds maximum limit of " + maxSize);
    }

    private String encodePeticion(@NonNull Object obj) {
        try (var outputStream = new ByteArrayOutputStream()) {
            try (var zipStream = new ZipOutputStream(outputStream)) {
                zipStream.putNextEntry(new ZipEntry("peticion.xml"));
                marshaller.marshal(obj, new StreamResult(zipStream));
            }
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
