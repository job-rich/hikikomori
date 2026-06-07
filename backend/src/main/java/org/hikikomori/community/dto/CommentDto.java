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
            boolean hidden,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt,
            List<Response> children,
            long voteScore
    ) {
        public static Response from(Comment comment) {
            List<Response> childResponses = comment.getChildren().stream()
                    .map(Response::from)
                    .toList();

            boolean hidden = comment.isHidden();
            boolean deleted = comment.getDeletedAt() != null;
            String content = (hidden || deleted) ? null : comment.getContent();

            return new Response(
                    comment.getId(),
                    comment.getUserId(),
                    comment.getNickName(),
                    content,
                    hidden,
                    comment.getCreatedAt(),
                    comment.getUpdatedAt(),
                    comment.getDeletedAt(),
                    childResponses,
                    comment.getVoteScore()
            );
        }
    }
}
