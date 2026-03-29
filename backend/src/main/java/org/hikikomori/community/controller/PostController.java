package org.hikikomori.community.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.dto.request.CommentCreateRequest;
import org.hikikomori.community.dto.request.CommentUpdateRequest;
import org.hikikomori.community.dto.request.PostCreateRequest;
import org.hikikomori.community.dto.request.PostUpdateRequest;
import org.hikikomori.community.dto.response.CommentResponse;
import org.hikikomori.community.dto.response.PostResponse;
import org.hikikomori.community.facade.PostFacade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostFacade postFacade;

    @GetMapping
    public ResponseEntity<Page<PostResponse>> findAll(@PageableDefault(size = 6) Pageable pageable) {
        Page<PostResponse> posts = postService.findAll(pageable).map(PostResponse::from);

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/my/{userId}")
    public ResponseEntity<Page<PostResponse>> findMyPosts(
            @PathVariable Long userId,
            @PageableDefault(size = 6) Pageable pageable
    ) {
        Page<PostResponse> posts = postService.findByUserId(userId, pageable).map(PostResponse::from);

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(postFacade.findPostById(id));
    }

    @PostMapping
    public ResponseEntity<PostResponse> create(@Valid @RequestBody PostCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postFacade.createPost(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable UUID id,
            @Valid @RequestBody PostUpdateRequest request) {
        postFacade.updatePost(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @RequestParam Long userId) {
        postFacade.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponse>> findComments(@PathVariable UUID id) {
        return ResponseEntity.ok(postFacade.findCommentsByPostId(id));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable UUID id,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postFacade.createComment(id, request));
    }

    @PatchMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable UUID id,
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        postFacade.updateComment(commentId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID id,
            @PathVariable UUID commentId,
            @RequestParam Long userId
    ) {
        postFacade.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
