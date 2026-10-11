package me.diegomcha.autoparte.core.repos;

import me.diegomcha.autoparte.domain.Booking;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepo extends CrudRepository<Booking, UUID>, PagingAndSortingRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {

    Optional<Booking> findByAccommodationIdAndId(UUID accommodationId, UUID id);

    boolean existsByAccommodationIdAndId(UUID accommodationId, UUID id);

    boolean existsByIdAndSelfCheckInRequestedTrue(UUID id);

    void deleteByEndTimeBefore(Instant cutoffDate);

}
