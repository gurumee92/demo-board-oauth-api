package com.gurumee.demoapi.posts;

import com.gurumee.demoapi.accounts.Account;
import com.gurumee.demoapi.accounts.AccountAdapter;
import com.gurumee.demoapi.accounts.AccountResponseDto;
import com.gurumee.demoapi.accounts.CurrentAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostRepository postRepository;

    @GetMapping
    public ResponseEntity getPosts(@RequestParam(value="username", required = false) String username,
                                   @CurrentAccount AccountAdapter currentAccount) {
        List<Post> posts;

        if (username == null) {
            posts = postRepository.findAll();
        } else {
            posts = postRepository.findByOwner_Username(username);
        }

        List<PostResponseDto> responseDtoList = posts.stream().map(this::convertResponseDto).collect(Collectors.toList());
        return ResponseEntity.ok(responseDtoList);
    }

    @GetMapping("/search")
    public ResponseEntity searchPosts(@RequestParam(value="keyword", required = false) String keyword,
                                   @CurrentAccount AccountAdapter currentAccount) {
        List<Post> posts;

        if (keyword == null) {
            posts = postRepository.findAll();
        } else {
            posts = postRepository.findByTitleContainingOrContentContaining(keyword, keyword);
        }

        List<PostResponseDto> responseDtoList = posts.stream().map(this::convertResponseDto).collect(Collectors.toList());
        return ResponseEntity.ok(responseDtoList);
    }

    private PostResponseDto convertResponseDto(Post post) {
        Account account = post.getOwner();
        AccountResponseDto owner = AccountResponseDto.builder()
                .id(account.getId())
                .username(account.getUsername())
                .role(account.getRoles().toString())
                .created_at(account.getCreatedAt())
                .updated_at(account.getUpdatedAt())
                .build();
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .owner(owner)
                .created_at(post.getCreatedAt())
                .updated_at(post.getUpdatedAt())
                .build();
    }

    @PostMapping
    public ResponseEntity createPost(@RequestBody @Valid CreatePostRequestDto requestDto,
                                     Errors errors,
                                     @CurrentAccount AccountAdapter currentAccount) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        Post newPost = Post.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .owner(currentAccount.getAccount())
                .build();
        Post saved = postRepository.save(newPost);
        PostResponseDto responseDto = convertResponseDto(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }



    @GetMapping("/{id}")
    public ResponseEntity getPost(@PathVariable("id") Long id, @CurrentAccount AccountAdapter currentAccount) {
        Optional<Post> postOrNull = postRepository.findById(id);

        if (postOrNull.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Post post = postOrNull.get();
        PostResponseDto responseDto = convertResponseDto(post);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity updatePost(@PathVariable("id") Long id,
                                     @RequestBody @Valid UpdatePostRequestDto requestDto,
                                     Errors errors,
                                     @CurrentAccount AccountAdapter currentAccount) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        Optional<Post> postOrNull = postRepository.findById(id);

        if (postOrNull.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Post post = postOrNull.get();

        if (!post.getOwner().getUsername().equals(currentAccount.getUsername())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());
        Post updated = postRepository.save(post);
        PostResponseDto responseDto = convertResponseDto(updated);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deletePost(@PathVariable("id") Long id, @CurrentAccount AccountAdapter currentAccount) {
        Optional<Post> postOrNull = postRepository.findById(id);

        if (postOrNull.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Post post = postOrNull.get();

        if (!post.getOwner().getUsername().equals(currentAccount.getUsername())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        PostResponseDto responseDto = convertResponseDto(post);
        postRepository.delete(post);
        return ResponseEntity.ok(responseDto);
    }

}
