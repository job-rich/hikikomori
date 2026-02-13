package org.hikikomori.community.controller.data;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import org.hikikomori.community.domain.Comment;

@Getter
@Builder
public class CommentResponse {

    private final UUID id;
    private final Long userId;
    private final String nickName;
    private final String content;
    private final String createdAt;
    private final List<CommentResponse> children;

    public static CommentResponse from(Comment comment) {
        List<CommentResponse> childResponses = comment.getChildren().stream()
                .map(CommentResponse::from)
                .toList();

        return CommentResponse.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .nickName(comment.getNickName())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt().toString())
                .children(childResponses)
                .build();
    }
}
