package me.diegomcha.autoparte.util;

import lombok.NonNull;
import me.diegomcha.autoparte.config.AutoparteProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

// TODO: MOVE!
@Service
public class SpainMunicipalitiesService {

    private final Map<String, String> provinces;
    // Province code -> (Municipality code -> Municipality name)
    private final Map<String, Map<String, String>> municipalities;
    // Province code -> (Municipality code -> Postal codes)
    private final Map<String, Map<String, Set<String>>> postalCodes;

    protected SpainMunicipalitiesService(AutoparteProperties config, ResourceLoader loader) throws IOException {
        String separator = config.getMunicipalities().getSeparator();
        this.provinces = parseProvinces(loader, config.getMunicipalities().getProvincesPath(), separator);
        this.municipalities = parseMunicipalities(loader, config.getMunicipalities().getMunicipalitiesPath(), separator);
        this.postalCodes = parsePostalCodes(loader, config.getMunicipalities().getPostalCodesPath(), separator);
    }

    private @NonNull Map<String, String> parseProvinces(ResourceLoader loader, String resourcePath, String separator) throws IOException {
        Map<String, String> varProvinces = new HashMap<>();
        try (Scanner scanner = new Scanner(loader.getResource(resourcePath).getFile(), StandardCharsets.UTF_8)) {
            scanner.nextLine(); // skip header
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(separator);

                String provinceCode = line[0];
                String provinceName = line[1];
                varProvinces.put(provinceCode, provinceName);
            }
        }

        // Make the map unmodifiable
        return Map.copyOf(varProvinces);
    }

    private @NonNull Map<String, Map<String, String>> parseMunicipalities(ResourceLoader loader, String resourcePath, String separator) throws IOException {
        Map<String, Map<String, String>> varMunicipalities = new HashMap<>();
        try (Scanner scanner = new Scanner(loader.getResource(resourcePath).getFile(), StandardCharsets.UTF_8)) {
            scanner.nextLine(); // skip header
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(separator);

                String provinceCode = line[0];
                String municipalityCode = line[1];
                String municipalityName = line[2];
                varMunicipalities
                        .computeIfAbsent(provinceCode, k -> new HashMap<>())
                        .put(municipalityCode, municipalityName);
            }
        }

        // Make the objets unmodifiable
        varMunicipalities.replaceAll((k, v) -> Map.copyOf(v));
        return Map.copyOf(varMunicipalities);
    }

    private @NonNull Map<String, Map<String, Set<String>>> parsePostalCodes(ResourceLoader loader, String resourcePath, String separator) throws IOException {
        Map<String, Map<String, Set<String>>> varPostalCodes = new HashMap<>();
        try (Scanner scanner = new Scanner(loader.getResource(resourcePath).getFile(), StandardCharsets.UTF_8)) {
            scanner.nextLine(); // skip header
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(separator);

                String provinceCode = line[0].substring(0, 2);
                String municipalityCode = line[0].substring(2);
                String postalCode = line[1];
                varPostalCodes
                        .computeIfAbsent(provinceCode, k -> new HashMap<>())
                        .computeIfAbsent(municipalityCode, k -> new HashSet<>())
                        .add(postalCode);
            }
        }

        // Make the objets unmodifiable
        varPostalCodes.replaceAll((k, v) -> {
            v.replaceAll((mk, mv) -> Set.copyOf(mv));
            return Map.copyOf(v);
        });
        return Map.copyOf(varPostalCodes);
    }

    /**
     * Gets the map of province codes to province names.
     *
     * @return An unmodifiable map of province codes to province names for the specified community.
     */
    public Map<String, String> getProvinces() {
        return provinces;
    }

    /**
     * Gets the map of municipality codes to municipality names for a given province code.
     *
     * @param provinceCode The code of the province for which to retrieve the municipalities.
     * @return An unmodifiable map of municipality codes to municipality names for the specified province.
     * @throws IllegalArgumentException if the provided province code does not exist in the data.
     */
    public Map<String, String> getMunicipalities(String provinceCode) {
        return Optional
                .ofNullable(municipalities.get(provinceCode))
                .orElseThrow(() -> new IllegalArgumentException("Invalid province code: " + provinceCode));
    }

    /**
     * Gets the set of postal codes for a given province & municipality code.
     *
     * @param provinceCode     The code of the province for which to retrieve the postal codes.
     * @param municipalityCode The code of the municipality for which to retrieve the postal codes.
     * @return An unmodifiable set of postal codes for the specified municipality.
     * @throws IllegalArgumentException if the provided province or municipality code does not exist in the data.
     */
    public Set<String> getPostalCodes(String provinceCode, String municipalityCode) {
        return Optional
                .ofNullable(postalCodes.get(provinceCode))
                .map(m -> m.get(municipalityCode))
                .orElseThrow(() -> new IllegalArgumentException("Invalid province or municipality code: " + provinceCode + ", " + municipalityCode));
    }
}
