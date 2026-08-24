package me.diegomcha.autoparte.api.booking;

import jakarta.persistence.criteria.Path;
import me.diegomcha.autoparte.domain.Booking;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public class BookingSpecs {
    private BookingSpecs() {
        /* This utility class should not be instantiated */
    }

    public static Specification<Booking> ofAccommodationWithDatesBetween(UUID accommodationId, Instant rangeStart, Instant rangeEnd) {
        return (root, query, cb) -> {
            Path<UUID> accommodationIdPath = root.get("accommodation").get("id");
            Path<Instant> startTime = root.get("startTime");
            Path<Instant> endTime = root.get("endTime");

            var outsideRange = cb.disjunction();
            if (rangeStart != null) // Check if the booking ends before the range starts
                outsideRange = cb.or(outsideRange, cb.lessThan(endTime, rangeStart));

            if (rangeEnd != null) // Check if the booking starts after the range ends
                outsideRange = cb.or(outsideRange, cb.greaterThan(startTime, rangeEnd));

            return cb.and(
                    cb.equal(accommodationIdPath, accommodationId),
                    cb.not(outsideRange) // Must not be outside the range
            );
        };
    }
}
