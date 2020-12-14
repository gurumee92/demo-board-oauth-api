package com.gurumee.demoapi.common;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @ToString @EqualsAndHashCode
@Builder
public class AppProperties {
    @Value("${my-app.client-id}")
    private String clientId;

    @Value("${my-app.client-secret}")
    private String clientSecret;
}
