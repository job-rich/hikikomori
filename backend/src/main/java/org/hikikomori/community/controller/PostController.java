package org.hikikomori.community.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.dto.CommentDto;
import org.hikikomori.community.dto.PostDto;
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
    public ResponseEntity<Page<PostDto.Response>> findAll(@PageableDefault(size = 6) Pageable pageable) {
        return ResponseEntity.ok(postFacade.findAllPosts(pageable));
    }

    @GetMapping("/my/{userId}")
    public ResponseEntity<Page<PostDto.Response>> findMyPosts(
            @PathVariable Long userId,
            @PageableDefault(size = 6) Pageable pageable
    ) {
        return ResponseEntity.ok(postFacade.findMyPosts(userId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto.Response> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(postFacade.findPostById(id));
    }

    @PostMapping
    public ResponseEntity<PostDto.Response> create(@Valid @RequestBody PostDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postFacade.createPost(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable UUID id,
            @Valid @RequestBody PostDto.UpdateRequest request) {
        postFacade.updatePost(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @RequestParam Long userId) {
        postFacade.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDto.Response>> findComments(@PathVariable UUID id) {
        return ResponseEntity.ok(postFacade.findCommentsByPostId(id));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDto.Response> createComment(
            @PathVariable UUID id,
            @Valid @RequestBody CommentDto.CreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postFacade.createComment(id, request));
    }

    @PatchMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable UUID id,
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentDto.UpdateRequest request
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
