package me.diegomcha.autoparte.api.ocr;

import me.diegomcha.autoparte.api.ocr.dto.PartialPersonDtoRequest;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.Document;
import me.diegomcha.autoparte.integration.ocr.dto.MrzDto.MrzData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.LocalDate;


@Mapper(componentModel = "spring")
abstract class OcrMapper {

    @Mapping(target = "personalInfo", source = ".")
    @Mapping(target = "document", source = ".")
    public abstract PartialPersonDtoRequest toPartialRequest(MrzData mrzData);

    @Mapping(target = "firstSurname", expression = "java(mrzData.firstSurname())")
    @Mapping(target = "secondSurname", expression = "java(mrzData.secondSurname())")
    @Mapping(target = "gender", qualifiedByName = "mapGender")
    protected abstract PartialPersonDtoRequest.PartialPersonalInfoDtoRequest mapPI(MrzData mrzData);

    @Mapping(target = "type", source = ".", qualifiedByName = "mapDocumentType")
    @Mapping(target = "number", source = "documentNumber")
    @Mapping(target = "supportNumber", expression = "java(mrzData.documentSupportNumber())")
    protected abstract PartialPersonDtoRequest.PartialDocumentDtoRequest mapDoc(MrzData mrzData);

    protected Instant map(LocalDate localDate) {
        return localDate != null ? localDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC) : null;
    }

    @Named("mapDocumentType")
    protected Document.DocumentType mapDocumentType(MrzData data) {
        return switch (data.documentType()) {
            case PASSPORT -> Document.DocumentType.PASSPORT;
            case DNI -> Document.DocumentType.NIF;
            case TIE_RESIDENCE_PERMIT, TIE_BORDER_WORKER ->
                    Document.DocumentType.NIE;
            default -> null;
        };
    }

    @Named("mapGender")
    protected PersonalInfo.PersonalInfoGender mapGender(MrzData.Gender gender) {
        return switch (gender) {
            case MALE -> PersonalInfo.PersonalInfoGender.MALE;
            case FEMALE -> PersonalInfo.PersonalInfoGender.FEMALE;
            case NON_BINARY, UNSPECIFIED ->
                    PersonalInfo.PersonalInfoGender.OTHER;
            default -> null;
        };
    }
}
