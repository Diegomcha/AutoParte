package me.diegomcha.autoparte.api.address;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.address.dto.AddressDtoRequest;
import me.diegomcha.autoparte.api.address.dto.AddressDtoResponse;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class AddressController implements AddressAPI {

    private final AddressService addressService;

    @Override
    public AddressDtoResponse getAddressById(UUID id) throws ResourceNotFoundException {
        return addressService.getAddress(id);
    }

    @Override
    public EntityDtoCreated createAddress(AddressDtoRequest address) {
        return addressService.createAddress(address);
    }
}
