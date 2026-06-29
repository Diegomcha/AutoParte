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

    /**
     * Factory method to create a Payment instance based on the provided payment type.
     * If the payment type is CREDIT_CARD, a CreditCardPayment instance is created.
     * Otherwise, a generic Payment instance is created.
     *
     * @param type       The type of payment (e.g., CASH, CREDIT_CARD, PLATFORM, etc.). Must not be null.
     * @param mean       The payment mean (e.g., card number, platform name, etc.). Can be null.
     * @param holder     The name of the payment holder. Can be null.
     * @param date       The date of the payment. Can be null.
     * @param expiryDate The expiry date of the payment method (only relevant for CREDIT_CARD type). Can be null.
     * @return A Payment instance (either CreditCardPayment or generic Payment) based on the provided type.
     * @throws IllegalArgumentException if type is null or
     *                                  the date is in the future or
     *                                  the payment type is CREDIT_CARD and the expiryDate is null or in the past.
     */
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
