package com.gurumee.demoboardauthapi.accounts;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @ToString @EqualsAndHashCode
@Builder
public class AccountResponseDto {
    private Long id;
    private String username;
    private String role;

    @DateTimeFormat(pattern = "yyyy-mm-dd HH:MM:ss")
    private LocalDateTime created_at;

    @DateTimeFormat(pattern = "yyyy-mm-dd HH:MM:ss")
    private LocalDateTime updated_at;
}
