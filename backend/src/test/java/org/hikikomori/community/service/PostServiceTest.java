package org.hikikomori.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hikikomori.community.controller.data.CommentCreateRequest;
import org.hikikomori.community.controller.data.PostCreateRequest;
import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.repository.CommentRepository;
import org.hikikomori.community.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    private static final UUID POST_ID = UUID.randomUUID();
    private static final UUID COMMENT_ID = UUID.randomUUID();
    private static final UUID REPLY_ID = UUID.randomUUID();

    @Test
    @DisplayName("게시글 생성")
    void create() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").build();
        given(postRepository.save(any(Post.class))).willReturn(post);

        PostCreateRequest request = new PostCreateRequest("제목", "내용", 1L, "테스터");
        Post result = postService.create(request);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getNickName()).isEqualTo("테스터");
        assertThat(result.getTitle()).isEqualTo("제목");
        assertThat(result.getContent()).isEqualTo("내용");
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 목록 조회 - 페이징")
    void findAll() {
        List<Post> posts = List.of(
                Post.builder().title("제목1").content("내용1").build(),
                Post.builder().title("제목2").content("내용2").build()
        );
        Page<Post> page = new PageImpl<>(posts);
        Pageable pageable = PageRequest.of(0, 10);
        given(postRepository.findAll(pageable)).willReturn(page);

        Page<Post> result = postService.findAll(pageable);

        assertThat(result.getContent()).hasSize(2);
        verify(postRepository).findAll(pageable);
    }

    @Test
    @DisplayName("게시글 단건 조회")
    void findById() {
        Post post = Post.builder().title("제목").content("내용").build();
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));

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
    @DisplayName("댓글 생성")
    void createComment() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").build();
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        CommentCreateRequest request = new CommentCreateRequest("댓글", null, 2L, "댓글러");
        Comment result = postService.createComment(POST_ID, request);

        assertThat(result.getUserId()).isEqualTo(2L);
        assertThat(result.getNickName()).isEqualTo("댓글러");
        assertThat(result.getContent()).isEqualTo("댓글");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("대댓글 생성")
    void createReply() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").build();
        Comment parent = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        Comment reply = Comment.builder().userId(3L).nickName("대댓글러").content("대댓글").post(post).parent(parent).build();
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));
        given(commentRepository.findById(COMMENT_ID)).willReturn(Optional.of(parent));
        given(commentRepository.save(any(Comment.class))).willReturn(reply);

        CommentCreateRequest request = new CommentCreateRequest("대댓글", COMMENT_ID, 3L, "대댓글러");
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
    @DisplayName("대댓글에 답글 달기 시 예외")
    void createReplyToReplyThrowsException() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").build();
        Comment parent = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        Comment reply = Comment.builder().userId(3L).nickName("대댓글러").content("대댓글").post(post).parent(parent).build();
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));
        given(commentRepository.findById(REPLY_ID)).willReturn(Optional.of(reply));

        CommentCreateRequest request = new CommentCreateRequest("대대댓글", REPLY_ID, 4L, "대대댓글러");
        assertThatThrownBy(() -> postService.createComment(POST_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("대댓글에는 답글을 달 수 없습니다");
    }
}
