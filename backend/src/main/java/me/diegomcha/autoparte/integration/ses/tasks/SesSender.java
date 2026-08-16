package me.diegomcha.autoparte.integration.ses.tasks;

import me.diegomcha.autoparte.config.DynamicConfigService;
import me.diegomcha.autoparte.core.exception.BadConfigurationException;
import me.diegomcha.autoparte.core.exception.ExceptionWrapper;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.domain.communication.CancellationCommunication;
import me.diegomcha.autoparte.domain.communication.Communication;
import me.diegomcha.autoparte.integration.ses.SesClient;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

// TODO: Test this class
@Component
class SesSender extends SesTask {

    protected SesSender(DynamicConfigService dynamicConfigService, SesClient sesClient, SesPersistencyService persistencyService) {
        super(LoggerFactory.getLogger(SesSender.class), dynamicConfigService, sesClient, persistencyService);
    }

    @Scheduled(cron = "0 0 */2 * * *")
    void schedule() {
        try {
            if (!this.credentialsAreValid()) {
                logger.warn("SES credentials are not validated, skipping sending pending communications");
                return;
            }

            this.sendBookings();
            this.sendCheckIns();
            this.sendCancellations();
        } catch (Exception e) {
            this.handleException(e);
        }
    }

    private void sendBookings() {
        logger.debug("Sending pending bookings to SES");

        try (var pendingCommunications = persistencyService.getBatchOfPendingCommunications(
                Communication.CommunicationType.BOOKING,
                SesClient.MAX_BOOKING_BATCH_SIZE
        )) {
            pendingCommunications.forEach(batch -> {
                var request = sesClient.prepareSendBooking(
                        batch.stream().map(Communication::getBooking).toList()
                );

                logger.trace("Sending batch of {} bookings to SES", batch.size());

                try {
                    var batchId = sesClient.sendCommunication(request);
                    logger.trace("Batch of {} bookings sent to SES with batchId {}", batch.size(), batchId);

                    // Update the communications in the batch to mark them as sent
                    batch.forEach(withCounter((counter, communication) -> communication.markSent(batchId, counter)));
                    persistencyService.updateCommunications(batch);
                } catch (ServiceUnavailableException |
                         BadConfigurationException e) {
                    throw new ExceptionWrapper(e);
                }
            });

            logger.info("Finished sending pending bookings to SES");
        }
    }

    private void sendCheckIns() {
        logger.debug("Sending pending check-ins to SES");

        try (var pendingCommunications = persistencyService.getBatchOfPendingCommunicationsGroupedByAccommodation(
                Communication.CommunicationType.CHECKIN,
                SesClient.MAX_CHECKIN_BATCH_SIZE
        )) {
            pendingCommunications.forEach(batch -> {
                var request = sesClient.prepareSendCheckIn(
                        batch.getFirst().getBooking().getAccommodation().getSesCode(), // All communications in the batch belong to the same accommodation
                        batch.stream().map(Communication::getBooking).toList()
                );

                logger.trace("Sending batch of {} check-ins to SES", batch.size());

                try {
                    var batchId = sesClient.sendCommunication(request);
                    logger.trace("Batch of {} check-ins sent to SES with batchId {}", batch.size(), batchId);

                    // Update the communications in the batch to mark them as sent
                    batch.forEach(withCounter((counter, communication) -> communication.markSent(batchId, counter)));
                    persistencyService.updateCommunications(batch);
                } catch (ServiceUnavailableException |
                         BadConfigurationException e) {
                    throw new ExceptionWrapper(e);
                }
            });

            logger.info("Finished sending pending check-ins to SES");
        }
    }

    private void sendCancellations() {
        logger.debug("Sending pending cancellations to SES");

        try (var pendingCommunications = persistencyService.getBatchOfPendingCommunications(
                Communication.CommunicationType.CANCELLATION,
                SesClient.MAX_CANCEL_BATCH_SIZE / 2 // Each cancellation may have up to 2 successful communications which need to be canceled
        )) {
            pendingCommunications
                    // Ensure there are no pending communications for the booking
                    .map(batch ->
                            batch.stream().filter(cancellation ->
                                    cancellation.getBooking().getCommunications().stream().noneMatch(c -> c.getStatus() == Communication.CommunicationStatus.SENT)).toList())
                    .takeWhile(batch -> !batch.isEmpty())
                    .forEach(batch -> { // TODO: Not sure if this works...
                        var commsToCancel = batch.stream()
                                .flatMap(cancellation -> cancellation.getBooking().getCommunications().stream())
                                .filter(c -> c.getType() != Communication.CommunicationType.CANCELLATION && c.getStatus() == Communication.CommunicationStatus.SUCCEEDED)
                                .toList();

                        // If there are no communications to cancel, mark the cancellations as finished successfully and update them in the database
                        if (commsToCancel.isEmpty()) {
                            logger.trace("Batch of {} cancellations has no communications to cancel, succeeding immediately", batch.size());

                            batch.forEach(withCounter((counter, communication) -> ((CancellationCommunication) communication).markFinishedSuccessfully()));
                            persistencyService.updateCommunications(batch);
                            return;
                        }

                        var request = sesClient.prepareSendCancellation(
                                commsToCancel.stream().map(Communication::getSesId).toList()
                        );

                        logger.trace("Sending batch of {} cancellations to SES ({} communications to cancel)", batch.size(), commsToCancel.size());

                        try {
                            var batchId = sesClient.sendCommunication(request);
                            logger.trace("Batch of {} cancellations sent to SES with batchId {}", batch.size(), batchId);

                            // Update the communications in the batch to mark them as sent & the communications to cancel to mark them as pending voided
                            batch.forEach(withCounter((counter, communication) -> communication.markSent(batchId, counter)));
                            commsToCancel.forEach(Communication::markPendingVoided);
                            persistencyService.updateCommunications(Stream.concat(batch.stream(), commsToCancel.stream()).toList());
                        } catch (ServiceUnavailableException |
                                 BadConfigurationException e) {
                            throw new ExceptionWrapper(e);
                        }
                    });

            logger.info("Finished sending pending cancellations to SES");
        }
    }

    private <T> Consumer<T> withCounter(BiConsumer<Integer, T> consumer) {
        AtomicInteger counter = new AtomicInteger(1); // Batch order starts at 1
        return item -> consumer.accept(counter.getAndIncrement(), item);
    }
}
