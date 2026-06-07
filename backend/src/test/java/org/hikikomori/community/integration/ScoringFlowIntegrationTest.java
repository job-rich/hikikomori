package org.hikikomori.community.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.awaitility.Awaitility;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.domain.PostTag;
import org.hikikomori.community.domain.VoteTargetType;
import org.hikikomori.community.domain.VoteValue;
import org.hikikomori.community.dto.UserDto;
import org.hikikomori.community.dto.VoteDto;
import org.hikikomori.community.event.UserBannedEvent;
import org.hikikomori.community.facade.UserFacade;
import org.hikikomori.community.facade.VoteFacade;
import org.hikikomori.community.repository.PostJpaRepository;
import org.hikikomori.community.repository.UserJpaRepository;
import org.hikikomori.community.repository.UserRepositoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class ScoringFlowIntegrationTest {

    @Autowired VoteFacade voteFacade;
    @Autowired UserFacade userFacade;
    @Autowired UserRepositoryImpl userRepository;
    @Autowired UserJpaRepository userJpaRepository;
    @Autowired PostJpaRepository postJpaRepository;
    @Autowired org.hikikomori.community.repository.VoteJpaRepository voteJpaRepository;
    @Autowired org.hikikomori.community.repository.ReportJpaRepository reportJpaRepository;
    @Autowired CacheManager cacheManager;
    @Autowired ApplicationEventPublisher publisher;
    @Autowired TransactionTemplate txTemplate;

    @AfterEach
    void clean() {
        voteJpaRepository.deleteAll();
        reportJpaRepository.deleteAll();
        postJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        evictCaches();
    }

    private void evictCaches() {
        // 랭킹/프로필 캐시가 테스트 메서드 간 상태를 오염시키지 않도록 비운다
        if (cacheManager.getCache("ranking") != null) {
            cacheManager.getCache("ranking").clear();
        }
        if (cacheManager.getCache("profile") != null) {
            cacheManager.getCache("profile").clear();
        }
    }

    @Test
    @DisplayName("추천하면 작성자 전투력이 오른다")
    void 추천_전투력() {
        long author = 100L;
        userFacade.touch(author, "작성자");
        Post post = postJpaRepository.save(
                Post.builder().userId(author).nickName("작성자").title("t").content("c").tag(PostTag.ETC).build());

        voteFacade.vote(author, new VoteDto.CreateRequest(1L, VoteTargetType.POST, post.getId(), VoteValue.UP));
        voteFacade.vote(author, new VoteDto.CreateRequest(2L, VoteTargetType.POST, post.getId(), VoteValue.UP));

        UserDto.ProfileResponse profile = userFacade.getProfile(author);
        assertThat(profile.voteNet()).isEqualTo(2L);
        assertThat(profile.power()).isEqualTo(20L); // Wv=10 * 2
    }

    @Test
    @DisplayName("랭킹 조회는 네이티브 findRanking으로 power 내림차순 정렬되고 countQuery로 전체 인원을 센다")
    void 랭킹_네이티브쿼리() {
        // given: 유저 3명을 생성하고 각자 글을 작성한 뒤, 서로 다른 추천 수를 받게 한다
        // A: 순 추천 3 → power 30, B: 순 추천 2 → power 20, C: 순 추천 1 → power 10
        long userA = 200L;
        long userB = 201L;
        long userC = 202L;
        userFacade.touch(userA, "A");
        userFacade.touch(userB, "B");
        userFacade.touch(userC, "C");

        Post postA = savePost(userA, "A");
        Post postB = savePost(userB, "B");
        Post postC = savePost(userC, "C");

        // 자기추천 금지 + 한 voter는 같은 대상에 한 표만 → voter를 대상마다 분리한다
        upVote(userA, postA, 901L);
        upVote(userA, postA, 902L);
        upVote(userA, postA, 903L);

        upVote(userB, postB, 901L);
        upVote(userB, postB, 902L);

        upVote(userC, postC, 901L);

        evictCaches();

        // when
        Page<UserDto.RankingResponse> page = userFacade.getRanking(PageRequest.of(0, 10));

        // then: power 내림차순 정렬 — A > B > C
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getContent())
                .extracting(UserDto.RankingResponse::userId)
                .containsExactly(userA, userB, userC);
        assertThat(page.getContent())
                .extracting(UserDto.RankingResponse::power)
                .containsExactly(30L, 20L, 10L);

        // countQuery 검증 — 시드한 유저 수와 일치해야 한다(쿼리가 잘못되면 런타임 예외)
        assertThat(page.getTotalElements()).isEqualTo(3L);

        // 페이지네이션 검증 — 페이지 크기 2로 자르면 첫 페이지엔 A,B만 들어오고 총원/총페이지는 유지된다
        evictCaches();
        Page<UserDto.RankingResponse> firstPage = userFacade.getRanking(PageRequest.of(0, 2));
        assertThat(firstPage.getContent())
                .extracting(UserDto.RankingResponse::userId)
                .containsExactly(userA, userB);
        assertThat(firstPage.getTotalElements()).isEqualTo(3L);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("UserBannedEvent가 커밋되면 비동기 리스너가 User.banned를 true로 동기화한다")
    void 밴_역정규화_비동기동기화() {
        // given: 유저를 생성한다(아직 밴 아님)
        long userId = 300L;
        userFacade.touch(userId, "밴대상");
        assertThat(userRepository.findByUserId(userId).orElseThrow().isBanned()).isFalse();

        // when: AFTER_COMMIT 리스너가 동작하도록 커밋된 트랜잭션 안에서 이벤트를 발행한다
        txTemplate.executeWithoutResult(s -> publisher.publishEvent(new UserBannedEvent(userId)));

        // then: 비동기 리스너 → UserFacade.markBanned → User.banned = true (eventual)
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() ->
                        assertThat(userRepository.findByUserId(userId).orElseThrow().isBanned()).isTrue());
    }

    private Post savePost(long userId, String nick) {
        return postJpaRepository.save(
                Post.builder().userId(userId).nickName(nick).title("t").content("c").tag(PostTag.ETC).build());
    }

    private void upVote(long authorId, Post post, long voterId) {
        voteFacade.vote(authorId, new VoteDto.CreateRequest(voterId, VoteTargetType.POST, post.getId(), VoteValue.UP));
    }
}
