package com.gurumee.demoboardauthapi.repositories;

import com.gurumee.demoboardauthapi.models.dtos.posts.PostResponseDto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Profile("test")
public class TestPostRepositoryImpl implements PostRepository {
    @Override
    public List<PostResponseDto> getPostListByUsername(String username) {
        return new ArrayList<>();
    }

    @Override
    public PostResponseDto deletePost(String token, Long id) {
        return null;
    }
}
