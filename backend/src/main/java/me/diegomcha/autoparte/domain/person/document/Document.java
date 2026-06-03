package me.diegomcha.autoparte.domain.person.document;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode
public class Document {

    public enum DocumentType {
        NIF, // NIF
        NIE, // NIE
        PASSPORT, // PAS
        OTHER // OTRO
    }

    public static Document of(@NonNull DocumentType type, @NonNull String number, String supportNumber) {
        if (type == DocumentType.NIE || type == DocumentType.NIF)
            return new DniDocument(type, number, supportNumber);
        return new Document(type, number);
    }

    protected @NonNull DocumentType type;
    private @NonNull String number;

    protected Document(@NonNull DocumentType type, @NonNull String number) {
        this.setType(type);
        this.setNumber(number);
    }

    protected void setType(@NonNull DocumentType type) {
        if (type == DocumentType.NIE || type == DocumentType.NIF)
            throw new IllegalArgumentException("Use DniDocument for NIF and NIE documents");
        this.type = type;
    }

    protected void setNumber(@NonNull String number) {
        this.number = number;
    }

    public boolean requiresSecondSurname() {
        return false;
    }
}
