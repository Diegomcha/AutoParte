package me.diegomcha.autoparte.domain;

import lombok.*;
import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.base.BaseEntity;
import me.diegomcha.autoparte.domain.person.ContactInfo;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.DocumentInfo;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Person extends BaseEntity {

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

    private @NonNull Booking booking;

    private @NonNull PersonalInfo personalInfo;
    private ContactInfo contactInfo;
    private DocumentInfo documentInfo;
    private Address address;
    private PersonRelationship relationship;

    public Person(@NonNull Booking booking, @NonNull PersonalInfo personalInfo) {
        this(booking, personalInfo, null, null, null, null);
    }

    public Person(@NonNull Booking booking, @NonNull PersonalInfo personalInfo, ContactInfo contactInfo, DocumentInfo documentInfo, Address address, PersonRelationship relationship) {
        this.setBooking(booking);
        this.setPersonalInfo(personalInfo);

        this.setContactInfo(contactInfo);
        this.setDocumentInfo(documentInfo);
        this.setAddress(address);

        this.setRelationship(relationship);
    }

    public void setBooking(@NonNull Booking booking) {
        if (this.booking != null) this.booking._removePerson(this);
        this.booking = booking;
        this.booking._addPerson(this);
    }

    public void setAddress(Address address) {
        if (this.address != null) this.address._getPeople().remove(this);
        this.address = address;
        if (this.address != null) this.address._getPeople().add(this);
    }

    public void setDocumentInfo(DocumentInfo documentInfo) {
        if (documentInfo != null && documentInfo.requiresSecondSurname() && this.personalInfo.getSecondSurname() == null)
            throw new IllegalStateException("Second surname is required for document type " + documentInfo.getType());
        this.documentInfo = documentInfo;
    }

}
