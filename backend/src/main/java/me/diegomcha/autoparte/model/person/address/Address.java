package me.diegomcha.autoparte.model.person.address;

import lombok.*;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode
public class Address {

    public static final Set<String> VALID_COUNTRIES = Set.copyOf(Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3));

//    public static Address of(String addressLine1, String addressLine2, String postalCode, String country) {
// TODO: ...
//    }

    private @NonNull String addressLine1;
    private String addressLine2;
    private @NonNull String municipality;
    private @NonNull String postalCode;
    private @NonNull String country;

    protected Address(@NonNull String addressLine1, String addressLine2, @NonNull String municipality, @NonNull String postalCode, @NonNull String country) {
        this.addressLine1 = Objects.requireNonNull(addressLine1);
        this.addressLine2 = addressLine2;
        this.setMunicipality(municipality);
        this.setPostalCode(postalCode);
        this.setCountry(country);
    }

    protected void setMunicipality(@NonNull String municipality) {
        this.municipality = Objects.requireNonNull(municipality);
    }

    protected void setPostalCode(@NonNull String postalCode) {
        this.postalCode = Objects.requireNonNull(postalCode);
    }

    private void setCountry(@NonNull String country) {
        if (!VALID_COUNTRIES.contains(Objects.requireNonNull(country)))
            throw new IllegalArgumentException("Invalid country code: " + country);

        this.country = country;
    }

}
