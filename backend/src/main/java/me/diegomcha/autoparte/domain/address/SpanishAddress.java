package me.diegomcha.autoparte.domain.address;

import lombok.*;
import me.diegomcha.autoparte.validation.Validations;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class SpanishAddress extends Address {

    protected SpanishAddress(@NonNull String addressLine1, String addressLine2, @NonNull String municipality, @NonNull String postalCode, @NonNull String country) {
        super(addressLine1, addressLine2, municipality, postalCode, country);
    }

    @Override
    protected void setPostalCode(@NonNull String postalCode) {
        if (!Validations.isValidSpanishPostalCode(postalCode, this.getMunicipality()))
            throw new IllegalArgumentException("Invalid postal code");
        super.setPostalCode(postalCode);
    }

    @Override
    protected void setMunicipality(@NonNull String municipality) {
        if (!Validations.isValidSpanishMunicipalityCode(municipality))
            throw new IllegalArgumentException("Invalid municipality code");
        super.setMunicipality(municipality);
    }
}
