package com.gurumee.demoauthapi.posts;

import com.gurumee.demoauthapi.accounts.Account;
import com.gurumee.demoauthapi.accounts.AccountAdapter;
import com.gurumee.demoauthapi.accounts.AccountResponseDto;
import com.gurumee.demoauthapi.accounts.CurrentAccount;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Api(value = "Post API -> 추후 Product")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostRepository postRepository;

    @ApiOperation(value = "GET /api/posts", notes = "get post list")
    @GetMapping
    public ResponseEntity getPosts(@RequestParam(value="username", required = false) String username,
                                   @ApiIgnore @CurrentAccount AccountAdapter currentAccount) {
        List<Post> posts;

        if (username == null) {
            posts = postRepository.findAll();
        } else {
            posts = postRepository.findByOwner_Username(username);
        }

        List<PostResponseDto> responseDtoList = posts.stream().map(this::convertResponseDto).collect(Collectors.toList());
        return ResponseEntity.ok(responseDtoList);
    }

    @ApiOperation(value = "GET /api/posts/search", notes = "search post list")
    @GetMapping("/search")
    public ResponseEntity searchPosts(@RequestParam(value="keyword", required = false) String keyword,
                                   @ApiIgnore @CurrentAccount AccountAdapter currentAccount) {
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

    @ApiOperation(value = "POST /api/posts/", notes = "create a post")
    @Authorization(value = "write")
    @PostMapping
    public ResponseEntity createPost(@RequestBody @Valid CreatePostRequestDto requestDto,
                                     @ApiIgnore Errors errors,
                                     @ApiIgnore @CurrentAccount AccountAdapter currentAccount) {
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


    @ApiOperation(value = "GET /api/posts/:id", notes = "get a post")
    @GetMapping("/{id}")
    public ResponseEntity getPost(@PathVariable("id") Long id,
                                  @ApiIgnore @CurrentAccount AccountAdapter currentAccount) {
        Optional<Post> postOrNull = postRepository.findById(id);

        if (postOrNull.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Post post = postOrNull.get();
        PostResponseDto responseDto = convertResponseDto(post);
        return ResponseEntity.ok(responseDto);
    }

    @ApiOperation(value = "PUT /api/posts/id", notes = "update a post")
    @Authorization(value = "write")
    @PutMapping("/{id}")
    public ResponseEntity updatePost(@PathVariable("id") Long id,
                                     @RequestBody @Valid UpdatePostRequestDto requestDto,
                                     @ApiIgnore Errors errors,
                                     @ApiIgnore @CurrentAccount AccountAdapter currentAccount) {
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

    @ApiOperation(value = "DELETE /api/posts/id", notes = "delete a post")
    @Authorization(value = "write")
    @DeleteMapping("/{id}")
    public ResponseEntity deletePost(@PathVariable("id") Long id,
                                     @ApiIgnore @CurrentAccount AccountAdapter currentAccount) {
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
