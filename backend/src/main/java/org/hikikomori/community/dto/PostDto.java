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

    public record Response(
            UUID id,
            Long userId,
            String nickName,
            String title,
            String content,
            PostTag tag,
            long commentCount,
            long view,
            long like,
            long fightPoint,
            boolean isBookmarked,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static Response from(Post post) {
            return new Response(
                    post.getId(),
                    post.getUserId(),
                    post.getNickName(),
                    post.getTitle(),
                    post.getContent(),
                    post.getTag(),
                    post.getCommentCount(),
                    // TODO: view/like/fightPoint/isBookmarked 별도 도메인 도입 후 채움
                    0L,
                    0L,
                    0L,
                    false,
                    post.getCreatedAt(),
                    post.getUpdatedAt()
            );
        }
    }
}
