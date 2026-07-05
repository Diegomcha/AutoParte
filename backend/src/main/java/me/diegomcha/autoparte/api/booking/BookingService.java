package me.diegomcha.autoparte.api.booking;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.booking.dto.BookingDtoRequest;
import me.diegomcha.autoparte.api.booking.dto.BookingDtoResponse;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.api.common.EntityMapper;
import me.diegomcha.autoparte.config.DynamicConfigService;
import me.diegomcha.autoparte.core.exception.BadRequestException;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import me.diegomcha.autoparte.core.security.SecurityService;
import me.diegomcha.autoparte.domain.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
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
    private final EntityMapper entityMapper;
    private final DynamicConfigService dynamicConfigService;
    private final SecurityService securityService;

    /**
     * Returns a paginated list of bookings for a specific accommodation.
     *
     * @param accommodationId The ID of the accommodation to retrieve bookings for
     * @param pageable        Pagination information (page number, size, sorting)
     * @param startRange       Optional start date filter for bookings
     * @param endRange         Optional end date filter for bookings
     * @return A page of bookings for the specified accommodation
     * @throws ResourceNotFoundException if no accommodation with the given ID exists
     */
    public Page<BookingDtoResponse> getBookings(UUID accommodationId, Pageable pageable, Instant startRange, Instant endRange) throws ResourceNotFoundException, BadRequestException {
        this.ensureAccommodationExists(accommodationId);
        if (startRange != null && endRange != null && startRange.isAfter(endRange))
            throw new BadRequestException("Start date cannot be after end date");

        return bookingMapper.toResponse(bookingRepo.findAll(BookingSpecs.ofAccommodationWithDatesBetween(accommodationId, startRange, endRange), pageable));
    }

    /**
     * Returns the booking with the given ID for a specific accommodation, if it exists.
     *
     * @param accommodationId The ID of the accommodation to which the booking belongs
     * @param id              The ID of the booking to retrieve
     * @return The booking with the given ID for the specified accommodation, or empty if it does not exist
     * @throws ResourceNotFoundException if no booking with the given ID exists for the specified accommodation or if no accommodation with the given ID exists
     */
    public BookingDtoResponse getBooking(UUID accommodationId, UUID id) throws ResourceNotFoundException {
        this.ensureAccommodationExists(accommodationId);
        return bookingRepo
                .findByAccommodationIdAndId(accommodationId, id)
                .map(bookingMapper::toResponse)
                .orElseThrow(NOT_FOUND_EXCEPTION);
    }

    /**
     * Creates a new booking for a specific accommodation using the provided booking data.
     *
     * @param accommodationId The ID of the accommodation to which the booking belongs
     * @param booking         The booking data to create the new booking
     * @return The created booking's ID and creation timestamp
     * @throws ResourceNotFoundException if no accommodation with the given ID exists
     */
    @Transactional
    public EntityDtoCreated createBooking(UUID accommodationId, BookingDtoRequest booking) throws ResourceNotFoundException {
        var accommodation = accommodationRepo
                .findById(accommodationId)
                .orElseThrow(ACCOMMODATION_NOT_FOUND_EXCEPTION);

        var newBooking = bookingMapper.fromCreate(accommodation, booking);
        newBooking = bookingRepo.save(newBooking);
        return entityMapper.toCreated(newBooking);
    }

    /**
     * Updates the booking with the given ID for a specific accommodation using the provided update data.
     *
     * @param accommodationId The ID of the accommodation to which the booking belongs
     * @param id              The ID of the booking to update
     * @param update          The update data
     * @throws ResourceNotFoundException if no booking with the given ID exists for the specified accommodation or if no accommodation with the given ID exists
     */
    @Transactional
    public void updateBooking(UUID accommodationId, UUID id, BookingDtoRequest update) throws ResourceNotFoundException {
        this.ensureAccommodationExists(accommodationId);

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
     * @throws ResourceNotFoundException if no booking with the given ID exists for the specified accommodation or if no accommodation with the given ID exists
     * @throws ResourceConflictException if the booking cannot be confirmed in its current state
     */
    @Transactional
    public void confirmBooking(UUID accommodationId, UUID id) throws ResourceNotFoundException, ResourceConflictException {
        this.ensureAccommodationExists(accommodationId);

        var booking = bookingRepo
                .findByAccommodationIdAndId(accommodationId, id)
                .orElseThrow(NOT_FOUND_EXCEPTION);

        // Check if the booking can be confirmed
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMATION_READY)
            throw CANNOT_BE_CONFIRMED_EXCEPTION.get();

        booking.confirm();
    }

    /**
     * Marks the booking with the given ID for a specific accommodation as published, allowing it to be used for self-check-in.
     *
     * @param accommodationId The ID of the accommodation to which the booking belongs
     * @param id              The ID of the booking to publish
     * @throws ResourceNotFoundException if no booking with the given ID exists for the specified accommodation or if no accommodation with the given ID exists
     */
    @Transactional
    public void publishBooking(UUID accommodationId, UUID id) throws ResourceNotFoundException {
        this.ensureAccommodationExists(accommodationId);

        var booking = bookingRepo
                .findByAccommodationIdAndId(accommodationId, id)
                .orElseThrow(NOT_FOUND_EXCEPTION);

        booking.setPublished(true);
    }

    /**
     * Marks the booking with the given ID for a specific accommodation as checked in.
     *
     * @param accommodationId The ID of the accommodation to which the booking belongs
     * @param id              The ID of the booking to check in
     * @return true if the booking was checked in successfully, false if the booking needs to be reviewed manually
     * @throws ResourceNotFoundException if no booking with the given ID exists for the specified accommodation or if no accommodation with the given ID exists
     * @throws ResourceConflictException if the booking cannot be checked in in its current state
     */
    @Transactional
    public boolean checkInBooking(UUID accommodationId, UUID id) throws ResourceNotFoundException, ResourceConflictException {
        this.ensureAccommodationExists(accommodationId);

        var booking = bookingRepo
                .findByAccommodationIdAndId(accommodationId, id)
                .orElseThrow(NOT_FOUND_EXCEPTION);

        if (booking.getStatus() != Booking.BookingStatus.CHECK_IN_READY)
            throw CANNOT_BE_CHECKED_IN_EXCEPTION.get();

        // If the user is not logged-in and manual review is enabled do not check-in directly
        var isLoggedIn = Optional
                .ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(securityService::getAccountFromAuthentication)
                .isPresent();

        if (!isLoggedIn && dynamicConfigService.getConfig().isManualReviewEnabled()) {
            booking.setPublished(false);
            return false;
        }

        booking.checkIn();
        return true;
    }

    /**
     * Cancels the booking with the given ID for a specific accommodation.
     *
     * @param accommodationId The ID of the accommodation to which the booking belongs
     * @param id              The ID of the booking to cancel
     * @throws ResourceNotFoundException if no booking with the given ID exists for the specified accommodation or if no accommodation with the given ID exists
     * @throws ResourceConflictException if the booking is already canceled
     */
    @Transactional
    public void cancelBooking(UUID accommodationId, UUID id) throws ResourceNotFoundException, ResourceConflictException {
        this.ensureAccommodationExists(accommodationId);

        var booking = bookingRepo
                .findByAccommodationIdAndId(accommodationId, id)
                .orElseThrow(NOT_FOUND_EXCEPTION);

        // Check if the booking is already canceled
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED)
            throw ALREADY_CANCELLED_EXCEPTION.get();

        booking.cancel();
    }

    private void ensureAccommodationExists(UUID accommodationId) throws ResourceNotFoundException {
        if (!accommodationRepo.existsById(accommodationId))
            throw ACCOMMODATION_NOT_FOUND_EXCEPTION.get();
    }

}
