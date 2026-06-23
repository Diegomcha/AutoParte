package me.diegomcha.autoparte.api.booking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.diegomcha.autoparte.api.booking.dto.BookingDtoRequest;
import me.diegomcha.autoparte.api.booking.dto.BookingDtoResponse;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Tag(name = "Bookings", description = "Operations related to bookings")
@SuppressWarnings("unused")
public interface BookingAPI {

    @Operation(summary = "List bookings for an accommodation")
    Page<BookingDtoResponse> getBookingsByAccommodation(UUID accommodationId, Pageable pageable) throws ResourceNotFoundException;

    @Operation(summary = "Get booking by id for an accommodation")
    BookingDtoResponse getBookingById(UUID accommodationId, UUID id) throws ResourceNotFoundException;

    @Operation(summary = "Create a new booking for an accommodation")
    void createBooking(UUID accommodationId, BookingDtoRequest booking) throws ResourceNotFoundException;

    @Operation(summary = "Update a booking for an accommodation")
    void updateBooking(UUID accommodationId, UUID id, BookingDtoRequest booking) throws ResourceNotFoundException;

    @Operation(summary = "Confirm a booking for an accommodation")
    void confirmBooking(UUID accommodationId, UUID id) throws ResourceConflictException, ResourceNotFoundException;

    @Operation(summary = "Check-in a booking for an accommodation")
    void checkInBooking(UUID accommodationId, UUID id) throws ResourceConflictException, ResourceNotFoundException;

    @Operation(summary = "Cancel a booking for an accommodation")
    void cancelBooking(UUID accommodationId, UUID id) throws ResourceConflictException, ResourceNotFoundException;

}
