package me.diegomcha.autoparte.api.catalogue.services;

import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.domain.Person;
import me.diegomcha.autoparte.domain.person.document.Document;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class CatalogueService {

    private final MessageSource messageSource;

    /**
     * Gets a map of person gender options, where the key is the enum name and the value is the localized name.
     *
     * @param locale Locale to use for localization
     * @return Map of person gender options
     */
    public Map<String, String> getPersonGenderOptions(Locale locale) {
        return Arrays
                .stream(Person.PersonGender.values())
                .collect(Collectors.toMap(
                        Person.PersonGender::name,
                        v -> this.getLocalizedEnumValue(v, locale)));
    }

    /**
     * Gets a map of person relationship options, where the key is the enum name and the value is the localized name.
     *
     * @param locale Locale to use for localization
     * @return Map of person relationship options
     */
    public Map<String, String> getPersonRelationshipOptions(Locale locale) {
        return Arrays
                .stream(Person.PersonRelationship.values())
                .collect(Collectors.toMap(
                        Person.PersonRelationship::name,
                        v -> this.getLocalizedEnumValue(v, locale)));
    }

    /**
     * Gets a map of document type options, where the key is the enum name and the value is the localized name.
     *
     * @param locale Locale to use for localization
     * @return Map of document type options
     */
    public Map<String, String> getDocumentTypeOptions(Locale locale) {
        return Arrays
                .stream(Document.DocumentType.values())
                .collect(Collectors.toMap(
                        Document.DocumentType::name,
                        v -> this.getLocalizedEnumValue(v, locale)));
    }

    private String getLocalizedEnumValue(Enum<?> enumValue, Locale locale) {
        return messageSource.getMessage("enums." + enumValue.getClass().getSimpleName() + "." + enumValue.name(), null, locale);
    }
}
