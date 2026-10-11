package me.diegomcha.autoparte.integration.ses.tasks;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.config.DynamicConfigService;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.CommunicationRepo;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.communication.Communication;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

// TODO: Test this service
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional(readOnly = true)
class SesPersistencyService {

    private final CommunicationRepo communicationRepo;
    private final AccommodationRepo accommodationRepo;
    private final DynamicConfigService dynamicConfigService;

    /**
     * Updates the given communications in the database.
     *
     * @param communications the communications to update
     */
    @Transactional
    public void updateCommunications(@NonNull Iterable<Communication> communications) {
        communicationRepo.saveAll(communications);
    }

    /**
     * Returns the next batch of pending communications of the given type, up to the given maximum batch size.
     *
     * @param type         the type of communications to retrieve
     * @param maxBatchSize the maximum number of communications to retrieve
     * @return the next batch of pending communications of the given type, up to the given maximum batch size
     */
    public List<Communication> getNextBatchOfPendingCommunications(@NonNull Communication.CommunicationType type, int maxBatchSize) {
        return this.getNextBatchOfPendingCommunications(type, maxBatchSize, null);
    }

    /**
     * Returns the next batch of pending communications of the given type, up to the given maximum batch size, grouped by accommodation.
     *
     * @param type         the type of communications to retrieve
     * @param maxBatchSize the maximum number of communications to retrieve
     * @return the next batch of pending communications of the given type, up to the given maximum batch size, grouped by accommodation
     */
    public List<Communication> getNextBatchOfPendingCommunicationsGroupedByAccommodation(@NonNull Communication.CommunicationType type, int maxBatchSize) {
        Accommodation accommodationWithPendingComms = accommodationRepo.findFirstByBookingsCommunicationsTypeAndBookingsCommunicationsStatus(type, Communication.CommunicationStatus.PENDING);

        if (accommodationWithPendingComms == null)
            return List.of();

        return this.getNextBatchOfPendingCommunications(
                type,
                maxBatchSize,
                accommodationWithPendingComms.getId()
        );
    }

    private List<Communication> getNextBatchOfPendingCommunications(@NonNull Communication.CommunicationType type, int maxBatchSize, @Nullable UUID accommodationId) {
        // There is no need to change the page number since the elements are being updated to "SENT".
        Slice<Communication> slice = accommodationId != null
                ? communicationRepo.findByTypeAndStatusAndBookingAccommodationId(type, Communication.CommunicationStatus.PENDING, accommodationId, Pageable.ofSize(maxBatchSize))
                : communicationRepo.findByTypeAndStatus(type, Communication.CommunicationStatus.PENDING, Pageable.ofSize(maxBatchSize));

        // Load the communications with their associated booking.people and booking.communications to avoid lazy loading issues when processing the batch.
        slice.forEach(c -> c.getBooking().getPeople().size());
        slice.forEach(c -> c.getBooking().getCommunications().size());

        return slice.getContent();
    }

    /**
     * Returns the next batch of sent registration communications, grouped by batch ID, up to the given maximum batch size.
     *
     * @param maxBatchSize the maximum number of batch IDs to retrieve
     * @return a map of batch IDs to lists of sent registration communications
     */
    public Map<UUID, List<Communication>> getNextSentRegistrationBatch(int maxBatchSize) {
        return this.getNextSentBatch(maxBatchSize, false);
    }

    /**
     * Returns the next batch of sent cancellation communications, grouped by batch ID, up to the given maximum batch size.
     *
     * @param maxBatchSize the maximum number of batch IDs to retrieve
     * @return a map of batch IDs to lists of sent cancellation communications
     */
    public Map<UUID, List<Communication>> getNextSentCancellationBatch(int maxBatchSize) {
        return this.getNextSentBatch(maxBatchSize, true);
    }

    private Map<UUID, List<Communication>> getNextSentBatch(int maxBatchSize, boolean isCancellation) {
        Slice<UUID> batchIdsSlice = isCancellation
                ? communicationRepo.findDistinctBatchIdsFromSentCancellations(Pageable.ofSize(maxBatchSize))
                : communicationRepo.findDistinctBatchIdsFromSentRegistrations(Pageable.ofSize(maxBatchSize));

        return batchIdsSlice.getContent().stream()
                .collect(Collectors.toMap(
                        batchId -> batchId,
                        batchId -> {
                            var comms = communicationRepo.findByBatchId(Sort.by("batchOrder"), batchId);

                            // Load the communications with their associated booking.communications to avoid lazy loading issues when processing the batch.
                            comms.forEach(c -> c.getBooking().getCommunications().size());

                            return comms;
                        }
                ));
    }

    /**
     * Checks if the SES credentials are valid by retrieving the configuration from the dynamic config service.
     *
     * @return true if the SES credentials are valid, false otherwise
     */
    public boolean areSesCredentialsValid() {
        return dynamicConfigService.getConfig().isSesCredentialsValid();
    }
}
