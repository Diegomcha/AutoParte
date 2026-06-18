package me.diegomcha.autoparte.core.config;

import me.diegomcha.autoparte.domain.Configuration;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

interface ConfigRepo extends ListCrudRepository<Configuration, UUID> {

}
