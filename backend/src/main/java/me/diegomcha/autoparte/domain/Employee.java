package me.diegomcha.autoparte.domain;

import lombok.*;
import me.diegomcha.autoparte.core.validation.Validations;
import me.diegomcha.autoparte.domain.base.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Employee extends BaseEntity {
    
    private @NonNull String name;
    private @NonNull String surname;

    @Setter(AccessLevel.NONE)
    private @NonNull Account account;
    @Setter(AccessLevel.NONE)
    private @NonNull Set<@NonNull Establishment> establishments = new HashSet<>();

    public Employee(@NonNull String name, @NonNull String surname, @NonNull String email, @NonNull String hashedPassword) {
        this.account = new Account(email, hashedPassword, Set.of("ROLE_EMPLOYEE"));
        this.setName(name);
        this.setSurname(surname);
        // Run email validations
        this.setEmail(email);
    }

    public String getEmail() {
        return this.account.getUsername();
    }

    public void setEmail(@NonNull String email) {
        Validations.ensureValidEmail(email);
        this.account.setUsername(email);
    }

    Set<Establishment> _getEstablishments() {
        return this.establishments;
    }

    public Set<Establishment> getEstablishments() {
        return Set.copyOf(this.establishments);
    }
}
