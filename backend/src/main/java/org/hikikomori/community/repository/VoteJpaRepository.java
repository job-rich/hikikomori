package org.hikikomori.community.repository;

import java.util.UUID;
import org.hikikomori.community.domain.Vote;
import org.hikikomori.community.domain.VoteTargetType;
import org.hikikomori.community.domain.VoteValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteJpaRepository extends JpaRepository<Vote, UUID> {

    // 내 현재 표 보유 여부용: 특정 value의 델타합 (∈ {0,1})
    @Query("SELECT COALESCE(SUM(v.delta), 0) FROM Vote v "
            + "WHERE v.voterId = :voterId AND v.targetType = :type AND v.targetId = :id AND v.value = :value")
    long sumDeltaByVoterTargetValue(@Param("voterId") Long voterId, @Param("type") VoteTargetType type,
                                    @Param("id") UUID id, @Param("value") VoteValue value);

    // 콘텐츠 순추천 = UP델타합 − DOWN델타합 (한 방 SUM(CASE))
    @Query("SELECT COALESCE(SUM(CASE WHEN v.value = org.hikikomori.community.domain.VoteValue.UP "
            + "THEN v.delta ELSE -v.delta END), 0) FROM Vote v "
            + "WHERE v.targetType = :type AND v.targetId = :id")
    long netByContent(@Param("type") VoteTargetType type, @Param("id") UUID id);

    // 작성자 받은 순추천
    @Query("SELECT COALESCE(SUM(CASE WHEN v.value = org.hikikomori.community.domain.VoteValue.UP "
            + "THEN v.delta ELSE -v.delta END), 0) FROM Vote v "
            + "WHERE v.targetUserId = :userId")
    long netByTargetUser(@Param("userId") Long userId);
}
