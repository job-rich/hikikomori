package org.hikikomori.community.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hikikomori.community.domain.Post;
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
class PostRepositoryImplTest {

    @Mock
    private PostJpaRepository jpaRepository;

    @InjectMocks
    private PostRepositoryImpl postRepository;

    @Test
    @DisplayName("존재하는 ID로 게시글 조회 시 게시글 반환")
    void getByIdWithExistingId() {
        // given
        UUID id = UUID.randomUUID();
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").build();
        given(jpaRepository.findById(id)).willReturn(Optional.of(post));

        // when
        Post result = postRepository.getById(id);

        // then
        assertThat(result).isEqualTo(post);
        verify(jpaRepository).findById(id);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 게시글 조회 시 예외 발생")
    void getByIdWithNonExistingId() {
        // given
        UUID id = UUID.randomUUID();
        given(jpaRepository.findById(id)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postRepository.getById(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("게시글 저장 위임")
    void saveDelegatesToJpaRepository() {
        // given
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").build();
        given(jpaRepository.save(post)).willReturn(post);

        // when
        Post result = postRepository.save(post);

        // then
        assertThat(result).isEqualTo(post);
        verify(jpaRepository).save(post);
    }

    @Test
    @DisplayName("게시글 전체 조회 위임")
    void findAllDelegatesToJpaRepository() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Post> posts = List.of(
                Post.builder().title("제목1").content("내용1").build(),
                Post.builder().title("제목2").content("내용2").build()
        );
        Page<Post> page = new PageImpl<>(posts);
        given(jpaRepository.findAll(pageable)).willReturn(page);

        // when
        Page<Post> result = postRepository.findAll(pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        verify(jpaRepository).findAll(pageable);
    }

    @Test
    @DisplayName("사용자 ID로 게시글 조회 위임")
    void findByUserIdDelegatesToJpaRepository() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Post> posts = List.of(
                Post.builder().userId(userId).title("제목1").content("내용1").build()
        );
        Page<Post> page = new PageImpl<>(posts);
        given(jpaRepository.findByUserId(userId, pageable)).willReturn(page);

        // when
        Page<Post> result = postRepository.findByUserId(userId, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(jpaRepository).findByUserId(userId, pageable);
    }

    @Test
    @DisplayName("게시글 삭제 위임")
    void deleteByIdDelegatesToJpaRepository() {
        // given
        UUID id = UUID.randomUUID();

        // when
        postRepository.deleteById(id);

        // then
        verify(jpaRepository).deleteById(id);
    }
}
