package me.diegomcha.autoparte.domain.person;

import jakarta.annotation.Nullable;
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
    private static final int SPAIN_MUST_SIGN_AGE = 16;

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

    /**
     * Constructor for creating a PersonalInfo instance.
     *
     * @param name          Name of the person. Must not be null.
     * @param firstSurname  First surname of the person. Must not be null.
     * @param secondSurname Second surname of the person. Can be null.
     * @param nationality   Nationality of the person. Can be null, but if provided, must be a valid country code.
     * @param birthDate     Birth date of the person. Can be null and cannot be in the future.
     * @param gender        Gender of the person. Can be null.
     * @throws IllegalArgumentException if birthDate is in the future or
     *                                  country code is invalid or
     *                                  any of the required parameters are null.
     */
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

    /**
     * Checks if the personal information is complete for check-in purposes.
     *
     * @param requiresSecondSurname Indicates whether a second surname is required for completeness.
     * @return true if the personal information is complete for check-in; false otherwise.
     */
    public boolean isCompleteForCheckIn(boolean requiresSecondSurname) {
        return birthDate != null && !(requiresSecondSurname && (this.secondSurname == null || this.secondSurname.isBlank()));
    }

    /**
     * Checks if the person is considered an adult based on their birthdate and the defined adult age in Spain.
     *
     * @return true if the person is an adult; false otherwise. May return null if birthDate is not set.
     */
    public @Nullable Boolean isAdult() {
        if (this.birthDate == null) return null;
        return Duration.between(this.birthDate, Instant.now()).toDays() >= SPAIN_ADULT_AGE * 365;
    }

    /**
     * Checks if the person is considered to need to setSignature documents based on their birthdate and the defined age in Spain.
     *
     * @return true if the person must setSignature; false otherwise. May return null if birthDate is not set.
     */
    public @Nullable Boolean mustSign() {
        if (this.birthDate == null) return null;
        return Duration.between(this.birthDate, Instant.now()).toDays() >= SPAIN_MUST_SIGN_AGE * 365;
    }
}
