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
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
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
    private @NonNull ContactInfo contactInfo;
    private DocumentInfo documentInfo;
    private Address address;
    private PersonRelationship relationship;

    public Person(@NonNull Booking booking, @NonNull PersonalInfo personalInfo, @NonNull ContactInfo contactInfo, DocumentInfo documentInfo, Address address, PersonRelationship relationship) {
        this.setBooking(booking);
        this.setPersonalInfo(personalInfo);

        this.setContactInfo(contactInfo);
        this.setDocumentInfo(documentInfo);
        this.setAddress(address);

        this.setRelationship(relationship);
    }

    private void setBooking(@NonNull Booking booking) {
        this.booking = booking;
        this.booking._addPerson(this);
    }

    public void setAddress(Address address) {
        if (this.address != null) this.address._getPeople().remove(this);
        this.address = address;
        if (this.address != null) this.address._getPeople().add(this);
    }

    public boolean isComplete() {
        return personalInfo.isComplete(this.documentInfo != null && this.documentInfo.requiresSecondSurname()) &&
                (personalInfo.isAdult() ? this.documentInfo != null : this.relationship != null) &&
                address != null;
    }

}
