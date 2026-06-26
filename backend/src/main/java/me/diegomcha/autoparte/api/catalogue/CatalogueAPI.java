package me.diegomcha.autoparte.api.catalogue;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.diegomcha.autoparte.api.catalogue.dto.ProvinceMunicipalityCodesDto;

import java.util.Map;
import java.util.Set;

@Tag(name = "Catalogue", description = "Operations related to catalogue")
@SuppressWarnings("unused")
interface CatalogueAPI {

    @Operation(summary = "Get valid countries")
    String[] getCountries();

    @Operation(summary = "Get valid Spanish provinces")
    Map<String, String> getSpanishProvinces();

    @Operation(summary = "Get valid Spanish municipalities by province code")
    Map<String, String> getSpanishMunicipalities(String provinceCode);

    @Operation(summary = "Get valid Spanish postal codes by province and municipality codes")
    Set<String> getSpanishPostalCodes(ProvinceMunicipalityCodesDto dto);

    @Operation(summary = "Get valid genders")
    String[] getPersonGenders();

    @Operation(summary = "Get valid relationships")
    String[] getPersonRelationships();

    @Operation(summary = "Get valid document types")
    String[] getDocumentTypes();
}
