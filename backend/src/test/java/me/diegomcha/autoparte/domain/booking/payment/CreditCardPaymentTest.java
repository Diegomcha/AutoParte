package me.diegomcha.autoparte.domain.booking.payment;

import me.diegomcha.autoparte.TestingUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreditCardPaymentTest {

    private Payment ccPayment;

    @BeforeEach
    void setUp() {
        this.ccPayment = Payment.of(Payment.PaymentType.CREDIT_CARD, null, null, null, null);
    }


    @Test
    void testFactoryMethod() {
        Assertions.assertSame(CreditCardPayment.class, ccPayment.getClass());
    }

    @Test
    void testCCExpiryDateValidation() {
        Payment.of(Payment.PaymentType.CREDIT_CARD, null, null, TestingUtils.INSTANT, null);
        Payment.of(Payment.PaymentType.CREDIT_CARD, null, null, null, TestingUtils.INSTANT);
        Payment.of(Payment.PaymentType.CREDIT_CARD, null, null, TestingUtils.INSTANT, TestingUtils.INSTANT);
        Payment.of(Payment.PaymentType.CREDIT_CARD, null, null, TestingUtils.INSTANT, TestingUtils.FUTURE_INSTANT);
        Assertions.assertThrows(IllegalArgumentException.class, () -> Payment.of(Payment.PaymentType.CREDIT_CARD, null, null, TestingUtils.INSTANT, TestingUtils.PAST_INSTANT));
    }
}
