package org.hikikomori.community.facade;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.hikikomori.community.config.ScoreWeights;
import org.hikikomori.community.domain.User;
import org.hikikomori.community.repository.ReportRepositoryImpl;
import org.hikikomori.community.repository.UserRepositoryImpl;
import org.hikikomori.community.repository.VoteRepositoryImpl;
import org.hikikomori.community.service.ScoreService;
import org.hikikomori.community.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserFacadeTouchTest {

    @Mock UserRepositoryImpl userRepository;
    @Mock VoteRepositoryImpl voteRepository;
    @Mock ReportRepositoryImpl reportRepository;
    UserService userService = new UserService();
    ScoreService scoreService = new ScoreService();
    ScoreWeights weights = new ScoreWeights(10, 20);

    UserFacade facade() {
        return new UserFacade(userService, scoreService, userRepository, voteRepository, reportRepository, weights);
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
    @DisplayName("touch: 닉네임이 동일하면 저장하지 않는다(불필요한 UPDATE 스킵)")
    void touch_무변경_스킵() {
        User user = User.builder().userId(1L).nickName("same").build();
        given(userRepository.findByUserId(1L)).willReturn(Optional.of(user));

        facade().touch(1L, "same");

        org.mockito.Mockito.verify(userRepository, org.mockito.Mockito.never()).save(any());
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
