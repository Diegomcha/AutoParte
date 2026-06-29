package me.diegomcha.autoparte.domain;

import lombok.*;
import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.base.BaseEntity;
import me.diegomcha.autoparte.domain.person.ContactInfo;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.Document;

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
    private Document document;
    private Address address;
    private PersonRelationship relationship;

    /**
     * Constructor for creating a Person instance.
     *
     * @param booking      Booking associated with the person. Must not be null.
     * @param personalInfo Personal information of the person. Must not be null.
     * @param contactInfo  Contact information of the person. Must not be null.
     * @param document     Document associated with the person. Can be null.
     * @param address      Address associated with the person. Can be null.
     * @param relationship Relationship of the person to another entity. Can be null.
     * @throws IllegalArgumentException if any of the required parameters (booking, personalInfo, contactInfo) are null.
     */
    public Person(@NonNull Booking booking, @NonNull PersonalInfo personalInfo, @NonNull ContactInfo contactInfo, Document document, Address address, PersonRelationship relationship) {
        this.setBooking(booking);
        this.setPersonalInfo(personalInfo);

        this.setContactInfo(contactInfo);
        this.setDocument(document);
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

    /**
     * Check if the Person instance is complete based on the following criteria:
     * - Personal information is complete, considering if the document requires a second surname.
     * - If the person is an adult, a document must be present; otherwise, a relationship must be specified.
     * - An address must be provided.
     *
     * @return true if the Person instance is complete; false otherwise.
     */
    public boolean isComplete() {
        return personalInfo.isComplete(this.document != null && this.document.requiresSecondSurname()) &&
                (personalInfo.isAdult() ? this.document != null : this.relationship != null) &&
                address != null;
    }

}
