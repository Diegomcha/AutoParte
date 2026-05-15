package me.diegomcha.autoparte.model.person;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode
public class ContactInfo {

    private String phoneNumber1;
    private String phoneNumber2;
    private String email;

    public ContactInfo(String phoneNumber1, String phoneNumber2, String email) {
        if (phoneNumber1 == null && phoneNumber2 == null && email == null)
            throw new IllegalArgumentException("At least one contact information must be provided");

        this.setPhoneNumber1(phoneNumber1);
        this.setPhoneNumber2(phoneNumber2);
        this.setEmail(email);
    }

    // TODO: validación de formato de número de teléfono y email, por ejemplo, usando una expresión regular
    public void setPhoneNumber1(String phoneNumber1) {
        this.phoneNumber1 = phoneNumber1;
    }

    // TODO: validación de formato de número de teléfono y email, por ejemplo, usando una expresión regular
    public void setPhoneNumber2(String phoneNumber2) {
        this.phoneNumber2 = phoneNumber2;
    }

    // TODO: validación de formato de número de teléfono y email, por ejemplo, usando una expresión regular
    public void setEmail(String email) {
        this.email = email;
    }
}
