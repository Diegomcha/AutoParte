package me.diegomcha.autoparte.security;

import me.diegomcha.autoparte.model.Account;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface AccountRepo extends CrudRepository<Account, UUID> {
}
