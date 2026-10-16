package me.diegomcha.autoparte.api.address;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.api.address.dto.AddressDtoRequest;
import me.diegomcha.autoparte.api.address.dto.AddressDtoResponse;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.AddressRepo;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.Person;
import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.address.SpanishAddress;
import me.diegomcha.autoparte.domain.person.ContactInfo;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class AddressServiceTest {

    private Address address;
    private Accommodation accommodation;
    private Booking booking;
    private Person person;

    @Autowired
    private AddressRepo repo;
    @Autowired
    private AddressService service;
    @Autowired
    private AccommodationRepo accommodationRepo;
    @Autowired
    private BookingRepo bookingRepo;

    @BeforeEach
    void setUp() {
        this.address = repo.save(Address.of("Street 1", "Appt 2", "Miami", "12345", "USA"));
        this.accommodation = accommodationRepo.save(new Accommodation("Test Accommodation", "SESCODE", null));
        this.booking = bookingRepo.save(new Booking(accommodation, TestingUtils.INSTANT, TestingUtils.FUTURE_INSTANT, 1, null, null, null));
        this.person = new Person(booking, new PersonalInfo("John", "Doe", null, null, TestingUtils.PAST_INSTANT, null), new ContactInfo(null, null, "john.doe@example.com"), null, null, null);
    }

    @Test
    void testGetAddress() throws ResourceNotFoundException {
        Assertions.assertNotNull(address.getId());
        AddressDtoResponse response = service.getAddress(address.getId());

        Assertions.assertEquals(address.getId(), response.id());
        Assertions.assertEquals("Street 1", response.addressLine1());
        Assertions.assertEquals("Appt 2", response.addressLine2());
        Assertions.assertEquals("Miami", response.municipality());
        Assertions.assertEquals("12345", response.postalCode());
        Assertions.assertEquals("USA", response.country());
        Assertions.assertNotNull(response.createdAt());
        Assertions.assertNotNull(response.updatedAt());
    }

    @Test
    void testGetAddressNotFound() {
        UUID nonExistingId = UUID.randomUUID();
        Assertions.assertThrows(ResourceNotFoundException.class, () -> service.getAddress(nonExistingId));
    }

    @Test
    void testCreateAddress() {
        AddressDtoRequest dto = new AddressDtoRequest("Street 2", "Appt 3", "33044", "33007", "ESP");
        EntityDtoCreated created = service.createAddress(dto);

        Assertions.assertNotNull(created.id());
        Assertions.assertNotNull(created.createdAt());

        Assertions.assertTrue(repo.existsById(created.id()));

        Address savedAddress = repo.findById(created.id()).orElseThrow();
        Assertions.assertInstanceOf(SpanishAddress.class, savedAddress);
        Assertions.assertEquals("Street 2", savedAddress.getAddressLine1());
        Assertions.assertEquals("Appt 3", savedAddress.getAddressLine2());
        Assertions.assertEquals("33044", savedAddress.getMunicipality());
        Assertions.assertEquals("33007", savedAddress.getPostalCode());
        Assertions.assertEquals("ESP", savedAddress.getCountry());
    }

    @Test
    void testGetBookingAddresses() throws ResourceNotFoundException {
        var addresses = service.getBookingAddresses(accommodation.getId(), booking.getId());

        Assertions.assertEquals(0, addresses.size()); // No addresses associated yet

        // Associate an address with the booking
        this.person.setAddress(address);

        var updatedAddresses = service.getBookingAddresses(accommodation.getId(), booking.getId());
        Assertions.assertEquals(1, updatedAddresses.size());
    }

    @Test
    void testGetBookingAddressesNotFound() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> service.getBookingAddresses(UUID.randomUUID(), UUID.randomUUID()));
        Assertions.assertThrows(ResourceNotFoundException.class, () -> service.getBookingAddresses(accommodation.getId(), UUID.randomUUID()));
        Assertions.assertThrows(ResourceNotFoundException.class, () -> service.getBookingAddresses(UUID.randomUUID(), booking.getId()));
    }
}

