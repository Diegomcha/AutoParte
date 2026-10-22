package me.diegomcha.autoparte.integration.ses.tasks;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.config.DynamicConfigService;
import me.diegomcha.autoparte.core.exception.BadConfigurationException;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.core.exception.ServiceValidationException;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import me.diegomcha.autoparte.core.repos.CommunicationRepo;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.communication.Communication;
import me.diegomcha.autoparte.integration.ses.SesClient;
import me.diegomcha.autoparte.integration.ses.dto.BatchDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class SesCheckerTest {

    @Autowired
    private SesChecker checker;
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
    void skipsCheckingWhenSesCredentialsAreInvalid() {
        dynamicConfigService.updateConfig(config -> config.setSesCredentialsValid(false));

        checker.schedule();

        Mockito.verifyNoInteractions(sesClient);
    }

    @Test
    void updatesCommunicationsFromSuccessfulBatchResponse()
            throws ServiceUnavailableException, BadConfigurationException, ServiceValidationException {
        var batchId = UUID.randomUUID();
        var succeededCommunication = createSentCommunication(batchId, 1);
        var failedCommunication = createSentCommunication(batchId, 2);
        var sesId = UUID.randomUUID();

        Mockito.when(sesClient.checkBatches(Mockito.anyCollection())).thenReturn(Map.of(
                batchId,
                new BatchDto(
                        BatchDto.BatchDtoStatus.SUCCESS,
                        null,
                        List.of(
                                new BatchDto.CommunicationDto(1, sesId, null),
                                new BatchDto.CommunicationDto(2, null, "Invalid guest data")
                        )
                )
        ));

        checker.schedule();

        var updatedSucceededCommunication = communicationRepo.findById(succeededCommunication.getId()).orElseThrow();
        var updatedFailedCommunication = communicationRepo.findById(failedCommunication.getId()).orElseThrow();
        Assertions.assertEquals(Communication.CommunicationStatus.SUCCEEDED, updatedSucceededCommunication.getStatus());
        Assertions.assertEquals(sesId, updatedSucceededCommunication.getSesId());
        Assertions.assertEquals(Communication.CommunicationStatus.FAILED, updatedFailedCommunication.getStatus());
        Assertions.assertEquals("Invalid guest data", updatedFailedCommunication.getError());
        Mockito.verify(sesClient).checkBatches(Set.of(batchId));
    }

    @Test
    void failsAllCommunicationsWhenSesRejectsTheBatch()
            throws ServiceUnavailableException, BadConfigurationException, ServiceValidationException {
        var batchId = UUID.randomUUID();
        var firstCommunication = createSentCommunication(batchId, 1);
        var secondCommunication = createSentCommunication(batchId, 2);

        Mockito.when(sesClient.checkBatches(Mockito.anyCollection())).thenReturn(Map.of(
                batchId,
                new BatchDto(BatchDto.BatchDtoStatus.ERROR_FORMAT, "Malformed batch", List.of())
        ));

        checker.schedule();

        Assertions.assertEquals(
                Communication.CommunicationStatus.FAILED,
                communicationRepo.findById(firstCommunication.getId()).orElseThrow().getStatus()
        );
        Assertions.assertEquals(
                Communication.CommunicationStatus.FAILED,
                communicationRepo.findById(secondCommunication.getId()).orElseThrow().getStatus()
        );
        Assertions.assertEquals(
                "ERROR_FORMAT",
                communicationRepo.findById(firstCommunication.getId()).orElseThrow().getError()
        );
    }

    private Communication createSentCommunication(UUID batchId, int batchOrder) {
        var accommodation = accommodationRepo.save(new Accommodation(
                "Test Accommodation " + batchId + "-" + batchOrder,
                "SES-" + batchId + "-" + batchOrder,
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
        var communication = communicationRepo.save(
                Communication._of(booking, Communication.CommunicationType.BOOKING)
        );
        communication.markSent(batchId, batchOrder);
        return communicationRepo.save(communication);
    }
}
