package org.hikikomori.community.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.hikikomori.community.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PostJpaRepository extends JpaRepository<Post, UUID> {

    Page<Post> findByUserId(Long userId, Pageable pageable);

    Optional<Post> findByIdAndHiddenAtIsNull(UUID id);

    Page<Post> findByHiddenAtIsNull(Pageable pageable);

    Page<Post> findByUserIdAndHiddenAtIsNull(Long userId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(UUID id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    void incrementLikeCount(UUID id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :id AND p.likeCount > 0")
    void decrementLikeCount(UUID id);

    @Modifying
    @Query("DELETE FROM Post p WHERE p.createdAt >= :startAt AND p.createdAt < :endAt")
    long deleteByCreatedAtBetween(LocalDateTime startAt, LocalDateTime endAt);
}
