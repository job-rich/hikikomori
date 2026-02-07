package org.hikikomori.community.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.controller.data.CommentCreateRequest;
import org.hikikomori.community.controller.data.CommentResponse;
import org.hikikomori.community.controller.data.PostCreateRequest;
import org.hikikomori.community.controller.data.PostResponse;
import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<Page<PostResponse>> findAll(@PageableDefault(size = 20) Pageable pageable) {
        Page<PostResponse> posts = postService.findAll(pageable).map(PostResponse::from);

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> findById(@PathVariable Long id) {
        Post post = postService.findById(id);

        return ResponseEntity.ok(PostResponse.from(post));
    }

    @PostMapping
    public ResponseEntity<PostResponse> create(@RequestBody PostCreateRequest request) {
        Post post = postService.create(request.title(), request.content());

        return ResponseEntity.status(HttpStatus.CREATED).body(PostResponse.from(post));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponse>> findComments(@PathVariable Long id) {
        List<CommentResponse> comments = postService.findCommentsByPostId(id).stream()
                .map(CommentResponse::from)
                .toList();

        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long id,
            @RequestBody CommentCreateRequest request
    ) {
        Comment comment = postService.createComment(id, request.parentId(), request.content());

        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(comment));
    }
}
