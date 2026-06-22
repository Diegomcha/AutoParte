package me.diegomcha.autoparte.api.accommodation;

import me.diegomcha.autoparte.api.accommodation.dto.AccommodationDtoRequest;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.EmployeeRepo;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Employee;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.StreamSupport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase
class AccommodationServiceTest {

    @Autowired
    private AccommodationRepo repo;
    @Autowired
    private AccommodationService service;

    private Accommodation accommodation;
    @Autowired
    private EmployeeRepo employeeRepo;

    @BeforeEach
    void setUp() {
        this.accommodation = repo.save(new Accommodation("Name", "00000", null));
    }

    @Test
    void testGetAccommodations() {
        // One accommodation in the database
        var accommodationsPage = service.getAccommodations(Pageable.unpaged());

        Assertions.assertEquals(1, accommodationsPage.getTotalElements());
        Assertions.assertEquals(accommodation.getId(), accommodationsPage.getContent().getFirst().id());

        // No accommodations in the database
        repo.delete(accommodation);
        accommodationsPage = service.getAccommodations(Pageable.unpaged());

        Assertions.assertEquals(0, accommodationsPage.getTotalElements());
    }

    @Test
    void testGetAccommodation() throws ResourceNotFoundException {
        Assertions.assertNotNull(accommodation.getId());
        var accommodationResponse = service.getAccommodation(accommodation.getId());

        Assertions.assertEquals(accommodation.getId(), accommodationResponse.id());
        Assertions.assertNotNull(accommodationResponse.employees());

        // Non-existing accommodation
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.getAccommodation(UUID.randomUUID())
        );
    }

    @Test
    void testCreateAccommodation() throws ResourceConflictException {
        service.createAccommodation(new AccommodationDtoRequest("Name1", "00001", null));

        Assertions.assertTrue(repo.existsByName("Name1"));
    }

    @Test
    void testCreateAccommodationFailed() {
        // Same name or sesCode as existing accommodation
        Assertions.assertThrows(ResourceConflictException.class, () ->
                service.createAccommodation(new AccommodationDtoRequest("Name", "00001", null))
        );
        Assertions.assertThrows(ResourceConflictException.class, () ->
                service.createAccommodation(new AccommodationDtoRequest("Name1", "00000", null))
        );

        Assertions.assertEquals(1, StreamSupport.stream(repo.findAll().spliterator(), false).count());
    }

    private static final AccommodationDtoRequest[] UPDATES = new AccommodationDtoRequest[]{
            new AccommodationDtoRequest("Name", "00000", null),
            new AccommodationDtoRequest("Name1", "00000", null),
            new AccommodationDtoRequest("Name1", "00001", null),
            new AccommodationDtoRequest("Name1", "00001", true)
    };

    @ParameterizedTest
    @FieldSource("UPDATES")
    void testUpdateAccommodation(AccommodationDtoRequest update) throws ResourceNotFoundException, ResourceConflictException {
        Assertions.assertNotNull(accommodation.getId());

        service.updateAccommodation(accommodation.getId(), update);

        var accommodationDb = repo.findById(accommodation.getId());

        Assertions.assertTrue(accommodationDb.isPresent());
        Assertions.assertEquals(update.name(), accommodationDb.get().getName());
        Assertions.assertEquals(update.sesCode(), accommodationDb.get().getSesCode());
        Assertions.assertEquals(update.internetConnection(), accommodationDb.get().getInternetConnection());
    }

    @Test
    void testUpdateAccommodationFailed() {
        // Non-existing accommodation
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.updateAccommodation(UUID.randomUUID(), new AccommodationDtoRequest("Name", "00000", null))
        );

        repo.save(new Accommodation("Name1", "00001", null));

        // Same name as another accommodation
        Assertions.assertNotNull(accommodation.getId());
        Assertions.assertThrows(ResourceConflictException.class, () ->
                service.updateAccommodation(accommodation.getId(), new AccommodationDtoRequest("Name1", "00000", null))
        );

        // Same sesCode as another accommodation
        Assertions.assertThrows(ResourceConflictException.class, () ->
                service.updateAccommodation(accommodation.getId(), new AccommodationDtoRequest("Name", "00001", null))
        );

        Assertions.assertNotNull(accommodation.getId());
        var accommodationDb = repo.findById(accommodation.getId());

        Assertions.assertTrue(accommodationDb.isPresent());
        Assertions.assertEquals("Name", accommodationDb.get().getName());
        Assertions.assertEquals("00000", accommodationDb.get().getSesCode());
    }

    @Test
    void testDeleteAccommodation() throws ResourceNotFoundException {
        Assertions.assertNotNull(accommodation.getId());

        service.deleteAccommodation(accommodation.getId());

        Assertions.assertFalse(repo.existsById(accommodation.getId()));
    }

    @Test
    void testDeleteAccommodationFailed() {
        Assertions.assertNotNull(accommodation.getId());

        // Non-existing accommodation
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.deleteAccommodation(UUID.randomUUID())
        );

        Assertions.assertTrue(repo.existsById(accommodation.getId()));
    }

    @Test
    void testAssignationEmployeeToAccommodation() throws ResourceNotFoundException, ResourceConflictException {
        var employee = employeeRepo.save(new Employee("Name", "Surname", "email@email.com", "hashedpassword"));

        Assertions.assertNotNull(accommodation.getId());
        Assertions.assertNotNull(employee.getId());

        service.assignEmployeeToAccommodation(accommodation.getId(), employee.getId());

        Assertions.assertTrue(accommodation.getEmployees().contains(employee));

        service.unassignEmployeeFromAccommodation(accommodation.getId(), employee.getId());

        Assertions.assertFalse(accommodation.getEmployees().contains(employee));
    }

    @Test
    void testFailedAssignationEmployeeToAccommodation() throws ResourceConflictException, ResourceNotFoundException {
        var employee = employeeRepo.save(new Employee("Name", "Surname", "email@email.com", "hashedpassword"));

        Assertions.assertNotNull(accommodation.getId());
        Assertions.assertNotNull(employee.getId());

        // Non-existing entities
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.assignEmployeeToAccommodation(UUID.randomUUID(), employee.getId())
        );
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.assignEmployeeToAccommodation(accommodation.getId(), UUID.randomUUID())
        );

        // Not assigned
        Assertions.assertThrows(ResourceConflictException.class, () ->
                service.unassignEmployeeFromAccommodation(accommodation.getId(), employee.getId())
        );

        service.assignEmployeeToAccommodation(accommodation.getId(), employee.getId());

        // Already assigned
        Assertions.assertThrows(ResourceConflictException.class, () ->
                service.assignEmployeeToAccommodation(accommodation.getId(), employee.getId())
        );
    }
}
