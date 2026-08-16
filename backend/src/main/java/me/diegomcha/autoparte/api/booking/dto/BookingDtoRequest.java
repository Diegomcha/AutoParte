package me.diegomcha.autoparte.api.booking.dto;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import me.diegomcha.autoparte.core.validation.annotations.ExpiryDateAfterDateConstraint;
import me.diegomcha.autoparte.core.validation.annotations.StartEndDatesConstraint;
import me.diegomcha.autoparte.domain.booking.payment.Payment;

import java.time.Instant;

@StartEndDatesConstraint
public record BookingDtoRequest(
        @Nonnull Instant startTime,
        @Nonnull Instant endTime,
        @NotNull @Min(1) int numberOfPeople,
        PaymentDtoRequest payment,
        @Min(1) Integer numberOfRooms,
        Boolean internetConnection
) {
    @ExpiryDateAfterDateConstraint
    public record PaymentDtoRequest(
            @Nonnull Payment.PaymentType type,
            String mean,
            String holder,
            Instant date,
            Instant expiryDate
    ) {
    }
}
