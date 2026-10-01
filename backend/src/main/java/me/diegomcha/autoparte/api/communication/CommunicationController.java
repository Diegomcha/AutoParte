package me.diegomcha.autoparte.api.communication;

import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.communication.dto.CommunicationDtoResponse;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
class CommunicationController implements CommunicationAPI {

    private final CommunicationService service;

    @GetMapping("/communications")
    @Override
    public Page<CommunicationDtoResponse> getGlobalCommunications(@ParameterObject Pageable pageable) {
        return service.getCommunications(pageable);
    }

    @GetMapping("/accommodations/{accommodationId}/communications")
    @Override
    public Page<CommunicationDtoResponse> getAccommodationCommunications(@PathVariable UUID accommodationId, @ParameterObject Pageable pageable) throws ResourceNotFoundException {
        return service.getCommunicationsByAccommodationId(accommodationId, pageable);
    }

    @GetMapping("/accommodations/{accommodationId}/bookings/{bookingId}/communications")
    @Override
    public List<CommunicationDtoResponse> getBookingCommunications(@PathVariable UUID accommodationId, @PathVariable UUID bookingId) throws ResourceNotFoundException {
        return service.getCommunicationsByBookingId(accommodationId, bookingId);
    }
}
