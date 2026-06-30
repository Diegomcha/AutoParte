package me.diegomcha.autoparte.core.repos;

import lombok.NonNull;
import me.diegomcha.autoparte.domain.communication.Communication;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CommunicationRepo extends ListCrudRepository<Communication, UUID> {

    Slice<Communication> findByTypeAndStatus(Communication.CommunicationType type, Communication.CommunicationStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"booking.people", "booking.communications"})
    List<Communication> findAllByIdIn(Collection<@NonNull UUID> id);

    Slice<Communication> findByTypeAndStatusAndBookingAccommodationId(Communication.CommunicationType type, Communication.CommunicationStatus status, UUID accommodationId, Pageable pageable);

}
