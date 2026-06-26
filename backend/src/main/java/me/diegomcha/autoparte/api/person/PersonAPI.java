package me.diegomcha.autoparte.api.person;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.api.person.dto.PersonDtoRequest;
import me.diegomcha.autoparte.api.person.dto.PersonDtoResponse;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Tag(name = "People", description = "Operations related to people associated with bookings")
@SuppressWarnings("unused")
public interface PersonAPI {

    @Operation(summary = "List people")
    List<PersonDtoResponse> getPeople(UUID accommodationId, UUID bookingId) throws ResourceNotFoundException;

    @Operation(summary = "Get person by id")
    PersonDtoResponse getPerson(UUID accommodationId, UUID bookingId, UUID id) throws ResourceNotFoundException;

    @Operation(summary = "Add person to booking")
    EntityDtoCreated addPerson(UUID accommodationId, UUID bookingId, PersonDtoRequest person) throws ResourceNotFoundException, ResourceConflictException;

    @Operation(summary = "Update person in booking")
    void updatePerson(UUID accommodationId, UUID bookingId, UUID id, PersonDtoRequest person) throws ResourceNotFoundException, ResourceConflictException;

    @Operation(summary = "Remove person from booking")
    void removePerson(UUID accommodationId, UUID bookingId, UUID id) throws ResourceNotFoundException, ResourceConflictException;

}
