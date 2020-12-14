package com.gurumee.demoapi.common;

import com.gurumee.demoapi.accounts.AccountService;
import com.gurumee.demoapi.accounts.CreateAccountRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationInitRunner implements ApplicationRunner {
    private final AccountService accountService;

    @Override
    public void run(ApplicationArguments args) {
        CreateAccountRequestDto user = CreateAccountRequestDto.builder()
                .username("test_user")
                .password("test_pass")
                .build();
        accountService.saveAccount(user);
    }
}
