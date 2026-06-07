package org.hikikomori.community.repository;

import java.util.UUID;
import org.hikikomori.community.domain.Vote;
import org.hikikomori.community.domain.VoteTargetType;
import org.hikikomori.community.domain.VoteValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteJpaRepository extends JpaRepository<Vote, UUID> {

    @Query("SELECT COALESCE(SUM(v.delta), 0) FROM Vote v "
            + "WHERE v.voterId = :voterId AND v.targetType = :type AND v.targetId = :id AND v.value = :value")
    long sumDeltaByVoterTargetValue(@Param("voterId") Long voterId, @Param("type") VoteTargetType type,
                                    @Param("id") UUID id, @Param("value") VoteValue value);

    @Query("SELECT COALESCE(SUM(v.delta), 0) FROM Vote v "
            + "WHERE v.targetType = :type AND v.targetId = :id AND v.value = :value")
    long sumDeltaByContentValue(@Param("type") VoteTargetType type, @Param("id") UUID id, @Param("value") VoteValue value);

    @Query("SELECT COALESCE(SUM(v.delta), 0) FROM Vote v "
            + "WHERE v.targetUserId = :userId AND v.value = :value")
    long sumDeltaByTargetUserValue(@Param("userId") Long userId, @Param("value") VoteValue value);
}
