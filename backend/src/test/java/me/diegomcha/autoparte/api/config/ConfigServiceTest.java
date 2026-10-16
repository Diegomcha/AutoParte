package me.diegomcha.autoparte.api.config;

import me.diegomcha.autoparte.api.config.dto.ConfigDtoRequest;
import me.diegomcha.autoparte.config.DynamicConfigService;
import me.diegomcha.autoparte.core.exception.BadConfigurationException;
import me.diegomcha.autoparte.core.exception.ServiceUnavailableException;
import me.diegomcha.autoparte.core.repos.ConfigRepo;
import me.diegomcha.autoparte.integration.ses.SesClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class ConfigServiceTest {
    @Autowired
    private ConfigService service;
    @Autowired
    private DynamicConfigService dynamicConfigService;
    @Autowired
    private ConfigRepo configRepo;
    @MockitoBean
    private SesClient sesClient;

    @Test
    void testGetConfig() {
        var config = service.getConfig();

        Assertions.assertEquals("test", config.sesUsername());
        Assertions.assertNotNull(config.sesPassword());
        Assertions.assertEquals("00000", config.sesLandlordCode());
        Assertions.assertTrue(config.sesCredentialsValid());
        Assertions.assertFalse(config.digitalSignatureEnabled());
        Assertions.assertFalse(config.manualReviewEnabled());
        Assertions.assertEquals(365, config.logsRetentionDays());
        Assertions.assertEquals(1095, config.bookingsRetentionDays());
    }

    @Test
    void testGetPublicConfig() {
        var config = service.getPublicConfig();

        Assertions.assertFalse(config.digitalSignatureEnabled());
    }

    @Test
    void testUpdateConfig() {
        var update = new ConfigDtoRequest(
                "username",
                "password",
                "landlord-code",
                true,
                true,
                30,
                60
        );

        service.updateConfig(update);

        var config = service.getConfig();
        Assertions.assertEquals(update.sesUsername(), config.sesUsername());
        Assertions.assertEquals(update.sesPassword(), config.sesPassword());
        Assertions.assertEquals(update.sesLandlordCode(), config.sesLandlordCode());
        Assertions.assertTrue(config.digitalSignatureEnabled());
        Assertions.assertTrue(config.manualReviewEnabled());
        Assertions.assertEquals(update.logsRetentionDays(), config.logsRetentionDays());
        Assertions.assertEquals(update.bookingsRetentionDays(), config.bookingsRetentionDays());
        Assertions.assertFalse(config.sesCredentialsValid());
        Assertions.assertEquals(1, configRepo.count());
    }

    @Test
    void testValidateSesCredentials() throws BadConfigurationException, ServiceUnavailableException {
        Mockito.doNothing().when(sesClient).checkConnection();

        service.validateSesCredentials();

        Assertions.assertTrue(dynamicConfigService.getConfig().isSesCredentialsValid());
        Mockito.verify(sesClient).checkConnection();
    }

    @Test
    void testValidateSesCredentialsWithoutCredentials() {
        service.updateConfig(new ConfigDtoRequest(null, null, null, false, false, 365, 1095));

        var exception = Assertions.assertThrows(BadConfigurationException.class, service::validateSesCredentials);

        Assertions.assertEquals(
                BadConfigurationException.BadConfigurationType.NO_CREDENTIALS_PROVIDED.name(),
                exception.getMessage()
        );
        Mockito.verifyNoInteractions(sesClient);
    }
}
