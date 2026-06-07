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
    @DisplayName("getByUserId: 없으면 예외")
    void getByUserId_없으면_예외() {
        given(jpaRepository.findByUserId(9L)).willReturn(Optional.empty());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> repository.getByUserId(9L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
