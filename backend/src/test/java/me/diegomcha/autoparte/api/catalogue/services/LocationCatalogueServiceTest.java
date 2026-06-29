package me.diegomcha.autoparte.api.catalogue.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class LocationCatalogueServiceTest {

    @Autowired
    private LocationCatalogueService locationCatalogueService;

    @Test
    void testGetCountries() {
        var countries = locationCatalogueService.getCountries();

        Assertions.assertNotNull(countries);
        Assertions.assertEquals(249, countries.length); // There are 249 ISO 3166-1 alpha-3 country codes
        Assertions.assertTrue(Arrays.stream(countries).allMatch(code -> code.length() == 3)); // All codes should be 3 characters long
    }

    @Test
    void testGetSpanishProvinces() {
        var provinces = locationCatalogueService.getSpanishProvinces();

        Assertions.assertNotNull(provinces);
        Assertions.assertFalse(provinces.isEmpty());
    }

    @Test
    void testGetSpanishMunicipalities() {
        var municipalities = locationCatalogueService.getSpanishMunicipalities("28"); // Madrid

        Assertions.assertNotNull(municipalities);
        Assertions.assertFalse(municipalities.isEmpty());

        Assertions.assertThrows(IllegalArgumentException.class, () -> locationCatalogueService.getSpanishMunicipalities("99")); // Invalid province code
        Assertions.assertThrows(IllegalArgumentException.class, () -> locationCatalogueService.getSpanishMunicipalities("code")); // Invalid province code
        Assertions.assertThrows(IllegalArgumentException.class, () -> locationCatalogueService.getSpanishMunicipalities("")); // Invalid province code
    }

    @Test
    void testGetSpanishPostalCodes() {
        var postalCodes = locationCatalogueService.getSpanishPostalCodes("28", "079"); // Madrid -> Madrid

        Assertions.assertNotNull(postalCodes);
        Assertions.assertFalse(postalCodes.isEmpty());

        Assertions.assertThrows(IllegalArgumentException.class, () -> locationCatalogueService.getSpanishPostalCodes("99", "079")); // Invalid province code
        Assertions.assertThrows(IllegalArgumentException.class, () -> locationCatalogueService.getSpanishPostalCodes("28", "999")); // Invalid municipality code
        Assertions.assertThrows(IllegalArgumentException.class, () -> locationCatalogueService.getSpanishPostalCodes("28", "code")); // Invalid municipality code
        Assertions.assertThrows(IllegalArgumentException.class, () -> locationCatalogueService.getSpanishPostalCodes("28", "")); // Invalid municipality code
    }
}
