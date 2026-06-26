package me.diegomcha.autoparte.api.ocr.dto;

import jakarta.annotation.Nonnull;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.Document;

import java.time.Instant;

public record PartialPersonDtoRequest(
        @Nonnull PartialPersonalInfoDtoRequest personalInfo,
        @Nonnull PartialDocumentDtoRequest document
) {
    public record PartialPersonalInfoDtoRequest(
            @Nonnull String name,
            @Nonnull String firstSurname,
            String secondSurname,
            @Nonnull String nationality,
            @Nonnull Instant birthDate,
            @Nonnull PersonalInfo.PersonalInfoGender gender
    ) {
    }

    public record PartialDocumentDtoRequest(
            @Nonnull Document.DocumentType type,
            @Nonnull String number,
            String supportNumber
    ) {
    }
}
