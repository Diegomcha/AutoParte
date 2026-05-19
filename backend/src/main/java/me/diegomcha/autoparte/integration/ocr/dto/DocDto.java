package me.diegomcha.autoparte.integration.ocr.dto;

import me.diegomcha.autoparte.domain.person.document.Document;
import me.diegomcha.autoparte.domain.Person;

import java.time.LocalDate;

public record DocDto(
        String name,
        String firstSurname,
        String secondSurname,
        LocalDate birthDate,
        String nationality,
        Person.PersonGender gender,
        Document.DocumentType documentType,
        String documentNumber,
        String documentSupportNumber,

        String country
) {
}
