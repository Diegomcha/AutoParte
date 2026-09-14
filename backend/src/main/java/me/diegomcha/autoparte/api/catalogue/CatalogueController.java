package me.diegomcha.autoparte.api.catalogue;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.catalogue.services.CatalogueService;
import me.diegomcha.autoparte.api.catalogue.services.LocationCatalogueService;
import me.diegomcha.autoparte.core.validation.annotations.SpanishProvinceCode;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/catalogue")
@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
class CatalogueController implements CatalogueAPI {

    private final CatalogueService catalogueService;
    private final LocationCatalogueService locationCatalogueService;

    @GetMapping("/countries")
    @Override
    public String[] getCountries() {
        return locationCatalogueService.getCountries();
    }

    @GetMapping("/countries/ESP/provinces")
    @Override
    public Map<String, String> getSpanishProvinces() {
        return locationCatalogueService.getSpanishProvinces();
    }

    @GetMapping("/countries/ESP/provinces/{provinceCode}/municipalities")
    @Override
    public Map<String, String> getSpanishMunicipalities(@PathVariable String provinceCode) {
        return locationCatalogueService.getSpanishMunicipalities(provinceCode);
    }

    @GetMapping("/countries/ESP/provinces/{provinceCode}/municipalities/{municipalityCode}/postal-codes")
    @Override
    public Set<String> getSpanishPostalCodes(@ModelAttribute ProvinceMunicipalityCodesDto codes) {
        return locationCatalogueService.getSpanishPostalCodes(codes.provinceCode(), codes.municipalityCode());
    }

    @GetMapping("/person/genders")
    @Override
    public String[] getPersonGenders() {
        return catalogueService.getPersonGenderOptions();
    }

    @GetMapping("/person/relationships")
    @Override
    public String[] getPersonRelationships() {
        return catalogueService.getPersonRelationshipOptions();
    }

    @GetMapping("/document/types")
    @Override
    public String[] getDocumentTypes() {
        return catalogueService.getDocumentTypeOptions();
    }

    @GetMapping("/payment/types")
    @Override
    public String[] getPaymentTypes() {
        return catalogueService.getPaymentTypeOptions();
    }
}
