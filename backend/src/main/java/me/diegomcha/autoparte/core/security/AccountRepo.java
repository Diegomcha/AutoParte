package me.diegomcha.autoparte.core.security;

import me.diegomcha.autoparte.domain.Account;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepo extends CrudRepository<Account, UUID> {
    boolean existsByUsername(String username);
    Optional<Account> findByUsername(String username);
}
