package me.diegomcha.autoparte.model.person.document;

import lombok.*;

import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(callSuper = true)
public class DniDocument extends Document {

    private @NonNull String supportNumber;

    protected DniDocument(@NonNull DocumentType type, @NonNull String nif, @NonNull String supportNumber) {
        super(type, nif);
        this.supportNumber = Objects.requireNonNull(supportNumber);
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
        if (!isNifValid(nif))
            throw new IllegalArgumentException("Invalid NIF number");
        super.setNumber(nif);
    }

    private boolean isNifValid(@NonNull String nif) {
        // Check if the NIF has the correct length
        if (nif.length() != 9)
            return false;

        // Get number part
        int number = Integer.parseInt(
                nif.substring(0, nif.length() - 1)
                        // Handle NIE
                        .replace("X", "0")
                        .replace("Y", "1")
                        .replace("Z", "2"));

        // Compare the control letter with the expected one
        return "TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23) == nif.charAt(8);
    }
}
