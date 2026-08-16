package me.diegomcha.autoparte.core.repos;

import me.diegomcha.autoparte.domain.address.Address;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface AddressRepo extends CrudRepository<Address, UUID> {

    @Query("""
        SELECT DISTINCT a
        FROM Address a
            JOIN a.people p
        WHERE p.booking.id = :bookingId
    """)
    List<Address> findByBooking(UUID bookingId);
}
