package me.diegomcha.autoparte.integration.ocr;

import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.DocumentInfo;
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
    protected DocumentInfo.DocumentType mapDocumentType(MrzData.DocumentType documentType) {
        return switch (documentType) {
            case PASSPORT -> DocumentInfo.DocumentType.PASSPORT;
            case DNI -> DocumentInfo.DocumentType.NIF;
            case TIE_RESIDENCE_PERMIT, TIE_BORDER_WORKER -> DocumentInfo.DocumentType.NIE;
            case null -> null;
        };
    }

    @Named("mapGender")
    protected PersonalInfo.PersonalInfoGender mapGender(MrzData.Gender gender) {
        return switch (gender) {
            case MALE -> PersonalInfo.PersonalInfoGender.MALE;
            case FEMALE -> PersonalInfo.PersonalInfoGender.FEMALE;
            case NON_BINARY, UNSPECIFIED -> PersonalInfo.PersonalInfoGender.OTHER;
            case null -> null;
        };
    }
}
