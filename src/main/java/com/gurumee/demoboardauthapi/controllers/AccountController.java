package com.gurumee.demoboardauthapi.controllers;

import com.gurumee.demoboardauthapi.components.AccountAdapter;
import com.gurumee.demoboardauthapi.components.annotations.CurrentAccount;
import com.gurumee.demoboardauthapi.models.dtos.ErrorResponseDto;
import com.gurumee.demoboardauthapi.models.dtos.accounts.AccountResponseDto;
import com.gurumee.demoboardauthapi.models.dtos.accounts.CreateAccountRequestDto;
import com.gurumee.demoboardauthapi.models.dtos.accounts.UpdateAccountRequestDto;
import com.gurumee.demoboardauthapi.models.dtos.posts.PostResponseDto;
import com.gurumee.demoboardauthapi.models.entities.accounts.Account;
import com.gurumee.demoboardauthapi.repositories.AccountRepository;
import com.gurumee.demoboardauthapi.repositories.PostRepository;
import com.gurumee.demoboardauthapi.services.AccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Api(value = "Account API")
@RestController
@RequestMapping(value = "/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final PostRepository postRepository;

    @ApiOperation(value = "POST /api/accounts", notes = "create a account")
    @PostMapping
    public ResponseEntity createAccount(@RequestBody @Valid CreateAccountRequestDto requestDto,
                                        @ApiIgnore Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        Optional<Account> saved = accountService.saveAccount(requestDto);

        if (saved.isEmpty()) {
            ErrorResponseDto errResponseDto = ErrorResponseDto.builder()
                    .message("Username is conflict.")
                    .build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errResponseDto);
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

    @ApiOperation(value = "GET /api/accounts/profile", notes = "get profile(need access token)")
    @Authorization(value = "read")
    @GetMapping("/profile")
    public ResponseEntity getAccount(@ApiIgnore @CurrentAccount AccountAdapter currentAccount) {
        if (currentAccount == null) {
            ErrorResponseDto errResponseDto = ErrorResponseDto.builder()
                    .message("You need to access token")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errResponseDto);
        }
        Account account = currentAccount.getAccount();
        AccountResponseDto accountResponseDto = AccountResponseDto.builder()
                .id(account.getId())
                .username(account.getUsername())
                .role(account.getRoles().toString())
                .created_at(account.getCreatedAt())
                .updated_at(account.getUpdatedAt())
                .build();
        return ResponseEntity.ok(accountResponseDto);
    }

    @ApiOperation(value = "PUT /api/accounts/profile", notes = "update profile(need access token)")
    @Authorization(value = "write")
    @PutMapping("/profile")
    public ResponseEntity updateAccount(@RequestBody @Valid UpdateAccountRequestDto requestDto,
                                     @ApiIgnore @CurrentAccount AccountAdapter currentAccount,
                                     @ApiIgnore Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        if (currentAccount == null) {
            ErrorResponseDto errResponseDto = ErrorResponseDto.builder()
                    .message("You need to access token")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errResponseDto);
        }

        Account account = currentAccount.getAccount();
        Optional<Account> updateOrNull = accountService.update(account, requestDto);

        if (updateOrNull.isEmpty()) {
            ErrorResponseDto errResponseDto = ErrorResponseDto.builder()
                    .message("Check Your Input, password and check different.")
                    .build();
            return ResponseEntity.badRequest().body(errResponseDto);
        }

        Account updated = updateOrNull.get();
        AccountResponseDto dto = AccountResponseDto.builder()
                .id(updated.getId())
                .username(updated.getUsername())
                .role(updated.getRoles().toString())
                .created_at(updated.getCreatedAt())
                .updated_at(updated.getUpdatedAt())
                .build();
        return ResponseEntity.ok(dto);
    }

    @ApiOperation(value = "DELETE /api/accounts/profile", notes = "delete profile(need access token)")
    @Authorization(value = "write")
    @DeleteMapping("/profile")
    public ResponseEntity deleteAccount(@ApiIgnore @CurrentAccount AccountAdapter currentAccount,
                                        @ApiIgnore @RequestHeader Map<String, String> headers) {
        if (currentAccount == null) {
            ErrorResponseDto errResponseDto = ErrorResponseDto.builder()
                    .message("You need to access token")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errResponseDto);
        }

        Account account = currentAccount.getAccount();
        List<PostResponseDto> postList = postRepository.getPostListByUsername(account.getUsername());

        for (PostResponseDto post : postList) {
            PostResponseDto response = postRepository.deletePost(headers.get("authorization"), post.getId());

            if (response == null) {
                ErrorResponseDto errResponseDto = ErrorResponseDto.builder()
                        .message("POST API와의 통신이 불안정합니다.")
                        .build();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errResponseDto);
            }
        }

        AccountResponseDto dto = AccountResponseDto.builder()
                .id(account.getId())
                .username(account.getUsername())
                .role(account.getRoles().toString())
                .created_at(account.getCreatedAt())
                .updated_at(account.getUpdatedAt())
                .build();
        accountRepository.delete(account);
        return ResponseEntity.ok(dto);
    }
}
