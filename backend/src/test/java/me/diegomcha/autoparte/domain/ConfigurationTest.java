package me.diegomcha.autoparte.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConfigurationTest {

    @Test
    void testConstructorContract() {
        var config = Configuration._blank();

        Assertions.assertFalse(config.isDigitalSignatureEnabled());
        Assertions.assertFalse(config.isManualReviewEnabled());

        Assertions.assertEquals(1 * 365, config.getLogsRetentionDays());
        Assertions.assertEquals(3 * 365, config.getBookingsRetentionDays());

        Assertions.assertFalse(config.isSesCredentialsValid());

        Assertions.assertNotNull(config.getRememberMeKey());
    }

    @Test
    void testIsSesConfigured() {
        var config = Configuration._blank();
        Assertions.assertFalse(config.isSesConfigured());

        config.setSesUsername("USERNAME");
        Assertions.assertFalse(config.isSesConfigured());

        config.setSesPassword("PASSWORD");
        Assertions.assertFalse(config.isSesConfigured());

        config.setSesLandlordCode("CODE");
        Assertions.assertTrue(config.isSesConfigured());
    }
}
