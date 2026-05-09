package me.diegomcha.autoparte.integration.ocr.dto;

import java.time.LocalDate;

public record DocDto(
        String name,
        String firstSurname,
        String secondSurname,
        LocalDate birthDate,
        String nationality,
        Gender gender,
        DocumentType documentType,
        String documentNumber,
        String documentSupportNumber,

        String country
) {
    public enum Gender {
        MALE,
        FEMALE,
        OTHER
    }

   public enum DocumentType {
        NIE,
        NIF,
        PASSPORT,
        OTHER
    }
}
