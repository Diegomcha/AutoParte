package me.diegomcha.autoparte.core.repos;

import me.diegomcha.autoparte.domain.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookingRepo extends CrudRepository<Booking, UUID>, PagingAndSortingRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {

    Page<Booking> findByAccommodationId(UUID accommodationId, Pageable pageable, Specification<Booking> spec);

    Optional<Booking> findByAccommodationIdAndId(UUID accommodationId, UUID id);

    boolean existsByAccommodationIdAndId(UUID accommodationId, UUID id);

    boolean existsByIdAndSelfCheckInRequestedTrue(UUID id);
}
