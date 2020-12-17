package com.gurumee.demoboardauthapi.models.dtos;

import lombok.*;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @ToString @EqualsAndHashCode
@Builder
public class ErrorResponseDto {
    private String message;
}
