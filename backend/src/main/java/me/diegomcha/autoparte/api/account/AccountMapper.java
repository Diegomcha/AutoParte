package me.diegomcha.autoparte.api.account;

import me.diegomcha.autoparte.api.account.dto.AccountDtoFull;
import me.diegomcha.autoparte.api.account.dto.SecurityEventDto;
import me.diegomcha.autoparte.domain.Account;
import me.diegomcha.autoparte.domain.SecurityEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
interface AccountMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    AccountDtoFull toDto(Account account);

    @Mapping(target = "accountId", source = "account.id")
    SecurityEventDto toDto(SecurityEvent securityEvent);
}
