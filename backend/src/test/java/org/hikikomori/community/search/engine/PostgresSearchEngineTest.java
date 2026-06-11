package org.hikikomori.community.search.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.List;
import org.hikikomori.community.search.model.HitType;
import org.hikikomori.community.search.model.SearchCriteria;
import org.hikikomori.community.search.model.SearchHit;
import org.hikikomori.community.search.model.SortType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Docker 필요: Testcontainers로 PostgreSQL 컨테이너를 실행해 pg_trgm 검색을 검증한다.
 */
@SpringBootTest
@Testcontainers
class PostgresSearchEngineTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.batch.jdbc.initialize-schema", () -> "always");
    }

    @Autowired
    private PostgresSearchEngine searchEngine;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM comment");
        jdbcTemplate.execute("DELETE FROM post");

        jdbcTemplate.execute("""
                INSERT INTO post (id, user_id, nick_name, title, content, tag, created_at, updated_at)
                VALUES
                  ('a0000000-0000-0000-0000-000000000001'::uuid, 1, '유저A', '자유에 대한 성찰', '자유는 무엇인가', 'PHILOSOPHY', now(), now()),
                  ('a0000000-0000-0000-0000-000000000002'::uuid, 2, '유저B', '사회와 자유', '우리 사회에서 자유의 의미', 'SOCIETY', now() - interval '1 day', now()),
                  ('a0000000-0000-0000-0000-000000000003'::uuid, 3, '유저C', '정의란 무엇인가', '정의에 대한 고찰', 'PHILOSOPHY', now() - interval '2 days', now()),
                  ('a0000000-0000-0000-0000-000000000004'::uuid, 4, '유저A', '오늘의 일상', '평범한 하루', 'DAILY', now() - interval '3 days', now()),
                  ('a0000000-0000-0000-0000-000000000006'::uuid, 6, '유저F', '가능성에 관하여', '가능성을 탐구한다', 'PHILOSOPHY', now() - interval '4 days', now()),
                  ('a0000000-0000-0000-0000-000000000007'::uuid, 7, '유저G', '개인주의가 가능한가', '개인주의 고찰', 'PHILOSOPHY', now() - interval '5 days', now()),
                  ('a0000000-0000-0000-0000-000000000008'::uuid, 8, '유저H', '국가이라는 형벌', '국가 권력', 'POLITICS', now() - interval '6 days', now()),
                  ('a0000000-0000-0000-0000-000000000009'::uuid, 9, '유저I', '감정의 흐름', '감정이란 무엇인가', 'PHILOSOPHY', now() - interval '7 days', now()),
                  ('a0000000-0000-0000-0000-000000000010'::uuid, 10, '유저J', '강함에 대하여', '강함의 의미', 'PHILOSOPHY', now() - interval '8 days', now()),
                  ('a0000000-0000-0000-0000-000000000011'::uuid, 11, '유저K', '각오와 나', '각오의 중요성', 'DAILY', now() - interval '9 days', now()),
                  ('a0000000-0000-0000-0000-000000000012'::uuid, 12, '유저L', '자유을 위한 변명', '자유에 관하여', 'SOCIETY', now() - interval '10 days', now()),
                  ('a0000000-0000-0000-0000-000000000013'::uuid, 13, '유저M', '자유을 위한 변명', '자유에 관하여', 'SOCIETY', now() - interval '11 days', now()),
                  ('a0000000-0000-0000-0000-000000000014'::uuid, 14, '유저N', '자유을 위한 변명', '자유에 관하여', 'SOCIETY', now() - interval '12 days', now()),
                  ('a0000000-0000-0000-0000-000000000015'::uuid, 15, '유저O', '자본와 나', '자본의 의미', 'SOCIETY', now() - interval '13 days', now())
                """);

        jdbcTemplate.execute("""
                INSERT INTO post (id, user_id, nick_name, title, content, tag, created_at, updated_at, hidden_at)
                VALUES ('a0000000-0000-0000-0000-000000000005'::uuid, 5, '유저D', '숨겨진 자유 글', '숨겨진 내용', 'ETC', now(), now(), now())
                """);

        jdbcTemplate.execute("""
                INSERT INTO comment (id, user_id, nick_name, content, post_id, created_at, updated_at)
                VALUES
                  ('b0000000-0000-0000-0000-000000000001'::uuid, 1, '유저A', '자유로운 댓글', 'a0000000-0000-0000-0000-000000000001'::uuid, now(), now()),
                  ('b0000000-0000-0000-0000-000000000002'::uuid, 2, '유저B', '정의에 관한 댓글', 'a0000000-0000-0000-0000-000000000003'::uuid, now(), now())
                """);

        jdbcTemplate.execute("""
                INSERT INTO comment (id, user_id, nick_name, content, post_id, created_at, updated_at, deleted_at)
                VALUES ('b0000000-0000-0000-0000-000000000003'::uuid, 3, '유저C', '삭제된 자유 댓글', 'a0000000-0000-0000-0000-000000000001'::uuid, now(), now(), now())
                """);

        jdbcTemplate.execute("""
                INSERT INTO comment (id, user_id, nick_name, content, post_id, created_at, updated_at, hidden_at)
                VALUES ('b0000000-0000-0000-0000-000000000004'::uuid, 4, '유저D', '숨겨진 자유 댓글', 'a0000000-0000-0000-0000-000000000001'::uuid, now(), now(), now())
                """);
    }

    @Test
    @DisplayName("searchPosts: 매칭 글만 반환, 숨김글 제외, 관련도 정렬, 페이지네이션 total 정확")
    void searchPosts_relevance_excludesHidden() {
        SearchCriteria criteria = SearchCriteria.of("자유", null, SortType.RELEVANCE);
        Page<SearchHit> result = searchEngine.searchPosts(criteria, PageRequest.of(0, 10));

        // 자유에 대한 성찰, 사회와 자유, 자유을 위한 변명(x3) — 총 5개 (자본와 나는 자유 미포함)
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getContent()).allMatch(hit -> hit.type() == HitType.POST);
        assertThat(result.getContent()).noneMatch(hit -> hit.title() != null && hit.title().contains("숨겨진"));
    }

    @Test
    @DisplayName("searchComments: deleted/hidden 댓글 제외")
    void searchComments_excludesDeletedAndHidden() {
        SearchCriteria criteria = SearchCriteria.of("자유", null, SortType.RELEVANCE);
        Page<SearchHit> result = searchEngine.searchComments(criteria, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).allMatch(hit -> hit.type() == HitType.COMMENT);
    }

    @Test
    @DisplayName("suggest: 자유 매칭 제목 포함, 숨김글 제외, 중복 없음, size ≤ limit")
    void suggest_matchesTitles_excludesHidden() {
        List<String> result = searchEngine.suggest("자유", 8);

        assertThat(result).isNotEmpty();
        assertThat(result).anyMatch(s -> s.contains("자유"));
        assertThat(result).noneMatch(s -> s.contains("숨겨진"));
        assertThat(new HashSet<>(result)).hasSameSizeAs(result);
        assertThat(result).hasSizeLessThanOrEqualTo(8);
    }

    @Test
    @DisplayName("Pageable.unpaged()로 호출해도(통합검색 병합 경로) 예외 없이 전체를 반환한다")
    void search_unpaged_returnsAllWithoutException() {
        SearchCriteria criteria = SearchCriteria.of("자유", null, SortType.RELEVANCE);

        Page<SearchHit> posts = searchEngine.searchPosts(criteria, Pageable.unpaged());
        Page<SearchHit> comments = searchEngine.searchComments(criteria, Pageable.unpaged());
        Page<SearchHit> users = searchEngine.searchUsers(
                SearchCriteria.of("유저", null, SortType.RELEVANCE), Pageable.unpaged());

        // 자유에 대한 성찰, 사회와 자유, 자유을 위한 변명(x3) — 총 5개 (자본와 나는 자유 미포함)
        assertThat(posts.getContent()).hasSize(5);
        assertThat(comments.getContent()).hasSize(1);
        assertThat(users.getContent()).isNotEmpty();
    }

    // ── 초성 검색 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("suggest: 초성 'ㅈㅇ' → 반환 제목이 모두 'ㅈㅇ'으로 시작하는 초성을 가짐 (prefix)")
    void suggest_chosung_returnsJayuTitle() {
        List<String> result = searchEngine.suggest("ㅈㅇ", 8);

        // 'ㅈㅇ'으로 시작하는 초성을 가진 제목만 포함: '자유에 대한 성찰'(ㅈㅇ...), '정의란 무엇인가'(ㅈㅇ...)
        assertThat(result).isNotEmpty();
        // 반환된 제목은 모두 '자' 또는 '정'으로 시작 (초성 ㅈ + 다음 초성 ㅇ)
        assertThat(result).anyMatch(s -> s.startsWith("자유") || s.startsWith("정의"));
        // '사회와 자유'는 초성이 ㅅㅎㅇ ㅈㅇ → ㅈㅇ이 앞에 없으므로 제외
        assertThat(result).noneMatch(s -> s.startsWith("사회"));
    }

    @Test
    @DisplayName("searchPosts: 초성 'ㅈㅇ' → '자유' 관련 글 매칭, 숨김글 제외")
    void searchPosts_chosung_matchesJayu() {
        SearchCriteria criteria = SearchCriteria.of("ㅈㅇ", null, SortType.RELEVANCE);
        Page<SearchHit> result = searchEngine.searchPosts(criteria, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isGreaterThan(0);
        assertThat(result.getContent()).anyMatch(hit ->
                hit.title() != null && hit.title().contains("자유"));
        assertThat(result.getContent()).noneMatch(hit ->
                hit.title() != null && hit.title().contains("숨겨진"));
    }

    @Test
    @DisplayName("searchPosts: 초성 'ㅅㅎ' → '사회' 제목 매칭 (쌍자음 폴딩 동작)")
    void searchPosts_chosung_matchesSahoe_withFolding() {
        SearchCriteria criteria = SearchCriteria.of("ㅅㅎ", null, SortType.RELEVANCE);
        Page<SearchHit> result = searchEngine.searchPosts(criteria, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isGreaterThan(0);
        assertThat(result.getContent()).anyMatch(hit ->
                hit.title() != null && hit.title().contains("사회"));
    }

    @Test
    @DisplayName("회귀: 음절 쿼리 '자유'는 기존 ILIKE 경로로 동작")
    void searchPosts_syllable_regression() {
        SearchCriteria criteria = SearchCriteria.of("자유", null, SortType.RELEVANCE);
        Page<SearchHit> result = searchEngine.searchPosts(criteria, PageRequest.of(0, 10));

        // 자유에 대한 성찰, 사회와 자유, 자유을 위한 변명(x3) — 총 5개 (자본와 나는 자유 미포함)
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getContent()).noneMatch(hit ->
                hit.title() != null && hit.title().contains("숨겨진"));
    }

    // ── suggest prefix 검증 ──────────────────────────────────────────────────

    @Test
    @DisplayName("suggest('자유', 8): 반환 결과가 모두 '자유'로 시작 (접두 일치)")
    void suggest_prefixOnly_jayu() {
        List<String> result = searchEngine.suggest("자유", 8);

        // 반환이 있으면 모두 '자유'로 시작해야 한다
        assertThat(result).allMatch(s -> s.startsWith("자유"),
                "suggest('자유') 결과에 접두 불일치 항목이 포함됨: " + result);
        // '사회와 자유'처럼 중간 매치는 포함되면 안 된다
        assertThat(result).noneMatch(s -> s.equals("사회와 자유"));
    }

    @Test
    @DisplayName("suggest('가', 8): 반환 결과가 모두 ㄱ+ㅏ 계열(가~깋)로 시작, '개인주의가 가능한가'·'국가이라는 형벌' 미포함")
    void suggest_prefixOnly_ga() {
        List<String> result = searchEngine.suggest("가", 8);

        // 자모 점진 매칭으로, 반환된 항목은 모두 ㄱ+ㅏ 계열(가~깋)로 시작해야 함
        assertThat(result).allMatch(s -> {
            char c = s.charAt(0);
            return c >= '가' && c <= '깋';
        }, "suggest('가') 결과에 ㄱ+ㅏ 계열이 아닌 항목이 포함됨: " + result);
        // 중간 매치 제목은 포함되면 안 됨
        assertThat(result).noneMatch(s -> s.equals("개인주의가 가능한가"));
        assertThat(result).noneMatch(s -> s.equals("국가이라는 형벌"));
        // '가능성에 관하여'는 ㄱ+ㅏ 계열로 시작하므로 포함되어야 함
        assertThat(result).anyMatch(s -> s.startsWith("가능성"));
    }

    // ── 자모 점진 접두 매칭 (jamo prefix) ──────────────────────────────────────

    @Test
    @DisplayName("suggest('가', 8): 결과 비어있지 않고, 모든 첫 글자가 ㄱ+ㅏ 계열 음절(가~깋), 감정·강함·각오 포함")
    void suggest_jamo_ga_matchesGaFamily() {
        List<String> result = searchEngine.suggest("가", 8);

        // '가'를 자모 분해하면 ㄱㅏ → '가능성...', '감정...', '강함...', '각오...' 등 ㄱ+ㅏ로 시작하는 제목 매칭
        assertThat(result).isNotEmpty();
        // 모든 결과의 첫 글자는 유니코드 '가'(44032) ~ '깋'(44032+27) 범위여야 한다
        assertThat(result).allMatch(s -> {
            char c = s.charAt(0);
            return c >= '가' && c <= '깋';
        }, "suggest('가') 결과에 ㄱ+ㅏ 계열이 아닌 항목 포함: " + result);
        // 자모 점진 매칭: '가'(ㄱㅏ)로 시작하는 '감정의 흐름'이 포함되어야 한다
        assertThat(result).anyMatch(s -> s.startsWith("감정"));
        // '개인주의가 가능한가'처럼 중간 매치는 포함되면 안 됨
        assertThat(result).noneMatch(s -> s.equals("개인주의가 가능한가"));
        assertThat(result).noneMatch(s -> s.equals("국가이라는 형벌"));
    }

    @Test
    @DisplayName("suggest('자유', 8): 자모 경로 회귀 — 결과 모두 '자유'로 시작")
    void suggest_jamo_jayu_regression() {
        List<String> result = searchEngine.suggest("자유", 8);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(s -> s.startsWith("자유"),
                "suggest('자유') 결과에 접두 불일치 항목 포함: " + result);
    }

    @Test
    @DisplayName("suggest('ㅈㅇ', 8): 초성 prefix — 반환 제목의 초성이 'ㅈㅇ'으로 시작")
    void suggest_chosung_prefix_jo() {
        List<String> result = searchEngine.suggest("ㅈㅇ", 8);

        // '사회와 자유'(ㅅㅎㅇ ㅈㅇ)는 초성 prefix 불일치이므로 제외
        assertThat(result).noneMatch(s -> s.equals("사회와 자유"));
        // 반환된 제목은 ㅈ 초성으로 시작해야 함 (자, 정 등)
        assertThat(result).allMatch(s -> {
            char first = s.charAt(0);
            // 한글 음절 초성 추출: (codepoint - 0xAC00) / 28 / 21
            if (first >= 0xAC00 && first <= 0xD7A3) {
                int chosungIdx = (first - 0xAC00) / 28 / 21;
                // ㅈ = index 12
                return chosungIdx == 12;
            }
            return true; // 비한글은 통과
        }, "suggest('ㅈㅇ') 결과에 ㅈ으로 시작하지 않는 항목이 있음: " + result);
    }

    // ── 연관성 기반 복합 정렬 (fix/search-suggest-relevance) ──────────────────

    @Test
    @DisplayName("suggest('자', 10): 인기(글 수 많은) 제목이 짧지만 덜 인기인 제목보다 앞에 위치")
    void suggest_relevanceSort_popularTitleBeforeLessPopular() {
        // '자유을 위한 변명'은 3개 포스트, '자본와 나'는 1개 포스트 → 인기 높은 게 먼저
        List<String> result = searchEngine.suggest("자", 10);

        assertThat(result).contains("자유을 위한 변명");
        assertThat(result).contains("자본와 나");
        int indexPopular = result.indexOf("자유을 위한 변명");
        int indexUnpopular = result.indexOf("자본와 나");
        assertThat(indexPopular).as("인기 제목('자유을 위한 변명', cnt=3)이 비인기 제목('자본와 나', cnt=1)보다 앞 인덱스여야 함")
                .isLessThan(indexUnpopular);
    }

    @Test
    @DisplayName("suggest('자유', 8): 회귀 — '자유을 위한 변명'(cnt=3)이 '자유에 대한 성찰'(cnt=1)보다 앞에 위치")
    void suggest_relevanceSort_jayu_popularFirst() {
        List<String> result = searchEngine.suggest("자유", 8);

        assertThat(result).contains("자유을 위한 변명");
        assertThat(result).contains("자유에 대한 성찰");
        int indexPopular = result.indexOf("자유을 위한 변명");
        int indexSingle = result.indexOf("자유에 대한 성찰");
        assertThat(indexPopular).as("'자유을 위한 변명'(cnt=3)이 '자유에 대한 성찰'(cnt=1)보다 앞이어야 함")
                .isLessThan(indexSingle);
    }
}
