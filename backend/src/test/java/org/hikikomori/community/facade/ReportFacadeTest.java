package org.hikikomori.community.facade;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.domain.ReportReason;
import org.hikikomori.community.domain.ReportTargetType;
import org.hikikomori.community.dto.ReportDto;
import org.hikikomori.community.event.ReportCreatedEvent;
import org.hikikomori.community.exception.DuplicateReportException;
import org.hikikomori.community.exception.SelfReportException;
import org.hikikomori.community.repository.CommentRepositoryImpl;
import org.hikikomori.community.repository.PostRepositoryImpl;
import org.hikikomori.community.repository.ReportRepositoryImpl;
import org.hikikomori.community.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ReportFacadeTest {

    @Mock ReportRepositoryImpl reportRepository;
    @Mock PostRepositoryImpl postRepository;
    @Mock CommentRepositoryImpl commentRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    ReportService reportService = new ReportService();

    ReportFacade facade() {
        return new ReportFacade(reportService, reportRepository,
                postRepository, commentRepository, eventPublisher);
    }

    private Post postBy(Long userId) {
        return Post.builder().userId(userId).nickName("n").title("t").content("c").tag(null).build();
    }

    @Test
    @DisplayName("게시글 신고를 저장하고 판정 이벤트를 발행한다")
    void 게시글_신고를_접수한다() {
        UUID targetId = UUID.randomUUID();
        given(postRepository.getById(targetId)).willReturn(postBy(2L));
        given(reportRepository.existsReport(1L, "1.1.1.1", ReportTargetType.POST, targetId)).willReturn(false);
        given(reportRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        ReportDto.CreateRequest req = new ReportDto.CreateRequest(
                1L, ReportTargetType.POST, targetId, ReportReason.SPAM, null);

        facade().report(2L, req, "1.1.1.1");

        verify(reportRepository).save(any());
        verify(eventPublisher).publishEvent(any(ReportCreatedEvent.class));
    }

    @Test
    @DisplayName("대상 작성자와 path userId가 다르면 거부한다")
    void 대상작성자_불일치_거부() {
        UUID targetId = UUID.randomUUID();
        given(postRepository.getById(targetId)).willReturn(postBy(99L)); // 실제 작성자 99
        ReportDto.CreateRequest req = new ReportDto.CreateRequest(
                1L, ReportTargetType.POST, targetId, ReportReason.SPAM, null);

        assertThatThrownBy(() -> facade().report(2L, req, "1.1.1.1"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("자기 콘텐츠 신고는 거부한다")
    void 자기신고_거부() {
        UUID targetId = UUID.randomUUID();
        given(postRepository.getById(targetId)).willReturn(postBy(1L));
        ReportDto.CreateRequest req = new ReportDto.CreateRequest(
                1L, ReportTargetType.POST, targetId, ReportReason.SPAM, null);

        assertThatThrownBy(() -> facade().report(1L, req, "1.1.1.1"))
                .isInstanceOf(SelfReportException.class);
    }

    @Test
    @DisplayName("동일 신고자(ID+IP)의 중복 신고는 거부한다")
    void 중복신고_거부() {
        UUID targetId = UUID.randomUUID();
        given(postRepository.getById(targetId)).willReturn(postBy(2L));
        given(reportRepository.existsReport(1L, "1.1.1.1", ReportTargetType.POST, targetId)).willReturn(true);
        ReportDto.CreateRequest req = new ReportDto.CreateRequest(
                1L, ReportTargetType.POST, targetId, ReportReason.SPAM, null);

        assertThatThrownBy(() -> facade().report(2L, req, "1.1.1.1"))
                .isInstanceOf(DuplicateReportException.class);
    }
}
