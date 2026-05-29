package org.hikikomori.community.service;

import org.hikikomori.community.exception.SelfReportException;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    public void checkNotSelfReport(Long reporterId, Long targetUserId) {
        if (reporterId.equals(targetUserId)) {
            throw new SelfReportException("본인의 콘텐츠는 신고할 수 없습니다");
        }
    }

    public boolean shouldHide(long distinctReporters, int hideThreshold) {
        return distinctReporters >= hideThreshold;
    }

    public boolean shouldBan(long hiddenContentCount, int banThreshold) {
        return hiddenContentCount >= banThreshold;
    }
}
