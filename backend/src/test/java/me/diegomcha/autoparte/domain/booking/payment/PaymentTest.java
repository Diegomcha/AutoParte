package me.diegomcha.autoparte.domain.booking.payment;

import me.diegomcha.autoparte.TestingUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentTest {

    private Payment payment;

    @BeforeEach
    void setUp() {
        this.payment = Payment.of(Payment.PaymentType.CASH,null,null,null,null);
    }

    @Test
    void testFactoryMethod() {
        Assertions.assertSame(Payment.class, payment.getClass());

    }

    @Test
    void testDateValidation() {
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            Payment.of(Payment.PaymentType.CASH, null, null, null,null);
            Payment.of(Payment.PaymentType.GIFT_CARD, null, null, TestingUtils.PAST_INSTANT, null);
            Payment.of(Payment.PaymentType.CREDIT_CARD, null, null, TestingUtils.INSTANT, null);
            Assertions.assertThrows(IllegalArgumentException.class, () -> Payment.of(Payment.PaymentType.CASH, null, null, TestingUtils.FUTURE_INSTANT, null));
        }
    }
}
