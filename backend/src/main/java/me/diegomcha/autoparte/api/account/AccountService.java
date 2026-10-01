package me.diegomcha.autoparte.api.account;

import lombok.RequiredArgsConstructor;
import me.diegomcha.autoparte.api.account.dto.AccountDtoFull;
import me.diegomcha.autoparte.api.account.dto.SecurityEventDto;
import me.diegomcha.autoparte.core.exception.ResourceNotFoundException;
import me.diegomcha.autoparte.core.repos.AccountRepo;
import me.diegomcha.autoparte.core.repos.SecurityEventRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Transactional(readOnly = true)
class AccountService {

    private static final Supplier<ResourceNotFoundException> NOT_FOUND_EXCEPTION = () ->
            new ResourceNotFoundException("Account not found");

    private final AccountRepo accountRepo;
    private final SecurityEventRepo securityEventRepo;

    private final AccountMapper accountMapper;

    /**
     * Returns a paginated list of all accounts.
     *
     * @param pageable Pagination information (page number, size, sorting)
     * @return A page of accounts
     */
    public Page<AccountDtoFull> getAccounts(Pageable pageable) {
        return accountRepo.findAll(pageable).map(accountMapper::toDto);
    }

    /**
     * Returns a paginated list of all security events.
     *
     * @param pageable Pagination information (page number, size, sorting)
     * @return A page of security events
     */
    public Page<SecurityEventDto> getSecurityEvents(Pageable pageable) {
        return securityEventRepo.findAll(pageable).map(accountMapper::toDto);
    }

    /**
     * Returns a paginated list of security events for a specific account.
     *
     * @param accountId The UUID of the account for which to retrieve security events
     * @param pageable  Pagination information (page number, size, sorting)
     * @return A page of security events associated with the specified account
     * @throws ResourceNotFoundException If the account with the given ID does not exist
     */
    public Page<SecurityEventDto> getSecurityEventsByAccountId(UUID accountId, Pageable pageable) throws ResourceNotFoundException {
        if (!accountRepo.existsById(accountId))
            throw NOT_FOUND_EXCEPTION.get();

        return securityEventRepo.findByAccountId(accountId, pageable).map(accountMapper::toDto);
    }

}
