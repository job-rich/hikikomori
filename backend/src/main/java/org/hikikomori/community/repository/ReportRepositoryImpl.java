package org.hikikomori.community.repository;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.Report;
import org.hikikomori.community.domain.ReportTargetType;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryImpl {

    private final ReportJpaRepository jpaRepository;

    public Report save(Report report) {
        return jpaRepository.save(report);
    }

    public boolean existsReport(Long reporterId, ReportTargetType targetType, UUID targetId) {
        return jpaRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, targetType, targetId);
    }

    public boolean existsReportByIp(String reporterIp, ReportTargetType targetType, UUID targetId) {
        return jpaRepository.existsByReporterIpAndTargetTypeAndTargetId(reporterIp, targetType, targetId);
    }

    public long countDistinctReporters(ReportTargetType targetType, UUID targetId) {
        return jpaRepository.countDistinctReportersForTarget(targetType, targetId);
    }

    public long countHiddenContents(Long targetUserId, int hideThreshold) {
        return jpaRepository.countHiddenContentsForUser(targetUserId, hideThreshold);
    }
}
