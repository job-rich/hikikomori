package org.hikikomori.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.hikikomori.community.domain.Comment;

public class CommentDto {

    public record CreateRequest(
            @NotBlank String content,
            UUID parentId,
            Long userId,
            String nickName
    ) {}

    public record UpdateRequest(
            @NotNull Long userId,
            @NotBlank String content
    ) {}

    public record Response(
            UUID id,
            Long userId,
            String nickName,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt,
            List<Response> children
    ) {
        public static Response from(Comment comment) {
            List<Response> childResponses = comment.getChildren().stream()
                    .map(Response::from)
                    .toList();

            return new Response(
                    comment.getId(),
                    comment.getUserId(),
                    comment.getNickName(),
                    comment.getDeletedAt() == null ? comment.getContent() : null,
                    comment.getCreatedAt(),
                    comment.getUpdatedAt(),
                    comment.getDeletedAt(),
                    childResponses
            );
        }
    }
}
