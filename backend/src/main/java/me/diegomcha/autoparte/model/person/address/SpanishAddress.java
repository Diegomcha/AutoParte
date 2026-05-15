package me.diegomcha.autoparte.model.person.address;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(callSuper = true)
public class SpanishAddress extends Address {

    protected SpanishAddress(@NonNull String addressLine1, String addressLine2, @NonNull String municipality, @NonNull String postalCode, @NonNull String country) {
        super(addressLine1, addressLine2, municipality, postalCode, country);
    }

    @Override
    protected void setPostalCode(@NonNull String postalCode) {
        // TODO: validation...
        super.setPostalCode(postalCode);
    }

    @Override
    protected void setMunicipality(@NonNull String municipality) {
        // TODO: validation & conversion to INE municipality code...
        super.setMunicipality(municipality);
    }
}
