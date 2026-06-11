package org.hikikomori.community.repository;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl {

    private final PostJpaRepository jpaRepository;

    public Post getById(UUID id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));
    }

    /** 숨김 처리된 게시글은 없는 것으로 취급한다(목록 제외와 동일 규칙). */
    public Post getVisibleById(UUID id) {
        return jpaRepository.findByIdAndHiddenAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));
    }

    public Post save(Post post) {
        return jpaRepository.save(post);
    }

    public Page<Post> findAll(Pageable pageable) {

        Sort sortWithId = pageable.getSort().and(Sort.by(Sort.Direction.ASC, "id"));
        Pageable deterministicPageable = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), sortWithId);
        return jpaRepository.findAll(deterministicPageable);

    }

    public Page<Post> findByUserId(Long userId, Pageable pageable) {
        return jpaRepository.findByUserIdAndHiddenAtIsNull(userId, pageable);
    }

    public void incrementViewCount(UUID id) {
        jpaRepository.incrementViewCount(id);
    }

    public void incrementLikeCount(UUID id) {
        jpaRepository.incrementLikeCount(id);
    }

    public void decrementLikeCount(UUID id) {
        jpaRepository.decrementLikeCount(id);
    }

    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    public long deleteByCreatedAtBetween(LocalDateTime startAt, LocalDateTime endAt) {
        return jpaRepository.deleteByCreatedAtBetween(startAt, endAt);
    }
}
