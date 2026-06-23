package me.diegomcha.autoparte.api.booking.dto;

import lombok.NonNull;
import me.diegomcha.autoparte.domain.booking.payment.Payment;

import java.time.Instant;


public record BookingDtoRequest(
        @NonNull Instant startTime,
        @NonNull Instant endTime,
        int numberOfPeople,
        PaymentDtoRequest payment,
        Integer numberOfRooms,
        Boolean internetConnection
) {
    public record PaymentDtoRequest(
            @NonNull Payment.PaymentType type,
            String mean,
            String holder,
            Instant date,
            Instant expiryDate
    ) {
    }
}
