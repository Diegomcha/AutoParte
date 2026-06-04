package me.diegomcha.autoparte.domain.person.document;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode
public class DocumentInfo {

    public enum DocumentType {
        NIF, // NIF
        NIE, // NIE
        PASSPORT, // PAS
        OTHER // OTRO
    }

    public static DocumentInfo of(@NonNull DocumentType type, @NonNull String number, String supportNumber) {
        if (type == DocumentType.NIE || type == DocumentType.NIF)
            return new DniDocument(type, number, supportNumber);
        return new DocumentInfo(type, number);
    }

    private @NonNull DocumentType type;
    @Setter(AccessLevel.PROTECTED)
    private @NonNull String number;

    protected DocumentInfo(@NonNull DocumentType type, @NonNull String number) {
        this.type = type;
        this.setNumber(number);
    }

    public boolean requiresSecondSurname() {
        return false;
    }
}
