package me.diegomcha.autoparte.core.repos;

import jdk.dynalink.Operation;
import me.diegomcha.autoparte.domain.Employee;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepo extends CrudRepository<Employee, UUID>, PagingAndSortingRepository<Employee, UUID> {

    default boolean existsByEmail(String email) {
        return this.existsByAccountUsername(email);
    }

    default Optional<Employee> findByEmail(String email) {
        return this.findByAccountUsername(email);
    }

    boolean existsByAccountUsername(String username);

    Optional<Employee> findByAccountUsername(String username);

    Optional<Employee> findByAccountId(UUID accountId);
}
