//package me.diegomcha.autoparte.config.security;
//
//import lombok.NonNull;
//import lombok.RequiredArgsConstructor;
//
//import me.diegomcha.autoparte.domain.Account;
//import org.jspecify.annotations.Nullable;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.util.Collection;
//
//@RequiredArgsConstructor
//public class SecurityAccount implements UserDetails {
//
//    @NonNull
//    private final Account account;
//
//    @Override
//    public String getUsername() {
//        return this.account.getUsername();
//    }
//
//    @Override
//    public @Nullable String getPassword() {
//        return this.account.getHashedPassword();
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return this.account.getRoles().stream()
//                .map(SimpleGrantedAuthority::new)
//                .toList();
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return this.account.isEnabled();
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return !this.account.isRequiresReset();
//    }
//}
