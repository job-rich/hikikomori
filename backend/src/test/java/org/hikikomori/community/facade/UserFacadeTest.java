package org.hikikomori.community.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.hikikomori.community.config.ScoreWeights;
import org.hikikomori.community.domain.User;
import org.hikikomori.community.dto.UserDto;
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
class UserFacadeTest {

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
    @DisplayName("프로필: 순추천·신고로 전투력 계산 + rank")
    void 프로필() {
        given(userRepository.getByUserId(2L))
                .willReturn(User.builder().userId(2L).nickName("니체").build());
        given(voteRepository.netByTargetUser(2L)).willReturn(5L);
        given(reportRepository.countByTargetUser(2L)).willReturn(1L);
        given(userRepository.countHigherPower(10, 20, 30L)).willReturn(11L); // power=30

        UserDto.ProfileResponse res = facade().getProfile(2L);

        assertThat(res.power()).isEqualTo(30L);
        assertThat(res.voteNet()).isEqualTo(5L);
        assertThat(res.reports()).isEqualTo(1L);
        assertThat(res.rank()).isEqualTo(12L); // 11 + 1
        assertThat(res.nickName()).isEqualTo("니체");
    }
}
