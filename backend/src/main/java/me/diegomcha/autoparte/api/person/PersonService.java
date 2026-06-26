package me.diegomcha.autoparte.api.person;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.api.common.EntityMapper;
import me.diegomcha.autoparte.api.person.dto.PersonDtoRequest;
import me.diegomcha.autoparte.api.person.dto.PersonDtoResponse;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import me.diegomcha.autoparte.core.repos.AddressRepo;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import me.diegomcha.autoparte.core.repos.PersonRepo;
import me.diegomcha.autoparte.domain.address.Address;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class PersonService {

    private static final Supplier<ResourceNotFoundException> BOOKING_NOT_FOUND_EXCEPTION = () ->
            new ResourceNotFoundException("Booking not found");
    private static final Supplier<ResourceNotFoundException> ADDRESS_FOUND_EXCEPTION = () ->
            new ResourceNotFoundException("Address not found");
    private static final Supplier<ResourceNotFoundException> NOT_FOUND_EXCEPTION = () ->
            new ResourceNotFoundException("Person not found");
    private static final Supplier<ResourceConflictException> CANNOT_BE_MODIFIED_EXCEPTION = () ->
            new ResourceConflictException("Booking cannot be modified in its current state");
    private static final Supplier<ResourceConflictException> BOOKING_FULL_EXCEPTION = () ->
            new ResourceConflictException("Booking is full");

    private final BookingRepo bookingRepo;
    private final PersonRepo personRepo;
    private final PersonMapper personMapper;
    private final EntityMapper entityDtoMapper;
    private final AddressRepo addressRepo;

    /**
     * Returns a list of people associated with a specific booking for a given accommodation.
     *
     * @param accommodationId The ID of the accommodation to which the booking belongs
     * @param bookingId       The ID of the booking for which to retrieve associated people
     * @return A list of people associated with the specified booking
     * @throws ResourceNotFoundException if no booking with the given ID exists for the specified accommodation
     */
    public List<PersonDtoResponse> getPeople(UUID accommodationId, UUID bookingId) throws ResourceNotFoundException {
        this.ensureBookingExists(accommodationId, bookingId);

        return personMapper.toResponse(personRepo.findByBookingAccommodationIdAndBookingId(accommodationId, bookingId));
    }

    /**
     * Returns a specific person associated with a booking for a given accommodation.
     *
     * @param accommodationId The ID of the accommodation to which the booking belongs
     * @param bookingId       The ID of the booking for which to retrieve the associated person
     * @param personId        The ID of the person to retrieve
     * @return The person associated with the specified booking and accommodation
     * @throws ResourceNotFoundException if no booking with the given ID exists for the specified accommodation or if no person with the given ID exists for the specified booking
     */
    public PersonDtoResponse getPerson(UUID accommodationId, UUID bookingId, UUID personId) throws ResourceNotFoundException {
        this.ensureBookingExists(accommodationId, bookingId);

        return personRepo
                .findByBookingAccommodationIdAndBookingIdAndId(accommodationId, bookingId, personId)
                .map(personMapper::toResponse)
                .orElseThrow(NOT_FOUND_EXCEPTION);
    }

    /**
     * Adds a new person to a specific booking for a given accommodation.
     *
     * @param accommodationId The ID of the accommodation to which the booking belongs
     * @param bookingId       The ID of the booking to which the person will be added
     * @param dto             The DTO containing the details of the person to be added
     * @return An EntityDtoCreated object containing the ID of the newly created person
     * @throws ResourceNotFoundException if no booking with the given ID exists for the specified accommodation or
     *                                   if no address with the given ID exists
     * @throws ResourceConflictException if the booking cannot be modified in its current state or
     *                                   if the booking is already full and cannot accommodate additional people
     */
    @Transactional
    public EntityDtoCreated addPerson(UUID accommodationId, UUID bookingId, PersonDtoRequest dto) throws ResourceNotFoundException, ResourceConflictException {
        this.ensureBookingCanBeModified(accommodationId, bookingId);

        var booking = bookingRepo
                .findByAccommodationIdAndId(accommodationId, bookingId)
                .orElseThrow(BOOKING_NOT_FOUND_EXCEPTION);

        // Check if the booking is not full before adding a new person
        if (booking.getPeople().size() >= booking.getNumberOfPeople())
            throw BOOKING_FULL_EXCEPTION.get();

        var newPerson = personMapper.fromCreate(booking, dto, this.getAddressById(dto.address()));
        newPerson = personRepo.save(newPerson);
        return entityDtoMapper.toCreated(newPerson);
    }

    /**
     * Updates an existing person associated with a specific booking for a given accommodation.
     *
     * @param accommodationId The ID of the accommodation to which the booking belongs
     * @param bookingId       The ID of the booking for which the person is associated
     * @param personId        The ID of the person to be updated
     * @param dto             The DTO containing the updated details of the person
     * @throws ResourceNotFoundException if no booking with the given ID exists for the specified accommodation,
     *                                   if no person with the given ID exists for the specified booking or
     *                                   if no address with the given ID exists
     * @throws ResourceConflictException if the booking cannot be modified in its current state
     */
    @Transactional
    public void updatePerson(UUID accommodationId, UUID bookingId, UUID personId, PersonDtoRequest dto) throws ResourceNotFoundException, ResourceConflictException {
        this.ensureBookingCanBeModified(accommodationId, bookingId);

        var person = personRepo
                .findByBookingAccommodationIdAndBookingIdAndId(accommodationId, bookingId, personId)
                .orElseThrow(NOT_FOUND_EXCEPTION);

        personMapper.fromUpdate(dto, this.getAddressById(dto.address()), person);
    }

    /**
     * Removes a person associated with a specific booking for a given accommodation.
     *
     * @param accommodationId The ID of the accommodation to which the booking belongs
     * @param bookingId       The ID of the booking for which the person is associated
     * @param personId        The ID of the person to be removed
     * @throws ResourceNotFoundException if no booking with the given ID exists for the specified accommodation or if no person with the given ID exists for the specified booking
     * @throws ResourceConflictException if the booking cannot be modified in its current state
     */
    @Transactional
    public void removePerson(UUID accommodationId, UUID bookingId, UUID personId) throws ResourceNotFoundException, ResourceConflictException {
        this.ensureBookingCanBeModified(accommodationId, bookingId);

        var person = personRepo
                .findByBookingAccommodationIdAndBookingIdAndId(accommodationId, bookingId, personId)
                .orElseThrow(NOT_FOUND_EXCEPTION);

        person.getBooking().removePerson(person);
        personRepo.delete(person);
    }

    private void ensureBookingExists(UUID accommodationId, UUID bookingId) throws ResourceNotFoundException {
        if (!bookingRepo.existsByAccommodationIdAndId(accommodationId, bookingId))
            throw BOOKING_NOT_FOUND_EXCEPTION.get();
    }

    private void ensureBookingCanBeModified(UUID accommodationId, UUID bookingId) throws ResourceNotFoundException, ResourceConflictException {
        var booking = bookingRepo
                .findByAccommodationIdAndId(accommodationId, bookingId)
                .orElseThrow(BOOKING_NOT_FOUND_EXCEPTION);

        if (!booking.canBeModified())
            throw CANNOT_BE_MODIFIED_EXCEPTION.get();
    }

    private Address getAddressById(@Nullable UUID addressId) throws ResourceNotFoundException {
        return addressId != null
                ? addressRepo.findById(addressId).orElseThrow(ADDRESS_FOUND_EXCEPTION)
                : null;
    }
}
