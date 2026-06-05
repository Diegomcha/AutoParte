package me.diegomcha.autoparte.domain;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingTest {

    private Establishment establishment;
    private Booking booking;

    @BeforeEach
    void setUp() {
        this.establishment = new Establishment("Test", "SESCODE");
        this.booking = new Booking(establishment, TestingUtils.INSTANT, TestingUtils.INSTANT.plusSeconds(3600), 1);
    }

    @Test
    void testEstablishmentAssociation() {
        Assertions.assertEquals(this.establishment, this.booking.getEstablishment());
        Assertions.assertTrue(this.establishment.getBookings().contains(this.booking));

        Establishment newEstablishment = new Establishment("New Test", "NEWSESCODE");
        this.booking.setEstablishment(newEstablishment);

        Assertions.assertEquals(newEstablishment, this.booking.getEstablishment());
        Assertions.assertFalse(this.establishment.getBookings().contains(this.booking));
        Assertions.assertTrue(newEstablishment.getBookings().contains(this.booking));
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

        Assertions.assertThrows(IllegalArgumentException.class, () -> new Booking(this.establishment, laterTime, earlierTime, 1));

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
        Assertions.assertNull(this.establishment.getInternetConnection());
        Assertions.assertNull(this.booking.getInternetConnection());

        this.establishment.setInternetConnection(true);
        Assertions.assertTrue(this.booking.getInternetConnection());

        this.establishment.setInternetConnection(false);
        Assertions.assertFalse(this.booking.getInternetConnection());

        this.booking.setInternetConnection(true);
        Assertions.assertTrue(this.booking.getInternetConnection());

        this.booking.setInternetConnection(false);
        Assertions.assertFalse(this.booking.getInternetConnection());

        this.booking.setInternetConnection(null);
        Assertions.assertFalse(this.booking.getInternetConnection());
    }
}
