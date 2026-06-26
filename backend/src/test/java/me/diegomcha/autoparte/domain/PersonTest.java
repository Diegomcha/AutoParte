package me.diegomcha.autoparte.domain;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.booking.payment.Payment;
import me.diegomcha.autoparte.domain.person.ContactInfo;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

import java.time.temporal.ChronoUnit;

class PersonTest {

    private Booking booking;
    private Person person;

    @BeforeEach
    void setUp() {
        Accommodation accommodation = new Accommodation("Test", "SESCODE", null);
        this.booking = new Booking(accommodation, TestingUtils.INSTANT, TestingUtils.INSTANT.plusSeconds(3600), 2, Payment.of(Payment.PaymentType.ON_SITE), null, null);
        this.person = new Person(booking, new PersonalInfo("Name", "Surname", null, null, TestingUtils.PAST_INSTANT, null), new ContactInfo(null, null, "email@email.com"), null, null, null);
    }

    @Test
    void testBookingAssociation() {
        Assertions.assertEquals(this.booking, this.person.getBooking());
        Assertions.assertTrue(this.booking.getPeople().contains(this.person));
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

    private static final Object[][] IS_COMPLETE_MCDC = new Boolean[][]{
            new Boolean[]{true, true, true, false, true,  /* = */ true},
            new Boolean[]{false, true, true, false, true, /* = */ false},
            new Boolean[]{true, true, true, false, false, /* = */ false},
            new Boolean[]{true, true, false, false, true, /* = */ false},
            new Boolean[]{true, false, true, false, true, /* = */ false},
            new Boolean[]{true, false, true, true, true,  /* = */ true},
            new Boolean[]{true, true, false, true, true,  /* = */ false},
    };

    @ParameterizedTest
    @FieldSource("IS_COMPLETE_MCDC")
    void testIsComplete(boolean completePInfo, boolean isAdult, boolean hasDInfo, boolean hasRelationship, boolean hasAddress, boolean expected) {
        var birthDate = isAdult ? TestingUtils.INSTANT.minus(18 * 365, ChronoUnit.DAYS) : TestingUtils.PAST_INSTANT;
        var pInfo = completePInfo
                ? new PersonalInfo("Name", "Surname", "2Surname", null, birthDate, null)
                : new PersonalInfo("Name", "Surname", null, null, birthDate, null);
        var dInfo = hasDInfo
                ? Document.of(Document.DocumentType.NIF, "54095720L", "SUPPORT")
                : null;
        var address = hasAddress
                ? Address.of("Line1", null, "Municipality", "PostalCode", "USA")
                : null;
        var relationship = hasRelationship
                ? Person.PersonRelationship.CHILD
                : null;

        var craftedPerson = new Person(this.booking, pInfo, new ContactInfo(null, null, "email@email.com"), dInfo, address, relationship);
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            Assertions.assertEquals(expected, craftedPerson.isComplete());
        }
    }
}
