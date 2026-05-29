package org.hikikomori.community.repository;

import java.util.UUID;
import org.hikikomori.community.domain.Report;
import org.hikikomori.community.domain.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportJpaRepository extends JpaRepository<Report, UUID> {

    // 신고자 ID와 IP가 모두 같을 때만 중복 신고로 본다.
    boolean existsByReporterIdAndReporterIpAndTargetTypeAndTargetId(
            Long reporterId, String reporterIp, ReportTargetType targetType, UUID targetId);

    // 특정 콘텐츠의 서로 다른 신고자 수
    @Query("SELECT COUNT(DISTINCT r.reporterId) FROM Report r "
            + "WHERE r.targetType = :targetType AND r.targetId = :targetId")
    long countDistinctReportersForTarget(
            @Param("targetType") ReportTargetType targetType,
            @Param("targetId") UUID targetId);

    // 특정 작성자의 콘텐츠 중, 서로 다른 신고자 수가 임계값 이상인 콘텐츠 개수
    @Query(value = "SELECT COUNT(*) FROM ("
            + "  SELECT r.target_id FROM report r "
            + "  WHERE r.target_user_id = :targetUserId "
            + "  GROUP BY r.target_type, r.target_id "
            + "  HAVING COUNT(DISTINCT r.reporter_id) >= :hideThreshold"
            + ") agg", nativeQuery = true)
    long countHiddenContentsForUser(
            @Param("targetUserId") Long targetUserId,
            @Param("hideThreshold") int hideThreshold);
}
