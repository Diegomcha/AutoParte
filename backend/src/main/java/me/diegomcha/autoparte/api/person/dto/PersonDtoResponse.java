package me.diegomcha.autoparte.api.person.dto;

import jakarta.validation.constraints.NotNull;
import me.diegomcha.autoparte.domain.Person;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.Document;

import java.time.Instant;
import java.util.UUID;

public record PersonDtoResponse(
        @NotNull UUID id,
        @NotNull Instant createdAt,
        @NotNull Instant updatedAt,

        @NotNull boolean hasSigned,

        Boolean isAdult,
        Boolean mustSign,

        @NotNull PersonDtoResponse.PersonalInfoDtoResponse personalInfo,
        @NotNull PersonDtoResponse.ContactInfoDtoResponse contactInfo,
        PersonDtoResponse.DocumentDtoResponse document,
        UUID address,
        Person.PersonRelationship relationship
) {
    public record PersonalInfoDtoResponse(
            @NotNull String name,
            @NotNull String firstSurname,
            String secondSurname,
            String nationality,
            Instant birthDate,
            PersonalInfo.PersonalInfoGender gender
    ) {
    }

    public record ContactInfoDtoResponse(
            String phoneNumber1,
            String phoneNumber2,
            String email
    ) {
    }

    public record DocumentDtoResponse(
            @NotNull Document.DocumentType type,
            @NotNull String number,
            String supportNumber
    ) {
    }
}
