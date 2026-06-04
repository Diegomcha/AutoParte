package me.diegomcha.autoparte.domain;

import lombok.*;
import me.diegomcha.autoparte.domain.base.BaseEntity;
import me.diegomcha.autoparte.domain.booking.payment.Payment;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Booking extends BaseEntity {

    private @NonNull Instant startTime;
    private @NonNull Instant endTime;
    private int numberOfPeople;
    private Integer numberOfRooms;
    private Payment payment;
    private Boolean internetConnection;

    private @NonNull Establishment establishment;
    @Setter(AccessLevel.NONE)
    private @NonNull Set<@NonNull Person> people = new HashSet<>();

    public Booking(@NonNull Establishment establishment, @NonNull Instant startTime, @NonNull Instant endTime, int numberOfPeople) {
        this(establishment, startTime, endTime, numberOfPeople, null, null, null);
    }

    public Booking(@NonNull Establishment establishment, @NonNull Instant startTime, @NonNull Instant endTime, int numberOfPeople, Integer numberOfRooms, Payment payment, Boolean internetConnection) {
        this.setEstablishment(establishment);
        this.startTime = startTime;
        this.setEndTime(endTime);
        this.setNumberOfPeople(numberOfPeople);
        this.setNumberOfRooms(numberOfRooms);
        this.setPayment(payment);
        this.setInternetConnection(internetConnection);
    }

    public void setEstablishment(@NonNull Establishment establishment) {
        if (this.establishment != null) this.establishment._getBookings().remove(this);
        this.establishment = establishment;
        this.establishment._getBookings().add(this);
    }

    public Boolean getInternetConnection() {
        return Optional
                .ofNullable(this.internetConnection)
                .orElse(this.establishment.getInternetConnection());
    }

    public void setStartTime(@NonNull Instant startTime) {
        if (!startTime.isBefore(endTime))
            throw new IllegalArgumentException("Start time must be before end time");
        this.startTime = startTime;
    }

    public void setEndTime(@NonNull Instant endTime) {
        if (!startTime.isBefore(endTime))
            throw new IllegalArgumentException("End time must be after start time");
        this.endTime = endTime;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        if (numberOfPeople <= 0)
            throw new IllegalArgumentException("Number of people must be greater than 0");
        this.numberOfPeople = numberOfPeople;
    }

    public void setNumberOfRooms(Integer numberOfRooms) {
        if (numberOfRooms != null && numberOfRooms <= 0)
            throw new IllegalArgumentException("Number of rooms must be greater than 0");
        this.numberOfRooms = numberOfRooms;
    }

    public Set<Person> getPeople() {
        return Set.copyOf(this.people);
    }

    void _addPerson(@NonNull Person person) {
        if (this.people.size() >= this.numberOfPeople)
            throw new IllegalStateException("Cannot add more people than the number specified in the booking");

        this.people.add(person);
    }

    void _removePerson(@NonNull Person person) {
        this.people.remove(person);
    }
}
