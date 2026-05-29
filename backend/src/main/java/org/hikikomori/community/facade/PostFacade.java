package org.hikikomori.community.facade;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.dto.CommentDto;
import org.hikikomori.community.dto.PostDto;
import org.hikikomori.community.exception.BannedUserException;
import org.hikikomori.community.repository.BanRepositoryImpl;
import org.hikikomori.community.repository.CommentRepositoryImpl;
import org.hikikomori.community.repository.PostRepositoryImpl;
import org.hikikomori.community.service.CommentService;
import org.hikikomori.community.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostFacade {

    private final PostService postService;
    private final CommentService commentService;
    private final PostRepositoryImpl postRepository;
    private final CommentRepositoryImpl commentRepository;
    private final BanRepositoryImpl banRepository;

    public Page<PostDto.Response> getPosts(Pageable pageable) {
        return postRepository.findAll(pageable).map(PostDto.Response::from);
    }

    public Page<PostDto.Response> getMyPosts(Long userId, Pageable pageable) {
        return postRepository.findByUserId(userId, pageable).map(PostDto.Response::from);
    }

    public PostDto.Response getPost(UUID id) {
        Post post = postRepository.getById(id);
        return PostDto.Response.from(post);
    }

    public PostDto.Response createPost(PostDto.CreateRequest request) {
        checkNotBanned(request.userId());
        Post post = postService.buildPost(request);
        Post saved = postRepository.save(post);
        return PostDto.Response.from(saved);
    }

    public void updatePost(UUID postId, PostDto.UpdateRequest request) {
        Post post = postRepository.getById(postId);
        postService.checkOwnership(post, request.userId(), "수정");
        postService.applyUpdate(post, request);
        postRepository.save(post); // update도 변경된거 다 보내주는게 맞긴함.
    }

    @Transactional
    public void deletePost(UUID postId, Long userId) {
        Post post = postRepository.getById(postId);
        postService.checkOwnership(post, userId, "삭제");
        commentRepository.deleteAllByPostId(postId);
        postRepository.deleteById(postId);
    }

    public List<CommentDto.Response> getComments(UUID postId) {
        return commentRepository.findByPostIdAndParentIsNull(postId).stream()
                .map(CommentDto.Response::from)
                .toList();
    }

    public CommentDto.Response createComment(UUID postId, CommentDto.CreateRequest request) {
        checkNotBanned(request.userId());
        Post post = postRepository.getById(postId);
        Comment parent = request.parentId() != null
                ? commentRepository.getParentById(request.parentId())
                : null;

        commentService.checkNestingDepth(parent);
        Comment comment = commentService.buildComment(request, post, parent);
        Comment saved = commentRepository.save(comment);
        return CommentDto.Response.from(saved);
    }

    public void updateComment(UUID commentId, CommentDto.UpdateRequest request) {
        Comment comment = commentRepository.getById(commentId);
        commentService.checkOwnership(comment, request.userId(), "수정");
        commentService.applyUpdate(comment, request.content());
        commentRepository.save(comment);
    }

    public void deleteComment(UUID commentId, Long userId) {
        Comment comment = commentRepository.getById(commentId);
        commentService.checkOwnership(comment, userId, "삭제");
        commentService.applySoftDelete(comment);
        commentRepository.save(comment);
    }

    private void checkNotBanned(Long userId) {
        if (banRepository.isBanned(userId)) {
            throw new BannedUserException("신고 누적으로 작성이 제한된 사용자입니다");
        }
    }
}
