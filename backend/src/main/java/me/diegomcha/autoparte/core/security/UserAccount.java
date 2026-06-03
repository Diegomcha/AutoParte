package me.diegomcha.autoparte.core.security;

import lombok.Getter;
import me.diegomcha.autoparte.domain.Account;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.stream.Collectors;

public class UserAccount extends User {

    @Getter
    private final transient Account account;

    public UserAccount(Account account) {
        super(
                account.getUsername(),
                account.getHashedPassword(),
                account.isEnabled(),
                true,
                !account.isRequiresReset(),
                true,
                account.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toUnmodifiableSet())
        );
        this.account = account;
    }
}
