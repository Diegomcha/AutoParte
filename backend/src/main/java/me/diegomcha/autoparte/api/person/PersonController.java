package me.diegomcha.autoparte.api.person;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.api.person.dto.PersonDtoRequest;
import me.diegomcha.autoparte.api.person.dto.PersonDtoResponse;
import me.diegomcha.autoparte.api.person.dto.SignatureDtoResponse;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/accommodations/{accommodationId}/bookings/{bookingId}/people")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class PersonController implements PersonAPI {

    private final PersonService personService;

    @GetMapping
    @Override
    public List<PersonDtoResponse> getPeople(@PathVariable UUID accommodationId, @PathVariable UUID bookingId) throws ResourceNotFoundException {
        return personService.getPeople(accommodationId, bookingId);
    }

    @GetMapping("/{id}")
    @Override
    public PersonDtoResponse getPerson(@PathVariable UUID accommodationId, @PathVariable UUID bookingId, @PathVariable UUID id) throws ResourceNotFoundException {
        return personService.getPerson(accommodationId, bookingId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public EntityDtoCreated addPerson(@PathVariable UUID accommodationId, @PathVariable UUID bookingId,  @RequestBody PersonDtoRequest person) throws ResourceNotFoundException, ResourceConflictException {
        return personService.addPerson(accommodationId, bookingId, person);
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void updatePerson(@PathVariable UUID accommodationId, @PathVariable UUID bookingId, @PathVariable UUID id, @RequestBody PersonDtoRequest person) throws ResourceNotFoundException, ResourceConflictException {
        personService.updatePerson(accommodationId, bookingId, id, person);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void removePerson(@PathVariable UUID accommodationId, @PathVariable UUID bookingId, @PathVariable UUID id) throws ResourceNotFoundException, ResourceConflictException {
        personService.removePerson(accommodationId, bookingId, id);
    }

    @PostMapping("{id}/signature")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void addSignature(@PathVariable UUID accommodationId, @PathVariable UUID bookingId, @PathVariable UUID id, @RequestBody Map<Instant, List<Point>> signaturePath, HttpServletRequest request) throws  ResourceNotFoundException, ResourceConflictException {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        personService.addSignature(accommodationId, bookingId, id, signaturePath, ipAddress, userAgent);
    }

    @GetMapping("{id}/signature")
    @Override
    public SignatureDtoResponse getSignature(@PathVariable UUID accommodationId, @PathVariable UUID bookingId, @PathVariable UUID id) throws ResourceNotFoundException {
        return personService.getSignature(accommodationId, bookingId, id);
    }
}
