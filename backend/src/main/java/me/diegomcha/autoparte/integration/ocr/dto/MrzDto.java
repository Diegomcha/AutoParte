package me.diegomcha.autoparte.integration.ocr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDate;


public record MrzDto(
        boolean valid,
        String raw,
        String[] errors,
        String[] warnings,
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
            @JsonProperty("document_type") DocumentType documentType,
            @JsonProperty("document_number") String documentNumber,
            @JsonProperty("optional_data") String optionalData,

            @JsonProperty("optional_data_2") String optionalData2,
            @JsonProperty("birth_date_hash") Integer birthDateHash,
            @JsonProperty("expiry_date_hash") Integer expiryDateHash,
            @JsonProperty("document_number_hash") Integer documentNumberHash,
            @JsonProperty("final_hash") Integer finalHash
    ) {
        @RequiredArgsConstructor
        public enum DocumentType {
            PASSPORT("P"),
            DNI("ID"),
            TIE_RESIDENCE_PERMIT("IR"),
            TIE_BORDER_WORKER("IX");

            @Getter
            @JsonValue
            private final String code;
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


        public String firstSurname() {
            if (this.surname == null) return null;
            return this.documentType == DocumentType.DNI
                    ? this.surname.split(" ")[0]
                    : this.surname;
        }

        public String secondSurname() {
            if (this.surname == null) return null;
            return this.documentType == DocumentType.DNI
                    ? this.surname.split(" ")[1]
                    : null;
        }

        @Override
        public String documentNumber() {
            return this.documentType != DocumentType.PASSPORT
                    ? this.optionalData
                    : this.documentNumber;
        }

        public String documentSupportNumber() {
            return this.documentType != DocumentType.PASSPORT
                    ? this.documentNumber
                    : this.optionalData;
        }
    }
}