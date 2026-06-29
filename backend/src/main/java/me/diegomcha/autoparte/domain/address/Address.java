package me.diegomcha.autoparte.domain.address;

import lombok.*;
import me.diegomcha.autoparte.core.validation.Validations;
import me.diegomcha.autoparte.domain.Person;
import me.diegomcha.autoparte.domain.base.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Address extends BaseEntity {

    /**
     * Factory method to create an Address instance based on the provided country.
     * If the country is "ESP", a SpanishAddress instance is created;
     * otherwise, a generic Address instance is created.
     *
     * @param addressLine1 First line of the address. Must not be null.
     * @param addressLine2 Second line of the address. Can be null.
     * @param municipality Municipality (code for Spain) of the address. Must not be null.
     * @param postalCode   Postal code of the address. Must not be null.
     * @param country      Country code of the address. Must not be null.
     * @return An instance of Address or SpanishAddress based on the country.
     * @throws IllegalArgumentException if any of the required parameters (addressLine1, municipality, postalCode, country) are null
     *                                  or the country code is invalid
     *                                  or the municipality code is invalid for Spain when country is "ESP"
     *                                  or the postal code does not match the municipality.
     */
    public static Address of(@NonNull String addressLine1, String addressLine2, @NonNull String municipality, @NonNull String postalCode, @NonNull String country) {
        if ("ESP".equals(country))
            return new SpanishAddress(addressLine1, addressLine2, municipality, postalCode, country);
        return new Address(addressLine1, addressLine2, municipality, postalCode, country);
    }

    private @NonNull String addressLine1;
    private String addressLine2;
    @Setter(AccessLevel.PROTECTED)
    private @NonNull String municipality;
    @Setter(AccessLevel.PROTECTED)
    private @NonNull String postalCode;
    private @NonNull String country;

    @ToString.Exclude
    private final @NonNull Set<@NonNull Person> people = new HashSet<>();

    protected Address(@NonNull String addressLine1, String addressLine2, @NonNull String municipality, @NonNull String postalCode, @NonNull String country) {
        this.setCountry(country);

        this.setMunicipality(municipality);
        this.setPostalCode(postalCode);

        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
    }

    private void setCountry(@NonNull String country) {
        Validations.ensureValidCountry(country);
        this.country = country;
    }

    public Set<Person> _getPeople() {
        return this.people;
    }

    public Set<Person> getPeople() {
        return Set.copyOf(this.people);
    }
}
