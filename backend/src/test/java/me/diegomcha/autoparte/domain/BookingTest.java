package me.diegomcha.autoparte.domain;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.booking.payment.Payment;
import me.diegomcha.autoparte.domain.communication.CancellationCommunication;
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

        Assertions.assertThrows(IllegalArgumentException.class, () -> this.booking.setDates(laterTime, laterTime));
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.booking.setDates(earlierTime, earlierTime));

        Assertions.assertThrows(IllegalArgumentException.class, () -> this.booking.setDates(evenLaterTime, laterTime));
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.booking.setDates(earlierTime, evenEarlierTime));

        this.booking.setDates(evenEarlierTime, evenLaterTime);
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

        this.makeConfirmable();
        Assertions.assertTrue(this.booking.canBeModified());
        booking.confirm();
        Assertions.assertFalse(this.booking.canBeModified());
        this.finalizeConfirmation();
        Assertions.assertTrue(this.booking.canBeModified());

        this.makeCheckinable();
        Assertions.assertTrue(this.booking.canBeModified());
        booking.checkIn();
        Assertions.assertFalse(this.booking.canBeModified());
        this.finalizeCheckIn();
        Assertions.assertFalse(this.booking.canBeModified());

        booking.cancel();
        Assertions.assertFalse(this.booking.canBeModified());
        this.finalizeCancellation();
        Assertions.assertFalse(this.booking.canBeModified());
    }

    @Test
    void testGetStatus() {
        Assertions.assertEquals(Booking.BookingStatus.DRAFT, this.booking.getStatus());

        this.makeConfirmable();
        Assertions.assertEquals(Booking.BookingStatus.CONFIRMATION_READY, this.booking.getStatus());
        booking.confirm();
        Assertions.assertEquals(Booking.BookingStatus.PENDING_CONFIRMATION, this.booking.getStatus());
        this.finalizeConfirmation();
        Assertions.assertEquals(Booking.BookingStatus.CONFIRMED, this.booking.getStatus());

        this.makeCheckinable();
        Assertions.assertEquals(Booking.BookingStatus.CHECK_IN_READY, this.booking.getStatus());
        booking.checkIn();
        Assertions.assertEquals(Booking.BookingStatus.PENDING_CHECK_IN, this.booking.getStatus());
        this.finalizeCheckIn();
        Assertions.assertEquals(Booking.BookingStatus.CHECKED_IN, this.booking.getStatus());

        booking.cancel();
        Assertions.assertEquals(Booking.BookingStatus.PENDING_CANCELLATION, this.booking.getStatus());
        this.finalizeCancellation();
        Assertions.assertEquals(Booking.BookingStatus.CANCELLED, this.booking.getStatus());
    }

    @Test
    void testConfirmation() {
        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.confirm());
        Assertions.assertTrue(this.booking.getCommunications().isEmpty());

        this.makeConfirmable();
        this.booking.setPayment(this.payment);
        this.booking.requestSelfCheckIn();

        this.booking.confirm();

        Assertions.assertEquals(1, this.booking.getCommunications().size());
        this.booking.getCommunications().forEach(communication -> {
            Assertions.assertEquals(this.booking, communication.getBooking());
            Assertions.assertEquals(Communication.CommunicationType.BOOKING, communication.getType());
        });

        Assertions.assertFalse(booking.isSelfCheckInRequested());

        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.confirm());
        Assertions.assertEquals(1, this.booking.getCommunications().size());
    }

    @Test
    void testCheckIn() {
        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.checkIn());
        Assertions.assertTrue(this.booking.getCommunications().isEmpty());

        this.makeConfirmable();
        booking.confirm();
        this.finalizeConfirmation();
        this.makeCheckinable();
        booking.requestSelfCheckIn();

        try (var ignored = TestingUtils.getMockedInstantNow()) {
            this.booking.checkIn();
        }

        var checkinComms = this.booking.getCommunications(Communication.CommunicationType.CHECKIN);
        Assertions.assertEquals(1, checkinComms.size());
        Assertions.assertEquals(this.booking, checkinComms.getLast().getBooking());
        Assertions.assertEquals(Communication.CommunicationType.CHECKIN, checkinComms.getLast().getType());

        Assertions.assertFalse(booking.isSelfCheckInRequested());

        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.checkIn());
        Assertions.assertEquals(2, this.booking.getCommunications().size());
    }

    @Test
    void testCancellation() {
        this.booking.setPayment(this.payment);
        booking.requestSelfCheckIn();
        this.makeConfirmable();

        // Booking cannot be canceled when no communications have been sent, only deleted
        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.cancel());

        this.booking.confirm();

        this.booking.cancel();

        var cancellations = this.booking.getCommunications(Communication.CommunicationType.CANCELLATION);
        Assertions.assertEquals(1, cancellations.size());
        cancellations.forEach(communication -> {
            Assertions.assertEquals(this.booking, communication.getBooking());
            Assertions.assertEquals(Communication.CommunicationType.CANCELLATION, communication.getType());
            Assertions.assertEquals(Communication.CommunicationStatus.SUCCEEDED, communication.getStatus());
        });
        Assertions.assertFalse(booking.isSelfCheckInRequested());

        this.makeCheckinable();

        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.confirm());
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            Assertions.assertThrows(IllegalStateException.class, () -> this.booking.checkIn());
        }
        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.cancel());
        Assertions.assertEquals(2, this.booking.getCommunications().size());
    }

    @Test
    void testCancellationVoidsOtherCommunicationsNotSent() {
        this.makeConfirmable();
        booking.confirm();

        Assertions.assertEquals(1, this.booking.getCommunications().size());

        this.booking.cancel();

        Assertions.assertEquals(2, this.booking.getCommunications().size());

        var cancellationComms = this.booking.getCommunications(Communication.CommunicationType.CANCELLATION);
        Assertions.assertEquals(1, cancellationComms.size());
        Assertions.assertEquals(Communication.CommunicationStatus.SUCCEEDED, cancellationComms.getLast().getStatus());

        var confirmationComms = this.booking.getCommunications(Communication.CommunicationType.BOOKING);
        Assertions.assertEquals(1, confirmationComms.size());
        Assertions.assertEquals(Communication.CommunicationStatus.VOIDED, confirmationComms.getLast().getStatus());
    }

    @Test
    void testCancellationVoidsOtherCommunicationsMixed() {
        this.makeConfirmable();
        booking.confirm();
        this.finalizeConfirmation();
        this.makeCheckinable();
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            this.booking.checkIn();
        }

        Assertions.assertEquals(2, this.booking.getCommunications().size());

        this.booking.cancel();

        Assertions.assertEquals(3, this.booking.getCommunications().size());

        Assertions.assertEquals(Communication.CommunicationStatus.PENDING, this.booking.getCommunications(Communication.CommunicationType.CANCELLATION).getLast().getStatus());
        Assertions.assertEquals(Communication.CommunicationStatus.SUCCEEDED, this.booking.getCommunications(Communication.CommunicationType.BOOKING).getLast().getStatus());
        Assertions.assertEquals(Communication.CommunicationStatus.VOIDED, this.booking.getCommunications(Communication.CommunicationType.CHECKIN).getLast().getStatus());
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

    @Test
    void testCanBeDeleted() {
        Assertions.assertTrue(this.booking.canBeDeleted());

        this.makeConfirmable();
        Assertions.assertTrue(this.booking.canBeDeleted());

        this.booking.confirm();
        Assertions.assertFalse(this.booking.canBeDeleted());
    }

    @Test
    void testCanBeConfirmed() {
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            Assertions.assertFalse(this.booking.canBeConfirmed());

            this.makeConfirmable();
            Assertions.assertTrue(this.booking.canBeConfirmed());

            this.booking.confirm();
            Assertions.assertFalse(this.booking.canBeConfirmed());

            this.finalizeConfirmation();
            this.makeCheckinable();
            Assertions.assertFalse(this.booking.canBeConfirmed());

            var bookingReadyForCheckInWithoutConfirmation = new Booking(
                    this.accommodation,
                    TestingUtils.INSTANT,
                    TestingUtils.INSTANT.plusSeconds(3600),
                    1,
                    this.payment,
                    null,
                    null
            );
            new Person(
                    bookingReadyForCheckInWithoutConfirmation,
                    new PersonalInfo("Name", "Surname", "2Surname", null,
                            TestingUtils.INSTANT.minus(18 * 365, ChronoUnit.DAYS), null),
                    new ContactInfo(null, null, "email@email.com"),
                    Document.of(Document.DocumentType.NIF, "54095720L", "SUPPORT"),
                    Address.of("Line1", null, "Municipality", "PostalCode", "USA"),
                    null
            );

            bookingReadyForCheckInWithoutConfirmation.checkIn();
        }
    }

    @Test
    void testCanBeCheckedIn() {
        try (var ignored = TestingUtils.getMockedInstantNow()) {
            Assertions.assertFalse(this.booking.canBeCheckedIn());

            this.makeConfirmable();
            this.makeCheckinable();
            Assertions.assertTrue(this.booking.canBeCheckedIn());

            this.booking.checkIn();
            Assertions.assertFalse(this.booking.canBeCheckedIn());
        }
    }

    @Test
    void testCanBeCancelled() {
        Assertions.assertFalse(this.booking.canBeCancelled());

        this.makeConfirmable();
        Assertions.assertFalse(this.booking.canBeCancelled());

        this.booking.confirm();
        Assertions.assertTrue(this.booking.canBeCancelled());

        this.booking.cancel();
        Assertions.assertFalse(this.booking.canBeCancelled());
    }

    @Test
    void testCanSelfCheckInBeRequested() {
        Assertions.assertFalse(this.booking.canSelfCheckInBeRequested());

        this.makeConfirmable();
        Assertions.assertTrue(this.booking.canSelfCheckInBeRequested());

        this.booking.requestSelfCheckIn();
        Assertions.assertFalse(this.booking.canSelfCheckInBeRequested());

        this.booking.terminateSelfCheckInRequest();
        Assertions.assertTrue(this.booking.canSelfCheckInBeRequested());

        this.booking.confirm();
        Assertions.assertFalse(this.booking.canSelfCheckInBeRequested());

        finalizeConfirmation();
        Assertions.assertTrue(this.booking.canSelfCheckInBeRequested());
    }

    @Test
    void testRequestSelfCheckIn() {
        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.requestSelfCheckIn());

        this.booking.setPayment(this.payment);
        this.booking.requestSelfCheckIn();
        Assertions.assertTrue(this.booking.isSelfCheckInRequested());

        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.requestSelfCheckIn());
    }

    @Test
    void testTerminateSelfCheckInRequest() {
        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.terminateSelfCheckInRequest());

        this.booking.setPayment(this.payment);
        this.booking.requestSelfCheckIn();
        this.booking.terminateSelfCheckInRequest();
        Assertions.assertFalse(this.booking.isSelfCheckInRequested());

        Assertions.assertThrows(IllegalStateException.class, () -> this.booking.terminateSelfCheckInRequest());
    }

    private void makeConfirmable() {
        booking.setPayment(this.payment);
        new Person(this.booking, new PersonalInfo("Name", "Surname", null, null, TestingUtils.PAST_INSTANT, null), new ContactInfo(null, null, "email@email.com"), null, null, null);
    }

    private void makeCheckinable() {
        var person = this.booking.getPeople().getFirst();

        person.setPersonalInfo(new PersonalInfo("Name", "Surname", "2Surname", null, TestingUtils.INSTANT.minus(18 * 365, ChronoUnit.DAYS), null));
        person.setDocument(Document.of(Document.DocumentType.NIF, "54095720L", "SUPPORT"));
        person.setAddress(Address.of("Line1", null, "Municipality", "PostalCode", "USA"));
        person.setRelationship(Person.PersonRelationship.CHILD);
    }

    private void finalizeConfirmation() {
        Assertions.assertEquals(Booking.BookingStatus.PENDING_CONFIRMATION, this.booking.getStatus());

        var communication = booking.getCommunications(Communication.CommunicationType.BOOKING).getLast();
        communication.markSent(UUID.randomUUID(), 0);
        communication.markFinishedSuccessfully(UUID.randomUUID());

        Assertions.assertEquals(Booking.BookingStatus.CONFIRMED, this.booking.getStatus());
    }

    private void finalizeCheckIn() {
        Assertions.assertEquals(Booking.BookingStatus.PENDING_CHECK_IN, this.booking.getStatus());

        var communication = booking.getCommunications(Communication.CommunicationType.CHECKIN).getLast();
        communication.markSent(UUID.randomUUID(), 0);
        communication.markFinishedSuccessfully(UUID.randomUUID());

        Assertions.assertEquals(Booking.BookingStatus.CHECKED_IN, this.booking.getStatus());
    }

    private void finalizeCancellation() {
        Assertions.assertEquals(Booking.BookingStatus.PENDING_CANCELLATION, this.booking.getStatus());

        var communication = (CancellationCommunication) booking.getCommunications(Communication.CommunicationType.CANCELLATION).getLast();
        communication.markFinishedSuccessfully();

        Assertions.assertEquals(Booking.BookingStatus.CANCELLED, this.booking.getStatus());
    }
}
