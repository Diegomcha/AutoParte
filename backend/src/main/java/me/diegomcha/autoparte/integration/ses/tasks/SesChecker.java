package me.diegomcha.autoparte.integration.ses.tasks;

import me.diegomcha.autoparte.config.DynamicConfigService;
import me.diegomcha.autoparte.core.exception.BadConfigurationException;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.core.exception.ServiceValidationException;
import me.diegomcha.autoparte.domain.communication.CancellationCommunication;
import me.diegomcha.autoparte.domain.communication.Communication;
import me.diegomcha.autoparte.integration.ses.SesClient;
import me.diegomcha.autoparte.integration.ses.dto.BatchDto;
import org.apache.commons.lang3.function.TriConsumer;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
class SesChecker extends SesTask {

    protected SesChecker(DynamicConfigService dynamicConfigService, SesClient sesClient, SesPersistencyService persistencyService) {
        super(LoggerFactory.getLogger(SesChecker.class), dynamicConfigService, sesClient, persistencyService);
    }

    @Scheduled(cron = "0 0 1/2 * * *")
    void schedule() {
        try {
            if (!persistencyService.areSesCredentialsValid()) {
                logger.warn("SES credentials are not validated, skipping checking pending batches");
                return;
            }

            this.checkPendingRegistrationBatches();
            this.checkPendingCancellationBatches();
        } catch (Exception e) {
            this.handleException(e);
        }
    }

    private void checkPendingRegistrationBatches() throws BadConfigurationException, ServiceUnavailableException, ServiceValidationException {
        logger.debug("Checking pending registration batches in SES");

        this.batchCheckingPipeline(
                () -> persistencyService.getNextSentRegistrationBatch(SesClient.MAX_CHECK_BATCH_SIZE),
                (batchId, state, comms) -> {
                    if (state.status() == BatchDto.BatchDtoStatus.ERROR_FORMAT || state.status() == BatchDto.BatchDtoStatus.ERROR_UNKNOWN) {
                        // If the entire batch has failed, fail all the communications in the batch
                        comms.forEach(communication ->
                                communication.markFinishedFailed(state.status().name())
                        );
                    } else {
                        // Update the communications in the batch based on the batch state
                        state.communications().forEach(commState -> {
                            // Get the communication in the batch with the same order as the one in the response (comms is ordered by the order of the communications in the batch)
                            var communication = comms.get(commState.order() - 1); // Order is 1-based, list is 0-based

                            assert communication.getBatchOrder() == commState.order() : "Communication order mismatch, expected " + communication.getBatchOrder() + " but got " + commState.order();

                            // Update the communication status based on the batch state
                            if (commState.error() != null)
                                communication.markFinishedFailed(commState.error());
                            else if (commState.id() != null)
                                communication.markFinishedSuccessfully(commState.id());
                            else
                                throw new IllegalStateException("Communication with order " + commState.order() + " in batch " + batchId + " has no ID and no error, cannot determine status");
                        });
                    }
                    persistencyService.updateCommunications(comms);
                });

        logger.info("Finished checking pending registration batches in SES");
    }

    private void checkPendingCancellationBatches() throws BadConfigurationException, ServiceUnavailableException, ServiceValidationException {
        logger.debug("Checking pending cancellation batches in SES");

        this.batchCheckingPipeline(
                () -> persistencyService.getNextSentCancellationBatch(SesClient.MAX_CHECK_BATCH_SIZE),
                (batchId, state, comms) -> {
                    // Get the communications that were canceled in the batch, to update their status accordingly
                    // (Cancellation communication -> List of canceled communications)
                    var commsMap = comms.stream()
                            .collect(Collectors.toMap(
                                    c -> c,
                                    c -> c.getBooking().getCommunications().stream()
                                            .filter(c2 -> c2.getStatus() == Communication.CommunicationStatus.PENDING_VOIDED).toList()
                            ));

                    if (state.status() == BatchDto.BatchDtoStatus.ERROR_FORMAT || state.status() == BatchDto.BatchDtoStatus.ERROR_UNKNOWN) {
                        // If the entire batch has failed, fail all the communications in the batch
                        commsMap.forEach((cancellationComm, canceledComms) -> {
                            cancellationComm.markFinishedFailed(state.status().name());
                            canceledComms.forEach(Communication::revertFromPendingVoided);
                        });
                    } else {
                        // Success, mark all communications in the batch as voided
                        assert state.status() != BatchDto.BatchDtoStatus.ERROR_COMMUNICATIONS;

                        commsMap.forEach((cancellationComm, canceledComms) -> {
                            ((CancellationCommunication) cancellationComm).markFinishedSuccessfully();
                            canceledComms.forEach(Communication::markVoided);
                        });
                    }

                    // Update the communications in the database
                    persistencyService.updateCommunications(
                            Stream.concat(
                                    commsMap.keySet().stream(),
                                    commsMap.values().stream().flatMap(List::stream)
                            ).toList()
                    );
                });

        logger.info("Finished checking pending cancellation batches in SES");
    }

    private void batchCheckingPipeline(Supplier<Map<UUID, List<Communication>>> getNextBatch, TriConsumer<UUID, BatchDto, List<Communication>> batchStateProcessor) throws BadConfigurationException, ServiceUnavailableException, ServiceValidationException {
        // Filter out batches that are still processing, to avoid checking them again until they are finished
        var stillProcessingBatches = new HashSet<UUID>();
        Supplier<Map<UUID, List<Communication>>> getNextBatchWithProcessingFilter = () -> {
            var batch = getNextBatch.get();
            stillProcessingBatches.forEach(batch::remove);
            return batch;
        };

        Map<UUID, List<Communication>> batch;
        while (!(batch = getNextBatchWithProcessingFilter.get()).isEmpty()) {
            logger.trace("Checking {} batches in SES", batch.size());

            var response = sesClient.checkBatches(batch.keySet());
            logger.trace("Received {} batches status", batch.size());

            // For each batch status
            for (var entry : response.entrySet()) {
                var batchId = entry.getKey();
                var state = entry.getValue();
                var comms = batch.get(batchId);

                // Log the batch status
                switch (state.status()) {
                    // If the batch is still pending or processing, log it and skip processing it for now
                    case PENDING, PROCESSING -> {
                        logger.trace("Batch with ID {} is still pending or processing, skipping", batchId);
                        stillProcessingBatches.add(batchId);
                        continue;
                    }
                    case ERROR_FORMAT, ERROR_UNKNOWN ->
                            logger.error("Batch with ID {} has failed with error: [{}] {}", batchId, state.status().name(), state.message());
                    case ERROR_COMMUNICATIONS ->
                            logger.warn("Batch with ID {} has partially failed with error: [{}] {}", batchId, state.status().name(), state.message());
                    case SUCCESS ->
                            logger.trace("Batch with ID {} has succeeded", batchId);
                    default ->
                            throw new IllegalStateException("Unexpected batch status: " + state.status());
                }

                // Process the batch status and update the communications accordingly
                batchStateProcessor.accept(batchId, state, comms);
            }
        }
    }
}
