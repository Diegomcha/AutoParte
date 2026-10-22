package me.diegomcha.autoparte.core.security;

import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AccessEvalsTest {

    private final BookingRepo bookingRepo = mock(BookingRepo.class);
    private final AccommodationRepo accommodationRepo = mock(AccommodationRepo.class);

    private AccessEvals accessEvals;
    private UUID accommodationId;
    private UUID bookingId;
    private UsernamePasswordAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        accessEvals = new AccessEvals(bookingRepo, accommodationRepo);
        accommodationId = UUID.randomUUID();
        bookingId = UUID.randomUUID();
        authentication = new UsernamePasswordAuthenticationToken("employee", "password");
    }

    @Test
    void grantsAccommodationAccessToAssignedEmployee() {
        when(accommodationRepo.existsByIdAndEmployeesAccountUsername(accommodationId, "employee"))
                .thenReturn(true);

        var decision = accommodationManager().authorize(
                () -> authentication,
                context("accommodationId", accommodationId)
        );

        assertTrue(decision.isGranted());
    }

    @Test
    void deniesAccommodationAccessToUnassignedEmployee() {
        when(accommodationRepo.existsByIdAndEmployeesAccountUsername(accommodationId, "employee"))
                .thenReturn(false);

        var decision = accommodationManager().authorize(
                () -> authentication,
                context("accommodationId", accommodationId)
        );

        assertFalse(decision.isGranted());
    }

    @Test
    void deniesAccommodationAccessWithoutAuthenticationOrValidAccommodationId() {
        assertFalse(accommodationManager().authorize(
                () -> null,
                context("accommodationId", accommodationId)
        ).isGranted());
        assertFalse(accommodationManager().authorize(
                () -> authentication,
                context("accommodationId", "invalid-id")
        ).isGranted());
        assertFalse(accommodationManager().authorize(
                () -> authentication,
                context()
        ).isGranted());

        verifyNoInteractions(accommodationRepo);
    }

    @Test
    void deniesAccommodationAccessWhenRepositoryFails() {
        when(accommodationRepo.existsByIdAndEmployeesAccountUsername(accommodationId, "employee"))
                .thenThrow(new RuntimeException("database failure"));

        var decision = accommodationManager().authorize(
                () -> authentication,
                context("accommodationId", accommodationId)
        );

        assertFalse(decision.isGranted());
    }

    @Test
    void grantsPublicBookingAccessWhenSelfCheckInWasRequested() {
        when(bookingRepo.existsByIdAndSelfCheckInRequestedTrue(bookingId))
                .thenReturn(true);

        var decision = publicBookingManager().authorize(
                () -> authentication,
                context("bookingId", bookingId)
        );

        assertTrue(decision.isGranted());
    }

    @Test
    void deniesPublicBookingAccessWhenSelfCheckInWasNotRequested() {
        when(bookingRepo.existsByIdAndSelfCheckInRequestedTrue(bookingId))
                .thenReturn(false);

        var decision = publicBookingManager().authorize(
                () -> null,
                context("bookingId", bookingId)
        );

        assertFalse(decision.isGranted());
    }

    @Test
    void deniesPublicBookingAccessWithoutValidBookingId() {
        assertFalse(publicBookingManager().authorize(
                () -> null,
                context("bookingId", "invalid-id")
        ).isGranted());
        assertFalse(publicBookingManager().authorize(
                () -> null,
                context()
        ).isGranted());

        verifyNoInteractions(bookingRepo);
    }

    @Test
    void deniesPublicBookingAccessWhenRepositoryFails() {
        when(bookingRepo.existsByIdAndSelfCheckInRequestedTrue(bookingId))
                .thenThrow(new RuntimeException("database failure"));

        var decision = publicBookingManager().authorize(
                () -> null,
                context("bookingId", bookingId)
        );

        assertFalse(decision.isGranted());
    }

    private AuthorizationManager<RequestAuthorizationContext> accommodationManager() {
        return accessEvals.accommodationAuthorizationManager();
    }

    private AuthorizationManager<RequestAuthorizationContext> publicBookingManager() {
        return accessEvals.publicBookingAuthorizationManager();
    }

    private RequestAuthorizationContext context(Object... values) {
        var variables = new HashMap<String, String>();
        for (int i = 0; i < values.length; i += 2) {
            variables.put((String) values[i], String.valueOf(values[i + 1]));
        }

        return new RequestAuthorizationContext(
                new MockHttpServletRequest(),
                Map.copyOf(variables)
        );
    }
}
