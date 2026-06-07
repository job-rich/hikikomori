package org.hikikomori.community.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hikikomori.community.util.UUIDGenerator;

// append-only 델타 로그: update·delete·unique 없음. 행마다 (value 행사/취소)를 delta로 기록.
@Entity
@Table(
        name = "vote",
        indexes = {
                @Index(name = "idx_vote_target", columnList = "targetType,targetId"),
                @Index(name = "idx_vote_target_user", columnList = "targetUserId"),
                @Index(name = "idx_vote_voter_target", columnList = "voterId,targetType,targetId")
        }
)
@Getter
@NoArgsConstructor
public class Vote {

    @Id
    private UUID id;

    private Long voterId;

    private Long targetUserId;

    @Enumerated(EnumType.STRING)
    private VoteTargetType targetType;

    private UUID targetId;

    @Enumerated(EnumType.STRING)
    private VoteValue value;

    private int delta; // +1 = 행사, -1 = 취소

    private LocalDateTime createdAt;

    @Builder
    public Vote(Long voterId, Long targetUserId, VoteTargetType targetType, UUID targetId, VoteValue value, int delta) {
        this.id = UUIDGenerator.generate();
        this.voterId = voterId;
        this.targetUserId = targetUserId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.value = value;
        this.delta = delta;
        this.createdAt = LocalDateTime.now();
    }
}
