package me.diegomcha.autoparte.api.booking.dto;

import lombok.NonNull;
import me.diegomcha.autoparte.domain.booking.payment.PaymentInfo;

import java.time.Instant;


public record BookingDtoRequest(
        @NonNull Instant startTime,
        @NonNull Instant endTime,
        int numberOfPeople,
        @NonNull BookingDtoRequest.PaymentInfoDtoRequest payment,
        Integer numberOfRooms,
        Boolean internetConnection
) {
    public record PaymentInfoDtoRequest(
            @NonNull PaymentInfo.PaymentType type,
            String mean,
            String holder,
            Instant date,
            Instant expiryDate
    ) {
    }
}
