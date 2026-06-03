package me.diegomcha.autoparte.integration.ocr;

import me.diegomcha.autoparte.domain.Person;
import me.diegomcha.autoparte.domain.person.document.Document;
import me.diegomcha.autoparte.integration.ocr.dto.DocDto;
import me.diegomcha.autoparte.integration.ocr.dto.MrzDto.MrzData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


@Mapper(componentModel = "spring")
abstract class OcrMapper {
    @Mapping(target = "documentType", qualifiedByName = "mapDocumentType")
    @Mapping(target = "gender", qualifiedByName = "mapGender")
    @Mapping(target = "firstSurname", expression = "java(mrzData.firstSurname())")
    @Mapping(target = "secondSurname", expression = "java(mrzData.secondSurname())")
    @Mapping(target = "documentSupportNumber", expression = "java(mrzData.documentSupportNumber())")
    abstract DocDto toDocument(MrzData mrzData);

    @Named("mapDocumentType")
    protected Document.DocumentType mapDocumentType(MrzData.DocumentType documentType) {
        return switch (documentType) {
            case PASSPORT -> Document.DocumentType.PASSPORT;
            case DNI -> Document.DocumentType.NIF;
            case TIE_RESIDENCE_PERMIT, TIE_BORDER_WORKER -> Document.DocumentType.NIE;
            case null -> null;
        };
    }

    @Named("mapGender")
    protected Person.PersonGender mapGender(MrzData.Gender gender) {
        return switch (gender) {
            case MALE -> Person.PersonGender.MALE;
            case FEMALE -> Person.PersonGender.FEMALE;
            case NON_BINARY, UNSPECIFIED -> Person.PersonGender.OTHER;
            case null -> null;
        };
    }
}
