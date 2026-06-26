package me.diegomcha.autoparte.api.person.dto;

import jakarta.annotation.Nonnull;
import me.diegomcha.autoparte.domain.Person;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.Document;

import java.time.Instant;
import java.util.UUID;

public record PersonDtoResponse(
        @Nonnull UUID id,
        @Nonnull Instant createdAt,
        @Nonnull Instant updatedAt,

        @Nonnull PersonDtoResponse.PersonalInfoDtoResponse personalInfo,
        @Nonnull PersonDtoResponse.ContactInfoDtoResponse contactInfo,
        PersonDtoResponse.DocumentDtoResponse document,
        UUID address,
        Person.PersonRelationship relationship
) {
    public record PersonalInfoDtoResponse(
            @Nonnull String name,
            @Nonnull String firstSurname,
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
            @Nonnull Document.DocumentType type,
            @Nonnull String number,
            String supportNumber
    ) {
    }
}
