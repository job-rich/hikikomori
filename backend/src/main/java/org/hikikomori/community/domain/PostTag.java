package org.hikikomori.community.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostTag {

    PHILOSOPHY("철학"),
    SOCIETY("사회"),
    POLITICS("정치"),
    ECONOMY("경제"),
    CULTURE("문화"),
    DAILY("일상"),
    ETC("기타"),
    /** 레거시 DB 문자열 호환. 새 글은 {@link #ETC} 사용. */
    @Deprecated
    VOID("기타");

    private final String description;
}
