package me.diegomcha.autoparte.api.booking;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.booking.dto.BookingDtoRequest;
import me.diegomcha.autoparte.api.booking.dto.BookingDtoResponse;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import me.diegomcha.autoparte.domain.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional(readOnly = true)
class BookingService {

    private static final Supplier<ResourceNotFoundException> NOT_FOUND_EXCEPTION = () ->
            new ResourceNotFoundException("Booking not found");
    private static final Supplier<ResourceNotFoundException> ACCOMMODATION_NOT_FOUND_EXCEPTION = () ->
            new ResourceNotFoundException("Accommodation not found");
    private static final Supplier<ResourceConflictException> ALREADY_CANCELLED_EXCEPTION = () ->
            new ResourceConflictException("Booking is already cancelled");
    private static final Supplier<ResourceConflictException> CANNOT_BE_CONFIRMED_EXCEPTION = () ->
            new ResourceConflictException("Booking cannot be confirmed in its current state");
    private static final Supplier<ResourceConflictException> CANNOT_BE_CHECKED_IN_EXCEPTION = () ->
            new ResourceConflictException("Booking cannot be checked in in its current state");

    private final BookingRepo bookingRepo;
    private final BookingMapper bookingMapper;
    private final AccommodationRepo accommodationRepo;

    /**
     * Returns a paginated list of bookings for a specific accommodation.
     *
     * @param accommodationId The ID of the accommodation to retrieve bookings for
     * @param pageable        Pagination information (page number, size, sorting)
     * @return A page of bookings for the specified accommodation
     */
    public Page<BookingDtoResponse> getBookings(UUID accommodationId, Pageable pageable) {
        return bookingMapper.toResponse(bookingRepo.findByAccommodationId(accommodationId, pageable));
    }

    /**
     * Returns the booking with the given ID for a specific accommodation, if it exists.
     *
     * @param accommodationId The ID of the accommodation to which the booking belongs
     * @param id              The ID of the booking to retrieve
     * @return The booking with the given ID for the specified accommodation, or empty if it does not exist
     * @throws ResourceNotFoundException if no booking with the given ID exists for the specified accommodation
     */
    public BookingDtoResponse getBookingById(UUID accommodationId, UUID id) throws ResourceNotFoundException {
        return bookingRepo
                .findByAccommodationIdAndId(accommodationId, id)
                .map(bookingMapper::toResponse)
                .orElseThrow(NOT_FOUND_EXCEPTION);
    }

    /**
     * Creates a new booking for a specific accommodation using the provided booking data.
     *
     * @param accommodationId The ID of the accommodation to which the booking belongs
     * @param booking         The booking data to fromCreate the new booking
     * @throws ResourceNotFoundException if no accommodation with the given ID exists
     */
    @Transactional
    public void createBooking(UUID accommodationId, BookingDtoRequest booking) throws ResourceNotFoundException {
        var accommodation = accommodationRepo
                .findById(accommodationId)
                .orElseThrow(ACCOMMODATION_NOT_FOUND_EXCEPTION);

        var newBooking = bookingMapper.fromCreate(accommodation, booking);
        bookingRepo.save(newBooking);
    }

    /**
     * Updates the booking with the given ID for a specific accommodation using the provided update data.
     *
     * @param accommodationId The ID of the accommodation to which the booking belongs
     * @param id              The ID of the booking to update
     * @param update          The update data
     * @throws ResourceNotFoundException if no booking with the given ID exists for the specified accommodation
     */
    @Transactional
    public void updateBooking(UUID accommodationId, UUID id, BookingDtoRequest update) throws ResourceNotFoundException {
        var booking = bookingRepo
                .findByAccommodationIdAndId(accommodationId, id)
                .orElseThrow(NOT_FOUND_EXCEPTION);

        bookingMapper.fromUpdate(update, booking);
    }

    /**
     * Marks the booking with the given ID for a specific accommodation as confirmed.
     *
     * @param accommodationId The ID of the accommodation to which the booking belongs
     * @param id              The ID of the booking to confirm
     * @throws ResourceNotFoundException if no booking with the given ID exists for the specified accommodation
     * @throws ResourceConflictException if the booking cannot be confirmed in its current state
     */
    @Transactional
    public void confirmBooking(UUID accommodationId, UUID id) throws ResourceNotFoundException, ResourceConflictException {
        var booking = bookingRepo
                .findByAccommodationIdAndId(accommodationId, id)
                .orElseThrow(NOT_FOUND_EXCEPTION);

        // Check if the booking can be confirmed
        if (!booking.canBeConfirmed())
            throw CANNOT_BE_CONFIRMED_EXCEPTION.get();

        booking.confirm();
    }

    /**
     * Marks the booking with the given ID for a specific accommodation as checked in.
     *
     * @param accommodationId The ID of the accommodation to which the booking belongs
     * @param id              The ID of the booking to check in
     * @throws ResourceNotFoundException if no booking with the given ID exists for the specified accommodation
     * @throws ResourceConflictException if the booking cannot be checked in in its current state
     */
    @Transactional
    public void checkInBooking(UUID accommodationId, UUID id) throws ResourceNotFoundException, ResourceConflictException {
        var booking = bookingRepo
                .findByAccommodationIdAndId(accommodationId, id)
                .orElseThrow(NOT_FOUND_EXCEPTION);

        // Check if the booking can be checked in
        if (!booking.canBeCheckedIn())
            throw CANNOT_BE_CHECKED_IN_EXCEPTION.get();

        booking.checkIn();
    }

    /**
     * Cancels the booking with the given ID for a specific accommodation.
     *
     * @param accommodationId The ID of the accommodation to which the booking belongs
     * @param id              The ID of the booking to cancel
     * @throws ResourceNotFoundException if no booking with the given ID exists for the specified accommodation
     * @throws ResourceConflictException if the booking is already canceled
     */
    @Transactional
    public void cancelBooking(UUID accommodationId, UUID id) throws ResourceNotFoundException, ResourceConflictException {
        var booking = bookingRepo
                .findByAccommodationIdAndId(accommodationId, id)
                .orElseThrow(NOT_FOUND_EXCEPTION);

        // Check if the booking is already canceled
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED)
            throw ALREADY_CANCELLED_EXCEPTION.get();

        booking.cancel();
    }

}
