package me.diegomcha.autoparte.domain.communication;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.booking.payment.Payment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class CancellationCommunicationTest {

    private CancellationCommunication communication;
    private UUID batchId;
    private UUID sesId;

    @BeforeEach
    void setUp() {
        var booking = new Booking(new Accommodation("Test", "SESCODE", null), TestingUtils.INSTANT, TestingUtils.FUTURE_INSTANT, 1, Payment.of(Payment.PaymentType.ON_SITE, null, null, null, null), null, null);
        this.communication = (CancellationCommunication) Communication._of(booking, Communication.CommunicationType.CANCELLATION);
        this.batchId = UUID.randomUUID();
        this.sesId = UUID.randomUUID();
    }

    @Test
    void testPendingMarkSucceeded() {
        communication.markFinishedSuccessfully();

        Assertions.assertEquals(Communication.CommunicationStatus.SUCCEEDED, communication.getStatus());
        Assertions.assertNull(communication.getSesId());
    }

    @Test
    void testSentMarkSucceeded() {
        // Invalid status
        communication.markSent(batchId, 0);

        communication.markFinishedSuccessfully();

        Assertions.assertEquals(Communication.CommunicationStatus.SUCCEEDED, communication.getStatus());
        Assertions.assertNull(communication.getSesId());
    }

    @Test
    void testFailedMarkSucceededFailed() {
        // Invalid status
        communication.markSent(batchId, 0);
        communication.markFinishedFailed("Error");

        Assertions.assertThrows(IllegalStateException.class, () -> communication.markFinishedSuccessfully());

        Assertions.assertEquals(Communication.CommunicationStatus.FAILED, communication.getStatus());
        Assertions.assertNull(communication.getSesId());
    }

    @Test
    void testMarkPendingVoidedFails() {
        communication.markSent(batchId, 0);
        communication.markFinishedSuccessfully(sesId);

        Assertions.assertThrows(UnsupportedOperationException.class, () -> communication.markPendingVoided());

        Assertions.assertEquals(Communication.CommunicationStatus.SUCCEEDED, communication.getStatus());
    }

    @Test
    void testMarkVoidedFails() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> communication.markVoided());

        Assertions.assertEquals(Communication.CommunicationStatus.PENDING, communication.getStatus());
    }

    // TODO: test revertFromPendingVoided

}
