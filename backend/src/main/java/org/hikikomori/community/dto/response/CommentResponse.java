package org.hikikomori.community.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.hikikomori.community.domain.Comment;

public record CommentResponse(
        UUID id,
        Long userId,
        String nickName,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt,
        List<CommentResponse> children
) {
    public static CommentResponse from(Comment comment) {
        List<CommentResponse> childResponses = comment.getChildren().stream()
                .map(CommentResponse::from)
                .toList();

        return new CommentResponse(
                comment.getId(),
                comment.getUserId(),
                comment.getNickName(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getDeletedAt(),
                childResponses
        );
    }
}
