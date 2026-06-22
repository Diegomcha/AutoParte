package me.diegomcha.autoparte.core.repos;

import me.diegomcha.autoparte.domain.SecurityEvent;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SecurityEventRepo extends CrudRepository<SecurityEvent, UUID> {
}
