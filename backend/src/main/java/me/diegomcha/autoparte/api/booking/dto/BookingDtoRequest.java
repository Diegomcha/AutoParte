package me.diegomcha.autoparte.api.booking.dto;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import me.diegomcha.autoparte.core.validation.annotations.StartEndDatesConstraint;
import me.diegomcha.autoparte.domain.booking.payment.Payment;

import java.time.Instant;

@StartEndDatesConstraint
public record BookingDtoRequest(
        @Nonnull Instant startTime,
        @Nonnull Instant endTime,
        @Min(1) int numberOfPeople,
        PaymentDtoRequest payment,
        @Min(1) Integer numberOfRooms,
        Boolean internetConnection
) {
    public record PaymentDtoRequest(
            @Nonnull Payment.PaymentType type,
            String mean,
            String holder,
            @PastOrPresent Instant date,
            @Future Instant expiryDate
    ) {
    }
}
