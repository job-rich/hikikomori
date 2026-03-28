package org.hikikomori.community.service.vo;

public record CommentCreate(
        String content,
        Long userId,
        String nickName
) {}
