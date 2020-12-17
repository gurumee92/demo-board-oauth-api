package com.gurumee.demoboardauthapi.models.dtos.accounts;

import lombok.*;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @ToString @EqualsAndHashCode
@Builder
public class UpdateAccountRequestDto {
    @NotNull @NotEmpty
    @JsonProperty(value = "password")
    private String password;

    @NotNull @NotEmpty
    @JsonProperty(value = "password_check")
    private String password_check;
}
