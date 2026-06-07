package org.hikikomori.community.repository;

import java.util.Optional;
import java.util.UUID;
import org.hikikomori.community.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUserId(Long userId);

    interface RankingRow {
        Long getUserId();
        String getNickName();
        long getPower();
        boolean getBanned();
    }

    @org.springframework.data.jpa.repository.Query(value = """
            SELECT u.user_id AS userId, u.nick_name AS nickName, u.banned AS banned,
                   GREATEST(0, :wVote * COALESCE(vn.net, 0) - :wReport * COALESCE(rc.cnt, 0)) AS power
            FROM users u
            LEFT JOIN (SELECT target_user_id,
                              SUM(CASE WHEN vote_value = 'UP' THEN delta ELSE -delta END) AS net
                       FROM vote GROUP BY target_user_id) vn ON vn.target_user_id = u.user_id
            LEFT JOIN (SELECT target_user_id, COUNT(*) AS cnt
                       FROM report GROUP BY target_user_id) rc ON rc.target_user_id = u.user_id
            ORDER BY power DESC
            """,
            countQuery = "SELECT COUNT(*) FROM users",
            nativeQuery = true)
    org.springframework.data.domain.Page<RankingRow> findRanking(
            @org.springframework.data.repository.query.Param("wVote") int wVote,
            @org.springframework.data.repository.query.Param("wReport") int wReport,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = """
            SELECT COUNT(*) FROM (
              SELECT u.user_id,
                     GREATEST(0, :wVote * COALESCE(vn.net,0) - :wReport * COALESCE(rc.cnt,0)) AS power
              FROM users u
              LEFT JOIN (SELECT target_user_id, SUM(CASE WHEN vote_value='UP' THEN delta ELSE -delta END) net FROM vote GROUP BY target_user_id) vn ON vn.target_user_id=u.user_id
              LEFT JOIN (SELECT target_user_id, COUNT(*) cnt FROM report GROUP BY target_user_id) rc ON rc.target_user_id=u.user_id
            ) p WHERE p.power > :myPower
            """, nativeQuery = true)
    long countHigherPower(
            @org.springframework.data.repository.query.Param("wVote") int wVote,
            @org.springframework.data.repository.query.Param("wReport") int wReport,
            @org.springframework.data.repository.query.Param("myPower") long myPower);
}
