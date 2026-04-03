package org.hikikomori.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.hikikomori.community.domain.Post;
import org.hikikomori.community.domain.PostTag;
import org.hikikomori.community.dto.PostDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostServiceTest {

    private final PostService postService = new PostService();

    @Test
    @DisplayName("게시글 엔티티 생성")
    void buildPost() {
        PostDto.CreateRequest request = new PostDto.CreateRequest("제목", "내용", PostTag.PHILOSOPHY, 1L, "테스터");

        Post post = postService.buildPost(request);

        assertThat(post.getUserId()).isEqualTo(1L);
        assertThat(post.getNickName()).isEqualTo("테스터");
        assertThat(post.getTitle()).isEqualTo("제목");
        assertThat(post.getContent()).isEqualTo("내용");
        assertThat(post.getTag()).isEqualTo(PostTag.PHILOSOPHY);
        assertThat(post.getId()).isNotNull();
        assertThat(post.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("게시글 소유자 검증 통과")
    void checkOwnershipSuccess() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").build();

        assertThatCode(() -> postService.checkOwnership(post, 1L, "수정"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("타인 게시글 수정 시 예외")
    void checkOwnershipFailOnUpdate() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").build();

        assertThatThrownBy(() -> postService.checkOwnership(post, 2L, "수정"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 게시글만 수정할 수 있습니다");
    }

    @Test
    @DisplayName("타인 게시글 삭제 시 예외")
    void checkOwnershipFailOnDelete() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("제목").content("내용").build();

        assertThatThrownBy(() -> postService.checkOwnership(post, 2L, "삭제"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 게시글만 삭제할 수 있습니다");
    }

    @Test
    @DisplayName("게시글 수정 적용")
    void applyUpdate() {
        Post post = Post.builder().userId(1L).nickName("테스터").title("구제목").content("구내용").tag(PostTag.ETC).build();
        PostDto.UpdateRequest request = new PostDto.UpdateRequest(1L, "새제목", "새내용", PostTag.DAILY);

        postService.applyUpdate(post, request);

        assertThat(post.getTitle()).isEqualTo("새제목");
        assertThat(post.getContent()).isEqualTo("새내용");
        assertThat(post.getTag()).isEqualTo(PostTag.DAILY);
        assertThat(post.getUpdatedAt()).isNotNull();
    }
}
