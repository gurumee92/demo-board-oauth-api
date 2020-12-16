package com.gurumee.demoboardauthapi.services;

import com.gurumee.demoboardauthapi.components.AccountAdapter;
import com.gurumee.demoboardauthapi.models.dtos.accounts.UpdateAccountRequestDto;
import com.gurumee.demoboardauthapi.models.entities.accounts.Account;
import com.gurumee.demoboardauthapi.models.entities.accounts.AccountRole;
import com.gurumee.demoboardauthapi.models.dtos.accounts.CreateAccountRequestDto;
import com.gurumee.demoboardauthapi.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
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

    @Transactional
    public Optional<Account> update(Account account, UpdateAccountRequestDto requestDto) {
        String password = requestDto.getPassword();
        String passwordCheck = requestDto.getPassword_check();

        if (!password.equals(passwordCheck)) {
            return Optional.empty();
        }

        account.setPassword(password);
        Account updated = accountRepository.save(account);
        return Optional.of(updated);
    }
}
