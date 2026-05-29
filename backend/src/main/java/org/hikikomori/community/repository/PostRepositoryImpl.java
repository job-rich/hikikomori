package org.hikikomori.community.repository;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl {

    private final PostJpaRepository jpaRepository;

    public Post getById(UUID id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));
    }

    public Post save(Post post) {
        return jpaRepository.save(post);
    }

    public Page<Post> findAll(Pageable pageable) {
        return jpaRepository.findByHiddenAtIsNull(pageable);
    }

    public Page<Post> findByUserId(Long userId, Pageable pageable) {
        return jpaRepository.findByUserIdAndHiddenAtIsNull(userId, pageable);
    }

    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    public long deleteByCreatedAtBetween(LocalDateTime startAt, LocalDateTime endAt) {
        return jpaRepository.deleteByCreatedAtBetween(startAt, endAt);
    }
}
