package me.diegomcha.autoparte.core.repos;

import me.diegomcha.autoparte.domain.Account;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepo extends CrudRepository<Account, UUID>, PagingAndSortingRepository<Account, UUID> {
    
    Optional<Account> findByUsername(String username);

    boolean existsByUsername(String username);
}
