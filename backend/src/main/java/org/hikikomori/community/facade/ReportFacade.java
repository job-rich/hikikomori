package org.hikikomori.community.facade;

import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.domain.Report;
import org.hikikomori.community.dto.ReportDto;
import org.hikikomori.community.event.ReportCreatedEvent;
import org.hikikomori.community.repository.CommentRepositoryImpl;
import org.hikikomori.community.repository.PostRepositoryImpl;
import org.hikikomori.community.repository.ReportRepositoryImpl;
import org.hikikomori.community.service.ReportService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportFacade {

    private final ReportService reportService;
    private final ReportRepositoryImpl reportRepository;
    private final PostRepositoryImpl postRepository;
    private final CommentRepositoryImpl commentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ReportDto.Response report(Long targetUserId, ReportDto.CreateRequest request, String reporterIp) {
        Long actualAuthor = resolveTargetAuthor(request);
        reportService.checkTargetAuthor(actualAuthor, targetUserId);
        reportService.checkNotSelfReport(request.reporterId(), targetUserId);
        // 신고자 ID와 IP가 모두 동일할 때만 중복 신고로 차단한다. (ID를 바꾼 재신고는 허용)
        reportService.checkNotDuplicate(reportRepository.existsReport(
                request.reporterId(), reporterIp, request.targetType(), request.targetId()));

        Report report = reportRepository.save(
                reportService.buildReport(targetUserId, request, reporterIp));

        eventPublisher.publishEvent(new ReportCreatedEvent(
                request.targetType(), request.targetId(), targetUserId));

        return ReportDto.Response.of(report.getId());
    }

    private Long resolveTargetAuthor(ReportDto.CreateRequest request) {
        return switch (request.targetType()) {
            case POST -> {
                Post post = postRepository.getById(request.targetId());
                yield post.getUserId();
            }
            case COMMENT -> {
                Comment comment = commentRepository.getById(request.targetId());
                yield comment.getUserId();
            }
        };
    }
}
