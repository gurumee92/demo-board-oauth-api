package com.gurumee.demoboardauthapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class OAuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AppProperties appProperties;

    private ObjectMapper objectMapper;


    @BeforeEach
    public void setUp() {
        accountRepository.deleteAll();
        CreateAccountRequestDto dto = CreateAccountRequestDto.builder()
                .username("test")
                .password("test")
                .build();
        accountService.saveAccount(dto);
        objectMapper = new ObjectMapper();
    }


    @Test
    @DisplayName("/oauth/token 테스트 - 성공")
    public void oauthTokenTest() throws Exception {
        mockMvc.perform(post("/oauth/token")
                    .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                    .param("username", "test")
                    .param("password", "test")
                    .param("grant_type", "password")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"))
                .andExpect(jsonPath("access_token").exists())
                .andExpect(jsonPath("token_type").exists())
                .andExpect(jsonPath("expires_in").exists())
                .andExpect(jsonPath("scope").exists())
        ;
    }

    @Test
    @DisplayName("/oauth/token 테스트 - 잘못된 헤더")
    public void oauthTokenTestFailed_wrong_authorization_header() throws Exception {
        String fakeHeaderValue = "fake";
        mockMvc.perform(post("/oauth/token")
                .with(httpBasic(fakeHeaderValue, fakeHeaderValue))
                .param("username", "test")
                .param("password", "test")
                .param("grant_type", "password")
        )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                ;
    }

    @Test
    @DisplayName("/oauth/token 테스트 - 잘못된 파라미터, grant_tpe")
    public void oauthTokenTestFailed_wrong_params_grant_type() throws Exception {
        mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                .param("username", "test")
                .param("password", "test")
                .param("grant_type", "refresh_token")
        )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("error").value("invalid_client"))
                .andExpect(jsonPath("error_description").value("Unauthorized grant type: refresh_token"))
        ;
    }

    @Test
    @DisplayName("/oauth/token 테스트 - 잘못된 파라미터, 유저 정보")
    public void oauthTokenTestFailed_wrong_params_user_info() throws Exception {
        String fakeHeaderValue = "fake";
        mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                .param("username", fakeHeaderValue)
                .param("password", fakeHeaderValue)
                .param("grant_type", "password")
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error").value("invalid_grant"))
                .andExpect(jsonPath("error_description").value("Bad credentials"))
        ;
    }

    private String getAccessToken() throws Exception {
        ResultActions perform = mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                .param("username", "test")
                .param("password", "test")
                .param("grant_type", "password"));

        String responseBody = perform.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();
        return parser.parseMap(responseBody).get("access_token").toString();
    }


    @Test
    @DisplayName("/oauth/check_token 테스트 - 성공")
    public void checkTokenTest() throws Exception {
        String token = getAccessToken();
        mockMvc.perform(get("/oauth/check_token").param("token", token)
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("active").exists())
                .andExpect(jsonPath("exp").exists())
                .andExpect(jsonPath("user_name").value("test"))
                .andExpect(jsonPath("client_id").value(appProperties.getClientId()))
                .andExpect(jsonPath("authorities").exists())
                .andExpect(jsonPath("scope").exists())
                ;
    }



    @Test
    @DisplayName("/oauth/check_token 테스트 - 실패: 잘못된 헤더")
    public void checkTokenTestFailed_invalid_header() throws Exception {
        String token = getAccessToken();
        String fakeValue = "fake";
        mockMvc.perform(get("/oauth/check_token").param("token", token)
                .with(httpBasic(fakeValue, fakeValue))
        )
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("/oauth/check_token 테스트 - 실패: 빈 토큰 전달")
    public void checkTokenTestFailed_empty_token() throws Exception {
        mockMvc.perform(get("/oauth/check_token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("/oauth/check_token 테스트 - 실패: 잘못된 토큰 전달")
    public void checkTokenTestFailed_invalid_token() throws Exception {
        mockMvc.perform(get("/oauth/check_token").param("token", "fake-token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error").value("invalid_token"))
                .andExpect(jsonPath("error_description").value("Token was not recognised"))
        ;

    }

}
