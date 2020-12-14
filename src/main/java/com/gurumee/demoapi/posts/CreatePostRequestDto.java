package com.gurumee.demoapi.posts;

import lombok.*;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @ToString @EqualsAndHashCode
@Builder
public class CreatePostRequestDto {
    @NotNull @NotEmpty
    private String title;

    @NotNull @NotEmpty
    private String content;
}
