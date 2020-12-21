package com.gurumee.demoboardauthapi.repositories;

import com.gurumee.demoboardauthapi.models.dtos.posts.PostResponseDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository {
    List<PostResponseDto> getPostListByUsername(String username);
    PostResponseDto deletePost(String token, Long id);
}
