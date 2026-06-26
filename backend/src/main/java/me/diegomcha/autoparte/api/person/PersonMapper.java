package me.diegomcha.autoparte.api.person;

import me.diegomcha.autoparte.api.person.dto.PersonDtoRequest;
import me.diegomcha.autoparte.api.person.dto.PersonDtoResponse;
import me.diegomcha.autoparte.domain.Booking;
import me.diegomcha.autoparte.domain.Person;
import me.diegomcha.autoparte.domain.address.Address;
import me.diegomcha.autoparte.domain.base.BaseEntity;
import me.diegomcha.autoparte.domain.person.ContactInfo;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.DniDocument;
import me.diegomcha.autoparte.domain.person.document.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
abstract class PersonMapper {

    public abstract PersonDtoResponse toResponse(Person person);

    public abstract List<PersonDtoResponse> toResponse(List<Person> people);

    @Mapping(target = "address", source = "address")
    public abstract Person fromCreate(Booking booking, PersonDtoRequest dto, Address address);

    @Mapping(target = "address", source = "address")
    public abstract void fromUpdate(PersonDtoRequest dto, Address address, @MappingTarget Person person);

    @Mapping(target = "supportNumber", source = ".", qualifiedByName = "mapSupportNumber")
    protected abstract PersonDtoResponse.DocumentDtoResponse map(Document document);

    protected PersonalInfo map(PersonDtoRequest.PersonalInfoDtoRequest dto) {
        return new PersonalInfo(dto.name(), dto.firstSurname(), dto.secondSurname(), dto.nationality(), dto.birthDate(), dto.gender());
    }

    protected ContactInfo map(PersonDtoRequest.ContactInfoDtoRequest dto) {
        return new ContactInfo(dto.phoneNumber1(), dto.phoneNumber2(), dto.email());
    }

    protected Document map(PersonDtoRequest.DocumentDtoRequest dto) {
        return dto != null
                ? Document.of(dto.type(), dto.number(), dto.supportNumber())
                : null;
    }

    protected UUID map(BaseEntity entity) {
        return entity != null
                ? entity.getId()
                : null;
    }

    @Named("mapSupportNumber")
    protected String mapSupportNumber(Document document) {
        return document instanceof DniDocument dniDocument
                ? dniDocument.getSupportNumber()
                : null;
    }

}
