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

            var startTimePredicate = cb.conjunction();
            var endTimePredicate = cb.conjunction();
            if (rangeStart != null) {
                startTimePredicate = cb.and(startTimePredicate, cb.greaterThanOrEqualTo(startTime, rangeStart));
                endTimePredicate = cb.and(endTimePredicate, cb.greaterThanOrEqualTo(endTime, rangeStart));
            }
            if (rangeEnd != null) {
                startTimePredicate = cb.and(startTimePredicate, cb.lessThanOrEqualTo(startTime, rangeEnd));
                endTimePredicate = cb.and(endTimePredicate, cb.lessThanOrEqualTo(endTime, rangeEnd));
            }

            return cb.and(cb.equal(accommodationIdPath, accommodationId), cb.or(startTimePredicate, endTimePredicate));
        };
    }
}
