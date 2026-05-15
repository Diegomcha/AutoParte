package me.diegomcha.autoparte.model;

import lombok.*;
import me.diegomcha.autoparte.model.base.BaseEntity;
import me.diegomcha.autoparte.model.booking.payment.Payment;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Booking extends BaseEntity {

    private boolean cancelled = false;

    @Setter(AccessLevel.PACKAGE)
    private Establishment establishment;
    @Setter(AccessLevel.NONE)
    private Set<Person> people = new HashSet<>();
    private Payment payment;

    private @NonNull Instant startTime;
    private @NonNull Instant endTime;

    private short numberOfPeople;
    private Short numberOfRooms;
    private Boolean internetConnection;

    public Booking(@NonNull Instant startTime, @NonNull Instant endTime, short numberOfPeople, Short numberOfRooms, Payment payment, Boolean internetConnection) {
        this.startTime = Objects.requireNonNull(startTime);
        this.setEndTime(endTime);
        this.setNumberOfPeople(numberOfPeople);
        this.setNumberOfRooms(numberOfRooms);
        this.setPayment(payment);
        this.setInternetConnection(internetConnection);
    }

    public Boolean getInternetConnection() {
        return Optional
                .ofNullable(this.internetConnection)
                .orElse(Optional
                        .ofNullable(this.establishment)
                        .map(Establishment::getInternetConnection)
                        .orElse(null));
    }

    public void setStartTime(@NonNull Instant startTime) {
        if (!Objects.requireNonNull(startTime).isBefore(endTime))
            throw new IllegalArgumentException("Start time must be before end time");
        this.startTime = startTime;
    }

    public void setEndTime(@NonNull Instant endTime) {
        if (!startTime.isBefore(Objects.requireNonNull(endTime)))
            throw new IllegalArgumentException("End time must be after start time");
        this.endTime = endTime;
    }

    public Set<Person> getPeople() {
        return Set.copyOf(this.people);
    }

    public void addPerson(Person person) {
        if (this.people.size() >= this.numberOfPeople)
            throw new IllegalStateException("Cannot add more people than the number specified in the booking");
        this.people.add(person);
        person.setBooking(this);
    }

    public void removePerson(Person person) {
        this.people.remove(person);
        person.setBooking(null);
    }
}
