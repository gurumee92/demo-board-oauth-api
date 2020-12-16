package com.gurumee.demoauthapi.posts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByOwner_Username(@Param("username")String username);
    List<Post> findByTitleContainingOrContentContaining(String title, String content);
}
