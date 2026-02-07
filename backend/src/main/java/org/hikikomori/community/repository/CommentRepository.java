package org.hikikomori.community.repository;

import java.util.List;
import org.hikikomori.community.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostIdAndParentIsNull(Long postId);
}
