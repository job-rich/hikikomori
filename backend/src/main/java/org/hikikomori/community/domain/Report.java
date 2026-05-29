package org.hikikomori.community.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hikikomori.community.util.UUIDGenerator;

@Entity
@Table(
        name = "report",
        indexes = {
                @Index(name = "idx_report_target", columnList = "targetType,targetId"),
                @Index(name = "idx_report_target_user", columnList = "targetUserId")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_report_reporter_target",
                columnNames = {"reporterId", "targetType", "targetId"}
        )
)
@Getter
@NoArgsConstructor
public class Report {

    @Id
    private UUID id;

    private Long reporterId;

    private String reporterIp;

    private Long targetUserId;

    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType;

    private UUID targetId;

    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime createdAt;

    @Builder
    public Report(Long reporterId, String reporterIp, Long targetUserId,
                  ReportTargetType targetType, UUID targetId,
                  ReportReason reason, String description) {
        this.id = UUIDGenerator.generate();
        this.reporterId = reporterId;
        this.reporterIp = reporterIp;
        this.targetUserId = targetUserId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }
}
