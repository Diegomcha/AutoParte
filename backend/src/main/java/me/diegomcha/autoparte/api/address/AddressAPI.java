package me.diegomcha.autoparte.api.address;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.diegomcha.autoparte.api.address.dto.AddressDtoRequest;
import me.diegomcha.autoparte.api.address.dto.AddressDtoResponse;
import me.diegomcha.autoparte.api.common.EntityDtoCreated;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;

import java.util.UUID;

@Tag(name = "Addresses", description = "Operations related to addresses")
@SuppressWarnings("unused")
interface AddressAPI {

    @Operation(summary = "Get address by id")
    AddressDtoResponse getAddressById(UUID id) throws ResourceNotFoundException;

    @Operation(summary = "Create address")
    EntityDtoCreated createAddress(AddressDtoRequest address);

}
