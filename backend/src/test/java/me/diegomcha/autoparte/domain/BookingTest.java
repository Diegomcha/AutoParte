package me.diegomcha.autoparte.domain;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.booking.payment.PaymentInfo;
import me.diegomcha.autoparte.domain.communication.Communication;
import me.diegomcha.autoparte.domain.person.ContactInfo;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.DocumentInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

class BookingTest {

    private Accommodation accommodation;
    private Booking booking;
    private PaymentInfo payment;

    @BeforeEach
    void setUp() {
        this.payment = PaymentInfo.of(PaymentInfo.PaymentType.ON_SITE);
        this.accommodation = new Accommodation("Test", "SESCODE", null);
        this.booking = new Booking(accommodation, TestingUtils.INSTANT, TestingUtils.INSTANT.plusSeconds(3600), 1, this.payment, null, null);
    }

    @Test
    void testAccommodationAssociation() {
        Assertions.assertEquals(this.accommodation, this.booking.getAccommodation());
        Assertions.assertTrue(this.accommodation.getBookings().contains(this.booking));
    }

    @Test
    void testNumberOfPeople() {
        Assertions.assertEquals(1, this.booking.getNumberOfPeople());

        var personalInfo = new PersonalInfo("Name", "Surname");
        var contactInfo = new ContactInfo(null, null, "email@email.com");

        new Person(this.booking, personalInfo, contactInfo, null, null, null);
        Assertions.assertThrows(IllegalStateException.class, () -> new Person(this.booking, personalInfo, contactInfo, null, null, null));
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
    void testCanBeConfirmed() {
        Assertions.assertFalse(this.booking.canBeConfirmed());

        new Person(this.booking, new PersonalInfo("Name", "Surname"), new ContactInfo(null, null, "email@email.com"), null, null, null);

        Assertions.assertTrue(this.booking.canBeConfirmed());
    }

    @Test
    void testCanBeCheckedIn() {
        Assertions.assertFalse(this.booking.canBeCheckedIn());

        var person = new Person(this.booking, new PersonalInfo("Name", "Surname"), new ContactInfo(null, null, "email@email.com"), null, null, null);

        Assertions.assertFalse(this.booking.canBeCheckedIn());

        person.setPersonalInfo(new PersonalInfo("Name", "Surname", "2Surname", null, TestingUtils.INSTANT.minus(18 * 365, ChronoUnit.DAYS), null));
        person.setDocumentInfo(DocumentInfo.of(DocumentInfo.DocumentType.NIF, "54095720L", "SUPPORT"));
        person.setAddress(Address.of("Line1", null, "Municipality", "PostalCode", "USA"));

        try (var ignored = TestingUtils.getMockedInstantNow()) {
            Assertions.assertTrue(this.booking.canBeCheckedIn());
        }
    }

    @Test
    void testConfirmation() {
        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.confirm());
        Assertions.assertTrue(this.booking.getCommunications().isEmpty());

        // Make the booking confirmable
        testCanBeConfirmed();

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

        // Make the booking checkinable
        testCanBeCheckedIn();

        try (var ignored = TestingUtils.getMockedInstantNow()) {
            this.booking.checkIn();
        }

        Assertions.assertEquals(1, this.booking.getCommunications().size());
        this.booking.getCommunications().forEach(communication -> {
            Assertions.assertEquals(this.booking, communication.getBooking());
            Assertions.assertEquals(Communication.CommunicationType.CHECKIN, communication.getType());
        });

        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.checkIn());
        Assertions.assertEquals(1, this.booking.getCommunications().size());
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

        // Make confirmable & checkinable to test that they cannot be done after cancellation
        testCanBeCheckedIn();

        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.confirm());
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            Assertions.assertThrows(IllegalStateException.class, () -> this.booking.checkIn());
        }
        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.cancel());
        Assertions.assertEquals(1, this.booking.getCommunications().size());
    }

    @Test
    void testCancellationVoidsOtherCommunicationsNotSent() {
        // Make the booking confirmable & checkinable
        testCanBeCheckedIn();

        this.booking.confirm();
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
        // Make the booking confirmable & checkinable
        testCanBeCheckedIn();

        this.booking.confirm();
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
}
