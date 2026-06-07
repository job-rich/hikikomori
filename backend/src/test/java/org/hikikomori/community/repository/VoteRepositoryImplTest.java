package org.hikikomori.community.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.UUID;
import org.hikikomori.community.domain.VoteTargetType;
import org.hikikomori.community.domain.VoteValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VoteRepositoryImplTest {

    @Mock VoteJpaRepository jpaRepository;
    @InjectMocks VoteRepositoryImpl repository;

    @Test
    @DisplayName("작성자 받은 순추천을 위임 집계한다")
    void 순추천_집계() {
        given(jpaRepository.netByTargetUser(2L)).willReturn(5L);
        assertThat(repository.netByTargetUser(2L)).isEqualTo(5L);
    }

    @Test
    @DisplayName("콘텐츠 순추천을 위임 집계한다")
    void 콘텐츠_순추천() {
        UUID id = UUID.randomUUID();
        given(jpaRepository.netByContent(VoteTargetType.POST, id)).willReturn(2L);
        assertThat(repository.netByContent(VoteTargetType.POST, id)).isEqualTo(2L);
    }

    @Test
    @DisplayName("내 현재 표 보유 여부 = 해당 value 델타합 > 0")
    void 내표_여부() {
        UUID id = UUID.randomUUID();
        given(jpaRepository.sumDeltaByVoterTargetValue(1L, VoteTargetType.POST, id, VoteValue.UP)).willReturn(1L);
        assertThat(repository.has(1L, VoteTargetType.POST, id, VoteValue.UP)).isTrue();
    }
}
