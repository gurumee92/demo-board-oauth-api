package com.gurumee.demoboardauthapi.accounts;

import lombok.*;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @ToString @EqualsAndHashCode
@Builder
public class CreateAccountRequestDto {
    @NotNull @NotEmpty
    @JsonProperty(value = "username")
    private String username;

    @NotNull @NotEmpty
    @JsonProperty(value = "password")
    private String password;
}
