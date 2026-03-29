package org.hikikomori.community.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import org.hikikomori.community.domain.Post;

public record PostResponse(
        UUID id,
        Long userId,
        String nickName,
        String title,
        String content,
        String tag,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getUserId(),
                post.getNickName(),
                post.getTitle(),
                post.getContent(),
                post.getTag(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
