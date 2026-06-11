package org.hikikomori.community.facade;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.dto.CommentDto;
import org.hikikomori.community.dto.PostDto;
import org.hikikomori.community.repository.BanRepositoryImpl;
import org.hikikomori.community.repository.CommentRepositoryImpl;
import org.hikikomori.community.repository.PostLikeRepositoryImpl;
import org.hikikomori.community.repository.PostRepositoryImpl;
import org.hikikomori.community.service.BanService;
import org.hikikomori.community.service.CommentService;
import org.hikikomori.community.service.PostLikeService;
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
    private final PostLikeService postLikeService;
    private final BanService banService;
    private final PostRepositoryImpl postRepository;
    private final CommentRepositoryImpl commentRepository;
    private final BanRepositoryImpl banRepository;
    private final PostLikeRepositoryImpl postLikeRepository;

    public Page<PostDto.Response> getPosts(Pageable pageable, Long viewerId) {
        return mapPosts(postRepository.findAll(pageable), viewerId);
    }

    public Page<PostDto.Response> getMyPosts(Long userId, Pageable pageable, Long viewerId) {
        return mapPosts(postRepository.findByUserId(userId, pageable), viewerId);
    }

    public PostDto.Response getPost(UUID id, Long viewerId) {
        Post post = postRepository.getVisibleById(id);
        boolean likedByMe = viewerId != null
                && postLikeRepository.existsByUserIdAndPostId(viewerId, id);
        return PostDto.Response.from(post, likedByMe);
    }

    public PostDto.Response createPost(PostDto.CreateRequest request) {
        banService.checkNotBanned(banRepository.isBanned(request.userId()));
        Post post = postService.buildPost(request);
        Post saved = postRepository.save(post);
        return PostDto.Response.from(saved);
    }

    @Transactional
    public void updatePost(UUID postId, PostDto.UpdateRequest request) {
        banService.checkNotBanned(banRepository.isBanned(request.userId()));
        Post post = postRepository.getById(postId);
        postService.checkOwnership(post, request.userId(), "수정");
        postService.applyUpdate(post, request);
        postRepository.save(post); // update도 변경된거 다 보내주는게 맞긴함.
    }

    @Transactional
    public void recordView(UUID postId) {
        postRepository.getById(postId);
        postRepository.incrementViewCount(postId);
    }

    @Transactional
    public PostDto.LikeToggleResponse toggleLike(UUID postId, Long userId) {
        banService.checkNotBanned(banRepository.isBanned(userId));
        Post post = postRepository.getVisibleById(postId);
        postLikeService.checkNotSelfLike(post.getUserId(), userId);

        boolean alreadyLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if (alreadyLiked) {
            postLikeRepository.deleteByUserIdAndPostId(userId, postId);
            postRepository.decrementLikeCount(postId);
        } else {
            postLikeRepository.save(postLikeService.buildPostLike(userId, postId));
            postRepository.incrementLikeCount(postId);
        }

        Post updated = postRepository.getById(postId);
        return new PostDto.LikeToggleResponse(!alreadyLiked, updated.getLikeCount());
    }

    @Transactional
    public void deletePost(UUID postId, Long userId) {
        Post post = postRepository.getById(postId);
        postService.checkOwnership(post, userId, "삭제");
        postLikeRepository.deleteAllByPostId(postId);
        commentRepository.deleteAllByPostId(postId);
        postRepository.deleteById(postId);
    }

    public List<CommentDto.Response> getComments(UUID postId) {
        return commentRepository.findByPostIdAndParentIsNull(postId).stream()
                .map(CommentDto.Response::from)
                .toList();
    }

    public CommentDto.Response createComment(UUID postId, CommentDto.CreateRequest request) {
        banService.checkNotBanned(banRepository.isBanned(request.userId()));
        Post post = postRepository.getById(postId);
        Comment parent = request.parentId() != null
                ? commentRepository.getParentById(request.parentId())
                : null;

        commentService.checkNestingDepth(parent);
        Comment comment = commentService.buildComment(request, post, parent);
        Comment saved = commentRepository.save(comment);
        return CommentDto.Response.from(saved);
    }

    @Transactional
    public void updateComment(UUID commentId, CommentDto.UpdateRequest request) {
        banService.checkNotBanned(banRepository.isBanned(request.userId()));
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

    private Page<PostDto.Response> mapPosts(Page<Post> posts, Long viewerId) {
        if (viewerId == null) {
            return posts.map(PostDto.Response::from);
        }
        List<UUID> postIds = posts.getContent().stream().map(Post::getId).toList();
        Set<UUID> likedIds = new HashSet<>(
                postLikeRepository.findLikedPostIds(viewerId, postIds));
        return posts.map(post -> PostDto.Response.from(post, likedIds.contains(post.getId())));
    }
}
