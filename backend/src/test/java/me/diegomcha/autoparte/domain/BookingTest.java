package me.diegomcha.autoparte.domain;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingTest {

    private Accommodation accommodation;
    private Booking booking;

    @BeforeEach
    void setUp() {
        this.accommodation = new Accommodation("Test", "SESCODE", null);
        this.booking = new Booking(accommodation, TestingUtils.INSTANT, TestingUtils.INSTANT.plusSeconds(3600), 1);
    }

    @Test
    void testAccommodationAssociation() {
        Assertions.assertEquals(this.accommodation, this.booking.getAccommodation());
        Assertions.assertTrue(this.accommodation.getBookings().contains(this.booking));

        Accommodation newAccommodation = new Accommodation("New Test", "NEWSESCODE", null);
        this.booking.setAccommodation(newAccommodation);

        Assertions.assertEquals(newAccommodation, this.booking.getAccommodation());
        Assertions.assertFalse(this.accommodation.getBookings().contains(this.booking));
        Assertions.assertTrue(newAccommodation.getBookings().contains(this.booking));
    }

    @Test
    void testNumberOfPeople() {
        Assertions.assertEquals(1, this.booking.getNumberOfPeople());

        var personalInfo = new PersonalInfo("Name", "Surname");

        new Person(this.booking, personalInfo);
        Assertions.assertThrows(IllegalStateException.class, () -> new Person(this.booking, personalInfo));
    }

    @Test
    void testDatesRangeValidation() {
        var earlierTime = this.booking.getStartTime();
        var laterTime = this.booking.getEndTime();
        var evenEarlierTime = earlierTime.minusSeconds(3600);
        var evenLaterTime = laterTime.plusSeconds(3600);

        Assertions.assertThrows(IllegalArgumentException.class, () -> new Booking(this.accommodation, laterTime, earlierTime, 1));

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
}
