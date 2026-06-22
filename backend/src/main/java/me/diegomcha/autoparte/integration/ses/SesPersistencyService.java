package me.diegomcha.autoparte.integration.ses;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.CommunicationRepo;
import me.diegomcha.autoparte.domain.communication.Communication;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional(readOnly = true)
class SesPersistencyService {

    private final CommunicationRepo communicationRepo;
    private final AccommodationRepo accommodationRepo;

    public void updateCommunications(@NonNull Iterable<Communication> communications) {
        communicationRepo.saveAll(communications);
    }

    public Stream<List<Communication>> getBatchOfPendingCommunications(@NonNull Communication.CommunicationType type, int maxBatchSize) {
        return this.getBatchOfPendingCommunications(type, maxBatchSize, null);
    }

    public Stream<List<Communication>> getBatchOfPendingCommunicationsGroupedByAccommodation(@NonNull Communication.CommunicationType type, int maxBatchSize) {
        return accommodationRepo
                .findByBookingsCommunicationsTypeAndBookingsCommunicationsStatus(type, Communication.CommunicationStatus.PENDING)
                .stream()
                .flatMap(accommodation -> this.getBatchOfPendingCommunications(type, maxBatchSize, accommodation.getId()));
    }

    private Stream<List<Communication>> getBatchOfPendingCommunications(@NonNull Communication.CommunicationType type, int maxBatchSize, @Nullable UUID accommodationId) {
        // There is no need to change the page number since the elements are being updated to "SENT".
        UnaryOperator<Slice<Communication>> supplier = slice ->
                accommodationId != null
                        ? communicationRepo.findByTypeAndStatusAndBookingAccommodationId(type, Communication.CommunicationStatus.PENDING, accommodationId, Pageable.ofSize(maxBatchSize))
                        : communicationRepo.findByTypeAndStatus(type, Communication.CommunicationStatus.PENDING, Pageable.ofSize(maxBatchSize));

        return Stream
                .iterate(
                        supplier.apply(null),
                        slice -> !slice.isEmpty(),
                        supplier
                )
                .map(Slice::getContent)
                // Load the communications with their associated booking.people and booking.communications to avoid lazy loading issues when processing the batch.
                .map(comms -> comms.stream().map(Communication::getId).toList())
                .map(communicationRepo::findAllByIdIn);
    }
}
