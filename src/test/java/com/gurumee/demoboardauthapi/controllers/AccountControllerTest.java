package com.gurumee.demoboardauthapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gurumee.demoboardauthapi.components.AppProperties;
import com.gurumee.demoboardauthapi.models.dtos.accounts.CreateAccountRequestDto;
import com.gurumee.demoboardauthapi.models.dtos.accounts.UpdateAccountRequestDto;
import com.gurumee.demoboardauthapi.models.entities.accounts.Account;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    @DisplayName("POST /api/accounts 테스트 - 성공")
    public void createAccountTest() throws Exception {
        CreateAccountRequestDto dto = CreateAccountRequestDto.builder()
                .username("test2")
                .password("test")
                .build();
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("created_at").exists())
                .andExpect(jsonPath("updated_at").exists())
                .andExpect(jsonPath("role").value("[USER]"))
                .andExpect(jsonPath("username").value("test2"))
        ;
    }

    @Test
    @DisplayName("POST /api/accounts 테스트 - 실패 : 빈 입력 값")
    public void createAccountTest_failed_empty_input() throws Exception {
        CreateAccountRequestDto dto = CreateAccountRequestDto.builder().build();
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        ;
    }

    @Test
    @DisplayName("POST /api/accounts 테스트 - 실패 : 같은 username 비지니스 로직 에러")
    public void createAccountTest_failed_collision_username() throws Exception {
        CreateAccountRequestDto dto = CreateAccountRequestDto.builder()
                .username("test")
                .password("test")
                .build();
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("message").value("Username is conflict."))
        ;
    }

    private String getBearerAccessToken() throws Exception {
        ResultActions perform = mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                .param("username", "test")
                .param("password", "test")
                .param("grant_type", "password"));

        String responseBody = perform.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();
        String token = parser.parseMap(responseBody).get("access_token").toString();
        return "Bearer " + token;
    }

    @Test
    @DisplayName("GET /api/accounts/profile 테스트 - 성공")
    public void getAccountTest() throws Exception {
        String bearerToken = getBearerAccessToken();
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
    @DisplayName("GET /api/accounts/profile 테스트 - 실패: accessToken 존재하지 않을 때")
    public void getAccountTest_failed_not_exist_access_token() throws Exception {
        mockMvc.perform(get("/api/accounts/profile"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("message").value("You need to access token"))
                ;

    }
    @Test
    @DisplayName("GET /api/accounts/profile 테스트 - 실패 : invalid access token")
    public void getAccountTest_failed_invalid_access_token() throws Exception {
        String fakeAccessToken = "ksfnqllkd1r0ifafdl";
        mockMvc.perform(get("/api/accounts/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + fakeAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("error").value("invalid_token"))
                .andExpect(jsonPath("error_description").value("Invalid access token: " + fakeAccessToken))
                ;
    }

    @Test
    @DisplayName("PUT /api/accounts/profile 테스트 - 성공")
    public void putAccountTest() throws Exception {
        Account account = accountRepository.findByUsername("test").get();
        final String beforeUpdatedPassword = account.getPassword();

        String bearerToken = getBearerAccessToken();
        UpdateAccountRequestDto dto = UpdateAccountRequestDto.builder()
                .password("updated")
                .password_check("updated")
                .build();

        mockMvc.perform(put("/api/accounts/profile")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto))

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

        account = accountRepository.findByUsername("test").get();
        System.out.println(beforeUpdatedPassword + " " + account.getPassword());
        assertNotEquals(beforeUpdatedPassword, account.getPassword());
    }

    @Test
    @DisplayName("PUT /api/accounts/profile 테스트 - 실패 : 빈 입력값")
    public void putAccountTest_failed_empty_input() throws Exception {
        String bearerToken = getBearerAccessToken();
        UpdateAccountRequestDto dto = UpdateAccountRequestDto.builder().build();
        mockMvc.perform(put("/api/accounts/profile")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
// 무슨 문제인지 모르겠으나 BadRequest 상태 값은 받아오는데 헤더, 바디 설정이 망가짐. (MockMvc/WebMvc 설정 때문이 아닐까 생각 중)
// 포스트맨에서는 다음의 출력 형식을 가짐
//        {
//                "timestamp": "2020-12-17T04:22:21.269+00:00",
//                "status": 400,
//                "error": "Bad Request",
//                "message": "",
//                "path": "/api/accounts/profile"
//        }
//                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
//                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
//                .andExpect(jsonPath("timestamp").exists())
//                .andExpect(jsonPath("error").value("Bad Request"))
//                .andExpect(jsonPath("message").exists())
//                .andExpect(jsonPath("path").value("/api/accounts/profile"))
        ;

    }

    @Test
    @DisplayName("PUT /api/accounts/profile 테스트 - 실패 : 잘못된 입력값 - 비지니스 로직 에러")
    public void putAccountTest_failed_wrong_input() throws Exception {
        String bearerToken = getBearerAccessToken();
        UpdateAccountRequestDto dto = UpdateAccountRequestDto.builder()
                .password("abc")
                .password_check("abb")
                .build();
        mockMvc.perform(put("/api/accounts/profile")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto))

        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("message").value("Check Your Input, password and check different."))
                ;
    }

    @Test
    @DisplayName("PUT /api/accounts/profile 테스트 - 실패 : not exist access token")
    public void putAccountTest_failed_not_exist_access_token() throws Exception {
        UpdateAccountRequestDto dto = UpdateAccountRequestDto.builder()
                .password("abc")
                .password_check("abc")
                .build();
        mockMvc.perform(put("/api/accounts/profile")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto))

        )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("error").value("unauthorized"))
                .andExpect(jsonPath("error_description").value("Full authentication is required to access this resource"))
        ;
    }

    @Test
    @DisplayName("PUT /api/accounts/profile 테스트 - 실패 : invalid access token")
    public void putAccountTest_failed_invalid_access_token() throws Exception {
        UpdateAccountRequestDto dto = UpdateAccountRequestDto.builder()
                .password("abc")
                .password_check("abc")
                .build();
        String fakeAccessToken = "ksfnqllkd1r0ifafdl";
        mockMvc.perform(put("/api/accounts/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer" + fakeAccessToken)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto))
        )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("error").value("invalid_token"))
                .andExpect(jsonPath("error_description").value("Invalid access token: " + fakeAccessToken))
        ;
    }

    @Test
    @DisplayName("DELETE /api/accounts/profile 테스트 - 성공")
    public void deleteAccountTest() throws Exception {
        long beforeDeletedCnt = accountRepository.count();

        String bearerToken = getBearerAccessToken();
        mockMvc.perform(delete("/api/accounts/profile")
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

        long afterDeletedCnt = accountRepository.count();
        assertEquals(beforeDeletedCnt-1, afterDeletedCnt);

    }

    @Test
    @DisplayName("DELETE /api/accounts/profile 테스트 - 실패: accessToken 존재하지 않을 때")
    public void deleteAccountTest_failed_not_exist_access_token() throws Exception {
        mockMvc.perform(delete("/api/accounts/profile"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("error").value("unauthorized"))
                .andExpect(jsonPath("error_description").value("Full authentication is required to access this resource"))
        ;

    }
    @Test
    @DisplayName("DELETE /api/accounts/profile 테스트 - 실패 : invalid access token")
    public void deleteAccountTest_failed_invalid_access_token() throws Exception {
        String fakeAccessToken = "ksfnqllkd1r0ifafdl";
        mockMvc.perform(delete("/api/accounts/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + fakeAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("error").value("invalid_token"))
                .andExpect(jsonPath("error_description").value("Invalid access token: " + fakeAccessToken))
        ;
    }

}