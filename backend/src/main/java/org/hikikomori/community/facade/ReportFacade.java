package org.hikikomori.community.facade;

import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.domain.Report;
import org.hikikomori.community.dto.ReportDto;
import org.hikikomori.community.event.ReportCreatedEvent;
import org.hikikomori.community.exception.DuplicateReportException;
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
        if (!actualAuthor.equals(targetUserId)) {
            throw new IllegalArgumentException("신고 대상 작성자가 일치하지 않습니다");
        }
        reportService.checkNotSelfReport(request.reporterId(), targetUserId);

        if (reportRepository.existsReport(request.reporterId(), request.targetType(), request.targetId())
                || reportRepository.existsReportByIp(reporterIp, request.targetType(), request.targetId())) {
            throw new DuplicateReportException("이미 신고한 콘텐츠입니다");
        }

        Report report = reportRepository.save(Report.builder()
                .reporterId(request.reporterId())
                .reporterIp(reporterIp)
                .targetUserId(targetUserId)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .reason(request.reason())
                .description(request.description())
                .build());

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
