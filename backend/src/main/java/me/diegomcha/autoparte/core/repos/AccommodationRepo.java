package me.diegomcha.autoparte.core.repos;

import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.communication.Communication;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.UUID;

public interface AccommodationRepo extends CrudRepository<Accommodation, UUID>, PagingAndSortingRepository<Accommodation, UUID> {

    boolean existsBySesCode(String sesCode);

    boolean existsByName(String name);

    Collection<Accommodation> findByBookingsCommunicationsTypeAndBookingsCommunicationsStatus(Communication.CommunicationType type, Communication.CommunicationStatus status);
    
}
