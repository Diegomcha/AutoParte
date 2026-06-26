package me.diegomcha.autoparte.integration.ocr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDate;
import java.util.Collection;

public record MrzDto(
        boolean valid,
        String raw,
        Collection<String> errors,
        Collection<String> warnings,
        MrzData data
) {
    public record MrzData(
            String surname,
            String name,
            String country,
            String nationality,
            @JsonDeserialize(using = BirthDateDeserializer.class)
            @JsonProperty("birth_date") LocalDate birthDate,
            @JsonFormat(pattern = "yyMMdd")
            @JsonProperty("expiry_date") LocalDate expiryDate,
            @JsonProperty("sex") Gender gender,
            @JsonProperty("document_type") String rawDocumentType,
            @JsonProperty("document_number") String documentNumber,
            @JsonProperty("optional_data") String optionalData,

            @JsonProperty("optional_data_2") String optionalData2,
            @JsonProperty("birth_date_hash") Integer birthDateHash,
            @JsonProperty("expiry_date_hash") Integer expiryDateHash,
            @JsonProperty("document_number_hash") Integer documentNumberHash,
            @JsonProperty("final_hash") Integer finalHash
    ) {
        public enum DocumentType {
            PASSPORT,
            DNI,
            TIE_RESIDENCE_PERMIT,
            TIE_BORDER_WORKER,
            OTHER
        }

        @RequiredArgsConstructor
        public enum Gender {
            MALE("M"),
            FEMALE("F"),
            NON_BINARY("X"),
            UNSPECIFIED("<");

            @Getter
            @JsonValue
            private final String code;
        }

        public DocumentType documentType() {
            // Get preliminar type from raw document type
            var type = switch (this.rawDocumentType) {
                case "P" -> DocumentType.PASSPORT;
                case "ID" -> DocumentType.DNI;
                case "IR" -> DocumentType.TIE_RESIDENCE_PERMIT;
                case "IX" -> DocumentType.TIE_BORDER_WORKER;
                default -> DocumentType.OTHER;
            };

            // If the country is not Spain and the type is not a passport, force it to be OTHER
            if (!"ESP".equalsIgnoreCase(this.country) && type != DocumentType.PASSPORT)
                type = DocumentType.OTHER;

            return type;
        }

        public String firstSurname() {
            if (this.surname == null) return null;
            return this.documentType() == DocumentType.DNI
                    ? this.surname.split(" ")[0]
                    : this.surname;
        }

        public String secondSurname() {
            if (this.surname == null) return null;
            assert this.surname.split(" ").length > 1;
            return this.documentType() == DocumentType.DNI
                    ? this.surname.split(" ")[1]
                    : null;
        }

        @Override
        public String documentNumber() {
            return switch (this.documentType()) {
                case DNI, TIE_RESIDENCE_PERMIT, TIE_BORDER_WORKER -> this.optionalData;
                default -> this.documentNumber;
            };
        }

        public String documentSupportNumber() {
            return switch (this.documentType()) {
                case DNI, TIE_RESIDENCE_PERMIT, TIE_BORDER_WORKER -> this.documentNumber;
                default -> null;
            };
        }
    }
}