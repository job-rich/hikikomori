package org.hikikomori.community.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.hikikomori.community.domain.Ban;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BanRepositoryImplTest {

    @Mock
    BanJpaRepository jpaRepository;

    @InjectMocks
    BanRepositoryImpl repository;

    @Test
    @DisplayName("userId 밴 여부를 확인한다")
    void 밴_여부를_확인한다() {
        given(jpaRepository.existsByUserId(7L)).willReturn(true);
        assertThat(repository.isBanned(7L)).isTrue();
    }

    @Test
    @DisplayName("밴을 저장한다")
    void 밴을_저장한다() {
        Ban ban = Ban.builder().userId(7L).reason("신고 누적").build();
        given(jpaRepository.save(ban)).willReturn(ban);
        assertThat(repository.save(ban)).isSameAs(ban);
    }
}
