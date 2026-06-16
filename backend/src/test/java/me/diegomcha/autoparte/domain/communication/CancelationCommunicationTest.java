package me.diegomcha.autoparte.domain.communication;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.booking.payment.PaymentInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class CancelationCommunicationTest {

    private CancelationCommunication communication;
    private UUID batchId;
    private UUID sesId;

    @BeforeEach
    void setUp() {
        var booking = new Booking(new Accommodation("Test", "SESCODE", null), TestingUtils.INSTANT, TestingUtils.FUTURE_INSTANT, 1, PaymentInfo.of(PaymentInfo.PaymentType.ON_SITE), null, null);
        this.communication = (CancelationCommunication) Communication._of(booking, Communication.CommunicationType.CANCELLATION);
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
    void testPendingMarkSucceededFailed() {
        // Invalid status
        communication.markSent(batchId);

        Assertions.assertThrows(IllegalStateException.class, () -> communication.markFinishedSuccessfully());

        Assertions.assertEquals(Communication.CommunicationStatus.SENT, communication.getStatus());
        Assertions.assertNull(communication.getSesId());
    }

    @Test
    void testMarkPendingVoidedFails() {
        communication.markSent(batchId);
        communication.markFinishedSuccessfully(sesId);

        Assertions.assertThrows(UnsupportedOperationException.class, () -> communication.markPendingVoided());

        Assertions.assertEquals(Communication.CommunicationStatus.SUCCEEDED, communication.getStatus());
    }

    @Test
    void testMarkVoidedFails() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> communication.markVoided());

        Assertions.assertEquals(Communication.CommunicationStatus.PENDING, communication.getStatus());
    }

}
