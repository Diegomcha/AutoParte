package me.diegomcha.autoparte.core.repos;

import me.diegomcha.autoparte.domain.communication.Communication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.UUID;

public interface CommunicationRepo extends ListCrudRepository<Communication, UUID>, PagingAndSortingRepository<Communication, UUID> {

    Page<Communication> findByBookingAccommodationIdOrderById(UUID accommodationId, Pageable pageable);

    List<Communication> findByBookingIdOrderById(UUID bookingId);

    Slice<Communication> findByTypeAndStatus(Communication.CommunicationType type, Communication.CommunicationStatus status, Pageable pageable);

    Slice<Communication> findByTypeAndStatusAndBookingAccommodationId(Communication.CommunicationType type, Communication.CommunicationStatus status, UUID accommodationId, Pageable pageable);

    @Query("""
        SELECT DISTINCT c.batchId
        FROM Communication c
        WHERE c.batchId IS NOT NULL
            AND c.status = SENT
            AND c.type <> CANCELLATION
    """)
    Slice<UUID> findDistinctBatchIdsFromSentRegistrations(Pageable pageable);

    @Query("""
        SELECT DISTINCT c.batchId
        FROM Communication c
        WHERE c.batchId IS NOT NULL
            AND c.status = SENT
            AND c.type = CANCELLATION
    """)
    Slice<UUID> findDistinctBatchIdsFromSentCancellations(Pageable pageable);

    List<Communication> findByBatchId(Sort sort, UUID batchId);
}
