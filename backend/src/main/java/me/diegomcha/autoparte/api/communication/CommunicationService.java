package me.diegomcha.autoparte.api.communication;

import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.communication.dto.CommunicationDtoResponse;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import me.diegomcha.autoparte.core.repos.CommunicationRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Transactional(readOnly = true)
class CommunicationService {

    private static final Supplier<ResourceNotFoundException> ACCOMMODATION_NOT_FOUND_EXCEPTION = () ->
            new ResourceNotFoundException("Accommodation not found for the given accommodation ID.");
    private static final Supplier<ResourceNotFoundException> BOOKING_NOT_FOUND_EXCEPTION = () ->
            new ResourceNotFoundException("Booking not found for the given accommodation ID and booking ID.");

    private final CommunicationRepo communicationRepo;
    private final CommunicationMapper communicationMapper;
    private final BookingRepo bookingRepo;
    private final AccommodationRepo accommodationRepo;

    /**
     * Retrieves a paginated list of communications.
     *
     * @param pageable Pagination information (page number, page size, sorting)
     * @return A page of CommunicationDtoResponse objects
     */
    public Page<CommunicationDtoResponse> getCommunications(Pageable pageable) {
        return communicationRepo.findAll(communicationMapper.translatePageable(pageable)).map(communicationMapper::toDto);
    }

    /**
     * Retrieves a paginated list of communications associated with a specific accommodation.
     *
     * @param accommodationId The UUID of the accommodation for which to retrieve communications
     * @param pageable        Pagination information (page number, page size, sorting)
     * @return A page of CommunicationDtoResponse objects associated with the specified accommodation ID
     * @throws ResourceNotFoundException If the accommodation with the given ID does not exist
     */
    public Page<CommunicationDtoResponse> getCommunicationsByAccommodationId(UUID accommodationId, Pageable pageable) throws ResourceNotFoundException {
        if (!accommodationRepo.existsById(accommodationId))
            throw ACCOMMODATION_NOT_FOUND_EXCEPTION.get();

        return communicationRepo.findByBookingAccommodationIdOrderById(accommodationId, communicationMapper.translatePageable(pageable)).map(communicationMapper::toDto);
    }

    /**
     * Retrieves a list of communications associated with a specific booking.
     *
     * @param accommodationId The UUID of the booking's accommodation for which to retrieve communications
     * @param bookingId       The UUID of the booking for which to retrieve communications
     * @return A list of CommunicationDtoResponse objects associated with the specified booking ID
     * @throws ResourceNotFoundException If the booking with the given accommodation ID and booking ID does not exist
     */
    public List<CommunicationDtoResponse> getCommunicationsByBookingId(UUID accommodationId, UUID bookingId) throws ResourceNotFoundException {
        if (!bookingRepo.existsByAccommodationIdAndId(accommodationId, bookingId))
            throw BOOKING_NOT_FOUND_EXCEPTION.get();

        return communicationRepo.findByBookingIdOrderById(bookingId).stream().map(communicationMapper::toDto).toList();
    }

}
