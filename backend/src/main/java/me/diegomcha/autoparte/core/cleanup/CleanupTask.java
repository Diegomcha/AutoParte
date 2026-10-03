package me.diegomcha.autoparte.core.cleanup;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// TODO: Test
@Component
@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
class CleanupTask {

    protected final Logger logger = LoggerFactory.getLogger(CleanupTask.class);

    private final CleanupPersistencyService persistencyService;

    @Scheduled(cron = "0 0 */6 * * *")
    public void cleanupAll() {
        // Enforce retention policies for security events and bookings
        logger.debug("Starting cleanup of old records based on retention policies");
        persistencyService.cleanupSecurityEvents();
        persistencyService.cleanupBookings();
        logger.debug("Finished cleanup of old records based on retention policies");

        // Clean up soft deleted accounts and accommodations with no associated records
        logger.debug("Starting cleanup of soft deleted entities with no associated records");
        persistencyService.cleanupAccounts();
        persistencyService.cleanupAccommodations();
        logger.debug("Finished cleanup of soft deleted entities with no associated records");

        // Clean up orphaned records in the database
        logger.debug("Starting cleanup of orphaned records in the database");
        persistencyService.cleanupOrphanAddresses();
        logger.debug("Finished cleanup of orphaned records in the database");

        logger.info("Database cleanup completed successfully");
    }

}
