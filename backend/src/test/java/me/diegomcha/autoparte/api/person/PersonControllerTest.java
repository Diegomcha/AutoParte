package me.diegomcha.autoparte.api.person;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.api.BaseControllerTest;
import me.diegomcha.autoparte.api.person.dto.PersonDtoRequest;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.AddressRepo;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import me.diegomcha.autoparte.core.repos.PersonRepo;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.Person;
import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.person.ContactInfo;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PersonControllerTest extends BaseControllerTest {

    private static final Instant ADULT_BIRTH_DATE = Instant.parse("2000-01-01T00:00:00Z");

    @Autowired
    private AccommodationRepo accommodationRepo;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private AddressRepo addressRepo;
    @Autowired
    private PersonRepo personRepo;
    @Autowired
    private ObjectMapper objectMapper;

    private Accommodation accommodation;
    private Booking booking;
    private Address address;
    private Person person;

    @BeforeEach
    void setUp() {
        accommodation = accommodationRepo.save(new Accommodation("Test accommodation", "TEST-001", true));
        booking = bookingRepo.save(new Booking(
                accommodation,
                TestingUtils.INSTANT,
                TestingUtils.FUTURE_INSTANT,
                2,
                null,
                null,
                null
        ));
        address = addressRepo.save(Address.of("Street 1", "Apartment 2", "Madrid", "28001", "USA"));
        person = personRepo.save(new Person(
                booking,
                new PersonalInfo("John", "Doe", "Smith", "ESP", ADULT_BIRTH_DATE, PersonalInfo.PersonalInfoGender.MALE),
                new ContactInfo("971 49 28 05", null, "john.doe@example.com"),
                Document.of(Document.DocumentType.PASSPORT, "PAS123456", null),
                address,
                Person.PersonRelationship.OTHER
        ));
    }

    @Test
    void peopleCanBeCreatedRetrievedUpdatedSignedAndRemoved() throws Exception {
        var createRequest = new PersonDtoRequest(
                new PersonDtoRequest.PersonalInfoDtoRequest(
                        "Jane", "Smith", "Johnson", "ESP", ADULT_BIRTH_DATE,
                        PersonalInfo.PersonalInfoGender.FEMALE
                ),
                new PersonDtoRequest.ContactInfoDtoRequest(
                        "971 49 28 05", null, "jane.smith@example.com"
                ),
                new PersonDtoRequest.DocumentDtoRequest(
                        Document.DocumentType.PASSPORT, "PAS987654", null
                ),
                address.getId(),
                Person.PersonRelationship.SPOUSE
        );

        var created = mockMvc.perform(post("/api/accommodations/{accommodationId}/bookings/{bookingId}/people",
                        accommodation.getId(), booking.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        var createdId = UUID.fromString(objectMapper.readTree(created.getResponse().getContentAsString())
                .get("id").asString());

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{bookingId}/people",
                        accommodation.getId(), booking.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}",
                        accommodation.getId(), booking.getId(), createdId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId.toString()))
                .andExpect(jsonPath("$.personalInfo.name").value("Jane"))
                .andExpect(jsonPath("$.contactInfo.email").value("jane.smith@example.com"))
                .andExpect(jsonPath("$.address").value(address.getId().toString()));

        var updateRequest = new PersonDtoRequest(
                new PersonDtoRequest.PersonalInfoDtoRequest(
                        "Janet", "Updated", "Johnson", "ESP", ADULT_BIRTH_DATE,
                        PersonalInfo.PersonalInfoGender.FEMALE
                ),
                new PersonDtoRequest.ContactInfoDtoRequest(
                        null, null, "janet.updated@example.com"
                ),
                createRequest.document(),
                address.getId(),
                Person.PersonRelationship.OTHER
        );

        mockMvc.perform(put("/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}",
                        accommodation.getId(), booking.getId(), createdId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNoContent());

        var timestamp = Instant.parse("2024-01-01T00:00:00Z");
        var signature = Map.of(timestamp, List.of(new Point(1, 2)));
        mockMvc.perform(post("/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}/signature",
                        accommodation.getId(), booking.getId(), createdId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .header("User-Agent", "PersonControllerTest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signature)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}/signature",
                        accommodation.getId(), booking.getId(), createdId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths").isMap())
                .andExpect(jsonPath("$.ipAddress").value("127.0.0.1"))
                .andExpect(jsonPath("$.userAgent").value("PersonControllerTest"));

        mockMvc.perform(delete("/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}",
                        accommodation.getId(), booking.getId(), createdId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}",
                        accommodation.getId(), booking.getId(), createdId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void employeesCannotAccessPeopleForUnassignedAccommodation() throws Exception {
        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{bookingId}/people",
                        accommodation.getId(), booking.getId())
                        .with(user("employee").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingPeopleAndBookingsAreReported() throws Exception {
        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{bookingId}/people",
                        accommodation.getId(), UUID.randomUUID())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}",
                        accommodation.getId(), booking.getId(), UUID.randomUUID())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}/signature",
                        accommodation.getId(), booking.getId(), person.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidPeopleParametersAreRejected() throws Exception {
        mockMvc.perform(get("/api/accommodations/not-a-uuid/bookings/{bookingId}/people",
                        booking.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/not-a-uuid/people",
                        accommodation.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{bookingId}/people/not-a-uuid",
                        accommodation.getId(), booking.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/accommodations/{accommodationId}/bookings/{bookingId}/people",
                        accommodation.getId(), booking.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "personalInfo": null,
                                  "contactInfo": null,
                                  "document": null,
                                  "address": null,
                                  "relationship": null
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stateChangingPersonRoutesRequireCsrf() throws Exception {
        mockMvc.perform(delete("/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}",
                        accommodation.getId(), booking.getId(), person.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/accommodations/{accommodationId}/bookings/{bookingId}/people/{id}/signature",
                        accommodation.getId(), booking.getId(), person.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
