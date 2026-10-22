package me.diegomcha.autoparte.integration.ses.tasks;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.config.DynamicConfigService;
import me.diegomcha.autoparte.core.exception.ServiceValidationException;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import me.diegomcha.autoparte.core.repos.CommunicationRepo;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.communication.Communication;
import me.diegomcha.autoparte.integration.ses.SesClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class SesSenderTest {

    @Autowired
    private SesSender sender;
    @Autowired
    private DynamicConfigService dynamicConfigService;
    @Autowired
    private AccommodationRepo accommodationRepo;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private CommunicationRepo communicationRepo;
    @MockitoBean
    private SesClient sesClient;

    @Test
    void skipsSendingWhenSesCredentialsAreInvalid() {
        dynamicConfigService.updateConfig(config -> config.setSesCredentialsValid(false));

        sender.schedule();

        Mockito.verifyNoInteractions(sesClient);
    }

    @Test
    void sendsPendingBookingsAndCheckIns() throws Exception {
        var bookingCommunication = createPendingCommunication(Communication.CommunicationType.BOOKING, 1);
        var checkInCommunication = createPendingCommunication(Communication.CommunicationType.CHECKIN, 2);
        var bookingBatchId = UUID.randomUUID();
        var checkInBatchId = UUID.randomUUID();

        Mockito.when(sesClient.sendCommunication(Mockito.any())).thenReturn(bookingBatchId, checkInBatchId);

        sender.schedule();

        var sentBooking = communicationRepo.findById(bookingCommunication.getId()).orElseThrow();
        var sentCheckIn = communicationRepo.findById(checkInCommunication.getId()).orElseThrow();
        Assertions.assertEquals(Communication.CommunicationStatus.SENT, sentBooking.getStatus());
        Assertions.assertEquals(bookingBatchId, sentBooking.getBatchId());
        Assertions.assertEquals(1, sentBooking.getBatchOrder());
        Assertions.assertEquals(Communication.CommunicationStatus.SENT, sentCheckIn.getStatus());
        Assertions.assertEquals(checkInBatchId, sentCheckIn.getBatchId());
        Assertions.assertEquals(1, sentCheckIn.getBatchOrder());
        Mockito.verify(sesClient).prepareSendBooking(Mockito.anyCollection());
        Mockito.verify(sesClient).prepareSendCheckIn(Mockito.anyString(), Mockito.anyCollection());
        Mockito.verify(sesClient, Mockito.times(2)).sendCommunication(Mockito.any());
    }

    @Test
    void marksBookingAsFailedWhenSesRejectsTheBatch() throws Exception {
        var communication = createPendingCommunication(Communication.CommunicationType.BOOKING, 3);
        var exception = new ServiceValidationException(ServiceValidationException.ServiceValidationType.INVALID_VALUE);

        Mockito.when(sesClient.sendCommunication(Mockito.any())).thenThrow(exception);

        sender.schedule();

        var failedCommunication = communicationRepo.findById(communication.getId()).orElseThrow();
        Assertions.assertEquals(Communication.CommunicationStatus.FAILED, failedCommunication.getStatus());
        Assertions.assertEquals(exception.getMessage(), failedCommunication.getError());
        Mockito.verify(sesClient).prepareSendBooking(Mockito.anyCollection());
        Mockito.verify(sesClient).sendCommunication(Mockito.any());
        Mockito.verify(sesClient, Mockito.never()).prepareSendCheckIn(Mockito.anyString(), Mockito.anyCollection());
    }

    @Test
    void finishesCancellationWithoutSesRequestWhenThereAreNoSuccessfulCommunicationsToCancel() {
        var communication = createPendingCommunication(Communication.CommunicationType.CANCELLATION, 4);

        sender.schedule();

        var finishedCancellation = communicationRepo.findById(communication.getId()).orElseThrow();
        Assertions.assertEquals(Communication.CommunicationStatus.SUCCEEDED, finishedCancellation.getStatus());
        Mockito.verify(sesClient, Mockito.never()).prepareSendCancellation(Mockito.anyCollection());
        Mockito.verifyNoInteractions(sesClient);
    }

    private Communication createPendingCommunication(Communication.CommunicationType type, int suffix) {
        var accommodation = accommodationRepo.save(new Accommodation(
                "Test Accommodation " + suffix + "-" + UUID.randomUUID(),
                "SES-" + suffix + "-" + UUID.randomUUID(),
                null
        ));
        var booking = bookingRepo.save(new Booking(
                accommodation,
                TestingUtils.INSTANT,
                TestingUtils.FUTURE_INSTANT,
                1,
                null,
                null,
                null
        ));
        return communicationRepo.save(Communication._of(booking, type));
    }
}
