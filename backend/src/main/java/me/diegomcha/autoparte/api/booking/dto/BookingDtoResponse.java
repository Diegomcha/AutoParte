package me.diegomcha.autoparte.api.booking.dto;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.booking.payment.Payment;
import me.diegomcha.autoparte.domain.communication.Communication;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record BookingDtoResponse(
        @Nonnull UUID id,
        @Nonnull Instant createdAt,
        @Nonnull Instant updatedAt,

        @Nonnull UUID createdBy,
        @Nonnull UUID lastModifiedBy,

        @Nonnull Booking.BookingStatus status,
        @NotNull boolean selfCheckInRequested,
        @NotNull boolean canBeModified,
        @NotNull boolean canBeDeleted,

        @Nonnull Instant startTime,
        @Nonnull Instant endTime,
        @NotNull int numberOfPeople,
        PaymentDtoResponse payment,
        Integer numberOfRooms,
        Boolean internetConnection,

        String holderName,

        @Nonnull Set<CommunicationDtoResponse> communications
) {
    public record PaymentDtoResponse(
            @Nonnull Payment.PaymentType type,
            String mean,
            String holder,
            Instant date,
            Instant expiryDate
    ) {
    }

    public record CommunicationDtoResponse(
            @Nonnull UUID id,
            @Nonnull Communication.CommunicationType type,
            @Nonnull Communication.CommunicationStatus status,
            Instant sentTimestamp,
            String error
    ) {
    }
}
