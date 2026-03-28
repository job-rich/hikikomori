package org.hikikomori.community.service;

import org.hikikomori.community.domain.Comment;
import org.hikikomori.community.domain.Post;
import org.hikikomori.community.service.vo.CommentCreate;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    public Comment buildComment(CommentCreate commentCreate, Post post, Comment parent) {
        return Comment.builder()
                .content(commentCreate.content())
                .userId(commentCreate.userId())
                .nickName(commentCreate.nickName())
                .post(post)
                .parent(parent)
                .build();
    }

    public void validateOwnership(Comment comment, Long userId, String operation) {
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 댓글만 " + operation + "할 수 있습니다");
        }
    }

    public void validateNestingDepth(Comment parent) {
        if (parent != null && parent.getParent() != null) {
            throw new IllegalArgumentException("대댓글에는 답글을 달 수 없습니다");
        }
    }

    public void applyUpdate(Comment comment, String content) {
        comment.updateContent(content);
    }

    public void applySoftDelete(Comment comment) {
        comment.softDelete();
    }
}
