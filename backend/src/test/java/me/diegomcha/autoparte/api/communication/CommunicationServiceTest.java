package me.diegomcha.autoparte.api.communication;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import me.diegomcha.autoparte.core.repos.CommunicationRepo;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.communication.Communication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class CommunicationServiceTest {
    @Autowired
    private CommunicationService service;
    @Autowired
    private AccommodationRepo accommodationRepo;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private CommunicationRepo communicationRepo;

    private Accommodation accommodation;
    private Booking booking;
    private Communication communication;

    @BeforeEach
    void setUp() {
        this.accommodation = accommodationRepo.save(new Accommodation("Test Accommodation", "SESCODE", null));
        this.booking = bookingRepo.save(new Booking(
                accommodation,
                TestingUtils.INSTANT,
                TestingUtils.FUTURE_INSTANT,
                1,
                null,
                null,
                null
        ));
        this.communication = communicationRepo.save(
                Communication._of(booking, Communication.CommunicationType.BOOKING)
        );
    }

    @Test
    void testGetCommunications() {
        var communications = service.getCommunications(Pageable.unpaged());

        Assertions.assertEquals(1, communications.getTotalElements());
        Assertions.assertEquals(communication.getId(), communications.getContent().getFirst().id());

        communicationRepo.delete(communication);

        Assertions.assertEquals(0, service.getCommunications(Pageable.unpaged()).getTotalElements());
    }

    @Test
    void testGetCommunicationsByAccommodationId() throws ResourceNotFoundException {
        var communications = service.getCommunicationsByAccommodationId(accommodation.getId(), Pageable.unpaged());

        Assertions.assertEquals(1, communications.getTotalElements());
        Assertions.assertEquals(communication.getId(), communications.getContent().getFirst().id());
        Assertions.assertEquals(accommodation.getId(), communications.getContent().getFirst().accommodationId());

        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.getCommunicationsByAccommodationId(UUID.randomUUID(), Pageable.unpaged())
        );
    }

    @Test
    void testGetCommunicationsByBookingId() throws ResourceNotFoundException {
        var communications = service.getCommunicationsByBookingId(accommodation.getId(), booking.getId());

        Assertions.assertEquals(1, communications.size());
        Assertions.assertEquals(communication.getId(), communications.getFirst().id());
        Assertions.assertEquals(booking.getId(), communications.getFirst().bookingId());

        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.getCommunicationsByBookingId(accommodation.getId(), UUID.randomUUID())
        );
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.getCommunicationsByBookingId(UUID.randomUUID(), booking.getId())
        );
    }
}
