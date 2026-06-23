package me.diegomcha.autoparte.api.booking;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.api.booking.dto.BookingDtoRequest;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.Person;
import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.booking.payment.Payment;
import me.diegomcha.autoparte.domain.person.ContactInfo;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class BookingServiceTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private AccommodationRepo accommodationRepo;
    @Autowired
    private BookingRepo bookingRepo;

    private Accommodation accommodation;
    private Booking booking;

    @BeforeEach
    void setUp() {
        this.accommodation = accommodationRepo.save(new Accommodation("Test Accommodation", "SESCODE", null));
        this.booking = bookingRepo.save(new Booking(accommodation, TestingUtils.INSTANT, TestingUtils.FUTURE_INSTANT, 1, null, null, null));
    }

    @Test
    void testGetBookings() throws ResourceNotFoundException {
        // One booking
        var page = bookingService.getBookings(accommodation.getId(), Pageable.unpaged());

        Assertions.assertEquals(1, page.getTotalElements());
        Assertions.assertEquals(booking.getId(), page.getContent().getFirst().id());

        // No bookings
        bookingRepo.delete(booking);
        var emptyPage = bookingService.getBookings(accommodation.getId(), Pageable.unpaged());

        Assertions.assertEquals(0, emptyPage.getTotalElements());
    }

    @Test
    void testGetBookingsFailed() {
        var nonExistentAccommodationId = UUID.randomUUID();

        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                bookingService.getBookings(nonExistentAccommodationId, Pageable.unpaged()));
    }

    @Test
    void testGetBooking() throws ResourceNotFoundException {
        var bookingResponse = bookingService.getBooking(accommodation.getId(), booking.getId());

        Assertions.assertEquals(booking.getId(), bookingResponse.id());

        Assertions.assertEquals(booking.canBeConfirmed(), bookingResponse.canBeConfirmed());
        Assertions.assertEquals(booking.canBeCheckedIn(), bookingResponse.canBeCheckedIn());

        Assertions.assertNotNull(bookingResponse.communications());
    }

    @Test
    void testGetBookingFailed() {
        var randomId = UUID.randomUUID();

        // Non-existent booking for existing accommodation
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                bookingService.getBooking(accommodation.getId(), randomId));

        // Non-existent accommodation
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                bookingService.getBooking(randomId, booking.getId()));

        // Non-existent accommodation and booking
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                bookingService.getBooking(randomId, randomId));
    }

    @Test
    void testCreateBooking() throws ResourceNotFoundException {
        var newBooking = new BookingDtoRequest(TestingUtils.INSTANT, TestingUtils.FUTURE_INSTANT, 2, new BookingDtoRequest.PaymentDtoRequest(Payment.PaymentType.ON_SITE, null, null, null, null), null, null);

        bookingService.createBooking(accommodation.getId(), newBooking);

        var bookings = bookingRepo.findByAccommodationId(accommodation.getId(), Pageable.unpaged());
        var dbBooking = bookings.getContent().stream().filter(b -> b.getNumberOfPeople() == 2).findFirst();

        Assertions.assertTrue(dbBooking.isPresent());
        Assertions.assertNotNull(dbBooking.get().getPayment());
        Assertions.assertEquals(Payment.PaymentType.ON_SITE, dbBooking.get().getPayment().getType());
    }

    @Test
    void testFailedCreateBooking() {
        var newBooking = new BookingDtoRequest(TestingUtils.INSTANT, TestingUtils.FUTURE_INSTANT, 2, new BookingDtoRequest.PaymentDtoRequest(Payment.PaymentType.ON_SITE, null, null, null, null), null, null);

        // Non-existent accommodation
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                bookingService.createBooking(UUID.randomUUID(), newBooking));
    }

    @Test
    void testUpdateBooking() throws ResourceNotFoundException {
        // Regular update

        var updateBooking = new BookingDtoRequest(TestingUtils.PAST_INSTANT, TestingUtils.INSTANT, 3, new BookingDtoRequest.PaymentDtoRequest(Payment.PaymentType.CASH, "MEAN", null, null, null), null, false);
        bookingService.updateBooking(accommodation.getId(), booking.getId(), updateBooking);

        var dbBooking = bookingRepo.findByAccommodationIdAndId(accommodation.getId(), booking.getId());

        Assertions.assertTrue(dbBooking.isPresent());
        Assertions.assertEquals(TestingUtils.PAST_INSTANT, dbBooking.get().getStartTime());
        Assertions.assertEquals(TestingUtils.INSTANT, dbBooking.get().getEndTime());
        Assertions.assertEquals(3, dbBooking.get().getNumberOfPeople());
        Assertions.assertEquals(Payment.PaymentType.CASH, dbBooking.get().getPayment().getType());
        Assertions.assertEquals("MEAN", dbBooking.get().getPayment().getMean());
        Assertions.assertEquals(false, dbBooking.get().getInternetConnection());

        // Update with null payment mean

        var updateNullBooking = new BookingDtoRequest(TestingUtils.PAST_INSTANT, TestingUtils.INSTANT, 3, new BookingDtoRequest.PaymentDtoRequest(Payment.PaymentType.CASH, null, null, null, null), null, false);
        bookingService.updateBooking(accommodation.getId(), booking.getId(), updateNullBooking);

        var dbNullBooking = bookingRepo.findByAccommodationIdAndId(accommodation.getId(), booking.getId());

        Assertions.assertTrue(dbNullBooking.isPresent());
        Assertions.assertNull(dbNullBooking.get().getPayment().getMean());
    }

    @Test
    void testUpdateBookingFailed() {
        var randomId = UUID.randomUUID();
        var updateBooking = new BookingDtoRequest(TestingUtils.PAST_INSTANT, TestingUtils.INSTANT, 3, new BookingDtoRequest.PaymentDtoRequest(Payment.PaymentType.CASH, "MEAN", null, null, null), null, false);

        // Non-existent booking for existing accommodation
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                bookingService.updateBooking(accommodation.getId(), randomId, updateBooking));

        // Non-existent accommodation
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                bookingService.updateBooking(randomId, booking.getId(), updateBooking));

        // Non-existent accommodation and booking
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                bookingService.updateBooking(randomId, randomId, updateBooking));
    }

    @Test
    void testConfirmBooking() throws ResourceNotFoundException, ResourceConflictException {
        this.makeConfirmable();

        bookingService.confirmBooking(accommodation.getId(), booking.getId());

        var dbBooking = bookingRepo.findByAccommodationIdAndId(accommodation.getId(), booking.getId());

        Assertions.assertTrue(dbBooking.isPresent());
        Assertions.assertEquals(Booking.BookingStatus.CONFIRMED, dbBooking.get().getStatus());
    }

    @Test
    void testConfirmBookingFailed() {
        // Non-existent booking for existing accommodation
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                bookingService.confirmBooking(accommodation.getId(), UUID.randomUUID()));

        // Non-existent accommodation
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                bookingService.confirmBooking(UUID.randomUUID(), booking.getId()));

        // Non-existent accommodation and booking
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                bookingService.confirmBooking(UUID.randomUUID(), UUID.randomUUID()));

        // Booking cannot be confirmed
        Assertions.assertThrows(ResourceConflictException.class, () ->
                bookingService.confirmBooking(accommodation.getId(), booking.getId()));
    }

    @Test
    void testCheckInBooking() throws ResourceNotFoundException, ResourceConflictException {
        this.makeCheckinable(true);

        bookingService.checkInBooking(accommodation.getId(), booking.getId());

        var dbBooking = bookingRepo.findByAccommodationIdAndId(accommodation.getId(), booking.getId());

        Assertions.assertTrue(dbBooking.isPresent());
        Assertions.assertEquals(Booking.BookingStatus.CHECKED_IN, dbBooking.get().getStatus());
    }

    @Test
    void testCheckInBookingFailed() {
        // Non-existent booking for existing accommodation
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                bookingService.checkInBooking(accommodation.getId(), UUID.randomUUID()));

        // Non-existent accommodation
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                bookingService.checkInBooking(UUID.randomUUID(), booking.getId()));

        // Non-existent accommodation and booking
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                bookingService.checkInBooking(UUID.randomUUID(), UUID.randomUUID()));

        // Booking cannot be checked in
        Assertions.assertThrows(ResourceConflictException.class, () ->
                bookingService.checkInBooking(accommodation.getId(), booking.getId()));
    }

    @Test
    void testCancelBooking() throws ResourceNotFoundException, ResourceConflictException {
        bookingService.cancelBooking(accommodation.getId(), booking.getId());

        var dbBooking = bookingRepo.findByAccommodationIdAndId(accommodation.getId(), booking.getId());

        Assertions.assertTrue(dbBooking.isPresent());
        Assertions.assertEquals(Booking.BookingStatus.CANCELLED, dbBooking.get().getStatus());
    }

    @Test
    void testCancelBookingFailed() throws ResourceConflictException, ResourceNotFoundException {
        // Non-existent booking for existing accommodation
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                bookingService.cancelBooking(accommodation.getId(), UUID.randomUUID()));

        // Non-existent accommodation
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                bookingService.cancelBooking(UUID.randomUUID(), booking.getId()));

        // Non-existent accommodation and booking
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                bookingService.cancelBooking(UUID.randomUUID(), UUID.randomUUID()));

        // Booking already cancelled
        bookingService.cancelBooking(accommodation.getId(), booking.getId());
        Assertions.assertThrows(ResourceConflictException.class, () ->
                bookingService.cancelBooking(accommodation.getId(), booking.getId()));
    }

    private void makeConfirmable() {
        Assertions.assertFalse(this.booking.canBeConfirmed());

        this.booking.setPayment(Payment.of(Payment.PaymentType.ON_SITE, null, null, null, null));
        new Person(this.booking, new PersonalInfo("Name", "Surname"), new ContactInfo(null, null, "email@email.com"), null, null, null);

        Assertions.assertTrue(this.booking.canBeConfirmed());
    }

    private void makeCheckinable(boolean confirm) {
        Assertions.assertFalse(this.booking.canBeCheckedIn());

        this.booking.setPayment(Payment.of(Payment.PaymentType.CREDIT_CARD, null, null, null, null));
        var person = new Person(this.booking, new PersonalInfo("Name", "Surname"), new ContactInfo(null, null, "email@email.com"), null, null, null);

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
