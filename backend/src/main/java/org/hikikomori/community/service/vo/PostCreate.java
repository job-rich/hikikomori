package org.hikikomori.community.service.vo;

public record PostCreate(
        String title,
        String content,
        String tag,
        Long userId,
        String nickName
) {}
