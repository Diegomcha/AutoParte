package me.diegomcha.autoparte.integration.ses;

import es.mir.hospedajes.servicios.soap.comunicacion.ComunicacionRequest;
import es.mir.hospedajes.servicios.soap.comunicacion.ComunicacionResponse;
import es.mir.hospedajes.servicios.soap.comunicacion.ConsultaLoteResponse;
import io.sentry.Sentry;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.core.config.ConfigService;
import me.diegomcha.autoparte.core.exception.BadConfigurationException;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.integration.ses.dto.BatchDto;
import me.diegomcha.autoparte.integration.ses.mappers.RequestMapper;
import me.diegomcha.autoparte.integration.ses.mappers.ResponseMapper;
import me.diegomcha.autoparte.integration.ses.mappers.TypesMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
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
public class SesAPI {

    // TODO: Adjust these values based on SES service limitations
    public static final int MAX_BOOKING_BATCH_SIZE = 100;
    public static final int MAX_CHECKIN_BATCH_SIZE = 100;
    public static final int MAX_CHECK_BATCH_SIZE = 10;
    public static final int MAX_CANCEL_BATCH_SIZE = 100;

    private final ApplicationContext applicationContext;
    private final WebServiceTemplate client;
    private final Jaxb2Marshaller marshaller;
    
    private final ConfigService configService;

    private final RequestMapper reqMapper;
    private final ResponseMapper resMapper;
    private final TypesMapper typesMapper;

    public ComunicacionRequest prepareSendBooking(@NonNull Collection<@NonNull Booking> bookings) {
        this.checkBatchSize(bookings, MAX_BOOKING_BATCH_SIZE);

        return reqMapper.toSubmitCommunicationRequest(
                applicationContext.getApplicationName(),
                configService.getConfig().getSesLandlordCode(),
                RequestMapper.SesCommunicationType.BOOKING,
                this.encodePeticion(typesMapper.toPeticionReserva(bookings))
        );
    }

    public ComunicacionRequest prepareSendCheckIn(@NonNull String accommodationSesCode, @NonNull Collection<@NonNull Booking> bookings) {
        this.checkBatchSize(bookings, MAX_CHECKIN_BATCH_SIZE);

        return reqMapper.toSubmitCommunicationRequest(
                applicationContext.getApplicationName(),
                configService.getConfig().getSesLandlordCode(),
                RequestMapper.SesCommunicationType.CHECKIN,
                this.encodePeticion(typesMapper.toPeticionAlta(accommodationSesCode, bookings))
        );
    }

    public ComunicacionRequest prepareSendCancellation(@NonNull Collection<@NonNull UUID> sesIds) {
        this.checkBatchSize(sesIds, MAX_CANCEL_BATCH_SIZE);

        return reqMapper.toCancelCommunicationRequest(
                applicationContext.getApplicationName(),
                configService.getConfig().getSesLandlordCode(),
                this.encodePeticion(typesMapper.toComunicacionAnulacion(sesIds))
        );
    }

    public UUID sendCommunication(@NonNull ComunicacionRequest request) throws ServiceUnavailableException, BadConfigurationException {
        return resMapper.toUUID(this.sendRequest(request, ComunicacionResponse.class));
    }

    public Map<UUID, BatchDto> checkBatches(@NonNull Collection<@NonNull UUID> batchIds) throws ServiceUnavailableException, BadConfigurationException {
        this.checkBatchSize(batchIds, MAX_CHECK_BATCH_SIZE);

        return resMapper.toBatchMapDto(
                this.sendRequest(
                        reqMapper.toConsultaLoteRequest(batchIds),
                        ConsultaLoteResponse.class
                )
        );
    }

    public void checkConnection() throws BadConfigurationException, ServiceUnavailableException {
        // Check connection by sending an empty batch check request
        this.checkBatches(List.of());
    }

    private <T, R> R sendRequest(T request, Class<R> responseClass) throws ServiceUnavailableException {
        try {
            return responseClass.cast(client.marshalSendAndReceive(request));
        } catch (Exception e) {
            Sentry.captureException(e);
            throw new ServiceUnavailableException("SES service unavailable");
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
