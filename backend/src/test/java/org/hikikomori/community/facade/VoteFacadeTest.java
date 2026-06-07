package org.hikikomori.community.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.domain.Vote;
import org.hikikomori.community.domain.VoteTargetType;
import org.hikikomori.community.domain.VoteValue;
import org.hikikomori.community.dto.VoteDto;
import org.hikikomori.community.repository.CommentRepositoryImpl;
import org.hikikomori.community.repository.PostRepositoryImpl;
import org.hikikomori.community.repository.VoteRepositoryImpl;
import org.hikikomori.community.service.VoteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VoteFacadeTest {

    @Mock VoteRepositoryImpl voteRepository;
    @Mock PostRepositoryImpl postRepository;
    @Mock CommentRepositoryImpl commentRepository;
    VoteService voteService = new VoteService();

    VoteFacade facade() {
        return new VoteFacade(voteService, voteRepository, postRepository, commentRepository);
    }

    private Post postBy(Long author) {
        return Post.builder().userId(author).nickName("n").title("t").content("c").tag(null).build();
    }

    private VoteDto.CreateRequest req(UUID t, VoteValue v) {
        return new VoteDto.CreateRequest(1L, VoteTargetType.POST, t, v);
    }

    @Test
    @DisplayName("미보유 상태 추천 → 1행 저장, 내표 UP")
    void 신규() {
        UUID t = UUID.randomUUID();
        given(postRepository.getById(t)).willReturn(postBy(2L));
        given(voteRepository.has(1L, VoteTargetType.POST, t, VoteValue.UP)).willReturn(false);
        given(voteRepository.has(1L, VoteTargetType.POST, t, VoteValue.DOWN)).willReturn(false);
        given(voteRepository.netByContent(VoteTargetType.POST, t)).willReturn(1L);

        VoteDto.Response res = facade().vote(2L, req(t, VoteValue.UP));

        verify(voteRepository, times(1)).save(any(Vote.class));
        assertThat(res.value()).isEqualTo(VoteValue.UP);
        assertThat(res.score()).isEqualTo(1L);
    }

    @Test
    @DisplayName("추천 보유 상태 추천 → 취소(-1행), 내표 null")
    void 취소() {
        UUID t = UUID.randomUUID();
        given(postRepository.getById(t)).willReturn(postBy(2L));
        given(voteRepository.has(1L, VoteTargetType.POST, t, VoteValue.UP)).willReturn(true);
        given(voteRepository.has(1L, VoteTargetType.POST, t, VoteValue.DOWN)).willReturn(false);
        given(voteRepository.netByContent(VoteTargetType.POST, t)).willReturn(0L);

        VoteDto.Response res = facade().vote(2L, req(t, VoteValue.UP));

        verify(voteRepository, times(1)).save(any(Vote.class));
        assertThat(res.value()).isNull();
    }

    @Test
    @DisplayName("비추천 보유 상태 추천 → 2행(UP+1, DOWN-1), 내표 UP")
    void 스위치() {
        UUID t = UUID.randomUUID();
        given(postRepository.getById(t)).willReturn(postBy(2L));
        given(voteRepository.has(1L, VoteTargetType.POST, t, VoteValue.UP)).willReturn(false);
        given(voteRepository.has(1L, VoteTargetType.POST, t, VoteValue.DOWN)).willReturn(true);
        given(voteRepository.netByContent(VoteTargetType.POST, t)).willReturn(1L);

        VoteDto.Response res = facade().vote(2L, req(t, VoteValue.UP));

        verify(voteRepository, times(2)).save(any(Vote.class));
        assertThat(res.value()).isEqualTo(VoteValue.UP);
    }
}
