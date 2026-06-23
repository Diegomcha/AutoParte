package me.diegomcha.autoparte.domain.communication;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.booking.payment.Payment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class CommunicationTest {

    private Communication communication;
    private UUID batchId;
    private UUID sesId;

    @BeforeEach
    void setUp() {
        var booking = new Booking(new Accommodation("Test", "SESCODE", null), TestingUtils.INSTANT, TestingUtils.FUTURE_INSTANT, 1, Payment.of(Payment.PaymentType.ON_SITE), null, null);
        this.communication = Communication._of(booking, Communication.CommunicationType.BOOKING);
        this.batchId = UUID.randomUUID();
        this.sesId = UUID.randomUUID();
    }

    @Test
    void testMarkSent() {
        communication.markSent(batchId);

        Assertions.assertEquals(Communication.CommunicationStatus.SENT, communication.getStatus());
        Assertions.assertNotNull(communication.getSentTimestamp());
        Assertions.assertEquals(batchId, communication.getBatchId());
    }

    @Test
    void testMarkSentFailed() {
        communication.markVoided();

        Assertions.assertThrows(IllegalStateException.class, () -> communication.markSent(batchId));

        Assertions.assertEquals(Communication.CommunicationStatus.VOIDED, communication.getStatus());
        Assertions.assertNull(communication.getSentTimestamp());
        Assertions.assertNull(communication.getBatchId());
    }

    @Test
    void testMarkSucceeded() {
        communication.markSent(batchId);

        communication.markFinishedSuccessfully(sesId);

        Assertions.assertEquals(Communication.CommunicationStatus.SUCCEEDED, communication.getStatus());
        Assertions.assertEquals(sesId, communication.getSesId());
    }

    @Test
    void testMarkSucceededFailed() {
        Assertions.assertThrows(IllegalStateException.class, () -> communication.markFinishedSuccessfully(sesId));

        Assertions.assertEquals(Communication.CommunicationStatus.PENDING, communication.getStatus());
        Assertions.assertNull(communication.getSesId());
    }

    @Test
    void testMarkFailed() {
        communication.markSent(batchId);

        communication.markFinishedFailed(10100);

        Assertions.assertEquals(Communication.CommunicationStatus.FAILED, communication.getStatus());
        Assertions.assertEquals(10100, communication.getErrorCode());
    }

    @Test
    void testMarkFailedFailed() {
        communication.markSent(batchId);
        communication.markFinishedSuccessfully(sesId);

        Assertions.assertThrows(IllegalStateException.class, () -> communication.markFinishedFailed(10100));

        Assertions.assertEquals(Communication.CommunicationStatus.SUCCEEDED, communication.getStatus());
        Assertions.assertNull(communication.getErrorCode());
    }

    @Test
    void testMarkPendingVoided() {
        communication.markSent(batchId);
        communication.markFinishedSuccessfully(sesId);

        communication.markPendingVoided();

        Assertions.assertEquals(Communication.CommunicationStatus.PENDING_VOIDED, communication.getStatus());
    }

    @Test
    void testMarkPendingVoidedFailed() {
        communication.markSent(batchId);
        communication.markFinishedFailed(10100);

        Assertions.assertThrows(IllegalStateException.class, () -> communication.markPendingVoided());

        Assertions.assertEquals(Communication.CommunicationStatus.FAILED, communication.getStatus());
    }

    @Test
    void testMarkVoided() {
        communication.markSent(batchId);
        communication.markFinishedSuccessfully(sesId);
        communication.markPendingVoided();

        communication.markVoided();

        Assertions.assertEquals(Communication.CommunicationStatus.VOIDED, communication.getStatus());
    }

    @Test
    void testMarkVoidedFailed() {
        communication.markSent(batchId);

        Assertions.assertThrows(IllegalStateException.class, () -> communication.markVoided());

        Assertions.assertEquals(Communication.CommunicationStatus.SENT, communication.getStatus());
    }

}
