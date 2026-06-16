package me.diegomcha.autoparte.integration.ses;

import me.diegomcha.autoparte.domain.communication.Communication;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

interface CommunicationRepo extends CrudRepository<Communication, UUID> {

    @EntityGraph(attributePaths = {"booking", "booking.people", "booking.communications"})
    Slice<Communication> findByTypeAndStatus(Communication.CommunicationType type, Communication.CommunicationStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"booking", "booking.people", "booking.communications"})
    Slice<Communication> findByTypeAndStatusAndBookingAccommodationId(Communication.CommunicationType type, Communication.CommunicationStatus status, UUID accommodationId, Pageable pageable);

}
