package org.hikikomori.community.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.hikikomori.community.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @Mock UserJpaRepository jpaRepository;
    @InjectMocks UserRepositoryImpl repository;

    @Test
    @DisplayName("userId로 유저를 조회한다")
    void userId로_조회() {
        User user = User.builder().userId(1L).nickName("n").build();
        given(jpaRepository.findByUserId(1L)).willReturn(Optional.of(user));
        assertThat(repository.findByUserId(1L)).contains(user);
    }

    @Test
    @DisplayName("밴 여부는 banned 플래그로 판정하며, 유저가 없으면 false")
    void 밴_여부() {
        given(jpaRepository.findByUserId(1L))
                .willReturn(Optional.of(User.builder().userId(1L).nickName("n").build()));
        assertThat(repository.isBanned(1L)).isFalse(); // 기본 false
        given(jpaRepository.findByUserId(2L)).willReturn(Optional.empty());
        assertThat(repository.isBanned(2L)).isFalse(); // 없으면 false
    }
}
