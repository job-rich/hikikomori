package org.hikikomori.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.hikikomori.community.domain.Vote;
import org.hikikomori.community.domain.VoteTargetType;
import org.hikikomori.community.domain.VoteValue;
import org.hikikomori.community.dto.VoteDto;
import org.hikikomori.community.exception.SelfReportException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VoteServiceTest {

    VoteService service = new VoteService();

    @Test
    @DisplayName("자기 콘텐츠 추천은 거부한다")
    void 자기추천_거부() {
        assertThatThrownBy(() -> service.checkNotSelfVote(5L, 5L))
                .isInstanceOf(SelfReportException.class);
        assertThatCode(() -> service.checkNotSelfVote(5L, 6L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("대상 작성자가 경로 userId와 다르면 거부한다")
    void 작성자_불일치_거부() {
        assertThatThrownBy(() -> service.checkTargetAuthor(99L, 2L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> service.checkTargetAuthor(2L, 2L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("resolveActions: 미보유 상태 추천 → (UP,+1)")
    void 추천_신규() {
        assertThat(service.resolveActions(false, false, VoteValue.UP))
                .containsExactly(new VoteService.VoteAction(VoteValue.UP, 1));
    }

    @Test
    @DisplayName("resolveActions: 추천 보유 상태 추천 → (UP,−1) 취소")
    void 추천_취소() {
        assertThat(service.resolveActions(true, false, VoteValue.UP))
                .containsExactly(new VoteService.VoteAction(VoteValue.UP, -1));
    }

    @Test
    @DisplayName("resolveActions: 비추천 보유 상태 추천 → (UP,+1)+(DOWN,−1) 스위치")
    void 비추천에서_추천() {
        assertThat(service.resolveActions(false, true, VoteValue.UP))
                .containsExactly(
                        new VoteService.VoteAction(VoteValue.UP, 1),
                        new VoteService.VoteAction(VoteValue.DOWN, -1));
    }

    @Test
    @DisplayName("resultVote: 추천 보유 상태에서 추천 클릭 → null(취소)")
    void 결과표() {
        assertThat(service.resultVote(true, false, VoteValue.UP)).isNull();
        assertThat(service.resultVote(false, false, VoteValue.UP)).isEqualTo(VoteValue.UP);
        assertThat(service.resultVote(false, true, VoteValue.UP)).isEqualTo(VoteValue.UP);
    }

    @Test
    @DisplayName("buildVote: 액션대로 Vote 생성")
    void buildVote() {
        UUID t = UUID.randomUUID();
        VoteDto.CreateRequest req = new VoteDto.CreateRequest(1L, VoteTargetType.POST, t, VoteValue.UP);
        Vote v = service.buildVote(2L, req, new VoteService.VoteAction(VoteValue.UP, 1));
        assertThat(v.getVoterId()).isEqualTo(1L);
        assertThat(v.getTargetUserId()).isEqualTo(2L);
        assertThat(v.getValue()).isEqualTo(VoteValue.UP);
        assertThat(v.getDelta()).isEqualTo(1);
    }
}
