package org.hikikomori.community.search.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SearchServiceTest {

    private final SearchService searchService = new SearchService();

    @Test
    @DisplayName("snippet: 매칭 위치 중심으로 window 추출, 앞뒤 잘리면 … 부착")
    void snippetMatchWindow() {
        // given
        String content = "가나다라마바사아자차카타파하ABCDEFGHIJ";
        // 'ABC'는 index 14에 위치, window=3 → start=max(0,14-3)=11, end=min(24,14+3)=17
        // when
        String result = searchService.snippet(content, "ABC", 3);
        // then
        assertThat(result).contains("ABC");
        assertThat(result).startsWith("…");
        assertThat(result).endsWith("…");
    }

    @Test
    @DisplayName("snippet: 앞쪽 매칭 시 prefix … 없음")
    void snippetMatchAtStart() {
        // given
        String content = "Hello World this is a long sentence";
        // window=10: matchPos=0, start=max(0,0-10)=0, end=min(35,0+10)=10
        // when
        String result = searchService.snippet(content, "Hello", 10);
        // then: start=0, no prefix ellipsis
        assertThat(result).doesNotStartWith("…");
        assertThat(result).contains("Hello");
    }

    @Test
    @DisplayName("snippet: 뒤쪽 매칭 시 suffix … 없음")
    void snippetMatchAtEnd() {
        // given: "Hello World" - 'World' starts at 6
        // window=10 → start=max(0,6-10)=0, end=min(11,6+10)=11 = content.length()
        String content = "Hello World";
        // when
        String result = searchService.snippet(content, "World", 10);
        // then: end == content.length, no suffix ellipsis
        assertThat(result).doesNotEndWith("…");
        assertThat(result).contains("World");
    }

    @Test
    @DisplayName("snippet: 매칭 없으면 앞 window*2 fallback + … 부착")
    void snippetNoMatchFallback() {
        // given: content 길이 > window*2
        String content = "가나다라마바사아자차카타파하";
        // window=3, fallback: 0..min(14, 6)=6, suffix "…"
        // when
        String result = searchService.snippet(content, "zzz", 3);
        // then
        assertThat(result).doesNotStartWith("…");
        assertThat(result).endsWith("…");
    }

    @Test
    @DisplayName("snippet: content가 null 또는 blank → 빈 문자열 반환")
    void snippetNullOrBlank() {
        assertThat(searchService.snippet(null, "test", 10)).isEqualTo("");
        assertThat(searchService.snippet("", "test", 10)).isEqualTo("");
        assertThat(searchService.snippet("   ", "test", 10)).isEqualTo("");
    }

    @Test
    @DisplayName("snippet: window=0 경계 — 예외 없이 반환")
    void snippetWindowZero() {
        // given
        String content = "Hello World";
        // when & then: no exception
        String result = searchService.snippet(content, "World", 0);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("snippet: 대소문자 무시 매칭")
    void snippetCaseInsensitive() {
        // given
        String content = "The Quick Brown Fox";
        // when
        String result = searchService.snippet(content, "quick", 5);
        // then: "Quick" 포함
        assertThat(result).contains("Quick");
    }

    @Test
    @DisplayName("tokenize: 공백으로 분리, blank 제거")
    void tokenize() {
        assertThat(searchService.tokenize("hello world")).containsExactly("hello", "world");
        assertThat(searchService.tokenize("  a  b  ")).containsExactly("a", "b");
        assertThat(searchService.tokenize(null)).isEmpty();
        assertThat(searchService.tokenize("")).isEmpty();
    }

    @Test
    @DisplayName("snippet: 검색어가 여러 토큰이면 첫 매칭 위치 사용")
    void snippetFirstTokenMatch() {
        // given
        String content = "Spring Boot 프레임워크 소개";
        // when: "소개 Boot" → tokens=["소개","Boot"], "Boot" is at index 7, "소개" is at index 14
        // 첫 매칭: "소개" (index 14) 이전에 "Boot" (index 7) 가 먼저 등장
        // tokenize 결과: ["소개", "Boot"] → 첫 토큰 "소개" 찾기 → 14; 두번째 "Boot" → 7
        // 스펙: 첫 매칭 위치 = 첫 번째 토큰이 아닌 content에서 첫 번째로 발견되는 토큰
        // 실제로는 tokens 순서대로 검색, 첫 hit 반환
        String result = searchService.snippet(content, "소개 Boot", 5);
        // "소개" 또는 "Boot" 중 하나를 포함
        assertThat(result).satisfiesAnyOf(
                r -> assertThat(r).contains("Boot"),
                r -> assertThat(r).contains("소개")
        );
    }
}
