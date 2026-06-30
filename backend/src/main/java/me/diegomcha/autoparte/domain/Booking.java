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

        CONFIRMATION_READY,
        PENDING_CONFIRMATION,
        CONFIRMED,

        CHECK_IN_READY,
        PENDING_CHECK_IN,
        CHECKED_IN,

        PENDING_CANCELLATION,
        CANCELLED
    }

    @Setter
    private boolean published = false;
    private @NonNull BookingStatus status = BookingStatus.DRAFT;

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

    /**
     * Creates a new booking for the given accommodation with the specified details.
     *
     * @param accommodation      The accommodation for which the booking is being made
     * @param startTime          The start time of the booking
     * @param endTime            The end time of the booking
     * @param numberOfPeople     The number of people included in the booking
     * @param payment            The payment details for the booking
     * @param numberOfRooms      The number of rooms included in the booking (optional)
     * @param internetConnection Whether the booking requires internet connection (optional)
     * @throws IllegalArgumentException if the start time is not before the end time,
     *                                  or if the number of people is less than or equal to zero,
     *                                  or if the number of rooms is less than or equal to zero
     */
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

    /**
     * Returns the internet connection status for the booking.
     * If the internet connection is not explicitly set for the booking,
     * it falls back to the accommodation's internet connection status.
     *
     * @return Boolean indicating whether internet connection is available for the booking
     */
    public Boolean getInternetConnection() {
        return Optional
                .ofNullable(this.internetConnection)
                .orElse(this.accommodation.getInternetConnection());
    }

    /**
     * Sets the start time for the booking.
     *
     * @param startTime The start time of the booking
     * @throws IllegalArgumentException if the start time is not before the end time
     */
    public void setStartTime(@NonNull Instant startTime) {
        if (!startTime.isBefore(endTime))
            throw new IllegalArgumentException("Start time must be before end time");
        this.startTime = startTime;
    }

    /**
     * Sets the end time for the booking.
     *
     * @param endTime The end time of the booking
     * @throws IllegalArgumentException if the end time is not after the start time
     */
    public void setEndTime(@NonNull Instant endTime) {
        if (!startTime.isBefore(endTime))
            throw new IllegalArgumentException("End time must be after start time");
        this.endTime = endTime;
    }

    /**
     * Sets the number of people for the booking.
     *
     * @param numberOfPeople The number of people included in the booking
     * @throws IllegalArgumentException if the number of people is less than or equal to zero
     * @throws IllegalStateException    if the number of people is less than the number of people already added to the booking
     */
    public void setNumberOfPeople(int numberOfPeople) {
        if (numberOfPeople <= 0)
            throw new IllegalArgumentException("Number of people must be greater than 0");
        if (people.size() > numberOfPeople)
            throw new IllegalStateException("Cannot set number of people less than the number of people already added to the booking");

        this.numberOfPeople = numberOfPeople;
    }

    /**
     * Sets the number of rooms for the booking.
     *
     * @param numberOfRooms The number of rooms included in the booking
     * @throws IllegalArgumentException if the number of rooms is less than or equal to zero
     */
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

    /**
     * Removes a person from the booking.
     *
     * @param person The person to be removed from the booking
     * @throws IllegalArgumentException if the person is not found in the booking
     */
    public void removePerson(@NonNull Person person) {
        if (!this.people.remove(person))
            throw new IllegalArgumentException("Person not found in booking");
    }

    /**
     * Checks if the booking can be modified based on its current status.
     *
     * @return true if the booking can be modified, false otherwise
     */
    public boolean canBeModified() {
        return this.getStatus() == BookingStatus.DRAFT ||
                this.getStatus() == BookingStatus.CONFIRMATION_READY ||
                this.getStatus() == BookingStatus.CONFIRMED ||
                this.getStatus() == BookingStatus.CHECK_IN_READY;
    }

    /**
     * Confirms the booking by adding a booking communication if it can be confirmed.
     *
     * @throws IllegalStateException if the booking cannot be confirmed
     */
    public void confirm() {
        if (this.getStatus() != BookingStatus.CONFIRMATION_READY)
            throw new IllegalStateException("Booking cannot be confirmed");
        this.addCommunication(Communication.CommunicationType.BOOKING);
    }

    /**
     * Checks in the booking by adding a check-in communication if it can be checked-in.
     *
     * @throws IllegalStateException if the booking cannot be checked-in
     */
    public void checkIn() {
        if (this.getStatus() != BookingStatus.CHECK_IN_READY)
            throw new IllegalStateException("Booking cannot be checked-in");

        this.published = false;

        this.addCommunication(Communication.CommunicationType.CHECKIN);
    }

    /**
     * Cancels the booking by adding a cancellation communication and marking all other communications as voided if they are pending or failed.
     * If all other communications are voided, the cancellation communication is marked as finished successfully.
     * Otherwise, the cancellation communication will be marked as pending and will be processed asynchronously.
     *
     * @throws IllegalStateException if the booking is already cancelled or if a cancellation communication already
     */
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

    public void _updateState() {
        // Cancelled
        if (this.hasSuccessfulCommunicationOfType(Communication.CommunicationType.CANCELLATION))
            this.status = BookingStatus.CANCELLED;
        else if (this.hasCommunicationOfType(Communication.CommunicationType.CANCELLATION))
            this.status = BookingStatus.PENDING_CANCELLATION;

            // Checked-in
        else if (this.hasSuccessfulCommunicationOfType(Communication.CommunicationType.CHECKIN))
            this.status = BookingStatus.CHECKED_IN;
        else if (this.hasCommunicationOfType(Communication.CommunicationType.CHECKIN))
            this.status = BookingStatus.PENDING_CHECK_IN;
        else if (this.people.size() == this.numberOfPeople &&
                this.people.stream().allMatch(Person::isComplete))
            this.status = BookingStatus.CHECK_IN_READY;

            // Confirmed
        else if (this.hasSuccessfulCommunicationOfType(Communication.CommunicationType.BOOKING))
            this.status = BookingStatus.CONFIRMED;
        else if (this.hasCommunicationOfType(Communication.CommunicationType.BOOKING))
            this.status = BookingStatus.PENDING_CONFIRMATION;
        else if (this.payment != null && !this.people.isEmpty())
            this.status = BookingStatus.CONFIRMATION_READY;

            // Draft
        else this.status = BookingStatus.DRAFT;
    }

    public Set<Communication> getCommunications() {
        return Set.copyOf(this.communications);
    }

    private Communication addCommunication(@NonNull Communication.CommunicationType type) {
        if (this.getStatus() == BookingStatus.CANCELLED || this.getStatus() == BookingStatus.PENDING_CANCELLATION)
            throw new IllegalStateException("Cannot make any communication to a cancelled booking");

        if (hasCommunicationOfType(type))
            throw new IllegalStateException("Communication of type " + type + " already exists for this booking");

        var communication = Communication._of(this, type);
        this.communications.add(communication);
        return communication;
    }

    private boolean hasCommunicationOfType(@NonNull Communication.CommunicationType type) {
        // Exclude communications that have failed, as they can be retried and should not block the booking state
        return this.communications.stream().anyMatch(c -> c.getType() == type && c.getStatus() != Communication.CommunicationStatus.FAILED);
    }

    private boolean hasSuccessfulCommunicationOfType(@NonNull Communication.CommunicationType type) {
        return this.communications.stream().anyMatch(c -> c.getType() == type && c.getStatus() == Communication.CommunicationStatus.SUCCEEDED);
    }
}
