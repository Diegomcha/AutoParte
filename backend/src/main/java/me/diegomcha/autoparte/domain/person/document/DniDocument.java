package me.diegomcha.autoparte.domain.person.document;

import lombok.*;
import me.diegomcha.autoparte.validation.Validations;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(callSuper = true)
public class DniDocument extends Document {

    private @NonNull String supportNumber;

    protected DniDocument(@NonNull DocumentType type, @NonNull String nif, @NonNull String supportNumber) {
        super(type, nif);
        this.supportNumber = supportNumber;
    }

    @Override
    protected void setType(@NonNull DocumentType type) {
        if (type != DocumentType.NIE && type != DocumentType.NIF)
            throw new IllegalArgumentException("DniDocument type must be NIF or NIE");
        this.type = type;
    }

    @Override
    protected void setNumber(@NonNull String nif) {
        nif = nif.toUpperCase();
        if (!Validations.isValidNif(nif))
            throw new IllegalArgumentException("Invalid NIF number");
        super.setNumber(nif);
    }

    @Override
    public boolean requiresSecondSurname() {
        return this.getType() == DocumentType.NIF;
    }
}
