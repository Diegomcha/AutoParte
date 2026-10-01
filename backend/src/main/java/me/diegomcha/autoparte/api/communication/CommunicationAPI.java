package me.diegomcha.autoparte.api.communication;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.diegomcha.autoparte.api.account.dto.AccountDtoFull;
import me.diegomcha.autoparte.api.account.dto.SecurityEventDto;
import me.diegomcha.autoparte.api.communication.dto.CommunicationDtoResponse;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Tag(name = "Communications", description = "Operations related to booking's communications")
@SuppressWarnings("unused")
interface CommunicationAPI {

    @Operation(summary = "Get all communications")
    Page<CommunicationDtoResponse> getGlobalCommunications(Pageable pageable);

    @Operation(summary = "Get accommodation communications")
    Page<CommunicationDtoResponse> getAccommodationCommunications(UUID accommodationId, Pageable pageable) throws ResourceNotFoundException;

    @Operation(summary = "Get booking communications")
    List<CommunicationDtoResponse> getBookingCommunications(UUID accommodationId, UUID bookingId) throws ResourceNotFoundException;

}
