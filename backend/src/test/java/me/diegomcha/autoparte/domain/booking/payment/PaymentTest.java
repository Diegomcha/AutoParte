package me.diegomcha.autoparte.domain.booking.payment;

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
}
