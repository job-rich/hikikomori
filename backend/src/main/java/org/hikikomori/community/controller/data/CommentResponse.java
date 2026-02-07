package org.hikikomori.community.controller.data;

import java.util.List;
import org.hikikomori.community.domain.Comment;

public record CommentResponse(
        Long id,
        String content,
        String createdAt,
        List<CommentResponse> children
) {

    public static CommentResponse from(Comment comment) {
        List<CommentResponse> childResponses = comment.getChildren().stream()
                .map(CommentResponse::from)
                .toList();

        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt().toString(),
                childResponses
        );
    }
}
