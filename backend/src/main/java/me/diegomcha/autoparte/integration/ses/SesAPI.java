package me.diegomcha.autoparte.integration.ses;

import es.mir.hospedajes.servicios.soap.comunicacion.ComunicacionRequest;
import es.mir.hospedajes.servicios.soap.comunicacion.ComunicacionResponse;
import es.mir.hospedajes.servicios.soap.comunicacion.ConsultaLoteResponse;
import io.sentry.Sentry;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.core.exception.BadConfigurationException;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.integration.ses.dto.BatchDto;
import me.diegomcha.autoparte.integration.ses.mappers.RequestMapper;
import me.diegomcha.autoparte.integration.ses.mappers.ResponseMapper;
import me.diegomcha.autoparte.integration.ses.mappers.TypesMapper;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SesAPI {

    // TODO: Adjust these values based on SES service limitations
    public static final int MAX_BOOKING_BATCH_SIZE = 100;
    public static final int MAX_CHECKIN_BATCH_SIZE = 100;
    public static final int MAX_CHECK_BATCH_SIZE = 10;
    public static final int MAX_CANCEL_BATCH_SIZE = 100;

    private final WebServiceTemplate client;
    private final RequestMapper reqMapper;
    private final ResponseMapper resMapper;
    private final TypesMapper typesMapper;

    public ComunicacionRequest prepareSendBooking(@NonNull Collection<@NonNull Booking> bookings) {
        this.checkBatchSize(bookings, MAX_BOOKING_BATCH_SIZE);

        return reqMapper.toSubmitCommunicationRequest(
                RequestMapper.SesCommunicationType.BOOKING,
                typesMapper.toPeticionReserva(bookings)
        );
    }

    public ComunicacionRequest prepareSendCheckIn(@NonNull String accommodationSesCode, @NonNull Collection<@NonNull Booking> bookings) {
        this.checkBatchSize(bookings, MAX_CHECKIN_BATCH_SIZE);

        return reqMapper.toSubmitCommunicationRequest(
                RequestMapper.SesCommunicationType.CHECKIN,
                typesMapper.toPeticionAlta(accommodationSesCode, bookings)
        );
    }

    public ComunicacionRequest prepareSendCancellation(@NonNull Collection<@NonNull UUID> sesIds) {
        this.checkBatchSize(sesIds, MAX_CANCEL_BATCH_SIZE);

        return reqMapper.toCancelCommunicationRequest(
                typesMapper.toComunicacionAnulacion(sesIds)
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
}
