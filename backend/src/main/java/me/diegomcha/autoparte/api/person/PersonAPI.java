package me.diegomcha.autoparte.api.person;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.api.person.dto.PersonDtoRequest;
import me.diegomcha.autoparte.api.person.dto.PersonDtoResponse;
import me.diegomcha.autoparte.api.person.dto.SignatureDtoResponse;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "People", description = "Operations related to people associated with bookings")
@SuppressWarnings("unused")
public interface PersonAPI {

    @Operation(summary = "List people")
    List<PersonDtoResponse> getPeople(UUID accommodationId, UUID bookingId) throws ResourceNotFoundException;

    @Operation(summary = "Get person by id")
    PersonDtoResponse getPerson(UUID accommodationId, UUID bookingId, UUID id) throws ResourceNotFoundException;

    @Operation(summary = "Add person to booking")
    EntityDtoCreated addPerson(UUID accommodationId, UUID bookingId, @Valid PersonDtoRequest person) throws ResourceNotFoundException, ResourceConflictException;

    @Operation(summary = "Update person in booking")
    void updatePerson(UUID accommodationId, UUID bookingId, UUID id, @Valid PersonDtoRequest person) throws ResourceNotFoundException, ResourceConflictException;

    @Operation(summary = "Remove person from booking")
    void removePerson(UUID accommodationId, UUID bookingId, UUID id) throws ResourceNotFoundException, ResourceConflictException;

    @Operation(summary = "Sign booking")
    void addSignature(UUID accommodationId, UUID bookingId, UUID id, @Valid @Nonnull Map<@NotNull Instant, @NotNull List<@NotNull Point>> signaturePaths, HttpServletRequest request) throws ResourceNotFoundException, ResourceConflictException;

    @Operation(summary = "Get signature for person in booking")
    SignatureDtoResponse getSignature(UUID accommodationId, UUID bookingId, UUID id) throws ResourceNotFoundException;
}
