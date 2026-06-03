package me.diegomcha.autoparte.domain;

import lombok.*;
import me.diegomcha.autoparte.domain.base.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Establishment extends BaseEntity {

    private @NonNull String name;
    private @NonNull String sesCode;
    private Boolean internetConnection;

    @Setter(AccessLevel.NONE)
    private @NonNull Set<@NonNull Employee> employees = new HashSet<>();
    @Getter(AccessLevel.NONE)
    private @NonNull Set<@NonNull Booking> bookings = new HashSet<>();

    public Establishment(String name, String sesCode, Boolean internetConnection) {
        this.setName(name);
        this.setSesCode(sesCode);
        this.setInternetConnection(internetConnection);
    }

    public Set<Employee> getEmployees() {
        return Set.copyOf(this.employees);
    }

    public void addEmployee(@NonNull Employee employee) {
        employee._getEstablishments().add(this);
        this.employees.add(employee);
    }

    public void removeEmployee(@NonNull Employee employee) {
        employee._getEstablishments().remove(this);
        this.employees.remove(employee);
    }

    public Set<Booking> getBookings() {
        return Set.copyOf(this.bookings);
    }

    public void addBooking(@NonNull Booking booking) {
        this.bookings.add(booking);
        booking.setEstablishment(this);
    }

    public void removeBooking(@NonNull Booking booking) {
        this.bookings.remove(booking);
        booking.setEstablishment(null);
    }
}
