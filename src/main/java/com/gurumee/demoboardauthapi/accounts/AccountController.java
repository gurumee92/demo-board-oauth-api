package com.gurumee.demoboardauthapi.accounts;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Optional;


@Api(value = "Account API")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final RestTemplate restTemplate;

    // 계정 생성 -> 토큰 필요 x
    @ApiOperation(value = "POST /api/accounts", notes = "create a account")
    @PostMapping
    public ResponseEntity createAccount(@RequestBody @Valid CreateAccountRequestDto requestDto,
                                        @ApiIgnore Errors errors) {
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
    @ApiOperation(value = "GET /api/accounts/profile", notes = "get profile need account")
    @Authorization(value = "read")
    @GetMapping("/profile")
    public ResponseEntity getProfile(@ApiIgnore @CurrentAccount AccountAdapter currentAccount) {
        if (currentAccount == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("you need access token");
        }
        Account account = currentAccount.getAccount();
        AccountResponseDto dto = AccountResponseDto.builder()
                .id(account.getId())
                .username(account.getUsername())
                .role(account.getRoles().toString())
                .created_at(account.getCreatedAt())
                .updated_at(account.getUpdatedAt())
                .build();
        System.out.println(dto);
        return ResponseEntity.ok(dto);
    }
}
