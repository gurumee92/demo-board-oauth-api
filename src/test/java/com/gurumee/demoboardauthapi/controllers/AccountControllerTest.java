package com.gurumee.demoboardauthapi.controllers;

import com.gurumee.demoboardauthapi.components.AppProperties;
import com.gurumee.demoboardauthapi.models.dtos.accounts.CreateAccountRequestDto;
import com.gurumee.demoboardauthapi.repositories.AccountRepository;
import com.gurumee.demoboardauthapi.services.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AppProperties appProperties;

    @BeforeEach
    public void setUp() {
        accountRepository.deleteAll();
        CreateAccountRequestDto dto = CreateAccountRequestDto.builder()
                .username("test")
                .password("test")
                .build();
        accountService.saveAccount(dto);
    }

    @Test
    @DisplayName("get account 테스트 - 성공")
    public void getAccountTest() throws Exception {
        ResultActions perform = mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                .param("username", "test")
                .param("password", "test")
                .param("grant_type", "password"));

        String responseBody = perform.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();
        String token = parser.parseMap(responseBody).get("access_token").toString();
        String bearerToken = "Bearer" + token;
        System.out.println(bearerToken);

        mockMvc.perform(get("/api/accounts/profile")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("created_at").exists())
                .andExpect(jsonPath("updated_at").exists())
                .andExpect(jsonPath("role").value("[USER]"))
                .andExpect(jsonPath("username").value("test"))
                ;
    }

    @Test
    @DisplayName("get account 테스트 - 실패: accessToken 존재하지 않을 때")
    public void getAccountTest_failed_not_exist_access_token() throws Exception {
        mockMvc.perform(get("/api/accounts/profile"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("message").value("You need to access token"))
                ;

    }

}