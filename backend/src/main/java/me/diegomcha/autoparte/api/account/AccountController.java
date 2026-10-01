package me.diegomcha.autoparte.api.account;

import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.account.dto.AccountDtoFull;
import me.diegomcha.autoparte.api.account.dto.SecurityEventDto;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import me.diegomcha.autoparte.domain.Account;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
class AccountController implements AccountAPI {

    private final AccountService accountService;

    @GetMapping
    @Override
    public Page<AccountDtoFull> getAccounts(@ParameterObject Pageable pageable) {
        return accountService.getAccounts(pageable);
    }

    @GetMapping("/global/security-events")
    @Override
    public Page<SecurityEventDto> getSecurityEvents(@ParameterObject Pageable pageable) {
        return accountService.getSecurityEvents(pageable);
    }

    @GetMapping("/{accountId}/security-events")
    @Override
    public Page<SecurityEventDto> getSecurityEventsByAccount(@PathVariable UUID accountId, @ParameterObject Pageable pageable) throws ResourceNotFoundException {
        return accountService.getSecurityEventsByAccountId(accountId, pageable);
    }
}
