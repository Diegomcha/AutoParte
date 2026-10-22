package me.diegomcha.autoparte.api.communication;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.api.BaseControllerTest;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import me.diegomcha.autoparte.core.repos.CommunicationRepo;
import me.diegomcha.autoparte.core.repos.EmployeeRepo;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.Employee;
import me.diegomcha.autoparte.domain.communication.Communication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommunicationControllerTest extends BaseControllerTest {

    @Autowired
    private AccommodationRepo accommodationRepo;

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private CommunicationRepo communicationRepo;

    private Accommodation accommodation;
    private Booking booking;
    private Communication communication;
    @Autowired
    private EmployeeRepo employeeRepo;

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
        var employee = employeeRepo.save(new Employee("Test", "Employee", "test.employee@example.com", "password"));
        accommodation.addEmployee(employee);
        communication = communicationRepo.save(
                Communication._of(booking, Communication.CommunicationType.BOOKING)
        );
    }

    @Test
    void communicationsAreExposedGloballyAndByAccommodationAndBooking() throws Exception {
        mockMvc.perform(get("/api/communications")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(communication.getId().toString()))
                .andExpect(jsonPath("$.content[0].accommodationId").value(accommodation.getId().toString()))
                .andExpect(jsonPath("$.content[0].bookingId").value(booking.getId().toString()))
                .andExpect(jsonPath("$.content[0].type").value("BOOKING"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));

        mockMvc.perform(get("/api/accommodations/{accommodationId}/communications", accommodation.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(communication.getId().toString()));

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{bookingId}/communications",
                        accommodation.getId(), booking.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(communication.getId().toString()))
                .andExpect(jsonPath("$[0].bookingId").value(booking.getId().toString()));
    }

    @Test
    void employeesCannotAccessCommunicationRoutes() throws Exception {
        mockMvc.perform(get("/api/communications")
                        .with(user("test.employee@example.com").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/accommodations/{accommodationId}/communications", accommodation.getId())
                        .with(user("test.employee@example.com").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());

        // Unassigned employees cannot access communications for accommodations they are not assigned to
        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{bookingId}/communications",
                        accommodation.getId(), booking.getId())
                        .with(user("employee").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());

        // Assigned employees can access communications for accommodations they are assigned to
        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{bookingId}/communications",
                        accommodation.getId(), booking.getId())
                        .with(user("test.employee@example.com").roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(communication.getId().toString()))
                .andExpect(jsonPath("$[0].bookingId").value(booking.getId().toString()));
    }

    @Test
    void missingCommunicationResourcesAreReported() throws Exception {
        mockMvc.perform(get("/api/accommodations/{accommodationId}/communications", UUID.randomUUID())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{bookingId}/communications",
                        accommodation.getId(), UUID.randomUUID())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/{bookingId}/communications",
                        UUID.randomUUID(), booking.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidCommunicationPathParametersAreRejected() throws Exception {
        mockMvc.perform(get("/api/accommodations/not-a-uuid/communications")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/accommodations/{accommodationId}/bookings/not-a-uuid/communications",
                        accommodation.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
    }
}
