package me.diegomcha.autoparte.domain.person.document;

import lombok.*;
import me.diegomcha.autoparte.core.validation.Validations;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DniDocumentInfo extends DocumentInfo {

    private @NonNull String supportNumber;

    protected DniDocumentInfo(@NonNull DocumentType type, @NonNull String nif, @NonNull String supportNumber) {
        super(type, nif);
        this.supportNumber = supportNumber;
    }

    @Override
    protected void setNumber(@NonNull String nif) {
        Validations.ensureValidNif(nif);
        super.setNumber(nif);
    }

    @Override
    public boolean requiresSecondSurname() {
        return this.getType() == DocumentType.NIF;
    }
}
