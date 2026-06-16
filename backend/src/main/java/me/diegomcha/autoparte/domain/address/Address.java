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
