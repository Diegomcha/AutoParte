package me.diegomcha.autoparte.api.booking;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.api.BaseControllerTest;
import me.diegomcha.autoparte.api.booking.dto.BookingDtoRequest;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.booking.payment.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookingControllerTest extends BaseControllerTest {

    @Autowired
    private AccommodationRepo accommodationRepo;

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private Accommodation accommodation;
    private Booking booking;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void bookingsCanBeCreatedRetrievedUpdatedAndDeleted() throws Exception {
        var createRequest = new BookingDtoRequest(
                TestingUtils.INSTANT,
                TestingUtils.FUTURE_INSTANT,
                2,
                new BookingDtoRequest.PaymentDtoRequest(Payment.PaymentType.ON_SITE, null, null, null, null),
                1,
                false
        );

        var result = mockMvc.perform(post("/api/accommodations/{accommodationId}/bookings", accommodation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();

        var created = objectMapper.readValue(result.getResponse().getContentAsString(), EntityDtoCreated.class);

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{id}",
                        accommodation.getId(), created.id())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.id().toString()))
                .andExpect(jsonPath("$.numberOfPeople").value(2))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.accommodationName").value("Test accommodation"));

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings", accommodation.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));

        var updateRequest = new BookingDtoRequest(
                TestingUtils.INSTANT.plusSeconds(60),
                TestingUtils.FUTURE_INSTANT,
                3,
                new BookingDtoRequest.PaymentDtoRequest(Payment.PaymentType.CASH, "cash", null, null, null),
                2,
                true
        );

        mockMvc.perform(put("/api/accommodations/{accommodationId}/bookings/{id}",
                        accommodation.getId(), created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{id}",
                        accommodation.getId(), created.id())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfPeople").value(3))
                .andExpect(jsonPath("$.payment.type").value("CASH"))
                .andExpect(jsonPath("$.numberOfRooms").value(2))
                .andExpect(jsonPath("$.internetConnection").value(true));

        mockMvc.perform(delete("/api/accommodations/{accommodationId}/bookings/{id}",
                        accommodation.getId(), created.id())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{id}",
                        accommodation.getId(), created.id())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void employeesCannotAccessBookingsForUnassignedAccommodation() throws Exception {
        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings", accommodation.getId())
                        .with(user("employee").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/accommodations/{accommodationId}/bookings/{id}/confirm",
                        accommodation.getId(), booking.getId())
                        .with(user("employee").roles("EMPLOYEE"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingBookingsAndAccommodationsAreReported() throws Exception {
        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{id}",
                        accommodation.getId(), UUID.randomUUID())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings",
                        UUID.randomUUID())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidBookingParametersAreRejected() throws Exception {
        mockMvc.perform(get("/api/accommodations/not-a-uuid/bookings/{id}", booking.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/not-a-uuid",
                        accommodation.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings",
                        accommodation.getId())
                        .param("startRange", TestingUtils.FUTURE_INSTANT.toString())
                        .param("endRange", TestingUtils.INSTANT.toString())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/accommodations/{accommodationId}/bookings", accommodation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startTime": "2026-01-02T00:00:00Z",
                                  "endTime": "2026-01-01T00:00:00Z",
                                  "numberOfPeople": 0,
                                  "payment": null,
                                  "numberOfRooms": 0,
                                  "internetConnection": true
                                }
                                """)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stateChangingBookingRoutesRequireCsrfAndDraftConflictsAreReported() throws Exception {
        mockMvc.perform(delete("/api/accommodations/{accommodationId}/bookings/{id}",
                        accommodation.getId(), booking.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/accommodations/{accommodationId}/bookings/{id}/confirm",
                        accommodation.getId(), booking.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/accommodations/{accommodationId}/bookings/{id}/self-check-in-request",
                        accommodation.getId(), booking.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }
}
