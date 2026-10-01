package me.diegomcha.autoparte.core.repos;

import me.diegomcha.autoparte.domain.Account;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepo extends CrudRepository<Account, UUID>, PagingAndSortingRepository<Account, UUID> {
    
    Optional<Account> findByUsername(String username);

    boolean existsByUsername(String username);

    // Override SQLSelect
    @Override
    @Query("SELECT a FROM Account a WHERE a.id = :uuid")
    Optional<Account> findById(UUID uuid);

    @Modifying
    @Query(
            value = """
                    DELETE FROM account a
                    WHERE deleted_at IS NOT NULL
                    AND NOT EXISTS (
                        SELECT 1 FROM security_event se WHERE se.account_id = a.id
                    )
                    """,
            nativeQuery = true
    )
    void deleteSoftDeletedBySecurityLogEmpty();
}
