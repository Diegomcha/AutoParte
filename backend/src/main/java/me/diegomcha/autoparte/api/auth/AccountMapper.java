package me.diegomcha.autoparte.api.auth;

import me.diegomcha.autoparte.api.auth.dto.AccountDto;
import me.diegomcha.autoparte.domain.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface AccountMapper {
    AccountDto toDto(Account account);
}
