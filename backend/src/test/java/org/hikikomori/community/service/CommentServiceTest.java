package org.hikikomori.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.dto.CommentDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommentServiceTest {

    private final CommentService commentService = new CommentService();

    @Test
    @DisplayName("댓글 엔티티 생성")
    void buildComment() {
        Post post = Post.builder().userId(1L).nickName("작성자").title("제목").content("내용").build();
        CommentDto.CreateRequest request = new CommentDto.CreateRequest("댓글", null, 2L, "댓글러");

        Comment comment = commentService.buildComment(request, post, null);

        assertThat(comment.getUserId()).isEqualTo(2L);
        assertThat(comment.getNickName()).isEqualTo("댓글러");
        assertThat(comment.getContent()).isEqualTo("댓글");
        assertThat(comment.getPost()).isEqualTo(post);
        assertThat(comment.getParent()).isNull();
        assertThat(comment.getId()).isNotNull();
    }

    @Test
    @DisplayName("대댓글 엔티티 생성")
    void buildReply() {
        Post post = Post.builder().userId(1L).nickName("작성자").title("제목").content("내용").build();
        Comment parent = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        CommentDto.CreateRequest request = new CommentDto.CreateRequest("대댓글", null, 3L, "대댓글러");

        Comment reply = commentService.buildComment(request, post, parent);

        assertThat(reply.getParent()).isEqualTo(parent);
    }

    @Test
    @DisplayName("댓글 소유자 검증 통과")
    void checkOwnershipSuccess() {
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").build();

        assertThatCode(() -> commentService.checkOwnership(comment, 2L, "수정"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("타인 댓글 수정 시 예외")
    void checkOwnershipFailOnUpdate() {
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").build();

        assertThatThrownBy(() -> commentService.checkOwnership(comment, 3L, "수정"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 댓글만 수정할 수 있습니다");
    }

    @Test
    @DisplayName("타인 댓글 삭제 시 예외")
    void checkOwnershipFailOnDelete() {
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").build();

        assertThatThrownBy(() -> commentService.checkOwnership(comment, 3L, "삭제"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 댓글만 삭제할 수 있습니다");
    }

    @Test
    @DisplayName("루트 댓글에 대댓글 허용")
    void checkNestingDepthRootComment() {
        Comment parent = Comment.builder().userId(2L).nickName("댓글러").content("댓글").build();

        assertThatCode(() -> commentService.checkNestingDepth(parent))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("null 부모 허용 (루트 댓글 생성)")
    void checkNestingDepthNullParent() {
        assertThatCode(() -> commentService.checkNestingDepth(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("대댓글에 답글 시 예외")
    void checkNestingDepthReplyToReply() {
        Post post = Post.builder().userId(1L).nickName("작성자").title("제목").content("내용").build();
        Comment parent = Comment.builder().userId(2L).nickName("댓글러").content("댓글").post(post).build();
        Comment reply = Comment.builder().userId(3L).nickName("대댓글러").content("대댓글").post(post).parent(parent).build();

        assertThatThrownBy(() -> commentService.checkNestingDepth(reply))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("대댓글에는 답글을 달 수 없습니다");
    }

    @Test
    @DisplayName("댓글 내용 수정")
    void applyUpdate() {
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("원본").build();

        commentService.applyUpdate(comment, "수정됨");

        assertThat(comment.getContent()).isEqualTo("수정됨");
        assertThat(comment.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("댓글 소프트 삭제")
    void applySoftDelete() {
        Comment comment = Comment.builder().userId(2L).nickName("댓글러").content("댓글").build();

        commentService.applySoftDelete(comment);

        assertThat(comment.getDeletedAt()).isNotNull();
        assertThat(comment.getContent()).isNotEqualTo("댓글");
    }
}
