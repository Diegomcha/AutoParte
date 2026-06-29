package me.diegomcha.autoparte.core.security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.config.DynamicConfigService;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Supplier;

// TODO: Test whether it works

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BookingPublicAccessEval {

    private final BookingRepo bookingRepo;
    private final DynamicConfigService dynamicConfigService;
    private final AuthenticatedAuthorizationManager<RequestAuthorizationContext> authenticator = new AuthenticatedAuthorizationManager<>();

    public AuthorizationManager<RequestAuthorizationContext> getBookingCanBeAccessedPubliclyAuthorizationManager() {
        return (authn, context) -> {
            try {
                // If the user is authenticated, allow access regardless
                if (this.isAuthenticated(authn, context))
                    return new AuthorizationDecision(true);

                // If the booking can be accessed publicly, allow access
                if (this.canBookingBeAccessedPublicly(context))
                    return new AuthorizationDecision(true);
            } catch (Exception ignored) {
                /* Ignore any exceptions */
            }

            // If the user is not authenticated and the booking cannot be accessed publicly, reject access
            return new AuthorizationDecision(false);
        };
    }

    public AuthorizationManager<RequestAuthorizationContext> getCheckinCanBeAccessedPubliclyAuthorizationManager() {
        return (authn, context) -> {
            try {
                // If the user is authenticated, allow access regardless
                if (this.isAuthenticated(authn, context))
                    return new AuthorizationDecision(true);

                // If check-in cannot be accessed publicly, reject access
                if (!this.canCheckinBeAccessedPublicly())
                    return new AuthorizationDecision(false);

                // If the booking can be accessed publicly, allow access
                if (this.canBookingBeAccessedPublicly(context))
                    return new AuthorizationDecision(true);
            } catch (Exception ignored) {
                /* Ignore any exceptions */
            }

            // If the user is not authenticated and the booking cannot be accessed publicly, reject access
            return new AuthorizationDecision(false);
        };
    }

    private boolean isAuthenticated(Supplier<? extends Authentication> authn,RequestAuthorizationContext context) {
        return authenticator.authorize(authn, context).isGranted();
    }

    private boolean canBookingBeAccessedPublicly(RequestAuthorizationContext context) {
        var accommodationId = UUID.fromString(context.getVariables().get("accommodationId"));
        var bookingId = UUID.fromString(context.getVariables().get("bookingId"));

        var booking = bookingRepo.findByAccommodationIdAndId(accommodationId, bookingId);
        return booking.isPresent() && booking.get().canBeCheckedIn();
    }

    private boolean canCheckinBeAccessedPublicly() {
        return !dynamicConfigService.getConfig().isManualReviewEnabled();
    }
}
