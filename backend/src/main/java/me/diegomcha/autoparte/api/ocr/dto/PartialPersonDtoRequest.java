package me.diegomcha.autoparte.api.ocr.dto;

import jakarta.validation.constraints.NotNull;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.Document;

import java.time.Instant;

public record PartialPersonDtoRequest(
        @NotNull PartialPersonalInfoDtoRequest personalInfo,
        @NotNull PartialDocumentDtoRequest document
) {
    public record PartialPersonalInfoDtoRequest(
            @NotNull String name,
            @NotNull String firstSurname,
            String secondSurname,
            @NotNull String nationality,
            @NotNull Instant birthDate,
            @NotNull PersonalInfo.PersonalInfoGender gender
    ) {
    }

    public record PartialDocumentDtoRequest(
            @NotNull Document.DocumentType type,
            @NotNull String number,
            String supportNumber
    ) {
    }
}
