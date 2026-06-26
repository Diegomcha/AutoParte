package me.diegomcha.autoparte.api.person.dto;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import me.diegomcha.autoparte.core.validation.annotations.CountryCode;
import me.diegomcha.autoparte.core.validation.annotations.DniConstraint;
import me.diegomcha.autoparte.core.validation.annotations.OneContact;
import me.diegomcha.autoparte.core.validation.annotations.PhoneNumber;
import me.diegomcha.autoparte.domain.Person;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.Document;

import java.time.Instant;
import java.util.UUID;

public record PersonDtoRequest(
        @Nonnull PersonalInfoDtoRequest personalInfo,
        @Nonnull ContactInfoDtoRequest contactInfo,
        DocumentDtoRequest document,
        UUID address,
        Person.PersonRelationship relationship
) {
    public record PersonalInfoDtoRequest(
            @Nonnull String name,
            @Nonnull String firstSurname,
            String secondSurname,
            @CountryCode(nullable = true) String nationality,
            @Past Instant birthDate,
            PersonalInfo.PersonalInfoGender gender
    ) {
    }

    @OneContact
    public record ContactInfoDtoRequest(
            @PhoneNumber String phoneNumber1,
            @PhoneNumber String phoneNumber2,
            @Email String email
    ) {
    }

    @DniConstraint
    public record DocumentDtoRequest(
            @Nonnull Document.DocumentType type,
            @Nonnull String number,
            String supportNumber
    ) {
    }
}
