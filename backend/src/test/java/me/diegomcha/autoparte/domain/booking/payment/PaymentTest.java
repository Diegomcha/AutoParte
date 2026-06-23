package me.diegomcha.autoparte.domain.booking.payment;

import me.diegomcha.autoparte.TestingUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// TODO: Split this test class into multiple classes for each payment type
class PaymentTest {

    private Payment payment;
    private Payment ccPayment;

    @BeforeEach
    void setUp() {
        this.payment = Payment.of(Payment.PaymentType.CASH);
        this.ccPayment = Payment.of(Payment.PaymentType.CREDIT_CARD);
    }

    @Test
    void testFactoryMethod() {
        Assertions.assertInstanceOf(Payment.class, this.payment);
        Assertions.assertInstanceOf(CreditCardPayment.class, this.ccPayment);
    }

    @Test
    void testDateValidation() {
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            Payment.of(Payment.PaymentType.CASH);
            Payment.of(Payment.PaymentType.GIFT_CARD, null, null, TestingUtils.PAST_INSTANT, null);
            Payment.of(Payment.PaymentType.CREDIT_CARD, null, null, TestingUtils.INSTANT, null);
            Assertions.assertThrows(IllegalArgumentException.class, () -> Payment.of(Payment.PaymentType.CASH, null, null, TestingUtils.FUTURE_INSTANT, null));
        }
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
