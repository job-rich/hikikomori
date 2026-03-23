package org.hikikomori.community.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.controller.data.CommentCreateRequest;
import org.hikikomori.community.controller.data.CommentUpdateRequest;
import org.hikikomori.community.controller.data.PostCreateRequest;
import org.hikikomori.community.controller.data.PostUpdateRequest;
import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.repository.CommentRepository;
import org.hikikomori.community.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Post findById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));
    }

    public Post create(PostCreateRequest request) {
        Post post = Post.builder()
                .userId(request.getUserId())
                .nickName(request.getNickName())
                .title(request.getTitle())
                .content(request.getContent())
                .tag(request.getTag())
                .build();

        return postRepository.save(post);
    }

    public void update(UUID postId, PostUpdateRequest request) {
        Post post = findById(postId);
        if (!post.getUserId().equals(request.getUserId())) {
            throw new IllegalArgumentException("본인의 게시글만 수정할 수 있습니다");
        }

        post.update(request.getTitle(), request.getContent(), request.getTag());

        postRepository.save(post);
    }

    @Transactional
    public void delete(UUID postId, Long userId) {
        Post post = findById(postId);
        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 게시글만 삭제할 수 있습니다");
        }

        commentRepository.deleteAllByPostId(postId);
        postRepository.deleteById(postId);
    }

    public List<Comment> findCommentsByPostId(UUID postId) {
        return commentRepository.findByPostIdAndParentIsNull(postId);
    }

    public Comment createComment(UUID postId, CommentCreateRequest request) {
        Post post = findById(postId);
        Comment parent = findParentComment(request.getParentId());
        Comment comment = Comment.builder()
                .content(request.getContent())
                .userId(request.getUserId())
                .nickName(request.getNickName())
                .post(post)
                .parent(parent)
                .build();

        return commentRepository.save(comment);
    }

    public void updateComment(UUID commentId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + commentId));
        if (!comment.getUserId().equals(request.getUserId())) {
            throw new IllegalArgumentException("본인의 댓글만 수정할 수 있습니다");
        }

        comment.updateContent(request.getContent());

        commentRepository.save(comment);
    }

    public void deleteComment(UUID commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + commentId));
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 댓글만 삭제할 수 있습니다");
        }

        comment.softDelete();

        commentRepository.save(comment);
    }

    private Comment findParentComment(UUID parentId) {
        if (parentId == null) {
            return null;
        }

        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다: " + parentId));

        if (parent.getParent() != null) {
            throw new IllegalArgumentException("대댓글에는 답글을 달 수 없습니다");
        }

        return parent;
    }
}
