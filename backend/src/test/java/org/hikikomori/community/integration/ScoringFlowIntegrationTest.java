package org.hikikomori.community.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.hikikomori.community.domain.Post;
import org.hikikomori.community.domain.PostTag;
import org.hikikomori.community.domain.VoteTargetType;
import org.hikikomori.community.domain.VoteValue;
import org.hikikomori.community.dto.UserDto;
import org.hikikomori.community.dto.VoteDto;
import org.hikikomori.community.facade.UserFacade;
import org.hikikomori.community.facade.VoteFacade;
import org.hikikomori.community.repository.PostJpaRepository;
import org.hikikomori.community.repository.UserRepositoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ScoringFlowIntegrationTest {

    @Autowired VoteFacade voteFacade;
    @Autowired UserFacade userFacade;
    @Autowired UserRepositoryImpl userRepository;
    @Autowired PostJpaRepository postJpaRepository;
    @Autowired org.hikikomori.community.repository.VoteJpaRepository voteJpaRepository;

    @AfterEach
    void clean() {
        voteJpaRepository.deleteAll();
        postJpaRepository.deleteAll();
        userRepository.findByUserId(100L).ifPresent(u -> {}); // no-op
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
}
