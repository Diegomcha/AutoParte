package me.diegomcha.autoparte.api.employee;

import me.diegomcha.autoparte.domain.Employee;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

interface EmployeeRepo extends CrudRepository<Employee, UUID>, PagingAndSortingRepository<Employee, UUID> {

    default boolean existsByEmail(String email) {
        return this.existsByAccountUsername(email);
    }

    boolean existsByAccountUsername(String username);

}
