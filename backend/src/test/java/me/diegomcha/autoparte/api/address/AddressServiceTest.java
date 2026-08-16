package me.diegomcha.autoparte.api.address;

import me.diegomcha.autoparte.api.address.dto.AddressDtoRequest;
import me.diegomcha.autoparte.api.address.dto.AddressDtoResponse;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import me.diegomcha.autoparte.core.repos.AddressRepo;
import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.address.SpanishAddress;
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

    @Autowired
    private AddressRepo repo;
    @Autowired
    private AddressService service;

    private Address address;

    @BeforeEach
    void setUp() {
        this.address = repo.save(Address.of("Street 1", "Appt 2", "Miami", "12345", "USA"));
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

    //TODO: Test getBookingAddresses
}

