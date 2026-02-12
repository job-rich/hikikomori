package org.hikikomori.community.repository;

import java.util.List;
import java.util.UUID;
import org.hikikomori.community.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByPostIdAndParentIsNull(UUID postId);
}
