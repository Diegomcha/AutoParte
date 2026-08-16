package me.diegomcha.autoparte.api.address;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.address.dto.AddressDtoRequest;
import me.diegomcha.autoparte.api.address.dto.AddressDtoResponse;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class AddressController implements AddressAPI {

    private final AddressService addressService;

    @GetMapping("/addresses/{id}")
    @Override
    public AddressDtoResponse getAddressById(@PathVariable UUID id) throws ResourceNotFoundException {
        return addressService.getAddress(id);
    }

    @PostMapping("/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public EntityDtoCreated createAddress(@Valid @RequestBody AddressDtoRequest address) {
        return addressService.createAddress(address);
    }


    @GetMapping("/accommodations/{accommodationId}/bookings/{bookingId}/addresses")
    @Override
    public List<AddressDtoResponse> getBookingAddresses(@PathVariable UUID accommodationId, @PathVariable UUID bookingId) throws ResourceNotFoundException {
        return addressService.getBookingAddresses(accommodationId, bookingId);
    }
}
