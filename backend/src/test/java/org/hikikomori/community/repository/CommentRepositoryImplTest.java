package org.hikikomori.community.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentRepositoryImplTest {

    @Mock
    private CommentJpaRepository jpaRepository;

    @InjectMocks
    private CommentRepositoryImpl commentRepository;

    @Test
    @DisplayName("존재하는 ID로 댓글 조회 시 댓글 반환")
    void getByIdWithExistingId() {
        // given
        UUID id = UUID.randomUUID();
        Comment comment = Comment.builder().userId(1L).nickName("댓글러").content("댓글").build();
        given(jpaRepository.findById(id)).willReturn(Optional.of(comment));

        // when
        Comment result = commentRepository.getById(id);

        // then
        assertThat(result).isEqualTo(comment);
        verify(jpaRepository).findById(id);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 댓글 조회 시 예외 발생")
    void getByIdWithNonExistingId() {
        // given
        UUID id = UUID.randomUUID();
        given(jpaRepository.findById(id)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentRepository.getById(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("댓글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("존재하는 ID로 부모 댓글 조회 시 댓글 반환")
    void getParentByIdWithExistingId() {
        // given
        UUID parentId = UUID.randomUUID();
        Comment parent = Comment.builder().userId(1L).nickName("댓글러").content("부모 댓글").build();
        given(jpaRepository.findById(parentId)).willReturn(Optional.of(parent));

        // when
        Comment result = commentRepository.getParentById(parentId);

        // then
        assertThat(result).isEqualTo(parent);
        verify(jpaRepository).findById(parentId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 부모 댓글 조회 시 예외 발생")
    void getParentByIdWithNonExistingId() {
        // given
        UUID parentId = UUID.randomUUID();
        given(jpaRepository.findById(parentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentRepository.getParentById(parentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("부모 댓글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("댓글 저장 위임")
    void saveDelegatesToJpaRepository() {
        // given
        Comment comment = Comment.builder().userId(1L).nickName("댓글러").content("댓글").build();
        given(jpaRepository.save(comment)).willReturn(comment);

        // when
        Comment result = commentRepository.save(comment);

        // then
        assertThat(result).isEqualTo(comment);
        verify(jpaRepository).save(comment);
    }

    @Test
    @DisplayName("게시글 ID로 루트 댓글 목록 조회 위임")
    void findByPostIdAndParentIsNullDelegatesToJpaRepository() {
        // given
        UUID postId = UUID.randomUUID();
        Post post = Post.builder().userId(1L).nickName("작성자").title("제목").content("내용").build();
        List<Comment> comments = List.of(
                Comment.builder().userId(2L).nickName("댓글러1").content("댓글1").post(post).build(),
                Comment.builder().userId(3L).nickName("댓글러2").content("댓글2").post(post).build()
        );
        given(jpaRepository.findByPostIdAndParentIsNull(postId)).willReturn(comments);

        // when
        List<Comment> result = commentRepository.findByPostIdAndParentIsNull(postId);

        // then
        assertThat(result).hasSize(2);
        verify(jpaRepository).findByPostIdAndParentIsNull(postId);
    }

    @Test
    @DisplayName("게시글 ID로 댓글 전체 삭제 위임")
    void deleteAllByPostIdDelegatesToJpaRepository() {
        // given
        UUID postId = UUID.randomUUID();

        // when
        commentRepository.deleteAllByPostId(postId);

        // then
        verify(jpaRepository).deleteAllByPostId(postId);
    }

    @Test
    @DisplayName("기간별 댓글 삭제 위임")
    void deleteByCreatedAtBetweenDelegatesToJpaRepository() {
        // given
        LocalDateTime startAt = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime endAt = LocalDate.now().atStartOfDay();
        given(jpaRepository.deleteByCreatedAtBetween(startAt, endAt)).willReturn(5L);

        // when
        long result = commentRepository.deleteByCreatedAtBetween(startAt, endAt);

        // then
        assertThat(result).isEqualTo(5L);
        verify(jpaRepository).deleteByCreatedAtBetween(startAt, endAt);
    }
}
