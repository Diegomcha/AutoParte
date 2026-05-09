package me.diegomcha.autoparte.api.model;

import jakarta.persistence.Transient;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Employee extends Account {
    private @NonNull String name;
    private @NonNull String surname;

    public Employee(@NonNull String name, @NonNull String surname, @NonNull String email, @NonNull String hashedPassword) {
        super(email, hashedPassword, Set.of("ROLE_EMPLOYEE"));
        this.name = name;
        this.surname = surname;
    }

    @Transient
    public String getEmail() {
        return this.getUsername();
    }

    public void setEmail(@NonNull String email) {
        this.setUsername(email);
    }
}
