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

    /**
     * Constructor for creating an Employee instance.
     *
     * @param name           Employee's first name. Must not be null.
     * @param surname        Employee's last name. Must not be null.
     * @param email          Employee's email address. Must not be null and must be a valid email format.
     * @param hashedPassword Employee's hashed password. Must not be null.
     * @throws IllegalArgumentException if any of the required parameters (name, surname, email, hashedPassword) are null or if the email is not in a valid format.
     */
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

    /**
     * Sets the email for the employee's account after validating it.
     *
     * @param email The new email address to set. Must not be null and must be a valid email format.
     * @throws IllegalArgumentException if the email is null or not in a valid format.
     */
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
