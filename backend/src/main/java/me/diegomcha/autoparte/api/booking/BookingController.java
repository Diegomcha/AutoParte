package me.diegomcha.autoparte.api.booking;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.booking.dto.BookingDtoRequest;
import me.diegomcha.autoparte.api.booking.dto.BookingDtoResponse;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/accommodations/{accommodationId}/bookings")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class BookingController implements BookingAPI {

    private final BookingService bookingService;

    @GetMapping
    @Override
    public Page<BookingDtoResponse> getBookingsByAccommodation(@PathVariable UUID accommodationId, @ParameterObject Pageable pageable) throws ResourceNotFoundException {
        return bookingService.getBookings(accommodationId, pageable);
    }

    @GetMapping("/{id}")
    @Override
    public BookingDtoResponse getBookingById(@PathVariable UUID accommodationId, @PathVariable UUID id) throws ResourceNotFoundException {
        return bookingService.getBooking(accommodationId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public EntityDtoCreated createBooking(@PathVariable UUID accommodationId, BookingDtoRequest booking) throws ResourceNotFoundException {
        return bookingService.createBooking(accommodationId, booking);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void updateBooking(@PathVariable UUID accommodationId, @PathVariable UUID id, BookingDtoRequest booking) throws ResourceNotFoundException {
        bookingService.updateBooking(accommodationId, id, booking);
    }

    @PostMapping("/{id}/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void confirmBooking(@PathVariable UUID accommodationId, @PathVariable UUID id) throws ResourceConflictException, ResourceNotFoundException {
        bookingService.confirmBooking(accommodationId, id);
    }

    @PostMapping("/{id}/check-in")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void checkInBooking(@PathVariable UUID accommodationId, @PathVariable UUID id) throws ResourceConflictException, ResourceNotFoundException {
        bookingService.checkInBooking(accommodationId, id);
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void cancelBooking(@PathVariable UUID accommodationId, @PathVariable UUID id) throws ResourceConflictException, ResourceNotFoundException {
        bookingService.cancelBooking(accommodationId, id);
    }

}
