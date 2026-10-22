package me.diegomcha.autoparte.api.booking.dto;

import jakarta.validation.constraints.NotNull;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.booking.payment.Payment;

import java.time.Instant;
import java.util.UUID;

public record BookingDtoResponse(
        @NotNull UUID id,
        @NotNull Instant createdAt,
        @NotNull Instant updatedAt,

        @NotNull UUID createdBy,
        @NotNull UUID lastModifiedBy,

        @NotNull Booking.BookingStatus status,
        @NotNull boolean selfCheckInRequested,

        @NotNull boolean canBeModified,
        @NotNull boolean canBeDeleted,
        @NotNull boolean canBeConfirmed,
        @NotNull boolean canBeCheckedIn,
        @NotNull boolean canBeCancelled,
        @NotNull boolean canSelfCheckInBeRequested,

        @NotNull Instant startTime,
        @NotNull Instant endTime,
        @NotNull int numberOfPeople,
        PaymentDtoResponse payment,
        Integer numberOfRooms,
        Boolean internetConnection,

        String holderName,
        @NotNull String accommodationName
) {
    public record PaymentDtoResponse(
            @NotNull Payment.PaymentType type,
            String mean,
            String holder,
            Instant date,
            Instant expiryDate
    ) {
    }
}
