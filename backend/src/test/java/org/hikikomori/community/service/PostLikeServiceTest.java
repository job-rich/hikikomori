package org.hikikomori.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostLikeServiceTest {

    private final PostLikeService postLikeService = new PostLikeService();

    @Test
    @DisplayName("본인 게시글 좋아요 시 예외")
    void checkNotSelfLike() {
        assertThatThrownBy(() -> postLikeService.checkNotSelfLike(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 게시글에는 좋아요를 할 수 없습니다");
    }

    @Test
    @DisplayName("PostLike 엔티티 생성")
    void buildPostLike() {
        UUID postId = UUID.randomUUID();

        var like = postLikeService.buildPostLike(2L, postId);

        assertThat(like.getUserId()).isEqualTo(2L);
        assertThat(like.getPostId()).isEqualTo(postId);
        assertThat(like.getId()).isNotNull();
        assertThat(like.getCreatedAt()).isNotNull();
    }
}
