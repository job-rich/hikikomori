package org.hikikomori.community.service;

import org.hikikomori.community.domain.Report;
import org.hikikomori.community.dto.ReportDto;
import org.hikikomori.community.exception.DuplicateReportException;
import org.hikikomori.community.exception.SelfReportException;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    public void checkTargetAuthor(Long actualAuthor, Long claimedUserId) {
        if (!actualAuthor.equals(claimedUserId)) {
            throw new IllegalArgumentException("신고 대상 작성자가 일치하지 않습니다");
        }
    }

    public void checkNotSelfReport(Long reporterId, Long targetUserId) {
        if (reporterId.equals(targetUserId)) {
            throw new SelfReportException("본인의 콘텐츠는 신고할 수 없습니다");
        }
    }

    public void checkNotDuplicate(boolean alreadyReported) {
        if (alreadyReported) {
            throw new DuplicateReportException("이미 신고한 콘텐츠입니다");
        }
    }

    public Report buildReport(Long targetUserId, ReportDto.CreateRequest request, String reporterIp) {
        return Report.builder()
                .reporterId(request.reporterId())
                .reporterIp(reporterIp)
                .targetUserId(targetUserId)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .reason(request.reason())
                .description(request.description())
                .build();
    }

    public boolean shouldHide(long distinctReporters, int hideThreshold) {
        return distinctReporters >= hideThreshold;
    }

    public boolean shouldBan(long hiddenContentCount, int banThreshold) {
        return hiddenContentCount >= banThreshold;
    }
}
