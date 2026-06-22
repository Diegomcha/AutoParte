package me.diegomcha.autoparte.domain;

import lombok.*;
import me.diegomcha.autoparte.core.validation.Validations;
import me.diegomcha.autoparte.domain.base.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Employee extends BaseEntity {

    @Setter
    private @NonNull String name;
    @Setter
    private @NonNull String surname;

    private @NonNull Account account;
    @ToString.Exclude
    private final @NonNull Set<@NonNull Accommodation> accommodations = new HashSet<>();

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

    Set<Accommodation> _getAccommodations() {
        return this.accommodations;
    }

    public Set<Accommodation> getAccommodations() {
        return Set.copyOf(this.accommodations);
    }
}
