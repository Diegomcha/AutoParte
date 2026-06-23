package me.diegomcha.autoparte.domain.person.document;

import lombok.*;
import me.diegomcha.autoparte.domain.base.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Document extends BaseEntity {

    public enum DocumentType {
        NIF, // NIF
        NIE, // NIE
        PASSPORT, // PAS
        OTHER // OTRO
    }

    public static Document of(@NonNull DocumentType type, @NonNull String number) {
        return of(type, number, null);
    }

    public static Document of(@NonNull DocumentType type, @NonNull String number, String supportNumber) {
        if (type == DocumentType.NIE || type == DocumentType.NIF)
            return new DniDocument(type, number, supportNumber);
        return new Document(type, number);
    }

    private @NonNull DocumentType type;
    @Setter(AccessLevel.PROTECTED)
    private @NonNull String number;

    protected Document(@NonNull DocumentType type, @NonNull String number) {
        this.type = type;
        this.setNumber(number);
    }

    public boolean requiresSecondSurname() {
        return false;
    }
}
