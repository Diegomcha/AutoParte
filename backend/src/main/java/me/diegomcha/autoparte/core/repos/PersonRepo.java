package me.diegomcha.autoparte.core.repos;

import me.diegomcha.autoparte.domain.Person;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonRepo extends CrudRepository<Person, UUID> {
    List<Person> findByBookingAccommodationIdAndBookingId(UUID accommodationId, UUID bookingId);
    Optional<Person> findByBookingAccommodationIdAndBookingIdAndId(UUID accommodationId, UUID bookingId, UUID id);
}
