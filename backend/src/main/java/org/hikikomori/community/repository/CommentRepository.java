package org.hikikomori.community.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.hikikomori.community.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByPostIdAndParentIsNull(UUID postId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.createdAt < :dateTime")
    int deleteByCreatedAtBefore(LocalDateTime dateTime);
}
