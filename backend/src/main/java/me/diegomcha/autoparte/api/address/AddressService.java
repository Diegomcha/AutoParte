package me.diegomcha.autoparte.api.address;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.address.dto.AddressDtoRequest;
import me.diegomcha.autoparte.api.address.dto.AddressDtoResponse;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.api.common.EntityMapper;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import me.diegomcha.autoparte.core.repos.AddressRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class AddressService {

    private static final Supplier<ResourceNotFoundException> NOT_FOUND_EXCEPTION = () ->
            new ResourceNotFoundException("Address not found");

    private final AddressRepo addressRepo;
    private final AddressMapper addressMapper;
    private final EntityMapper entityMapper;

    /**
     * Returns the address with the given ID, if it exists.
     *
     * @param id The ID of the address to retrieve
     * @return The address with the given ID, or empty if it does not exist
     * @throws ResourceNotFoundException if no address with the given ID exists
     */
    public AddressDtoResponse getAddress(UUID id) throws ResourceNotFoundException {
        return addressRepo.findById(id)
                .map(addressMapper::toResponse)
                .orElseThrow(NOT_FOUND_EXCEPTION);
    }

    /**
     * Creates a new address based on the provided dto.
     *
     * @param dto The dto containing the details of the address to be created
     * @return An EntityDtoCreated containing the ID of the newly created address
     */
    @Transactional
    public EntityDtoCreated createAddress(AddressDtoRequest dto) {
        var address = addressMapper.fromCreate(dto);
        address = addressRepo.save(address);
        return entityMapper.toCreated(address);
    }

}
