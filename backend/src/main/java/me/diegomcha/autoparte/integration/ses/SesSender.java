package me.diegomcha.autoparte.integration.ses;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.accommodation.AccommodationRepo;
import me.diegomcha.autoparte.core.exception.BadConfigurationException;
import me.diegomcha.autoparte.core.exception.ExceptionWrapper;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.domain.communication.Communication;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class SesSender {

    private final Logger logger = LoggerFactory.getLogger(SesSender.class);

    private final CommunicationRepo communicationRepo;
    private final AccommodationRepo accommodationRepo;
    private final SesAPI sesAPI;

    @Scheduled(cron = "0 0 */2 * * *")
    void sendBookings() {
        logger.info("Sending pending bookings to SES");

        try (var pendingCommunications = this.getPendingCommunications(
                Communication.CommunicationType.BOOKING,
                SesAPI.MAX_BOOKING_BATCH_SIZE,
                null
        )) {
            pendingCommunications.forEach(communications -> {
                logger.debug("Sending batch of {} bookings to SES", communications.size());

                try {
                    var batchId = sesAPI.sendCommunication(sesAPI.prepareSendBooking(communications.stream().map(Communication::getBooking).toList()));
                    logger.debug("Batch of {} bookings sent to SES with batchId {}", communications.size(), batchId);

                    communications.forEach(communication -> communication.markSent(batchId));
                    communicationRepo.saveAll(communications);
                } catch (ServiceUnavailableException |
                         BadConfigurationException e) {
                    throw new ExceptionWrapper(e);
                }
            });

            logger.debug("Finished sending pending bookings to SES");
        } catch (ExceptionWrapper e) {
            this.handleExceptionWrapper(e);
        }
    }

    @Scheduled(cron = "0 15 */2 * * *")
    void sendCheckIns() {
        logger.info("Sending pending check-ins to SES");

        try {
            for (var accommodation : accommodationRepo.findByBookingsCommunicationsTypeAndBookingsCommunicationsStatus(
                    Communication.CommunicationType.CHECKIN,
                    Communication.CommunicationStatus.PENDING
            )) {
                try (var pendingCommunications = this.getPendingCommunications(
                        Communication.CommunicationType.CHECKIN,
                        SesAPI.MAX_CHECKIN_BATCH_SIZE,
                        accommodation.getId()
                )) {
                    pendingCommunications.forEach(communications -> {
                        logger.debug("Sending batch of {} check-ins to SES", communications.size());

                        try {
                            var batchId = sesAPI.sendCommunication(sesAPI.prepareSendCheckIn(accommodation.getSesCode(), communications.stream().map(Communication::getBooking).toList()));
                            logger.debug("Batch of {} check-ins sent to SES with batchId {}", communications.size(), batchId);

                            communications.forEach(communication -> communication.markSent(batchId));
                            communicationRepo.saveAll(communications);
                        } catch (ServiceUnavailableException |
                                 BadConfigurationException e) {
                            throw new ExceptionWrapper(e);
                        }
                    });
                }
            }
            logger.debug("Finished sending pending check-ins to SES");
        } catch (ExceptionWrapper e) {
            this.handleExceptionWrapper(e);
        }
    }

    @Scheduled(cron = "0 30 */2 * * *")
    void sendCancellations() {
        logger.info("Sending pending cancellations to SES");

        try (var pendingCommunications = this.getPendingCommunications(
                Communication.CommunicationType.CANCELLATION,
                SesAPI.MAX_CANCEL_BATCH_SIZE / 2, // At most 2 communications will be voided per cancellation communication
                null
        )) {
            pendingCommunications.forEach(cancellations -> {
                var cToCancel = cancellations.stream()
                        .flatMap(cancellation -> cancellation.getBooking().getCommunications().stream())
                        .filter(c -> c.getType() != Communication.CommunicationType.CANCELLATION && c.getStatus() == Communication.CommunicationStatus.SUCCEEDED)
                        .toList();

                logger.debug("Sending batch of {} cancellations to SES ({} communications to cancel)", cancellations.size(), cToCancel.size());

                try {
                    var batchId = sesAPI.sendCommunication(sesAPI.prepareSendCancellation(cToCancel.stream().map(Communication::getSesId).toList()));
                    logger.debug("Batch of {} cancellations sent to SES with batchId {}", cancellations.size(), batchId);

                    cancellations.forEach(cancellation -> cancellation.markSent(batchId));
                    cToCancel.forEach(Communication::markPendingVoided);
                    communicationRepo.saveAll(Stream.concat(cancellations.stream(), cToCancel.stream()).toList());
                } catch (ServiceUnavailableException |
                         BadConfigurationException e) {
                    throw new ExceptionWrapper(e);
                }
            });

            logger.debug("Finished sending pending cancellations to SES");
        } catch (ExceptionWrapper e) {
            this.handleExceptionWrapper(e);
        }
    }

    private Stream<List<Communication>> getPendingCommunications(@NonNull Communication.CommunicationType type, int maxBatchSize, @Nullable UUID accommodationId) {
        // There is no need to change the page number since the elements are being updated to "SENT".
        UnaryOperator<Slice<Communication>> supplier = slice ->
                accommodationId != null
                        ? communicationRepo.findByTypeAndStatusAndBookingAccommodationId(type, Communication.CommunicationStatus.PENDING, accommodationId, Pageable.ofSize(maxBatchSize))
                        : communicationRepo.findByTypeAndStatus(type, Communication.CommunicationStatus.PENDING, Pageable.ofSize(maxBatchSize));

        return Stream.iterate(
                supplier.apply(null),
                slice -> !slice.isEmpty(),
                supplier
        ).map(Slice::getContent);
    }

    private void handleExceptionWrapper(ExceptionWrapper e) {
        var cause = e.getCause();

        switch (e.getCause()) {
            case ServiceUnavailableException ignored:
                logger.warn("SES service unavailable, will retry later");
                break;
            // TODO: This should be notified to the administrator
            case BadConfigurationException bce:
                logger.error("Bad configuration for SES: {}", bce.getMessage());
                break;
            default:
                logger.error("Unexpected error while sending communications to SES", cause);
                throw new RuntimeException(cause);
        }
    }
}
