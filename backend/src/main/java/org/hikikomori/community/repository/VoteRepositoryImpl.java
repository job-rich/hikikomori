package org.hikikomori.community.repository;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.Vote;
import org.hikikomori.community.domain.VoteTargetType;
import org.hikikomori.community.domain.VoteValue;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VoteRepositoryImpl {

    private final VoteJpaRepository jpaRepository;

    public Vote save(Vote vote) {
        return jpaRepository.save(vote);
    }

    /** 해당 (voter, content, value)의 현재 보유 여부 = 델타합 > 0 */
    public boolean has(Long voterId, VoteTargetType targetType, UUID targetId, VoteValue value) {
        return jpaRepository.sumDeltaByVoterTargetValue(voterId, targetType, targetId, value) > 0;
    }

    public long netByTargetUser(Long targetUserId) {
        return jpaRepository.netByTargetUser(targetUserId);
    }

    public long netByContent(VoteTargetType targetType, UUID targetId) {
        return jpaRepository.netByContent(targetType, targetId);
    }
}
