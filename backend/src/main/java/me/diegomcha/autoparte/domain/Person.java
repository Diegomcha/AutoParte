package me.diegomcha.autoparte.domain;

import lombok.*;
import me.diegomcha.autoparte.core.validation.Validations;
import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.base.BaseEntity;
import me.diegomcha.autoparte.domain.person.ContactInfo;
import me.diegomcha.autoparte.domain.person.document.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Person extends BaseEntity {

    public enum PersonGender {
        MALE, // H
        FEMALE, // M
        OTHER // O
    }

    public enum PersonRelationship {
        GRANDPARENT, // AB
        GREAT_GRANDPARENT, // BA
        GREAT_GRANDCHILD, // BN
        SIBLING_IN_LAW, // CD
        SPOUSE, // CY
        CHILD, // HJ
        SIBLING, // HR
        GRANDCHILD, // NI
        PARENT, // PM
        NEPHEW_NIECE, // SB
        PARENT_IN_LAW, // SG
        UNCLE_AUNT, // TI
        SON_DAUGHTER_IN_LAW, // YN
        TUTOR, // TU
        OTHER // OT
    }

    @Setter(AccessLevel.PACKAGE)
    private Booking booking;

    private @NonNull String name;
    private @NonNull String firstSurname;
    private String secondSurname;
    private String nationality;
    private Instant birthDate;
    private Document document;
    private PersonGender gender;
    private Address address;
    private ContactInfo contactInfo;
    private PersonRelationship relationship;

    public Person(@NonNull String name, @NonNull String firstSurname, String secondSurname, String nationality, Instant birthDate, Document document, PersonGender gender, Address address, ContactInfo contactInfo, PersonRelationship relationship) {
        this.setName(name);
        this.setFirstSurname(firstSurname);
        this.setSecondSurname(secondSurname);
        this.setNationality(nationality);
        this.setBirthDate(birthDate);
        this.setDocument(document);
        this.setGender(gender);
        this.setAddress(address);
        this.setContactInfo(contactInfo);
        this.setRelationship(relationship);
    }

    public void setAddress(Address address) {
        if (this.address != null) address._getPeople().remove(this);
        if (address != null) address._getPeople().add(this);
        this.address = address;
    }

    public void setSecondSurname(String secondSurname) {
        if (this.document != null && this.document.requiresSecondSurname() && secondSurname == null)
            throw new IllegalArgumentException("Second surname is required for document type " + this.document.getType());

        this.secondSurname = secondSurname;
    }

    public void setNationality(String nationality) {
        if (nationality != null && !Validations.isValidCountry(nationality))
            throw new IllegalArgumentException("Invalid nationality: " + nationality);

        this.nationality = nationality;}

    public void setBirthDate(Instant birthDate) {
        if (birthDate != null && birthDate.isAfter(Instant.now()))
            throw new IllegalArgumentException("Birth date cannot be in the future");

        this.birthDate = birthDate;
    }
}
