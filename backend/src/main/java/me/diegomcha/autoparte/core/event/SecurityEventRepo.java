package me.diegomcha.autoparte.core.event;

import me.diegomcha.autoparte.domain.SecurityEvent;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

interface SecurityEventRepo extends CrudRepository<SecurityEvent, UUID> {
}
