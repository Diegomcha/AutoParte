package me.diegomcha.autoparte.model.booking.payment;

import lombok.*;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(callSuper = true)
public class CreditCardPayment extends Payment {

    private Instant expiryDate;

    protected CreditCardPayment(String mean, String holder, Instant date, Instant expiryDate) {
        super(PaymentType.CREDIT_CARD, mean, holder, date);
        this.setExpiryDate(expiryDate);
    }

    public void setExpiryDate(Instant expiryDate) {
        if (expiryDate != null && this.getDate() != null && expiryDate.isBefore(this.getDate()))
            throw new IllegalArgumentException("Expiry date must be after payment date");

        this.expiryDate = expiryDate;
    }
}
