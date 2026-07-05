package me.diegomcha.autoparte.api.catalogue.services;

import lombok.NonNull;
import me.diegomcha.autoparte.domain.Person;
import me.diegomcha.autoparte.domain.booking.payment.Payment;
import me.diegomcha.autoparte.domain.person.PersonalInfo;
import me.diegomcha.autoparte.domain.person.document.Document;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class CatalogueService {

    /**
     * Gets an array of person gender options.
     *
     * @return Array of person gender options
     */
    public String[] getPersonGenderOptions() {
        return this.getOptions(PersonalInfo.PersonalInfoGender.class);
    }

    /**
     * Gets an array of person relationship options.
     *
     * @return Array of person relationship options
     */
    public String[] getPersonRelationshipOptions() {
        return this.getOptions(Person.PersonRelationship.class);
    }

    /**
     * Gets an array of document type options.
     *
     * @return Array of document type options
     */
    public String[] getDocumentTypeOptions() {
        return this.getOptions(Document.DocumentType.class);
    }

    /**
     * Gets an array of payment type options.
     *
     * @return Array of payment type options
     */
    public String[] getPaymentTypeOptions() {
        return this.getOptions(Payment.PaymentType.class);
    }

    private <T extends Enum<T>> String[] getOptions(@NonNull Class<T> enumerator) {
        return Arrays.stream(enumerator.getEnumConstants())
                .map(Enum::name)
                .toArray(String[]::new);
    }
}
