package org.hikikomori.community.service;

import java.util.UUID;
import org.hikikomori.community.domain.PostLike;
import org.springframework.stereotype.Service;

@Service
public class PostLikeService {

    public void checkNotSelfLike(Long postAuthorId, Long userId) {
        if (postAuthorId.equals(userId)) {
            throw new IllegalArgumentException("본인의 게시글에는 좋아요를 할 수 없습니다");
        }
    }

    public PostLike buildPostLike(Long userId, UUID postId) {
        return PostLike.builder()
                .userId(userId)
                .postId(postId)
                .build();
    }
}
