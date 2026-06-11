package org.hikikomori.community.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.PostLike;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostLikeRepositoryImpl {

    private final PostLikeJpaRepository jpaRepository;

    public boolean existsByUserIdAndPostId(Long userId, UUID postId) {
        return jpaRepository.existsByUserIdAndPostId(userId, postId);
    }

    public PostLike save(PostLike postLike) {
        return jpaRepository.save(postLike);
    }

    public void deleteByUserIdAndPostId(Long userId, UUID postId) {
        jpaRepository.deleteByUserIdAndPostId(userId, postId);
    }

    public void deleteAllByPostId(UUID postId) {
        jpaRepository.deleteAllByPostId(postId);
    }

    public List<UUID> findLikedPostIds(Long userId, Collection<UUID> postIds) {
        if (postIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findPostIdsByUserIdAndPostIdIn(userId, postIds);
    }
}
