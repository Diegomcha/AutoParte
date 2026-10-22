package me.diegomcha.autoparte.api.address;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.api.BaseControllerTest;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.AddressRepo;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.Person;
import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.person.ContactInfo;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AddressControllerTest extends BaseControllerTest {

    @Autowired
    private AddressRepo addressRepo;

    @Autowired
    private AccommodationRepo accommodationRepo;

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private Address address;
    private Accommodation accommodation;
    private Booking booking;

    @BeforeEach
    void setUp() {
        address = addressRepo.save(Address.of("Street 1", "Appt 2", "Miami", "12345", "USA"));
        accommodation = accommodationRepo.save(new Accommodation("Test accommodation", "TEST-001", true));
        booking = bookingRepo.save(new Booking(
                accommodation,
                TestingUtils.INSTANT,
                TestingUtils.FUTURE_INSTANT,
                1,
                null,
                null,
                null
        ));

        new Person(
                booking,
                new PersonalInfo("John", "Doe", null, null, TestingUtils.PAST_INSTANT, null),
                new ContactInfo(null, null, "john.doe@example.com"),
                null,
                address,
                null
        );
        bookingRepo.save(booking);
    }

    @Test
    void addressesCanBeCreatedAndRetrieved() throws Exception {
        var result = mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "addressLine1": "Street 2",
                                  "addressLine2": "Appt 3",
                                  "municipality": "33044",
                                  "postalCode": "33007",
                                  "country": "ESP"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();

        var created = objectMapper.readValue(result.getResponse().getContentAsString(), EntityDtoCreated.class);

        mockMvc.perform(get("/api/addresses/{id}", created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.id().toString()))
                .andExpect(jsonPath("$.addressLine1").value("Street 2"))
                .andExpect(jsonPath("$.addressLine2").value("Appt 3"))
                .andExpect(jsonPath("$.municipality").value("33044"))
                .andExpect(jsonPath("$.postalCode").value("33007"))
                .andExpect(jsonPath("$.country").value("ESP"));
    }

    @Test
    void bookingAddressesAreProtectedAndExposedToAdmins() throws Exception {
        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{bookingId}/addresses",
                        accommodation.getId(), booking.getId())
                        .with(user("employee").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{bookingId}/addresses",
                        accommodation.getId(), booking.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(address.getId().toString()))
                .andExpect(jsonPath("$[0].addressLine1").value("Street 1"));
    }

    @Test
    void missingAddressesAndBookingsAreReported() throws Exception {
        mockMvc.perform(get("/api/addresses/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{bookingId}/addresses",
                        accommodation.getId(), UUID.randomUUID())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidAddressParametersAreRejected() throws Exception {
        mockMvc.perform(get("/api/addresses/not-a-uuid"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "addressLine1": "",
                                  "addressLine2": null,
                                  "municipality": "33044",
                                  "postalCode": "33007",
                                  "country": "ESP"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "addressLine1": "Street 2",
                                  "addressLine2": null,
                                  "municipality": "33044",
                                  "postalCode": "33007",
                                  "country": "INVALID"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/accommodations/not-a-uuid/bookings/{bookingId}/addresses", booking.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
    }
}
