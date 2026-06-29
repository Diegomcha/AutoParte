package me.diegomcha.autoparte.domain;

import lombok.*;
import me.diegomcha.autoparte.domain.base.BaseEntity;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Accommodation extends BaseEntity {

    @Setter
    private @NonNull String name;
    @Setter
    private @NonNull String sesCode;
    @Setter
    private Boolean internetConnection;

    private Instant deletedAt;

    @ToString.Exclude
    private final @NonNull Set<@NonNull Employee> employees = new HashSet<>();
    @ToString.Exclude
    private final @NonNull Set<@NonNull Booking> bookings = new HashSet<>();

    /**
     * Constructor for creating an Accommodation instance.
     *
     * @param name               Accommodation name. Must not be null.
     * @param sesCode            SES code for the accommodation. Must not be null.
     * @param internetConnection Boolean indicating if the accommodation has internet connection. Can be null.
     * @throws IllegalArgumentException if any of the required parameters (name, sesCode) are null.
     */
    public Accommodation(@NonNull String name, @NonNull String sesCode, Boolean internetConnection) {
        this.setName(name);
        this.setSesCode(sesCode);
        this.setInternetConnection(internetConnection);
    }

    public Set<Employee> getEmployees() {
        return Set.copyOf(this.employees);
    }

    public void addEmployee(@NonNull Employee employee) {
        employee._getAccommodations().add(this);
        this.employees.add(employee);
    }

    public void removeEmployee(@NonNull Employee employee) {
        employee._getAccommodations().remove(this);
        this.employees.remove(employee);
    }

    Set<Booking> _getBookings() {
        return this.bookings;
    }

    public Set<Booking> getBookings() {
        return Set.copyOf(this.bookings);
    }

}
