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
public class Address extends BaseEntity {

    public static Address of(@NonNull String addressLine1, String addressLine2, @NonNull String municipality, @NonNull String postalCode, @NonNull String country) {
        if ("ESP".equals(country))
            return new SpanishAddress(addressLine1, addressLine2, municipality, postalCode, country);
        return new Address(addressLine1, addressLine2, municipality, postalCode, country);
    }
    
    private @NonNull String addressLine1;
    private String addressLine2;
    private @NonNull String municipality;
    private @NonNull String postalCode;
    private @NonNull String country;

    @Setter(AccessLevel.NONE)
    private @NonNull Set<@NonNull Person> people = new HashSet<>();

    public Address(@NonNull String addressLine1, String addressLine2, @NonNull String municipality, @NonNull String postalCode, @NonNull String country) {
        this.setCountry(country);

        this.setMunicipality(municipality);
        this.setPostalCode(postalCode);

        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
    }

    private void setCountry(@NonNull String country) {
        if (!Validations.isValidCountry(country))
            throw new IllegalArgumentException("Invalid country code: " + country);

        this.country = country;
    }

    protected void setPostalCode(@NonNull String postalCode) {
        this.postalCode = postalCode;
    }

    protected void setMunicipality(@NonNull String municipality) {
        this.municipality = municipality;
    }

    public Set<Person> _getPeople() {
        return this.people;
    }

    public Set<Person> getPeople() {
        return Set.copyOf(this.people);
    }

}
