package com.gurumee.demoboardauthapi.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        super.configure(resources);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .anonymous()
                    .and()
                .authorizeRequests()
                    .antMatchers(HttpMethod.OPTIONS).permitAll()
                    .antMatchers(HttpMethod.GET).permitAll()
                    .mvcMatchers(HttpMethod.GET, "/api/**").permitAll()
                    .mvcMatchers(HttpMethod.POST, "/api/accounts").permitAll()
                    .mvcMatchers(HttpMethod.PUT, "/api/**").access("#oauth2.hasScope('write')")
                    .mvcMatchers(HttpMethod.DELETE, "/api/**").access("#oauth2.hasScope('write')")
                .anyRequest().authenticated()
                    .and()
                .exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler())
                ;
    }
}
