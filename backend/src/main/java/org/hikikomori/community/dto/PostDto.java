package org.hikikomori.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.domain.PostTag;

public class PostDto {

    public record CreateRequest(
            @NotBlank String title,
            @NotBlank String content,
            @NotNull PostTag tag,
            @NotNull Long userId,
            @NotBlank String nickName
    ) {}

    public record UpdateRequest(
            @NotNull Long userId,
            @NotBlank String title,
            @NotBlank String content,
            @NotNull PostTag tag
    ) {}

    public record LikeToggleResponse(boolean liked, long likeCount) {}

    public record Response(
            UUID id,
            Long userId,
            String nickName,
            String title,
            String content,
            PostTag tag,
            long commentCount,
            long viewCount,
            long likeCount,
            boolean likedByMe,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static Response from(Post post) {
            return from(post, false);
        }

        public static Response from(Post post, boolean likedByMe) {
            return new Response(
                    post.getId(),
                    post.getUserId(),
                    post.getNickName(),
                    post.getTitle(),
                    post.getContent(),
                    post.getTag(),
                    post.getCommentCount(),
                    post.getViewCount(),
                    post.getLikeCount(),
                    likedByMe,
                    post.getCreatedAt(),
                    post.getUpdatedAt()
            );
        }
    }
}
