package com.gurumee.demoapi.accounts;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    // 계정 생성 -> 토큰 필요 x
    @PostMapping
    public ResponseEntity createAccount(@RequestBody @Valid CreateAccountRequestDto requestDto, Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        Optional<Account> saved = accountService.saveAccount(requestDto);

        if (saved.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }

        Account account = saved.get();
        AccountResponseDto responseDto = AccountResponseDto.builder()
                .id(account.getId())
                .username(account.getUsername())
                .role(account.getRoles().toString())
                .created_at(account.getCreatedAt())
                .updated_at(account.getUpdatedAt())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // 계정 조회 -> 토큰 필요, 같은 계정 아니면 403
    @GetMapping("/profile")
    public ResponseEntity getProfile(@CurrentAccount AccountAdapter currentAccount) {
        Account account = currentAccount.getAccount();
        AccountResponseDto dto = AccountResponseDto.builder()
                .id(account.getId())
                .username(account.getUsername())
                .role(account.getRoles().toString())
                .created_at(account.getCreatedAt())
                .updated_at(account.getUpdatedAt())
                .build();
        return ResponseEntity.ok(dto);
    }
}
