package me.diegomcha.autoparte.api.booking.dto;

import jakarta.annotation.Nonnull;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.booking.payment.PaymentInfo;
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
        boolean canBeConfirmed,
        boolean canBeCheckedIn,

        @Nonnull Instant startTime,
        @Nonnull Instant endTime,
        int numberOfPeople,
        @Nonnull PaymentInfoDtoResponse payment,
        Integer numberOfRooms,
        Boolean internetConnection,

        @Nonnull Set<CommunicationDtoResponse> communications
) {
    public record PaymentInfoDtoResponse(
            @Nonnull PaymentInfo.PaymentType type,
            String mean,
            String holder,
            Instant date,
            Instant expiryDate
    ) {
    }

    public record CommunicationDtoResponse(
            @Nonnull Communication.CommunicationType type,
            @Nonnull Communication.CommunicationStatus status,
            Instant sentTimestamp
    ) {
    }
}
