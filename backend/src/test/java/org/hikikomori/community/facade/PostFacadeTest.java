package org.hikikomori.community.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PostFacadeTest {

    @InjectMocks
    private PostFacade postFacade;

    @Spy
    private PostService postService = new PostService();

    @Spy
    private CommentService commentService = new CommentService();

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    private static final UUID POST_ID = UUID.randomUUID();
    private static final UUID COMMENT_ID = UUID.randomUUID();
    private static final UUID REPLY_ID = UUID.randomUUID();

    // === Post ===

    @Test
    @DisplayName("게시글 생성")
    void createPost() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        given(postRepository.save(any(Post.class))).willReturn(post);

        PostCreateRequest request = new PostCreateRequest("제목", "내용", "VOID", 1L, "테스터");
        PostResponse result = postFacade.createPost(request);

        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.nickName()).isEqualTo("테스터");
        assertThat(result.title()).isEqualTo("제목");
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 목록 조회 - 페이징")
    void findAllPosts() {
        List<Post> posts = List.of(
                Post.builder().title("제목1").content("내용1").build(),
                Post.builder().title("제목2").content("내용2").build()
        );
        Page<Post> page = new PageImpl<>(posts);
        Pageable pageable = PageRequest.of(0, 10);
        given(postRepository.findAll(pageable)).willReturn(page);

        Page<PostResponse> result = postFacade.findAllPosts(pageable);

        assertThat(result.getContent()).hasSize(2);
        verify(postRepository).findAll(pageable);
    }

    @Test
    @DisplayName("게시글 단건 조회")
    void findPostById() {
        Post post = Post.builder().title("제목").content("내용").build();
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));

        PostResponse result = postFacade.findPostById(POST_ID);

        assertThat(result.title()).isEqualTo("제목");
        verify(postRepository).findById(POST_ID);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외")
    void findPostByIdNotFound() {
        UUID notFoundId = UUID.randomUUID();
        given(postRepository.findById(notFoundId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postFacade.findPostById(notFoundId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("게시글 수정")
    void updatePost() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("OLD").build();
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));

        PostUpdateRequest request = new PostUpdateRequest(1L, "새제목", "새내용", "NEW");
        postFacade.updatePost(POST_ID, request);

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

        PostUpdateRequest request = new PostUpdateRequest(2L, "새제목", "새내용", "NEW");

        assertThatThrownBy(() -> postFacade.updatePost(POST_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 게시글만 수정할 수 있습니다");
    }

    @Test
    @DisplayName("게시글 삭제")
    void deletePost() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));

        postFacade.deletePost(POST_ID, 1L);

        verify(commentRepository).deleteAllByPostId(POST_ID);
        verify(postRepository).deleteById(POST_ID);
    }

    @Test
    @DisplayName("게시글 삭제 - 타인 게시글 삭제 시 예외")
    void deletePostByOtherUserThrowsException() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> postFacade.deletePost(POST_ID, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 게시글만 삭제할 수 있습니다");
    }

    // === Comment ===

    @Test
    @DisplayName("댓글 목록 조회")
    void findCommentsByPostId() {
        List<Comment> comments = List.of(
                Comment.builder().content("댓글1").build(),
                Comment.builder().content("댓글2").build()
        );
        given(commentRepository.findByPostIdAndParentIsNull(POST_ID)).willReturn(comments);

        List<CommentResponse> result = postFacade.findCommentsByPostId(POST_ID);

        assertThat(result).hasSize(2);
        verify(commentRepository).findByPostIdAndParentIsNull(POST_ID);
    }

    @Test
    @DisplayName("댓글 생성")
    void createComment() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        CommentCreateRequest request = new CommentCreateRequest("댓글", null, 2L, "댓글러");
        CommentResponse result = postFacade.createComment(POST_ID, request);

        assertThat(result.userId()).isEqualTo(2L);
        assertThat(result.nickName()).isEqualTo("댓글러");
        assertThat(result.content()).isEqualTo("댓글");
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

        CommentCreateRequest request = new CommentCreateRequest("대댓글", COMMENT_ID, 3L, "대댓글러");
        CommentResponse result = postFacade.createComment(POST_ID, request);

        assertThat(result.content()).isEqualTo("대댓글");
    }

    @Test
    @DisplayName("대댓글에 답글 달기 시 예외")
    void createReplyToReplyThrowsException() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        Comment parent = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        Comment reply = Comment.builder().userId(3L).nickName("대댓글러").content("대댓글").post(post).parent(parent).build();
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));
        given(commentRepository.findById(REPLY_ID)).willReturn(Optional.of(reply));

        CommentCreateRequest request = new CommentCreateRequest("대대댓글", REPLY_ID, 4L, "대대댓글러");

        assertThatThrownBy(() -> postFacade.createComment(POST_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("대댓글에는 답글을 달 수 없습니다");
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 댓글 생성 시 예외")
    void createCommentOnNonExistentPost() {
        UUID notFoundId = UUID.randomUUID();
        given(postRepository.findById(notFoundId)).willReturn(Optional.empty());

        CommentCreateRequest request = new CommentCreateRequest("댓글", null, 2L, "댓글러");

        assertThatThrownBy(() -> postFacade.createComment(notFoundId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("댓글 수정")
    void updateComment() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        given(commentRepository.findById(COMMENT_ID)).willReturn(Optional.of(comment));

        CommentUpdateRequest request = new CommentUpdateRequest(2L, "수정된 댓글");
        postFacade.updateComment(COMMENT_ID, request);

        assertThat(comment.getContent()).isEqualTo("수정된 댓글");
        assertThat(comment.getUpdatedAt()).isNotNull();
        verify(commentRepository).save(comment);
    }

    @Test
    @DisplayName("댓글 수정 - 타인 댓글 수정 시 예외")
    void updateCommentByOtherUserThrowsException() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        given(commentRepository.findById(COMMENT_ID)).willReturn(Optional.of(comment));

        CommentUpdateRequest request = new CommentUpdateRequest(3L, "수정된 댓글");

        assertThatThrownBy(() -> postFacade.updateComment(COMMENT_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 댓글만 수정할 수 있습니다");
    }

    @Test
    @DisplayName("댓글 삭제 - 소프트 삭제로 deletedAt 설정 및 내용 대체")
    void deleteComment() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        given(commentRepository.findById(COMMENT_ID)).willReturn(Optional.of(comment));

        postFacade.deleteComment(COMMENT_ID, 2L);

        assertThat(comment.getDeletedAt()).isNotNull();
        assertThat(comment.getContent()).isNotBlank();
        verify(commentRepository).save(comment);
    }

    @Test
    @DisplayName("댓글 삭제 - 타인 댓글 삭제 시 예외")
    void deleteCommentByOtherUserThrowsException() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").tag("VOID").build();
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        given(commentRepository.findById(COMMENT_ID)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> postFacade.deleteComment(COMMENT_ID, 3L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 댓글만 삭제할 수 있습니다");
    }
}
