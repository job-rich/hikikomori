package org.hikikomori.community.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.hikikomori.community.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostLikeJpaRepository extends JpaRepository<PostLike, UUID> {

    boolean existsByUserIdAndPostId(Long userId, UUID postId);

    void deleteByUserIdAndPostId(Long userId, UUID postId);

    void deleteAllByPostId(UUID postId);

    @Query("SELECT pl.postId FROM PostLike pl "
            + "WHERE pl.userId = :userId AND pl.postId IN :postIds")
    List<UUID> findPostIdsByUserIdAndPostIdIn(
            @Param("userId") Long userId,
            @Param("postIds") Collection<UUID> postIds);
}
