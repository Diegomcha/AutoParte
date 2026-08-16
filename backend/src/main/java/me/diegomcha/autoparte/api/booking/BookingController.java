package me.diegomcha.autoparte.api.booking;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.booking.dto.BookingDtoRequest;
import me.diegomcha.autoparte.api.booking.dto.BookingDtoResponse;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.core.exception.BadRequestException;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/accommodations/{accommodationId}/bookings")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class BookingController implements BookingAPI {

    private final BookingService bookingService;

    @GetMapping
    @Override
    public Page<BookingDtoResponse> getBookingsByAccommodation(@PathVariable UUID accommodationId,
                                                               @ParameterObject Pageable pageable,
                                                               @RequestParam(required = false) Instant startRange,
                                                               @RequestParam(required = false) Instant endRange) throws ResourceNotFoundException, BadRequestException {
        return bookingService.getBookings(accommodationId, pageable, startRange, endRange);
    }

    @GetMapping("/{id}")
    @Override
    public BookingDtoResponse getBookingById(@PathVariable UUID accommodationId, @PathVariable UUID id) throws ResourceNotFoundException {
        return bookingService.getBooking(accommodationId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public EntityDtoCreated createBooking(@PathVariable UUID accommodationId, @Valid @RequestBody BookingDtoRequest booking) throws ResourceNotFoundException {
        return bookingService.createBooking(accommodationId, booking);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void updateBooking(@PathVariable UUID accommodationId, @PathVariable UUID id, @Valid @RequestBody BookingDtoRequest booking) throws ResourceNotFoundException, ResourceConflictException {
        bookingService.updateBooking(accommodationId, id, booking);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void deleteBooking(@PathVariable UUID accommodationId, @PathVariable UUID id) throws ResourceNotFoundException, ResourceConflictException {
        bookingService.deleteBooking(accommodationId, id);
    }

    @PostMapping("/{id}/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void confirmBooking(@PathVariable UUID accommodationId, @PathVariable UUID id) throws ResourceConflictException, ResourceNotFoundException {
        bookingService.confirmBooking(accommodationId, id);
    }

    @PostMapping("/{id}/request-self-check-in")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void requestSelfCheckInForBooking(@PathVariable UUID accommodationId, @PathVariable UUID id) throws ResourceNotFoundException, ResourceConflictException {
        bookingService.requestSelfCheckInForBooking(accommodationId, id);
    }

    @PostMapping("/{id}/check-in")
    @Override
    public ResponseEntity<Void> checkInBooking(@PathVariable UUID accommodationId, @PathVariable UUID id) throws ResourceConflictException, ResourceNotFoundException {
        return bookingService.checkInBooking(accommodationId, id) ?
                ResponseEntity.noContent().build() :
                ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void cancelBooking(@PathVariable UUID accommodationId, @PathVariable UUID id) throws ResourceConflictException, ResourceNotFoundException {
        bookingService.cancelBooking(accommodationId, id);
    }

}
