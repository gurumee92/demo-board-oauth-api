package com.gurumee.demoauthapi.accounts;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<Account> saveAccount(CreateAccountRequestDto requestDto) {
        Optional<Account> account = accountRepository.findByUsername(requestDto.getUsername());

        if (account.isPresent()) {
            return Optional.empty();
        }

        Account newAccount = Account.builder()
                .username(requestDto.getUsername())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .roles(Set.of(AccountRole.USER))
                .build();
        Account saved = accountRepository.save(newAccount);
        return Optional.of(saved);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return new AccountAdapter(account);
    }
}
