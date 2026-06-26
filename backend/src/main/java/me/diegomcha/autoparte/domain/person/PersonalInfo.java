package me.diegomcha.autoparte.domain.person;

import lombok.*;
import me.diegomcha.autoparte.core.validation.Validations;

import java.time.Duration;
import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@EqualsAndHashCode
public class PersonalInfo {

    private static final int SPAIN_ADULT_AGE = 18;

    public enum PersonalInfoGender {
        MALE, // H
        FEMALE, // M
        OTHER // O
    }

    private @NonNull String name;
    private @NonNull String firstSurname;
    private String secondSurname;
    private String nationality;
    private @NonNull Instant birthDate;
    private PersonalInfoGender gender;

    public PersonalInfo(@NonNull String name, @NonNull String firstSurname, String secondSurname, String nationality, @NonNull Instant birthDate, PersonalInfoGender gender) {
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

    private void setBirthDate(@NonNull Instant birthDate) {
        if (birthDate.isAfter(Instant.now()))
            throw new IllegalArgumentException("Birth date cannot be in the future");

        this.birthDate = birthDate;
    }

    public boolean isComplete(boolean requiresSecondSurname) {
        return !(requiresSecondSurname && (this.secondSurname == null || this.secondSurname.isBlank()));
    }

    public boolean isAdult() {
        return Duration.between(this.birthDate, Instant.now()).toDays() >= SPAIN_ADULT_AGE * 365;
    }
}
