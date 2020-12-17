package com.gurumee.demoboardauthapi.services;

import com.gurumee.demoboardauthapi.components.AccountAdapter;
import com.gurumee.demoboardauthapi.models.dtos.accounts.CreateAccountRequestDto;
import com.gurumee.demoboardauthapi.models.dtos.accounts.UpdateAccountRequestDto;
import com.gurumee.demoboardauthapi.models.entities.accounts.Account;
import com.gurumee.demoboardauthapi.models.entities.accounts.AccountRole;
import com.gurumee.demoboardauthapi.repositories.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccountServiceTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @BeforeEach
    public void setUp() {
        accountRepository.deleteAll();
        CreateAccountRequestDto dto = CreateAccountRequestDto.builder()
                .username("test")
                .password("test")
                .build();
        accountService.saveAccount(dto);
    }

    // save test
    @Test
    @DisplayName("saveAccountTest - 성공")
    public void saveTest() {
        CreateAccountRequestDto dto = CreateAccountRequestDto.builder()
                .username("test2")
                .password("test")
                .build();
        Optional<Account> accountOrNull = accountService.saveAccount(dto);
        assertTrue(accountOrNull.isPresent());

        Account account = accountOrNull.get();
        assertNotNull(account.getId());
        assertNotNull(account.getCreatedAt());
        assertNotNull(account.getUpdatedAt());
        assertTrue(passwordEncoder.matches(dto.getPassword(), account.getPassword()));
        assertEquals(Set.of(AccountRole.USER), account.getRoles());
    }

    @Test
    @DisplayName("saveAccountTest - 실패:존재하는 유저")
    public void saveTestFailed_exist_user() {
        CreateAccountRequestDto dto = CreateAccountRequestDto.builder()
                .username("test")
                .password("test")
                .build();
        Optional<Account> accountOrNull = accountService.saveAccount(dto);
        assertTrue(accountOrNull.isEmpty());
    }

    // loadByUsername test
    @Test
    @DisplayName("loadByUsernameTest - 성공")
    public void loadUserByUsernameTest() {
        String name = "test";
        AccountAdapter accountAdapter = (AccountAdapter) accountService.loadUserByUsername(name);
        Account account = accountAdapter.getAccount();

        assertEquals(name, account.getUsername());
        assertTrue(passwordEncoder.matches("test", account.getPassword()));
        assertNotNull(account.getId());
        assertNotNull(account.getCreatedAt());
        assertNotNull(account.getUpdatedAt());
        assertEquals(Set.of(AccountRole.USER), account.getRoles());
    }

    @Test
    @DisplayName("loadByUsernameTest - 실패: 존재하지 않는 유저이름")
    public void loadUserByUsernameFailed_not_exist_user() {
        String name = "faked";
        assertThrows(UsernameNotFoundException.class, () -> {
           accountService.loadUserByUsername(name);
        });
    }


    // update test
    @Test
    @DisplayName("updateAccountTest - 성공")
    public void updateTest() {
        String name = "test";
        String updatePassword = "updated";
        AccountAdapter accountAdapter = (AccountAdapter) accountService.loadUserByUsername(name);
        Account account = accountAdapter.getAccount();
        UpdateAccountRequestDto dto = UpdateAccountRequestDto.builder()
                .password(updatePassword)
                .password_check(updatePassword)
                .build();

        Optional<Account> updateOrNull = accountService.update(account, dto);
        assertTrue(updateOrNull.isPresent());

        Account updated = updateOrNull.get();
        assertEquals(account, updated);
        assertTrue(passwordEncoder.matches(updatePassword, updated.getPassword()));
    }

    @Test
    @DisplayName("updateAccountTest - 실패: 패스워드,패스워드 확인 불일치")
    public void updateTestFailed_different_password_and_check() {
        String name = "test";
        AccountAdapter accountAdapter = (AccountAdapter) accountService.loadUserByUsername(name);
        Account account = accountAdapter.getAccount();
        UpdateAccountRequestDto dto = UpdateAccountRequestDto.builder()
                .password("ab")
                .password_check("aa")
                .build();

        Optional<Account> updateOrNull = accountService.update(account, dto);
        assertTrue(updateOrNull.isEmpty());
    }




}