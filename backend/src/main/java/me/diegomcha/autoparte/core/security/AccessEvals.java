package me.diegomcha.autoparte.core.security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.config.DynamicConfigService;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagerFactory;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AccessEvals implements AuthorizationManagerFactory<RequestAuthorizationContext> {

    private final BookingRepo bookingRepo;
    private final AccommodationRepo accommodationRepo;

    private final DynamicConfigService dynamicConfigService;

    public AuthorizationManager<RequestAuthorizationContext> accommodationAuthorizationManager() {
        return (authn, context) -> {
            try {
                var authentication = authn.get();
                var accommodationId = Optional.ofNullable(context)
                        .map(ctx -> ctx.getVariables().get("accommodationId"))
                        .map(UUID::fromString);

                if (authentication != null && accommodationId.isPresent() && accommodationRepo.existsByIdAndEmployeesAccountUsername(accommodationId.get(), authentication.getName()))
                    return new AuthorizationDecision(true);

            } catch (Exception ignored) {/* Ignore any exceptions */}

            return new AuthorizationDecision(false);
        };
    }

    public AuthorizationManager<RequestAuthorizationContext> publicBookingAuthorizationManager() {
        return (_authn, context) -> {
            try {
                var bookingId = Optional.ofNullable(context)
                        .map(ctx -> ctx.getVariables().get("bookingId"))
                        .map(UUID::fromString);

                if (bookingId.isPresent() && bookingRepo.existsByIdAndSelfCheckInRequestedTrue(bookingId.get()))
                    return new AuthorizationDecision(true);
            } catch (Exception ignored) {/* Ignore any exceptions */}

            return new AuthorizationDecision(false);
        };
    }
}