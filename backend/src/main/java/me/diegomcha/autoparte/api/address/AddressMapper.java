package me.diegomcha.autoparte.api.address;

import me.diegomcha.autoparte.api.address.dto.AddressDtoRequest;
import me.diegomcha.autoparte.api.address.dto.AddressDtoResponse;
import me.diegomcha.autoparte.domain.address.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
abstract class AddressMapper {

    public abstract AddressDtoResponse toResponse(Address address);

    public Address fromCreate(AddressDtoRequest dto) {
        return Address.of(
                dto.addressLine1(),
                dto.addressLine2(),
                dto.municipality(),
                dto.postalCode(),
                dto.country()
        );
    }

}
