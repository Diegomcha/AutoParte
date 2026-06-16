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
