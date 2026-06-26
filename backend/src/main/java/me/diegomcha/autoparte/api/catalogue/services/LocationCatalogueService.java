package me.diegomcha.autoparte.api.catalogue.services;

import lombok.Getter;
import lombok.NonNull;
import me.diegomcha.autoparte.config.AutoparteProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class LocationCatalogueService {

    @Getter
    private final String[] countries = Locale.getISOCountries();

    @Getter
    private final Map<String, String> spanishProvinces;
    // Province code -> (Municipality code -> Municipality name)
    private final Map<String, Map<String, String>> spanishMunicipalities;
    // Province code -> (Municipality code -> Postal codes)
    private final Map<String, Map<String, Set<String>>> spanishPostalCodes;

    // TODO: MAYBE move this!

    protected LocationCatalogueService(AutoparteProperties config, ResourceLoader loader) throws IOException {
        String separator = config.getLocationCatalogue().getSeparator();
        this.spanishProvinces = parseSpanishProvinces(loader, config.getLocationCatalogue().getProvincesPath(), separator);
        this.spanishMunicipalities = parseSpanishMunicipalities(loader, config.getLocationCatalogue().getMunicipalitiesPath(), separator);
        this.spanishPostalCodes = parseSpanishPostalCodes(loader, config.getLocationCatalogue().getPostalCodesPath(), separator);
    }

    private Map<String, String> parseSpanishProvinces(ResourceLoader loader, String resourcePath, String separator) throws IOException {
        Map<String, String> provinces = new HashMap<>();
        try (Scanner scanner = new Scanner(loader.getResource(resourcePath).getFile(), StandardCharsets.UTF_8)) {
            scanner.nextLine(); // skip header
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(separator);

                String provinceCode = line[0];
                String provinceName = line[1];
                provinces.put(provinceCode, provinceName);
            }
        }

        // Make the map unmodifiable
        return Map.copyOf(provinces);
    }

    private Map<String, Map<String, String>> parseSpanishMunicipalities(ResourceLoader loader, String resourcePath, String separator) throws IOException {
        Map<String, Map<String, String>> municipalities = new HashMap<>();
        try (Scanner scanner = new Scanner(loader.getResource(resourcePath).getFile(), StandardCharsets.UTF_8)) {
            scanner.nextLine(); // skip header
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(separator);

                String provinceCode = line[0];
                String municipalityCode = line[1];
                String municipalityName = line[2];
                municipalities
                        .computeIfAbsent(provinceCode, k -> new HashMap<>())
                        .put(municipalityCode, municipalityName);
            }
        }

        // Make the objets unmodifiable
        municipalities.replaceAll((k, v) -> Map.copyOf(v));
        return Map.copyOf(municipalities);
    }

    private Map<String, Map<String, Set<String>>> parseSpanishPostalCodes(ResourceLoader loader, String resourcePath, String separator) throws IOException {
        Map<String, Map<String, Set<String>>> postalCodes = new HashMap<>();
        try (Scanner scanner = new Scanner(loader.getResource(resourcePath).getFile(), StandardCharsets.UTF_8)) {
            scanner.nextLine(); // skip header
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(separator);

                String provinceCode = line[0].substring(0, 2);
                String municipalityCode = line[0].substring(2);
                String postalCode = line[1];
                postalCodes
                        .computeIfAbsent(provinceCode, k -> new HashMap<>())
                        .computeIfAbsent(municipalityCode, k -> new HashSet<>())
                        .add(postalCode);
            }
        }

        // Make the objets unmodifiable
        postalCodes.replaceAll((k, v) -> {
            v.replaceAll((mk, mv) -> Set.copyOf(mv));
            return Map.copyOf(v);
        });
        return Map.copyOf(postalCodes);
    }

    // --------------------

    /**
     * Finds the Spanish province code corresponding to a given municipality code.
     *
     * @param municipalityCode The code of the municipality for which to find the corresponding province.
     * @return The code of the province corresponding to the given municipality code.
     * @throws IllegalArgumentException if the provided municipality code does not exist in the data.
     */
    public String findSpanishProvinceFromMunicipalityCode(@NonNull String municipalityCode) {
        return spanishMunicipalities.entrySet().stream()
                .filter(entry -> entry.getValue().containsKey(municipalityCode))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid municipality code: " + municipalityCode));
    }

    /**
     * Gets the map of municipality codes to municipality names for a given province code in Spain.
     *
     * @param provinceCode The code of the province for which to retrieve the municipalities.
     * @return An unmodifiable map of municipality codes to municipality names for the specified province.
     * @throws IllegalArgumentException if the provided province code does not exist in the data.
     */
    public Map<String, String> getSpanishMunicipalities(@NonNull String provinceCode) {
        return Optional
                .ofNullable(spanishMunicipalities.get(provinceCode))
                .orElseThrow(() -> new IllegalArgumentException("Invalid province code: " + provinceCode));
    }

    /**
     * Gets the set of postal codes for a given province & municipality code in Spain.
     *
     * @param provinceCode     The code of the province for which to retrieve the postal codes.
     * @param municipalityCode The code of the municipality for which to retrieve the postal codes.
     * @return An unmodifiable set of postal codes for the specified municipality.
     * @throws IllegalArgumentException if the provided province or municipality code does not exist in the data.
     */
    public Set<String> getSpanishPostalCodes(@NonNull String provinceCode, @NonNull String municipalityCode) {
        return Optional
                .ofNullable(spanishPostalCodes.get(provinceCode))
                .map(m -> m.get(municipalityCode))
                .orElseThrow(() -> new IllegalArgumentException("Invalid province or municipality code: " + provinceCode + ", " + municipalityCode));
    }
}
