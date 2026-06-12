package me.diegomcha.autoparte.domain.booking.payment;

import me.diegomcha.autoparte.TestingUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentInfoTest {

    private PaymentInfo payment;
    private PaymentInfo ccPayment;

    @BeforeEach
    void setUp() {
        this.payment = PaymentInfo.of(PaymentInfo.PaymentType.CASH);
        this.ccPayment = PaymentInfo.of(PaymentInfo.PaymentType.CREDIT_CARD);
    }

    @Test
    void testFactoryMethod() {
        Assertions.assertInstanceOf(PaymentInfo.class, this.payment);
        Assertions.assertInstanceOf(CreditCardPaymentInfo.class, this.ccPayment);
    }

    @Test
    void testDateValidation() {
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            PaymentInfo.of(PaymentInfo.PaymentType.CASH);
            PaymentInfo.of(PaymentInfo.PaymentType.GIFT_CARD, null, null, TestingUtils.PAST_INSTANT, null);
            PaymentInfo.of(PaymentInfo.PaymentType.CREDIT_CARD, null, null, TestingUtils.INSTANT, null);
            Assertions.assertThrows(IllegalArgumentException.class, () -> PaymentInfo.of(PaymentInfo.PaymentType.CASH, null, null, TestingUtils.FUTURE_INSTANT, null));
        }
    }

    @Test
    void testCCExpiryDateValidation() {
        PaymentInfo.of(PaymentInfo.PaymentType.CREDIT_CARD, null, null, TestingUtils.INSTANT, null);
        PaymentInfo.of(PaymentInfo.PaymentType.CREDIT_CARD, null, null, null, TestingUtils.INSTANT);
        PaymentInfo.of(PaymentInfo.PaymentType.CREDIT_CARD, null, null, TestingUtils.INSTANT, TestingUtils.INSTANT);
        PaymentInfo.of(PaymentInfo.PaymentType.CREDIT_CARD, null, null, TestingUtils.INSTANT, TestingUtils.FUTURE_INSTANT);
        Assertions.assertThrows(IllegalArgumentException.class, () -> PaymentInfo.of(PaymentInfo.PaymentType.CREDIT_CARD, null, null, TestingUtils.INSTANT, TestingUtils.PAST_INSTANT));
    }
}
