package me.diegomcha.autoparte.domain;

import lombok.*;
import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.base.BaseEntity;
import me.diegomcha.autoparte.domain.person.ContactInfo;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.Signature;
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
    private Signature signature;
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
        this.personalInfo = personalInfo;

        this.contactInfo = contactInfo;
        this.document = document;
        this.setAddress(address);

        this.relationship = relationship;
    }

    private void setBooking(@NonNull Booking booking) {
        this.booking = booking;
        this.booking._addPerson(this);
    }

    /**
     * Sets the address for the Person instance and manages the bidirectional relationship between Person and Address.
     *
     * @param address The address to be associated with the Person instance. Can be null.
     */
    public void setAddress(Address address) {
        if (this.address != null) this.address._getPeople().remove(this);
        this.address = address;
        if (this.address != null) this.address._getPeople().add(this);
    }

    /**
     * Checks if the Person instance has complete information for check-in purposes.
     *
     * @return true if the personal information is complete for check-in; false otherwise.
     */
    public boolean isCompleteForCheckIn() {
        var isAdult = personalInfo.isAdult();
        var requiresSecondSurname = this.document != null && this.document.requiresSecondSurname();

        return personalInfo.isCompleteForCheckIn(requiresSecondSurname) &&
                isAdult != null && (isAdult ? this.document != null : this.relationship != null) &&
                address != null;
    }

    /**
     * Sets the signature for the Person instance.
     * This method checks if the person is required to sign based on their personal information.
     *
     * @param signature The signature to be associated with the Person instance. Must not be null.
     * @throws IllegalStateException if the person does not need to sign or if the person is not complete for check-in.
     */
    public void setSignature(@NonNull Signature signature) {
        if (!this.isCompleteForCheckIn())
            throw new IllegalStateException("Person is not complete for check-in");

        var mustSign = this.personalInfo.mustSign();
        if (mustSign == null || !mustSign)
            throw new IllegalStateException("Person does not need to sign");

        this.signature = signature;
    }
}
