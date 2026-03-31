package org.hikikomori.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hikikomori.community.domain.Post;

public class PostDto {

    public record CreateRequest(
            @NotBlank String title,
            @NotBlank String content,
            @NotBlank String tag,
            @NotNull Long userId,
            @NotBlank String nickName
    ) {}

    public record UpdateRequest(
            @NotNull Long userId,
            @NotBlank String title,
            @NotBlank String content,
            @NotBlank String tag
    ) {}

    public record Response(
            UUID id,
            Long userId,
            String nickName,
            String title,
            String content,
            String tag,
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
                    post.getCreatedAt(),
                    post.getUpdatedAt()
            );
        }
    }
}
