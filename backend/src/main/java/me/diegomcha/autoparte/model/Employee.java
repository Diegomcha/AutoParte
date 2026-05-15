package me.diegomcha.autoparte.model;

import jakarta.persistence.Transient;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Employee extends Account {

    private @NonNull String name;
    private @NonNull String surname;

    @Setter(AccessLevel.NONE)
    private @NonNull Set<@NonNull Establishment> establishments = new HashSet<>();

    // TODO: validacion de formato de email
    public Employee(@NonNull String name, @NonNull String surname, @NonNull String email, @NonNull String hashedPassword) {
        super(email, hashedPassword, Set.of("ROLE_EMPLOYEE"));
        this.setName(name);
        this.setSurname(surname);
    }

    @Transient
    public String getEmail() {
        return this.getUsername();
    }

    public void setEmail(@NonNull String email) {
        this.setUsername(email);
    }

    Set<Establishment> _getEstablishments() {
        return this.establishments;
    }

    public Set<Establishment> getEstablishments() {
        return Set.copyOf(this.establishments);
    }
}
