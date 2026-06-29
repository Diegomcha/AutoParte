package me.diegomcha.autoparte.domain.person;

import lombok.*;
import me.diegomcha.autoparte.core.validation.Validations;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode
public class ContactInfo {

    private String phoneNumber1;
    private String phoneNumber2;
    private String email;

    /**
     * Constructor for creating a ContactInfo instance.
     * At least one of the parameters (phoneNumber1, phoneNumber2, email) must be provided
     *
     * @param phoneNumber1 1st valid phone number
     * @param phoneNumber2 2nd valid phone number
     * @param email        Valid email address
     * @throws IllegalArgumentException if all parameters are null or
     *                                  if any provided phone number or email is invalid
     */
    public ContactInfo(String phoneNumber1, String phoneNumber2, String email) {
        if (phoneNumber1 == null && phoneNumber2 == null && email == null)
            throw new IllegalArgumentException("At least one contact information must be provided");

        this.setPhoneNumber1(phoneNumber1);
        this.setPhoneNumber2(phoneNumber2);
        this.setEmail(email);
    }

    private void setPhoneNumber1(String phoneNumber1) {
        if (phoneNumber1 != null) Validations.ensureValidPhone(phoneNumber1);
        this.phoneNumber1 = phoneNumber1;
    }

    private void setPhoneNumber2(String phoneNumber2) {
        if (phoneNumber2 != null) Validations.ensureValidPhone(phoneNumber2);
        this.phoneNumber2 = phoneNumber2;
    }

    private void setEmail(String email) {
        if (email != null) Validations.ensureValidEmail(email);
        this.email = email;
    }
}
