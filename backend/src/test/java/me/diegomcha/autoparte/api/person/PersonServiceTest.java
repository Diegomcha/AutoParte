package me.diegomcha.autoparte.api.person;

import me.diegomcha.autoparte.TestingUtils;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.api.person.dto.PersonDtoRequest;
import me.diegomcha.autoparte.core.exception.ResourceConflictException;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import me.diegomcha.autoparte.core.repos.AccommodationRepo;
import me.diegomcha.autoparte.core.repos.AddressRepo;
import me.diegomcha.autoparte.core.repos.BookingRepo;
import me.diegomcha.autoparte.core.repos.PersonRepo;
import me.diegomcha.autoparte.domain.Accommodation;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.Person;
import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.booking.payment.Payment;
import me.diegomcha.autoparte.domain.person.ContactInfo;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class PersonServiceTest {

    @Autowired
    private PersonService service;
    @Autowired
    private AccommodationRepo accommodationRepo;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private AddressRepo addressRepo;
    @Autowired
    private PersonRepo personRepo;

    private Accommodation accommodation;
    private Booking booking;
    private Address address;
    private Person person;

    @BeforeEach
    void setUp() {
        this.accommodation = accommodationRepo.save(new Accommodation("Test Accommodation", "SESCODE", null));
        this.booking = bookingRepo.save(new Booking(accommodation, TestingUtils.INSTANT, TestingUtils.FUTURE_INSTANT, 2, Payment.of(Payment.PaymentType.ON_SITE, null, null, null, null), null, null));
        this.address = addressRepo.save(Address.of("Street 1", "Appt 2", "Miami", "12345", "USA"));

        var personalInfo = new PersonalInfo("John", "Doe", null, "USA", TestingUtils.PAST_INSTANT, PersonalInfo.PersonalInfoGender.MALE);
        var contactInfo = new ContactInfo("971 49 28 05", null, "john.doe@example.com");
        var document = Document.of(Document.DocumentType.PASSPORT, "PAS123456", null);

        this.person = personRepo.save(new Person(booking, personalInfo, contactInfo, document, address, Person.PersonRelationship.OTHER));
    }

    @Test
    void testGetPeople() throws ResourceNotFoundException {
        var people = service.getPeople(accommodation.getId(), booking.getId());

        Assertions.assertEquals(1, people.size());
        Assertions.assertEquals(person.getId(), people.getFirst().id());

        // Non-existent booking
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.getPeople(accommodation.getId(), UUID.randomUUID())
        );

        // Non-existent accommodation
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.getPeople(UUID.randomUUID(), booking.getId())
        );
    }

    @Test
    void testGetPerson() throws ResourceNotFoundException {
        var response = service.getPerson(accommodation.getId(), booking.getId(), person.getId());

        Assertions.assertEquals(person.getId(), response.id());
        Assertions.assertEquals("John", response.personalInfo().name());
        Assertions.assertEquals("Doe", response.personalInfo().firstSurname());
        Assertions.assertEquals("john.doe@example.com", response.contactInfo().email());

        // Non-existent person
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.getPerson(accommodation.getId(), booking.getId(), UUID.randomUUID())
        );
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.getPerson(UUID.randomUUID(), booking.getId(), person.getId())
        );
    }

    @Test
    void testAddPerson() throws ResourceNotFoundException, ResourceConflictException {
        var personalInfoReq = new PersonDtoRequest.PersonalInfoDtoRequest(
                "Jane", "Smith", "Johnson", "USA", TestingUtils.PAST_INSTANT, PersonalInfo.PersonalInfoGender.FEMALE
        );
        var contactInfoReq = new PersonDtoRequest.ContactInfoDtoRequest(
                "971 49 28 05", null, "jane.smith@example.com"
        );
        var documentReq = new PersonDtoRequest.DocumentDtoRequest(
                Document.DocumentType.PASSPORT, "PAS987654", null
        );
        var dto = new PersonDtoRequest(
                personalInfoReq,
                contactInfoReq,
                documentReq,
                address.getId(),
                Person.PersonRelationship.SPOUSE
        );

        EntityDtoCreated created = service.addPerson(accommodation.getId(), booking.getId(), dto);

        Assertions.assertNotNull(created.id());
        Assertions.assertNotNull(created.createdAt());

        Assertions.assertTrue(personRepo.existsById(created.id()));
        var savedPerson = personRepo.findById(created.id()).orElseThrow();
        Assertions.assertEquals("Jane", savedPerson.getPersonalInfo().getName());
        Assertions.assertEquals("Smith", savedPerson.getPersonalInfo().getFirstSurname());
        Assertions.assertEquals("jane.smith@example.com", savedPerson.getContactInfo().getEmail());
        Assertions.assertEquals(address, savedPerson.getAddress());
        Assertions.assertEquals(Person.PersonRelationship.SPOUSE, savedPerson.getRelationship());

        booking.setNumberOfPeople(3);
        var withoutAddress = new PersonDtoRequest(
                personalInfoReq,
                contactInfoReq,
                documentReq,
                null,
                Person.PersonRelationship.SPOUSE
        );
        var withoutAddressCreated = service.addPerson(accommodation.getId(), booking.getId(), withoutAddress);
        Assertions.assertNull(personRepo.findById(withoutAddressCreated.id()).orElseThrow().getAddress());
    }

    @Test
    void testAddPersonBookingFull() {
        // Set number of people allowed in booking to 1, since we already have 1 person in setUp(), the booking is full
        booking.setNumberOfPeople(1);

        var personalInfoReq = new PersonDtoRequest.PersonalInfoDtoRequest(
                "Jane", "Smith", "Johnson", "USA", TestingUtils.PAST_INSTANT, PersonalInfo.PersonalInfoGender.FEMALE
        );
        var contactInfoReq = new PersonDtoRequest.ContactInfoDtoRequest(
                "971 49 28 05", null, "jane.smith@example.com"
        );
        var documentReq = new PersonDtoRequest.DocumentDtoRequest(
                Document.DocumentType.PASSPORT, "PAS987654", null
        );
        var dto = new PersonDtoRequest(
                personalInfoReq,
                contactInfoReq,
                documentReq,
                address.getId(),
                Person.PersonRelationship.SPOUSE
        );

        Assertions.assertThrows(ResourceConflictException.class, () ->
                service.addPerson(accommodation.getId(), booking.getId(), dto)
        );
    }

    @Test
    void testAddPersonUnknownAddress() {
        var personalInfoReq = new PersonDtoRequest.PersonalInfoDtoRequest(
                "Jane", "Smith", "Johnson", "USA", TestingUtils.PAST_INSTANT, PersonalInfo.PersonalInfoGender.FEMALE
        );
        var contactInfoReq = new PersonDtoRequest.ContactInfoDtoRequest(
                "971 49 28 05", null, "jane.smith@example.com"
        );
        var documentReq = new PersonDtoRequest.DocumentDtoRequest(
                Document.DocumentType.PASSPORT, "PAS987654", null
        );
        var dto = new PersonDtoRequest(
                personalInfoReq,
                contactInfoReq,
                documentReq,
                UUID.randomUUID(), // Unknown address ID
                Person.PersonRelationship.SPOUSE
        );

        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.addPerson(accommodation.getId(), booking.getId(), dto)
        );
    }

    @Test
    void testUpdatePerson() throws ResourceNotFoundException, ResourceConflictException {
        var personalInfoReq = new PersonDtoRequest.PersonalInfoDtoRequest(
                "JohnUpdated", "DoeUpdated", "SmithUpdated", "USA", TestingUtils.PAST_INSTANT, PersonalInfo.PersonalInfoGender.MALE
        );
        var contactInfoReq = new PersonDtoRequest.ContactInfoDtoRequest(
                "971 49 28 05", null, "john.updated@example.com"
        );
        var documentReq = new PersonDtoRequest.DocumentDtoRequest(
                Document.DocumentType.PASSPORT, "PASUPDATED", null
        );
        var dto = new PersonDtoRequest(
                personalInfoReq,
                contactInfoReq,
                documentReq,
                null,
                Person.PersonRelationship.OTHER
        );

        service.updatePerson(accommodation.getId(), booking.getId(), person.getId(), dto);

        var updatedPerson = personRepo.findById(person.getId()).orElseThrow();
        Assertions.assertEquals("JohnUpdated", updatedPerson.getPersonalInfo().getName());
        Assertions.assertEquals("DoeUpdated", updatedPerson.getPersonalInfo().getFirstSurname());
        Assertions.assertEquals("john.updated@example.com", updatedPerson.getContactInfo().getEmail());
        Assertions.assertEquals("PASUPDATED", updatedPerson.getDocument().getNumber());

        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.updatePerson(accommodation.getId(), booking.getId(), UUID.randomUUID(), dto));
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.updatePerson(accommodation.getId(), booking.getId(), person.getId(),
                        new PersonDtoRequest(personalInfoReq, contactInfoReq, documentReq, UUID.randomUUID(), null)));
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.updatePerson(UUID.randomUUID(), booking.getId(), person.getId(), dto));
    }

    @Test
    void testUpdatePersonWhileBookingNotModifiable() {
        // Set booking to not modifiable
        booking.confirm();
        booking.cancel();

        var personalInfoReq = new PersonDtoRequest.PersonalInfoDtoRequest(
                "JohnUpdated", "DoeUpdated", "SmithUpdated", "USA", TestingUtils.PAST_INSTANT, PersonalInfo.PersonalInfoGender.MALE
        );
        var contactInfoReq = new PersonDtoRequest.ContactInfoDtoRequest(
                "971 49 28 05", null, "john.updated@example.com"
        );
        var documentReq = new PersonDtoRequest.DocumentDtoRequest(
                Document.DocumentType.PASSPORT, "PASUPDATED", null
        );
        var dto = new PersonDtoRequest(
                personalInfoReq,
                contactInfoReq,
                documentReq,
                address.getId(),
                Person.PersonRelationship.OTHER
        );

        Assertions.assertThrows(ResourceConflictException.class, () -> service.updatePerson(accommodation.getId(), booking.getId(), person.getId(), dto));
    }

    @Test
    void testRemovePerson() throws ResourceNotFoundException, ResourceConflictException {
        service.removePerson(accommodation.getId(), booking.getId(), person.getId());

        Assertions.assertFalse(personRepo.existsById(person.getId()));
        Assertions.assertFalse(booking.getPeople().contains(person));

        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.removePerson(accommodation.getId(), booking.getId(), person.getId()));
    }

    @Test
    void testRemovePersonWhileBookingNotModifiable() {
        booking.confirm();
        booking.cancel();

        Assertions.assertThrows(ResourceConflictException.class, () ->
                service.removePerson(accommodation.getId(), booking.getId(), person.getId()));
    }

    @Test
    void testAddAndGetSignature() throws ResourceNotFoundException, ResourceConflictException {
        person.setPersonalInfo(new PersonalInfo(
                "John", "Doe", "Second", "USA",
                Instant.parse("2000-01-01T00:00:00Z"),
                PersonalInfo.PersonalInfoGender.MALE
        ));
        var timestamp = Instant.parse("2024-01-01T00:00:00Z");
        var paths = Map.of(timestamp, List.of(new Point(1, 2)));

        service.addSignature(accommodation.getId(), booking.getId(), person.getId(), paths, "127.0.0.1", "JUnit");

        var signature = service.getSignature(accommodation.getId(), booking.getId(), person.getId());
        Assertions.assertEquals(paths, signature.paths());
        Assertions.assertEquals("127.0.0.1", signature.ipAddress());
        Assertions.assertEquals("JUnit", signature.userAgent());
        Assertions.assertNotNull(signature.signedAt());
    }

    @Test
    void testSignatureFailures() {
        var paths = Map.of(Instant.parse("2024-01-01T00:00:00Z"), List.of(new Point(1, 2)));

        var accommodationId = accommodation.getId();
        var bookingId = booking.getId();
        var personId = person.getId();

        Assertions.assertThrows(IllegalStateException.class, () ->
                service.addSignature(accommodationId, bookingId, personId, paths, "127.0.0.1", "JUnit"));
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.getSignature(accommodationId, bookingId, personId));
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                service.getSignature(UUID.randomUUID(), bookingId, personId));

        person.setPersonalInfo(new PersonalInfo(
                "John", "Doe", "Second", "USA",
                Instant.parse("2015-01-01T00:00:00Z"),
                PersonalInfo.PersonalInfoGender.MALE
        ));
        person.setDocument(null);
        person.setRelationship(Person.PersonRelationship.CHILD);
        Assertions.assertThrows(IllegalStateException.class, () ->
                service.addSignature(accommodationId, bookingId, personId, paths, "127.0.0.1", "JUnit"));
    }
}
