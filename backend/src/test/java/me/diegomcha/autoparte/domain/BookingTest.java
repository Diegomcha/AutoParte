package me.diegomcha.autoparte.domain;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.booking.payment.Payment;
import me.diegomcha.autoparte.domain.communication.Communication;
import me.diegomcha.autoparte.domain.person.ContactInfo;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

class BookingTest {

    private Accommodation accommodation;
    private Booking booking;
    private Payment payment;

    @BeforeEach
    void setUp() {
        this.payment = Payment.of(Payment.PaymentType.ON_SITE, null, null, null, null);
        this.accommodation = new Accommodation("Test", "SESCODE", null);
        this.booking = new Booking(accommodation, TestingUtils.INSTANT, TestingUtils.INSTANT.plusSeconds(3600), 1, null, null, null);
    }

    @Test
    void testAccommodationAssociation() {
        Assertions.assertEquals(this.accommodation, this.booking.getAccommodation());
        Assertions.assertTrue(this.accommodation.getBookings().contains(this.booking));
    }

    @Test
    void testNumberOfPeople() {
        Assertions.assertEquals(1, this.booking.getNumberOfPeople());

        // Test that creating a second person without increasing the number of people throws an exception
        var personalInfo = new PersonalInfo("Name", "Surname", null, null, TestingUtils.PAST_INSTANT, null);
        var contactInfo = new ContactInfo(null, null, "email@email.com");

        new Person(this.booking, personalInfo, contactInfo, null, null, null);
        Assertions.assertThrows(IllegalStateException.class, () -> new Person(this.booking, personalInfo, contactInfo, null, null, null));

        // Now increase the number of people and create a second person
        this.booking.setNumberOfPeople(2);
        new Person(this.booking, personalInfo, contactInfo, null, null, null);

        // Test reducing the number of people below the current number of persons throws an exception
        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.setNumberOfPeople(1));
    }

    @Test
    void testDatesRangeValidation() {
        var earlierTime = this.booking.getStartTime();
        var laterTime = this.booking.getEndTime();
        var evenEarlierTime = earlierTime.minusSeconds(3600);
        var evenLaterTime = laterTime.plusSeconds(3600);

        Assertions.assertThrows(IllegalArgumentException.class, () -> new Booking(this.accommodation, laterTime, earlierTime, 1, this.payment, null, null));

        Assertions.assertThrows(IllegalArgumentException.class, () -> this.booking.setStartTime(laterTime));
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.booking.setEndTime(earlierTime));

        Assertions.assertThrows(IllegalArgumentException.class, () -> this.booking.setStartTime(evenLaterTime));
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.booking.setEndTime(evenEarlierTime));

        this.booking.setStartTime(evenEarlierTime);
        this.booking.setEndTime(evenLaterTime);
    }

    @Test
    void testPositiveNumberValidation() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.booking.setNumberOfPeople(0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.booking.setNumberOfPeople(-1));

        this.booking.setNumberOfPeople(2);

        Assertions.assertThrows(IllegalArgumentException.class, () -> this.booking.setNumberOfRooms(0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.booking.setNumberOfRooms(-1));

        this.booking.setNumberOfRooms(1);
    }

    @Test
    void testInternetConnection() {
        Assertions.assertNull(this.accommodation.getInternetConnection());
        Assertions.assertNull(this.booking.getInternetConnection());

        this.accommodation.setInternetConnection(true);
        Assertions.assertTrue(this.booking.getInternetConnection());

        this.accommodation.setInternetConnection(false);
        Assertions.assertFalse(this.booking.getInternetConnection());

        this.booking.setInternetConnection(true);
        Assertions.assertTrue(this.booking.getInternetConnection());

        this.booking.setInternetConnection(false);
        Assertions.assertFalse(this.booking.getInternetConnection());

        this.booking.setInternetConnection(null);
        Assertions.assertFalse(this.booking.getInternetConnection());
    }

    @Test
    void testCanBeModified() {
        Assertions.assertTrue(this.booking.canBeModified());
        this.makeCheckinable(true);
        this.booking.checkIn();
        Assertions.assertFalse(this.booking.canBeModified());
    }

    @Test
    void testCanBeConfirmed() {
        Assertions.assertFalse(this.booking.canBeConfirmed());
        this.makeConfirmable();
        Assertions.assertTrue(this.booking.canBeConfirmed());
    }

    @Test
    void testCanBeCheckedIn() {
        Assertions.assertFalse(this.booking.canBeCheckedIn());
        this.makeCheckinable(true);
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            Assertions.assertTrue(this.booking.canBeCheckedIn());
        }
    }

    @Test
    void testConfirmation() {
        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.confirm());
        Assertions.assertTrue(this.booking.getCommunications().isEmpty());

        this.makeConfirmable();

        this.booking.confirm();

        Assertions.assertEquals(1, this.booking.getCommunications().size());
        this.booking.getCommunications().forEach(communication -> {
            Assertions.assertEquals(this.booking, communication.getBooking());
            Assertions.assertEquals(Communication.CommunicationType.BOOKING, communication.getType());
        });

        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.confirm());
        Assertions.assertEquals(1, this.booking.getCommunications().size());
    }

    @Test
    void testCheckIn() {
        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.checkIn());
        Assertions.assertTrue(this.booking.getCommunications().isEmpty());

        this.makeCheckinable(true);

        try (var ignored = TestingUtils.getMockedInstantNow()) {
            this.booking.checkIn();
        }

        Assertions.assertEquals(2, this.booking.getCommunications().size());
        this.booking.getCommunications().stream()
                .filter(communication -> communication.getType() == Communication.CommunicationType.CHECKIN)
                .forEach(communication -> {
                    Assertions.assertEquals(this.booking, communication.getBooking());
                    Assertions.assertEquals(Communication.CommunicationType.CHECKIN, communication.getType());
                });

        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.checkIn());
        Assertions.assertEquals(2, this.booking.getCommunications().size());
    }

    @Test
    void testCancellation() {
        this.booking.cancel();

        Assertions.assertEquals(1, this.booking.getCommunications().size());
        this.booking.getCommunications().forEach(communication -> {
            Assertions.assertEquals(this.booking, communication.getBooking());
            Assertions.assertEquals(Communication.CommunicationType.CANCELLATION, communication.getType());
            Assertions.assertEquals(Communication.CommunicationStatus.SUCCEEDED, communication.getStatus());
        });

        this.makeCheckinable(false);

        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.confirm());
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            Assertions.assertThrows(IllegalStateException.class, () -> this.booking.checkIn());
        }
        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.cancel());
        Assertions.assertEquals(1, this.booking.getCommunications().size());
    }

    @Test
    void testCancellationVoidsOtherCommunicationsNotSent() {
        this.makeCheckinable(true);

        try (var ignored = TestingUtils.getMockedInstantNow()) {
            this.booking.checkIn();
        }

        Assertions.assertEquals(2, this.booking.getCommunications().size());

        this.booking.cancel();

        Assertions.assertEquals(3, this.booking.getCommunications().size());

        this.booking.getCommunications().forEach(communication -> {
            var expectedStatus = communication.getType() == Communication.CommunicationType.CANCELLATION
                    ? Communication.CommunicationStatus.SUCCEEDED
                    : Communication.CommunicationStatus.VOIDED;
            Assertions.assertEquals(expectedStatus, communication.getStatus());
        });
    }

    @Test
    void testCancellationVoidsOtherCommunicationsMixed() {
        this.makeCheckinable(true);
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            this.booking.checkIn();
        }

        Assertions.assertEquals(2, this.booking.getCommunications().size());

        // Mark one as sent to simulate it being sent to SES
        this.booking.getCommunications().stream()
                .filter(c -> c.getType() == Communication.CommunicationType.CHECKIN)
                .findFirst()
                .ifPresent(c -> c.markSent(UUID.randomUUID()));

        this.booking.cancel();

        Assertions.assertEquals(3, this.booking.getCommunications().size());

        this.booking.getCommunications().forEach(communication -> {
            var expectedStatus = switch (communication.getType()) {
                case CHECKIN -> Communication.CommunicationStatus.SENT;
                case BOOKING -> Communication.CommunicationStatus.VOIDED;
                case CANCELLATION -> Communication.CommunicationStatus.PENDING;

            };
            Assertions.assertEquals(expectedStatus, communication.getStatus());
        });
    }

    @Test
    void testRemovePerson() {
        this.makeConfirmable(); // adds a person to the booking

        Assertions.assertEquals(1, this.booking.getPeople().size());
        var person = this.booking.getPeople().getFirst();

        this.booking.removePerson(person);

        // After removing the person, the list of people should be empty
        Assertions.assertTrue(this.booking.getPeople().isEmpty());

        // Trying to remove the same person again should throw an exception
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.booking.removePerson(person));
    }

    private void makeConfirmable() {
        Assertions.assertFalse(this.booking.canBeConfirmed());

        booking.setPayment(this.payment);
        new Person(this.booking, new PersonalInfo("Name", "Surname", null, null, TestingUtils.PAST_INSTANT, null), new ContactInfo(null, null, "email@email.com"), null, null, null);

        Assertions.assertTrue(this.booking.canBeConfirmed());
    }

    private void makeCheckinable(boolean confirm) {
        Assertions.assertFalse(this.booking.canBeCheckedIn());

        booking.setPayment(this.payment);
        var person = new Person(this.booking, new PersonalInfo("Name", "Surname", null, null, TestingUtils.PAST_INSTANT, null), new ContactInfo(null, null, "email@email.com"), null, null, null);

        Assertions.assertFalse(this.booking.canBeCheckedIn());

        person.setPersonalInfo(new PersonalInfo("Name", "Surname", "2Surname", null, TestingUtils.INSTANT.minus(18 * 365, ChronoUnit.DAYS), null));
        person.setDocument(Document.of(Document.DocumentType.NIF, "54095720L", "SUPPORT"));
        person.setAddress(Address.of("Line1", null, "Municipality", "PostalCode", "USA"));

        if (confirm) {
            this.booking.confirm();
            Assertions.assertTrue(this.booking.canBeCheckedIn());
        }
    }
}
