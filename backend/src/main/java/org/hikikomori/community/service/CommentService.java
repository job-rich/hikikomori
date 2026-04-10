package org.hikikomori.community.service;

import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.dto.CommentDto;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    public Comment buildComment(CommentDto.CreateRequest request, Post post, Comment parent) {
        return Comment.builder()
                .content(request.content())
                .userId(request.userId())
                .nickName(request.nickName())
                .post(post)
                .parent(parent)
                .build();
    }

    public void checkOwnership(Comment comment, Long userId, String operation) {
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 댓글만 " + operation + "할 수 있습니다");
        }
    }

    public void checkNestingDepth(Comment parent) {
        if (parent != null && parent.getParent() != null && parent.getParent().getParent() != null) {
            throw new IllegalArgumentException("3단계 이상의 댓글은 작성할 수 없습니다");
        }
    }

    public void applyUpdate(Comment comment, String content) {
        comment.updateContent(content);
    }

    public void applySoftDelete(Comment comment) {
        comment.softDelete();
    }
}
