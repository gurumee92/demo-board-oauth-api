package com.gurumee.demoboardauthapi.components;

import com.gurumee.demoboardauthapi.services.AccountService;
import com.gurumee.demoboardauthapi.models.dtos.accounts.CreateAccountRequestDto;
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
