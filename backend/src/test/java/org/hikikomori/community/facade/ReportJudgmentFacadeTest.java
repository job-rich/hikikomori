package org.hikikomori.community.facade;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.hikikomori.community.config.ReportPolicyProperties;
import org.hikikomori.community.domain.Ban;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.domain.ReportTargetType;
import org.hikikomori.community.event.ReportCreatedEvent;
import org.hikikomori.community.repository.BanRepositoryImpl;
import org.hikikomori.community.repository.CommentRepositoryImpl;
import org.hikikomori.community.repository.PostRepositoryImpl;
import org.hikikomori.community.repository.ReportRepositoryImpl;
import org.hikikomori.community.service.BanService;
import org.hikikomori.community.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportJudgmentFacadeTest {

    @Mock ReportRepositoryImpl reportRepository;
    @Mock BanRepositoryImpl banRepository;
    @Mock PostRepositoryImpl postRepository;
    @Mock CommentRepositoryImpl commentRepository;

    ReportService reportService = new ReportService();
    BanService banService = new BanService();
    ReportPolicyProperties properties = new ReportPolicyProperties(5, 5);

    ReportJudgmentFacade facade() {
        return new ReportJudgmentFacade(reportService, banService, reportRepository,
                banRepository, postRepository, commentRepository, properties);
    }

    @Test
    @DisplayName("신고자 수가 임계값 미만이면 숨김·밴 모두 하지 않는다")
    void 임계값_미만이면_아무것도_안한다() {
        UUID targetId = UUID.randomUUID();
        given(reportRepository.countDistinctReporters(ReportTargetType.POST, targetId))
                .willReturn(4L);

        facade().judge(new ReportCreatedEvent(ReportTargetType.POST, targetId, 2L));

        verify(postRepository, never()).getById(any());
        verify(banRepository, never()).save(any());
    }

    @Test
    @DisplayName("신고자 수가 임계값 이상이면 게시글을 숨기고, 숨김 누적이 밴 임계값 이상이면 밴한다")
    void 숨김후_밴_처리() {
        UUID targetId = UUID.randomUUID();
        Post post = Post.builder().userId(2L).nickName("n").title("t").content("c").tag(null).build();
        given(reportRepository.countDistinctReporters(ReportTargetType.POST, targetId))
                .willReturn(5L);
        given(postRepository.getById(targetId)).willReturn(post);
        given(reportRepository.countHiddenContents(2L, 5)).willReturn(5L);
        given(banRepository.isBanned(2L)).willReturn(false);

        facade().judge(new ReportCreatedEvent(ReportTargetType.POST, targetId, 2L));

        verify(postRepository).save(post);
        verify(banRepository).save(any(Ban.class));
    }

    @Test
    @DisplayName("이미 밴된 사용자는 다시 밴하지 않는다")
    void 이미_밴이면_재밴하지_않는다() {
        UUID targetId = UUID.randomUUID();
        Post post = Post.builder().userId(2L).nickName("n").title("t").content("c").tag(null).build();
        given(reportRepository.countDistinctReporters(ReportTargetType.POST, targetId))
                .willReturn(5L);
        given(postRepository.getById(targetId)).willReturn(post);
        given(reportRepository.countHiddenContents(2L, 5)).willReturn(5L);
        given(banRepository.isBanned(2L)).willReturn(true);

        facade().judge(new ReportCreatedEvent(ReportTargetType.POST, targetId, 2L));

        verify(banRepository, never()).save(any());
    }
}
