package me.diegomcha.autoparte.core.repos;

import me.diegomcha.autoparte.domain.address.Address;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface AddressRepo extends CrudRepository<Address, UUID> {
}
