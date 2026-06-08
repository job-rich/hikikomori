package org.hikikomori.community.repository;

import java.time.LocalDateTime;
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
        // 숨김 댓글도 트리에 유지한다 — 응답에서 내용만 가린 placeholder로 노출(자식 노출 유지).
        return jpaRepository.findByPostIdAndParentIsNull(postId);
    }

    public void deleteAllByPostId(UUID postId) {
        jpaRepository.deleteAllByPostId(postId);
    }

    public long deleteByCreatedAtBetween(LocalDateTime startAt, LocalDateTime endAt) {
        return jpaRepository.deleteByCreatedAtBetween(startAt, endAt);
    }
}
