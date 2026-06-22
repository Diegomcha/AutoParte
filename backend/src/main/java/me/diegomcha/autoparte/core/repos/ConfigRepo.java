package me.diegomcha.autoparte.core.repos;

import me.diegomcha.autoparte.domain.Configuration;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface ConfigRepo extends ListCrudRepository<Configuration, UUID> {

}
