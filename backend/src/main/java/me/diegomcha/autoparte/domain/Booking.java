package me.diegomcha.autoparte.domain;

import lombok.*;
import me.diegomcha.autoparte.domain.base.BaseEntity;
import me.diegomcha.autoparte.domain.booking.payment.Payment;
import me.diegomcha.autoparte.domain.communication.CancellationCommunication;
import me.diegomcha.autoparte.domain.communication.Communication;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.Instant;
import java.util.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Booking extends BaseEntity {

    public enum BookingStatus {
        DRAFT,
        CONFIRMED,
        CHECKED_IN,
        CANCELLED
    }

    private @NonNull Instant startTime;
    private @NonNull Instant endTime;
    private int numberOfPeople;
    @Setter
    private Payment payment;
    private Integer numberOfRooms;
    @Setter
    private Boolean internetConnection;

    @CreatedBy
    private Account createdBy;
    @LastModifiedBy
    private Account lastModifiedBy;

    private @NonNull Accommodation accommodation;
    @ToString.Exclude
    private final @NonNull List<@NonNull Person> people = new ArrayList<>();
    @ToString.Exclude
    private final @NonNull Set<@NonNull Communication> communications = new HashSet<>();

    public Booking(@NonNull Accommodation accommodation, @NonNull Instant startTime, @NonNull Instant endTime, int numberOfPeople, Payment payment, Integer numberOfRooms, Boolean internetConnection) {
        this.setAccommodation(accommodation);
        this.startTime = startTime;
        this.setEndTime(endTime);
        this.setNumberOfPeople(numberOfPeople);
        this.setNumberOfRooms(numberOfRooms);
        this.setPayment(payment);
        this.setInternetConnection(internetConnection);
    }

    private void setAccommodation(@NonNull Accommodation accommodation) {
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

    public List<Person> getPeople() {
        return List.copyOf(this.people);
    }

    void _addPerson(@NonNull Person person) {
        if (this.people.size() >= this.numberOfPeople)
            throw new IllegalStateException("Cannot add more people than the number specified in the booking");
        this.people.add(person);
    }

    public boolean canBeConfirmed() {
        return this.getStatus() == BookingStatus.DRAFT &&
                this.payment != null &&
                !this.people.isEmpty();
    }

    public boolean canBeCheckedIn() {
        return this.getStatus() == BookingStatus.CONFIRMED &&
                this.people.size() == this.numberOfPeople &&
                this.people.stream().allMatch(Person::isComplete);
    }

    public void confirm() {
        if (!this.canBeConfirmed())
            throw new IllegalStateException("Booking cannot be confirmed");
        this.addCommunication(Communication.CommunicationType.BOOKING);
    }

    public void checkIn() {
        if (!this.canBeCheckedIn())
            throw new IllegalStateException("Booking cannot be checked-in");
        this.addCommunication(Communication.CommunicationType.CHECKIN);
    }

    public void cancel() {
        var communication = (CancellationCommunication) this.addCommunication(Communication.CommunicationType.CANCELLATION);

        // Mark all other communications as voided if they are pending or failed (not in SES)
        this.communications.stream()
                .filter(c -> c.getType() != Communication.CommunicationType.CANCELLATION)
                .filter(c -> c.getStatus() == Communication.CommunicationStatus.PENDING || c.getStatus() == Communication.CommunicationStatus.FAILED)
                .forEach(Communication::markVoided);

        // If all other communications are voided, mark the cancellation communication as finished successfully
        if (this.communications.stream()
                .filter(c -> c.getType() != Communication.CommunicationType.CANCELLATION)
                .allMatch(c -> c.getStatus() == Communication.CommunicationStatus.VOIDED))
            communication.markFinishedSuccessfully();
    }

    public BookingStatus getStatus() {
        if (this.communications.stream().anyMatch(c -> c.getType() == Communication.CommunicationType.CANCELLATION))
            return BookingStatus.CANCELLED;

        if (this.communications.stream().anyMatch(c -> c.getType() == Communication.CommunicationType.CHECKIN))
            return BookingStatus.CHECKED_IN;

        if (this.communications.stream().anyMatch(c -> c.getType() == Communication.CommunicationType.BOOKING))
            return BookingStatus.CONFIRMED;

        return BookingStatus.DRAFT;
    }

    public Set<Communication> getCommunications() {
        return Set.copyOf(this.communications);
    }

    private Communication addCommunication(@NonNull Communication.CommunicationType type) {
        if (this.getStatus() == BookingStatus.CANCELLED)
            throw new IllegalStateException("Cannot make any communication to a cancelled booking");

        if (this.communications.stream().anyMatch(c -> c.getType() == type))
            throw new IllegalStateException("Communication of type " + type + " already exists for this booking");

        var communication = Communication._of(this, type);
        this.communications.add(communication);
        return communication;
    }
}
