package me.diegomcha.autoparte.integration.ocr;

import me.diegomcha.autoparte.integration.ocr.dto.DocDto;
import me.diegomcha.autoparte.integration.ocr.dto.MrzDto.MrzData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


@Mapper(componentModel = "spring")
public abstract class OcrMapper {
    @Mapping(target = "documentType", qualifiedByName = "mapDocumentType")
    @Mapping(target = "gender", qualifiedByName = "mapGender")
    @Mapping(target = "firstSurname", expression = "java(mrzData.firstSurname())")
    @Mapping(target = "secondSurname", expression = "java(mrzData.secondSurname())")
    @Mapping(target = "documentSupportNumber", expression = "java(mrzData.documentSupportNumber())")
    public abstract DocDto toDocument(MrzData mrzData);

    @Named("mapDocumentType")
    protected DocDto.DocumentType mapDocumentType(MrzData.DocumentType documentType) {
        return switch (documentType) {
            case PASSPORT -> DocDto.DocumentType.PASSPORT;
            case DNI -> DocDto.DocumentType.NIF;
            case TIE_RESIDENCE_PERMIT -> DocDto.DocumentType.NIE;
            case TIE_BORDER_WORKER -> DocDto.DocumentType.NIE;
            case null -> null;
        };
    }

    @Named("mapGender")
    protected DocDto.Gender mapGender(MrzData.Gender gender) {
        return switch (gender) {
            case MALE -> DocDto.Gender.MALE;
            case FEMALE -> DocDto.Gender.FEMALE;
            case NON_BINARY -> DocDto.Gender.OTHER;
            case UNSPECIFIED -> DocDto.Gender.OTHER;
            case null -> null;
        };
    }
}
