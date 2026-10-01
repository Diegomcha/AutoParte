package me.diegomcha.autoparte.core.repos;

import me.diegomcha.autoparte.domain.SecurityEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.Instant;
import java.util.UUID;

public interface SecurityEventRepo extends CrudRepository<SecurityEvent, UUID>, PagingAndSortingRepository<SecurityEvent, UUID> {
    Page<SecurityEvent> findByAccountId(UUID accountId, Pageable pageable);

    void deleteByTimestampBefore(Instant cutoffDate);
}
