package me.diegomcha.autoparte.api.catalogue;

import me.diegomcha.autoparte.api.BaseControllerTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CatalogueControllerTest extends BaseControllerTest {

    @Test
    void catalogueOptionsArePubliclyAvailable() throws Exception {
        mockMvc.perform(get("/api/catalogue/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(249));

        mockMvc.perform(get("/api/catalogue/person/genders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", greaterThan(0)));

        mockMvc.perform(get("/api/catalogue/person/relationships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", greaterThan(0)));

        mockMvc.perform(get("/api/catalogue/document/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", greaterThan(0)));

        mockMvc.perform(get("/api/catalogue/payment/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", greaterThan(0)));
    }

    @Test
    void SpanishLocationCataloguesAreAvailable() throws Exception {
        mockMvc.perform(get("/api/catalogue/countries/ESP/provinces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['28']").value("Madrid"));

        mockMvc.perform(get("/api/catalogue/countries/ESP/provinces/28/municipalities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['079']").value("Madrid"));

        mockMvc.perform(get("/api/catalogue/countries/ESP/provinces/28/municipalities/079/postal-codes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", greaterThan(0)));
    }

    @Test
    void invalidSpanishLocationParametersAreRejected() throws Exception {
        mockMvc.perform(get("/api/catalogue/countries/ESP/provinces/99/municipalities"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/catalogue/countries/ESP/provinces/invalid/municipalities"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/catalogue/countries/ESP/provinces/28/municipalities/999/postal-codes"))
                .andExpect(status().isBadRequest());
    }
}
