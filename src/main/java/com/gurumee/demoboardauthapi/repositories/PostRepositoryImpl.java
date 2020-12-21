package com.gurumee.demoboardauthapi.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.gurumee.demoboardauthapi.components.AppProperties;
import com.gurumee.demoboardauthapi.models.dtos.posts.PostResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Profile("!test")
public class PostRepositoryImpl implements PostRepository{
    private final AppProperties appProperties;
    private final RestTemplate restTemplate;


    public List<PostResponseDto> getPostListByUsername(String username) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.registerModule(new JavaTimeModule()
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-mm-dd HH:MM:ss")))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-mm-dd HH:MM:ss"))));
        String requestUrl = appProperties.getResourcePostEndpointUrl() + "/api/posts?username=" + username;
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(requestUrl, String.class);

        try {
            List<PostResponseDto> dtoList = objectMapper.readValue(responseEntity.getBody(), objectMapper.getTypeFactory().constructCollectionType(List.class, PostResponseDto.class));
            return dtoList;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json Parsing Error - post list");
        }
    }

    public PostResponseDto deletePost(String token, Long id) {
        HttpHeaders reqHeader = new HttpHeaders();
        reqHeader.add("Authorization", token);
        HttpEntity<String> request = new HttpEntity<>(reqHeader);
        String requestUrl = appProperties.getResourcePostEndpointUrl() + "/api/posts/" + id;
        ResponseEntity<PostResponseDto> response = restTemplate.exchange(requestUrl, HttpMethod.DELETE, request, PostResponseDto.class);

        if (!response.getStatusCode().equals(HttpStatus.OK)) {
            return null;
        }

        return response.getBody();
    }
}
