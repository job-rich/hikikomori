package org.hikikomori.community.repository;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.Comment;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl {

    private final CommentJpaRepository jpaRepository;

    public Comment getById(UUID id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + id));
    }

    public Comment getParentById(UUID parentId) {
        return jpaRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다: " + parentId));
    }

    public Comment save(Comment comment) {
        return jpaRepository.save(comment);
    }

    public List<Comment> findByPostIdAndParentIsNull(UUID postId) {
        return jpaRepository.findByPostIdAndParentIsNull(postId);
    }

    public void deleteAllByPostId(UUID postId) {
        jpaRepository.deleteAllByPostId(postId);
    }
}
