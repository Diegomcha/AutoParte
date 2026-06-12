package me.diegomcha.autoparte.domain.person;

import lombok.*;
import me.diegomcha.autoparte.core.validation.Validations;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@EqualsAndHashCode
public class PersonalInfo {

    public enum PersonalInfoGender {
        MALE, // H
        FEMALE, // M
        OTHER // O
    }

    private @NonNull String name;
    private @NonNull String firstSurname;
    private String secondSurname;
    private String nationality;
    private Instant birthDate;
    private PersonalInfoGender gender;

    public PersonalInfo(@NonNull String name, @NonNull String firstSurname) {
        this(name, firstSurname, null, null, null, null);
    }

    public PersonalInfo(@NonNull String name, @NonNull String firstSurname, String secondSurname) {
        this(name, firstSurname, secondSurname, null, null, null);
    }

    public PersonalInfo(@NonNull String name, @NonNull String firstSurname, String secondSurname, String nationality, Instant birthDate, PersonalInfoGender gender) {
        this.name = name;
        this.firstSurname = firstSurname;
        this.secondSurname = secondSurname;
        this.setNationality(nationality);
        this.setBirthDate(birthDate);
        this.gender = gender;
    }

    private void setNationality(String nationality) {
        if (nationality != null) Validations.ensureValidCountry(nationality);
        this.nationality = nationality;
    }

    private void setBirthDate(Instant birthDate) {
        if (birthDate != null && birthDate.isAfter(Instant.now()))
            throw new IllegalArgumentException("Birth date cannot be in the future");

        this.birthDate = birthDate;
    }
}
