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

    /**
     * Factory method to create a Document instance based on the provided type, number, and optional support number.
     *
     * @param type          DocumentType enum value representing the type of document (NIF, NIE, PASSPORT, OTHER).
     * @param number        String representing the document number. Must not be null.
     * @param supportNumber Optional String representing a support number for certain document types (e.g., NIE, NIF). Can be null.
     * @return A Document instance of the appropriate subclass (DniDocument) if the type is NIE or NIF, otherwise a generic Document instance.
     * @throws IllegalArgumentException if the type is NIE or NIF and the supportNumber is null or empty,
     *                                  or if the number is null
     *                                  or invalid (NIE/NIF).
     */
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

    /**
     * Indicates whether the document requires a second surname.
     *
     * @return true if the document type requires a second surname, false otherwise.
     */
    public boolean requiresSecondSurname() {
        return false;
    }
}
