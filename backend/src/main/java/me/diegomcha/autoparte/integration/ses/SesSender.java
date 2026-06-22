package me.diegomcha.autoparte.integration.ses;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.config.DynamicConfigService;
import me.diegomcha.autoparte.core.exception.BadConfigurationException;
import me.diegomcha.autoparte.core.exception.ExceptionWrapper;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.domain.communication.Communication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class SesSender {

    private final Logger logger = LoggerFactory.getLogger(SesSender.class);

    private final DynamicConfigService dynamicConfigService;
    private final SesPersistencyService persistencyService;
    private final SesAPI sesAPI;

    @Scheduled(cron = "0 0 */2 * * *")
    void sendBookings() {
        // Ensure SES credentials are valid
        if (!dynamicConfigService.getConfig().isSesCredentialsValid()) {
            logger.warn("SES credentials are not validated, skipping sending bookings");
            return;
        }

        logger.debug("Sending pending bookings to SES");

        try (var pendingCommunications = persistencyService.getBatchOfPendingCommunications(
                Communication.CommunicationType.BOOKING,
                SesAPI.MAX_BOOKING_BATCH_SIZE
        )) {
            pendingCommunications.forEach(batch -> {
                var request = sesAPI.prepareSendBooking(
                        batch.stream().map(Communication::getBooking).toList()
                );

                logger.trace("Sending batch of {} bookings to SES", batch.size());

                try {
                    var batchId = sesAPI.sendCommunication(request);
                    logger.trace("Batch of {} bookings sent to SES with batchId {}", batch.size(), batchId);

                    // Update the communications in the batch to mark them as sent
                    batch.forEach(communication -> communication.markSent(batchId));
                    persistencyService.updateCommunications(batch);
                } catch (ServiceUnavailableException |
                         BadConfigurationException e) {
                    throw new ExceptionWrapper(e);
                }
            });

            logger.info("Finished sending pending bookings to SES");
        } catch (ExceptionWrapper e) {
            this.handleExceptionWrapper(e);
        }
    }

    @Scheduled(cron = "0 15 */2 * * *")
    void sendCheckIns() {
        // Ensure SES credentials are valid
        if (!dynamicConfigService.getConfig().isSesCredentialsValid()) {
            logger.warn("SES credentials are not validated, skipping sending check-ins");
            return;
        }

        logger.debug("Sending pending check-ins to SES");

        try (var pendingCommunications = persistencyService.getBatchOfPendingCommunicationsGroupedByAccommodation(
                Communication.CommunicationType.CHECKIN,
                SesAPI.MAX_CHECKIN_BATCH_SIZE
        )) {
            pendingCommunications.forEach(batch -> {
                var request = sesAPI.prepareSendCheckIn(
                        batch.getFirst().getBooking().getAccommodation().getSesCode(), // All communications in the batch belong to the same accommodation
                        batch.stream().map(Communication::getBooking).toList()
                );

                logger.trace("Sending batch of {} check-ins to SES", batch.size());

                try {
                    var batchId = sesAPI.sendCommunication(request);
                    logger.trace("Batch of {} check-ins sent to SES with batchId {}", batch.size(), batchId);

                    // Update the communications in the batch to mark them as sent
                    batch.forEach(communication -> communication.markSent(batchId));
                    persistencyService.updateCommunications(batch);
                } catch (ServiceUnavailableException |
                         BadConfigurationException e) {
                    throw new ExceptionWrapper(e);
                }
            });

            logger.info("Finished sending pending check-ins to SES");
        } catch (ExceptionWrapper e) {
            this.handleExceptionWrapper(e);
        }
    }

    @Scheduled(cron = "0 30 */2 * * *")
    void sendCancellations() {
        // Ensure SES credentials are valid
        if (!dynamicConfigService.getConfig().isSesCredentialsValid()) {
            logger.warn("SES credentials are not validated, skipping sending cancellations");
            return;
        }

        logger.debug("Sending pending cancellations to SES");

        try (var pendingCommunications = persistencyService.getBatchOfPendingCommunications(
                Communication.CommunicationType.CANCELLATION,
                SesAPI.MAX_CANCEL_BATCH_SIZE
        )) {
            pendingCommunications.forEach(batch -> {
                var commsToCancel = batch.stream()
                        .flatMap(cancellation -> cancellation.getBooking().getCommunications().stream())
                        .filter(c -> c.getType() != Communication.CommunicationType.CANCELLATION && c.getStatus() == Communication.CommunicationStatus.SUCCEEDED)
                        .toList();

                var request = sesAPI.prepareSendCancellation(
                        commsToCancel.stream().map(Communication::getSesId).toList()
                );

                logger.trace("Sending batch of {} cancellations to SES ({} communications to cancel)", batch.size(), commsToCancel.size());

                try {
                    var batchId = sesAPI.sendCommunication(request);
                    logger.trace("Batch of {} cancellations sent to SES with batchId {}", batch.size(), batchId);

                    // Update the communications in the batch to mark them as sent & the communications to cancel to mark them as pending voided
                    batch.forEach(communication -> communication.markSent(batchId));
                    commsToCancel.forEach(Communication::markPendingVoided);
                    persistencyService.updateCommunications(Stream.concat(batch.stream(), commsToCancel.stream()).toList());
                } catch (ServiceUnavailableException |
                         BadConfigurationException e) {
                    throw new ExceptionWrapper(e);
                }
            });

            logger.info("Finished sending pending cancellations to SES");
        } catch (ExceptionWrapper e) {
            this.handleExceptionWrapper(e);
        }
    }

    private void handleExceptionWrapper(ExceptionWrapper e) {
        var cause = e.getCause();

        switch (e.getCause()) {
            case ServiceUnavailableException ignored:
                logger.warn("SES service unavailable, will retry later");
                break;
            case BadConfigurationException bce:
                logger.error("Bad configuration for SES: {}", bce.getMessage());
                dynamicConfigService.updateConfig(config -> config.setSesCredentialsValid(false));
                break;
            default:
                logger.error("Unexpected error while sending communications to SES", cause);
                throw new RuntimeException(cause);
        }
    }
}
