package me.diegomcha.autoparte.domain;

import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.DocumentInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class PersonTest {

    private Establishment establishment;
    private Booking booking;
    private PersonalInfo personalInfo;
    private Person person;

    @BeforeEach
    void setUp() {
        this.establishment = new Establishment("Test", "SESCODE");
        this.booking = new Booking(establishment, Instant.now(), Instant.now().plusSeconds(3600), 1);
        this.personalInfo = new PersonalInfo("Name", "Surname");
        this.person = new Person(booking, personalInfo);
    }

    @Test
    void testBookingAssociation() {
        Assertions.assertEquals(this.booking, this.person.getBooking());
        Assertions.assertTrue(this.booking.getPeople().contains(this.person));

        var newBooking = new Booking(this.establishment, Instant.now().plusSeconds(7200), Instant.now().plusSeconds(10800), 1);
        this.person.setBooking(newBooking);

        Assertions.assertEquals(newBooking, this.person.getBooking());
        Assertions.assertFalse(this.booking.getPeople().contains(this.person));
        Assertions.assertTrue(newBooking.getPeople().contains(this.person));
    }

    @Test
    void testAddressAssociation() {
        Assertions.assertNull(this.person.getAddress());

        var address = Address.of("Line1", null, "Municipality", "PostalCode", "USA");
        this.person.setAddress(address);

        Assertions.assertEquals(address, this.person.getAddress());
        Assertions.assertTrue(address.getPeople().contains(this.person));

        this.person.setAddress(null);

        Assertions.assertNull(this.person.getAddress());
        Assertions.assertFalse(address.getPeople().contains(this.person));
    }

    @Test
    void testDocumentInfoValidation() {
        var documentInfo = DocumentInfo.of(DocumentInfo.DocumentType.NIF, "54095720L", "SUPPORT");

        Assertions.assertNull(this.person.getDocumentInfo());
        Assertions.assertNull(this.person.getPersonalInfo().getSecondSurname());
        Assertions.assertTrue(documentInfo.requiresSecondSurname());

        Assertions.assertThrows(IllegalStateException.class, () -> this.person.setDocumentInfo(documentInfo));

        this.person.setPersonalInfo(new PersonalInfo("Name", "Surname", "SecondSurname"));

        this.person.setDocumentInfo(documentInfo);
    }
}
