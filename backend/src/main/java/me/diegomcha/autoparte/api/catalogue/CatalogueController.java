package me.diegomcha.autoparte.api.catalogue;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.catalogue.dto.ProvinceMunicipalityCodesDto;
import me.diegomcha.autoparte.api.catalogue.services.CatalogueService;
import me.diegomcha.autoparte.api.catalogue.services.LocationCatalogueService;
import me.diegomcha.autoparte.core.validation.annotations.ProvinceCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/catalogue")
@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
class CatalogueController {

    private final CatalogueService catalogueService;
    private final LocationCatalogueService locationCatalogueService;

    @GetMapping("/countries")
    public Map<String, String> getCountries(Locale locale) {
        return locationCatalogueService.getCountries(locale);
    }

    @GetMapping("/countries/ESP/provinces")
    public Map<String, String> getSpanishProvinces() {
        return locationCatalogueService.getSpanishProvinces();
    }

    @GetMapping("/countries/ESP/provinces/{provinceCode}/municipalities")
    public Map<String, String> getSpanishMunicipalities(@ProvinceCode @PathVariable String provinceCode) {
        return locationCatalogueService.getSpanishMunicipalities(provinceCode);
    }

    @GetMapping("/countries/ESP/provinces/{provinceCode}/municipalities/{municipalityCode}/postal-codes")
    public Set<String> getSpanishPostalCodes(@Valid ProvinceMunicipalityCodesDto dto) {
        return locationCatalogueService.getSpanishPostalCodes(dto.provinceCode(), dto.municipalityCode());
    }

    @GetMapping("/person/genders")
    public Map<String, String> getPersonGenders(Locale locale) {
        return catalogueService.getPersonGenderOptions(locale);
    }

    @GetMapping("/person/relationships")
    public Map<String, String> getPersonRelationships(Locale locale) {
        return catalogueService.getPersonRelationshipOptions(locale);
    }

    @GetMapping("/document/types")
    public Map<String, String> getDocumentTypes(Locale locale) {
        return catalogueService.getDocumentTypeOptions(locale);
    }
}
