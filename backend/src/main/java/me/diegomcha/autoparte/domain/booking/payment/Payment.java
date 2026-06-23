package me.diegomcha.autoparte.domain.booking.payment;

import lombok.*;
import me.diegomcha.autoparte.domain.base.BaseEntity;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Payment extends BaseEntity {

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

    public static Payment of(@NonNull PaymentType type) {
        return of(type, null, null, null, null);
    }

    public static Payment of(@NonNull PaymentType type, String mean, String holder, Instant date, Instant expiryDate) {
        if (type == PaymentType.CREDIT_CARD)
            return new CreditCardPayment(mean, holder, date, expiryDate);
        return new Payment(type, mean, holder, date);
    }

    private @NonNull PaymentType type;
    private String mean;
    private String holder;
    private Instant date;

    protected Payment(@NonNull PaymentType type, String mean, String holder, Instant date) {
        this.type = type;
        this.mean = mean;
        this.holder = holder;
        this.setDate(date);
    }

    private void setDate(Instant date) {
        if (date != null && date.isAfter(Instant.now()))
            throw new IllegalArgumentException("Payment date cannot be in the future");

        this.date = date;
    }
}
