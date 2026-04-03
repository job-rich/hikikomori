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
    ETC("기타");

    private final String description;
}
