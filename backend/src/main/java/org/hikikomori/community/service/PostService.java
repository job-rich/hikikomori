package org.hikikomori.community.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.controller.data.CommentCreateRequest;
import org.hikikomori.community.controller.data.PostCreateRequest;
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

    @Transactional
    public Post create(PostCreateRequest request) {
        Post post = Post.builder()
                .userId(request.getUserId())
                .nickName(request.getNickName())
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        return postRepository.save(post);
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
