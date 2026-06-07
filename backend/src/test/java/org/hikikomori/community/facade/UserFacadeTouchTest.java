package org.hikikomori.community.facade;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.hikikomori.community.domain.User;
import org.hikikomori.community.repository.UserRepositoryImpl;
import org.hikikomori.community.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserFacadeTouchTest {

    @Mock UserRepositoryImpl userRepository;
    UserService userService = new UserService();

    UserFacade facade() {
        return new UserFacade(userService, userRepository);
    }

    @Test
    @DisplayName("touch: 기존 유저면 닉네임 갱신 후 저장")
    void touch_갱신() {
        User user = User.builder().userId(1L).nickName("old").build();
        given(userRepository.findByUserId(1L)).willReturn(Optional.of(user));

        facade().touch(1L, "new");

        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("touch: 없는 유저면 생성 후 저장")
    void touch_생성() {
        given(userRepository.findByUserId(1L)).willReturn(Optional.empty());

        facade().touch(1L, "n");

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("markBanned: 유저를 밴 상태로 표시 후 저장(없으면 생성)")
    void markBanned() {
        given(userRepository.findByUserId(1L)).willReturn(Optional.empty());

        facade().markBanned(1L);

        verify(userRepository).save(any(User.class));
    }
}
