package me.diegomcha.autoparte.api.booking.dto;

import jakarta.annotation.Nonnull;
import lombok.NonNull;
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
        boolean published,
        boolean canBeModified,

        @Nonnull Instant startTime,
        @Nonnull Instant endTime,
        int numberOfPeople,
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
            Instant sentTimestamp
    ) {
    }
}
