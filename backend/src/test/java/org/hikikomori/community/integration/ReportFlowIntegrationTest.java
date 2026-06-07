package org.hikikomori.community.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.UUID;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.domain.PostTag;
import org.hikikomori.community.domain.ReportReason;
import org.hikikomori.community.domain.ReportTargetType;
import org.hikikomori.community.dto.PostDto;
import org.hikikomori.community.dto.ReportDto;
import org.hikikomori.community.exception.BannedUserException;
import org.hikikomori.community.facade.PostFacade;
import org.hikikomori.community.facade.ReportFacade;
import org.hikikomori.community.repository.BanJpaRepository;
import org.hikikomori.community.repository.BanRepositoryImpl;
import org.hikikomori.community.repository.CommentJpaRepository;
import org.hikikomori.community.repository.PostJpaRepository;
import org.hikikomori.community.repository.ReportJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 신고 → 비동기 판정(이벤트 리스너) → 숨김/밴까지의 전체 흐름을 실제 스택으로 검증한다.
 * 트랜잭션 롤백을 쓰지 않는다(AFTER_COMMIT 이벤트가 발화해야 하므로). 각 테스트 후 직접 정리한다.
 */
@SpringBootTest
class ReportFlowIntegrationTest {

    @Autowired ReportFacade reportFacade;
    @Autowired PostFacade postFacade;
    @Autowired BanRepositoryImpl banRepository;
    @Autowired PostJpaRepository postJpaRepository;
    @Autowired ReportJpaRepository reportJpaRepository;
    @Autowired BanJpaRepository banJpaRepository;
    @Autowired CommentJpaRepository commentJpaRepository;

    @AfterEach
    void cleanUp() {
        reportJpaRepository.deleteAll();
        banJpaRepository.deleteAll();
        commentJpaRepository.deleteAll();
        postJpaRepository.deleteAll();
    }

    private UUID createPost(long authorId, String title) {
        Post post = Post.builder()
                .userId(authorId).nickName("작성자" + authorId)
                .title(title).content("내용").tag(PostTag.ETC)
                .build();
        return postJpaRepository.save(post).getId();
    }

    private void reportBy(long authorId, UUID postId, long reporterId, String ip) {
        reportFacade.report(authorId,
                new ReportDto.CreateRequest(reporterId, ReportTargetType.POST, postId, ReportReason.SPAM, null),
                ip);
    }

    @Test
    @DisplayName("게시글이 서로 다른 신고자 5명에게 신고되면 비동기 판정으로 숨김 처리된다")
    void 신고_5명이면_숨김() {
        long author = 100L;
        UUID postId = createPost(author, "신고당할 글");

        for (int i = 1; i <= 5; i++) {
            reportBy(author, postId, i, "10.0.0." + i);
        }

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(postJpaRepository.findById(postId))
                        .get()
                        .extracting(Post::getHiddenAt)
                        .isNotNull());
    }

    @Test
    @DisplayName("신고자 4명까지는 숨겨지지 않는다")
    void 신고_4명이면_유지() {
        long author = 101L;
        UUID postId = createPost(author, "아슬아슬한 글");

        for (int i = 1; i <= 4; i++) {
            reportBy(author, postId, i, "10.0.1." + i);
        }

        // 비동기 판정이 끝날 시간을 준 뒤에도 숨김 아님
        await().during(Duration.ofMillis(800)).atMost(Duration.ofSeconds(3)).untilAsserted(() ->
                assertThat(postJpaRepository.findById(postId).orElseThrow().getHiddenAt()).isNull());
    }

    @Test
    @DisplayName("숨김 게시글이 5개 누적된 작성자는 밴되고 작성이 차단된다")
    void 숨김_5개면_밴되고_작성차단() {
        long author = 200L;
        for (int p = 0; p < 5; p++) {
            UUID postId = createPost(author, "글" + p);
            for (int r = 1; r <= 5; r++) {
                reportBy(author, postId, r, "10.1." + p + "." + r);
            }
        }

        await().atMost(Duration.ofSeconds(8)).untilAsserted(() ->
                assertThat(banRepository.isBanned(author)).isTrue());

        assertThatThrownBy(() -> postFacade.createPost(
                new PostDto.CreateRequest("새 글", "내용", PostTag.ETC, author, "작성자200")))
                .isInstanceOf(BannedUserException.class);
    }
}
