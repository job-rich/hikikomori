package org.hikikomori.community.repository;

import java.time.LocalDateTime;
import java.util.UUID;
import org.hikikomori.community.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, UUID> {

    @Modifying
    @Query("DELETE FROM Post p WHERE p.createdAt < :dateTime")
    int deleteByCreatedAtBefore(LocalDateTime dateTime);
}
