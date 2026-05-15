package me.diegomcha.autoparte.api.employee;

import me.diegomcha.autoparte.model.Employee;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface EmployeeRepo extends CrudRepository<Employee, UUID>, PagingAndSortingRepository<Employee, UUID> {
    boolean existsByUsername(String username);
}
