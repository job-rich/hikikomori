package org.hikikomori.community.facade;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.dto.request.CommentCreateRequest;
import org.hikikomori.community.dto.request.CommentUpdateRequest;
import org.hikikomori.community.dto.request.PostCreateRequest;
import org.hikikomori.community.dto.request.PostUpdateRequest;
import org.hikikomori.community.dto.response.CommentResponse;
import org.hikikomori.community.dto.response.PostResponse;
import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.repository.CommentRepository;
import org.hikikomori.community.repository.PostRepository;
import org.hikikomori.community.service.CommentService;
import org.hikikomori.community.service.PostService;
import org.hikikomori.community.service.vo.CommentCreate;
import org.hikikomori.community.service.vo.PostCreate;
import org.hikikomori.community.service.vo.PostUpdate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostFacade {

    private final PostService postService;
    private final CommentService commentService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public Page<PostResponse> findAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable).map(PostResponse::from);
    }

    public Page<PostResponse> findMyPosts(Long userId, Pageable pageable) {
        return postRepository.findByUserId(userId, pageable).map(PostResponse::from);
    }

    public PostResponse findPostById(UUID id) {
        Post post = findPostOrThrow(id);

        return PostResponse.from(post);
    }

    public PostResponse createPost(PostCreateRequest request) {
        PostCreate postCreate = new PostCreate(
                request.title(), request.content(), request.tag(),
                request.userId(), request.nickName()
        );

        Post post = postService.buildPost(postCreate);
        Post saved = postRepository.save(post);

        return PostResponse.from(saved);
    }

    public void updatePost(UUID postId, PostUpdateRequest request) {
        Post post = findPostOrThrow(postId);
        PostUpdate postUpdate = new PostUpdate(request.title(), request.content(), request.tag());

        postService.validateOwnership(post, request.userId(), "수정");
        postService.applyUpdate(post, postUpdate);
        postRepository.save(post);
    }

    @Transactional
    public void deletePost(UUID postId, Long userId) {
        Post post = findPostOrThrow(postId);

        postService.validateOwnership(post, userId, "삭제");
        commentRepository.deleteAllByPostId(postId);
        postRepository.deleteById(postId);
    }

    public List<CommentResponse> findCommentsByPostId(UUID postId) {
        return commentRepository.findByPostIdAndParentIsNull(postId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    public CommentResponse createComment(UUID postId, CommentCreateRequest request) {
        Post post = findPostOrThrow(postId);
        Comment parent = findParentComment(request.parentId());
        CommentCreate commentCreate = new CommentCreate(request.content(), request.userId(), request.nickName());

        commentService.validateNestingDepth(parent);
        Comment comment = commentService.buildComment(commentCreate, post, parent);
        Comment saved = commentRepository.save(comment);

        return CommentResponse.from(saved);
    }

    public void updateComment(UUID commentId, CommentUpdateRequest request) {
        Comment comment = findCommentOrThrow(commentId);

        commentService.validateOwnership(comment, request.userId(), "수정");
        commentService.applyUpdate(comment, request.content());
        commentRepository.save(comment);
    }

    public void deleteComment(UUID commentId, Long userId) {
        Comment comment = findCommentOrThrow(commentId);

        commentService.validateOwnership(comment, userId, "삭제");
        commentService.applySoftDelete(comment);
        commentRepository.save(comment);
    }

    private Post findPostOrThrow(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));
    }

    private Comment findCommentOrThrow(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + id));
    }

    private Comment findParentComment(UUID parentId) {
        if (parentId == null) {
            return null;
        }
        return commentRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다: " + parentId));
    }
}
