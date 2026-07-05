package me.diegomcha.autoparte.api.catalogue.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class CatalogueServiceTest {

    @Autowired
    private CatalogueService catalogueService;

    @Test
    void testGetPersonGenderOptions() {
        var options = catalogueService.getPersonGenderOptions();

        Assertions.assertNotNull(options);
        Assertions.assertTrue(options.length > 0);
    }

    @Test
    void testGetPersonRelationshipOptions() {
        var options = catalogueService.getPersonRelationshipOptions();

        Assertions.assertNotNull(options);
        Assertions.assertTrue(options.length > 0);
    }

    @Test
    void testGetDocumentTypeOptions() {
        var options = catalogueService.getDocumentTypeOptions();

        Assertions.assertNotNull(options);
        Assertions.assertTrue(options.length > 0);
    }

    @Test
    void testGetPaymentTypeOptions() {
        var options = catalogueService.getPaymentTypeOptions();

        Assertions.assertNotNull(options);
        Assertions.assertTrue(options.length > 0);
    }
}
