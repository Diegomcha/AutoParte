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

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/catalogue")
@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
class CatalogueController {

    private final CatalogueService catalogueService;
    private final LocationCatalogueService locationCatalogueService;

    @GetMapping("/countries")
    public String[] getCountries() {
        return locationCatalogueService.getCountries();
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
    public String[] getPersonGenders() {
        return catalogueService.getPersonGenderOptions();
    }

    @GetMapping("/person/relationships")
    public String[] getPersonRelationships() {
        return catalogueService.getPersonRelationshipOptions();
    }

    @GetMapping("/document/types")
    public String[] getDocumentTypes() {
        return catalogueService.getDocumentTypeOptions();
    }
}
