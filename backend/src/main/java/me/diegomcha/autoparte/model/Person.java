package me.diegomcha.autoparte.model;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import me.diegomcha.autoparte.model.base.BaseEntity;
import me.diegomcha.autoparte.model.person.ContactInfo;
import me.diegomcha.autoparte.model.person.document.Document;
import me.diegomcha.autoparte.model.person.address.Address;

import java.time.Instant;

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
    private @NonNull Booking booking;

    private @NonNull String name;
    private @NonNull String firstSurname;
    private String secondSurname; // TODO: requerido con NIF
    private String nationality;
    private @NonNull Instant birthDate; // TODO: es obligatorio?
    private Document document; // TODO: no requerido si menor
    private PersonGender gender;
    private Address address;
    private ContactInfo contactInfo;
    private PersonRelationship relationship; // TODO: obligatorio para menores de edad

}
