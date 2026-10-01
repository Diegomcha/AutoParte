package me.diegomcha.autoparte.api.account;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.diegomcha.autoparte.api.account.dto.AccountDtoFull;
import me.diegomcha.autoparte.api.account.dto.SecurityEventDto;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Tag(name = "Accounts", description = "Operations related to accounts")
@SuppressWarnings("unused")
interface AccountAPI {

    @Operation(summary = "List accounts")
    Page<AccountDtoFull> getAccounts(Pageable pageable);

    @Operation(summary = "List security events")
    Page<SecurityEventDto> getSecurityEvents(Pageable pageable);

    @Operation(summary = "List security events by account")
    Page<SecurityEventDto> getSecurityEventsByAccount(UUID accountId, Pageable pageable) throws ResourceNotFoundException;
}
