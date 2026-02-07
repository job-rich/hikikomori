package org.hikikomori.community.service;

import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.repository.CommentRepository;
import org.hikikomori.community.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
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

    @Test
    @DisplayName("게시글 생성")
    void create() {
        Post post = Post.builder().title("제목").content("내용").build();
        given(postRepository.save(any(Post.class))).willReturn(post);

        Post result = postService.create("제목", "내용");

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
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Post result = postService.findById(1L);

        assertThat(result.getTitle()).isEqualTo("제목");
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외")
    void findByIdNotFound() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.findById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("댓글 생성")
    void createComment() {
        Post post = Post.builder().title("제목").content("내용").build();
        Comment comment = Comment.builder().content("댓글").post(post).build();
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        Comment result = postService.createComment(1L, null, "댓글");

        assertThat(result.getContent()).isEqualTo("댓글");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("대댓글 생성")
    void createReply() {
        Post post = Post.builder().title("제목").content("내용").build();
        Comment parent = Comment.builder().content("댓글").post(post).build();
        Comment reply = Comment.builder().content("대댓글").post(post).parent(parent).build();
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(commentRepository.findById(1L)).willReturn(Optional.of(parent));
        given(commentRepository.save(any(Comment.class))).willReturn(reply);

        Comment result = postService.createComment(1L, 1L, "대댓글");

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
        given(commentRepository.findByPostIdAndParentIsNull(1L)).willReturn(comments);

        List<Comment> result = postService.findCommentsByPostId(1L);

        assertThat(result).hasSize(2);
        verify(commentRepository).findByPostIdAndParentIsNull(1L);
    }

    @Test
    @DisplayName("대댓글에 답글 달기 시 예외")
    void createReplyToReplyThrowsException() {
        Post post = Post.builder().title("제목").content("내용").build();
        Comment parent = Comment.builder().content("댓글").post(post).build();
        Comment reply = Comment.builder().content("대댓글").post(post).parent(parent).build();
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(commentRepository.findById(2L)).willReturn(Optional.of(reply));

        assertThatThrownBy(() -> postService.createComment(1L, 2L, "대대댓글"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("대댓글에는 답글을 달 수 없습니다");
    }
}
