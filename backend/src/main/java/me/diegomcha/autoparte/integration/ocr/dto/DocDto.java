package me.diegomcha.autoparte.integration.ocr.dto;

import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.DocumentInfo;

import java.time.LocalDate;

public record DocDto(
        String name,
        String firstSurname,
        String secondSurname,
        LocalDate birthDate,
        String nationality,
        PersonalInfo.PersonalInfoGender gender,
        DocumentInfo.DocumentType documentType,
        String documentNumber,
        String documentSupportNumber,

        String country
) {
}
