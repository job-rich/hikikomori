package org.hikikomori.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.hikikomori.community.domain.Post;
import org.hikikomori.community.service.vo.PostCreate;
import org.hikikomori.community.service.vo.PostUpdate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostServiceTest {

    private final PostService postService = new PostService();

    @Test
    @DisplayName("게시글 엔티티 생성")
    void buildPost() {
        PostCreate postCreate = new PostCreate("제목", "내용", "VOID", 1L, "테스터");

        Post post = postService.buildPost(postCreate);

        assertThat(post.getUserId()).isEqualTo(1L);
        assertThat(post.getNickName()).isEqualTo("테스터");
        assertThat(post.getTitle()).isEqualTo("제목");
        assertThat(post.getContent()).isEqualTo("내용");
        assertThat(post.getId()).isNotNull();
        assertThat(post.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("게시글 소유자 검증 통과")
    void validateOwnershipSuccess() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").build();

        assertThatCode(() -> postService.validateOwnership(post, 1L, "수정"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("타인 게시글 수정 시 예외")
    void validateOwnershipFailOnUpdate() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").build();

        assertThatThrownBy(() -> postService.validateOwnership(post, 2L, "수정"))
        Post result = postService.findById(POST_ID);

        assertThat(result.getTitle()).isEqualTo("제목");
        verify(postRepository).findById(POST_ID);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외")
    void findByIdNotFound() {
        UUID notFoundId = UUID.randomUUID();
        given(postRepository.findById(notFoundId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.findById(notFoundId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("userId로 게시글 조회")
    void findByUserId() {
        Long userId = 12345L;
        List<Post> posts = List.of(
                Post.builder().userId(userId).nickName("유저").title("제목1").content("내용1").tag("VOID").build(),
                Post.builder().userId(userId).nickName("유저").title("제목2").content("내용2").tag("VOID").build()
        );
        Page<Post> page = new PageImpl<>(posts);
        Pageable pageable = PageRequest.of(0, 20);
        given(postRepository.findByUserId(userId, pageable)).willReturn(page);

        Page<Post> result = postService.findByUserId(userId, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(post -> post.getUserId().equals(userId));
        verify(postRepository).findByUserId(userId, pageable);
    }

    @Test
    @DisplayName("댓글 생성")
    void createComment() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        CommentCreateRequest request = CommentCreateRequest.builder().content("댓글").userId(2L).nickName("댓글러").build();
        Comment result = postService.createComment(POST_ID, request);

        assertThat(result.getUserId()).isEqualTo(2L);
        assertThat(result.getNickName()).isEqualTo("댓글러");
        assertThat(result.getContent()).isEqualTo("댓글");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("대댓글 생성")
    void createReply() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        Comment parent = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        Comment reply = Comment.builder().userId(3L).nickName("대댓글러").content("대댓글").post(post).parent(parent).build();
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));
        given(commentRepository.findById(COMMENT_ID)).willReturn(Optional.of(parent));
        given(commentRepository.save(any(Comment.class))).willReturn(reply);

        CommentCreateRequest request = CommentCreateRequest.builder().content("대댓글").parentId(COMMENT_ID).userId(3L).nickName("대댓글러").build();
        Comment result = postService.createComment(POST_ID, request);

        assertThat(result.getContent()).isEqualTo("대댓글");
        assertThat(result.getParent()).isEqualTo(parent);
    }

    @Test
    @DisplayName("댓글 목록 조회")
    void findCommentsByPostId() {
        List<Comment> comments = List.of(
                Comment.builder().content("댓글1").build(),
                Comment.builder().content("댓글2").build()
        );
        given(commentRepository.findByPostIdAndParentIsNull(POST_ID)).willReturn(comments);

        List<Comment> result = postService.findCommentsByPostId(POST_ID);

        assertThat(result).hasSize(2);
        verify(commentRepository).findByPostIdAndParentIsNull(POST_ID);
    }

    @Test
    @DisplayName("게시글 수정")
    void updatePost() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("OLD").build();
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));

        PostUpdateRequest request = PostUpdateRequest.builder().userId(1L).title("새제목").content("새내용").tag("NEW").build();
        postService.update(POST_ID, request);

        assertThat(post.getTitle()).isEqualTo("새제목");
        assertThat(post.getContent()).isEqualTo("새내용");
        assertThat(post.getUpdatedAt()).isNotNull();
        verify(postRepository).save(post);
    }

    @Test
    @DisplayName("게시글 수정 - 타인 게시글 수정 시 예외")
    void updatePostByOtherUserThrowsException() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("OLD").build();
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));

        PostUpdateRequest request = PostUpdateRequest.builder().userId(2L).title("새제목").content("새내용").tag("NEW").build();

        assertThatThrownBy(() -> postService.update(POST_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 게시글만 수정할 수 있습니다");
    }

    @Test
    @DisplayName("타인 게시글 삭제 시 예외")
    void validateOwnershipFailOnDelete() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").build();

        assertThatThrownBy(() -> postService.validateOwnership(post, 2L, "삭제"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 게시글만 삭제할 수 있습니다");
    }

    @Test
    @DisplayName("게시글 수정 적용")
    void applyUpdate() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("구제목").content("구내용").tag("OLD").build();
        PostUpdate postUpdate = new PostUpdate("새제목", "새내용", "NEW");

        postService.applyUpdate(post, postUpdate);

        assertThat(post.getTitle()).isEqualTo("새제목");
        assertThat(post.getContent()).isEqualTo("새내용");
        assertThat(post.getTag()).isEqualTo("NEW");
        assertThat(post.getUpdatedAt()).isNotNull();
    }
}
