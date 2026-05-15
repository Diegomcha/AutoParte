package me.diegomcha.autoparte.model.booking.payment;

import lombok.*;

import java.time.Instant;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode
public class Payment {

    public enum PaymentType {
        CASH, // EFECT
        CREDIT_CARD, // TARJT
        PLATFORM, // PLATF
        TRANSFER, // TRANS
        MOBILE, // MOVIL
        GIFT_CARD, // TREG
        ON_SITE, // DESTI
        OTHER // OTRO
    }

    public static Payment create(@NonNull PaymentType type, String mean, String holder, Instant date, Instant expiryDate) {
        if (type == PaymentType.CREDIT_CARD)
            return new CreditCardPayment(mean, holder, date, expiryDate);
        return new Payment(type, mean, holder, date);
    }

    private @NonNull PaymentType type;
    private String mean;
    private String holder;
    private Instant date;

    protected Payment(@NonNull PaymentType type, String mean, String holder, Instant date) {
        if (type == PaymentType.CREDIT_CARD)
            throw new IllegalArgumentException("Use CreditCardPayment for credit card payments");

        this.type = Objects.requireNonNull(type);
        this.mean = mean;
        this.holder = holder;
        this.date = date;
    }

}
