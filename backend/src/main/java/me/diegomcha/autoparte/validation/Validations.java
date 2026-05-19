package me.diegomcha.autoparte.validation;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import lombok.NonNull;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class Validations {

    public static final Set<String> VALID_COUNTRIES = Set.copyOf(Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3));

    private static final Pattern NIF_PATTERN = Pattern.compile("^(\\d{8})([A-Z])$");
    private static final Pattern NIE_PATTERN = Pattern.compile("^[XYZ]\\d{7,8}[A-Z]$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MUNICIPALITY_CODE_PATTERN = Pattern.compile("^\\d{5}$");
    private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile("^\\d{5}$");
    private static final PhoneNumberUtil PHONE_UTIL = PhoneNumberUtil.getInstance();

    public static boolean isValidNif(@NonNull String nif) {
        // Check the format of the NIF
        if (!NIF_PATTERN.matcher(nif).matches() && !NIE_PATTERN.matcher(nif).matches())
            return false;

        // Get number part
        int number = Integer.parseInt(
                nif.substring(0, nif.length() - 1)
                        // Handle NIE
                        .replace("X", "0")
                        .replace("Y", "1")
                        .replace("Z", "2"));

        // Compare the control letter with the expected one
        return "TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23) == nif.charAt(8);
    }

    public static boolean isValidEmail(@NonNull String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(@NonNull String phone) {
        try {
            var number = PHONE_UTIL.parse(phone, "ES");
            return PHONE_UTIL.isValidNumber(number);
        } catch (NumberParseException e) {
            return false;
        }
    }

    public static boolean isValidSpanishMunicipalityCode(@NonNull String municipalityCode) {
        return MUNICIPALITY_CODE_PATTERN.matcher(municipalityCode).matches();
    }

    public static boolean isValidSpanishPostalCode(@NonNull String postalCode, @NonNull String municipalityCode) {
        // Check if the postal code matches the expected format
        if (POSTAL_CODE_PATTERN.matcher(postalCode).matches())
            return false;

        // Check if the postal code starts with the municipality code
        return postalCode.startsWith(municipalityCode.substring(0, 2));
    }

    public static boolean isValidCountry(@NonNull String country) {
        return VALID_COUNTRIES.contains(country);
    }

    private Validations() {
        // Private constructor to prevent instantiation
    }
}
