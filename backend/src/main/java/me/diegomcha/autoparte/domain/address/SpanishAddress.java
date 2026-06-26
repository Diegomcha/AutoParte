package me.diegomcha.autoparte.domain.address;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.diegomcha.autoparte.core.validation.Validations;

@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class SpanishAddress extends Address {
    protected SpanishAddress(@NonNull String addressLine1, String addressLine2, @NonNull String municipality, @NonNull String postalCode, @NonNull String country) {
        super(addressLine1, addressLine2, municipality, postalCode, country);
    }

    @Override
    protected void setPostalCode(@NonNull String postalCode) {
        Validations.ensureValidSpanishPostalCode(postalCode, this.getMunicipality());
        super.setPostalCode(postalCode);
    }

    @Override
    protected void setMunicipality(@NonNull String municipality) {
        Validations.ensureValidSpanishMunicipalityCode(municipality);
        super.setMunicipality(municipality);
    }
}
