package me.diegomcha.autoparte.api.accommodation;

import me.diegomcha.autoparte.domain.Accommodation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface AccommodationRepo extends CrudRepository<Accommodation, UUID>, PagingAndSortingRepository<Accommodation, UUID> {

    boolean existsBySesCode(String sesCode);

    boolean existsByName(String name);
    
}
