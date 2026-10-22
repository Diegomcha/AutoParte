package me.diegomcha.autoparte.core.cleanup;

import jakarta.persistence.EntityManager;
import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.config.DynamicConfigService;
import me.diegomcha.autoparte.core.repos.*;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.SecurityEvent;
import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.booking.payment.Payment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class CleanupTaskTest {

    @Autowired
    private CleanupTask cleanupTask;
    @MockitoSpyBean
    private CleanupPersistencyService persistencyService;
    @Autowired
    private DynamicConfigService dynamicConfigService;

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private SecurityEventRepo securityEventRepo;
    @Autowired
    private AccommodationRepo accommodationRepo;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private AddressRepo addressRepo;
    @Autowired
    private AccountRepo accountRepo;

    @Test
    void cleanupAllRunsEveryCleanupOperationInOrder() {
        cleanupTask.cleanupAll();

        InOrder inOrder = Mockito.inOrder(persistencyService);
        inOrder.verify(persistencyService).cleanupSecurityEvents();
        inOrder.verify(persistencyService).cleanupBookings();
        inOrder.verify(persistencyService).cleanupAccounts();
        inOrder.verify(persistencyService).cleanupAccommodations();
        inOrder.verify(persistencyService).cleanupOrphanAddresses();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void cleanupSecurityEventsUsesConfiguredRetentionAndStrictCutoff() {
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            setRetention(1, 1095);
            saveSecurityEvent(TestingUtils.INSTANT.minus(2, ChronoUnit.DAYS));
            saveSecurityEvent(TestingUtils.INSTANT.minus(1, ChronoUnit.DAYS));
            saveSecurityEvent(TestingUtils.INSTANT.minus(1, ChronoUnit.HOURS));

            persistencyService.cleanupSecurityEvents();
            entityManager.flush();
            entityManager.clear();

            Assertions.assertEquals(2, securityEventRepo.count());
        }
    }

    @Test
    void cleanupBookingsUsesConfiguredRetentionAndStrictCutoff() {
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            setRetention(365, 1);
            var accommodation = accommodationRepo.save(new Accommodation("Cleanup accommodation", "CLEANUP", null));
            saveBooking(accommodation, TestingUtils.INSTANT.minus(2, ChronoUnit.DAYS));
            saveBooking(accommodation, TestingUtils.INSTANT.minus(1, ChronoUnit.DAYS));
            saveBooking(accommodation, TestingUtils.INSTANT.minus(1, ChronoUnit.HOURS));

            persistencyService.cleanupBookings();
            entityManager.flush();
            entityManager.clear();

            Assertions.assertEquals(2, bookingRepo.count());
        }
    }

    @Test
    void disabledRetentionSkipsSecurityEventsAndBookings() {
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            setRetention(0, 0);
            saveSecurityEvent(TestingUtils.INSTANT.minus(2, ChronoUnit.DAYS));
            var accommodation = accommodationRepo.save(new Accommodation("Disabled cleanup", "DISABLED", null));
            saveBooking(accommodation, TestingUtils.INSTANT.minus(2, ChronoUnit.DAYS));

            persistencyService.cleanupSecurityEvents();
            persistencyService.cleanupBookings();
            entityManager.flush();

            Assertions.assertEquals(1, securityEventRepo.count());
            Assertions.assertEquals(1, bookingRepo.count());
        }
    }

    @Test
    void cleanupRemovesSoftDeletedEntitiesAndOldOrphanAddresses() {
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            var account = accountRepo.findByUsername("admin").orElseThrow();
            var accommodation = accommodationRepo.save(new Accommodation("Deleted accommodation", "DELETED", null));
            var address = addressRepo.save(Address.of("Street", null, "Municipality", "PostalCode", "USA"));

            entityManager.createNativeQuery("UPDATE account SET deleted_at = :deletedAt WHERE id = :id")
                    .setParameter("deletedAt", TestingUtils.INSTANT)
                    .setParameter("id", account.getId())
                    .executeUpdate();
            entityManager.createNativeQuery("UPDATE accommodation SET deleted_at = :deletedAt WHERE id = :id")
                    .setParameter("deletedAt", TestingUtils.INSTANT)
                    .setParameter("id", accommodation.getId())
                    .executeUpdate();
            entityManager.createNativeQuery("UPDATE address SET created_at = :createdAt WHERE id = :id")
                    .setParameter("createdAt", TestingUtils.INSTANT.minus(2, ChronoUnit.DAYS))
                    .setParameter("id", address.getId())
                    .executeUpdate();
            entityManager.flush();

            persistencyService.cleanupAccounts();
            persistencyService.cleanupAccommodations();
            persistencyService.cleanupOrphanAddresses();
            entityManager.flush();
            entityManager.clear();

            Assertions.assertEquals(0, nativeCount("account", account.getId()));
            Assertions.assertEquals(0, nativeCount("accommodation", accommodation.getId()));
            Assertions.assertEquals(0, nativeCount("address", address.getId()));
        }
    }

    private void setRetention(int logsRetentionDays, int bookingsRetentionDays) {
        dynamicConfigService.updateConfig(config -> {
            config.setLogsRetentionDays(logsRetentionDays);
            config.setBookingsRetentionDays(bookingsRetentionDays);
        });
    }

    private void saveSecurityEvent(Instant timestamp) {
        securityEventRepo.save(new SecurityEvent(
                timestamp,
                "127.0.0.1",
                SecurityEvent.SecurityEventType.LOGIN,
                SecurityEvent.SecurityEventMethod.USERNAME_PASSWORD,
                null
        ));
    }

    private void saveBooking(Accommodation accommodation, Instant endTime) {
        bookingRepo.save(new Booking(
                accommodation,
                endTime.minus(1, ChronoUnit.HOURS),
                endTime,
                1,
                Payment.of(Payment.PaymentType.ON_SITE, null, null, null, null),
                null,
                null
        ));
    }

    private long nativeCount(String table, Object id) {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + table + " WHERE id = :id")
                .setParameter("id", id)
                .getSingleResult()).longValue();
    }
}
