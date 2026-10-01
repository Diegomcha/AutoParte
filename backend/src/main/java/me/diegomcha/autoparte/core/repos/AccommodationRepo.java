package me.diegomcha.autoparte.core.repos;

import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.communication.Communication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface AccommodationRepo extends CrudRepository<Accommodation, UUID>, PagingAndSortingRepository<Accommodation, UUID> {

    boolean existsBySesCode(String sesCode);

    boolean existsByName(String name);

    Collection<Accommodation> findByBookingsCommunicationsTypeAndBookingsCommunicationsStatus(Communication.CommunicationType type, Communication.CommunicationStatus status);

    Page<Accommodation> findByEmployeesId(UUID employeeId, Pageable pageable);

    boolean existsByIdAndEmployeesAccountUsername(UUID id, String username);

    // Override SQLSelect
    @Override
    @Query("SELECT a FROM Accommodation a WHERE a.id = :uuid")
    Optional<Accommodation> findById(UUID uuid);

    @Modifying
    @Query(
            value = """
                    DELETE FROM accommodation a
                    WHERE deleted_at IS NOT NULL
                    AND NOT EXISTS (
                        SELECT 1 FROM booking b WHERE b.accommodation_id = a.id
                    )
                    """,
            nativeQuery = true
    )
    void deleteSoftDeletedByBookingsEmpty();
}
