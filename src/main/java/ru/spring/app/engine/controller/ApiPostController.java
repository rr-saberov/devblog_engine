package ru.spring.app.engine.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.spring.app.engine.api.request.AddPostRequest;
import ru.spring.app.engine.api.request.CommentRequest;
import ru.spring.app.engine.api.request.ModerationRequest;
import ru.spring.app.engine.api.request.VoteRequest;
import ru.spring.app.engine.api.response.AddCommentResponse;
import ru.spring.app.engine.api.response.AddPostResponse;
import ru.spring.app.engine.api.response.CurrentPostResponse;
import ru.spring.app.engine.api.response.OkResult;
import ru.spring.app.engine.api.response.PostsResponse;
import ru.spring.app.engine.exceptions.AddCommentFailException;
import ru.spring.app.engine.exceptions.PostNotFoundException;
import ru.spring.app.engine.service.CommentService;
import ru.spring.app.engine.service.PostService;

import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@Tag(name = "post controller for rest api")
public class ApiPostController {

    private static final Logger LOGGER = LogManager.getLogger(ApiPostController.class);
    private final PostService postService;
    private final CommentService commentsService;

    public ApiPostController(PostService postService, CommentService commentsService) {
        this.postService = postService;
        this.commentsService = commentsService;
    }

    @GetMapping("/post")
    @Operation(summary = "method to get all posts")
    public ResponseEntity<PostsResponse> getPosts(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "30") Integer limit,
            @RequestParam(defaultValue = "recent") String mode) {
        return ResponseEntity.ok(postService.getPosts(offset, limit, mode));
    }

    @GetMapping("/post/search")
    @Operation(summary = "method to search posts")
    public ResponseEntity<PostsResponse> searchPosts(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "test") String query) {
        return ResponseEntity.ok(postService.getPostsByUserRequest(offset, limit, query));
    }

    @GetMapping("/post/byDate")
    @Operation(summary = "method to get posts by date")
    public ResponseEntity<PostsResponse> getPostsByDate(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "5") Integer limit,
            @RequestParam(defaultValue = "2005-10-9")
            @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate date) {
        return ResponseEntity.ok(postService.getPostsOnDay(offset, limit, date));
    }

    @GetMapping("/post/byTag")
    @Operation(summary = "method to get posts by tag")
    public ResponseEntity<PostsResponse> getPostsByTag(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "java") String tag) {
        return ResponseEntity.ok(postService.getPostsByTag(offset, limit, tag));
    }

    @GetMapping("/post/{ID}")
    @Operation(summary = "method to get post by id")
    public ResponseEntity<CurrentPostResponse> postById(@PathVariable(value = "ID") Long id) throws PostNotFoundException {
        LOGGER.info("try to get post by id");
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping("/post/moderation")
    @Operation(summary = "method to get posts for moderation")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<PostsResponse> postsForModeration(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "ACCEPTED") String status) {
        return ResponseEntity.ok(postService.getPostsForModeration(offset, limit, status));
    }

    @GetMapping("/post/my")
    @Operation(summary = "method to get users posts")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<PostsResponse> userPosts(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "published") String status,
            Principal principal) {
        return ResponseEntity.ok(postService.getUserPosts(offset, limit, status, principal.getName()));
    }

    @PostMapping("/moderation")
    @Operation(summary = "method to moderate post")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<Boolean> moderatePost(@RequestBody ModerationRequest request) {
        Boolean result = postService.moderatePost(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/post")
    @Operation(summary = "method to add new post")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<AddPostResponse> addPost(@RequestBody AddPostRequest request, Principal principal) {
        LOGGER.info("try to add post");
        return ResponseEntity.ok(postService.addNewPost(request, principal));
    }

    @PutMapping("/post/{ID}")
    @Operation(summary = "method to update post")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<AddPostResponse> updatePost(@PathVariable("ID") Long id, @RequestBody AddPostRequest request) {
        LOGGER.info("try to change post");
        return ResponseEntity.ok(postService.updatePost(id, request));
    }

    @PostMapping("/comment")
    @Operation(summary = "method to add comment")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<AddCommentResponse> addComment(@RequestBody CommentRequest comment,
                                                         Principal principal) throws AddCommentFailException {
        LOGGER.info("try to add comment");
        return ResponseEntity.ok(commentsService.addComment(comment, principal.getName()));
    }

    @PostMapping("/post/like")
    @Operation(summary = "method to add like")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<OkResult> addLike(@RequestBody VoteRequest request, Principal principal) {
        return ResponseEntity.ok(postService.addLike(request, principal.getName()));
    }

    @PostMapping("/post/dislike")
    @Operation(summary = "method to add dislike")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<OkResult> addDislike(@RequestParam VoteRequest request, Principal principal) {
        return ResponseEntity.ok(postService.addDislike(request, principal.getName()));
    }
}
