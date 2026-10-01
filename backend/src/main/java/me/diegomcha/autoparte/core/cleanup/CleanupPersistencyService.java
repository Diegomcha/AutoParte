package me.diegomcha.autoparte.core.cleanup;

import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.config.DynamicConfigService;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.AccountRepo;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import me.diegomcha.autoparte.core.repos.SecurityEventRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Transactional
class CleanupPersistencyService {

    private final Logger logger = LoggerFactory.getLogger(CleanupPersistencyService.class);

    private final DynamicConfigService dynamicConfigService;

    private final SecurityEventRepo securityEventRepo;
    private final AccountRepo accountRepo;
    private final AccommodationRepo accommodationRepo;
    private final BookingRepo bookingRepo;

    // * Cleanup methods: These methods clean up old records based on the configured retention periods.

    /**
     * Cleans up security events that are older than the configured retention period.
     */
    public void cleanupSecurityEvents() {
        logger.trace("Cleaning up security events older than the configured retention period");

        var retentionDays = dynamicConfigService.getConfig().getLogsRetentionDays();
        if (retentionDays == 0) {
            logger.trace("Logs retention period is disabled (0 days), skipping cleanup of security events");
            return;
        }

        var cutoffDate = this.getCutoffDate(retentionDays);
        logger.trace("Deleting security events older than {}", cutoffDate);
        securityEventRepo.deleteByTimestampBefore(cutoffDate);

        logger.trace("Finished cleanup of security events");
    }

    /**
     * Cleans up bookings that are older than the configured retention period.
     */
    public void cleanupBookings() {
        logger.trace("Cleaning up bookings older than the configured retention period");

        var retentionDays = dynamicConfigService.getConfig().getBookingsRetentionDays();
        if (retentionDays == 0) {
            logger.trace("Bookings retention period is disabled (0 days), skipping cleanup of bookings");
            return;
        }

        var cutoffDate = this.getCutoffDate(retentionDays);
        logger.trace("Deleting bookings older than {}", cutoffDate);
        bookingRepo.deleteByEndTimeBefore(cutoffDate);

        logger.trace("Finished cleanup of bookings");
    }

    // * Soft delete cleanup methods: These methods clean up soft deleted entities that have no associated records.

    /**
     * Cleans up accounts that are soft deleted and have no associated security events.
     */
    public void cleanupAccounts() {
        logger.trace("Cleaning up soft deleted accounts with no associated security events");
        accountRepo.deleteSoftDeletedBySecurityLogEmpty();
        logger.trace("Finished cleanup of soft deleted accounts");
    }

    /**
     * Cleans up accommodations that are soft deleted and have no associated bookings.
     */
    public void cleanupAccommodations() {
        logger.trace("Cleaning up soft deleted accommodations with no associated bookings");
        accommodationRepo.deleteSoftDeletedByBookingsEmpty();
        logger.trace("Finished cleanup of soft deleted accommodations");
    }

    private Instant getCutoffDate(int retentionDays) {
        return Instant.now().minus(retentionDays, ChronoUnit.DAYS);
    }
}
