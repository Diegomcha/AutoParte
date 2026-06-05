package me.diegomcha.autoparte.core.validation;

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

    public static void ensureValidNif(@NonNull String nif) {
        if (!isValidNif(nif))
            throw new IllegalArgumentException("Invalid NIF format");
    }

    public static void ensureValidEmail(@NonNull String email) {
        if (!EMAIL_PATTERN.matcher(email).matches())
            throw new IllegalArgumentException("Invalid email format");
    }

    public static void ensureValidPhone(@NonNull String phone) {
        if (!isValidPhone(phone))
            throw new IllegalArgumentException("Invalid phone number format");
    }

    public static void ensureValidSpanishMunicipalityCode(@NonNull String municipalityCode) {
        if (!MUNICIPALITY_CODE_PATTERN.matcher(municipalityCode).matches())
            throw new IllegalArgumentException("Invalid Spanish municipality code format");
    }

    public static void ensureValidSpanishPostalCode(@NonNull String postalCode, @NonNull String municipalityCode) {
        if (!POSTAL_CODE_PATTERN.matcher(postalCode).matches())
            throw new IllegalArgumentException("Invalid Spanish postal code format");
        if (!postalCode.startsWith(municipalityCode.substring(0, 2)))
            throw new IllegalArgumentException("Postal code does not match municipality code");
    }

    public static void ensureValidCountry(@NonNull String country) {
        if (!VALID_COUNTRIES.contains(country))
            throw new IllegalArgumentException("Invalid country code");
    }

    private static boolean isValidNif(@NonNull String nif) {
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

    private static boolean isValidPhone(@NonNull String phone) {
        try {
            var number = PHONE_UTIL.parse(phone, "ES");
            return PHONE_UTIL.isValidNumber(number);
        } catch (NumberParseException e) {
            return false;
        }
    }

    private Validations() {
        // Private constructor to prevent instantiation
    }
}
