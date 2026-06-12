package me.diegomcha.autoparte.domain;

import lombok.*;
import me.diegomcha.autoparte.domain.base.BaseEntity;
import me.diegomcha.autoparte.domain.booking.payment.PaymentInfo;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

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
    private PaymentInfo payment;
    private Boolean internetConnection;

    @CreatedBy
    @Setter(AccessLevel.NONE)
    private @NonNull Account createdBy;
    @LastModifiedBy
    @Setter(AccessLevel.NONE)
    private @NonNull Account lastModifiedBy;

    private @NonNull Accommodation accommodation;
    @Setter(AccessLevel.NONE)
    private @NonNull Set<@NonNull Person> people = new HashSet<>();

    public Booking(@NonNull Accommodation accommodation, @NonNull Instant startTime, @NonNull Instant endTime, int numberOfPeople) {
        this(accommodation, startTime, endTime, numberOfPeople, null, null, null);
    }

    public Booking(@NonNull Accommodation accommodation, @NonNull Instant startTime, @NonNull Instant endTime, int numberOfPeople, Integer numberOfRooms, PaymentInfo payment, Boolean internetConnection) {
        this.setAccommodation(accommodation);
        this.startTime = startTime;
        this.setEndTime(endTime);
        this.setNumberOfPeople(numberOfPeople);
        this.setNumberOfRooms(numberOfRooms);
        this.setPayment(payment);
        this.setInternetConnection(internetConnection);
    }

    public void setAccommodation(@NonNull Accommodation accommodation) {
        if (this.accommodation != null) this.accommodation._getBookings().remove(this);
        this.accommodation = accommodation;
        this.accommodation._getBookings().add(this);
    }

    public Boolean getInternetConnection() {
        return Optional
                .ofNullable(this.internetConnection)
                .orElse(this.accommodation.getInternetConnection());
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
